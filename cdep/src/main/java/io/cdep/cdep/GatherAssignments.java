package io.cdep.cdep;

import io.cdep.cdep.ast.finder.AssignmentExpression;
import io.cdep.cdep.ast.finder.Expression;

import java.util.*;

public class GatherAssignments extends ReadonlyVisitor {
    final public List<String> names = new ArrayList<>();
    final public Map<String, Set<AssignmentExpression>> assignments = new HashMap<>();

    GatherAssignments(Expression expr) {
        visit(expr);
    }

    @Override
    protected void visitAssignmentExpression(AssignmentExpression expr) {
        super.visitAssignmentExpression(expr);
        Set<AssignmentExpression> set = assignments.get(expr.name);
        if (set == null) {
            set = new HashSet<>();
            assignments.put(expr.name, set);
            names.add(expr.name); // Record the order found
        }
        set.add(expr);
    }
}
