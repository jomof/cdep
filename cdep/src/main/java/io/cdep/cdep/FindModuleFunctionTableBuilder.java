/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package io.cdep.cdep;

import io.cdep.cdep.ast.finder.*;
import io.cdep.cdep.ast.service.ResolvedManifest;
import io.cdep.cdep.utils.CoordinateUtils;
import io.cdep.cdep.yml.cdepmanifest.Android;

import io.cdep.cdep.yml.cdepmanifest.AndroidArchive;
import io.cdep.cdep.yml.cdepmanifest.Archive;
import io.cdep.cdep.yml.cdepmanifest.HardNameDependency;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;

public class FindModuleFunctionTableBuilder {

    private final Map<Coordinate, ResolvedManifest> manifests = new HashMap<>();
    private final ParameterExpression targetPlatform =
        new ParameterExpression("targetPlatform");
    private final ParameterExpression androidArchAbi =
        new ParameterExpression("androidArchAbi");
    private final ParameterExpression androidStlType =
        new ParameterExpression("androidStlType");
    private final ParameterExpression systemVersion =
        new ParameterExpression("systemVersion");

    public void addManifest(ResolvedManifest resolved) {
        manifests.put(resolved.cdepManifestYml.coordinate, resolved);
    }

    public FunctionTableExpression build() throws MalformedURLException, URISyntaxException {
        FunctionTableExpression functionTable = new FunctionTableExpression();

        // Build module lookup findFunctions
        for (ResolvedManifest resolved : manifests.values()) {
            assert resolved.cdepManifestYml.coordinate != null;
            functionTable.findFunctions.put(resolved.cdepManifestYml.coordinate,
                buildFindModule(resolved));
        }

        // Build examples
        for (ResolvedManifest resolved : manifests.values()) {
            if (resolved.cdepManifestYml.example == null) {
                continue;
            }
            functionTable.examples.put(resolved.cdepManifestYml.coordinate,
                new ExampleExpression(resolved.cdepManifestYml.example));
        }
        return functionTable;
    }

    private FindModuleExpression buildFindModule(ResolvedManifest resolved)
        throws MalformedURLException, URISyntaxException {

        Map<String, Expression> cases = new HashMap<>();
        String supported = "";
        Set<Coordinate> dependencies = new HashSet<>();
        if (resolved.cdepManifestYml.dependencies != null) {
            for (HardNameDependency dependency : resolved.cdepManifestYml.dependencies) {
                Coordinate coordinate = CoordinateUtils.tryParse(dependency.compile);
                dependencies.add(coordinate);
            }
        }
        if (resolved.cdepManifestYml.android != null) {
            supported += "'Android' ";
            cases.put("Android",
                buildAndroidStlTypeCase(resolved, dependencies));
        }
        CaseExpression expression = new CaseExpression(
            targetPlatform,
            cases,
            new AbortExpression(
                String.format("Target platform '%%s' is not supported by module '%s'. "
                        + "Supported: %s",
                    resolved.cdepManifestYml.coordinate, supported), targetPlatform));

        return new FindModuleExpression(resolved.cdepManifestYml.coordinate, targetPlatform,
            systemVersion, androidArchAbi, androidStlType, expression);
    }

    private Expression buildAndroidStlTypeCase(
        ResolvedManifest resolved,
        Set<Coordinate> dependencies) throws MalformedURLException, URISyntaxException {

        // Gather up the runtime names
        Map<String, List<AndroidArchive>> stlTypes = new HashMap<>();
        assert resolved.cdepManifestYml.android != null;
        for (AndroidArchive android : resolved.cdepManifestYml.android.archives) {
            List<AndroidArchive> androids = stlTypes.get(android.runtime);
            if (androids == null) {
                androids = new ArrayList<>();
                stlTypes.put(android.runtime, androids);
            }
            androids.add(android);
        }

        List<AndroidArchive> noRuntimeAndroids = stlTypes.get(null);
        if (noRuntimeAndroids != null) {
            if (stlTypes.size() == 1) {
                // If there are no runtimes, then skip the runtime check. This is likely a
                // header-only module.
                return buildAndroidPlatformExpression(resolved, noRuntimeAndroids, dependencies);
            }
            // There are some android sub modules with runtime and some without
            return new AbortExpression(
                String.format("Runtime is on some android submodules but not other in module '%s'",
                    resolved.cdepManifestYml.coordinate));
        }

        Map<String, Expression> cases = new HashMap<>();
        String runtimes = "";
        for (String stlType : stlTypes.keySet()) {
            runtimes += stlType + " ";
            cases.put(stlType + "_shared", buildAndroidPlatformExpression(
                resolved,
                stlTypes.get(stlType),
                dependencies));
            cases.put(stlType + "_static", buildAndroidPlatformExpression(
                resolved,
                stlTypes.get(stlType),
                dependencies));
        }

        return new CaseExpression(
            androidStlType,
            cases,
            new AbortExpression(
                String.format("Android runtime '%%s' is not supported by module '%s'. Supported: %s",
                    resolved.cdepManifestYml.coordinate, runtimes), androidStlType));
    }

    private Expression buildAndroidPlatformExpression(
        ResolvedManifest resolved,
        List<AndroidArchive> androids,
        Set<Coordinate> dependencies) throws MalformedURLException, URISyntaxException {

        // If there's only one android left and it doesn't have a platform then this is
        // a header-only module.
        if (androids.size() == 1 && androids.get(0).platform == null) {
            return returnOnly(resolved, androids, dependencies);
        }

        Map<Long, List<AndroidArchive>> grouped = new HashMap<>();
        for (AndroidArchive android : androids) {
            Long platform = Long.parseLong(android.platform);
            List<AndroidArchive> group = grouped.get(platform);
            if (group == null) {
                group = new ArrayList<>();
                grouped.put(platform, group);
            }
            group.add(android);
        }

        List<Long> platforms = new ArrayList<>();
        platforms.addAll(grouped.keySet());
        Collections.sort(platforms);

        Expression prior = new AbortExpression(
            String.format("Android API level '%%s' is not supported by module '%s'",
                resolved.cdepManifestYml.coordinate), systemVersion);
        for (long platform : platforms) {
            prior = new IfGreaterThanOrEqualExpression(
                systemVersion,
                new LongConstantExpression(platform),
                returnOnly(resolved, grouped.get(platform), dependencies),
                prior);
        }
        return prior;
    }

    private FoundModuleExpression returnOnly(
            ResolvedManifest resolved,
            List<AndroidArchive> androids,
            Set<Coordinate> dependencies)
        throws URISyntaxException, MalformedURLException {
        if (androids.size() != 1) {
            throw new RuntimeException(String.format(
                "Expected only one android archive upon reaching ABI level. There were %s.",
                androids.size()));
        }
        AndroidArchive android = androids.get(0);
        ModuleArchive archives[] = new ModuleArchive[1];
        archives[0] = new ModuleArchive(
            resolved.remote.toURI()
                .resolve(".")
                .resolve(android.file)
                .toURL(),
            android.sha256,
            android.size);
        String include = android.include;
        return new FoundModuleExpression(resolved.cdepManifestYml.coordinate, archives,
            include, android.lib, dependencies);
    }
}
