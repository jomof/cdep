package io.cdep.AST;

public class StringExpression extends Expression {

    final public String value;

    public StringExpression(String value) {
        this.value = value;
    }
}
