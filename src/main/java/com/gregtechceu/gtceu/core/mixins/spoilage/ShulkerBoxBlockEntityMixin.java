package com.gregtechceu.gtceu.core.mixins.spoilage;

import com.gregtechceu.gtceu.api.item.component.SpoilUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerBoxBlockEntity.class)
public class ShulkerBoxBlockEntityMixin {

    @Inject(at = @At(value = "HEAD"), method = "tick")
    private static void gtceu$serverTick(Level level, BlockPos pos, BlockState state, ShulkerBoxBlockEntity blockEntity,
                                         CallbackInfo ci) {
        SpoilUtils.updateBlock(blockEntity);
    }
}
