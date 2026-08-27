package com.gz.xg.annotation;

import com.gz.xg.enums.BusinessType;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OpLog {
    String title() default "";
    BusinessType businessType() default BusinessType.OTHER;
    boolean saveParam() default true;
    boolean saveResult() default false;
}
