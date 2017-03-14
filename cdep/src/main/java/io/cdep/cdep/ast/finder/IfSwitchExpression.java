package io.cdep.cdep.ast.finder;

public class IfSwitchExpression extends StatementExpression {

  final public Expression conditions[];
  final public Expression expressions[];
  final public Expression elseExpression;

  IfSwitchExpression(Expression conditions[], Expression expressions[], Expression elseExpression) {
    assert conditions != null;
    assert expressions != null;
    assert elseExpression != null;
    assert conditions.length == expressions.length;
    for (int i = 0; i < expressions.length; ++i) {
      assert conditions[i] != null;
      assert expressions[i] != null;
    }
    this.conditions = conditions;
    this.expressions = expressions;
    this.elseExpression = elseExpression;
  }
}
