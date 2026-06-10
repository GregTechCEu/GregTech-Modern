package com.gregtechceu.gtceu.api.recipe.content;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.codec.DispatchedMapCodec;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.minecraft.network.FriendlyByteBuf;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ContentListMap{

    public static final Codec<ContentListMap> CODEC = new DispatchedMapCodec<RecipeCapability<?>, List<?>>(RecipeCapability.DIRECT_CODEC,
            ContentListMap::contentListCodec)
            .xmap(ContentListMap::new, ContentListMap::asMap);

    private final Map<RecipeCapability<?>, List<?>> contentsMap;

    public ContentListMap() {
        contentsMap = new Reference2ObjectArrayMap<>();
    }

    private ContentListMap(Map<RecipeCapability<?>, List<?>> contentsMap) {
        this.contentsMap = contentsMap;
    }

    public <T> List<T> get(RecipeCapability<T> capability) {
        return (List<T>) contentsMap.get(capability);
    }

    public <T> void put(RecipeCapability<T> capability, List<T> contents) {
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

    public int sizeOf(RecipeCapability<?> capability) {
        List<?> contents = contentsMap.get(capability);
        return contents == null ? 0 : contents.size();
    }

    public ObjectIterator<Reference2ObjectMap.Entry<RecipeCapability<?>, List<?>>> fastIterator() {
        if(contentsMap instanceof Reference2ObjectArrayMap<RecipeCapability<?>, List<?>> r) {
            return r.reference2ObjectEntrySet().fastIterator();
        }
        throw new RuntimeException();
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

    public ContentListMap copyAndAppend(ContentListMap other) {
        ContentListMap copy = copy();
        copy.appendAll(other);
        return copy;
    }

    public void appendAll(ContentListMap other) {
        other.forEachEntry(new EntryConsumer() {
            @Override
            public <T> void accept(RecipeCapability<T> capability, List<T> contents) {
                List<T> list = get(capability);
                if (list == null) {
                    put(capability, new ArrayList<>(contents));
                } else {
                    list.addAll(contents);
                }
            }
        });
    }

    public ContentListMap copyWithMultiplier(int multiplier) {
        Map<RecipeCapability<?>, List<?>> newMap = new Reference2ObjectArrayMap<>();
        forEachEntry(new EntryConsumer() {
            @Override
            public <T> void accept(RecipeCapability<T> cap, List<T> list) {
                var newList = new ArrayList<>();
                for(var content: list) {
                    newList.add(cap.copyWithMultiplier(content, multiplier));
                }
                newMap.put(cap, newList);
            }
        });
        return new ContentListMap(newMap);
    }

    public void multiply(int multiplier) {
        replaceContents(multiplier);
    }

    public void multiply(double multiplier) {
        replaceContents(multiplier);
    }

    private <N extends Number> void replaceContents(N multiplier) {
        forEachEntry(new EntryConsumer() {
            @Override
            public <T> void accept(RecipeCapability<T> cap, List<T> list) {
                list.replaceAll(content -> cap.copyWithMultiplier(content, multiplier.intValue()));
            }
        });
    }

    public void forEachEntry(EntryConsumer consumer) {
        contentsMap.forEach((capability, contents) ->
                acceptCaptured(consumer, capability, contents)
        );
    }

    private static <T> void acceptCaptured(
            EntryConsumer consumer,
            RecipeCapability<?> capability,
            List<?> contents
    ) {
        consumer.accept(
                (RecipeCapability<T>) capability,
                (List<T>) contents
        );
    }

    public interface EntryConsumer {
        <T> void accept(RecipeCapability<T> capability, List<T> contents);
    }

    public interface TypedEntry {
        <T> void accept(EntryConsumer consumer);
    }

    public Iterator<TypedEntry> iterator() {
        Iterator<Map.Entry<RecipeCapability<?>, List<?>>> it =
                contentsMap.entrySet().iterator();

        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public TypedEntry next() {
                Map.Entry<RecipeCapability<?>, List<?>> entry = it.next();

                return new TypedEntry() {
                    @Override
                    public void accept(EntryConsumer consumer) {
                        acceptCaptured(consumer, entry.getKey(), entry.getValue());
                    }
                };
            }

            @Override
            public void remove() {
                it.remove();
            }
        };
    }
}
