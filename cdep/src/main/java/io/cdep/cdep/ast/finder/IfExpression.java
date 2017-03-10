package io.cdep.cdep.ast.finder;

public class IfExpression extends Expression {
  final public Expression bool;
  final public Expression trueExpression;
  final public Expression falseExpression;

  public IfExpression(Expression bool, Expression trueExpression,
      Expression falseExpression) {
    this.bool = bool;
    this.trueExpression = trueExpression;
    this.falseExpression = falseExpression;
  }
}
