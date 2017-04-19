package io.cdep.cdep.yml.cdepmanifest;

import io.cdep.annotations.NotNull;

public class Linux {
  @NotNull
  final public LinuxArchive archives[];

  Linux() {
    archives = new LinuxArchive[0];
  }

  public Linux(@NotNull LinuxArchive archives[]) {
    this.archives = archives;
  }
}
