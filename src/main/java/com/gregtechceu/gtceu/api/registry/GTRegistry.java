package com.gregtechceu.gtceu.api.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.gregtechceu.gtceu.GTCEu;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author KilaBash
 * @date 2023/2/13
 * @implNote GTRegistry
 */
public abstract class GTRegistry<K, V> implements Iterable<V> {
    protected final BiMap<K, V> registry;
    @Getter
    protected final ResourceLocation registryName;
    @Getter
    protected boolean isFrozen = false;

    public GTRegistry(ResourceLocation registryName) {
        registry = initRegistry();
        this.registryName = registryName;

        if (!registryName.getPath().equals("root")) {
            GTRegistries.REGISTRIES.register(registryName, this);
        }
    }

    protected BiMap<K, V> initRegistry() {
        return HashBiMap.create();
    }

    public boolean containKey(K key) {
        return registry.containsKey(key);
    }

    public boolean containValue(V value) {
        return registry.containsValue(value);
    }

    public void freeze() {
        isFrozen = true;
    }

    public void register(K key, V value) {
        if (isFrozen) {
            throw new IllegalStateException("[register] registry %s has been frozen");
        }
        if (containKey(key)) {
            throw new IllegalStateException("[register] registry %s contains key %s already".formatted(registryName, key));
        }
        registry.put(key, value);
    }

    @Nullable
    public V replace(K key, V value) {
        if (isFrozen) {
            throw new IllegalStateException("[replace] registry %s has been frozen");
        }
        if (!containKey(key)) {
            GTCEu.LOGGER.warn("[replace] couldn't find key %s in registry %s".formatted(registryName, key));
        }
        return registry.put(key, value);
    }

    public V registerOrOverride(K key, V value) {
        if (isFrozen) {
            throw new IllegalStateException("[register] registry %s has been frozen");
        }
        return registry.put(key, value);
    }

    @NotNull
    @Override
    public Iterator<V> iterator() {
        return registry.values().iterator();
    }

    public Set<V> values() {
        return registry.values();
    }

    public Set<K> keys() {
        return registry.keySet();
    }

    public Set<Map.Entry<K, V>> entries() {
        return registry.entrySet();
    }

    public Map<K, V> registry() {
        return registry;
    }

    @Nullable
    public V get(K key) {
        return registry.get(key);
    }

    public V getOrDefault(K key, V defaultValue) {
        return registry.getOrDefault(key, defaultValue);
    }

    public K getKey(V value) {
        return registry.inverse().get(value);
    }

    public K getOrDefaultKey(V key, K defaultKey) {
        return registry.inverse().getOrDefault(key, defaultKey);
    }

    public abstract void writeBuf(V value, FriendlyByteBuf buf);

    @Nullable
    public abstract V readBuf(FriendlyByteBuf buf);

    public abstract Tag saveToNBT(V value);

    @Nullable
    public abstract V loadFromNBT(Tag tag);

    public boolean remove(K name) {
        return registry.remove(name) != null;
    }

    public abstract Codec<V> codec();

    //************************ Built-in Registry ************************//

    public static class String<V> extends GTRegistry<java.lang.String, V> {

        public String(ResourceLocation registryName) {
            super(registryName);
        }

        @Override
        public void writeBuf(V value, FriendlyByteBuf buf) {
            buf.writeBoolean(containValue(value));
            if (containValue(value)) {
                buf.writeUtf(getKey(value));
            }
        }

        @Override
        public V readBuf(FriendlyByteBuf buf) {
            if (buf.readBoolean()) {
                return get(buf.readUtf());
            }
            return null;
        }

        @Override
        public Tag saveToNBT(V value) {
            if (containValue(value)) {
                return StringTag.valueOf(getKey(value));
            }
            return new CompoundTag();
        }

        @Override
        public V loadFromNBT(Tag tag) {
            return get(tag.getAsString());
        }

        @Override
        public Codec<V> codec() {
            return Codec.STRING.flatXmap(str -> Optional.ofNullable(this.get(str)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown registry key in " + this.registryName + ": " + str)), obj -> Optional.ofNullable(this.getKey(obj)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown registry element in " + this.registryName + ": " + obj)));
        }
    }

    public static class RL<V> extends GTRegistry<ResourceLocation, V> {

        public RL(ResourceLocation registryName) {
            super(registryName);
        }

        @Override
        public void writeBuf(V value, FriendlyByteBuf buf) {
            buf.writeBoolean(containValue(value));
            if (containValue(value)) {
                buf.writeUtf(getKey(value).toString());
            }
        }

        @Override
        public V readBuf(FriendlyByteBuf buf) {
            if (buf.readBoolean()) {
                return get(new ResourceLocation(buf.readUtf()));
            }
            return null;
        }

        @Override
        public Tag saveToNBT(V value) {
            if (containValue(value)) {
                return StringTag.valueOf(getKey(value).toString());
            }
            return new CompoundTag();
        }

        @Override
        public V loadFromNBT(Tag tag) {
            return get(new ResourceLocation(tag.getAsString()));
        }

        @Override
        public Codec<V> codec() {
            return ResourceLocation.CODEC.flatXmap(rl -> Optional.ofNullable(this.get(rl)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown registry key in " + this.registryName + ": " + rl)), obj -> Optional.ofNullable(this.getKey(obj)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown registry element in " + this.registryName + ": " + obj)));
        }
    }
}
