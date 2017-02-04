package com.jomofisher.cdep;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.Files;
import com.jomofisher.cdep.model.Configuration;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

public class TestCDep {

    private static String main(String... args) throws IOException, URISyntaxException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
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

    //@Test
    public void testScript() throws IOException, URISyntaxException {
        File yaml = new File("test-files/simpleConfiguration/.cdep.yml");
        yaml.getParentFile().mkdirs();
        Files.write("android:\n" +
                        "  ndk:\n" +
                        "    platforms: [21, 22]\n",
            yaml, StandardCharsets.UTF_8);
        main("-wf", yaml.getParent(), "--host", "Linux");
        File scriptFile = new File(".cdep/build.sh");
        String script = Joiner.on("\n").join(Files.readLines(scriptFile, Charsets.UTF_8));
        assertThat(script).contains("cmake-3.7.2-Linux-x86_64.tar.gz");
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
                + "- compile: com.github.jomof:boost:1.0.63-rev7\n"
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
    public void dumpIsSelfHost() throws IOException, URISyntaxException {
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
}