package com.jomofisher.cdep;


import com.jomofisher.cdep.AST.FunctionTableExpression;
import com.jomofisher.cdep.manifest.Coordinate;
import com.jomofisher.cdep.manifest.Manifest;
import com.jomofisher.cdep.model.BuildSystem;
import com.jomofisher.cdep.model.Configuration;
import com.jomofisher.cdep.model.Reference;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class CDep {
    private PrintStream out = System.out;
    private File workingFolder = new File(".");
    private Configuration config = null;
    private Map<Coordinate, Manifest> manifests = null;

    CDep(PrintStream out) {
        this.out = out;
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        new CDep(System.out).go(args);
    }

    void go(String[] args) throws IOException, URISyntaxException {
        if (!handleVersion(args)) return;
        handleWorkingFolder(args);
        if (!handleReadConfig(args)) return;
        if (!handleDump(args)) return;
        handleGenerateScript();
    }

    private void handleGenerateScript() throws IOException, URISyntaxException {
        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        Set<String> seen = new HashSet<>();
        for(Reference dependency : config.dependencies) {
            if (dependency.compile == null) {
                continue;
            }
            if (seen.contains(dependency.compile)) {
                continue;
            }
            ResolvedManifest resolved = Resolver.resolveAny(dependency.compile);
            if (resolved == null) {
                throw new RuntimeException("Could not resolve: " + dependency.compile);

            }
            builder.addManifest(resolved);
            seen.add(dependency.compile);
        }

        FunctionTableExpression table = builder.build();
        GeneratorEnvironment environment = new GeneratorEnvironment(
            new File(workingFolder, ".cdep/downloads").getAbsoluteFile(),
            new File(workingFolder, ".cdep/exploded").getAbsoluteFile()
        );
        new CMakeGenerator().generate(environment, table);
    }

    private boolean handleDump(String[] args) {
        for (int i = 0; i < args.length; ++i) {
            if (args[i].equals("--dump") || args[i].equals("-d")) {
                out.print(config.toString());
                return false;
            }
        }
        return true;
    }

    private boolean handleReadConfig(String[] args) throws IOException {
        File config = new File(workingFolder, "cdep.yml");
        if (!config.exists()) {
            out.printf("Expected a configuration file at %s\n", config.getCanonicalFile());
            return false;
        }

        Yaml yaml = new Yaml(new Constructor(Configuration.class));
        this.config = (Configuration)yaml.load(new FileInputStream(config));
        if (this.config == null) {
            this.config = new Configuration();
        }
        validateConfig(config);
        return true;
    }

    private void validateConfig(File configurationFile) {
        if (config.builders == null || config.builders.length == 0) {
            StringBuilder sb = new StringBuilder();
            for (BuildSystem builder : BuildSystem.values()) {
                sb.append(builder.toString());
                sb.append(" ");
            }
            throw new RuntimeException(String.format("Error in '%s'. The 'builders' section is "
                + "missing or empty. Valid values are: %s.", configurationFile, sb));
        }
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

    private boolean handleVersion(String[] args) {
        if (args.length != 1 || !args[0].equals("--version")) {
            return true;
        }
        out.printf("cdep %s\n", BuildInfo.PROJECT_VERSION);
        return false;
    }
}