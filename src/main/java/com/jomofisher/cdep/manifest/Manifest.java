package com.jomofisher.cdep.manifest;

public class Manifest {

    final public Coordinate coordinate;
    final public Android android[];
    final public Object linux[];

    Manifest() {
        this.coordinate = null;
        this.android = null;
        this.linux = null;
    }
}
