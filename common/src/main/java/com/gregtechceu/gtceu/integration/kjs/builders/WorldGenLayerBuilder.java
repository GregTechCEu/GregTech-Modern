package com.gregtechceu.gtceu.integration.kjs.builders;

import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import com.gregtechceu.gtceu.integration.kjs.built.KJSWorldGenLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

public class WorldGenLayerBuilder extends BuilderBase<KJSWorldGenLayer> {
    public transient RuleTest target;

    public WorldGenLayerBuilder(ResourceLocation id, Object... args) {
        super(id, args);
        this.target = (RuleTest) args[0];
    }

    @Override
    public KJSWorldGenLayer register() {
        return new KJSWorldGenLayer(this.id.getPath(), target);
    }
}
