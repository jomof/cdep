package io.cdep.manifest;

@SuppressWarnings("unused")
public class Manifest {

    final public Coordinate coordinate;
    final public Android android[];
    @SuppressWarnings("WeakerAccess")
    final public Object[] linux;

    Manifest() {
        this.coordinate = null;
        this.android = null;
        this.linux = null;
    }
}
