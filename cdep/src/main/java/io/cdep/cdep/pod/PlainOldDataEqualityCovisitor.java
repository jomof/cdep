package io.cdep.cdep.pod;

import io.cdep.cdep.utils.StringUtils;
import java.util.Objects;

public class PlainOldDataEqualityCovisitor extends PlainOldDataReadonlyCovisitor {

  protected boolean areEqual = true;
  protected String firstDifference = null;

  public static boolean areDeeplyIdentical(Object left, Object right) {
    PlainOldDataEqualityCovisitor thiz = new PlainOldDataEqualityCovisitor();
    thiz.covisit(left, right);
    return thiz.areEqual;
  }

  protected void checkEquals(Object left, Object right) {
    if (areEqual) {
      boolean equal = Objects.equals(left, right);
      if (!equal) {
        areEqual = false;
        firstDifference = StringUtils.joinOn(".", namestack);
      }
    }
  }

  @Override
  public void covisitLong(String name, Long left, Long right) {
    checkEquals(left, right);
  }

  @Override
  public void covisitString(String name, String left, String right) {
    checkEquals(left, right);
  }
}
