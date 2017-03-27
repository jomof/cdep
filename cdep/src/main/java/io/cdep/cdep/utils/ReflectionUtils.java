package io.cdep.cdep.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static io.cdep.cdep.utils.Invariant.notNull;

public class ReflectionUtils {

  /**
   * Invoke but convert atypical exceptions to RuntimeException
   */
  public static Object invoke(Method method, Object thiz, Object... args) {
    notNull(method);
    try {
      return method.invoke(thiz, args);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      // Unwrap RuntimeException
      if (e.getTargetException() instanceof RuntimeException) {
        throw (RuntimeException) e.getTargetException();
      }
      throw new RuntimeException(e);
    }
  }
}
