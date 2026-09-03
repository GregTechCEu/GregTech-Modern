package com.gregtechceu.gtceu.common.data.materials;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttributes;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.registry.registrate.builder.MaterialBuilder;
import com.gregtechceu.gtceu.common.data.GTMedicalConditions;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dustSmall;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dustTiny;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class UnknownCompositionMaterials {

    public static void register() {
        WoodGas = REGISTRATE.material("wood_gas")
                .gas()
                .color(0xDECD87).secondaryColor(0xdeb287)
                .register();

        WoodVinegar = REGISTRATE.material("wood_vinegar")
                .fluid()
                .color(0xD45500).secondaryColor(0x905800)
                .register();

        WoodTar = REGISTRATE.material("wood_tar")
                .fluid()
                .color(0x3a271a).secondaryColor(0x28170B)
                .flags(STICKY, FLAMMABLE).register();

        CharcoalByproducts = REGISTRATE.material("charcoal_byproducts")
                .fluid().color(0x784421).register();

        Biomass = REGISTRATE.material("biomass")
                .liquid(new FluidBuilder().customStill()).color(0x00FF00).register();

        BioDiesel = REGISTRATE.material("bio_diesel")
                .fluid().color(0xFF8000)
                .flags(FLAMMABLE, EXPLOSIVE).register();

        FermentedBiomass = REGISTRATE.material("fermented_biomass")
                .liquid(new FluidBuilder().temperature(300))
                .color(0x445500)
                .register();

        Creosote = REGISTRATE.material("creosote")
                .liquid(new FluidBuilder().block().customStill().burnTime(6400)).color(0x804000)
                .flags(STICKY).register();

        Diesel = REGISTRATE.material("diesel")
                .liquid(new FluidBuilder().customStill()).flags(FLAMMABLE, EXPLOSIVE).register();

        RocketFuel = REGISTRATE.material("rocket_fuel")
                .fluid().flags(FLAMMABLE, EXPLOSIVE).color(0xBDB78C).register();

        Glue = REGISTRATE.material("glue")
                .liquid(new FluidBuilder().customStill()).flags(STICKY).register();

        Lubricant = REGISTRATE.material("lubricant")
                .liquid(new FluidBuilder().customStill()).register();

        McGuffium239 = REGISTRATE.material("mc_guffium_239")
                .liquid(new FluidBuilder().customStill()).register();

        IndiumConcentrate = REGISTRATE.material("indium_concentrate")
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x0E2950).register();

        SeedOil = REGISTRATE.material("seed_oil")
                .liquid(new FluidBuilder().customStill())
                .color(0xFFFFFF)
                .flags(STICKY, FLAMMABLE).register();

        DrillingFluid = REGISTRATE.material("drilling_fluid")
                .fluid().color(0xFFFFAA).register();

        ConstructionFoam = REGISTRATE.material("construction_foam")
                .fluid().color(0x808080).register();

        SulfuricHeavyFuel = REGISTRATE.material("sulfuric_heavy_fuel")
                .liquid(new FluidBuilder().customStill()).flags(FLAMMABLE).register();

        HeavyFuel = REGISTRATE.material("heavy_fuel")
                .liquid(new FluidBuilder().customStill()).flags(FLAMMABLE).register();

        LightlyHydroCrackedHeavyFuel = REGISTRATE.material("lightly_hydro_cracked_heavy_fuel")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xFFFF00).flags(FLAMMABLE).register();

        SeverelyHydroCrackedHeavyFuel = REGISTRATE.material("severely_hydro_cracked_heavy_fuel")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xFFFF00).flags(FLAMMABLE).register();

        LightlySteamCrackedHeavyFuel = REGISTRATE.material("lightly_steam_cracked_heavy_fuel")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .flags(FLAMMABLE).register();

        SeverelySteamCrackedHeavyFuel = REGISTRATE.material("severely_steam_cracked_heavy_fuel")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .flags(FLAMMABLE).register();

        SulfuricLightFuel = REGISTRATE.material("sulfuric_light_fuel")
                .liquid(new FluidBuilder()
                        .customStill())
                .flags(FLAMMABLE).register();

        LightFuel = REGISTRATE.material("light_fuel")
                .liquid(new FluidBuilder().customStill()).flags(FLAMMABLE).register();

        LightlyHydroCrackedLightFuel = REGISTRATE.material("lightly_hydro_cracked_light_fuel")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xB7AF08).flags(FLAMMABLE).register();

        SeverelyHydroCrackedLightFuel = REGISTRATE.material("severely_hydro_cracked_light_fuel")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xB7AF08).flags(FLAMMABLE).register();

        LightlySteamCrackedLightFuel = REGISTRATE.material("lightly_steam_cracked_light_fuel")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .flags(FLAMMABLE).register();

        SeverelySteamCrackedLightFuel = REGISTRATE.material("severely_steam_cracked_light_fuel")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .flags(FLAMMABLE).register();

        SulfuricNaphtha = REGISTRATE.material("sulfuric_naphtha")
                .liquid(new FluidBuilder().customStill()).flags(FLAMMABLE).register();

        Naphtha = REGISTRATE.material("naphtha")
                .liquid(new FluidBuilder().customStill()).flags(FLAMMABLE).register();

        LightlyHydroCrackedNaphtha = REGISTRATE.material("lightly_hydro_cracked_naphtha")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xBFB608).flags(FLAMMABLE).register();

        SeverelyHydroCrackedNaphtha = REGISTRATE.material("severely_hydro_cracked_naphtha")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xBFB608).flags(FLAMMABLE).register();

        LightlySteamCrackedNaphtha = REGISTRATE.material("lightly_steam_cracked_naphtha")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xBFB608).flags(FLAMMABLE).register();

        SeverelySteamCrackedNaphtha = REGISTRATE.material("severely_steam_cracked_naphtha")
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xBFB608).flags(FLAMMABLE).register();

        SulfuricGas = REGISTRATE.material("sulfuric_gas")
                .gas(new FluidBuilder().customStill())
                .color(0xECDCCC).register();

        RefineryGas = REGISTRATE.material("refinery_gas")
                .gas(new FluidBuilder().customStill())
                .color(0xB4B4B4)
                .flags(FLAMMABLE)
                .register();

        LightlyHydroCrackedGas = REGISTRATE.material("lightly_hydro_cracked_gas")
                .gas(new FluidBuilder().temperature(775))
                .color(0xA0A0A0)
                .flags(FLAMMABLE)
                .register();

        SeverelyHydroCrackedGas = REGISTRATE.material("severely_hydro_cracked_gas")
                .gas(new FluidBuilder().temperature(775))
                .color(0xC8C8C8)
                .flags(FLAMMABLE)
                .register();

        LightlySteamCrackedGas = REGISTRATE.material("lightly_steam_cracked_gas")
                .gas(new FluidBuilder().temperature(775))
                .color(0xE0E0E0)
                .flags(FLAMMABLE)
                .register();

        SeverelySteamCrackedGas = REGISTRATE.material("severely_steam_cracked_gas")
                .gas(new FluidBuilder().temperature(775))
                .color(0xE0E0E0).flags(FLAMMABLE).register();

        HydroCrackedEthane = REGISTRATE.material("hydro_cracked_ethane")
                .gas(new FluidBuilder().temperature(775))
                .color(0x9696BC).flags(FLAMMABLE).register();

        HydroCrackedEthylene = REGISTRATE.material("hydro_cracked_ethylene")
                .gas(new FluidBuilder().temperature(775))
                .color(0xA3A3A0).flags(FLAMMABLE).register();

        HydroCrackedPropene = REGISTRATE.material("hydro_cracked_propene")
                .gas(new FluidBuilder().temperature(775))
                .color(0xBEA540).flags(FLAMMABLE).register();

        HydroCrackedPropane = REGISTRATE.material("hydro_cracked_propane")
                .gas(new FluidBuilder().temperature(775))
                .color(0xBEA540).flags(FLAMMABLE).register();

        HydroCrackedButane = REGISTRATE.material("hydro_cracked_butane")
                .gas(new FluidBuilder().temperature(775))
                .color(0x852C18).flags(FLAMMABLE).register();

        HydroCrackedButene = REGISTRATE.material("hydro_cracked_butene")
                .gas(new FluidBuilder().temperature(775))
                .color(0x993E05).flags(FLAMMABLE).register();

        HydroCrackedButadiene = REGISTRATE.material("hydro_cracked_butadiene")
                .gas(new FluidBuilder().temperature(775))
                .color(0xAD5203).flags(FLAMMABLE).register();

        SteamCrackedEthane = REGISTRATE.material("steam_cracked_ethane")
                .gas(new FluidBuilder().temperature(775))
                .color(0x9696BC).flags(FLAMMABLE).register();

        SteamCrackedEthylene = REGISTRATE.material("steam_cracked_ethylene")
                .gas(new FluidBuilder().temperature(775))
                .color(0xA3A3A0).flags(FLAMMABLE).register();

        SteamCrackedPropene = REGISTRATE.material("steam_cracked_propene")
                .gas(new FluidBuilder().temperature(775))
                .color(0xBEA540).flags(FLAMMABLE).register();

        SteamCrackedPropane = REGISTRATE.material("steam_cracked_propane")
                .gas(new FluidBuilder().temperature(775))
                .color(0xBEA540).flags(FLAMMABLE).register();

        SteamCrackedButane = REGISTRATE.material("steam_cracked_butane")
                .gas(new FluidBuilder().temperature(775))
                .color(0x852C18).flags(FLAMMABLE).register();

        SteamCrackedButene = REGISTRATE.material("steam_cracked_butene")
                .gas(new FluidBuilder().temperature(775))
                .color(0x993E05).flags(FLAMMABLE).register();

        SteamCrackedButadiene = REGISTRATE.material("steam_cracked_butadiene")
                .gas(new FluidBuilder().temperature(775))
                .color(0xAD5203).flags(FLAMMABLE).register();

        LPG = REGISTRATE.material("lpg")
                .liquid(new FluidBuilder().customStill())
                .color(0xFCFCAC).flags(FLAMMABLE, EXPLOSIVE).register();

        RawGrowthMedium = REGISTRATE.material("raw_growth_medium")
                .fluid().color(0xA47351).register();

        SterileGrowthMedium = REGISTRATE.material("sterilized_growth_medium")
                .fluid().color(0xAC876E).register();

        Oil = REGISTRATE.material("oil")
                .liquid(new FluidBuilder().block().customStill())
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE)
                .register();

        HeavyOil = REGISTRATE.material("heavy_oil")
                .liquid(new FluidBuilder().block().customStill())
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE)
                .register();

        RawOil = REGISTRATE.material("raw_oil")
                .liquid(new FluidBuilder().block().customStill())
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE)
                .register();

        LightOil = REGISTRATE.material("light_oil")
                .liquid(new FluidBuilder().block().customStill())
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE)
                .register();

        NaturalGas = REGISTRATE.material("natural_gas")
                .gas(new FluidBuilder().block().customStill())
                .flags(FLAMMABLE, EXPLOSIVE).register();

        Bacteria = REGISTRATE.material("bacteria")
                .fluid().color(0x808000).register();

        BacterialSludge = REGISTRATE.material("bacterial_sludge")
                .fluid().color(0x355E3B).register();

        EnrichedBacterialSludge = REGISTRATE.material("enriched_bacterial_sludge")
                .fluid().color(0x7FFF00).register();

        Mutagen = REGISTRATE.material("mutagen")
                .fluid().color(0x00FF7F).register();

        GelatinMixture = REGISTRATE.material("gelatin_mixture")
                .fluid().color(0x588BAE).register();

        RawGasoline = REGISTRATE.material("raw_gasoline")
                .fluid().color(0xFF6400).flags(FLAMMABLE).register();

        Gasoline = REGISTRATE.material("gasoline")
                .fluid().color(0xFAA500).flags(FLAMMABLE, EXPLOSIVE).register();

        HighOctaneGasoline = REGISTRATE.material("high_octane_gasoline")
                .fluid().color(0xFFA500).flags(FLAMMABLE, EXPLOSIVE).register();

        CoalGas = REGISTRATE.material("coal_gas")
                .gas().color(0x333333).register();

        CoalTar = REGISTRATE.material("coal_tar")
                .fluid().color(0x1A1A1A).flags(STICKY, FLAMMABLE).register();

        Gunpowder = REGISTRATE.material("gunpowder")
                .dust(0)
                .color(0xa4a4a4).secondaryColor(0x767676).iconSet(ROUGH)
                .flags(FLAMMABLE, EXPLOSIVE, NO_SMELTING, NO_SMASHING)
                .components(Saltpeter, 2, Sulfur, 1, Carbon, 3)
                .register();

        Oilsands = REGISTRATE.material("oilsands")
                .dust(1).ore()
                .color(0xe3c78a).secondaryColor(0x161e22).iconSet(SAND)
                .flags(FLAMMABLE)
                .register();

        RareEarth = REGISTRATE.material("rare_earth")
                .dust(0)
                .color(0xffdc88).secondaryColor(0xe99673).iconSet(FINE)
                .register();

        Stone = REGISTRATE.material("stone")
                .dust(2)
                .color(0x8f8f8f).secondaryColor(0x898989).iconSet(ROUGH)
                .flags(MORTAR_GRINDABLE, GENERATE_GEAR, NO_SMASHING, NO_SMELTING)
                .register();

        Lava = REGISTRATE.material("lava")
                .fluid().color(0xFF4000).register();

        Netherite = REGISTRATE.material("netherite")
                .ingot().color(0x4b4042).secondaryColor(0x474447)
                .flags(FIRE_RESISTANT)
                .toolStats(ToolProperty.Builder.of(10.0F, 4.0F, 2032, 4)
                        .enchantability(21).build())
                .register();

        Glowstone = REGISTRATE.material("glowstone")
                .dust(1)
                .liquid(new FluidBuilder().temperature(500))
                .color(0xfcb34c).secondaryColor(0xce7533).iconSet(SHINY)
                .flags(NO_SMASHING, GENERATE_PLATE, EXCLUDE_PLATE_COMPRESSOR_RECIPE,
                        EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .register();

        NetherStar = REGISTRATE.material("nether_star")
                .gem(4)
                .color(0xfeffc6).secondaryColor(0x7fd7e2)
                .iconSet(NETHERSTAR)
                .flags(NO_SMASHING, NO_SMELTING, GENERATE_LENS)
                .register();

        Endstone = REGISTRATE.material("endstone")
                .dust(1)
                .color(0xf6fabd).secondaryColor(0xc5be8b).iconSet(ROUGH)
                .flags(NO_SMASHING)
                .register();

        Netherrack = REGISTRATE.material("netherrack")
                .dust(1)
                .color(0x7c4249).secondaryColor(0x400b0b).iconSet(ROUGH)
                .flags(NO_SMASHING, FLAMMABLE)
                .register();

        CetaneBoostedDiesel = REGISTRATE.material("cetane_boosted_diesel")
                .liquid(new FluidBuilder().customStill())
                .color(0xC8FF00)
                .flags(FLAMMABLE, EXPLOSIVE)
                .register();

        Collagen = REGISTRATE.material("collagen")
                .dust(1)
                .color(0xffadb7).secondaryColor(0x80471C).iconSet(ROUGH)
                .register();

        Gelatin = REGISTRATE.material("gelatin")
                .dust(1)
                .color(0xfaf7cb).secondaryColor(0x693d00).iconSet(ROUGH)
                .register();

        Agar = REGISTRATE.material("agar")
                .dust(1)
                .color(0xbdd168).secondaryColor(0x403218).iconSet(ROUGH)
                .register();

        Milk = REGISTRATE.material("milk")
                .liquid(new FluidBuilder()
                        .temperature(295)
                        .customStill())
                .color(0xfffbf0).secondaryColor(0xf6eac8).iconSet(FINE)
                .register();

        Cocoa = REGISTRATE.material("cocoa")
                .dust(0)
                .color(0x976746).secondaryColor(0x301a0a).iconSet(FINE)
                .register();

        Wheat = REGISTRATE.material("wheat")
                .dust(0)
                .color(0xdcbb65).secondaryColor(0x565138).iconSet(FINE)
                .register();

        Meat = REGISTRATE.material("meat")
                .dust(1)
                .color(0xe85048).secondaryColor(0x470a06).iconSet(SAND)
                .register();

        Wood = REGISTRATE.material("wood")
                .wood()
                .color(0xc29f6d).secondaryColor(0x643200).iconSet(WOOD)
                .fluidPipeProperties(340, 5, false)
                .toolStats(ToolProperty.Builder.of(1.0F, 1.0F, 128, 1, GTToolType.SOFT_MALLET).build())
                .flags(GENERATE_PLATE, GENERATE_ROD, GENERATE_BOLT_SCREW, GENERATE_LONG_ROD, FLAMMABLE, GENERATE_GEAR,
                        GENERATE_FRAME)
                .register();

        Paper = REGISTRATE.material("paper")
                .dust(0)
                .color(0xF9F9F9).secondaryColor(0xECECEC).iconSet(DULL)
                .flags(GENERATE_PLATE, FLAMMABLE, NO_SMELTING, NO_SMASHING,
                        MORTAR_GRINDABLE, EXCLUDE_PLATE_COMPRESSOR_RECIPE)
                .register();

        FishOil = REGISTRATE.material("fish_oil")
                .fluid()
                .color(0xDCC15D)
                .flags(STICKY, FLAMMABLE)
                .register();

        RubySlurry = REGISTRATE.material("ruby_slurry")
                .fluid().color(0xff6464).register();

        SapphireSlurry = REGISTRATE.material("sapphire_slurry")
                .fluid().color(0x6464c8).register();

        GreenSapphireSlurry = REGISTRATE.material("green_sapphire_slurry")
                .fluid().color(0x64c882).register();

        // These colors are much nicer looking than those in MC's EnumDyeColor
        DyeBlack = REGISTRATE.material("black_dye")
                .fluid().color(0x202020).register();

        DyeRed = REGISTRATE.material("red_dye")
                .fluid().color(0xFF0000).register();

        DyeGreen = REGISTRATE.material("green_dye")
                .fluid().color(0x00FF00).register();

        DyeBrown = REGISTRATE.material("brown_dye")
                .fluid().color(0x604000).register();

        DyeBlue = REGISTRATE.material("blue_dye")
                .fluid().color(0x0020FF).register();

        DyePurple = REGISTRATE.material("purple_dye")
                .fluid().color(0x800080).register();

        DyeCyan = REGISTRATE.material("cyan_dye")
                .fluid().color(0x00FFFF).register();

        DyeLightGray = REGISTRATE.material("light_gray_dye")
                .fluid().color(0xC0C0C0).register();

        DyeGray = REGISTRATE.material("gray_dye")
                .fluid().color(0x808080).register();

        DyePink = REGISTRATE.material("pink_dye")
                .fluid().color(0xFFC0C0).register();

        DyeLime = REGISTRATE.material("lime_dye")
                .fluid().color(0x80FF80).register();

        DyeYellow = REGISTRATE.material("yellow_dye")
                .fluid().color(0xFFFF00).register();

        DyeLightBlue = REGISTRATE.material("light_blue_dye")
                .fluid().color(0x6080FF).register();

        DyeMagenta = REGISTRATE.material("magenta_dye")
                .fluid().color(0xFF00FF).register();

        DyeOrange = REGISTRATE.material("orange_dye")
                .fluid().color(0xFF8000).register();

        DyeWhite = REGISTRATE.material("white_dye")
                .fluid().color(0xFFFFFF).register();

        ImpureEnrichedNaquadahSolution = REGISTRATE.material("impure_enriched_naquadah_solution")
                .fluid().color(0x388438).register();

        EnrichedNaquadahSolution = REGISTRATE.material("enriched_naquadah_solution")
                .fluid().color(0x3AAD3A).register();

        AcidicEnrichedNaquadahSolution = REGISTRATE.material("acidic_enriched_naquadah_solution")
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x3DD63D).register();

        EnrichedNaquadahWaste = REGISTRATE.material("enriched_naquadah_waste")
                .fluid().color(0x355B35).register();

        ImpureNaquadriaSolution = REGISTRATE.material("impure_naquadria_solution")
                .fluid().color(0x518451).register();

        NaquadriaSolution = REGISTRATE.material("naquadria_solution")
                .fluid().color(0x61AD61).register();

        AcidicNaquadriaSolution = REGISTRATE.material("acidic_naquadria_solution")
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x70D670).register();

        NaquadriaWaste = REGISTRATE.material("naquadria_waste")
                .fluid().color(0x425B42).register();

        Lapotron = REGISTRATE.material("lapotron")
                .gem()
                .color(0x7497ea).secondaryColor(0x1c0b39).iconSet(DIAMOND)
                .flags(DISABLE_MATERIAL_RECIPES)
                .ignoredTagPrefixes(dustTiny, dustSmall)
                .register();

        TreatedWood = REGISTRATE.material("treated_wood")
                .wood()
                .color(0x644218).secondaryColor(0x4e0b00).iconSet(WOOD)
                .fluidPipeProperties(340, 10, false)
                .flags(GENERATE_PLATE, FLAMMABLE, GENERATE_ROD, GENERATE_FRAME)
                .register();

        UUMatter = REGISTRATE.material("uu_matter")
                .liquid(new FluidBuilder()
                        .temperature(300)
                        .customStill())
                .register();

        PCBCoolant = REGISTRATE.material("pcb_coolant")
                .fluid().color(0xD5D69C)
                .hazard(HazardProperty.HazardTrigger.INHALATION, GTMedicalConditions.CARCINOGEN)
                .register();

        Sculk = REGISTRATE.material("sculk")
                .dust(1)
                .color(0x015a5c).secondaryColor(0x001616).iconSet(ROUGH)
                .register();

        Wax = REGISTRATE.material("wax")
                .gem().fluid()
                .color(0xfabf29)
                .flags(NO_SMELTING)
                .register();

        BauxiteSlurry = REGISTRATE.material("bauxite_slurry")
                .fluid()
                .color(0x051650)
                .register();

        CrackedBauxiteSlurry = REGISTRATE.material("cracked_bauxite_slurry")
                .liquid(775)
                .color(0x052C50)
                .register();

        BauxiteSludge = REGISTRATE.material("bauxite_sludge")
                .fluid()
                .color(0x563D2D)
                .register();

        DecalcifiedBauxiteSludge = REGISTRATE.material("decalcified_bauxite_sludge")
                .fluid()
                .color(0x6F2DA8)
                .register();

        BauxiteSlag = REGISTRATE.material("bauxite_slag")
                .dust()
                .color(0x6F2DA8).iconSet(SAND)
                .register();
    }
}
