package io.cdep.cdep.yml.cdepmanifest;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;

public class Linux {
  @Nullable
  final public LinuxArchive archives[];

  Linux() {
    archives = null;
  }

  Linux(@NotNull LinuxArchive archives[]) {
    this.archives = archives;
  }
}
