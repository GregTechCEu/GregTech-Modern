package com.gregtechceu.gtceu.integration.kjs.builders.worldgen;

import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.SimpleWorldGenLayer;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import dev.latvian.mods.kubejs.level.gen.ruletest.AnyMatchRuleTest;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Accessors(fluent = true, chain = true)
public class WorldGenLayerBuilder extends BuilderBase<SimpleWorldGenLayer> {

    public transient List<IWorldGenLayer.RuleTestSupplier> targets = new ObjectArrayList<>();
    public transient List<ResourceKey<Level>> dimensions = new ObjectArrayList<>();

    public WorldGenLayerBuilder(ResourceLocation id) {
        super(id);
    }

    @Override
    public RegistryInfo<IWorldGenLayer> getRegistryType() {
        return null;
    }

    @Override
    public SimpleWorldGenLayer createObject() {
        return new SimpleWorldGenLayer(
                this.id,
                () -> new AnyMatchRuleTest(targets.stream().map(IWorldGenLayer.RuleTestSupplier::get).toList()),
                Set.copyOf(dimensions));
    }

    public WorldGenLayerBuilder targets(IWorldGenLayer.RuleTestSupplier... targets) {
        Collections.addAll(this.targets, targets);
        return this;
    }

    @SafeVarargs
    public final WorldGenLayerBuilder dimensions(ResourceKey<Level>... dimension) {
        this.dimensions.addAll(Arrays.asList(dimension));
        return this;
    }

    public WorldGenLayerBuilder dimensions(ResourceLocation... dimension) {
        this.dimensions.addAll(Arrays.stream(dimension).map(m -> ResourceKey.create(Registries.DIMENSION, m)).toList());
        return this;
    }
}
