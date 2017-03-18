package io.cdep.cdep.yml;

import io.cdep.cdep.ResolvedManifests;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;
import io.cdep.cdep.yml.cdepmanifest.MergeCDepManifestYmls;
import org.junit.Test;

import java.net.MalformedURLException;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.fail;

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
    CDepManifestYml ios = ResolvedManifests.sqliteiOS().cdepManifestYml;
    CDepManifestYml android = ResolvedManifests.sqliteAndroid().cdepManifestYml;
    CDepManifestYml result = MergeCDepManifestYmls.merge(android, ios);
    assertThat(result.iOS.archives).hasLength(ios.iOS.archives.length);
    assertThat(result.android.archives).hasLength(android.android.archives.length);
  }
}
