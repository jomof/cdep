package io.cdep.cdep.yml.cdepmanifest;

import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class Linux {
  @Nullable
  final public LinuxArchive archives[];

  Linux() {
    archives = null;
  }

  Linux(LinuxArchive archives[]) {
    this.archives = archives;
  }
}
