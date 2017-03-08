package io.cdep.cdep.ast.finder;

public class StringExpression extends Expression {
    final public String value;

    public StringExpression(String value) {
        assert value != null;
        this.value = value;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
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
