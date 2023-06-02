package com.gregtechceu.gtceu.integration.kjs.builders;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.SimpleWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGenLayers;
import com.gregtechceu.gtceu.api.data.worldgen.generator.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

public class WorldGenLayerBuilder extends BuilderBase<SimpleWorldGenLayer> {
    public transient RuleTest target;

    public WorldGenLayerBuilder(ResourceLocation id, Object... args) {
        super(id, args);
        this.target = (RuleTest) args[0];
    }

    @Override
    public SimpleWorldGenLayer register() {
        this.value = new SimpleWorldGenLayer(this.id.getPath(), target);
        WorldGeneratorUtils.WORLD_GEN_LAYERS.put(value.getSerializedName(), value);
        return value;
    }
}
