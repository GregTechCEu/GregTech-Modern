package com.gregtechceu.gtceu.client;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.GTMenuTypes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import brachy.modularui.screen.ContainerScreenWrapper;
import brachy.modularui.screen.ModularContainerMenu;

@Mod.EventBusSubscriber(modid = GTCEu.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModClientEventListener {

    @SubscribeEvent
    public static void registerScreens(FMLClientSetupEvent event) {
        // noinspection deprecation,RedundantCast
        event.enqueueWork(() -> MenuScreens.register(GTMenuTypes.MODULAR_CONTAINER.get(),
                (MenuScreens.ScreenConstructor<ModularContainerMenu, ContainerScreenWrapper>) ContainerScreenWrapper::new));
    }

    @SubscribeEvent
    public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new GuiSpriteManager(Minecraft.getInstance().textureManager));
    }
}
