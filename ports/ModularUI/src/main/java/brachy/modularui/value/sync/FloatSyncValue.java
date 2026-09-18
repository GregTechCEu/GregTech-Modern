package brachy.modularui.value.sync;

import brachy.modularui.ModularUI;
import brachy.modularui.api.value.sync.IDoubleSyncValue;
import brachy.modularui.api.value.sync.IFloatSyncValue;
import brachy.modularui.api.value.sync.IStringSyncValue;
import brachy.modularui.utils.FloatConsumer;
import brachy.modularui.utils.FloatSupplier;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class FloatSyncValue extends ValueSyncHandler<ByteBuf, Float, FloatSyncValue> implements IFloatSyncValue<ByteBuf, Float>, IDoubleSyncValue<ByteBuf, Float>, IStringSyncValue<ByteBuf, Float> {

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
        if (ModularUI.isClientThread()) {
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
        onValueChanged();
        if (sync) sync();
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
    public void write(ByteBuf buffer) {
        buffer.writeFloat(getFloatValue());
    }

    @Override
    public void read(ByteBuf buffer) {
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

    @Override
    public Class<Float> getValueType() {
        return Float.class;
    }
}
