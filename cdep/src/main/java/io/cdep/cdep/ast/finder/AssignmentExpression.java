package io.cdep.cdep.ast.finder;

import io.cdep.annotations.NotNull;

public class AssignmentExpression extends ParameterExpression {
  @NotNull
  final public Expression expression;

  AssignmentExpression(@NotNull String name, @NotNull Expression expression) {
    super(name);
    this.expression = expression;
  }
}
