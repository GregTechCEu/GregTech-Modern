package com.gregtechceu.gtceu.common.item.tool;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.capability.ElectricItem;
import com.gregtechceu.gtceu.api.item.datacomponents.ToolBehaviors;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.common.data.GTToolBehaviors;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;

import net.minecraft.core.BlockPos;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
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
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerDestroyItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid = GTCEu.MOD_ID)
public class ToolEventHandlers {

    /**
     * Handles returning broken stacks for tools
     */
    @SubscribeEvent
    public static void onPlayerDestroyItem(PlayerDestroyItemEvent event) {
        ItemStack original = event.getOriginal();
        InteractionHand hand = event.getHand();
        Player player = event.getEntity();

        if (!(original.getItem() instanceof IGTTool gtTool)) {
            return;
        }
        ItemStack brokenStack = gtTool.getToolStats().getBrokenStack();
        // Transfer over remaining charge to power units
        if (GTCapabilityHelper.getElectricItem(brokenStack) != null && gtTool.isElectric()) {
            long remainingCharge = gtTool.getCharge(original);
            IElectricItem electricStack = GTCapabilityHelper.getElectricItem(brokenStack);
            if (electricStack != null) {
                // update the max charge of the item, if possible
                // applies to items like power units, which can have different max charges depending on their recipe
                if (electricStack instanceof ElectricItem electricItem) {
                    electricItem.setMaxChargeOverride(gtTool.getMaxCharge(original));
                }

                electricStack.charge(Math.min(remainingCharge, gtTool.getMaxCharge(original)), gtTool.getElectricTier(),
                        true, false);
            }
        }
        if (brokenStack.isEmpty()) {
            return;
        }
        if (hand == null) {
            if (!player.addItem(brokenStack)) {
                player.drop(brokenStack, true);
            }
        } else {
            player.setItemInHand(hand, brokenStack);
        }
    }

    /**
     * Handle item frame power unit duping
     */
    @SubscribeEvent
    public static void onPlayerEntityInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack itemStack = event.getItemStack();

        if (!(itemStack.getItem() instanceof IGTTool gtTool) || !(event.getTarget() instanceof ItemFrame itemFrame)) {
            return;
        }
        ItemStack brokenStack = gtTool.getToolStats().getBrokenStack();
        if (!brokenStack.isEmpty()) {
            itemFrame.interact(event.getEntity(), event.getHand());

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
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
            drops = new ArrayList<>(drops);
            Iterator<ItemStack> dropItr = drops.iterator();
            while (dropItr.hasNext()) {
                ItemStack dropStack = dropItr.next();
                // Place close to the player for sanity reasons (Instead of XYZ=0,0,0)
                ItemEntity drop = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), dropStack);

                if (isPickupAllowedByEvent(drop, player) && player.addItem(dropStack)) {
                    EventHooks.fireItemPickupPost(drop, player, dropStack.copy());
                    dropItr.remove();
                }

                // Just in case, destroy it
                drop.discard();
            }
        }
        return drops;
    }

    public static boolean isPickupAllowedByEvent(ItemEntity drop, Player player) {
        return !EventHooks.fireItemPickupPre(drop, player).canPickup().isFalse();
    }

    /**
     * Prevents anvil repairing if tools do not have the same material, or if either are electric.
     * Electric tools can still be repaired with ingots in the anvil, but electric tools cannot
     * be combined with other GT tools, electric or otherwise.
     */
    @SubscribeEvent
    public static void onAnvilUpdateEvent(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        if (!(left.getItem() instanceof IGTTool leftTool) || !(right.getItem() instanceof IGTTool rightTool)) {
            return;
        }
        if (leftTool.isElectric() || rightTool.isElectric()) {
            event.setCanceled(true);
        }
        if (leftTool.getToolMaterial(left) != rightTool.getToolMaterial(right)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerKilledEntity(LivingDropsEvent event) {
        Entity entity = event.getSource().getEntity();
        if (!(entity instanceof Player player)) {
            return;
        }
        ItemStack tool = player.getMainHandItem();

        if (!tool.has(GTDataComponents.RELOCATE_MOB_DROPS)) {
            return;
        }
        Iterator<ItemEntity> dropItr = event.getDrops().iterator();

        while (dropItr.hasNext()) {
            ItemEntity drop = dropItr.next();
            ItemStack dropStack = drop.getItem();

            if (isPickupAllowedByEvent(drop, player) && player.addItem(dropStack)) {
                EventHooks.fireItemPickupPost(drop, player, dropStack.copy());
                dropItr.remove();
            }
        }
    }
}
