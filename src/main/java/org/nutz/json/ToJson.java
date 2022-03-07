package org.nutz.json;

import java.lang.annotation.*;

/**
 * 把对象转为Json字符串时调用的方法
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface ToJson {

    /**
     * 把对象转为Json字符串时调用的方法，默认为调用该对象的『toJson』方法
     *
     * @return 对象转为Json字符串时调用的方法
     */
    String value() default "toJson";
}







