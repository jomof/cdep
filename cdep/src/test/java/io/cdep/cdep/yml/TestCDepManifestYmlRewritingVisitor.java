package io.cdep.cdep.yml;

import io.cdep.cdep.ResolvedManifests;
import io.cdep.cdep.utils.CDepManifestYmlUtils;
import io.cdep.cdep.utils.FileUtils;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYmlRewritingVisitor;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class TestCDepManifestYmlRewritingVisitor {

  @Test
  public void testBasic() throws IOException {
    CDepManifestYml before = CDepManifestYmlUtils.convertStringToManifest(
        FileUtils.readAllText(new File("../third_party/stb/cdep/cdep-manifest-divide.yml")));
    CDepManifestYml after = new CDepManifestYmlRewritingVisitor().visitCDepManifestYml(before);
  }

  @Test
  public void testAllResolvedManifests() throws Exception {
    for (ResolvedManifests.NamedManifest manifest : ResolvedManifests.all()) {
      new CDepManifestYmlRewritingVisitor().visitCDepManifestYml(manifest.resolved.cdepManifestYml);
    }
  }
}
