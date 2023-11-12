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

public class OrganicChemistryMaterials {
    /**
     * ID RANGE: 1000-1068 (incl.)
     */
    public static void register() {
        SiliconeRubber = new Material.Builder("silicone_rubber")
                .polymer()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(900))
                .color(0xF0F0F0).secondaryColor(0xE8E8E0)
                .flags(GENERATE_GEAR, GENERATE_RING, GENERATE_FOIL)
                .components(Carbon, 2, Hydrogen, 6, Oxygen, 1, Silicon, 1)
                .buildAndRegister();

        Nitrobenzene = new Material.Builder("nitrobenzene")
                .gas()
                .color(0x704936)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 5, Nitrogen, 1, Oxygen, 2)
                .buildAndRegister();

        RawRubber = new Material.Builder("raw_rubber")
                .polymer()
                .color(0x54503D).secondaryColor(0x54403D)
                .components(Carbon, 5, Hydrogen, 8)
                .buildAndRegister();

        RawStyreneButadieneRubber = new Material.Builder("raw_styrene_butadiene_rubber")
                .dust()
                .color(0x54403D).secondaryColor(0x241520).iconSet(SHINY)
                .flags(DISABLE_DECOMPOSITION, FLAMMABLE)
                .components(Carbon, 20, Hydrogen, 26)
                .buildAndRegister()
                .setFormula("(C4H6)3C8H8", true);

        StyreneButadieneRubber = new Material.Builder("styrene_butadiene_rubber")
                .polymer()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(1000))
                .color(0x34312b).secondaryColor(0x110B09).iconSet(SHINY)
                .flags(GENERATE_FOIL, GENERATE_RING)
                .components(Carbon, 20, Hydrogen, 26)
                .buildAndRegister()
                .setFormula("(C4H6)3C8H8", true);

        PolyvinylAcetate = new Material.Builder("polyvinyl_acetate")
                .fluid()
                .color(0xFF9955)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 6, Oxygen, 2)
                .buildAndRegister();

        ReinforcedEpoxyResin = new Material.Builder("reinforced_epoxy_resin")
                .polymer()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(600))
                .color(0xA07A10)
                .appendFlags(STD_METAL)
                .components(Carbon, 6, Hydrogen, 4, Oxygen, 1)
                .buildAndRegister();

        PolyvinylChloride = new Material.Builder("polyvinyl_chloride")
                .polymer()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(373))
                .color(0xFF9955)
                .appendFlags(EXT_METAL, GENERATE_FOIL)
                .components(Carbon, 2, Hydrogen, 3, Chlorine, 1)
                .itemPipeProperties(512, 4)
                .buildAndRegister();

        PolyphenyleneSulfide = new Material.Builder("polyphenylene_sulfide")
                .polymer()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(500))
                .color(0xAA8800)
                .appendFlags(EXT_METAL, GENERATE_FOIL)
                .components(Carbon, 6, Hydrogen, 4, Sulfur, 1)
                .buildAndRegister();

        GlycerylTrinitrate = new Material.Builder("glyceryl_trinitrate")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().customStill())
                .flags(FLAMMABLE, EXPLOSIVE)
                .components(Carbon, 3, Hydrogen, 5, Nitrogen, 3, Oxygen, 9)
                .buildAndRegister();

        Polybenzimidazole = new Material.Builder("polybenzimidazole")
                .polymer()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(1450))
                .color(0x2D2D2D)
                .flags(EXCLUDE_BLOCK_CRAFTING_RECIPES, GENERATE_FOIL)
                .toolStats(ToolProperty.Builder.of(1.0F, 1.0F, 128, 1, GTToolType.SOFT_MALLET).build())
                .components(Carbon, 20, Hydrogen, 12, Nitrogen, 4)
                .fluidPipeProperties(1000, 350, true)
                .buildAndRegister();

        Polydimethylsiloxane = new Material.Builder("polydimethylsiloxane")
                .dust()
                .color(0xF5F5F5)
                .flags(DISABLE_DECOMPOSITION, FLAMMABLE)
                .components(Carbon, 2, Hydrogen, 6, Oxygen, 1, Silicon, 1)
                .buildAndRegister();

        Polyethylene = new Material.Builder("polyethylene")
                .polymer(1)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(408))
                .color(0xC8C8C8)
                .flags(GENERATE_FOIL)
                .toolStats(ToolProperty.Builder.of(1.0F, 1.0F, 128, 1, GTToolType.SOFT_MALLET).build())
                .components(Carbon, 2, Hydrogen, 4)
                .fluidPipeProperties(370, 50, true)
                .buildAndRegister();

        Epoxy = new Material.Builder("epoxy")
                .polymer(1)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(400))
                .color(0xC88C14)
                .appendFlags(STD_METAL)
                .components(Carbon, 21, Hydrogen, 25, Chlorine, 1, Oxygen, 5)
                .buildAndRegister();

        Polycaprolactam = new Material.Builder("polycaprolactam")
                .polymer(1)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(493))
                .color(0x323232)
                .appendFlags(STD_METAL, GENERATE_FOIL)
                .components(Carbon, 6, Hydrogen, 11, Nitrogen, 1, Oxygen, 1)
                .buildAndRegister();

        Polytetrafluoroethylene = new Material.Builder("polytetrafluoroethylene")
                .polymer(1)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(600))
                .color(0x646464)
                .appendFlags(STD_METAL, GENERATE_FRAME, GENERATE_FOIL)
                .toolStats(ToolProperty.Builder.of(1.0F, 1.0F, 128, 1, GTToolType.SOFT_MALLET).build())
                .components(Carbon, 2, Fluorine, 4)
                .fluidPipeProperties(600, 100, true, true, false, false)
                .buildAndRegister();

        Sugar = new Material.Builder("sugar")
                .gem(1)
                .color(0xFFFFFF).secondaryColor(0x545468).iconSet(FINE)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 12, Oxygen, 6)
                .buildAndRegister();

        Methane = new Material.Builder("methane")
                .fluid(FluidStorageKeys.GAS, new FluidBuilder()
                .translation("gtceu.fluid.gas_generic"))
                .color(0xFF0078).iconSet(FLUID)
                .components(Carbon, 1, Hydrogen, 4)
                .buildAndRegister();

        Epichlorohydrin = new Material.Builder("epichlorohydrin")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().customStill())
                .color(0x712400)
                .components(Carbon, 3, Hydrogen, 5, Chlorine, 1, Oxygen, 1)
                .buildAndRegister();

        Monochloramine = new Material.Builder("monochloramine")
                .gas()
                .color(0x3F9F80)
                .components(Nitrogen, 1, Hydrogen, 2, Chlorine, 1)
                .buildAndRegister();

        Chloroform = new Material.Builder("chloroform")
                .fluid()
                .color(0x892CA0)
                .components(Carbon, 1, Hydrogen, 1, Chlorine, 3)
                .buildAndRegister();

        Cumene = new Material.Builder("cumene")
                .gas()
                .color(0x552200)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 9, Hydrogen, 12)
                .buildAndRegister();

        Tetrafluoroethylene = new Material.Builder("tetrafluoroethylene")
                .gas()
                .color(0x7D7D7D)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Fluorine, 4)
                .buildAndRegister();

        Chloromethane = new Material.Builder("chloromethane")
                .gas()
                .color(0xC82CA0)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 1, Hydrogen, 3, Chlorine, 1)
                .buildAndRegister();

        AllylChloride = new Material.Builder("allyl_chloride")
                .fluid()
                .color(0x87DEAA)
                .components(Carbon, 2, Methane, 1, HydrochloricAcid, 1)
                .buildAndRegister()
                .setFormula("C3H5Cl", true);

        Isoprene = new Material.Builder("isoprene")
                .fluid()
                .color(0x141414)
                .components(Carbon, 5, Hydrogen, 8)
                .buildAndRegister();

        Propane = new Material.Builder("propane")
                .gas()
                .color(0xFAE250)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 3, Hydrogen, 8)
                .buildAndRegister();

        Propene = new Material.Builder("propene")
                .gas()
                .color(0xFFDD55)
                .components(Carbon, 3, Hydrogen, 6)
                .buildAndRegister();

        Ethane = new Material.Builder("ethane")
                .gas()
                .color(0xC8C8FF)
                .components(Carbon, 2, Hydrogen, 6)
                .buildAndRegister();

        Butene = new Material.Builder("butene")
                .gas()
                .color(0xCF5005)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 8)
                .buildAndRegister();

        Butane = new Material.Builder("butane")
                .gas()
                .color(0xB6371E)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 10)
                .buildAndRegister();

        DissolvedCalciumAcetate = new Material.Builder("dissolved_calcium_acetate")
                .fluid()
                .color(0xDCC8B4)
                .flags(DISABLE_DECOMPOSITION)
                .components(Calcium, 1, Carbon, 4, Oxygen, 4, Hydrogen, 6, Water, 1)
                .buildAndRegister();

        VinylAcetate = new Material.Builder("vinyl_acetate")
                .fluid()
                .color(0xE1B380)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 6, Oxygen, 2)
                .buildAndRegister();

        MethylAcetate = new Material.Builder("methyl_acetate")
                .fluid()
                .color(0xEEC6AF)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 3, Hydrogen, 6, Oxygen, 2)
                .buildAndRegister();

        Ethenone = new Material.Builder("ethenone")
                .fluid()
                .color(0x141446)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 2, Oxygen, 1)
                .buildAndRegister();

        Tetranitromethane = new Material.Builder("tetranitromethane")
                .fluid()
                .color(0x0F2828)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 1, Nitrogen, 4, Oxygen, 8)
                .buildAndRegister();

        Dimethylamine = new Material.Builder("dimethylamine")
                .gas()
                .color(0x554469)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 7, Nitrogen, 1)
                .buildAndRegister();

        Dimethylhydrazine = new Material.Builder("dimethylhydrazine")
                .fluid()
                .color(0x000055)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 8, Nitrogen, 2)
                .buildAndRegister();

        DinitrogenTetroxide = new Material.Builder("dinitrogen_tetroxide")
                .gas()
                .color(0x004184)
                .components(Nitrogen, 2, Oxygen, 4)
                .buildAndRegister();

        Dimethyldichlorosilane = new Material.Builder("dimethyldichlorosilane")
                .gas()
                .color(0x441650)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 6, Chlorine, 2, Silicon, 1)
                .buildAndRegister();

        Styrene = new Material.Builder("styrene")
                .fluid()
                .color(0xD2C8BE)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 8, Hydrogen, 8)
                .buildAndRegister();

        Butadiene = new Material.Builder("butadiene")
                .gas()
                .color(0xB55A10)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 6)
                .buildAndRegister();

        Dichlorobenzene = new Material.Builder("dichlorobenzene")
                .fluid()
                .color(0x004455)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 4, Chlorine, 2)
                .buildAndRegister();

        AceticAcid = new Material.Builder("acetic_acid")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0xC8B4A0)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 4, Oxygen, 2)
                .buildAndRegister();

        Phenol = new Material.Builder("phenol")
                .fluid()
                .color(0x784421)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 6, Oxygen, 1)
                .buildAndRegister();

        BisphenolA = new Material.Builder("bisphenol_a")
                .fluid()
                .color(0xD4AA00)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 15, Hydrogen, 16, Oxygen, 2)
                .buildAndRegister();

        VinylChloride = new Material.Builder("vinyl_chloride")
                .gas()
                .color(0xE1F0F0)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 3, Chlorine, 1)
                .buildAndRegister();

        Ethylene = new Material.Builder("ethylene")
                .gas()
                .color(0xE1E1E1)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 4)
                .buildAndRegister();

        Benzene = new Material.Builder("benzene")
                .fluid()
                .color(0x1A1A1A)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 6)
                .buildAndRegister();

        Acetone = new Material.Builder("acetone")
                .fluid()
                .color(0xAFAFAF)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 3, Hydrogen, 6, Oxygen, 1)
                .buildAndRegister();

        Glycerol = new Material.Builder("glycerol")
                .fluid()
                .color(0x87DE87)
                .components(Carbon, 3, Hydrogen, 8, Oxygen, 3)
                .buildAndRegister();

        Methanol = new Material.Builder("methanol")
                .fluid()
                .color(0xAA8800)
                .components(Carbon, 1, Hydrogen, 4, Oxygen, 1)
                .buildAndRegister();

        // FREE ID 1053

        Ethanol = new Material.Builder("ethanol")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().customStill())
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 6, Oxygen, 1)
                .buildAndRegister();

        Toluene = new Material.Builder("toluene")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().customStill())
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 7, Hydrogen, 8)
                .buildAndRegister();

        DiphenylIsophtalate = new Material.Builder("diphenyl_isophthalate")
                .fluid()
                .color(0x246E57)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 20, Hydrogen, 14, Oxygen, 4)
                .buildAndRegister();

        PhthalicAcid = new Material.Builder("phthalic_acid")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0xD1D1D1)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 8, Hydrogen, 6, Oxygen, 4)
                .buildAndRegister()
                .setFormula("C6H4(CO2H)2", true);

        Dimethylbenzene = new Material.Builder("dimethylbenzene")
                .fluid()
                .color(0x669C40)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 8, Hydrogen, 10)
                .buildAndRegister()
                .setFormula("C6H4(CH3)2", true);

        Diaminobenzidine = new Material.Builder("diaminobenzidine")
                .fluid()
                .color(0x337D59)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 12, Hydrogen, 14, Nitrogen, 4)
                .buildAndRegister()
                .setFormula("(C6H3(NH2)2)2", true);

        Dichlorobenzidine = new Material.Builder("dichlorobenzidine")
                .fluid()
                .color(0xA1DEA6)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 12, Hydrogen, 10, Chlorine, 2, Nitrogen, 2)
                .buildAndRegister()
                .setFormula("(C6H3Cl(NH2))2", true);

        Nitrochlorobenzene = new Material.Builder("nitrochlorobenzene")
                .fluid()
                .color(0x8FB51A)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 4, Chlorine, 1, Nitrogen, 1, Oxygen, 2)
                .buildAndRegister();

        Chlorobenzene = new Material.Builder("chlorobenzene")
                .fluid()
                .color(0x326A3E)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 5, Chlorine, 1)
                .buildAndRegister();

        Octane = new Material.Builder("octane")
                .fluid()
                .flags(DISABLE_DECOMPOSITION)
                .color(0x8A0A09)
                .components(Carbon, 8, Hydrogen, 18)
                .buildAndRegister();

        EthylTertButylEther = new Material.Builder("ethyl_tertbutyl_ether")
                .fluid()
                .flags(DISABLE_DECOMPOSITION)
                .color(0xB15C06)
                .components(Carbon, 6, Hydrogen, 14, Oxygen, 1)
                .buildAndRegister();

        Ethylbenzene = new Material.Builder("ethylbenzene")
                .fluid()
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 8, Hydrogen, 10)
                .buildAndRegister();

        Naphthalene = new Material.Builder("naphthalene")
                .fluid()
                .flags(DISABLE_DECOMPOSITION)
                .color(0xF4F4D7)
                .components(Carbon, 10, Hydrogen, 8)
                .buildAndRegister();

        Rubber = new Material.Builder("rubber")
                .polymer(0)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(400))
                .color(0x353529).secondaryColor(0x080808).iconSet(SHINY)
                .toolStats(ToolProperty.Builder.of(1.0F, 1.0F, 128, 1, GTToolType.SOFT_MALLET).build())
                .flags(GENERATE_GEAR, GENERATE_RING, GENERATE_FOIL, GENERATE_BOLT_SCREW)
                .components(Carbon, 5, Hydrogen, 8)
                .buildAndRegister();

        Cyclohexane = new Material.Builder("cyclohexane")
                .fluid()
                .color(0xF2F2F2E7)
                .components(Carbon, 6, Hydrogen, 12)
                .buildAndRegister();

        NitrosylChloride = new Material.Builder("nitrosyl_chloride")
                .gas()
                .flags(FLAMMABLE)
                .color(0xF3F100)
                .components(Nitrogen, 1, Oxygen, 1, Chlorine, 1)
                .buildAndRegister();

        CyclohexanoneOxime = new Material.Builder("cyclohexanone_oxime")
                .dust()
                .flags(DISABLE_DECOMPOSITION, FLAMMABLE)
                .color(0xEBEBF0).iconSet(ROUGH)
                .components(Carbon, 6, Hydrogen, 11, Nitrogen, 1, Oxygen, 1)
                .buildAndRegister()
                .setFormula("C6H11NO", true);

        Caprolactam = new Material.Builder("caprolactam")
                .dust()
                .flags(DISABLE_DECOMPOSITION, FLAMMABLE)
                .color(0x676768)
                .components(Carbon, 6, Hydrogen, 11, Nitrogen, 1, Oxygen, 1)
                .buildAndRegister()
                .setFormula("(CH2)5C(O)NH", true);

        Butyraldehyde = new Material.Builder("butyraldehyde")
                .fluid()
                .color(0x554A3F)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 8, Oxygen, 1)
                .buildAndRegister();

        PolyvinylButyral = new Material.Builder("polyvinyl_butyral")
                .ingot().fluid()
                .color(0x347D41)
                .flags(GENERATE_PLATE, DISABLE_DECOMPOSITION, NO_SMASHING)
                .components(Butyraldehyde, 1, PolyvinylAcetate, 1)
                .buildAndRegister();

        Biphenyl = new Material.Builder("biphenyl")
                .dust()
                .color(0x8B8C4F).iconSet(FINE)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 12, Hydrogen, 10)
                .buildAndRegister()
                .setFormula("(C6H5)2", true);

        PolychlorinatedBiphenyl = new Material.Builder("polychlorinated_biphenyl")
                .fluid()
                .color(0xCACC0E)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 12, Hydrogen, 8, Chlorine, 2)
                .buildAndRegister()
                .setFormula("(C6H4Cl)2", true);
    }
}
