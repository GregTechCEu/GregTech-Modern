package com.gregtechceu.gtceu.forge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.capability.forge.compat.EUToFEProvider;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.DrumMachineItem;
import com.gregtechceu.gtceu.api.item.TagPrefixItem;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.common.ServerCommands;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.data.loader.forge.FluidVeinLoaderImpl;
import com.gregtechceu.gtceu.data.loader.forge.OreDataLoaderImpl;
import com.gregtechceu.gtceu.utils.TaskHandler;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.MissingMappingsEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gregtechceu.gtceu.utils.FormattingUtil.toLowerCaseUnder;

/**
 * @author KilaBash
 * @date 2022/8/27
 * @implNote ForgeCommonEventListener
 */
@Mod.EventBusSubscriber(modid = GTCEu.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeCommonEventListener {

    @SubscribeEvent
    public static void registerItemStackCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        if (event.getObject().getItem() instanceof ComponentItem componentItem) {

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
                public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction arg) {
                    return drumMachineItem.getCapability(itemStack, capability);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        var blockState = event.getLevel().getBlockState(event.getPos());
        if (blockState.hasBlockEntity() && blockState.getBlock() instanceof MetaMachineBlock block
                && block.getMachine(event.getLevel(), event.getPos()) instanceof IInteractedMachine machine) {
            if (machine.onLeftClick(event.getEntity(), event.getLevel(), event.getHand(), event.getPos(), event.getFace())) {
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
        event.addListener(new OreDataLoaderImpl());
        event.addListener(new FluidVeinLoaderImpl());
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        event.addCapability(GTCEu.id("fe_capability"), new EUToFEProvider(event.getObject()));
    }

    @SubscribeEvent
    public static void levelTick(TickEvent.LevelTickEvent event) {
        if (event.level instanceof ServerLevel serverLevel && event.phase.equals(TickEvent.Phase.END)) {
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
    public static void remapIds(MissingMappingsEvent event) {
        event.getMappings(Registry.BLOCK.key(), GTCEu.MOD_ID).forEach(mapping -> {
            if (mapping.getKey().equals(GTCEu.id("tungstensteel_coil_block"))) {
                mapping.remap(GTBlocks.COIL_RTMALLOY.get());
            }
        });
        event.getMappings(Registry.ITEM.key(), GTCEu.MOD_ID).forEach(mapping -> {
            if (mapping.getKey().equals(GTCEu.id("tungstensteel_coil_block"))) {
                mapping.remap(GTBlocks.COIL_RTMALLOY.get().asItem());
            }
        });

        for (TagPrefix prefix : TagPrefix.values()) {
            String first = prefix.invertedName ? toLowerCaseUnder(prefix.name) : "(.+?)";
            String last = prefix.invertedName ? "(.+?)" : toLowerCaseUnder(prefix.name);
            Pattern idPattern = Pattern.compile(first + "_" + last);
            event.getMappings(Registry.BLOCK_REGISTRY, GTCEu.MOD_ID).forEach(mapping -> {
                Matcher matcher = idPattern.matcher(mapping.getKey().getPath());
                if (matcher.matches()) {
                    BlockEntry<? extends MaterialBlock> block = GTBlocks.MATERIAL_BLOCKS.get(prefix, GTCEuAPI.materialManager.getRegistry(GTCEu.MOD_ID).get(matcher.group(1)));
                    if (block != null && block.isPresent()) {
                        mapping.remap(block.get());
                    }
                }
            });
            event.getMappings(Registry.ITEM_REGISTRY, GTCEu.MOD_ID).forEach(mapping -> {
                Matcher matcher = idPattern.matcher(mapping.getKey().getPath());
                if (matcher.matches()) {
                    BlockEntry<? extends MaterialBlock> block = GTBlocks.MATERIAL_BLOCKS.get(prefix, GTCEuAPI.materialManager.getRegistry(GTCEu.MOD_ID).get(matcher.group(1)));
                    if (block != null && block.isPresent()) {
                        mapping.remap(block.get().asItem());
                    } else {
                        ItemEntry<? extends TagPrefixItem> item = GTItems.MATERIAL_ITEMS.get(prefix, GTCEuAPI.materialManager.getRegistry(GTCEu.MOD_ID).get(matcher.group(1)));
                        if (item != null && item.isPresent()) {
                            mapping.remap(item.get().asItem());
                        }
                    }
                }
            });
        }
    }
}
