package io.cdep.cdep.yml.cdepmanifest;

import io.cdep.annotations.NotNull;

public class LinuxArchive {
  @NotNull
  final public String file;
  @NotNull
  final public String sha256;
  @NotNull
  final public Long size;
  @NotNull
  final public String libs[];
  @NotNull
  final public String include;

  LinuxArchive() {
    this.file = "";
    this.sha256 = "";
    this.size = 0L;
    this.libs = new String[0];
    this.include = "";
  }

  public LinuxArchive(@NotNull String file,
      @NotNull String sha256,
      @NotNull Long size,
      @NotNull String libs[],
      @NotNull String include) {
    this.file = file;
    this.sha256 = sha256;
    this.size = size;
    this.libs = libs;
    this.include = include;
  }
}
