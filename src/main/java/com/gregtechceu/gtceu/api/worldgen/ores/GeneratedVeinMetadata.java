package com.gregtechceu.gtceu.api.worldgen.ores;

import com.gregtechceu.gtceu.api.worldgen.GTOreDefinition;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import org.jetbrains.annotations.NotNull;

public record GeneratedVeinMetadata(
                                    @NotNull ResourceLocation id,
                                    @NotNull ChunkPos originChunk,
                                    @NotNull BlockPos center,
                                    @NotNull GTOreDefinition definition) {}
