package io.cdep.service;

import io.cdep.manifest.Coordinate;
import io.cdep.manifest.Manifest;

@SuppressWarnings("FieldCanBeLocal")
class ResolvedDependency {

  private final Coordinate coordinate;
  private final Manifest manifest;

  ResolvedDependency(Coordinate coordinate, Manifest manifest) {
    this.coordinate = coordinate;
    this.manifest = manifest;
  }
}
