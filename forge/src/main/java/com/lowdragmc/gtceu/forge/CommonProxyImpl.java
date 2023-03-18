package com.lowdragmc.gtceu.forge;

import com.lowdragmc.gtceu.GTCEu;
import com.lowdragmc.gtceu.api.GTValues;
import com.lowdragmc.gtceu.api.capability.forge.GTCapabilities;
import com.lowdragmc.gtceu.api.recipe.ingredient.SizedIngredient;
import com.lowdragmc.gtceu.api.recipe.ingredient.forge.SizedIngredientImpl;
import com.lowdragmc.gtceu.api.registry.GTRegistries;
import com.lowdragmc.gtceu.common.CommonProxy;
import com.lowdragmc.gtceu.common.libs.GTSyncedFieldAccessors;
import com.lowdragmc.gtceu.integration.top.forge.TheOneProbePluginImpl;
import com.lowdragmc.lowdraglib.LDLib;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class CommonProxyImpl {

    public CommonProxyImpl() {
        // used for forge events (ClientProxy + CommonProxy)
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.register(this);

        GTRegistries.REGISTRATE.registerRegistrate();
        // init common features
        CommonProxy.init();
        // register payloads
        GTSyncedFieldAccessors.init();
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
        GTCapabilities.register(event);
    }

}
