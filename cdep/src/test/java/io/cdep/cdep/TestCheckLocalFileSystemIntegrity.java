package io.cdep.cdep;

import static io.cdep.cdep.ast.finder.ExpressionBuilder.abort;
import static io.cdep.cdep.ast.finder.ExpressionBuilder.assign;
import static io.cdep.cdep.ast.finder.ExpressionBuilder.assignmentBlock;
import static io.cdep.cdep.ast.finder.ExpressionBuilder.gte;
import static io.cdep.cdep.ast.finder.ExpressionBuilder.ifSwitch;
import static io.cdep.cdep.ast.finder.ExpressionBuilder.integer;
import static io.cdep.cdep.ast.finder.ExpressionBuilder.reference;

import io.cdep.cdep.ast.finder.AssignmentExpression;
import org.junit.Test;


public class TestCheckLocalFileSystemIntegrity {

  @Test
  public void testUnreferencedAssignment() {
    // This is an assignment that is used within an if-statement.
    // CheckLocalFilesystemIntegrity should not evaluate if-conditions
    AssignmentExpression assignment1 =
        assign("assignment1", gte(integer(1), 2));
    AssignmentExpression assignment2 =
        assign("assignment2", reference(assignment1));
    new CheckLocalFileSystemIntegrity(null).visit(
        assignmentBlock(
            assignment2,
            ifSwitch(reference(assignment2),
                abort("true"),
                abort("false"))));
  }
}
