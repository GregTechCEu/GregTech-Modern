package com.gregtechceu.gtceu.common.data.materials;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttributes;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.common.data.GTMedicalConditions;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dustSmall;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dustTiny;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class UnknownCompositionMaterials {

    public static void register() {
        WoodGas = REGISTRATE.material("wood_gas", builder -> builder
                .gas()
                .color(0xDECD87).secondaryColor(0xdeb287));

        WoodVinegar = REGISTRATE.material("wood_vinegar", builder -> builder
                .fluid()
                .color(0xD45500).secondaryColor(0x905800));

        WoodTar = REGISTRATE.material("wood_tar", builder -> builder
                .fluid()
                .color(0x3a271a).secondaryColor(0x28170B)
                .flags(STICKY, FLAMMABLE));

        CharcoalByproducts = REGISTRATE.material("charcoal_byproducts", builder -> builder
                .fluid().color(0x784421));

        Biomass = REGISTRATE.material("biomass", builder -> builder
                .liquid(new FluidBuilder().customStill()).color(0x00FF00));

        BioDiesel = REGISTRATE.material("bio_diesel", builder -> builder
                .fluid().color(0xFF8000)
                .flags(FLAMMABLE, EXPLOSIVE));

        FermentedBiomass = REGISTRATE.material("fermented_biomass", builder -> builder
                .liquid(new FluidBuilder().temperature(300))
                .color(0x445500));

        Creosote = REGISTRATE.material("creosote", builder -> builder
                .liquid(new FluidBuilder().block().customStill().burnTime(6400)).color(0x804000)
                .flags(STICKY));

        Diesel = REGISTRATE.material("diesel", builder -> builder
                .liquid(new FluidBuilder().customStill()).flags(FLAMMABLE, EXPLOSIVE));

        RocketFuel = REGISTRATE.material("rocket_fuel", builder -> builder
                .fluid().flags(FLAMMABLE, EXPLOSIVE).color(0xBDB78C));

        Glue = REGISTRATE.material("glue", builder -> builder
                .liquid(new FluidBuilder().customStill()).flags(STICKY));

        Lubricant = REGISTRATE.material("lubricant", builder -> builder
                .liquid(new FluidBuilder().customStill()));

        McGuffium239 = REGISTRATE.material("mc_guffium_239", builder -> builder
                .liquid(new FluidBuilder().customStill()));

        IndiumConcentrate = REGISTRATE.material("indium_concentrate", builder -> builder
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x0E2950));

        SeedOil = REGISTRATE.material("seed_oil", builder -> builder
                .liquid(new FluidBuilder().customStill())
                .color(0xFFFFFF)
                .flags(STICKY, FLAMMABLE));

        DrillingFluid = REGISTRATE.material("drilling_fluid", builder -> builder
                .fluid().color(0xFFFFAA));

        ConstructionFoam = REGISTRATE.material("construction_foam", builder -> builder
                .fluid().color(0x808080));

        SulfuricHeavyFuel = REGISTRATE.material("sulfuric_heavy_fuel", builder -> builder
                .liquid(new FluidBuilder().customStill()).flags(FLAMMABLE));

        HeavyFuel = REGISTRATE.material("heavy_fuel", builder -> builder
                .liquid(new FluidBuilder().customStill()).flags(FLAMMABLE));

        LightlyHydroCrackedHeavyFuel = REGISTRATE.material("lightly_hydro_cracked_heavy_fuel", builder -> builder
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xFFFF00).flags(FLAMMABLE));

        SeverelyHydroCrackedHeavyFuel = REGISTRATE.material("severely_hydro_cracked_heavy_fuel", builder -> builder
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xFFFF00).flags(FLAMMABLE));

        LightlySteamCrackedHeavyFuel = REGISTRATE.material("lightly_steam_cracked_heavy_fuel", builder -> builder
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .flags(FLAMMABLE));

        SeverelySteamCrackedHeavyFuel = REGISTRATE.material("severely_steam_cracked_heavy_fuel", builder -> builder
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .flags(FLAMMABLE));

        SulfuricLightFuel = REGISTRATE.material("sulfuric_light_fuel", builder -> builder
                .liquid(new FluidBuilder()
                        .customStill())
                .flags(FLAMMABLE));

        LightFuel = REGISTRATE.material("light_fuel", builder -> builder
                .liquid(new FluidBuilder().customStill()).flags(FLAMMABLE));

        LightlyHydroCrackedLightFuel = REGISTRATE.material("lightly_hydro_cracked_light_fuel", builder -> builder
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xB7AF08).flags(FLAMMABLE));

        SeverelyHydroCrackedLightFuel = REGISTRATE.material("severely_hydro_cracked_light_fuel", builder -> builder
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xB7AF08).flags(FLAMMABLE));

        LightlySteamCrackedLightFuel = REGISTRATE.material("lightly_steam_cracked_light_fuel", builder -> builder
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .flags(FLAMMABLE));

        SeverelySteamCrackedLightFuel = REGISTRATE.material("severely_steam_cracked_light_fuel", builder -> builder
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .flags(FLAMMABLE));

        SulfuricNaphtha = REGISTRATE.material("sulfuric_naphtha", builder -> builder
                .liquid(new FluidBuilder().customStill()).flags(FLAMMABLE));

        Naphtha = REGISTRATE.material("naphtha", builder -> builder
                .liquid(new FluidBuilder().customStill()).flags(FLAMMABLE));

        LightlyHydroCrackedNaphtha = REGISTRATE.material("lightly_hydro_cracked_naphtha", builder -> builder
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xBFB608).flags(FLAMMABLE));

        SeverelyHydroCrackedNaphtha = REGISTRATE.material("severely_hydro_cracked_naphtha", builder -> builder
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xBFB608).flags(FLAMMABLE));

        LightlySteamCrackedNaphtha = REGISTRATE.material("lightly_steam_cracked_naphtha", builder -> builder
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xBFB608).flags(FLAMMABLE));

        SeverelySteamCrackedNaphtha = REGISTRATE.material("severely_steam_cracked_naphtha", builder -> builder
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xBFB608).flags(FLAMMABLE));

        SulfuricGas = REGISTRATE.material("sulfuric_gas", builder -> builder
                .gas(new FluidBuilder().customStill())
                .color(0xECDCCC));

        RefineryGas = REGISTRATE.material("refinery_gas", builder -> builder
                .gas(new FluidBuilder().customStill())
                .color(0xB4B4B4)
                .flags(FLAMMABLE));

        LightlyHydroCrackedGas = REGISTRATE.material("lightly_hydro_cracked_gas", builder -> builder
                .gas(new FluidBuilder().temperature(775))
                .color(0xA0A0A0)
                .flags(FLAMMABLE));

        SeverelyHydroCrackedGas = REGISTRATE.material("severely_hydro_cracked_gas", builder -> builder
                .gas(new FluidBuilder().temperature(775))
                .color(0xC8C8C8)
                .flags(FLAMMABLE));

        LightlySteamCrackedGas = REGISTRATE.material("lightly_steam_cracked_gas", builder -> builder
                .gas(new FluidBuilder().temperature(775))
                .color(0xE0E0E0)
                .flags(FLAMMABLE));

        SeverelySteamCrackedGas = REGISTRATE.material("severely_steam_cracked_gas", builder -> builder
                .gas(new FluidBuilder().temperature(775))
                .color(0xE0E0E0).flags(FLAMMABLE));

        HydroCrackedEthane = REGISTRATE.material("hydro_cracked_ethane", builder -> builder
                .gas(new FluidBuilder().temperature(775))
                .color(0x9696BC).flags(FLAMMABLE));

        HydroCrackedEthylene = REGISTRATE.material("hydro_cracked_ethylene", builder -> builder
                .gas(new FluidBuilder().temperature(775))
                .color(0xA3A3A0).flags(FLAMMABLE));

        HydroCrackedPropene = REGISTRATE.material("hydro_cracked_propene", builder -> builder
                .gas(new FluidBuilder().temperature(775))
                .color(0xBEA540).flags(FLAMMABLE));

        HydroCrackedPropane = REGISTRATE.material("hydro_cracked_propane", builder -> builder
                .gas(new FluidBuilder().temperature(775))
                .color(0xBEA540).flags(FLAMMABLE));

        HydroCrackedButane = REGISTRATE.material("hydro_cracked_butane", builder -> builder
                .gas(new FluidBuilder().temperature(775))
                .color(0x852C18).flags(FLAMMABLE));

        HydroCrackedButene = REGISTRATE.material("hydro_cracked_butene", builder -> builder
                .gas(new FluidBuilder().temperature(775))
                .color(0x993E05).flags(FLAMMABLE));

        HydroCrackedButadiene = REGISTRATE.material("hydro_cracked_butadiene", builder -> builder
                .gas(new FluidBuilder().temperature(775))
                .color(0xAD5203).flags(FLAMMABLE));

        SteamCrackedEthane = REGISTRATE.material("steam_cracked_ethane", builder -> builder
                .gas(new FluidBuilder().temperature(775))
                .color(0x9696BC).flags(FLAMMABLE));

        SteamCrackedEthylene = REGISTRATE.material("steam_cracked_ethylene", builder -> builder
                .gas(new FluidBuilder().temperature(775))
                .color(0xA3A3A0).flags(FLAMMABLE));

        SteamCrackedPropene = REGISTRATE.material("steam_cracked_propene", builder -> builder
                .gas(new FluidBuilder().temperature(775))
                .color(0xBEA540).flags(FLAMMABLE));

        SteamCrackedPropane = REGISTRATE.material("steam_cracked_propane", builder -> builder
                .gas(new FluidBuilder().temperature(775))
                .color(0xBEA540).flags(FLAMMABLE));

        SteamCrackedButane = REGISTRATE.material("steam_cracked_butane", builder -> builder
                .gas(new FluidBuilder().temperature(775))
                .color(0x852C18).flags(FLAMMABLE));

        SteamCrackedButene = REGISTRATE.material("steam_cracked_butene", builder -> builder
                .gas(new FluidBuilder().temperature(775))
                .color(0x993E05).flags(FLAMMABLE));

        SteamCrackedButadiene = REGISTRATE.material("steam_cracked_butadiene", builder -> builder
                .gas(new FluidBuilder().temperature(775))
                .color(0xAD5203).flags(FLAMMABLE));

        LPG = REGISTRATE.material("lpg", builder -> builder
                .liquid(new FluidBuilder().customStill())
                .color(0xFCFCAC).flags(FLAMMABLE, EXPLOSIVE));

        RawGrowthMedium = REGISTRATE.material("raw_growth_medium", builder -> builder
                .fluid().color(0xA47351));

        SterileGrowthMedium = REGISTRATE.material("sterilized_growth_medium", builder -> builder
                .fluid().color(0xAC876E));

        Oil = REGISTRATE.material("oil", builder -> builder
                .liquid(new FluidBuilder().block().customStill())
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE));

        HeavyOil = REGISTRATE.material("heavy_oil", builder -> builder
                .liquid(new FluidBuilder().block().customStill())
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE));

        RawOil = REGISTRATE.material("raw_oil", builder -> builder
                .liquid(new FluidBuilder().block().customStill())
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE));

        LightOil = REGISTRATE.material("light_oil", builder -> builder
                .liquid(new FluidBuilder().block().customStill())
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE));

        NaturalGas = REGISTRATE.material("natural_gas", builder -> builder
                .gas(new FluidBuilder().block().customStill())
                .flags(FLAMMABLE, EXPLOSIVE));

        Bacteria = REGISTRATE.material("bacteria", builder -> builder
                .fluid().color(0x808000));

        BacterialSludge = REGISTRATE.material("bacterial_sludge", builder -> builder
                .fluid().color(0x355E3B));

        EnrichedBacterialSludge = REGISTRATE.material("enriched_bacterial_sludge", builder -> builder
                .fluid().color(0x7FFF00));

        Mutagen = REGISTRATE.material("mutagen", builder -> builder
                .fluid().color(0x00FF7F));

        GelatinMixture = REGISTRATE.material("gelatin_mixture", builder -> builder
                .fluid().color(0x588BAE));

        RawGasoline = REGISTRATE.material("raw_gasoline", builder -> builder
                .fluid().color(0xFF6400).flags(FLAMMABLE));

        Gasoline = REGISTRATE.material("gasoline", builder -> builder
                .fluid().color(0xFAA500).flags(FLAMMABLE, EXPLOSIVE));

        HighOctaneGasoline = REGISTRATE.material("high_octane_gasoline", builder -> builder
                .fluid().color(0xFFA500).flags(FLAMMABLE, EXPLOSIVE));

        CoalGas = REGISTRATE.material("coal_gas", builder -> builder
                .gas().color(0x333333));

        CoalTar = REGISTRATE.material("coal_tar", builder -> builder
                .fluid().color(0x1A1A1A).flags(STICKY, FLAMMABLE));

        Gunpowder = REGISTRATE.material("gunpowder", builder -> builder
                .dust(0)
                .color(0xa4a4a4).secondaryColor(0x767676).iconSet(ROUGH)
                .flags(FLAMMABLE, EXPLOSIVE, NO_SMELTING, NO_SMASHING)
                .components(Saltpeter, 2, Sulfur, 1, Carbon, 3));

        Oilsands = REGISTRATE.material("oilsands", builder -> builder
                .dust(1).ore()
                .color(0xe3c78a).secondaryColor(0x161e22).iconSet(SAND)
                .flags(FLAMMABLE));

        RareEarth = REGISTRATE.material("rare_earth", builder -> builder
                .dust(0)
                .color(0xffdc88).secondaryColor(0xe99673).iconSet(FINE));

        Stone = REGISTRATE.material("stone", builder -> builder
                .dust(2)
                .color(0x8f8f8f).secondaryColor(0x898989).iconSet(ROUGH)
                .flags(MORTAR_GRINDABLE, GENERATE_GEAR, NO_SMASHING, NO_SMELTING));

        Lava = REGISTRATE.material("lava", builder -> builder
                .fluid().color(0xFF4000));

        Netherite = REGISTRATE.material("netherite", builder -> builder
                .ingot().color(0x4b4042).secondaryColor(0x474447)
                .flags(FIRE_RESISTANT)
                .toolStats(ToolProperty.Builder.of(10.0F, 4.0F, 2032, 4)
                        .enchantability(21).build()));

        Glowstone = REGISTRATE.material("glowstone", builder -> builder
                .dust(1)
                .liquid(new FluidBuilder().temperature(500))
                .color(0xfcb34c).secondaryColor(0xce7533).iconSet(SHINY)
                .flags(NO_SMASHING, GENERATE_PLATE, EXCLUDE_PLATE_COMPRESSOR_RECIPE,
                        EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES));

        NetherStar = REGISTRATE.material("nether_star", builder -> builder
                .gem(4)
                .color(0xfeffc6).secondaryColor(0x7fd7e2)
                .iconSet(NETHERSTAR)
                .flags(NO_SMASHING, NO_SMELTING, GENERATE_LENS));

        Endstone = REGISTRATE.material("endstone", builder -> builder
                .dust(1)
                .color(0xf6fabd).secondaryColor(0xc5be8b).iconSet(ROUGH)
                .flags(NO_SMASHING));

        Netherrack = REGISTRATE.material("netherrack", builder -> builder
                .dust(1)
                .color(0x7c4249).secondaryColor(0x400b0b).iconSet(ROUGH)
                .flags(NO_SMASHING, FLAMMABLE));

        CetaneBoostedDiesel = REGISTRATE.material("cetane_boosted_diesel", builder -> builder
                .liquid(new FluidBuilder().customStill())
                .color(0xC8FF00)
                .flags(FLAMMABLE, EXPLOSIVE));

        Collagen = REGISTRATE.material("collagen", builder -> builder
                .dust(1)
                .color(0xffadb7).secondaryColor(0x80471C).iconSet(ROUGH));

        Gelatin = REGISTRATE.material("gelatin", builder -> builder
                .dust(1)
                .color(0xfaf7cb).secondaryColor(0x693d00).iconSet(ROUGH));

        Agar = REGISTRATE.material("agar", builder -> builder
                .dust(1)
                .color(0xbdd168).secondaryColor(0x403218).iconSet(ROUGH));

        Milk = REGISTRATE.material("milk", builder -> builder
                .liquid(new FluidBuilder()
                        .temperature(295)
                        .customStill())
                .color(0xfffbf0).secondaryColor(0xf6eac8).iconSet(FINE));

        Cocoa = REGISTRATE.material("cocoa", builder -> builder
                .dust(0)
                .color(0x976746).secondaryColor(0x301a0a).iconSet(FINE));

        Wheat = REGISTRATE.material("wheat", builder -> builder
                .dust(0)
                .color(0xdcbb65).secondaryColor(0x565138).iconSet(FINE));

        Meat = REGISTRATE.material("meat", builder -> builder
                .dust(1)
                .color(0xe85048).secondaryColor(0x470a06).iconSet(SAND));

        Wood = REGISTRATE.material("wood", builder -> builder
                .wood()
                .color(0xc29f6d).secondaryColor(0x643200).iconSet(WOOD)
                .fluidPipeProperties(340, 5, false)
                .toolStats(ToolProperty.Builder.of(1.0F, 1.0F, 128, 1, GTToolType.SOFT_MALLET).build())
                .flags(GENERATE_PLATE, GENERATE_ROD, GENERATE_BOLT_SCREW, GENERATE_LONG_ROD, FLAMMABLE, GENERATE_GEAR,
                        GENERATE_FRAME));

        Paper = REGISTRATE.material("paper", builder -> builder
                .dust(0)
                .color(0xF9F9F9).secondaryColor(0xECECEC).iconSet(DULL)
                .flags(GENERATE_PLATE, FLAMMABLE, NO_SMELTING, NO_SMASHING,
                        MORTAR_GRINDABLE, EXCLUDE_PLATE_COMPRESSOR_RECIPE));

        FishOil = REGISTRATE.material("fish_oil", builder -> builder
                .fluid()
                .color(0xDCC15D)
                .flags(STICKY, FLAMMABLE));

        RubySlurry = REGISTRATE.material("ruby_slurry", builder -> builder
                .fluid().color(0xff6464));

        SapphireSlurry = REGISTRATE.material("sapphire_slurry", builder -> builder
                .fluid().color(0x6464c8));

        GreenSapphireSlurry = REGISTRATE.material("green_sapphire_slurry", builder -> builder
                .fluid().color(0x64c882));

        // These colors are much nicer looking than those in MC's EnumDyeColor
        DyeBlack = REGISTRATE.material("black_dye", builder -> builder
                .fluid().color(0x202020));

        DyeRed = REGISTRATE.material("red_dye", builder -> builder
                .fluid().color(0xFF0000));

        DyeGreen = REGISTRATE.material("green_dye", builder -> builder
                .fluid().color(0x00FF00));

        DyeBrown = REGISTRATE.material("brown_dye", builder -> builder
                .fluid().color(0x604000));

        DyeBlue = REGISTRATE.material("blue_dye", builder -> builder
                .fluid().color(0x0020FF));

        DyePurple = REGISTRATE.material("purple_dye", builder -> builder
                .fluid().color(0x800080));

        DyeCyan = REGISTRATE.material("cyan_dye", builder -> builder
                .fluid().color(0x00FFFF));

        DyeLightGray = REGISTRATE.material("light_gray_dye", builder -> builder
                .fluid().color(0xC0C0C0));

        DyeGray = REGISTRATE.material("gray_dye", builder -> builder
                .fluid().color(0x808080));

        DyePink = REGISTRATE.material("pink_dye", builder -> builder
                .fluid().color(0xFFC0C0));

        DyeLime = REGISTRATE.material("lime_dye", builder -> builder
                .fluid().color(0x80FF80));

        DyeYellow = REGISTRATE.material("yellow_dye", builder -> builder
                .fluid().color(0xFFFF00));

        DyeLightBlue = REGISTRATE.material("light_blue_dye", builder -> builder
                .fluid().color(0x6080FF));

        DyeMagenta = REGISTRATE.material("magenta_dye", builder -> builder
                .fluid().color(0xFF00FF));

        DyeOrange = REGISTRATE.material("orange_dye", builder -> builder
                .fluid().color(0xFF8000));

        DyeWhite = REGISTRATE.material("white_dye", builder -> builder
                .fluid().color(0xFFFFFF));

        ImpureEnrichedNaquadahSolution = REGISTRATE.material("impure_enriched_naquadah_solution", builder -> builder
                .fluid().color(0x388438));

        EnrichedNaquadahSolution = REGISTRATE.material("enriched_naquadah_solution", builder -> builder
                .fluid().color(0x3AAD3A));

        AcidicEnrichedNaquadahSolution = REGISTRATE.material("acidic_enriched_naquadah_solution", builder -> builder
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x3DD63D));

        EnrichedNaquadahWaste = REGISTRATE.material("enriched_naquadah_waste", builder -> builder
                .fluid().color(0x355B35));

        ImpureNaquadriaSolution = REGISTRATE.material("impure_naquadria_solution", builder -> builder
                .fluid().color(0x518451));

        NaquadriaSolution = REGISTRATE.material("naquadria_solution", builder -> builder
                .fluid().color(0x61AD61));

        AcidicNaquadriaSolution = REGISTRATE.material("acidic_naquadria_solution", builder -> builder
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x70D670));

        NaquadriaWaste = REGISTRATE.material("naquadria_waste", builder -> builder
                .fluid().color(0x425B42));

        Lapotron = REGISTRATE.material("lapotron", builder -> builder
                .gem()
                .color(0x7497ea).secondaryColor(0x1c0b39).iconSet(DIAMOND)
                .flags(DISABLE_MATERIAL_RECIPES)
                .ignoredTagPrefixes(dustTiny, dustSmall));

        TreatedWood = REGISTRATE.material("treated_wood", builder -> builder
                .wood()
                .color(0x644218).secondaryColor(0x4e0b00).iconSet(WOOD)
                .fluidPipeProperties(340, 10, false)
                .flags(GENERATE_PLATE, FLAMMABLE, GENERATE_ROD, GENERATE_FRAME));

        UUMatter = REGISTRATE.material("uu_matter", builder -> builder
                .liquid(new FluidBuilder()
                        .temperature(300)
                        .customStill()));

        PCBCoolant = REGISTRATE.material("pcb_coolant", builder -> builder
                .fluid().color(0xD5D69C)
                .hazard(HazardProperty.HazardTrigger.INHALATION, GTMedicalConditions.CARCINOGEN));

        Sculk = REGISTRATE.material("sculk", builder -> builder
                .dust(1)
                .color(0x015a5c).secondaryColor(0x001616).iconSet(ROUGH));

        Wax = REGISTRATE.material("wax", builder -> builder
                .gem().fluid()
                .color(0xfabf29)
                .flags(NO_SMELTING));

        BauxiteSlurry = REGISTRATE.material("bauxite_slurry", builder -> builder
                .fluid()
                .color(0x051650));

        CrackedBauxiteSlurry = REGISTRATE.material("cracked_bauxite_slurry", builder -> builder
                .liquid(775)
                .color(0x052C50));

        BauxiteSludge = REGISTRATE.material("bauxite_sludge", builder -> builder
                .fluid()
                .color(0x563D2D));

        DecalcifiedBauxiteSludge = REGISTRATE.material("decalcified_bauxite_sludge", builder -> builder
                .fluid()
                .color(0x6F2DA8));

        BauxiteSlag = REGISTRATE.material("bauxite_slag", builder -> builder
                .dust()
                .color(0x6F2DA8).iconSet(SAND));
    }
}
