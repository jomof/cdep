package io.cdep.cdep.yml.cdepmanifest;

import io.cdep.annotations.NotNull;

/**
 * This is a name like com.github.jomof:firebase/admob:2.1.3-rev7
 * It is expected to be a parseable coordinate.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class HardNameDependency {
  static final public HardNameDependency EMPTY_HARDNAME_DEPENDENCY = new HardNameDependency();
  @NotNull
  final public String compile;
  @NotNull
  final public String sha256;

  private HardNameDependency() {
    this.compile = "";
    this.sha256 = "";
  }

  public HardNameDependency(@NotNull String compile, @NotNull String sha256) {
    this.compile = compile;
    this.sha256 = sha256;
  }
}
