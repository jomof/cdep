package io.cdep.cdep.generator;

import io.cdep.cdep.RewritingVisitor;
import io.cdep.cdep.ast.finder.AssignmentExpression;
import io.cdep.cdep.ast.finder.Expression;

/**
 * Convert assignments that have rhs value of String or File to set statement
 */
public class CMakeConvertToSetStatementVisitor extends RewritingVisitor {
    @Override
    protected Expression visitAssignmentExpression(AssignmentExpression expr) {
        return super.visitAssignmentExpression(expr);
    }
}
