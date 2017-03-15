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
        append("\n");
        appendIndented("end_find");
        append("\n");
    }

    @Override
    protected void visitIfSwitchExpression(IfSwitchExpression expr) {
        append("\n");
        appendIndented("");
        for (int i = 0; i < expr.conditions.length; ++i) {
            append("if(");
            visit(expr.conditions[i]);
            append(") ");
            ++indent;
            visit(expr.expressions[i]);
            --indent;
            append("\n");
            appendIndented("else ");
        }
        ++indent;
        visit(expr.elseExpression);
        --indent;
        append("\n");
        appendIndented("end_if");
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

        append("\n");
        appendIndented("abort " + expr.message, parms);
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
    protected void visitModuleExpression(ModuleExpression expr) {
        append("{Module}");
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
