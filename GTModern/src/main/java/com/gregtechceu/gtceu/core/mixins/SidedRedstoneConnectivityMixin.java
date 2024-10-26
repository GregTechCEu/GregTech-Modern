package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RedstoneSide;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedStoneWireBlock.class)
public class SidedRedstoneConnectivityMixin {

    @Inject(
            method = "getConnectingSide(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)Lnet/minecraft/world/level/block/state/properties/RedstoneSide;",
            at = @At("RETURN"),
            cancellable = true)
    private void gtceu$getConnectingSide(BlockGetter level, BlockPos ownPos, Direction direction,
                                         boolean nonNormalCubeAbove, CallbackInfoReturnable<RedstoneSide> cir) {
        BlockPos blockPos = ownPos.relative(direction);
        BlockState blockState = level.getBlockState(blockPos);

        if (blockState.getBlock() instanceof MetaMachineBlock metaMachine) {
            if (cir.getReturnValue() != RedstoneSide.NONE) {
                return;
            }

            if (!metaMachine.canConnectRedstone(level, blockPos, direction.getOpposite())) {
                cir.setReturnValue(RedstoneSide.NONE);
                return;
            }

            cir.setReturnValue(switch (direction) {
                case UP, DOWN -> RedstoneSide.NONE;
                case NORTH, EAST, SOUTH, WEST -> RedstoneSide.SIDE;
            });
        }
    }
}
