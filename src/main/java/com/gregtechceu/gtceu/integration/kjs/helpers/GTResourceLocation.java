package com.gregtechceu.gtceu.integration.kjs.helpers;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.core.mixins.kubejs.ContextAccessor;
import com.gregtechceu.gtceu.core.mixins.kubejs.InterpreterCallFrameAccessor;
import com.gregtechceu.gtceu.core.mixins.kubejs.RegistryKubeEventAccessor;

import net.minecraft.ResourceLocationException;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.core.RegistryObjectKJS;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.registry.ServerRegistryKubeEvent;
import dev.latvian.mods.kubejs.util.ID;
import dev.latvian.mods.kubejs.util.KubeResourceLocation;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.Wrapper;
import dev.latvian.mods.rhino.type.TypeInfo;
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

    public static @Nullable GTResourceLocation wrap(@Nullable Object o) {
        ResourceLocation wrapped = switch (o) {
            case null -> null;
            case ResourceLocation id -> id;
            case ResourceKey<?> key -> key.location();
            case Holder<?> holder when holder.getKey() != null -> holder.getKey().location();
            case RegistryObjectKJS<?> obj -> obj.kjs$getIdLocation();
            default -> parseResLocFromString(o);
        };
        if (wrapped == null) return null;
        return new GTResourceLocation(wrapped);
    }

    @ApiStatus.Internal
    public static KubeResourceLocation wrapWithImplicitGTInRelevantRegistries(Context cx, @Nullable Object from,
                                                                              TypeInfo target) {
        // Hackery to get information about the calling scope without access to the actual `Scriptable scope` parameter

        // fast path: parse it as normal and check if the resulting namespace is `kubejs`
        // this immediately weeds out any resource locations with explicit namespaces
        // without needing all the below heuristics
        KubeResourceLocation wrapped = KubeResourceLocation.wrap(from);
        if (!wrapped.wrapped().getNamespace().equals(KubeJS.MOD_ID)) {
            // it has an explicit namespace, use it.
            return wrapped;
        }
        // also check if it's a string-like type that can contains a ':' at all
        String plainString = stringifyIfPlainString(from);
        if (plainString == null || plainString.indexOf(':') > 0) {
            // not a plain string or string contains an explicit namespace
            return wrapped;
        }

        // this has to be Object typed because Interpreter$CallFrame is private.
        Object lastFrame = ((ContextAccessor) cx).gtceu$getLastInterpreterFrame();
        if (!(lastFrame instanceof InterpreterCallFrameAccessor callFrame)) {
            return wrapped;
        }
        // `Interpreter#interpretLoop`'s function invocation branch uses `stack[stackTop + 1]` as `this`.
        // `CallFrame#savedStackTop` is updated just before `Function#call` so using it should be safe.
        // check Interpreter:919 for more on that
        Object calleeObject = callFrame.gtceu$getStack()[callFrame.gtceu$getSavedStackTop() + 1];
        calleeObject = Wrapper.unwrapped(calleeObject);

        ResourceKey<? extends Registry<?>> registry;
        // only try to use this if it's the correct kind of event
        if (calleeObject instanceof RegistryKubeEventAccessor<?> registryEvent) {
            registry = registryEvent.gtceu$getRegistryKey();
        } else if (calleeObject instanceof ServerRegistryKubeEvent<?> registryEvent) {
            registry = registryEvent.registryKey;
        } else {
            return wrapped;
        }
        // check if it's a GT registry and only do custom parsing if it is
        if (!registry.location().getNamespace().equals(GTCEu.MOD_ID)) {
            return wrapped;
        }

        // this is already checked by stringifyIfPlainString, but IntelliSense can't figure that out
        assert from != null;
        // all conditions match, parse with `gtceu` as the default namespace instead of `kubejs`
        return new KubeResourceLocation(parseResLocFromString(from));
    }

    /**
     * Duplicate of {@link ID#of}'s default branch with {@code preferKJS = true}, but defaulting to the {@code kubejs}
     * namespace has been changed to defaulting to {@code gtceu}.
     * <p>
     * For example, the string <code>"<b><i>kubejs</i></b>:my_path_here"</code> has an explicit namespace, so
     * it'll parse to {@code ResourceLocation(namespace: "kubejs", path: "my_path_here")}.<br>
     * In contrast, {@code "my_custom_id_here"} does <b>not</b> have an explicit namespace, so it'll parse to
     * {@code ResourceLocation(namespace: "gtceu", path: "my_custom_id_here")}.
     *
     * @param o An object.
     * @return A {@code ResourceLocation} with the namespace set to {@code gtceu}
     *         if it doesn't have an explicit namespace.
     * @see ID#kjs
     * @see ID#of
     * @see KubeResourceLocation#wrap
     */
    private static ResourceLocation parseResLocFromString(Object o) {
        String s = o instanceof JsonPrimitive p ? p.getAsString() : o.toString();
        s = GTCEu.appendIdString(s);

        try {
            return ResourceLocation.parse(s);
        } catch (ResourceLocationException ex) {
            throw new KubeRuntimeException("Could not create ID from '%s'!".formatted(s));
        }
    }

    private static @Nullable String stringifyIfPlainString(@Nullable Object o) {
        return switch (o) {
            case null -> null;
            case ResourceLocation id -> null;
            case ResourceKey<?> key -> null;
            case Holder<?> holder -> null;
            case RegistryObjectKJS<?> obj -> null;
            // other kinds of JSON elements should probably also be excluded from this
            case JsonElement json when !json.isJsonPrimitive() -> null;

            // if it isn't any of the above, assume it's something string-like. This is what KJS does.
            case JsonPrimitive p -> p.getAsString();
            default -> o.toString();
        };
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
