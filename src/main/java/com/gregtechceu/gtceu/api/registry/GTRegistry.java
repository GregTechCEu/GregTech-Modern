package com.gregtechceu.gtceu.api.registry;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public class GTRegistry<V> implements Iterable<V> {

    public static final Map<ResourceLocation, GTRegistry<?>> REGISTERED = new HashMap<>();

    protected final Map<ResourceLocation, V> keyToValue;
    protected final Map<V, ResourceLocation> valueToKey;
    @Getter
    protected final ResourceLocation registryName;
    @Getter
    protected boolean frozen = true;

    public GTRegistry(ResourceLocation registryName) {
        this.keyToValue = new HashMap<>();
        this.valueToKey = new HashMap<>();
        this.registryName = registryName;

        REGISTERED.put(registryName, this);
    }

    public boolean containsKey(ResourceLocation key) {
        return keyToValue.containsKey(key);
    }

    /**
     * @deprecated use {@link #containsKey(ResourceLocation)} (Object)} (Object)}
     */
    @Deprecated(since = "8.0.0")
    public boolean containKey(ResourceLocation key) {
        return containsKey(key);
    }

    /**
     * @deprecated use {@link #containsValue(Object)} (Object)}
     */
    @Deprecated(since = "8.0.0")
    public boolean containValue(V value) {
        return containsValue(value);
    }

    public boolean containsValue(V value) {
        return keyToValue.containsValue(value);
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

    public <T extends V> T register(ResourceLocation key, T value) {
        if (keyToValue.containsKey(key)) {
            throw new IllegalStateException(
                    "[register] registry %s contains key %s already".formatted(registryName, key));
        }

        return registerOrOverride(key, value);
    }

    public void remap(ResourceLocation oldKey, ResourceLocation newKey) {
        if (frozen) {
            throw new IllegalStateException("[register] registry %s has been frozen".formatted(registryName));
        }

        if (keyToValue.containsKey(oldKey)) {
            GTCEu.LOGGER.warn("[remap] cannot remap existing key {} in registry {}", oldKey, registryName);
            return;
        }
        if (!keyToValue.containsKey(newKey)) {
            GTCEu.LOGGER.warn("[remap] couldn't find value for key {} in registry {}", newKey, registryName);
            return;
        }
        V newValue = keyToValue.get(newKey);
        keyToValue.put(oldKey, newValue);
    }

    @Nullable
    public <T extends V> T replace(ResourceLocation key, T value) {
        if (!containsKey(key)) {
            GTCEu.LOGGER.warn("[replace] couldn't find key {} in registry {}", registryName, key);
        }

        return registerOrOverride(key, value);
    }

    public <T extends V> T registerOrOverride(ResourceLocation key, T value) {
        if (frozen) {
            throw new IllegalStateException("[register] registry %s has been frozen".formatted(registryName));
        }
        keyToValue.put(key, value);
        valueToKey.put(value, key);

        return value;
    }

    @NotNull
    @Override
    public @UnmodifiableView Iterator<V> iterator() {
        return registry().values().iterator();
    }

    public @UnmodifiableView Set<V> values() {
        return Collections.unmodifiableMap(valueToKey).keySet();
    }

    public @UnmodifiableView Set<ResourceLocation> keys() {
        return registry().keySet();
    }

    public @UnmodifiableView Set<Map.Entry<ResourceLocation, V>> entries() {
        return registry().entrySet();
    }

    public @UnmodifiableView Map<ResourceLocation, V> registry() {
        return Collections.unmodifiableMap(keyToValue);
    }

    public void clear() {
        if (frozen) {
            throw new IllegalArgumentException("Registry is frozen!");
        }
        keyToValue.clear();
        valueToKey.clear();
    }

    @Nullable
    public V get(ResourceLocation key) {
        return keyToValue.get(key);
    }

    public V getOrDefault(ResourceLocation key, V defaultValue) {
        return keyToValue.getOrDefault(key, defaultValue);
    }

    public ResourceLocation getKey(V value) {
        return valueToKey.get(value);
    }

    public ResourceLocation getOrDefaultKey(V value, ResourceLocation defaultKey) {
        return valueToKey.getOrDefault(value, defaultKey);
    }

    public void writeBuf(V value, FriendlyByteBuf buf) {
        buf.writeBoolean(containsValue(value));
        if (containsValue(value)) {
            buf.writeUtf(getKey(value).toString());
        }
    }

    public V readBuf(FriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            return get(new ResourceLocation(buf.readUtf()));
        }
        return null;
    }

    public Tag saveToNBT(V value) {
        if (containsValue(value)) {
            return StringTag.valueOf(getKey(value).toString());
        }
        return new CompoundTag();
    }

    public V loadFromNBT(Tag tag) {
        return get(new ResourceLocation(tag.getAsString()));
    }

    public boolean remove(ResourceLocation name) {
        var value = keyToValue.remove(name);
        if (value != null) {
            valueToKey.remove(value);
            return true;
        }
        return false;
    }

    public Codec<V> codec() {
        return ResourceLocation.CODEC.flatXmap(
                key -> Optional.ofNullable(this.get(key)).map(DataResult::success)
                        .orElseGet(() -> DataResult.error(
                                () -> "Unknown registry key in %s: %s".formatted(this.registryName, key))),
                val -> Optional.ofNullable(this.getKey(val)).map(DataResult::success)
                        .orElseGet(() -> DataResult.error(
                                () -> "Unknown registry value in %s: %s".formatted(this.registryName, val))));
    }
}
