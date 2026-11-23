package com.gregtechceu.gtceu.api.data.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

import org.jetbrains.annotations.ApiStatus;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public interface IWorldGenLayer extends StringRepresentable {

    @ApiStatus.OverrideOnly
    default boolean existsInDimension(ResourceKey<Level> level) {
        return isApplicableForLevel(level.location());
    }

    /**
     * @deprecated override {@link #existsInDimension(ResourceKey)} instead
     */
    @Deprecated(forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "9.0.0")
    @ApiStatus.OverrideOnly
    default boolean isApplicableForLevel(ResourceLocation level) {
        return false;
    }

    @ApiStatus.OverrideOnly
    default Set<ResourceKey<Level>> getDimensions() {
        return getLevels().stream()
                .map(id -> ResourceKey.create(Registries.DIMENSION, id))
                .collect(Collectors.toSet());
    }

    /**
     * @deprecated override {@link #getDimensions()} instead
     */
    @Deprecated(forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "9.0.0")
    @ApiStatus.OverrideOnly
    default Set<ResourceLocation> getLevels() {
        return Set.of();
    }

    @ApiStatus.OverrideOnly
    RuleTest getTarget();

    @FunctionalInterface
    interface RuleTestSupplier extends Supplier<RuleTest> {

        RuleTest get();
    }

    IWorldGenLayer NOWHERE = new IWorldGenLayer() {

        @Override
        public boolean existsInDimension(ResourceKey<Level> level) {
            return false;
        }

        @Override
        public Set<ResourceKey<Level>> getDimensions() {
            return Set.of();
        }

        @Override
        public RuleTest getTarget() {
            return AlwaysTrueTest.INSTANCE;
        }

        @Override
        public String getSerializedName() {
            return "nowhere";
        }
    };
}
