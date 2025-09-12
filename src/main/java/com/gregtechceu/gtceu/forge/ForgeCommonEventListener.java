package com.gregtechceu.gtceu.forge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.capability.IMedicalConditionTracker;
import com.gregtechceu.gtceu.api.capability.compat.EUToFEProvider;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.cosmetics.CapeRegistry;
import com.gregtechceu.gtceu.api.cosmetics.event.RegisterGTCapesEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.misc.virtualregistry.VirtualEnderRegistry;
import com.gregtechceu.gtceu.api.pattern.MultiblockWorldSavedData;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.capability.EnvironmentalHazardSavedData;
import com.gregtechceu.gtceu.common.capability.LocalizedHazardSavedData;
import com.gregtechceu.gtceu.common.capability.MedicalConditionTracker;
import com.gregtechceu.gtceu.common.capability.WorldIDSaveData;
import com.gregtechceu.gtceu.common.commands.GTCommands;
import com.gregtechceu.gtceu.common.commands.HazardCommands;
import com.gregtechceu.gtceu.common.commands.MedicalConditionCommands;
import com.gregtechceu.gtceu.common.cosmetics.GTCapes;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines;
import com.gregtechceu.gtceu.common.fluid.potion.BottleItemFluidHandler;
import com.gregtechceu.gtceu.common.fluid.potion.PotionItemFluidHandler;
import com.gregtechceu.gtceu.common.item.ToggleEnergyConsumerBehavior;
import com.gregtechceu.gtceu.common.item.armor.IJetpack;
import com.gregtechceu.gtceu.common.item.armor.QuarkTechSuite;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.packets.SPacketSendWorldID;
import com.gregtechceu.gtceu.common.network.packets.SPacketSyncBedrockOreVeins;
import com.gregtechceu.gtceu.common.network.packets.SPacketSyncFluidVeins;
import com.gregtechceu.gtceu.common.network.packets.SPacketSyncOreVeins;
import com.gregtechceu.gtceu.common.network.packets.hazard.SPacketAddHazardZone;
import com.gregtechceu.gtceu.common.network.packets.hazard.SPacketRemoveHazardZone;
import com.gregtechceu.gtceu.common.network.packets.hazard.SPacketSyncLevelHazards;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.loader.BedrockFluidLoader;
import com.gregtechceu.gtceu.data.loader.BedrockOreLoader;
import com.gregtechceu.gtceu.data.loader.GTOreLoader;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.integration.map.ClientCacheManager;
import com.gregtechceu.gtceu.integration.map.WaypointManager;
import com.gregtechceu.gtceu.integration.map.cache.server.ServerCache;
import com.gregtechceu.gtceu.utils.TaskHandler;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.*;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.MissingMappingsEvent;

import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gregtechceu.gtceu.utils.FormattingUtil.toLowerCaseUnderscore;

@Mod.EventBusSubscriber(modid = GTCEu.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeCommonEventListener {

    @SubscribeEvent
    public static void registerItemStackCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        final ItemStack itemStack = event.getObject();
        if (itemStack.getItem() instanceof PotionItem) {
            event.addCapability(GTCEu.id("potion_item_handler"), new PotionItemFluidHandler(itemStack));
        } else if (itemStack.is(Items.GLASS_BOTTLE)) {
            event.addCapability(GTCEu.id("bottle_item_handler"), new BottleItemFluidHandler(itemStack));
        }
    }

    @SubscribeEvent
    public static void registerEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player entity) {
            final MedicalConditionTracker tracker = new MedicalConditionTracker(entity);
            event.addCapability(GTCEu.id("medical_condition_tracker"), new ICapabilitySerializable<CompoundTag>() {

                @Override
                public CompoundTag serializeNBT() {
                    return tracker.serializeNBT();
                }

                @Override
                public void deserializeNBT(CompoundTag arg) {
                    tracker.deserializeNBT(arg);
                }

                @Override
                public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability,
                                                                  @Nullable Direction arg) {
                    return GTCapability.CAPABILITY_MEDICAL_CONDITION_TRACKER.orEmpty(capability,
                            LazyOptional.of(() -> tracker));
                }
            });
        }
    }

    @SubscribeEvent
    public static void registerBlockEntityCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        event.addCapability(GTCEu.id("fe_capability"), new EUToFEProvider(event.getObject()));
    }

    @SubscribeEvent
    public static void registerCapes(RegisterGTCapesEvent event) {
        GTCapes.registerGTCapes(event);
        GTCapes.giveDevCapes(event);
    }

    @SubscribeEvent
    public static void tickPlayerInventoryHazards(TickEvent.PlayerTickEvent event) {
        if (event.side == LogicalSide.CLIENT || event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;
        IMedicalConditionTracker tracker = GTCapabilityHelper.getMedicalConditionTracker(player);
        if (tracker == null) {
            return;
        }
        if (!ConfigHolder.INSTANCE.gameplay.hazardsEnabled) {
            for (MedicalCondition medicalCondition : tracker.getMedicalConditions().keySet()) {
                tracker.removeMedicalCondition(medicalCondition);
            }
            return;
        }

        IItemHandler inventory = player.getCapability(ForgeCapabilities.ITEM_HANDLER, null).resolve().orElse(null);
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
            if (item.is(GTItems.QUANTUM_HELMET.asItem()) && GTCapabilityHelper.getElectricItem(item) != null) {
                IElectricItem helmet = GTCapabilityHelper.getElectricItem(item);
                MobEffectInstance effect = event.getEffectInstance();
                int cost = QuarkTechSuite.potionRemovalCost.getOrDefault(effect.getEffect(), -1);
                if (cost != -1) {
                    cost = cost * (effect.getAmplifier() + 1);
                    if (helmet.canUse(cost)) {
                        helmet.discharge(cost, helmet.getTier(), true, false, false);
                        event.setResult(Event.Result.DENY);
                    }
                }
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

        event.addListener(new GTOreLoader());
        event.addListener(new BedrockFluidLoader());
        event.addListener(new BedrockOreLoader());
    }

    @SubscribeEvent
    public static void levelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel serverLevel) {
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
            ClientCacheManager.saveCaches();
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
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer) {
            GTNetwork.sendToPlayer(serverPlayer, new SPacketSendWorldID());

            if (!ConfigHolder.INSTANCE.gameplay.environmentalHazards)
                return;

            ServerLevel level = serverPlayer.serverLevel();
            var data = EnvironmentalHazardSavedData.getOrCreate(level);
            GTNetwork.sendToPlayer(serverPlayer, new SPacketSyncLevelHazards(data.getHazardZones()));
        }
        CapeRegistry.detectNewCapes(player);
        CapeRegistry.loadCurrentCapesOnLogin(player);
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        ServerPlayer player = event.getPlayer();
        if (player == null) {
            // if player == null, the /reload command was ran. sync to all players.
            GTNetwork.sendToAll(new SPacketSyncOreVeins(GTRegistries.ORE_VEINS.registry()));
            GTNetwork.sendToAll(new SPacketSyncFluidVeins(GTRegistries.BEDROCK_FLUID_DEFINITIONS.registry()));
            GTNetwork.sendToAll(new SPacketSyncBedrockOreVeins(GTRegistries.BEDROCK_ORE_DEFINITIONS.registry()));
        } else {
            // else it's a player logging in. sync to only that player.
            GTNetwork.sendToPlayer(player, new SPacketSyncOreVeins(GTRegistries.ORE_VEINS.registry()));
            GTNetwork.sendToPlayer(player,
                    new SPacketSyncFluidVeins(GTRegistries.BEDROCK_FLUID_DEFINITIONS.registry()));
            GTNetwork.sendToPlayer(player,
                    new SPacketSyncBedrockOreVeins(GTRegistries.BEDROCK_ORE_DEFINITIONS.registry()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onEntityLivingFallEvent(LivingFallEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.fallDistance < 3.2f)
                return;

            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);

            if (boots.is(CustomTags.STEP_BOOTS) && boots.getItem() instanceof ArmorComponentItem armor) {
                armor.getArmorLogic().damageArmor(player, boots, player.damageSources().fall(),
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
    public static void stepAssistHandler(LivingEvent.LivingTickEvent event) {
        float MAGIC_STEP_HEIGHT = 1.0023f;
        if (event.getEntity() == null || !(event.getEntity() instanceof Player player)) return;
        if (!player.isCrouching() && player.getItemBySlot(EquipmentSlot.FEET).is(CustomTags.STEP_BOOTS)) {
            if (player.getStepHeight() < MAGIC_STEP_HEIGHT) {
                player.setMaxUpStep(MAGIC_STEP_HEIGHT);
            }
        } else if (player.getStepHeight() == MAGIC_STEP_HEIGHT) {
            player.setMaxUpStep(0.6f);
        }
    }

    @SubscribeEvent
    public static void onEntityDie(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            IMedicalConditionTracker tracker = GTCapabilityHelper.getMedicalConditionTracker(player);
            if (tracker == null) {
                return;
            }
            for (MedicalCondition condition : tracker.getMedicalConditions().keySet()) {
                tracker.removeMedicalCondition(condition);
            }
        }
    }

    @SubscribeEvent
    public static void onEntitySpawn(MobSpawnEvent.FinalizeSpawn event) {
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
        GTNetwork.sendToPlayer((ServerPlayer) event.getEntity(), new SPacketSyncLevelHazards(data.getHazardZones()));
    }

    @SubscribeEvent
    public static void onChunkWatch(ChunkWatchEvent.Watch event) {
        ChunkPos pos = event.getPos();
        ServerPlayer player = event.getPlayer();
        var data = EnvironmentalHazardSavedData.getOrCreate(event.getLevel());

        var zone = data.getZoneByPos(pos);
        if (zone != null) {
            GTNetwork.sendToPlayer(player, new SPacketAddHazardZone(pos, zone));
        }
    }

    @SubscribeEvent
    public static void onChunkUnWatch(ChunkWatchEvent.UnWatch event) {
        ChunkPos pos = event.getPos();
        ServerPlayer player = event.getPlayer();
        var data = EnvironmentalHazardSavedData.getOrCreate(event.getLevel());

        var zone = data.getZoneByPos(pos);
        if (zone != null) {
            GTNetwork.sendToPlayer(player, new SPacketRemoveHazardZone(pos));
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
    public static void remapIds(MissingMappingsEvent event) {
        event.getMappings(Registries.BLOCK, GTCEu.MOD_ID).forEach(mapping -> {
            if (mapping.getKey().equals(GTCEu.id("tungstensteel_coil_block"))) {
                mapping.remap(GTBlocks.COIL_RTMALLOY.get());
            }
            if (mapping.getKey().equals(GTCEu.id("steam_miner"))) {
                mapping.remap(GTMachines.STEAM_MINER.first().getBlock());
            }
        });
        event.getMappings(Registries.ITEM, GTCEu.MOD_ID).forEach(mapping -> {
            if (mapping.getKey().equals(GTCEu.id("tungstensteel_coil_block"))) {
                mapping.remap(GTBlocks.COIL_RTMALLOY.get().asItem());
            }
            if (mapping.getKey().equals(GTCEu.id("steam_miner"))) {
                mapping.remap(GTMachines.STEAM_MINER.first().getItem());
            }
            if (mapping.getKey().equals(GTCEu.id("tungstensteel_fluid_cell"))) {
                mapping.remap(GTItems.FLUID_CELL_LARGE_TUNGSTEN_STEEL.get().asItem());
            }
            if (mapping.getKey().equals(GTCEu.id("avanced_nanomuscle_chestplate"))) {
                mapping.remap(GTItems.NANO_CHESTPLATE_ADVANCED.get());
            }
            String path = mapping.getKey().getPath();
            if (path.matches("[lhi]v_.+_wirecutter")) {
                String suffix = "_wirecutter";
                String typeString = path.substring(0, 2) + suffix; // [lhi]v_wirecutter -- tooltype name
                String matString = path.substring(3, path.length() - suffix.length()); // material name

                GTToolType type = GTToolType.getTypes().get(typeString);
                Material material = GTMaterials.get(matString);
                if (type == null || material.isNull()) {
                    mapping.warn();
                    return;
                }
                var tool = GTMaterialItems.TOOL_ITEMS.get(material, type);
                if (tool == null) {
                    mapping.warn();
                    return;
                }
                mapping.remap(tool.asItem());
            }
        });
        event.getMappings(Registries.BLOCK_ENTITY_TYPE, GTCEu.MOD_ID).forEach(mapping -> {
            if (mapping.getKey().equals(GTCEu.id("steam_miner"))) {
                mapping.remap(GTMachines.STEAM_MINER.first().getBlockEntityType());
            }
        });

        event.getMappings(Registries.BLOCK, "gregiceng").forEach(mapping -> {
            String path = mapping.getKey().getPath();
            switch (path) {
                case "stocking_bus", "adv_stocking_bus" -> mapping
                        .remap(GTAEMachines.STOCKING_IMPORT_BUS_ME.getBlock());
                case "stocking_hatch", "adv_stocking_hatch" -> mapping
                        .remap(GTAEMachines.STOCKING_IMPORT_HATCH_ME.getBlock());
                case "crafting_io_buffer" -> mapping.remap(GTAEMachines.ME_PATTERN_BUFFER.getBlock());
                case "crafting_io_slave" -> mapping.remap(GTAEMachines.ME_PATTERN_BUFFER_PROXY.getBlock());
            }
            if (path.contains("input_buffer")) {
                ResourceLocation newName = GTCEu.id(path.replace("input_buffer", "dual_input_hatch"));
                if (mapping.getRegistry().containsKey(newName)) {
                    mapping.remap(mapping.getRegistry().getValue(newName));
                } else {
                    mapping.remap(GTMachines.DUAL_IMPORT_HATCH[GTValues.LuV].getBlock());
                }
            } else if (path.contains("output_buffer")) {
                ResourceLocation newName = GTCEu.id(path.replace("output_buffer", "dual_output_hatch"));
                if (mapping.getRegistry().containsKey(newName)) {
                    mapping.remap(mapping.getRegistry().getValue(newName));
                } else {
                    mapping.remap(GTMachines.DUAL_EXPORT_HATCH[GTValues.LuV].getBlock());
                }
            }
        });
        event.getMappings(Registries.BLOCK_ENTITY_TYPE, "gregiceng").forEach(mapping -> {
            String path = mapping.getKey().getPath();
            switch (path) {
                case "stocking_bus", "adv_stocking_bus" -> mapping
                        .remap(GTAEMachines.STOCKING_IMPORT_BUS_ME.getBlockEntityType());
                case "stocking_hatch", "adv_stocking_hatch" -> mapping
                        .remap(GTAEMachines.STOCKING_IMPORT_HATCH_ME.getBlockEntityType());
                case "crafting_io_buffer" -> mapping.remap(GTAEMachines.ME_PATTERN_BUFFER.getBlockEntityType());
                case "crafting_io_slave" -> mapping.remap(GTAEMachines.ME_PATTERN_BUFFER_PROXY.getBlockEntityType());
            }
            if (path.contains("input_buffer")) {
                ResourceLocation newName = GTCEu.id(path.replace("input_buffer", "dual_input_hatch"));
                if (mapping.getRegistry().containsKey(newName)) {
                    mapping.remap(mapping.getRegistry().getValue(newName));
                } else {
                    mapping.remap(GTMachines.DUAL_IMPORT_HATCH[GTValues.LuV].getBlockEntityType());
                }
            } else if (path.contains("output_buffer")) {
                ResourceLocation newName = GTCEu.id(path.replace("output_buffer", "dual_output_hatch"));
                if (mapping.getRegistry().containsKey(newName)) {
                    mapping.remap(mapping.getRegistry().getValue(newName));
                } else {
                    mapping.remap(GTMachines.DUAL_EXPORT_HATCH[GTValues.LuV].getBlockEntityType());
                }
            }
        });
        event.getMappings(Registries.ITEM, "gregiceng").forEach(mapping -> {
            String path = mapping.getKey().getPath();
            switch (path) {
                case "stocking_bus", "adv_stocking_bus" -> mapping.remap(GTAEMachines.STOCKING_IMPORT_BUS_ME.getItem());
                case "stocking_hatch", "adv_stocking_hatch" -> mapping
                        .remap(GTAEMachines.STOCKING_IMPORT_HATCH_ME.getItem());
                case "crafting_io_buffer" -> mapping.remap(GTAEMachines.ME_PATTERN_BUFFER.getItem());
                case "crafting_io_slave" -> mapping.remap(GTAEMachines.ME_PATTERN_BUFFER_PROXY.getItem());
            }
            if (path.contains("input_buffer")) {
                ResourceLocation newName = GTCEu.id(path.replace("input_buffer", "dual_input_hatch"));
                if (mapping.getRegistry().containsKey(newName)) {
                    mapping.remap(mapping.getRegistry().getValue(newName));
                } else {
                    mapping.remap(GTMachines.DUAL_IMPORT_HATCH[GTValues.LuV].getItem());
                }
            } else if (path.contains("output_buffer")) {
                ResourceLocation newName = GTCEu.id(path.replace("output_buffer", "dual_output_hatch"));
                if (mapping.getRegistry().containsKey(newName)) {
                    mapping.remap(mapping.getRegistry().getValue(newName));
                } else {
                    mapping.remap(GTMachines.DUAL_EXPORT_HATCH[GTValues.LuV].getItem());
                }
            }
        });

        for (TagPrefix prefix : TagPrefix.values()) {
            String first = prefix.invertedName ? toLowerCaseUnderscore(prefix.name) : "(.+?)";
            String last = prefix.invertedName ? "(.+?)" : toLowerCaseUnderscore(prefix.name);
            Pattern idPattern = Pattern.compile(first + "_" + last);
            event.getMappings(Registries.BLOCK, GTCEu.MOD_ID).forEach(mapping -> {
                Matcher matcher = idPattern.matcher(mapping.getKey().getPath());
                if (matcher.matches()) {
                    BlockEntry<? extends Block> block = GTMaterialBlocks.MATERIAL_BLOCKS.get(prefix,
                            GTCEuAPI.materialManager.getRegistry(GTCEu.MOD_ID).get(matcher.group(1)));
                    if (block != null && block.isPresent()) {
                        mapping.remap(block.get());
                    }
                }
            });
            event.getMappings(Registries.ITEM, GTCEu.MOD_ID).forEach(mapping -> {
                Matcher matcher = idPattern.matcher(mapping.getKey().getPath());
                if (matcher.matches()) {
                    BlockEntry<? extends Block> block = GTMaterialBlocks.MATERIAL_BLOCKS.get(prefix,
                            GTCEuAPI.materialManager.getRegistry(GTCEu.MOD_ID).get(matcher.group(1)));
                    if (block != null && block.isPresent()) {
                        mapping.remap(block.asItem());
                    } else {
                        ItemEntry<? extends Item> item = GTMaterialItems.MATERIAL_ITEMS.get(prefix,
                                GTCEuAPI.materialManager.getRegistry(GTCEu.MOD_ID).get(matcher.group(1)));
                        if (item != null && item.isPresent()) {
                            mapping.remap(item.asItem());
                        }
                    }
                }
            });
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
}
