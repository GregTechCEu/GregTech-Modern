package com.gregtechceu.gtceu.integration.kjs.builders;

import com.gregtechceu.gtceu.api.data.worldgen.SimpleWorldGenLayer;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import dev.latvian.mods.kubejs.level.gen.ruletest.AnyMatchRuleTest;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Accessors(fluent = true, chain = true)
public class WorldGenLayerBuilder extends BuilderBase<SimpleWorldGenLayer> {
    public transient List<RuleTest> targets = new ObjectArrayList<>();
    public transient List<ResourceLocation> dimensions = new ObjectArrayList<>();

    public WorldGenLayerBuilder(ResourceLocation id, Object... args) {
        super(id, args);
    }

    @Override
    public SimpleWorldGenLayer register() {
        this.value = new SimpleWorldGenLayer(this.id.getPath(), () -> new AnyMatchRuleTest(targets), Set.copyOf(dimensions));
        return value;
    }

    public WorldGenLayerBuilder targets(RuleTest... tags) {
        this.targets.addAll(Arrays.asList(tags));
        return this;
    }

    public WorldGenLayerBuilder dimensions(ResourceLocation... dimension) {
        this.dimensions.addAll(Arrays.asList(dimension));
        return this;
    }
}
