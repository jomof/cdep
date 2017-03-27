package io.cdep.cdep.yml.cdepmanifest;


import io.cdep.annotations.NotNull;

public class CDepManifestBuilder {

  @org.jetbrains.annotations.NotNull
  @NotNull
  public static HardNameDependency hardname(String compile, String sha256) {
    return new HardNameDependency(compile, sha256);
  }

  @org.jetbrains.annotations.NotNull
  @NotNull
  public static Archive archive(String file, String sha256, long size, String include) {
    return new Archive(file, sha256, size, include);
  }

  @org.jetbrains.annotations.NotNull
  @NotNull
  public static Android android(HardNameDependency[] dependencies, AndroidArchive archives[]) {
    return new Android(dependencies, archives);
  }

  @org.jetbrains.annotations.NotNull
  @NotNull
  public static iOS iOS(HardNameDependency[] dependencies, iOSArchive archives[]) {
    return new iOS(dependencies, archives);
  }

  @org.jetbrains.annotations.NotNull
  @NotNull
  public static Linux linux(LinuxArchive archives[]) {
    return new Linux(archives);
  }
}
