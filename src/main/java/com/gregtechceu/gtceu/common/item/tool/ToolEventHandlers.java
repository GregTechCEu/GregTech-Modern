package com.gregtechceu.gtceu.common.item.tool;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.capability.ElectricItem;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import dev.architectury.injectables.annotations.ExpectPlatform;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class ToolEventHandlers {

    /**
     * Handles returning broken stacks for tools
     */
    public static void onPlayerDestroyItem(ItemStack original, InteractionHand hand, Player player) {
        Item item = original.getItem();
        if (item instanceof IGTTool def) {
            ItemStack brokenStack = def.getToolStats().getBrokenStack();
            // Transfer over remaining charge to power units
            if (GTCapabilityHelper.getElectricItem(brokenStack) != null && def.isElectric()) {
                long remainingCharge = def.getCharge(original);
                IElectricItem electricStack = GTCapabilityHelper.getElectricItem(brokenStack);
                if (electricStack != null) {
                    // update the max charge of the item, if possible
                    // applies to items like power units, which can have different max charges depending on their recipe
                    if (electricStack instanceof ElectricItem electricItem) {
                        electricItem.setMaxChargeOverride(def.getMaxCharge(original));
                    }

                    electricStack.charge(Math.min(remainingCharge, def.getMaxCharge(original)), def.getElectricTier(), true, false);
                }
            }
            if (!brokenStack.isEmpty()) {
                if (hand == null) {
                    if (!player.addItem(brokenStack)) {
                        player.drop(brokenStack, true);
                    }
                } else {
                    player.setItemInHand(hand, brokenStack);
                }
            }
        }
    }

    public static InteractionResult onPlayerEntityInteract(Player player, InteractionHand hand, Entity target) {
        ItemStack itemStack = player.getItemInHand(hand);
        Item item = itemStack.getItem();

        /*
         * Handle item frame power unit duping
         */
        if (item instanceof IGTTool def) {
            if (target instanceof ItemFrame itemFrame) {
                ItemStack brokenStack = def.getToolStats().getBrokenStack();
                if (!brokenStack.isEmpty()) {
                    itemFrame.interact(player, hand);

                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    /**
     * Handles saws harvesting ice without leaving water behind
     * Handles mined blocks teleporting straight into inventory
     * Handles drop conversion when a hammer tool (or tool with hard hammer enchantment) is used
     */
    public static ObjectArrayList<ItemStack> onHarvestDrops(@Nullable Player player, ItemStack tool, Level world, BlockPos pos, BlockState state, boolean isSilkTouch, int fortuneLevel, ObjectArrayList<ItemStack> drops, float dropChance) {
        if (player != null && world instanceof ServerLevel serverLevel) {
            if (tool.isEmpty() || !tool.hasTag() || !(tool.getItem() instanceof IGTTool)) {
                return drops;
            }
            if (!isSilkTouch) {
                ToolHelper.applyHammerDropConversion(serverLevel, pos, tool, state, drops, fortuneLevel, dropChance, player.getRandom());
            }
            if (!ToolHelper.hasBehaviorsTag(tool)) return drops;

            CompoundTag behaviorTag = ToolHelper.getBehaviorsTag(tool);
            Block block = state.getBlock();
            if (!isSilkTouch && state.is(BlockTags.ICE) && behaviorTag.getBoolean(ToolHelper.HARVEST_ICE_KEY)) {
                Item iceBlock = block.asItem();
                if (drops.stream().noneMatch(drop -> drop.getItem() == iceBlock)) {
                    drops.add(new ItemStack(iceBlock));
                    world.getServer().tell(new TickTask(0, () -> {
                        FluidState flowingState = world.getFluidState(pos);
                        if (flowingState == Fluids.FLOWING_WATER.defaultFluidState()) {
                            world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                        }
                    }));
                    ((IGTTool) tool.getItem()).playSound(player);
                }
            }
            if (behaviorTag.getBoolean(ToolHelper.RELOCATE_MINED_BLOCKS_KEY)) {
                Iterator<ItemStack> dropItr = drops.iterator();
                while (dropItr.hasNext()) {
                    ItemStack dropStack = dropItr.next();
                    ItemEntity drop = new ItemEntity(EntityType.ITEM, world);
                    drop.setItem(dropStack);

                    if (fireItemPickupEvent(drop, player) == -1 || player.addItem(dropStack)) {
                        dropItr.remove();
                    }
                }
            }
        }
        return drops;
    }

    @ExpectPlatform
    public static int fireItemPickupEvent(ItemEntity drop, Player player) {
        throw new AssertionError();
    }

    /**
     * Prevents anvil repairing if tools do not have the same material, or if either are electric.
     * Electric tools can still be repaired with ingots in the anvil, but electric tools cannot
     * be combined with other GT tools, electric or otherwise.
     */
    public static boolean onAnvilUpdateEvent(ItemStack left, ItemStack right) {
        if (left.getItem() instanceof IGTTool leftTool && right.getItem() instanceof IGTTool rightTool) {
            if (leftTool.getToolMaterial(left) != rightTool.getToolMaterial(right)) {
                return false;
            }
            if (leftTool.isElectric() || rightTool.isElectric()) {
                return false;
            }
        }
        return true;
    }

}
