package com.jomofisher.cdep.AST;

/**
 * Created by jomof on 1/31/17.
 */
public class IfGreaterThanOrEqualExpression extends Expression {

    final public Expression value;
    final public Expression compareTo;
    final public Expression trueExpression;
    final public Expression falseExpression;

    public IfGreaterThanOrEqualExpression(Expression value, Expression compareTo,
        Expression trueExpression, Expression falseExpression) {
        if (value == null) {
            throw new RuntimeException("value");
        }
        if (compareTo == null) {
            throw new RuntimeException("compareTo");
        }
        if (trueExpression == null) {
            throw new RuntimeException("trueExpression");
        }
        if (falseExpression == null) {
            throw new RuntimeException("falseExpression");
        }
        this.value = value;
        this.compareTo = compareTo;
        this.trueExpression = trueExpression;
        this.falseExpression = falseExpression;
    }
}
