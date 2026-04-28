package net.neoforged.neoforge.common.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;

import org.jetbrains.annotations.UnknownNullability;

/**
 * Temporary compatibility bridge for 1.21-era dependencies while GTCEu migrates
 * fully onto 26.1 APIs.
 */
@Deprecated(forRemoval = true)
public interface INBTSerializable<T extends Tag> {

    @UnknownNullability
    T serializeNBT(HolderLookup.Provider provider);

    void deserializeNBT(HolderLookup.Provider provider, T nbt);
}
