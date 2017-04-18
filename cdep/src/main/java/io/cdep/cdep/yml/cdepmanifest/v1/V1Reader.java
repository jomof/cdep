package io.cdep.cdep.yml.cdepmanifest.v1;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static io.cdep.cdep.utils.Invariant.require;

public class V1Reader {

  @NotNull
  public static io.cdep.cdep.yml.cdepmanifest.v2.CDepManifestYml convertStringToManifest(@NotNull String content) {
    Yaml yaml = new Yaml(new Constructor(CDepManifestYml.class));
    CDepManifestYml manifest = (CDepManifestYml) yaml.load(
        new ByteArrayInputStream(content.getBytes(StandardCharsets
            .UTF_8)));
    require(manifest != null, "Manifest was empty");
    assert manifest != null;
    return convert(manifest);
  }

  @Nullable
  private static io.cdep.cdep.yml.cdepmanifest.v2.CDepManifestYml convert(@NotNull CDepManifestYml manifest) {
    assert manifest.coordinate != null;
    assert manifest.android != null;
    return new io.cdep.cdep.yml.cdepmanifest.v2.CDepManifestYml(
        manifest.coordinate,
        manifest.dependencies,
        manifest.archive,
        convert(manifest.android),
        manifest.iOS,
        manifest.linux,
        manifest.example);
  }

  @NotNull
  private static io.cdep.cdep.yml.cdepmanifest.v3.Android convert(@NotNull Android android) {
    assert android.archives != null;
    return new io.cdep.cdep.yml.cdepmanifest.v3.Android(android.dependencies, convert(android.archives));
  }

  private static io.cdep.cdep.yml.cdepmanifest.v3.AndroidArchive[] convert(@NotNull AndroidArchive[] archives) {
    List<io.cdep.cdep.yml.cdepmanifest.v3.AndroidArchive> singleAbiArchives = new ArrayList<>();
    for (AndroidArchive archive : archives) {
      if (archive.abis == null) {
        continue;
      }
      for (String abi : archive.abis) {
        singleAbiArchives.add(new io.cdep.cdep.yml.cdepmanifest.v3.AndroidArchive(
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

    return singleAbiArchives.toArray(new io.cdep.cdep.yml.cdepmanifest.v3.AndroidArchive[singleAbiArchives.size()]);
  }
}
