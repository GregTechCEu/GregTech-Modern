package com.gregtechceu.gtceu.syncsystem.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Instructs the sync system to apply this method when saving or loading a field.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FieldDataModifier {

    enum ModifyTarget {
        SAVE_NBT,
        LOAD_NBT,
    }

    /**
     * The field that this function applies to.
     */
    String fieldName();

    /**
     * If this function is called when saving or loading NBT
     */
    ModifyTarget target();
}
