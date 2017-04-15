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
package io.cdep.cdep.generator;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;
import io.cdep.cdep.Coordinate;
import io.cdep.cdep.ast.finder.*;
import io.cdep.cdep.utils.FileUtils;

import java.io.File;
import java.io.IOException;

import static io.cdep.cdep.io.IO.info;
import static io.cdep.cdep.utils.Invariant.notNull;
import static io.cdep.cdep.utils.Invariant.require;

public class CMakeGenerator {

  final static private String CONFIG_FILE_NAME = "cdep-dependencies-config.cmake";

  @NotNull
  final private GeneratorEnvironment environment;

  @NotNull
  final private FunctionTableExpression table;
  @NotNull
  private StringBuilder sb;
  private int indent = 0;
  @Nullable
  private Coordinate coordinate = null;

  public CMakeGenerator(@NotNull GeneratorEnvironment environment, @NotNull FunctionTableExpression table) {
    this.environment = environment;
    table = (FunctionTableExpression) notNull(new CMakeConvertJoinedFileToString().visit(table));
    table = (FunctionTableExpression) notNull(new ConvertRequiresToCMakeTargetCompileFeaturesRewritingVisitor().visit(table));
    this.table = table;
    this.sb = new StringBuilder();
  }

  public void generate() throws IOException {
    File file = getCMakeConfigurationFile();
    String text = create();
    info("Generating %s\n", file);
    FileUtils.writeTextToFile(file, text);
  }

  @NotNull
  public String create() {
    append("# GENERATED FILE. DO NOT EDIT.\n");
    for (StatementExpression findFunction : table.findFunctions.values()) {
      indent = 0;
      visit(findFunction);
      require(indent == 0);
    }

    append("\nfunction(add_all_cdep_dependencies target)\n");
    for (StatementExpression findFunction : table.findFunctions.values()) {
      FindModuleExpression finder = getFindFunction(findFunction);
      String function = getAddDependencyFunctionName(finder.coordinate);
      append("  %s(${target})\n", function);
    }
    append("endfunction(add_all_cdep_dependencies)\n");
    return sb.toString();
  }

  private FindModuleExpression getFindFunction(StatementExpression statement) {
    if (statement instanceof FindModuleExpression) {
      return (FindModuleExpression) statement;
    }
    if (statement instanceof AssignmentBlockExpression) {
      return getFindFunction(((AssignmentBlockExpression) statement).statement);
    }
    throw new RuntimeException(statement.getClass().toString());
  }

  @NotNull
  private String getCMakePath(@NotNull File file) {
    return file.toString().replace("\\", "/");
  }

  @NotNull
  File getCMakeConfigurationFile() {
    return new File(environment.modulesFolder, CONFIG_FILE_NAME);
  }

  private void visit(@NotNull Expression expression) {

    String prefix = new String(new char[indent * 2]).replace('\0', ' ');

    if (expression instanceof FindModuleExpression) {
      FindModuleExpression specific = (FindModuleExpression) expression;
      this.coordinate = specific.coordinate;
      append("\n###\n");
      append("### Add dependency for CDep module: %s\n", specific.coordinate.toString());
      append("###\n");
      String coordinateVar = String.format("%s_CDEP_COORDINATE", getUpperArtifactId());
      append("%sif(%s)\n", prefix, coordinateVar);
      append("%s  message(FATAL_ERROR \"CDep module '${%s}' was already defined\")\n", prefix, coordinateVar);
      append("%sendif(%s)\n", prefix, coordinateVar);
      append("%sset(%s \"%s\")\n", prefix, coordinateVar, specific.coordinate);
      if (specific.headerArchive != null && specific.include != null) {
        append("%sset(%s_ROOT \"%s/%s/%s/%s/%s/%s\")\n",
            prefix,
            getUpperArtifactId(),
            getCMakePath(environment.unzippedArchivesFolder),
            coordinate.groupId,
            coordinate.artifactId,
            coordinate.version,
            specific.headerArchive,
            specific.include);
      }
      String appenderFunctionName = getAddDependencyFunctionName(coordinate);
      append("function({appenderFunctionName} target)\n".replace("{appenderFunctionName}", appenderFunctionName));

      append("  # Choose between Android NDK Toolchain and CMake Android Toolchain\n"
          + "  if(DEFINED CMAKE_ANDROID_STL_TYPE)\n"
          + "    set(cdep_determined_android_runtime ${CMAKE_ANDROID_STL_TYPE})\n"
          + "    set(cdep_determined_android_abi ${CMAKE_ANDROID_ARCH_ABI})\n"
          + "  else()\n"
          + "    set(cdep_determined_android_runtime ${ANDROID_STL})\n"
          + "    set(cdep_determined_android_abi ${ANDROID_ABI})\n" +
          "  endif()\n\n");
      append("  set(cdep_exploded_root \"%s\")", getCMakePath(environment.unzippedArchivesFolder));
      ++indent;
      visit(specific.body);
      --indent;
      append("endfunction({appenderFunctionName})\n".replace("{appenderFunctionName}", appenderFunctionName));
      return;
    } else if (expression instanceof IfSwitchExpression) {
      IfSwitchExpression specific = (IfSwitchExpression) expression;
      append("\n");
      append(prefix);
      for (int i = 0; i < specific.conditions.length; ++i) {
        append("if(");
        ++indent;
        visit(specific.conditions[i]);
        --indent;
        append(")");
        ++indent;
        visit(specific.expressions[i]);
        --indent;
        append("%selse", prefix);
      }
      append("()");
      ++indent;
      visit(specific.elseExpression);
      --indent;
      append("%sendif()\n", prefix);
      return;
    } else if (expression instanceof AssignmentExpression) {
      appendAssignments(prefix, expression, null);
      return;
    } else if (expression instanceof InvokeFunctionExpression) {
      InvokeFunctionExpression specific = (InvokeFunctionExpression) expression;
      String parms[] = new String[specific.parameters.length];
      for (int i = 0; i < specific.parameters.length; ++i) {
        Expression parm = specific.parameters[i];
        StringBuilder parmBuilder = new StringBuilder();
        StringBuilder old = sb;
        sb = parmBuilder;
        ++indent;
        visit(parm);
        --indent;
        sb = old;
        parms[i] = parmBuilder.toString();
      }
      // These are non-assignment function calls.
      if (specific.function == ExternalFunctionExpression.STRING_STARTSWITH) {
        append("%s MATCHES \"$%s.*\"", parms[0], unquote(parms[1]));
      } else if (specific.function == ExternalFunctionExpression.INTEGER_GTE) {
        append("%s GREATER %s", parms[0], Integer.parseInt(parms[1]) - 1);
      } else if (specific.function == ExternalFunctionExpression.STRING_EQUALS) {
        append("%s STREQUAL %s", parms[0], parms[1]);
      } else if (specific.function == ExternalFunctionExpression.ARRAY_HAS_ONLY_ELEMENT) {
        append("%s STREQUAL %s", parms[0], parms[1]);
      } else {
        throw new RuntimeException(specific.function.method.getName());
      }
      return;
    } else if (expression instanceof ParameterExpression) {
      ParameterExpression specific = (ParameterExpression) expression;
      append(parameterName(specific));
      return;
    } else if (expression instanceof IntegerExpression) {
      IntegerExpression specific = (IntegerExpression) expression;
      append("%s", specific.value);
      return;
    } else if (expression instanceof StringExpression) {
      StringExpression specific = (StringExpression) expression;
      append("\"" + specific.value + "\"");
      return;
    } else if (expression instanceof ModuleExpression) {
      ModuleExpression specific = (ModuleExpression) expression;
      for (Coordinate dependency : specific.dependencies) {
        append("\n%s%s(${target})", prefix, getAddDependencyFunctionName(dependency));
      }
      append("\n");
      visit(specific.archive);
      return;
    } else if (expression instanceof AbortExpression) {
      AbortExpression specific = (AbortExpression) expression;
      Object parms[] = new String[specific.parameters.length];
      for (int i = 0; i < parms.length; ++i) {
        StringBuilder argBuilder = new StringBuilder();
        StringBuilder old = sb;
        sb = argBuilder;
        int oldIndent = indent;
        indent = 0;
        visit(specific.parameters[i]);
        indent = oldIndent;
        sb = old;
        parms[i] = "${" + argBuilder.toString() + "}";
      }
      String message = String.format(specific.message, parms);
      append("\n%smessage(FATAL_ERROR \"%s\")\n", prefix, message);
      return;
    } else if (expression instanceof AssignmentBlockExpression) {
      append("\n");
      AssignmentBlockExpression specific = (AssignmentBlockExpression) expression;
      for (int i = 0; i < specific.assignments.size(); i++) {
        visit(specific.assignments.get(i));
      }
      visit(specific.statement);
      return;
    } else if (expression instanceof AssignmentReferenceExpression) {
      AssignmentReferenceExpression specific = (AssignmentReferenceExpression) expression;
      append("%s", specific.assignment.name);
      return;
    } else if (expression instanceof MultiStatementExpression) {
      MultiStatementExpression specific = (MultiStatementExpression) expression;
      for (StatementExpression expr : specific.statements) {
        visit(expr);
      }
      return;
    } else if (expression instanceof NopExpression) {
      append("\n");
      return;
    } else if (expression instanceof CMakeInvokeMethod) {
      CMakeInvokeMethod specific = (CMakeInvokeMethod) expression;
      append("%s%s\n", prefix, specific.toString());
      return;
    } else if (expression instanceof ModuleArchiveExpression) {
      ModuleArchiveExpression specific = (ModuleArchiveExpression) expression;
      assert specific.requires == null; // Should have been rewritten by now.
      if (specific.includePath != null) {
        append("%starget_include_directories(${target} PRIVATE ", prefix);
        visit(specific.includePath);
        append(")\n");
        append("%smessage(\"  cdep including ${exploded_archive_tail}/%s\")\n", prefix, specific.include);
      }

      if (specific.libraryPath != null) {
        append("%starget_link_libraries(${target} ", prefix);
        visit(specific.libraryPath);
        append(")\n");
        append("%smessage(\"  cdep linking ${target} with ${exploded_archive_tail}/%s\")\n", prefix, specific.library);
      }
      return;
    }
    throw new RuntimeException(expression.getClass().toString());
  }

  private String getUpperArtifactId() {
    assert coordinate != null;
    assert coordinate.artifactId != null;
    return coordinate.artifactId.toUpperCase().replace("-", "_").replace("/", "_");
  }

  @NotNull
  private String unquote(@NotNull String string) {
    require(string.startsWith("\"") && string.endsWith("\""));
    return string.substring(1, string.length() - 1);
  }

  private void append(@NotNull String format, @NotNull Object... args) {
    sb.append(String.format(format, args));
  }

  @NotNull
  private String parameterName(@NotNull ParameterExpression expr) {
    return expr.name;
  }

  private Object appendAssignments(@NotNull String prefix,
      @NotNull Expression expr,
      @Nullable String assignResult) {
    if (expr instanceof AssignmentExpression) {
      AssignmentExpression specific = (AssignmentExpression) expr;
      String identifier = specific.name;
      appendAssignments(prefix, specific.expression, identifier);
      return null;
    } else if (expr instanceof InvokeFunctionExpression) {
      InvokeFunctionExpression specific = (InvokeFunctionExpression) expr;
      Object values[] = new Object[specific.parameters.length];
      for (int i = 0; i < specific.parameters.length; ++i) {
        Object value = appendAssignments(prefix, specific.parameters[i], null);
        require(value != null);
        values[i] = value;
      }
      if (specific.function == ExternalFunctionExpression.FILE_GETNAME) {
        require(assignResult != null);
        append("%sget_filename_component(%s ${%s} NAME)\n", prefix, assignResult, values[0]);
        return null;
      } else if (specific.function == ExternalFunctionExpression.STRING_LASTINDEXOF) {
        require(assignResult != null);
        append("%sstring(FIND %s %s %s REVERSE)\n", prefix, values[0], values[1], assignResult);
        return null;
      } else if (specific.function == ExternalFunctionExpression.STRING_SUBSTRING_BEGIN_END) {
        require(assignResult != null);
        append("%sstring(SUBSTRING %s %s %s %s)\n", prefix, values[0], values[1], values[2], assignResult);
        return null;
      }
      throw new RuntimeException(specific.function.method.getName());
    } else if (expr instanceof StringExpression) {
      StringExpression specific = (StringExpression) expr;
      String result = "\"" + specific.value + "\"";
      if (assignResult != null) {
        append("%sset(%s %s)\n", prefix, assignResult, result);
        return null;
      }
      return result;
    } else if (expr instanceof ParameterExpression) {
      ParameterExpression specific = (ParameterExpression) expr;
      require(assignResult == null);
      return parameterName(specific);
    } else if (expr instanceof IntegerExpression) {
      require(assignResult == null);
      IntegerExpression specific = (IntegerExpression) expr;
      return String.format("%s", specific.value);
    } else if (expr instanceof AssignmentReferenceExpression) {
      AssignmentReferenceExpression specific = (AssignmentReferenceExpression) expr;
      return String.format("${%s}", specific.assignment.name);
    }
    throw new RuntimeException(expr.getClass().toString());
  }

  @NotNull
  String getAddDependencyFunctionName(@NotNull Coordinate coordinate) {
    return String.format("add_cdep_%s_dependency", coordinate.artifactId).replace("-", "_").replace("/", "_");
  }
}
