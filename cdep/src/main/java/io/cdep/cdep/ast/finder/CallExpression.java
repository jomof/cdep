package io.cdep.cdep.ast.finder;

/**
 * Invokes an expression that has no parameters.
 */
public class CallExpression extends Expression {
  final public FunctionExpression function;

  public CallExpression(FunctionExpression function) {
    this.function = function;
  }
}
