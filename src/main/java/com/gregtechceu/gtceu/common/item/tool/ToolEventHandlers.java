package com.gregtechceu.gtceu.common.item.tool;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.capability.ElectricItem;
import com.gregtechceu.gtceu.api.item.datacomponents.ToolBehaviors;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.data.tag.GTDataComponents;

import com.gregtechceu.gtceu.data.tools.GTToolBehaviors;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.PlayerDestroyItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid = GTCEu.MOD_ID)
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

                    electricStack.charge(Math.min(remainingCharge, def.getMaxCharge(original)), def.getElectricTier(),
                            true, false);
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
    public static List<ItemStack> onHarvestDrops(Player player, ItemStack tool, ServerLevel level,
                                                 BlockPos pos, BlockState state, boolean isSilkTouch,
                                                 int fortuneLevel, List<ItemStack> drops,
                                                 float dropChance) {
        if (!(tool.getItem() instanceof IGTTool)) {
            return drops;
        }
        if (!isSilkTouch) {
            ToolHelper.applyHammerDropConversion(level, pos, tool, state, drops, fortuneLevel, dropChance,
                    player.getRandom());
        }
        if (!ToolHelper.hasBehaviorsComponent(tool)) return drops;

        ToolBehaviors behaviorTag = ToolHelper.getBehaviorsComponent(tool);
        Block block = state.getBlock();
        if (!isSilkTouch && state.is(BlockTags.ICE) && behaviorTag.hasBehavior(GTToolBehaviors.HARVEST_ICE)) {
            Item iceBlock = block.asItem();
            if (drops.stream().noneMatch(drop -> drop.getItem() == iceBlock)) {
                drops.add(new ItemStack(iceBlock));
                level.getServer().tell(new TickTask(0, () -> {
                    BlockState oldState = level.getBlockState(pos);
                    if (oldState.getFluidState().isSourceOfType(Fluids.WATER)) {
                        // I think it may be a waterlogged block, although the probability is very small
                        BlockState newState = oldState.hasProperty(BlockStateProperties.WATERLOGGED) ?
                                oldState.setValue(BlockStateProperties.WATERLOGGED, false) :
                                Blocks.AIR.defaultBlockState();
                        level.setBlockAndUpdate(pos, newState);
                    }
                }));
                ((IGTTool) tool.getItem()).playSound(player);
            }
        }
        if (tool.has(GTDataComponents.RELOCATE_MINED_BLOCKS)) {
            Iterator<ItemStack> dropItr = drops.iterator();
            while (dropItr.hasNext()) {
                ItemStack dropStack = dropItr.next();
                ItemEntity drop = new ItemEntity(EntityType.ITEM, level);
                drop.setItem(dropStack);

                if (fireItemPickupEvent(drop, player) || player.addItem(dropStack)) {
                    EventHooks.fireItemPickupPost(drop, player, dropStack.copy());
                    dropItr.remove();
                }
            }
        }
        return drops;
    }

    public static boolean fireItemPickupEvent(ItemEntity drop, Player player) {
        return !EventHooks.fireItemPickupPre(drop, player).canPickup().isFalse();
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

    @SubscribeEvent
    public static void onPlayerDestroyItem(@NotNull PlayerDestroyItemEvent event) {
        ToolEventHandlers.onPlayerDestroyItem(event.getOriginal(), event.getHand(), event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerEntityInteract(@NotNull PlayerInteractEvent.EntityInteract event) {
        InteractionResult result = ToolEventHandlers.onPlayerEntityInteract(event.getEntity(), event.getHand(),
                event.getTarget());
        if (result != InteractionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }

    @SubscribeEvent
    public static void onAnvilUpdateEvent(@NotNull AnvilUpdateEvent event) {
        if (!ToolEventHandlers.onAnvilUpdateEvent(event.getLeft(), event.getRight())) {
            event.setCanceled(true);
        }
    }
}
