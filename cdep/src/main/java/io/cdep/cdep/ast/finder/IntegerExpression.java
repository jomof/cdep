package io.cdep.cdep.ast.finder;

public class IntegerExpression extends Expression {
  final public int value;

  IntegerExpression(int value) {
    this.value = value;
  }
}
