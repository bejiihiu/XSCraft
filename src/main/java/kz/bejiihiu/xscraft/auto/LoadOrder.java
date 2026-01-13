package kz.bejiihiu.xscraft.auto;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LoadOrder {
    int value() default 0;
}
