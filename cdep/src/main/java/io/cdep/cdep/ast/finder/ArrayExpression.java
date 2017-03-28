package io.cdep.cdep.ast.finder;

import io.cdep.annotations.NotNull;

import static io.cdep.cdep.utils.Invariant.elementsNotNull;

public class ArrayExpression extends Expression {
  @NotNull
  final public Expression elements[];

  ArrayExpression(@NotNull Expression elements[]) {
    this.elements = elementsNotNull(elements);
  }
}
