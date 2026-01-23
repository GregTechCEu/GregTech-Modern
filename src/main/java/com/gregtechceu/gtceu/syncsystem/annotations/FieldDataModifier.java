package com.gregtechceu.gtceu.syncsystem.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to define methods used as serialise/deserialise functions for fields annotated with {@code @CustomDataField}
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
