package com.gregtechceu.gtceu.integration.kjs.builders;

import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.SimpleWorldGenLayer;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import dev.latvian.mods.kubejs.level.ruletest.AnyMatchRuleTest;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Accessors(fluent = true, chain = true)
public class WorldGenLayerBuilder {

    public transient String name;
    public transient List<IWorldGenLayer.RuleTestSupplier> targets = new ObjectArrayList<>();
    public transient List<ResourceKey<Level>> dimensions = new ObjectArrayList<>();

    public WorldGenLayerBuilder(String name) {
        this.name = name;
    }

    public SimpleWorldGenLayer build() {
        return new SimpleWorldGenLayer(
                name,
                () -> new AnyMatchRuleTest(targets.stream().map(IWorldGenLayer.RuleTestSupplier::get).toList()),
                Set.copyOf(dimensions));
    }

    public WorldGenLayerBuilder targets(IWorldGenLayer.RuleTestSupplier... targets) {
        Collections.addAll(this.targets, targets);
        return this;
    }

    public WorldGenLayerBuilder dimensions(List<ResourceKey<Level>> dimensions) {
        this.dimensions.addAll(dimensions);
        return this;
    }
}
