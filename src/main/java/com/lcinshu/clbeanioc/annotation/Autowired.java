package com.lcinshu.clbeanioc.annotation;

import java.lang.annotation.*;

/**
 * @author: licheng
 * @Date: 2020/12/27 20:05
 * @Desc:
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {

    String value() default "";
}
