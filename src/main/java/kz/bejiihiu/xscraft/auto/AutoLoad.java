package kz.bejiihiu.xscraft.auto;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoLoad {
    boolean value() default true;
}
