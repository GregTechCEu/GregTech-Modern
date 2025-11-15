package com.gregtechceu.gtceu.api.mui.value.sync;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.value.IEnumValue;
import com.gregtechceu.gtceu.api.mui.base.value.sync.IIntSyncValue;

import net.minecraft.network.FriendlyByteBuf;

import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EnumSyncValue<T extends Enum<T>> extends ValueSyncHandler<T> implements IEnumValue<T>, IIntSyncValue<T> {

    @Getter
    protected final Class<T> enumClass;
    private final Supplier<T> getter;
    private final Consumer<T> setter;
    protected T cache;

    public EnumSyncValue(@NotNull Class<T> enumClass, @NotNull Supplier<T> getter, @Nullable Consumer<T> setter) {
        this.enumClass = Objects.requireNonNull(enumClass);
        this.getter = Objects.requireNonNull(getter);
        this.setter = setter;
        this.cache = getter.get();
    }

    public EnumSyncValue(@NotNull Class<T> enumClass, @NotNull Supplier<T> getter) {
        this(enumClass, getter, (Consumer<T>) null);
    }

    @Contract("_, null, null -> fail")
    public EnumSyncValue(Class<T> enumClass,
                         @Nullable Supplier<T> clientGetter,
                         @Nullable Supplier<T> serverGetter) {
        this(enumClass, clientGetter, null, serverGetter, null);
    }

    @Contract("_, null, _, null, _ -> fail")
    public EnumSyncValue(Class<T> enumClass, @Nullable Supplier<T> clientGetter, @Nullable Consumer<T> clientSetter,
                         @Nullable Supplier<T> serverGetter, @Nullable Consumer<T> serverSetter) {
        this.enumClass = enumClass;
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
        this.cache = this.getter.get();
    }

    @Override
    public T getValue() {
        return this.cache;
    }

    @Override
    public void setValue(T value, boolean setSource, boolean sync) {
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
        if (isFirstSync || this.getter.get() != this.cache) {
            setValue(this.getter.get(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void notifyUpdate() {
        setValue(this.getter.get(), false, true);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeEnum(getValue());
    }

    @Override
    public void read(FriendlyByteBuf buffer) {
        setValue(buffer.readEnum(this.enumClass), true, false);
    }

    @Override
    public void setIntValue(int value, boolean setSource, boolean sync) {
        setValue(this.enumClass.getEnumConstants()[value], setSource, sync);
    }

    @Override
    public int getIntValue() {
        return this.cache.ordinal();
    }
}
