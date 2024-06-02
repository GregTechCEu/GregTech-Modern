package com.gregtechceu.gtceu.client.forge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.ClientCommands;
import com.gregtechceu.gtceu.client.TooltipHelper;
import com.gregtechceu.gtceu.client.TooltipsHandler;
import com.gregtechceu.gtceu.client.renderer.BlockHighLightRenderer;
import com.gregtechceu.gtceu.data.block.GTOres;
import com.gregtechceu.gtceu.data.fluid.GTBedrockFluids;

import net.minecraft.commands.CommandSourceStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import java.util.List;

/**
 * @author KilaBash
 * @date 2022/8/27
 * @implNote ForgeCommonEventListener
 */
@EventBusSubscriber(modid = GTCEu.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class ForgeClientEventListener {

    @SubscribeEvent
    public static void registerItemStackCapabilities(RenderHighlightEvent.Block event) {
        BlockHighLightRenderer.renderBlockHighLight(event.getPoseStack(), event.getCamera(), event.getTarget(),
                event.getMultiBufferSource(), event.getPartialTick());
    }

    @SubscribeEvent
    public static void onTooltipEvent(ItemTooltipEvent event) {
        TooltipsHandler.appendTooltips(event.getItemStack(), event.getFlags(), event.getToolTip());
    }

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        var dispatcher = event.getDispatcher();
        List<LiteralArgumentBuilder<CommandSourceStack>> commands = ClientCommands.createClientCommands();
        commands.forEach(dispatcher::register);
    }

    @SubscribeEvent
    public static void onClientTickEvent(ClientTickEvent.Post event) {
        TooltipHelper.onClientTick();
        GTValues.CLIENT_TIME++;
    }

    @SubscribeEvent
    public static void onClientLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        if (GTRegistries.ORE_VEINS.isFrozen()) {
            GTRegistries.ORE_VEINS.unfreeze();
        }
        GTOres.init();
        if (!GTRegistries.ORE_VEINS.isFrozen()) {
            GTRegistries.ORE_VEINS.freeze();
        }

        if (GTRegistries.BEDROCK_FLUID_DEFINITIONS.isFrozen()) {
            GTRegistries.BEDROCK_FLUID_DEFINITIONS.unfreeze();
        }
        GTBedrockFluids.init();
        if (!GTRegistries.BEDROCK_FLUID_DEFINITIONS.isFrozen()) {
            GTRegistries.BEDROCK_FLUID_DEFINITIONS.freeze();
        }

        if (GTRegistries.BEDROCK_ORE_DEFINITIONS.isFrozen()) {
            GTRegistries.BEDROCK_ORE_DEFINITIONS.unfreeze();
        }
        GTOres.toReRegisterBedrock.forEach(GTRegistries.BEDROCK_ORE_DEFINITIONS::registerOrOverride);
        if (!GTRegistries.BEDROCK_ORE_DEFINITIONS.isFrozen()) {
            GTRegistries.BEDROCK_ORE_DEFINITIONS.freeze();
        }
    }
}
