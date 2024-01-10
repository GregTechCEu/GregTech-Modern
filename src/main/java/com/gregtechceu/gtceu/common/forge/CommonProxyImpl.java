package com.gregtechceu.gtceu.common.forge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.registry.forge.GTRegistriesImpl;
import com.gregtechceu.gtceu.common.CommonProxy;
import com.gregtechceu.gtceu.common.data.forge.GTFeaturesImpl;
import com.gregtechceu.gtceu.common.item.tool.forge.ToolLootModifier;
import com.gregtechceu.gtceu.integration.top.forge.TheOneProbePluginImpl;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.forge.SizedIngredientImpl;
import com.lowdragmc.lowdraglib.LDLib;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class CommonProxyImpl {
    private static final Object LOCK = new Object();
    private static boolean isKubJSSetup = false;

    public static void onKubeJSSetup() {
        synchronized (LOCK) {
            isKubJSSetup = true;
            LOCK.notify();
        }
    }

    public CommonProxyImpl() {
        // used for forge events (ClientProxy + CommonProxy)
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.register(this);
        GTRegistriesImpl.init(eventBus);
        GTFeaturesImpl.init(eventBus);
        // init common features
        if (GTCEu.isKubeJSLoaded()) {
            synchronized (LOCK) {
                if (!isKubJSSetup) {
                    try { LOCK.wait(); } catch (InterruptedException ignored) {}
                }
            }
        }
        CommonProxy.init();
        GTRegistriesImpl.GLOBAL_LOOT_MODIFIES.register("tool", () -> ToolLootModifier.CODEC);
    }

    @SubscribeEvent
    public void commonSetup(FMLCommonSetupEvent e) {
        e.enqueueWork(() -> {

        });
        CraftingHelper.register(SizedIngredient.TYPE, SizedIngredientImpl.SERIALIZER);
    }

    @SubscribeEvent
    public void loadComplete(FMLLoadCompleteEvent e) {
        e.enqueueWork(() -> {
            if (LDLib.isModLoaded(GTValues.MODID_TOP)) {
                GTCEu.LOGGER.info("TheOneProbe found. Enabling integration...");
                TheOneProbePluginImpl.init();
            }
        });
    }

    @SubscribeEvent
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        GTCapability.register(event);
    }

}
