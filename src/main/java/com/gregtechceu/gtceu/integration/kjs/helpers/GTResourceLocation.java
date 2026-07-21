package com.gregtechceu.gtceu.integration.kjs.helpers;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.ResourceLocationException;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

/**
 * Exists to indicate that a ResourceLocation would use gtceu: namespace by default when written as plain string. Should
 * only be used as an argument in gt's registry methods
 */
public record GTResourceLocation(ResourceLocation wrapped) {

    public static final Codec<GTResourceLocation> CODEC = GTCEu.GTCEU_ID.xmap(GTResourceLocation::new,
            GTResourceLocation::wrapped);

    public static @Nullable GTResourceLocation wrap(@Nullable Object o) {
        ResourceLocation inner = null;
        if (o instanceof ResourceLocation id) {
            inner = id;
        } else if (o instanceof ResourceKey<?> key) {
            inner = key.location();
        } else if (o instanceof Holder<?> holder) {
            var key = holder.unwrapKey();
            if (key.isPresent()) inner = key.get().location();
        } else if (o != null) {
            var s = o instanceof JsonPrimitive p ? p.getAsString() : o.toString();
            s = GTCEu.appendIdString(s);

            try {
                return new GTResourceLocation(ResourceLocation.tryParse(s));
            } catch (ResourceLocationException ex) {
                throw new ResourceLocationException("Could not create ID from '%s'!".formatted(s));
            }
        }
        if (inner == null) return null;
        return new GTResourceLocation(inner);
    }

    @Override
    public String toString() {
        return wrapped.toString();
    }

    public GTResourceLocation withPath(String path) {
        return new GTResourceLocation(wrapped.withPath(path));
    }

    public GTResourceLocation withPath(UnaryOperator<String> path) {
        return new GTResourceLocation(wrapped.withPath(path));
    }

    public GTResourceLocation withPrefix(String pathPrefix) {
        return new GTResourceLocation(wrapped.withPrefix(pathPrefix));
    }

    public GTResourceLocation withSuffix(String pathSuffix) {
        return new GTResourceLocation(wrapped.withSuffix(pathSuffix));
    }
}
