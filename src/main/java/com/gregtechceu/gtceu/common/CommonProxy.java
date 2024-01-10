package com.gregtechceu.gtceu.common;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGenLayers;
import com.gregtechceu.gtceu.api.gui.factory.CoverUIFactory;
import com.gregtechceu.gtceu.api.gui.factory.GTUIEditorFactory;
import com.gregtechceu.gtceu.api.gui.factory.MachineUIFactory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.common.data.materials.GTFoods;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.GregTechDatagen;
import com.lowdragmc.lowdraglib.gui.factory.UIFactory;
import dev.architectury.injectables.annotations.ExpectPlatform;

public class CommonProxy {

    /**
     * If kjs is loaded, make sure our mod is loaded after it. {@link com.gregtechceu.gtceu.core.mixins.kjs.KubeJSMixin}
     */
    @ExpectPlatform
    public static void onKubeJSSetup() {
        throw new AssertionError();
    }

    public static void init() {
        GTCEu.LOGGER.info("GTCEu common proxy init!");
        ConfigHolder.init();
        GTCEu.initializeHighTier();

        UIFactory.register(MachineUIFactory.INSTANCE);
        UIFactory.register(CoverUIFactory.INSTANCE);
        UIFactory.register(GTUIEditorFactory.INSTANCE);
        GTPlacerTypes.init();
        GTRecipeCapabilities.init();
        GTRecipeConditions.init();
        GTElements.init();
        MaterialIconSet.init();
        MaterialIconType.init();
        GTMaterials.init();
        TagPrefix.init();
        GTSoundEntries.init();
        GTDamageTypes.init();
        GTCompassSections.init();
        GTCompassNodes.init();
        GTCovers.init();
        GTFluids.init();
        GTCreativeModeTabs.init();
        GTBlocks.init();
        GTBlockEntities.init();
        GTRecipeTypes.init();
        GTMachines.init();
        GTFoods.init();
        GTItems.init();
        GregTechDatagen.init();
        AddonFinder.getAddons().forEach(IGTAddon::initializeAddon);

        // fabric exclusive, squeeze this in here to register before stuff is used
        GTRegistries.REGISTRATE.registerRegistrate();
        WorldGenLayers.registerAll();
        GTFeatures.init();
        GTFeatures.register();
    }
}
