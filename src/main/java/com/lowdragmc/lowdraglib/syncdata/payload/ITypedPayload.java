package com.lowdragmc.lowdraglib.syncdata.payload;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;

public interface ITypedPayload<T> {

    byte getType();

    void writePayload(RegistryFriendlyByteBuf buffer);

    void readPayload(RegistryFriendlyByteBuf buffer);

    Tag serializeNBT(HolderLookup.Provider provider);

    void deserializeNBT(Tag tag, HolderLookup.Provider provider);

    T getPayload();

    boolean isPrimitive();

    default Object copyForManaged(Object value) {
        return value;
    }
}
