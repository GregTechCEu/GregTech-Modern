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
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.FINE;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.ROUGH;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class OrganicChemistryMaterials {

    /**
     * ID RANGE: 1000-1068 (incl.)
     */
    public static void register() {
        SiliconeRubber = REGISTRATE.material("silicone_rubber")
                .polymer()
                .liquid(new FluidBuilder().temperature(900))
                .toolStats(
                        ToolProperty.Builder.of(1.0F, 1.0F, 512, 1, GTToolType.SOFT_MALLET, GTToolType.PLUNGER).build())
                .color(0xF0F0F0).secondaryColor(0xE8E8E0)
                .flags(GENERATE_GEAR, GENERATE_RING, GENERATE_FOIL)
                .components(Carbon, 2, Hydrogen, 6, Oxygen, 1, Silicon, 1)
                .buildAndRegister();

        Nitrobenzene = REGISTRATE.material("nitrobenzene")
                .gas()
                .color(0x704936)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 5, Nitrogen, 1, Oxygen, 2)
                .hazard(HazardProperty.HazardTrigger.INHALATION, GTMedicalConditions.CARCINOGEN)
                .buildAndRegister();

        RawRubber = REGISTRATE.material("raw_rubber")
                .polymer()
                .color(0x54503D).secondaryColor(0x54403D)
                .components(Carbon, 5, Hydrogen, 8)
                .buildAndRegister();

        RawStyreneButadieneRubber = REGISTRATE.material("raw_styrene_butadiene_rubber")
                .dust()
                .color(0x54403D).secondaryColor(0x241520)
                .flags(DISABLE_DECOMPOSITION, FLAMMABLE)
                .components(Carbon, 20, Hydrogen, 26)
                .buildAndRegister()
                .setFormula("(C4H6)3C8H8", true);

        StyreneButadieneRubber = REGISTRATE.material("styrene_butadiene_rubber")
                .polymer()
                .liquid(new FluidBuilder().temperature(1000))
                .toolStats(
                        ToolProperty.Builder.of(1.0F, 1.0F, 512, 1, GTToolType.SOFT_MALLET, GTToolType.PLUNGER).build())
                .color(0x34312b).secondaryColor(0x110B09)
                .flags(GENERATE_FOIL, GENERATE_RING)
                .components(Carbon, 20, Hydrogen, 26)
                .buildAndRegister()
                .setFormula("(C4H6)3C8H8", true);

        PolyvinylAcetate = REGISTRATE.material("polyvinyl_acetate")
                .fluid()
                .color(0xFF9955)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 6, Oxygen, 2)
                .buildAndRegister();

        ReinforcedEpoxyResin = REGISTRATE.material("reinforced_epoxy_resin")
                .polymer()
                .liquid(new FluidBuilder().temperature(600))
                .color(0x9ecaad).secondaryColor(0xb1b2a1).iconSet(ROUGH)
                .appendFlags(STD_METAL)
                .components(Carbon, 6, Hydrogen, 4, Oxygen, 1)
                .buildAndRegister();

        PolyvinylChloride = REGISTRATE.material("polyvinyl_chloride")
                .polymer()
                .liquid(new FluidBuilder().temperature(373))
                .color(0xFF9955).secondaryColor(0x6ca5bf)
                .appendFlags(EXT_METAL, GENERATE_FOIL, GENERATE_RING)
                .components(Carbon, 2, Hydrogen, 3, Chlorine, 1)
                .itemPipeProperties(512, 4)
                .buildAndRegister();

        PolyphenyleneSulfide = REGISTRATE.material("polyphenylene_sulfide")
                .polymer()
                .liquid(new FluidBuilder().temperature(500))
                .color(0x5e5e08).secondaryColor(0x2c373c)
                .appendFlags(EXT_METAL, GENERATE_FOIL)
                .components(Carbon, 6, Hydrogen, 4, Sulfur, 1)
                .buildAndRegister();

        GlycerylTrinitrate = REGISTRATE.material("glyceryl_trinitrate")
                .liquid(new FluidBuilder().customStill())
                .flags(FLAMMABLE, EXPLOSIVE)
                .components(Carbon, 3, Hydrogen, 5, Nitrogen, 3, Oxygen, 9)
                .buildAndRegister();

        Polybenzimidazole = REGISTRATE.material("polybenzimidazole")
                .polymer()
                .liquid(new FluidBuilder().temperature(1450))
                .color(0x464441).secondaryColor(0x382e1b)
                .flags(GENERATE_FOIL)
                .toolStats(
                        ToolProperty.Builder.of(1.0F, 1.0F, 1024, 1, GTToolType.SOFT_MALLET, GTToolType.PLUNGER)
                                .build())
                .components(Carbon, 20, Hydrogen, 12, Nitrogen, 4)
                .fluidPipeProperties(1000, 350, true)
                .buildAndRegister();

        Polydimethylsiloxane = REGISTRATE.material("polydimethylsiloxane")
                .dust()
                .color(0xF5F5F5).secondaryColor(0x9d9fa1)
                .flags(DISABLE_DECOMPOSITION, FLAMMABLE)
                .components(Carbon, 2, Hydrogen, 6, Oxygen, 1, Silicon, 1)
                .buildAndRegister();

        Polyethylene = REGISTRATE.material("polyethylene")
                .polymer(1)
                .liquid(new FluidBuilder().temperature(408))
                .color(0xC8C8C8)
                .flags(GENERATE_FOIL)
                .toolStats(
                        ToolProperty.Builder.of(1.0F, 1.0F, 256, 1, GTToolType.SOFT_MALLET, GTToolType.PLUNGER).build())
                .components(Carbon, 2, Hydrogen, 4)
                .fluidPipeProperties(370, 60, true)
                .buildAndRegister();

        Epoxy = REGISTRATE.material("epoxy")
                .polymer(1)
                .liquid(new FluidBuilder().temperature(400))
                .color(0xf6fabd).secondaryColor(0xC88C14).iconSet(ROUGH)
                .appendFlags(STD_METAL)
                .components(Carbon, 21, Hydrogen, 25, Chlorine, 1, Oxygen, 5)
                .buildAndRegister();

        Polycaprolactam = REGISTRATE.material("polycaprolactam")
                .polymer(1)
                .liquid(new FluidBuilder().temperature(493))
                .color(0x3f3d2d).secondaryColor(0x43432e)
                .appendFlags(STD_METAL, GENERATE_FOIL)
                .components(Carbon, 6, Hydrogen, 11, Nitrogen, 1, Oxygen, 1)
                .buildAndRegister();

        Polytetrafluoroethylene = REGISTRATE.material("polytetrafluoroethylene")
                .polymer(1)
                .liquid(new FluidBuilder().temperature(600))
                .color(0x6e6e6e).secondaryColor(0x202020)
                .appendFlags(STD_METAL, GENERATE_FRAME, GENERATE_FOIL)
                .toolStats(
                        ToolProperty.Builder.of(1.0F, 1.0F, 512, 1, GTToolType.SOFT_MALLET, GTToolType.PLUNGER).build())
                .components(Carbon, 2, Fluorine, 4)
                .fluidPipeProperties(600, 100, true, true, false, false)
                .buildAndRegister();

        Sugar = REGISTRATE.material("sugar")
                .gem(1)
                .color(0xFFFFFF).secondaryColor(0x545468).iconSet(FINE)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 12, Oxygen, 6)
                .buildAndRegister();

        Methane = REGISTRATE.material("methane")
                .gas(new FluidBuilder()
                        .translation("gtceu.fluid.gas_generic"))
                .color(0xFF0078)
                .components(Carbon, 1, Hydrogen, 4)
                .buildAndRegister();

        Epichlorohydrin = REGISTRATE.material("epichlorohydrin")
                .liquid(new FluidBuilder().customStill())
                .color(0x712400)
                .components(Carbon, 3, Hydrogen, 5, Chlorine, 1, Oxygen, 1)
                .buildAndRegister();

        Monochloramine = REGISTRATE.material("monochloramine")
                .gas()
                .color(0x3F9F80)
                .components(Nitrogen, 1, Hydrogen, 2, Chlorine, 1)
                .buildAndRegister();

        Chloroform = REGISTRATE.material("chloroform")
                .fluid()
                .color(0x892CA0)
                .components(Carbon, 1, Hydrogen, 1, Chlorine, 3)
                .hazard(HazardProperty.HazardTrigger.INHALATION, GTMedicalConditions.POISON)
                .buildAndRegister();

        Cumene = REGISTRATE.material("cumene")
                .gas()
                .color(0x552200)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 9, Hydrogen, 12)
                .buildAndRegister();

        Tetrafluoroethylene = REGISTRATE.material("tetrafluoroethylene")
                .gas()
                .color(0x7D7D7D)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Fluorine, 4)
                .buildAndRegister();

        Chloromethane = REGISTRATE.material("chloromethane")
                .gas()
                .color(0xC82CA0)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 1, Hydrogen, 3, Chlorine, 1)
                .buildAndRegister();

        AllylChloride = REGISTRATE.material("allyl_chloride")
                .fluid()
                .color(0x87DEAA)
                .components(Carbon, 2, Methane, 1, HydrochloricAcid, 1)
                .buildAndRegister()
                .setFormula("C3H5Cl", true);

        Isoprene = REGISTRATE.material("isoprene")
                .fluid()
                .color(0x141414)
                .components(Carbon, 5, Hydrogen, 8)
                .buildAndRegister();

        Propane = REGISTRATE.material("propane")
                .gas()
                .color(0xFAE250)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 3, Hydrogen, 8)
                .buildAndRegister();

        Propene = REGISTRATE.material("propene")
                .gas()
                .color(0xFFDD55)
                .components(Carbon, 3, Hydrogen, 6)
                .buildAndRegister();

        Ethane = REGISTRATE.material("ethane")
                .gas()
                .color(0xC8C8FF)
                .components(Carbon, 2, Hydrogen, 6)
                .buildAndRegister();

        Butene = REGISTRATE.material("butene")
                .gas()
                .color(0xCF5005)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 8)
                .buildAndRegister();

        Butane = REGISTRATE.material("butane")
                .gas()
                .color(0xB6371E)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 10)
                .buildAndRegister();

        DissolvedCalciumAcetate = REGISTRATE.material("dissolved_calcium_acetate")
                .fluid()
                .color(0xDCC8B4)
                .flags(DISABLE_DECOMPOSITION)
                .components(Calcium, 1, Carbon, 4, Oxygen, 4, Hydrogen, 6, Water, 1)
                .buildAndRegister();

        VinylAcetate = REGISTRATE.material("vinyl_acetate")
                .fluid()
                .color(0xE1B380)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 6, Oxygen, 2)
                .buildAndRegister();

        MethylAcetate = REGISTRATE.material("methyl_acetate")
                .fluid()
                .color(0xEEC6AF)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 3, Hydrogen, 6, Oxygen, 2)
                .buildAndRegister();

        Ethenone = REGISTRATE.material("ethenone")
                .fluid()
                .color(0x141446)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 2, Oxygen, 1)
                .buildAndRegister();

        Tetranitromethane = REGISTRATE.material("tetranitromethane")
                .fluid()
                .color(0x0F2828)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 1, Nitrogen, 4, Oxygen, 8)
                .hazard(HazardProperty.HazardTrigger.INHALATION, GTMedicalConditions.WEAK_POISON)
                .buildAndRegister();

        Dimethylamine = REGISTRATE.material("dimethylamine")
                .gas()
                .color(0x554469)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 7, Nitrogen, 1)
                .buildAndRegister();

        Dimethylhydrazine = REGISTRATE.material("dimethylhydrazine")
                .fluid()
                .color(0x000055)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 8, Nitrogen, 2)
                .hazard(HazardProperty.HazardTrigger.INHALATION, GTMedicalConditions.CARCINOGEN)
                .buildAndRegister();

        DinitrogenTetroxide = REGISTRATE.material("dinitrogen_tetroxide")
                .gas()
                .color(0x004184)
                .components(Nitrogen, 2, Oxygen, 4)
                .buildAndRegister();

        Dimethyldichlorosilane = REGISTRATE.material("dimethyldichlorosilane")
                .gas()
                .color(0x441650)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 6, Chlorine, 2, Silicon, 1)
                .buildAndRegister();

        Styrene = REGISTRATE.material("styrene")
                .fluid()
                .color(0xD2C8BE)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 8, Hydrogen, 8)
                .buildAndRegister();

        Butadiene = REGISTRATE.material("butadiene")
                .gas()
                .color(0xB55A10)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 6)
                .buildAndRegister();

        Dichlorobenzene = REGISTRATE.material("dichlorobenzene")
                .fluid()
                .color(0x004455)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 4, Chlorine, 2)
                .buildAndRegister();

        AceticAcid = REGISTRATE.material("acetic_acid")
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0xC8B4A0)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 4, Oxygen, 2)
                .buildAndRegister();

        Phenol = REGISTRATE.material("phenol")
                .fluid()
                .color(0x784421)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 6, Oxygen, 1)
                .hazard(HazardProperty.HazardTrigger.INHALATION, GTMedicalConditions.CARCINOGEN)
                .buildAndRegister();

        BisphenolA = REGISTRATE.material("bisphenol_a")
                .fluid()
                .color(0xD4AA00)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 15, Hydrogen, 16, Oxygen, 2)
                .buildAndRegister();

        VinylChloride = REGISTRATE.material("vinyl_chloride")
                .gas()
                .color(0xE1F0F0)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 3, Chlorine, 1)
                .hazard(HazardProperty.HazardTrigger.INHALATION, GTMedicalConditions.CARCINOGEN)
                .buildAndRegister();

        Ethylene = REGISTRATE.material("ethylene")
                .gas()
                .color(0xE1E1E1)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 4)
                .buildAndRegister();

        Benzene = REGISTRATE.material("benzene")
                .fluid()
                .color(0x1A1A1A)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 6)
                .hazard(HazardProperty.HazardTrigger.INHALATION, GTMedicalConditions.CARCINOGEN)
                .buildAndRegister();

        Acetone = REGISTRATE.material("acetone")
                .fluid()
                .color(0xAFAFAF)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 3, Hydrogen, 6, Oxygen, 1)
                .buildAndRegister();

        Glycerol = REGISTRATE.material("glycerol")
                .fluid()
                .color(0x87DE87)
                .components(Carbon, 3, Hydrogen, 8, Oxygen, 3)
                .buildAndRegister();

        Methanol = REGISTRATE.material("methanol")
                .fluid()
                .color(0xAA8800)
                .components(Carbon, 1, Hydrogen, 4, Oxygen, 1)
                .hazard(HazardProperty.HazardTrigger.INHALATION, GTMedicalConditions.METHANOL_POISONING)
                .buildAndRegister();

        Ethanol = REGISTRATE.material("ethanol")
                .liquid(new FluidBuilder().customStill())
                .components(Carbon, 2, Hydrogen, 6, Oxygen, 1)
                .flags(DISABLE_DECOMPOSITION)
                // TODO ethanol intoxication .hazard(HazardProperty.HazardTrigger.INHALATION,
                .buildAndRegister();

        Toluene = REGISTRATE.material("toluene")
                .liquid(new FluidBuilder().customStill())
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 7, Hydrogen, 8)
                .buildAndRegister();

        DiphenylIsophtalate = REGISTRATE.material("diphenyl_isophthalate")
                .fluid()
                .color(0x246E57)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 20, Hydrogen, 14, Oxygen, 4)
                .buildAndRegister();

        PhthalicAcid = REGISTRATE.material("phthalic_acid")
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0xD1D1D1)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 8, Hydrogen, 6, Oxygen, 4)
                .buildAndRegister()
                .setFormula("C6H4(CO2H)2", true);

        Dimethylbenzene = REGISTRATE.material("dimethylbenzene")
                .fluid()
                .color(0x669C40)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 8, Hydrogen, 10)
                .buildAndRegister()
                .setFormula("C6H4(CH3)2", true);

        Diaminobenzidine = REGISTRATE.material("diaminobenzidine")
                .fluid()
                .color(0x337D59)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 12, Hydrogen, 14, Nitrogen, 4)
                .buildAndRegister()
                .setFormula("(C6H3(NH2)2)2", true);

        Dichlorobenzidine = REGISTRATE.material("dichlorobenzidine")
                .fluid()
                .color(0xA1DEA6)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 12, Hydrogen, 10, Chlorine, 2, Nitrogen, 2)
                .buildAndRegister()
                .setFormula("(C6H3Cl(NH2))2", true);

        Nitrochlorobenzene = REGISTRATE.material("nitrochlorobenzene")
                .fluid()
                .color(0x8FB51A)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 4, Chlorine, 1, Nitrogen, 1, Oxygen, 2)
                .buildAndRegister();

        Chlorobenzene = REGISTRATE.material("chlorobenzene")
                .fluid()
                .color(0x326A3E)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 5, Chlorine, 1)
                .hazard(HazardProperty.HazardTrigger.INHALATION, GTMedicalConditions.CARCINOGEN)
                .buildAndRegister();

        Octane = REGISTRATE.material("octane")
                .fluid()
                .flags(DISABLE_DECOMPOSITION)
                .color(0x8A0A09)
                .components(Carbon, 8, Hydrogen, 18)
                .buildAndRegister();

        EthylTertButylEther = REGISTRATE.material("ethyl_tertbutyl_ether")
                .fluid()
                .flags(DISABLE_DECOMPOSITION)
                .color(0xB15C06)
                .components(Carbon, 6, Hydrogen, 14, Oxygen, 1)
                .buildAndRegister();

        Ethylbenzene = REGISTRATE.material("ethylbenzene")
                .fluid()
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 8, Hydrogen, 10)
                .hazard(HazardProperty.HazardTrigger.INHALATION, GTMedicalConditions.CARCINOGEN)
                .buildAndRegister();

        Naphthalene = REGISTRATE.material("naphthalene")
                .fluid()
                .flags(DISABLE_DECOMPOSITION)
                .color(0xF4F4D7)
                .components(Carbon, 10, Hydrogen, 8)
                .buildAndRegister();

        Rubber = REGISTRATE.material("rubber")
                .polymer(0)
                .liquid(new FluidBuilder().temperature(400))
                .color(0x353529).secondaryColor(0x080808)
                .toolStats(
                        ToolProperty.Builder.of(1.0F, 1.0F, 256, 1, GTToolType.SOFT_MALLET, GTToolType.PLUNGER).build())
                .flags(GENERATE_RING, GENERATE_FOIL)
                .components(Carbon, 5, Hydrogen, 8)
                .buildAndRegister();

        Cyclohexane = REGISTRATE.material("cyclohexane")
                .fluid()
                .color(0xe8b113).secondaryColor(0x602a10)
                .components(Carbon, 6, Hydrogen, 12)
                .buildAndRegister();

        NitrosylChloride = REGISTRATE.material("nitrosyl_chloride")
                .gas()
                .flags(FLAMMABLE)
                .color(0xF3F100)
                .components(Nitrogen, 1, Oxygen, 1, Chlorine, 1)
                .buildAndRegister();

        CyclohexanoneOxime = REGISTRATE.material("cyclohexanone_oxime")
                .dust()
                .flags(DISABLE_DECOMPOSITION, FLAMMABLE)
                .color(0xEBEBF0).iconSet(ROUGH)
                .components(Carbon, 6, Hydrogen, 11, Nitrogen, 1, Oxygen, 1)
                .buildAndRegister()
                .setFormula("C6H11NO", true);

        Caprolactam = REGISTRATE.material("caprolactam")
                .dust()
                .flags(DISABLE_DECOMPOSITION, FLAMMABLE)
                .color(0xfffef8).secondaryColor(0xbab7a2)
                .components(Carbon, 6, Hydrogen, 11, Nitrogen, 1, Oxygen, 1)
                .buildAndRegister()
                .setFormula("(CH2)5C(O)NH", true);

        Butyraldehyde = REGISTRATE.material("butyraldehyde")
                .fluid()
                .color(0x554A3F)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 8, Oxygen, 1)
                .hazard(HazardProperty.HazardTrigger.INHALATION, GTMedicalConditions.NAUSEA, false)
                .buildAndRegister();

        PolyvinylButyral = REGISTRATE.material("polyvinyl_butyral")
                .ingot().fluid()
                .color(0x3e7051).secondaryColor(0x535648)
                .flags(GENERATE_PLATE, DISABLE_DECOMPOSITION, NO_SMASHING)
                .components(Butyraldehyde, 1, PolyvinylAcetate, 1)
                .buildAndRegister();

        Biphenyl = REGISTRATE.material("biphenyl")
                .dust()
                .color(0x8B8C4F).iconSet(FINE)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 12, Hydrogen, 10)
                .buildAndRegister()
                .setFormula("(C6H5)2", true);

        PolychlorinatedBiphenyl = REGISTRATE.material("polychlorinated_biphenyl")
                .fluid()
                .color(0xCACC0E)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 12, Hydrogen, 8, Chlorine, 2)
                .hazard(HazardProperty.HazardTrigger.INHALATION, GTMedicalConditions.CARCINOGEN)
                .buildAndRegister()
                .setFormula("(C6H4Cl)2", true);

        AceticAnhydride = REGISTRATE.material("acetic_anhydride")
                .fluid()
                .color(0xE0D182)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 6, Oxygen, 3)
                .buildAndRegister()
                .setFormula("(CH3CO)2O", true);

        AminoPhenol = REGISTRATE.material("aminophenol")
                .fluid()
                .color(0x784421)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 7, Nitrogen, 1, Oxygen, 1)
                .buildAndRegister()
                .setFormula("H2NC6H4OH", true);

        Paracetamol = REGISTRATE.material("paracetamol")
                .dust()
                .color(0xF2EDCB)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 8, Hydrogen, 9, Nitrogen, 1, Oxygen, 2)
                .buildAndRegister();

        AmmoniumFormate = REGISTRATE.material("ammonium_formate")
                .gas()
                .color(0x93badb)
                .components(Carbon, 1, Hydrogen, 5, Nitrogen, 1, Oxygen, 2)
                .hazard(HazardProperty.HazardTrigger.INHALATION, GTMedicalConditions.IRRITANT)
                .buildAndRegister();

        Formamide = REGISTRATE.material("formamide")
                .liquid()
                .color(0x5cccb6)
                .components(Carbon, 1, Hydrogen, 3, Nitrogen, 1, Oxygen, 1)
                .hazard(HazardProperty.HazardTrigger.INHALATION, GTMedicalConditions.CHEMICAL_BURNS)
                .buildAndRegister();
    }
}
