package com.gregtechceu.gtceu.integration.kjs.builders;

import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.SimpleWorldGenLayer;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import dev.latvian.mods.kubejs.level.gen.ruletest.AnyMatchRuleTest;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.experimental.Accessors;

import java.util.*;
import java.util.stream.Collectors;

@Accessors(fluent = true, chain = true)
public class WorldGenLayerBuilder extends BuilderBase<SimpleWorldGenLayer> {

    public transient List<IWorldGenLayer.RuleTestSupplier> targets = new ObjectArrayList<>();
    public transient Set<ResourceLocation> dimensions = new LinkedHashSet<>();

    public WorldGenLayerBuilder(ResourceLocation id) {
        super(id);
    }

    @Override
    public SimpleWorldGenLayer register() {
        IWorldGenLayer.RuleTestSupplier target = () -> {
            return new AnyMatchRuleTest(this.targets.stream().map(IWorldGenLayer.RuleTestSupplier::get).toList());
        };
        Set<ResourceKey<Level>> dimensions = this.dimensions.stream()
                .map(id -> ResourceKey.create(Registries.DIMENSION, id))
                .collect(Collectors.toSet());
        this.value = new SimpleWorldGenLayer(this.id, target, dimensions);
        return value;
    }

    public WorldGenLayerBuilder targets(IWorldGenLayer.RuleTestSupplier... targets) {
        Collections.addAll(this.targets, targets);
        return this;
    }

    public WorldGenLayerBuilder dimensions(ResourceLocation... dimensions) {
        Collections.addAll(this.dimensions, dimensions);
        return this;
    }
}
