package com.gregtechceu.gtceu.api.mui.value.sync;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.network.FriendlyByteBuf;

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
        if (type == null) {
            if (this.cache == null) {
                throw new IllegalArgumentException(
                        "If the value class is not give, then the getter must return a non null value!");
            }
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
        if (type == null) {
            if (this.cache == null) {
                throw new IllegalArgumentException(
                        "If the value class is not give, then the getter must return a non null value!");
            }
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
        onSetCache(setSource, sync);
    }

    protected void onSetCache(boolean setSource, boolean sync) {
        if (setSource && this.setter != null) {
            this.setter.accept(this.cache);
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

    @Override
    public Class<T> getValueType() {
        return type;
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

    /**
     * Allows safe modification of the cached value. Normally modifying the cached value can cause the value to never be
     * synced.
     * This method forces a sync after the modification.
     *
     * @param consumer function that operates on the current cached value
     */
    public void modifyValue(Consumer<T> consumer) {
        modifyValue(true, true, consumer);
    }

    /**
     * Allows safe modification of the cached value. Normally modifying the cached value can cause the value to never be
     * synced.
     * This method can automatically sync the cache after the modification. Be careful with potential issues when the
     * sync arg is false.
     *
     * @param consumer function that operates on the current cached value
     */
    public void modifyValue(boolean setSource, boolean sync, Consumer<T> consumer) {
        consumer.accept(this.cache);
        onSetCache(setSource, sync);
    }
}
