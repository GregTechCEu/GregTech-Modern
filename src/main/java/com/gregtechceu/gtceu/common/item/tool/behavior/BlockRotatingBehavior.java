package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.item.tool.behavior.ToolBehaviorType;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.data.GTToolBehaviors;
import com.gregtechceu.gtceu.common.data.item.GTItemAbilities;
import com.gregtechceu.gtceu.common.item.tool.rotation.CustomBlockRotations;
import com.gregtechceu.gtceu.common.item.tool.rotation.ICustomRotationBehavior;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
import net.neoforged.neoforge.common.ItemAbility;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BlockRotatingBehavior implements IToolBehavior<BlockRotatingBehavior> {

    public static final BlockRotatingBehavior INSTANCE = new BlockRotatingBehavior();
    public static final Codec<BlockRotatingBehavior> CODEC = Codec.unit(INSTANCE);
    public static final StreamCodec<ByteBuf, BlockRotatingBehavior> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    protected BlockRotatingBehavior() {}

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility action) {
        return action == GTItemAbilities.WRENCH_ROTATE;
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
    public void addInformation(@NotNull ItemStack stack, Item.TooltipContext context, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.gtceu.tool.behavior.block_rotation"));
    }

    @Override
    public ToolBehaviorType<BlockRotatingBehavior> getType() {
        return GTToolBehaviors.BLOCK_ROTATING;
    }

    public static BlockHitResult retraceBlock(BlockGetter level, Player player, BlockPos pos) {
        double playerX = player.getX();
        double playerY = player.getY() + (double) player.getEyeHeight();
        double playerZ = player.getZ();

        Vec3 startVec = new Vec3(playerX, playerY, playerZ);

        double reachDistance = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE).getValue();

        float playerXRot = player.getXRot();
        float playerYRot = player.getYRot();

        float yawCos = Mth.cos(-playerYRot * ((float) Math.PI / 180F) - (float) Math.PI);
        float yawSin = Mth.sin(-playerYRot * ((float) Math.PI / 180F) - (float) Math.PI);

        float pitchCos = -Mth.cos(-playerXRot * ((float) Math.PI / 180F));
        float pitchSin = Mth.sin(-playerXRot * ((float) Math.PI / 180F));

        float lookX = yawSin * pitchCos;
        float lookZ = yawCos * pitchCos;

        Vec3 endVec = startVec.add(
                (double) lookX * reachDistance,
                (double) pitchSin * reachDistance,
                (double) lookZ * reachDistance);

        BlockState state = level.getBlockState(pos);

        VoxelShape baseShape = state.getShape(level, pos);
        BlockHitResult baseTraceResult = baseShape.clip(startVec, endVec, pos);

        if (baseTraceResult != null) {
            BlockHitResult visualShapeTraceResult = state
                    .getVisualShape(level, pos, CollisionContext.of(player))
                    .clip(startVec, endVec, pos);

            if (visualShapeTraceResult != null) {
                return visualShapeTraceResult;
            }
        }

        return baseTraceResult;
    }
}
