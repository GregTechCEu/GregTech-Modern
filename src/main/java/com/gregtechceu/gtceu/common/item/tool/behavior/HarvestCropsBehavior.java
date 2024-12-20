package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.aoe.AoESymmetrical;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class HarvestCropsBehavior implements IToolBehavior {

    public static final HarvestCropsBehavior INSTANCE = new HarvestCropsBehavior();

    protected HarvestCropsBehavior() {/**/}

    @NotNull
    @Override
    public InteractionResult onItemUse(UseOnContext context) {
        if (context.getLevel().isClientSide) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        InteractionHand hand = context.getHand();

        ItemStack stack = player.getItemInHand(hand);

        AoESymmetrical aoeDefinition = ToolHelper.getAoEDefinition(stack);

        Set<BlockPos> blocks;

        if (aoeDefinition == AoESymmetrical.none()) {
            blocks = ImmutableSet.of(pos);
        } else {
            HitResult rayTraceResult = ToolHelper.getPlayerDefaultRaytrace(player);

            if (rayTraceResult == null) return InteractionResult.PASS;
            if (rayTraceResult.getType() != HitResult.Type.BLOCK) return InteractionResult.PASS;
            if (!(rayTraceResult instanceof BlockHitResult blockHitResult))
                return InteractionResult.PASS;
            if (blockHitResult.getDirection() == null)
                return InteractionResult.PASS;

            blocks = ToolHelper.iterateAoE(stack, aoeDefinition, player.level(), player, rayTraceResult,
                    HarvestCropsBehavior::isBlockCrops);
            if (isBlockCrops(stack, context.getLevel(), player, blockHitResult.getBlockPos(), context)) {
                blocks.add(blockHitResult.getBlockPos());
            }
        }

        boolean harvested = false;
        for (BlockPos blockPos : blocks) {
            if (harvestBlockRoutine(stack, blockPos, player)) {
                harvested = true;
            }
        }

        return harvested ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private static boolean isBlockCrops(ItemStack stack, Level world, Player player, BlockPos pos,
                                        @Nullable UseOnContext context) {
        if (world.getBlockState(pos.above()).isAir()) {
            Block block = world.getBlockState(pos).getBlock();
            return block instanceof CropBlock;
        }
        return false;
    }

    private static boolean harvestBlockRoutine(ItemStack stack, BlockPos pos, Player player) {
        var level = player.level();
        var blockState = level.getBlockState(pos);
        var block = blockState.getBlock();
        var cropBlock = (CropBlock) block;
        final var seed = cropBlock.getCloneItemStack(level, pos, blockState).getItem();
        if (cropBlock.isMaxAge(blockState)) {
            var drops = Block.getDrops(blockState, (ServerLevel) level, pos, null);
            var iterator = drops.listIterator();
            while (iterator.hasNext()) {
                var drop = iterator.next();
                if (drop.is(seed)) {
                    drop.shrink(1);
                    if (drop.isEmpty()) {
                        iterator.remove();
                    }
                    break;
                }
            }
            dropListOfItems(level, pos, drops);
            level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(blockState));
            level.setBlock(pos, cropBlock.getStateForAge(0), Block.UPDATE_ALL);
            if (!player.isCreative()) {
                ToolHelper.damageItem(stack, player);
            }
            return true;
        }

        return false;
    }

    private static void dropListOfItems(Level world, BlockPos pos, List<ItemStack> drops) {
        for (ItemStack stack : drops) {
            float f = 0.7F;
            double offX = (GTValues.RNG.nextFloat() * f) + (1.0F - f) * 0.5D;
            double offY = (GTValues.RNG.nextFloat() * f) + (1.0F - f) * 0.5D;
            double offZ = (GTValues.RNG.nextFloat() * f) + (1.0F - f) * 0.5D;
            ItemEntity entityItem = new ItemEntity(world, pos.getX() + offX, pos.getY() + offY, pos.getZ() + offZ,
                    stack);
            entityItem.setDefaultPickUpDelay();
            world.addFreshEntity(entityItem);
        }
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.gtceu.tool.behavior.crop_harvesting"));
    }
}
