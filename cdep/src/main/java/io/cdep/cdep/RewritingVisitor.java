package io.cdep.cdep;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;
import io.cdep.cdep.ast.finder.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.cdep.cdep.ast.finder.ExpressionBuilder.*;


public class RewritingVisitor {
  final protected Map<Expression, Expression> identity = new HashMap<>();

  @Nullable
  public Expression visit(@Nullable Expression expr) {
    if (expr == null) {
      return null;
    }
    Expression prior = identity.get(expr);
    if (prior != null) {
      return prior;
    }
    identity.put(expr, visitNoIdentity(expr));
    return visit(expr);
  }

  @Nullable
  protected Expression visitNoIdentity(@NotNull Expression expr) {

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
    if (expr.getClass().equals(ModuleExpression.class)) {
      return visitModuleExpression((ModuleExpression) expr);
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
    if (expr.getClass().equals(MultiStatementExpression.class)) {
      return visitMultiStatementExpression((MultiStatementExpression) expr);
    }
    if (expr.getClass().equals(NopExpression.class)) {
      return visitNopExpression((NopExpression) expr);
    }
    throw new RuntimeException("rw" + expr.getClass().toString());
  }

  private Expression visitAssignmentReferenceExpression(AssignmentReferenceExpression expr) {
    return expr;
  }


  @NotNull
  private Expression visitAssignmentBlockExpression(@NotNull AssignmentBlockExpression expr) {
    return assignmentBlock(
        visitList(expr.assignments),
        (StatementExpression) visit(expr.statement)
    );
  }


  @NotNull
  private List<AssignmentExpression> visitList(@NotNull List<AssignmentExpression> assignments) {
    List<AssignmentExpression> result = new ArrayList<>();
    for (AssignmentExpression assignment : assignments) {
      result.add((AssignmentExpression) visit(assignment));
    }
    return result;
  }


  @NotNull
  protected Expression visitArrayExpression(@NotNull ArrayExpression expr) {
    return array(visitArray(expr.elements));
  }


  @NotNull
  protected Expression visitIntegerExpression(@NotNull IntegerExpression expr) {
    return integer(expr.value);
  }

  protected Expression visitExternalFunctionExpression(ExternalFunctionExpression expr) {
    // Don't rewrite since identity is used for lookup.
    return expr;
  }


  @NotNull
  protected Expression visitExampleExpression(@NotNull ExampleExpression expr) {
    return new ExampleExpression(expr.sourceCode);
  }


  @NotNull
  protected Expression visitAbortExpression(@NotNull AbortExpression expr) {
    return abort(expr.message, visitArray(expr.parameters));
  }


  protected Expression visitModuleExpression(@NotNull ModuleExpression expr) {
    return module(
        (ModuleArchiveExpression) visit(expr.archive),
        expr.dependencies
    );
  }


  @NotNull
  private ModuleArchiveExpression[] visitArchiveArray(@NotNull ModuleArchiveExpression[] archives) {
    ModuleArchiveExpression result[] = new ModuleArchiveExpression[archives.length];
    for (int i = 0; i < result.length; ++i) {
      result[i] = (ModuleArchiveExpression) visit(archives[i]);
    }
    return result;
  }


  @NotNull
  private Expression visitModuleArchiveExpression(@NotNull ModuleArchiveExpression expr) {
    return archive(
        expr.file,
        expr.sha256,
        expr.size,
        expr.include,
        visit(expr.includePath),
        expr.library,
        visit(expr.libraryPath));
  }

  protected Expression visitInvokeFunctionExpression(@NotNull InvokeFunctionExpression expr) {
    return invoke(
        (ExternalFunctionExpression) visit(expr.function),
        visitArray(expr.parameters)
    );
  }


  @NotNull
  protected Expression[] visitArray(@NotNull Expression[] array) {
    Expression result[] = new Expression[array.length];
    for (int i = 0; i < array.length; ++i) {
      result[i] = visit(array[i]);
    }
    return result;
  }


  @NotNull
  protected Expression visitAssignmentExpression(@NotNull AssignmentExpression expr) {
    return assign(expr.name, visit(expr.expression));
  }


  @NotNull
  protected Expression visitStringExpression(@NotNull StringExpression expr) {
    return string(expr.value);
  }

  protected Expression visitIfSwitchExpression(@NotNull IfSwitchExpression expr) {
    return ifSwitch(
        visitArray(expr.conditions),
        visitArray(expr.expressions),
        visit(expr.elseExpression)
    );
  }

  protected Expression visitParameterExpression(ParameterExpression expr) {
    return expr;
  }


  @Nullable
  protected Expression visitFindModuleExpression(@NotNull FindModuleExpression expr) {
    return new FindModuleExpression(
        expr.coordinate,
        (ParameterExpression) visit(expr.cdepExplodedRoot),
        (ParameterExpression) visit(expr.targetPlatform),
        (ParameterExpression) visit(expr.systemVersion),
        (ParameterExpression) visit(expr.androidTargetAbi),
        (ParameterExpression) visit(expr.androidStlType),
        (ParameterExpression) visit(expr.osxSysroot),
        (ParameterExpression) visit(expr.osxArchitectures),
        (StatementExpression) visit(expr.expression)
    );
  }

  @NotNull Expression visitFunctionTableExpression(@NotNull FunctionTableExpression
      expr) {
    FunctionTableExpression newExpr = new FunctionTableExpression();
    for (Coordinate coordinate : expr.findFunctions.keySet()) {
      newExpr.findFunctions.put(coordinate, (FindModuleExpression) visit(expr.findFunctions.get(coordinate)));
    }
    for (Coordinate coordinate : expr.examples.keySet()) {
      newExpr.examples.put(coordinate, (ExampleExpression) visit(expr.examples.get(coordinate)));
    }
    return newExpr;
  }


  @NotNull
  protected StatementExpression[] visitStatementExpressionArray(@NotNull StatementExpression[] array) {
    StatementExpression result[] = new StatementExpression[array.length];
    for (int i = 0; i < array.length; ++i) {
      result[i] = (StatementExpression) visit(array[i]);
    }
    return result;
  }


  @NotNull
  protected Expression visitMultiStatementExpression(@NotNull MultiStatementExpression expr) {
    return multi(visitStatementExpressionArray(expr.statements));
  }


  @NotNull
  protected Expression visitNopExpression(NopExpression expr) {
    return nop();
  }
}
