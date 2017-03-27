package io.cdep.cdep.ast.finder;


import static io.cdep.cdep.utils.Invariant.notNull;

/**
 * A reference to the result of an assignment
 */
public class AssignmentReferenceExpression extends Expression {
  private static int next = 1000;
  final public int created;
  final public AssignmentExpression assignment;

  AssignmentReferenceExpression(AssignmentExpression assignment) {
    notNull(assignment);
    this.assignment = assignment;
    this.created = next++;
  }
}
