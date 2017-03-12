package io.cdep.cdep.ast.finder;


import java.util.List;

public class AssignmentBlockExpression extends StatementExpression {
    final public List<AssignmentExpression> assignments;
    final public StatementExpression statement;

    public AssignmentBlockExpression(List<AssignmentExpression> assignments, StatementExpression statement) {
        this.assignments = assignments;
        this.statement = statement;
    }
}
