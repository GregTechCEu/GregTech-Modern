package brachy.modularui.value.sync;

import brachy.modularui.ModularUI;
import brachy.modularui.api.value.sync.IIntSyncValue;
import brachy.modularui.api.value.sync.IShortSyncValue;
import brachy.modularui.api.value.sync.IStringSyncValue;
import brachy.modularui.value.ShortValue;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ShortSyncValue extends ValueSyncHandler<ByteBuf, Short, ShortSyncValue> implements IShortSyncValue<ByteBuf, Short>, IIntSyncValue<ByteBuf, Short>, IStringSyncValue<ByteBuf, Short> {

    private short cache;
    private final ShortValue.Supplier getter;
    private final ShortValue.Consumer setter;

    public ShortSyncValue(@NotNull ShortValue.Supplier getter, @Nullable ShortValue.Consumer setter) {
        this.getter = Objects.requireNonNull(getter);
        this.setter = setter;
        this.cache = getter.getShort();
    }

    public ShortSyncValue(@NotNull ShortValue.Supplier getter) {
        this(getter, (ShortValue.Consumer) null);
    }

    @Contract("null, null -> fail")
    public ShortSyncValue(@Nullable ShortValue.Supplier clientGetter,
                          @Nullable ShortValue.Supplier serverGetter) {
        this(clientGetter, null, serverGetter, null);
    }

    @Contract("null, _, null, _ -> fail")
    public ShortSyncValue(@Nullable ShortValue.Supplier clientGetter, @Nullable ShortValue.Consumer clientSetter,
                          @Nullable ShortValue.Supplier serverGetter, @Nullable ShortValue.Consumer serverSetter) {
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
        this.cache = this.getter.getShort();
    }

    @Override
    public Short getValue() {
        return this.cache;
    }

    @Override
    public short getShortValue() {
        return this.cache;
    }

    @Override
    public void setValue(Short value, boolean setSource, boolean sync) {
        setShortValue(value, setSource, sync);
    }

    @Override
    public void setShortValue(short value, boolean setSource, boolean sync) {
        this.cache = value;
        if (setSource && this.setter != null) {
            this.setter.setShort(value);
        }
        onValueChanged();
        if (sync) sync();
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (isFirstSync || this.getter.getShort() != this.cache) {
            setShortValue(this.getter.getShort(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void notifyUpdate() {
        setShortValue(this.getter.getShort(), false, true);
    }

    @Override
    public void write(ByteBuf buffer) {
        buffer.writeShort(this.cache);
    }

    @Override
    public void read(ByteBuf buffer) {
        setShortValue(buffer.readShort(), true, false);
    }

    @Override
    public void setStringValue(String value, boolean setSource, boolean sync) {
        setShortValue(Short.parseShort(value), setSource, sync);
    }

    @Override
    public String getStringValue() {
        return String.valueOf(this.cache);
    }

    @Override
    public Class<Short> getValueType() {
        return Short.class;
    }

    @Override
    public void setIntValue(int value, boolean setSource, boolean sync) {
        setShortValue((short) value, setSource, sync);
    }

    @Override
    public int getIntValue() {
        return this.cache;
    }
}
