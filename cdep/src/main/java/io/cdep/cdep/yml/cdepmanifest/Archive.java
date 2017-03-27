package io.cdep.cdep.yml.cdepmanifest;

import org.jetbrains.annotations.Nullable;

public class Archive {
  @Nullable
  final public String file;
  @Nullable
  final public String sha256;
  @Nullable
  final public Long size;
  final public String include;

  private Archive() {
    this.file = null;
    this.sha256 = null;
    this.size = null;
    this.include = "include";
  }

  public Archive(String file, String sha256, long size, String include) {
    this.file = file;
    this.sha256 = sha256;
    this.size = size;
    this.include = include;
  }
}
