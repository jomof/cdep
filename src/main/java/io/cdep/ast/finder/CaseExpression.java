package io.cdep.ast.finder;

import java.util.Map;

public class CaseExpression extends Expression {

    final public Expression var;
    final public Map<String, Expression> cases;
    final public Expression defaultCase;

    public CaseExpression(Expression var, Map<String, Expression> cases, Expression defaultCase) {
        this.var = var;
        this.cases = cases;
        this.defaultCase = defaultCase;
    }
}
