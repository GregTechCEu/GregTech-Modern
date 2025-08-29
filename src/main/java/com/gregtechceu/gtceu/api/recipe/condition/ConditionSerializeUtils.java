package com.gregtechceu.gtceu.api.recipe.condition;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConditionSerializeUtils {

    /**
     * Encode a list of HolderSet<Fluids> to a string encoding
     * 
     * @param fluids the List<HolderSet<Fluids>> to be encoded
     * @return A string, where HolderSets are separated by |'s and elements of the HolderSet are separated by ,
     */
    public static String encodeFluids(List<HolderSet<Fluid>> fluids) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (HolderSet<Fluid> holderSet : fluids) {
            if (!first) {
                sb.append("|");
            }
            sb.append(encodeHolderSet(holderSet));
            first = false;
        }

        return sb.toString();
    }

    /**
     * Encode a HolderSet<Fluids> to a string encoding
     * The encoding is a string, where if it's a tag, it's encoded as #+location,
     * and if it's a list, elements of the HolderSet are separated by ,
     *
     * @param holderSet the HolderSet<Fluids> to be encoded
     * @return the encoded string
     */
    public static String encodeHolderSet(HolderSet<Fluid> holderSet) {
        return holderSet.unwrap().map(
                // Case 1: Tag
                tagKey -> "#" + tagKey.location(),
                // Case 2: Direct list of holders
                holders -> holders.stream()
                        .map(holder -> getStringFromHolder(holder))
                        .collect(Collectors.joining(",")));
    }

    /**
     * Encode a Holder<Fluid> into a String.
     * 
     * @param holder the Holder<Fluid> to be encoded
     * @return a string encoding, as # + location if it's a tagkey, or the location if it's a registry entry.
     */
    public static String getStringFromHolder(Holder<Fluid> holder) {
        // Case 1: If the holder has a registry key, use it
        Optional<ResourceKey<Fluid>> keyOpt = holder.unwrapKey();
        if (keyOpt.isPresent()) {
            return keyOpt.get().location().toString();
        }

        // Case 2: If the holder is tagged, return the first tag with a '#' prefix
        Optional<TagKey<Fluid>> tagOpt = holder.tags().findFirst();
        if (tagOpt.isPresent()) {
            return "#" + tagOpt.get().location().toString();
        }

        throw new RuntimeException("could not deserialize holder: " + holder);
    }

    /**
     * Decode a FluidString into a List<HolderSet<Fluid>>
     * The encoding is a string, where if it's a tag, it's encoded as #+location,
     * and if it's a list, elements of the HolderSet are separated by ,
     * 
     * @param fluidString the string encoding
     * @return The decoded list
     */
    public static List<HolderSet<Fluid>> decodeFluids(String fluidString) {
        List<HolderSet<Fluid>> result = new ArrayList<>();
        for (String token : fluidString.split("\\|")) {
            if (!token.isBlank()) {
                result.add(decodeHolderSet(token));
            }
        }
        return result;
    }

    /**
     * Decode a string into a HolderSet<Fluid>
     * The encoding is a string, where if it's a tag, it's encoded as #+location,
     * and if it's a list, elements of the HolderSet are separated by ,
     * 
     * @param encodedSet the encoded set
     * @return The decoded list
     */
    public static HolderSet<Fluid> decodeHolderSet(String encodedSet) {
        encodedSet = encodedSet.trim();
        if (encodedSet.isEmpty()) {
            return HolderSet.direct(List.of());
        }

        // Case 1: Tag-based holder set
        if (encodedSet.startsWith("#")) {
            ResourceLocation tagId = new ResourceLocation(encodedSet.substring(1));
            TagKey<Fluid> tagKey = TagKey.create(Registries.FLUID, tagId);
            return GTRegistries.builtinRegistry().registry(Registries.FLUID).get().getOrCreateTag(tagKey);
        }

        // Case 2: Direct list of fluids
        String[] parts = encodedSet.split(",");
        List<Holder<Fluid>> holders = new ArrayList<>();
        for (String part : parts) {
            ResourceLocation rl = new ResourceLocation(part.trim());
            Fluid fluid = GTRegistries.builtinRegistry().registry(Registries.FLUID).get().get(rl);
            if (fluid != null && fluid != Fluids.EMPTY) {
                holders.add(BuiltInRegistries.FLUID.wrapAsHolder(fluid));
            } else {
                throw new RuntimeException("Unknown fluid id: " + rl);
            }
        }
        return HolderSet.direct(holders);
    }
}
