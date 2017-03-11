package io.cdep.cdep.ast.finder;


import java.lang.reflect.Modifier;

public class CallFunctionExpression extends Expression {
    final public ExternalFunctionExpression function;
    final public Expression parameters[];

    public CallFunctionExpression(ExternalFunctionExpression function, Expression... parameters) {
        this.function = function;
        this.parameters = parameters;
        int expectedParameters = function.method.getParameterCount();
        if (!Modifier.isStatic(function.method.getModifiers())) {
            expectedParameters++;
        }
        if (parameters.length != expectedParameters) {
            throw new RuntimeException(String.format(
                    "CallFunctionExpression '%s' expected %s parameters but received %s",
                    function.method, expectedParameters, parameters.length));
        }
    }
}
