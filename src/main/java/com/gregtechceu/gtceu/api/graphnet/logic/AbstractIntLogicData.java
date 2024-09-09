package com.gregtechceu.gtceu.api.graphnet.logic;

import net.minecraft.nbt.IntTag;
import net.minecraft.network.FriendlyByteBuf;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractIntLogicData<T extends AbstractIntLogicData<T>> extends NetLogicEntry<T, IntTag> {

    private int value;

    protected AbstractIntLogicData(@NotNull NetLogicEntryType<T> type) {
        super(type);
    }

    public T getWith(int value) {
        return getType().getNew().setValue(value);
    }

    protected T setValue(int value) {
        this.value = value;
        return (T) this;
    }

    public int getValue() {
        return this.value;
    }

    @Override
    public IntTag serializeNBT() {
        return IntTag.valueOf(this.value);
    }

    @Override
    public void deserializeNBT(IntTag nbt) {
        this.value = nbt.getAsInt();
    }

    @Override
    public void encode(FriendlyByteBuf buf, boolean fullChange) {
        buf.writeVarInt(this.value);
    }

    @Override
    public void decode(FriendlyByteBuf buf, boolean fullChange) {
        this.value = buf.readVarInt();
    }
}
