package com.gregtechceu.gtceu.syncsystem.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Instructs the sync system to call a custom data serialisation function
 * <p>
 * Two {@link FieldDataModifier} functions must be defined for this field - one save function and one load function
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CustomDataField {}
