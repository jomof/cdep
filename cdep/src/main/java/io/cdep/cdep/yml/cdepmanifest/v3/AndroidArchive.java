package io.cdep.cdep.yml.cdepmanifest.v3;

import io.cdep.annotations.Nullable;

@SuppressWarnings("unused")
public class AndroidArchive {
  @Nullable
  final public String file;
  @Nullable
  final public String sha256;
  @Nullable
  final public Long size;
  @Nullable
  final public String ndk;
  @Nullable
  final public String compiler;
  @Nullable
  final public String runtime;
  @Nullable
  final public String platform;
  @Nullable
  final public String builder;
  @Nullable
  final public String abi;
  @Nullable
  final public String include;
  @Nullable
  final public String lib;
  @Nullable
  final public String flavor;

  private AndroidArchive() {
    this.file = null;
    this.sha256 = null;
    this.size = null;
    this.ndk = null;
    this.compiler = null;
    this.runtime = null;
    this.platform = null;
    this.builder = null;
    this.abi = null;
    this.include = null;
    this.lib = null;
    this.flavor = null;
  }

  public AndroidArchive(@Nullable String file,
      @Nullable String sha256,
      @Nullable Long size,
      @Nullable String ndk,
      @Nullable String compiler,
      @Nullable String runtime,
      @Nullable String platform,
      @Nullable String builder,
      @Nullable String abi,
      @Nullable String include,
      @Nullable String lib,
      @Nullable String flavor) {
    this.file = file;
    this.sha256 = sha256;
    this.size = size;
    this.ndk = ndk;
    this.compiler = compiler;
    this.runtime = runtime;
    this.platform = platform;
    this.builder = builder;
    this.abi = abi;
    this.include = include;
    this.lib = lib;
    this.flavor = flavor;
  }
}
