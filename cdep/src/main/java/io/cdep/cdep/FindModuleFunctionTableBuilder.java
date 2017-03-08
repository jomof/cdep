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
import io.cdep.cdep.generator.AndroidAbi;
import io.cdep.cdep.resolver.ResolvedManifest;
import io.cdep.cdep.utils.CoordinateUtils;
import io.cdep.cdep.yml.cdepmanifest.AndroidArchive;
import io.cdep.cdep.yml.cdepmanifest.HardNameDependency;
import io.cdep.cdep.yml.cdepmanifest.iOSArchive;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;

public class FindModuleFunctionTableBuilder {

    private final Map<Coordinate, ResolvedManifest> manifests = new HashMap<>();
    private final ParameterExpression iOSPlatform =
            new ParameterExpression("iOSPlatform");
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

        Map<Expression, Expression> cases = new HashMap<>();
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
            cases.put(new StringExpression("Android"),
                buildAndroidStlTypeCase(resolved, dependencies));
        }
        if (resolved.cdepManifestYml.iOS != null) {
            supported += "'Darwin' ";
            cases.put(new StringExpression("Darwin"),
                    buildDarwinPlatformCase(resolved, dependencies));
        }
        CaseExpression expression = new CaseExpression(
            targetPlatform,
            cases,
            new AbortExpression(
                String.format("Target platform '%%s' is not supported by module '%s'. "
                        + "Supported: %s",
                    resolved.cdepManifestYml.coordinate, supported), targetPlatform));

        return new FindModuleExpression(resolved.cdepManifestYml.coordinate, targetPlatform,
                systemVersion, androidArchAbi, androidStlType, iOSPlatform, expression);
    }

    private Expression buildDarwinPlatformCase(
            ResolvedManifest resolved,
            Set<Coordinate> dependencies) throws MalformedURLException, URISyntaxException {

        // Gather up the platform names
        Map<Expression, Expression> platforms = new HashMap<>();
        assert resolved.cdepManifestYml.iOS != null;
        String supported = "";
        for (iOSArchive archive : resolved.cdepManifestYml.iOS.archives) {
            Expression found = platforms.get(archive.platform);
            if (found != null) {
                throw new RuntimeException(
                        String.format("iOS platform '%s' seen in multiple archives"));
            }

            supported += archive.platform.toString() + " ";
            platforms.put(new iOSPlatformExpression(archive.platform),
                    buildiosArchiveExpression(
                            resolved,
                            archive,
                            dependencies));
        }

        return new CaseExpression(iOSPlatform,
                platforms,
                new AbortExpression(
                        String.format("iOS platform '%%s' is not supported by module '%s'. Supported: %s",
                                resolved.cdepManifestYml.coordinate, supported), iOSPlatform));
    }

    private Expression buildiosArchiveExpression(
            ResolvedManifest resolved,
            iOSArchive archive,
            Set<Coordinate> dependencies) throws URISyntaxException, MalformedURLException {
        int archiveCount = 1;
        if (resolved.cdepManifestYml.archive != null) {
            ++archiveCount;
        }
        ModuleArchive archives[] = new ModuleArchive[archiveCount];
        archives[0] = new ModuleArchive(
                resolved.remote.toURI()
                        .resolve(".")
                        .resolve(archive.file)
                        .toURL(),
                archive.sha256,
                archive.size,
                archive.include,
                archive.lib);

        if (resolved.cdepManifestYml.archive != null) {
            // This is the global zip file from the top level of the manifest.
            archives[1] = new ModuleArchive(
                    resolved.remote.toURI()
                            .resolve(".")
                            .resolve(resolved.cdepManifestYml.archive.file)
                            .toURL(),
                    resolved.cdepManifestYml.archive.sha256,
                    resolved.cdepManifestYml.archive.size,
                    "include",
                    null);
        }
        return new FoundiOSModuleExpression(
                resolved.cdepManifestYml.coordinate,
                archives,
                dependencies);
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

        Map<Expression, Expression> cases = new HashMap<>();
        String runtimes = "";
        for (String stlType : stlTypes.keySet()) {
            runtimes += stlType + " ";
            cases.put(new StringExpression(stlType + "_shared"), buildAndroidPlatformExpression(
                resolved,
                stlTypes.get(stlType),
                dependencies));
            cases.put(new StringExpression(stlType + "_static"), buildAndroidPlatformExpression(
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
            return buildAndroidAbiExpression(resolved, androids, dependencies);
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
                buildAndroidAbiExpression(resolved, grouped.get(platform), dependencies),
                prior);
        }
        return prior;
    }

    private Expression buildAndroidAbiExpression(
        ResolvedManifest resolved,
        List<AndroidArchive> androids,
        Set<Coordinate> dependencies) throws MalformedURLException, URISyntaxException {
        if (androids.size() != 1) {
            throw new RuntimeException(String.format(
                "Expected only one android archive upon reaching ABI level. There were %s.",
                androids.size()));
        }
        AndroidArchive android = androids.get(0);
        int archiveCount = 1;
        if (resolved.cdepManifestYml.archive != null) {
            ++archiveCount;
        }
        ModuleArchive archives[] = new ModuleArchive[archiveCount];
        archives[0] = new ModuleArchive(
            resolved.remote.toURI()
                .resolve(".")
                .resolve(android.file)
                .toURL(),
            android.sha256,
            android.size,
            android.include,
            android.lib);

        if (resolved.cdepManifestYml.archive != null) {
            // This is the global zip file from the top level of the manifest.
            archives[1] = new ModuleArchive(
                resolved.remote.toURI()
                    .resolve(".")
                    .resolve(resolved.cdepManifestYml.archive.file)
                    .toURL(),
                resolved.cdepManifestYml.archive.sha256,
                resolved.cdepManifestYml.archive.size,
                "include",
                null);
        }

        Map<Expression, Expression> cases = new HashMap<>();
        String supported = "";
        String abis[] = android.abis;
        if (abis == null) {
            abis = AndroidAbi.getNames();
        }
        for (String abi : abis) {
            supported += abi + " ";
            cases.put(new StringExpression(abi), new FoundAndroidModuleExpression(
                resolved.cdepManifestYml.coordinate,
                archives,
                dependencies));
        }

        Expression prior = new AbortExpression(
            String.format("Android ABI '%%s' is not supported by module '%s'. Supported: %s",
                resolved.cdepManifestYml.coordinate, supported), androidArchAbi);

        return new CaseExpression(
            androidArchAbi,
            cases,
            prior);
    }
}
