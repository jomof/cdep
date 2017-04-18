package io.cdep.cdep.yml.cdepmanifest.v3;

import io.cdep.annotations.NotNull;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYmlVersion;
import io.cdep.cdep.yml.cdepmanifest.v2.V2Reader;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static io.cdep.cdep.utils.Invariant.require;

public class V3Reader {

  @NotNull
  public static io.cdep.cdep.yml.cdepmanifest.CDepManifestYml convertStringToManifest(@NotNull String content) {
    Yaml yaml = new Yaml(new Constructor(io.cdep.cdep.yml.cdepmanifest.v3.CDepManifestYml.class));
    io.cdep.cdep.yml.cdepmanifest.CDepManifestYml manifest;
    try {
      CDepManifestYml prior = (CDepManifestYml) yaml.load(
          new ByteArrayInputStream(content.getBytes(StandardCharsets
              .UTF_8)));
      prior.sourceVersion = CDepManifestYmlVersion.v3;
      manifest = convert(prior);
      require(manifest.sourceVersion == CDepManifestYmlVersion.v3);
    } catch (YAMLException e) {
      manifest = convert(V2Reader.convertStringToManifest(content));
    }
    return manifest;
  }

  private static io.cdep.cdep.yml.cdepmanifest.CDepManifestYml convert(CDepManifestYml manifest) {
    return new io.cdep.cdep.yml.cdepmanifest.CDepManifestYml(
        manifest.sourceVersion,
        manifest.coordinate,
        manifest.dependencies,
        manifest.interfaces,
        convertAndroid(manifest.android),
        convertiOS(manifest.iOS),
        convertLinux(manifest.linux),
        manifest.example);
  }

  private static io.cdep.cdep.yml.cdepmanifest.Linux convertLinux(Linux linux) {
    if (linux == null) {
      return null;
    }
    return new io.cdep.cdep.yml.cdepmanifest.Linux(convertLinuxArchiveArray(linux.archives));
  }

  private static io.cdep.cdep.yml.cdepmanifest.LinuxArchive[] convertLinuxArchiveArray(LinuxArchive[] archives) {
    io.cdep.cdep.yml.cdepmanifest.LinuxArchive result[] = new io.cdep.cdep.yml.cdepmanifest.LinuxArchive[archives.length];
    for (int i = 0; i < result.length; ++i) {
      result[i] = convertLinuxArchive(archives[i]);
    }
    return result;
  }

  private static io.cdep.cdep.yml.cdepmanifest.LinuxArchive convertLinuxArchive(LinuxArchive archive) {
    return new io.cdep.cdep.yml.cdepmanifest.LinuxArchive(
        archive.file,
        archive.sha256,
        archive.size,
        new String[] { archive.lib },
        archive.include
    );
  }

  private static io.cdep.cdep.yml.cdepmanifest.iOS convertiOS(iOS ios) {
    if (ios == null) {
      return null;
    }
    return new io.cdep.cdep.yml.cdepmanifest.iOS(ios.dependencies, convertiOSArchiveArray(ios.archives));
  }

  private static io.cdep.cdep.yml.cdepmanifest.iOSArchive[] convertiOSArchiveArray(iOSArchive[] archives) {
    if (archives == null) {
      return null;
    }
    io.cdep.cdep.yml.cdepmanifest.iOSArchive result[] = new io.cdep.cdep.yml.cdepmanifest.iOSArchive[archives.length];
    for (int i = 0; i < result.length; ++i) {
      result[i] = convertiOSArchive(archives[i]);
    }
    return result;
  }

  private static io.cdep.cdep.yml.cdepmanifest.iOSArchive convertiOSArchive(iOSArchive archive) {
    return new io.cdep.cdep.yml.cdepmanifest.iOSArchive(
        archive.file,
        archive.sha256,
        archive.size,
        archive.platform,
        archive.architecture,
        archive.sdk,
        archive.include,
        new String[] { archive.lib },
        archive.flavor
    );
  }

  private static io.cdep.cdep.yml.cdepmanifest.Android convertAndroid(Android android) {
    if (android == null) {
      return null;
    }
    return new io.cdep.cdep.yml.cdepmanifest.Android(android.dependencies, convertAndroidArchiveArray(android.archives));
  }

  private static io.cdep.cdep.yml.cdepmanifest.AndroidArchive[] convertAndroidArchiveArray(AndroidArchive[] archives) {
    if (archives == null) {
      return null;
    }
    io.cdep.cdep.yml.cdepmanifest.AndroidArchive result[] = new io.cdep.cdep.yml.cdepmanifest.AndroidArchive[archives.length];
    for (int i = 0; i < result.length; ++i) {
      result[i] = convertAndroidArchive(archives[i]);
    }
    return result;
  }

  private static io.cdep.cdep.yml.cdepmanifest.AndroidArchive convertAndroidArchive(AndroidArchive archive) {
    return new io.cdep.cdep.yml.cdepmanifest.AndroidArchive(
        archive.file,
        archive.sha256,
        archive.size,
        archive.ndk,
        archive.compiler,
        archive.runtime,
        archive.platform,
        archive.builder,
        archive.abi,
        archive.include,
        new String[] { archive.lib },
        archive.flavor
    );
  }
}
