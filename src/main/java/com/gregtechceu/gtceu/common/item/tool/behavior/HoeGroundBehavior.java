package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.datacomponents.AoESymmetrical;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.item.tool.behavior.ToolBehaviorType;
import com.gregtechceu.gtceu.common.data.GTToolBehaviors;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Used to allow a tool to hoe the ground
 */
public class HoeGroundBehavior implements IToolBehavior<HoeGroundBehavior> {

    public static final HoeGroundBehavior INSTANCE = new HoeGroundBehavior();
    public static final Codec<HoeGroundBehavior> CODEC = Codec.unit(INSTANCE);
    public static final StreamCodec<ByteBuf, HoeGroundBehavior> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    protected HoeGroundBehavior() {/**/}

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility action) {
        return action == ItemAbilities.HOE_TILL;
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
        // only attempt to till if the center block is tillable
        if (isBlockTillable(context)) {
            if (aoeDefinition.isZero()) {
                blocks = List.of(pos);
            } else {
                blocks = getTillableBlocks(aoeDefinition, context);
                blocks.addFirst(context.getClickedPos());
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
        BlockState newState = state.getToolModifiedState(context, ItemAbilities.HOE_TILL, true);
        return newState != null && newState != state;
    }

    protected boolean tillGround(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        BlockState newState = state.getToolModifiedState(context, ItemAbilities.HOE_TILL, false);
        if (newState != null && newState != state) {
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(context.getPlayer(), newState));
            return level.setBlock(pos, newState, Block.UPDATE_ALL_IMMEDIATE);
        }
        return false;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, Item.TooltipContext context, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.gtceu.tool.behavior.ground_tilling"));
    }

    @Override
    public ToolBehaviorType<HoeGroundBehavior> getType() {
        return GTToolBehaviors.HOE_GROUND;
    }
}
