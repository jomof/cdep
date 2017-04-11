package io.cdep.cdep.fullfill;

import io.cdep.cdep.generator.GeneratorEnvironment;
import io.cdep.cdep.utils.CDepManifestYmlUtils;
import io.cdep.cdep.utils.FileUtils;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

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
}