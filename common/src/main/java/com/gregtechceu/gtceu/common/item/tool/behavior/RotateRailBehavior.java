package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RotateRailBehavior implements IToolBehavior {

    public static final RotateRailBehavior INSTANCE = new RotateRailBehavior();

    protected RotateRailBehavior() {/**/}

    @NotNull
    @Override
    public InteractionResult onItemUseFirst(@NotNull Player player, @NotNull Level world, @NotNull BlockPos pos,
                                            @NotNull Direction facing, float hitX, float hitY, float hitZ,
                                            @NotNull InteractionHand hand) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof RailBlock) {
            if (world.setBlock(pos, state.rotate(Rotation.CLOCKWISE_90), Block.UPDATE_ALL)) {
                ToolHelper.onActionDone(player, world, hand);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.gt.tool.behavior.rail_rotation"));
    }
}