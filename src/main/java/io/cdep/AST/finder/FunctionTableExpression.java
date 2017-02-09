package io.cdep.AST.finder;

import java.util.HashMap;
import java.util.Map;

public class FunctionTableExpression extends Expression {

    final public Map<String, FindModuleExpression> functions = new HashMap<>();
}
