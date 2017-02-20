package io.cdep.cdep.yml.cdepmanifest;

public class Dependency {
  final public String compile;
  final public String sha256;
    private Dependency() {
        this.compile = null;
        this.sha256 = null;
    }
}
