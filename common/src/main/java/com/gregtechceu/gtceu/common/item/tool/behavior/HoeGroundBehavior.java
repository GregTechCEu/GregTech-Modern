package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.google.common.collect.ImmutableSet;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.aoe.AoESymmetrical;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Used to allow a tool to hoe the ground, only if it cannot extend the {@link gregtech.api.items.toolitem.ItemGTHoe}
 * class.
 */
public class HoeGroundBehavior implements IToolBehavior {

    public static final HoeGroundBehavior INSTANCE = new HoeGroundBehavior();

    protected HoeGroundBehavior() {/**/}

    @NotNull
    @Override
    public InteractionResult onItemUse(@NotNull Player player, @NotNull Level world, @NotNull BlockPos pos, @NotNull InteractionHand hand, @NotNull Direction facing, float hitX, float hitY, float hitZ) {
        if (facing == Direction.DOWN) return InteractionResult.PASS;

        ItemStack stack = player.getItemInHand(hand);
        AoESymmetrical aoeDefinition = ToolHelper.getAoEDefinition(stack);

        Set<BlockPos> blocks;
        // only attempt to till if the center block is tillable
        if (world.getBlockState(pos.above()).isAir() && isBlockTillable(stack, world, player, pos, null)) {
            if (aoeDefinition == AoESymmetrical.none()) {
                blocks = ImmutableSet.of(pos);
            } else {
                HitResult rayTraceResult = ToolHelper.getPlayerDefaultRaytrace(player);

                if (rayTraceResult == null) return InteractionResult.PASS;
                if (rayTraceResult.getType() != HitResult.Type.BLOCK) return InteractionResult.PASS;
                if (!(rayTraceResult instanceof BlockHitResult blockHitResult))
                    return InteractionResult.PASS;
                if (blockHitResult.getDirection() == null)
                    return InteractionResult.PASS;

                blocks = getTillableBlocks(stack, aoeDefinition, world, player, blockHitResult);
                if (isBlockTillable(stack, world, player, blockHitResult.getBlockPos(), null)) {
                    blocks.add(blockHitResult.getBlockPos());
                }
            }
        } else return InteractionResult.PASS;

        boolean tilled = false;
        for (BlockPos blockPos : blocks) {
            BlockState state = world.getBlockState(blockPos);
            Block block = state.getBlock();
            if (HoeItem.TILLABLES.containsKey(block)) {
                tillGround(world, player, hand, new Vec3(hitX, hitY, hitZ), stack, facing, blockPos, HoeItem.TILLABLES.get(block).getSecond());
                tilled = true;
            }
        }

        if (tilled) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.HOE_TILL,
                    SoundSource.PLAYERS, 1.0F, 1.0F);
            player.swing(hand);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    public static Set<BlockPos> getTillableBlocks(ItemStack stack, AoESymmetrical aoeDefinition, Level world, Player player, HitResult rayTraceResult) {
        return ToolHelper.iterateAoE(stack, aoeDefinition, world, player, rayTraceResult,
                HoeGroundBehavior::isBlockTillable);
    }

    private static boolean isBlockTillable(ItemStack stack, Level world, Player player, BlockPos pos, @Nullable BlockPos hitBlockPos) {
        if (world.getBlockState(pos.above()).isAir()) {
            Block block = world.getBlockState(pos).getBlock();
            return HoeItem.TILLABLES.containsKey(block);
        }
        return false;
    }

    private static void tillGround(@NotNull Level world, Player player, InteractionHand hand, Vec3 hitPos, ItemStack stack, Direction direction, BlockPos pos, Consumer<UseOnContext> state) {
        state.accept(new UseOnContext(player, hand, new BlockHitResult(hitPos, direction, pos, false)));
        if (!player.isCreative()) {
            ToolHelper.damageItem(stack, player);
        }
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.gt.tool.behavior.ground_tilling"));
    }
}