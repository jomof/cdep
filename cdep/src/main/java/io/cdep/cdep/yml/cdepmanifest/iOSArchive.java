package io.cdep.cdep.yml.cdepmanifest;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;

@SuppressWarnings({"WeakerAccess", "unused"})
public class iOSArchive {

  @NotNull
  final public String file;
  @NotNull
  final public String sha256;
  @NotNull
  final public Long size;
  @Nullable
  final public iOSPlatform platform;
  @Nullable
  final public iOSArchitecture architecture;
  @NotNull
  final public String sdk;
  @NotNull
  final public String include;
  @NotNull
  final public String libs[];
  @NotNull
  final public String flavor;

  private iOSArchive() {
    this.file = "";
    this.sha256 = "";
    this.size = 0L;
    this.platform = null;
    this.architecture = null;
    this.sdk = "";
    this.libs = new String[0];
    this.flavor = "";
    this.include = "";
  }

  public iOSArchive(
      @NotNull String file,
      @NotNull String sha256,
      @NotNull Long size,
      @Nullable iOSPlatform platform,
      @Nullable iOSArchitecture architecture,
      @NotNull String sdk,
      @NotNull String include,
      @NotNull String libs[],
      @NotNull String flavor) {
    this.file = file;
    this.sha256 = sha256;
    this.size = size;
    this.platform = platform;
    this.architecture = architecture;
    this.sdk = sdk;
    this.include = include;
    this.libs = libs;
    this.flavor = flavor;
  }
}
