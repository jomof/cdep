package io.cdep.cdep.ast.finder;

import io.cdep.annotations.NotNull;

public class GlobalBuildEnvironmentExpression extends Expression {
  @NotNull final public ParameterExpression cdepExplodedRoot;
  @NotNull final public ParameterExpression cmakeSystemName;
  @NotNull final public ParameterExpression cmakeSystemVersion;
  @NotNull final public ParameterExpression cdepDeterminedAndroidAbi;
  @NotNull final public ParameterExpression cdepDeterminedAndroidRuntime;
  @NotNull final public ParameterExpression cmakeOsxSysroot;
  @NotNull final public ParameterExpression cmakeOsxArchitectures;

  public GlobalBuildEnvironmentExpression() {
    this.cdepExplodedRoot = new ParameterExpression("cdep_exploded_root");
    this.cmakeSystemName = new ParameterExpression("cmake_system_name");
    this.cmakeSystemVersion = new ParameterExpression("cmake_system_version");
    this.cdepDeterminedAndroidAbi = new ParameterExpression("cdep_determined_android_abi");
    this.cdepDeterminedAndroidRuntime = new ParameterExpression("cdep_determined_android_runtime");
    this.cmakeOsxSysroot = new ParameterExpression("cmake_osx_sysroot");
    this.cmakeOsxArchitectures = new ParameterExpression("cmake_osx_architectures");
  }
}
