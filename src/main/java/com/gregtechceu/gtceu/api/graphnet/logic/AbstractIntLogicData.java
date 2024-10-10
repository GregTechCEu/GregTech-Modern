package com.gregtechceu.gtceu.api.graphnet.logic;

import net.minecraft.nbt.IntTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public abstract class AbstractIntLogicData<T extends AbstractIntLogicData<T>> extends NetLogicEntry<T, IntTag> {

    private int value;

    protected AbstractIntLogicData() {}

    protected AbstractIntLogicData(int init) {
        this.value = init;
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

    @Override
    public abstract @NotNull IntLogicType<T> getType();

    public static class IntLogicType<T extends AbstractIntLogicData<T>> extends NetLogicType<T> {

        public IntLogicType(@NotNull ResourceLocation name, @NotNull Supplier<@NotNull T> supplier,
                            @NotNull T defaultable) {
            super(name, supplier, defaultable);
        }

        public IntLogicType(@NotNull String namespace, @NotNull String name, @NotNull Supplier<@NotNull T> supplier,
                            @NotNull T defaultable) {
            super(namespace, name, supplier, defaultable);
        }

        public T getWith(int value) {
            return getNew().setValue(value);
        }
    }
}
