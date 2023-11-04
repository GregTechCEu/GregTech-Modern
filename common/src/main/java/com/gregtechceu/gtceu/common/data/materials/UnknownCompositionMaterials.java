package com.gregtechceu.gtceu.common.data.materials;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttributes;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class UnknownCompositionMaterials {

    public static void register() {

        WoodGas = new Material.Builder("wood_gas")
                .gas().color(0xDECD87).buildAndRegister();

        WoodVinegar = new Material.Builder("wood_vinegar")
                .fluid().color(0xD45500).buildAndRegister();

        WoodTar = new Material.Builder("wood_tar")
                .fluid().color(0x28170B)
                .flags(STICKY, FLAMMABLE).buildAndRegister();

        CharcoalByproducts = new Material.Builder("charcoal_byproducts")
                .fluid().color(0x784421).buildAndRegister();

        Biomass = new Material.Builder("biomass")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().customStill()).color(0x00FF00).buildAndRegister();

        BioDiesel = new Material.Builder("bio_diesel")
                .fluid().color(0xFF8000)
                .flags(FLAMMABLE, EXPLOSIVE).buildAndRegister();

        FermentedBiomass = new Material.Builder("fermented_biomass")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(300))
                .color(0x445500)
                .buildAndRegister();

        Creosote = new Material.Builder("creosote")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().customStill()).fluidBurnTime(6400).color(0x804000)
                .flags(STICKY).buildAndRegister();

        Diesel = new Material.Builder("diesel")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().customStill()).flags(FLAMMABLE, EXPLOSIVE).buildAndRegister();

        RocketFuel = new Material.Builder("rocket_fuel")
                .fluid().flags(FLAMMABLE, EXPLOSIVE).color(0xBDB78C).buildAndRegister();

        Glue = new Material.Builder("glue")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().customStill()).flags(STICKY).buildAndRegister();

        Lubricant = new Material.Builder("lubricant")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().customStill()).buildAndRegister();

        McGuffium239 = new Material.Builder("mc_guffium_239")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().customStill()).buildAndRegister();

        IndiumConcentrate = new Material.Builder("indium_concentrate")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x0E2950).buildAndRegister();

        SeedOil = new Material.Builder("seed_oil")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().customStill())
                .color(0xFFFFFF)
                .flags(STICKY, FLAMMABLE).buildAndRegister();

        DrillingFluid = new Material.Builder("drilling_fluid")
                .fluid().color(0xFFFFAA).buildAndRegister();

        ConstructionFoam = new Material.Builder("construction_foam")
                .fluid().color(0x808080).buildAndRegister();

        // Free IDs 1517-1521

        SulfuricHeavyFuel = new Material.Builder("sulfuric_heavy_fuel")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().customStill()).flags(FLAMMABLE).buildAndRegister();

        HeavyFuel = new Material.Builder("heavy_fuel")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().customStill()).flags(FLAMMABLE).buildAndRegister();

        LightlyHydroCrackedHeavyFuel = new Material.Builder("lightly_hydro_cracked_heavy_fuel")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xFFFF00).flags(FLAMMABLE).buildAndRegister();

        SeverelyHydroCrackedHeavyFuel = new Material.Builder("severely_hydro_cracked_heavy_fuel")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xFFFF00).flags(FLAMMABLE).buildAndRegister();

        LightlySteamCrackedHeavyFuel = new Material.Builder("lightly_steam_cracked_heavy_fuel")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .flags(FLAMMABLE).buildAndRegister();

        SeverelySteamCrackedHeavyFuel = new Material.Builder("severely_steam_cracked_heavy_fuel")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .flags(FLAMMABLE).buildAndRegister();

        SulfuricLightFuel = new Material.Builder("sulfuric_light_fuel")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .flags(FLAMMABLE).buildAndRegister();

        LightFuel = new Material.Builder("light_fuel")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().customStill()).flags(FLAMMABLE).buildAndRegister();

        LightlyHydroCrackedLightFuel = new Material.Builder("lightly_hydro_cracked_light_fuel")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xB7AF08).flags(FLAMMABLE).buildAndRegister();

        SeverelyHydroCrackedLightFuel = new Material.Builder("severely_hydro_cracked_light_fuel")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xB7AF08).flags(FLAMMABLE).buildAndRegister();

        LightlySteamCrackedLightFuel = new Material.Builder("lightly_steam_cracked_light_fuel")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .flags(FLAMMABLE).buildAndRegister();

        SeverelySteamCrackedLightFuel = new Material.Builder("severely_steam_cracked_light_fuel")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .flags(FLAMMABLE).buildAndRegister();

        SulfuricNaphtha = new Material.Builder("sulfuric_naphtha")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().customStill()).flags(FLAMMABLE).buildAndRegister();

        Naphtha = new Material.Builder("naphtha")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().customStill()).flags(FLAMMABLE).buildAndRegister();

        LightlyHydroCrackedNaphtha = new Material.Builder("lightly_hydro_cracked_naphtha")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xBFB608).flags(FLAMMABLE).buildAndRegister();

        SeverelyHydroCrackedNaphtha = new Material.Builder("severely_hydro_cracked_naphtha")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xBFB608).flags(FLAMMABLE).buildAndRegister();

        LightlySteamCrackedNaphtha = new Material.Builder("lightly_steam_cracked_naphtha")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xBFB608).flags(FLAMMABLE).buildAndRegister();

        SeverelySteamCrackedNaphtha = new Material.Builder("severely_steam_cracked_naphtha")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xBFB608).flags(FLAMMABLE).buildAndRegister();

        SulfuricGas = new Material.Builder("sulfuric_gas")
                .fluid(FluidStorageKeys.GAS, new FluidBuilder().customStill())
                .color(0xECDCCC).buildAndRegister();

        RefineryGas = new Material.Builder("refinery_gas")
                .fluid(FluidStorageKeys.GAS, new FluidBuilder().customStill())
                .color(0xB4B4B4)
                .flags(FLAMMABLE)
                .buildAndRegister();

        LightlyHydroCrackedGas = new Material.Builder("lightly_hydro_cracked_gas")
                .fluid(FluidStorageKeys.GAS, new FluidBuilder().temperature(775))
                .color(0xA0A0A0)
                .flags(FLAMMABLE)
                .buildAndRegister();

        SeverelyHydroCrackedGas = new Material.Builder("severely_hydro_cracked_gas")
                .fluid(FluidStorageKeys.GAS, new FluidBuilder().temperature(775))
                .color(0xC8C8C8)
                .flags(FLAMMABLE)
                .buildAndRegister();

        LightlySteamCrackedGas = new Material.Builder("lightly_steam_cracked_gas")
                .fluid(FluidStorageKeys.GAS, new FluidBuilder().temperature(775))
                .color(0xE0E0E0)
                .flags(FLAMMABLE)
                .buildAndRegister();

        SeverelySteamCrackedGas = new Material.Builder("severely_steam_cracked_gas")
                .fluid(FluidStorageKeys.GAS, new FluidBuilder().temperature(775))
                .color(0xE0E0E0).flags(FLAMMABLE).buildAndRegister();

        HydroCrackedEthane = new Material.Builder("hydro_cracked_ethane")
                .fluid(FluidStorageKeys.GAS, new FluidBuilder().temperature(775))
                .color(0x9696BC).flags(FLAMMABLE).buildAndRegister();

        HydroCrackedEthylene = new Material.Builder("hydro_cracked_ethylene")
                .fluid(FluidStorageKeys.GAS, new FluidBuilder().temperature(775))
                .color(0xA3A3A0).flags(FLAMMABLE).buildAndRegister();

        HydroCrackedPropene = new Material.Builder("hydro_cracked_propene")
                .fluid(FluidStorageKeys.GAS, new FluidBuilder().temperature(775))
                .color(0xBEA540).flags(FLAMMABLE).buildAndRegister();

        HydroCrackedPropane = new Material.Builder("hydro_cracked_propane")
                .fluid(FluidStorageKeys.GAS, new FluidBuilder().temperature(775))
                .color(0xBEA540).flags(FLAMMABLE).buildAndRegister();

        HydroCrackedButane = new Material.Builder("hydro_cracked_butane")
                .fluid(FluidStorageKeys.GAS, new FluidBuilder().temperature(775))
                .color(0x852C18).flags(FLAMMABLE).buildAndRegister();

        HydroCrackedButene = new Material.Builder("hydro_cracked_butene")
                .fluid(FluidStorageKeys.GAS, new FluidBuilder().temperature(775))
                .color(0x993E05).flags(FLAMMABLE).buildAndRegister();

        HydroCrackedButadiene = new Material.Builder("hydro_cracked_butadiene")
                .fluid(FluidStorageKeys.GAS, new FluidBuilder().temperature(775))
                .color(0xAD5203).flags(FLAMMABLE).buildAndRegister();

        SteamCrackedEthane = new Material.Builder("steam_cracked_ethane")
                .fluid(FluidStorageKeys.GAS, new FluidBuilder().temperature(775))
                .color(0x9696BC).flags(FLAMMABLE).buildAndRegister();

        SteamCrackedEthylene = new Material.Builder("steam_cracked_ethylene")
                .fluid(FluidStorageKeys.GAS, new FluidBuilder().temperature(775))
                .color(0xA3A3A0).flags(FLAMMABLE).buildAndRegister();

        SteamCrackedPropene = new Material.Builder("steam_cracked_propene")
                .fluid(FluidStorageKeys.GAS, new FluidBuilder().temperature(775))
                .color(0xBEA540).flags(FLAMMABLE).buildAndRegister();

        SteamCrackedPropane = new Material.Builder("steam_cracked_propane")
                .fluid(FluidStorageKeys.GAS, new FluidBuilder().temperature(775))
                .color(0xBEA540).flags(FLAMMABLE).buildAndRegister();

        SteamCrackedButane = new Material.Builder("steam_cracked_butane")
                .fluid(FluidStorageKeys.GAS, new FluidBuilder().temperature(775))
                .color(0x852C18).flags(FLAMMABLE).buildAndRegister();

        SteamCrackedButene = new Material.Builder("steam_cracked_butene")
                .fluid(FluidStorageKeys.GAS, new FluidBuilder().temperature(775))
                .color(0x993E05).flags(FLAMMABLE).buildAndRegister();

        SteamCrackedButadiene = new Material.Builder("steam_cracked_butadiene")
                .fluid(FluidStorageKeys.GAS, new FluidBuilder().temperature(775))
                .color(0xAD5203).flags(FLAMMABLE).buildAndRegister();

        LPG = new Material.Builder("lpg")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().customStill())
                .color(0xFCFCAC).flags(FLAMMABLE, EXPLOSIVE).buildAndRegister();

        RawGrowthMedium = new Material.Builder("raw_growth_medium")
                .fluid().color(0xA47351).buildAndRegister();

        SterileGrowthMedium = new Material.Builder("sterilized_growth_medium")
                .fluid().color(0xAC876E).buildAndRegister();

        Oil = new Material.Builder("oil")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().block().customStill())
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE)
                .buildAndRegister();

        OilHeavy = new Material.Builder("oil_heavy")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().block().customStill())
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE)
                .buildAndRegister();

        RawOil = new Material.Builder("oil_medium")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().block().customStill())
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE)
                .buildAndRegister();

        OilLight = new Material.Builder("oil_light")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().block().customStill())
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE)
                .buildAndRegister();

        NaturalGas = new Material.Builder("natural_gas")
                .fluid(FluidStorageKeys.GAS, new FluidBuilder().block().customStill())
                .flags(FLAMMABLE, EXPLOSIVE).buildAndRegister();

        Bacteria = new Material.Builder("bacteria")
                .fluid().color(0x808000).buildAndRegister();

        BacterialSludge = new Material.Builder("bacterial_sludge")
                .fluid().color(0x355E3B).buildAndRegister();

        EnrichedBacterialSludge = new Material.Builder("enriched_bacterial_sludge")
                .fluid().color(0x7FFF00).buildAndRegister();

        // free id: 1587

        Mutagen = new Material.Builder("mutagen")
                .fluid().color(0x00FF7F).buildAndRegister();

        GelatinMixture = new Material.Builder("gelatin_mixture")
                .fluid().color(0x588BAE).buildAndRegister();

        RawGasoline = new Material.Builder("raw_gasoline")
                .fluid().color(0xFF6400).flags(FLAMMABLE).buildAndRegister();

        Gasoline = new Material.Builder("gasoline")
                .fluid().color(0xFAA500).flags(FLAMMABLE, EXPLOSIVE).buildAndRegister();

        HighOctaneGasoline = new Material.Builder("high_octane_gasoline")
                .fluid().color(0xFFA500).flags(FLAMMABLE, EXPLOSIVE).buildAndRegister();

        // free id: 1593

        CoalGas = new Material.Builder("coal_gas")
                .gas().color(0x333333).buildAndRegister();

        CoalTar = new Material.Builder("coal_tar")
                .fluid().color(0x1A1A1A).flags(STICKY, FLAMMABLE).buildAndRegister();

        Gunpowder = new Material.Builder("gunpowder")
                .dust(0)
                .color(0x808080).iconSet(ROUGH)
                .flags(FLAMMABLE, EXPLOSIVE, NO_SMELTING, NO_SMASHING)
                .buildAndRegister();

        Oilsands = new Material.Builder("oilsands")
                .dust(1).ore()
                .color(0x0A0A0A).iconSet(SAND)
                .flags(FLAMMABLE)
                .buildAndRegister();

        RareEarth = new Material.Builder("rare_earth")
                .dust(0)
                .color(0x808064).iconSet(FINE)
                .buildAndRegister();

        Stone = new Material.Builder("stone")
                .dust(2)
                .color(0xCDCDCD).iconSet(ROUGH)
                .flags(MORTAR_GRINDABLE, GENERATE_GEAR, NO_SMASHING, NO_SMELTING)
                .buildAndRegister();

        Lava = new Material.Builder("lava")
                .fluid().color(0xFF4000).buildAndRegister();

        Glowstone = new Material.Builder("glowstone")
                .dust(1)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(500))
                .color(0xFFFF00).iconSet(SHINY)
                .flags(NO_SMASHING, GENERATE_PLATE, EXCLUDE_PLATE_COMPRESSOR_RECIPE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .buildAndRegister();

        NetherStar = new Material.Builder("nether_star")
                .gem(4)
                .iconSet(NETHERSTAR)
                .flags(NO_SMASHING, NO_SMELTING, GENERATE_LENS)
                .buildAndRegister();

        Endstone = new Material.Builder("endstone")
                .dust(1)
                .color(0xD9DE9E)
                .flags(NO_SMASHING)
                .buildAndRegister();

        Netherrack = new Material.Builder("netherrack")
                .dust(1)
                .color(0xC80000)
                .flags(NO_SMASHING, FLAMMABLE)
                .buildAndRegister();

        CetaneBoostedDiesel = new Material.Builder("cetane_boosted_diesel")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().customStill())
                .color(0xC8FF00)
                .flags(FLAMMABLE, EXPLOSIVE)
                .buildAndRegister();

        Collagen = new Material.Builder("collagen")
                .dust(1)
                .color(0x80471C).iconSet(ROUGH)
                .buildAndRegister();

        Gelatin = new Material.Builder("gelatin")
                .dust(1)
                .color(0x588BAE).iconSet(ROUGH)
                .buildAndRegister();

        Agar = new Material.Builder("agar")
                .dust(1)
                .color(0x4F7942).iconSet(ROUGH)
                .buildAndRegister();

        Milk = new Material.Builder("milk")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder()
                        .temperature(295)
                        .customStill())
                .color(0xFEFEFE).iconSet(FINE)
                .buildAndRegister();

        Cocoa = new Material.Builder("cocoa")
                .dust(0)
                .color(0x643200).iconSet(FINE)
                .buildAndRegister();

        Wheat = new Material.Builder("wheat")
                .dust(0)
                .color(0xFFFFC4).iconSet(FINE)
                .buildAndRegister();

        Meat = new Material.Builder("meat")
                .dust(1)
                .color(0xC14C4C).iconSet(SAND)
                .buildAndRegister();

        Wood = new Material.Builder("wood")
                .wood()
                .color(0x643200).iconSet(WOOD)
                .fluidPipeProperties(340, 5, false)
                .toolStats(ToolProperty.Builder.of(1.0F, 1.0F, 128, 1, GTToolType.SOFT_MALLET).build())
                .flags(GENERATE_PLATE, GENERATE_ROD, GENERATE_BOLT_SCREW, GENERATE_LONG_ROD, FLAMMABLE, GENERATE_GEAR, GENERATE_FRAME)
                .buildAndRegister();

        Paper = new Material.Builder("paper")
                .dust(0)
                .color(0xFAFAFA).iconSet(FINE)
                .flags(GENERATE_PLATE, FLAMMABLE, NO_SMELTING, NO_SMASHING,
                        MORTAR_GRINDABLE, EXCLUDE_PLATE_COMPRESSOR_RECIPE)
                .buildAndRegister();

        FishOil = new Material.Builder("fish_oil")
                .fluid()
                .color(0xDCC15D)
                .flags(STICKY, FLAMMABLE)
                .buildAndRegister();

        RubySlurry = new Material.Builder("ruby_slurry")
                .fluid().color(0xff6464).buildAndRegister();

        SapphireSlurry = new Material.Builder("sapphire_slurry")
                .fluid().color(0x6464c8).buildAndRegister();

        GreenSapphireSlurry = new Material.Builder("green_sapphire_slurry")
                .fluid().color(0x64c882).buildAndRegister();

        // These colors are much nicer looking than those in MC's EnumDyeColor
        DyeBlack = new Material.Builder("black_dye")
                .fluid().color(0x202020).buildAndRegister();

        DyeRed = new Material.Builder("red_dye")
                .fluid().color(0xFF0000).buildAndRegister();

        DyeGreen = new Material.Builder("green_dye")
                .fluid().color(0x00FF00).buildAndRegister();

        DyeBrown = new Material.Builder("brown_dye")
                .fluid().color(0x604000).buildAndRegister();

        DyeBlue = new Material.Builder("blue_dye")
                .fluid().color(0x0020FF).buildAndRegister();

        DyePurple = new Material.Builder("purple_dye")
                .fluid().color(0x800080).buildAndRegister();

        DyeCyan = new Material.Builder("cyan_dye")
                .fluid().color(0x00FFFF).buildAndRegister();

        DyeLightGray = new Material.Builder("light_gray_dye")
                .fluid().color(0xC0C0C0).buildAndRegister();

        DyeGray = new Material.Builder("gray_dye")
                .fluid().color(0x808080).buildAndRegister();

        DyePink = new Material.Builder("pink_dye")
                .fluid().color(0xFFC0C0).buildAndRegister();

        DyeLime = new Material.Builder("lime_dye")
                .fluid().color(0x80FF80).buildAndRegister();

        DyeYellow = new Material.Builder("yellow_dye")
                .fluid().color(0xFFFF00).buildAndRegister();

        DyeLightBlue = new Material.Builder("light_blue_dye")
                .fluid().color(0x6080FF).buildAndRegister();

        DyeMagenta = new Material.Builder("magenta_dye")
                .fluid().color(0xFF00FF).buildAndRegister();

        DyeOrange = new Material.Builder("orange_dye")
                .fluid().color(0xFF8000).buildAndRegister();

        DyeWhite = new Material.Builder("white_dye")
                .fluid().color(0xFFFFFF).buildAndRegister();

        ImpureEnrichedNaquadahSolution = new Material.Builder("impure_enriched_naquadah_solution")
                .fluid().color(0x388438).buildAndRegister();

        EnrichedNaquadahSolution = new Material.Builder("enriched_naquadah_solution")
                .fluid().color(0x3AAD3A).buildAndRegister();

        AcidicEnrichedNaquadahSolution = new Material.Builder("acidic_enriched_naquadah_solution")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x3DD63D).buildAndRegister();

        EnrichedNaquadahWaste = new Material.Builder("enriched_naquadah_waste")
                .fluid().color(0x355B35).buildAndRegister();

        ImpureNaquadriaSolution = new Material.Builder("impure_naquadria_solution")
                .fluid().color(0x518451).buildAndRegister();

        NaquadriaSolution = new Material.Builder("naquadria_solution")
                .fluid().color(0x61AD61).buildAndRegister();

        AcidicNaquadriaSolution = new Material.Builder("acidic_naquadria_solution")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x70D670).buildAndRegister();

        NaquadriaWaste = new Material.Builder("naquadria_waste")
                .fluid().color(0x425B42).buildAndRegister();

        Lapotron = new Material.Builder("lapotron")
                .gem()
                .color(0x2C39B1).iconSet(DIAMOND)
                .flags(NO_UNIFICATION)
                .buildAndRegister();

        TreatedWood = new Material.Builder("treated_wood")
                .wood()
                .color(0x502800).iconSet(WOOD)
                .fluidPipeProperties(340, 10, false)
                .flags(GENERATE_PLATE, FLAMMABLE, GENERATE_ROD, GENERATE_FRAME)
                .buildAndRegister();

        UUMatter = new Material.Builder("uu_matter")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder()
                        .temperature(300)
                        .customStill())
                .buildAndRegister();
    }
}
