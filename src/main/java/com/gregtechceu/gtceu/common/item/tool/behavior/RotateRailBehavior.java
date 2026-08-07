package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.item.tool.behavior.ToolBehaviorType;
import com.gregtechceu.gtceu.common.data.GTToolBehaviors;
import com.gregtechceu.gtceu.common.data.item.GTItemAbilities;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbility;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RotateRailBehavior implements IToolBehavior<RotateRailBehavior> {

    public static final RotateRailBehavior INSTANCE = new RotateRailBehavior();
    public static final Codec<RotateRailBehavior> CODEC = Codec.unit(INSTANCE);
    public static final StreamCodec<ByteBuf, RotateRailBehavior> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    protected RotateRailBehavior() {}

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility action) {
        return action == GTItemAbilities.CROWBAR_ROTATE;
    }

    @NotNull
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof BaseRailBlock) {
            if (level.setBlock(pos, state.rotate(level, pos, Rotation.CLOCKWISE_90), Block.UPDATE_ALL)) {
                ToolHelper.onActionDone(context.getPlayer(), stack, level, context.getClickLocation());
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, Item.TooltipContext context, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.gtceu.tool.behavior.rail_rotation"));
    }

    @Override
    public ToolBehaviorType<RotateRailBehavior> getType() {
        return GTToolBehaviors.ROTATE_RAIL;
    }
}
