/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package io.cdep.cdep;

import io.cdep.cdep.ast.finder.AbortExpression;
import io.cdep.cdep.ast.finder.CallExpression;
import io.cdep.cdep.ast.finder.CaseExpression;
import io.cdep.cdep.ast.finder.CurryExpression;
import io.cdep.cdep.ast.finder.Expression;
import io.cdep.cdep.ast.finder.ExternalFunctionExpression;
import io.cdep.cdep.ast.finder.FindModuleExpression;
import io.cdep.cdep.ast.finder.FoundAndroidModuleExpression;
import io.cdep.cdep.ast.finder.FoundiOSModuleExpression;
import io.cdep.cdep.ast.finder.FunctionTableExpression;
import io.cdep.cdep.ast.finder.IfGreaterThanOrEqualExpression;
import io.cdep.cdep.ast.finder.IntegerExpression;
import io.cdep.cdep.ast.finder.LongConstantExpression;
import io.cdep.cdep.ast.finder.ParameterExpression;
import io.cdep.cdep.ast.finder.StringExpression;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class FindModuleInterpreter {

  @SuppressWarnings("SameParameterValue")
  static FoundAndroidModuleExpression findAndroid(
      FunctionTableExpression table,
      Coordinate functionName,
      String targetPlatform,
      String systemVersion, // On android, platform like 21
      String androidStlType,
      String androidTargetAbi) throws InvocationTargetException, IllegalAccessException {
    FindModuleExpression function = table.findFunctions.get(functionName);
    Map<ParameterExpression, String> parameters = new HashMap<>();
    parameters.put(function.targetPlatform, targetPlatform);
    parameters.put(function.systemVersion, systemVersion);
    parameters.put(function.androidStlType, androidStlType);
    parameters.put(function.androidTargetAbi, androidTargetAbi);
    return (FoundAndroidModuleExpression) interpret(parameters, function.expression);
  }

  static FoundiOSModuleExpression findiOS(
      FunctionTableExpression table,
      Coordinate functionName,
      String targetPlatform,
      String osxArchitecture[],
      String osxSysroot) throws InvocationTargetException, IllegalAccessException {
    FindModuleExpression function = table.findFunctions.get(functionName);
    Map<ParameterExpression, String> parameters = new HashMap<>();
    parameters.put(function.targetPlatform, targetPlatform);
    parameters.put(function.osxSysroot, osxSysroot);
    return (FoundiOSModuleExpression) interpret(parameters, function.expression);
  }

  private static Object interpret(
      Map<ParameterExpression, String> parameters,
      Object expression) throws InvocationTargetException, IllegalAccessException {

    if (expression instanceof String) {
      return expression;
    } else if (expression instanceof CaseExpression) {
      CaseExpression caseExpression = (CaseExpression) expression;
      Object caseVar = interpret(parameters, interpret(parameters, caseExpression.var));
      for (Expression caseValueExpression : caseExpression.cases.keySet()) {
        if (caseValueExpression.toString().equals(caseVar)) {
          return interpret(parameters,
              caseExpression.cases.get(caseValueExpression));
        }
      }
      return interpret(parameters, caseExpression.defaultCase);
    } else if (expression instanceof ParameterExpression) {
      return parameters.get(expression);
    } else if (expression instanceof AbortExpression) {
      AbortExpression abortExpression = (AbortExpression) expression;
      Object parms[] = new String[abortExpression.parameters.length];
      for (int i = 0; i < parms.length; ++i) {
        parms[i] = interpret(parameters, abortExpression.parameters[i]);
      }
      throw new RuntimeException(String.format(abortExpression.message, parms));
    } else if (expression instanceof IfGreaterThanOrEqualExpression) {
      IfGreaterThanOrEqualExpression ifexpr = (IfGreaterThanOrEqualExpression) expression;
      Long value = Long.parseLong((String) interpret(parameters, ifexpr.value));
      Long compareTo = (Long) interpret(parameters, ifexpr.compareTo);
      if (value >= compareTo) {
        return interpret(parameters, ifexpr.trueExpression);
      }
      return interpret(parameters, ifexpr.falseExpression);
    } else if (expression instanceof LongConstantExpression) {
      LongConstantExpression longConst = (LongConstantExpression) expression;
      return longConst.value;
    } else if (expression instanceof FoundAndroidModuleExpression) {
      return expression;
    } else if (expression instanceof FoundiOSModuleExpression) {
      return expression;
    } else if (expression instanceof CallExpression) {
      CallExpression specific = (CallExpression) expression;
      return interpret(parameters, specific.function);
    } else if (expression instanceof CurryExpression) {
      CurryExpression specific = (CurryExpression) expression;
      Object value = interpret(parameters, specific.finalParameter);
      List<Object> values = new ArrayList<>();
      values.add(value);

      Object originalFunction = interpret(parameters, specific.originalFunction);
      if (originalFunction instanceof IncompleteMethod) {
        IncompleteMethod incomplete = (IncompleteMethod) originalFunction;
        originalFunction = incomplete.method;
        values.addAll(incomplete.parameters);
      }

      if (originalFunction instanceof Method) {
        Method method = (Method) originalFunction;
        int expectedParameters = method.getParameterTypes().length;
        if (!Modifier.isStatic(method.getModifiers())) {
          expectedParameters++;
        }
        if (expectedParameters != values.size()) {
          return new IncompleteMethod(method, values);
        }
        Object thiz = null;
        int parmStart = 0;
        if (!Modifier.isStatic(method.getModifiers())) {
          ++parmStart;
          thiz = coerce(values.get(0),
              method.getDeclaringClass());
        }
        Object parms[] = new Object[method.getParameterTypes().length];
        for (int i = 0; i < parms.length; ++i) {
          parms[i] = coerce(values.get(i + parmStart),
              method.getParameterTypes()[i]);
        }
        return method.invoke(thiz, parms);
      }

      throw new RuntimeException(originalFunction.toString());

    } else if (expression instanceof StringExpression) {
      StringExpression specific = (StringExpression) expression;
      return specific.value;
    } else if (expression instanceof ExternalFunctionExpression) {
      ExternalFunctionExpression specific = (ExternalFunctionExpression) expression;
      return specific.method;
    } else if (expression instanceof IntegerExpression) {
      IntegerExpression specific = (IntegerExpression) expression;
      return specific.value;
    }
    throw new RuntimeException(expression.toString());
  }

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
    }

    throw new RuntimeException(String.format("Did not coerce %s to %s", o.getClass(), clazz));
  }

  private static class IncompleteMethod {

    public final List<Object> parameters;
    public final Method method;

    public IncompleteMethod(Method method, List<Object> parameters) {
      this.parameters = parameters;
      this.method = method;
    }
  }

}
