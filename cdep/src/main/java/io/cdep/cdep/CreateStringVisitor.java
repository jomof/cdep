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
        while (result.startsWith("\n")) {
            result = result.substring(1);
        }
        return result;
    }

    @Override
    protected void visitFindModuleExpression(FindModuleExpression expr) {
        appendIndented("find(%s)", expr.coordinate);
        ++indent;
        visit(expr.expression);
        --indent;
        appendIndented("end_find");
    }

    @Override
    protected void visitCaseExpression(CaseExpression expr) {
        append("\n");
        appendIndented("switch(");
        visit(expr.var);
        append(")\n");
        ++indent;
        for (Expression key : expr.cases.keySet()) {
            appendIndented("case ");
            visit(key);
            append(": ");
            ++indent;
            visit(expr.cases.get(key));
            append("\n");
            --indent;
        }
        appendIndented("default: ");
        ++indent;
        visit(expr.defaultCase);
        append("\n");
        --indent;
        --indent;
        appendIndented("end_switch");
    }

    @Override
    protected void visitIntegerExpression(IntegerExpression expr) {
        append("%s", expr.value);
    }

    @Override
    protected void visitLongExpression(LongExpression expr) {
        append("%s", expr.value);
    }

    @Override
    protected void visitIfGreaterThanOrEqualExpression(IfGreaterThanOrEqualExpression expr) {
        append("\n");
        appendIndented("if(");
        visit(expr.value);
        append(" >= ");
        visit(expr.compareTo);
        append(") ");
        ++indent;
        visit(expr.trueExpression);
        --indent;
        append("\n");
        appendIndented("else ");
        ++indent;
        visit(expr.falseExpression);
        append("\n");
        --indent;
        appendIndented("end_if");
    }

    @Override
    protected void visitIfExpression(IfExpression expr) {
        append("\n");
        appendIndented("if(");
        visit(expr.bool);
        append(") ");
        ++indent;
        visit(expr.trueExpression);
        --indent;
        append("\n");
        appendIndented("else ");
        ++indent;
        visit(expr.falseExpression);
        append("\n");
        --indent;
        appendIndented("end_if");
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

        append("abort " + expr.message, parms);
    }


    @Override
    protected void visitAssignmentExpression(AssignmentExpression expr) {
        append("\n");
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
    protected void visitFoundiOSModuleExpression(FoundiOSModuleExpression expr) {
        append("{iOS Package}");
    }

    @Override
    protected void visitFoundAndroidModuleExpression(FoundAndroidModuleExpression expr) {
        append("{Android Package}");
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

    private void appendIndented(String format, Object... parms) {
        String prefix = new String(new char[indent * 2]).replace('\0', ' ');
        sb.append(String.format(prefix + format, parms));
    }
}
