package io.cdep.cdep.yml.cdepmanifest;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;
import io.cdep.cdep.Coordinate;
import io.cdep.cdep.Version;

import static io.cdep.cdep.utils.Invariant.notNull;

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

  protected String visitExample(String example) {
    return visitString(example);
  }

  protected String visitString(String example) {
    return example;
  }

  protected Linux visitLinux(Linux linux) {
    if (linux == null) {
      return null;
    }
    return new Linux(
        visitLinuxArchiveArray(linux.archives)
    );
  }

  @Nullable
  protected LinuxArchive[] visitLinuxArchiveArray(@Nullable LinuxArchive[] archives) {
    if (archives == null) {
      return null;
    }
    LinuxArchive[] result = new LinuxArchive[archives.length];
    for (int i = 0; i < result.length; ++i) {
      result[i] = visitLinuxArchive(archives[i]);
    }
    return result;
  }

  protected LinuxArchive visitLinuxArchive(LinuxArchive archive) {
    return new LinuxArchive(
        visitFile(archive.file),
        visitSha256(archive.sha256),
        visitSize(archive.size),
        visitLib(archive.lib),
        visitInclude(archive.include));
  }

  protected String visitInclude(String include) {
    return visitString(include);
  }

  protected String visitLib(String lib) {
    return visitString(lib);
  }

  protected Long visitSize(Long size) {
    return visitLong(size);
  }

  protected Long visitLong(Long value) {
    return value;
  }

  protected String visitSha256(String sha256) {
    return visitString(sha256);
  }

  protected String visitFile(String file) {
    return visitString(file);
  }

  protected iOS visitiOS(iOS iOS) {
    if (iOS == null) {
      return null;
    }
    return new iOS(
        visitHardNameDependencyArray(iOS.dependencies),
        visitiOSArchiveArray(iOS.archives)
    );
  }

  @Nullable
  protected iOSArchive[] visitiOSArchiveArray(@Nullable iOSArchive[] archives) {
    if (archives == null) {
      return null;
    }
    iOSArchive[] result = new iOSArchive[archives.length];
    for (int i = 0; i < result.length; ++i) {
      result[i] = visitiOSArchive(archives[i]);
    }
    return result;
  }

  protected iOSArchive visitiOSArchive(iOSArchive archive) {
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

  protected String visitFlavor(String flavor) {
    return visitString(flavor);
  }

  protected String visitSdk(String sdk) {
    return visitString(sdk);
  }

  protected iOSArchitecture visitiOSArchitecture(iOSArchitecture architecture) {
    return architecture;
  }

  protected iOSPlatform visitiOSPlatform(iOSPlatform platform) {
    return platform;
  }

  protected Android visitAndroid(Android android) {
    if (android == null) {
      return null;
    }
    return new Android(
        visitHardNameDependencyArray(android.dependencies),
        visitAndroidArchiveArray(android.archives)
    );
  }

  @Nullable
  protected AndroidArchive[] visitAndroidArchiveArray(@Nullable AndroidArchive[] archives) {
    if (archives == null) {
      return null;
    }
    AndroidArchive[] result = new AndroidArchive[archives.length];
    for (int i = 0; i < result.length; ++i) {
      result[i] = visitAndroidArchive(archives[i]);
    }
    return result;
  }

  protected AndroidArchive visitAndroidArchive(AndroidArchive archive) {
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

  protected String visitAbi(String abi) {
    return visitString(abi);
  }

  protected String visitBuilder(String builder) {
    return visitString(builder);
  }

  protected String visitPlatform(String platform) {
    return visitString(platform);
  }

  protected String visitRuntime(String runtime) {
    return visitString(runtime);
  }

  protected String visitCompiler(String compiler) {
    return visitString(compiler);
  }

  protected String visitNdk(String ndk) {
    return visitString(ndk);
  }

  protected Interfaces visitInterfaces(Interfaces interfaces) {
    if (interfaces == null) {
      return null;
    }
    return new Interfaces(visitArchive(interfaces.headers));
  }

  protected Archive visitArchive(Archive archive) {
    if (archive == null) {
      return null;
    }
    return new Archive(
        visitFile(archive.file),
        visitSha256(archive.sha256),
        visitLong(archive.size),
        visitInclude(archive.include),
        visitRequiresArray(archive.requires)
    );
  }

  protected CxxLanguageFeatures[] visitRequiresArray(CxxLanguageFeatures[] requires) {
    if (requires == null) {
      return null;
    }
    CxxLanguageFeatures[] result = new CxxLanguageFeatures[requires.length];
    for (int i = 0; i < result.length; ++i) {
      result[i] = visitRequire(requires[i]);
    }
    return result;
  }

  protected CxxLanguageFeatures visitRequire(CxxLanguageFeatures require) {
    return notNull(require);
  }

  protected HardNameDependency[] visitHardNameDependencyArray(HardNameDependency[] dependencies) {
    if (dependencies == null) {
      return null;
    }
    HardNameDependency[] result = new HardNameDependency[dependencies.length];
    for (int i = 0; i < result.length; ++i) {
      result[i] = visitHardNameDependency(dependencies[i]);
    }
    return result;
  }

  protected HardNameDependency visitHardNameDependency(HardNameDependency dependency) {
    return new HardNameDependency(
        visitCompile(dependency.compile),
        visitSha256(dependency.sha256));
  }

  protected String visitCompile(String compile) {
    return visitString(compile);
  }

  protected Coordinate visitCoordinate(Coordinate coordinate) {
    return new Coordinate(
        visitGroupId(coordinate.groupId),
        visitArtifactId(coordinate.artifactId),
        visitVersion(coordinate.version)
    );
  }

  protected Version visitVersion(Version version) {
    return new Version(visitString(version.value));
  }

  protected String visitArtifactId(String artifactId) {
    return visitString(artifactId);
  }

  protected String visitGroupId(String groupId) {
    return visitString(groupId);
  }

  protected CDepManifestYmlVersion visitDepManifestYmlVersion(CDepManifestYmlVersion sourceVersion) {
    return sourceVersion;
  }
}
