package io.cdep.cdep.utils;

import io.cdep.annotations.NotNull;
import io.cdep.cdep.ResolvedManifests;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;
import io.cdep.cdep.yml.cdepmanifest.MergeCDepManifestYmls;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

public class TestCDepManifestYmlUtils {

  @Test
  public void coverConstructor() {
    // Call constructor of tested class to cover that code.
    new CoverConstructor();
  }

  @Test
  public void empty() {
    try {
      check("");
      fail("Expected an exception");
    } catch (Exception e) {
      assertThat(e).hasMessage("Manifest was empty");
    }
  }

  @Test
  public void missingCoordinate() {
    try {
      check("coordinate:\n");
      fail("Expected an exception");
    } catch (Exception e) {
      assertThat(e).hasMessage("Manifest was missing coordinate");
    }
  }

  @Test
  public void noArtifactId() {
    try {
      check("coordinate:\n" + "  groupId: com.github.jomof\n");
      fail("Expected an exception");
    } catch (Exception e) {
      assertThat(e).hasMessage("Manifest was missing coordinate.artifactId");
    }
  }

  @Test
  public void noVersion() {
    try {
      check("coordinate:\n" + "  groupId: com.github.jomof\n" + "  artifactId: boost\n");
      fail("Expected an exception");
    } catch (Exception e) {
      assertThat(e).hasMessage("Manifest was missing coordinate.version");
    }
  }

  @Test
  public void noGroupId() {
    try {
      check("coordinate:\n" + "  artifactId: boost\n" + "  version: 1.0.63-rev10");
      fail("Expected an exception");
    } catch (Exception e) {
      assertThat(e).hasMessage("Manifest was missing coordinate.groupId");
    }
  }

  @Test
  public void noTargets() {
    try {
      check("coordinate:\n" + "  groupId: com.github.jomof\n" + "  artifactId: boost\n" + "  version: 1.0.63-rev10");
      fail("Expected an exception");
    } catch (Exception e) {
      assertThat(e).hasMessage("Package 'com.github.jomof:boost:1.0.63-rev10' does not contain any files");
    }
  }

  @Test
  public void malformedVersion() {
    try {
      check("coordinate:\n" + "  groupId: com.github.jomof\n" + "  artifactId: boost\n" + "  version: 1.0");
      fail("Expected an exception");
    } catch (Exception e) {
      assertThat(e).hasMessage("Package 'com.github.jomof:boost:1.0' has malformed version, " + "expected major.minor" +
          ".point[-tweak] but there was only one " + "dot");
    }
  }

  @Test
  public void duplicateAndroidZips() {
    //    try {
    //      check("coordinate:\n" + "  groupId: com.github.jomof\n" + "  artifactId: boost\n" + "  version: 1.0.63-rev10\n" +
    //          "android:\n" + "  archives:\n" + "  -" + " file: bob.zip\n" + "    size: 99\n" + "    sha256: " +
    //          "97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b\n" + "  - file: bob.zip\n" + "    size: 99\n"
    // + "  "  + "  sha256: 97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b\n");
    //      fail("Expected an exception");
    //    } catch (Exception e) {
    //      assertThat(e).hasMessage("Package 'com.github.jomof:boost:1.0.63-rev10' contains multiple references " + "to the
    // same " +
    //          "archive file 'bob.zip'");
    //    }
  }

  @Test
  public void duplicateiOSZips() {
    //    try {
    //      check("coordinate:\n" + "  groupId: com.github.jomof\n" + "  artifactId: boost\n" + "  version: 1.0.63-rev10\n" +
    //          "iOS:\n" + "  archives:\n" + "  - " + "file: bob.zip\n" + "    size: 99\n" + "    platform: iPhoneSimulator\n"
    // + "   " +
    //          "" + " sdk: 10.2\n" + "    architecture: i386\n" + "    sha256: " +
    //          "97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b\n" + "  - file: bob.zip\n" + "    size: 99\n"
    // + "  " +
    //          "" + "  platform: iPhoneSimulator\n" + "    sdk: 10.2\n" + "    architecture: i386\n" + "    sha256: " +
    //          "97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b\n");
    //      fail("Expected an exception");
    //    } catch (Exception e) {
    //      assertThat(e).hasMessage("Package 'com.github.jomof:boost:1.0.63-rev10' contains multiple references " + "to the
    // same " +
    //          "archive file 'bob.zip'");
    //    }
  }

  @Test
  public void duplicateZipsBetweenAndroidAndiOS() {
    //    try {
    //      check("coordinate:\n" +
    //          "  groupId: com.github.jomof\n" + "  artifactId: boost\n" + "  version: 1.0.63-rev10\n" +
    //          "android:\n" + "  archives:\n" + "  -" + " file: bob.zip\n" + "    size: 99\n" + "    sha256: " +
    // "97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b\n" + "iOS:\n" + "  " + "archives:\n" + "  - file: " +
    // "bob.zip\n" + "    size: 99\n" + "    platform: iPhoneSimulator\n" + "    sdk: 10.2\n" + "    architecture: i386\n" + "
    // " + "  sha256: 97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b\n");
    //      fail("Expected an exception");
    //    } catch (Exception e) {
    //      assertThat(e).hasMessage("Package 'com.github.jomof:boost:1.0.63-rev10' contains multiple references " + "to the
    // same " +
    //          "archive file 'bob.zip'");
    //    }
  }

  @Test
  public void emptyiOSArchive() {
    check("coordinate:\n" + "  groupId: com.github.jomof\n" + "  artifactId: boost\n" + "  version: 1.0.63-rev10\n" +
        "android:\n" + "  archives:\n" + "  - " + "file: bob.zip\n" + "    size: 99\n" + "    sha256: " +
        "97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b\n" + "iOS:\n" + "  archives:\n");
  }

  @Test
  public void emptyAndroidArchive() {
    check("coordinate:\n" + "  groupId: com.github.jomof\n" + "  artifactId: boost\n" + "  version: 1.0.63-rev10\n" + "iOS:\n"
        + "  archives:\n" + "  - file:" + " bob.zip\n" + "    size: 99\n" + "    sha256: " +
        "97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b\n" + "    platform: " + "iPhoneSimulator\n" + "    "
        + "sdk: 10.2\n" + "    architecture: i386\n" + "android:\n" + "  archives:\n");
  }

  @Test
  public void missingAndroidSha() {
    try {
      check("coordinate:\n" + "  groupId: com.github.jomof\n" + "  artifactId: boost\n" + "  version: 1.0.63-rev10\n" +
          "android:\n" + "  archives:\n" + "   " + " - file: bob.zip\n");
      fail("Expected an exception");
    } catch (Exception e) {
      assertThat(e).hasMessage("Package 'com.github.jomof:boost:1.0.63-rev10' has missing android.archive.sha256 for 'bob.zip'");
    }
  }

  @Test
  public void missingiOSSha() {
    try {
      check("coordinate:\n" + "  groupId: com.github.jomof\n" + "  artifactId: boost\n" + "  version: 1.0.63-rev10\n" +
          "iOS:\n" + "  archives:\n" + "    - " + "file: bob.zip\n");
      fail("Expected an exception");
    } catch (Exception e) {
      assertThat(e).hasMessage("Package 'com.github.jomof:boost:1.0.63-rev10' has missing ios.archive.sha256 for 'bob.zip'");
    }
  }

  @Test
  public void missingAndroidFile() {
    try {
      check("coordinate:\n" + "  groupId: com.github.jomof\n" + "  artifactId: boost\n" + "  version: 1.0.63-rev10\n" +
          "android:\n" + "  archives:\n" + "   " + " - sha256: " +
          "97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b" + "\n");
      fail("Expected an exception");
    } catch (Exception e) {
      assertThat(e).hasMessage("Package 'com.github.jomof:boost:1.0.63-rev10' has missing android.archive.file");
    }
  }

  @Test
  public void missingiOSFile() {
    try {
      check("coordinate:\n" + "  groupId: com.github.jomof\n" + "  artifactId: boost\n" + "  version: 1.0.63-rev10\n" +
          "iOS:\n" + "  archives:\n" + "    - " + "sha256: 97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b\n");
      fail("Expected an exception");
    } catch (Exception e) {
      assertThat(e).hasMessage("Package 'com.github.jomof:boost:1.0.63-rev10' has missing ios.archive.file");
    }
  }

  @Test
  public void missingAndroidSize() {
    try {
      check("coordinate:\n" + "  groupId: com.github.jomof\n" + "  artifactId: boost\n" + "  version: 1.0.63-rev10\n" +
          "android:\n" + "  archives:\n" + "   " + " - file: bob.zip\n" + "      sha256: " +
          "97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b\n");
      fail("Expected an exception");
    } catch (Exception e) {
      assertThat(e).hasMessage("Package 'com.github.jomof:boost:1.0.63-rev10' has missing android.archive.size for 'bob.zip'");
    }
  }

  @Test
  public void missingiOSSize() {
    try {
      check("coordinate:\n" + "  groupId: com.github.jomof\n" + "  artifactId: boost\n" + "  version: 1.0.63-rev10\n" +
          "iOS:\n" + "  archives:\n" + "    - " + "file: bob.zip\n" + "      sha256: " +
          "97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b\n");
      fail("Expected an exception");
    } catch (Exception e) {
      assertThat(e).hasMessage("Package 'com.github.jomof:boost:1.0.63-rev10' has missing ios.archive.size for 'bob.zip'");
    }
  }

  @Test
  public void checkAndroidSuccess() {
    check("coordinate:\n" + "  groupId: com.github.jomof\n" + "  artifactId: boost\n" + "  version: 1.0.63-rev10\n" +
        "android:\n" + "  archives:\n" + "    -" + " file: bob.zip\n" + "      sha256: " +
        "97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b\n" + "      size: 192\n");
  }

  @Test
  public void checkiOSSuccess() {
    check("coordinate:\n" + "  groupId: com.github.jomof\n" + "  artifactId: boost\n" + "  version: 1.0.63-rev10\n" +
        "android:\n" + "  archives:\n" + "    -" + " file: bob.zip\n" + "      sha256: " +
        "97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b\n" + "      size: 192\n");
  }

  private void check(@NotNull String content) {
    CDepManifestYml manifest = CDepManifestYmlUtils.convertStringToManifest(content);
    CDepManifestYmlUtils.checkManifestSanity(manifest);
  }

  @Test
  public void testAllResolvedManifests() throws Exception {
    Map<String, String> expected = new HashMap<>();
    expected.put("archiveMissingSize", "Archive is missing size or it is zero");
    expected.put("archiveMissingFile", "Archive is missing file");
    expected.put("archiveMissingSha256", "Archive is missing sha256");
    expected.put("sqliteLinuxMultiple",
        "Package 'com.github.jomof:sqlite:0.0.0' has multiple linux archives. " + "Only one is " + "allowed.");
    boolean unexpectedFailure = false;
    for (ResolvedManifests.NamedManifest manifest : ResolvedManifests.all()) {
      String key = manifest.name;
      String expectedFailure = expected.get(key);
      try {
        CDepManifestYmlUtils.checkManifestSanity(manifest.resolved.cdepManifestYml);
        if (expectedFailure != null) {
          fail("Expected failure");
        }
      } catch (RuntimeException e) {
        if (!e.getMessage().equals(expectedFailure)) {
          e.printStackTrace();
          System.out.printf("expected.put(\"%s\", \"%s\");\n", key, e.getMessage());
          unexpectedFailure = true;
        }
      }
    }
    if (unexpectedFailure) {
      throw new RuntimeException("Unexpected failures. See console.");
    }
  }

  @Test
  public void testTwoWayMergeSanity() throws Exception {
    Map<String, String> expected = new HashMap<>();
    expected.put("archiveMissingSha256-archiveMissingSha256", "Archive is missing sha256");
    expected.put("archiveMissingFile-archiveMissingFile", "Archive is missing file");
    expected.put("archiveMissingSize-archiveMissingSize", "Archive is missing size or it is zero");
    expected.put("sqliteLinuxMultiple-sqliteLinuxMultiple",
        "Package 'com.github.jomof:sqlite:0.0.0' has multiple linux archives. Only one is allowed.");
    expected.put("sqliteLinuxMultiple-singleABI",
        "Package 'com.github.jomof:sqlite:0.0.0' has multiple linux archives. Only one is allowed.");
    expected.put("sqliteLinuxMultiple-sqliteLinux",
        "Package 'com.github.jomof:sqlite:0.0.0' has multiple linux archives. Only one is allowed.");
    expected.put("sqliteLinuxMultiple-sqlite",
        "Package 'com.github.jomof:sqlite:0.0.0' has multiple linux archives. Only one is allowed.");
    expected.put("singleABI-sqliteLinuxMultiple",
        "Package 'com.github.jomof:sqlite:0.0.0' has multiple linux archives. Only one is allowed.");
    expected.put("sqliteLinux-sqliteLinuxMultiple",
        "Package 'com.github.jomof:sqlite:0.0.0' has multiple linux archives. Only one is allowed.");
    expected.put("sqliteLinux-sqliteLinux",
        "Package 'com.github.jomof:sqlite:0.0.0' has multiple linux archives. Only one is allowed.");
    expected.put("sqlite-sqliteLinuxMultiple",
        "Package 'com.github.jomof:sqlite:0.0.0' has multiple linux archives. Only one is allowed.");//
    expected.put("singleABI-singleABI",
        "Package 'com.github.jomof:sqlite:0.0.0' contains multiple references to the same archive file " +
            "'sqlite-android-cxx-platform-12-armeabi.zip'");
    expected.put("sqliteiOS-sqliteiOS",
        "Package 'com.github.jomof:sqlite:3.16.2-rev33' contains multiple references to the same archive file " +
            "'sqlite-ios-platform-iPhoneOS-architecture-armv7-sdk-9.3.zip'");

    boolean somethingUnexpected = false;
    for (ResolvedManifests.NamedManifest manifest1 : ResolvedManifests.all()) {
      for (ResolvedManifests.NamedManifest manifest2 : ResolvedManifests.all()) {
        String key = manifest1.name + "-" + manifest2.name;
        String expectedFailure = expected.get(key);
        CDepManifestYml manifest;

        try {
          manifest = MergeCDepManifestYmls.merge(manifest1.resolved.cdepManifestYml, manifest2.resolved.cdepManifestYml);
        } catch (RuntimeException e) {
          continue;
        }

        try {
          CDepManifestYmlUtils.checkManifestSanity(manifest);
          if (expectedFailure != null) {
            TestCase.fail("Expected a failure.");
          }
        } catch (RuntimeException e) {
          String actual = e.getMessage();
          if (!actual.equals(expectedFailure)) {
            // e.printStackTrace();
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

  private static class CoverConstructor extends CDepManifestYmlUtils {

  }
}
