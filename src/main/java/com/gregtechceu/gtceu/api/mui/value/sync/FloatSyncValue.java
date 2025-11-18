package com.gregtechceu.gtceu.api.mui.value.sync;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.value.sync.IDoubleSyncValue;
import com.gregtechceu.gtceu.api.mui.base.value.sync.IFloatSyncValue;
import com.gregtechceu.gtceu.api.mui.base.value.sync.IStringSyncValue;
import com.gregtechceu.gtceu.utils.FloatConsumer;
import com.gregtechceu.gtceu.utils.FloatSupplier;

import net.minecraft.network.FriendlyByteBuf;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class FloatSyncValue extends ValueSyncHandler<Float> implements
                            IFloatSyncValue<Float>, IDoubleSyncValue<Float>, IStringSyncValue<Float> {

    private final FloatSupplier getter;
    private final FloatConsumer setter;
    private float cache;

    public FloatSyncValue(@NotNull FloatSupplier getter, @Nullable FloatConsumer setter) {
        this.getter = Objects.requireNonNull(getter);
        this.setter = setter;
        this.cache = getter.getAsFloat();
    }

    public FloatSyncValue(@NotNull FloatSupplier getter) {
        this(getter, (FloatConsumer) null);
    }

    @Contract("null, null -> fail")
    public FloatSyncValue(@Nullable FloatSupplier clientGetter,
                          @Nullable FloatSupplier serverGetter) {
        this(clientGetter, null, serverGetter, null);
    }

    @Contract("null, _, null, _ -> fail")
    public FloatSyncValue(@Nullable FloatSupplier clientGetter, @Nullable FloatConsumer clientSetter,
                          @Nullable FloatSupplier serverGetter, @Nullable FloatConsumer serverSetter) {
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
        this.cache = this.getter.getAsFloat();
    }

    @Override
    public Float getValue() {
        return this.cache;
    }

    @Override
    public void setValue(@NotNull Float value, boolean setSource, boolean sync) {
        setFloatValue(value, setSource, sync);
    }

    @Override
    public float getFloatValue() {
        return this.cache;
    }

    @Override
    public void setFloatValue(float value, boolean setSource, boolean sync) {
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
        if (isFirstSync || this.getter.getAsFloat() != this.cache) {
            setFloatValue(this.getter.getAsFloat(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void notifyUpdate() {
        setFloatValue(this.getter.getAsFloat(), false, true);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeFloat(getFloatValue());
    }

    @Override
    public void read(FriendlyByteBuf buffer) {
        setFloatValue(buffer.readFloat(), true, false);
    }

    @Override
    public void setStringValue(String value, boolean setSource, boolean sync) {
        setFloatValue(Float.parseFloat(value), setSource, sync);
    }

    @Override
    public String getStringValue() {
        return String.valueOf(this.cache);
    }

    @Override
    public double getDoubleValue() {
        return getFloatValue();
    }

    @Override
    public void setDoubleValue(double value, boolean setSource, boolean sync) {
        setFloatValue((float) value, setSource, sync);
    }
}
