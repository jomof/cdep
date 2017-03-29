package io.cdep.cdep.pod;

import io.cdep.annotations.NotNull;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class PlainOldDataFuzzer {
  Map<Class, Set<Class>> childTypes = new HashMap<>();
  Set<Object> terminals = new HashSet<>();
  Set<Class> seen = new HashSet<>();
  Set<String> expectedContains = new HashSet<>();

  public void addTerminal(Object value) {
    terminals.add(value);
  }

  public void addParentTypes(Class clazz) {
    if (clazz.equals(Object.class)) {
      return;
    }
    if (seen.contains(clazz)) {
      return;
    }
    seen.add(clazz);

    // Add parents.
    Class parent = clazz.getSuperclass();
    if (parent == null) {
      return;
    }
    Set<Class> children = childTypes.get(parent);
    if (children == null) {
      children = new HashSet<>();
      childTypes.put(parent, children);
    }
    children.add(clazz);
    addParentTypes(parent);
  }


  private <T> T inst(Class<T> clazz, int seed, int depth) throws IllegalAccessException, InstantiationException {
    seed += 1234;

    if (clazz.equals(String.class)) {
      return (T) String.format("depth=%s", seed);
    }

    if (clazz.isArray()) {
      int elementCount = seed % 6;
      Class elementType = clazz.getComponentType();
      Object result= Array.newInstance(elementType, elementCount);
      for (int i = 0; i < elementCount; ++i) {
        Array.set(result, i, inst(clazz.getComponentType(), seed, depth + 1));
      }
      return (T) result;
    }

    // The deeper we go the harder we try to use a terminal
    if ((seed % 30) < depth) {
      for (Object terminal : terminals) {
        if (clazz.isAssignableFrom(terminal.getClass())) {
          return (T) terminal;
        }
      }
    }

    addParentTypes(clazz);

    if (Modifier.isAbstract(clazz.getModifiers())) {
      // Look up the child types
      int i = 0;
      Set<Class> children = childTypes.get(clazz);
      if (children == null) {
        throw new RuntimeException(clazz.toString());
      }
      for (Class child : children) {
        if (i == seed % children.size()) {
          return (T) inst(child, seed, depth + 1);
        }
        ++i;
      }
      throw new RuntimeException(clazz.toString());
    }
    T t = clazz.newInstance();
    for (Field field : clazz.getFields()) {
      field.setAccessible(true);
      Object sub = inst(field.getType(), seed, depth + 1);
      field.set(t, sub);
    }
    return t;
  }

  public <T> void fuzz(@NotNull Class<T> clazz, Function<Object, Object> action)
      throws IllegalAccessException, InstantiationException {
    for (int i = 0; i < 100; ++i) {
      try {
        T t = inst(clazz, i, 0);
        action.apply(t);
      } catch (RuntimeException e) {
        if (!e.getClass().equals(RuntimeException.class)) {
          throw e;
        }
        boolean matched = false;
        for (String expected : expectedContains) {
          if (e.getMessage().contains(expected)) {
            matched = true;
          }
        }
        if (!matched) {
          throw e;
        }
      }
    }
  }

  public void addExpectedException(String expected) {
    this.expectedContains.add(expected);
  }
}
