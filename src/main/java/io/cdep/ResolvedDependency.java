package io.cdep;

import io.cdep.manifest.Coordinate;
import io.cdep.manifest.Manifest;

class ResolvedDependency {

  final public Coordinate coordinate;
  final public Manifest manifest;

  ResolvedDependency(Coordinate coordinate, Manifest manifest) {
    this.coordinate = coordinate;
    this.manifest = manifest;
  }
}
