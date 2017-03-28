package io.cdep.cdep;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;
import io.cdep.cdep.ast.finder.*;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static io.cdep.cdep.utils.Invariant.*;
import static io.cdep.cdep.utils.ReflectionUtils.invoke;

/**
 * Walks the expression tree and interprets the value for the supplied state.
 */
public class InterpretingVisitor {

  @Nullable
  private Frame stack = null;

  @Nullable
  private static Object coerce(@Nullable Object o, @NotNull Class<?> clazz) {
    if (o == null) {
      return null;
    }
    if (clazz.isInstance(o)) {
      return o;
    }
    if (clazz.equals(File.class)) {
      if (o instanceof String) {
        return new File((String) o);
      }
    }
    if (clazz.equals(int.class)) {
      if (o instanceof Integer) {
        return o;
      }
      if (o instanceof String) {
        return Integer.parseInt((String) o);
      }
    }
    if (clazz.equals(String[].class)) {
      if (o instanceof Object[]) {
        Object objarr[] = (Object[]) o;
        String result[] = new String[objarr.length];
        for (int i = 0; i < result.length; ++i) {
          result[i] = (String) coerce(objarr[i], String.class);
        }
        return result;
      }
    }
    if (clazz.equals(String.class)) {
      if (o instanceof File) {
        return o.toString();
      }
      if (o instanceof String[]) {
        String specific[] = (String[]) o;
        String result = "[";
        for (int i = 0; i < specific.length; ++i) {
          if (i != 0) {
            result += ", ";
          }
          result += specific[i];

        }
        result += "]";
        return result;
      }
    }
    fail("Did not coerce %s to %s", o.getClass(), clazz);
    return null;
  }

  @Nullable
  public Object visit(@Nullable Expression expr) {
    if (expr == null) {
      return null;
    }

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
    if (expr.getClass().equals(AssignmentBlockExpression.class)) {
      return visitAssignmentBlockExpression((AssignmentBlockExpression) expr);
    }
    if (expr.getClass().equals(AssignmentReferenceExpression.class)) {
      return visitAssignmentReferenceExpression((AssignmentReferenceExpression) expr);
    }
    if (expr.getClass().equals(ModuleArchiveExpression.class)) {
      return visitModuleArchiveExpression((ModuleArchiveExpression) expr);
    }
    if (expr.getClass().equals(MultiStatementExpression.class)) {
      return visitMultiStatementExpression((MultiStatementExpression) expr);
    }
    if (expr.getClass().equals(NopExpression.class)) {
      return visitNopExpression((NopExpression) expr);
    }
    throw new RuntimeException(expr.getClass().toString());
  }

  @NotNull
  ModuleArchive visitModuleArchiveExpression(@NotNull ModuleArchiveExpression expr) {
    Object fullIncludePath = visit(expr.includePath);
    Object fullLibraryName = visit(expr.libraryPath);
    return new ModuleArchive(expr.file, (File) fullIncludePath, (File) fullLibraryName);
  }

  @NotNull
  private Object visitAssignmentReferenceExpression(@NotNull AssignmentReferenceExpression expr) {
    notNull(stack);
    AssignmentFuture future = stack.lookup(expr.assignment);
    if (future.value == null) {
      Frame oldStack = stack;
      stack = future.stack;
      future.value = visit(future.expr);
      stack = oldStack;
      return visitAssignmentReferenceExpression(expr);
    }
    return future.value;
  }

  @Nullable
  private Object visitAssignmentBlockExpression(@NotNull AssignmentBlockExpression expr) {
    stack = new Frame(stack);
    for (AssignmentExpression assignment : expr.assignments) {
      visitAssignmentExpression(assignment);
    }
    visit(expr.statement);
    Object result = visit(expr.statement);
    stack = stack.prior;
    return result;
  }


  @NotNull
  private Object visitMultiStatementExpression(@NotNull MultiStatementExpression expr) {
    return visitArray(expr.statements);
  }


  @NotNull
  private Object visitArrayExpression(@NotNull ArrayExpression expr) {
    return visitArray(expr.elements);
  }

  private Object visitIntegerExpression(@NotNull IntegerExpression expr) {
    return expr.value;
  }

  private Method visitExternalFunctionExpression(@NotNull ExternalFunctionExpression expr) {
    return expr.method;
  }

  @Nullable
  private Object visitExampleExpression(ExampleExpression expr) {
    return null;
  }


  @Nullable
  Object visitAbortExpression(@NotNull AbortExpression expr) {
    Object parameters[] = (Object[]) coerce(visitArray(expr.parameters), String[].class);
    fail(expr.message, parameters);
    return null;
  }

  @Nullable
  private ModuleArchive visitModuleExpression(@NotNull ModuleExpression expr) {
    return (ModuleArchive) visit(expr.archive);
  }

  private Object visitNopExpression(NopExpression expr) {
    return expr;
  }

  private Object visitInvokeFunctionExpression(@NotNull InvokeFunctionExpression expr) {
    Method method = visitExternalFunctionExpression(expr.function);
    Object parameters[] = visitArray(expr.parameters);

    Object thiz = null;
    int firstParameter = 0;
    if (!Modifier.isStatic(method.getModifiers())) {
      thiz = coerce(parameters[0], method.getDeclaringClass());
      ++firstParameter;
    }
    Object parms[] = new Object[expr.parameters.length - firstParameter];
    for (int i = firstParameter; i < expr.parameters.length; ++i) {
      parms[i - firstParameter] = coerce(parameters[i], method.getParameterTypes()[i - firstParameter]);
    }
    return invoke(method, thiz, parms);
  }


  @NotNull
  private Object[] visitArray(@NotNull Expression[] array) {
    Object result[] = new Object[array.length];
    for (int i = 0; i < array.length; ++i) {
      result[i] = visit(array[i]);
    }
    return result;
  }

  @Nullable
  private Object visitAssignmentExpression(@NotNull AssignmentExpression expr) {
    assert stack != null;
    stack.assignments.put(expr, new AssignmentFuture(stack, expr.expression));
    return null;
  }


  @NotNull
  private String visitStringExpression(@NotNull StringExpression expr) {
    return expr.value;
  }


  @Nullable
  Object visitIfSwitchExpression(@NotNull IfSwitchExpression expr) {
    for (int i = 0; i < expr.conditions.length; ++i) {
      boolean condition = (boolean) visit(expr.conditions[i]);
      if (condition) {
        Object result = visit(expr.expressions[i]);
        require(result != null, "Expected %s to not return null", expr.expressions[i]);
        return result;
      }
    }
    Object result = visit(expr.elseExpression);
    require(result != null, "Expected %s to not return null", expr.elseExpression);
    return result;
  }

  Object visitParameterExpression(@NotNull ParameterExpression expr) {
    throw new RuntimeException("Need to bind " + expr.name);
  }


  @Nullable
  Object visitFindModuleExpression(@NotNull FindModuleExpression expr) {
    visit(expr.cdepExplodedRoot);
    visit(expr.targetPlatform);
    visit(expr.systemVersion);
    visit(expr.androidTargetAbi);
    visit(expr.androidStlType);
    visit(expr.osxSysroot);
    visit(expr.osxArchitectures);
    visit(expr.expression);
    return null;
  }

  @Nullable
  private Object visitFunctionTableExpression(@NotNull FunctionTableExpression expr) {
    for (Coordinate coordinate : expr.findFunctions.keySet()) {
      visit(expr.findFunctions.get(coordinate));
    }
    for (Coordinate coordinate : expr.examples.keySet()) {
      visit(expr.examples.get(coordinate));
    }
    return null;
  }

  private static class AssignmentFuture {

    public Expression expr;
    @Nullable
    public Object value;
    public Frame stack;

    AssignmentFuture(Frame stack, Expression expr) {
      if (expr instanceof AssignmentExpression) {
        throw new RuntimeException();
      }
      this.expr = expr;
      this.value = null;
      this.stack = stack;
    }
  }

  private static class Frame {

    final public Frame prior;

    @NotNull
    final public Map<AssignmentExpression, AssignmentFuture> assignments;

    Frame(Frame prior) {
      this.prior = prior;
      this.assignments = new HashMap<>();
    }

    AssignmentFuture lookup(@NotNull AssignmentExpression assignment) {
      AssignmentFuture value = assignments.get(assignment);
      if (value == null) {
        require(prior != null, "Could not resolve '%s", assignment.name);
        return prior.lookup(assignment);
      }
      return value;
    }
  }

  static class ModuleArchive {

    final public URL remote;
    final public File fullIncludePath;
    final public File fullLibraryName;

    ModuleArchive(URL remote, File fullIncludePath, File fullLibraryName) {
      this.remote = remote;
      this.fullIncludePath = fullIncludePath;
      this.fullLibraryName = fullLibraryName;
    }
  }
}
