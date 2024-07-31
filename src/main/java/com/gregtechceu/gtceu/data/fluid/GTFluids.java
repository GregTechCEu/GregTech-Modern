package com.gregtechceu.gtceu.data.fluid;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.fluid.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.material.material.registry.MaterialRegistry;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.data.GTCreativeModeTabs;
import com.gregtechceu.gtceu.data.material.GTMaterials;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import org.jetbrains.annotations.NotNull;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

/**
 * @author KilaBash
 * @date 2023/2/13
 * @implNote GTFluids
 */
public class GTFluids {

    public static void init() {
        handleNonMaterialFluids(GTMaterials.Water, Fluids.WATER);
        handleNonMaterialFluids(GTMaterials.Lava, Fluids.LAVA);
        REGISTRATE.creativeModeTab(() -> GTCreativeModeTabs.MATERIAL_FLUID);
        // register fluids for materials
        for (MaterialRegistry registry : GTCEuAPI.materialManager.getRegistries()) {
            GTRegistrate registrate = registry.getRegistrate();
            for (var material : registry.getAllMaterials()) {
                var fluidProperty = material.getProperty(PropertyKey.FLUID);

                if (fluidProperty != null) {
                    fluidProperty.registerFluids(material, registrate);
                }
            }
        }
    }

    public static void handleNonMaterialFluids(@NotNull Material material, @NotNull Fluid fluid) {
        var property = material.getProperty(PropertyKey.FLUID);
        property.getStorage().store(FluidStorageKeys.LIQUID, () -> fluid, null);
    }
}
