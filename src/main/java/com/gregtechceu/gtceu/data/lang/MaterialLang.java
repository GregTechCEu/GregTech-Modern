package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.registry.MaterialRegistry;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import com.tterrag.registrate.providers.RegistrateLangProvider;

import static com.gregtechceu.gtceu.data.lang.LangUtil.*;
import static com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName;

public class MaterialLang {

    public static void generate(RegistrateLangProvider provider, MaterialRegistry registry) {
        for (Material material : registry.getAllMaterials())
            provider.add(material.getUnlocalizedName(), toEnglishName(material.getName()));
    }

    public static void init(RegistrateLangProvider provider) {
        generateCustomMaterialNames(provider);
        generateFluidKeys(provider);
    }

    private static void generateCustomMaterialNames(RegistrateLangProvider provider) {
        replace(provider, GTMaterials.FullersEarth.getUnlocalizedName(), "Fuller's Earth");
        replace(provider, GTMaterials.Cooperite.getUnlocalizedName(), "Sheldonite"); // greg's humor is now on

        // 1.20.1...
        replace(provider, GTMaterials.HSSG.getUnlocalizedName(), "HSS-G");
        replace(provider, GTMaterials.HSSE.getUnlocalizedName(), "HSS-E");
        replace(provider, GTMaterials.HSSS.getUnlocalizedName(), "HSS-S");
        replace(provider, GTMaterials.RTMAlloy.getUnlocalizedName(), "RTM Alloy");
        replace(provider, GTMaterials.HSLASteel.getUnlocalizedName(), "HSLA Steel");

        replace(provider, GTMaterials.UUMatter.getUnlocalizedName(), "UU-Matter");
        replace(provider, GTMaterials.PCBCoolant.getUnlocalizedName(), "PCB Coolant");
        replace(provider, GTMaterials.TungstenSteel.getUnlocalizedName(), "Tungstensteel");
        replace(provider, GTMaterials.Iron3Chloride.getUnlocalizedName(), "Iron III Chloride");
        replace(provider, GTMaterials.Iron2Chloride.getUnlocalizedName(), "Iron II Chloride");
        replace(provider, GTMaterials.OilHeavy.getUnlocalizedName(), "Heavy Oil");
        replace(provider, "block.gtceu.oil_heavy", "Heavy Oil");
        replace(provider, GTMaterials.OilLight.getUnlocalizedName(), "Light Oil");
        replace(provider, "block.gtceu.oil_light", "Light Oil");
        replace(provider, GTMaterials.RawOil.getUnlocalizedName(), "Raw Oil");
        replace(provider, "block.gtceu.oil_medium", "Raw Oil");

        replace(provider, GTMaterials.HydroCrackedButadiene.getUnlocalizedName(), "Hydro-Cracked Butadiene");
        replace(provider, GTMaterials.HydroCrackedButane.getUnlocalizedName(), "Hydro-Cracked Butane");
        replace(provider, GTMaterials.HydroCrackedButene.getUnlocalizedName(), "Hydro-Cracked Butene");
        replace(provider, GTMaterials.HydroCrackedButene.getUnlocalizedName(), "Hydro-Cracked Butene");
        replace(provider, GTMaterials.HydroCrackedEthane.getUnlocalizedName(), "Hydro-Cracked Ethane");
        replace(provider, GTMaterials.HydroCrackedEthylene.getUnlocalizedName(), "Hydro-Cracked Ethylene");
        replace(provider, GTMaterials.HydroCrackedPropane.getUnlocalizedName(), "Hydro-Cracked Propane");
        replace(provider, GTMaterials.HydroCrackedPropene.getUnlocalizedName(), "Hydro-Cracked Propene");
        replace(provider, GTMaterials.SteamCrackedButadiene.getUnlocalizedName(), "Steam-Cracked Butadiene");
        replace(provider, GTMaterials.SteamCrackedButane.getUnlocalizedName(), "Steam-Cracked Butane");
        replace(provider, GTMaterials.SteamCrackedButene.getUnlocalizedName(), "Steam-Cracked Butene");
        replace(provider, GTMaterials.SteamCrackedButene.getUnlocalizedName(), "Steam-Cracked Butene");
        replace(provider, GTMaterials.SteamCrackedEthane.getUnlocalizedName(), "Steam-Cracked Ethane");
        replace(provider, GTMaterials.SteamCrackedEthylene.getUnlocalizedName(), "Steam-Cracked Ethylene");
        replace(provider, GTMaterials.SteamCrackedPropane.getUnlocalizedName(), "Steam-Cracked Propane");
        replace(provider, GTMaterials.SteamCrackedPropene.getUnlocalizedName(), "Steam-Cracked Propene");
        replace(provider, GTMaterials.LightlyHydroCrackedGas.getUnlocalizedName(), "Lightly Hydro-Cracked Gas");
        replace(provider, GTMaterials.LightlyHydroCrackedHeavyFuel.getUnlocalizedName(),
                "Lightly Hydro-Cracked Heavy Fuel");
        replace(provider, GTMaterials.LightlyHydroCrackedLightFuel.getUnlocalizedName(),
                "Lightly Hydro-Cracked Light Fuel");
        replace(provider, GTMaterials.LightlyHydroCrackedNaphtha.getUnlocalizedName(),
                "Lightly Hydro-Cracked Naphtha");
        replace(provider, GTMaterials.LightlySteamCrackedGas.getUnlocalizedName(), "Lightly Steam-Cracked Gas");
        replace(provider, GTMaterials.LightlySteamCrackedHeavyFuel.getUnlocalizedName(),
                "Lightly Steam-Cracked Heavy Fuel");
        replace(provider, GTMaterials.LightlySteamCrackedLightFuel.getUnlocalizedName(),
                "Lightly Steam-Cracked Light Fuel");
        replace(provider, GTMaterials.LightlySteamCrackedNaphtha.getUnlocalizedName(),
                "Lightly Steam-Cracked Naphtha");
        replace(provider, GTMaterials.SeverelyHydroCrackedGas.getUnlocalizedName(),
                "Severely Hydro-Cracked Gas");
        replace(provider, GTMaterials.SeverelyHydroCrackedHeavyFuel.getUnlocalizedName(),
                "Severely Hydro-Cracked Heavy Fuel");
        replace(provider, GTMaterials.SeverelyHydroCrackedLightFuel.getUnlocalizedName(),
                "Severely Hydro-Cracked Light Fuel");
        replace(provider, GTMaterials.SeverelyHydroCrackedNaphtha.getUnlocalizedName(),
                "Severely Hydro-Cracked Naphtha");
        replace(provider, GTMaterials.SeverelySteamCrackedGas.getUnlocalizedName(),
                "Severely Steam-Cracked Gas");
        replace(provider, GTMaterials.SeverelySteamCrackedHeavyFuel.getUnlocalizedName(),
                "Severely Steam-Cracked Heavy Fuel");
        replace(provider, GTMaterials.SeverelySteamCrackedLightFuel.getUnlocalizedName(),
                "Severely Steam-Cracked Light Fuel");
        replace(provider, GTMaterials.SeverelySteamCrackedNaphtha.getUnlocalizedName(),
                "Severely Steam-Cracked Naphtha");
        replace(provider, GTMaterials.LPG.getUnlocalizedName(), "LPG");

        replace(provider, GTMaterials.Zeron100.getUnlocalizedName(), "Zeron-100");
        replace(provider, GTMaterials.IncoloyMA956.getUnlocalizedName(), "Incoloy MA-956");
        replace(provider, GTMaterials.Stellite100.getUnlocalizedName(), "Stellite-100");
        replace(provider, GTMaterials.HastelloyC276.getUnlocalizedName(), "Hastelloy C-276");
    }

    private static void generateFluidKeys(RegistrateLangProvider provider) {
        provider.add("fluid.gtceu.liquid_generic", "Liquid %s");
        provider.add("fluid.gtceu.generic", "%s");
        provider.add("fluid.gtceu.gas_generic", "%s Gas");
        provider.add("fluid.gtceu.gas_vapor", "%s Vapor");
        provider.add("fluid.gtceu.plasma", "%s Plasma");
        provider.add("fluid.gtceu.molten", "Molten %s");

        // ui
        provider.add("fluid.gtceu.empty", "Empty");
        provider.add("fluid.gtceu.amount", "§9Amount: %d/%d mB");
        provider.add("fluid.gtceu.click_to_fill",
                "§7Click with a Fluid Container to §bfill §7the tank (Shift-click for a full stack).");
        provider.add("fluid.gtceu.click_combined",
                "§7Click with a Fluid Container to §cempty §7or §bfill §7the tank (Shift-click for a full stack).");
        provider.add("fluid.gtceu.click_to_empty",
                "§7Click with a Fluid Container to §cempty §7the tank (Shift-click for a full stack).");

        // tooltips
        provider.add("fluid.gtceu.tooltip.temperature", "§cTemperature: %d K");
        provider.add("fluid.gtceu.tooltip.cryogenic", "§bCryogenic! Handle with care!");
        provider.add("fluid.gtceu.tooltip.gas", "§aState: Gaseous");
        provider.add("fluid.gtceu.tooltip.liquid", "§aState: Liquid");
        provider.add("fluid.gtceu.tooltip.plasma", "§aState: Plasma");
        provider.add("fluid.gtceu.tooltip.acid", "§6Acidic! Handle with care!");
    }
}
