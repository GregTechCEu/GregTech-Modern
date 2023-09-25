package com.gregtechceu.gtceu.integration.kjs.builders;

import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.SimpleWorldGenLayer;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Accessors(fluent = true, chain = true)
public class WorldGenLayerBuilder extends BuilderBase<SimpleWorldGenLayer> {
    @Setter
    public transient IWorldGenLayer.RuleTestSupplier target;

    @Setter
    public transient List<ResourceLocation> dimensions = new ObjectArrayList<>();

    public WorldGenLayerBuilder(ResourceLocation id, Object... args) {
        super(id, args);
    }

    @Override
    public SimpleWorldGenLayer register() {
        this.value = new SimpleWorldGenLayer(this.id.getPath(), target, Set.copyOf(dimensions));
        return value;
    }

    public WorldGenLayerBuilder addDimension(ResourceLocation dimension) {
        this.dimensions.add(dimension);
        return this;
    }
}
