package io.cdep.cdep.yml;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.fail;

import io.cdep.cdep.ResolvedManifests;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;
import io.cdep.cdep.yml.cdepmanifest.MergeCDepManifestYmls;
import java.net.MalformedURLException;
import org.junit.Test;

public class TestMergeCDepManifestYmls {

  @Test
  public void testJustCoordinate() throws MalformedURLException {
    try {
      MergeCDepManifestYmls.merge(ResolvedManifests.sqlite().cdepManifestYml,
          ResolvedManifests.admob().cdepManifestYml);
      fail("Expected exception");
    } catch (RuntimeException e) {
      assertThat(e).hasMessage("Manifests were different at artifactId.coordinate.[value]");
    }
  }

  @Test
  public void testMergeAndroidiOS() throws MalformedURLException {
    CDepManifestYml result = MergeCDepManifestYmls
        .merge(ResolvedManifests.sqliteAndroid().cdepManifestYml,
            ResolvedManifests.sqliteiOS().cdepManifestYml);
  }
}
