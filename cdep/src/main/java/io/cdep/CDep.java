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

import io.cdep.cdep.FindModuleFunctionTableBuilder;
import io.cdep.cdep.ast.finder.FunctionTableExpression;
import io.cdep.cdep.ast.service.ResolvedManifest;
import io.cdep.cdep.generator.CMakeExamplesGenerator;
import io.cdep.cdep.generator.CMakeGenerator;
import io.cdep.cdep.generator.GeneratorEnvironment;
import io.cdep.cdep.generator.GeneratorEnvironmentUtils;
import io.cdep.cdep.resolver.ResolutionScope;
import io.cdep.cdep.resolver.ResolutionScopeResolver;
import io.cdep.cdep.utils.CDepYmlUtils;
import io.cdep.cdep.utils.FileUtils;
import io.cdep.cdep.yml.cdep.BuildSystem;
import io.cdep.cdep.yml.cdep.CDepYml;
import io.cdep.cdep.yml.cdep.SoftNameDependency;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class CDep {

    final private static String EXAMPLE_COORDINATE = "com.github.jomof:boost:1.0.63-rev18";
    private PrintStream out = System.out;
    private File workingFolder = new File(".");
    private File downloadFolder = null;
    private CDepYml config = null;
    private File configFile = null;

    CDep(PrintStream out) {
        this.out = out;
    }

    public static int main(String[] args)
        throws IOException, URISyntaxException, NoSuchAlgorithmException {
        try {
            new CDep(System.out).go(args);
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }
        return 0;
    }

    void go(String[] args) throws IOException, URISyntaxException, NoSuchAlgorithmException {
        if (!handleHelp(args)) {
            return;
        }
        if (!handleVersion(args)) return;
        handleWorkingFolder(args);
        handleDownloadFolder(args);
        if (handleWrapper(args)) return;
        if (handleShow(args)) return;
        if (!handleReadCDepYml()) {
            return;
        }
        if (handleCreate(args)) return;
        if (handleRedownload(args)) {
            return;
        }
        handleGenerateScript();
    }

    private void runBuilders(GeneratorEnvironment environment, FunctionTableExpression table) throws IOException {
        for (BuildSystem buildSystem : config.builders) {
            switch(buildSystem) {
                case cmake:
                    new CMakeGenerator(environment).generate(table);
                    break;
                case cmakeExamples:
                    new CMakeExamplesGenerator(environment).generate(table);
                    break;
                default:
                    throw new RuntimeException("Unknown builder: " + buildSystem);
            }

        }
    }

    private boolean handleRedownload(String[] args)
        throws IOException, URISyntaxException, NoSuchAlgorithmException {
        if (args.length > 0 && "redownload".equals(args[0])) {
            GeneratorEnvironment environment = getGeneratorEnvironment(true);
            FunctionTableExpression table = getFunctionTableExpression(environment);

            // Download and unzip archives.
            GeneratorEnvironmentUtils.downloadReferencedModules(
                environment,
                table,
                true /* forceRedownload */);

            runBuilders(environment, table);
            return true;
        }
        return false;
    }

    private boolean handleCreate(String args[]) throws IOException, NoSuchAlgorithmException, URISyntaxException {
        if (args.length > 0 && "create".equals(args[0])) {
            if (args.length > 1 && "hashes".equals(args[1])) {
                GeneratorEnvironment environment = getGeneratorEnvironment(false);
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

    private boolean handleShow(String args[]) throws IOException, NoSuchAlgorithmException {
        if (args.length > 0 && "show".equals(args[0])) {
            if (args.length > 1 && "folders".equals(args[1])) {
                GeneratorEnvironment environment = getGeneratorEnvironment(false);
                out.printf("Downloads: %s\n", environment.downloadFolder.getAbsolutePath());
                out.printf("Exploded: %s\n", environment.unzippedArchivesFolder.getAbsolutePath());
                out.printf("Modules: %s\n", environment.modulesFolder.getAbsolutePath());
                return true;
            }
            if (args.length > 1 && "local".equals(args[1])) {
                GeneratorEnvironment environment = getGeneratorEnvironment(false);
                if (args.length == 2) {
                    out.printf("Usage: cdep show local %s\n", EXAMPLE_COORDINATE);
                    return true;
                }
                SoftNameDependency dependency = new SoftNameDependency(args[2]);
                ResolutionScopeResolver resolver = new ResolutionScopeResolver(environment);
                ResolvedManifest resolved = resolver.resolveAny(dependency);
                if (resolved == null) {
                    out.printf("Could not resolve manifest coordinate %s\n", args[2]);
                    return true;
                }

                File local = environment
                    .getLocalDownloadFilename(resolved.cdepManifestYml.coordinate,
                    resolved.remote);
                out.println(local.getCanonicalFile());
                return true;
            }
            if (args.length > 1 && "manifest".equals(args[1])) {
                handleReadCDepYml();
                out.print(config.toString());
                return true;
            }
            out.print("Usage: cdep show [folders|local|manifest]'\n");
            return true;
        }
        return false;
    }

    private boolean handleWrapper(String args[]) throws IOException {
        if (args.length > 0 && "wrapper".equals(args[0])) {
            String appname = System.getProperty("io.cdep.appname");
            if (appname == null) {
                throw new RuntimeException(
                        "Must set java system proeperty io.cdep.appname to the path of cdep.bat");
            }
            File applicationBase = new File(appname).getParentFile();
            if (applicationBase == null || !applicationBase.isDirectory()) {
                throw new RuntimeException(String.format("Could not find folder for io.cdep.appname='%s'",
                        appname));
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

    private void handleGenerateScript()
        throws IOException, URISyntaxException, NoSuchAlgorithmException {
        //noinspection ConstantConditions
        if (config.dependencies == null || config.dependencies.length == 0) {
            out.printf("Nothing to do. Add dependencies to %s\n", configFile);
            return;
        }
        GeneratorEnvironment environment = getGeneratorEnvironment(false);
        environment.readCDepSHA256File();
        FunctionTableExpression table = getFunctionTableExpression(environment);

        // Download and unzip archives.
        GeneratorEnvironmentUtils.downloadReferencedModules(
            environment,
            table,
            false /* forceRedownload */);

        runBuilders(environment, table);
        environment.writeCDepSHA256File();
    }

    private FunctionTableExpression getFunctionTableExpression(GeneratorEnvironment environment)
            throws IOException, URISyntaxException, NoSuchAlgorithmException {
        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        ResolutionScope scope = new ResolutionScope(config.dependencies);
        ResolutionScopeResolver resolver = new ResolutionScopeResolver(environment);
        resolver.resolveAll(scope);
        for (ResolutionScope.Resolution resolved : scope.getResolutions()) {
            if (resolved instanceof  ResolutionScope.FoundManifestResolution) {
                builder.addManifest(((ResolutionScope.FoundManifestResolution) resolved).resolved);
            }
        }

        return builder.build();
    }

    private GeneratorEnvironment getGeneratorEnvironment(boolean forceRedownload) {
        return new GeneratorEnvironment(out, workingFolder, downloadFolder, forceRedownload);
    }

    private boolean handleReadCDepYml() throws IOException {
        configFile = new File(workingFolder, "cdep.yml");
        if (!configFile.exists()) {
            out.printf("Expected a configuration file at %s\n", configFile.getCanonicalFile());
            return false;
        }

        Yaml yaml = new Yaml(new Constructor(CDepYml.class));
        this.config = (CDepYml) yaml.load(new FileInputStream(configFile));
        if (this.config == null) {
            this.config = new CDepYml();
        }
        CDepYmlUtils.checkSanity(config, configFile);
        return true;
    }

    private boolean handleHelp(String[] args) throws IOException {
        if (args.length != 1 || !args[0].equals("--help")) {
            return true;
        }
        out.printf("cdep %s\n", BuildInfo.PROJECT_VERSION);
        out.printf(" cdep: download dependencies and generate build modules for current cdep.yml\n");
        out.printf(" cdep show folders: show local download and file folders\n");
        out.printf(" cdep show manifest: show cdep interpretation of cdep.yml\n");
        out.printf(" cdep redownload: redownload dependencies for current cdep.yml\n");
        out.printf(" cdep create hashes: create or recreate cdep.sha256 file\n");
        out.printf(" cdep --version: show version information\n");
        return false;
    }

    private void handleWorkingFolder(String[] args) throws IOException {
        boolean takeNext = false;
        for (int i = 0; i < args.length; ++i) {
            if (takeNext) {
                this.workingFolder = new File(args[i]);
                takeNext = false;
            } else if (args[i].equals("--working-folder") || args[i].equals("-wf")) {
                takeNext = true;
            }
        }
    }

    private void handleDownloadFolder(String[] args) throws IOException {
        boolean takeNext = false;
        for (int i = 0; i < args.length; ++i) {
            if (takeNext) {
                this.downloadFolder = new File(args[i]);
                takeNext = false;
            } else if (args[i].equals("--download-folder") || args[i].equals("-df")) {
                takeNext = true;
            }
        }
    }

    private boolean handleVersion(String[] args) {
        if (args.length != 1 || !args[0].equals("--version")) {
            return true;
        }
        out.printf("cdep %s\n", BuildInfo.PROJECT_VERSION);
        return false;
    }
}