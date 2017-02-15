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
package io.cdep.ast.finder;

import io.cdep.yml.cdepmanifest.Coordinate;
import java.net.URL;

public class FoundModuleExpression extends Expression {

    final public Coordinate coordinate; // Coordinate of the module.
    final public URL archive; // The zip file.
    final public String archiveSHA256;
    final public String include; // The relative path of include files under the zip
    final public String libraryName; // The library name

    public FoundModuleExpression(
            Coordinate coordinate,
        URL archive,
        String archiveSHA256,
            String include,
            String libraryName) {
        this.coordinate = coordinate;
        this.archive = archive;
        this.archiveSHA256 = archiveSHA256;
        this.include = include;
        this.libraryName = libraryName;
    }
}
