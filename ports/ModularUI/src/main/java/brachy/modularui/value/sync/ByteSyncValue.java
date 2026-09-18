package brachy.modularui.value.sync;

import brachy.modularui.ModularUI;
import brachy.modularui.api.value.sync.IByteSyncValue;
import brachy.modularui.value.ByteValue;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ByteSyncValue extends ValueSyncHandler<ByteBuf, Byte, ByteSyncValue> implements IByteSyncValue<ByteBuf, Byte> {

    private byte cache;
    private final ByteValue.Supplier getter;
    private final ByteValue.Consumer setter;

    public ByteSyncValue(@NotNull ByteValue.Supplier getter) {
        this(getter, (ByteValue.Consumer) null);
    }

    public ByteSyncValue(@NotNull ByteValue.Supplier getter, @Nullable ByteValue.Consumer setter) {
        this.getter = Objects.requireNonNull(getter);
        this.setter = setter;
        this.cache = getter.getByte();
    }

    @Contract("null, _, null, _ -> fail")
    public ByteSyncValue(@Nullable ByteValue.Supplier clientGetter, @Nullable ByteValue.Consumer clientSetter,
                         @Nullable ByteValue.Supplier serverGetter, @Nullable ByteValue.Consumer serverSetter) {
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
        this.cache = this.getter.getByte();
    }

    public ByteSyncValue(@Nullable ByteValue.Supplier clientGetter,
                         @Nullable ByteValue.Supplier serverGetter) {
        this(clientGetter, null, serverGetter, null);
    }

    @Override
    public void setValue(Byte value, boolean setSource, boolean sync) {
        setByteValue(value, setSource, sync);
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (isFirstSync || this.getter.getByte() != this.cache) {
            setByteValue(this.getter.getByte(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void notifyUpdate() {
        setByteValue(this.getter.getByte(), false, true);
    }

    @Override
    public void write(ByteBuf buffer) {
        buffer.writeByte(getByteValue());
    }

    @Override
    public void read(ByteBuf buffer) {
        setByteValue(buffer.readByte(), true, false);
    }

    @Override
    public Byte getValue() {
        return getByteValue();
    }

    @Override
    public void setByteValue(byte value, boolean setSource, boolean sync) {
        this.cache = value;
        if (setSource && this.setter != null) {
            this.setter.setByte(value);
        }
        onValueChanged();
        if (sync) sync();
    }

    @Override
    public byte getByteValue() {
        return this.cache;
    }

    @Override
    public Class<Byte> getValueType() {
        return Byte.class;
    }
}
