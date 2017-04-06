package io.cdep.cdep.yml.cdepmanifest;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;

public class Archive {
  @Nullable
  final public String file;
  @Nullable
  final public String sha256;
  @Nullable
  final public Long size;
  @Nullable
  final public String include;

  private Archive() {
    this.file = null;
    this.sha256 = null;
    this.size = null;
    this.include = "include";
  }

  public Archive(@NotNull String file, @NotNull String sha256, long size, @Nullable String include) {
    this.file = file;
    this.sha256 = sha256;
    this.size = size;
    this.include = include;
  }
}
