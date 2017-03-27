package io.cdep.cdep.ast.finder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.cdep.cdep.utils.Invariant.elementsNotNull;
import static io.cdep.cdep.utils.Invariant.notNull;

public class IfSwitchExpression extends StatementExpression {

  @NotNull
  final public Expression conditions[];
  @NotNull
  final public Expression expressions[];
  @Nullable
  final public Expression elseExpression;

  IfSwitchExpression(@NotNull Expression conditions[], @NotNull Expression expressions[], Expression elseExpression) {
    this.conditions = elementsNotNull(conditions);
    this.expressions = elementsNotNull(expressions);
    this.elseExpression = notNull(elseExpression);
  }
}
