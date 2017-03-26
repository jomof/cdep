package io.cdep.cdep;

import io.cdep.cdep.ast.finder.*;

import java.util.*;

import static io.cdep.cdep.ast.finder.ExpressionBuilder.assignmentBlock;

public class LiftToCommonAncestor extends RewritingVisitor {
  Set<AssignmentExpression> captured = new HashSet<>();
  List<AssignmentExpression> functionOrder = new ArrayList<>();
  Map<AssignmentExpression, Integer> functionCounts = new HashMap<>();
  FindModuleExpression latest = null;

  public LiftToCommonAncestor() {
  }

  @Override
  protected Expression visitAssignmentExpression(AssignmentExpression expr) {
    // Don't duplicate any assignments
    return expr;
  }

  @Override
  protected Expression visitFindModuleExpression(FindModuleExpression expr) {
    List<AssignmentExpression> order = new ArrayList<>();
    Map<AssignmentExpression, Integer> counts = new HashMap<>();
    assignments(expr, order, counts);
    this.functionCounts = counts;
    this.functionOrder = order;
    latest = expr;
    FindModuleExpression result = (FindModuleExpression) super.visitFindModuleExpression(expr);
    List<AssignmentExpression> block = extractBlocks(result);
    if (block.size() > 0) {
      result = new FindModuleExpression(
          result.coordinate,
          result.cdepExplodedRoot,
          result.targetPlatform,
          result.systemVersion,
          result.androidTargetAbi,
          result.androidStlType,
          result.osxSysroot,
          result.osxArchitectures,
          assignmentBlock(block, result.expression));
    }
    this.functionOrder = null;
    this.functionCounts = null;
    return result;
  }


  @Override
  protected Expression visitIfSwitchExpression(IfSwitchExpression expr) {
    Expression result = super.visitIfSwitchExpression(expr);
    List<AssignmentExpression> block = extractBlocks(result);

    if (block.size() > 0) {
      return assignmentBlock(block, (StatementExpression) result);
    }
    return result;
  }

  @Override
  protected Expression visitModuleExpression(ModuleExpression expr) {
    Expression result = super.visitModuleExpression(expr);
    List<AssignmentExpression> block = extractBlocks(result);

    if (block.size() > 0) {
      return assignmentBlock(block, (StatementExpression) result);
    }
    return result;
  }

  private List<AssignmentExpression> extractBlocks(Expression result) {
    List<AssignmentExpression> order = new ArrayList<>();
    Map<AssignmentExpression, Integer> count = new HashMap<>();
    assignments(result, order, count);
    List<AssignmentExpression> block = new ArrayList<>();

    for (AssignmentExpression assignment : order) {
      if (captured.contains(assignment)) {
        continue;
      }
      long functionCount = functionCounts.get(assignment);
      long currentCount = count.get(assignment);
      if (currentCount > functionCount) {
        assert currentCount <= functionCount;
      }
      if (currentCount == functionCount) {
        // Current scope covers all references in the function so
        // we can lift the assignments to this level.
        captured.add(assignment);
        block.add(assignment);
      }
    }
    return block;
  }

  void assignments(Expression expr,
                   List<AssignmentExpression> order,
                   Map<AssignmentExpression, Integer> counts) {
    assert order.size() == 0;
    assert counts.size() == 0;
    List<AssignmentExpression> assignments = new GetContainedReferences(expr).list;
    for (AssignmentExpression assignment : assignments) {
      Integer n = counts.get(assignment);
      if (n == null) {
        n = 0;
      }
      order.add(assignment);
      counts.put(assignment, ++n);
    }
  }
}
