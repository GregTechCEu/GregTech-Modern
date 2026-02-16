package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTOres;

import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.event.EventJS;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class GTOreVeinEventJS extends EventJS {

    public GTOreVeinEventJS() {}

    public void add(ResourceLocation id, Consumer<GTOreDefinition> consumer) {
        var vein = GTOres.blankOreDefinition();
        consumer.accept(vein);
        vein.register(id);
    }

    public void modify(ResourceLocation id, Consumer<GTOreDefinition> consumer) {
        GTOreDefinition vein = GTRegistries.ORE_VEINS.get(id);

        if (vein == null)
            throw new IllegalArgumentException("Ore vein doesn't exist: " + id);

        consumer.accept(vein);
        vein.register(id);
    }

    public void modifyAll(BiConsumer<ResourceLocation, GTOreDefinition> consumer) {
        Set<ResourceLocation> keys = Set.copyOf(GTRegistries.ORE_VEINS.keys());
        keys.forEach(key -> modify(key, vein -> consumer.accept(key, vein)));
    }

    public void remove(ResourceLocation id) {
        GTRegistries.ORE_VEINS.remove(id);
    }

    public void removeAll() {
        Set<ResourceLocation> keys = Set.copyOf(GTRegistries.ORE_VEINS.keys());
        keys.forEach(this::remove);
    }

    public void removeAll(BiPredicate<ResourceLocation, GTOreDefinition> predicate) {
        Set<ResourceLocation> keys = Set.copyOf(GTRegistries.ORE_VEINS.keys());
        keys.stream()
                .filter(key -> predicate.test(key, GTRegistries.ORE_VEINS.get(key)))
                .forEach(this::remove);
    }
}
