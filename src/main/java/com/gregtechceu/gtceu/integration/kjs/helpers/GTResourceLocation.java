package com.gregtechceu.gtceu.integration.kjs.helpers;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.ResourceLocationException;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.core.RegistryObjectKJS;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.rhino.Wrapper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

/**
 * Exists to indicate that a ResourceLocation would use gtceu: namespace by default when written as plain string. Should
 * only be used as an argument in gt's registry methods
 */
public record GTResourceLocation(ResourceLocation wrapped) {

    public static final Codec<GTResourceLocation> CODEC = GTCEu.GTCEU_ID.xmap(GTResourceLocation::new,
            GTResourceLocation::wrapped);

    @Nullable
    public static GTResourceLocation wrap(@Nullable Object o) {
        if (o == null) return null;
        o = Wrapper.unwrapped(o);

        ResourceLocation id = switch (o) {
            case ResourceLocation resLoc -> resLoc;
            case ResourceKey<?> key -> key.location();
            case Holder<?> holder when holder.getKey() != null -> holder.getKey().location();
            case RegistryObjectKJS<?> key -> key.kjs$getIdLocation();
            default -> {
                var s = o instanceof JsonPrimitive p ? p.getAsString() : o.toString();
                s = GTCEu.appendIdString(s);

                try {
                    yield ResourceLocation.parse(s);
                } catch (ResourceLocationException ex) {
                    throw new KubeRuntimeException("Could not create ID from '%s'!".formatted(s));
                }
            }
        };
        return new GTResourceLocation(id);
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

    private static final ThreadLocal<Boolean> IMPLICIT_KUBE_JS_NAMESPACE = ThreadLocal.withInitial(() -> false);

    @ApiStatus.Internal
    public static void nextKubeResLocNamespaceIsImplicit() {
        IMPLICIT_KUBE_JS_NAMESPACE.set(true);
    }

    @ApiStatus.Internal
    public static void clearCurrentTrackedImplicitValue() {
        IMPLICIT_KUBE_JS_NAMESPACE.remove();
    }

    public static ResourceLocation unwrapMaybeImplicitResourceLocation(ResourceLocation location) {
        try {
            if (IMPLICIT_KUBE_JS_NAMESPACE.get() == true && location.getNamespace().equals(GTValues.MODID_KUBEJS)) {
                return GTCEu.id(location.getPath());
            } else {
                return location;
            }
        } finally {
            IMPLICIT_KUBE_JS_NAMESPACE.remove();
        }
    }
}
