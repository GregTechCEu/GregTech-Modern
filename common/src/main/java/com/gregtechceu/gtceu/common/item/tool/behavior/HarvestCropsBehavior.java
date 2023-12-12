package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.google.common.collect.ImmutableSet;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.aoe.AoESymmetrical;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class HarvestCropsBehavior implements IToolBehavior {

    public static final HarvestCropsBehavior INSTANCE = new HarvestCropsBehavior();

    protected HarvestCropsBehavior() {/**/}

    @NotNull
    @Override
    public InteractionResult onItemUse(@NotNull Player player, @NotNull Level world, @NotNull BlockPos pos,
                                       @NotNull InteractionHand hand, @NotNull Direction facing, float hitX, float hitY,
                                       float hitZ) {
        if (world.isClientSide) {
            return InteractionResult.PASS;
        }
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
            if (isBlockCrops(stack, world, player, blockHitResult.getBlockPos(), null)) {
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
                                        @Nullable BlockPos hitBlockPos) {
        if (world.getBlockState(pos.above()).isAir()) {
            Block block = world.getBlockState(pos).getBlock();
            return block instanceof CropBlock;
        }
        return false;
    }

    private static boolean harvestBlockRoutine(ItemStack stack, BlockPos pos, Player player) {
        BlockState blockState = player.level().getBlockState(pos);
        Block block = blockState.getBlock();
        CropBlock blockCrops = (CropBlock) block;
        if (blockCrops.isMaxAge(blockState)) {
            NonNullList<ItemStack> drops = NonNullList.create();
            drops.addAll(Block.getDrops(blockState, (ServerLevel) player.level(), pos, null));
            dropListOfItems(player.level(), pos, drops);
            player.level().levelEvent(2001, pos, Block.getId(blockState));
            player.level().setBlock(pos, blockCrops.getStateForAge(0), Block.UPDATE_ALL);
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
        tooltip.add(Component.translatable("item.gt.tool.behavior.crop_harvesting"));
    }
}