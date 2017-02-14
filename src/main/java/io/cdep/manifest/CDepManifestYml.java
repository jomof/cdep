package io.cdep.manifest;

@SuppressWarnings("unused")
public class CDepManifestYml {

    final public Coordinate coordinate;
    final public Android android[];
    @SuppressWarnings("WeakerAccess")
    final public Object[] linux;

    CDepManifestYml() {
        this.coordinate = null;
        this.android = null;
        this.linux = null;
    }
}
