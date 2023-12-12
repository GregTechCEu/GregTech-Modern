package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockRotatingBehavior implements IToolBehavior {

    public static final BlockRotatingBehavior INSTANCE = new BlockRotatingBehavior();

    protected BlockRotatingBehavior() {/**/}

    @Override
    public InteractionResult onItemUseFirst(@NotNull Player player, @NotNull Level world, @NotNull BlockPos pos,
                                            @NotNull Direction side, float hitX, float hitY, float hitZ,
                                            @NotNull InteractionHand hand) {
        BlockEntity te = world.getBlockEntity(pos);
        // MTEs have special handling on rotation
        if (te instanceof IMachineBlockEntity) {
            return InteractionResult.PASS;
        }

        BlockState state = world.getBlockState(pos);
        Block b = state.getBlock();
        // leave rail rotation to Crowbar only
        if (b instanceof RailBlock) {
            return InteractionResult.FAIL;
        }

        if (!player.isCrouching()) {
            // Special cases for vanilla blocks where the default rotation behavior is less than ideal
            /* TODO custom rotation behaviour
            ICustomRotationBehavior behavior = CustomBlockRotations.getCustomRotation(b);
            if (behavior != null) {
                if (behavior.customRotate(state, world, pos, RayTracer.retraceBlock(world, player, pos))) {
                    ToolHelper.onActionDone(player, world, hand);
                    return InteractionResult.SUCCESS;
                }
            } else*/ if (state.rotate(player.getDirection().getClockWise() == side ? Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90) != state) {
                ToolHelper.onActionDone(player, world, hand);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.gt.tool.behavior.block_rotation"));
    }
}