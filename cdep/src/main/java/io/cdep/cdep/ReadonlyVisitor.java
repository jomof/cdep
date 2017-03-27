package io.cdep.cdep;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;
import io.cdep.cdep.ast.finder.*;

public class ReadonlyVisitor {
  protected void visit(@Nullable Expression expr) {
    if (expr == null) {
      return;
    }

    if (expr.getClass().equals(FunctionTableExpression.class)) {
      visitFunctionTableExpression((FunctionTableExpression) expr);
      return;
    }
    if (expr.getClass().equals(FindModuleExpression.class)) {
      visitFindModuleExpression((FindModuleExpression) expr);
      return;
    }
    if (expr.getClass().equals(ParameterExpression.class)) {
      visitParameterExpression((ParameterExpression) expr);
      return;
    }
    if (expr.getClass().equals(IfSwitchExpression.class)) {
      visitIfSwitchExpression((IfSwitchExpression) expr);
      return;
    }
    if (expr.getClass().equals(StringExpression.class)) {
      visitStringExpression((StringExpression) expr);
      return;
    }
    if (expr.getClass().equals(AssignmentExpression.class)) {
      visitAssignmentExpression((AssignmentExpression) expr);
      return;
    }
    if (expr.getClass().equals(InvokeFunctionExpression.class)) {
      visitInvokeFunctionExpression((InvokeFunctionExpression) expr);
      return;
    }
    if (expr.getClass().equals(ModuleExpression.class)) {
      visitModuleExpression((ModuleExpression) expr);
      return;
    }
    if (expr.getClass().equals(AbortExpression.class)) {
      visitAbortExpression((AbortExpression) expr);
      return;
    }
    if (expr.getClass().equals(ExampleExpression.class)) {
      visitExampleExpression((ExampleExpression) expr);
      return;
    }
    if (expr.getClass().equals(ExternalFunctionExpression.class)) {
      visitExternalFunctionExpression((ExternalFunctionExpression) expr);
      return;
    }
    if (expr.getClass().equals(IntegerExpression.class)) {
      visitIntegerExpression((IntegerExpression) expr);
      return;
    }
    if (expr.getClass().equals(ArrayExpression.class)) {
      visitArrayExpression((ArrayExpression) expr);
      return;
    }
    if (expr.getClass().equals(AssignmentBlockExpression.class)) {
      visitAssignmentBlockExpression((AssignmentBlockExpression) expr);
      return;
    }
    if (expr.getClass().equals(AssignmentReferenceExpression.class)) {
      visitAssignmentReferenceExpression((AssignmentReferenceExpression) expr);
      return;
    }
    if (expr.getClass().equals(ModuleArchiveExpression.class)) {
      visitModuleArchiveExpression((ModuleArchiveExpression) expr);
      return;
    }
    if (expr.getClass().equals(MultiStatementExpression.class)) {
      visitMultiStatementExpression((MultiStatementExpression) expr);
      return;
    }
    if (expr.getClass().equals(NopExpression.class)) {
      visitNopExpression((NopExpression) expr);
      return;
    }
    throw new RuntimeException("ro" + expr.getClass().toString());
  }

  protected void visitModuleArchiveExpression(@org.jetbrains.annotations.NotNull @NotNull ModuleArchiveExpression expr) {
    visit(expr.includePath);
    visit(expr.libraryPath);
  }

  protected void visitAssignmentReferenceExpression(AssignmentReferenceExpression expr) {
  }

  protected void visitAssignmentBlockExpression(@org.jetbrains.annotations.NotNull @NotNull AssignmentBlockExpression expr) {
    for (AssignmentExpression assignment : expr.assignments) {
      visit(assignment);
    }
    visit(expr.statement);
  }

  protected void visitArrayExpression(@org.jetbrains.annotations.NotNull @NotNull ArrayExpression expr) {
    visitArray(expr.elements);
  }

  protected void visitIntegerExpression(IntegerExpression expr) {
  }

  protected void visitExternalFunctionExpression(ExternalFunctionExpression expr) {
  }

  protected void visitExampleExpression(ExampleExpression expr) {
  }

  protected void visitAbortExpression(@org.jetbrains.annotations.NotNull @NotNull AbortExpression expr) {
    visitArray(expr.parameters);
  }

  protected void visitModuleExpression(@org.jetbrains.annotations.NotNull @NotNull ModuleExpression expr) {
    visit(expr.archive);
  }

  protected void visitInvokeFunctionExpression(@org.jetbrains.annotations.NotNull @NotNull InvokeFunctionExpression expr) {
    visit(expr.function);
    visitArray(expr.parameters);
  }

  protected void visitArray(@org.jetbrains.annotations.NotNull @NotNull Expression[] array) {
    for (int i = 0; i < array.length; ++i) {
      visit(array[i]);
    }
  }

  protected void visitAssignmentExpression(@org.jetbrains.annotations.NotNull @NotNull AssignmentExpression expr) {
    visit(expr.expression);
  }

  protected void visitStringExpression(StringExpression expr) {
  }

  protected void visitIfSwitchExpression(@org.jetbrains.annotations.NotNull @NotNull IfSwitchExpression expr) {
    visitArray(expr.conditions);
    visitArray(expr.expressions);
    visit(expr.elseExpression);
  }

  protected void visitParameterExpression(ParameterExpression expr) {
  }

  protected void visitFindModuleExpression(@org.jetbrains.annotations.NotNull @NotNull FindModuleExpression expr) {
    visit(expr.cdepExplodedRoot);
    visit(expr.targetPlatform);
    visit(expr.systemVersion);
    visit(expr.androidTargetAbi);
    visit(expr.androidStlType);
    visit(expr.osxSysroot);
    visit(expr.osxArchitectures);
    visit(expr.expression);
  }

  protected void visitMultiStatementExpression(@org.jetbrains.annotations.NotNull @NotNull MultiStatementExpression expr) {
    visitArray(expr.statements);
  }

  protected void visitNopExpression(NopExpression expr) {
  }

  void visitFunctionTableExpression(@org.jetbrains.annotations.NotNull @NotNull FunctionTableExpression expr) {
    for (Coordinate coordinate : expr.findFunctions.keySet()) {
      visit(expr.findFunctions.get(coordinate));
    }
    for (Coordinate coordinate : expr.examples.keySet()) {
      visit(expr.examples.get(coordinate));
    }
  }
}
