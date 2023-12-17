package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.integration.kjs.builders.OreVeinBuilderJS;
import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;
import java.util.function.Consumer;

public class GTOreVeinEventJS extends EventJS {

    public GTOreVeinEventJS() {

    }

    public void add(ResourceLocation id, Consumer<OreVeinBuilderJS> consumer) {
        OreVeinBuilderJS builder = new OreVeinBuilderJS(id);
        consumer.accept(builder);
        if (!builder.isBuilt()) {
            builder.build();
        }
    }

    public void remove(ResourceLocation id) {
        GTRegistries.ORE_VEINS.remove(id);
    }

    public void modify(ResourceLocation id, Consumer<OreVeinBuilderJS> consumer) {
        GTOreDefinition vein = GTRegistries.ORE_VEINS.get(id);
        if (vein == null)
            throw new IllegalArgumentException("Cannot modify vein as it does not exist: " + id);

        OreVeinBuilderJS builder = OreVeinBuilderJS.fromDefinition(id, vein);
        consumer.accept(builder);

        if (!builder.isBuilt()) {
            builder.build();
        }
    }

    public void modifyAll(Consumer<OreVeinBuilderJS> consumer) {
        Set<ResourceLocation> keys = Set.copyOf(GTRegistries.ORE_VEINS.keys());
        keys.forEach(key -> modify(key, consumer));
    }
}
