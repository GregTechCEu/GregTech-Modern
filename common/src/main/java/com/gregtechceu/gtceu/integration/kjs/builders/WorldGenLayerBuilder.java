package com.gregtechceu.gtceu.integration.kjs.builders;

import com.gregtechceu.gtceu.api.data.worldgen.SimpleWorldGenLayer;
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
        return new SimpleWorldGenLayer(this.id.getPath(), target);
    }
}
