package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.data.worldgen.BiomeWeightModifier;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.integration.kjs.builders.worldgen.BedrockFluidBuilder;

import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.script.ConsoleJS;
import dev.latvian.mods.kubejs.script.SourceLine;
import dev.latvian.mods.rhino.Context;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class GTBedrockFluidVeinEventJS implements KubeEvent {

    private final WritableRegistry<BedrockFluidDefinition> registry;

    public GTBedrockFluidVeinEventJS(WritableRegistry<BedrockFluidDefinition> registry) {
        this.registry = registry;
    }

    public void add(Context cx, Identifier id, Consumer<BedrockFluidBuilder> consumer) {
        BedrockFluidBuilder builder = new BedrockFluidBuilder(id);
        consumer.accept(builder);
        register(id, builder.createTransformedObject());
    }

    private void register(Identifier id, BedrockFluidDefinition def) {
        registry.register(createKey(id), def, RegistrationInfo.BUILT_IN);
    }

    public void modify(Context cx, Identifier id, Consumer<BedrockFluidBuilder> consumer) {
        var vein = registry.getValue(id);
        if (vein == null) throw new IllegalArgumentException("Fluid vein doesn't exist: " + id);
        var builder = BedrockFluidBuilder.from(vein, id);
        consumer.accept(builder);
        register(id, builder.createTransformedObject());
    }

    public void modifyAll(Context cx, BiConsumer<Identifier, BedrockFluidBuilder> consumer) {
        Set<Identifier> keys = Set.copyOf(registry.keySet());
        keys.forEach(id -> {
            var vein = registry.getValue(id);
            if (vein == null) throw new IllegalArgumentException("Fluid vein doesn't exist: " + id);
            var builder = BedrockFluidBuilder.from(vein, id);
            consumer.accept(id, builder);
            register(id, builder.createTransformedObject());
        });
    }

    public void removeAll(Context cx) {
        Set<Identifier> keys = Set.copyOf(registry.keySet());
        keys.forEach(key -> remove(cx, key));
    }

    public void removeAll(Context cx, BiPredicate<Identifier, BedrockFluidDefinition> predicate) {
        Set<Identifier> keys = Set.copyOf(registry.keySet());
        keys.stream()
                .filter(key -> predicate.test(key, registry.getValue(key)))
                .forEach(key -> remove(cx, key));
    }

    public void remove(Context cx, Identifier id) {
        if (!registry.containsKey(id)) {
            ConsoleJS.SERVER.error("", new KubeRuntimeException("Trying to remove nonexistent bedrock ore vein " + id)
                    .source(SourceLine.of(cx)));
            return;
        }
        // blank out the vein info because we can't remove from the registry
        var vein = registry.getValueOrThrow(createKey(id));
        vein.setBiomeWeightModifier(BiomeWeightModifier.EMPTY);
        vein.setWeight(0);
    }

    public static ResourceKey<BedrockFluidDefinition> createKey(Identifier id) {
        return ResourceKey.create(GTRegistries.BEDROCK_FLUID_REGISTRY, id);
    }
}
