package com.gregtechceu.gtceu.api.graphnet.logic;

import net.minecraft.nbt.DoubleTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public abstract class AbstractDoubleLogicData<T extends AbstractDoubleLogicData<T>>
                                             extends NetLogicEntry<T, DoubleTag> {

    private double value;

    protected AbstractDoubleLogicData() {}

    protected AbstractDoubleLogicData(double init) {
        this.value = init;
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

    @Override
    public abstract @NotNull DoubleLogicType<T> getType();

    public static class DoubleLogicType<T extends AbstractDoubleLogicData<T>> extends NetLogicType<T> {

        public DoubleLogicType(@NotNull ResourceLocation name, @NotNull Supplier<@NotNull T> supplier,
                               @NotNull T defaultable) {
            super(name, supplier, defaultable);
        }

        public DoubleLogicType(@NotNull String namespace, @NotNull String name, @NotNull Supplier<@NotNull T> supplier,
                               @NotNull T defaultable) {
            super(namespace, name, supplier, defaultable);
        }

        public T getWith(double value) {
            return getNew().setValue(value);
        }
    }
}
