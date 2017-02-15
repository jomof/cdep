/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package io.cdep.yml.cdepmanifest;

@SuppressWarnings("unused")
public class Coordinate {

  final public String groupId; // like com.github.jomof
  final public String artifactId; // like cmakeify
  final public String version; // like alpha-0.0.27

  private Coordinate() {
    groupId = null;
    artifactId = null;
    version = null;
  }

    public Coordinate(String groupId, String artifactId, String version) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Coordinate)) {
      return false;
    }
    return toString().equals(obj);
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public String toString() {
    return groupId + ":" + artifactId + ":" + version;
  }
}
