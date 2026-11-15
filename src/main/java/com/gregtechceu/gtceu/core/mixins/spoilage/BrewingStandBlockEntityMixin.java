package com.gregtechceu.gtceu.core.mixins.spoilage;

import com.gregtechceu.gtceu.api.item.component.SpoilUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin {

    @Inject(at = @At(value = "HEAD"), method = "serverTick")
    private static void gtceu$serverTick(Level level, BlockPos pos, BlockState state,
                                         BrewingStandBlockEntity blockEntity, CallbackInfo ci) {
        SpoilUtils.updateBlock(blockEntity);
    }
}
