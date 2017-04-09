package io.cdep.cdep.fullfill;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class TestFullfill {
  @Test
  public void testBasic() throws IOException {
    File templates[] = new File[]{
        new File("../third_party/stb/cdep/cdep-manifest-divide.yml"),
        new File("../third_party/stb/cdep/cdep-manifest-c_lexer.yml")
    };
    File output = new File(".test-files/TestFullfill").getAbsoluteFile();
    Fullfill.multiple(templates, output, new File("../third_party/stb"), "1.2.3");
  }
}