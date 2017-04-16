package io.cdep.cdep.ast.finder;

import io.cdep.annotations.NotNull;

public class ParameterAssignmentExpression extends Expression {
  final public ParameterExpression parameter;
  @NotNull
  final public Expression expression;

  public ParameterAssignmentExpression(@NotNull ParameterExpression parameter, @NotNull Expression expression) {
    this.parameter = parameter;
    this.expression = expression;
  }
}
