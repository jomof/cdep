package io.cdep.cdep.fullfill;

import io.cdep.cdep.utils.CDepManifestYmlUtils;
import io.cdep.cdep.utils.FileUtils;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

public class TestHashAndSizeRewritingVisitor {
  @Test
  public void testBasic() throws IOException {
    CDepManifestYml before = CDepManifestYmlUtils.convertStringToManifest(
        FileUtils.readAllText(new File("../third_party/stb/cdep/cdep-manifest-divide.yml")));

    CDepManifestYml afterSubstitution = new SubstituteStringsRewritingVisitor()
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

    ZipFilesRewritingVisitor zipper = new ZipFilesRewritingVisitor(layout, staging);
    CDepManifestYml afterZipping = zipper.visitCDepManifestYml(afterSubstitution);

    assertThat(layout.isDirectory()).isTrue();
    assertThat(afterZipping.interfaces.headers.file).isEqualTo("stb-divide-header.zip");
    assertThat(afterZipping.interfaces.headers.include).isEqualTo("include");

    CDepManifestYml afterHashing = new FileHashAndSizeRewritingVisitor(layout)
        .visitCDepManifestYml(afterZipping);

    assertThat(afterHashing.interfaces.headers.sha256).isNotNull();
    assertThat(afterHashing.interfaces.headers.size).isNotNull();
  }
}