package com.gregtechceu.gtceu.data.fluid;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.fluid.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.fluid.potion.PotionFluid;
import com.gregtechceu.gtceu.data.material.GTMaterials;
import com.gregtechceu.gtceu.data.misc.GTCreativeModeTabs;
import com.gregtechceu.gtceu.data.tag.CustomTags;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.NeoForgeMod;

import com.tterrag.registrate.util.entry.FluidEntry;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class GTFluids {

    @SuppressWarnings("UnstableApiUsage")
    public static final FluidEntry<PotionFluid> POTION = REGISTRATE
            .fluid("potion", GTCEu.id("block/fluids/fluid.potion"), GTCEu.id("block/fluids/fluid.potion"),
                    PotionFluid.PotionFluidType::new, PotionFluid::new)
            .lang("Potion")
            .source(PotionFluid::new).noBlock().noBucket()
            .tag(CustomTags.POTION_FLUIDS)
            .register();

    public static void init() {
        // Register fluids for non-materials
        handleNonMaterialFluids(GTMaterials.Water, Fluids.WATER);
        handleNonMaterialFluids(GTMaterials.Lava, Fluids.LAVA);
        handleNonMaterialFluids(GTMaterials.Milk, NeoForgeMod.MILK);
        NeoForgeMod.enableMilkFluid();

        // register fluids for materials
        REGISTRATE.creativeModeTab(GTCreativeModeTabs.MATERIAL_FLUID);
    }

    public static void generateMaterialFluids() {
        for (var material : GTCEuAPI.materialManager) {
            var fluidProperty = material.getProperty(PropertyKey.FLUID);

            if (fluidProperty != null) {
                GTRegistrate registrate = GTRegistrate.createIgnoringListenerErrors(material.getModid());
                fluidProperty.registerFluids(material, registrate);
            }
        }
    }

    public static void handleNonMaterialFluids(@NotNull Material material, @NotNull Fluid fluid) {
        handleNonMaterialFluids(material, () -> fluid);
    }

    public static void handleNonMaterialFluids(@NotNull Material material, @NotNull Supplier<Fluid> fluid) {
        var property = material.getProperty(PropertyKey.FLUID);
        property.getStorage().store(FluidStorageKeys.LIQUID, fluid, null);
    }
}
