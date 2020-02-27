package net.tokensmith.parser;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Function;


@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Parameter {
    String name();
    boolean required() default true;
    boolean nested() default false;
    String[] expected() default {};
    boolean parsable() default false;
    String delimiter() default " ";
    boolean allowMany() default false;
}
