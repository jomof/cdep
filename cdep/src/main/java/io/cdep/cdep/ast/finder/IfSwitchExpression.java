package io.cdep.cdep.ast.finder;

import java.util.List;

public class IfSwitchExpression extends StatementExpression {
    final public Expression conditions[];
    final public Expression expressions[];
    final public Expression elseExpression;

    public IfSwitchExpression(Expression conditions[], Expression expressions[], Expression elseExpression) {
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

    public IfSwitchExpression(List<Expression> conditionList, List<Expression> expressionList, Expression elseExpression) {
        assert conditionList != null;
        assert expressionList != null;
        assert elseExpression != null;
        assert conditionList.size() == expressionList.size();
        int size = conditionList.size();
        conditions = new Expression[size];
        expressions = new Expression[size];
        for (int i = 0; i < size; ++i) {
            conditions[i] = conditionList.get(i);
            expressions[i] = expressionList.get(i);
            assert conditions[i] != null;
            assert expressions[i] != null;
        }
        this.elseExpression = elseExpression;
    }
}
