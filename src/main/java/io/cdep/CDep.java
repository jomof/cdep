package io.cdep;


import io.cdep.AST.finder.FunctionTableExpression;
import io.cdep.AST.service.ResolvedManifest;
import io.cdep.model.BuildSystem;
import io.cdep.model.Configuration;
import io.cdep.model.Reference;
import io.cdep.service.GeneratorEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;


public class CDep {

    final private static String EXAMPLE_COORDINATE = "com.github.jomof:boost:1.0.63-rev9";
    private PrintStream out = System.out;
    private File workingFolder = new File(".");
    private File downloadFolder = null;
    private Configuration config = null;
    private File configFile = null;

    CDep(PrintStream out) {
        this.out = out;
    }

    public static void main(String[] args)
        throws IOException, URISyntaxException, NoSuchAlgorithmException {
        new CDep(System.out).go(args);
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
        if (!handleReadConfig()) {
            return;
        }
        if (handleRedownload(args)) {
            return;
        }
        handleGenerateScript();
    }

    private boolean handleRedownload(String[] args)
        throws IOException, URISyntaxException, NoSuchAlgorithmException {
        if (args.length > 0 && "redownload".equals(args[0])) {
            FunctionTableExpression table = getFunctionTableExpression(true);
            GeneratorEnvironment environment = getGeneratorEnvironment();

            // Download and unzip archives.
            GeneratorEnvironmentUtils.downloadReferencedModules(
                environment,
                table,
                true /* forceRedownload */);

            new CMakeGenerator(environment).generate(table);
            return true;
        }
        return false;
    }

    private boolean handleShow(String args[]) throws IOException {
        if (args.length > 0 && "show".equals(args[0])) {
            if (args.length > 1 && "folders".equals(args[1])) {
                GeneratorEnvironment environment = getGeneratorEnvironment();
                out.printf("Downloads: %s\n", environment.downloadFolder.getAbsolutePath());
                out.printf("Exploded: %s\n", environment.unzippedArchivesFolder.getAbsolutePath());
                out.printf("Modules: %s\n", environment.modulesFolder.getAbsolutePath());
                return true;
            }
            if (args.length > 1 && "local".equals(args[1])) {
                GeneratorEnvironment environment = getGeneratorEnvironment();
                if (args.length == 2) {
                    out.printf("Usage: cdep show local %s\n", EXAMPLE_COORDINATE);
                    return true;
                }
                Reference reference = new Reference(args[2]);
                ResolvedManifest resolved = environment.resolveAny(reference, false);
                if (resolved == null) {
                    out.printf("Could not resolve manifest coordinate %s\n", args[2]);
                    return true;
                }

                File local = environment.getLocalDownloadFilename(resolved.manifest.coordinate,
                    resolved.remote);
                out.println(local.getCanonicalFile());
                return true;
            }
            if (args.length > 1 && "manifest".equals(args[1])) {
                handleReadConfig();
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
                throw new RuntimeException("Must set java system proeperty io.cdep.appname to the path of cdep.bat");
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
        FunctionTableExpression table = getFunctionTableExpression(false);
        GeneratorEnvironment environment = getGeneratorEnvironment();

        // Download and unzip archives.
        GeneratorEnvironmentUtils.downloadReferencedModules(
            environment,
            table,
            false /* forceRedownload */);

        new CMakeGenerator(environment).generate(table);
    }

    private FunctionTableExpression getFunctionTableExpression(boolean forceRedownload)
        throws IOException, URISyntaxException {
        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        Set<String> seen = new HashSet<>();
        for(Reference dependency : config.dependencies) {
            if (dependency.compile == null) {
                continue;
            }
            if (seen.contains(dependency.compile)) {
                continue;
            }
            ResolvedManifest resolved = getGeneratorEnvironment().resolveAny(
                dependency, forceRedownload);
            if (resolved == null) {
                throw new RuntimeException("Could not resolve: " + dependency.compile);
            }
            builder.addManifest(resolved);
            seen.add(dependency.compile);
        }

        return builder.build();
    }

    private GeneratorEnvironment getGeneratorEnvironment() {
        return new GeneratorEnvironment(out, workingFolder, downloadFolder);
    }

    private boolean handleReadConfig() throws IOException {
        configFile = new File(workingFolder, "cdep.yml");
        if (!configFile.exists()) {
            out.printf("Expected a configuration file at %s\n", configFile.getCanonicalFile());
            return false;
        }

        Yaml yaml = new Yaml(new Constructor(Configuration.class));
        this.config = (Configuration)yaml.load(new FileInputStream(configFile));
        if (this.config == null) {
            this.config = new Configuration();
        }
        validateConfig(configFile);
        return true;
    }

    private void validateConfig(File configurationFile) {
        if (config.builders.length == 0) {
            StringBuilder sb = new StringBuilder();
            for (BuildSystem builder : BuildSystem.values()) {
                sb.append(builder.toString());
                sb.append(" ");
            }
            throw new RuntimeException(String.format("Error in '%s'. The 'builders' section is "
                + "missing or empty. Valid values are: %s.", configurationFile, sb));
        }
    }

    private boolean handleHelp(String[] args) throws IOException {
        if (args.length != 1 || !args[0].equals("--help")) {
            return true;
        }
        out.printf("cdep %s\n", BuildInfo.PROJECT_VERSION);
        out.printf(
            " cdep: download dependencies and generate build modules for current cdep.yml\n");
        out.printf(" cdep show folders: show local download and archive folders\n");
        out.printf(" cdep show manifest: show cdep interpretation of cdep.yml\n");
        out.printf(" cdep redownload: redownload dependencies for current cdep.yml\n");
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