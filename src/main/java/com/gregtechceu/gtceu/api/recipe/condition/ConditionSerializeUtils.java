package com.gregtechceu.gtceu.api.recipe.condition;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConditionSerializeUtils {

    /**
     * Encode a List<HolderSet<T>> to a string encoding.
     * HolderSets are separated by '|', and elements of a direct HolderSet are separated by ','.
     *
     * @param items The list of HolderSet<T> to be encoded.
     * @return A string encoding.
     */
    public static <T> String encodeHolderSets(List<HolderSet<T>> items) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (HolderSet<T> holderSet : items) {
            if (!first) {
                sb.append("|");
            }
            sb.append(encodeHolderSet(holderSet));
            first = false;
        }

        return sb.toString();
    }

    /**
     * Encode a single HolderSet<T> to a string.
     * If it's a tag, it's encoded as #+location.
     * If it's a direct list, elements are separated by ','.
     *
     * @param holderSet The HolderSet<T> to be encoded.
     * @return The encoded string.
     */
    public static <T> String encodeHolderSet(HolderSet<T> holderSet) {
        return holderSet.unwrap().map(
                // Case 1: Tag
                tagKey -> "#" + tagKey.location(),
                // Case 2: Direct list of holders
                holders -> holders.stream()
                        .map(holder -> getStringFromHolder(holder))
                        .collect(Collectors.joining(",")));
    }

    /**
     * Encode a Holder<T> into a String.
     *
     * @param holder The Holder<T> to be encoded.
     * @return A string encoding (location or #+tag_location).
     * @throws RuntimeException if the holder cannot be serialized.
     */
    public static <T> String getStringFromHolder(Holder<T> holder) {
        Optional<ResourceKey<T>> keyOpt = holder.unwrapKey();
        if (keyOpt.isPresent()) {
            return keyOpt.get().location().toString();
        }

        Optional<TagKey<T>> tagOpt = holder.tags().findFirst();
        if (tagOpt.isPresent()) {
            return "#" + tagOpt.get().location();
        }
        throw new RuntimeException("Could not serialize holder: " + holder);
    }

    /**
     * Decode a string into a List<HolderSet<T>>.
     *
     * @param encodedString The string encoding.
     * @param registryKey   The ResourceKey of the registry.
     * @return The decoded list of HolderSet<T>.
     */
    public static <T> List<HolderSet<T>> decodeHolderSets(String encodedString,
                                                          ResourceKey<? extends Registry<T>> registryKey) {
        List<HolderSet<T>> result = new ArrayList<>();
        for (String token : encodedString.split("\\|")) {
            if (!token.isBlank()) {
                result.add(decodeSingleHolderSet(token, registryKey));
            }
        }
        return result;
    }

    /**
     * Decode a string into a HolderSet<T>.
     *
     * @param encodedSet  The encoded set string.
     * @param registryKey The ResourceKey of the registry.
     * @return The decoded HolderSet<T>.
     * @throws RuntimeException if an unknown ID is encountered.
     */
    public static <T> HolderSet<T> decodeSingleHolderSet(String encodedSet,
                                                         ResourceKey<? extends Registry<T>> registryKey) {
        encodedSet = encodedSet.trim();
        if (encodedSet.isEmpty()) {
            return HolderSet.direct(List.of());
        }

        Registry<T> registry = GTRegistries.builtinRegistry().registry(registryKey).get(); // Use the helper

        // Case 1: Tag-based holder set
        if (encodedSet.startsWith("#")) {
            ResourceLocation tagId = new ResourceLocation(encodedSet.substring(1));
            TagKey<T> tagKey = TagKey.create(registryKey, tagId);
            return registry.getOrCreateTag(tagKey);
        }

        // Case 2: Direct list of items
        String[] parts = encodedSet.split(",");
        List<Holder<T>> holders = new ArrayList<>();
        for (String part : parts) {
            ResourceLocation rl = new ResourceLocation(part.trim());
            T item = registry.get(rl);
            if (item != null && item != getDefaultEmptyValue(registryKey)) { // Use a generic default empty check
                holders.add(registry.wrapAsHolder(item));
            } else {
                throw new RuntimeException("Unknown ID for registry " + registryKey.location() + ": " + rl);
            }
        }
        return HolderSet.direct(holders);
    }

    // Helper to get the default "empty" value for a registry type
    private static <T> T getDefaultEmptyValue(ResourceKey<? extends Registry<T>> registryKey) {
        // Compare ResourceLocation directly instead of ResourceKey objects
        if (registryKey.location().equals(Registries.FLUID.location())) {
            return (T) Fluids.EMPTY;
        } else if (registryKey.location().equals(Registries.BLOCK.location())) {
            return (T) Blocks.AIR;
        }
        throw new IllegalArgumentException(
                "Unsupported registry type for default value lookup: " + registryKey.location());
    }
}
