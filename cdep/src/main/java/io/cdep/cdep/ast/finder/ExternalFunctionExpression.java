package io.cdep.cdep.ast.finder;

import java.io.File;
import java.lang.reflect.Method;

public class ExternalFunctionExpression extends FunctionExpression {

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

  final public Method method;

  private ExternalFunctionExpression(Class clazz, String functionName, Class<?>... parameterTypes) {
    try {
      this.method = clazz.getMethod(functionName, parameterTypes);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }
}
