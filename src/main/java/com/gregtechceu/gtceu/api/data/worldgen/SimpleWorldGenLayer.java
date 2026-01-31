package com.gregtechceu.gtceu.api.data.worldgen;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

import com.mojang.serialization.JsonOps;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.stream.Collectors;

public class SimpleWorldGenLayer implements IWorldGenLayer {

    private final String name;
    private final IWorldGenLayer.RuleTestSupplier target;
    @Getter
    private final Set<ResourceKey<Level>> dimensions;

    public SimpleWorldGenLayer(String name, IWorldGenLayer.RuleTestSupplier target,
                               Set<ResourceKey<Level>> dimensions) {
        this.name = name;
        this.target = target;
        this.dimensions = dimensions;
        WorldGeneratorUtils.WORLD_GEN_LAYERS.put(name, this);
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    private @Nullable String cachedToString;

    @Override
    public String toString() {
        if (this.cachedToString == null) {
            String serializedTarget = String.valueOf(RuleTest.CODEC.encodeStart(JsonOps.INSTANCE, target.get()).result().orElse(null));
            String dimensionsString = this.dimensions.stream()
                    .map(key -> key.location().toString())
                    .collect(Collectors.joining(", ", "[", "]"));

            this.cachedToString = getSerializedName() + "[" + serializedTarget + "]" +
                    ",dimensions=" + dimensionsString;
        }
        return this.cachedToString;
    }

    @Override
    public int hashCode() {
        return getSerializedName().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IWorldGenLayer that)) return false;

        return getSerializedName().equals(that.getSerializedName());
    }

    public RuleTest getTarget() {
        return this.target.get();
    }

    @Override
    public boolean isApplicableForLevel(ResourceKey<Level> dimension) {
        return this.dimensions.contains(dimension);
    }
}
