package io.cdep.cdep.pod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static io.cdep.cdep.utils.Invariant.*;
import static io.cdep.cdep.utils.ReflectionUtils.invoke;

/**
 * Read-only visitor over a plain object. Uses reflection to find public fields to walk over.
 */
abstract public class PlainOldDataReadonlyVisitor {

  public void visitPlainOldDataObject(String name, Object value) {
    visitFields(value);
  }

  abstract public void visitString(String name, String node);

  public void visitStringArray(String name, String array[]) {
    visitArray(name, array, String.class);
  }

  public void visitLong(String name, Long value) {
  }

  public void visitArray(String name, Object[] array, Class<?> elementType) {
    elementsNotNull(array);
    for (Object value : array) {
      visit(value, elementType);
    }
  }

  public void visit(Object element, Class<?> elementClass) {
    notNull(element);
    try {
      String methodName = getVisitorName(elementClass);
      Method method = getClass().getMethod(methodName, String.class, elementClass);
      invoke(method, this, null, element);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  public void visitFields(Object node) {
    notNull(node);
    if (node.getClass().isEnum()) {
      return;
    }
    try {
      for (Field field : node.getClass().getFields()) {
        require(field.getDeclaringClass() != Object.class);
        require(field.getDeclaringClass() != String.class);
        String methodName = getVisitorName(field.getType());
        Method method = getClass().getMethod(methodName, String.class, field.getType());
        Object fieldValue = field.get(node);
        if (fieldValue != null) {
          invoke(method, this, field.getName(), fieldValue);
        }
      }
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (NoSuchMethodException e) {
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
