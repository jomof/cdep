/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package io.cdep.cdep.ast.finder;

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
