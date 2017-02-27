package io.cdep.cdep.ast.finder;

public class FunctionExpression extends Expression {
    final public String name;
    final public ParameterExpression parameters[];
    final public Expression body;
    public FunctionExpression(String name, ParameterExpression parameters[], Expression body) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }
}
