package io.cdep.cdep.ast.finder;

import io.cdep.annotations.NotNull;

public class ExampleExpression extends Expression {
  @NotNull
  final public String sourceCode;

  public ExampleExpression(@NotNull String sourceCode) {
    this.sourceCode = sourceCode;
  }
}
