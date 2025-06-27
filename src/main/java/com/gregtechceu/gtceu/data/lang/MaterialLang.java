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

    private static void generateCustomMaterialNames(GTLangProvider provider) {
        provider.add(FullersEarth.getUnlocalizedName(), "Fuller's Earth");
        // greg's humor is now on 1.20.1...
        provider.add(Cooperite.getUnlocalizedName(), "Sheldonite");

        provider.add(HSSG.getUnlocalizedName(), "HSS-G");
        provider.add(HSSE.getUnlocalizedName(), "HSS-E");
        provider.add(HSSS.getUnlocalizedName(), "HSS-S");
        provider.add(RTMAlloy.getUnlocalizedName(), "RTM Alloy");
        provider.add(HSLASteel.getUnlocalizedName(), "HSLA Steel");

        provider.add(UUMatter.getUnlocalizedName(), "UU-Matter");
        provider.add(PCBCoolant.getUnlocalizedName(), "PCB Coolant");
        provider.add(TungstenSteel.getUnlocalizedName(), "Tungstensteel");

        provider.add(Iron3Chloride.getUnlocalizedName(), "Iron III Chloride");
        provider.add(Iron2Chloride.getUnlocalizedName(), "Iron II Chloride");

        provider.add(OilHeavy.getUnlocalizedName(), "Heavy Oil");
        provider.add("block.gtceu.oil_heavy", "Heavy Oil");
        provider.add(OilLight.getUnlocalizedName(), "Light Oil");
        provider.add("block.gtceu.oil_light", "Light Oil");
        provider.add(RawOil.getUnlocalizedName(), "Raw Oil");
        provider.add("block.gtceu.oil_medium", "Raw Oil");

        provider.add(HydroCrackedButadiene.getUnlocalizedName(), "Hydro-Cracked Butadiene");
        provider.add(HydroCrackedButane.getUnlocalizedName(), "Hydro-Cracked Butane");
        provider.add(HydroCrackedButene.getUnlocalizedName(), "Hydro-Cracked Butene");
        provider.add(HydroCrackedButene.getUnlocalizedName(), "Hydro-Cracked Butene");
        provider.add(HydroCrackedEthane.getUnlocalizedName(), "Hydro-Cracked Ethane");
        provider.add(HydroCrackedEthylene.getUnlocalizedName(), "Hydro-Cracked Ethylene");
        provider.add(HydroCrackedPropane.getUnlocalizedName(), "Hydro-Cracked Propane");
        provider.add(HydroCrackedPropene.getUnlocalizedName(), "Hydro-Cracked Propene");

        provider.add(SteamCrackedButadiene.getUnlocalizedName(), "Steam-Cracked Butadiene");
        provider.add(SteamCrackedButane.getUnlocalizedName(), "Steam-Cracked Butane");
        provider.add(SteamCrackedButene.getUnlocalizedName(), "Steam-Cracked Butene");
        provider.add(SteamCrackedButene.getUnlocalizedName(), "Steam-Cracked Butene");
        provider.add(SteamCrackedEthane.getUnlocalizedName(), "Steam-Cracked Ethane");
        provider.add(SteamCrackedEthylene.getUnlocalizedName(), "Steam-Cracked Ethylene");
        provider.add(SteamCrackedPropane.getUnlocalizedName(), "Steam-Cracked Propane");
        provider.add(SteamCrackedPropene.getUnlocalizedName(), "Steam-Cracked Propene");

        provider.add(LightlyHydroCrackedGas.getUnlocalizedName(), "Lightly Hydro-Cracked Gas");
        provider.add(LightlyHydroCrackedHeavyFuel.getUnlocalizedName(),
                "Lightly Hydro-Cracked Heavy Fuel");
        provider.add(LightlyHydroCrackedLightFuel.getUnlocalizedName(),
                "Lightly Hydro-Cracked Light Fuel");
        provider.add(LightlyHydroCrackedNaphtha.getUnlocalizedName(),
                "Lightly Hydro-Cracked Naphtha");
        provider.add(LightlySteamCrackedGas.getUnlocalizedName(), "Lightly Steam-Cracked Gas");
        provider.add(LightlySteamCrackedHeavyFuel.getUnlocalizedName(),
                "Lightly Steam-Cracked Heavy Fuel");
        provider.add(LightlySteamCrackedLightFuel.getUnlocalizedName(),
                "Lightly Steam-Cracked Light Fuel");
        provider.add(LightlySteamCrackedNaphtha.getUnlocalizedName(),
                "Lightly Steam-Cracked Naphtha");

        provider.add(SeverelyHydroCrackedGas.getUnlocalizedName(),
                "Severely Hydro-Cracked Gas");
        provider.add(SeverelyHydroCrackedHeavyFuel.getUnlocalizedName(),
                "Severely Hydro-Cracked Heavy Fuel");
        provider.add(SeverelyHydroCrackedLightFuel.getUnlocalizedName(),
                "Severely Hydro-Cracked Light Fuel");
        provider.add(SeverelyHydroCrackedNaphtha.getUnlocalizedName(),
                "Severely Hydro-Cracked Naphtha");
        provider.add(SeverelySteamCrackedGas.getUnlocalizedName(),
                "Severely Steam-Cracked Gas");
        provider.add(SeverelySteamCrackedHeavyFuel.getUnlocalizedName(),
                "Severely Steam-Cracked Heavy Fuel");
        provider.add(SeverelySteamCrackedLightFuel.getUnlocalizedName(),
                "Severely Steam-Cracked Light Fuel");
        provider.add(SeverelySteamCrackedNaphtha.getUnlocalizedName(),
                "Severely Steam-Cracked Naphtha");
        provider.add(LPG.getUnlocalizedName(), "LPG");

        provider.add(Zeron100.getUnlocalizedName(), "Zeron-100");
        provider.add(IncoloyMA956.getUnlocalizedName(), "Incoloy MA-956");
        provider.add(Stellite100.getUnlocalizedName(), "Stellite-100");
        provider.add(HastelloyC276.getUnlocalizedName(), "Hastelloy C-276");

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
        provider.add("tooltip.gtceu.fluid_property.temperature", "§cTemperature: %d K");
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
