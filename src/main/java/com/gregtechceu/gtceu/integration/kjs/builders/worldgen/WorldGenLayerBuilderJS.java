package com.gregtechceu.gtceu.integration.kjs.builders.worldgen;

import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.SimpleWorldGenLayer;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import dev.latvian.mods.kubejs.level.ruletest.AnyMatchRuleTest;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Accessors(fluent = true, chain = true)
public class WorldGenLayerBuilderJS extends BuilderBase<IWorldGenLayer> {

    public transient List<IWorldGenLayer.RuleTestSupplier> targets = new ObjectArrayList<>();
    public transient List<ResourceKey<Level>> dimensions = new ObjectArrayList<>();

    public WorldGenLayerBuilderJS(ResourceLocation id) {
        super(id);
    }

    public WorldGenLayerBuilderJS targets(IWorldGenLayer.RuleTestSupplier... targets) {
        Collections.addAll(this.targets, targets);
        return this;
    }

    public WorldGenLayerBuilderJS dimensions(List<ResourceKey<Level>> dimensions) {
        this.dimensions.addAll(dimensions);
        return this;
    }

    @Override
    public IWorldGenLayer createObject() {
        return new SimpleWorldGenLayer(
                this.id,
                () -> new AnyMatchRuleTest(targets.stream().map(IWorldGenLayer.RuleTestSupplier::get).toList()),
                Set.copyOf(dimensions));
    }
}
