package com.gregtechceu.gtceu.api.graphnet.logic;

import net.minecraft.nbt.DoubleTag;
import net.minecraft.network.FriendlyByteBuf;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractDoubleLogicData<T extends AbstractDoubleLogicData<T>>
                                             extends NetLogicEntry<T, DoubleTag> {

    private double value;

    protected AbstractDoubleLogicData(@NotNull NetLogicEntryType<T> type) {
        super(type);
    }

    public T getWith(double value) {
        return getType().getNew().setValue(value);
    }

    protected T setValue(double value) {
        this.value = value;
        return (T) this;
    }

    public double getValue() {
        return this.value;
    }

    @Override
    public DoubleTag serializeNBT() {
        return DoubleTag.valueOf(this.value);
    }

    @Override
    public void deserializeNBT(DoubleTag nbt) {
        this.value = nbt.getAsDouble();
    }

    @Override
    public void encode(FriendlyByteBuf buf, boolean fullChange) {
        buf.writeDouble(value);
    }

    @Override
    public void decode(FriendlyByteBuf buf, boolean fullChange) {
        this.value = buf.readDouble();
    }
}
