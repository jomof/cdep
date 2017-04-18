package io.cdep.cdep.ast.finder;

import io.cdep.annotations.NotNull;

public class ArrayExpression extends Expression {
  @NotNull
  final public Expression elements[];

  ArrayExpression(@NotNull Expression elements[]) {
    this.elements = elements;
  }
}
