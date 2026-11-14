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
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.core.BlockPos;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerDestroyItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

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
     * <ul>
     * <li>Handles saws harvesting ice without leaving water behind</li>
     * <li>Handles mined blocks teleporting straight into the player's inventory</li>
     * <li>Handles drop conversion when a hammer tool (or tool with the Hard Hammer enchantment) is used</li>
     * </ul>
     */
    public static List<ItemStack> onHarvestDrops(@Nullable Entity entity, ItemStack tool, ServerLevel level,
                                                 BlockPos pos, BlockState state,
                                                 List<ItemStack> drops, LootParams.Builder lootParams) {
        if (!EnchantmentHelper.hasTag(tool, CustomTags.PREVENTS_HAMMER_CRUSHING)) {
            ToolHelper.applyHammerDropConversion(level, pos, tool, state, drops, lootParams);
        }

        ToolBehaviors behaviors = ToolHelper.getBehaviorsComponent(tool);
        Block block = state.getBlock();
        if (state.is(BlockTags.ICE) && !EnchantmentHelper.hasTag(tool, EnchantmentTags.PREVENTS_ICE_MELTING) &&
                behaviors.hasBehavior(GTToolBehaviors.HARVEST_ICE)) {
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
                if (entity != null && tool.getItem() instanceof IGTTool gtTool) {
                    gtTool.playSound(entity);
                }
            }
        }
        if (tool.has(GTDataComponents.RELOCATE_MINED_BLOCKS) && entity instanceof Player player) {
            drops = new ArrayList<>(drops);
            ListIterator<ItemStack> dropItr = drops.listIterator();
            while (dropItr.hasNext()) {
                ItemStack dropStack = dropItr.next();

                // Place close to the player for sanity reasons (Instead of XYZ=0,0,0)
                ItemEntity drop = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), dropStack);
                // make the player attempt picking the item up immediately
                drop.pickupDelay = 0;
                drop.setTarget(player.getUUID());
                drop.playerTouch(player);

                // item entities discard themselves if they're picked up completely
                if (drop.isRemoved()) {
                    // if the whole stack was picked up, remove the item from the list of drops
                    dropItr.remove();
                } else {
                    // otherwise replace it with the new item
                    dropStack = drop.getItem();
                    dropItr.set(dropStack);
                    // and destroy the item entity ourselves
                    drop.discard();
                }
            }
        }
        return drops;
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

        // copy the list of drops so we can remove discarded entities from the original safely
        List<ItemEntity> dropsCopy = new ArrayList<>(event.getDrops());
        for (ItemEntity drop : dropsCopy) {
            // make the player attempt picking the item up immediately
            drop.pickupDelay = 0;
            drop.setTarget(player.getUUID());
            drop.playerTouch(player);

            // item entities discard themselves if they're picked up completely
            if (drop.isRemoved()) {
                // if the whole stack was picked up, remove the item from the list of drops
                event.getDrops().remove(drop);
            }
        }
    }

    @SubscribeEvent
    public static void onServerClosing(ServerStoppingEvent event) {
        ToolHelper.clearCachedLootModifiers();
    }
}
