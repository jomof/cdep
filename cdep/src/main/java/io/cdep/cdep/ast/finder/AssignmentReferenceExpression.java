package io.cdep.cdep.ast.finder;


import static io.cdep.cdep.utils.Invariant.notNull;

/**
 * A reference to the result of an assignment
 */
public class AssignmentReferenceExpression extends Expression {
  final public AssignmentExpression assignment;

  AssignmentReferenceExpression(AssignmentExpression assignment) {
    notNull(assignment);
    this.assignment = assignment;
  }
}
