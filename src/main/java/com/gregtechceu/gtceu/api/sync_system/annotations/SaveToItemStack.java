package com.gregtechceu.gtceu.api.sync_system.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Saves a field on a BlockEntity to an item stack when the block is destroyed or picked.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SaveToItemStack {

    /**
     * If this field should be saved when the block is destroyed.
     */
    boolean saveToDroppedStack() default true;

    /**
     * If this field should be saved when the block is cloned.
     */
    boolean saveToPickedStack() default true;
}
