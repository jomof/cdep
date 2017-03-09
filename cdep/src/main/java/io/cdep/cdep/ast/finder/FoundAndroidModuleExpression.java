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

import java.util.Set;

public class FoundAndroidModuleExpression extends Expression {

    final public Coordinate coordinate; // Coordinate of the module.
    final public ModuleArchive archives[];
    final public Set<Coordinate> dependencies;

    public FoundAndroidModuleExpression(
            Coordinate coordinate,
            ModuleArchive archives[],
            Set<Coordinate> dependencies) {
        assert dependencies != null;
        for (ModuleArchive archive : archives) assert archive != null;
        this.coordinate = coordinate;
        this.archives = archives;
        this.dependencies = dependencies;
    }
}