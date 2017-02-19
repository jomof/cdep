package io.cdep.cdep.utils;

import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

public class TestCDepManifestYmlUtils {

    private static class CoverConstructor extends CDepManifestYmlUtils {

    }

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
                    "  - file: bob.zip\n" +
                    "  - file: bob.zip\n");
            fail("Expected an exception");
        } catch (Exception e) {
            assertThat(e).hasMessage(
                    "Package 'com.github.jomof:boost:1.0.63-rev10' contains multiple references " +
                            "to the same zip file for android target: bob.zip");
        }
    }

    @Test
    public void duplicateLinuxZips() {
        try {
            check("coordinate:\n" +
                    "  groupId: com.github.jomof\n" +
                    "  artifactId: boost\n" +
                    "  version: 1.0.63-rev10\n" +
                    "linux:\n" +
                    "  - file: bob.zip\n" +
                    "  - file: bob.zip\n");
            fail("Expected an exception");
        } catch (Exception e) {
            assertThat(e).hasMessage(
                    "Package 'com.github.jomof:boost:1.0.63-rev10' contains multiple references " +
                            "to the same zip file for linux target: bob.zip");
        }
    }

    @Test
    public void missingAndroidSha() {
        try {
            check("coordinate:\n" +
                    "  groupId: com.github.jomof\n" +
                    "  artifactId: boost\n" +
                    "  version: 1.0.63-rev10\n" +
                    "android:\n" +
                    "  - file: bob.zip\n");
            fail("Expected an exception");
        } catch (Exception e) {
            assertThat(e).hasMessage(
                    "Package 'com.github.jomof:boost:1.0.63-rev10' has missing android.sha256 for 'bob.zip'");
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
                    "  - sha256: 97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b\n");
            fail("Expected an exception");
        } catch (Exception e) {
            assertThat(e).hasMessage(
                    "Package 'com.github.jomof:boost:1.0.63-rev10' has missing android.file");
        }
    }

    @Test
    public void duplicateZipOkayBetweenTargetOSes() {
        check("coordinate:\n" +
                "  groupId: com.github.jomof\n" +
                "  artifactId: boost\n" +
                "  version: 1.0.63-rev10\n" +
                "android:\n" +
                "  - file: boost_1_63_0.zip\n" +
                "    sha256: 97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b\n" +
                "    include: boost_1_63_0\n" +
                "\n" +
                "linux:\n" +
                "  - file: boost_1_63_0.zip\n" +
                "    include: boost_1_63_0\n" +
                "    sha256: 97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b");
    }

    @Test
    public void checkAndroidSuccess() {
        check("coordinate:\n" +
                "  groupId: com.github.jomof\n" +
                "  artifactId: boost\n" +
                "  version: 1.0.63-rev10\n" +
                "android:\n" +
                "  - file: bob.zip\n" +
                "    sha256: 97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b\n");
    }

    @Test
    public void checkLinuxSuccess() {
        check("coordinate:\n" +
                "  groupId: com.github.jomof\n" +
                "  artifactId: boost\n" +
                "  version: 1.0.63-rev10\n" +
                "linux:\n" +
                "  - file: bob.zip\n" +
                "    sha256: 97ce6635df1f44653a597343cd5757bb8b6b992beb3720f5fc761e3644bcbe7b\n");
    }

    private void check(String content) {
        CDepManifestYml manifest = CDepManifestYmlUtils.convertStringToManifest(content);
        CDepManifestYmlUtils.checkManifestSanity(manifest);
    }
}
