package com.gregtechceu.gtceu.api.graphnet.logic;

import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractDoubleLogicData<T extends AbstractDoubleLogicData<T>>
                                             extends NetLogicEntry<T, NBTTagDouble> {

    private double value;

    protected AbstractDoubleLogicData(@NotNull String name) {
        super(name);
    }

    public T getWith(double value) {
        return getNew().setValue(value);
    }

    protected T setValue(double value) {
        this.value = value;
        return (T) this;
    }

    public double getValue() {
        return this.value;
    }

    @Override
    public NBTTagDouble serializeNBT() {
        return new NBTTagDouble(this.value);
    }

    @Override
    public void deserializeNBT(NBTTagDouble nbt) {
        this.value = nbt.getDouble();
    }

    @Override
    public void encode(PacketBuffer buf, boolean fullChange) {
        buf.writeDouble(value);
    }

    @Override
    public void decode(PacketBuffer buf, boolean fullChange) {
        this.value = buf.readDouble();
    }
}
