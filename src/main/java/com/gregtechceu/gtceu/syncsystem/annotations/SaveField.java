package com.gregtechceu.gtceu.syncsystem.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Instructs the sync system to save and load this field to and from world data.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SaveField {

    /**
     * Specifies the NBT key the data should be stored under, defaulting to the field name.
     */
    String nbtKey() default "";
}
