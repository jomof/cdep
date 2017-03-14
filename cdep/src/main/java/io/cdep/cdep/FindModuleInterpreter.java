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

import io.cdep.cdep.InterpretingVisitor.ModuleArchive;
import io.cdep.cdep.ast.finder.FindModuleExpression;
import io.cdep.cdep.ast.finder.FunctionTableExpression;
import io.cdep.cdep.ast.finder.ParameterExpression;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

class FindModuleInterpreter {

  @SuppressWarnings("SameParameterValue")
  static ModuleArchive[] findAndroid(
      FunctionTableExpression table,
      Coordinate functionName,
      String cdepExplodedRoot,
      String targetPlatform,
      String systemVersion, // On android, platform like 21
      String androidStlType,
      String androidTargetAbi) throws InvocationTargetException, IllegalAccessException {
    final FindModuleExpression function = table.findFunctions.get(functionName);
    Map<ParameterExpression, Object> parameters = new HashMap<>();
    parameters.put(function.targetPlatform, targetPlatform);
    parameters.put(function.systemVersion, systemVersion);
    parameters.put(function.androidStlType, androidStlType);
    parameters.put(function.androidTargetAbi, androidTargetAbi);
    return (ModuleArchive[]) new InterpretingVisitor() {
      @Override
      protected Object visitParameterExpression(ParameterExpression expr) {
        if (expr == function.targetPlatform) {
          return targetPlatform;
        }
        if (expr == function.systemVersion) {
          return systemVersion;
        }
        if (expr == function.androidStlType) {
          return androidStlType;
        }
        if (expr == function.androidTargetAbi) {
          return androidTargetAbi;
        }
        if (expr == function.cdepExplodedRoot) {
          return cdepExplodedRoot;
        }
        return super.visitParameterExpression(expr);
      }
    }.visit(function.expression);
  }

  static ModuleArchive[] findiOS(
      FunctionTableExpression table,
      Coordinate functionName,
      String cdepExplodedRoot,
      String targetPlatform,
      String osxArchitecture[],
      String osxSysroot) throws InvocationTargetException, IllegalAccessException {
    final FindModuleExpression function = table.findFunctions.get(functionName);
    Map<ParameterExpression, Object> parameters = new HashMap<>();
    parameters.put(function.targetPlatform, targetPlatform);
    parameters.put(function.osxSysroot, osxSysroot);
    return (ModuleArchive[]) new InterpretingVisitor() {
      @Override
      protected Object visitParameterExpression(ParameterExpression expr) {
        if (expr == function.targetPlatform) {
          return targetPlatform;
        }
        if (expr == function.osxSysroot) {
          return osxSysroot;
        }
        if (expr == function.cdepExplodedRoot) {
          return cdepExplodedRoot;
        }
        return super.visitParameterExpression(expr);
      }
    }.visit(function.expression);
  }
}
