package io.cdep.utils;

import io.cdep.yml.cdepmanifest.Android;
import io.cdep.yml.cdepmanifest.CDepManifestYml;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;


public class ManifestUtils {

    public static CDepManifestYml convertStringToManifest(String content) {
        Yaml yaml = new Yaml(new Constructor(CDepManifestYml.class));
        CDepManifestYml dependencyConfig =
            (CDepManifestYml) yaml.load(new ByteArrayInputStream(content.getBytes(
                StandardCharsets.UTF_8)));
        if (dependencyConfig == null) {
            throw new RuntimeException();
        }
        return dependencyConfig;
  }

    public static void checkManifestSanity(CDepManifestYml cdepManifestYml) {
        if (cdepManifestYml.coordinate == null) {
            throw new RuntimeException("Manifest was missing coordinate");
        }
        if (cdepManifestYml.coordinate.groupId == null) {
            throw new RuntimeException("Manifest was missing coordinate.groupId");
        }
        if (cdepManifestYml.coordinate.artifactId == null) {
            throw new RuntimeException("Manifest was missing coordinate.artifactId");
        }
        if (cdepManifestYml.coordinate.version == null) {
            throw new RuntimeException("Manifest was missing coordinate.version");
        }

        checkForDuplicateZipFiles(cdepManifestYml);

        for (Android android : cdepManifestYml.android) {
            validateAndroid(android);
        }
    }

    private static void validateAndroid(Android android) {
        if (android.file == null) {
            throw new RuntimeException("Package Android manifest missing file.");
        }
        if (android.sha256 == null) {
            throw new RuntimeException(
                String.format("Package Android manifest '%s' is missing required sha256",
                    android.file));
        }
    }

    private static void checkForDuplicateZipFiles(CDepManifestYml cdepManifestYml) {
        Set<String> zips = new HashSet<>();
        assert cdepManifestYml.android != null;
        for (Android android : cdepManifestYml.android) {
            if (zips.contains(android.file)) {
                throw new RuntimeException(
                    String.format(
                        "Module '%s' contains multiple references to the same zip file: %s",
                        cdepManifestYml.coordinate, android.file));
            }
            zips.add(android.file);
        }
    }

}
