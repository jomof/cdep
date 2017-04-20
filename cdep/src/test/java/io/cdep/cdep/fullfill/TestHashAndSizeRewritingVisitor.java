package io.cdep.cdep.fullfill;

import static com.google.common.truth.Truth.assertThat;

import io.cdep.cdep.utils.CDepManifestYmlUtils;
import io.cdep.cdep.utils.FileUtils;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;
import java.io.File;
import java.io.IOException;
import org.junit.Test;

public class TestHashAndSizeRewritingVisitor {
  @Test
  public void testBasic() throws IOException {
    CDepManifestYml before = CDepManifestYmlUtils.convertStringToManifest(
        FileUtils.readAllText(new File("../third_party/stb/cdep/cdep-manifest-divide.yml")));

    CDepManifestYml afterSubstitution = new SubstituteStringsRewriter()
        .replace("${source}", new File("../third_party/stb").getAbsolutePath())
        .visitCDepManifestYml(before);

    File outputFolder = new File(".test-files/testHashAndSize").getAbsoluteFile();
    outputFolder.delete();

    File layout = new File(outputFolder, "layout");
    layout.delete();
    layout.mkdirs();

    File staging = new File(outputFolder, "staging");
    staging.delete();
    staging.mkdirs();

    ZipFilesRewriter zipper = new ZipFilesRewriter(layout, staging);
    CDepManifestYml afterZipping = zipper.visitCDepManifestYml(afterSubstitution);

    assertThat(layout.isDirectory()).isTrue();
    assertThat(afterZipping.interfaces.headers.file).isEqualTo("stb-divide-headers.zip");
    assertThat(afterZipping.interfaces.headers.include).isEqualTo("include");

    CDepManifestYml afterHashing = new FileHashAndSizeRewriter(layout)
        .visitCDepManifestYml(afterZipping);

    assertThat(afterHashing.interfaces.headers.sha256).isNotNull();
    assertThat(afterHashing.interfaces.headers.size).isNotNull();
  }
}