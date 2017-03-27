package io.cdep.cdep.ast.finder;

import io.cdep.annotations.Nullable;

import static io.cdep.cdep.utils.Invariant.notNull;

public class StringExpression extends Expression {
  @org.jetbrains.annotations.Nullable
  final public String value;

  StringExpression(String value) {
    this.value = notNull(value);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null || !(obj instanceof StringExpression)) {
            return false;
        }
        return value.equals(((StringExpression) obj).value);
    }

    @Override
    public String toString() {
        return value;
    }
}
