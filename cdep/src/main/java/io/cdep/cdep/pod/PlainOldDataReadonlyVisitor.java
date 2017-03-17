package io.cdep.cdep.pod;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Read-only visitor over a plain object. Uses reflection to find public fields to walk over.
 */
public class PlainOldDataReadonlyVisitor {

  public void visitPlainOldDataObject(String name, Object value) {
    visitFields(value);
  }

  public void visitString(String name, String node) {
  }

  public void visitStringArray(String name, String array[]) {
    visitArray(name, array, String.class);
  }

  public void visitLong(String name, Long value) {

  }

  public void visitObject(String name, Object value) {
    visitFields(value);
  }

  public void visitArray(String name, Object[] array, Class<?> elementType) {
    if (array == null) {
      return;
    }
    for (Object value : array) {
      visit(value, elementType);
    }
  }

  public void visit(Object element, Class<?> elementClass) {
    if (element == null) {
      return;
    }
    try {
      String methodName = getVisitorName(elementClass);
      Method method = getClass().getMethod(methodName, String.class, elementClass);
      method.invoke(this, null, element);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  public void visitFields(Object node) {
    if (node == null) {
      return;
    }
    if (node.getClass().isEnum()) {
      return;
    }
    try {
      for (Field field : node.getClass().getFields()) {
        if (field.getDeclaringClass() == Object.class) {
          continue;
        }
        if (field.getDeclaringClass() == String.class) {
          continue;
        }
        String methodName = getVisitorName(field.getType());
        Method method = getClass().getMethod(methodName, String.class, field.getType());
        Object fieldValue = field.get(node);
        if (fieldValue != null) {
          method.invoke(this, field.getName(), fieldValue);
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
    String name = type.getName();
    name = name.substring(name.lastIndexOf(".") + 1);
    name = "visit" + name;
    if (type.isArray()) {
      name = name.substring(0, name.length() - 1);
      name += "Array";
    }
    return name;
  }
}
