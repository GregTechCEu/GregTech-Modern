package com.gregtechceu.gtceu.core.mixins.spoilage;

import com.gregtechceu.gtceu.api.item.spoilage.BlockSpoilTicker;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.TickPriority;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @Inject(method = "onBlockStateChange", at = @At(value = "TAIL"))
    public void gtceu$updateSpoilageOnBlockStateChange(BlockPos pos, BlockState blockState, BlockState newState, CallbackInfo ci) {
        BlockSpoilTicker ticker = BlockSpoilTicker.get((ServerLevel) (Object) this);
        ticker.scheduleInitialTick(blockState.getBlock(), pos, BlockSpoilTicker.INITIAL_TICK_DELAY, TickPriority.EXTREMELY_HIGH);
    }
}
