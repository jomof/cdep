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
package io.cdep;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;
import io.cdep.cdep.CheckLocalFileSystemIntegrity;
import io.cdep.cdep.FindModuleFunctionTableBuilder;
import io.cdep.cdep.StubCheckLocalFileSystemIntegrity;
import io.cdep.cdep.ast.finder.FunctionTableExpression;
import io.cdep.cdep.generator.CMakeExamplesGenerator;
import io.cdep.cdep.generator.CMakeGenerator;
import io.cdep.cdep.generator.GeneratorEnvironment;
import io.cdep.cdep.generator.GeneratorEnvironmentUtils;
import io.cdep.cdep.resolver.ResolutionScope;
import io.cdep.cdep.resolver.ResolvedManifest;
import io.cdep.cdep.resolver.Resolver;
import io.cdep.cdep.utils.*;
import io.cdep.cdep.yml.cdep.BuildSystem;
import io.cdep.cdep.yml.cdep.CDepYml;
import io.cdep.cdep.yml.cdep.SoftNameDependency;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;
import io.cdep.cdep.yml.cdepmanifest.CreateCDepManifestYmlString;
import io.cdep.cdep.yml.cdepmanifest.MergeCDepManifestYmls;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.cdep.cdep.utils.Invariant.*;

public class CDep {

  final private static String EXAMPLE_COORDINATE = "com.github.jomof:boost:1.0.63-rev21";
  private PrintStream out = System.out;

  @NotNull
  private File workingFolder = new File(".");
  @Nullable
  private File downloadFolder = null;
  @Nullable
  private CDepYml config = null;
  @Nullable
  private File configFile = null;

  CDep(PrintStream out) {
    this.out = out;
  }

  public static int main(@NotNull String[] args)
      throws IOException, URISyntaxException, NoSuchAlgorithmException {
    try {
      new CDep(System.out).go(args);
    } catch (Throwable e) {
      e.printStackTrace();
      return Integer.MIN_VALUE;
    }
    return 0;
  }

  @NotNull
  private static FunctionTableExpression getFunctionTableExpression(GeneratorEnvironment environment, @NotNull SoftNameDependency
      dependencies[]) throws IOException, URISyntaxException, NoSuchAlgorithmException {
    FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
    Resolver resolver = new Resolver(environment);
    ResolutionScope scope = resolver.resolveAll(dependencies);
    for (String name : scope.getResolutions()) {
      ResolutionScope.Resolution resolved = scope.getResolution(name);
      require(resolved instanceof ResolutionScope.FoundManifestResolution, "Could not resolve %s", name);
      ResolutionScope.FoundManifestResolution found = (ResolutionScope.FoundManifestResolution) resolved;
      builder.addManifest(found.resolved);
    }
    return builder.build();
  }

  /**
   * Return the first string after matching one of the arguments. Argument and strign are removed
   * from the list.
   */

  @NotNull
  static private List<String> eatStringArgument(
      String shortArgument, String longArgument, @NotNull List<String> args) {

    boolean takeNext = false;
    List<String> result = new ArrayList<>();
    for (int i = 0; i < args.size(); ++i) {
      if (takeNext) {
        result.add(args.get(i));
        takeNext = false;
        args.set(i, null);
      } else if (args.get(i).equals(longArgument) || args.get(i).equals(shortArgument)) {
        takeNext = true;
        args.set(i, null);
      }
    }
    args.removeAll(Collections.<String>singleton(null));
    return result;
  }

  void go(@NotNull String[] argArray) throws IOException, URISyntaxException, NoSuchAlgorithmException {
    List<String> args = new ArrayList<>();
    for (int i = 0; i < argArray.length; ++i) {
      args.add(argArray[i]);
    }

    if (!handleHelp(args)) {
      return;
    }
    if (!handleVersion(args)) {
      return;
    }
    handleWorkingFolder(args);
    handleDownloadFolder(args);
    if (handleWrapper(args)) {
      return;
    }
    if (handleShow(args)) {
      return;
    }
    if (handleLint(args)) {
      return;
    }
    if (handleMerge(args)) {
      return;
    }
    if (handleFetch(args)) {
      return;
    }
    if (!handleReadCDepYml()) {
      return;
    }
    if (handleCreate(args)) {
      return;
    }
    if (handleRedownload(args)) {
      return;
    }

    handleGenerateScript();
  }

  private void runBuilders(GeneratorEnvironment environment, @NotNull FunctionTableExpression table)
      throws IOException {
    for (BuildSystem buildSystem : notNull(config).builders) {
      switch (buildSystem) {
        case cmake:
          new CMakeGenerator(environment, table).generate();
          break;
        case cmakeExamples:
          new CMakeExamplesGenerator(environment).generate(table);
          break;
        default:
          throw new RuntimeException("Unknown builder: " + buildSystem);
      }
    }
  }

  private boolean handleRedownload(@NotNull List<String> args)
      throws IOException, URISyntaxException, NoSuchAlgorithmException {
    if (args.size() > 0 && "redownload".equals(args.get(0))) {
      GeneratorEnvironment environment = getGeneratorEnvironment(true, false);
      FunctionTableExpression table = getFunctionTableExpression(environment);

      // Download and unzip archives.
      GeneratorEnvironmentUtils.downloadReferencedModules(
          environment,
          ExpressionUtils.getAllFoundModuleExpressions(table));

      // Check that the expected files were downloaded
      new CheckLocalFileSystemIntegrity(environment.unzippedArchivesFolder)
          .visit(table);

      runBuilders(environment, table);
      return true;
    }
    return false;
  }

  private boolean handleLint(@NotNull List<String> args)
      throws IOException, NoSuchAlgorithmException, URISyntaxException {
    if (args.size() > 0 && "lint".equals(args.get(0))) {
      if (args.size() > 1) {
        GeneratorEnvironment environment = getGeneratorEnvironment(false, false);

        SoftNameDependency dependencies[] = new SoftNameDependency[args.size() - 1];
        for (int i = 1; i < args.size(); ++i) {
          dependencies[i - 1] = new SoftNameDependency(args.get(i));
        }

        FunctionTableExpression table = getFunctionTableExpression(environment, dependencies);

        // Check that the expected files were downloaded
        new StubCheckLocalFileSystemIntegrity(environment.unzippedArchivesFolder)
            .visit(table);
        return true;
      } else {
        out.print("Usage: cdep lint (coordinate or path/to/cdep-manifest.yml)'\n");
        return true;
      }
    }
    return false;
  }

  private boolean handleCreate(@NotNull List<String> args)
      throws IOException, NoSuchAlgorithmException, URISyntaxException {
    if (args.size() > 0 && "create".equals(args.get(0))) {
      if (args.size() > 1 && "hashes".equals(args.get(1))) {
        GeneratorEnvironment environment = getGeneratorEnvironment(false, false);
        getFunctionTableExpression(environment);
        environment.writeCDepSHA256File();
        out.printf("Created cdep.sha256\n");
        return true;
      }
      out.print("Usage: cdep create hashes'\n");
      return true;
    }
    return false;
  }

  private boolean handleMerge(@NotNull List<String> args) throws IOException, NoSuchAlgorithmException {
    if (args.size() > 0 && "merge".equals(args.get(0))) {
      if (args.size() < 4) {
        out.printf("Usage: cdep merge coordinate1 coordinate2 ... outputmanifest.yml");
        return true;
      }

      File output = new File(args.get(args.size() - 1));
      if (output.exists()) {
        throw new RuntimeException(
            String.format("File %s already exists", output.getAbsolutePath()));
      }

      GeneratorEnvironment environment = getGeneratorEnvironment(false, true);

      CDepManifestYml merged = null;
      for (int i = 1; i < args.size() - 1; ++i) {
        SoftNameDependency name = new SoftNameDependency(args.get(i));
        ResolvedManifest resolved = new Resolver(environment).resolveAny(name);
        if (resolved == null) {
          out.printf("Manifest for '%s' didn't exist. Aborting merge.\n", args.get(i));
          return true;
        } else if (merged == null) {
          merged = resolved.cdepManifestYml;
        } else {
          merged = MergeCDepManifestYmls.merge(merged, resolved.cdepManifestYml);
        }
      }
      notNull(merged);

      // Check the merge for sanity
      CDepManifestYmlUtils.checkManifestSanity(merged);

      // Write the merged manifest out
      String body = CreateCDepManifestYmlString.create(merged);
      FileUtils.writeTextToFile(output, body);
      out.printf("Merged %s manifests into %s.\n", args.size() - 2, output);
      return true;
    }
    return false;
  }

  private boolean handleShow(@NotNull List<String> args) throws IOException, NoSuchAlgorithmException, URISyntaxException {
    if (args.size() > 0 && "show".equals(args.get(0))) {
      if (args.size() > 1 && "folders".equals(args.get(1))) {
        GeneratorEnvironment environment = getGeneratorEnvironment(false, false);
        out.printf("Downloads: %s\n", environment.downloadFolder.getAbsolutePath());
        out.printf("Exploded: %s\n", environment.unzippedArchivesFolder.getAbsolutePath());
        out.printf("Modules: %s\n", environment.modulesFolder.getAbsolutePath());
        return true;
      }
      if (args.size() > 1 && "local".equals(args.get(1))) {
        GeneratorEnvironment environment = getGeneratorEnvironment(false, false);
        if (args.size() == 2) {
          out.printf("Usage: cdep show local %s\n", EXAMPLE_COORDINATE);
          return true;
        }
        SoftNameDependency dependency = new SoftNameDependency(args.get(2));
        Resolver resolver = new Resolver(environment);
        ResolvedManifest resolved = resolver.resolveAny(dependency);
        if (resolved == null) {
          out.printf("Could not resolve manifest coordinate %s\n", args.get(2));
          return true;
        }

        File local = environment
            .getLocalDownloadFilename(resolved.cdepManifestYml.coordinate,
                resolved.remote);
        out.println(local.getCanonicalFile());
        return true;
      }
      if (args.size() > 1 && "manifest".equals(args.get(1))) {
        handleReadCDepYml();
        assert config != null;
        out.print(config.toString());
        return true;
      }
      if (args.size() > 1 && "include".equals(args.get(1))) {
        if (args.size() == 2) {
          out.print("Usage: show include {coordinate}/n");
          return true;
        }
        // Redirect output so that only the include folder is printed (so that it can be redirected in shells)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream alternOut = new PrintStream(baos);
        for (int i = 2; i < args.size(); ++i) {
          GeneratorEnvironment environment = getGeneratorEnvironment(
              alternOut, false, true);
          File include = EnvironmentUtils.getPackageLevelIncludeFolder(environment, args.get(i));
          out.printf("%s\n", include);
        }
        return true;
      }
      out.print("Usage: cdep show [folders|local|manifest|include]'\n");
      return true;
    }
    return false;
  }

  private boolean handleWrapper(@NotNull List<String> args) throws IOException {
    if (args.size() > 0 && "wrapper".equals(args.get(0))) {
      String appname = System.getProperty("io.cdep.appname");
      if (appname == null) {
        throw new RuntimeException(
            "Must set java system proeperty io.cdep.appname to the path of cdep.bat");
      }
      File applicationBase = new File(appname).getParentFile();
      if (applicationBase == null || !applicationBase.isDirectory()) {
        fail("Could not find folder for io.cdep.appname='%s'", appname);
      }
      out.printf("Installing cdep wrapper from %s\n", applicationBase);
      File cdepBatFrom = new File(applicationBase, "cdep.bat");
      File cdepBatTo = new File(workingFolder, "cdep.bat");
      File cdepFrom = new File(applicationBase, "cdep");
      File cdepTo = new File(workingFolder, "cdep");
      File cdepYmlFrom = new File(applicationBase, "cdep.yml");
      File cdepYmlTo = new File(workingFolder, "cdep.yml");
      File bootstrapFrom = new File(applicationBase, "bootstrap/wrapper/bootstrap.jar");
      File bootstrapTo = new File(workingFolder, "bootstrap/wrapper/bootstrap.jar");
      //noinspection ResultOfMethodCallIgnored
      bootstrapTo.getParentFile().mkdirs();
      out.printf("Installing %s\n", cdepBatTo);
      FileUtils.copyFile(cdepBatFrom, cdepBatTo);
      out.printf("Installing %s\n", cdepTo);
      FileUtils.copyFile(cdepFrom, cdepTo);
      if (!cdepTo.setExecutable(true)) {
        throw new RuntimeException("User did not have permission to make cdep executable");
      }
      out.printf("Installing %s\n", bootstrapTo);
      FileUtils.copyFile(bootstrapFrom, bootstrapTo);
      if (cdepYmlTo.isFile()) {
        out.printf("Not overwriting %s\n", cdepYmlTo);
      } else {
        out.printf("Installing %s\n", cdepYmlTo);
        FileUtils.copyFile(cdepYmlFrom, cdepYmlTo);
      }
      return true;
    }
    return false;
  }

  private boolean handleFetch(@NotNull List<String> args)
      throws IOException, URISyntaxException, NoSuchAlgorithmException {
    if (args.size() > 0 && "fetch".equals(args.get(0))) {
      if (args.size() < 2) {
        out.printf("Usage: cdep fetch {coordinate1} {coordinate2} ...\n");
        return true;
      }

      for (int i = 1; i < args.size(); ++i) {
        GeneratorEnvironment environment = getGeneratorEnvironment(false, true);
        SoftNameDependency dependencies[] = new SoftNameDependency[]{
            new SoftNameDependency(args.get(i))};
        FunctionTableExpression table = getFunctionTableExpression(environment, dependencies);
        // Download and unzip archives.
        GeneratorEnvironmentUtils.downloadReferencedModules(
            environment,
            ExpressionUtils.getAllFoundModuleExpressions(table));
        // Check that the expected files were downloaded
        new CheckLocalFileSystemIntegrity(environment.unzippedArchivesFolder)
            .visit(table);
      }

      out.printf("Fetch complete\n");
      return true;
    }
    return false;
  }

  private void handleGenerateScript()
      throws IOException, URISyntaxException, NoSuchAlgorithmException {
    //noinspection ConstantConditions
    if (config.dependencies == null || config.dependencies.length == 0) {
      out.printf("Nothing to do. Add dependencies to %s\n", configFile);
      return;
    }
    GeneratorEnvironment environment = getGeneratorEnvironment(false, false);
    environment.readCDepSHA256File();
    FunctionTableExpression table = getFunctionTableExpression(environment);

    // Download and unzip archives.
    GeneratorEnvironmentUtils.downloadReferencedModules(
        environment,
        ExpressionUtils.getAllFoundModuleExpressions(table));

    // Check that the expected files were downloaded
    new CheckLocalFileSystemIntegrity(environment.unzippedArchivesFolder)
        .visit(table);

    runBuilders(environment, table);
    environment.writeCDepSHA256File();
  }

  @NotNull
  private FunctionTableExpression getFunctionTableExpression(GeneratorEnvironment environment) throws IOException, URISyntaxException, NoSuchAlgorithmException {

    assert config != null;
    return getFunctionTableExpression(environment, config.dependencies);
  }

  @NotNull
  private GeneratorEnvironment getGeneratorEnvironment(boolean forceRedownload, boolean ignoreManifestHashes) {
    return getGeneratorEnvironment(out, forceRedownload, ignoreManifestHashes);
  }

  @NotNull
  private GeneratorEnvironment getGeneratorEnvironment(PrintStream out, boolean forceRedownload, boolean ignoreManifestHashes) {
    return new GeneratorEnvironment(out, workingFolder, downloadFolder, forceRedownload, ignoreManifestHashes);
  }

  private boolean handleReadCDepYml() throws IOException {
    configFile = new File(workingFolder, "cdep.yml");
    if (!configFile.exists()) {
      out.printf("Expected a configuration file at %s\n", configFile.getCanonicalFile());
      return false;
    }

    this.config = CDepYmlUtils.fromString(FileUtils.readAllText(configFile));
    CDepYmlUtils.checkSanity(config, configFile);
    return true;
  }

  private boolean handleHelp(@NotNull List<String> args) throws IOException {
    if (args.size() != 1 || !args.get(0).equals("--help")) {
      return true;
    }
    out.printf("cdep %s\n", BuildInfo.PROJECT_VERSION);
    out.printf(" cdep: download dependencies and generate build modules for current cdep.yml\n");
    out.printf(" cdep show folders: show local download and file folders\n");
    out.printf(" cdep show manifest: show cdep interpretation of cdep.yml\n");
    out.printf(" cdep show include {coordinate}: show local include path for the given coordinate\n");
    out.printf(" cdep redownload: redownload dependencies for current cdep.yml\n");
    out.printf(" cdep create hashes: create or recreate cdep.sha256 file\n");
    out.printf(" cdep merge {coordinate} {coordinate2} ... outputmanifest.yml: " +
        "merge manifests into outputmanifest.yml\n");
    out.printf(" cdep fetch {coordinate} {coordinate2} ... : download multiple packages\n");
    out.printf(" cdep wrapper: copy cdep to the current folder\n");
    out.printf(" cdep --version: show version information\n");
    return false;
  }

  private void handleWorkingFolder(@NotNull List<String> args) throws IOException {
    for (String workingFolder : eatStringArgument("-wf", "--working-folder", args)) {
      this.workingFolder = new File(workingFolder);
    }
  }

  private void handleDownloadFolder(@NotNull List<String> args) throws IOException {
    for (String workingFolder : eatStringArgument("-df", "--download-folder", args)) {
      this.workingFolder = new File(workingFolder);
    }
  }

  private boolean handleVersion(@NotNull List<String> args) {
    if (args.size() != 1 || !args.get(0).equals("--version")) {
      return true;
    }
    out.printf("cdep %s\n", BuildInfo.PROJECT_VERSION);
    return false;
  }
}