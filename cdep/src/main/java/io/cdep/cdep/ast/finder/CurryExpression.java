package io.cdep.cdep.ast.finder;

/**
 * Curry an expression in the form of f(a, b, z()) -> f'(a, b)
 */
public class CurryExpression extends FunctionExpression {
  final public FunctionExpression originalFunction;
  final public Expression finalParameter;
  public CurryExpression(FunctionExpression originalFunction, Expression finalParameter) {
    this.originalFunction = originalFunction;
    this.finalParameter = finalParameter;
  }
}
