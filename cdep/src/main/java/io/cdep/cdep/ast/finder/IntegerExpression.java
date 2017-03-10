package io.cdep.cdep.ast.finder;

public class IntegerExpression extends Expression {
  final public int value;
  public IntegerExpression(int value) {
    this.value = value;
  }
}
