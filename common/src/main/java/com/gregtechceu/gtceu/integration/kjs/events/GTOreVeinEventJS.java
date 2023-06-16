package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureEntry;
import com.gregtechceu.gtceu.integration.kjs.builders.OreVeinBuilderJS;
import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class GTOreVeinEventJS extends EventJS {

    public GTOreVeinEventJS() {

    }

    public void add(ResourceLocation id, Consumer<OreVeinBuilderJS> consumer) {
        OreVeinBuilderJS builder = new OreVeinBuilderJS(id);
        consumer.accept(builder);
        builder.build();
    }

    public void remove(ResourceLocation id) {
        GTOreFeatureEntry.ALL.remove(id);
    }

    public void modify(ResourceLocation id, Consumer<GTOreFeatureEntry> consumer) {
        consumer.accept(GTOreFeatureEntry.ALL.get(id));
    }
}
