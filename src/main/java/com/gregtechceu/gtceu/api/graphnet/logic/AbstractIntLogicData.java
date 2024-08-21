package com.gregtechceu.gtceu.api.graphnet.logic;

import net.minecraft.nbt.NBTTagInt;
import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractIntLogicData<T extends AbstractIntLogicData<T>> extends NetLogicEntry<T, NBTTagInt> {

    private int value;

    protected AbstractIntLogicData(@NotNull String name) {
        super(name);
    }

    public T getWith(int value) {
        return getNew().setValue(value);
    }

    protected T setValue(int value) {
        this.value = value;
        return (T) this;
    }

    public int getValue() {
        return this.value;
    }

    @Override
    public NBTTagInt serializeNBT() {
        return new NBTTagInt(this.value);
    }

    @Override
    public void deserializeNBT(NBTTagInt nbt) {
        this.value = nbt.getInt();
    }

    @Override
    public void encode(PacketBuffer buf, boolean fullChange) {
        buf.writeVarInt(this.value);
    }

    @Override
    public void decode(PacketBuffer buf, boolean fullChange) {
        this.value = buf.readVarInt();
    }
}
