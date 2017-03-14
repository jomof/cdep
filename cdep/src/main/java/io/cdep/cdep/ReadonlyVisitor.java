package io.cdep.cdep;

import io.cdep.cdep.ast.finder.*;

public class ReadonlyVisitor {
    protected void visit(Expression expr) {
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
        if (expr.getClass().equals(FoundAndroidModuleExpression.class)) {
            visitFoundAndroidModuleExpression((FoundAndroidModuleExpression) expr);
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
        if (expr.getClass().equals(FoundiOSModuleExpression.class)) {
            visitFoundiOSModuleExpression((FoundiOSModuleExpression) expr);
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
        throw new RuntimeException(expr.getClass().toString());
    }

  protected void visitModuleArchiveExpression(ModuleArchiveExpression expr) {
        visit(expr.fullIncludePath);
    }

  protected void visitAssignmentReferenceExpression(AssignmentReferenceExpression expr) {
    }

    protected void visitAssignmentBlockExpression(AssignmentBlockExpression expr) {
        for (AssignmentExpression assignment : expr.assignments) {
            visit(assignment);
        }
        visit(expr.statement);
    }

    protected void visitArrayExpression(ArrayExpression expr) {
        visitArray(expr.elements);
    }

    protected void visitIntegerExpression(IntegerExpression expr) {
    }

    protected void visitExternalFunctionExpression(ExternalFunctionExpression expr) {
    }

    protected void visitExampleExpression(ExampleExpression expr) {
    }

    protected void visitAbortExpression(AbortExpression expr) {
        visitArray(expr.parameters);
    }

    protected void visitFoundAndroidModuleExpression(FoundAndroidModuleExpression expr) {
        visitArray(expr.archives);
    }

    protected void visitFoundiOSModuleExpression(FoundiOSModuleExpression expr) {
        visitArray(expr.archives);
    }

    protected void visitInvokeFunctionExpression(InvokeFunctionExpression expr) {
        visit(expr.function);
        visitArray(expr.parameters);
    }

    protected void visitArray(Expression[] array) {
        for (int i = 0; i < array.length; ++i) {
            visit(array[i]);
        }
    }

    protected void visitAssignmentExpression(AssignmentExpression expr) {
        visit(expr.expression);
    }

    protected void visitStringExpression(StringExpression expr) {
    }

    protected void visitIfSwitchExpression(IfSwitchExpression expr) {
        visitArray(expr.conditions);
        visitArray(expr.expressions);
        visit(expr.elseExpression);
    }

    protected void visitParameterExpression(ParameterExpression expr) {
    }

    protected void visitFindModuleExpression(FindModuleExpression expr) {
        visit(expr.cdepExplodedRoot);
        visit(expr.targetPlatform);
        visit(expr.systemVersion);
        visit(expr.androidTargetAbi);
        visit(expr.androidStlType);
        visit(expr.osxSysroot);
        visit(expr.osxArchitecture);
        visit(expr.expression);
    }

    void visitFunctionTableExpression(FunctionTableExpression expr) {
        for (Coordinate coordinate : expr.findFunctions.keySet()) {
            visit(expr.findFunctions.get(coordinate));
        }
        for (Coordinate coordinate : expr.examples.keySet()) {
            visit(expr.examples.get(coordinate));
        }
    }
}
