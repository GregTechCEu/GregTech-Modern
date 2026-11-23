package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

import com.mojang.serialization.JsonOps;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class SimpleWorldGenLayer implements IWorldGenLayer {

    private final ResourceLocation id;
    private final IWorldGenLayer.RuleTestSupplier target;
    @Getter
    private final Set<ResourceKey<Level>> dimensions;

    // TODO change last parameter's type to Set in 9.0.0
    public SimpleWorldGenLayer(ResourceLocation id, IWorldGenLayer.RuleTestSupplier target,
                               Collection<ResourceKey<Level>> dimensions) {
        this.id = id;
        this.target = target;
        this.dimensions = Set.copyOf(dimensions);

        GTRegistries.WORLD_GEN_LAYERS.register(id, this);
    }

    /**
     * @deprecated use {@link #SimpleWorldGenLayer(ResourceLocation, RuleTestSupplier, Collection)} instead
     */
    @Deprecated(forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "9.0.0")
    public SimpleWorldGenLayer(ResourceLocation id, IWorldGenLayer.RuleTestSupplier target,
                               Set<ResourceLocation> dimensions) {
        this(id, target, dimensions.stream()
                .map(name -> ResourceKey.create(Registries.DIMENSION, name))
                .collect(Collectors.toSet()));
    }

    @Override
    public RuleTest getTarget() {
        return this.target.get();
    }

    @Override
    public boolean existsInDimension(ResourceKey<Level> dimension) {
        return this.dimensions.contains(dimension);
    }

    @Override
    public @NotNull String getSerializedName() {
        return id.toString();
    }

    @Override
    public String toString() {
        return getSerializedName() + "[" +
                RuleTest.CODEC.encodeStart(JsonOps.INSTANCE, this.target.get()).result().orElse(null) + "]" +
                ",dimensions=" + this.dimensions.toString();
    }

    @Override
    public int hashCode() {
        return getSerializedName().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IWorldGenLayer that)) return false;

        return this.getSerializedName().equals(that.getSerializedName());
    }
}
