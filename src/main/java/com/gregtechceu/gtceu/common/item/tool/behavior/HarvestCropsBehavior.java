package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.aoe.AoESymmetrical;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.common.data.item.GTToolActions;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HarvestCropsBehavior implements IToolBehavior {

    public static final HarvestCropsBehavior INSTANCE = new HarvestCropsBehavior();

    protected HarvestCropsBehavior() {/**/}

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction action) {
        return action == GTToolActions.HOE_HARVEST;
    }

    @NotNull
    @Override
    public InteractionResult onItemUse(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        AoESymmetrical aoeDefinition = ToolHelper.getAoEDefinition(stack);

        List<BlockPos> blocks;
        if (aoeDefinition.isZero()) {
            blocks = List.of(pos);
        } else {
            blocks = ToolHelper.iterateAoE(aoeDefinition, HarvestCropsBehavior::isBlockCrops, context);
            if (isBlockCrops(context)) {
                blocks.add(0, context.getClickedPos());
            }
        }

        boolean harvested = false;
        for (BlockPos blockPos : blocks) {
            harvested |= harvestBlockRoutine(blockPos, context);
            if (stack.isEmpty()) break;
        }

        if (harvested) {
            BlockState state = level.getBlockState(pos);
            level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    private static boolean isBlockCrops(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (level.getBlockState(pos.above()).isAir()) {
            Block block = level.getBlockState(pos).getBlock();
            return block instanceof CropBlock;
        }
        return false;
    }

    private static boolean harvestBlockRoutine(BlockPos pos, UseOnContext context) {
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();

        BlockState blockState = level.getBlockState(pos);
        if (!(blockState.getBlock() instanceof CropBlock cropBlock)) return false;

        ItemStack seed = blockState.getCloneItemStack(context.getHitResult().withPosition(pos), level, pos, player);
        if (cropBlock.isMaxAge(blockState)) {
            if (!level.isClientSide) {
                var drops = Block.getDrops(blockState, (ServerLevel) level, pos, null);
                boolean removedSeed = false;
                for (ItemStack drop : drops) {
                    if (!removedSeed && ItemStack.isSameItemSameTags(drop, seed)) {
                        drop.shrink(1);
                        removedSeed = true;
                        if (drop.isEmpty()) continue;
                    }
                    Block.popResource(level, pos, drop);
                }
            }
            level.setBlock(pos, cropBlock.getStateForAge(0), Block.UPDATE_ALL_IMMEDIATE);
            ToolHelper.damageItem(stack, player);
            return true;
        }

        return false;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.gtceu.tool.behavior.crop_harvesting"));
    }
}
