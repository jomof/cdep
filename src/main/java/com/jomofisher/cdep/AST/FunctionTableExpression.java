package com.jomofisher.cdep.AST;

import java.util.HashMap;
import java.util.Map;

public class FunctionTableExpression extends Expression {

    final public Map<String, FindModuleExpression> functions = new HashMap<>();
}