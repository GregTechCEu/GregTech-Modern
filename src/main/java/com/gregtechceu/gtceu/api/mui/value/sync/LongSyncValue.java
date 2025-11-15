package com.gregtechceu.gtceu.api.mui.value.sync;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.value.sync.IIntSyncValue;
import com.gregtechceu.gtceu.api.mui.base.value.sync.ILongSyncValue;
import com.gregtechceu.gtceu.api.mui.base.value.sync.IStringSyncValue;

import net.minecraft.network.FriendlyByteBuf;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;

public class LongSyncValue extends ValueSyncHandler<Long>
                           implements ILongSyncValue<Long>, IIntSyncValue<Long>, IStringSyncValue<Long> {

    private final LongSupplier getter;
    private final LongConsumer setter;
    private long cache;

    public LongSyncValue(@NotNull LongSupplier getter, @Nullable LongConsumer setter) {
        this.getter = Objects.requireNonNull(getter);
        this.setter = setter;
        this.cache = getter.getAsLong();
    }

    public LongSyncValue(@NotNull LongSupplier getter) {
        this(getter, (LongConsumer) null);
    }

    @Contract("null, null -> fail")
    public LongSyncValue(@Nullable LongSupplier clientGetter,
                         @Nullable LongSupplier serverGetter) {
        this(clientGetter, null, serverGetter, null);
    }

    @Contract("null, _, null, _ -> fail")
    public LongSyncValue(@Nullable LongSupplier clientGetter, @Nullable LongConsumer clientSetter,
                         @Nullable LongSupplier serverGetter, @Nullable LongConsumer serverSetter) {
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
        this.cache = this.getter.getAsLong();
    }

    @Override
    public Long getValue() {
        return this.cache;
    }

    @Override
    public long getLongValue() {
        return this.cache;
    }

    @Override
    public void setValue(Long value, boolean setSource, boolean sync) {
        setLongValue(value, setSource, sync);
    }

    @Override
    public void setLongValue(long value, boolean setSource, boolean sync) {
        this.cache = value;
        if (setSource && this.setter != null) {
            this.setter.accept(value);
        }
        if (sync) {
            sync(0, this::write);
        }
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (isFirstSync || this.getter.getAsLong() != this.cache) {
            setLongValue(this.getter.getAsLong(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void notifyUpdate() {
        setLongValue(this.getter.getAsLong(), false, true);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarLong(getLongValue());
    }

    @Override
    public void read(FriendlyByteBuf buffer) {
        setValue(buffer.readVarLong(), true, false);
    }

    @Override
    public void setIntValue(int value, boolean setSource, boolean sync) {
        setLongValue(value, setSource, sync);
    }

    @Override
    public void setStringValue(String value, boolean setSource, boolean sync) {
        setLongValue(Long.parseLong(value), setSource, sync);
    }

    @Override
    public int getIntValue() {
        return (int) this.cache;
    }

    @Override
    public String getStringValue() {
        return String.valueOf(this.cache);
    }
}
