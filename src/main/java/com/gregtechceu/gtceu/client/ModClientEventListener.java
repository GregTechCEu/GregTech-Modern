package com.gregtechceu.gtceu.client;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

@EventBusSubscriber(modid = GTCEu.MOD_ID, value = Dist.CLIENT)
public class ModClientEventListener {

    @SubscribeEvent
    public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new GuiSpriteManager(Minecraft.getInstance().getTextureManager()));
    }
}
