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

import static io.cdep.cdep.ast.finder.ExpressionBuilder.*;


public class FindModuleFunctionTableBuilder {

  private final Map<Coordinate, ResolvedManifest> manifests = new HashMap<>();
  private final ParameterExpression cdepExplodedRoot =
      parameter("cdep_exploded_root");
  private final ParameterExpression osxSysroot =
      parameter("osxSysroot");
  private final ParameterExpression osxArchitectures =
      parameter("osxArchitectures");
  private final ParameterExpression targetPlatform =
      parameter("targetPlatform");
  private final ParameterExpression androidArchAbi =
      parameter("androidArchAbi");
  private final ParameterExpression androidStlType =
      parameter("androidStlType");
  private final ParameterExpression systemVersion =
      parameter("systemVersion");

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
    functionTable = (FunctionTableExpression) new ReplaceAssignmentWithReferenceVisitor()
        .visit(functionTable);
    functionTable = (FunctionTableExpression) new LiftToCommonAncestor().visit(functionTable);

    // Check sanity of the function system
    new FindMultiplyReferencedArchives().visit(functionTable);

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

    AssignmentExpression coordinateGroupId = assign(
        "coordinate_group_id",
        string(resolved.cdepManifestYml.coordinate.groupId)
    );

    AssignmentExpression coordinateArtifactId = assign(
        "coordinate_artifact_id",
        string(resolved.cdepManifestYml.coordinate.artifactId)
    );

    AssignmentExpression coordinateVersion = assign(
        "coordinate_version",
        string(resolved.cdepManifestYml.coordinate.version)
    );

    // Like, {root}/com.github.jomof/vectorial/1.0.0
    AssignmentExpression explodedArchiveFolder = assign(
        "exploded_archive_folder",
        joinFileSegments(cdepExplodedRoot, coordinateGroupId,
            coordinateArtifactId, coordinateVersion)
    );

    if (resolved.cdepManifestYml.android != null
        && resolved.cdepManifestYml.android.archives != null) {
      supported += "'Android' ";
      cases.put(string("Android"),
          buildAndroidStlTypeCase(resolved, explodedArchiveFolder, dependencies));
    }
    if (resolved.cdepManifestYml.iOS != null && resolved.cdepManifestYml.iOS.archives != null) {
      supported += "'Darwin' ";
      cases.put(string("Darwin"),
          buildDarwinPlatformCase(resolved, explodedArchiveFolder, dependencies));
    }
    Expression bool[] = new Expression[cases.size()];
    Expression expressions[] = new Expression[cases.size()];
    int i = 0;
    for (Map.Entry<Expression, Expression> entry : cases.entrySet()) {
      bool[i] = eq(
          targetPlatform,
          entry.getKey());
      expressions[i] = entry.getValue();
      ++i;
    }

    IfSwitchExpression expression = ifSwitch(
        bool,
        expressions,
        abort(
            String.format("Target platform '%%s' is not supported by module '%s'. "
                    + "Supported: %s",
                resolved.cdepManifestYml.coordinate, supported), targetPlatform));

    return new FindModuleExpression(resolved.cdepManifestYml.coordinate, cdepExplodedRoot,
        targetPlatform,
        systemVersion, androidArchAbi, androidStlType, osxSysroot, osxArchitectures,
        expression);
  }

  private Expression buildDarwinPlatformCase(
      ResolvedManifest resolved,
      AssignmentExpression explodedArchiveFolder,
      Set<Coordinate> dependencies) throws MalformedURLException, URISyntaxException {

    // Something like iPhone10.2.sdk or iPhone.sdk
    AssignmentExpression osxSysrootSDKName =
        assign(
            "osx_sysroot_sdk_name",
            getFileName(osxSysroot));

    // The position of the right-most dot
    AssignmentExpression lastDotPosition = assign(
        "last_dot_position",
        lastIndexOfString(
            osxSysrootSDKName,
            "."));

    // Something like iPhone10.2 or iPhone
    AssignmentExpression combinedPlatformAndSDK =
        assign(
            "combined_platform_and_sdk",
            substring(
                osxSysrootSDKName,
                integer(0),
                lastDotPosition));

    List<Expression> conditionList = new ArrayList<>();
    List<Expression> expressionList = new ArrayList<>();
    String supported = "";

    // Exact matches. For example, path ends with exactly iPhoneOS10.2
    // TODO:  Linter should verify that there is not duplicate exact platforms (ie platform+sdk)
    for (iOSArchive archive : resolved.cdepManifestYml.iOS.archives) {
      String platformSDK = archive.platform + archive.sdk;
      conditionList.add(eq(
          combinedPlatformAndSDK,
          string(platformSDK)
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
      conditionList.add(stringStartsWith(
          combinedPlatformAndSDK,
          string(archive.platform.toString())
      ));
      expressionList.add(buildiosArchiveExpression(
          resolved,
          archive,
          explodedArchiveFolder,
          dependencies));
    }

    Expression notFound = abort(
        String.format("OSX SDK '%%s' is not supported by module '%s'. Supported: %s",
            resolved.cdepManifestYml.coordinate, supported), combinedPlatformAndSDK);

    return ifSwitch(conditionList, expressionList, notFound);
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

    ModuleArchiveExpression archives[] = new ModuleArchiveExpression[archiveCount];
    archives[0] = archive(
        resolved.remote.toURI()
            .resolve(".")
            .resolve(archive.file)
            .toURL(),
        archive.sha256,
        archive.size,
        archive.include == null ? null
            : joinFileSegments(explodedArchiveFolder, archive.file, archive.include),
        archive.lib == null ? null
            : joinFileSegments(explodedArchiveFolder, archive.file, "lib", archive.lib));

    if (resolved.cdepManifestYml.archive != null) {
      // This is the global zip file from the top level of the manifest.
      archives[1] = archive(
          resolved.remote.toURI()
              .resolve(".")
              .resolve(resolved.cdepManifestYml.archive.file)
              .toURL(),
          resolved.cdepManifestYml.archive.sha256,
          resolved.cdepManifestYml.archive.size,
          joinFileSegments(explodedArchiveFolder,
              resolved.cdepManifestYml.archive.file, "include"),
          null);
    }
    return module(archives, dependencies);
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
        return buildAndroidPlatformExpression(resolved, noRuntimeAndroids, explodedArchiveFolder,
            dependencies);
      }
      // There are some android sub modules with runtime and some without
      return abort(
          String.format("Runtime is on some android submodules but not other in module '%s'",
              resolved.cdepManifestYml.coordinate));
    }

    Map<Expression, Expression> cases = new HashMap<>();
    String runtimes = "";
    for (String stlType : stlTypes.keySet()) {
      runtimes += stlType + " ";
      cases.put(string(stlType + "_shared"), buildAndroidPlatformExpression(
          resolved,
          stlTypes.get(stlType),
          explodedArchiveFolder,
          dependencies));
      cases.put(string(stlType + "_static"), buildAndroidPlatformExpression(
          resolved,
          stlTypes.get(stlType),
          explodedArchiveFolder,
          dependencies));
    }

    Expression bool[] = new Expression[cases.size()];
    Expression expressions[] = new Expression[cases.size()];
    int i = 0;
    for (Map.Entry<Expression, Expression> entry : cases.entrySet()) {
      bool[i] = eq(
          androidStlType,
          entry.getKey());
      expressions[i] = entry.getValue();
      ++i;
    }
    return ifSwitch(
        bool,
        expressions,
        abort(
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
      conditions.add(0, gte(systemVersion, platform));
      expressions.add(0, buildAndroidAbiExpression(resolved, grouped.get(platform),
          explodedArchiveFolder, dependencies));
    }
    return ifSwitch(
        conditions,
        expressions,
        abort(
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
    AndroidArchive archive = androids.get(0);
    int archiveCount = 1;
    if (resolved.cdepManifestYml.archive != null) {
      ++archiveCount;
    }

    Map<Expression, Expression> cases = new HashMap<>();
    String supported = "";
    String abis[] = archive.abis;
    if (abis == null) {
      abis = AndroidAbi.getNames();
    }
    for (String abi : abis) {
      supported += abi + " ";
      ModuleArchiveExpression archives[] = new ModuleArchiveExpression[archiveCount];
      archives[0] = archive(
          resolved.remote.toURI()
              .resolve(".")
              .resolve(archive.file)
              .toURL(),
          archive.sha256,
          archive.size,
          archive.include == null ? null
              : joinFileSegments(explodedArchiveFolder, archive.file, archive.include),
          archive.lib == null ? null
              : joinFileSegments(explodedArchiveFolder, archive.file, "lib", abi, archive.lib));

      if (resolved.cdepManifestYml.archive != null) {
        // This is the global zip file from the top level of the manifest.
        archives[1] = archive(
            resolved.remote.toURI()
                .resolve(".")
                .resolve(resolved.cdepManifestYml.archive.file)
                .toURL(),
            resolved.cdepManifestYml.archive.sha256,
            resolved.cdepManifestYml.archive.size,
            joinFileSegments(explodedArchiveFolder,
                resolved.cdepManifestYml.archive.file, "include"),
            null);
      }
      cases.put(string(abi), module(archives, dependencies));
    }

    Expression prior = abort(
        String.format("Android ABI '%%s' is not supported by module '%s'. Supported: %s",
            resolved.cdepManifestYml.coordinate, supported), androidArchAbi);

    Expression bool[] = new Expression[cases.size()];
    Expression expressions[] = new Expression[cases.size()];
    int i = 0;
    for (Map.Entry<Expression, Expression> entry : cases.entrySet()) {
      bool[i] = eq(
          androidArchAbi,
          entry.getKey());
      expressions[i] = entry.getValue();
      ++i;
    }
    return ifSwitch(bool, expressions, prior);
  }
}
