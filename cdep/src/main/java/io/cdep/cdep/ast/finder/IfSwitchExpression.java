package io.cdep.cdep.ast.finder;

import io.cdep.annotations.NotNull;

public class IfSwitchExpression extends StatementExpression {

  @NotNull
  final public Expression conditions[];
  @NotNull
  final public Expression expressions[];
  @NotNull
  final public Expression elseExpression;

  IfSwitchExpression(@NotNull Expression conditions[], @NotNull Expression expressions[], @NotNull Expression elseExpression) {
    this.conditions = conditions;
    this.expressions = expressions;
    this.elseExpression = elseExpression;
  }
}
