package com.jomofisher.cdep.AST;

public class LongConstantExpression extends Expression {

    final public Long value;

    public LongConstantExpression(Long value) {
        this.value = value;
    }
}
