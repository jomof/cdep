package io.cdep.cdep;


import io.cdep.cdep.ast.finder.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Replace AssignmentExpression with AssignmentReferenceExpression and lift assignments to the nearest block.
 */
public class LiftAssignmentVisitor extends RewritingVisitor {
    final private List<List<AssignmentExpression>> currentBlock = new ArrayList<>();

    @Override
    Expression visit(Expression expr) {

        if (expr instanceof StatementExpression) {
            List<AssignmentExpression> assignments = new ArrayList<>();
            currentBlock.add(0, assignments);
            Expression result = super.visit(expr);
            currentBlock.remove(0);
            if (assignments.size() > 0) {
                return new AssignmentBlockExpression(assignments, (StatementExpression) result);
            }
            return result;
        }
        return super.visit(expr);
    }

    @Override
    protected Expression visitInvokeFunctionExpression(InvokeFunctionExpression expr) {
        return super.visitInvokeFunctionExpression(expr);
    }

    @Override
    protected Expression visitAssignmentExpression(AssignmentExpression expr) {
        Expression expression = visit(expr.expression);
        AssignmentExpression assignment = new AssignmentExpression(expr.name, expression);
        currentBlock.get(0).add(assignment);
        return new AssignmentReferenceExpression(assignment);
    }
}
