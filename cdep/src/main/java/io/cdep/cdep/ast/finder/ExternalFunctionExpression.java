package io.cdep.cdep.ast.finder;

import java.io.File;
import java.lang.reflect.Method;

public class ExternalFunctionExpression extends Expression {

  // Given /a/b/c.txt returns c.txt
  final public static ExternalFunctionExpression FILE_GETNAME =
      new ExternalFunctionExpression(File.class, "getName");

  // Given /a/b/c.1.txt returns index of . in .txt
  // CMAKE string(FIND <string> <substring> <output variable> [REVERSE])
  final public static ExternalFunctionExpression STRING_LASTINDEXOF =
      new ExternalFunctionExpression(
          String.class,
          "lastIndexOf",
          String.class);

  // Given abcde, 2, 1 returns c
  final public static ExternalFunctionExpression STRING_SUBSTRING_BEGIN_END =
      new ExternalFunctionExpression(
          String.class,
          "substring",
          int.class,
          int.class);

  final public static ExternalFunctionExpression STRING_STARTSWITH =
      new ExternalFunctionExpression(
          String.class,
          "startsWith",
          String.class);

  final public static ExternalFunctionExpression FILE_JOIN_SEGMENTS =
      new ExternalFunctionExpression(
          ExternalFunctionExpression.class,
          "fileJoinSegments",
          File.class,
          String[].class);

  final public static ExternalFunctionExpression INTEGER_GTE =
      new ExternalFunctionExpression(
          ExternalFunctionExpression.class,
          "gte",
          int.class,
          int.class);

  final public static ExternalFunctionExpression STRING_EQUALS =
      new ExternalFunctionExpression(
          ExternalFunctionExpression.class,
          "eq",
          String.class,
          String.class);

  final public static ExternalFunctionExpression ARRAY_HAS_ONLY_ELEMENT =
      new ExternalFunctionExpression(
          ExternalFunctionExpression.class,
          "hasOnlyElement",
          String[].class,
          String.class);

  final public Method method;

  private ExternalFunctionExpression(Class clazz, String functionName, Class<?>... parameterTypes) {
    try {
      this.method = clazz.getMethod(functionName, parameterTypes);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  static public File fileJoinSegments(File base, String... segments) {
    for (int i = 0; i < segments.length; ++i) {
      base = new File(base, segments[i]);
    }
    return base;
  }

  static public boolean gte(int left, int right) {
    return left >= right;
  }

  static public boolean eq(String left, String right) {
    return left.equals(right);
  }

  static public boolean hasOnlyElement(String array[], String value) {
    if (array.length != 1) {
      return false;
    }
    return value.equals(array[0]);
  }
}
