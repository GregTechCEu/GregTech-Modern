package brachy.modularui.value.sync;

import brachy.modularui.ModularUI;
import brachy.modularui.api.value.sync.IIntSyncValue;
import brachy.modularui.api.value.sync.ILongSyncValue;
import brachy.modularui.api.value.sync.IStringSyncValue;

import net.minecraft.network.VarLong;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;

public class LongSyncValue extends ValueSyncHandler<ByteBuf, Long, LongSyncValue> implements ILongSyncValue<ByteBuf, Long>, IIntSyncValue<ByteBuf, Long>, IStringSyncValue<ByteBuf, Long> {

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
        if (ModularUI.isClientThread()) {
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
        onValueChanged();
        if (sync) sync();
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
    public void write(ByteBuf buffer) {
        VarLong.write(buffer, getLongValue());
    }

    @Override
    public void read(ByteBuf buffer) {
        setValue(VarLong.read(buffer), true, false);
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

    @Override
    public Class<Long> getValueType() {
        return Long.class;
    }
}
