package com.gregtechceu.gtceu.core.mixins.spoilage;

import com.gregtechceu.gtceu.api.item.spoilage.BlockEntitySpoilTicker;

import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.ticks.TickPriority;
import net.minecraftforge.common.capabilities.ICapabilityProviderImpl;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnstableApiUsage")
@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin extends ChunkAccess implements ICapabilityProviderImpl<LevelChunk> {

    public LevelChunkMixin(ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor,
                           Registry<Biome> biomeRegistry, long inhabitedTime, @Nullable LevelChunkSection[] sections,
                           @Nullable BlendingData blendingData) {
        super(chunkPos, upgradeData, levelHeightAccessor, biomeRegistry, inhabitedTime, sections, blendingData);
    }

    @Inject(method = "addAndRegisterBlockEntity",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/world/level/block/entity/BlockEntity;onLoad()V",
                     shift = At.Shift.AFTER),
            remap = false)
    private void gtceu$addBlockEntityToSpoilTickers(BlockEntity blockEntity, CallbackInfo ci) {
        if (blockEntity.getLevel() instanceof ServerLevel level) {
            BlockEntitySpoilTicker.get(level).scheduleTickIfValid(blockEntity, 0L, TickPriority.EXTREMELY_HIGH);
        }
    }
}
