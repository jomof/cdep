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
    private final ParameterExpression cdepExplodedRoot =
            new ParameterExpression("cdepExplodedRoot");
    private final ParameterExpression osxSysroot =
            new ParameterExpression("osxSysroot");
    private final ParameterExpression osxArchitectures =
            new ParameterExpression("osxArchitectures");
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

        // Lift assignments up to the highest correct scope
        functionTable = (FunctionTableExpression) new ReplaceAssignmentWithReferenceVisitor().visit(functionTable);
        functionTable = (FunctionTableExpression) new LiftToCommonAncestor().visit(functionTable);

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

        AssignmentExpression coordinateGroupId = new AssignmentExpression(
                "coordinate_group_id",
                new StringExpression(resolved.cdepManifestYml.coordinate.groupId)
        );

        AssignmentExpression coordinateArtifactId = new AssignmentExpression(
                "coordinate_artifact_id",
                new StringExpression(resolved.cdepManifestYml.coordinate.artifactId)
        );

        AssignmentExpression coordinateVersion = new AssignmentExpression(
                "coordinate_version",
                new StringExpression(resolved.cdepManifestYml.coordinate.version)
        );

        // Like, {root}/com.github.jomof/vectorial/1.0.0
        AssignmentExpression explodedArchiveFolder = new AssignmentExpression(
                "exploded_archive_folder",
                new InvokeFunctionExpression(
                        ExternalFunctionExpression.FILE_JOIN_SEGMENTS,
                        cdepExplodedRoot,
                        new ArrayExpression(
                                coordinateGroupId,
                                coordinateArtifactId,
                                coordinateVersion
                        )
                )
        );

        if (resolved.cdepManifestYml.android != null
                && resolved.cdepManifestYml.android.archives != null) {
            supported += "'Android' ";
            cases.put(new StringExpression("Android"),
                    buildAndroidStlTypeCase(resolved, explodedArchiveFolder, dependencies));
        }
        if (resolved.cdepManifestYml.iOS != null && resolved.cdepManifestYml.iOS.archives != null) {
            supported += "'Darwin' ";
            cases.put(new StringExpression("Darwin"),
                    buildDarwinPlatformCase(resolved, explodedArchiveFolder, dependencies));
        }
        Expression bool[] = new Expression[cases.size()];
        Expression expressions[] = new Expression[cases.size()];
        int i = 0;
        for (Map.Entry<Expression, Expression> entry : cases.entrySet()) {
            bool[i] = new InvokeFunctionExpression(
                    ExternalFunctionExpression.STRING_EQUALS,
                    targetPlatform,
                    entry.getKey());
            expressions[i] = entry.getValue();
            ++i;
        }

        IfSwitchExpression expression = new IfSwitchExpression(
                bool,
                expressions,
                new AbortExpression(
                        String.format("Target platform '%%s' is not supported by module '%s'. "
                                        + "Supported: %s",
                                resolved.cdepManifestYml.coordinate, supported), targetPlatform));

        return new FindModuleExpression(resolved.cdepManifestYml.coordinate, cdepExplodedRoot, targetPlatform,
                systemVersion, androidArchAbi, androidStlType, osxSysroot, osxArchitectures,
                expression);
    }

    private Expression buildDarwinPlatformCase(
            ResolvedManifest resolved,
            AssignmentExpression explodedArchiveFolder,
            Set<Coordinate> dependencies) throws MalformedURLException, URISyntaxException {

        // Something like iPhone10.2.sdk or iPhone.sdk
        AssignmentExpression osxSysrootSDKName =
                new AssignmentExpression(
                        "osx_sysroot_sdk_name",
                        new InvokeFunctionExpression(
                                ExternalFunctionExpression.FILE_GETNAME,
                                osxSysroot));

        // The position of the right-most dot
        AssignmentExpression lastDotPosition = new AssignmentExpression(
                "last_dot_position",
                new InvokeFunctionExpression(
                        ExternalFunctionExpression.STRING_LASTINDEXOF,
                        osxSysrootSDKName,
                        new StringExpression(".")));

        // Something like iPhone10.2 or iPhone
        AssignmentExpression combinedPlatformAndSDK =
                new AssignmentExpression(
                        "combined_platform_and_sdk",
                        new InvokeFunctionExpression(
                                ExternalFunctionExpression.STRING_SUBSTRING_BEGIN_END,
                                osxSysrootSDKName,
                                new IntegerExpression(0),
                                lastDotPosition));


        List<Expression> conditionList = new ArrayList<>();
        List<Expression> expressionList = new ArrayList<>();
        String supported = "";

        // Exact matches. For example, path ends with exactly iPhoneOS10.2
        // TODO:  Linter should verify that there is not duplicate exact platforms (ie platform+sdk)
        for (iOSArchive archive : resolved.cdepManifestYml.iOS.archives) {
            String platformSDK = archive.platform + archive.sdk;
            conditionList.add(new InvokeFunctionExpression(
                    ExternalFunctionExpression.STRING_EQUALS,
                    combinedPlatformAndSDK,
                    new StringExpression(platformSDK)
            ));
            expressionList.add(buildiosArchiveExpression(
                    resolved,
                    archive,
                    explodedArchiveFolder,
                    dependencies));

            supported += platformSDK + " ";
        }

        // If there was no exact match then do a startsWith match like, starts  with iPhone*
        // TODO: Need to match on the highest SDK version. This matches the first seen.
        for (iOSArchive archive : resolved.cdepManifestYml.iOS.archives) {
            conditionList.add(new InvokeFunctionExpression(
                    ExternalFunctionExpression.STRING_STARTSWITH,
                    combinedPlatformAndSDK,
                    new StringExpression(archive.platform.toString())
            ));
            expressionList.add(buildiosArchiveExpression(
                    resolved,
                    archive,
                    explodedArchiveFolder,
                    dependencies));
        }

        Expression notFound = new AbortExpression(
                String.format("OSX SDK '%%s' is not supported by module '%s'. Supported: %s",
                        resolved.cdepManifestYml.coordinate, supported), combinedPlatformAndSDK);

        return new IfSwitchExpression(
                conditionList,
                expressionList,
                notFound);
    }

    private Expression buildiosArchiveExpression(
            ResolvedManifest resolved,
            iOSArchive archive,
            AssignmentExpression explodedArchiveFolder,
            Set<Coordinate> dependencies) throws URISyntaxException, MalformedURLException {
        int archiveCount = 1;
        if (resolved.cdepManifestYml.archive != null) {
            ++archiveCount;
        }

        AssignmentExpression zipFolder = new AssignmentExpression(
                "zip_folder",
                new InvokeFunctionExpression(
                        ExternalFunctionExpression.FILE_JOIN_SEGMENTS,
                        explodedArchiveFolder,
                        new ArrayExpression(new StringExpression(archive.file))
                )
        );

        ModuleArchiveExpression archives[] = new ModuleArchiveExpression[archiveCount];
        archives[0] = new ModuleArchiveExpression(
                resolved.remote.toURI()
                        .resolve(".")
                        .resolve(archive.file)
                        .toURL(),
                archive.sha256,
                archive.size,
                new InvokeFunctionExpression(
                        ExternalFunctionExpression.FILE_JOIN_SEGMENTS,
                        zipFolder,
                        new ArrayExpression(new StringExpression(archive.include))),
                archive.include,
                archive.lib);

        if (resolved.cdepManifestYml.archive != null) {
            // This is the global zip file from the top level of the manifest.
            archives[1] = new ModuleArchiveExpression(
                    resolved.remote.toURI()
                            .resolve(".")
                            .resolve(resolved.cdepManifestYml.archive.file)
                            .toURL(),
                    resolved.cdepManifestYml.archive.sha256,
                    resolved.cdepManifestYml.archive.size,
                    new InvokeFunctionExpression(
                            ExternalFunctionExpression.FILE_JOIN_SEGMENTS,
                            zipFolder,
                            new ArrayExpression(new StringExpression("include"))),
                    "include",
                    null);
        }
        return new FoundiOSModuleExpression(
                archives,
                dependencies);
    }

    private Expression buildAndroidStlTypeCase(
            ResolvedManifest resolved,
            AssignmentExpression explodedArchiveFolder,
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
                return buildAndroidPlatformExpression(resolved, noRuntimeAndroids, explodedArchiveFolder, dependencies);
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
                    explodedArchiveFolder,
                    dependencies));
            cases.put(new StringExpression(stlType + "_static"), buildAndroidPlatformExpression(
                    resolved,
                    stlTypes.get(stlType),
                    explodedArchiveFolder,
                    dependencies));
        }

        Expression bool[] = new Expression[cases.size()];
        Expression expressions[] = new Expression[cases.size()];
        int i = 0;
        for (Map.Entry<Expression, Expression> entry : cases.entrySet()) {
            bool[i] = new InvokeFunctionExpression(
                    ExternalFunctionExpression.STRING_EQUALS,
                    androidStlType,
                    entry.getKey());
            expressions[i] = entry.getValue();
            ++i;
        }
        return new IfSwitchExpression(
                bool,
                expressions,
                new AbortExpression(
                        String.format("Android runtime '%%s' is not supported by module '%s'. Supported: %s",
                                resolved.cdepManifestYml.coordinate, runtimes), androidStlType));
    }

    private Expression buildAndroidPlatformExpression(
            ResolvedManifest resolved,
            List<AndroidArchive> androids,
            AssignmentExpression explodedArchiveFolder, // Parent of all .zip folders for this coordinate
            Set<Coordinate> dependencies) throws MalformedURLException, URISyntaxException {

        // If there's only one android left and it doesn't have a platform then this is
        // a header-only module.
        if (androids.size() == 1 && androids.get(0).platform == null) {
            return buildAndroidAbiExpression(resolved, androids, explodedArchiveFolder, dependencies);
        }

        Map<Integer, List<AndroidArchive>> grouped = new HashMap<>();
        for (AndroidArchive android : androids) {
            Integer platform = Integer.parseInt(android.platform);
            List<AndroidArchive> group = grouped.get(platform);
            if (group == null) {
                group = new ArrayList<>();
                grouped.put(platform, group);
            }
            group.add(android);
        }

        List<Integer> platforms = new ArrayList<>();
        platforms.addAll(grouped.keySet());
        Collections.sort(platforms);

        List<Expression> conditions = new ArrayList<>();
        List<Expression> expressions = new ArrayList<>();

        for (int platform : platforms) {
            conditions.add(0, new InvokeFunctionExpression(
                    ExternalFunctionExpression.INTEGER_GTE,
                    systemVersion,
                    new IntegerExpression(platform)
            ));
            expressions.add(0, buildAndroidAbiExpression(resolved, grouped.get(platform),
                    explodedArchiveFolder, dependencies));
        }
        return new IfSwitchExpression(
                conditions,
                expressions,
                new AbortExpression(
                        String.format("Android API level '%%s' is not supported by module '%s'",
                                resolved.cdepManifestYml.coordinate), systemVersion));
    }

    private Expression buildAndroidAbiExpression(
            ResolvedManifest resolved,
            List<AndroidArchive> androids,
            AssignmentExpression explodedArchiveFolder, // Parent of all .zip folders for this coordinate
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

        AssignmentExpression zipFolder = new AssignmentExpression(
                "zip_folder",
                new InvokeFunctionExpression(
                        ExternalFunctionExpression.FILE_JOIN_SEGMENTS,
                        explodedArchiveFolder,
                        new ArrayExpression(new StringExpression(android.file))
                )
        );

        ModuleArchiveExpression archives[] = new ModuleArchiveExpression[archiveCount];
        archives[0] = new ModuleArchiveExpression(
                resolved.remote.toURI()
                        .resolve(".")
                        .resolve(android.file)
                        .toURL(),
                android.sha256,
                android.size,
                new InvokeFunctionExpression(
                        ExternalFunctionExpression.FILE_JOIN_SEGMENTS,
                        zipFolder,
                        new ArrayExpression(new StringExpression(android.include))),
                android.include,
                android.lib);

        if (resolved.cdepManifestYml.archive != null) {
            // This is the global zip file from the top level of the manifest.
            archives[1] = new ModuleArchiveExpression(
                    resolved.remote.toURI()
                            .resolve(".")
                            .resolve(resolved.cdepManifestYml.archive.file)
                            .toURL(),
                    resolved.cdepManifestYml.archive.sha256,
                    resolved.cdepManifestYml.archive.size,
                    new InvokeFunctionExpression(
                            ExternalFunctionExpression.FILE_JOIN_SEGMENTS,
                            zipFolder,
                            new ArrayExpression(new StringExpression("include"))),
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
                    archives,
                    dependencies));
        }

        Expression prior = new AbortExpression(
                String.format("Android ABI '%%s' is not supported by module '%s'. Supported: %s",
                        resolved.cdepManifestYml.coordinate, supported), androidArchAbi);

        Expression bool[] = new Expression[cases.size()];
        Expression expressions[] = new Expression[cases.size()];
        int i = 0;
        for (Map.Entry<Expression, Expression> entry : cases.entrySet()) {
            bool[i] = new InvokeFunctionExpression(
                    ExternalFunctionExpression.STRING_EQUALS,
                    androidArchAbi,
                    entry.getKey());
            expressions[i] = entry.getValue();
            ++i;
        }
        return new IfSwitchExpression(
                bool,
                expressions,
                prior);
    }
}
