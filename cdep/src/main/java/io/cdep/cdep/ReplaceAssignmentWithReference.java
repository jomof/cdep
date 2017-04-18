package io.cdep.cdep;

import io.cdep.annotations.NotNull;
import io.cdep.cdep.ast.finder.AssignmentExpression;
import io.cdep.cdep.ast.finder.Expression;

import static io.cdep.cdep.ast.finder.ExpressionBuilder.assign;
import static io.cdep.cdep.ast.finder.ExpressionBuilder.reference;

/**
 * Replace AssignmentExpression with AssignmentReferenceExpression and lift assignments to the
 * nearest block.
 */
class ReplaceAssignmentWithReference extends RewritingVisitor {

  @NotNull
  @Override
  protected Expression visitAssignmentExpression(@NotNull AssignmentExpression expr) {
    Expression expression = visit(expr.expression);
    AssignmentExpression assignment = assign(expr.name, expression);
    return reference(assignment);
  }
}
