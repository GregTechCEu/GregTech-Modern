package com.gregtechceu.gtceu.common.data.materials;

import com.gregtechceu.gtceu.api.data.chemical.fluid.FluidTypes;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class UnknownCompositionMaterials {

    public static void register() {

        AcidicEnrichedNaquadahSolution = new Material.Builder("acidic_enriched_naquadah_solution")
                .fluid(FluidTypes.ACID).color(0x3DD63D).buildAndRegister();

        AcidicNaquadriaSolution = new Material.Builder("acidic_naquadria_solution")
                .fluid(FluidTypes.ACID).color(0x70D670).buildAndRegister();

        Agar = new Material.Builder("agar")
                .dust(1)
                .color(0x4F7942).iconSet(ROUGH)
                .buildAndRegister();

        Bacteria = new Material.Builder("bacteria")
                .fluid().color(0x808000).buildAndRegister();

        BacterialSludge = new Material.Builder("bacterial_sludge")
                .fluid().color(0x355E3B).buildAndRegister();

        BioDiesel = new Material.Builder("bio_diesel")
                .fluid().color(0xFF8000)
                .flags(FLAMMABLE, EXPLOSIVE).buildAndRegister();

        Biomass = new Material.Builder("biomass")
                .fluid().fluidCustomTexture().color(0x00FF00).buildAndRegister();

        CetaneBoostedDiesel = new Material.Builder("nitro_fuel")
                .fluid().fluidCustomTexture()
                .color(0xC8FF00)
                .flags(FLAMMABLE, EXPLOSIVE)
                .buildAndRegister();

        Cocoa = new Material.Builder("cocoa")
                .dust(0)
                .color(0x643200).iconSet(FINE)
                .buildAndRegister();

        Collagen = new Material.Builder("collagen")
                .dust(1)
                .color(0x80471C).iconSet(ROUGH)
                .buildAndRegister();

        ConstructionFoam = new Material.Builder("construction_foam")
                .fluid().color(0x808080).buildAndRegister();

        Creosote = new Material.Builder("creosote")
                .fluid().fluidCustomTexture().color(0x804000)
                .flags(STICKY).buildAndRegister();

        CharcoalByproducts = new Material.Builder("charcoal_byproducts")
                .fluid().color(0x784421).buildAndRegister();

        CoalGas = new Material.Builder("coal_gas")
                .fluid(FluidTypes.GAS).color(0x333333).buildAndRegister();

        CoalTar = new Material.Builder("coal_tar")
                .fluid().color(0x1A1A1A).flags(STICKY, FLAMMABLE).buildAndRegister();

        Diesel = new Material.Builder("diesel")
                .fluid().fluidCustomTexture().flags(FLAMMABLE, EXPLOSIVE).buildAndRegister();

        DrillingFluid = new Material.Builder("drilling_fluid")
                .fluid().color(0xFFFFAA).buildAndRegister();

        DyeCyan = new Material.Builder("dye_cyan")
                .fluid().color(0x00FFFF).buildAndRegister();

        DyeBlack = new Material.Builder("dye_black")
                .fluid().color(0x202020).buildAndRegister();

        DyeBlue = new Material.Builder("dye_blue")
                .fluid().color(0x0020FF).buildAndRegister();

        DyeBrown = new Material.Builder("dye_brown")
                .fluid().color(0x604000).buildAndRegister();

        DyeGray = new Material.Builder("dye_gray")
                .fluid().color(0x808080).buildAndRegister();

        DyeGreen = new Material.Builder("dye_green")
                .fluid().color(0x00FF00).buildAndRegister();

        DyeLightBlue = new Material.Builder("dye_light_blue")
                .fluid().color(0x6080FF).buildAndRegister();

        DyeLightGray = new Material.Builder("dye_light_gray")
                .fluid().color(0xC0C0C0).buildAndRegister();

        DyeLime = new Material.Builder("dye_lime")
                .fluid().color(0x80FF80).buildAndRegister();

        DyeMagenta = new Material.Builder("dye_magenta")
                .fluid().color(0xFF00FF).buildAndRegister();

        DyeOrange = new Material.Builder("dye_orange")
                .fluid().color(0xFF8000).buildAndRegister();

        DyePink = new Material.Builder("dye_pink")
                .fluid().color(0xFFC0C0).buildAndRegister();

        DyePurple = new Material.Builder("dye_purple")
                .fluid().color(0x800080).buildAndRegister();

        DyeRed = new Material.Builder("dye_red")
                .fluid().color(0xFF0000).buildAndRegister();

        DyeWhite = new Material.Builder("dye_white")
                .fluid().color(0xFFFFFF).buildAndRegister();

        DyeYellow = new Material.Builder("dye_yellow")
                .fluid().color(0xFFFF00).buildAndRegister();

        EnrichedBacterialSludge = new Material.Builder("enriched_bacterial_sludge")
                .fluid().color(0x7FFF00).buildAndRegister();

        EnrichedNaquadahSolution = new Material.Builder("enriched_naquadah_solution")
                .fluid().color(0x3AAD3A).buildAndRegister();

        EnrichedNaquadahWaste = new Material.Builder("enriched_naquadah_waste")
                .fluid().color(0x355B35).buildAndRegister();

        FermentedBiomass = new Material.Builder("fermented_biomass")
                .fluid().color(0x445500).fluidTemp(300).buildAndRegister();

        FishOil = new Material.Builder("fish_oil")
                .fluid()
                .color(0xDCC15D)
                .flags(STICKY, FLAMMABLE)
                .buildAndRegister();

        Gasoline = new Material.Builder("gasoline")
                .fluid().color(0xFAA500).flags(FLAMMABLE, EXPLOSIVE).buildAndRegister();

        Gelatin = new Material.Builder("gelatin")
                .dust(1)
                .color(0x588BAE).iconSet(ROUGH)
                .buildAndRegister();

        GelatinMixture = new Material.Builder("gelatin_mixture")
                .fluid().color(0x588BAE).buildAndRegister();

        Glowstone = new Material.Builder("glowstone")
                .dust(1).fluid()
                .color(0xFFFF00).iconSet(SHINY)
                .flags(NO_SMASHING, GENERATE_PLATE, EXCLUDE_PLATE_COMPRESSOR_RECIPE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .fluidTemp(500)
                .buildAndRegister();

        Glue = new Material.Builder("glue")
                .fluid().fluidCustomTexture().flags(STICKY).buildAndRegister();

        GreenSapphireSlurry = new Material.Builder("green_sapphire_slurry")
                .fluid().color(0x64c882).buildAndRegister();

        Gunpowder = new Material.Builder("gunpowder")
                .dust(0)
                .color(0x808080).iconSet(ROUGH)
                .flags(FLAMMABLE, EXPLOSIVE, NO_SMELTING, NO_SMASHING)
                .buildAndRegister();

        HeavyFuel = new Material.Builder("heavy_fuel")
                .fluid().fluidCustomTexture().flags(FLAMMABLE).buildAndRegister();

        HighOctaneGasoline = new Material.Builder("gasoline_premium")
                .fluid().color(0xFFA500).flags(FLAMMABLE, EXPLOSIVE).buildAndRegister();

        HydroCrackedButane = new Material.Builder("hydrocracked_butane")
                .fluid(FluidTypes.GAS).color(0x852C18)
                .fluidTemp(775).flags(FLAMMABLE).buildAndRegister();

        HydroCrackedButadiene = new Material.Builder("hydrocracked_butadiene")
                .fluid(FluidTypes.GAS).color(0xAD5203)
                .fluidTemp(775).flags(FLAMMABLE).buildAndRegister();

        HydroCrackedButene = new Material.Builder("hydrocracked_butene")
                .fluid(FluidTypes.GAS).color(0x993E05)
                .fluidTemp(775).flags(FLAMMABLE).buildAndRegister();

        HydroCrackedEthane = new Material.Builder("hydrocracked_ethane")
                .fluid(FluidTypes.GAS).color(0x9696BC)
                .fluidTemp(775).flags(FLAMMABLE).buildAndRegister();

        HydroCrackedEthylene = new Material.Builder("hydrocracked_ethylene")
                .fluid(FluidTypes.GAS).color(0xA3A3A0)
                .fluidTemp(775).flags(FLAMMABLE).buildAndRegister();

        HydroCrackedPropane = new Material.Builder("hydrocracked_propane")
                .fluid(FluidTypes.GAS).color(0xBEA540)
                .fluidTemp(775).flags(FLAMMABLE).buildAndRegister();

        HydroCrackedPropene = new Material.Builder("hydrocracked_propene")
                .fluid(FluidTypes.GAS).color(0xBEA540)
                .fluidTemp(775).flags(FLAMMABLE).buildAndRegister();

        ImpureEnrichedNaquadahSolution = new Material.Builder("impure_enriched_naquadah_solution")
                .fluid().color(0x388438).buildAndRegister();

        ImpureNaquadriaSolution = new Material.Builder("impure_naquadria_solution")
                .fluid().color(0x518451).buildAndRegister();

        IndiumConcentrate = new Material.Builder("indium_concentrate")
                .fluid(FluidTypes.ACID).color(0x0E2950).buildAndRegister();

        Lapotron = new Material.Builder("lapotron")
                .gem()
                .color(0x2C39B1).iconSet(DIAMOND)
                .flags(NO_UNIFICATION)
                .buildAndRegister();

        Lava = new Material.Builder("lava")
                .fluid().color(0xFF4000).fluidTemp(1300).buildAndRegister();

        LightFuel = new Material.Builder("light_fuel")
                .fluid().fluidCustomTexture().flags(FLAMMABLE).buildAndRegister();

        LightlyHydroCrackedGas = new Material.Builder("lightly_hydrocracked_gas")
                .fluid(FluidTypes.GAS).color(0xB4B4B4)
                .fluidTemp(775).flags(FLAMMABLE).buildAndRegister();

        LightlyHydroCrackedHeavyFuel = new Material.Builder("lightly_hydrocracked_heavy_fuel")
                .fluid().color(0xFFFF00).fluidTemp(775).flags(FLAMMABLE).buildAndRegister();

        LightlyHydroCrackedLightFuel = new Material.Builder("lightly_hydrocracked_light_fuel")
                .fluid().color(0xB7AF08).fluidTemp(775).flags(FLAMMABLE).buildAndRegister();

        LightlyHydroCrackedNaphtha = new Material.Builder("lightly_hydrocracked_naphtha")
                .fluid().color(0xBFB608).fluidTemp(775).flags(FLAMMABLE).buildAndRegister();

        LightlySteamCrackedGas = new Material.Builder("lightly_steamcracked_gas")
                .fluid(FluidTypes.GAS).color(0xB4B4B4)
                .fluidTemp(775).flags(FLAMMABLE).buildAndRegister();

        LightlySteamCrackedHeavyFuel = new Material.Builder("lightly_steamcracked_heavy_fuel")
                .fluid().fluidCustomTexture().fluidTemp(775).flags(FLAMMABLE).buildAndRegister();

        LightlySteamCrackedLightFuel = new Material.Builder("lightly_steamcracked_light_fuel")
                .fluid().fluidCustomTexture().fluidTemp(775).flags(FLAMMABLE).buildAndRegister();

        LightlySteamCrackedNaphtha = new Material.Builder("lightly_steamcracked_naphtha")
                .fluid().color(0xBFB608).fluidTemp(775).flags(FLAMMABLE).buildAndRegister();

        LPG = new Material.Builder("lpg")
                .fluid(FluidTypes.GAS).fluidCustomTexture().flags(FLAMMABLE, EXPLOSIVE)
                .buildAndRegister();

        Lubricant = new Material.Builder("lubricant")
                .fluid().fluidCustomTexture().buildAndRegister();

        McGuffium239 = new Material.Builder("mc_guffium_239")
                .fluid().fluidCustomTexture().buildAndRegister();

        Meat = new Material.Builder("meat")
                .dust(1)
                .color(0xC14C4C).iconSet(SAND)
                .buildAndRegister();

        Milk = new Material.Builder("milk")
                .fluid().fluidCustomTexture()
                .color(0xFEFEFE).iconSet(FINE)
                .fluidTemp(295)
                .buildAndRegister();

        Mutagen = new Material.Builder("mutagen")
                .fluid().color(0x00FF7F).buildAndRegister();

        Naphtha = new Material.Builder("naphtha")
                .fluid().fluidCustomTexture().flags(FLAMMABLE).buildAndRegister();

        NaquadriaSolution = new Material.Builder("naquadria_solution")
                .fluid().color(0x61AD61).buildAndRegister();

        NaquadriaWaste = new Material.Builder("naquadria_waste")
                .fluid().color(0x425B42).buildAndRegister();

        NaturalGas = new Material.Builder("natural_gas")
                .fluid(FluidTypes.GAS, true).fluidCustomTexture()
                .flags(FLAMMABLE, EXPLOSIVE).buildAndRegister();

        Netherrack = new Material.Builder("netherrack")
                .dust(1)
                .color(0xC80000)
                .flags(NO_SMASHING, FLAMMABLE)
                .buildAndRegister();

        NetherStar = new Material.Builder("nether_star")
                .gem(4)
                .iconSet(NETHERSTAR)
                .flags(NO_SMASHING, NO_SMELTING, GENERATE_LENS)
                .buildAndRegister();

        Oil = new Material.Builder("oil")
                .fluid(FluidTypes.LIQUID, true).fluidCustomTexture()
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE)
                .buildAndRegister();

        OilHeavy = new Material.Builder("oil_heavy")
                .fluid(FluidTypes.LIQUID, true).fluidCustomTexture()
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE)
                .buildAndRegister();

        OilLight = new Material.Builder("oil_light")
                .fluid(FluidTypes.LIQUID, true).fluidCustomTexture()
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE)
                .buildAndRegister();

        Oilsands = new Material.Builder("oilsands")
                .dust(1).ore()
                .color(0x0A0A0A).iconSet(SAND)
                .flags(FLAMMABLE)
                .buildAndRegister();

        Paper = new Material.Builder("paper")
                .dust(0)
                .color(0xFAFAFA).iconSet(FINE)
                .flags(GENERATE_PLATE, FLAMMABLE, NO_SMELTING, NO_SMASHING,
                        MORTAR_GRINDABLE, EXCLUDE_PLATE_COMPRESSOR_RECIPE)
                .buildAndRegister();

        RareEarth = new Material.Builder("rare_earth")
                .dust(0)
                .color(0x808064).iconSet(FINE)
                .buildAndRegister();

        RawGasoline = new Material.Builder("raw_gasoline")
                .fluid().color(0xFF6400).flags(FLAMMABLE).buildAndRegister();

        RawGrowthMedium = new Material.Builder("raw_growth_medium")
                .fluid().color(0xA47351).buildAndRegister();

        RawOil = new Material.Builder("oil_medium")
                .fluid(FluidTypes.LIQUID, true).fluid()
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE)
                .buildAndRegister();

        RefineryGas = new Material.Builder("refinery_gas")
                .fluid(FluidTypes.GAS).fluidCustomTexture().flags(FLAMMABLE).buildAndRegister();

        RocketFuel = new Material.Builder("rocket_fuel")
                .fluid().flags(FLAMMABLE, EXPLOSIVE).color(0xBDB78C).buildAndRegister();

        RubySlurry = new Material.Builder("ruby_slurry")
                .fluid().color(0xff6464).buildAndRegister();

        SapphireSlurry = new Material.Builder("sapphire_slurry")
                .fluid().color(0x6464c8).buildAndRegister();

        SeverelyHydroCrackedGas = new Material.Builder("severely_hydrocracked_gas")
                .fluid(FluidTypes.GAS).color(0xB4B4B4)
                .fluidTemp(775).flags(FLAMMABLE).buildAndRegister();

        SeverelyHydroCrackedHeavyFuel = new Material.Builder("severely_hydrocracked_heavy_fuel")
                .fluid().color(0xFFFF00).fluidTemp(775).flags(FLAMMABLE).buildAndRegister();

        SeverelyHydroCrackedLightFuel = new Material.Builder("severely_hydrocracked_light_fuel")
                .fluid().color(0xB7AF08).fluidTemp(775).flags(FLAMMABLE).buildAndRegister();

        SeverelyHydroCrackedNaphtha = new Material.Builder("severely_hydrocracked_naphtha")
                .fluid().color(0xBFB608).fluidTemp(775).flags(FLAMMABLE).buildAndRegister();

        SeverelySteamCrackedGas = new Material.Builder("severely_steamcracked_gas")
                .fluid(FluidTypes.GAS).color(0xB4B4B4)
                .fluidTemp(775).flags(FLAMMABLE).buildAndRegister();

        SeverelySteamCrackedHeavyFuel = new Material.Builder("severely_steamcracked_heavy_fuel")
                .fluid().fluidCustomTexture().fluidTemp(775).flags(FLAMMABLE).buildAndRegister();

        SeverelySteamCrackedLightFuel = new Material.Builder("severely_steamcracked_light_fuel")
                .fluid().fluidCustomTexture().fluidTemp(775).flags(FLAMMABLE).buildAndRegister();

        SeverelySteamCrackedNaphtha = new Material.Builder("severely_steamcracked_naphtha")
                .fluid().color(0xBFB608).fluidTemp(775).flags(FLAMMABLE).buildAndRegister();

        SteamCrackedButane = new Material.Builder("steamcracked_butane")
                .fluid(FluidTypes.GAS).color(0x852C18)
                .fluidTemp(775).flags(FLAMMABLE).buildAndRegister();

        SteamCrackedButadiene = new Material.Builder("steamcracked_butadiene")
                .fluid(FluidTypes.GAS).color(0xAD5203)
                .fluidTemp(775).flags(FLAMMABLE).buildAndRegister();

        SteamCrackedButene = new Material.Builder("steamcracked_butene")
                .fluid(FluidTypes.GAS).color(0x993E05)
                .fluidTemp(775).flags(FLAMMABLE).buildAndRegister();

        SteamCrackedEthane = new Material.Builder("steamcracked_ethane")
                .fluid(FluidTypes.GAS).color(0x9696BC)
                .fluidTemp(775).flags(FLAMMABLE).buildAndRegister();

        SteamCrackedEthylene = new Material.Builder("steamcracked_ethylene")
                .fluid(FluidTypes.GAS).color(0xA3A3A0)
                .fluidTemp(775).flags(FLAMMABLE).buildAndRegister();

        SteamCrackedPropane = new Material.Builder("steamcracked_propane")
                .fluid(FluidTypes.GAS).color(0xBEA540)
                .fluidTemp(775).flags(FLAMMABLE).buildAndRegister();

        SteamCrackedPropene = new Material.Builder("steamcracked_propene")
                .fluid(FluidTypes.GAS).color(0xBEA540)
                .fluidTemp(775).flags(FLAMMABLE).buildAndRegister();

        SterileGrowthMedium = new Material.Builder("sterilized_growth_medium")
                .fluid().color(0xAC876E).buildAndRegister();

        Stone = new Material.Builder("stone")
                .dust(2)
                .color(0xCDCDCD).iconSet(ROUGH)
                .flags(MORTAR_GRINDABLE, GENERATE_GEAR, NO_SMASHING, NO_SMELTING)
                .buildAndRegister();

        SulfuricGas = new Material.Builder("sulfuric_gas")
                .fluid(FluidTypes.GAS).fluidCustomTexture().buildAndRegister();

        SulfuricHeavyFuel = new Material.Builder("sulfuric_heavy_fuel")
                .fluid().fluidCustomTexture().flags(FLAMMABLE).buildAndRegister();

        SulfuricLightFuel = new Material.Builder("sulfuric_light_fuel")
                .fluid().fluidCustomTexture().fluidTemp(775).flags(FLAMMABLE).buildAndRegister();

        SulfuricNaphtha = new Material.Builder("sulfuric_naphtha")
                .fluid().fluidCustomTexture().flags(FLAMMABLE).buildAndRegister();

        TreatedWood = new Material.Builder("treated_wood")
                .dust(0, 300)
                .color(0x502800).iconSet(WOOD)
                .flags(GENERATE_PLATE, FLAMMABLE, GENERATE_ROD, GENERATE_FRAME)
                .buildAndRegister();

        UUMatter = new Material.Builder("uu_matter")
                .fluid().fluidCustomTexture()
                .fluidTemp(300)
                .buildAndRegister();

        Wheat = new Material.Builder("wheat")
                .dust(0)
                .color(0xFFFFC4).iconSet(FINE)
                .buildAndRegister();

        Wood = new Material.Builder("wood")
                .dust(0, 300)
                .color(0x643200).iconSet(WOOD)
                .toolStats(ToolProperty.Builder.of(1.0F, 1.0F, 128, 1, GTToolType.SOFT_MALLET).build())
                .flags(GENERATE_PLATE, GENERATE_ROD, GENERATE_BOLT_SCREW, GENERATE_LONG_ROD, FLAMMABLE, GENERATE_GEAR, GENERATE_FRAME)
                .buildAndRegister();

        WoodGas = new Material.Builder("wood_gas")
                .fluid(FluidTypes.GAS).color(0xDECD87).buildAndRegister();

        WoodTar = new Material.Builder("wood_tar")
                .fluid().color(0x28170B)
                .flags(STICKY, FLAMMABLE).buildAndRegister();

        WoodVinegar = new Material.Builder("wood_vinegar")
                .fluid().color(0xD45500).buildAndRegister();

        //Free IDs 1517-1521

        //Free IDs 1560-1575

        //Free ID: 1587

        //Free ID: 1593

        // Free ID 1609

        // Free ID 1610

        // Free ID 1611

        // Free ID 1612

    }
}
