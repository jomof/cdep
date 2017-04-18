package io.cdep.cdep.yml.cdepmanifest.v3;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;

@SuppressWarnings("unused")
public class LinuxArchive {
  @Nullable
  final public String file;
  @Nullable
  final public String sha256;
  @Nullable
  final public Long size;
  @Nullable
  final public String lib;
  @Nullable
  final public String include;

  LinuxArchive() {
    this.file = null;
    this.sha256 = null;
    this.size = null;
    this.lib = null;
    this.include = null;
  }

  public LinuxArchive(@NotNull String file,
      @NotNull String sha256,
      @NotNull Long size,
      @NotNull String lib,
      @Nullable String include) {
    this.file = file;
    this.sha256 = sha256;
    this.size = size;
    this.lib = lib;
    this.include = include;
  }
}
