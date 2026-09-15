package com.gregtechceu.gtceu.client;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GTCEu.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModClientEventListener {

    @SubscribeEvent
    public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new GuiSpriteManager(Minecraft.getInstance().textureManager));
    }
}
