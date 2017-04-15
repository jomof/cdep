package io.cdep.cdep.yml.cdepmanifest;

import io.cdep.annotations.Nullable;
import io.cdep.cdep.utils.Invariant;

public class Archive {
  @Nullable
  final public String file;
  @Nullable
  final public String sha256;
  @Nullable
  final public Long size;
  @Nullable
  final public String include;
  @Nullable
  final public CxxLanguageFeatures requires[];

  private Archive() {
    this.file = null;
    this.sha256 = null;
    this.size = null;
    this.include = null;
    this.requires = null;
  }

  public Archive(
      @Nullable String file,
      @Nullable String sha256,
      @Nullable Long size,
      @Nullable String include,
      @Nullable CxxLanguageFeatures requires[]) {
    this.file = file;
    this.sha256 = sha256;
    this.size = size;
    this.include = include;
    this.requires = requires;
    Invariant.noNullElements(requires);
  }
}
