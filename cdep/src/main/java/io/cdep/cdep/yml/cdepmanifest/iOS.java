package io.cdep.cdep.yml.cdepmanifest;

public class iOS {

  final public HardNameDependency dependencies[];
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
