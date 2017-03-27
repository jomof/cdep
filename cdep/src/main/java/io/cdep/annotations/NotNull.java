package io.cdep.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
public @interface NotNull {
  @NotNull String value() default "";

  @NotNull Class<? extends Exception> exception() default Exception.class;
}
