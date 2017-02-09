package io.cdep.AST.finder;

class VariableExpression extends Expression {

    private String name;

    public VariableExpression(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name.toString();
    }
}
