package com.jomofisher.cdep.AST;

import java.util.Map;

public class CaseExpression extends Expression {

    final public Expression var;
    final public Map<String, Expression> cases;

    public CaseExpression(Expression var, Map<String, Expression> cases) {
        this.var = var;
        this.cases = cases;
    }


}
