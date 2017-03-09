package io.cdep.cdep.utils;

import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;
import org.junit.Test;

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
            check("coordinate:\n" +
                    "  groupId: com.github.jomof\n");
            fail("Expected an exception");
        } catch (Exception e) {
            assertThat(e).hasMessage("Manifest was missing coordinate.artifactId");
        }
    }

    @Test
    public void noVersion() {
        try {
            check("coordinate:\n" +
                    "  groupId: com.github.jomof\n" +
                    "  artifactId: boost\n");
            fail("Expected an exception");
        } catch (Exception e) {
            assertThat(e).hasMessage("Manifest was missing coordinate.version");
        }
    }

    @Test
    public void noGroupId() {
        try {
            check("coordinate:\n" +
                    "  artifactId: boost\n" +
                    "  version: 1.0.63-rev10");
            fail("Expected an exception");
        } catch (Exception e) {
            assertThat(e).hasMessage("Manifest was missing coordinate.groupId");
        }
    }

    @Test
    public void noTargets() {
        try {
            check("coordinate:\n" +
                    "  groupId: com.github.jomof\n" +
                    "  artifactId: boost\n" +
                    "  version: 1.0.63-rev10");
            fail("Expected an exception");
        } catch (Exception e) {
            assertThat(e).hasMessage(
                    "Package 'com.github.jomof:boost:1.0.63-rev10' does not contain any files");
        }
    }

    @Test
    public void malformedVersion() {
        try {
            check("coordinate:\n" +
                    "  groupId: com.github.jomof\n" +
                    "  artifactId: boost\n" +
                    "  version: 1.0");
            fail("Expected an exception");
        } catch (Exception e) {
            assertThat(e).hasMessage(
                    "Package 'com.github.jomof:boost:1.0' has malformed version, " +
                            "expected major.minor.point[-tweak] but there was only one dot");
        }
    }

    @Test
    public void duplicateAndroidZips() {
        try {
            check("coordinate:\n" +
                    "  groupId: com.github.jomof\n" +
                    "  artifactId: boost\n" +
                    "  version: 1.0.63-rev10\n" +
                    "android:\n" +
                    "  archives:\n" +
                    "  - file: bob.zip\n" +
                    "    size: 99\n" +
                    "    sha256: 97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b\n" +
                    "  - file: bob.zip\n" +
                    "    size: 99\n" +
                    "    sha256: 97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b\n");
            fail("Expected an exception");
        } catch (Exception e) {
            assertThat(e).hasMessage(
                    "Package 'com.github.jomof:boost:1.0.63-rev10' contains multiple references " +
                            "to the same archive file 'bob.zip'");
        }
    }

    @Test
    public void duplicateiOSZips() {
        try {
            check("coordinate:\n" +
                "  groupId: com.github.jomof\n" +
                "  artifactId: boost\n" +
                "  version: 1.0.63-rev10\n" +
                "iOS:\n" +
                "  archives:\n" +
                "  - file: bob.zip\n" +
                "    size: 99\n" +
                "    sha256: 97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b\n" +
                "  - file: bob.zip\n" +
                "    size: 99\n" +
                "    sha256: 97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b\n");
            fail("Expected an exception");
        } catch (Exception e) {
            assertThat(e).hasMessage(
                "Package 'com.github.jomof:boost:1.0.63-rev10' contains multiple references " +
                    "to the same archive file 'bob.zip'");
        }
    }

    @Test
    public void duplicateZipsBetweenAndroidAndiOS() {
        try {
            check("coordinate:\n" +
                "  groupId: com.github.jomof\n" +
                "  artifactId: boost\n" +
                "  version: 1.0.63-rev10\n" +
                "android:\n" +
                "  archives:\n" +
                "  - file: bob.zip\n" +
                "    size: 99\n" +
                "    sha256: 97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b\n" +
                "iOS:\n" +
                "  archives:\n" +
                "  - file: bob.zip\n" +
                "    size: 99\n" +
                "    sha256: 97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b\n");
            fail("Expected an exception");
        } catch (Exception e) {
            assertThat(e).hasMessage(
                "Package 'com.github.jomof:boost:1.0.63-rev10' contains multiple references " +
                    "to the same archive file 'bob.zip'");
        }
    }

    @Test
    public void emptyiOSArchive() {
        check("coordinate:\n" +
                "  groupId: com.github.jomof\n" +
                "  artifactId: boost\n" +
                "  version: 1.0.63-rev10\n" +
                "android:\n" +
                "  archives:\n" +
                "  - file: bob.zip\n" +
                "    size: 99\n" +
                "    sha256: 97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b\n" +
                "iOS:\n" +
                "  archives:\n");
    }

    @Test
    public void emptyAndroidArchive() {
        check("coordinate:\n" +
                "  groupId: com.github.jomof\n" +
                "  artifactId: boost\n" +
                "  version: 1.0.63-rev10\n" +
                "iOS:\n" +
                "  archives:\n" +
                "  - file: bob.zip\n" +
                "    size: 99\n" +
                "    sha256: 97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b\n" +
                "android:\n" +
                "  archives:\n");
    }

    @Test
    public void missingAndroidSha() {
        try {
            check("coordinate:\n" +
                    "  groupId: com.github.jomof\n" +
                    "  artifactId: boost\n" +
                    "  version: 1.0.63-rev10\n" +
                    "android:\n" +
                    "  archives:\n" +
                    "    - file: bob.zip\n");
            fail("Expected an exception");
        } catch (Exception e) {
            assertThat(e).hasMessage(
                    "Package 'com.github.jomof:boost:1.0.63-rev10' has missing android.archive.sha256 for 'bob.zip'");
        }
    }

    @Test
    public void missingiOSSha() {
        try {
            check("coordinate:\n" +
                "  groupId: com.github.jomof\n" +
                "  artifactId: boost\n" +
                "  version: 1.0.63-rev10\n" +
                "iOS:\n" +
                "  archives:\n" +
                "    - file: bob.zip\n");
            fail("Expected an exception");
        } catch (Exception e) {
            assertThat(e).hasMessage(
                "Package 'com.github.jomof:boost:1.0.63-rev10' has missing ios.archive.sha256 for 'bob.zip'");
        }
    }

    @Test
    public void missingAndroidFile() {
        try {
            check("coordinate:\n" +
                    "  groupId: com.github.jomof\n" +
                    "  artifactId: boost\n" +
                    "  version: 1.0.63-rev10\n" +
                    "android:\n" +
                    "  archives:\n" +
                    "    - sha256: 97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b\n");
            fail("Expected an exception");
        } catch (Exception e) {
            assertThat(e).hasMessage(
                    "Package 'com.github.jomof:boost:1.0.63-rev10' has missing android.archive.file");
        }
    }

    @Test
    public void missingiOSFile() {
        try {
            check("coordinate:\n" +
                "  groupId: com.github.jomof\n" +
                "  artifactId: boost\n" +
                "  version: 1.0.63-rev10\n" +
                "iOS:\n" +
                "  archives:\n" +
                "    - sha256: 97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b\n");
            fail("Expected an exception");
        } catch (Exception e) {
            assertThat(e).hasMessage(
                "Package 'com.github.jomof:boost:1.0.63-rev10' has missing ios.archive.file");
        }
    }

    @Test
    public void missingAndroidSize() {
        try {
            check("coordinate:\n" +
                    "  groupId: com.github.jomof\n" +
                    "  artifactId: boost\n" +
                    "  version: 1.0.63-rev10\n" +
                    "android:\n" +
                    "  archives:\n" +
                    "    - file: bob.zip\n" +
                    "      sha256: 97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b\n");
            fail("Expected an exception");
        } catch (Exception e) {
            assertThat(e).hasMessage(
                    "Package 'com.github.jomof:boost:1.0.63-rev10' has missing android.archive.size for 'bob.zip'");
        }
    }

    @Test
    public void missingiOSSize() {
        try {
            check("coordinate:\n" +
                "  groupId: com.github.jomof\n" +
                "  artifactId: boost\n" +
                "  version: 1.0.63-rev10\n" +
                "iOS:\n" +
                "  archives:\n" +
                "    - file: bob.zip\n" +
                "      sha256: 97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b\n");
            fail("Expected an exception");
        } catch (Exception e) {
            assertThat(e).hasMessage(
                "Package 'com.github.jomof:boost:1.0.63-rev10' has missing ios.archive.size for 'bob.zip'");
        }
    }

    @Test
    public void checkAndroidSuccess() {
        check("coordinate:\n" +
                "  groupId: com.github.jomof\n" +
                "  artifactId: boost\n" +
                "  version: 1.0.63-rev10\n" +
                "android:\n" +
                "  archives:\n" +
                "    - file: bob.zip\n" +
                "      sha256: 97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b\n" +
                "      size: 192\n");
    }

    @Test
    public void checkiOSSuccess() {
        check("coordinate:\n" +
            "  groupId: com.github.jomof\n" +
            "  artifactId: boost\n" +
            "  version: 1.0.63-rev10\n" +
            "android:\n" +
            "  archives:\n" +
            "    - file: bob.zip\n" +
            "      sha256: 97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b\n" +
            "      size: 192\n");
    }

    private void check(String content) {
        CDepManifestYml manifest = CDepManifestYmlUtils.convertStringToManifest(content);
        CDepManifestYmlUtils.checkManifestSanity(manifest);
    }

    private static class CoverConstructor extends CDepManifestYmlUtils {

    }
}
