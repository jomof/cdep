package io.cdep.cdep.pod;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Visit two instances at the same time. Allows comparison and merging.
 */
public class PlainOldDataReadonlyCovisitor {

  public List<String> namestack = new ArrayList<>();

  protected void push(String name) {
    if (name == null) {
      push("[value]");
      return;
    }
    namestack.add(0, name);
  }

  protected void pop() {
    namestack.remove(0);
  }

  public void covisitPlainOldDataObject(String name, Object left, Object right) {
    covisitFields(left, right);
  }

  public void covisitString(String name, String left, String right) {
  }

  public void covisitInteger(String name, Integer left, Integer right) {
  }

  public void covisitStringArray(String name, String left[], String right[]) {
    covisitArray(name, left, right, String.class);
  }

  public void covisitLongArray(String name, Long left[], Long right[]) {
    covisitArray(name, left, right, Long.class);
  }

  public void covisitLong(String name, Long left, Long right) {

  }

  public void covisitObject(String name, Object left, Object right) {
    covisitFields(left, right);
  }

  public <T> void covisitArray(String name, T[] left, T[] right, Class<T> elementType) {
    if (left == null && right == null) {
      return;
    }
    int max = 0;
    if (left != null) {
      max = Math.max(max, left.length);
    }
    if (right != null) {
      max = Math.max(max, right.length);
    }
    for (int i = 0; i < max; ++i) {
      Object leftElement = null;
      Object rightElement = null;
      if (left != null && left.length > i) {
        leftElement = left[i];
      }
      if (right != null && right.length > i) {
        rightElement = right[i];
      }
      covisit(name, leftElement, rightElement, elementType);
    }
  }

  public void covisit(Object left, Object right) {
    if (left == null && right == null) {
      return;
    }
    Object representative = left;
    if (representative == null) {
      representative = right;
    }
    covisit(null, left, right, representative.getClass());
  }

  public void covisit(String name, Object left, Object right, Class<?> type) {
    if (left == null && right == null) {
      return;
    }
    String methodName = getVisitorName(type);
    push(name);
    try {
      Method method = getClass().getMethod(methodName, String.class, type, type);
      method.invoke(this, null, left, right);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      if (e.getTargetException() instanceof RuntimeException) {
        throw (RuntimeException) e.getTargetException();
      }
      throw new RuntimeException(e);
    } finally {
      pop();
    }
  }

  public void covisitFields(Object left, Object right) {
    if (left == null && right == null) {
      return;
    }
    Object representative = right;
    if (representative == null) {
      representative = left;
    }
    if (representative.getClass().isEnum()) {
      throw new RuntimeException("Don't visit enum field");
    }
    try {
      for (Field field : representative.getClass().getFields()) {
        Object leftValue = left == null ? null : field.get(left);
        Object rightValue = right == null ? null : field.get(right);
        covisit(field.getName(), leftValue, rightValue, field.getType());
      }
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private String getVisitorName(Class<?> type) {
    String name;
    if (type.isArray()) {
      name = type.getComponentType().getName();
    } else {
      name = type.getName();
    }
    int dotPos = name.lastIndexOf(".");
    if (dotPos != -1) {
      // Trim the namespace
      name = name.substring(dotPos + 1);
    }
    name = "covisit" + name;
    if (type.isArray()) {
      name += "Array";
    }
    return name;
  }
}
