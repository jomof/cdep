package io.cdep.cdep.yml;

import static com.google.common.truth.Truth.assertThat;

import io.cdep.cdep.ResolvedManifests;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYmlEquality;
import org.junit.Test;

public class TestCDepManifestYmlEquality {

  private static void checkEquals(CDepManifestYml left, CDepManifestYml right) {
    assertThat(CDepManifestYmlEquality.areDeeplyIdentical(left, right)).isTrue();
  }

  private static void checkNotEquals(CDepManifestYml left, CDepManifestYml right) {
    assertThat(CDepManifestYmlEquality.areDeeplyIdentical(left, right)).isFalse();
  }

  @Test
  public void testSqlite() throws Exception {
    checkEquals(ResolvedManifests.sqlite().cdepManifestYml,
        ResolvedManifests.sqlite().cdepManifestYml);
  }

  @Test
  public void testAdmob() throws Exception {
    checkEquals(ResolvedManifests.admob().cdepManifestYml,
        ResolvedManifests.admob().cdepManifestYml);
  }

  @Test
  public void testSqliteAdmob() throws Exception {
    checkNotEquals(ResolvedManifests.sqlite().cdepManifestYml,
        ResolvedManifests.admob().cdepManifestYml);
  }

  @Test
  public void testAdmobSqlite() throws Exception {
    checkNotEquals(ResolvedManifests.admob().cdepManifestYml,
        ResolvedManifests.sqlite().cdepManifestYml);
  }
}
