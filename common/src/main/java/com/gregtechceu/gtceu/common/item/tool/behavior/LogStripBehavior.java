package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.google.common.collect.ImmutableSet;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.aoe.AoESymmetrical;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class LogStripBehavior implements IToolBehavior {

    public static final LogStripBehavior INSTANCE = new LogStripBehavior();

    protected LogStripBehavior() {/**/}

    @NotNull
    @Override
    public InteractionResult onItemUse(@NotNull Player player, @NotNull Level level, @NotNull BlockPos pos,
                                       @NotNull InteractionHand hand, @NotNull Direction facing, float hitX, float hitY,
                                       float hitZ) {
        if (facing == Direction.DOWN)
            return InteractionResult.PASS;

        ItemStack stack = player.getItemInHand(hand);
        AoESymmetrical aoeDefinition = ToolHelper.getAoEDefinition(stack);

        Set<BlockPos> blocks;
        // only attempt to till if the center block is tillable
        if (level.getBlockState(pos.above()).isAir() && isBlockLogConvertible(stack, level, player, pos, null)) {
            if (aoeDefinition == AoESymmetrical.none()) {
                blocks = ImmutableSet.of(pos);
            } else {
                HitResult rayTraceResult = ToolHelper.getPlayerDefaultRaytrace(player);

                if (rayTraceResult == null)
                    return InteractionResult.PASS;
                if (rayTraceResult.getType() != HitResult.Type.BLOCK)
                    return InteractionResult.PASS;
                if (!(rayTraceResult instanceof BlockHitResult blockHitResult))
                    return InteractionResult.PASS;
                if (blockHitResult.getDirection() == null)
                    return InteractionResult.PASS;

                blocks = getPathConvertibleBlocks(stack, aoeDefinition, level, player, rayTraceResult);
                blocks.add(blockHitResult.getBlockPos());
            }
        } else
            return InteractionResult.PASS;

        boolean pathed = false;
        for (BlockPos blockPos : blocks) {
            pathed |= level.setBlock(blockPos, AxeItem.STRIPPABLES.get(level.getBlockState(blockPos).getBlock()).defaultBlockState(), Block.UPDATE_ALL);
            ToolHelper.damageItem(stack, player);
            if (stack.isEmpty())
                break;
        }

        if (pathed) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SHOVEL_FLATTEN,
                    SoundSource.PLAYERS, 1.0F, 1.0F);
            player.swing(hand);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    public static Set<BlockPos> getPathConvertibleBlocks(ItemStack stack, AoESymmetrical aoeDefinition, Level Level,
                                                         Player player, HitResult rayTraceResult) {
        return ToolHelper.iterateAoE(stack, aoeDefinition, Level, player, rayTraceResult,
                LogStripBehavior::isBlockLogConvertible);
    }

    private static boolean isBlockLogConvertible(ItemStack stack, Level level, Player player, BlockPos pos,
                                                 @Nullable BlockPos hitBlockPos) {
        if (level.getBlockState(pos.above()).isAir()) {
            Block block = level.getBlockState(pos).getBlock();
            return AxeItem.STRIPPABLES.containsKey(block);
        }
        return false;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable Level Level, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.gt.tool.behavior.strip_log"));
    }
}