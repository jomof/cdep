package io.cdep.cdep.ast.finder;


import io.cdep.annotations.NotNull;

import static io.cdep.cdep.utils.Invariant.notNull;

/**
 * A reference to the result of an assignment
 */
public class AssignmentReferenceExpression extends Expression {
  @NotNull
  final public AssignmentExpression assignment;

  AssignmentReferenceExpression(@NotNull AssignmentExpression assignment) {
    notNull(assignment);
    this.assignment = assignment;
  }
}
