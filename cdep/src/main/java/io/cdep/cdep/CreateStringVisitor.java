package io.cdep.cdep;

import io.cdep.cdep.ast.finder.*;

public class CreateStringVisitor extends ReadonlyVisitor {
  private StringBuilder sb = new StringBuilder();
  private int indent = 0;

  private CreateStringVisitor(Expression expr) {
    visit(expr);
  }

  public static String convert(Expression expr) {
    String result = new CreateStringVisitor(expr).sb.toString();
    while (result.startsWith("\n") || result.startsWith("\r")) {
      result = result.substring(1);
    }
    while (result.endsWith("\n") || result.endsWith("\r")) {
      result = result.substring(0, result.length() - 1);
    }
    return result;
  }

  @Override
  protected void visitFindModuleExpression(FindModuleExpression expr) {
    appendIndented("find(%s)", expr.coordinate);
    ++indent;
    visit(expr.expression);
    --indent;
    append("\r\n");
    appendIndented("end_find");
    append("\r\n");
  }

  @Override
  protected void visitIfSwitchExpression(IfSwitchExpression expr) {
    append("\r\n");
    appendIndented("");
    for (int i = 0; i < expr.conditions.length; ++i) {
      append("if(");
      visit(expr.conditions[i]);
      append(") ");
      ++indent;
      visit(expr.expressions[i]);
      --indent;
      append("\r\n");
      appendIndented("else ");
    }
    ++indent;
    visit(expr.elseExpression);
    --indent;
    append("\r\n");
    if (expr.conditions.length > 0) {
      appendIndented("end_if");
    }
  }

  @Override
  protected void visitIntegerExpression(IntegerExpression expr) {
    append("%s", expr.value);
  }

  @Override
  protected void visitAbortExpression(AbortExpression expr) {
    Object parms[] = new String[expr.parameters.length];
    for (int i = 0; i < parms.length; ++i) {
      StringBuilder old = sb;
      sb = new StringBuilder();
      visit(expr.parameters[i]);
      parms[i] = sb.toString();
      sb = old;
    }

    append("\r\n");
    appendIndented("abort " + expr.message, parms);
  }


  @Override
  protected void visitAssignmentExpression(AssignmentExpression expr) {
    append("\r\n");
    appendIndented("var %s = ", expr.name);
    visit(expr.expression);
  }

  @Override
  protected void visitInvokeFunctionExpression(InvokeFunctionExpression expr) {
    append("%s(", expr.function.method.getName());
    for (int i = 0; i < expr.parameters.length; ++i) {
      if (i > 0) {
        append(", ");
      }
      visit(expr.parameters[i]);
    }
    append(")");
  }

  @Override
  protected void visitArray(Expression[] array) {
    append("[");
    for (int i = 0; i < array.length; ++i) {
      if (i > 0) {
        append(", ");
      }
      visit(array[i]);
    }
    append("]");
  }

  @Override
  protected void visitParameterExpression(ParameterExpression expr) {
    append(expr.name);
  }

  @Override
  protected void visitModuleExpression(ModuleExpression expr) {
    append("\r\n");
    appendIndented("module");
    append("\r\n");
    ++indent;
    visit(expr.archive);
    --indent;
    appendIndented("end_module");
  }

  protected void visitIndentedArray(Expression[] array) {
    for (int i = 0; i < array.length; ++i) {
      visit(array[i]);
    }
  }

  protected void visitModuleArchiveExpression(ModuleArchiveExpression expr) {
    if (expr.includePath != null) {
      appendIndent();
      append("include: ");
      visit(expr.includePath);
      append("\r\n");
    }
    if (expr.libraryPath != null) {
      appendIndent();
      append("library: ");
      visit(expr.libraryPath);
      append("\r\n");
    }
  }

  @Override
  public void visitAssignmentReferenceExpression(AssignmentReferenceExpression expr) {
    append("*" + expr.assignment.name);
  }

  @Override
  protected void visitStringExpression(StringExpression expr) {
    append("'%s'", expr.value);
  }

  private void append(String format, Object... parms) {
    sb.append(String.format(format, parms));
  }


  private void appendIndent() {
    String prefix = new String(new char[indent * 2]).replace('\0', ' ');
    sb.append(prefix);
  }

  private void appendIndented(String format, Object... parms) {
    appendIndent();
    sb.append(String.format(format, parms));
  }
}
