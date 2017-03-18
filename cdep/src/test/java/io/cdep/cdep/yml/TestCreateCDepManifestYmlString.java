package io.cdep.cdep.yml;

import io.cdep.cdep.ResolvedManifests;
import io.cdep.cdep.utils.CDepManifestYmlUtils;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYmlEquality;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.cdep.cdep.yml.cdepmanifest.CDepManifestBuilder.archive;
import static io.cdep.cdep.yml.cdepmanifest.CDepManifestBuilder.hardname;
import static io.cdep.cdep.yml.cdepmanifest.CreateCDepManifestYmlString.create;

public class TestCreateCDepManifestYmlString {

  private static void check(CDepManifestYml manifest) {
    // Convert to string
    String result = create(manifest);

    // Convert from string
    CDepManifestYml manifest2 = CDepManifestYmlUtils.convertStringToManifest(result);

    // Would like to compare equality here.
    assertThat(CDepManifestYmlEquality.areDeeplyIdentical(manifest, manifest2)).isTrue();
  }

  @Test
  public void testSimple() {

    assertThat(create(hardname("nameval", "shaval"))).isEqualTo(
        "compile: nameval\n" +
            "sha256: shaval\n"
    );
  }

  @Test
  public void testArchive() {
    assertThat(create(archive("fileval", "shaval", 100))).isEqualTo(
        "file: fileval\n" +
            "sha256: shaval\n" +
            "size: 100\n"
    );
  }

  @Test
  public void testSqlite() throws Exception {
    check(ResolvedManifests.sqlite().cdepManifestYml);
  }

  @Test
  public void testAdmob() throws Exception {
    check(ResolvedManifests.admob().cdepManifestYml);
  }
}
