package io.cdep.cdep.yml.cdepmanifest;

import io.cdep.annotations.NotNull;
import io.cdep.cdep.Coordinate;

public class CDepManifestYmlRewritingVisitor {

  @NotNull
  public CDepManifestYml visitCDepManifestYml(@NotNull CDepManifestYml value) {
    return new CDepManifestYml(
        visitDepManifestYmlVersion(value.sourceVersion),
        visitCoordinate(value.coordinate),
        visitHardNameDependencyArray(value.dependencies),
        visitInterfaces(value.interfaces),
        visitAndroid(value.android),
        visitiOS(value.iOS),
        visitLinux(value.linux),
        visitExample(value.example));
  }

  private String visitExample(String example) {
    return visitString(example);
  }

  private String visitString(String example) {
    return example;
  }

  private Linux visitLinux(Linux linux) {
    if (linux == null) {
      return null;
    }
    return new Linux(
        visitLinuxArchiveArray(linux.archives)
    );
  }

  private LinuxArchive[] visitLinuxArchiveArray(LinuxArchive[] archives) {
    LinuxArchive[] result = new LinuxArchive[archives.length];
    for (int i = 0; i < result.length; ++i) {
      result[i] = visitLinuxArchive(archives[i]);
    }
    return result;
  }

  private LinuxArchive visitLinuxArchive(LinuxArchive archive) {
    return new LinuxArchive(
        visitFile(archive.file),
        visitSha256(archive.sha256),
        visitSize(archive.size),
        visitLib(archive.lib),
        visitInclude(archive.include));
  }

  private String visitInclude(String include) {
    return visitString(include);
  }

  private String visitLib(String lib) {
    return visitString(lib);
  }

  private Long visitSize(Long size) {
    return visitLong(size);
  }

  private Long visitLong(Long value) {
    return value;
  }

  private String visitSha256(String sha256) {
    return visitString(sha256);
  }

  private String visitFile(String file) {
    return visitString(file);
  }

  private iOS visitiOS(iOS iOS) {
    if (iOS == null) {
      return null;
    }
    return new iOS(
        visitHardNameDependencyArray(iOS.dependencies),
        visitiOSArchiveArray(iOS.archives)
    );
  }

  private iOSArchive[] visitiOSArchiveArray(iOSArchive[] archives) {
    iOSArchive[] result = new iOSArchive[archives.length];
    for (int i = 0; i < result.length; ++i) {
      result[i] = visitiOSArchive(archives[i]);
    }
    return result;
  }

  private iOSArchive visitiOSArchive(iOSArchive archive) {
    return new iOSArchive(
        visitFile(archive.file),
        visitSha256(archive.sha256),
        visitSize(archive.size),
        visitiOSPlatform(archive.platform),
        visitiOSArchitecture(archive.architecture),
        visitSdk(archive.sdk),
        visitInclude(archive.include),
        visitLib(archive.lib),
        visitFlavor(archive.flavor));
  }

  private String visitFlavor(String flavor) {
    return visitString(flavor);
  }

  private String visitSdk(String sdk) {
    return visitString(sdk);
  }

  private iOSArchitecture visitiOSArchitecture(iOSArchitecture architecture) {
    return architecture;
  }

  private iOSPlatform visitiOSPlatform(iOSPlatform platform) {
    return platform;
  }

  private Android visitAndroid(Android android) {
    if (android == null) {
      return null;
    }
    return new Android(
        visitHardNameDependencyArray(android.dependencies),
        visitAndroidArchiveArray(android.archives)
    );
  }

  private AndroidArchive[] visitAndroidArchiveArray(AndroidArchive[] archives) {
    AndroidArchive[] result = new AndroidArchive[archives.length];
    for (int i = 0; i < result.length; ++i) {
      result[i] = visitAndroidArchive(archives[i]);
    }
    return result;
  }

  private AndroidArchive visitAndroidArchive(AndroidArchive archive) {
    return new AndroidArchive(
        visitFile(archive.file),
        visitSha256(archive.sha256),
        visitSize(archive.size),
        visitNdk(archive.ndk),
        visitCompiler(archive.compiler),
        visitRuntime(archive.runtime),
        visitPlatform(archive.platform),
        visitBuilder(archive.builder),
        visitAbi(archive.abi),
        visitInclude(archive.include),
        visitLib(archive.lib),
        visitFlavor(archive.flavor));
  }

  private String visitAbi(String abi) {
    return visitString(abi);
  }

  private String visitBuilder(String builder) {
    return visitString(builder);
  }

  private String visitPlatform(String platform) {
    return visitString(platform);
  }

  private String visitRuntime(String runtime) {
    return visitString(runtime);
  }

  private String visitCompiler(String compiler) {
    return visitString(compiler);
  }

  private String visitNdk(String ndk) {
    return visitString(ndk);
  }

  private Interfaces visitInterfaces(Interfaces interfaces) {
    if (interfaces == null) {
      return null;
    }
    return new Interfaces(visitArchive(interfaces.headers));
  }

  private Archive visitArchive(Archive archive) {
    if (archive == null) {
      return null;
    }
    return new Archive(
        visitFile(archive.file),
        visitSha256(archive.sha256),
        visitLong(archive.size),
        visitInclude(archive.include));
  }

  private HardNameDependency[] visitHardNameDependencyArray(HardNameDependency[] dependencies) {
    if (dependencies == null) {
      return null;
    }
    HardNameDependency[] result = new HardNameDependency[dependencies.length];
    for (int i = 0; i < result.length; ++i) {
      result[i] = visitHardNameDependency(dependencies[i]);
    }
    return result;
  }

  private HardNameDependency visitHardNameDependency(HardNameDependency dependency) {
    return new HardNameDependency(
        visitCompile(dependency.compile),
        visitSha256(dependency.sha256));
  }

  private String visitCompile(String compile) {
    return visitString(compile);
  }

  private Coordinate visitCoordinate(Coordinate coordinate) {
    return new Coordinate(
        visitGroupId(coordinate.groupId),
        visitArtifactId(coordinate.artifactId),
        visitVersion(coordinate.version)
    );
  }

  private String visitVersion(String version) {
    return visitString(version);
  }

  private String visitArtifactId(String artifactId) {
    return visitString(artifactId);
  }

  private String visitGroupId(String groupId) {
    return visitString(groupId);
  }

  private CDepManifestYmlVersion visitDepManifestYmlVersion(CDepManifestYmlVersion sourceVersion) {
    return sourceVersion;
  }
}
