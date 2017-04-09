package io.cdep.cdep.fullfill;

import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class TestFullfill {

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
    Fullfill.multiple(templates, output, new File("../third_party/stb"), "1.2.3");
  }

  @Test
  public void testBasicTinyDir() throws IOException {
    File templates[] = templates(
        new File("../third_party/tinydir/"));
    File output = new File(".test-files/testBasicTinyDir").getAbsoluteFile();
    List<File> result = Fullfill.multiple(templates, output, new File("../third_party/tinydir"), "1.2.3");
    assertThat(result).hasSize(2);
  }
}