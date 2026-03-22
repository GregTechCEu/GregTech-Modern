package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.aoe.AoESymmetrical;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Used to allow a tool to hoe the ground, only if it cannot extend the
 * {@link com.gregtechceu.gtceu.api.item.tool.GTHoeItem}
 * class.
 */
public class HoeGroundBehavior implements IToolBehavior {

    public static final HoeGroundBehavior INSTANCE = new HoeGroundBehavior();

    protected HoeGroundBehavior() {/**/}

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction action) {
        return action == ToolActions.HOE_TILL;
    }

    @NotNull
    @Override
    public InteractionResult onItemUse(UseOnContext context) {
        if (context.getClickedFace() == Direction.DOWN) return InteractionResult.PASS;

        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        AoESymmetrical aoeDefinition = ToolHelper.getAoEDefinition(stack);

        List<BlockPos> blocks;
        // only attempt to till if the center block is tillable
        if (isBlockTillable(context)) {
            if (aoeDefinition.isZero()) {
                blocks = List.of(pos);
            } else {
                blocks = getTillableBlocks(aoeDefinition, context);
                blocks.add(0, context.getClickedPos());
            }
        } else {
            return InteractionResult.PASS;
        }

        boolean tilled = false;
        for (BlockPos blockPos : blocks) {
            UseOnContext posContext = new UseOnContext(level, player, context.getHand(), stack,
                    context.getHitResult().withPosition(blockPos));
            tilled |= tillGround(posContext);

            ToolHelper.damageItem(stack, player);
            if (stack.isEmpty()) break;
        }

        if (tilled) {
            level.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    public static List<BlockPos> getTillableBlocks(AoESymmetrical aoeDefinition, UseOnContext context) {
        return ToolHelper.iterateAoE(aoeDefinition, HoeGroundBehavior::isBlockTillable, context);
    }

    protected static boolean isBlockTillable(UseOnContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        BlockState newState = state.getToolModifiedState(context, ToolActions.HOE_TILL, true);
        return newState != null && newState != state;
    }

    protected boolean tillGround(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        BlockState newState = state.getToolModifiedState(context, ToolActions.HOE_TILL, false);
        if (newState != null && newState != state) {
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(context.getPlayer(), newState));
            return level.setBlock(pos, newState, Block.UPDATE_ALL_IMMEDIATE);
        }
        return false;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.gtceu.tool.behavior.ground_tilling"));
    }
}
