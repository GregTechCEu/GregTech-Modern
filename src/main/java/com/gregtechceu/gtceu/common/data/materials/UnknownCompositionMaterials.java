package com.gregtechceu.gtceu.common.data.materials;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttributes;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.common.data.GTMedicalConditions;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class UnknownCompositionMaterials {

    public static void register() {
        WoodGas = REGISTRATE.material("wood_gas")
                .gas()
                .color(0xDECD87).secondaryColor(0xdeb287)
                .buildAndRegister();

        WoodVinegar = REGISTRATE.material("wood_vinegar")
                .fluid()
                .color(0xD45500).secondaryColor(0x905800)
                .buildAndRegister();

        WoodTar = REGISTRATE.material("wood_tar")
                .fluid()
                .color(0x3a271a).secondaryColor(0x28170B)
                .flags(STICKY, FLAMMABLE).buildAndRegister();

        CharcoalByproducts = REGISTRATE.material("charcoal_byproducts")
                .fluid().color(0x784421).buildAndRegister();

        Biomass = REGISTRATE.material("biomass")
                .liquid(new FluidBuilder().customStill()).color(0x00FF00).buildAndRegister();

        BioDiesel = REGISTRATE.material("bio_diesel")
                .fluid().color(0xFF8000)
                .flags(FLAMMABLE, EXPLOSIVE).buildAndRegister();

        FermentedBiomass = REGISTRATE.material("fermented_biomass")
                .liquid(new FluidBuilder().temperature(300))
                .color(0x445500)
                .buildAndRegister();

        Creosote = REGISTRATE.material("creosote")
                .liquid(new FluidBuilder().block().customStill().burnTime(6400)).color(0x804000)
                .flags(STICKY).buildAndRegister();

        Diesel = REGISTRATE.material("diesel")
                .liquid(new FluidBuilder().customStill()).flags(FLAMMABLE, EXPLOSIVE).buildAndRegister();

        RocketFuel = REGISTRATE.material("rocket_fuel")
                .fluid().flags(FLAMMABLE, EXPLOSIVE).color(0xBDB78C).buildAndRegister();

        Glue = REGISTRATE.material("glue")
                .liquid(new FluidBuilder().customStill()).flags(STICKY).buildAndRegister();

        Lubricant = REGISTRATE.material("lubricant")
                .liquid(new FluidBuilder().customStill()).buildAndRegister();

        McGuffium239 = REGISTRATE.material("mc_guffium_239")
                .liquid(new FluidBuilder().customStill()).buildAndRegister();

        IndiumConcentrate = REGISTRATE.material("indium_concentrate")
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x0E2950).buildAndRegister();

        SeedOil = REGISTRATE.material("seed_oil")
                .liquid(new FluidBuilder().customStill())
                .color(0xFFFFFF)
                .flags(STICKY, FLAMMABLE).buildAndRegister();

        DrillingFluid = REGISTRATE.material("drilling_fluid")
                .fluid().color(0xFFFFAA).buildAndRegister();

        ConstructionFoam = REGISTRATE.material("construction_foam")
                .fluid().color(0x808080).buildAndRegister();

        SulfuricHeavyFuel = REGISTRATE.material("sulfuric_heavy_fuel")
                .liquid(new FluidBuilder().customStill()).flags(FLAMMABLE).buildAndRegister();

        HeavyFuel = REGISTRATE.material("heavy_fuel")
                .liquid(new FluidBuilder().customStill()).flags(FLAMMABLE).buildAndRegister();

        LightlyHydroCrackedHeavyFuel = REGISTRATE.material("lightly_hydro_cracked_heavy_fuel")
                .langValue("Lightly Hydro-Cracked Heavy Fuel")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xFFFF00).flags(FLAMMABLE).buildAndRegister();

        SeverelyHydroCrackedHeavyFuel = REGISTRATE.material("severely_hydro_cracked_heavy_fuel")
                .langValue("Severely Hydro-Cracked Heavy Fuel")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xFFFF00).flags(FLAMMABLE).buildAndRegister();

        LightlySteamCrackedHeavyFuel = REGISTRATE.material("lightly_steam_cracked_heavy_fuel")
                .langValue("Lightly Steam-Cracked Heavy Fuel")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .flags(FLAMMABLE).buildAndRegister();

        SeverelySteamCrackedHeavyFuel = REGISTRATE.material("severely_steam_cracked_heavy_fuel")
                .langValue("Severely Steam-Cracked Heavy Fuel")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .flags(FLAMMABLE).buildAndRegister();

        SulfuricLightFuel = REGISTRATE.material("sulfuric_light_fuel")
                .liquid(new FluidBuilder()
                        .customStill())
                .flags(FLAMMABLE).buildAndRegister();

        LightFuel = REGISTRATE.material("light_fuel")
                .liquid(new FluidBuilder().customStill()).flags(FLAMMABLE).buildAndRegister();

        LightlyHydroCrackedLightFuel = REGISTRATE.material("lightly_hydro_cracked_light_fuel")
                .langValue("Lightly Hydro-Cracked Light Fuel")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xB7AF08).flags(FLAMMABLE).buildAndRegister();

        SeverelyHydroCrackedLightFuel = REGISTRATE.material("severely_hydro_cracked_light_fuel")
                .langValue("Severely Hydro-Cracked Light Fuel")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xB7AF08).flags(FLAMMABLE).buildAndRegister();

        LightlySteamCrackedLightFuel = REGISTRATE.material("lightly_steam_cracked_light_fuel")
                .langValue("Lightly Steam-Cracked Light Fuel")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .flags(FLAMMABLE).buildAndRegister();

        SeverelySteamCrackedLightFuel = REGISTRATE.material("severely_steam_cracked_light_fuel")
                .langValue("Severely Steam-Cracked Light Fuel")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .flags(FLAMMABLE).buildAndRegister();

        SulfuricNaphtha = REGISTRATE.material("sulfuric_naphtha")
                .liquid(new FluidBuilder().customStill()).flags(FLAMMABLE).buildAndRegister();

        Naphtha = REGISTRATE.material("naphtha")
                .liquid(new FluidBuilder().customStill()).flags(FLAMMABLE).buildAndRegister();

        LightlyHydroCrackedNaphtha = REGISTRATE.material("lightly_hydro_cracked_naphtha")
                .langValue("Lightly Hydro-Cracked Naphtha")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xBFB608).flags(FLAMMABLE).buildAndRegister();

        SeverelyHydroCrackedNaphtha = REGISTRATE.material("severely_hydro_cracked_naphtha")
                .langValue("Severely Hydro-Cracked Naphtha")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xBFB608).flags(FLAMMABLE).buildAndRegister();

        LightlySteamCrackedNaphtha = REGISTRATE.material("lightly_steam_cracked_naphtha")
                .langValue("Lightly Steam-Cracked Naphtha")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xBFB608).flags(FLAMMABLE).buildAndRegister();

        SeverelySteamCrackedNaphtha = REGISTRATE.material("severely_steam_cracked_naphtha")
                .langValue("Severely Steam-Cracked Naphtha")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xBFB608).flags(FLAMMABLE).buildAndRegister();

        SulfuricGas = REGISTRATE.material("sulfuric_gas")
                .gas(new FluidBuilder().customStill())
                .color(0xECDCCC).buildAndRegister();

        RefineryGas = REGISTRATE.material("refinery_gas")
                .gas(new FluidBuilder().customStill())
                .color(0xB4B4B4)
                .flags(FLAMMABLE)
                .buildAndRegister();

        LightlyHydroCrackedGas = REGISTRATE.material("lightly_hydro_cracked_gas")
                .langValue("Lightly Hydro-Cracked Gas")
                .gas(new FluidBuilder().temperature(775))
                .color(0xA0A0A0)
                .flags(FLAMMABLE)
                .buildAndRegister();

        SeverelyHydroCrackedGas = REGISTRATE.material("severely_hydro_cracked_gas")
                .langValue("Severely Hydro-Cracked Gas")
                .gas(new FluidBuilder().temperature(775))
                .color(0xC8C8C8)
                .flags(FLAMMABLE)
                .buildAndRegister();

        LightlySteamCrackedGas = REGISTRATE.material("lightly_steam_cracked_gas")
                .langValue("Lightly Steam-Cracked Gas")
                .gas(new FluidBuilder().temperature(775))
                .color(0xE0E0E0)
                .flags(FLAMMABLE)
                .buildAndRegister();

        SeverelySteamCrackedGas = REGISTRATE.material("severely_steam_cracked_gas")
                .langValue("Severely Steam-Cracked Gas")
                .gas(new FluidBuilder().temperature(775))
                .color(0xE0E0E0).flags(FLAMMABLE).buildAndRegister();

        HydroCrackedEthane = REGISTRATE.material("hydro_cracked_ethane")
                .langValue("Hydro-Cracked Ethane")
                .gas(new FluidBuilder().temperature(775))
                .color(0x9696BC).flags(FLAMMABLE).buildAndRegister();

        HydroCrackedEthylene = REGISTRATE.material("hydro_cracked_ethylene")
                .langValue("Hydro-Cracked Ethylene")
                .gas(new FluidBuilder().temperature(775))
                .color(0xA3A3A0).flags(FLAMMABLE).buildAndRegister();

        HydroCrackedPropene = REGISTRATE.material("hydro_cracked_propene")
                .langValue("Hydro-Cracked Propene")
                .gas(new FluidBuilder().temperature(775))
                .color(0xBEA540).flags(FLAMMABLE).buildAndRegister();

        HydroCrackedPropane = REGISTRATE.material("hydro_cracked_propane")
                .langValue("Hydro-Cracked Propane")
                .gas(new FluidBuilder().temperature(775))
                .color(0xBEA540).flags(FLAMMABLE).buildAndRegister();

        HydroCrackedButane = REGISTRATE.material("hydro_cracked_butane")
                .langValue("Hydro-Cracked Butane")
                .gas(new FluidBuilder().temperature(775))
                .color(0x852C18).flags(FLAMMABLE).buildAndRegister();

        HydroCrackedButene = REGISTRATE.material("hydro_cracked_butene")
                .langValue("Hydro-Cracked Butene")
                .gas(new FluidBuilder().temperature(775))
                .color(0x993E05).flags(FLAMMABLE).buildAndRegister();

        HydroCrackedButadiene = REGISTRATE.material("hydro_cracked_butadiene")
                .langValue("Hydro-Cracked Butadiene")
                .gas(new FluidBuilder().temperature(775))
                .color(0xAD5203).flags(FLAMMABLE).buildAndRegister();

        SteamCrackedEthane = REGISTRATE.material("steam_cracked_ethane")
                .langValue("Steam-Cracked Ethane")
                .gas(new FluidBuilder().temperature(775))
                .color(0x9696BC).flags(FLAMMABLE).buildAndRegister();

        SteamCrackedEthylene = REGISTRATE.material("steam_cracked_ethylene")
                .langValue("Steam-Cracked Ethylene")
                .gas(new FluidBuilder().temperature(775))
                .color(0xA3A3A0).flags(FLAMMABLE).buildAndRegister();

        SteamCrackedPropene = REGISTRATE.material("steam_cracked_propene")
                .langValue("Steam-Cracked Propene")
                .gas(new FluidBuilder().temperature(775))
                .color(0xBEA540).flags(FLAMMABLE).buildAndRegister();

        SteamCrackedPropane = REGISTRATE.material("steam_cracked_propane")
                .langValue("Steam-Cracked Propane")
                .gas(new FluidBuilder().temperature(775))
                .color(0xBEA540).flags(FLAMMABLE).buildAndRegister();

        SteamCrackedButane = REGISTRATE.material("steam_cracked_butane")
                .langValue("Steam-Cracked Butane")
                .gas(new FluidBuilder().temperature(775))
                .color(0x852C18).flags(FLAMMABLE).buildAndRegister();

        SteamCrackedButene = REGISTRATE.material("steam_cracked_butene")
                .langValue("Steam-Cracked Butene")
                .gas(new FluidBuilder().temperature(775))
                .color(0x993E05).flags(FLAMMABLE).buildAndRegister();

        SteamCrackedButadiene = REGISTRATE.material("steam_cracked_butadiene")
                .langValue("Steam-Cracked Butadiene")
                .gas(new FluidBuilder().temperature(775))
                .color(0xAD5203).flags(FLAMMABLE).buildAndRegister();

        LPG = REGISTRATE.material("lpg")
                .langValue("LPG")
                .liquid(new FluidBuilder().customStill())
                .color(0xFCFCAC).flags(FLAMMABLE, EXPLOSIVE).buildAndRegister();

        RawGrowthMedium = REGISTRATE.material("raw_growth_medium")
                .fluid().color(0xA47351).buildAndRegister();

        SterileGrowthMedium = REGISTRATE.material("sterilized_growth_medium")
                .fluid().color(0xAC876E).buildAndRegister();

        Oil = REGISTRATE.material("oil")
                .liquid(new FluidBuilder().block().customStill())
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE)
                .buildAndRegister();

        OilHeavy = REGISTRATE.material("oil_heavy")
                .langValue("Heavy Oil")
                .liquid(new FluidBuilder().block().customStill())
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE)
                .buildAndRegister();

        RawOil = REGISTRATE.material("oil_medium")
                .langValue("Raw Oil")
                .liquid(new FluidBuilder().block().customStill())
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE)
                .buildAndRegister();

        OilLight = REGISTRATE.material("oil_light")
                .langValue("Light Oil")
                .liquid(new FluidBuilder().block().customStill())
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE)
                .buildAndRegister();

        NaturalGas = REGISTRATE.material("natural_gas")
                .gas(new FluidBuilder().block().customStill())
                .flags(FLAMMABLE, EXPLOSIVE).buildAndRegister();

        Bacteria = REGISTRATE.material("bacteria")
                .fluid().color(0x808000).buildAndRegister();

        BacterialSludge = REGISTRATE.material("bacterial_sludge")
                .fluid().color(0x355E3B).buildAndRegister();

        EnrichedBacterialSludge = REGISTRATE.material("enriched_bacterial_sludge")
                .fluid().color(0x7FFF00).buildAndRegister();

        Mutagen = REGISTRATE.material("mutagen")
                .fluid().color(0x00FF7F).buildAndRegister();

        GelatinMixture = REGISTRATE.material("gelatin_mixture")
                .fluid().color(0x588BAE).buildAndRegister();

        RawGasoline = REGISTRATE.material("raw_gasoline")
                .fluid().color(0xFF6400).flags(FLAMMABLE).buildAndRegister();

        Gasoline = REGISTRATE.material("gasoline")
                .fluid().color(0xFAA500).flags(FLAMMABLE, EXPLOSIVE).buildAndRegister();

        HighOctaneGasoline = REGISTRATE.material("high_octane_gasoline")
                .fluid().color(0xFFA500).flags(FLAMMABLE, EXPLOSIVE).buildAndRegister();

        CoalGas = REGISTRATE.material("coal_gas")
                .gas().color(0x333333).buildAndRegister();

        CoalTar = REGISTRATE.material("coal_tar")
                .fluid().color(0x1A1A1A).flags(STICKY, FLAMMABLE).buildAndRegister();

        Gunpowder = REGISTRATE.material("gunpowder")
                .dust(0)
                .color(0xa4a4a4).secondaryColor(0x767676).iconSet(ROUGH)
                .flags(FLAMMABLE, EXPLOSIVE, NO_SMELTING, NO_SMASHING)
                .components(Saltpeter, 2, Sulfur, 1, Carbon, 3)
                .buildAndRegister();

        Oilsands = REGISTRATE.material("oilsands")
                .dust(1).ore()
                .color(0xe3c78a).secondaryColor(0x161e22).iconSet(SAND)
                .flags(FLAMMABLE)
                .buildAndRegister();

        RareEarth = REGISTRATE.material("rare_earth")
                .dust(0)
                .color(0xffdc88).secondaryColor(0xe99673).iconSet(FINE)
                .buildAndRegister();

        Stone = REGISTRATE.material("stone")
                .dust(2)
                .color(0x8f8f8f).secondaryColor(0x898989).iconSet(ROUGH)
                .flags(MORTAR_GRINDABLE, GENERATE_GEAR, NO_SMASHING, NO_SMELTING)
                .buildAndRegister();

        Lava = REGISTRATE.material("lava")
                .fluid().color(0xFF4000).buildAndRegister();

        Netherite = REGISTRATE.material("netherite")
                .ingot().color(0x4b4042).secondaryColor(0x474447)
                .flags(FIRE_RESISTANT)
                .toolStats(ToolProperty.Builder.of(10.0F, 4.0F, 2032, 4)
                        .enchantability(21).build())
                .buildAndRegister();

        Glowstone = REGISTRATE.material("glowstone")
                .dust(1)
                .liquid(new FluidBuilder().temperature(500))
                .color(0xfcb34c).secondaryColor(0xce7533).iconSet(SHINY)
                .flags(NO_SMASHING, GENERATE_PLATE, EXCLUDE_PLATE_COMPRESSOR_RECIPE,
                        EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .buildAndRegister();

        NetherStar = REGISTRATE.material("nether_star")
                .gem(4)
                .color(0xfeffc6).secondaryColor(0x7fd7e2)
                .iconSet(NETHERSTAR)
                .flags(NO_SMASHING, NO_SMELTING, GENERATE_LENS)
                .buildAndRegister();

        Endstone = REGISTRATE.material("endstone")
                .dust(1)
                .color(0xf6fabd).secondaryColor(0xc5be8b).iconSet(ROUGH)
                .flags(NO_SMASHING)
                .buildAndRegister();

        Netherrack = REGISTRATE.material("netherrack")
                .dust(1)
                .color(0x7c4249).secondaryColor(0x400b0b).iconSet(ROUGH)
                .flags(NO_SMASHING, FLAMMABLE)
                .buildAndRegister();

        CetaneBoostedDiesel = REGISTRATE.material("cetane_boosted_diesel")
                .liquid(new FluidBuilder().customStill())
                .color(0xC8FF00)
                .flags(FLAMMABLE, EXPLOSIVE)
                .buildAndRegister();

        Collagen = REGISTRATE.material("collagen")
                .dust(1)
                .color(0xffadb7).secondaryColor(0x80471C).iconSet(ROUGH)
                .buildAndRegister();

        Gelatin = REGISTRATE.material("gelatin")
                .dust(1)
                .color(0xfaf7cb).secondaryColor(0x693d00).iconSet(ROUGH)
                .buildAndRegister();

        Agar = REGISTRATE.material("agar")
                .dust(1)
                .color(0xbdd168).secondaryColor(0x403218).iconSet(ROUGH)
                .buildAndRegister();

        Milk = REGISTRATE.material("milk")
                .liquid(new FluidBuilder()
                        .temperature(295)
                        .customStill())
                .color(0xfffbf0).secondaryColor(0xf6eac8).iconSet(FINE)
                .buildAndRegister();

        Cocoa = REGISTRATE.material("cocoa")
                .dust(0)
                .color(0x976746).secondaryColor(0x301a0a).iconSet(FINE)
                .buildAndRegister();

        Wheat = REGISTRATE.material("wheat")
                .dust(0)
                .color(0xdcbb65).secondaryColor(0x565138).iconSet(FINE)
                .buildAndRegister();

        Meat = REGISTRATE.material("meat")
                .dust(1)
                .color(0xe85048).secondaryColor(0x470a06).iconSet(SAND)
                .buildAndRegister();

        Wood = REGISTRATE.material("wood")
                .wood()
                .color(0xc29f6d).secondaryColor(0x643200).iconSet(WOOD)
                .fluidPipeProperties(340, 5, false)
                .toolStats(ToolProperty.Builder.of(1.0F, 1.0F, 128, 1, GTToolType.SOFT_MALLET).build())
                .flags(GENERATE_PLATE, GENERATE_ROD, GENERATE_BOLT_SCREW, GENERATE_LONG_ROD, FLAMMABLE, GENERATE_GEAR,
                        GENERATE_FRAME)
                .buildAndRegister();

        Paper = REGISTRATE.material("paper")
                .dust(0)
                .color(0xF9F9F9).secondaryColor(0xECECEC).iconSet(DULL)
                .flags(GENERATE_PLATE, FLAMMABLE, NO_SMELTING, NO_SMASHING,
                        MORTAR_GRINDABLE, EXCLUDE_PLATE_COMPRESSOR_RECIPE)
                .buildAndRegister();

        FishOil = REGISTRATE.material("fish_oil")
                .fluid()
                .color(0xDCC15D)
                .flags(STICKY, FLAMMABLE)
                .buildAndRegister();

        RubySlurry = REGISTRATE.material("ruby_slurry")
                .fluid().color(0xff6464).buildAndRegister();

        SapphireSlurry = REGISTRATE.material("sapphire_slurry")
                .fluid().color(0x6464c8).buildAndRegister();

        GreenSapphireSlurry = REGISTRATE.material("green_sapphire_slurry")
                .fluid().color(0x64c882).buildAndRegister();

        // These colors are much nicer looking than those in MC's EnumDyeColor
        DyeBlack = REGISTRATE.material("black_dye")
                .fluid().color(0x202020).buildAndRegister();

        DyeRed = REGISTRATE.material("red_dye")
                .fluid().color(0xFF0000).buildAndRegister();

        DyeGreen = REGISTRATE.material("green_dye")
                .fluid().color(0x00FF00).buildAndRegister();

        DyeBrown = REGISTRATE.material("brown_dye")
                .fluid().color(0x604000).buildAndRegister();

        DyeBlue = REGISTRATE.material("blue_dye")
                .fluid().color(0x0020FF).buildAndRegister();

        DyePurple = REGISTRATE.material("purple_dye")
                .fluid().color(0x800080).buildAndRegister();

        DyeCyan = REGISTRATE.material("cyan_dye")
                .fluid().color(0x00FFFF).buildAndRegister();

        DyeLightGray = REGISTRATE.material("light_gray_dye")
                .fluid().color(0xC0C0C0).buildAndRegister();

        DyeGray = REGISTRATE.material("gray_dye")
                .fluid().color(0x808080).buildAndRegister();

        DyePink = REGISTRATE.material("pink_dye")
                .fluid().color(0xFFC0C0).buildAndRegister();

        DyeLime = REGISTRATE.material("lime_dye")
                .fluid().color(0x80FF80).buildAndRegister();

        DyeYellow = REGISTRATE.material("yellow_dye")
                .fluid().color(0xFFFF00).buildAndRegister();

        DyeLightBlue = REGISTRATE.material("light_blue_dye")
                .fluid().color(0x6080FF).buildAndRegister();

        DyeMagenta = REGISTRATE.material("magenta_dye")
                .fluid().color(0xFF00FF).buildAndRegister();

        DyeOrange = REGISTRATE.material("orange_dye")
                .fluid().color(0xFF8000).buildAndRegister();

        DyeWhite = REGISTRATE.material("white_dye")
                .fluid().color(0xFFFFFF).buildAndRegister();

        ImpureEnrichedNaquadahSolution = REGISTRATE.material("impure_enriched_naquadah_solution")
                .fluid().color(0x388438).buildAndRegister();

        EnrichedNaquadahSolution = REGISTRATE.material("enriched_naquadah_solution")
                .fluid().color(0x3AAD3A).buildAndRegister();

        AcidicEnrichedNaquadahSolution = REGISTRATE.material("acidic_enriched_naquadah_solution")
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x3DD63D).buildAndRegister();

        EnrichedNaquadahWaste = REGISTRATE.material("enriched_naquadah_waste")
                .fluid().color(0x355B35).buildAndRegister();

        ImpureNaquadriaSolution = REGISTRATE.material("impure_naquadria_solution")
                .fluid().color(0x518451).buildAndRegister();

        NaquadriaSolution = REGISTRATE.material("naquadria_solution")
                .fluid().color(0x61AD61).buildAndRegister();

        AcidicNaquadriaSolution = REGISTRATE.material("acidic_naquadria_solution")
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x70D670).buildAndRegister();

        NaquadriaWaste = REGISTRATE.material("naquadria_waste")
                .fluid().color(0x425B42).buildAndRegister();

        Lapotron = REGISTRATE.material("lapotron")
                .gem()
                .color(0x7497ea).secondaryColor(0x1c0b39).iconSet(DIAMOND)
                .flags(DISABLE_MATERIAL_RECIPES)
                .ignoredTagPrefixes(dustTiny, dustSmall)
                .buildAndRegister();

        TreatedWood = REGISTRATE.material("treated_wood")
                .wood()
                .color(0x644218).secondaryColor(0x4e0b00).iconSet(WOOD)
                .fluidPipeProperties(340, 10, false)
                .flags(GENERATE_PLATE, FLAMMABLE, GENERATE_ROD, GENERATE_FRAME)
                .buildAndRegister();

        UUMatter = REGISTRATE.material("uu_matter")
                .langValue("UU-Matter")
                .liquid(new FluidBuilder()
                        .temperature(300)
                        .customStill())
                .buildAndRegister();

        PCBCoolant = REGISTRATE.material("pcb_coolant")
                .langValue("PCB Coolant")
                .fluid().color(0xD5D69C)
                .hazard(HazardProperty.HazardTrigger.INHALATION, GTMedicalConditions.CARCINOGEN)
                .buildAndRegister();

        Sculk = REGISTRATE.material("sculk")
                .dust(1)
                .color(0x015a5c).secondaryColor(0x001616).iconSet(ROUGH)
                .buildAndRegister();

        Wax = REGISTRATE.material("wax")
                .ingot().fluid()
                .color(0xfabf29)
                .flags(NO_SMELTING)
                .buildAndRegister();

        BauxiteSlurry = REGISTRATE.material("bauxite_slurry")
                .fluid()
                .color(0x051650)
                .buildAndRegister();

        CrackedBauxiteSlurry = REGISTRATE.material("cracked_bauxite_slurry")
                .liquid(new FluidBuilder()
                        .temperature(775))
                .color(0x052C50)
                .buildAndRegister();

        BauxiteSludge = REGISTRATE.material("bauxite_sludge")
                .fluid()
                .color(0x563D2D)
                .buildAndRegister();

        DecalcifiedBauxiteSludge = REGISTRATE.material("decalcified_bauxite_sludge")
                .fluid()
                .color(0x6F2DA8)
                .buildAndRegister();

        BauxiteSlag = REGISTRATE.material("bauxite_slag")
                .dust()
                .color(0x6F2DA8).iconSet(SAND)
                .buildAndRegister();
    }
}
