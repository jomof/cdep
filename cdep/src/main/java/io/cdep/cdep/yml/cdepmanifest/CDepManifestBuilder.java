package io.cdep.cdep.yml.cdepmanifest;


public class CDepManifestBuilder {

  public static HardNameDependency hardname(String compile, String sha256) {
    return new HardNameDependency(compile, sha256);
  }

  public static Archive archive(String file, String sha256, long size, String include) {
    return new Archive(file, sha256, size, include);
  }

  public static Android android(HardNameDependency[] dependencies, AndroidArchive archives[]) {
    return new Android(dependencies, archives);
  }

  public static iOS iOS(HardNameDependency[] dependencies, iOSArchive archives[]) {
    return new iOS(dependencies, archives);
  }

  public static Linux linux(LinuxArchive archives[]) {
    return new Linux(archives);
  }
}
