package com.jomofisher.cdep;

import com.jomofisher.cdep.manifest.Coordinate;
import com.jomofisher.cdep.manifest.Manifest;

class ResolvedDependency {

  final public Coordinate coordinate;
  final public Manifest manifest;

  ResolvedDependency(Coordinate coordinate, Manifest manifest) {
    this.coordinate = coordinate;
    this.manifest = manifest;
  }
}
