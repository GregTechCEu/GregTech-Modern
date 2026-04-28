package com.gregtechceu.gtceu.gametest.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GameTest {

    String template() default "";

    String templateNamespace() default "";

    String batch() default "defaultBatch";

    long setupTicks() default 0L;

    long timeoutTicks() default 100L;

    boolean required() default true;

    int attempts() default 1;

    int requiredSuccesses() default 1;

    int rotationSteps() default 0;

    boolean manualOnly() default false;

    boolean skyAccess() default false;
}
