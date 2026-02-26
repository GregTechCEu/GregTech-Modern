package com.gregtechceu.gtceu.forge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.capability.IMedicalConditionTracker;
import com.gregtechceu.gtceu.api.cosmetics.CapeRegistry;
import com.gregtechceu.gtceu.api.cosmetics.event.RegisterGTCapesEvent;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.material.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.misc.virtualregistry.VirtualEnderRegistry;
import com.gregtechceu.gtceu.api.multiblock.MultiblockWorldSavedData;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.TooltipsHandler;
import com.gregtechceu.gtceu.common.capability.EnvironmentalHazardSavedData;
import com.gregtechceu.gtceu.common.capability.LocalizedHazardSavedData;
import com.gregtechceu.gtceu.common.capability.WorldIDSaveData;
import com.gregtechceu.gtceu.common.cosmetics.GTCapes;
import com.gregtechceu.gtceu.common.data.loader.PostRegistryListener;
import com.gregtechceu.gtceu.common.item.armor.IJetpack;
import com.gregtechceu.gtceu.common.item.armor.IStepAssist;
import com.gregtechceu.gtceu.common.item.armor.QuarkTechSuite;
import com.gregtechceu.gtceu.common.item.behavior.ToggleEnergyConsumerBehavior;
import com.gregtechceu.gtceu.common.item.datacomponents.FormatStringList;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.common.network.packets.SPacketSendWorldID;
import com.gregtechceu.gtceu.common.network.packets.hazard.SPacketAddHazardZone;
import com.gregtechceu.gtceu.common.network.packets.hazard.SPacketRemoveHazardZone;
import com.gregtechceu.gtceu.common.network.packets.hazard.SPacketSyncLevelHazards;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.command.GTCommands;
import com.gregtechceu.gtceu.data.command.HazardCommands;
import com.gregtechceu.gtceu.data.command.MedicalConditionCommands;
import com.gregtechceu.gtceu.data.entity.GTAttributeModifierIds;
import com.gregtechceu.gtceu.data.item.GTDataComponents;
import com.gregtechceu.gtceu.data.item.GTItems;
import com.gregtechceu.gtceu.data.tag.CustomTags;
import com.gregtechceu.gtceu.integration.map.ClientCacheManager;
import com.gregtechceu.gtceu.integration.map.WaypointManager;
import com.gregtechceu.gtceu.integration.map.cache.server.ServerCache;
import com.gregtechceu.gtceu.utils.GTStringUtils;
import com.gregtechceu.gtceu.utils.GlobalPosWithRot;
import com.gregtechceu.gtceu.utils.TaskHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.event.AddAttributeTooltipsEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = GTCEu.MOD_ID)
public class CommonEventListener {

    @SubscribeEvent
    public static void registerCapes(RegisterGTCapesEvent event) {
        GTCapes.registerGTCapes(event);
        GTCapes.giveDevCapes(event);
    }

    @SubscribeEvent
    public static void tickPlayerInventoryHazards(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }

        Player player = event.getEntity();
        IMedicalConditionTracker tracker = GTCapabilityHelper.getMedicalConditionTracker(player);
        if (!ConfigHolder.INSTANCE.gameplay.hazardsEnabled) {
            for (MedicalCondition medicalCondition : tracker.getMedicalConditions().keySet()) {
                tracker.removeMedicalCondition(medicalCondition);
            }
            return;
        }

        IItemHandler inventory = player.getCapability(Capabilities.ItemHandler.ENTITY, null);
        if (inventory == null) {
            return;
        }
        tracker.tick();

        for (int i = 0; i < inventory.getSlots(); ++i) {
            ItemStack stack = inventory.getStackInSlot(i);
            Material material = HazardProperty.getValidHazardMaterial(stack);
            if (material.isNull() || !material.hasProperty(PropertyKey.HAZARD)) {
                continue;
            }
            HazardProperty property = material.getProperty(PropertyKey.HAZARD);
            if (property.hazardTrigger.protectionType().isProtected(player)) {
                // entity has proper safety equipment, so damage it per material every 5 seconds.
                property.hazardTrigger.protectionType().damageEquipment(player, 1);
                // don't progress this material condition if entity is protected
                continue;
            }
            tracker.progressRelatedCondition(material);
        }
    }

    @SubscribeEvent
    public static void onMobEffectEvent(MobEffectEvent.Applicable event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack item = player.getItemBySlot(EquipmentSlot.HEAD);
            IElectricItem helmet = GTCapabilityHelper.getElectricItem(item);
            if (item.is(GTItems.QUANTUM_HELMET.asItem()) && helmet != null) {
                MobEffectInstance effect = event.getEffectInstance();
                int cost = QuarkTechSuite.potionRemovalCost.getOrDefault(effect.getEffect(), -1);
                if (cost != -1) {
                    cost = cost * (effect.getAmplifier() + 1);
                    if (helmet.canUse(cost)) {
                        helmet.discharge(cost, helmet.getTier(), true, false, false);
                        event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBlockStartBreak(BlockEvent.BreakEvent event) {
        if (ToolHelper.IS_AOE_BREAKING_BLOCKS.get()) {
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
    public static void onBreakEvent(BlockEvent.BreakEvent event) {
        var machine = MetaMachine.getMachine(event.getLevel(), event.getPos());
        if (machine != null) {
            if (!MachineOwner.canBreakOwnerMachine(event.getPlayer(), machine)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        GTCommands.register(event.getDispatcher(), event.getBuildContext());
        MedicalConditionCommands.register(event.getDispatcher(), event.getBuildContext());
        HazardCommands.register(event.getDispatcher(), event.getBuildContext());
    }

    @SubscribeEvent
    public static void registerReloadListeners(AddReloadListenerEvent event) {
        GTRegistries.updateFrozenRegistry(event.getRegistryAccess());
        event.addListener(PostRegistryListener.INSTANCE);
    }

    @SubscribeEvent
    public static void levelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            TaskHandler.onTickUpdate(serverLevel);
            if (ConfigHolder.INSTANCE.gameplay.environmentalHazards) {
                EnvironmentalHazardSavedData.getOrCreate(serverLevel).tick();
                LocalizedHazardSavedData.getOrCreate(serverLevel).tick();
            }
        }
    }

    @SubscribeEvent
    public static void worldLoad(LevelEvent.Load event) {
        if (event.getLevel().isClientSide()) {
            WaypointManager.updateDimension(event.getLevel());
        } else if (event.getLevel() instanceof ServerLevel serverLevel) {
            ServerCache.instance.maybeInitWorld(serverLevel);
        }
    }

    @SubscribeEvent
    public static void worldUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            TaskHandler.onWorldUnLoad(serverLevel);
            MultiblockWorldSavedData.getOrCreate(serverLevel).releaseExecutorService();
            ServerCache.instance.invalidateWorld(serverLevel);
        } else if (event.getLevel().isClientSide()) {
            ClientCacheManager.saveCaches(event.getLevel().registryAccess());
        }
    }

    @SubscribeEvent
    public static void serverStarting(ServerStartingEvent event) {
        ServerLevel mainLevel = event.getServer().overworld();
        WorldIDSaveData.init(mainLevel);
        CapeRegistry.registerToServer(mainLevel);
    }

    @SubscribeEvent
    public static void serverStopped(ServerStoppedEvent event) {
        ServerCache.instance.clear();
        VirtualEnderRegistry.release();
    }

    @SubscribeEvent
    public static void serverStopping(ServerStoppingEvent event) {
        var levels = event.getServer().getAllLevels();
        for (var level : levels) {
            if (!level.isClientSide()) {
                MultiblockWorldSavedData.getOrCreate(level).releaseExecutorService();
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerJoinServer(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        PacketDistributor.sendToPlayer(serverPlayer, new SPacketSendWorldID());

        if (ConfigHolder.INSTANCE.gameplay.environmentalHazards) {
            ServerLevel level = serverPlayer.serverLevel();
            var data = EnvironmentalHazardSavedData.getOrCreate(level);
            PacketDistributor.sendToPlayer(serverPlayer, new SPacketSyncLevelHazards(data.getHazardZones()));
        }
        CapeRegistry.detectNewCapes(serverPlayer);
        CapeRegistry.loadCurrentCapesOnLogin(serverPlayer);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onEntityLivingFallEvent(LivingFallEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.fallDistance < 3.2f)
                return;

            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);

            if (boots.is(CustomTags.STEP_BOOTS) && boots.getItem() instanceof ArmorComponentItem armor) {
                armor.getArmorLogic().damageArmor(player, boots,
                        (int) (player.fallDistance - 1.2f), EquipmentSlot.FEET);
                player.fallDistance = 0;
                event.setCanceled(true);
            } else if (chest.getItem() instanceof ArmorComponentItem armor &&
                    armor.getArmorLogic() instanceof IJetpack jetpack &&
                    jetpack.canUseEnergy(chest, jetpack.getEnergyPerUse()) &&
                    player.fallDistance >= player.getHealth() + 3.2f) {
                        IJetpack.performEHover(chest, player);
                        player.fallDistance = 0;
                        event.setCanceled(true);
                    }
        }
    }

    @SubscribeEvent
    public static void stepAssistHandler(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }
        AttributeInstance stepHeightAttribute = entity.getAttribute(Attributes.STEP_HEIGHT);
        if (stepHeightAttribute == null) {
            return;
        }
        if (!entity.isShiftKeyDown() && entity.getItemBySlot(EquipmentSlot.FEET).is(CustomTags.STEP_BOOTS)) {
            stepHeightAttribute.addOrUpdateTransientModifier(IStepAssist.STEP_ASSIST_MODIFIER);
        } else {
            stepHeightAttribute.removeModifier(IStepAssist.STEP_ASSIST_MODIFIER);
        }
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!event.getSlot().isArmor()) return;
        if (!(event.getEntity() instanceof Player player)) return;

        if (!event.getFrom().isEmpty() && event.getFrom().getItem() instanceof ArmorComponentItem armor) {
            armor.getArmorLogic().onUnequip(player);
        }
        if (!event.getTo().isEmpty() && event.getTo().getItem() instanceof ArmorComponentItem armor) {
            armor.getArmorLogic().onEquip(player);
        }
    }

    @SubscribeEvent
    public static void onEntityDie(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            IMedicalConditionTracker tracker = GTCapabilityHelper.getMedicalConditionTracker(player);
            for (MedicalCondition condition : tracker.getMedicalConditions().keySet()) {
                tracker.removeMedicalCondition(condition);
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

    @SubscribeEvent
    public static void onPlayerLevelChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!ConfigHolder.INSTANCE.gameplay.environmentalHazards) {
            return;
        }

        ServerLevel newLevel = event.getEntity().getServer().getLevel(event.getTo());
        var data = EnvironmentalHazardSavedData.getOrCreate(newLevel);
        PacketDistributor.sendToPlayer((ServerPlayer) event.getEntity(),
                new SPacketSyncLevelHazards(data.getHazardZones()));
    }

    @SubscribeEvent
    public static void onChunkWatch(ChunkWatchEvent.Watch event) {
        ChunkPos pos = event.getPos();
        ServerPlayer player = event.getPlayer();
        var data = EnvironmentalHazardSavedData.getOrCreate(event.getLevel());

        var zone = data.getZoneByPos(pos);
        if (zone != null) {
            PacketDistributor.sendToPlayer(player, new SPacketAddHazardZone(pos, zone));
        }
    }

    @SubscribeEvent
    public static void onChunkUnWatch(ChunkWatchEvent.UnWatch event) {
        ChunkPos pos = event.getPos();
        ServerPlayer player = event.getPlayer();
        var data = EnvironmentalHazardSavedData.getOrCreate(event.getLevel());

        var zone = data.getZoneByPos(pos);
        if (zone != null) {
            PacketDistributor.sendToPlayer(player, new SPacketRemoveHazardZone(pos));
        }
    }

    @SubscribeEvent
    public static void onTooltipEvent(ItemTooltipEvent event) {
        TooltipsHandler.appendTooltips(event.getItemStack(), event.getFlags(), event.getToolTip(), event.getContext());
    }

    @SubscribeEvent
    public static void onAttributeTooltipEvent(AddAttributeTooltipsEvent event) {
        ItemStack stack = event.getStack();

        stack.addToTooltip(GTDataComponents.BINDING_DATA, event.getContext(),
                event::addTooltipLines, event.getContext().flag());
        stack.addToTooltip(GTDataComponents.COMPUTER_MONITOR_CONFIG, event.getContext(),
                event::addTooltipLines, event.getContext().flag());

        if (stack.has(GTDataComponents.MONITOR_TARGET)) {
            GlobalPosWithRot target = stack.get(GTDataComponents.MONITOR_TARGET);
            BlockPos pos = target.pos();
            event.addTooltipLines(Component.translatable(
                    "gtceu.tooltip.wireless_transmitter_bind",
                    Component.literal("" + pos.getX()).withStyle(ChatFormatting.GOLD),
                    Component.literal("" + pos.getY()).withStyle(ChatFormatting.GOLD),
                    Component.literal("" + pos.getZ()).withStyle(ChatFormatting.GOLD),
                    Component.translatable("gtceu.direction.tooltip." + target.side().getName())
                            .withStyle(ChatFormatting.DARK_PURPLE),
                    Component.translatable(target.dimension().location().toLanguageKey(Level.TRANSLATION_PREFIX))
                            .withStyle(ChatFormatting.DARK_PURPLE)));
        }
        if (!stack.has(GTDataComponents.RESEARCH_ITEM) && stack.has(GTDataComponents.DATA_COPY_POS)) {
            BlockPos pos = stack.get(GTDataComponents.DATA_COPY_POS);
            event.addTooltipLines(Component.translatable("gtceu.tooltip.proxy_bind",
                    Component.literal("" + pos.getX()).withStyle(ChatFormatting.LIGHT_PURPLE),
                    Component.literal("" + pos.getY()).withStyle(ChatFormatting.LIGHT_PURPLE),
                    Component.literal("" + pos.getZ()).withStyle(ChatFormatting.LIGHT_PURPLE)));
        }
        if (stack.has(GTDataComponents.COMPUTER_MONITOR_DATA)) {
            FormatStringList list = stack.getOrDefault(GTDataComponents.COMPUTER_MONITOR_DATA, FormatStringList.EMPTY);
            event.addTooltipLines(Component.translatable("gtceu.tooltip.computer_monitor_data",
                    GTStringUtils.toCompactedComponent(list.lines())));
        }
        if (!stack.has(GTDataComponents.DATA_COPY_POS)) {
            stack.addToTooltip(GTDataComponents.RESEARCH_ITEM, event.getContext(),
                    event::addTooltipLines, event.getContext().flag());
        }
    }

    @SubscribeEvent
    public static void breakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        for (ItemStack stack : player.getArmorSlots()) {
            if (stack.getItem() instanceof ArmorComponentItem componentItem) {
                if (componentItem.getArmorLogic() instanceof IJetpack jetpack && jetpack.removeMiningSpeedPenalty()) {
                    if (!player.onGround() || player.isUnderWater()) event.setNewSpeed(event.getOriginalSpeed() * 5);
                }
            }
        }
    }

    @SubscribeEvent
    public static void playerTickEvent(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide) {
            var speedAttrib = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttrib == null) return;
            var speedMod = speedAttrib.getModifier(GTAttributeModifierIds.BLOCK_SPEED_BOOST);

            float speedBoost = 0.0f;
            if (player.onGround() && !player.isInWater() && !player.isCrouching()) {
                BlockState state = player.level().getBlockState(player.getOnPos());
                if (state.is(CustomTags.VERY_FAST_WALKABLE_BLOCKS)) {
                    speedBoost = 0.6f; // value that is added to the base MC speed
                } else if (state.is(CustomTags.FAST_WALKABLE_BLOCKS)) {
                    speedBoost = 0.25f; // slower to walk on studs
                } else if (state.is(CustomTags.SLOW_WALKABLE_BLOCKS)) {
                    speedBoost = -0.20f; // slower on frames
                }
            }
            if (speedMod != null) {
                if (speedBoost == speedMod.amount()) {
                    return;
                } else {
                    speedAttrib.removeModifier(speedMod);
                }
            }
            if (speedBoost == 0.0f) {
                return;
            }
            speedAttrib.addOrUpdateTransientModifier(
                    new AttributeModifier(GTAttributeModifierIds.BLOCK_SPEED_BOOST,
                            speedBoost, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }
}
