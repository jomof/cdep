package io.cdep.cdep.fullfill;

import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
  public void testBasic() throws IOException {
    File templates[] = templates(new File("../third_party/stb/cdep/"));
    File output = new File(".test-files/TestFullfill").getAbsoluteFile();
    Fullfill.multiple(templates, output, new File("../third_party/stb"), "1.2.3");
  }
}