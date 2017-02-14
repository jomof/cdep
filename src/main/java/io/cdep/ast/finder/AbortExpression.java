package io.cdep.ast.finder;

public class AbortExpression extends Expression {

    final public String message;
    final public Expression parameters[];

    public AbortExpression(String message, Expression... parameters) {
        this.message = message;
        this.parameters = parameters;
    }
}
