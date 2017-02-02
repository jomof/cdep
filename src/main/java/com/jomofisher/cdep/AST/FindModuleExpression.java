package com.jomofisher.cdep.AST;

public class FindModuleExpression extends Expression {

    final public String name;
    final public ParameterExpression targetPlatform;
    final public ParameterExpression systemVersion;
    final public ParameterExpression androidTargetAbi;
    final public ParameterExpression androidStlType;
    final public CaseExpression expression;

    public FindModuleExpression(
        String functionName,
        ParameterExpression targetPlatform,
        ParameterExpression systemVersion,
        ParameterExpression androidTargetAbi,
        ParameterExpression androidStlType,
        CaseExpression expression) {
        this.name = functionName;
        this.targetPlatform = targetPlatform;
        this.systemVersion = systemVersion;
        this.androidTargetAbi = androidTargetAbi;
        this.androidStlType = androidStlType;
        this.expression = expression;
    }
}
