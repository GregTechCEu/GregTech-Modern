package com.gregtechceu.gtceu.forge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IMedicalConditionTracker;
import com.gregtechceu.gtceu.api.capability.compat.EUToFEProvider;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.registry.MaterialRegistry;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.item.DrumMachineItem;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.capability.EnvironmentalHazardSavedData;
import com.gregtechceu.gtceu.common.capability.LocalizedHazardSavedData;
import com.gregtechceu.gtceu.common.capability.MedicalConditionTracker;
import com.gregtechceu.gtceu.common.commands.GTCommands;
import com.gregtechceu.gtceu.common.commands.HazardCommands;
import com.gregtechceu.gtceu.common.commands.MedicalConditionCommands;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTBlockEntities;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines;
import com.gregtechceu.gtceu.common.item.ToggleEnergyConsumerBehavior;
import com.gregtechceu.gtceu.common.item.armor.IJetpack;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.packets.*;
import com.gregtechceu.gtceu.common.network.packets.hazard.SPacketAddHazardZone;
import com.gregtechceu.gtceu.common.network.packets.hazard.SPacketRemoveHazardZone;
import com.gregtechceu.gtceu.common.network.packets.hazard.SPacketSyncLevelHazards;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.loader.BedrockFluidLoader;
import com.gregtechceu.gtceu.data.loader.BedrockOreLoader;
import com.gregtechceu.gtceu.data.loader.GTOreLoader;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.utils.TaskHandler;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.*;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.MissingMappingsEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author KilaBash
 * @date 2022/8/27
 * @implNote ForgeCommonEventListener
 */
@Mod.EventBusSubscriber(modid = GTCEu.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeCommonEventListener {

    @SubscribeEvent
    public static void registerItemStackCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        if (event.getObject().getItem() instanceof IComponentItem componentItem) {
            final ItemStack itemStack = event.getObject();
            event.addCapability(GTCEu.id("capability"), new ICapabilityProvider() {

                @NotNull
                @Override
                public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                    return componentItem.getCapability(itemStack, cap);
                }
            });
        }
        if (event.getObject().getItem() instanceof DrumMachineItem drumMachineItem) {
            final ItemStack itemStack = event.getObject();
            event.addCapability(GTCEu.id("fluid"), new ICapabilityProvider() {

                @Override
                public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability,
                                                                  @Nullable Direction arg) {
                    return drumMachineItem.getCapability(itemStack, capability);
                }
            });
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
    public static void attachCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        event.addCapability(GTCEu.id("fe_capability"), new EUToFEProvider(event.getObject()));
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
            if (material == null || !material.hasProperty(PropertyKey.HAZARD)) {
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
            if (!MetaMachineBlock.canBreakOwnerMachine(event.getPlayer(), machine.holder)) {
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
    public static void worldUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            TaskHandler.onWorldUnLoad(serverLevel);
        }
    }

    @SubscribeEvent
    public static void onPlayerJoinServer(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            if (!ConfigHolder.INSTANCE.gameplay.environmentalHazards)
                return;

            ServerLevel level = serverPlayer.serverLevel();
            var data = EnvironmentalHazardSavedData.getOrCreate(level);
            GTNetwork.NETWORK.sendToPlayer(new SPacketSyncLevelHazards(data.getHazardZones()), serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        ServerPlayer player = event.getPlayer();
        if (player == null) {
            // if player == null, the /reload command was ran. sync to all players.
            GTNetwork.NETWORK.sendToAll(new SPacketSyncOreVeins(GTRegistries.ORE_VEINS.registry()));
            GTNetwork.NETWORK.sendToAll(new SPacketSyncFluidVeins(GTRegistries.BEDROCK_FLUID_DEFINITIONS.registry()));
            GTNetwork.NETWORK
                    .sendToAll(new SPacketSyncBedrockOreVeins(GTRegistries.BEDROCK_ORE_DEFINITIONS.registry()));
        } else {
            // else it's a player logging in. sync to only that player.
            GTNetwork.NETWORK.sendToPlayer(new SPacketSyncOreVeins(GTRegistries.ORE_VEINS.registry()), player);
            GTNetwork.NETWORK.sendToPlayer(new SPacketSyncFluidVeins(GTRegistries.BEDROCK_FLUID_DEFINITIONS.registry()),
                    player);
            GTNetwork.NETWORK.sendToPlayer(
                    new SPacketSyncBedrockOreVeins(GTRegistries.BEDROCK_ORE_DEFINITIONS.registry()), player);
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
        GTNetwork.NETWORK.sendToPlayer(new SPacketSyncLevelHazards(data.getHazardZones()),
                (ServerPlayer) event.getEntity());
    }

    @SubscribeEvent
    public static void onChunkWatch(ChunkWatchEvent.Watch event) {
        ChunkPos pos = event.getPos();
        ServerPlayer player = event.getPlayer();
        var data = EnvironmentalHazardSavedData.getOrCreate(event.getLevel());

        var zone = data.getZoneByPos(pos);
        if (zone != null) {
            GTNetwork.NETWORK.sendToPlayer(new SPacketAddHazardZone(pos, zone), player);
        }
    }

    @SubscribeEvent
    public static void onChunkUnWatch(ChunkWatchEvent.UnWatch event) {
        ChunkPos pos = event.getPos();
        ServerPlayer player = event.getPlayer();
        var data = EnvironmentalHazardSavedData.getOrCreate(event.getLevel());

        var zone = data.getZoneByPos(pos);
        if (zone != null) {
            GTNetwork.NETWORK.sendToPlayer(new SPacketRemoveHazardZone(pos), player);
        }
    }

    @SubscribeEvent
    public static void remapIds(MissingMappingsEvent event) {
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

        // remap pipe blocks (uses the datafixer in 1.21)
        final Pattern itemPipe = Pattern.compile("(.+?)_(.+?)_item_pipe");
        final Pattern fluidPipe = Pattern.compile("(.+?)_(.+?)_fluid_pipe");
        for (MaterialRegistry registry : GTCEuAPI.materialManager.getRegistries()) {
            String namespace = registry.getModid();
            event.getMappings(Registries.BLOCK, namespace).forEach(mapping -> {
                remapPipe(itemPipe, mapping);
                remapPipe(fluidPipe, mapping);
            });
            event.getMappings(Registries.ITEM, namespace).forEach(mapping -> {
                remapPipe(itemPipe, mapping);
                remapPipe(fluidPipe, mapping);
            });
        }
        event.getMappings(Registries.BLOCK, GTCEu.MOD_ID).forEach(mapping -> {
            switch (mapping.getKey().getPath()) {
                case "normal_laser_pipe" -> mapping.remap(GTBlocks.LASER_PIPE.get());
                case "normal_optical_pipe" -> mapping.remap(GTBlocks.OPTICAL_PIPE.get());
            }
        });
        event.getMappings(Registries.ITEM, GTCEu.MOD_ID).forEach(mapping -> {
            switch (mapping.getKey().getPath()) {
                case "normal_laser_pipe" -> mapping.remap(GTBlocks.LASER_PIPE.get().asItem());
                case "normal_optical_pipe" -> mapping.remap(GTBlocks.OPTICAL_PIPE.get().asItem());
            }
        });
        event.getMappings(Registries.BLOCK_ENTITY_TYPE, GTCEu.MOD_ID).forEach(mapping -> {
            switch (mapping.getKey().getPath()) {
                case "cable", "fluid_pipe", "item_pipe" -> mapping.remap(GTBlockEntities.MATERIAL_PIPE.get());
                case "laser_pipe", "optical_pipe" -> mapping.remap(GTBlockEntities.ACTIVABLE_PIPE.get());
                case "duct_pipe" -> mapping.remap(GTBlockEntities.PIPE.get());
            }
        });
    }

    private static <T> void remapPipe(Pattern pattern, MissingMappingsEvent.Mapping<T> mapping) {
        String namespace = mapping.getKey().getNamespace();
        String path = mapping.getKey().getPath();
        Matcher match = pattern.matcher(path);
        if (match.matches()) {
            ResourceLocation newBlock = new ResourceLocation(namespace, match.replaceAll("$2_$1_pipe"));
            mapping.remap(mapping.getRegistry().getValue(newBlock));
        }
    }
}
