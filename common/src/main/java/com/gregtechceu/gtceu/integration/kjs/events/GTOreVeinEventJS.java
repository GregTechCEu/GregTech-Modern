package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GTOreVeinEventJS extends EventJS {

    public GTOreVeinEventJS() {

    }

    public void add(ResourceLocation id, Consumer<GTOreDefinition> consumer) {
        var vein = blankOreDefinition();
        consumer.accept(vein);
        registerVein(id, vein);
    }

    public void remove(ResourceLocation id) {
        GTRegistries.ORE_VEINS.remove(id);
    }

    public void modify(ResourceLocation id, Consumer<GTOreDefinition> consumer) {
        GTOreDefinition vein = GTRegistries.ORE_VEINS.get(id);
        consumer.accept(vein);
        registerVein(id, vein);
    }

    public void modifyAll(BiConsumer<ResourceLocation, GTOreDefinition> consumer) {
        Set<ResourceLocation> keys = Set.copyOf(GTRegistries.ORE_VEINS.keys());
        keys.forEach(key -> modify(key, vein -> consumer.accept(key, vein)));
    }

    private static void registerVein(ResourceLocation id, GTOreDefinition vein) {
        new GTOreDefinition(id, vein);
    }

    private static GTOreDefinition blankOreDefinition() {
        return new GTOreDefinition(0, 0, 0, IWorldGenLayer.nowhere(), Set.of(), HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(0)), 0, null, null, null, null);
    }
}
