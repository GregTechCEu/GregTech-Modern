package com.gregtechceu.gtceu.api.registry;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author KilaBash
 * @date 2023/2/13
 * @implNote GTRegistry
 */
public abstract class GTRegistry<K, V> implements Iterable<V> {

    public static final Map<ResourceLocation, GTRegistry<?, ?>> REGISTERED = new HashMap<>();

    protected final BiMap<K, V> registry;
    @Getter
    protected final ResourceLocation registryName;
    @Getter
    protected boolean frozen = true;

    public GTRegistry(ResourceLocation registryName) {
        registry = initRegistry();
        this.registryName = registryName;

        REGISTERED.put(registryName, this);
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
        if (frozen) {
            throw new IllegalStateException("Registry is already frozen!");
        }

        if (!checkActiveModContainerIsGregtech()) {
            return;
        }

        this.frozen = true;
    }

    public void unfreeze() {
        if (!frozen) {
            throw new IllegalStateException("Registry is already unfrozen!");
        }

        if (!checkActiveModContainerIsGregtech()) {
            return;
        }

        this.frozen = false;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean checkActiveModContainerIsGregtech() {
        ModContainer container = ModLoadingContext.get().getActiveContainer();
        return container != null && (container.getModId().equals(this.registryName.getNamespace()) ||
                container.getModId().equals(GTCEu.MOD_ID) ||
                container.getModId().equals("minecraft")); // check for minecraft modid in case of datagen or a mishap
    }

    public <T extends V> T register(K key, T value) {
        if (frozen) {
            throw new IllegalStateException("[register] registry %s has been frozen".formatted(registryName));
        }
        if (containKey(key)) {
            throw new IllegalStateException(
                    "[register] registry %s contains key %s already".formatted(registryName, key));
        }
        registry.put(key, value);
        return value;
    }

    @Nullable
    public <T extends V> T replace(K key, T value) {
        if (frozen) {
            throw new IllegalStateException("[replace] registry %s has been frozen".formatted(registryName));
        }
        if (!containKey(key)) {
            GTCEu.LOGGER.warn("[replace] couldn't find key %s in registry %s".formatted(registryName, key));
        }
        registry.put(key, value);
        return value;
    }

    public <T extends V> T registerOrOverride(K key, T value) {
        if (frozen) {
            throw new IllegalStateException("[register] registry %s has been frozen".formatted(registryName));
        }
        registry.put(key, value);
        return value;
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

    // ************************ Built-in Registry ************************//

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
            return Codec.STRING
                    .flatXmap(
                            str -> Optional.ofNullable(this.get(str)).map(DataResult::success)
                                    .orElseGet(() -> DataResult
                                            .error(() -> "Unknown registry key in " + this.registryName + ": " + str)),
                            obj -> Optional.ofNullable(this.getKey(obj)).map(DataResult::success)
                                    .orElseGet(() -> DataResult.error(
                                            () -> "Unknown registry element in " + this.registryName + ": " + obj)));
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
            return ResourceLocation.CODEC
                    .flatXmap(
                            rl -> Optional.ofNullable(this.get(rl)).map(DataResult::success)
                                    .orElseGet(() -> DataResult
                                            .error(() -> "Unknown registry key in " + this.registryName + ": " + rl)),
                            obj -> Optional.ofNullable(this.getKey(obj)).map(DataResult::success)
                                    .orElseGet(() -> DataResult.error(
                                            () -> "Unknown registry element in " + this.registryName + ": " + obj)));
        }
    }
}
