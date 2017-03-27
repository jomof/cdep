package io.cdep.cdep.yml.cdepmanifest;

import org.jetbrains.annotations.Nullable;

public class iOS {

  @Nullable
  final public HardNameDependency dependencies[];
  @Nullable
  final public iOSArchive archives[];

  iOS() {
    this.dependencies = null;
    this.archives = null;
  }

  public iOS(HardNameDependency[] dependencies, iOSArchive[] archives) {
    this.dependencies = dependencies;
    this.archives = archives;
  }
}
