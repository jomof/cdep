package io.cdep.cdep.ast.finder;

public class ArrayExpression extends Expression {
    final public Expression elements[];

    public ArrayExpression(Expression... elements) {
        this.elements = elements;
    }
}
