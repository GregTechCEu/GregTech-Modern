package com.gregtechceu.gtceu.forge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IHazardEffectTracker;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.commands.ServerCommands;
import com.gregtechceu.gtceu.common.item.ToggleEnergyConsumerBehavior;
import com.gregtechceu.gtceu.common.network.packets.SPacketSyncBedrockOreVeins;
import com.gregtechceu.gtceu.common.network.packets.SPacketSyncFluidVeins;
import com.gregtechceu.gtceu.common.network.packets.SPacketSyncOreVeins;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.item.GTItems;
import com.gregtechceu.gtceu.data.loader.BedrockOreLoader;
import com.gregtechceu.gtceu.data.loader.FluidVeinLoader;
import com.gregtechceu.gtceu.data.loader.OreDataLoader;
import com.gregtechceu.gtceu.data.tag.GTDataComponents;
import com.gregtechceu.gtceu.utils.TaskHandler;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * @author KilaBash
 * @date 2022/8/27
 * @implNote ForgeCommonEventListener
 */
@EventBusSubscriber(modid = GTCEu.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ForgeCommonEventListener {

    @SubscribeEvent
    public static void tickPlayerInventoryHazards(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }
        Player player = event.getEntity();
        IHazardEffectTracker tracker = GTCapabilityHelper.getHazardEffectTracker(player);
        IItemHandler inventory = player.getCapability(Capabilities.ItemHandler.ENTITY);
        if (tracker != null && inventory != null) {
            tracker.startTick();
            for (int i = 0; i < inventory.getSlots(); ++i) {
                ItemStack stack = inventory.getStackInSlot(i);
                Material material = HazardProperty.getValidHazardMaterial(stack);
                if (material == null) {
                    continue;
                }
                tracker.tick(material);
            }
            tracker.endTick();
        }
    }

    @SubscribeEvent
    public static void onBlockStartBreak(BlockEvent.BreakEvent event) {
        if (ToolHelper.isAoeBreakingBlocks.get()) {
            return;
        }

        ItemStack toolStack = event.getPlayer().getItemInHand(event.getPlayer().getUsedItemHand());
        if (toolStack.getItem() instanceof IGTTool tool) {
            if (tool.definition$onBlockStartBreak(toolStack, event.getPos(), event.getPlayer())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        var blockState = event.getLevel().getBlockState(event.getPos());
        if (blockState.hasBlockEntity() && blockState.getBlock() instanceof MetaMachineBlock block &&
                block.getMachine(event.getLevel(), event.getPos()) instanceof IInteractedMachine machine) {
            if (machine.onLeftClick(event.getEntity(), event.getLevel(), event.getHand(), event.getPos(),
                    event.getFace())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        ServerCommands.createServerCommands().forEach(event.getDispatcher()::register);
    }

    @SubscribeEvent
    public static void registerReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new OreDataLoader());
        event.addListener(new FluidVeinLoader());
        event.addListener(new BedrockOreLoader());
    }

    @SubscribeEvent
    public static void levelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            TaskHandler.onTickUpdate(serverLevel);
        }
    }

    @SubscribeEvent
    public static void worldUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            TaskHandler.onWorldUnLoad(serverLevel);
        }
    }

    @SubscribeEvent
    public static void onPlayerJoinServer(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new SPacketSyncOreVeins(GTRegistries.ORE_VEINS.registry()));
            PacketDistributor.sendToPlayer(serverPlayer,
                    new SPacketSyncFluidVeins(GTRegistries.BEDROCK_FLUID_DEFINITIONS.registry()));
            PacketDistributor.sendToPlayer(serverPlayer,
                    new SPacketSyncBedrockOreVeins(GTRegistries.BEDROCK_ORE_DEFINITIONS.registry()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onEntityLivingFallEvent(LivingFallEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ItemStack armor = player.getItemBySlot(EquipmentSlot.FEET);
            ItemStack jet = player.getItemBySlot(EquipmentSlot.CHEST);

            if (player.fallDistance < 3.2f)
                return;

            if (!armor.isEmpty() && armor.getItem() instanceof ArmorComponentItem valueItem) {
                valueItem.getArmorLogic().damageArmor(player, armor, player.damageSources().fall(),
                        (int) (player.fallDistance - 1.2f), EquipmentSlot.FEET);
                player.fallDistance = 0;
                event.setCanceled(true);
            } else if (!jet.isEmpty() && jet.getItem() instanceof ArmorComponentItem valueItem &&
                    jet.has(GTDataComponents.FLY_MODE)) {
                        valueItem.getArmorLogic().damageArmor(player, jet, player.damageSources().fall(),
                                (int) (player.fallDistance - 1.2f), EquipmentSlot.FEET);
                        player.fallDistance = 0;
                        event.setCanceled(true);
                    }
        }
    }

    @SubscribeEvent
    public static void onEntitySpawn(FinalizeSpawnEvent event) {
        Mob entity = event.getEntity();
        Difficulty difficulty = entity.level().getDifficulty();
        if (difficulty == Difficulty.HARD && entity.getRandom().nextFloat() <= 0.03f) {
            if (entity instanceof Zombie zombie && ConfigHolder.INSTANCE.tools.nanoSaber.zombieSpawnWithSabers) {
                ItemStack itemStack = GTItems.NANO_SABER.get().getInfiniteChargedStack();
                ToggleEnergyConsumerBehavior.setItemActive(itemStack, true);
                entity.setItemSlot(EquipmentSlot.MAINHAND, itemStack);
                zombie.setDropChance(EquipmentSlot.MAINHAND, 0.0f);
            }
        }
    }
}
