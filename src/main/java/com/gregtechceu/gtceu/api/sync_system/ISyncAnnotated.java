package com.gregtechceu.gtceu.api.sync_system;

import com.gregtechceu.gtceu.api.sync_system.annotations.ClientFieldChangeListener;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformer;

import net.minecraftforge.common.util.INBTSerializable;

/**
 * Represents a class with fields that have sync annotations. <br>
 *
 * This interface has no functionality on its own, it just marks that a class has sync annotations. <br>
 *
 * A sync annotated class cannot be synced on its own, it must be a field of an {@link ISyncManaged} class. <br>
 *
 * All {@link ISyncAnnotated} classes should have a no-args constructor. <br>
 *
 * {@link ClientFieldChangeListener} does not work for fields in {@link ISyncAnnotated} classes.
 *
 * <p>
 * A field of type {@code T} can be marked with sync annotations if:
 * <ul>
 * <li>{@code T} is primitive
 * <li>{@code T} has an {@link ValueTransformer} registered
 * <li>{@code T} implements {@link INBTSerializable}
 * <li>{@code T} is an {@link ISyncAnnotated} class
 * </ul>
 *
 * @see ISyncManaged
 */
public interface ISyncAnnotated {}
