package io.cdep.cdep.yml.cdepsha25;

public class CDepSHA256 {
    final public HashEntry[] hashes;
    public CDepSHA256(HashEntry[] hashes) {
        this.hashes = hashes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (HashEntry hash : hashes) {
            sb.append(String.format("- coordinate: %s\n", hash.coordinate));
            sb.append(String.format("  sha256: %s\n", hash.sha256));
        }
        return sb.toString();
    }
}
