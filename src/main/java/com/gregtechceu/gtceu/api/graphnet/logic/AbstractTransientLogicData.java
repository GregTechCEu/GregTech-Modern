package com.gregtechceu.gtceu.api.graphnet.logic;

import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractTransientLogicData<T extends AbstractTransientLogicData<T>>
                                                extends NetLogicEntry<T, Tag> {

    @Override
    public final void deserializeNBT(Tag nbt) {}

    @Override
    public final @Nullable Tag serializeNBT() {
        return null;
    }

    @Override
    public boolean shouldEncode() {
        return false;
    }

    @Override
    public void encode(FriendlyByteBuf buf, boolean fullChange) {}

    @Override
    public void decode(FriendlyByteBuf buf, boolean fullChange) {}
}
