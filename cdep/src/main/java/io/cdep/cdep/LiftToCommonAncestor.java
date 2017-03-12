package io.cdep.cdep;

import io.cdep.cdep.ast.finder.*;

import java.util.*;

public class LiftToCommonAncestor extends RewritingVisitor {

    Set<AssignmentExpression> captured = new HashSet<>();
    List<AssignmentExpression> functionOrder = new ArrayList<>();
    Map<AssignmentExpression, Integer> functionCounts = new HashMap<>();


    public LiftToCommonAncestor() {
    }

    @Override
    Expression visitFunctionTableExpression(FunctionTableExpression expr) {
        List<AssignmentExpression> order = new ArrayList<>();
        Map<AssignmentExpression, Integer> counts = new HashMap<>();
        assignments(expr, order, counts);
        this.functionCounts = counts;
        this.functionOrder = order;
        return super.visitFunctionTableExpression(expr);
    }

    @Override
    protected Expression visitCaseExpression(CaseExpression expr) {
        Expression result = super.visitCaseExpression(expr);
        List<AssignmentExpression> block = extractBlocks(result);

        if (block.size() > 0) {
            return new AssignmentBlockExpression(block, (StatementExpression) result);
        }

        return result;
    }

    private List<AssignmentExpression> extractBlocks(Expression result) {
        List<AssignmentExpression> order = new ArrayList<>();
        Map<AssignmentExpression, Integer> count = new HashMap<>();
        assignments(result, order, count);
        return getAssignmentBlock(order, count);
    }

    private List<AssignmentExpression> getAssignmentBlock(List<AssignmentExpression> order, Map<AssignmentExpression, Integer> count) {
        List<AssignmentExpression> block = new ArrayList<>();
        for (AssignmentExpression assignment : order) {
            if (captured.contains(assignment)) {
                continue;
            }
            long functionCount = functionCounts.get(assignment);
            long currentCount = count.get(assignment);
            assert currentCount <= functionCount;
            if (currentCount == functionCount) {
                // Current scope covers all references in the function so
                // we can lift the assignments to this level.
                captured.add(assignment);
                block.add(assignment);
            }
        }
        return block;
    }

    @Override
    protected Expression visitIfExpression(IfExpression expr) {
        Expression result = super.visitIfExpression(expr);
        List<AssignmentExpression> block = extractBlocks(result);

        if (block.size() > 0) {
            return new AssignmentBlockExpression(block, (StatementExpression) result);
        }

        return result;
    }

    void assignments(Expression expr,
                     List<AssignmentExpression> order,
                     Map<AssignmentExpression, Integer> counts) {
        List<AssignmentExpression> assignments = new GetContainedReferences(expr).list;
        for (AssignmentExpression assignment : assignments) {
            Integer n = counts.get(assignment);
            if (n == null) {
                n = 0;
                order.add(assignment);
            }
            ++n;
            counts.put(assignment, n);
        }
    }

//    private void countAssignments(Map<AssignmentExpression, Integer> count, Expression expr) {
//        List<AssignmentExpression> set = new GetContainedReferences(expr).list;
//        for (AssignmentExpression assignment : set) {
//            Integer last = count.get(assignment);
//            if (last == null) {
//                last = 0;
//            }
//            count.put(assignment, last + 1);
//        }
//    }
}
