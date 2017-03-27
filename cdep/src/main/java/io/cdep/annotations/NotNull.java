package io.cdep.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.TYPE_USE})
public @interface NotNull {
  @org.jetbrains.annotations.NotNull String value() default "";

  @org.jetbrains.annotations.NotNull Class<? extends Exception> exception() default Exception.class;
}
