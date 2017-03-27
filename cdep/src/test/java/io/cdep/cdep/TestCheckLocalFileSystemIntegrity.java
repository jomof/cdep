package io.cdep.cdep;

import io.cdep.cdep.ast.finder.AssignmentExpression;
import org.junit.Test;

import static io.cdep.cdep.ast.finder.ExpressionBuilder.*;


public class TestCheckLocalFileSystemIntegrity {

  @Test
  public void testUnreferencedAssignment() {
    // This is an assignment that is used within an if-statement.
    // CheckLocalFilesystemIntegrity should not evaluate if-conditions
    AssignmentExpression assignment1 = assign("assignment1", gte(integer(1), 2));
    AssignmentExpression assignment2 = assign("assignment2", reference(assignment1));
    new CheckLocalFileSystemIntegrity(null).visit(assignmentBlock(assignment2, ifSwitch(reference(assignment2), abort("true"),
        abort("false"))));
  }
}
