package io.cdep.cdep.ast.finder;

import io.cdep.annotations.Nullable;
import io.cdep.cdep.utils.StringUtils;
import io.cdep.cdep.yml.cdepmanifest.CxxLanguageFeatures;

import static io.cdep.cdep.ast.finder.CMakeInvokeMethod.Method.target_compile_features;

public class CMakeInvokeMethod extends StatementExpression {
  final Method method;
  final Object parameters[];

  CMakeInvokeMethod(Method method, Object parameters[]) {
    this.method = method;
    this.parameters = parameters;
  }

  public static CMakeInvokeMethod targetCompileFeatures(String target, CMakeAccessLevel access, CxxLanguageFeatures... features) {
    return new CMakeInvokeMethod(target_compile_features, new Object[]{target, access, features});
  }

  @Nullable
  @Override
  public String toString() {
    if (string == null) {
      string = method.toString();
      string += "(";
      for (int i = 0; i < parameters.length; ++i) {
        if (i > 0) {
          string += " ";
        }
        string += stringOf(parameters[i]);
      }
      string += ")";
    }
    return string;
  }

  private String stringOf(Object parameter) {
    if (parameter == null) {
      return "NULL";
    } else if (parameter.getClass().isArray()) {
      StringBuilder sb = new StringBuilder();
      int i = 0;
      for (Object object :(Object[]) parameter) {
        if (i != 0) {
          sb.append(" ");
        }
        sb.append(stringOf(object));
        ++i;
      }
      return sb.toString();
    }
    return parameter.toString();
  }

  enum Method {
    target_compile_features
  }
}
