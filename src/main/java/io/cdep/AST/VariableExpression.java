package io.cdep.AST;

public class VariableExpression extends Expression {

    public String name;

    public VariableExpression(String name) {
        this.name = name;
    }
}
