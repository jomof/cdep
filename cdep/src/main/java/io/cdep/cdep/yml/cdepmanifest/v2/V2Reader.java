package io.cdep.cdep.yml.cdepmanifest.v2;

import ext.org.yaml.snakeyaml.Yaml;
import ext.org.yaml.snakeyaml.constructor.Constructor;
import ext.org.yaml.snakeyaml.error.YAMLException;
import io.cdep.annotations.NotNull;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYmlVersion;
import io.cdep.cdep.yml.cdepmanifest.Interfaces;
import io.cdep.cdep.yml.cdepmanifest.v1.V1Reader;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static io.cdep.cdep.utils.Invariant.require;

public class V2Reader {

  @NotNull
  public static io.cdep.cdep.yml.cdepmanifest.CDepManifestYml convertStringToManifest(@NotNull String content) {
    Yaml yaml = new Yaml(new Constructor(CDepManifestYml.class));
    io.cdep.cdep.yml.cdepmanifest.CDepManifestYml manifest;
    try {
      CDepManifestYml prior = (CDepManifestYml) yaml.load(
          new ByteArrayInputStream(content.getBytes(StandardCharsets
              .UTF_8)));
      prior.sourceVersion = CDepManifestYmlVersion.v2;
      manifest = convertx(prior);
      require(manifest.sourceVersion == CDepManifestYmlVersion.v2);
    } catch (YAMLException e) {
      manifest = convertx(V1Reader.convertStringToManifest(content));
      require(manifest.sourceVersion == CDepManifestYmlVersion.v1);
    }
    return manifest;
  }

  private static io.cdep.cdep.yml.cdepmanifest.CDepManifestYml convertx(CDepManifestYml manifest) {
    return new io.cdep.cdep.yml.cdepmanifest.CDepManifestYml(
        manifest.sourceVersion,
        manifest.coordinate,
        manifest.dependencies,
        new Interfaces(manifest.archive),
        manifest.android,
        manifest.iOS,
        manifest.linux,
        manifest.example);
  }
}
