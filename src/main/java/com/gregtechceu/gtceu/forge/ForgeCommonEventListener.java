package com.gregtechceu.gtceu.forge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.common.ServerCommands;
import com.gregtechceu.gtceu.data.GTBlocks;
import com.gregtechceu.gtceu.data.GTItems;
import com.gregtechceu.gtceu.data.GTRecipes;
import com.gregtechceu.gtceu.data.loader.BedrockOreLoader;
import com.gregtechceu.gtceu.data.loader.FluidVeinLoader;
import com.gregtechceu.gtceu.data.loader.OreDataLoader;
import com.gregtechceu.gtceu.data.pack.GTDynamicDataPack;
import com.gregtechceu.gtceu.utils.TaskHandler;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.jetbrains.annotations.Nullable;

/**
 * @author KilaBash
 * @date 2022/8/27
 * @implNote ForgeCommonEventListener
 */
@EventBusSubscriber(modid = GTCEu.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ForgeCommonEventListener {

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
}
