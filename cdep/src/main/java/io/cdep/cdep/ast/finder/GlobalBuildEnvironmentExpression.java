package io.cdep.cdep.ast.finder;

import io.cdep.annotations.NotNull;

public class GlobalBuildEnvironmentExpression extends Expression {
  @NotNull final public ParameterExpression cdepExplodedRoot;
  @NotNull final public ParameterExpression targetPlatform;
  @NotNull final public ParameterExpression systemVersion;
  @NotNull final public ParameterExpression androidTargetAbi;
  @NotNull final public ParameterExpression androidStlType;
  @NotNull final public ParameterExpression osxSysroot;
  @NotNull final public ParameterExpression osxArchitectures;

  public GlobalBuildEnvironmentExpression() {
    this.cdepExplodedRoot = new ParameterExpression("cdepExplodedRoot");
    this.targetPlatform = new ParameterExpression("targetPlatform");
    this.systemVersion = new ParameterExpression("systemVersion");
    this.androidTargetAbi = new ParameterExpression("androidTargetAbi");
    this.androidStlType = new ParameterExpression("androidStlType");
    this.osxSysroot = new ParameterExpression("osxSysroot");
    this.osxArchitectures = new ParameterExpression("osxArchitectures");
  }
}
