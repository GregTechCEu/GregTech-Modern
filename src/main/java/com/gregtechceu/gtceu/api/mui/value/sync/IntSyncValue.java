package com.gregtechceu.gtceu.api.mui.value.sync;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.value.sync.IDoubleSyncValue;
import com.gregtechceu.gtceu.api.mui.base.value.sync.IIntSyncValue;
import com.gregtechceu.gtceu.api.mui.base.value.sync.IStringSyncValue;

import net.minecraft.network.FriendlyByteBuf;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class IntSyncValue extends ValueSyncHandler<Integer>
                          implements IIntSyncValue<Integer>, IDoubleSyncValue<Integer>, IStringSyncValue<Integer> {

    private int cache;
    private final IntSupplier getter;
    private final IntConsumer setter;

    public IntSyncValue(@NotNull IntSupplier getter, @Nullable IntConsumer setter) {
        this.getter = Objects.requireNonNull(getter);
        this.setter = setter;
        this.cache = getter.getAsInt();
    }

    public IntSyncValue(@NotNull IntSupplier getter) {
        this(getter, (IntConsumer) null);
    }

    @Contract("null, null -> fail")
    public IntSyncValue(@Nullable IntSupplier clientGetter,
                        @Nullable IntSupplier serverGetter) {
        this(clientGetter, null, serverGetter, null);
    }

    @Contract("null, _, null, _ -> fail")
    public IntSyncValue(@Nullable IntSupplier clientGetter, @Nullable IntConsumer clientSetter,
                        @Nullable IntSupplier serverGetter, @Nullable IntConsumer serverSetter) {
        if (clientGetter == null && serverGetter == null) {
            throw new NullPointerException("Client or server getter must not be null!");
        }
        if (GTCEu.isClientThread()) {
            this.getter = clientGetter != null ? clientGetter : serverGetter;
            this.setter = clientSetter != null ? clientSetter : serverSetter;
        } else {
            this.getter = serverGetter != null ? serverGetter : clientGetter;
            this.setter = serverSetter != null ? serverSetter : clientSetter;
        }
        this.cache = this.getter.getAsInt();
    }

    @Override
    public Integer getValue() {
        return this.cache;
    }

    @Override
    public int getIntValue() {
        return this.cache;
    }

    @Override
    public void setValue(Integer value, boolean setSource, boolean sync) {
        setIntValue(value, setSource, sync);
    }

    @Override
    public void setIntValue(int value, boolean setSource, boolean sync) {
        this.cache = value;
        if (setSource && this.setter != null) {
            this.setter.accept(value);
        }
        if (sync) {
            sync(0, this::write);
        }
    }

    @Override
    public void setDoubleValue(double value, boolean setSource, boolean sync) {
        setIntValue((int) value, setSource, sync);
    }

    @Override
    public double getDoubleValue() {
        return cache;
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (isFirstSync || this.getter.getAsInt() != this.cache) {
            setIntValue(this.getter.getAsInt(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.cache);
    }

    @Override
    public void read(FriendlyByteBuf buffer) {
        setIntValue(buffer.readVarInt(), true, false);
    }

    @Override
    public void setStringValue(String value, boolean setSource, boolean sync) {
        setIntValue(Integer.parseInt(value), setSource, sync);
    }

    @Override
    public String getStringValue() {
        return String.valueOf(this.cache);
    }
}
