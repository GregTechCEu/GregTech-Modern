package com.gregtechceu.gtceu.api.mui.value.sync;

import com.gregtechceu.gtceu.api.mui.base.value.sync.IBoolSyncValue;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Accepts enums which have exactly two elements. Can act as {@link IBoolSyncValue}.
 */
public class BinaryEnumSyncValue<T extends Enum<T>> extends EnumSyncValue<T> implements IBoolSyncValue<T> {

    public BinaryEnumSyncValue(@NotNull Class<T> enumClass, @NotNull Supplier<T> getter, @Nullable Consumer<T> setter) {
        super(enumClass, getter, setter);
        if (enumClass.getEnumConstants().length != 2) {
            throw new IllegalArgumentException("Enum class must have exactly two elements");
        }
    }

    public BinaryEnumSyncValue(@NotNull Class<T> enumClass, @NotNull Supplier<T> getter) {
        this(enumClass, getter, (Consumer<T>) null);
    }

    @Contract("_, null, _, null, _ -> fail")
    public BinaryEnumSyncValue(@NotNull Class<T> enumClass,
                               @Nullable Supplier<T> clientGetter, @Nullable Consumer<T> clientSetter,
                               @Nullable Supplier<T> serverGetter, @Nullable Consumer<T> serverSetter) {
        super(enumClass, clientGetter, clientSetter, serverGetter, serverSetter);
        if (enumClass.getEnumConstants().length != 2) {
            throw new IllegalArgumentException("Enum class must have exactly two elements");
        }
    }

    @Contract("_, null, null -> fail")
    public BinaryEnumSyncValue(@NotNull Class<T> enumClass,
                               @Nullable Supplier<T> clientGetter,
                               @Nullable Supplier<T> serverGetter) {
        this(enumClass, clientGetter, null, serverGetter, null);
    }

    @Override
    public boolean getBoolValue() {
        return this.cache.ordinal() == 1;
    }

    @Override
    public void setBoolValue(boolean value, boolean setSource, boolean sync) {
        setValue(this.enumClass.getEnumConstants()[value ? 1 : 0], setSource, sync);
    }
}
