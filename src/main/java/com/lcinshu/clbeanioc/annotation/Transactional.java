package com.lcinshu.clbeanioc.annotation;

import java.lang.annotation.*;

/**
 * @author: licheng
 * @Date: 2020/12/27 20:13
 * @Desc:
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Transactional {

    Class rollBack() default Exception.class;
}
