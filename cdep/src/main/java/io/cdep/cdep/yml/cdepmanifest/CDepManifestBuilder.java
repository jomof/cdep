package io.cdep.cdep.yml.cdepmanifest;


import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;

public class CDepManifestBuilder {

  @NotNull
  public static HardNameDependency hardname(@NotNull String compile, @NotNull String sha256) {
    return new HardNameDependency(compile, sha256);
  }

  @NotNull
  public static Archive archive(@NotNull String file, @NotNull String sha256, long size, @NotNull String include) {
    return new Archive(file, sha256, size, include);
  }

  @NotNull
  public static Android android(@Nullable HardNameDependency[] dependencies, @NotNull AndroidArchive archives[]) {
    return new Android(dependencies, archives);
  }

  @NotNull
  public static iOS iOS(@Nullable HardNameDependency[] dependencies, @NotNull iOSArchive archives[]) {
    return new iOS(dependencies, archives);
  }

  @NotNull
  public static Linux linux(@NotNull LinuxArchive archives[]) {
    return new Linux(archives);
  }
}
