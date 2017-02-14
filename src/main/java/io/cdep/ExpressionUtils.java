package io.cdep;

import io.cdep.AST.finder.AbortExpression;
import io.cdep.AST.finder.CaseExpression;
import io.cdep.AST.finder.Expression;
import io.cdep.AST.finder.FindModuleExpression;
import io.cdep.AST.finder.FoundModuleExpression;
import io.cdep.AST.finder.FunctionTableExpression;
import io.cdep.AST.finder.IfGreaterThanOrEqualExpression;
import io.cdep.AST.finder.LongConstantExpression;
import io.cdep.AST.finder.ParameterExpression;
import java.util.ArrayList;
import java.util.List;

/**
 * Methods for dealing with FinderExpressions.
 */
abstract public class ExpressionUtils {

    private static void getAllFoundModuleExpressions(
        Expression expression, List<FoundModuleExpression> foundModules) {
        if (expression instanceof CaseExpression) {
            CaseExpression caseExpression = (CaseExpression) expression;
            getAllFoundModuleExpressions(caseExpression.var, foundModules);
            for (String caseValue : caseExpression.cases.keySet()) {
                getAllFoundModuleExpressions(caseExpression.cases.get(caseValue), foundModules);
            }
            getAllFoundModuleExpressions(caseExpression.defaultCase, foundModules);
            return;
        } else if (expression instanceof ParameterExpression) {
            return;
        } else if (expression instanceof AbortExpression) {
            return;
        } else if (expression instanceof IfGreaterThanOrEqualExpression) {
            IfGreaterThanOrEqualExpression ifexpr = (IfGreaterThanOrEqualExpression) expression;
            getAllFoundModuleExpressions(ifexpr.value, foundModules);
            getAllFoundModuleExpressions(ifexpr.compareTo, foundModules);
            getAllFoundModuleExpressions(ifexpr.trueExpression, foundModules);
            getAllFoundModuleExpressions(ifexpr.falseExpression, foundModules);
            return;
        } else if (expression instanceof LongConstantExpression) {
            return;
        } else if (expression instanceof FoundModuleExpression) {
            foundModules.add((FoundModuleExpression) expression);
            return;
        } else if (expression instanceof FunctionTableExpression) {
            FunctionTableExpression table = (FunctionTableExpression) expression;
            for (FindModuleExpression function : table.functions.values()) {
                getAllFoundModuleExpressions(function, foundModules);
            }
            return;
        } else if (expression instanceof FindModuleExpression) {
            FindModuleExpression findModule = (FindModuleExpression) expression;
            getAllFoundModuleExpressions(findModule.expression, foundModules);
            return;
        }
        throw new RuntimeException(expression.toString());
    }

    /**
     * Traverse the given expression and locate all of the FoundModuleExpressions.
     * These expressions contain the local module location as well as the resolved coordinate
     * and other information
     */
    static List<FoundModuleExpression> getAllFoundModuleExpressions(Expression expression) {
        List<FoundModuleExpression> foundModules = new ArrayList<>();
        getAllFoundModuleExpressions(expression, foundModules);
        return foundModules;
    }

}
