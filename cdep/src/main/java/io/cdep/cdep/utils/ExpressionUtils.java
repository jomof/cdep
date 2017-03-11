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
import io.cdep.cdep.ast.finder.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Methods for dealing with FinderExpressions.
 */
abstract public class ExpressionUtils {

    private static void getAllFoundModuleExpressions(
            Expression expression,
            Map<Coordinate, List<Expression>> foundModules,
            Coordinate coordinate) {
        if (expression instanceof CaseExpression) {
            CaseExpression caseExpression = (CaseExpression) expression;
            getAllFoundModuleExpressions(caseExpression.var, foundModules, coordinate);
            for (Expression caseValue : caseExpression.cases.keySet()) {
                getAllFoundModuleExpressions(caseExpression.cases.get(caseValue), foundModules, coordinate);
            }
            getAllFoundModuleExpressions(caseExpression.defaultCase, foundModules, coordinate);
            return;
        } else if (expression instanceof ParameterExpression) {
            return;
        } else if (expression instanceof AbortExpression) {
            return;
        } else if (expression instanceof IfGreaterThanOrEqualExpression) {
            IfGreaterThanOrEqualExpression ifexpr = (IfGreaterThanOrEqualExpression) expression;
            getAllFoundModuleExpressions(ifexpr.value, foundModules, coordinate);
            getAllFoundModuleExpressions(ifexpr.compareTo, foundModules, coordinate);
            getAllFoundModuleExpressions(ifexpr.trueExpression, foundModules, coordinate);
            getAllFoundModuleExpressions(ifexpr.falseExpression, foundModules, coordinate);
            return;
        } else if (expression instanceof IfExpression) {
            IfExpression ifexpr = (IfExpression) expression;
            getAllFoundModuleExpressions(ifexpr.bool, foundModules, coordinate);
            getAllFoundModuleExpressions(ifexpr.trueExpression, foundModules, coordinate);
            getAllFoundModuleExpressions(ifexpr.falseExpression, foundModules, coordinate);
            return;
        } else if (expression instanceof LongExpression) {
            return;
        } else if (expression instanceof FoundAndroidModuleExpression) {
            addModule(foundModules, expression, coordinate);
            return;
        } else if (expression instanceof FoundiOSModuleExpression) {
            addModule(foundModules, expression, coordinate);
            return;
        } else if (expression instanceof FunctionTableExpression) {
            FunctionTableExpression table = (FunctionTableExpression) expression;
            for (FindModuleExpression function : table.findFunctions.values()) {
                getAllFoundModuleExpressions(function, foundModules, coordinate);
            }
            return;
        } else if (expression instanceof FindModuleExpression) {
            FindModuleExpression findModule = (FindModuleExpression) expression;
            getAllFoundModuleExpressions(findModule.expression, foundModules, findModule.coordinate);
            return;
        } else if (expression instanceof InvokeFunctionExpression) {
            InvokeFunctionExpression specific = (InvokeFunctionExpression) expression;
            for (int i = 0; i < specific.parameters.length; ++i) {
                getAllFoundModuleExpressions(specific.parameters[i], foundModules, coordinate);
            }
            return;
        } else if (expression instanceof StringExpression) {
            return;
        }
        throw new RuntimeException(expression.getClass().toString());
    }

    private static void addModule(
            Map<Coordinate, List<Expression>> foundModules,
            Expression expression,
            Coordinate coordinate) {
        List<Expression> modules = foundModules.get(coordinate);
        if (modules == null) {
            modules = new ArrayList<>();
            foundModules.put(coordinate, modules);
            addModule(foundModules, expression, coordinate);
            return;
        }
        modules.add(expression);
    }

    /*
     * Traverse the given expression and locate all of the FoundModuleExpressions.
     * These expressions contain the local module location as well as the resolved coordinate
     * and other information
     */
    public static Map<Coordinate, List<Expression>> getAllFoundModuleExpressions(Expression expression) {
        Map<Coordinate, List<Expression>> foundModules = new HashMap<>();
        getAllFoundModuleExpressions(expression, foundModules, null);
        return foundModules;
    }

}
