package io.cdep.cdep.ast.finder;


/**
 * A reference to the result of an assignment
 */
public class AssignmentReferenceExpression extends Expression {
    private static int next = 1000;
    final public int created;
    final public AssignmentExpression assignment;

    public AssignmentReferenceExpression(AssignmentExpression assignment) {
        this.assignment = assignment;
        this.created = next++;
    }
}
