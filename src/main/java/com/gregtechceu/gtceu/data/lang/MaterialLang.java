package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.registry.MaterialRegistry;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;

import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class MaterialLang {

    public static void generate(GTLangProvider provider, MaterialRegistry registry) {
        for (Material material : registry.getAllMaterials()) {
            provider.add(material.getUnlocalizedName(), material.getDefaultTranslation());
        }
    }

    public static void init(GTLangProvider provider) {
        generateMaterialKeys(provider);
        generateCustomMaterialNames(provider);
        generateFluidKeys(provider);
    }

    private static void generateCustomMaterialNames(GTLangProvider provider) {
        provider.add("item.gtceu.nether_quartz_netherrack", "Nether Quartz Ore");
    }

    private static void generateFluidKeys(GTLangProvider provider) {
        // Fluid Types
        provider.add("fluid.gtceu.liquid_generic", "Liquid %s");
        provider.add("fluid.gtceu.generic", "%s");
        provider.add("fluid.gtceu.gas_generic", "%s Gas");
        provider.add("fluid.gtceu.gas_vapor", "%s Vapor");
        provider.add("fluid.gtceu.plasma", "%s Plasma");
        provider.add("fluid.gtceu.molten", "Molten %s");

        // Fluid Widgets
        provider.add("fluid.gtceu.empty", "Empty");
        provider.add("fluid.gtceu.amount", "§9Amount: %d/%d mB");
        provider.add("fluid.gtceu.click_to_fill",
                "§7Click with a Fluid Container to §bfill §7the tank (Shift-click for a full stack).");
        provider.add("fluid.gtceu.click_combined",
                "§7Click with a Fluid Container to §cempty §7or §bfill §7the tank (Shift-click for a full stack).");
        provider.add("fluid.gtceu.click_to_empty",
                "§7Click with a Fluid Container to §cempty §7the tank (Shift-click for a full stack).");

        // Fluid Tooltips
        provider.add("tooltip.gtceu.fluid_state.gas", "§aState: Gaseous");
        provider.add("tooltip.gtceu.fluid_state.liquid", "§aState: Liquid");
        provider.add("tooltip.gtceu.fluid_state.plasma", "§aState: Plasma");
        provider.add("tooltip.gtceu.fluid_property.temperature", "§cTemperature: %s");
        provider.add("tooltip.gtceu.fluid_property.cryogenic", "§bCryogenic! Handle with care!");
        provider.add("tooltip.gtceu.fluid_property.acid", "§6Acidic! Handle with care!");

        // Fluid Tiles?
        provider.add("fluid.empty", "Empty");
        provider.add("fluid.tile.lava", "Lava");
        provider.add("fluid.tile.water", "Water");
    }

    private static void generateMaterialKeys(GTLangProvider provider) {
        // Material Page
        provider.add("gtceu.jei.materials.average_mass", "Average mass: %d");
        provider.add("gtceu.jei.materials.average_protons", "Average protons: %d");
        provider.add("gtceu.jei.materials.average_neutrons", "Average neutrons: %d");

        // Cauldron Washing
        provider.add("tagprefix.ore_dust.tooltip.purify", "Right click a Cauldron to get clean Dust");
        provider.add("tagprefix.crushed.tooltip.purify", "Right click a Cauldron to get Purified Ore");
    }
}
