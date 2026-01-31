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

@Accessors(fluent = true, chain = true)
public class WorldGenLayerBuilder extends BuilderBase<SimpleWorldGenLayer> {

    public transient List<IWorldGenLayer.RuleTestSupplier> targets = new ObjectArrayList<>();
    public transient Set<ResourceKey<Level>> dimensions = new HashSet<>();

    public WorldGenLayerBuilder(ResourceLocation id) {
        super(id);
    }

    @Override
    public SimpleWorldGenLayer register() {
        this.value = new SimpleWorldGenLayer(
                this.id.getPath(),
                () -> new AnyMatchRuleTest(this.targets.stream().map(IWorldGenLayer.RuleTestSupplier::get).toList()),
                this.dimensions);
        return value;
    }

    public WorldGenLayerBuilder targets(IWorldGenLayer.RuleTestSupplier... targets) {
        Collections.addAll(this.targets, targets);
        return this;
    }

    public WorldGenLayerBuilder dimensions(ResourceLocation... dimensions) {
        for (ResourceLocation id : dimensions) {
            this.dimensions.add(ResourceKey.create(Registries.DIMENSION, id));
        }
        return this;
    }
}
