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
package io.cdep.cdep;

import io.cdep.cdep.ast.finder.*;

import java.util.HashMap;
import java.util.Map;

class FindModuleInterpreter {

    @SuppressWarnings("SameParameterValue")
    static FoundAndroidModuleExpression findAndroid(
            FunctionTableExpression table,
            Coordinate functionName,
            String targetPlatform,
            String systemVersion, // On android, platform like 21
            String androidStlType,
            String androidTargetAbi) {
        FindModuleExpression function = table.findFunctions.get(functionName);
        Map<ParameterExpression, String> parameters = new HashMap<>();
        parameters.put(function.targetPlatform, targetPlatform);
        parameters.put(function.systemVersion, systemVersion);
        parameters.put(function.androidStlType, androidStlType);
        parameters.put(function.androidTargetAbi, androidTargetAbi);
        return (FoundAndroidModuleExpression) interpret(parameters, function.expression);
    }

    static FoundiOSModuleExpression findiOS(
            FunctionTableExpression table,
            Coordinate functionName,
            String targetPlatform,
            String iOSPlatform) {
        FindModuleExpression function = table.findFunctions.get(functionName);
        Map<ParameterExpression, String> parameters = new HashMap<>();
        parameters.put(function.targetPlatform, targetPlatform);
        parameters.put(function.iOSPlatform, iOSPlatform);
        return (FoundiOSModuleExpression) interpret(parameters, function.expression);
    }

    private static Object interpret(Map<ParameterExpression, String> parameters,
        Expression expression) {
        if (expression instanceof CaseExpression) {
            CaseExpression caseExpression = (CaseExpression) expression;
            String caseVar = (String) interpret(parameters, caseExpression.var);
            for (Expression caseValueExpression : caseExpression.cases.keySet()) {
                if (caseValueExpression.toString().equals(caseVar)) {
                    return interpret(parameters,
                            caseExpression.cases.get(caseValueExpression));
                }
            }
            return interpret(parameters, caseExpression.defaultCase);
        } else if (expression instanceof ParameterExpression) {
            return parameters.get(expression);
        } else if (expression instanceof AbortExpression) {
            AbortExpression abortExpression = (AbortExpression) expression;
            Object parms[] = new String[abortExpression.parameters.length];
            for (int i = 0; i < parms.length; ++i) {
                parms[i] = interpret(parameters, abortExpression.parameters[i]);
            }
            throw new RuntimeException(String.format(abortExpression.message, parms));
        } else if (expression instanceof IfGreaterThanOrEqualExpression) {
            IfGreaterThanOrEqualExpression ifexpr = (IfGreaterThanOrEqualExpression) expression;
            Long value = Long.parseLong((String) interpret(parameters, ifexpr.value));
            Long compareTo = (Long) interpret(parameters, ifexpr.compareTo);
            if (value >= compareTo) {
                return interpret(parameters, ifexpr.trueExpression);
            }
            return interpret(parameters, ifexpr.falseExpression);
        } else if (expression instanceof LongConstantExpression) {
            LongConstantExpression longConst = (LongConstantExpression) expression;
            return longConst.value;
        } else if (expression instanceof FoundAndroidModuleExpression) {
            return expression;
        } else if (expression instanceof FoundiOSModuleExpression) {
            return expression;
        }
        throw new RuntimeException(expression.toString());
    }

}
