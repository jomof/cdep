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
package io.cdep.cdep.ast.finder;

import io.cdep.cdep.Coordinate;

public class FoundModuleExpression extends Expression {

    final public Coordinate coordinate; // Coordinate of the module.
    final public ModuleArchive archives[];
    final public String include; // The relative path of include files under the zip
    final public String libraryName; // The library name

    public FoundModuleExpression(
            Coordinate coordinate,
            ModuleArchive archives[],
            String include,
            String libraryName) {
        for (ModuleArchive archive : archives) assert archive != null;
        this.coordinate = coordinate;
        this.archives = archives;
        this.include = include;
        this.libraryName = libraryName;
    }
}
