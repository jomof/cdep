package io.cdep;

import io.cdep.manifest.Android;
import io.cdep.manifest.Manifest;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;


public class ManifestUtils {
  public static Manifest convertStringToManifest(String content) {
        Yaml yaml = new Yaml(new Constructor(Manifest.class));
        Manifest dependencyConfig =
            (Manifest)yaml.load( new ByteArrayInputStream(content.getBytes(
                StandardCharsets.UTF_8)));
        if (dependencyConfig == null) {
            throw new RuntimeException();
        }
        return dependencyConfig;
  }

    public static void checkManifestSanity(Manifest manifest) {
        if (manifest.coordinate == null) {
            throw new RuntimeException("Manifest was missing coordinate");
        }
        if (manifest.coordinate.groupId == null) {
            throw new RuntimeException("Manifest was missing coordinate.groupId");
        }
        if (manifest.coordinate.artifactId == null) {
            throw new RuntimeException("Manifest was missing coordinate.artifactId");
        }
        if (manifest.coordinate.version == null) {
            throw new RuntimeException("Manifest was missing coordinate.version");
        }

        checkForDuplicateZipFiles(manifest);

        for (Android android : manifest.android) {
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

    private static void checkForDuplicateZipFiles(Manifest manifest) {
        Set<String> zips = new HashSet<>();
        assert manifest.android != null;
        for (Android android : manifest.android) {
            if (zips.contains(android.file)) {
                throw new RuntimeException(
                    String.format(
                        "Module '%s' contains multiple references to the same zip file: %s",
                        manifest.coordinate, android.file));
            }
            zips.add(android.file);
        }
    }

}
