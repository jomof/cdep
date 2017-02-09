package io.cdep.AST.finder;

@SuppressWarnings("unused")
public class StringExpression extends Expression {

    final public String value;

    public StringExpression(String value) {
        this.value = value;
    }
}
