package io.cdep.cdep.utils;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import static io.cdep.cdep.utils.Invariant.require;

public class ReflectionUtils {

  /**
   * Invoke but convert atypical exceptions to RuntimeException. If the invoked method threw a RuntimeException then unwrap and
   * throw.
   */
  public static Object invoke(@NotNull Method method, Object thiz, Object... args) {
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

  /**
   * Get method but convert atypical exceptions into RuntimeException. Should be used
   * when it is a bug if the method doesn't exist.
   */
  public static Method getMethod(@NotNull Class<?> clazz, @NotNull String name, Class<?>... parameterTypes) {
    try {
      return clazz.getMethod(name, parameterTypes);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Get field constant but convert atypical exceptions into RuntimeException. Should be used
   * when it is a bug if the method doesn't exist.
   */
  public static Object getFieldValue(@NotNull Field field, Object instance) {
    try {
      return field.get(instance);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @Nullable
  public static File getLocation(@NotNull Class<?> c) throws MalformedURLException {
    URL codeSourceLocation = c.getProtectionDomain().getCodeSource().getLocation();
    if (codeSourceLocation != null) {
      return urlToFile(codeSourceLocation);
    }

    URL classResource = c.getResource(c.getSimpleName() + ".class");
    require(classResource != null);

    assert classResource != null;
    String url = classResource.toString();
    String suffix = c.getCanonicalName().replace('.', '/') + ".class";
    require(url.endsWith(suffix));

    String path = url.substring(0, url.length() - suffix.length());

    if (path.startsWith("jar:")) {
      path = path.substring(4, path.length() - 2);
    }

    return urlToFile(new URL(path));
  }

  @Nullable
  private static File urlToFile(@Nullable final URL url) {
    return url == null ? null : urlToFile(url.toString());
  }

  @Nullable
  private static File urlToFile(String url) {
    String path = url;
    if (path.startsWith("jar:")) {
      int index = path.indexOf("!/");
      path = path.substring(4, index);
    }
    try {
      if (PlatformUtils.isWindows() && path.matches("file:[A-Za-z]:.*")) {
        path = "file:/" + path.substring(5);
      }
      return new File(new URL(path).toURI());
    } catch (@NotNull MalformedURLException | URISyntaxException e) {
      if (path.startsWith("file:")) {
        path = path.substring(5);
        return new File(path);
      }
    }
    require(false, "Invalid URL: %s", url);
    return null;
  }
}
