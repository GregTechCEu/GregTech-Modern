package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.data.chemical.fluid.FluidTypes;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PlasmaProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.registry.GTRegistries.REGISTRATE;

/**
 * @author KilaBash
 * @date 2023/2/13
 * @implNote GTFluids
 */
public class GTFluids {

    public static final Map<Fluid, FluidProperty> MATERIAL_FLUIDS = new HashMap<>();
    public static final Map<Fluid, PlasmaProperty> PLASMA_FLUIDS = new HashMap<>();

    public static void init() {
        handleNonMaterialFluids(GTMaterials.Water, () -> Fluids.WATER);
        handleNonMaterialFluids(GTMaterials.Lava, () -> Fluids.LAVA);
        REGISTRATE.creativeModeTab(() -> GTCreativeModeTabs.MATERIAL_FLUID);
        // register fluids for materials
        for (var material : GTRegistries.MATERIALS) {
            var fluidProperty = material.getProperty(PropertyKey.FLUID);

            if (fluidProperty != null && !fluidProperty.hasFluidSupplier()) {
                var fluidType = fluidProperty.getFluidType();
                fluidProperty.setFluid(GTRegistries.REGISTRATE.createFluid(material.getName(), fluidProperty.getStillTexture(), fluidProperty.getFlowTexture())
                        .temperature(Math.max(material.getBlastTemperature(), fluidProperty.getFluidTemperature()))
                        .density(fluidType.getDensity())
                        .viscosity(fluidType.getViscosity())
                        .luminance(fluidType.getLuminance())
                        .hasBlock(fluidProperty.hasBlock())
                        .color(material.hasFluidColor() ? material.getMaterialARGB() : -1)
                        .onFluidRegister(fluid -> MATERIAL_FLUIDS.put(fluid, fluidProperty))
                        .registerFluid());
            }

            PlasmaProperty plasmaProperty = material.getProperty(PropertyKey.PLASMA);
            if (plasmaProperty != null && !plasmaProperty.hasPlasmaSupplier()) {
                var fluidType = FluidTypes.PLASMA;
                plasmaProperty.setPlasma(GTRegistries.REGISTRATE.createFluid(material.getName() + "_plasma", plasmaProperty.getStillTexture(), plasmaProperty.getFlowTexture())
                        .temperature((fluidProperty == null ? 0 : fluidProperty.getFluidTemperature()) + 10000)
                        .density(fluidType.getDensity())
                        .viscosity(fluidType.getViscosity())
                        .luminance(fluidType.getLuminance())
                        .hasBlock(false)
                        .color(material.hasFluidColor() ? material.getMaterialARGB() : -1)
                        .onFluidRegister(fluid -> PLASMA_FLUIDS.put(fluid, plasmaProperty))
                        .registerFluid());
                ;

            }

        }
    }

    public static void handleNonMaterialFluids(@Nonnull Material material, @Nonnull Supplier<Fluid> fluid) {
        var property = material.getProperty(PropertyKey.FLUID);
        property.setFluid(fluid);
        // TODO TOOLTIPS
//        List<String> tooltip = new ArrayList<>();
//        if (!material.getChemicalFormula().isEmpty()) {
//            tooltip.add(TextFormatting.YELLOW + material.getChemicalFormula());
//        }
//        tooltip.add(LocalizationUtils.format("gregtech.fluid.temperature", property.getFluidTemperature()));
//        tooltip.add(LocalizationUtils.format(property.getFluidType().getUnlocalizedTooltip()));
//        tooltip.addAll(property.getFluidType().getAdditionalTooltips());
//        FluidTooltipUtil.registerTooltip(fluid, tooltip);
    }

    @Nullable
    public static Fluid getFluidByMaterial(Material material) {
        return material.getFluid();
    }

    @Nullable
    public static Fluid getPlasmaByMaterial(Material material) {
        return material.getPlasma();
    }

}
