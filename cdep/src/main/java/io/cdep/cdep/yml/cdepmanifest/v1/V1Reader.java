package io.cdep.cdep.yml.cdepmanifest.v1;

import io.cdep.annotations.NotNull;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYmlVersion;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static io.cdep.cdep.utils.Invariant.require;

public class V1Reader {

  @NotNull
  public static io.cdep.cdep.yml.cdepmanifest.CDepManifestYml convertStringToManifest(@NotNull String content) {
    Yaml yaml = new Yaml(new Constructor(CDepManifestYml.class));
    CDepManifestYml manifest = (CDepManifestYml) yaml.load(
        new ByteArrayInputStream(content.getBytes(StandardCharsets
            .UTF_8)));
    require(manifest != null, "Manifest was empty");
    return convert(manifest);
  }

  private static io.cdep.cdep.yml.cdepmanifest.CDepManifestYml convert(CDepManifestYml manifest) {
    return new io.cdep.cdep.yml.cdepmanifest.CDepManifestYml(
        CDepManifestYmlVersion.v1,
        manifest.coordinate,
        manifest.dependencies,
        manifest.archive,
        convert(manifest.android),
        manifest.iOS,
        manifest.linux,
        manifest.example);
  }

  private static io.cdep.cdep.yml.cdepmanifest.Android convert(Android android) {
    return new io.cdep.cdep.yml.cdepmanifest.Android(android.dependencies, convert(android.archives));
  }

  private static io.cdep.cdep.yml.cdepmanifest.AndroidArchive[] convert(AndroidArchive[] archives) {
    List<io.cdep.cdep.yml.cdepmanifest.AndroidArchive> singleAbiArchives = new ArrayList<>();
    for (AndroidArchive archive : archives) {
      for (String abi : archive.abis) {
        singleAbiArchives.add(new io.cdep.cdep.yml.cdepmanifest.AndroidArchive(
            archive.file,
            archive.sha256,
            archive.size,
            archive.ndk,
            archive.compiler,
            archive.runtime,
            archive.platform,
            archive.builder,
            abi,
            archive.include,
            archive.lib,
            archive.flavor));

      }
    }

    return singleAbiArchives.toArray(new io.cdep.cdep.yml.cdepmanifest.AndroidArchive[singleAbiArchives.size()]);
  }
}
