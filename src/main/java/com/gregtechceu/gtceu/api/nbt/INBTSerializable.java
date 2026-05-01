package com.gregtechceu.gtceu.api.nbt;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;

/**
 * Replacement for the legacy NeoForge {@code INBTSerializable<T extends Tag>}
 * interface, which was removed in NeoForge 26.1 in favour of
 * {@link net.neoforged.neoforge.common.util.ValueIOSerializable}. gtceu's
 * existing serialization callers still use the {@code Tag}-shaped contract,
 * so we keep this small interface in the gtceu package until those callers
 * are migrated to the {@code ValueIOSerializable} shape.
 */
public interface INBTSerializable<T extends Tag> {

    T serializeNBT(HolderLookup.Provider provider);

    void deserializeNBT(HolderLookup.Provider provider, T nbt);
}
