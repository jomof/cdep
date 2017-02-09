package io.cdep;

import io.cdep.manifest.Manifest;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
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
    }
}
