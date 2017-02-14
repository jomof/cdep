package io.cdep.AST.finder;

import io.cdep.yml.cdepmanifest.Coordinate;

public class FindModuleExpression extends Expression {

    final public Coordinate coordinate;
    final public ParameterExpression targetPlatform;
    final public ParameterExpression systemVersion;
    final public ParameterExpression androidTargetAbi;
    final public ParameterExpression androidStlType;
    final public CaseExpression expression;

    public FindModuleExpression(
        Coordinate coordinate,
        ParameterExpression targetPlatform,
        ParameterExpression systemVersion,
        ParameterExpression androidTargetAbi,
        ParameterExpression androidStlType,
        CaseExpression expression) {
        this.coordinate = coordinate;
        this.targetPlatform = targetPlatform;
        this.systemVersion = systemVersion;
        this.androidTargetAbi = androidTargetAbi;
        this.androidStlType = androidStlType;
        this.expression = expression;
    }
}
