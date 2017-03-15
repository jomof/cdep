package io.cdep.cdep.generator;

import io.cdep.cdep.RewritingVisitor;
import io.cdep.cdep.ast.finder.*;

import static io.cdep.cdep.ast.finder.ExpressionBuilder.string;

/**
 * Locate File.join statements and join them into strings.
 */
public class CMakeConvertJoinedFileToString extends RewritingVisitor {
    @Override
    protected Expression visitInvokeFunctionExpression(InvokeFunctionExpression expr) {
        if (expr.function == ExternalFunctionExpression.FILE_JOIN_SEGMENTS) {
            String value = getUnquotedConcatenation(expr.parameters[0], "/");
            value += "/";
            value += getUnquotedConcatenation(expr.parameters[1], "/");
          return string(value);

        }
        return super.visitInvokeFunctionExpression(expr);
    }

    @Override
    protected Expression visitModuleExpression(ModuleExpression expr) {
        return super.visitModuleExpression(expr);
    }

    /**
     * If a string the return xyz without quotes.
     * If an assignment reference then return ${xyz}.
     */
    private String getUnquotedConcatenation(Expression expr, String joinOn) {
        if (expr instanceof StringExpression) {
            return ((StringExpression) expr).value;
        }
        if (expr instanceof AssignmentReferenceExpression) {
            return String.format("${%s}",
                    ((AssignmentReferenceExpression) expr).assignment.name);
        }
        if (expr instanceof ParameterExpression) {
            return String.format("${%s}",
                    ((ParameterExpression) expr).name);
        }
        if (expr instanceof ArrayExpression) {
            ArrayExpression specific = (ArrayExpression) expr;
            String result = "";
            for (int i = 0; i < specific.elements.length; ++i) {
                if (i > 0) {
                    result += joinOn;
                }
                result += getUnquotedConcatenation(specific.elements[i], joinOn);
            }
            return result;
        }
        throw new RuntimeException(expr.getClass().toString());
    }
}
