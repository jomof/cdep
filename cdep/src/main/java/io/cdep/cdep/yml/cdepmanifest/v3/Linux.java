package io.cdep.cdep.yml.cdepmanifest.v3;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;

public class Linux {
  @Nullable
  final public LinuxArchive archives[];

  Linux() {
    archives = null;
  }

  public Linux(@NotNull LinuxArchive archives[]) {
    this.archives = archives;
  }
}
