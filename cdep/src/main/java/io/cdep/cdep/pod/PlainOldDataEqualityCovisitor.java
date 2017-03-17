package io.cdep.cdep.pod;

import java.util.Objects;

public class PlainOldDataEqualityCovisitor extends PlainOldDataReadonlyCovisitor {

  public boolean areEqual = true;

  public static boolean areDeeplyIdentical(Object left, Object right) {
    PlainOldDataEqualityCovisitor thiz = new PlainOldDataEqualityCovisitor();
    thiz.covisit(left, right);
    return thiz.areEqual;
  }

  @Override
  public void covisitLong(String name, Long left, Long right) {
    boolean equal = Objects.equals(left, right);
    if (!equal) {
      areEqual = false;
    }
  }

  @Override
  public void covisitString(String name, String left, String right) {
    boolean equal = Objects.equals(left, right);
    if (!equal) {
      areEqual = false;
    }
  }
}
