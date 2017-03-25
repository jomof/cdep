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
import io.cdep.cdep.utils.StringUtils;
import io.cdep.cdep.yml.cdepmanifest.*;

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
    functionTable = (FunctionTableExpression) new ReplaceAssignmentWithReference()
        .visit(functionTable);
    functionTable = (FunctionTableExpression) new LiftToCommonAncestor().visit(functionTable);

    // Check sanity of the function system
    new FindMultiplyReferencedArchives().visit(functionTable);

    return functionTable;
  }

  private FindModuleExpression buildFindModule(ResolvedManifest resolved)
      throws MalformedURLException, URISyntaxException {

    Map<Expression, Expression> cases = new HashMap<>();
    Set<Coordinate> dependencies = new HashSet<>();
    CDepManifestYml manifest = resolved.cdepManifestYml;
    if (manifest.dependencies != null) {
      for (HardNameDependency dependency : manifest.dependencies) {
        Coordinate coordinate = CoordinateUtils.tryParse(dependency.compile);
        dependencies.add(coordinate);
      }
    }

    AssignmentExpression coordinateGroupId = assign(
        "coordinate_group_id",
        string(manifest.coordinate.groupId)
    );

    AssignmentExpression coordinateArtifactId = assign(
        "coordinate_artifact_id",
        string(manifest.coordinate.artifactId)
    );

    AssignmentExpression coordinateVersion = assign(
        "coordinate_version",
        string(manifest.coordinate.version)
    );

    AssignmentExpression explodedArchiveTail = assign(
        "exploded_archive_tail",
        joinFileSegments(coordinateGroupId, coordinateArtifactId, coordinateVersion)
    );

    // Like, {root}/com.github.jomof/vectorial/1.0.0
    AssignmentExpression explodedArchiveFolder = assign(
        "exploded_archive_folder",
        joinFileSegments(cdepExplodedRoot, explodedArchiveTail)
    );

    Set<String> supported = new HashSet<>();
    boolean headerOnly = true;
    if (manifest.android != null
        && manifest.android.archives != null) {
      headerOnly = false;
      supported.add("Android");
      cases.put(string("Android"),
          buildAndroidStlTypeCase(resolved, explodedArchiveFolder, dependencies));
    }
    if (manifest.iOS != null && manifest.iOS.archives != null) {
      headerOnly = false;
      supported.add("Darwin");
      cases.put(string("Darwin"),
          buildDarwinPlatformCase(resolved, explodedArchiveFolder, dependencies));
    }
    if (manifest.linux != null && manifest.linux.archives != null && manifest.linux.archives.length > 0) {
      headerOnly = false;
      supported.add("Linux");
      cases.put(string("Linux"),
          buildSingleLinuxArchiveResolution(
              resolved,
              manifest.linux.archives[0],
              explodedArchiveFolder,
              dependencies));
    }
    if (headerOnly && manifest.archive != null) {
      Expression module = buildSingleArchiveResolution(
          resolved,
          manifest.archive,
          explodedArchiveFolder,
          dependencies);
      supported.add("Android");
      supported.add("Darwin");
      supported.add("Linux");
      cases.put(string("Android"), module);
      cases.put(string("Darwin"), module);
      cases.put(string("Linux"), module);
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

    AbortExpression abort;
    if (supported.size() > 0) {
      abort = abort(
          String.format("Target platform '%%s' is not supported by module '%s'. "
                  + "Supported: %s",
              manifest.coordinate, StringUtils.joinOn(" ", supported)), targetPlatform);
    } else {
      throw new RuntimeException(String.format("Module '%s' doesn't support any platforms.",
          manifest.coordinate));
    }

    IfSwitchExpression expression = ifSwitch(bool, expressions, abort);

    return new FindModuleExpression(manifest.coordinate, cdepExplodedRoot,
        targetPlatform,
        systemVersion, androidArchAbi, androidStlType, osxSysroot, osxArchitectures,
        expression);
  }

  private Expression buildSingleLinuxArchiveResolution(
      ResolvedManifest resolved,
      LinuxArchive archive,
      AssignmentExpression explodedArchiveFolder,
      Set<Coordinate> dependencies) throws URISyntaxException, MalformedURLException {
    if (archive.file == null || archive.sha256 == null || archive.size == null || archive.include == null) {
      return abort(String.format("Archive in %s was malformed", resolved.remote));
    }
    return module(new ModuleArchiveExpression[]{archive(
        resolved.remote.toURI()
            .resolve(".")
            .resolve(archive.file)
            .toURL(),
        archive.sha256,
        archive.size,
        archive.include,
        joinFileSegments(explodedArchiveFolder, archive.file, archive.include),
        null,
        null)}, dependencies);
  }

  private Expression buildSingleArchiveResolution(
      ResolvedManifest resolved,
      Archive archive,
      AssignmentExpression explodedArchiveFolder,
      Set<Coordinate> dependencies) throws URISyntaxException, MalformedURLException {
    if (archive.file == null || archive.sha256 == null || archive.size == null || archive.include == null) {
      return abort(String.format("Archive in %s was malformed", resolved.remote));
    }
    return module(new ModuleArchiveExpression[]{archive(
        resolved.remote.toURI()
            .resolve(".")
            .resolve(archive.file)
            .toURL(),
        archive.sha256,
        archive.size,
        archive.include,
        joinFileSegments(explodedArchiveFolder, archive.file, archive.include),
        null,
        null)}, dependencies);
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
    return buildiosArchitectureSwitch(
        resolved,
        resolved.cdepManifestYml.iOS.archives,
        explodedArchiveFolder,
        combinedPlatformAndSDK,
        dependencies);
  }

  private Expression buildiosArchitectureSwitch(
      ResolvedManifest resolved,
      iOSArchive archive[],
      AssignmentExpression explodedArchiveFolder,
      AssignmentExpression combinedPlatformAndSDK,
      Set<Coordinate> dependencies) throws MalformedURLException, URISyntaxException {
    Map<iOSArchitecture, List<iOSArchive>> grouped = groupByArchitecture(archive);
    List<Expression> conditions = new ArrayList<>();
    List<Expression> expressions = new ArrayList<>();
    String supported = "";
    for (iOSArchitecture architecture : grouped.keySet()) {
      conditions.add(arrayHasOnlyElement(osxArchitectures, string(architecture.toString())));
      expressions.add(buildiOSPlatformSdkSwitch(
          resolved,
          grouped.get(architecture),
          explodedArchiveFolder,
          combinedPlatformAndSDK,
          architecture,
          dependencies));

      supported += " " + architecture.toString();
    }
    return ifSwitch(conditions, expressions, abort(
        String.format("OSX architecture '%%s' is not supported by module '%s'. Supported: %s",
            resolved.cdepManifestYml.coordinate, supported), osxArchitectures));
  }

  private Expression buildiOSPlatformSdkSwitch(
      ResolvedManifest resolved,
      List<iOSArchive> archives,
      AssignmentExpression explodedArchiveFolder,
      AssignmentExpression combinedPlatformAndSDK,
      iOSArchitecture architecture,
      Set<Coordinate> dependencies) throws MalformedURLException, URISyntaxException {
    List<Expression> conditionList = new ArrayList<>();
    List<Expression> expressionList = new ArrayList<>();
    String supported = "";

    // Exact matches. For example, path ends with exactly iPhoneOS10.2
    // TODO:  Linter should verify that there is not duplicate exact platforms (ie platform+sdk)
    for (iOSArchive archive : archives) {
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
        String.format(
            "OSX SDK '%%s' is not supported by module '%s' and architecture '%s'. Supported: %s",
            resolved.cdepManifestYml.coordinate, architecture, supported), combinedPlatformAndSDK);

    return ifSwitch(conditionList, expressionList, notFound);
  }

  private Map<iOSArchitecture, List<iOSArchive>> groupByArchitecture(iOSArchive archives[]) {
    Map<iOSArchitecture, List<iOSArchive>> result = new HashMap<>();
    for (iOSArchive archive : archives) {
      List<iOSArchive> list = result.get(archive.architecture);
      if (list == null) {
        list = new ArrayList<>();
        result.put(archive.architecture, list);
      }
      list.add(archive);
    }
    return result;
  }

  private Expression buildiosArchiveExpression(
      ResolvedManifest resolved,
      iOSArchive archive,
      AssignmentExpression explodedArchiveFolder,
      Set<Coordinate> dependencies) throws URISyntaxException, MalformedURLException {
    int archiveCount = 1;
    CDepManifestYml manifest = resolved.cdepManifestYml;
    if (manifest.archive != null) {
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
        archive.include,
        archive.include == null ? null
            : joinFileSegments(explodedArchiveFolder, archive.file, archive.include),
        archive.lib == null ? null : "lib/" + archive.lib,
        archive.lib == null ? null
            : joinFileSegments(explodedArchiveFolder, archive.file, "lib", archive.lib));

    if (manifest.archive != null) {
      // This is the global zip file from the top level of the manifest.
      archives[1] = archive(
          resolved.remote.toURI()
              .resolve(".")
              .resolve(manifest.archive.file)
              .toURL(),
          manifest.archive.sha256,
          manifest.archive.size,
          manifest.archive.include,
          joinFileSegments(explodedArchiveFolder,
              manifest.archive.file, manifest.archive.include),
          null,
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
      throw new RuntimeException(
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
    CDepManifestYml manifest = resolved.cdepManifestYml;
    if (manifest.archive != null) {
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
          archive.include,
          archive.include == null ? null
              : joinFileSegments(explodedArchiveFolder, archive.file, archive.include),
          archive.lib == null ? null : "lib/" + archive.lib,
          archive.lib == null ? null
              : joinFileSegments(explodedArchiveFolder, archive.file, "lib", abi, archive.lib));

      if (manifest.archive != null) {
        // This is the global zip file from the top level of the manifest.
        archives[1] = archive(
            resolved.remote.toURI()
                .resolve(".")
                .resolve(manifest.archive.file)
                .toURL(),
            manifest.archive.sha256,
            manifest.archive.size,
            manifest.archive.include,
            joinFileSegments(explodedArchiveFolder,
                manifest.archive.file, manifest.archive.include),
            null,
            null);
      }
      cases.put(string(abi), module(archives, dependencies));
    }

    Expression prior = abort(
        String.format("Android ABI '%%s' is not supported by module '%s'. Supported: %s",
            manifest.coordinate, supported), androidArchAbi);

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
