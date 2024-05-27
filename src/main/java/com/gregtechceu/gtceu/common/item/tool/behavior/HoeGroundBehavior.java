package com.gregtechceu.gtceu.common.item.tool.behavior;

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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ToolActions;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * Used to allow a tool to hoe the ground, only if it cannot extend the
 * {@link com.gregtechceu.gtceu.api.item.tool.GTHoeItem}
 * class.
 */
public class HoeGroundBehavior implements IToolBehavior {

    public static final HoeGroundBehavior INSTANCE = create();

    protected HoeGroundBehavior() {/**/}

    protected static HoeGroundBehavior create() {
        return new HoeGroundBehavior();
    }

    @NotNull
    @Override
    public InteractionResult onItemUse(UseOnContext context) {
        if (context.getClickedFace() == Direction.DOWN) return InteractionResult.PASS;

        Level world = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        InteractionHand hand = context.getHand();

        ItemStack stack = player.getItemInHand(hand);
        AoESymmetrical aoeDefinition = ToolHelper.getAoEDefinition(stack);

        Set<BlockPos> blocks;
        // only attempt to till if the center block is tillable
        if (isBlockTillable(stack, world, player, pos, context)) {
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

                blocks = getTillableBlocks(stack, aoeDefinition, world, player, blockHitResult);
                if (isBlockTillable(stack, world, player, blockHitResult.getBlockPos(), context)) {
                    blocks.add(blockHitResult.getBlockPos());
                }
            }
        } else return InteractionResult.PASS;

        boolean tilled = false;
        for (BlockPos blockPos : blocks) {
            BlockState state = world.getBlockState(blockPos);
            tilled |= tillGround(new UseOnContext(player, hand, context.getHitResult().withPosition(blockPos)), state);
            if (!player.isCreative()) {
                ToolHelper.damageItem(context.getItemInHand(), context.getPlayer());
            }
            if (stack.isEmpty())
                break;
        }

        if (tilled) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.HOE_TILL,
                    SoundSource.PLAYERS, 1.0F, 1.0F);
            player.swing(hand);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    public static Set<BlockPos> getTillableBlocks(ItemStack stack, AoESymmetrical aoeDefinition, Level world,
                                                  Player player, HitResult rayTraceResult) {
        return ToolHelper.iterateAoE(stack, aoeDefinition, world, player, rayTraceResult,
                HoeGroundBehavior.INSTANCE::isBlockTillable);
    }

    protected boolean isBlockTillable(ItemStack stack, Level world, Player player, BlockPos pos, UseOnContext context) {
        if (world.getBlockState(pos.above()).isAir()) {
            BlockState state = world.getBlockState(pos);
            BlockState newState = state.getToolModifiedState(context, ToolActions.HOE_TILL, false);
            return newState != null && newState != state;
        }
        return false;
    }

    protected boolean tillGround(UseOnContext context, BlockState state) {
        BlockState newState = state.getToolModifiedState(context, ToolActions.HOE_TILL, false);
        if (newState != null && newState != state) {
            context.getLevel().gameEvent(GameEvent.BLOCK_CHANGE, context.getClickedPos(),
                    GameEvent.Context.of(context.getPlayer(), state));
            return context.getLevel().setBlock(context.getClickedPos(), newState, Block.UPDATE_ALL_IMMEDIATE);
        }
        return false;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.gtceu.tool.behavior.ground_tilling"));
    }
}
