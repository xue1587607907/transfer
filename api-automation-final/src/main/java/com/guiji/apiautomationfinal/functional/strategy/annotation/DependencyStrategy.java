package com.guiji.apiautomationfinal.functional.strategy.annotation;


import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DependencyStrategy {
    String value();
}