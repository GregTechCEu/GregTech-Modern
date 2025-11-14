package com.gregtechceu.gtceu.api.mui.value.sync;

import com.gregtechceu.gtceu.utils.EqualityTest;
import com.gregtechceu.gtceu.utils.ICopy;
import com.gregtechceu.gtceu.utils.serialization.network.IByteBufAdapter;
import com.gregtechceu.gtceu.utils.serialization.network.IByteBufDeserializer;
import com.gregtechceu.gtceu.utils.serialization.network.IByteBufSerializer;

import net.minecraft.network.FriendlyByteBuf;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GenericMapSyncHandler<K, V> extends ValueSyncHandler<Map<K, V>> {

    private final Supplier<Map<K, V>> getter;
    private final Consumer<Map<K, V>> setter;
    private final IByteBufDeserializer<K> keyDeserializer;
    private final IByteBufDeserializer<V> valueDeserializer;
    private final IByteBufSerializer<K> keySerializer;
    private final IByteBufSerializer<V> valueSerializer;
    private final EqualityTest<V> equals;
    private final ICopy<K> keyCopy;
    private final ICopy<V> valueCopy;
    private final Map<K, V> cache = new Object2ObjectOpenHashMap<>();

    public GenericMapSyncHandler(Supplier<Map<K, V>> getter,
                                 Consumer<Map<K, V>> setter,
                                 IByteBufDeserializer<K> keyDeserializer,
                                 IByteBufDeserializer<V> valueDeserializer,
                                 IByteBufSerializer<K> keySerializer,
                                 IByteBufSerializer<V> valueSerializer,
                                 EqualityTest<V> equals,
                                 ICopy<K> keyCopy,
                                 ICopy<V> valueCopy) {
        this.getter = getter;
        this.setter = setter;
        this.keyDeserializer = keyDeserializer;
        this.valueDeserializer = valueDeserializer;
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
        this.equals = equals != null ? EqualityTest.wrapNullSafe(equals) : Objects::equals;
        this.keyCopy = keyCopy != null ? keyCopy : ICopy.ofSerializer(keySerializer, keyDeserializer);
        this.valueCopy = valueCopy != null ? valueCopy : ICopy.ofSerializer(valueSerializer, valueDeserializer);;
    }

    @Override
    public void setValue(Map<K, V> value, boolean setSource, boolean sync) {
        this.cache.clear();
        for (Map.Entry<K, V> entry : value.entrySet()) {
            this.cache.put(this.keyCopy.createDeepCopy(entry.getKey()),
                    this.valueCopy.createDeepCopy(entry.getValue()));
        }
        if (setSource && this.setter != null) {
            this.setter.accept(value);
        }
        if (sync) {
            sync(0, this::write);
        }
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        Map<K, V> map = this.getter.get();
        if (isFirstSync || didValuesChange(map)) {
            setValue(map, false, false);
            return true;
        }
        return false;
    }

    @Override
    public void notifyUpdate() {
        setValue(this.getter.get(), false, true);
    }

    protected boolean didValuesChange(Map<K, V> value) {
        if (this.cache.size() != value.size()) return true;
        for (Map.Entry<K, V> entry : this.cache.entrySet()) {
            if (!value.containsKey(entry.getKey())) return true;
            V v = value.get(entry.getKey());
            if (!this.equals.areEqual(entry.getValue(), v)) return true;
        }
        return false;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.cache.size());
        for (Map.Entry<K, V> entry : this.cache.entrySet()) {
            this.keySerializer.serialize(buffer, entry.getKey());
            this.valueSerializer.serialize(buffer, entry.getValue());
        }
    }

    @Override
    public void read(FriendlyByteBuf buffer) {
        this.cache.clear();
        int size = buffer.readVarInt();
        for (int i = 0; i < size; i++) {
            this.cache.put(this.keyDeserializer.deserialize(buffer), this.valueDeserializer.deserialize(buffer));
        }
        this.setter.accept(getValue());
    }

    @Override
    public Map<K, V> getValue() {
        return Collections.unmodifiableMap(this.cache);
    }

    public static class Builder<K, V> {

        private Supplier<Map<K, V>> getter;
        private Consumer<Map<K, V>> setter;
        private IByteBufDeserializer<K> keyDeserializer;
        private IByteBufDeserializer<V> valueDeserializer;
        private IByteBufSerializer<K> keySerializer;
        private IByteBufSerializer<V> valueSerializer;
        private EqualityTest<V> equals;
        private ICopy<K> keyCopy;
        private ICopy<V> valueCopy;

        public Builder<K, V> getter(Supplier<Map<K, V>> getter) {
            this.getter = getter;
            return this;
        }

        public Builder<K, V> setter(Consumer<Map<K, V>> setter) {
            this.setter = setter;
            return this;
        }

        public Builder<K, V> keyDeserializer(IByteBufDeserializer<K> keyDeserializer) {
            this.keyDeserializer = keyDeserializer;
            return this;
        }

        public Builder<K, V> valueDeserializer(IByteBufDeserializer<V> valueDeserializer) {
            this.valueDeserializer = valueDeserializer;
            return this;
        }

        public Builder<K, V> keySerializer(IByteBufSerializer<K> keySerializer) {
            this.keySerializer = keySerializer;
            return this;
        }

        public Builder<K, V> valueSerializer(IByteBufSerializer<V> valueSerializer) {
            this.valueSerializer = valueSerializer;
            return this;
        }

        public Builder<K, V> keyAdapter(IByteBufAdapter<K> adapter) {
            return keyDeserializer(adapter).keySerializer(adapter);
        }

        public Builder<K, V> valueAdapter(IByteBufAdapter<V> adapter) {
            return valueDeserializer(adapter).valueSerializer(adapter).equals(adapter);
        }

        public Builder<K, V> equals(EqualityTest<V> equals) {
            this.equals = equals;
            return this;
        }

        public Builder<K, V> keyCopy(ICopy<K> keyCopy) {
            this.keyCopy = keyCopy;
            return this;
        }

        public Builder<K, V> immutableKey() {
            return keyCopy(ICopy.immutable());
        }

        public Builder<K, V> valueCopy(ICopy<V> valueCopy) {
            this.valueCopy = valueCopy;
            return this;
        }

        public Builder<K, V> immutableValue() {
            return valueCopy(ICopy.immutable());
        }

        public GenericMapSyncHandler<K, V> build() {
            if (this.getter == null) throw new NullPointerException("Getter in GenericMapSyncHandler must not be null");
            if (this.keyDeserializer == null)
                throw new NullPointerException("Key deserializer in GenericMapSyncHandler must not be null");
            if (this.valueDeserializer == null)
                throw new NullPointerException("Value deserializer in GenericMapSyncHandler must not be null");
            if (this.keySerializer == null)
                throw new NullPointerException("Key serializer in GenericMapSyncHandler must not be null");
            if (this.valueSerializer == null)
                throw new NullPointerException("Value serializer in GenericMapSyncHandler must not be null");
            return new GenericMapSyncHandler<>(this.getter, this.setter, this.keyDeserializer, this.valueDeserializer,
                    this.keySerializer,
                    this.valueSerializer, this.equals, this.keyCopy, this.valueCopy);
        }
    }
}
