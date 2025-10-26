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

public class UnknownCompositionMaterials {

    public static void register() {
        WoodGas = new Material.Builder(GTCEu.id("wood_gas"))
                .gas()
                .color(0xDECD87).secondaryColor(0xdeb287)
                .buildAndRegister();

        WoodVinegar = new Material.Builder(GTCEu.id("wood_vinegar"))
                .fluid()
                .color(0xD45500).secondaryColor(0x905800)
                .buildAndRegister();

        WoodTar = new Material.Builder(GTCEu.id("wood_tar"))
                .fluid()
                .color(0x3a271a).secondaryColor(0x28170B)
                .flags(STICKY, FLAMMABLE).buildAndRegister();

        CharcoalByproducts = new Material.Builder(GTCEu.id("charcoal_byproducts"))
                .fluid().color(0x784421).buildAndRegister();

        Biomass = new Material.Builder(GTCEu.id("biomass"))
                .liquid(new FluidBuilder().customStill()).color(0x00FF00).buildAndRegister();

        BioDiesel = new Material.Builder(GTCEu.id("bio_diesel"))
                .fluid().color(0xFF8000)
                .flags(FLAMMABLE, EXPLOSIVE).buildAndRegister();

        FermentedBiomass = new Material.Builder(GTCEu.id("fermented_biomass"))
                .liquid(new FluidBuilder().temperature(300))
                .color(0x445500)
                .buildAndRegister();

        Creosote = new Material.Builder(GTCEu.id("creosote"))
                .liquid(new FluidBuilder().block().customStill().burnTime(6400)).color(0x804000)
                .flags(STICKY).buildAndRegister();

        Diesel = new Material.Builder(GTCEu.id("diesel"))
                .liquid(new FluidBuilder().customStill()).flags(FLAMMABLE, EXPLOSIVE).buildAndRegister();

        RocketFuel = new Material.Builder(GTCEu.id("rocket_fuel"))
                .fluid().flags(FLAMMABLE, EXPLOSIVE).color(0xBDB78C).buildAndRegister();

        Glue = new Material.Builder(GTCEu.id("glue"))
                .liquid(new FluidBuilder().customStill()).flags(STICKY).buildAndRegister();

        Lubricant = new Material.Builder(GTCEu.id("lubricant"))
                .liquid(new FluidBuilder().customStill()).buildAndRegister();

        McGuffium239 = new Material.Builder(GTCEu.id("mc_guffium_239"))
                .liquid(new FluidBuilder().customStill()).buildAndRegister();

        IndiumConcentrate = new Material.Builder(GTCEu.id("indium_concentrate"))
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x0E2950).buildAndRegister();

        SeedOil = new Material.Builder(GTCEu.id("seed_oil"))
                .liquid(new FluidBuilder().customStill())
                .color(0xFFFFFF)
                .flags(STICKY, FLAMMABLE).buildAndRegister();

        DrillingFluid = new Material.Builder(GTCEu.id("drilling_fluid"))
                .fluid().color(0xFFFFAA).buildAndRegister();

        ConstructionFoam = new Material.Builder(GTCEu.id("construction_foam"))
                .fluid().color(0x808080).buildAndRegister();

        SulfuricHeavyFuel = new Material.Builder(GTCEu.id("sulfuric_heavy_fuel"))
                .liquid(new FluidBuilder().customStill()).flags(FLAMMABLE).buildAndRegister();

        HeavyFuel = new Material.Builder(GTCEu.id("heavy_fuel"))
                .liquid(new FluidBuilder().customStill()).flags(FLAMMABLE).buildAndRegister();

        LightlyHydroCrackedHeavyFuel = new Material.Builder(GTCEu.id("lightly_hydro_cracked_heavy_fuel"))
                .langValue("Lightly Hydro-Cracked Heavy Fuel")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xFFFF00).flags(FLAMMABLE).buildAndRegister();

        SeverelyHydroCrackedHeavyFuel = new Material.Builder(GTCEu.id("severely_hydro_cracked_heavy_fuel"))
                .langValue("Severely Hydro-Cracked Heavy Fuel")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xFFFF00).flags(FLAMMABLE).buildAndRegister();

        LightlySteamCrackedHeavyFuel = new Material.Builder(GTCEu.id("lightly_steam_cracked_heavy_fuel"))
                .langValue("Lightly Steam-Cracked Heavy Fuel")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .flags(FLAMMABLE).buildAndRegister();

        SeverelySteamCrackedHeavyFuel = new Material.Builder(GTCEu.id("severely_steam_cracked_heavy_fuel"))
                .langValue("Severely Steam-Cracked Heavy Fuel")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .flags(FLAMMABLE).buildAndRegister();

        SulfuricLightFuel = new Material.Builder(GTCEu.id("sulfuric_light_fuel"))
                .liquid(new FluidBuilder()
                        .customStill())
                .flags(FLAMMABLE).buildAndRegister();

        LightFuel = new Material.Builder(GTCEu.id("light_fuel"))
                .liquid(new FluidBuilder().customStill()).flags(FLAMMABLE).buildAndRegister();

        LightlyHydroCrackedLightFuel = new Material.Builder(GTCEu.id("lightly_hydro_cracked_light_fuel"))
                .langValue("Lightly Hydro-Cracked Light Fuel")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xB7AF08).flags(FLAMMABLE).buildAndRegister();

        SeverelyHydroCrackedLightFuel = new Material.Builder(GTCEu.id("severely_hydro_cracked_light_fuel"))
                .langValue("Severely Hydro-Cracked Light Fuel")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xB7AF08).flags(FLAMMABLE).buildAndRegister();

        LightlySteamCrackedLightFuel = new Material.Builder(GTCEu.id("lightly_steam_cracked_light_fuel"))
                .langValue("Lightly Steam-Cracked Light Fuel")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .flags(FLAMMABLE).buildAndRegister();

        SeverelySteamCrackedLightFuel = new Material.Builder(GTCEu.id("severely_steam_cracked_light_fuel"))
                .langValue("Severely Steam-Cracked Light Fuel")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .flags(FLAMMABLE).buildAndRegister();

        SulfuricNaphtha = new Material.Builder(GTCEu.id("sulfuric_naphtha"))
                .liquid(new FluidBuilder().customStill()).flags(FLAMMABLE).buildAndRegister();

        Naphtha = new Material.Builder(GTCEu.id("naphtha"))
                .liquid(new FluidBuilder().customStill()).flags(FLAMMABLE).buildAndRegister();

        LightlyHydroCrackedNaphtha = new Material.Builder(GTCEu.id("lightly_hydro_cracked_naphtha"))
                .langValue("Lightly Hydro-Cracked Naphtha")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xBFB608).flags(FLAMMABLE).buildAndRegister();

        SeverelyHydroCrackedNaphtha = new Material.Builder(GTCEu.id("severely_hydro_cracked_naphtha"))
                .langValue("Severely Hydro-Cracked Naphtha")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xBFB608).flags(FLAMMABLE).buildAndRegister();

        LightlySteamCrackedNaphtha = new Material.Builder(GTCEu.id("lightly_steam_cracked_naphtha"))
                .langValue("Lightly Steam-Cracked Naphtha")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xBFB608).flags(FLAMMABLE).buildAndRegister();

        SeverelySteamCrackedNaphtha = new Material.Builder(GTCEu.id("severely_steam_cracked_naphtha"))
                .langValue("Severely Steam-Cracked Naphtha")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xBFB608).flags(FLAMMABLE).buildAndRegister();

        SulfuricGas = new Material.Builder(GTCEu.id("sulfuric_gas"))
                .gas(new FluidBuilder().customStill())
                .color(0xECDCCC).buildAndRegister();

        RefineryGas = new Material.Builder(GTCEu.id("refinery_gas"))
                .gas(new FluidBuilder().customStill())
                .color(0xB4B4B4)
                .flags(FLAMMABLE)
                .buildAndRegister();

        LightlyHydroCrackedGas = new Material.Builder(GTCEu.id("lightly_hydro_cracked_gas"))
                .langValue("Lightly Hydro-Cracked Gas")
                .gas(new FluidBuilder().temperature(775))
                .color(0xA0A0A0)
                .flags(FLAMMABLE)
                .buildAndRegister();

        SeverelyHydroCrackedGas = new Material.Builder(GTCEu.id("severely_hydro_cracked_gas"))
                .langValue("Severely Hydro-Cracked Gas")
                .gas(new FluidBuilder().temperature(775))
                .color(0xC8C8C8)
                .flags(FLAMMABLE)
                .buildAndRegister();

        LightlySteamCrackedGas = new Material.Builder(GTCEu.id("lightly_steam_cracked_gas"))
                .langValue("Lightly Steam-Cracked Gas")
                .gas(new FluidBuilder().temperature(775))
                .color(0xE0E0E0)
                .flags(FLAMMABLE)
                .buildAndRegister();

        SeverelySteamCrackedGas = new Material.Builder(GTCEu.id("severely_steam_cracked_gas"))
                .langValue("Severely Steam-Cracked Gas")
                .gas(new FluidBuilder().temperature(775))
                .color(0xE0E0E0).flags(FLAMMABLE).buildAndRegister();

        HydroCrackedEthane = new Material.Builder(GTCEu.id("hydro_cracked_ethane"))
                .langValue("Hydro-Cracked Ethane")
                .gas(new FluidBuilder().temperature(775))
                .color(0x9696BC).flags(FLAMMABLE).buildAndRegister();

        HydroCrackedEthylene = new Material.Builder(GTCEu.id("hydro_cracked_ethylene"))
                .langValue("Hydro-Cracked Ethylene")
                .gas(new FluidBuilder().temperature(775))
                .color(0xA3A3A0).flags(FLAMMABLE).buildAndRegister();

        HydroCrackedPropene = new Material.Builder(GTCEu.id("hydro_cracked_propene"))
                .langValue("Hydro-Cracked Propene")
                .gas(new FluidBuilder().temperature(775))
                .color(0xBEA540).flags(FLAMMABLE).buildAndRegister();

        HydroCrackedPropane = new Material.Builder(GTCEu.id("hydro_cracked_propane"))
                .langValue("Hydro-Cracked Propane")
                .gas(new FluidBuilder().temperature(775))
                .color(0xBEA540).flags(FLAMMABLE).buildAndRegister();

        HydroCrackedButane = new Material.Builder(GTCEu.id("hydro_cracked_butane"))
                .langValue("Hydro-Cracked Butane")
                .gas(new FluidBuilder().temperature(775))
                .color(0x852C18).flags(FLAMMABLE).buildAndRegister();

        HydroCrackedButene = new Material.Builder(GTCEu.id("hydro_cracked_butene"))
                .langValue("Hydro-Cracked Butene")
                .gas(new FluidBuilder().temperature(775))
                .color(0x993E05).flags(FLAMMABLE).buildAndRegister();

        HydroCrackedButadiene = new Material.Builder(GTCEu.id("hydro_cracked_butadiene"))
                .langValue("Hydro-Cracked Butadiene")
                .gas(new FluidBuilder().temperature(775))
                .color(0xAD5203).flags(FLAMMABLE).buildAndRegister();

        SteamCrackedEthane = new Material.Builder(GTCEu.id("steam_cracked_ethane"))
                .langValue("Steam-Cracked Ethane")
                .gas(new FluidBuilder().temperature(775))
                .color(0x9696BC).flags(FLAMMABLE).buildAndRegister();

        SteamCrackedEthylene = new Material.Builder(GTCEu.id("steam_cracked_ethylene"))
                .langValue("Steam-Cracked Ethylene")
                .gas(new FluidBuilder().temperature(775))
                .color(0xA3A3A0).flags(FLAMMABLE).buildAndRegister();

        SteamCrackedPropene = new Material.Builder(GTCEu.id("steam_cracked_propene"))
                .langValue("Steam-Cracked Propene")
                .gas(new FluidBuilder().temperature(775))
                .color(0xBEA540).flags(FLAMMABLE).buildAndRegister();

        SteamCrackedPropane = new Material.Builder(GTCEu.id("steam_cracked_propane"))
                .langValue("Steam-Cracked Propane")
                .gas(new FluidBuilder().temperature(775))
                .color(0xBEA540).flags(FLAMMABLE).buildAndRegister();

        SteamCrackedButane = new Material.Builder(GTCEu.id("steam_cracked_butane"))
                .langValue("Steam-Cracked Butane")
                .gas(new FluidBuilder().temperature(775))
                .color(0x852C18).flags(FLAMMABLE).buildAndRegister();

        SteamCrackedButene = new Material.Builder(GTCEu.id("steam_cracked_butene"))
                .langValue("Steam-Cracked Butene")
                .gas(new FluidBuilder().temperature(775))
                .color(0x993E05).flags(FLAMMABLE).buildAndRegister();

        SteamCrackedButadiene = new Material.Builder(GTCEu.id("steam_cracked_butadiene"))
                .langValue("Steam-Cracked Butadiene")
                .gas(new FluidBuilder().temperature(775))
                .color(0xAD5203).flags(FLAMMABLE).buildAndRegister();

        LPG = new Material.Builder(GTCEu.id("lpg"))
                .langValue("LPG")
                .liquid(new FluidBuilder().customStill())
                .color(0xFCFCAC).flags(FLAMMABLE, EXPLOSIVE).buildAndRegister();

        RawGrowthMedium = new Material.Builder(GTCEu.id("raw_growth_medium"))
                .fluid().color(0xA47351).buildAndRegister();

        SterileGrowthMedium = new Material.Builder(GTCEu.id("sterilized_growth_medium"))
                .fluid().color(0xAC876E).buildAndRegister();

        Oil = new Material.Builder(GTCEu.id("oil"))
                .liquid(new FluidBuilder().block().customStill())
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE)
                .buildAndRegister();

        OilHeavy = new Material.Builder(GTCEu.id("oil_heavy"))
                .langValue("Heavy Oil")
                .liquid(new FluidBuilder().block().customStill())
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE)
                .buildAndRegister();

        RawOil = new Material.Builder(GTCEu.id("oil_medium"))
                .langValue("Raw Oil")
                .liquid(new FluidBuilder().block().customStill())
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE)
                .buildAndRegister();

        OilLight = new Material.Builder(GTCEu.id("oil_light"))
                .langValue("Light Oil")
                .liquid(new FluidBuilder().block().customStill())
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE)
                .buildAndRegister();

        NaturalGas = new Material.Builder(GTCEu.id("natural_gas"))
                .gas(new FluidBuilder().block().customStill())
                .flags(FLAMMABLE, EXPLOSIVE).buildAndRegister();

        Bacteria = new Material.Builder(GTCEu.id("bacteria"))
                .fluid().color(0x808000).buildAndRegister();

        BacterialSludge = new Material.Builder(GTCEu.id("bacterial_sludge"))
                .fluid().color(0x355E3B).buildAndRegister();

        EnrichedBacterialSludge = new Material.Builder(GTCEu.id("enriched_bacterial_sludge"))
                .fluid().color(0x7FFF00).buildAndRegister();

        Mutagen = new Material.Builder(GTCEu.id("mutagen"))
                .fluid().color(0x00FF7F).buildAndRegister();

        GelatinMixture = new Material.Builder(GTCEu.id("gelatin_mixture"))
                .fluid().color(0x588BAE).buildAndRegister();

        RawGasoline = new Material.Builder(GTCEu.id("raw_gasoline"))
                .fluid().color(0xFF6400).flags(FLAMMABLE).buildAndRegister();

        Gasoline = new Material.Builder(GTCEu.id("gasoline"))
                .fluid().color(0xFAA500).flags(FLAMMABLE, EXPLOSIVE).buildAndRegister();

        HighOctaneGasoline = new Material.Builder(GTCEu.id("high_octane_gasoline"))
                .fluid().color(0xFFA500).flags(FLAMMABLE, EXPLOSIVE).buildAndRegister();

        CoalGas = new Material.Builder(GTCEu.id("coal_gas"))
                .gas().color(0x333333).buildAndRegister();

        CoalTar = new Material.Builder(GTCEu.id("coal_tar"))
                .fluid().color(0x1A1A1A).flags(STICKY, FLAMMABLE).buildAndRegister();

        Gunpowder = new Material.Builder(GTCEu.id("gunpowder"))
                .dust(0)
                .color(0xa4a4a4).secondaryColor(0x767676).iconSet(ROUGH)
                .flags(FLAMMABLE, EXPLOSIVE, NO_SMELTING, NO_SMASHING)
                .components(Saltpeter, 2, Sulfur, 1, Coal, 3)
                .buildAndRegister();

        Oilsands = new Material.Builder(GTCEu.id("oilsands"))
                .dust(1).ore()
                .color(0xe3c78a).secondaryColor(0x161e22).iconSet(SAND)
                .flags(FLAMMABLE)
                .buildAndRegister();

        RareEarth = new Material.Builder(GTCEu.id("rare_earth"))
                .dust(0)
                .color(0xffdc88).secondaryColor(0xe99673).iconSet(FINE)
                .buildAndRegister();

        Stone = new Material.Builder(GTCEu.id("stone"))
                .dust(2)
                .color(0x8f8f8f).secondaryColor(0x898989).iconSet(ROUGH)
                .flags(MORTAR_GRINDABLE, GENERATE_GEAR, NO_SMASHING, NO_SMELTING)
                .buildAndRegister();

        Lava = new Material.Builder(GTCEu.id("lava"))
                .fluid().color(0xFF4000).buildAndRegister();

        Netherite = new Material.Builder(GTCEu.id("netherite"))
                .ingot().color(0x4b4042).secondaryColor(0x474447)
                .toolStats(ToolProperty.Builder.of(10.0F, 4.0F, 2032, 4)
                        .enchantability(21).build())
                .buildAndRegister();

        Glowstone = new Material.Builder(GTCEu.id("glowstone"))
                .dust(1)
                .liquid(new FluidBuilder().temperature(500))
                .color(0xfcb34c).secondaryColor(0xce7533).iconSet(SHINY)
                .flags(NO_SMASHING, GENERATE_PLATE, EXCLUDE_PLATE_COMPRESSOR_RECIPE,
                        EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .buildAndRegister();

        NetherStar = new Material.Builder(GTCEu.id("nether_star"))
                .gem(4)
                .color(0xfeffc6).secondaryColor(0x7fd7e2)
                .iconSet(NETHERSTAR)
                .flags(NO_SMASHING, NO_SMELTING, GENERATE_LENS)
                .buildAndRegister();

        Endstone = new Material.Builder(GTCEu.id("endstone"))
                .dust(1)
                .color(0xf6fabd).secondaryColor(0xc5be8b).iconSet(ROUGH)
                .flags(NO_SMASHING)
                .buildAndRegister();

        Netherrack = new Material.Builder(GTCEu.id("netherrack"))
                .dust(1)
                .color(0x7c4249).secondaryColor(0x400b0b).iconSet(ROUGH)
                .flags(NO_SMASHING, FLAMMABLE)
                .buildAndRegister();

        CetaneBoostedDiesel = new Material.Builder(GTCEu.id("cetane_boosted_diesel"))
                .liquid(new FluidBuilder().customStill())
                .color(0xC8FF00)
                .flags(FLAMMABLE, EXPLOSIVE)
                .buildAndRegister();

        Collagen = new Material.Builder(GTCEu.id("collagen"))
                .dust(1)
                .color(0xffadb7).secondaryColor(0x80471C).iconSet(ROUGH)
                .buildAndRegister();

        Gelatin = new Material.Builder(GTCEu.id("gelatin"))
                .dust(1)
                .color(0xfaf7cb).secondaryColor(0x693d00).iconSet(ROUGH)
                .buildAndRegister();

        Agar = new Material.Builder(GTCEu.id("agar"))
                .dust(1)
                .color(0xbdd168).secondaryColor(0x403218).iconSet(ROUGH)
                .buildAndRegister();

        Milk = new Material.Builder(GTCEu.id("milk"))
                .liquid(new FluidBuilder()
                        .temperature(295)
                        .customStill())
                .color(0xfffbf0).secondaryColor(0xf6eac8).iconSet(FINE)
                .buildAndRegister();

        Cocoa = new Material.Builder(GTCEu.id("cocoa"))
                .dust(0)
                .color(0x976746).secondaryColor(0x301a0a).iconSet(FINE)
                .buildAndRegister();

        Wheat = new Material.Builder(GTCEu.id("wheat"))
                .dust(0)
                .color(0xdcbb65).secondaryColor(0x565138).iconSet(FINE)
                .buildAndRegister();

        Meat = new Material.Builder(GTCEu.id("meat"))
                .dust(1)
                .color(0xe85048).secondaryColor(0x470a06).iconSet(SAND)
                .buildAndRegister();

        Wood = new Material.Builder(GTCEu.id("wood"))
                .wood()
                .color(0xc29f6d).secondaryColor(0x643200).iconSet(WOOD)
                .fluidPipeProperties(340, 5, false)
                .toolStats(ToolProperty.Builder.of(1.0F, 1.0F, 128, 1, GTToolType.SOFT_MALLET).build())
                .flags(GENERATE_PLATE, GENERATE_ROD, GENERATE_BOLT_SCREW, GENERATE_LONG_ROD, FLAMMABLE, GENERATE_GEAR,
                        GENERATE_FRAME)
                .buildAndRegister();

        Paper = new Material.Builder(GTCEu.id("paper"))
                .dust(0)
                .color(0xF9F9F9).secondaryColor(0xECECEC).iconSet(DULL)
                .flags(GENERATE_PLATE, FLAMMABLE, NO_SMELTING, NO_SMASHING,
                        MORTAR_GRINDABLE, EXCLUDE_PLATE_COMPRESSOR_RECIPE)
                .buildAndRegister();

        FishOil = new Material.Builder(GTCEu.id("fish_oil"))
                .fluid()
                .color(0xDCC15D)
                .flags(STICKY, FLAMMABLE)
                .buildAndRegister();

        RubySlurry = new Material.Builder(GTCEu.id("ruby_slurry"))
                .fluid().color(0xff6464).buildAndRegister();

        SapphireSlurry = new Material.Builder(GTCEu.id("sapphire_slurry"))
                .fluid().color(0x6464c8).buildAndRegister();

        GreenSapphireSlurry = new Material.Builder(GTCEu.id("green_sapphire_slurry"))
                .fluid().color(0x64c882).buildAndRegister();

        // These colors are much nicer looking than those in MC's EnumDyeColor
        DyeBlack = new Material.Builder(GTCEu.id("black_dye"))
                .fluid().color(0x202020).buildAndRegister();

        DyeRed = new Material.Builder(GTCEu.id("red_dye"))
                .fluid().color(0xFF0000).buildAndRegister();

        DyeGreen = new Material.Builder(GTCEu.id("green_dye"))
                .fluid().color(0x00FF00).buildAndRegister();

        DyeBrown = new Material.Builder(GTCEu.id("brown_dye"))
                .fluid().color(0x604000).buildAndRegister();

        DyeBlue = new Material.Builder(GTCEu.id("blue_dye"))
                .fluid().color(0x0020FF).buildAndRegister();

        DyePurple = new Material.Builder(GTCEu.id("purple_dye"))
                .fluid().color(0x800080).buildAndRegister();

        DyeCyan = new Material.Builder(GTCEu.id("cyan_dye"))
                .fluid().color(0x00FFFF).buildAndRegister();

        DyeLightGray = new Material.Builder(GTCEu.id("light_gray_dye"))
                .fluid().color(0xC0C0C0).buildAndRegister();

        DyeGray = new Material.Builder(GTCEu.id("gray_dye"))
                .fluid().color(0x808080).buildAndRegister();

        DyePink = new Material.Builder(GTCEu.id("pink_dye"))
                .fluid().color(0xFFC0C0).buildAndRegister();

        DyeLime = new Material.Builder(GTCEu.id("lime_dye"))
                .fluid().color(0x80FF80).buildAndRegister();

        DyeYellow = new Material.Builder(GTCEu.id("yellow_dye"))
                .fluid().color(0xFFFF00).buildAndRegister();

        DyeLightBlue = new Material.Builder(GTCEu.id("light_blue_dye"))
                .fluid().color(0x6080FF).buildAndRegister();

        DyeMagenta = new Material.Builder(GTCEu.id("magenta_dye"))
                .fluid().color(0xFF00FF).buildAndRegister();

        DyeOrange = new Material.Builder(GTCEu.id("orange_dye"))
                .fluid().color(0xFF8000).buildAndRegister();

        DyeWhite = new Material.Builder(GTCEu.id("white_dye"))
                .fluid().color(0xFFFFFF).buildAndRegister();

        ImpureEnrichedNaquadahSolution = new Material.Builder(GTCEu.id("impure_enriched_naquadah_solution"))
                .fluid().color(0x388438).buildAndRegister();

        EnrichedNaquadahSolution = new Material.Builder(GTCEu.id("enriched_naquadah_solution"))
                .fluid().color(0x3AAD3A).buildAndRegister();

        AcidicEnrichedNaquadahSolution = new Material.Builder(GTCEu.id("acidic_enriched_naquadah_solution"))
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x3DD63D).buildAndRegister();

        EnrichedNaquadahWaste = new Material.Builder(GTCEu.id("enriched_naquadah_waste"))
                .fluid().color(0x355B35).buildAndRegister();

        ImpureNaquadriaSolution = new Material.Builder(GTCEu.id("impure_naquadria_solution"))
                .fluid().color(0x518451).buildAndRegister();

        NaquadriaSolution = new Material.Builder(GTCEu.id("naquadria_solution"))
                .fluid().color(0x61AD61).buildAndRegister();

        AcidicNaquadriaSolution = new Material.Builder(GTCEu.id("acidic_naquadria_solution"))
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x70D670).buildAndRegister();

        NaquadriaWaste = new Material.Builder(GTCEu.id("naquadria_waste"))
                .fluid().color(0x425B42).buildAndRegister();

        Lapotron = new Material.Builder(GTCEu.id("lapotron"))
                .gem()
                .color(0x7497ea).secondaryColor(0x1c0b39).iconSet(DIAMOND)
                .flags(DISABLE_MATERIAL_RECIPES)
                .ignoredTagPrefixes(dustTiny, dustSmall)
                .buildAndRegister();

        TreatedWood = new Material.Builder(GTCEu.id("treated_wood"))
                .wood()
                .color(0x644218).secondaryColor(0x4e0b00).iconSet(WOOD)
                .fluidPipeProperties(340, 10, false)
                .flags(GENERATE_PLATE, FLAMMABLE, GENERATE_ROD, GENERATE_FRAME)
                .buildAndRegister();

        UUMatter = new Material.Builder(GTCEu.id("uu_matter"))
                .langValue("UU-Matter")
                .liquid(new FluidBuilder()
                        .temperature(300)
                        .customStill())
                .buildAndRegister();

        PCBCoolant = new Material.Builder(GTCEu.id("pcb_coolant"))
                .langValue("PCB Coolant")
                .fluid().color(0xD5D69C)
                .hazard(HazardProperty.HazardTrigger.INHALATION, GTMedicalConditions.CARCINOGEN)
                .buildAndRegister();

        Sculk = new Material.Builder(GTCEu.id("sculk"))
                .dust(1)
                .color(0x015a5c).secondaryColor(0x001616).iconSet(ROUGH)
                .buildAndRegister();

        Wax = new Material.Builder(GTCEu.id("wax"))
                .ingot().fluid()
                .color(0xfabf29)
                .flags(NO_SMELTING)
                .buildAndRegister();

        BauxiteSlurry = new Material.Builder(GTCEu.id("bauxite_slurry"))
                .fluid()
                .color(0x051650)
                .buildAndRegister();

        CrackedBauxiteSlurry = new Material.Builder(GTCEu.id("cracked_bauxite_slurry"))
                .liquid(new FluidBuilder()
                        .temperature(775))
                .color(0x052C50)
                .buildAndRegister();

        BauxiteSludge = new Material.Builder(GTCEu.id("bauxite_sludge"))
                .fluid()
                .color(0x563D2D)
                .buildAndRegister();

        DecalcifiedBauxiteSludge = new Material.Builder(GTCEu.id("decalcified_bauxite_sludge"))
                .fluid()
                .color(0x6F2DA8)
                .buildAndRegister();

        BauxiteSlag = new Material.Builder(GTCEu.id("bauxite_slag"))
                .dust()
                .color(0x6F2DA8).iconSet(SAND)
                .buildAndRegister();
    }
}
