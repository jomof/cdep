package io.cdep.cdep.yml;

import io.cdep.annotations.NotNull;
import io.cdep.cdep.ResolvedManifests;
import io.cdep.cdep.utils.CDepManifestYmlUtils;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYmlEquality;
import io.cdep.cdep.yml.cdepmanifest.CxxLanguageFeatures;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.cdep.cdep.yml.cdepmanifest.CDepManifestBuilder.archive;
import static io.cdep.cdep.yml.cdepmanifest.CDepManifestBuilder.hardname;
import static io.cdep.cdep.yml.cdepmanifest.CreateCDepManifestYmlString.serialize;
import static io.cdep.cdep.yml.cdepmanifest.CxxLanguageFeatures.cxx_alignas;
import static io.cdep.cdep.yml.cdepmanifest.CxxLanguageFeatures.cxx_auto_type;

public class TestCreateCDepManifestYmlString {

  private static void check(@NotNull CDepManifestYml manifest) {
    // Convert to constant
    String result = CDepManifestYmlUtils.convertManifestToString(manifest);

    // Convert from constant
    CDepManifestYml manifest2 = CDepManifestYmlUtils.convertStringToManifest(result);

    // Would like to compare equality here.
    assertThat(CDepManifestYmlEquality.areDeeplyIdentical(manifest, manifest2)).isTrue();
  }

  @Test
  public void testSimple1() {
    assertThat(serialize(hardname("nameval", "shaval"))).isEqualTo("compile: nameval\r\n" + "sha256: shaval\r\n");
  }

  @Test
  public void testSimple2() {

    assertThat(serialize(hardname("nameval2", "shaval2"))).isEqualTo("compile: nameval2\r\n" + "sha256: shaval2\r\n");
  }

  @Test
  public void testArchive() {
    assertThat(serialize(archive("fileval", "shaval", 100, "hello", null))).isEqualTo("file: fileval\r\n" + "sha256: shaval\r\n" +
        "size: 100\r\n" + "include: " + "hello\r\n");
  }

  @Test
  public void testArchiveWithRequires() {
    assertThat(serialize(archive(
        "fileval",
        "shaval",
        100,
        "hello",
        new CxxLanguageFeatures[]{cxx_auto_type, cxx_alignas})))
        .isEqualTo(
            "file: fileval\r\n" +
                "sha256: shaval\r\n" +
                "size: 100\r\n" +
                "include: hello\r\n" +
                "requires: [cxx_auto_type, cxx_alignas]\r\n");
  }

  @Test
  public void testSqlite() throws Exception {
    check(ResolvedManifests.sqlite().getValue().cdepManifestYml);
  }

  @Test
  public void testAdmob() throws Exception {
    check(ResolvedManifests.admob().getValue().cdepManifestYml);
  }

  @Test
  public void testRequires() throws Exception {
    // Ensure that round-trip works
    String originalString = ResolvedManifests.simpleRequires().getKey();
    CDepManifestYml originalManifest = CDepManifestYmlUtils.convertStringToManifest(originalString);
    String convertedString = CDepManifestYmlUtils.convertManifestToString(originalManifest);
    CDepManifestYml convertedManifest = CDepManifestYmlUtils.convertStringToManifest(convertedString);
    assertThat(CDepManifestYmlEquality.areDeeplyIdentical(originalManifest, convertedManifest)).isTrue();
  }

  @Test
  public void testAllResolvedManifests() throws Exception {
    for (ResolvedManifests.NamedManifest manifest : ResolvedManifests.all()) {
      // Ensure that round-trip works
      String originalString = manifest.body;
      CDepManifestYml originalManifest = CDepManifestYmlUtils.convertStringToManifest(originalString);
      String convertedString = CDepManifestYmlUtils.convertManifestToString(originalManifest);
      CDepManifestYml convertedManifest = CDepManifestYmlUtils.convertStringToManifest(convertedString);
      assertThat(CDepManifestYmlEquality.areDeeplyIdentical(originalManifest, convertedManifest)).isTrue();
    }
  }
}
