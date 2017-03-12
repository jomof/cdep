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

import io.cdep.cdep.ast.finder.*;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
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
    Map<ParameterExpression, Object> parameters = new HashMap<>();
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
    Map<ParameterExpression, Object> parameters = new HashMap<>();
    parameters.put(function.targetPlatform, targetPlatform);
    parameters.put(function.osxSysroot, osxSysroot);
    return (FoundiOSModuleExpression) interpret(parameters, function.expression);
  }

  private static Object interpret(
      Map<ParameterExpression, Object> parameters,
      Object expression) throws InvocationTargetException, IllegalAccessException {

    if (expression instanceof String) {
      return expression;
    } else if (expression instanceof IfSwitchExpression) {
      IfSwitchExpression specific = (IfSwitchExpression) expression;
      for (int i = 0; i < specific.conditions.length; ++i) {
        boolean value = (boolean) interpret(parameters, specific.conditions[i]);
        if (value) {
          return interpret(parameters, specific.expressions[i]);
        }
      }
      return interpret(parameters, specific.elseExpression);
    } else if (expression instanceof AssignmentExpression) {
      Object result = parameters.get(expression);
      if (result != null) {
        return null;
      }
      AssignmentExpression specific = (AssignmentExpression) expression;
      result = interpret(parameters, specific.expression);
      parameters.put(specific, result);
      return null;
    } else if (expression instanceof ParameterExpression) {
      return parameters.get(expression);
    } else if (expression instanceof AbortExpression) {
      AbortExpression abortExpression = (AbortExpression) expression;
      Object parms[] = new String[abortExpression.parameters.length];
      for (int i = 0; i < parms.length; ++i) {
        parms[i] = interpret(parameters, abortExpression.parameters[i]);
      }
      throw new RuntimeException(String.format(abortExpression.message, parms));
    } else if (expression instanceof FoundAndroidModuleExpression) {
      return expression;
    } else if (expression instanceof FoundiOSModuleExpression) {
      return expression;
    } else if (expression instanceof InvokeFunctionExpression) {
      InvokeFunctionExpression specific = (InvokeFunctionExpression) expression;
      Object thiz = null;
      int firstParameter = 0;
      if (!Modifier.isStatic(specific.function.method.getModifiers())) {
        thiz = coerce(interpret(parameters, specific.parameters[0]),
                specific.function.method.getDeclaringClass());
        ++firstParameter;
      }
      Object parms[] = new Object[specific.parameters.length - firstParameter];
      for (int i = firstParameter; i < specific.parameters.length; ++i) {
        parms[i - firstParameter] = coerce(
                interpret(parameters, specific.parameters[i]),
                specific.function.method.getParameterTypes()[i - firstParameter]);
      }
      try {
        return specific.function.method.invoke(thiz, parms);
      } catch (Exception e) {
        throw e;
      }
    } else if (expression instanceof StringExpression) {
      StringExpression specific = (StringExpression) expression;
      return specific.value;
    } else if (expression instanceof ExternalFunctionExpression) {
      ExternalFunctionExpression specific = (ExternalFunctionExpression) expression;
      return specific.method;
    } else if (expression instanceof IntegerExpression) {
      IntegerExpression specific = (IntegerExpression) expression;
      return specific.value;
    } else if (expression instanceof ArrayExpression) {
      ArrayExpression specific = (ArrayExpression) expression;
      Object elements[] = new Object[specific.elements.length];
      for (int i = 0; i < elements.length; ++i) {
        elements[i] = interpret(parameters, specific.elements[i]);
      }
      return elements;
    } else if (expression instanceof AssignmentBlockExpression) {
      AssignmentBlockExpression specific = (AssignmentBlockExpression) expression;
      for (int i = 0; i < specific.assignments.size(); i++) {
        interpret(parameters, specific.assignments.get(i));
      }
        return interpret(parameters, specific.statement);
    } else if (expression instanceof AssignmentReferenceExpression) {
      AssignmentReferenceExpression specific = (AssignmentReferenceExpression) expression;
      return parameters.get(specific.assignment);
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
      if (o instanceof String) {
        return Integer.parseInt((String) o);
      }
    }
    if (clazz.equals(String[].class)) {
      if (o instanceof Object[]) {
        Object objarr[] = (Object[]) o;
        String result[] = new String[objarr.length];
        for (int i = 0; i < result.length; ++i) {
          result[i] = objarr.toString();
        }
        return result;
      }
    }

    throw new RuntimeException(String.format("Did not coerce %s to %s", o.getClass(), clazz));
  }

}
