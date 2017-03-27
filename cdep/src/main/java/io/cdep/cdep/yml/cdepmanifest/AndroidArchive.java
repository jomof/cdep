package io.cdep.cdep.yml.cdepmanifest;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;

public class AndroidArchive {
  @Nullable
  final public String file;
  @Nullable
  final public String sha256;
  @Nullable
  final public Long size;
  @Nullable
  final public String ndk = null;
  @Nullable
  final public String compiler = null;
  @Nullable
  final public String runtime = null;
  @Nullable
  final public String platform = null;
  @Nullable
  final public String builder = null;
  @Nullable
  final public String abis[] = null;
  @org.jetbrains.annotations.NotNull
  @NotNull
  final public String include;
  @Nullable
  final public String lib = null;
  @Nullable
  final public String flavor = null;
    private AndroidArchive() {
        this.file = null;
        this.sha256 = null;
        this.size = null;
        this.include = "include";
    }
}
