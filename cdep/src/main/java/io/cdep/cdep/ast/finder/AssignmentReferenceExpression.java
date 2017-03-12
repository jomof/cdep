package io.cdep.cdep.ast.finder;


/**
 * A reference to the result of an assignment
 */
public class AssignmentReferenceExpression extends Expression {
    final public AssignmentExpression assignment;

    public AssignmentReferenceExpression(AssignmentExpression assignment) {
        this.assignment = assignment;
    }
}
