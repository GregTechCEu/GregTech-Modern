package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.item.tool.behavior.ToolBehaviorType;
import com.gregtechceu.gtceu.data.tools.GTToolBehaviors;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RotateRailBehavior implements IToolBehavior<RotateRailBehavior> {

    public static final RotateRailBehavior INSTANCE = new RotateRailBehavior();
    public static final Codec<RotateRailBehavior> CODEC = Codec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, RotateRailBehavior> STREAM_CODEC = StreamCodec
            .unit(INSTANCE);

    protected RotateRailBehavior() {/**/}

    @NotNull
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (state.getBlock() instanceof BaseRailBlock) {
            if (context.getLevel().setBlock(context.getClickedPos(), state.rotate(Rotation.CLOCKWISE_90),
                    Block.UPDATE_ALL)) {
                ToolHelper.onActionDone(context.getPlayer(), context.getLevel(), context.getHand());
                return InteractionResult.SUCCESS;
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
