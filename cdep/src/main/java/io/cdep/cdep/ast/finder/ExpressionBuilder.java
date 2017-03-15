package io.cdep.cdep.ast.finder;

import io.cdep.cdep.Coordinate;

import java.net.URL;
import java.util.List;
import java.util.Set;

/**
 * Methods for creating expression trees
 */
public class ExpressionBuilder {

  public static AssignmentBlockExpression assignmentBlock(List<AssignmentExpression> assignments,
      StatementExpression statement) {
    return new AssignmentBlockExpression(assignments, statement);
  }

  public static ModuleArchiveExpression archive(
      URL file,
      String sha256,
      Long size,
      Expression fullIncludePath,
      Expression fullLibraryName) {
    return new ModuleArchiveExpression(file, sha256, size, fullIncludePath, fullLibraryName);
  }

  public static FoundiOSModuleExpression iosModule(ModuleArchiveExpression archives[],
      Set<Coordinate> dependencies) {
    return new FoundiOSModuleExpression(archives, dependencies);
  }

  public static FoundAndroidModuleExpression androidModule(
      ModuleArchiveExpression archives[],
      Set<Coordinate> dependencies) {
    return new FoundAndroidModuleExpression(archives, dependencies);
  }

  public static AssignmentReferenceExpression reference(AssignmentExpression assignment) {
    return new AssignmentReferenceExpression(assignment);
  }

  public static IfSwitchExpression ifSwitch(List<Expression> conditionList,
      List<Expression> expressionList,
      Expression elseExpression) {
    assert conditionList != null;
    assert expressionList != null;
    assert elseExpression != null;
    assert conditionList.size() == expressionList.size();
    int size = conditionList.size();
    Expression conditions[] = new Expression[size];
    Expression expressions[] = new Expression[size];
    for (int i = 0; i < size; ++i) {
      conditions[i] = conditionList.get(i);
      expressions[i] = expressionList.get(i);
      assert conditions[i] != null;
      assert expressions[i] != null;
    }
    return ifSwitch(conditions, expressions, elseExpression);
  }

  public static IfSwitchExpression ifSwitch(Expression conditions[], Expression expressions[],
      Expression elseExpression) {
    return new IfSwitchExpression(conditions, expressions, elseExpression);
  }

  public static InvokeFunctionExpression invoke(
      ExternalFunctionExpression function,
      Expression... parameters) {
    return new InvokeFunctionExpression(function, parameters);

  }

  /**
   * Returns true if expression left is greater than or equal to integer right.
   */
  public static InvokeFunctionExpression gte(Expression left, int right) {
    return invoke(
        ExternalFunctionExpression.INTEGER_GTE,
        left,
        integer(right));
  }

  /**
   * Return true if string starts with find.
   */
  public static InvokeFunctionExpression stringStartsWith(Expression string, Expression find) {
    return invoke(
        ExternalFunctionExpression.STRING_STARTSWITH,
        string,
        find
    );
  }

  /**
   * Extract a substring.
   */
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
  public static InvokeFunctionExpression lastIndexOfString(Expression string, String value) {
    return invoke(
        ExternalFunctionExpression.STRING_LASTINDEXOF,
        string,
        string(value));
  }

  /**
   * Given a file with path, return just the filename with extension.
   */
  public static InvokeFunctionExpression getFileName(Expression file) {
    return invoke(
        ExternalFunctionExpression.FILE_GETNAME,
        file);
  }

  public static InvokeFunctionExpression eq(Expression left, Expression right) {
    return invoke(
        ExternalFunctionExpression.STRING_EQUALS,
        left,
        right);
  }

  public static IntegerExpression integer(int value) {
    return new IntegerExpression(value);
  }

  public static AssignmentExpression assign(String name, Expression expression) {
    return new AssignmentExpression(name, expression);
  }

  public static ParameterExpression parameter(String name) {
    return new ParameterExpression(name);
  }

  public static AbortExpression abort(String message, Expression... parameters) {
    return new AbortExpression(message, parameters);
  }

  public static ArrayExpression array(Expression... expressions) {
    return new ArrayExpression(expressions);
  }

  public static StringExpression string(String value) {
    return new StringExpression(value);
  }

  public static ArrayExpression array(String... elements) {
    Expression array[] = new Expression[elements.length];
    for (int i = 0; i < elements.length; ++i) {
      array[i] = string(elements[i]);
    }
    return array(array);
  }

  public static Expression joinFileSegments(Expression root, String... segments) {
    return
        invoke(
            ExternalFunctionExpression.FILE_JOIN_SEGMENTS,
            root,
            array(segments));
  }

  public static Expression joinFileSegments(Expression root, Expression... segments) {
    return
        invoke(
            ExternalFunctionExpression.FILE_JOIN_SEGMENTS,
            root,
            array(segments));
  }
}
