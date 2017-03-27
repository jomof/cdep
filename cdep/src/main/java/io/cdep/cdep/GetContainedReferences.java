package io.cdep.cdep;

import io.cdep.cdep.ast.finder.AssignmentBlockExpression;
import io.cdep.cdep.ast.finder.AssignmentExpression;
import io.cdep.cdep.ast.finder.AssignmentReferenceExpression;
import io.cdep.cdep.ast.finder.Expression;

import java.util.ArrayList;
import java.util.List;

public class GetContainedReferences extends ReadonlyVisitor {
    final public List<AssignmentExpression> list = new ArrayList<>();

    public GetContainedReferences(Expression expression) {
        visit(expression);
    }

    @Override
    public void visitAssignmentReferenceExpression(AssignmentReferenceExpression expr) {
        super.visit(expr.assignment);
        list.add(expr.assignment);
    }

    @Override
    protected void visitAssignmentBlockExpression(AssignmentBlockExpression expr) {
        // Don't count assign block
        visit(expr.statement);
    }
}
