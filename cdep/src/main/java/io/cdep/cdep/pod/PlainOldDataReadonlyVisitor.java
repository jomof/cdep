package io.cdep.cdep.pod;

import io.cdep.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static io.cdep.cdep.utils.Invariant.*;
import static io.cdep.cdep.utils.ReflectionUtils.*;

/**
 * Read-only visitor over a plain object. Uses reflection to find public fields to walk over.
 */
abstract public class PlainOldDataReadonlyVisitor {

  public void visitPlainOldDataObject(String name, @org.jetbrains.annotations.NotNull @NotNull Object value) {
    visitFields(value);
  }

  abstract public void visitString(String name, String node);

  public void visitStringArray(String name, @org.jetbrains.annotations.NotNull @NotNull String array[]) {
    visitArray(name, array, String.class);
  }

  public void visitLong(String name, Long value) {
  }

  public void visitArray(String name, @org.jetbrains.annotations.NotNull @NotNull Object[] array, @org.jetbrains.annotations.NotNull @NotNull Class<?>
      elementType) {
    elementsNotNull(array);
    for (Object value : array) {
      visit(value, elementType);
    }
  }

  public void visit(Object element, @org.jetbrains.annotations.NotNull @NotNull Class<?> elementClass) {
    notNull(element);
    String methodName = getVisitorName(elementClass);
    Method method = getMethod(getClass(), methodName, String.class, elementClass);
    invoke(method, this, null, element);
  }

  public void visitFields(@org.jetbrains.annotations.NotNull @NotNull Object node) {
    notNull(node);
    if (node.getClass().isEnum()) {
      return;
    }
    for (Field field : node.getClass().getFields()) {
      require(field.getDeclaringClass() != Object.class);
      require(field.getDeclaringClass() != String.class);
      String methodName = getVisitorName(field.getType());
      Method method = getMethod(getClass(), methodName, String.class, field.getType());
      Object fieldValue = getFieldValue(field, node);
      if (fieldValue != null) {
        invoke(method, this, field.getName(), fieldValue);
      }
    }
  }

  private String getVisitorName(@org.jetbrains.annotations.NotNull @NotNull Class<?> type) {
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
