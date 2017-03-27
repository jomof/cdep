package io.cdep.cdep.ast.finder;

public class AssignmentExpression extends ParameterExpression {
  final public Expression expression;

  AssignmentExpression(String name, Expression expression) {
    super(name);
    this.expression = expression;
  }
}
