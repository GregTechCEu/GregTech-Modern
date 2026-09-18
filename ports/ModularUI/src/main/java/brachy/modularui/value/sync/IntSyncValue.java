package brachy.modularui.value.sync;

import brachy.modularui.ModularUI;
import brachy.modularui.api.value.sync.IDoubleSyncValue;
import brachy.modularui.api.value.sync.IIntSyncValue;
import brachy.modularui.api.value.sync.IStringSyncValue;

import net.minecraft.network.VarInt;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class IntSyncValue extends ValueSyncHandler<ByteBuf, Integer, IntSyncValue> implements IIntSyncValue<ByteBuf, Integer>, IDoubleSyncValue<ByteBuf, Integer>, IStringSyncValue<ByteBuf, Integer> {

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
        if (ModularUI.isClientThread()) {
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
        onValueChanged();
        if (sync) sync();
    }

    @Override
    public void setDoubleValue(double value, boolean setSource, boolean sync) {
        setIntValue((int) value, setSource, sync);
    }

    @Override
    public double getDoubleValue() {
        return this.cache;
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
    public void notifyUpdate() {
        setIntValue(this.getter.getAsInt(), false, true);
    }

    @Override
    public void write(ByteBuf buffer) {
        VarInt.write(buffer, getIntValue());
    }

    @Override
    public void read(ByteBuf buffer) {
        setIntValue(VarInt.read(buffer), true, false);
    }

    @Override
    public void setStringValue(String value, boolean setSource, boolean sync) {
        setIntValue(Integer.parseInt(value), setSource, sync);
    }

    @Override
    public String getStringValue() {
        return String.valueOf(this.cache);
    }

    @Override
    public Class<Integer> getValueType() {
        return Integer.class;
    }
}
