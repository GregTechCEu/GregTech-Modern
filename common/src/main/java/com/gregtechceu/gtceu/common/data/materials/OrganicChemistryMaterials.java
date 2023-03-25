package com.gregtechceu.gtceu.common.data.materials;

import com.gregtechceu.gtceu.api.data.chemical.fluid.FluidTypes;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class OrganicChemistryMaterials {
    /**
     * ID RANGE: 1000-1068 (incl.)
     */
    public static void register() {

        AceticAcid = new Material.Builder("acetic_acid")
                .fluid(FluidTypes.ACID)
                .color(0xC8B4A0)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 4, Oxygen, 2)
                .buildAndRegister();

        Acetone = new Material.Builder("acetone")
                .fluid()
                .color(0xAFAFAF)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 3, Hydrogen, 6, Oxygen, 1)
                .buildAndRegister();

        AllylChloride = new Material.Builder("allyl_chloride")
                .fluid()
                .color(0x87DEAA)
                .components(Carbon, 2, Methane, 1, HydrochloricAcid, 1)
                .buildAndRegister()
                .setFormula("C3H5Cl", true);

        Benzene = new Material.Builder("benzene")
                .fluid()
                .color(0x1A1A1A)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 6)
                .buildAndRegister();

        BisphenolA = new Material.Builder("bisphenol_a")
                .fluid()
                .color(0xD4AA00)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 15, Hydrogen, 16, Oxygen, 2)
                .buildAndRegister();

        Butadiene = new Material.Builder("butadiene")
                .fluid(FluidTypes.GAS)
                .color(0xB55A10)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 6)
                .buildAndRegister();

        Butane = new Material.Builder("butane")
                .fluid(FluidTypes.GAS)
                .color(0xB6371E)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 10)
                .buildAndRegister();

        Butene = new Material.Builder("butene")
                .fluid(FluidTypes.GAS)
                .color(0xCF5005)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 8)
                .buildAndRegister();

        Butyraldehyde = new Material.Builder("butyraldehyde")
                .fluid()
                .color(0x554A3F)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 8, Oxygen, 1)
                .buildAndRegister();

        Caprolactam = new Material.Builder("caprolactam")
                .dust()
                .flags(DISABLE_DECOMPOSITION, FLAMMABLE)
                .color(0x676768)
                .components(Carbon, 6, Hydrogen, 11, Nitrogen, 1, Oxygen, 1)
                .buildAndRegister()
                .setFormula("(CH2)5C(O)NH", true);

        Chlorobenzene = new Material.Builder("chlorobenzene")
                .fluid()
                .color(0x326A3E)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 5, Chlorine, 1)
                .buildAndRegister();


        Chloromethane = new Material.Builder("chloromethane")
                .fluid(FluidTypes.GAS)
                .color(0xC82CA0)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 1, Hydrogen, 3, Chlorine, 1)
                .buildAndRegister();

        Chloroform = new Material.Builder("chloroform")
                .fluid()
                .color(0x892CA0)
                .components(Carbon, 1, Hydrogen, 1, Chlorine, 3)
                .buildAndRegister();

        Cumene = new Material.Builder("cumene")
                .fluid(FluidTypes.GAS)
                .color(0x552200)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 9, Hydrogen, 12)
                .buildAndRegister();

        Cyclohexane = new Material.Builder("cyclohexane")
                .fluid()
                .color(0xF2F2F2E7)
                .components(Carbon, 6, Hydrogen, 12)
                .buildAndRegister();

        CyclohexanoneOxime = new Material.Builder("cyclohexanone_oxime")
                .dust()
                .flags(DISABLE_DECOMPOSITION, FLAMMABLE)
                .color(0xEBEBF0).iconSet(ROUGH)
                .components(Carbon, 6, Hydrogen, 11, Nitrogen, 1, Oxygen, 1)
                .buildAndRegister()
                .setFormula("C6H11NO", true);

        Diaminobenzidine = new Material.Builder("diaminobenzidine")
                .fluid()
                .color(0x337D59)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 12, Hydrogen, 14, Nitrogen, 4)
                .buildAndRegister()
                .setFormula("(C6H3(NH2)2)2", true);

        Dichlorobenzene = new Material.Builder("dichlorobenzene")
                .fluid()
                .color(0x004455)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 4, Chlorine, 2)
                .buildAndRegister();

        Dichlorobenzidine = new Material.Builder("dichlorobenzidine")
                .fluid()
                .color(0xA1DEA6)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 12, Hydrogen, 10, Chlorine, 2, Nitrogen, 2)
                .buildAndRegister()
                .setFormula("(C6H3Cl(NH2))2", true);

        Dimethylamine = new Material.Builder("dimethylamine")
                .fluid(FluidTypes.GAS)
                .color(0x554469)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 7, Nitrogen, 1)
                .buildAndRegister();

        Dimethylbenzene = new Material.Builder("dimethylbenzene")
                .fluid()
                .color(0x669C40)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 8, Hydrogen, 10)
                .buildAndRegister()
                .setFormula("C6H4(CH3)2", true);

        Dimethyldichlorosilane = new Material.Builder("dimethyldichlorosilane")
                .fluid(FluidTypes.GAS)
                .color(0x441650)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 6, Chlorine, 2, Silicon, 1)
                .buildAndRegister();

        Dimethylhydrazine = new Material.Builder("dimethylhydrazine")
                .fluid()
                .color(0x000055)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 8, Nitrogen, 2)
                .buildAndRegister();

        DinitrogenTetroxide = new Material.Builder("dinitrogen_tetroxide")
                .fluid(FluidTypes.GAS)
                .color(0x004184)
                .components(Nitrogen, 2, Oxygen, 4)
                .buildAndRegister();

        DiphenylIsophtalate = new Material.Builder("diphenyl_isophthalate")
                .fluid()
                .color(0x246E57)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 20, Hydrogen, 14, Oxygen, 4)
                .buildAndRegister();

        DissolvedCalciumAcetate = new Material.Builder("dissolved_calcium_acetate")
                .fluid()
                .color(0xDCC8B4)
                .flags(DISABLE_DECOMPOSITION)
                .components(Calcium, 1, Carbon, 4, Oxygen, 4, Hydrogen, 6, Water, 1)
                .buildAndRegister();

        Epichlorohydrin = new Material.Builder("epichlorohydrin")
                .fluid()
                .color(0x712400)
                .components(Carbon, 3, Hydrogen, 5, Chlorine, 1, Oxygen, 1)
                .buildAndRegister();

        Epoxy = new Material.Builder("epoxy")
                .polymer(1)
                .color(0xC88C14)
                .flags(STD_METAL)
                .components(Carbon, 21, Hydrogen, 25, Chlorine, 1, Oxygen, 5)
                .fluidTemp(400)
                .buildAndRegister();

        Ethane = new Material.Builder("ethane")
                .fluid(FluidTypes.GAS)
                .color(0xC8C8FF)
                .components(Carbon, 2, Hydrogen, 6)
                .buildAndRegister();

        Ethanol = new Material.Builder("ethanol")
                .fluid().fluidCustomTexture()
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 6, Oxygen, 1)
                .buildAndRegister();

        Ethenone = new Material.Builder("ethenone")
                .fluid()
                .color(0x141446)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 2, Oxygen, 1)
                .buildAndRegister();

        Ethylbenzene = new Material.Builder("ethylbenzene")
                .fluid()
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 8, Hydrogen, 10)
                .buildAndRegister();

        Ethylene = new Material.Builder("ethylene")
                .fluid(FluidTypes.GAS)
                .color(0xE1E1E1)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 4)
                .buildAndRegister();

        EthylTertButylEther = new Material.Builder("ethyl_tertbutyl_ether")
                .fluid()
                .flags(DISABLE_DECOMPOSITION)
                .color(0xB15C06)
                .components(Carbon, 6, Hydrogen, 14, Oxygen, 1)
                .buildAndRegister();

        Glycerol = new Material.Builder("glycerol")
                .fluid()
                .color(0x87DE87)
                .components(Carbon, 3, Hydrogen, 8, Oxygen, 3)
                .buildAndRegister();

        GlycerylTrinitrate = new Material.Builder("glyceryl_trinitrate")
                .fluid().fluidCustomTexture()
                .flags(FLAMMABLE, EXPLOSIVE)
                .components(Carbon, 3, Hydrogen, 5, Nitrogen, 3, Oxygen, 9)
                .buildAndRegister();

        Isoprene = new Material.Builder("isoprene")
                .fluid()
                .color(0x141414)
                .components(Carbon, 5, Hydrogen, 8)
                .buildAndRegister();

        Methane = new Material.Builder("methane")
                .fluid(FluidTypes.GAS)
                .color(0xFF0078).iconSet(GAS)
                .components(Carbon, 1, Hydrogen, 4)
                .buildAndRegister();

        Methanol = new Material.Builder("methanol")
                .fluid()
                .color(0xAA8800)
                .components(Carbon, 1, Hydrogen, 4, Oxygen, 1)
                .buildAndRegister();

        MethylAcetate = new Material.Builder("methyl_acetate")
                .fluid()
                .color(0xEEC6AF)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 3, Hydrogen, 6, Oxygen, 2)
                .buildAndRegister();

        Monochloramine = new Material.Builder("monochloramine")
                .fluid(FluidTypes.GAS)
                .color(0x3F9F80)
                .components(Nitrogen, 1, Hydrogen, 2, Chlorine, 1)
                .buildAndRegister();

        Naphthalene = new Material.Builder("naphthalene")
                .fluid()
                .flags(DISABLE_DECOMPOSITION)
                .color(0xF4F4D7)
                .components(Carbon, 10, Hydrogen, 8)
                .buildAndRegister();

        Nitrobenzene = new Material.Builder("nitrobenzene")
                .fluid(FluidTypes.GAS)
                .color(0x704936)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 5, Nitrogen, 1, Oxygen, 2)
                .buildAndRegister();

        Nitrochlorobenzene = new Material.Builder("nitrochlorobenzene")
                .fluid()
                .color(0x8FB51A)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 4, Chlorine, 1, Nitrogen, 1, Oxygen, 2)
                .buildAndRegister();

        NitrosylChloride = new Material.Builder("nitrosyl_chloride")
                .fluid(FluidTypes.GAS)
                .flags(FLAMMABLE)
                .color(0xF3F100)
                .components(Nitrogen, 1, Oxygen, 1, Chlorine, 1)
                .buildAndRegister();

        Octane = new Material.Builder("octane")
                .fluid()
                .flags(DISABLE_DECOMPOSITION)
                .color(0x8A0A09)
                .components(Carbon, 8, Hydrogen, 18)
                .buildAndRegister();

        Phenol = new Material.Builder("phenol")
                .fluid()
                .color(0x784421)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 6, Oxygen, 1)
                .buildAndRegister();

        PhthalicAcid = new Material.Builder("phthalic_acid")
                .fluid(FluidTypes.ACID)
                .color(0xD1D1D1)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 8, Hydrogen, 6, Oxygen, 4)
                .buildAndRegister()
                .setFormula("C6H4(CO2H)2", true);

        Polybenzimidazole = new Material.Builder("polybenzimidazole")
                .polymer()
                .color(0x2D2D2D)
                .flags(EXCLUDE_BLOCK_CRAFTING_RECIPES, GENERATE_FOIL)
                .toolStats(ToolProperty.Builder.of(1.0F, 1.0F, 128, 1, GTToolType.SOFT_MALLET).build())
                .components(Carbon, 20, Hydrogen, 12, Nitrogen, 4)
                .fluidPipeProperties(1000, 350, true)
                .fluidTemp(1450)
                .buildAndRegister();

        Polycaprolactam = new Material.Builder("polycaprolactam")
                .polymer(1)
                .color(0x323232)
                .flags(STD_METAL, GENERATE_FOIL)
                .components(Carbon, 6, Hydrogen, 11, Nitrogen, 1, Oxygen, 1)
                .fluidTemp(493)
                .buildAndRegister();

        Polydimethylsiloxane = new Material.Builder("polydimethylsiloxane")
                .dust()
                .color(0xF5F5F5)
                .flags(DISABLE_DECOMPOSITION, FLAMMABLE)
                .components(Carbon, 2, Hydrogen, 6, Oxygen, 1, Silicon, 1)
                .buildAndRegister();

        Polyethylene = new Material.Builder("plastic") //todo add polyethylene oredicts
                .polymer(1)
                .color(0xC8C8C8)
                .flags(GENERATE_FOIL)
                .toolStats(ToolProperty.Builder.of(1.0F, 1.0F, 128, 1, GTToolType.SOFT_MALLET).build())
                .components(Carbon, 2, Hydrogen, 4)
                .fluidPipeProperties(370, 50, true)
                .fluidTemp(408)
                .buildAndRegister();

        PolyphenyleneSulfide = new Material.Builder("polyphenylene_sulfide")
                .polymer()
                .color(0xAA8800)
                .flags(EXT_METAL, GENERATE_FOIL)
                .components(Carbon, 6, Hydrogen, 4, Sulfur, 1)
                .fluidTemp(500)
                .buildAndRegister();

        Polytetrafluoroethylene = new Material.Builder("polytetrafluoroethylene")
                .polymer(1)
                .color(0x646464)
                .flags(STD_METAL, GENERATE_FRAME, GENERATE_FOIL)
                .toolStats(ToolProperty.Builder.of(1.0F, 1.0F, 128, 1, GTToolType.SOFT_MALLET).build())
                .components(Carbon, 2, Fluorine, 4)
                .fluidPipeProperties(600, 100, true, true, false, false)
                .fluidTemp(600)
                .buildAndRegister();

        PolyvinylAcetate = new Material.Builder("polyvinyl_acetate")
                .fluid()
                .color(0xFF9955)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 6, Oxygen, 2)
                .buildAndRegister();

        PolyvinylButyral = new Material.Builder("polyvinyl_butyral")
                .ingot().fluid()
                .color(0x347D41)
                .flags(GENERATE_PLATE, DISABLE_DECOMPOSITION, NO_SMASHING)
                .components(Butyraldehyde, 1, PolyvinylAcetate, 1)
                .buildAndRegister();

        PolyvinylChloride = new Material.Builder("polyvinyl_chloride")
                .polymer()
                .color(0xD7E6E6)
                .flags(EXT_METAL, GENERATE_FOIL)
                .components(Carbon, 2, Hydrogen, 3, Chlorine, 1)
                .itemPipeProperties(512, 4)
                .fluidTemp(373)
                .buildAndRegister();

        Propane = new Material.Builder("propane")
                .fluid(FluidTypes.GAS)
                .color(0xFAE250)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 3, Hydrogen, 8)
                .buildAndRegister();

        Propene = new Material.Builder("propene")
                .fluid(FluidTypes.GAS)
                .color(0xFFDD55)
                .components(Carbon, 3, Hydrogen, 6)
                .buildAndRegister();

        RawRubber = new Material.Builder("raw_rubber")
                .polymer()
                .color(0xCCC789)
                .components(Carbon, 5, Hydrogen, 8)
                .buildAndRegister();

        RawStyreneButadieneRubber = new Material.Builder("raw_styrene_butadiene_rubber")
                .dust()
                .color(0x54403D).iconSet(SHINY)
                .flags(DISABLE_DECOMPOSITION, FLAMMABLE)
                .components(Carbon, 20, Hydrogen, 26)
                .buildAndRegister()
                .setFormula("(C4H6)3C8H8", true);

        ReinforcedEpoxyResin = new Material.Builder("reinforced_epoxy_resin")
                .polymer()
                .color(0xA07A10)
                .flags(STD_METAL)
                .components(Carbon, 6, Hydrogen, 4, Oxygen, 1)
                .fluidTemp(600)
                .buildAndRegister();

        Rubber = new Material.Builder("rubber")
                .polymer(0)
                .color(0x000000).iconSet(SHINY)
                .toolStats(ToolProperty.Builder.of(1.0F, 1.0F, 128, 1, GTToolType.SOFT_MALLET).build())
                .flags(GENERATE_GEAR, GENERATE_RING, GENERATE_FOIL, GENERATE_BOLT_SCREW)
                .components(Carbon, 5, Hydrogen, 8)
                .fluidTemp(400)
                .buildAndRegister();

        SiliconeRubber = new Material.Builder("silicone_rubber")
                .polymer()
                .color(0xDCDCDC)
                .flags(GENERATE_GEAR, GENERATE_RING, GENERATE_FOIL)
                .components(Carbon, 2, Hydrogen, 6, Oxygen, 1, Silicon, 1)
                .fluidTemp(900)
                .buildAndRegister();

        Styrene = new Material.Builder("styrene")
                .fluid()
                .color(0xD2C8BE)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 8, Hydrogen, 8)
                .buildAndRegister();

        StyreneButadieneRubber = new Material.Builder("styrene_butadiene_rubber")
                .polymer()
                .color(0x211A18).iconSet(SHINY)
                .flags(GENERATE_FOIL, GENERATE_RING)
                .components(Carbon, 20, Hydrogen, 26)
                .fluidTemp(1000)
                .buildAndRegister()
                .setFormula("(C4H6)3C8H8", true);

        Sugar = new Material.Builder("sugar")
                .gem(1)
                .color(0xFAFAFA).iconSet(FINE)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 12, Oxygen, 6)
                .buildAndRegister();

        Tetrafluoroethylene = new Material.Builder("tetrafluoroethylene")
                .fluid(FluidTypes.GAS)
                .color(0x7D7D7D)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Fluorine, 4)
                .buildAndRegister();

        Tetranitromethane = new Material.Builder("tetranitromethane")
                .fluid()
                .color(0x0F2828)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 1, Nitrogen, 4, Oxygen, 8)
                .buildAndRegister();

        Toluene = new Material.Builder("toluene")
                .fluid()
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 7, Hydrogen, 8)
                .buildAndRegister();

        VinylAcetate = new Material.Builder("vinyl_acetate")
                .fluid()
                .color(0xE1B380)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 6, Oxygen, 2)
                .buildAndRegister();

        VinylChloride = new Material.Builder("vinyl_chloride")
                .fluid(FluidTypes.GAS)
                .color(0xE1F0F0)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 3, Chlorine, 1)
                .buildAndRegister();

        // Free ID 1014

        // FREE ID 1053

    }
}
