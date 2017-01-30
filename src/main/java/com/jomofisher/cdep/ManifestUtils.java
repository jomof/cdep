package com.jomofisher.cdep;

import com.jomofisher.cdep.manifest.Manifest;
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
}
