package io.cdep.cdep.ast.finder;


import java.lang.reflect.Modifier;

public class InvokeFunctionExpression extends Expression {
    final public ExternalFunctionExpression function;
    final public Expression parameters[];

    public InvokeFunctionExpression(ExternalFunctionExpression function, Expression... parameters) {
        this.function = function;
        this.parameters = parameters;
        int expectedParameters = function.method.getParameterTypes().length;
        if (!Modifier.isStatic(function.method.getModifiers())) {
            expectedParameters++;
        }
        if (parameters.length != expectedParameters) {
            throw new RuntimeException(String.format(
                    "InvokeFunctionExpression '%s' expected %s parameters but received %s",
                    function.method, expectedParameters, parameters.length));
        }
    }
}
