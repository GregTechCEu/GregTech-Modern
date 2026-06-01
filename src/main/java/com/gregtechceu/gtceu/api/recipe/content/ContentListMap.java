package com.gregtechceu.gtceu.api.recipe.content;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.codec.DispatchedMapCodec;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.network.FriendlyByteBuf;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ContentListMap{

    public static final Codec<ContentListMap> CODEC = new DispatchedMapCodec<RecipeCapability<?>, List<?>>(RecipeCapability.DIRECT_CODEC,
            ContentListMap::contentListCodec)
            .xmap(ContentListMap::new, ContentListMap::asMap);

    private final Map<RecipeCapability<?>, List<?>> contentsMap = new Reference2ObjectArrayMap<>();

    public ContentListMap() {}

    private ContentListMap(Map<RecipeCapability<?>, List<?>> contentsMap) {
        this.contentsMap.putAll(contentsMap);
    }

    public <T> List<T> get(RecipeCapability<T> capability) {
        return (List<T>) contentsMap.get(capability);
    }

    public void put(RecipeCapability<?> capability, List<?> contents) {
        contentsMap.put(capability, contents);
    }

    public void putAll(ContentListMap other) {
        contentsMap.putAll(other.contentsMap);
    }

    public <T> List<T> computeIfAbsent(RecipeCapability<T> capability, Function<RecipeCapability<T>, List<T>> function) {
        return (List<T>) contentsMap.computeIfAbsent(capability, cap -> function.apply((RecipeCapability<T>) cap));
    }

    public void remove(RecipeCapability<?> capability) {
        contentsMap.remove(capability);
    }

    public void clear() {
        contentsMap.clear();
    }

    public boolean containsKey(RecipeCapability<?> capability) {
        return contentsMap.containsKey(capability);
    }

    public Set<RecipeCapability<?>> keySet() {
        return contentsMap.keySet();
    }

    public Set<Map.Entry<RecipeCapability<?>, List<?>>> entrySet() {
        return contentsMap.entrySet();
    }

    public <T> List<T> getOrDefault(RecipeCapability<T> capability, List<T> fallback) {
        return (List<T>) contentsMap.getOrDefault(capability, fallback);
    }

    public void forEach(BiConsumer<RecipeCapability<?>, List<?>> consumer) {
        contentsMap.forEach(consumer);
    }

    public <T> void add(RecipeCapability<T> capability, T content) {
        ((List<T>) contentsMap.computeIfAbsent(capability, c -> new ArrayList<T>()))
                .add(content);
    }

    public <T> T getFirst(RecipeCapability<T> capability) {
        List<T> list = (List<T>) contentsMap.get(capability);
        if(list != null) {
            return list.get(0);
        }
        else {
            return null;
        }
    }

    public boolean isEmpty() {
        return contentsMap.isEmpty();
    }

    public int size() {
        return contentsMap.size();
    }

    public Iterable<Map.Entry<RecipeCapability<?>, List<?>>> entries() {
        return contentsMap.entrySet();
    }

    public Map<RecipeCapability<?>, List<?>> asMap() {
        return contentsMap;
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeVarInt(contentsMap.size());
        for (var entry : contentsMap.entrySet()) {
            buf.writeUtf(GTRegistries.RECIPE_CAPABILITIES.getKey(entry.getKey()));
            buf.writeCollection(entry.getValue(), (buffer, content) -> writeContent(buffer, entry.getKey(), content));
        }
    }

    public static ContentListMap fromNetwork(FriendlyByteBuf buf) {
        ContentListMap map = new ContentListMap();
        int size = buf.readVarInt();
        for (int i = 0; i < size; i++) {
            String name = buf.readUtf();
            RecipeCapability<?> capability = GTRegistries.RECIPE_CAPABILITIES.get(name);
            if (capability == null) {
                throw new IllegalArgumentException("Unknown recipe capability: " + name);
            }
            List<?> contents = buf.readList(buffer -> readContent(buffer, capability));
            map.contentsMap.put(capability, contents);
        }
        return map;
    }

    private static Codec<? extends List<?>> contentListCodec(RecipeCapability<?> capability) {
        return RecipeCapability.contentCodec(capability);
    }

    private static void writeContent(FriendlyByteBuf buf, RecipeCapability<?> capability, Object content) {
        ((RecipeCapability) capability).toNetwork(content, buf);
    }

    private static Object readContent(FriendlyByteBuf buf, RecipeCapability<?> capability) {
        return ((RecipeCapability) capability).fromNetwork(buf);
    }

    public ContentListMap copy() {
        Map<RecipeCapability<?>, List<?>> newMap = new Reference2ObjectArrayMap<>();
        contentsMap.forEach((k ,v) -> newMap.put(k, new ArrayList<>(v)));
        return new ContentListMap(newMap);
    }

    public ContentListMap copyWithMultiplier(int multiplier) {
        Map<RecipeCapability<?>, List<?>> newMap = new Reference2ObjectArrayMap<>();
        contentsMap.forEach((cap, list) -> {
            var newList = new ArrayList<>();
            for(var content: list) {
                newList.add(cap.copyWithMultiplier(content, multiplier));
            }
            newMap.put(cap, newList);
        });
        return new ContentListMap(newMap);
    }
}
