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

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A highly configurable value sync handler for any type. The constructors are obsolete in favor of the builder.
 * Create a builder with {@link #builder(Class)}, {@link #rawTypeBuilder(Class)} or {@link #notNullBuilder()}.
 * Each of these methods has its uses, however you should always try to use the {@link #builder(Class)} method.
 * <p>
 * Example:
 * <code>GenericSyncValue.builder(ItemStack.class)</code>
 * <p>
 * When you work with types which themselves have generic types, then your IDE will likely yell at you. In this case you
 * can use
 * {@link #rawTypeBuilder(Class)}. Only use this if you have to, since you can technically put any class inside there
 * without ModularUI be
 * able to verify it. Since you can't specify the type {@link T} with the type here you need to use the diamond
 * operator.
 * <p>
 * Example: <code>GenericSyncValue.&lt;List&lt;ItemStack&gt;&gt;rawTypeBuilder(List.class)</code>
 * <br>
 * Note that you wouldn't actually use {@link java.util.List List} here. It's just an example for a type with a generic
 * parameter.
 * <p>
 * If the value is never null you can also use {@link #notNullBuilder()}. This will infer the type with the value
 * supplier.
 * <p>
 * Example: <code>GenericSyncValue.&lt;ItemStack&gt;notNullBuilder()</code>
 * <p>
 * <p>
 * For collections, it is highly recommended to use {@link GenericListSyncHandler}, {@link GenericSetSyncHandler} or
 * {@link GenericMapSyncHandler}. For custom collections {@link GenericCollectionSyncHandler} can be helpful.
 *
 * @param <T> type of the value to sync
 */
public class GenericSyncValue<T> extends AbstractGenericSyncValue<T> {

    public static GenericSyncValue<ItemStack> forItem(@NotNull Supplier<ItemStack> getter,
                                                      @Nullable Consumer<ItemStack> setter) {
        return new GenericSyncValue<>(ItemStack.class, getter, setter, ByteBufAdapters.ITEM_STACK);
    }

    public static GenericSyncValue<FluidStack> forFluid(@NotNull Supplier<FluidStack> getter,
                                                        @Nullable Consumer<FluidStack> setter) {
        return new GenericSyncValue<>(FluidStack.class, getter, setter, ByteBufAdapters.FLUID_STACK);
    }

    private final IByteBufDeserializer<T> deserializer;
    private final IByteBufSerializer<T> serializer;
    private final EqualityTest<T> equals;
    private final ICopy<T> copy;

    @ApiStatus.Obsolete
    public GenericSyncValue(@NotNull Class<T> type,
                            @NotNull Supplier<T> getter,
                            @Nullable Consumer<T> setter,
                            @NotNull IByteBufAdapter<T> adapter,
                            @Nullable ICopy<T> copy,
                            boolean nullable) {
        this(type, getter, setter, adapter, adapter, adapter, copy, nullable);
    }

    @ApiStatus.Obsolete
    public GenericSyncValue(@NotNull Class<T> type,
                            @NotNull Supplier<T> getter,
                            @Nullable Consumer<T> setter,
                            @NotNull IByteBufAdapter<T> adapter) {
        this(type, getter, setter, adapter, adapter, adapter, null, false);
    }

    @ApiStatus.Obsolete
    public GenericSyncValue(@NotNull Class<T> type,
                            @NotNull Supplier<T> getter,
                            @Nullable Consumer<T> setter,
                            @NotNull IByteBufDeserializer<T> deserializer,
                            @NotNull IByteBufSerializer<T> serializer,
                            @Nullable EqualityTest<T> equals,
                            @Nullable ICopy<T> copy,
                            boolean nullable) {
        super(type, getter, setter);
        Objects.requireNonNull(deserializer);
        Objects.requireNonNull(serializer);
        this.deserializer = nullable ? IByteBufDeserializer.wrapNullSafe(deserializer) : deserializer;
        this.serializer = nullable ? IByteBufSerializer.wrapNullSafe(serializer) : serializer;
        if (equals == null) equals = EqualityTest.defaultTester();
        this.equals = nullable ? EqualityTest.wrapNullSafe(equals) : equals;
        if (copy == null) copy = ICopy.ofSerializer(serializer, deserializer);
        this.copy = copy; // null check in createDeepCopyOf()
    }

    @Override
    protected T createDeepCopyOf(T value) {
        return value == null ? null : this.copy.createDeepCopy(value);
    }

    @Override
    protected boolean areEqual(T a, T b) {
        return this.equals.areEqual(a, b);
    }

    @Override
    protected void serialize(FriendlyByteBuf buffer, T value) {
        this.serializer.serialize(buffer, value);
    }

    @Override
    protected T deserialize(FriendlyByteBuf buffer) {
        return this.deserializer.deserialize(buffer);
    }

    @SuppressWarnings("unchecked")
    public <V> GenericSyncValue<V> cast() {
        return (GenericSyncValue<V>) this;
    }

    /**
     * Creates a builder for a generic sync value. This is the recommended builder method.
     * The other builder methods are for when type has generic parameters.
     *
     * @param type Class of the value to sync. This is used to verify synced value types.
     * @param <T>  type of the value to sync
     * @return builder
     */
    public static <T> Builder<T> builder(Class<T> type) {
        return new Builder<>(type);
    }

    /**
     * Creates a builder for a generic sync value. This method should only be used if the type has generic parameters.
     * The downside of this method is that the compiler can't verify the passed in class. Technically any class can be
     * passed on,
     * but issues will arise later down the line.
     *
     * @param type Class of the value to sync. This is used to verify synced value types.
     * @param <T>  type of the value to sync
     * @return builder
     */
    @SuppressWarnings("unchecked")
    public static <T> Builder<T> rawTypeBuilder(Class<?> type) {
        return new Builder<T>((Class<T>) type);
    }

    /**
     * Creates a builder for a generic sync value. This method should only be used if the value is never null.
     * The class is inferred from the value of the value supplier once.
     *
     * @param <T> type of the value to sync
     * @return builder
     */
    public static <T> Builder<T> notNullBuilder() {
        return new Builder<>(null);
    }

    public static class Builder<T> {

        private final Class<T> type;
        private Supplier<T> getter;
        private Consumer<T> setter;
        private IByteBufDeserializer<T> deserializer;
        private IByteBufSerializer<T> serializer;
        private EqualityTest<T> equals;
        private ICopy<T> copy;
        private boolean nullable;

        public Builder(Class<T> type) {
            this.type = type;
        }

        /**
         * Sets the getter for the sync value. Usually this returns a field in a
         * {@link net.minecraft.world.level.block.entity.BlockEntity BlockEntity} or
         * similar. On server side this is called every tick and compared to the sync values cache. If the values are
         * different
         * (determined by {@link #equals(EqualityTest)}), it is synced to the client.
         * <p>
         * <b>This setter is required!</b>
         * </p>
         *
         * @param getter function that returns the current value that may be synced
         * @return this builder
         */
        public Builder<T> getter(Supplier<T> getter) {
            this.getter = getter;
            return this;
        }

        /**
         * Sets the setter for the sync value. Usually this sets a field in a
         * {@link net.minecraft.world.level.block.entity.BlockEntity BlockEntity} or
         * similar. This is called when a value is synced in client or server. In many cases this can be null since the
         * synced value is
         * cached inside the sync handler.
         * <p>
         * <b>This setter is optional!</b>
         * </p>
         *
         * @param setter function that updates the source of the value
         * @return this builder
         */
        public Builder<T> setter(Consumer<T> setter) {
            this.setter = setter;
            return this;
        }

        /**
         * Sets a function which writes the value to a {@link FriendlyByteBuf}. The function is called every time the
         * value is synced to the
         * other side.
         * <p>
         * <b>This setter is required!</b>
         * </p>
         *
         * @param deserializer function that writes the value to a packet buffer
         * @return this builder
         */
        public Builder<T> deserializer(IByteBufDeserializer<T> deserializer) {
            this.deserializer = deserializer;
            return this;
        }

        /**
         * Sets a function which reads the value from a {@link FriendlyByteBuf}. The function is called every time the
         * value is synced to the
         * other side.
         * <p>
         * <b>This setter is required!</b>
         * </p>
         *
         * @param serializer function that reads the value from a packet buffer
         * @return this builder
         */
        public Builder<T> serializer(IByteBufSerializer<T> serializer) {
            this.serializer = serializer;
            return this;
        }

        /**
         * Sets a function which tests two instances of the value for differences. This function is called every tick on
         * server to determine
         * if the value from {@link #getter(Supplier)} needs to be synced. By default, this uses
         * {@link Objects#equals(Object, Object)}.
         * If your value does not implement {@link Object#equals(Object)}, then this setter must be called in order for
         * it to function
         * properly. Otherwise {@link Objects#equals(Object, Object)} always returns false since the instances will
         * always be different,
         * which causes the value to be synced every tick.
         * <p>
         * <b>This setter is optional!</b>
         * </p>
         *
         * @param equals function that determines if two instances of the value are equal
         * @return this builder
         */
        public Builder<T> equals(EqualityTest<T> equals) {
            this.equals = equals;
            return this;
        }

        /**
         * Sets the equals function to {@link Objects#equals(Object, Object)}, which is the default function.
         *
         * @return this builder
         * @see #equals(EqualityTest)
         */
        public Builder<T> equalsDefault() {
            return equals(EqualityTest.defaultTester());
        }

        /**
         * Sets a function which creates a copy of the value. This is called every time the value cache is updated. This
         * is to prevent
         * accidental mutations of the cache value. For example if you called {@link #setValue(Object)}, but keep a
         * reference to the new
         * value and then modify your reference, then the cached value is also updated since it's the same object. If
         * now the
         * {@link #getter(Supplier)} function happens to return exactly that reference, then the
         * {@link #equals(EqualityTest)} function always
         * compares its cache to itself (since it compare the cache to the getter value, which are the same in this
         * case). This causes
         * the value to be never synced from server to client.
         * <p>
         * By default, the {@link #serializer(IByteBufSerializer)} and {@link #deserializer(IByteBufDeserializer)}
         * function are called to
         * create a new instance. If these functions are implemented properly, it is always guaranteed to create a new
         * instance of the
         * value. However, it is still recommended to provide your own proper copy method.
         * <p>
         * If you know, that your values is immutable (can't be modified once created), then you can use
         * {@link #copyImmutable()}.
         * That function won't copy the value, but just return the value.
         * <p>
         * <b>This setter is optional!</b>
         * </p>
         *
         * @param copy function that creates a new exact copy of the value
         * @return this builder
         */
        public Builder<T> copy(ICopy<T> copy) {
            this.copy = copy;
            return this;
        }

        /**
         * Sets a {@link #copy(ICopy)} function which assumes the values are always immutable. This should only be used
         * if the value is
         * always immutable (can't be modified once created). See {@link #copy(ICopy)} for more info.
         *
         * @return this builder
         * @see #copy(ICopy)
         */
        public Builder<T> copyImmutable() {
            return copy(ICopy.immutable());
        }

        /**
         * Sets an adapter. This combines {@link #deserializer(IByteBufDeserializer)},
         * {@link #serializer(IByteBufSerializer)} and
         * {@link #equals(EqualityTest)} into one method.
         *
         * @param adapter byte buf adapter
         * @return this builder
         * @see #deserializer(IByteBufDeserializer)
         * @see #serializer(IByteBufSerializer)
         * @see #equals(EqualityTest)
         */
        public Builder<T> adapter(IByteBufAdapter<T> adapter) {
            return deserializer(adapter)
                    .serializer(adapter)
                    .equals(adapter);
        }

        /**
         * Sets the value to be nullable. This wraps all the used functions into null safe variants. This only for
         * convenience.
         * Manually having to consider nullability inside all the function is cumbersome. This setter is a shortcut.
         * It is opt-in since, it creates a very minor overhead in the serializer and deserializer.
         * <p>
         * <b>This setter is optional!</b>
         * </p>
         *
         * @return this builder
         */
        public Builder<T> nullable() {
            this.nullable = true;
            return this;
        }

        /**
         * Creates the sync value from this builder.
         *
         * @return new sync value handler
         * @throws NullPointerException     if the getter, serializer or deserializer is null
         * @throws IllegalArgumentException if the value type is null and the getter returns null
         */
        public GenericSyncValue<T> build() {
            return new GenericSyncValue<>(type, getter, setter, deserializer, serializer, equals, copy, nullable);
        }
    }
}
