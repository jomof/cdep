package io.cdep.cdep;


import io.cdep.cdep.ast.finder.AssignmentExpression;
import io.cdep.cdep.ast.finder.Expression;

import static io.cdep.cdep.ast.finder.ExpressionBuilder.assign;
import static io.cdep.cdep.ast.finder.ExpressionBuilder.reference;

/**
 * Replace AssignmentExpression with AssignmentReferenceExpression and lift assignments to the
 * nearest block.
 */
public class ReplaceAssignmentWithReference extends RewritingVisitor {

  @Override
  protected Expression visitAssignmentExpression(AssignmentExpression expr) {
    Expression expression = visit(expr.expression);
    AssignmentExpression assignment = assign(expr.name, expression);
    return reference(assignment);
  }
}
