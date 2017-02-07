package io.cdep;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.io.Files;
import io.cdep.model.Configuration;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

public class TestCDep {

    private static String main(String... args) throws IOException, URISyntaxException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        if (System.getProperty("io.cdep.appname") == null) {
            System.setProperty("io.cdep.appname", "rando-test-folder");
        }
        new CDep(ps).go(args);
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    @Test
    public void testVersion() throws IOException, URISyntaxException {
        assertThat(main("--version")).contains(BuildInfo.PROJECT_VERSION);
    }

    @Test
    public void missingConfigurationFile() throws IOException, URISyntaxException {
        new File("test-files/empty-folder").mkdirs();
        assertThat(main("-wf", "test-files/empty-folder")).contains("configuration file");
    }

    @Test
    public void workingFolderFlag() throws IOException, URISyntaxException {
        assertThat(main("--working-folder", "non-existing-blah")).contains("non-existing-blah");
    }

    @Test
    public void wfFlag() throws IOException, URISyntaxException {
        assertThat(main("-wf", "non-existing-blah")).contains("non-existing-blah");
    }

    @Test
    public void someKnownUrls() throws IOException, URISyntaxException {
        Configuration config = new Configuration();
        System.out.printf(new Yaml().dump(config));
        File yaml = new File("test-files/simpleDependency/cdep.yml");
        yaml.getParentFile().mkdirs();
        Files.write("builders: [cmake]\n"
                + "dependencies:\n"
                + "- compile: com.github.jomof:cmakeify:alpha-0.0.35\n"
                + "- compile: https://github.com/jomof/cmakeify/releases/download/alpha-0.0.35/cdep-manifest.yml\n"
                + "- compile: com.github.jomof:low-level-statistics:0.0.6\n",
            yaml, StandardCharsets.UTF_8);
        String result1 = main("-wf", yaml.getParent(), "--dump");
        yaml.delete();
        Files.write(result1, yaml, StandardCharsets.UTF_8);
        System.out.print(result1);
        String result2 = main("-wf", yaml.getParent(), "--dump");
        assertThat(result2).isEqualTo(result1);
        assertThat(result2).contains("alpha-0.0.35");
        String result3 = main("-wf", yaml.getParent());
    }

    @Test
    public void noDependencies() throws IOException, URISyntaxException {
        Configuration config = new Configuration();
        System.out.printf(new Yaml().dump(config));
        File yaml = new File("test-files/simpleDependency/cdep.yml");
        yaml.getParentFile().mkdirs();
        Files.write("builders: [cmake]\n"
                + "dependencies:\n",
            yaml, StandardCharsets.UTF_8);
        String result1 = main("-wf", yaml.getParent());
        System.out.printf(result1);
        assertThat(result1).contains("Nothing");
    }

    @Test
    public void dumpIsSelfHost() throws IOException, URISyntaxException {
        System.out.printf("%s\n", System.getProperty("user.home"));
        Configuration config = new Configuration();
        System.out.printf(new Yaml().dump(config));
        File yaml = new File("test-files/simpleConfiguration/cdep.yml");
        yaml.getParentFile().mkdirs();
        Files.write("builders: [cmake]", yaml, StandardCharsets.UTF_8);
        String result1 = main("-wf", yaml.getParent(), "--dump");
        yaml.delete();
        Files.write(result1, yaml, StandardCharsets.UTF_8);
        System.out.print(result1);
        String result2 = main("-wf", yaml.getParent(), "--dump");
        assertThat(result2).isEqualTo(result1);
    }

    @Test
    public void testNakedCall() throws IOException, URISyntaxException {
        main();
    }

    @Test
    public void showFolders() throws IOException, URISyntaxException {
        String result = main("show", "folders");
        System.out.printf(result);
    }

    @Test
    public void testWrapper() throws IOException, URISyntaxException {
        File testFolder = new File("test-files/testWrapper");
        File redistFolder = new File(testFolder, "redist");
        File workingFolder = new File(testFolder, "working");
        File cdepFile = new File(redistFolder, "cdep");
        File cdepBatFile = new File(redistFolder, "cdep.bat");
        File cdepYmlFile = new File(redistFolder, "cdep.yml");
        File bootstrapJar = new File(redistFolder, "bootstrap/wrapper/bootstrap.jar");
        redistFolder.mkdirs();
        workingFolder.mkdirs();
        bootstrapJar.getParentFile().mkdirs();
        Files.write("cdepFile content", cdepFile, Charset.defaultCharset());
        Files.write("cdepBatFile content", cdepBatFile, Charset.defaultCharset());
        Files.write("cdepYmlFile content", cdepYmlFile, Charset.defaultCharset());
        Files.write("bootstrapJar content", bootstrapJar, Charset.defaultCharset());
        System.setProperty("io.cdep.appname", new File(redistFolder, "cdep.bat").getAbsolutePath());
        String result;
        try {
            result = main("wrapper", "-wf", workingFolder.toString());
        } finally {
            System.setProperty("io.cdep.appname", "rando-test-folder");
        }

        System.out.print(result);
        assertThat(result).contains("Installing cdep");
        File cdepToFile = new File(workingFolder, "cdep");
        File cdepBatToFile = new File(workingFolder, "cdep.bat");
        File cdepYmlToFile = new File(workingFolder, "cdep.yml");
        File bootstrapJarToFile = new File(workingFolder, "bootstrap/wrapper/bootstrap.jar");
        assertThat(cdepToFile.isFile()).isTrue();
        assertThat(cdepBatToFile.isFile()).isTrue();
        assertThat(cdepYmlToFile.isFile()).isTrue();
        assertThat(bootstrapJarToFile.isFile()).isTrue();
    }
}