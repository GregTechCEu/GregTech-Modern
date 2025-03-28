package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.item.component.IInteractionItem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;

public class FertilizerBehavior implements IInteractionItem {

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        BlockPos blockPos2 = blockPos.relative(context.getClickedFace());
        if (BoneMealItem.applyBonemeal(context.getItemInHand(), level, blockPos, context.getPlayer())) {
            if (!level.isClientSide) {
                level.levelEvent(1505, blockPos, 0);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else {
            BlockState blockState = level.getBlockState(blockPos);
            boolean bl = blockState.isFaceSturdy(level, blockPos, context.getClickedFace());
            if (bl &&
                    BoneMealItem.growWaterPlant(context.getItemInHand(), level, blockPos2, context.getClickedFace())) {
                if (!level.isClientSide) {
                    level.levelEvent(1505, blockPos2, 0);
                }

                return InteractionResult.sidedSuccess(level.isClientSide);
            } else {
                return InteractionResult.PASS;
            }
        }
    }

    @Override
    public void onAttached(Item item) {
        DispenserBlock.registerBehavior(item, new OptionalDispenseItemBehavior() {

            @SuppressWarnings("deprecation")
            @Override
            protected @NotNull ItemStack execute(@NotNull BlockSource source, @NotNull ItemStack stack) {
                this.setSuccess(true);
                var level = source.getLevel();
                var blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
                if (!BoneMealItem.growCrop(stack, level, blockpos) &&
                        !BoneMealItem.growWaterPlant(stack, level, blockpos, null)) {
                    this.setSuccess(false);
                } else if (!level.isClientSide) {
                    level.levelEvent(1505, blockpos, 0);
                }
                return stack;
            }
        });
    }
}
