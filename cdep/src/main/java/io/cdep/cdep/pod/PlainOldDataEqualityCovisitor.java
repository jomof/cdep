package io.cdep.cdep.pod;

import io.cdep.annotations.Nullable;
import io.cdep.cdep.utils.StringUtils;

import java.util.Objects;

public class PlainOldDataEqualityCovisitor extends PlainOldDataReadonlyCovisitor {

  protected boolean areEqual = true;
  @Nullable
  protected String firstDifference = null;
  @Nullable
  protected Object firstDifferenceLeft = null;
  @Nullable
  protected Object firstDifferenceRight = null;

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
        firstDifferenceLeft = left;
        firstDifferenceRight = right;
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
