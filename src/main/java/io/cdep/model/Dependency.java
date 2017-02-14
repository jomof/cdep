package io.cdep.model;

@SuppressWarnings("unused")
public class Dependency {
  final public String compile;
  final public Boolean enforceSourceUrlMatchesManifest; // null means yes

  public Dependency() {
    compile = null;
    enforceSourceUrlMatchesManifest = null;
  }

  public Dependency(String compile) {
    this.compile = compile;
    this.enforceSourceUrlMatchesManifest = null;
  }

  public String toYaml(int indent) {
    String firstPrefix = new String(new char[(indent - 1) * 2]).replace('\0', ' ');
    String nextPrefix = new String(new char[indent * 2]).replace('\0', ' ');
    StringBuilder sb = new StringBuilder();
    String prefix = firstPrefix;
    if (compile != null && compile.length() > 0) {
      sb.append(String.format("%scompile: %s\n", prefix, compile));
      prefix = nextPrefix;
    }
    if (enforceSourceUrlMatchesManifest != null) {
      sb.append(String.format("%senforceSourceUrlMatchesManifest: %s\n", prefix,
          enforceSourceUrlMatchesManifest));
    }
    return sb.toString();
  }

  @Override
  public String toString() {
    return toYaml(1);
  }
}
