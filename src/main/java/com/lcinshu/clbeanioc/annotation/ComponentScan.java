package com.lcinshu.clbeanioc.annotation;

import java.lang.annotation.*;

/**
 * @author: licheng
 * @Date: 2020/12/27 21:21
 * @Desc:
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ComponentScan {

    String[] scanPackages() default {};
}
