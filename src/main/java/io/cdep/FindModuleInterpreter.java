package io.cdep;

import io.cdep.ast.finder.AbortExpression;
import io.cdep.ast.finder.CaseExpression;
import io.cdep.ast.finder.Expression;
import io.cdep.ast.finder.FindModuleExpression;
import io.cdep.ast.finder.FoundModuleExpression;
import io.cdep.ast.finder.FunctionTableExpression;
import io.cdep.ast.finder.IfGreaterThanOrEqualExpression;
import io.cdep.ast.finder.LongConstantExpression;
import io.cdep.ast.finder.ParameterExpression;
import java.util.HashMap;
import java.util.Map;

class FindModuleInterpreter {

    @SuppressWarnings("SameParameterValue")
    static FoundModuleExpression find(
        FunctionTableExpression table,
        String functionName,
        String targetPlatform,
        String systemVersion, // On android, "platform"
        String androidStlType,
        String androidTargetAbi) {
        FindModuleExpression function = table.functions.get(functionName);
        Map<ParameterExpression, String> parameters = new HashMap<>();
        parameters.put(function.targetPlatform, targetPlatform);
        parameters.put(function.systemVersion, systemVersion);
        parameters.put(function.androidStlType, androidStlType);
        parameters.put(function.androidTargetAbi, androidTargetAbi);
        return (FoundModuleExpression) interpret(parameters, function.expression);
    }

    private static Object interpret(Map<ParameterExpression, String> parameters,
        Expression expression) {
        if (expression instanceof CaseExpression) {
            CaseExpression caseExpression = (CaseExpression) expression;
            String caseVar = (String) interpret(parameters, caseExpression.var);
            for (String caseValue : caseExpression.cases.keySet()) {
                if (caseValue.equals(caseVar)) {
                    return interpret(parameters, caseExpression.cases.get(caseValue));
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
        } else if (expression instanceof FoundModuleExpression) {
            return expression;
        }
        throw new RuntimeException(expression.toString());
    }

}
