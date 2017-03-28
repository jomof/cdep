package io.cdep.cdep.ast.finder;

import io.cdep.annotations.NotNull;

import java.util.List;

public class AssignmentBlockExpression extends StatementExpression {
  @NotNull
  final public List<AssignmentExpression> assignments;
  @NotNull
  final public StatementExpression statement;

  AssignmentBlockExpression(@NotNull List<AssignmentExpression> assignments, @NotNull StatementExpression statement) {
    this.assignments = assignments;
    this.statement = statement;
  }
}
