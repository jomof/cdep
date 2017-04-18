package io.cdep.cdep.yml.cdepmanifest;

import io.cdep.annotations.NotNull;

@SuppressWarnings("unused")
public class Archive {
  @NotNull
  final public String file;
  @NotNull
  final public String sha256;
  @NotNull
  final public Long size;
  @NotNull
  final public String include;
  @NotNull
  final public CxxLanguageFeatures requires[];

  private Archive() {
    this.file = "";
    this.sha256 = "";
    this.size = 0L;
    this.include = "";
    this.requires = new CxxLanguageFeatures[0];
  }

  public Archive(
      @NotNull String file,
      @NotNull String sha256,
      @NotNull Long size,
      @NotNull String include,
      @NotNull CxxLanguageFeatures requires[]) {
    this.file = file;
    this.sha256 = sha256;
    this.size = size;
    this.include = include;
    this.requires = requires;
  }
}
