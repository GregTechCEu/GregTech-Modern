package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.data.item.GTToolActions;
import com.gregtechceu.gtceu.common.item.tool.rotation.CustomBlockRotations;
import com.gregtechceu.gtceu.common.item.tool.rotation.ICustomRotationBehavior;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ToolAction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockRotatingBehavior implements IToolBehavior {

    public static final BlockRotatingBehavior INSTANCE = new BlockRotatingBehavior();

    protected BlockRotatingBehavior() {/**/}

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction action) {
        return action == GTToolActions.WRENCH_ROTATE;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockEntity be = level.getBlockEntity(pos);
        // Machines have special handling on rotation
        if (be instanceof MetaMachine) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();

        BlockState state = level.getBlockState(pos);
        Block b = state.getBlock();
        // leave rail rotation to Crowbar only
        if (b instanceof BaseRailBlock) {
            return InteractionResult.FAIL;
        }

        if (player == null || !player.isShiftKeyDown()) {
            // Special cases for vanilla blocks where the default rotation behavior is less than ideal
            ICustomRotationBehavior behavior = CustomBlockRotations.getCustomRotation(b);
            if (behavior != null) {
                if (behavior.customRotate(state, level, pos, retraceBlock(level, player, pos))) {
                    ToolHelper.onActionDone(player, stack, level, context.getClickLocation());
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            } else {
                Rotation rot = player == null || player.getDirection().getClockWise() == context.getClickedFace() ?
                        Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90;
                if (state.rotate(level, pos, rot) != state) {
                    ToolHelper.onActionDone(player, stack, level, context.getClickLocation());
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.gtceu.tool.behavior.block_rotation"));
    }

    public static BlockHitResult retraceBlock(BlockGetter level, Player player, BlockPos pos) {
        double d0 = player.getX();
        double d1 = player.getY() + (double) player.getEyeHeight();
        double d2 = player.getZ();

        Vec3 startVec = new Vec3(d0, d1, d2);

        double range = ToolHelper.getPlayerBlockReach(player);
        float f = player.getXRot();
        float f1 = player.getYRot();
        float f2 = Mth.cos(-f1 * 0.017453292F - (float) Math.PI);
        float f3 = Mth.sin(-f1 * 0.017453292F - (float) Math.PI);
        float f4 = -Mth.cos(-f * 0.017453292F);
        float f5 = Mth.sin(-f * 0.017453292F);
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        Vec3 endVec = startVec.add((double) f6 * range, (double) f5 * range, (double) f7 * range);

        BlockState state = level.getBlockState(pos);
        VoxelShape baseShape = state.getShape(level, pos);
        BlockHitResult baseTraceResult = baseShape.clip(startVec, endVec, pos);
        if (baseTraceResult != null) {
            BlockHitResult raytraceTraceShape = state.getVisualShape(level, pos, CollisionContext.of(player))
                    .clip(startVec, endVec, pos);
            if (raytraceTraceShape != null) {
                return raytraceTraceShape;
            }
        }
        return baseTraceResult;
    }
}
