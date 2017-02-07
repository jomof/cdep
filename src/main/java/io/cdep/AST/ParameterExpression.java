package io.cdep.AST;

/**
 * Created by jomof on 1/31/17.
 */
public class ParameterExpression extends Expression {

    final public String name;

    public ParameterExpression(String name) {
        this.name = name;
    }

}
