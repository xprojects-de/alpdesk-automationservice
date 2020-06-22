package x.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface DashboardInfo {

    int handle() default 0;

    String displayName() default "";

    boolean stateful() default false;

    boolean editable() default false;
}
