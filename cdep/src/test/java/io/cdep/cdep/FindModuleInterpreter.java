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

import static io.cdep.cdep.utils.Invariant.require;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;
import io.cdep.cdep.InterpretingVisitor.ModuleArchive;
import io.cdep.cdep.ast.finder.FindModuleExpression;
import io.cdep.cdep.ast.finder.FunctionTableExpression;
import io.cdep.cdep.ast.finder.NopExpression;
import io.cdep.cdep.ast.finder.ParameterExpression;

class FindModuleInterpreter {

  @Nullable
  @SuppressWarnings("SameParameterValue")
  static ModuleArchive findAndroid(@NotNull FunctionTableExpression table, Coordinate functionName, final String
      cdepExplodedRoot, final String targetPlatform, final String systemVersion, // On android, platform like 21
      final String androidStlType, final String androidTargetAbi) {
    final FindModuleExpression function = table.findFunctions.get(functionName);
    return toModuleArchive(new InterpretingVisitor() {
      @Override
      protected Object visitParameterExpression(@NotNull ParameterExpression expr) {
        if (expr == table.globals.targetPlatform) {
          return targetPlatform;
        }
        if (expr == table.globals.systemVersion) {
          return systemVersion;
        }
        if (expr == table.globals.androidStlType) {
          return androidStlType;
        }
        if (expr == table.globals.androidTargetAbi) {
          return androidTargetAbi;
        }
        if (expr == table.globals.cdepExplodedRoot) {
          return cdepExplodedRoot;
        }
        return super.visitParameterExpression(expr);
      }
    }.visit(function.expression));
  }

  @Nullable
  static ModuleArchive findiOS(@NotNull FunctionTableExpression table, Coordinate functionName, final String cdepExplodedRoot,
      final String osxArchitectures[], final String osxSysroot) {
    final FindModuleExpression function = table.findFunctions.get(functionName);
    return toModuleArchive(new InterpretingVisitor() {
      @Override
      protected Object visitParameterExpression(@NotNull ParameterExpression expr) {
        if (expr == table.globals.targetPlatform) {
          return "Darwin";
        }
        if (expr == table.globals.osxSysroot) {
          return osxSysroot;
        }
        if (expr == table.globals.cdepExplodedRoot) {
          return cdepExplodedRoot;
        }
        if (expr == table.globals.osxArchitectures) {
          return osxArchitectures;
        }
        return super.visitParameterExpression(expr);
      }
    }.visit(function.expression));
  }

  @Nullable
  private static ModuleArchive toModuleArchive(Object value) {
    if (value instanceof ModuleArchive) {
      return (ModuleArchive) value;
    }
    if (value instanceof Object[]) {
      ModuleArchive found = null;
      for (Object object : (Object[]) value) {
        if (object instanceof ModuleArchive) {
          require(found == null);
          found = (ModuleArchive) object;
          continue;
        }
        require(object instanceof NopExpression);
      }
      require(found != null);
      return found;
    }
    throw new RuntimeException(value.getClass().toString());
  }

  @Nullable
  static ModuleArchive findLinux(@NotNull FunctionTableExpression table, Coordinate functionName, final String cdepExplodedRoot) {
    final FindModuleExpression function = table.findFunctions.get(functionName);
    return toModuleArchive(new InterpretingVisitor() {
      @Override
      protected Object visitParameterExpression(@NotNull ParameterExpression expr) {
        if (expr == table.globals.targetPlatform) {
          return "Linux";
        }
        if (expr == table.globals.cdepExplodedRoot) {
          return cdepExplodedRoot;
        }
        return super.visitParameterExpression(expr);
      }
    }.visit(function.expression));
  }
}
