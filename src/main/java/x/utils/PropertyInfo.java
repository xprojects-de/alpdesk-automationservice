package x.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import x.DeviceUtils.Types;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface PropertyInfo {

    int handle() default 0;

    boolean editable() default false;

    int type() default Types.TYPE_PROPERTIEINFO_DEFAULT;

    String displayName() default "";

    boolean stateful() default false;

    boolean visibleValue() default true;

    int propertyType() default Types.PROPERTYTYPE_DEFAULT;

    int stepValue() default 0;

    boolean visibleOnREST() default false;

}
