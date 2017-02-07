package io.cdep.AST;

/**
 * Created by jomof on 1/31/17.
 */
public class AbortExpression extends Expression {

    final public String message;
    final public Expression parameters[];

    public AbortExpression(String message, Expression... parameters) {
        this.message = message;
        this.parameters = parameters;
    }
}
