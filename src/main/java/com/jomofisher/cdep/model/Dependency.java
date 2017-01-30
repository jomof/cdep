package com.jomofisher.cdep.model;

import static com.sun.tools.corba.se.idl.toJavaPortable.Compile.compiler;

public class Dependency {
  final public String compile;
  Dependency() {
    compile = null;
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
    return sb.toString();
  }

  @Override
  public String toString() {
    return toYaml(1);
  }
}
