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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.common.io.Files;
import io.cdep.cdep.yml.cdep.CDepYml;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

public class TestCDep {

  private static String main(String... args) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    if (System.getProperty("io.cdep.appname") == null) {
      System.setProperty("io.cdep.appname", "rando-test-folder");
    }
    new CDep(ps).go(args);
    return new String(baos.toByteArray(), StandardCharsets.UTF_8);
  }

  @Test
  public void lintUsage() throws Exception {
    assertThat(main("lint")).contains("Usage:");
  }

  @Test
  public void lintWhereTwoFilesContainTheSameZip() throws Exception {
    try {
      main(main("lint", "com.github.jomof:firebase/admob:2.1.3-rev8"));
      fail("Expected failure");
    } catch (RuntimeException e) {
      assertThat(e.toString())
          .contains(" The file should only be in the lowest level package "
              + "'com.github.jomof:firebase/app:2.1.3-rev8'");
    }
  }

  @Test
  public void testVersion() throws Exception {
    assertThat(main("--version")).contains(BuildInfo.PROJECT_VERSION);
  }

  @Test
  public void missingConfigurationFile() throws Exception {
    new File(".test-files/empty-folder").mkdirs();
    assertThat(main("-wf", ".test-files/empty-folder")).contains("configuration file");
  }

  @Test
  public void workingFolderFlag() throws Exception {
    assertThat(main("--working-folder", "non-existing-blah")).contains("non-existing-blah");
  }

  @Test
  public void wfFlag() throws Exception {
    assertThat(main("-wf", "non-existing-blah")).contains("non-existing-blah");
  }

  @Test
  public void runVectorial() throws Exception {
    CDepYml config = new CDepYml();
    System.out.printf(new Yaml().dump(config));
    File yaml = new File(".test-files/runVectorial/cdep.yml");
    yaml.getParentFile().mkdirs();
    Files.write("builders: [cmake, cmakeExamples]\n"
            + "dependencies:\n"
            + "- compile: com.github.jomof:vectorial:0.0.0-rev11\n",
        yaml, StandardCharsets.UTF_8);
    String result = main("-wf", yaml.getParent());
    System.out.printf(result);
  }

  @Test
  public void runMathfu() throws Exception {
    CDepYml config = new CDepYml();
    System.out.printf(new Yaml().dump(config));
    File yaml = new File(".test-files/runMathfu/cdep.yml");
    yaml.getParentFile().mkdirs();
    Files.write("builders: [cmake, cmakeExamples]\n"
            + "dependencies:\n"
            + "- compile: com.github.jomof:mathfu:1.0.2-rev7\n",
        yaml, StandardCharsets.UTF_8);
    String result = main("-wf", yaml.getParent());
    System.out.printf(result);
  }

  @Test
  public void testMissingGithubCoordinate() throws Exception {
    CDepYml config = new CDepYml();
    System.out.printf(new Yaml().dump(config));
    File yaml = new File(".test-files/runMathfu/cdep.yml");
    yaml.getParentFile().mkdirs();
    Files.write("builders: [cmake, cmakeExamples]\n"
            + "dependencies:\n"
            + "- compile: com.github.jomof:mathfoo:1.0.2-rev7\n",
        yaml, StandardCharsets.UTF_8);
    try {
      String result = main("-wf", yaml.getParent());
      System.out.printf(result);
      fail("Expected an exception");
    } catch (RuntimeException e) {
      assertThat(e).hasMessage("Could not resolve 'com.github.jomof:mathfoo:1.0.2-rev7'. "
          + "It doesn't exist.");
    }
  }

  @Test
  public void emptyCdepSha256() throws Exception {
    CDepYml config = new CDepYml();
    System.out.printf(new Yaml().dump(config));
    File yaml = new File(".test-files/emptyCdepSha256/cdep.yml");
    File yamlSha256 = new File(".test-files/emptyCdepSha256/cdep.sha256");
    yaml.getParentFile().mkdirs();
    Files.write("builders: [cmake, cmakeExamples]\n"
            + "dependencies:\n"
            + "- compile: com.github.jomof:mathfu:1.0.2-rev7\n",
        yaml, StandardCharsets.UTF_8);
    Files.write("# This file is automatically maintained by CDep.\n"
            + "#\n"
            + "#     MANUAL EDITS WILL BE LOST ON THE NEXT CDEP RUN\n"
            + "#\n"
            + "# This file contains a list of CDep coordinates along with the SHA256 hash of their\n"
            + "# manifest file. This is to ensure that a manifest hasn't changed since the last\n"
            + "# time CDep ran.\n"
            + "# The recommended best practice is to check this file into source control so that\n"
            + "# anyone else who builds this project is guaranteed to get the same dependencies.\n"
            + "\n"
            + "\n",
        yamlSha256, StandardCharsets.UTF_8);
    String result = main("-wf", yaml.getParent());
    System.out.printf(result);
  }

  @Test
  public void unfindableLocalFile() throws Exception {
    CDepYml config = new CDepYml();
    System.out.printf(new Yaml().dump(config));
    File yaml = new File(".test-files/unfindableLocalFile/cdep.yml");
    yaml.getParentFile().mkdirs();
    Files.write("builders: [cmake, cmakeExamples]\n"
            + "dependencies:\n"
            + "- compile: ../not-a-file/cdep-manifest.yml\n",
        yaml, StandardCharsets.UTF_8);

    try {
      main("-wf", yaml.getParent());
      fail("Expected failure");
    } catch (RuntimeException e) {
      assertThat(e).hasMessage("Could not resolve '../not-a-file/cdep-manifest.yml'."
          + " It doesn't exist.");
    }
  }

  @Test
  public void someKnownUrls() throws Exception {
    CDepYml config = new CDepYml();
    System.out.printf(new Yaml().dump(config));
    File yaml = new File(".test-files/someKnownUrls/cdep.yml");
    yaml.getParentFile().mkdirs();
    Files.write("builders: [cmake, cmakeExamples]\n"
            + "dependencies:\n"
//                + "- compile: com.github.jomof:boost:1.0.63-rev12\n"
//                + "- compile: com.github.jomof:cmakeify:0.0.70\n"
            + "- compile: com.github.jomof:mathfu:1.0.2-rev7\n"
            + "- compile: https://github.com/jomof/cmakeify/releases/download/0.0.81/cdep-manifest.yml\n"
            + "- compile: com.github.jomof:low-level-statistics:0.0.16\n",
        yaml, StandardCharsets.UTF_8);
    String result1 = main("show", "manifest", "-wf", yaml.getParent());
    yaml.delete();
    Files.write(result1, yaml, StandardCharsets.UTF_8);
    System.out.print(result1);
    String result2 = main("show", "manifest", "-wf", yaml.getParent());
    assertThat(result2).isEqualTo(result1);
    assertThat(result2).contains("0.0.81");
    String result3 = main("-wf", yaml.getParent());
  }

//    @Test
//    public void firebase() throws Exception {
//        CDepYml config = new CDepYml();
//        System.out.printf(new Yaml().dump(config));
//        File yaml = new File(".test-files/firebase/cdep.yml");
//        yaml.getParentFile().mkdirs();
//        Files.write("builders: [cmake, cmakeExamples]\n"
//                + "dependencies:\n"
//                + "- compile: /usr/local/google/home/jomof/projects/firebase/.deploy/"
//                + "com.jomofisher/firebase/2.1.3-rev1/cdep-manifest-storage.yml\n",
//            yaml, StandardCharsets.UTF_8);
//        String result1 = main("show", "manifest", "-wf", yaml.getParent());
//        yaml.delete();
//        Files.write(result1, yaml, StandardCharsets.UTF_8);
//        System.out.print(result1);
//        String result = main("-wf", yaml.getParent());
//        System.out.printf(result);
//    }

  @Test
  public void redownload() throws Exception {
    CDepYml config = new CDepYml();
    File yaml = new File(".test-files/simpleDependency/cdep.yml");
    yaml.getParentFile().mkdirs();
    Files.write("builders: [cmake, cmakeExamples]\n"
            + "dependencies:\n"
            + "- compile: com.github.jomof:low-level-statistics:0.0.16\n",
        yaml, StandardCharsets.UTF_8);
    // Download first.
    main("-wf", yaml.getParent());
    // Redownload
    String result = main("redownload", "-wf", yaml.getParent());
    System.out.printf(result);
    assertThat(result).contains("Redownload");
  }

  @Test
  public void createHashes() throws Exception {
    File yaml = new File(".test-files/simpleDependency/cdep.yml");
    yaml.getParentFile().mkdirs();
    Files.write("builders: [cmake, cmakeExamples]\n"
            + "dependencies:\n"
            + "- compile: com.github.jomof:low-level-statistics:0.0.16\n",
        yaml, StandardCharsets.UTF_8);
    String text = main("create", "hashes", "-wf", yaml.getParent());
    assertThat(text).contains("Created cdep.sha256");
    File hashFile = new File(".test-files/simpleDependency/cdep.sha256");
    assertThat(hashFile.isFile());
  }

  @Test
  public void checkThatHashesWork() throws Exception {
    File yaml = new File(".test-files/checkThatHashesWork/cdep.yml");
    yaml.getParentFile().mkdirs();
    Files.write("builders: [cmake, cmakeExamples]\n"
            + "dependencies:\n"
            + "- compile: com.github.jomof:low-level-statistics:0.0.16\n",
        yaml, StandardCharsets.UTF_8);
    File hashes = new File(".test-files/checkThatHashesWork/cdep.sha256");
    Files.write("- coordinate: com.github.jomof:low-level-statistics:0.0.16\n" +
            "  sha256: dogbone",
        hashes, StandardCharsets.UTF_8);
    try {
      main("-wf", yaml.getParent());
      fail("Expected failure");
    } catch (RuntimeException e) {
      assertThat(e).hasMessage("SHA256 of cdep-manifest.yml for package " +
          "'com.github.jomof:low-level-statistics:0.0.16' does not agree with value in cdep.sha256. "
          +
          "Something changed.");
    }
  }

  @Test
  public void noDependencies() throws Exception {
    CDepYml config = new CDepYml();
    System.out.printf(new Yaml().dump(config));
    File yaml = new File(".test-files/simpleDependency/cdep.yml");
    yaml.getParentFile().mkdirs();
    Files.write("builders: [cmake, cmakeExamples]\n"
            + "dependencies:\n",
        yaml, StandardCharsets.UTF_8);
    String result1 = main("-wf", yaml.getParent());
    System.out.printf(result1);
    assertThat(result1).contains("Nothing");
  }

  @Test
  public void dumpIsSelfHost() throws Exception {
    System.out.printf("%s\n", System.getProperty("user.home"));
    CDepYml config = new CDepYml();
    System.out.printf(new Yaml().dump(config));
    File yaml = new File(".test-files/simpleConfiguration/cdep.yml");
    yaml.getParentFile().mkdirs();
    Files.write("builders: [cmake, cmakeExamples]", yaml, StandardCharsets.UTF_8);
    String result1 = main("show", "manifest", "-wf", yaml.getParent());
    yaml.delete();
    Files.write(result1, yaml, StandardCharsets.UTF_8);
    System.out.print(result1);
    String result2 = main("show", "manifest", "-wf", yaml.getParent());
    assertThat(result2).isEqualTo(result1);
  }

  @Test
  public void testNakedCall() throws Exception {
    main();
  }

  @Test
  public void showFolders() throws Exception {
    String result = main("show", "folders");
    System.out.printf(result);
  }

  @Test
  public void help() throws Exception {
    String result = main("--help");
    System.out.printf(result);
    assertThat(result).contains("show folders");
  }

  @Test
  public void testWrapper() throws Exception {
    File testFolder = new File(".test-files/testWrapper");
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

  @Test
  public void localPathsWork() throws Exception {
    File yaml = new File(".test-files/localPathsWork/cdep.yml");
    yaml.getParentFile().mkdirs();
    Files.write("builders: [cmake, cmakeExamples]\n"
            + "dependencies:\n"
            + "- compile: com.github.jomof:low-level-statistics:0.0.16\n",
        yaml, StandardCharsets.UTF_8);
    // Download everything
    String resultRemote = main("-wf", yaml.getParent());
    // Ask for the local path to the manifest.
    String localPath = main("show", "local", "com.github.jomof:low-level-statistics:0.0.16");
    assertThat(localPath).contains("cdep-manifest.yml");
    // Write a new manifest with the local path.
    Files.write(String.format("builders: [cmake, cmakeExamples]\n"
            + "dependencies:\n"
            + "- compile: %s\n", localPath),
        yaml, StandardCharsets.UTF_8);
    String resultLocal = main("-wf", yaml.getParent(), "-df",
        new File(yaml.getParent(), "downloads").getPath());
    System.out.print(resultLocal);
    resultLocal = main("-wf", yaml.getParent());
    System.out.print(resultLocal);
  }
}