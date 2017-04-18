package io.cdep.cdep.yml.cdepmanifest.v3;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;
import io.cdep.cdep.yml.cdepmanifest.HardNameDependency;

@SuppressWarnings("unused")
public class iOS {

  @Nullable
  final public HardNameDependency dependencies[];
  @Nullable
  final public iOSArchive archives[];

  iOS() {
    this.dependencies = null;
    this.archives = null;
  }

  public iOS(@Nullable HardNameDependency[] dependencies, @NotNull iOSArchive[] archives) {
    this.dependencies = dependencies;
    this.archives = archives;
  }
}
