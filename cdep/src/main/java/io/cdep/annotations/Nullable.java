package io.cdep.annotations;

import java.lang.annotation.*;

@SuppressWarnings("unused")
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
public @interface Nullable {
  @NotNull String value() default "";
}
