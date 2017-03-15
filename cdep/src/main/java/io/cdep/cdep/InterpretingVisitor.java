package io.cdep.cdep;

import io.cdep.cdep.ast.finder.*;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Walks the expression tree and interprets the value for the supplied state.
 */
public class InterpretingVisitor {

  private List<Map<AssignmentExpression, Object>> stack = new ArrayList<>();

  private static Object coerce(Object o, Class<?> clazz) {
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
          result[i] = (String) objarr[i];
        }
        return result;
      }
    }

    throw new RuntimeException(String.format("Did not coerce %s to %s", o.getClass(), clazz));
  }

  public Object visit(Expression expr) {
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
    throw new RuntimeException(expr.getClass().toString());
  }

  protected ModuleArchive visitModuleArchiveExpression(ModuleArchiveExpression expr) {
    Object fullIncludePath = visit(expr.fullIncludePath);
    Object fullLibraryName = visit(expr.fullLibraryName);
    return new ModuleArchive(expr.file, (File) fullIncludePath, (File) fullLibraryName);
  }

  protected Object visitAssignmentReferenceExpression(AssignmentReferenceExpression expr) {
    return lookup(expr.assignment);
  }

  private Object lookup(AssignmentExpression assignment) {
    for (int i = 0; i < stack.size(); ++i) {
      Object result = stack.get(i).get(assignment);
      if (result != null) {
        return result;
      }
    }
    throw new RuntimeException(String.format("%s was not assigned a value", assignment.name));
  }

  protected Object visitAssignmentBlockExpression(AssignmentBlockExpression expr) {
    Map<AssignmentExpression, Object> frame = new HashMap<>();
    stack.add(0, frame);
    for (AssignmentExpression assignment : expr.assignments) {
      visitAssignmentExpression(assignment);
    }
    visit(expr.statement);
    Object result = visit(expr.statement);
    stack.remove(0);
    return result;
  }

  protected Object visitArrayExpression(ArrayExpression expr) {
    return visitArray(expr.elements);
  }

  protected Object visitIntegerExpression(IntegerExpression expr) {
    return expr.value;
  }

  protected Method visitExternalFunctionExpression(ExternalFunctionExpression expr) {
    return expr.method;
  }

  protected Object visitExampleExpression(ExampleExpression expr) {
    return null;
  }

  protected Object visitAbortExpression(AbortExpression expr) {
    Object parameters[] = visitArray(expr.parameters);
    throw new RuntimeException(String.format(expr.message, parameters));
  }

  protected ModuleArchive[] visitModuleExpression(ModuleExpression expr) {
    return visitArray(expr.archives);
  }

  protected Object visitInvokeFunctionExpression(InvokeFunctionExpression expr) {
    Method method = visitExternalFunctionExpression(expr.function);
    Object parameters[] = visitArray(expr.parameters);

    Object thiz = null;
    int firstParameter = 0;
    if (!Modifier.isStatic(method.getModifiers())) {
      thiz = coerce(parameters[0],
          method.getDeclaringClass());
      ++firstParameter;
    }
    Object parms[] = new Object[expr.parameters.length - firstParameter];
    for (int i = firstParameter; i < expr.parameters.length; ++i) {
      parms[i - firstParameter] = coerce(
          parameters[i],
          method.getParameterTypes()[i - firstParameter]);
    }
    try {
      return method.invoke(thiz, parms);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected Object[] visitArray(Expression[] array) {
    Object result[] = new Object[array.length];
    for (int i = 0; i < array.length; ++i) {
      result[i] = visit(array[i]);
      if (result[i] == null) {
        throw new RuntimeException(String.format("Did not expect %s to return null",
            array[i].getClass()));
      }
    }
    return result;
  }

  protected ModuleArchive[] visitArray(ModuleArchiveExpression[] array) {
    ModuleArchive result[] = new ModuleArchive[array.length];
    for (int i = 0; i < array.length; ++i) {
      result[i] = visitModuleArchiveExpression(array[i]);
    }
    return result;
  }

  protected Object visitAssignmentExpression(AssignmentExpression expr) {
    Object rhs = visit(expr.expression);
    if (rhs == null) {
      throw new RuntimeException(
          String.format("Did not expect %s to return null", expr.expression.getClass()));
    }
    stack.get(0).put(expr, rhs);
    return null;
  }

  protected String visitStringExpression(StringExpression expr) {
    return expr.value;
  }

  protected Object visitIfSwitchExpression(IfSwitchExpression expr) {
    for (int i = 0; i < expr.conditions.length; ++i) {
      boolean condition = (boolean) visit(expr.conditions[i]);
      if (condition) {
        Object result = visit(expr.expressions[i]);
        if (result == null) {
          throw new RuntimeException(
              String.format("Expected %s to not return null", expr.expressions[i]));
        }
        return result;
      }
    }
    Object result = visit(expr.elseExpression);
    if (result == null) {
      throw new RuntimeException(
          String.format("Expected %s to not return null", expr.elseExpression));
    }
    return result;
  }

  protected Object visitParameterExpression(ParameterExpression expr) {
    throw new RuntimeException("Need to bind " + expr.name);
  }

  protected Object visitFindModuleExpression(FindModuleExpression expr) {
    visit(expr.cdepExplodedRoot);
    visit(expr.targetPlatform);
    visit(expr.systemVersion);
    visit(expr.androidTargetAbi);
    visit(expr.androidStlType);
    visit(expr.osxSysroot);
    visit(expr.osxArchitecture);
    visit(expr.expression);
    return null;
  }

  Object visitFunctionTableExpression(FunctionTableExpression expr) {
    for (Coordinate coordinate : expr.findFunctions.keySet()) {
      visit(expr.findFunctions.get(coordinate));
    }
    for (Coordinate coordinate : expr.examples.keySet()) {
      visit(expr.examples.get(coordinate));
    }
    return null;
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
