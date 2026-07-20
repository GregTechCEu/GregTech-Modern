package com.gregtechceu.gtceu.integration.kjs.builders.worldgen;

import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.SimpleWorldGenLayer;

import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.level.gen.ruletest.AnyMatchRuleTest;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Accessors(fluent = true, chain = true)
public class WorldGenLayerBuilder extends BuilderBase<IWorldGenLayer> {

    public transient List<IWorldGenLayer.RuleTestSupplier> targets = new ObjectArrayList<>();
    public transient List<ResourceLocation> dimensions = new ObjectArrayList<>();

    public WorldGenLayerBuilder(ResourceLocation id) {
        super(id);
    }

    @Override
    public RegistryInfo<IWorldGenLayer> getRegistryType() {
        return GTRegistryInfo.WORLD_GEN_LAYER;
    }

    public WorldGenLayerBuilder targets(IWorldGenLayer.RuleTestSupplier... targets) {
        Collections.addAll(this.targets, targets);
        return this;
    }

    public WorldGenLayerBuilder dimensions(ResourceLocation... dimension) {
        this.dimensions.addAll(Arrays.asList(dimension));
        return this;
    }

    @Override
    public IWorldGenLayer createObject() {
        return new SimpleWorldGenLayer(
                this.id,
                () -> new AnyMatchRuleTest(targets.stream().map(IWorldGenLayer.RuleTestSupplier::get).toList()),
                Set.copyOf(dimensions), false);
    }
}
