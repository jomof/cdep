package io.cdep.cdep.pod;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Visit two instances at the same time. Allows comparison and merging.
 */
public class PlainOldDataReadonlyCovisitor {

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
      covisit(leftElement, rightElement, elementType);
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
    covisit(left, right, representative.getClass());
  }

  public void covisit(Object left, Object right, Class<?> type) {
    if (left == null && right == null) {
      return;
    }
    try {
      String methodName = getVisitorName(type);
      Method method = getClass().getMethod(methodName, String.class, type, type);
      method.invoke(this, null, left, right);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
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
      return;
    }
    try {
      for (Field field : representative.getClass().getFields()) {
        if (field.getDeclaringClass() == Object.class) {
          continue;
        }
        if (field.getDeclaringClass() == String.class) {
          continue;
        }
        String methodName = getVisitorName(field.getType());
        Method method = getClass()
            .getMethod(methodName, String.class, field.getType(), field.getType());
        Object leftValue = left == null ? null : field.get(left);
        Object rightValue = right == null ? null : field.get(right);
        if (leftValue != null || rightValue != null) {
          method.invoke(this, field.getName(), leftValue, rightValue);
        }
      }
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
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
