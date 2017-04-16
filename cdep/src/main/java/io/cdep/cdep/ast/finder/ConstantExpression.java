package io.cdep.cdep.ast.finder;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;

import static io.cdep.cdep.utils.Invariant.notNull;

public class ConstantExpression extends Expression {

  @NotNull
  final public Object value;

  ConstantExpression(Object value) {
    this.value = notNull(value);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    return !(obj == null || !(obj instanceof ConstantExpression)) && value.equals(((ConstantExpression) obj).value);
  }

  @Override
  @NotNull
  public String toString() {
    return value.toString();
  }
}