package io.cdep.cdep.yml.cdepmanifest;

@SuppressWarnings("unused")
public class Linux {
  final public LinuxArchive archives[];

  Linux() {
    archives = null;
  }

  Linux(LinuxArchive archives[]) {
    this.archives = archives;
  }
}
