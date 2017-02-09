package io.cdep.AST.finder;

public class LongConstantExpression extends Expression {

    final public Long value;

    public LongConstantExpression(Long value) {
        this.value = value;
    }
}
