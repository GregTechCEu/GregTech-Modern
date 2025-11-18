package com.gregtechceu.gtceu.api.mui.value.sync;

import com.gregtechceu.gtceu.utils.EqualityTest;
import com.gregtechceu.gtceu.utils.ICopy;
import com.gregtechceu.gtceu.utils.serialization.network.ByteBufAdapters;
import com.gregtechceu.gtceu.utils.serialization.network.IByteBufAdapter;
import com.gregtechceu.gtceu.utils.serialization.network.IByteBufDeserializer;
import com.gregtechceu.gtceu.utils.serialization.network.IByteBufSerializer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GenericSyncValue<T> extends ValueSyncHandler<T> {

    public static GenericSyncValue<ItemStack> forItem(@NotNull Supplier<ItemStack> getter,
                                                      @Nullable Consumer<ItemStack> setter) {
        return new GenericSyncValue<>(getter, setter, ByteBufAdapters.ITEM_STACK);
    }

    public static GenericSyncValue<FluidStack> forFluid(@NotNull Supplier<FluidStack> getter,
                                                        @Nullable Consumer<FluidStack> setter) {
        return new GenericSyncValue<>(getter, setter, ByteBufAdapters.FLUID_STACK);
    }

    private final Supplier<T> getter;
    private final Consumer<T> setter;
    private final IByteBufDeserializer<T> deserializer;
    private final IByteBufSerializer<T> serializer;
    private final EqualityTest<T> equals;
    private final ICopy<T> copy;
    private T cache;

    public GenericSyncValue(@NotNull Supplier<T> getter,
                            @Nullable Consumer<T> setter,
                            @NotNull IByteBufAdapter<T> adapter) {
        this(getter, setter, adapter, adapter, adapter, null);
    }

    public GenericSyncValue(@NotNull Supplier<T> getter,
                            @Nullable Consumer<T> setter,
                            @NotNull IByteBufAdapter<T> adapter,
                            @Nullable ICopy<T> copy) {
        this(getter, setter, adapter, adapter, adapter, copy);
    }

    public GenericSyncValue(@NotNull Supplier<T> getter,
                            @Nullable Consumer<T> setter,
                            @NotNull IByteBufDeserializer<T> deserializer,
                            @NotNull IByteBufSerializer<T> serializer) {
        this(getter, setter, deserializer, serializer, null, null);
    }

    public GenericSyncValue(@NotNull Supplier<T> getter,
                            @Nullable Consumer<T> setter,
                            @NotNull IByteBufDeserializer<T> deserializer,
                            @NotNull IByteBufSerializer<T> serializer,
                            @Nullable ICopy<T> copy) {
        this(getter, setter, deserializer, serializer, null, copy);
    }

    public GenericSyncValue(@NotNull Supplier<T> getter,
                            @NotNull IByteBufAdapter<T> adapter) {
        this(getter, null, adapter, adapter, adapter, null);
    }

    public GenericSyncValue(@NotNull Supplier<T> getter,
                            @NotNull IByteBufAdapter<T> adapter,
                            @Nullable ICopy<T> copy) {
        this(getter, null, adapter, adapter, adapter, copy);
    }

    public GenericSyncValue(@NotNull Supplier<T> getter,
                            @NotNull IByteBufDeserializer<T> deserializer,
                            @NotNull IByteBufSerializer<T> serializer) {
        this(getter, null, deserializer, serializer, null, null);
    }

    public GenericSyncValue(@NotNull Supplier<T> getter,
                            @NotNull IByteBufDeserializer<T> deserializer,
                            @NotNull IByteBufSerializer<T> serializer,
                            @Nullable ICopy<T> copy) {
        this(getter, null, deserializer, serializer, null, copy);
    }

    public GenericSyncValue(@NotNull Supplier<T> getter,
                            @Nullable Consumer<T> setter,
                            @NotNull IByteBufDeserializer<T> deserializer,
                            @NotNull IByteBufSerializer<T> serializer,
                            @Nullable EqualityTest<T> equals,
                            @Nullable ICopy<T> copy) {
        this.getter = Objects.requireNonNull(getter);
        this.cache = getter.get();
        this.setter = setter;
        this.deserializer = Objects.requireNonNull(deserializer);
        this.serializer = Objects.requireNonNull(serializer);
        this.equals = equals == null ? Objects::equals : EqualityTest.wrapNullSafe(equals);
        this.copy = copy == null ? ICopy.ofSerializer(serializer, deserializer) : copy;
    }

    @Override
    public T getValue() {
        return this.cache;
    }

    @Override
    public void setValue(T value, boolean setSource, boolean sync) {
        this.cache = this.copy.createDeepCopy(value);
        if (setSource && this.setter != null) {
            this.setter.accept(value);
        }
        if (sync) {
            sync(0, this::write);
        }
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        T t = this.getter.get();
        if (isFirstSync || !this.equals.areEqual(this.cache, t)) {
            setValue(t, false, false);
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
        this.serializer.serialize(buffer, this.cache);
    }

    @Override
    public void read(FriendlyByteBuf buffer) {
        setValue(this.deserializer.deserialize(buffer), true, false);
    }

    @SuppressWarnings("unchecked")
    public @Nullable Class<? extends T> getType() {
        if (this.cache != null) {
            return (Class<? extends T>) this.cache.getClass();
        }
        T t = this.getter.get();
        if (t != null) {
            return (Class<? extends T>) t.getClass();
        }
        return null;
    }

    public boolean isOfType(Class<?> expectedType) {
        Class<? extends T> type = getType();
        if (type == null) {
            throw new IllegalStateException("Could not infer type of GenericSyncValue since value is null!");
        }
        return expectedType.isAssignableFrom(type);
    }

    @SuppressWarnings("unchecked")
    public <V> GenericSyncValue<V> cast() {
        return (GenericSyncValue<V>) this;
    }
}
