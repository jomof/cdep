package io.cdep.cdep.yml;

import io.cdep.cdep.ResolvedManifests;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.cdep.cdep.yml.CDepManifestBuilder.archive;
import static io.cdep.cdep.yml.CDepManifestBuilder.hardname;
import static io.cdep.cdep.yml.cdepmanifest.CreateCDepManifestYmlString.create;

public class TestCreateCDepManifestYmlString {

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
    CDepManifestYml cdepManifestYml = ResolvedManifests.sqlite().cdepManifestYml;
    String result = create(cdepManifestYml);
    System.out.printf(result);

  }
}
