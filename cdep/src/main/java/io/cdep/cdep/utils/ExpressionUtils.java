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
package io.cdep.cdep.utils;

import io.cdep.cdep.Coordinate;
import io.cdep.cdep.ReadonlyVisitor;
import io.cdep.cdep.ast.finder.Expression;
import io.cdep.cdep.ast.finder.FindModuleExpression;
import io.cdep.cdep.ast.finder.ModuleExpression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Methods for dealing with FinderExpressions.
 */
abstract public class ExpressionUtils {
    /*
     * Traverse the given expression and locate all of the FoundModuleExpressions.
     * These expressions contain the local module location as well as the resolved coordinate
     * and other information
     */
    public static Map<Coordinate, List<Expression>> getAllFoundModuleExpressions(Expression expression) {
        return new Finder(expression).foundModules;
    }

    private static class Finder extends ReadonlyVisitor {
        final private Map<Coordinate, List<Expression>> foundModules = new HashMap<>();
        private Coordinate coordinate;

        Finder(Expression expression) {
            visit(expression);
        }

        @Override
        protected void visitModuleExpression(ModuleExpression expr) {
            addModule(expr);
        }

        @Override
        protected void visitFindModuleExpression(FindModuleExpression expr) {
            coordinate = expr.coordinate;
            super.visitFindModuleExpression(expr);
        }

        private void addModule(Expression expression) {
            List<Expression> modules = foundModules.get(coordinate);
            if (modules == null) {
                modules = new ArrayList<>();
                foundModules.put(coordinate, modules);
                addModule(expression);
                return;
            }
            modules.add(expression);
        }
    }

}
