package com.gregtechceu.gtceu.api.graphnet.logic;

import net.minecraft.nbt.LongTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public abstract class AbstractLongLogicData<T extends AbstractLongLogicData<T>> extends NetLogicEntry<T, LongTag> {

    private long value;

    protected AbstractLongLogicData() {}

    protected AbstractLongLogicData(long init) {
        this.value = init;
    }

    public T getWith(long value) {
        return getType().getNew().setValue(value);
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

    @Override
    public abstract @NotNull LongLogicType<T> getType();

    public static class LongLogicType<T extends AbstractLongLogicData<T>> extends NetLogicType<T> {

        public LongLogicType(@NotNull ResourceLocation name, @NotNull Supplier<@NotNull T> supplier,
                             @NotNull T defaultable) {
            super(name, supplier, defaultable);
        }

        public LongLogicType(@NotNull String namespace, @NotNull String name, @NotNull Supplier<@NotNull T> supplier,
                             @NotNull T defaultable) {
            super(namespace, name, supplier, defaultable);
        }

        public T getWith(long value) {
            return getNew().setValue(value);
        }
    }
}
