package io.cdep.cdep;


import io.cdep.cdep.ast.finder.AssignmentExpression;
import io.cdep.cdep.ast.finder.AssignmentReferenceExpression;
import io.cdep.cdep.ast.finder.Expression;

/**
 * Replace AssignmentExpression with AssignmentReferenceExpression and lift assignments to the nearest block.
 */
public class LiftAssignmentVisitor extends RewritingVisitor {

    @Override
    protected Expression visitAssignmentExpression(AssignmentExpression expr) {
        Expression expression = visit(expr.expression);
        AssignmentExpression assignment = new AssignmentExpression(expr.name, expression);
        return new AssignmentReferenceExpression(assignment);
    }
}
