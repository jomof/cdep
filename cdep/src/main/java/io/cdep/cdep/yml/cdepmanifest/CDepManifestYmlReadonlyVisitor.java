package io.cdep.cdep.yml.cdepmanifest;

import io.cdep.cdep.Coordinate;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CDepManifestYmlReadonlyVisitor {

  public void visitString(String name, String node) {

  }

  public void visitHardNameDependency(String name, HardNameDependency value) {
    visit(value);
  }

  public void visitCoordinate(String name, Coordinate node) {
    visit(node);
  }

  public void visitCDepManifestYml(String name, CDepManifestYml node) {
    visit(node);
  }

  public void visitHardNameDependencyArray(String name, HardNameDependency array[]) {
    visitArray(array, HardNameDependency.class);
  }

  public void visitStringArray(String name, String array[]) {
    visitArray(array, String.class);
  }

  public void visitAndroidArchiveArray(String name, AndroidArchive array[]) {
    visitArray(array, AndroidArchive.class);
  }

  public void visitiOSArchiveArray(String name, iOSArchive array[]) {
    visitArray(array, iOSArchive.class);
  }

  public void visitiOSPlatform(String name, iOSPlatform value) {
    visit(value);
  }

  public void visitiOSArchitecture(String name, iOSArchitecture value) {
    visit(value);
  }

  public void visitArchive(String name, Archive archive) {
    visit(archive);
  }

  public void visitAndroid(String name, Android android) {
    visit(android);
  }

  public void visitLong(String name, Long value) {

  }

  public void visitObject(String name, Object value) {
    visit(value);
  }

  public void visitiOS(String name, iOS value) {
    visit(value);
  }

  public void visitAndroidArchive(String name, AndroidArchive value) {
    visit(value);
  }

  public void visitiOSArchive(String name, iOSArchive value) {
    visit(value);
  }

  public void visitArray(Object[] array, Class<?> elementType) {
    if (array == null) {
      return;
    }
    for (Object value : array) {
      visitElement(value, elementType);
    }
  }

  public void visitElement(Object element, Class<?> elementClass) {
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

  public void visit(Object node) {
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
