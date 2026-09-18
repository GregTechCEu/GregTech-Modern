package brachy.modularui.value.sync;

import brachy.modularui.utils.EqualityTest;
import brachy.modularui.utils.ICopy;

import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class GenericListSyncHandler<B extends ByteBuf, T> extends GenericCollectionSyncHandler<B, T, List<T>, GenericListSyncHandler<B, T>> {

    private final List<T> cache = new ObjectArrayList<>();

    public GenericListSyncHandler(@NotNull Supplier<List<T>> getter, @Nullable Consumer<List<T>> setter,
                                  @NotNull StreamDecoder<B, T> deserializer,
                                  @NotNull StreamEncoder<B, T> serializer,
                                  @Nullable EqualityTest<T> equals, @Nullable ICopy<T> copy) {
        super(getter, setter, deserializer, serializer, equals, copy);
    }

    @Override
    protected void setCache(List<T> value) {
        this.cache.clear();
        for (T item : value) {
            this.cache.add(copyValue(item));
        }
    }

    @Override
    protected boolean didValuesChange(List<T> newValues) {
        if (this.cache.size() != newValues.size()) return true;
        for (int i = 0; i < this.cache.size(); i++) {
            if (!areValuesEqual(this.cache.get(i), newValues.get(i))) return true;
        }
        return false;
    }

    @Override
    public List<T> getValue() {
        return Collections.unmodifiableList(this.cache);
    }

    @Override
    public void read(B buffer) {
        this.cache.clear();
        int size = VarInt.read(buffer);
        for (int i = 0; i < size; i++) {
            this.cache.add(deserializeValue(buffer));
        }
        onSetCache(true, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<List<T>> getValueType() {
        return (Class<List<T>>) (Object) List.class;
    }

    /**
     * Allows safe modification of the cached value. Normally modifying the cached value can cause the value to never be synced.
     * This method forces a sync after the modification.
     *
     * @param consumer function that operates on the current cached value
     */
    public void modifyValue(Consumer<List<T>> consumer) {
        modifyValue(true, true, consumer);
    }

    /**
     * Allows safe modification of the cached value. Normally modifying the cached value can cause the value to never be synced.
     * This method can automatically sync the cache after the modification. Be careful with potential issues when the sync arg is false.
     *
     * @param consumer function that operates on the current cached value
     */
    public void modifyValue(boolean setSource, boolean sync, Consumer<List<T>> consumer) {
        consumer.accept(this.cache);
        onSetCache(setSource, sync);
    }

    public static <B extends ByteBuf, T> Builder<B, T> builder() {
        return new Builder<>();
    }

    public static class Builder<B extends ByteBuf, T> extends GenericCollectionSyncHandler.Builder<B, T, List<T>, Builder<B, T>> {

        public Builder<B, T> getterArray(Supplier<T[]> getter) {
            getter(() -> Arrays.asList(getter.get()));
            return this;
        }

        public Builder<B, T> setterArray(Consumer<T[]> setter, IntFunction<T[]> arrayFactory) {
            setter(c -> setter.accept(c.toArray(arrayFactory.apply(c.size()))));
            return this;
        }

        public GenericListSyncHandler<B, T> build() {
            if (this.getter == null)
                throw new NullPointerException("Getter in GenericListSyncHandler must not be null");
            if (this.deserializer == null)
                throw new NullPointerException("Deserializer in GenericListSyncHandler must not be null");
            if (this.serializer == null)
                throw new NullPointerException("Serializer in GenericListSyncHandler must not be null");
            return new GenericListSyncHandler<>(this.getter, this.setter, this.deserializer, this.serializer,
                    this.equals, this.copy);
        }
    }
}
