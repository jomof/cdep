package io.cdep.cdep.ast.finder;

public class AssignmentExpression extends ParameterExpression {
  private static int next = 0;
  final public Expression expression;
  final private int created;

  AssignmentExpression(String name, Expression expression) {
    super(name);
    created = next++;
    this.expression = expression;
  }
}
