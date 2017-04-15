package io.cdep.cdep.ast.finder;

public class MultiStatementExpression extends StatementExpression {
  final public StatementExpression statements[];

  public MultiStatementExpression(StatementExpression statements[]) {
    this.statements = statements;
  }
}
