package io.cdep.cdep.yml.cdepsha25;

public class HashEntry {
    final public String coordinate;
    final public String sha256;
    private HashEntry() {
        this.coordinate = null;
        this.sha256 = null;
    }

    public HashEntry(String coordinate, String sha256) {
        assert coordinate != null;
        assert sha256 != null;
        this.coordinate = coordinate;
        this.sha256 = sha256;
    }
}
