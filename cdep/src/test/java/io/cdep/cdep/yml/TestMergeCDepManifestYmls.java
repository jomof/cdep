package io.cdep.cdep.yml;

import io.cdep.cdep.ResolvedManifests;
import io.cdep.cdep.utils.CDepManifestYmlUtils;
import io.cdep.cdep.yml.cdepmanifest.*;
import org.junit.Test;

import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static io.cdep.cdep.utils.Invariant.notNull;
import static junit.framework.TestCase.fail;

public class TestMergeCDepManifestYmls {

  @Test
  public void testJustCoordinate() throws MalformedURLException {
    try {
      MergeCDepManifestYmls.merge(ResolvedManifests.sqlite().cdepManifestYml, ResolvedManifests.admob().cdepManifestYml);
      fail("Expected exception");
    } catch (RuntimeException e) {
      assertThat(e).hasMessage("Manifests were different at artifactId.coordinate.[value]");
    }
  }

  @Test
  public void testMergeAndroidiOS() throws MalformedURLException {
    CDepManifestYml iOSManifest = ResolvedManifests.sqliteiOS().cdepManifestYml;
    CDepManifestYml androidManifest = ResolvedManifests.sqliteAndroid().cdepManifestYml;
    CDepManifestYml result = MergeCDepManifestYmls.merge(androidManifest, iOSManifest);
    iOS iOS = notNull(result.iOS);
    assert iOSManifest.iOS != null;
    assert iOSManifest.iOS.archives != null;
    assertThat(iOS.archives).hasLength(iOSManifest.iOS.archives.length);
    Android android = notNull(result.android);
    assert androidManifest.android != null;
    assert androidManifest.android.archives != null;
    assertThat(android.archives).hasLength(androidManifest.android.archives.length);
  }

  @Test
  public void testTwoWayMerges() throws Exception {
    Set<String> commonDifferences = new HashSet<>();
    commonDifferences.add("Manifests were different at artifactId.coordinate.[value]");
    commonDifferences.add("Manifests were different at file.headers.interfaces.[value]");
    commonDifferences.add("Manifests were different at include.headers.interfaces.[value]");
    commonDifferences.add("Manifests were different at size.headers.interfaces.[value]");
    commonDifferences.add("Manifests were different at sha256.archive.[value]");
    commonDifferences.add("Manifests were different at value.version.coordinate.[value]");
    boolean somethingUnexpected = false;
    for (ResolvedManifests.NamedManifest manifest1 : ResolvedManifests.all()) {
      for (ResolvedManifests.NamedManifest manifest2 : ResolvedManifests.all()) {
        String key = manifest1.name + "-" + manifest2.name;
        try {
          CDepManifestYml merged1 = MergeCDepManifestYmls.merge(manifest1.resolved.cdepManifestYml, manifest2.resolved
              .cdepManifestYml);
          String string = CreateCDepManifestYmlString.create(merged1);
          CDepManifestYml merged2 = CDepManifestYmlUtils.convertStringToManifest(string);
          if (!CDepManifestYmlEquality.areDeeplyIdentical(merged1, merged2)) {
            assertThat(string).isEqualTo(CreateCDepManifestYmlString.create(merged2));
            CDepManifestYmlEquality.areDeeplyIdentical(merged1, merged2);
            fail("Converted string wasn't the same as original");
          }
        } catch (RuntimeException e) {
          if (!e.getClass().equals(RuntimeException.class)) {
            throw e;
          }
          String actual = e.getMessage();
          if (!commonDifferences.contains(actual)) {
             e.printStackTrace();
            System.out.printf("expected.put(\"%s\", \"%s\");\n", key, actual);
            somethingUnexpected = true;
          }
        }
      }
    }

    if (somethingUnexpected) {
      throw new RuntimeException("Saw unexpected results. See console.");
    }
  }

  @Test
  public void mergeAndroidiOSLinux() throws Exception {
    CDepManifestYml result = MergeCDepManifestYmls.merge(ResolvedManifests.sqlite().cdepManifestYml, ResolvedManifests
        .sqliteLinux().cdepManifestYml);
    assertThat(result.linux).isNotNull();
    assertThat(result.linux.archives).isNotEmpty();
    assertThat(result.linux.archives).hasLength(1);
  }
}
