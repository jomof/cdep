package io.cdep.cdep.ast.finder;

import io.cdep.annotations.NotNull;

import java.lang.reflect.Modifier;

import static io.cdep.cdep.utils.Invariant.require;

public class InvokeFunctionExpression extends StatementExpression {
  @NotNull
  final public ExternalFunctionExpression function;

  @NotNull
  final public Expression parameters[];

  InvokeFunctionExpression(@NotNull ExternalFunctionExpression function, @NotNull Expression parameters[]) {
    this.function = function;
    this.parameters = parameters;
    int expectedParameters = function.method.getParameterTypes().length;
    if (!Modifier.isStatic(function.method.getModifiers())) {
      expectedParameters++;
    }
    require(parameters.length == expectedParameters,
        "InvokeFunctionExpression '%s' expected %s " + "parameters but " + "received %s",
        function.method,
        expectedParameters,
        parameters.length);
  }
}
