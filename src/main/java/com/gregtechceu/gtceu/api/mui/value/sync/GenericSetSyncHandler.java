package com.gregtechceu.gtceu.api.mui.value.sync;

import com.gregtechceu.gtceu.utils.ICopy;
import com.gregtechceu.gtceu.utils.serialization.network.IByteBufDeserializer;
import com.gregtechceu.gtceu.utils.serialization.network.IByteBufSerializer;

import net.minecraft.network.FriendlyByteBuf;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GenericSetSyncHandler<T> extends GenericCollectionSyncHandler<T, Set<T>> {

    private final Set<T> cache = new ObjectOpenHashSet<T>();

    public GenericSetSyncHandler(@NotNull Supplier<Set<T>> getter, @Nullable Consumer<Set<T>> setter,
                                 @NotNull IByteBufDeserializer<T> deserializer,
                                 @NotNull IByteBufSerializer<T> serializer,
                                 @Nullable ICopy<T> copy) {
        super(getter, setter, deserializer, serializer, null, copy);
        setCache(getter.get());
    }

    @Override
    protected void setCache(Set<T> value) {
        this.cache.clear();
        for (T item : value) {
            this.cache.add(copyValue(item));
        }
    }

    @Override
    protected boolean didValuesChange(Set<T> newValues) {
        if (this.cache.size() != newValues.size()) return true;
        return cache.containsAll(newValues);
    }

    @Override
    public Set<T> getValue() {
        return Collections.unmodifiableSet(this.cache);
    }

    @Override
    public void read(FriendlyByteBuf buffer) {
        this.cache.clear();
        int size = buffer.readVarInt();
        for (int i = 0; i < size; i++) {
            this.cache.add(deserializeValue(buffer));
        }
        onSetCache(getValue(), true, false);
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> extends GenericCollectionSyncHandler.Builder<T, Set<T>, Builder<T>> {

        public GenericSetSyncHandler<T> build() {
            if (this.getter == null) throw new NullPointerException("Getter in GenericSetSyncHandler must not be null");
            if (this.deserializer == null)
                throw new NullPointerException("Deserializer in GenericSetSyncHandler must not be null");
            if (this.serializer == null)
                throw new NullPointerException("Serializer in GenericSetSyncHandler must not be null");
            return new GenericSetSyncHandler<>(this.getter, this.setter, this.deserializer, this.serializer, this.copy);
        }
    }
}
