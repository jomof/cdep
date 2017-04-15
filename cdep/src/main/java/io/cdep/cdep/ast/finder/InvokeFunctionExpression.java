package io.cdep.cdep.ast.finder;

import io.cdep.annotations.NotNull;

import java.lang.reflect.Modifier;

import static io.cdep.cdep.utils.Invariant.*;

public class InvokeFunctionExpression extends StatementExpression {
  @NotNull
  final public ExternalFunctionExpression function;

  @NotNull
  final public Expression parameters[];

  InvokeFunctionExpression(@NotNull ExternalFunctionExpression function, @NotNull Expression parameters[]) {
    this.function = notNull(function);
    this.parameters = elementsNotNull(parameters);
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
