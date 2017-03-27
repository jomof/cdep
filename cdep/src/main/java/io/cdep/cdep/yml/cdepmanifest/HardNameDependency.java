package io.cdep.cdep.yml.cdepmanifest;

/**
 * This is a name like com.github.jomof:firebase/admob:2.1.3-rev7
 * It is expected to be a parseable coordinate.
 */
public class HardNameDependency {
  final public String compile;
  final public String sha256;

  private HardNameDependency() {
    this.compile = null;
    this.sha256 = null;
  }

  public HardNameDependency(String compile, String sha256) {
    this.compile = compile;
    this.sha256 = sha256;
  }
}
