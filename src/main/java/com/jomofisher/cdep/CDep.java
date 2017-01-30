package com.jomofisher.cdep;


import static com.jomofisher.cdep.Resolver.resolveAny;

import com.jomofisher.cdep.model.Configuration;
import com.jomofisher.cdep.manifest.Manifest;
import com.jomofisher.cdep.model.Reference;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class CDep {
    private PrintStream out = System.out;
    private File workingFolder = new File(".");
    private Configuration config = null;

    CDep(PrintStream out) {
        this.out = out;
    }

    void go(String [] args) throws IOException {
        if (!handleVersion(args)) return;
        handleWorkingFolder(args);
        if (!handleReadConfig(args)) return;
        if (!handleDump(args)) return;
        handleGenerateScript();
    }


    private void handleGenerateScript() throws IOException {
        Map<Coordinate, Manifest> compile = new HashMap<>();

        for(Reference dependency : config.dependencies) {
            if (dependency.compile != null) {
                ResolvedDependency resolved = Resolver.resolveAny(dependency.compile);
                if (resolved == null) {
                    throw new RuntimeException("Could not resolve: " + dependency.compile);

                }
                compile.put(resolved.coordinate, resolved.manifest);
            }
        }
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
        return true;
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

    public static void main(String [] args) throws IOException {
        new CDep(System.out).go(args);
    }
}