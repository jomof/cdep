package io.cdep.cdep.yml.cdepmanifest;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;

@SuppressWarnings({"WeakerAccess", "unused"})
public class iOSArchive {

  @Nullable
  final public String file;
  @Nullable
  final public String sha256;
  @Nullable
  final public Long size;
  @Nullable
  final public iOSPlatform platform;
  @Nullable
  final public iOSArchitecture architecture;
  @Nullable
  final public String sdk;
  @Nullable
  final public String include;
  @Nullable
  final public String libs[];
  @Nullable
  final public String flavor;

  private iOSArchive() {
    this.file = null;
    this.sha256 = null;
    this.size = null;
    this.platform = null;
    this.architecture = null;
    this.sdk = null;
    this.libs = null;
    this.flavor = null;
    this.include = null;
  }

  public iOSArchive(
      @Nullable String file,
      @Nullable String sha256,
      @Nullable Long size,
      @Nullable iOSPlatform platform,
      @Nullable iOSArchitecture architecture,
      @Nullable String sdk,
      @Nullable String include,
      @NotNull String libs[],
      @Nullable String flavor) {
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
