package com.gregtechceu.gtceu.common;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.gui.CoverUIFactory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.api.gui.MachineUIFactory;
import com.gregtechceu.gtceu.common.data.materials.GTFoods;
import com.gregtechceu.gtceu.core.mixins.IRegistryAccessor;
import com.gregtechceu.gtceu.data.data.GregTechDatagen;
import com.gregtechceu.gtceu.integration.kjs.registrymirror.GTRLRegistryWrapper;
import com.gregtechceu.gtceu.integration.kjs.registrymirror.GTStringRegistryWrapper;
import com.lowdragmc.lowdraglib.gui.factory.UIFactory;
import com.mojang.serialization.Lifecycle;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.latvian.mods.kubejs.KubeJSRegistries;

/**
 * @author KilaBash
 * @date 2023/2/13
 * @implNote CommonProxy
 */
public class CommonProxy {

    /**
     * If kjs is loaded, make sure our mod is loaded after it. {@link com.gregtechceu.gtceu.core.mixins.kjs.KubeJSMixin}
     */
    @ExpectPlatform
    public static void onKubeJSSetup() {
        throw new AssertionError();
    }

    public static void initRegistries() {
        if (GTCEu.isKubeJSLoaded()) {
            GTRegistries.ELEMENTS_WRAPPED = IRegistryAccessor.invokeInternalRegister(GTRegistries.ELEMENTS_REGISTRY, new GTStringRegistryWrapper<>(GTRegistries.ELEMENTS_REGISTRY, Lifecycle.experimental(), GTRegistries.ELEMENTS), val -> GTElements.Nt, Lifecycle.experimental());
            GTRegistries.MATERIALS_WRAPPED = IRegistryAccessor.invokeInternalRegister(GTRegistries.MATERIALS_REGISTRY, new GTStringRegistryWrapper<>(GTRegistries.MATERIALS_REGISTRY, Lifecycle.experimental(), GTRegistries.MATERIALS), val -> GTMaterials.Neutronium, Lifecycle.experimental());

            //GTRegistries.MACHINES_WRAPPED = IRegistryAccessor.invokeInternalRegister(GTRegistries.MACHINES_REGISTRY, new GTRLRegistryWrapper<>(GTRegistries.MACHINES_REGISTRY, Lifecycle.experimental(), GTRegistries.MACHINES), val -> GTMachines.ELECTRIC_FURNACE[0], Lifecycle.experimental());
        }
    }

    public static void init() {
        GTCEu.LOGGER.info("GTCEu common proxy init!");
        UIFactory.register(MachineUIFactory.INSTANCE);
        UIFactory.register(CoverUIFactory.INSTANCE);
        GTPlacerTypes.init();
        GTRecipeCapabilities.init();
        GTRecipeConditions.init();
        GTElements.init();
        GTMaterials.init();
        GTSoundEntries.init();
        GTCovers.init();
        GTFluids.init();
        GTBlocks.init();
        GTBlockEntities.init();
        GTRecipeTypes.init();
        GTMachines.init();
        GTFoods.init();
        GTItems.init();
        GTRecipes.init();
        GregTechDatagen.init();

        //if (!GTCEu.isKubeJSLoaded()) {
            register();
        //}
    }

    public static void register() {
        // fabric exclusive, squeeze this in here to register before stuff is used
        GTRegistries.REGISTRATE.registerRegistrate();
        GTOres.init();
        GTFeatures.init();
    }
}
