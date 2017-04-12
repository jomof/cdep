package io.cdep.cdep.fullfill;

import io.cdep.cdep.ResolvedManifests;
import io.cdep.cdep.utils.CDepManifestYmlUtils;
import io.cdep.cdep.utils.FileUtils;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

public class TestSubstituteStringsRewritingVisitor {
  @Test
  public void testBasic() throws IOException {
    CDepManifestYml before = CDepManifestYmlUtils.convertStringToManifest(
        FileUtils.readAllText(new File("../third_party/stb/cdep/cdep-manifest-divide.yml")));
    CDepManifestYml after = new SubstituteStringsRewritingVisitor()
        .replace("${version}", "0.0.0")
        .visitCDepManifestYml(before);
    assertThat(after.coordinate.version.value).isEqualTo("0.0.0");
  }

  @Test
  public void testAllResolvedManifests() throws Exception {
    for (ResolvedManifests.NamedManifest manifest : ResolvedManifests.all()) {
      new SubstituteStringsRewritingVisitor()
          .replace("a", "b")
          .visitCDepManifestYml(manifest.resolved.cdepManifestYml);
    }
  }
}