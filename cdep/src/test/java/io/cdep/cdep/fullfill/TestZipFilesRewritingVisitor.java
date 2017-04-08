package io.cdep.cdep.fullfill;

import io.cdep.cdep.utils.CDepManifestYmlUtils;
import io.cdep.cdep.utils.FileUtils;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class TestZipFilesRewritingVisitor {
  @Test
  public void testBasic() throws IOException {
    CDepManifestYml before = CDepManifestYmlUtils.convertStringToManifest(
        FileUtils.readAllText(new File("../third_party/stb/cdep/cdep-manifest-divide.yml")));

    CDepManifestYml afterSubstitution = new SubstituteStringsRewritingVisitor()
        .replace("${wf}", new File("../third_party/stb").getAbsolutePath())
        .visitCDepManifestYml(before);

    File output = new File(".test-files/testZipFullfill").getAbsoluteFile();

    CDepManifestYml afterZipping = new ZipFilesRewritingVisitor(output)
        .visitCDepManifestYml(afterSubstitution);

  }
}