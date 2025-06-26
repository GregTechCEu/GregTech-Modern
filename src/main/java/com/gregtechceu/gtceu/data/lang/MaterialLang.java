package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.registry.MaterialRegistry;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;

import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName;

public class MaterialLang {

    public static void generate(GTLangProvider provider, MaterialRegistry registry) {
        for (Material material : registry.getAllMaterials())
            provider.add(material.getUnlocalizedName(), toEnglishName(material.getName()));
    }

    public static void init(GTLangProvider provider) {
        generateMaterialKeys(provider);
        generateCustomMaterialNames(provider);
        generateFluidKeys(provider);
    }

        replace(provider, FullersEarth.getUnlocalizedName(), "Fuller's Earth");
    private static void generateCustomMaterialNames(GTLangProvider provider) {
        // greg's humor is now on 1.20.1...
        replace(provider, Cooperite.getUnlocalizedName(), "Sheldonite");

        replace(provider, HSSG.getUnlocalizedName(), "HSS-G");
        replace(provider, HSSE.getUnlocalizedName(), "HSS-E");
        replace(provider, HSSS.getUnlocalizedName(), "HSS-S");
        replace(provider, RTMAlloy.getUnlocalizedName(), "RTM Alloy");
        replace(provider, HSLASteel.getUnlocalizedName(), "HSLA Steel");

        replace(provider, UUMatter.getUnlocalizedName(), "UU-Matter");
        replace(provider, PCBCoolant.getUnlocalizedName(), "PCB Coolant");
        replace(provider, TungstenSteel.getUnlocalizedName(), "Tungstensteel");

        replace(provider, Iron3Chloride.getUnlocalizedName(), "Iron III Chloride");
        replace(provider, Iron2Chloride.getUnlocalizedName(), "Iron II Chloride");

        replace(provider, OilHeavy.getUnlocalizedName(), "Heavy Oil");
        replace(provider, "block.gtceu.oil_heavy", "Heavy Oil");
        replace(provider, OilLight.getUnlocalizedName(), "Light Oil");
        replace(provider, "block.gtceu.oil_light", "Light Oil");
        replace(provider, RawOil.getUnlocalizedName(), "Raw Oil");
        replace(provider, "block.gtceu.oil_medium", "Raw Oil");

        replace(provider, HydroCrackedButadiene.getUnlocalizedName(), "Hydro-Cracked Butadiene");
        replace(provider, HydroCrackedButane.getUnlocalizedName(), "Hydro-Cracked Butane");
        replace(provider, HydroCrackedButene.getUnlocalizedName(), "Hydro-Cracked Butene");
        replace(provider, HydroCrackedButene.getUnlocalizedName(), "Hydro-Cracked Butene");
        replace(provider, HydroCrackedEthane.getUnlocalizedName(), "Hydro-Cracked Ethane");
        replace(provider, HydroCrackedEthylene.getUnlocalizedName(), "Hydro-Cracked Ethylene");
        replace(provider, HydroCrackedPropane.getUnlocalizedName(), "Hydro-Cracked Propane");
        replace(provider, HydroCrackedPropene.getUnlocalizedName(), "Hydro-Cracked Propene");

        replace(provider, SteamCrackedButadiene.getUnlocalizedName(), "Steam-Cracked Butadiene");
        replace(provider, SteamCrackedButane.getUnlocalizedName(), "Steam-Cracked Butane");
        replace(provider, SteamCrackedButene.getUnlocalizedName(), "Steam-Cracked Butene");
        replace(provider, SteamCrackedButene.getUnlocalizedName(), "Steam-Cracked Butene");
        replace(provider, SteamCrackedEthane.getUnlocalizedName(), "Steam-Cracked Ethane");
        replace(provider, SteamCrackedEthylene.getUnlocalizedName(), "Steam-Cracked Ethylene");
        replace(provider, SteamCrackedPropane.getUnlocalizedName(), "Steam-Cracked Propane");
        replace(provider, SteamCrackedPropene.getUnlocalizedName(), "Steam-Cracked Propene");

        replace(provider, LightlyHydroCrackedGas.getUnlocalizedName(), "Lightly Hydro-Cracked Gas");
        replace(provider, LightlyHydroCrackedHeavyFuel.getUnlocalizedName(),
                "Lightly Hydro-Cracked Heavy Fuel");
        replace(provider, LightlyHydroCrackedLightFuel.getUnlocalizedName(),
                "Lightly Hydro-Cracked Light Fuel");
        replace(provider, LightlyHydroCrackedNaphtha.getUnlocalizedName(),
                "Lightly Hydro-Cracked Naphtha");
        replace(provider, LightlySteamCrackedGas.getUnlocalizedName(), "Lightly Steam-Cracked Gas");
        replace(provider, LightlySteamCrackedHeavyFuel.getUnlocalizedName(),
                "Lightly Steam-Cracked Heavy Fuel");
        replace(provider, LightlySteamCrackedLightFuel.getUnlocalizedName(),
                "Lightly Steam-Cracked Light Fuel");
        replace(provider, LightlySteamCrackedNaphtha.getUnlocalizedName(),
                "Lightly Steam-Cracked Naphtha");

        replace(provider, SeverelyHydroCrackedGas.getUnlocalizedName(),
                "Severely Hydro-Cracked Gas");
        replace(provider, SeverelyHydroCrackedHeavyFuel.getUnlocalizedName(),
                "Severely Hydro-Cracked Heavy Fuel");
        replace(provider, SeverelyHydroCrackedLightFuel.getUnlocalizedName(),
                "Severely Hydro-Cracked Light Fuel");
        replace(provider, SeverelyHydroCrackedNaphtha.getUnlocalizedName(),
                "Severely Hydro-Cracked Naphtha");
        replace(provider, SeverelySteamCrackedGas.getUnlocalizedName(),
                "Severely Steam-Cracked Gas");
        replace(provider, SeverelySteamCrackedHeavyFuel.getUnlocalizedName(),
                "Severely Steam-Cracked Heavy Fuel");
        replace(provider, SeverelySteamCrackedLightFuel.getUnlocalizedName(),
                "Severely Steam-Cracked Light Fuel");
        replace(provider, SeverelySteamCrackedNaphtha.getUnlocalizedName(),
                "Severely Steam-Cracked Naphtha");
        replace(provider, LPG.getUnlocalizedName(), "LPG");

        replace(provider, Zeron100.getUnlocalizedName(), "Zeron-100");
        replace(provider, IncoloyMA956.getUnlocalizedName(), "Incoloy MA-956");
        replace(provider, Stellite100.getUnlocalizedName(), "Stellite-100");
        replace(provider, HastelloyC276.getUnlocalizedName(), "Hastelloy C-276");

        replace(provider, "item.gtceu.nether_quartz_netherrack", "Nether Quartz Ore");
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
        provider.add("fluid.gtceu.tooltip.temperature", "§cTemperature: %d K");
        provider.add("fluid.gtceu.tooltip.cryogenic", "§bCryogenic! Handle with care!");
        provider.add("fluid.gtceu.tooltip.gas", "§aState: Gaseous");
        provider.add("fluid.gtceu.tooltip.liquid", "§aState: Liquid");
        provider.add("fluid.gtceu.tooltip.plasma", "§aState: Plasma");
        provider.add("fluid.gtceu.tooltip.acid", "§6Acidic! Handle with care!");

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
