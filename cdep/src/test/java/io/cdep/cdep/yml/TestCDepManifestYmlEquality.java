package io.cdep.cdep.yml;

import io.cdep.cdep.ResolvedManifests;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYmlEquality;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class TestCDepManifestYmlEquality {

  private static void checkEquals(CDepManifestYml left, CDepManifestYml right) {
    assertThat(CDepManifestYmlEquality.areDeeplyIdentical(left, right)).isTrue();
  }

  private static void checkNotEquals(CDepManifestYml left, CDepManifestYml right) {
    assertThat(CDepManifestYmlEquality.areDeeplyIdentical(left, right)).isFalse();
  }

  @Test
  public void testSqlite() throws Exception {
    checkEquals(ResolvedManifests.sqlite().manifest.cdepManifestYml, ResolvedManifests.sqlite().manifest.cdepManifestYml);
  }

  @Test
  public void testAdmob() throws Exception {
    checkEquals(ResolvedManifests.admob().manifest.cdepManifestYml, ResolvedManifests.admob().manifest.cdepManifestYml);
  }

  @Test
  public void testSqliteAdmob() throws Exception {
    checkNotEquals(ResolvedManifests.sqlite().manifest.cdepManifestYml, ResolvedManifests.admob().manifest.cdepManifestYml);
  }

  @Test
  public void testAdmobSqlite() throws Exception {
    checkNotEquals(ResolvedManifests.admob().manifest.cdepManifestYml, ResolvedManifests.sqlite().manifest.cdepManifestYml);
  }

  @Test
  public void testEqualityPairs() throws Exception {
    for (ResolvedManifests.NamedManifest manifest1 : ResolvedManifests.all()) {
      for (ResolvedManifests.NamedManifest manifest2 : ResolvedManifests.all()) {
        boolean expected = manifest1.name.equals(manifest2.name);
        assertThat(CDepManifestYmlEquality.areDeeplyIdentical(manifest1.resolved.cdepManifestYml, manifest2.resolved
            .cdepManifestYml)).isEqualTo(expected);
      }
    }
  }
}

