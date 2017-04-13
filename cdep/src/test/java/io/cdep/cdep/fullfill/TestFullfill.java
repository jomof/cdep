package io.cdep.cdep.fullfill;

import io.cdep.cdep.ResolvedManifests;
import io.cdep.cdep.generator.GeneratorEnvironment;
import io.cdep.cdep.utils.CDepManifestYmlUtils;
import io.cdep.cdep.utils.FileUtils;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

public class TestFullfill {
  private final GeneratorEnvironment environment = new GeneratorEnvironment(
      new File("./test-files/TestFullfill/working"),
      null,
      false,
      false);

  File[] templates(File... folders) {
    List<File> templates = new ArrayList<>();
    for (File folder : folders) {
      for (File file : folder.listFiles(new FileFilter() {
        @Override
        public boolean accept(File pathname) {
          return pathname.getName().startsWith("cdep-manifest");
        }
      })) {
        templates.add(file);
      }
    }
    return templates.toArray(new File[templates.size()]);
  }

  @Test
  public void testBasicSTB() throws IOException {
    File templates[] = templates(
        new File("../third_party/stb/cdep/"));
    File output = new File(".test-files/testBasicSTB").getAbsoluteFile();
    Fullfill.multiple(environment, templates, output, new File("../third_party/stb"), "1.2.3");
  }

  @Test
  public void testBasicTinyDir() throws IOException {
    File templates[] = templates(
        new File("../third_party/tinydir/"));
    File output = new File(".test-files/testBasicTinyDir").getAbsoluteFile();
    List<File> result = Fullfill.multiple(environment, templates, output, new File("../third_party/tinydir"), "1.2.3");
    assertThat(result).hasSize(2);
  }

  @Test
  public void testBasicVectorial() throws IOException {
    File templates[] = templates(
        new File("../third_party/vectorial/cdep"));
    File output = new File(".test-files/testBasicVectorial").getAbsoluteFile();
    List<File> result = Fullfill.multiple(environment, templates, output, new File("../third_party/vectorial"), "1.2.3");
    assertThat(result).hasSize(2);
  }

  @Test
  public void testBasicMathFu() throws IOException {
    File templates[] = templates(
        new File("../third_party/mathfu/cdep"));
    File output = new File(".test-files/testBasicMathFu").getAbsoluteFile();
    List<File> result = Fullfill.multiple(environment, templates, output, new File("../third_party/mathfu"), "1.2.3");
    assertThat(result).hasSize(2);
    File manifestFile = new File(output, "layout");
    manifestFile = new File(manifestFile, "cdep-manifest.yml");
    CDepManifestYml manifest = CDepManifestYmlUtils.convertStringToManifest(FileUtils.readAllText(manifestFile));
    assertThat(manifest.dependencies[0].sha256).isNotNull();
    assertThat(manifest.dependencies[0].sha256).isNotEmpty();
  }

  @Test
  public void testAllResolvedManifests() throws Exception {
    Map<String, String> expected = new HashMap<>();
    expected.put("sqliteLinuxMultiple", "Package 'com.github.jomof:sqlite:0.0.0' has multiple linux archives. Only one is allowed.");
    expected.put("archiveMissingSize", "Archive com.github.jomof:vectorial:0.0.0 is missing size or it is zero");
    expected.put("archiveMissingFile", "Archive com.github.jomof:vectorial:0.0.0 is missing file");
    expected.put("admob", "Archive com.github.jomof:firebase/admob:2.1.3-rev8 is missing include");
    expected.put("sqlite", "Package 'com.github.jomof:sqlite:0.0.0' contains multiple references to the same archive file 'sqlite-android-cxx-platform-12.zip'");
    expected.put("sqliteAndroid", "Package 'com.github.jomof:sqlite:3.16.2-rev33' contains multiple references to the same archive file 'sqlite-android-cxx-platform-12.zip'");
    expected.put("archiveMissingSha256", "Could not hash file bob.zip because it didn't exist");
    expected.put("indistinguishableAndroidArchives", "Android archive com.github.jomof:firebase/"
        + "app:0.0.0 file archive2.zip is indistinguishable at build time from archive1.zip given "
        + "the information in the manifest");
    boolean unexpectedFailure = false;

    for (ResolvedManifests.NamedManifest manifest : ResolvedManifests.all()) {
      File outputFolder = new File(".test-files/TestFullfill/testAllResolvedManifests/"
          + manifest.name).getAbsoluteFile();
      outputFolder.delete();
      outputFolder.mkdirs();

      String key = manifest.name;
      String expectedFailure = expected.get(key);

      File manifestFile = new File(outputFolder, "cdep-manifest.yml");
      FileUtils.writeTextToFile(manifestFile, manifest.body);
      try {
        Fullfill.multiple(
            environment,
            new File[] {manifestFile},
            new File(outputFolder, "output"),
            new File(outputFolder, "source"),
            "1.2.3");
        if (expectedFailure != null) {
          fail("Expected failure");
        }
      } catch (RuntimeException e) {
        if (! (e.getClass().equals(RuntimeException.class))) {
          throw e;
        }
        if (e.getMessage() == null) {
          throw e;
        }
        if (e.getMessage().contains("Could not zip file")) {
          continue;
        }
        if (!e.getMessage().equals(expectedFailure)) {
          e.printStackTrace();
          System.out.printf("expected.put(\"%s\", \"%s\");\n", key, e.getMessage());
          unexpectedFailure = true;
        }
      }
    }
    if (unexpectedFailure) {
      throw new RuntimeException("Unexpected failures. See console.");
    }
  }



}