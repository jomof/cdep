package io.cdep.cdep.yml.cdepsha25;

import org.jetbrains.annotations.Nullable;

import static io.cdep.cdep.utils.Invariant.notNull;

public class HashEntry {
  @Nullable
  final public String coordinate;
  @Nullable
  final public String sha256;
    private HashEntry() {
        this.coordinate = null;
        this.sha256 = null;
    }

    public HashEntry(String coordinate, String sha256) {
      this.coordinate = notNull(coordinate);
      this.sha256 = notNull(sha256);
    }
}
