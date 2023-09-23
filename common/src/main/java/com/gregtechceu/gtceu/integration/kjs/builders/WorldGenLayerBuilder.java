package com.gregtechceu.gtceu.integration.kjs.builders;

import com.gregtechceu.gtceu.api.data.worldgen.SimpleWorldGenLayer;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

import java.util.Set;

@Accessors(fluent = true, chain = true)
public class WorldGenLayerBuilder extends BuilderBase<SimpleWorldGenLayer> {
    @Setter
    public transient RuleTest target;

    public WorldGenLayerBuilder(ResourceLocation id, Object... args) {
        super(id, args);
    }

    @Override
    public SimpleWorldGenLayer register() {
        // TODO add applicable dimensions here instead of passing an empty set
        this.value = new SimpleWorldGenLayer(this.id.getPath(), target, Set.of());
        return value;
    }

    // TODO add a way to modify applicable dimensions
}
