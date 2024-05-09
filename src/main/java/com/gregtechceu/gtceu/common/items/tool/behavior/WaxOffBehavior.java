package com.gregtechceu.gtceu.common.items.tool.behavior;

import com.google.common.collect.ImmutableSet;
import com.gregtechceu.gtceu.api.items.tool.ToolHelper;
import com.gregtechceu.gtceu.api.items.datacomponents.AoESymmetrical;
import com.gregtechceu.gtceu.api.items.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.items.tool.behavior.ToolBehaviorType;
import com.gregtechceu.gtceu.data.tools.GTToolBehaviors;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.ToolActions;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class WaxOffBehavior implements IToolBehavior<WaxOffBehavior> {

    public static final WaxOffBehavior INSTANCE = create();
    public static final MapCodec<WaxOffBehavior> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, WaxOffBehavior> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    protected WaxOffBehavior() {/**/}

    protected static WaxOffBehavior create() {
        return new WaxOffBehavior();
    }

    @NotNull
    @Override
    public InteractionResult onItemUse(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        InteractionHand hand = context.getHand();

        ItemStack stack = player.getItemInHand(hand);
        AoESymmetrical aoeDefinition = ToolHelper.getAoEDefinition(stack);

        Set<BlockPos> blocks;
        // only attempt to strip if the center block is strippable
        if (isBlockUnWaxable(stack, level, player, pos, context)) {
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

                blocks = getUnWaxableBlocks(stack, aoeDefinition, level, player, rayTraceResult);
                blocks.add(blockHitResult.getBlockPos());
            }
        } else
            return InteractionResult.PASS;

        boolean pathed = false;
        for (BlockPos blockPos : blocks) {
            pathed |= level.setBlock(blockPos, getUnWaxed(level.getBlockState(blockPos), new UseOnContext(player, hand, context.getHitResult().withPosition(blockPos))), Block.UPDATE_ALL);
            level.levelEvent(player, LevelEvent.PARTICLES_WAX_OFF, blockPos, 0);
            if (!player.isCreative()) {
                ToolHelper.damageItem(context.getItemInHand(), context.getPlayer());
            }
            if (stack.isEmpty())
                break;
        }

        if (pathed) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AXE_WAX_OFF, SoundSource.PLAYERS, 1.0F, 1.0F);
            player.swing(hand);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    public static Set<BlockPos> getUnWaxableBlocks(ItemStack stack, AoESymmetrical aoeDefinition, Level Level, Player player, HitResult rayTraceResult) {
        return ToolHelper.iterateAoE(stack, aoeDefinition, Level, player, rayTraceResult,
                WaxOffBehavior.INSTANCE::isBlockUnWaxable);
    }

    protected boolean isBlockUnWaxable(ItemStack stack, Level level, Player player, BlockPos pos, UseOnContext context) {
        BlockState state = level.getBlockState(pos);
        BlockState newState = state.getToolModifiedState(context, ToolActions.AXE_WAX_OFF, false);
        return newState != null && newState != state;
    }

    protected BlockState getUnWaxed(BlockState unscrapedState, UseOnContext context) {
        return unscrapedState.getToolModifiedState(context, ToolActions.AXE_WAX_OFF, false);
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, Item.TooltipContext Level, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.gtceu.tool.behavior.remove_wax"));
    }

    @Override
    public ToolBehaviorType<WaxOffBehavior> getType() {
        return GTToolBehaviors.WAX_OFF;
    }
}