package io.cdep.cdep.yml.cdepmanifest.v3;

import io.cdep.annotations.Nullable;
import io.cdep.cdep.yml.cdepmanifest.iOSArchitecture;
import io.cdep.cdep.yml.cdepmanifest.iOSPlatform;

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
  final public String lib;
  @Nullable
  final public String flavor;

  private iOSArchive() {
    this.file = null;
    this.sha256 = null;
    this.size = null;
    this.platform = null;
    this.architecture = null;
    this.sdk = null;
    this.lib = null;
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
      @Nullable String lib,
      @Nullable String flavor) {
    this.file = file;
    this.sha256 = sha256;
    this.size = size;
    this.platform = platform;
    this.architecture = architecture;
    this.sdk = sdk;
    this.include = include;
    this.lib = lib;
    this.flavor = flavor;
  }
}
