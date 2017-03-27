package io.cdep.cdep.yml;

import io.cdep.cdep.ResolvedManifests;
import io.cdep.cdep.utils.CDepManifestYmlUtils;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYmlEquality;
import io.cdep.cdep.yml.cdepmanifest.CreateCDepManifestYmlString;
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
  public void testSimple1() {
    assertThat(create(hardname("nameval", "shaval"))).isEqualTo("compile: nameval\r\n" + "sha256: shaval\r\n");
  }

  @Test
  public void testSimple2() {

    assertThat(create(hardname("nameval2", "shaval2"))).isEqualTo("compile: nameval2\r\n" + "sha256: shaval2\r\n");
  }

  @Test
  public void testArchive() {
    assertThat(create(archive("fileval", "shaval", 100, "hello"))).isEqualTo("file: fileval\r\n" + "sha256: shaval\r\n" + "size: 100\r\n" + "include: " +
        "hello\r\n");
  }

  @Test
  public void testSqlite() throws Exception {
    check(ResolvedManifests.sqlite().cdepManifestYml);
  }

  @Test
  public void testAdmob() throws Exception {
    check(ResolvedManifests.admob().cdepManifestYml);
  }

  @Test
  public void testAllResolvedManifests() throws Exception {
    for (ResolvedManifests.NamedManifest manifest : ResolvedManifests.all()) {
      CreateCDepManifestYmlString.create(manifest.resolved.cdepManifestYml);
    }
  }
}
