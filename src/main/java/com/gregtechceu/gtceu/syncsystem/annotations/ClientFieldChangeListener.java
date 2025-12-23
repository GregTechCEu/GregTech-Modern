package com.gregtechceu.gtceu.syncsystem.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Instructs the sync system to call this method on the client after a field has changed.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ClientFieldChangeListener {

    /**
     * The field to listen for changes on.
     */
    String fieldName();
}
