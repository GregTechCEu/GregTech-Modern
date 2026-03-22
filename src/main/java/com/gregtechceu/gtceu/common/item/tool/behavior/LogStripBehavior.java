package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.aoe.AoESymmetrical;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;

import net.minecraft.core.BlockPos;
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
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LogStripBehavior implements IToolBehavior {

    public static final LogStripBehavior INSTANCE = new LogStripBehavior();

    protected LogStripBehavior() {/**/}

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction action) {
        return action == ToolActions.AXE_STRIP;
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
        if (isBlockStrippable(context)) {
            if (aoeDefinition.isZero()) {
                blocks = List.of(pos);
            } else {
                blocks = getStrippableBlocks(aoeDefinition, context);
                blocks.add(context.getClickedPos());
            }
        } else {
            return InteractionResult.PASS;
        }

        boolean stripped = false;
        for (BlockPos blockPos : blocks) {
            UseOnContext posContext = new UseOnContext(level, player, context.getHand(), stack,
                    context.getHitResult().withPosition(blockPos));
            BlockState newState = getStripped(level.getBlockState(blockPos), posContext);
            stripped |= level.setBlock(blockPos, newState, Block.UPDATE_ALL);

            ToolHelper.damageItem(stack, player);
            if (stack.isEmpty()) break;
        }

        if (stripped) {
            level.playSound(player, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    public static List<BlockPos> getStrippableBlocks(AoESymmetrical aoeDefinition, UseOnContext context) {
        return ToolHelper.iterateAoE(aoeDefinition, LogStripBehavior::isBlockStrippable, context);
    }

    protected static boolean isBlockStrippable(UseOnContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        BlockState newState = state.getToolModifiedState(context, ToolActions.AXE_STRIP, true);
        return newState != null && newState != state;
    }

    protected BlockState getStripped(BlockState state, UseOnContext context) {
        return state.getToolModifiedState(context, ToolActions.AXE_STRIP, false);
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable Level Level, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.gtceu.tool.behavior.strip_log"));
    }
}
