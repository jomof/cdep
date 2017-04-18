package io.cdep.cdep.ast.finder;

import io.cdep.annotations.NotNull;

/**
 * A reference to the result of an assignment
 */
public class AssignmentReferenceExpression extends Expression {
  @NotNull
  final public AssignmentExpression assignment;

  AssignmentReferenceExpression(@NotNull AssignmentExpression assignment) {
    this.assignment = assignment;
  }
}
