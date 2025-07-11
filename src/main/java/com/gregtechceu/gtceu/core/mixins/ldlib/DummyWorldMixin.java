package com.gregtechceu.gtceu.core.mixins.ldlib;

import com.lowdragmc.lowdraglib.utils.DummyWorld;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraftforge.client.model.data.ModelDataManager;
import net.minecraftforge.common.extensions.IForgeBlockGetter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Supplier;

@Mixin(value = DummyWorld.class, remap = false)
public abstract class DummyWorldMixin extends Level implements IForgeBlockGetter {

    @Shadow @NotNull public abstract Level getLevel();

    protected DummyWorldMixin(WritableLevelData levelData, ResourceKey<Level> dimension,
                              RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration,
                              Supplier<ProfilerFiller> profiler, boolean isClientSide, boolean isDebug,
                              long biomeZoomSeed, int maxChainedNeighborUpdates) {
        super(levelData, dimension, registryAccess, dimensionTypeRegistration, profiler,
                isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @Nullable ModelDataManager getModelDataManager() {
        return getLevel().getModelDataManager();
    }
}
