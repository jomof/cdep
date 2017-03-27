package io.cdep.cdep.ast.finder;

import static io.cdep.cdep.utils.Invariant.elementsNotNull;
import static io.cdep.cdep.utils.Invariant.notNull;

public class IfSwitchExpression extends StatementExpression {

  final public Expression conditions[];
  final public Expression expressions[];
  final public Expression elseExpression;

  IfSwitchExpression(Expression conditions[], Expression expressions[], Expression elseExpression) {
    this.conditions = elementsNotNull(conditions);
    this.expressions = elementsNotNull(expressions);
    this.elseExpression = notNull(elseExpression);
  }
}
