package io.cdep.cdep.yml.cdepmanifest;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;

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
