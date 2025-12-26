package com.gregtechceu.gtceu.api.mui.value.sync;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.network.FriendlyByteBuf;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractGenericSyncValue<T> extends ValueSyncHandler<T> {

    private final Class<T> type;
    private final Supplier<T> getter;
    private final Consumer<T> setter;
    private T cache;

    protected AbstractGenericSyncValue(Class<T> type, Supplier<T> getter, Consumer<T> setter) {
        this.getter = Objects.requireNonNull(getter);
        this.setter = setter;
        this.cache = getter.get();
        if (type == null && this.cache != null) {
            type = (Class<T>) this.cache.getClass();
        }
        this.type = type;
    }

    @Contract("_, null, _, null, _ -> fail")
    protected AbstractGenericSyncValue(Class<T> type, @Nullable Supplier<T> clientGetter,
                                       @Nullable Consumer<T> clientSetter,
                                       @Nullable Supplier<T> serverGetter, @Nullable Consumer<T> serverSetter) {
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
        if (type == null && this.cache != null) {
            type = (Class<T>) this.cache.getClass();
        }
        this.type = type;
    }

    protected abstract T createDeepCopyOf(T value);

    protected abstract boolean areEqual(T a, T b);

    protected abstract void serialize(FriendlyByteBuf buffer, T value);

    protected abstract T deserialize(FriendlyByteBuf buffer);

    @Override
    public T getValue() {
        return this.cache;
    }

    @Override
    public void setValue(T value, boolean setSource, boolean sync) {
        this.cache = createDeepCopyOf(value);
        if (setSource && this.setter != null) {
            this.setter.accept(value);
        }
        onValueChanged();
        if (sync) sync();
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (this.getter == null) return false;
        T t = this.getter.get();
        if (isFirstSync || !areEqual(this.cache, t)) {
            setValue(t, false, false);
            return true;
        }
        return false;
    }

    @Override
    public void notifyUpdate() {
        if (this.getter == null) throw new NullPointerException("Can't notify sync handler with null getter.");
        setValue(this.getter.get(), false, true);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        serialize(buffer, this.cache);
    }

    @Override
    public void read(FriendlyByteBuf buffer) {
        setValue(deserialize(buffer), true, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<T> getValueType() {
        return (Class<T>) getType();
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    @SuppressWarnings("unchecked")
    public @Nullable Class<? extends T> getType() {
        if (this.type != null) return type;
        if (this.cache != null) {
            return (Class<? extends T>) this.cache.getClass();
        }
        T t = this.getter.get();
        if (t != null) {
            return (Class<? extends T>) t.getClass();
        }
        return null;
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public boolean isOfType(Class<?> expectedType) {
        return isValueOfType(expectedType);
    }

    @Override
    public boolean isValueOfType(Class<?> expectedType) {
        Class<T> type = getValueType();
        if (type == null) {
            throw new IllegalStateException("Could not infer type of GenericSyncValue since value is null!");
        }
        return expectedType.isAssignableFrom(type);
    }

    @SuppressWarnings("unchecked")
    public <V> AbstractGenericSyncValue<V> cast() {
        return (AbstractGenericSyncValue<V>) this;
    }
}
