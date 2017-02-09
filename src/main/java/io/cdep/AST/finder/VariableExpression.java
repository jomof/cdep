package io.cdep.AST.finder;

@SuppressWarnings("unused")
class VariableExpression extends Expression {

    private final String name;

    public VariableExpression(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
