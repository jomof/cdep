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
package io.cdep.cdep.yml.cdep;

@SuppressWarnings("unused")
public class SoftNameDependency {
  final public String compile;
  final public Boolean enforceSourceUrlMatchesManifest; // null means yes

  public SoftNameDependency() {
    compile = null;
    enforceSourceUrlMatchesManifest = null;
  }

  public SoftNameDependency(String compile) {
    this.compile = compile;
    this.enforceSourceUrlMatchesManifest = null;
  }

  public String toYaml(int indent) {
    String firstPrefix = new String(new char[(indent - 1) * 2]).replace('\0', ' ');
    String nextPrefix = new String(new char[indent * 2]).replace('\0', ' ');
    StringBuilder sb = new StringBuilder();
    String prefix = firstPrefix;
    if (compile != null && compile.length() > 0) {
      sb.append(String.format("%scompile: %s\n", prefix, compile));
      prefix = nextPrefix;
    }
    if (enforceSourceUrlMatchesManifest != null) {
      sb.append(String.format("%senforceSourceUrlMatchesManifest: %s\n", prefix,
          enforceSourceUrlMatchesManifest));
    }
    return sb.toString();
  }

  @Override
  public String toString() {
    return toYaml(1);
  }
}
