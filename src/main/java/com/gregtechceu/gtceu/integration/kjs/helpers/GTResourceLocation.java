package com.gregtechceu.gtceu.integration.kjs.helpers;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.core.IResourceLocationExtensions;

import net.minecraft.IdentifierException;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.core.RegistryObjectKJS;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.util.KubeResourceLocation;
import dev.latvian.mods.rhino.Wrapper;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

/**
 * Exists to indicate that a Identifier would use gtceu: namespace by default when written as plain string. Should
 * only be used as an argument in gt's registry methods
 */
public record GTResourceLocation(Identifier wrapped) {

    public static final Codec<GTResourceLocation> CODEC = GTCEu.GTCEU_ID.xmap(GTResourceLocation::new,
            GTResourceLocation::wrapped);

    public GTResourceLocation(String id) {
        this(parseGtceu(id));
    }

    @Nullable
    public static GTResourceLocation wrap(@Nullable Object o) {
        Identifier id = parse(o, GTCEu.MOD_ID);
        return id == null ? null : new GTResourceLocation(id);
    }

    @Nullable
    public static Identifier parseMinecraft(@Nullable Object o) {
        return parse(o, "minecraft");
    }

    @Nullable
    private static Identifier parse(@Nullable Object o, String defaultNamespace) {
        if (o == null) return null;
        o = Wrapper.unwrapped(o);

        return switch (o) {
            case Identifier resLoc -> resLoc;
            case ResourceLocation resLoc -> resLoc.toIdentifier();
            case GTResourceLocation gtId -> gtId.wrapped();
            case KubeResourceLocation kubeId -> parse(kubeId.toString(), defaultNamespace);
            case ResourceKey<?> key -> key.identifier();
            case Holder<?> holder when holder.getKey() != null -> holder.getKey().identifier();
            case RegistryObjectKJS<?> key -> Identifier.parse(key.kjs$getId());
            default -> {
                var s = o instanceof JsonPrimitive p ? p.getAsString() : o.toString();
                yield parse(s, defaultNamespace);
            }
        };
    }

    public static Identifier parseGtceu(String id) {
        return parse(id, GTCEu.MOD_ID);
    }

    public static Identifier parseMinecraft(String id) {
        return parse(id, "minecraft");
    }

    private static Identifier parse(String id, String defaultNamespace) {
        String fullId;
        int separator = id.indexOf(':');
        if (separator > 0) {
            fullId = id;
        } else if (separator == 0) {
            fullId = defaultNamespace + id;
        } else {
            fullId = defaultNamespace + ":" + id;
        }

        try {
            return Identifier.parse(fullId);
        } catch (IdentifierException ex) {
            throw new KubeRuntimeException("Could not create ID from '%s'!".formatted(fullId));
        }
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

    public static ResourceLocation implicitAsGtceu(Identifier loc) {
        return ResourceLocation.fromIdentifier(((IResourceLocationExtensions) (Object) loc).gtm$asNonImplicit());
    }

    public static ResourceLocation implicitAsGtceu(ResourceLocation loc) {
        return implicitAsGtceu(loc.toIdentifier());
    }

    public static Identifier toIdentifier(Identifier loc) {
        return loc;
    }

    public static Identifier toIdentifier(ResourceLocation loc) {
        return loc.toIdentifier();
    }

    public static ResourceLocation toResourceLocation(Identifier loc) {
        return ResourceLocation.fromIdentifier(loc);
    }

    public static ResourceLocation toResourceLocation(ResourceLocation loc) {
        return loc;
    }

    public ResourceLocation resourceLocation() {
        return toResourceLocation(wrapped);
    }
}
