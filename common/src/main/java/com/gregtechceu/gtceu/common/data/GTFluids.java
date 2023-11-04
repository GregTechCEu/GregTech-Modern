package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.gregtechceu.gtceu.api.registry.GTRegistries.REGISTRATE;

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
        for (var material : GTRegistries.MATERIALS) {
            var fluidProperty = material.getProperty(PropertyKey.FLUID);

            if (fluidProperty != null) {
                fluidProperty.getStorage().registerFluids(material);
            }
        }
    }

    public static void handleNonMaterialFluids(@Nonnull Material material, @Nonnull Fluid fluid) {
        var property = material.getProperty(PropertyKey.FLUID);
        property.getStorage().store(FluidStorageKeys.LIQUID, fluid);
        // TODO TOOLTIPS
//        List<String> tooltip = new ArrayList<>();
//        if (!material.getChemicalFormula().isEmpty()) {
//            tooltip.add(TextFormatting.YELLOW + material.getChemicalFormula());
//        }
//        tooltip.add(LocalizationUtils.format("gtceu.fluid.temperature", property.getFluidTemperature()));
//        tooltip.add(LocalizationUtils.format(property.getFluidType().getUnlocalizedTooltip()));
//        tooltip.addAll(property.getFluidType().getAdditionalTooltips());
//        FluidTooltipUtil.registerTooltip(fluid, tooltip);
    }
}
