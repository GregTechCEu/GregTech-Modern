package com.gregtechceu.gtceu.api.sync_system.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Instructs the sync system to sync any changes to this field with clients.
 * <p>
 * Changes are not detected automatically -
 * {@code getSyncDataHolder().markClientSyncFieldDirty(FIELD_NAME)} must be called to include the field in the next sync
 * update
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SyncToClient {}
