package io.cdep.cdep.ast.finder;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;
import io.cdep.cdep.Coordinate;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.cdep.cdep.utils.Invariant.notNull;
import static io.cdep.cdep.utils.Invariant.require;

/**
 * Methods for creating expression trees
 */
public class ExpressionBuilder {

  @org.jetbrains.annotations.NotNull
  @NotNull
  public static AssignmentBlockExpression assignmentBlock(List<AssignmentExpression> assignments, StatementExpression statement) {
    return new AssignmentBlockExpression(assignments, statement);
  }

  @org.jetbrains.annotations.NotNull
  @NotNull
  public static AssignmentBlockExpression assignmentBlock(AssignmentExpression assignment, StatementExpression statement) {
    List<AssignmentExpression> assignments = new ArrayList<>();
    assignments.add(assignment);
    return new AssignmentBlockExpression(assignments, statement);
  }

  @org.jetbrains.annotations.NotNull
  @NotNull
  public static ModuleArchiveExpression archive(URL file, String sha256, Long size, String include, Expression includePath, String library, Expression libraryPath) {
    return new ModuleArchiveExpression(file, sha256, size, include, includePath, library, libraryPath);
  }

  @org.jetbrains.annotations.Nullable
  @NotNull
  public static ModuleExpression module(ModuleArchiveExpression archive, @Nullable Set<Coordinate> dependencies) {
    if (dependencies == null) {
      dependencies = new HashSet<>();
    }
    return new ModuleExpression(archive, dependencies);
  }

  @org.jetbrains.annotations.NotNull
  @NotNull
  public static AssignmentReferenceExpression reference(AssignmentExpression assignment) {
    return new AssignmentReferenceExpression(assignment);
  }

  @org.jetbrains.annotations.NotNull
  @NotNull
  public static IfSwitchExpression ifSwitch(@org.jetbrains.annotations.NotNull @NotNull List<Expression> conditionList, @org.jetbrains.annotations.NotNull
  @NotNull List<Expression> expressionList,
      Expression elseExpression) {
    notNull(conditionList);
    notNull(expressionList);
    notNull(elseExpression);
    require(conditionList.size() == expressionList.size());
    int size = conditionList.size();
    Expression conditions[] = new Expression[size];
    Expression expressions[] = new Expression[size];
    for (int i = 0; i < size; ++i) {
      conditions[i] = conditionList.get(i);
      expressions[i] = expressionList.get(i);
      notNull(conditions[i]);
      notNull(expressions[i]);
    }
    return ifSwitch(conditions, expressions, elseExpression);
  }

  @org.jetbrains.annotations.NotNull
  @NotNull
  public static IfSwitchExpression ifSwitch(Expression condition, Expression trueExpression, Expression falseExpression) {
    Expression conditions[] = new Expression[]{condition};
    Expression expressions[] = new Expression[]{trueExpression};
    return ifSwitch(conditions, expressions, falseExpression);
  }

  @org.jetbrains.annotations.NotNull
  @NotNull
  public static IfSwitchExpression ifSwitch(Expression conditions[], Expression expressions[], Expression elseExpression) {
    return new IfSwitchExpression(conditions, expressions, elseExpression);
  }

  @org.jetbrains.annotations.NotNull
  @NotNull
  public static InvokeFunctionExpression invoke(@org.jetbrains.annotations.NotNull ExternalFunctionExpression function, @org.jetbrains.annotations.NotNull
      Expression... parameters) {
    return new InvokeFunctionExpression(function, parameters);

  }

  /**
   * Returns true if expression left is greater than or equal to integer right.
   */
  @org.jetbrains.annotations.NotNull
  @NotNull
  public static InvokeFunctionExpression gte(Expression left, int right) {
    return invoke(
        ExternalFunctionExpression.INTEGER_GTE,
        left,
        integer(right));
  }

  /**
   * Return true if string starts with find.
   */
  @org.jetbrains.annotations.NotNull
  @NotNull
  public static InvokeFunctionExpression stringStartsWith(Expression string, Expression find) {
    return invoke(
        ExternalFunctionExpression.STRING_STARTSWITH,
        string,
        find
    );
  }

  @org.jetbrains.annotations.NotNull
  @NotNull
  public static InvokeFunctionExpression arrayHasOnlyElement(Expression array, Expression value) {
    return invoke(ExternalFunctionExpression.ARRAY_HAS_ONLY_ELEMENT, array, value);
  }


  /**
   * Extract a substring.
   */
  @org.jetbrains.annotations.NotNull
  @NotNull
  public static InvokeFunctionExpression substring(Expression string, Expression start,
      Expression end) {
    return invoke(
        ExternalFunctionExpression.STRING_SUBSTRING_BEGIN_END,
        string,
        start,
        end);
  }

  /**
   * Return the last index of value inside of string.
   */
  @org.jetbrains.annotations.NotNull
  @NotNull
  public static InvokeFunctionExpression lastIndexOfString(Expression string, String value) {
    return invoke(
        ExternalFunctionExpression.STRING_LASTINDEXOF,
        string,
        string(value));
  }

  /**
   * Given a file with path, return just the filename with extension.
   */
  @org.jetbrains.annotations.NotNull
  @NotNull
  public static InvokeFunctionExpression getFileName(Expression file) {
    return invoke(
        ExternalFunctionExpression.FILE_GETNAME,
        file);
  }

  @org.jetbrains.annotations.NotNull
  @NotNull
  public static InvokeFunctionExpression eq(Expression left, Expression right) {
    return invoke(ExternalFunctionExpression.STRING_EQUALS, left, right);
  }

  @org.jetbrains.annotations.NotNull
  @NotNull
  public static IntegerExpression integer(int value) {
    return new IntegerExpression(value);
  }

  @org.jetbrains.annotations.NotNull
  @NotNull
  public static AssignmentExpression assign(String name, Expression expression) {
    return new AssignmentExpression(name, expression);
  }

  @org.jetbrains.annotations.NotNull
  @NotNull
  public static ParameterExpression parameter(String name) {
    return new ParameterExpression(name);
  }

  @org.jetbrains.annotations.NotNull
  @NotNull
  public static AbortExpression abort(String message, Expression... parameters) {
    return new AbortExpression(message, parameters);
  }

  @org.jetbrains.annotations.NotNull
  @NotNull
  public static ArrayExpression array(Expression... expressions) {
    return new ArrayExpression(expressions);
  }

  @org.jetbrains.annotations.NotNull
  @NotNull
  public static StringExpression string(String value) {
    return new StringExpression(value);
  }

  @org.jetbrains.annotations.NotNull
  @NotNull
  public static ArrayExpression array(@org.jetbrains.annotations.NotNull @NotNull String... elements) {
    Expression array[] = new Expression[elements.length];
    for (int i = 0; i < elements.length; ++i) {
      array[i] = string(elements[i]);
    }
    return array(array);
  }

  @org.jetbrains.annotations.NotNull
  @NotNull
  public static Expression joinFileSegments(Expression root, String... segments) {
    return invoke(ExternalFunctionExpression.FILE_JOIN_SEGMENTS, root, array(segments));
  }

  @org.jetbrains.annotations.NotNull
  @NotNull
  public static Expression joinFileSegments(Expression root, Expression... segments) {
    return invoke(ExternalFunctionExpression.FILE_JOIN_SEGMENTS, root, array(segments));
  }

  @org.jetbrains.annotations.NotNull
  @NotNull
  public static MultiStatementExpression multi(StatementExpression... statements) {
    return new MultiStatementExpression(statements);
  }

  @org.jetbrains.annotations.NotNull
  @NotNull
  public static NopExpression nop() {
    return new NopExpression();
  }
}
