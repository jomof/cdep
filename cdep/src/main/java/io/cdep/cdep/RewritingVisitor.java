package io.cdep.cdep;

import static io.cdep.cdep.ast.finder.ExpressionBuilder.abort;
import static io.cdep.cdep.ast.finder.ExpressionBuilder.androidModule;
import static io.cdep.cdep.ast.finder.ExpressionBuilder.archive;
import static io.cdep.cdep.ast.finder.ExpressionBuilder.array;
import static io.cdep.cdep.ast.finder.ExpressionBuilder.assign;
import static io.cdep.cdep.ast.finder.ExpressionBuilder.assignmentBlock;
import static io.cdep.cdep.ast.finder.ExpressionBuilder.ifSwitch;
import static io.cdep.cdep.ast.finder.ExpressionBuilder.integer;
import static io.cdep.cdep.ast.finder.ExpressionBuilder.invoke;
import static io.cdep.cdep.ast.finder.ExpressionBuilder.iosModule;
import static io.cdep.cdep.ast.finder.ExpressionBuilder.string;

import io.cdep.cdep.ast.finder.AbortExpression;
import io.cdep.cdep.ast.finder.ArrayExpression;
import io.cdep.cdep.ast.finder.AssignmentBlockExpression;
import io.cdep.cdep.ast.finder.AssignmentExpression;
import io.cdep.cdep.ast.finder.AssignmentReferenceExpression;
import io.cdep.cdep.ast.finder.ExampleExpression;
import io.cdep.cdep.ast.finder.Expression;
import io.cdep.cdep.ast.finder.ExternalFunctionExpression;
import io.cdep.cdep.ast.finder.FindModuleExpression;
import io.cdep.cdep.ast.finder.FoundAndroidModuleExpression;
import io.cdep.cdep.ast.finder.FoundiOSModuleExpression;
import io.cdep.cdep.ast.finder.FunctionTableExpression;
import io.cdep.cdep.ast.finder.IfSwitchExpression;
import io.cdep.cdep.ast.finder.IntegerExpression;
import io.cdep.cdep.ast.finder.InvokeFunctionExpression;
import io.cdep.cdep.ast.finder.ModuleArchiveExpression;
import io.cdep.cdep.ast.finder.ParameterExpression;
import io.cdep.cdep.ast.finder.StatementExpression;
import io.cdep.cdep.ast.finder.StringExpression;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RewritingVisitor {
    final protected Map<Expression, Expression> identity = new HashMap<>();

    public Expression visit(Expression expr) {
        assert expr != null;
        Expression prior = identity.get(expr);
        if (prior != null) {
            return prior;
        }
        identity.put(expr, visitNoIdentity(expr));
        return visit(expr);
    }

    protected Expression visitNoIdentity(Expression expr) {

        if (expr.getClass().equals(FunctionTableExpression.class)) {
            return visitFunctionTableExpression((FunctionTableExpression) expr);
        }
        if (expr.getClass().equals(FindModuleExpression.class)) {
            return visitFindModuleExpression((FindModuleExpression) expr);
        }
        if (expr.getClass().equals(ParameterExpression.class)) {
            return visitParameterExpression((ParameterExpression) expr);
        }
        if (expr.getClass().equals(IfSwitchExpression.class)) {
            return visitIfSwitchExpression((IfSwitchExpression) expr);
        }
        if (expr.getClass().equals(StringExpression.class)) {
            return visitStringExpression((StringExpression) expr);
        }
        if (expr.getClass().equals(AssignmentExpression.class)) {
            return visitAssignmentExpression((AssignmentExpression) expr);
        }
        if (expr.getClass().equals(InvokeFunctionExpression.class)) {
            return visitInvokeFunctionExpression((InvokeFunctionExpression) expr);
        }
        if (expr.getClass().equals(FoundAndroidModuleExpression.class)) {
            return visitFoundAndroidModuleExpression((FoundAndroidModuleExpression) expr);
        }
        if (expr.getClass().equals(AbortExpression.class)) {
            return visitAbortExpression((AbortExpression) expr);
        }
        if (expr.getClass().equals(ExampleExpression.class)) {
            return visitExampleExpression((ExampleExpression) expr);
        }
        if (expr.getClass().equals(ExternalFunctionExpression.class)) {
            return visitExternalFunctionExpression((ExternalFunctionExpression) expr);
        }
        if (expr.getClass().equals(IntegerExpression.class)) {
            return visitIntegerExpression((IntegerExpression) expr);
        }
        if (expr.getClass().equals(FoundiOSModuleExpression.class)) {
            return visitFoundiOSModuleExpression((FoundiOSModuleExpression) expr);
        }
        if (expr.getClass().equals(ArrayExpression.class)) {
            return visitArrayExpression((ArrayExpression) expr);
        }
        if (expr.getClass().equals(ModuleArchiveExpression.class)) {
            return visitModuleArchiveExpression((ModuleArchiveExpression) expr);
        }
        if (expr.getClass().equals(AssignmentBlockExpression.class)) {
            return visitAssignmentBlockExpression((AssignmentBlockExpression) expr);
        }
        if (expr.getClass().equals(AssignmentReferenceExpression.class)) {
            return visitAssignmentReferenceExpression((AssignmentReferenceExpression) expr);
        }
        throw new RuntimeException(expr.getClass().toString());
    }

    private Expression visitAssignmentReferenceExpression(AssignmentReferenceExpression expr) {
        return expr;
    }

    private Expression visitAssignmentBlockExpression(AssignmentBlockExpression expr) {
      return assignmentBlock(
                visitList(expr.assignments),
                (StatementExpression) visit(expr.statement)
        );
    }

    private List<AssignmentExpression> visitList(List<AssignmentExpression> assignments) {
        List<AssignmentExpression> result = new ArrayList<>();
        for (AssignmentExpression assignment : assignments) {
            result.add((AssignmentExpression) visit(assignment));
        }
        return result;
    }

    protected Expression visitArrayExpression(ArrayExpression expr) {
      return array(visitArray(expr.elements));
    }

    protected Expression visitIntegerExpression(IntegerExpression expr) {
      return integer(expr.value);
    }

    protected Expression visitExternalFunctionExpression(ExternalFunctionExpression expr) {
        // Don't rewrite since identity is used for lookup.
        return expr;
    }

    protected Expression visitExampleExpression(ExampleExpression expr) {
        return new ExampleExpression(expr.sourceCode);
    }

    protected Expression visitAbortExpression(AbortExpression expr) {
      return abort(expr.message, visitArray(expr.parameters));
    }

    protected Expression visitFoundAndroidModuleExpression(FoundAndroidModuleExpression expr) {
      return androidModule(
                visitArchiveArray(expr.archives),
                expr.dependencies
        );
    }

    private ModuleArchiveExpression[] visitArchiveArray(ModuleArchiveExpression[] archives) {
        ModuleArchiveExpression result[] = new ModuleArchiveExpression[archives.length];
        for (int i = 0; i < result.length; ++i) {
            result[i] = (ModuleArchiveExpression) visit(archives[i]);
        }
        return result;
    }

    private Expression visitModuleArchiveExpression(ModuleArchiveExpression expr) {
      return archive(
                expr.file,
                expr.sha256,
                expr.size,
                visit(expr.fullIncludePath),
                expr.libraryName);
    }

    protected Expression visitFoundiOSModuleExpression(FoundiOSModuleExpression expr) {
      return iosModule(
                visitArchiveArray(expr.archives),
                expr.dependencies
        );
    }

    protected Expression visitInvokeFunctionExpression(InvokeFunctionExpression expr) {
      return invoke(
                (ExternalFunctionExpression) visit(expr.function),
                visitArray(expr.parameters)
        );
    }

    protected Expression[] visitArray(Expression[] array) {
        Expression result[] = new Expression[array.length];
        for (int i = 0; i < array.length; ++i) {
            result[i] = visit(array[i]);
        }
        return result;
    }

    protected Expression visitAssignmentExpression(AssignmentExpression expr) {
      return assign(
                expr.name,
                visit(expr.expression)
        );
    }

    protected Expression visitStringExpression(StringExpression expr) {
      return string(expr.value);
    }

    protected Expression visitIfSwitchExpression(IfSwitchExpression expr) {
      return ifSwitch(
                visitArray(expr.conditions),
                visitArray(expr.expressions),
                visit(expr.elseExpression)
        );
    }

    protected Expression visitParameterExpression(ParameterExpression expr) {
        return expr;
    }

    protected Expression visitFindModuleExpression(FindModuleExpression expr) {
        return new FindModuleExpression(
                expr.coordinate,
                (ParameterExpression) visit(expr.cdepExplodedRoot),
                (ParameterExpression) visit(expr.targetPlatform),
                (ParameterExpression) visit(expr.systemVersion),
                (ParameterExpression) visit(expr.androidTargetAbi),
                (ParameterExpression) visit(expr.androidStlType),
                (ParameterExpression) visit(expr.osxSysroot),
                (ParameterExpression) visit(expr.osxArchitecture),
                visit(expr.expression)
        );
    }

    Expression visitFunctionTableExpression(FunctionTableExpression expr) {
        FunctionTableExpression newExpr = new FunctionTableExpression();
        for (Coordinate coordinate : expr.findFunctions.keySet()) {
            newExpr.findFunctions.put(coordinate, (FindModuleExpression) visit(expr.findFunctions.get(coordinate)));
        }
        for (Coordinate coordinate : expr.examples.keySet()) {
            newExpr.examples.put(coordinate, (ExampleExpression) visit(expr.examples.get(coordinate)));
        }
        return newExpr;
    }
}
