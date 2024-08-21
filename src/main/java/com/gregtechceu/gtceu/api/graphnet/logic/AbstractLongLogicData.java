package com.gregtechceu.gtceu.api.graphnet.logic;

import net.minecraft.nbt.LongTag;
import net.minecraft.network.FriendlyByteBuf;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractLongLogicData<T extends AbstractLongLogicData<T>> extends NetLogicEntry<T, LongTag> {

    private long value;

    protected AbstractLongLogicData(@NotNull String name) {
        super(name);
    }

    public T getWith(long value) {
        return getNew().setValue(value);
    }

    @Contract("_ -> this")
    public T setValue(long value) {
        this.value = value;
        return (T) this;
    }

    public long getValue() {
        return this.value;
    }

    @Override
    public LongTag serializeNBT() {
        return LongTag.valueOf(this.value);
    }

    @Override
    public void deserializeNBT(LongTag nbt) {
        this.value = nbt.getAsLong();
    }

    @Override
    public void encode(FriendlyByteBuf buf, boolean fullChange) {
        buf.writeVarLong(this.value);
    }

    @Override
    public void decode(FriendlyByteBuf buf, boolean fullChange) {
        this.value = buf.readVarLong();
    }
}
