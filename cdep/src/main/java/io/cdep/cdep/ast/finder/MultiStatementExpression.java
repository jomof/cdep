package io.cdep.cdep.ast.finder;

public class MultiStatementExpression extends StatementExpression {
  final public StatementExpression statements[];

  MultiStatementExpression(StatementExpression statements[]) {
    this.statements = statements;
  }
}
