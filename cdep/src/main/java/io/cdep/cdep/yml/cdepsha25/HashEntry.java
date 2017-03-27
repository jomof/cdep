package io.cdep.cdep.yml.cdepsha25;

import static io.cdep.cdep.utils.Invariant.notNull;

public class HashEntry {
    final public String coordinate;
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
