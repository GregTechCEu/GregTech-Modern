package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.AlloyBlastProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.mojang.datafixers.util.Pair;
import com.tterrag.registrate.util.entry.FluidEntry;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.resources.ResourceLocation;
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
    public static final Map<FluidProperty, Supplier<Pair<? extends Fluid, ? extends Fluid>>> MATERIAL_FLUID_FLOWING = new HashMap<>();
    public static final Map<Fluid, AlloyBlastProperty> HOT_FLUIDS = new HashMap<>();

    public static final ResourceLocation AUTO_GENERATED_MOLTEN_TEXTURE = GTCEu.id("block/fluids/fluid.molten.autogenerated");

    public static void init() {
        handleNonMaterialFluids(GTMaterials.Water, () -> Fluids.WATER);
        handleNonMaterialFluids(GTMaterials.Lava, () -> Fluids.LAVA);
        REGISTRATE.creativeModeTab(() -> GTCreativeModeTabs.MATERIAL_FLUID);
        // register fluids for materials
        for (var material : GTRegistries.MATERIALS) {
            var fluidProperty = material.getProperty(PropertyKey.FLUID);

            if (fluidProperty != null) {
                fluidProperty.getStorage().registerFluids(material);
                /*
                var fluidType = fluidProperty.getFluidType();
                FluidEntry<? extends Fluid> fluidEntry = GTRegistries.REGISTRATE.createFluid(material.getName(), fluidType.getLocalization(), material, fluidProperty.getStillTexture(), fluidProperty.getFlowTexture())
                        .temperature(Math.max(material.getBlastTemperature(), fluidProperty.getFluidTemperature()))
                        .density(fluidType.getDensity())
                        .viscosity(fluidType.getViscosity())
                        .luminance(fluidType.getLuminance())
                        .hasBlock(fluidProperty.hasBlock())
                        .color(material.hasFluidColor() ? material.getMaterialARGB() : -1)
                        .onFluidRegister(fluid -> MATERIAL_FLUIDS.put(fluid, fluidProperty))
                        .register();
                MATERIAL_FLUID_FLOWING.put(fluidProperty, () -> Pair.of(fluidEntry.getSource(), fluidEntry.get()));*/
                //setPropertyFluid(fluidProperty, fluidEntry);
            }

            /*
            PlasmaProperty plasmaProperty = material.getProperty(PropertyKey.PLASMA);
            if (plasmaProperty != null && !plasmaProperty.hasPlasmaSupplier()) {
                var fluidType = FluidTypes.PLASMA;
                plasmaProperty.setPlasma(GTRegistries.REGISTRATE.createFluid(material.getName() + "_plasma", fluidType.getLocalization(), material, plasmaProperty.getStillTexture(), plasmaProperty.getFlowTexture())
                        .temperature((fluidProperty == null ? 0 : fluidProperty.getFluidTemperature()) + 10000)
                        .density(fluidType.getDensity())
                        .viscosity(fluidType.getViscosity())
                        .luminance(fluidType.getLuminance())
                        .hasBlock(false)
                        .color(material.hasFluidColor() ? material.getMaterialARGB() : -1)
                        .onFluidRegister(fluid -> PLASMA_FLUIDS.put(fluid, plasmaProperty))
                        .registerFluid());
            }

            createMoltenFluid(material);
             */
        }
    }

    /*
    public static void createMoltenFluid(@Nonnull Material material) {
        // ignore materials set not to be alloy blast handled
        if (material.hasFlag(MaterialFlags.DISABLE_ALLOY_PROPERTY)) return;

        // ignore materials which are not alloys
        if (material.getMaterialComponents().size() <= 1) return;

        final BlastProperty blastProperty = material.getProperty(PropertyKey.BLAST);
        if (blastProperty == null) return;

        final AlloyBlastProperty alloyBlastProperty = material.getProperty(PropertyKey.ALLOY_BLAST);
        if (alloyBlastProperty == null) return;

        if (TagPrefix.ingotHot.doGenerateItem(material)) {
            int temperature = blastProperty.getBlastTemperature();
            var fluidType = FluidTypes.MOLTEN;
            Supplier<? extends Fluid> fluid = GTRegistries.REGISTRATE.createFluid("molten_" + material.getName(), fluidType.getLocalization(), material, AUTO_GENERATED_MOLTEN_TEXTURE, AUTO_GENERATED_MOLTEN_TEXTURE)
                    .temperature(temperature)
                    .density(fluidType.getDensity())
                    .viscosity(fluidType.getViscosity())
                    .luminance(fluidType.getLuminance())
                    .hasBlock(false)
                    .color(material.hasFluidColor() ? material.getMaterialARGB() : -1)
                    .onFluidRegister(f -> HOT_FLUIDS.put(f, alloyBlastProperty))
                    .registerFluid();

            alloyBlastProperty.setFluid(fluid);
        } else if (material.hasProperty(PropertyKey.FLUID)) {
            // not hot enough to produce molten fluid, so produce regular fluid
            alloyBlastProperty.setFluid(material::getFluid);
        } else return;

        alloyBlastProperty.setTemperature(blastProperty.getBlastTemperature());
    }
     */

    public static void handleNonMaterialFluids(@Nonnull Material material, @Nonnull Supplier<Fluid> fluid) {
        var property = material.getProperty(PropertyKey.FLUID);
        property.getStorage().store(FluidStorageKeys.LIQUID, fluid.get());
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

    @Nullable
    public static Fluid getFluidByMaterial(Material material) {
        return material.getFluid();
    }

    @Nullable
    public static Fluid getPlasmaByMaterial(Material material) {
        return material.getFluid(FluidStorageKeys.PLASMA);
    }
}
