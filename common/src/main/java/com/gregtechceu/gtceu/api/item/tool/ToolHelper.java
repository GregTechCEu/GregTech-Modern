package com.gregtechceu.gtceu.api.item.tool;

import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.item.GTToolItem;
import com.lowdragmc.lowdraglib.utils.RayTraceHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/2/23
 * @implNote ToolHelper
 */
public class ToolHelper {

    public static ItemStack get(GTToolType toolType, Material material) {
        if (material.hasProperty(PropertyKey.TOOL)) {
            var entry = GTItems.TOOL_ITEMS.get(material.getToolTier(), toolType);
            if (entry != null) {
                return entry.asStack();
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean is(ItemStack stack, GTToolType toolType) {
        if (stack.getItem() instanceof GTToolItem item) {
            return item.getToolType() == toolType;
        }
        return false;
    }

    public static boolean canUse(ItemStack stack) {
        return stack.getDamageValue() <= stack.getMaxDamage();
    }

    public static void damageItem(@Nonnull ItemStack stack, RandomSource random, @Nullable ServerPlayer user) {
        if (canUse(stack)) {
            stack.hurt(1, random, user);
        } else {
            stack.shrink(1);
        }
    }

    public static void playToolSound(GTToolType toolType, ServerPlayer player) {
        if (toolType.soundEntry != null) {
            toolType.soundEntry.playOnServer(player.level(), player.blockPosition());
        }
    }

    public static List<BlockPos> getAOEPositions(LivingEntity miner, ItemStack stack, BlockPos pos, int radius) {

        Level level = miner.level();

        ArrayList<BlockPos> aoePositions = new ArrayList<>();
        ArrayList<BlockPos> potentialPositions = new ArrayList<>();


        for(int x = -radius; x <= radius; x++) {
            for(int y = -radius; y <= radius; y++) {
                for(int z = -radius; z <= radius; z++) {
                    potentialPositions.add(new BlockPos(x, y, z));
                }
            }
        }

        Vec3 cameraPos = miner.getEyePosition(1);
        Vec3 rotation = miner.getViewVector(1);

        Vec3 combined = cameraPos.add(rotation.x * 4.5F, rotation.y * 4.5F, rotation.z * 4.5F);

        BlockHitResult result = level.clip(new ClipContext(cameraPos, combined, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, miner));


        for (BlockPos blockPos : potentialPositions) {

            switch (result.getDirection().getAxis()) {
                case X -> {
                    if(blockPos.getX() == 0) {
                        aoePositions.add(pos.offset(blockPos));
                    }
                }
                case Y -> {
                    if(blockPos.getY() == 0) {
                        aoePositions.add(pos.offset(blockPos));
                    }
                }
                case Z -> {
                    if(blockPos.getZ() == 0) {
                        aoePositions.add(pos.offset(blockPos));
                    }
                }
            }
        }

        return aoePositions;
    }

    public static boolean aoeCanBreak(ItemStack stack, Level level, BlockPos origin, BlockPos pos) {
        if (origin.equals(pos)) return false;
        if (!stack.isCorrectToolForDrops(level.getBlockState(pos))) return false;

        BlockState state = level.getBlockState(pos);
        BlockState originState = level.getBlockState(origin);

        //Adapted from GTCEU 1.12.2, used to stop mining blocks like obsidian faster by targeting neighbouring stone. The value 8 is an arbitrary and does not represent anything.
        if (state.getDestroySpeed(level, pos) < 0 || state.getDestroySpeed(level, pos) - originState.getDestroySpeed(level, pos) > 8) return false;

        return true;
    }

}
