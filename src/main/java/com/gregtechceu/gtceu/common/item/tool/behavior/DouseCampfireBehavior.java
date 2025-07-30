package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.aoe.AoESymmetrical;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.common.data.item.GTToolActions;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.ToolAction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DouseCampfireBehavior implements IToolBehavior {

    public static final DouseCampfireBehavior INSTANCE = new DouseCampfireBehavior();

    protected DouseCampfireBehavior() {/**/}

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction action) {
        return action == GTToolActions.SHOVEL_DOUSE;
    }

    @NotNull
    @Override
    public InteractionResult onItemUse(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        AoESymmetrical aoeDefinition = ToolHelper.getAoEDefinition(stack);

        List<BlockPos> blocks;
        // only attempt to strip if the center block is strippable
        if (isBlockDousable(context)) {
            if (aoeDefinition.isZero()) {
                blocks = List.of(pos);
            } else {
                blocks = getDousableBlocks(aoeDefinition, context);
                blocks.add(0, context.getClickedPos());
            }
        } else {
            return InteractionResult.PASS;
        }

        boolean dowsed = false;
        for (BlockPos blockPos : blocks) {
            BlockState oldState = level.getBlockState(blockPos);
            CampfireBlock.dowse(player, level, blockPos, oldState);

            BlockState newState = oldState.setValue(CampfireBlock.LIT, false);
            dowsed |= level.setBlock(blockPos, newState, Block.UPDATE_ALL_IMMEDIATE);
            level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(player, newState));

            ToolHelper.damageItem(stack, player);
            if (stack.isEmpty()) break;
        }

        if (dowsed) {
            level.levelEvent(player, LevelEvent.SOUND_EXTINGUISH_FIRE, pos, 0);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    public static List<BlockPos> getDousableBlocks(AoESymmetrical aoeDefinition, UseOnContext context) {
        return ToolHelper.iterateAoE(aoeDefinition, DouseCampfireBehavior::isBlockDousable, context);
    }

    protected static boolean isBlockDousable(UseOnContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        return state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT);
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable Level Level, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.gtceu.tool.behavior.dowse_campfire"));
    }
}
