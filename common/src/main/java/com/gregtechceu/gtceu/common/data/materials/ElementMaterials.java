package com.gregtechceu.gtceu.common.data.materials;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.fluid.FluidTypes;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty.GasTier;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.common.data.GTElements;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;


public class ElementMaterials {

    public static void register() {
        Actinium = new Material.Builder("actinium")
                .color(0xC3D1FF).iconSet(METALLIC)
                .element(GTElements.Ac)
                .buildAndRegister();

        Aluminium = new Material.Builder("aluminium")
                .ingot().fluid().ore()
                .color(0x80C8F0)
                .appendFlags(EXT2_METAL, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .element(GTElements.Al)
                .toolStats(ToolProperty.Builder.of(6.0F, 7.5F, 768, 2)
                        .enchantability(14).build())
                .rotorStats(10.0f, 2.0f, 128)
                .cableProperties(GTValues.V[4], 1, 1)
                .fluidPipeProperties(1166, 100, true)
                .blastTemp(1700, GasTier.LOW)
                .fluidTemp(933)
                .buildAndRegister();

        Americium = new Material.Builder("americium")
                .ingot(3).fluid()
                .color(0x287869).iconSet(METALLIC)
                .appendFlags(EXT_METAL, GENERATE_FOIL, GENERATE_FINE_WIRE)
                .element(GTElements.Am)
                .itemPipeProperties(64, 64)
                .fluidTemp(1449)
                .buildAndRegister();

        Antimony = new Material.Builder("antimony")
                .ingot().fluid()
                .color(0xDCDCF0).iconSet(SHINY)
                .flags(MORTAR_GRINDABLE)
                .element(GTElements.Sb)
                .fluidTemp(904)
                .buildAndRegister();

        Argon = new Material.Builder("argon")
                .fluid(FluidTypes.GAS).plasma()
                .color(0x00FF00).iconSet(GAS)
                .element(GTElements.Ar)
                .buildAndRegister();

        Arsenic = new Material.Builder("arsenic")
                .dust().fluid()
                .color(0x676756)
                .element(GTElements.As)
                .fluidTemp(887)
                .buildAndRegister();

        Astatine = new Material.Builder("astatine")
                .color(0x241A24)
                .element(GTElements.At)
                .buildAndRegister();

        Barium = new Material.Builder("barium")
                .dust()
                .color(0x83824C).iconSet(METALLIC)
                .element(GTElements.Ba)
                .buildAndRegister();

        Berkelium = new Material.Builder("berkelium")
                .color(0x645A88).iconSet(METALLIC)
                .element(GTElements.Bk)
                .buildAndRegister();

        Beryllium = new Material.Builder("beryllium")
                .ingot().fluid().ore()
                .color(0x64B464).iconSet(METALLIC)
                .appendFlags(STD_METAL)
                .element(GTElements.Be)
                .fluidTemp(1560)
                .buildAndRegister();

        Bismuth = new Material.Builder("bismuth")
                .ingot(1).fluid()
                .color(0x64A0A0).iconSet(METALLIC)
                .element(GTElements.Bi)
                .fluidTemp(545)
                .buildAndRegister();

        Bohrium = new Material.Builder("bohrium")
                .color(0xDC57FF).iconSet(SHINY)
                .element(GTElements.Bh)
                .buildAndRegister();

        Boron = new Material.Builder("boron")
                .dust()
                .color(0xD2FAD2)
                .element(GTElements.B)
                .buildAndRegister();

        Bromine = new Material.Builder("bromine")
                .color(0x500A0A).iconSet(SHINY)
                .element(GTElements.Br)
                .buildAndRegister();

        Caesium = new Material.Builder("caesium")
                .dust()
                .color(0x80620B).iconSet(METALLIC)
                .element(GTElements.Cs)
                .buildAndRegister();

        Calcium = new Material.Builder("calcium")
                .dust()
                .color(0xFFF5DE).iconSet(METALLIC)
                .element(GTElements.Ca)
                .buildAndRegister();

        Californium = new Material.Builder("californium")
                .color(0xA85A12).iconSet(METALLIC)
                .element(GTElements.Cf)
                .buildAndRegister();

        Carbon = new Material.Builder("carbon")
                .dust().fluid()
                .color(0x141414)
                .element(GTElements.C)
                .fluidTemp(4600)
                .buildAndRegister();

        Cadmium = new Material.Builder("cadmium")
                .dust()
                .color(0x32323C).iconSet(SHINY)
                .element(GTElements.Cd)
                .buildAndRegister();

        Cerium = new Material.Builder("cerium")
                .dust().fluid()
                .color(0x87917D).iconSet(METALLIC)
                .element(GTElements.Ce)
                .fluidTemp(1068)
                .buildAndRegister();

        Chlorine = new Material.Builder("chlorine")
                .fluid(FluidTypes.GAS).fluidCustomTexture()
                .element(GTElements.Cl)
                .buildAndRegister();

        Chromium = new Material.Builder("chromium")
                .ingot(3).fluid()
                .color(0xEAC4D8).iconSet(SHINY)
                .appendFlags(EXT_METAL, GENERATE_ROTOR)
                .element(GTElements.Cr)
                .rotorStats(12.0f, 3.0f, 512)
                .fluidPipeProperties(2180, 35, true, true, false, false)
                .blastTemp(1700, GasTier.LOW)
                .fluidTemp(2180)
                .buildAndRegister();

        Cobalt = new Material.Builder("cobalt")
                .ingot().fluid().ore() // leave for TiCon ore processing
                .color(0x5050FA).iconSet(METALLIC)
                .appendFlags(EXT_METAL)
                .element(GTElements.Co)
                .cableProperties(GTValues.V[1], 2, 2)
                .itemPipeProperties(2560, 2.0f)
                .fluidTemp(1768)
                .buildAndRegister();

        Copernicium = new Material.Builder("copernicium")
                .color(0xFFFEFF)
                .element(GTElements.Cn)
                .buildAndRegister();

        Copper = new Material.Builder("copper")
                .ingot(1).fluid().ore()
                .color(0xFF6400).iconSet(SHINY)
                .appendFlags(EXT_METAL, MORTAR_GRINDABLE, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .element(GTElements.Cu)
                .cableProperties(GTValues.V[2], 1, 2)
                .fluidPipeProperties(1696, 6, true)
                .fluidTemp(1358)
                .buildAndRegister();

        Curium = new Material.Builder("curium")
                .color(0x7B544E).iconSet(METALLIC)
                .element(GTElements.Cm)
                .buildAndRegister();

        Darmstadtium = new Material.Builder("darmstadtium")
                .ingot().fluid()
                .color(0x578062)
                .appendFlags(EXT2_METAL, GENERATE_ROTOR, GENERATE_DENSE, GENERATE_SMALL_GEAR)
                .element(GTElements.Ds)
                .buildAndRegister();

        Deuterium = new Material.Builder("deuterium")
                .fluid(FluidTypes.GAS)
                .fluidCustomTexture()
                .element(GTElements.D)
                .buildAndRegister();

        Dubnium = new Material.Builder("dubnium")
                .color(0xD3FDFF).iconSet(SHINY)
                .element(GTElements.Db)
                .buildAndRegister();

        Dysprosium = new Material.Builder("dysprosium")
                .iconSet(METALLIC)
                .element(GTElements.Dy)
                .buildAndRegister();

        Einsteinium = new Material.Builder("einsteinium")
                .color(0xCE9F00).iconSet(METALLIC)
                .element(GTElements.Es)
                .buildAndRegister();

        Erbium = new Material.Builder("erbium")
                .iconSet(METALLIC)
                .element(GTElements.Er)
                .buildAndRegister();

        Europium = new Material.Builder("europium")
                .ingot().fluid()
                .color(0x20FFFF).iconSet(METALLIC)
                .appendFlags(STD_METAL, GENERATE_LONG_ROD, GENERATE_FINE_WIRE, GENERATE_SPRING, GENERATE_FOIL, GENERATE_FRAME)
                .element(GTElements.Eu)
                .cableProperties(GTValues.V[GTValues.UHV], 2, 32)
                .fluidPipeProperties(7750, 300, true)
                .blastTemp(6000, GasTier.MID, GTValues.VA[GTValues.IV], 180)
                .fluidTemp(1099)
                .buildAndRegister();

        Fermium = new Material.Builder("fermium")
                .color(0x984ACF).iconSet(METALLIC)
                .element(GTElements.Fm)
                .buildAndRegister();

        Flerovium = new Material.Builder("flerovium")
                .iconSet(SHINY)
                .element(GTElements.Fl)
                .buildAndRegister();

        Fluorine = new Material.Builder("fluorine")
                .fluid(FluidTypes.GAS).fluidCustomTexture()
                .element(GTElements.F)
                .buildAndRegister();

        Francium = new Material.Builder("francium")
                .color(0xAAAAAA).iconSet(SHINY)
                .element(GTElements.Fr)
                .buildAndRegister();

        Gadolinium = new Material.Builder("gadolinium")
                .color(0xDDDDFF).iconSet(METALLIC)
                .element(GTElements.Gd)
                .buildAndRegister();

        Gallium = new Material.Builder("gallium")
                .ingot().fluid()
                .color(0xDCDCFF).iconSet(SHINY)
                .appendFlags(STD_METAL, GENERATE_FOIL)
                .element(GTElements.Ga)
                .fluidTemp(303)
                .buildAndRegister();

        Germanium = new Material.Builder("germanium")
                .color(0x434343).iconSet(SHINY)
                .element(GTElements.Ge)
                .buildAndRegister();

        Gold = new Material.Builder("gold")
                .ingot().fluid().ore()
                .color(0xFFE650).iconSet(SHINY)
                .appendFlags(EXT2_METAL, GENERATE_RING, MORTAR_GRINDABLE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE, GENERATE_FOIL)
                .element(GTElements.Au)
                .cableProperties(GTValues.V[3], 3, 2)
                .fluidPipeProperties(1671, 25, true, true, false, false)
                .fluidTemp(1337)
                .buildAndRegister();

        Hafnium = new Material.Builder("hafnium")
                .color(0x99999A).iconSet(SHINY)
                .element(GTElements.Hf)
                .buildAndRegister();

        Hassium = new Material.Builder("hassium")
                .color(0xDDDDDD)
                .element(GTElements.Hs)
                .buildAndRegister();

        Holmium = new Material.Builder("holmium")
                .iconSet(METALLIC)
                .element(GTElements.Ho)
                .buildAndRegister();

        Hydrogen = new Material.Builder("hydrogen")
                .fluid(FluidTypes.GAS)
                .color(0x0000B5)
                .element(GTElements.H)
                .buildAndRegister();

        Helium = new Material.Builder("helium")
                .fluid(FluidTypes.GAS).fluidCustomTexture()
                .plasma().fluidCustomTexture()
                .element(GTElements.He)
                .buildAndRegister();

        Helium3 = new Material.Builder("helium_3")
                .fluid(FluidTypes.GAS).fluidCustomTexture()
                .element(GTElements.He3)
                .buildAndRegister();

        Indium = new Material.Builder("indium")
                .ingot().fluid()
                .color(0x400080).iconSet(SHINY)
                .element(GTElements.In)
                .fluidTemp(430)
                .buildAndRegister();

        Iodine = new Material.Builder("iodine")
                .color(0x2C344F).iconSet(SHINY)
                .element(GTElements.I)
                .buildAndRegister();

        Iridium = new Material.Builder("iridium")
                .ingot(3).fluid()
                .color(0xA1E4E4).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, GENERATE_FINE_WIRE, GENERATE_GEAR)
                .element(GTElements.Ir)
                .rotorStats(7.0f, 3.0f, 2560)
                .fluidPipeProperties(3398, 250, true, false, true, false)
                .blastTemp(4500, GasTier.HIGH, GTValues.VA[GTValues.IV], 1100)
                .fluidTemp(2719)
                .buildAndRegister();

        Iron = new Material.Builder("iron")
                .ingot().fluid().plasma().ore()
                .color(0xC8C8C8).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_ROTOR, GENERATE_SMALL_GEAR, GENERATE_GEAR, GENERATE_SPRING_SMALL, GENERATE_SPRING, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, BLAST_FURNACE_CALCITE_TRIPLE)
                .element(GTElements.Fe)
                .toolStats(ToolProperty.Builder.of(2.0F, 2.0F, 256, 2)
                        .enchantability(14).addTypes(GTToolType.MORTAR).build())
                .rotorStats(7.0f, 2.5f, 256)
                .cableProperties(GTValues.V[2], 2, 3)
                .fluidTemp(1811)
                .buildAndRegister();

        Krypton = new Material.Builder("krypton")
                .fluid(FluidTypes.GAS)
                .color(0x80FF80).iconSet(GAS)
                .element(GTElements.Kr)
                .buildAndRegister();

        Lanthanum = new Material.Builder("lanthanum")
                .dust().fluid()
                .color(0x5D7575).iconSet(METALLIC)
                .element(GTElements.La)
                .fluidTemp(1193)
                .buildAndRegister();

        Lawrencium = new Material.Builder("lawrencium")
                .iconSet(METALLIC)
                .element(GTElements.Lr)
                .buildAndRegister();

        Lead = new Material.Builder("lead")
                .ingot(1).fluid().ore()
                .color(0x8C648C)
                .appendFlags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_ROTOR, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .element(GTElements.Pb)
                .cableProperties(GTValues.V[0], 2, 2)
                .fluidPipeProperties(1200, 8, true)
                .fluidTemp(600)
                .buildAndRegister();

        Lithium = new Material.Builder("lithium")
                .dust().fluid().ore()
                .color(0xBDC7DB)
                .element(GTElements.Li)
                .fluidTemp(454)
                .buildAndRegister();

        Livermorium = new Material.Builder("livermorium")
                .color(0xAAAAAA).iconSet(SHINY)
                .element(GTElements.Lv)
                .buildAndRegister();

        Lutetium = new Material.Builder("lutetium")
                .dust().fluid()
                .color(0x00AAFF).iconSet(METALLIC)
                .element(GTElements.Lu)
                .fluidTemp(1925)
                .buildAndRegister();

        Magnesium = new Material.Builder("magnesium")
                .dust().fluid()
                .color(0xFFC8C8).iconSet(METALLIC)
                .element(GTElements.Mg)
                .fluidTemp(923)
                .buildAndRegister();

        Mendelevium = new Material.Builder("mendelevium")
                .color(0x1D4ACF).iconSet(METALLIC)
                .element(GTElements.Md)
                .buildAndRegister();

        Manganese = new Material.Builder("manganese")
                .ingot().fluid()
                .color(0xCDE1B9)
                .appendFlags(STD_METAL, GENERATE_FOIL, GENERATE_BOLT_SCREW)
                .element(GTElements.Mn)
                .rotorStats(7.0f, 2.0f, 512)
                .fluidTemp(1519)
                .buildAndRegister();

        Meitnerium = new Material.Builder("meitnerium")
                .color(0x2246BE).iconSet(SHINY)
                .element(GTElements.Mt)
                .buildAndRegister();

        Mercury = new Material.Builder("mercury")
                .fluid()
                .color(0xE6DCDC).iconSet(DULL)
                .element(GTElements.Hg)
                .buildAndRegister();

        Molybdenum = new Material.Builder("molybdenum")
                .ingot().fluid().ore()
                .color(0xB4B4DC).iconSet(SHINY)
                .element(GTElements.Mo)
                .flags(GENERATE_FOIL, GENERATE_BOLT_SCREW)
                .rotorStats(7.0f, 2.0f, 512)
                .fluidTemp(2896)
                .buildAndRegister();

        Moscovium = new Material.Builder("moscovium")
                .color(0x7854AD).iconSet(SHINY)
                .element(GTElements.Mc)
                .buildAndRegister();

        Neodymium = new Material.Builder("neodymium")
                .ingot().fluid().ore()
                .color(0x646464).iconSet(METALLIC)
                .appendFlags(STD_METAL, GENERATE_ROD, GENERATE_BOLT_SCREW)
                .element(GTElements.Nd)
                .rotorStats(7.0f, 2.0f, 512)
                .blastTemp(1297, GasTier.MID)
                .buildAndRegister();

        Neon = new Material.Builder("neon")
                .fluid(FluidTypes.GAS)
                .color(0xFAB4B4).iconSet(GAS)
                .element(GTElements.Ne)
                .buildAndRegister();

        Neptunium = new Material.Builder("neptunium")
                .color(0x284D7B).iconSet(METALLIC)
                .element(GTElements.Np)
                .buildAndRegister();

        Nickel = new Material.Builder("nickel")
                .ingot().fluid().plasma().ore()
                .color(0xC8C8FA).iconSet(METALLIC)
                .appendFlags(STD_METAL, MORTAR_GRINDABLE)
                .element(GTElements.Ni)
                .cableProperties(GTValues.V[GTValues.LV], 3, 3)
                .itemPipeProperties(2048, 1.0f)
                .fluidTemp(1728)
                .buildAndRegister();

        Nihonium = new Material.Builder("nihonium")
                .color(0x08269E).iconSet(SHINY)
                .element(GTElements.Nh)
                .buildAndRegister();

        Niobium = new Material.Builder("niobium")
                .ingot().fluid()
                .color(0xBEB4C8).iconSet(METALLIC)
                .element(GTElements.Nb)
                .blastTemp(2750, GasTier.MID, GTValues.VA[GTValues.HV], 900)
                .buildAndRegister();

        Nitrogen = new Material.Builder("nitrogen")
                .fluid(FluidTypes.GAS).plasma()
                .color(0x00BFC1).iconSet(GAS)
                .element(GTElements.N)
                .buildAndRegister();

        Nobelium = new Material.Builder("nobelium")
                .iconSet(SHINY)
                .element(GTElements.No)
                .buildAndRegister();

        Oganesson = new Material.Builder("oganesson")
                .color(0x142D64).iconSet(METALLIC)
                .element(GTElements.Og)
                .buildAndRegister();

        Osmium = new Material.Builder("osmium")
                .ingot(4).fluid()
                .color(0x3232FF).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, GENERATE_FOIL)
                .element(GTElements.Os)
                .rotorStats(16.0f, 4.0f, 1280)
                .cableProperties(GTValues.V[6], 4, 2)
                .itemPipeProperties(256, 8.0f)
                .blastTemp(4500, GasTier.HIGH, GTValues.VA[GTValues.LuV], 1000)
                .fluidTemp(3306)
                .buildAndRegister();

        Oxygen = new Material.Builder("oxygen")
                .fluid(FluidTypes.GAS).plasma()
                .color(0x4CC3FF)
                .element(GTElements.O)
                .buildAndRegister();

        Palladium = new Material.Builder("palladium")
                .ingot().fluid().ore()
                .color(0x808080).iconSet(SHINY)
                .appendFlags(EXT_METAL, GENERATE_FOIL, GENERATE_FINE_WIRE)
                .element(GTElements.Pd)
                .blastTemp(1828, GasTier.LOW, GTValues.VA[GTValues.HV], 900)
                .buildAndRegister();

        Phosphorus = new Material.Builder("phosphorus")
                .dust()
                .color(0xFFFF00)
                .element(GTElements.P)
                .buildAndRegister();

        Polonium = new Material.Builder("polonium")
                .color(0xC9D47E)
                .element(GTElements.Po)
                .buildAndRegister();

        Platinum = new Material.Builder("platinum")
                .ingot().fluid().ore()
                .color(0xFFFFC8).iconSet(SHINY)
                .appendFlags(EXT2_METAL, GENERATE_FOIL, GENERATE_FINE_WIRE, GENERATE_RING)
                .element(GTElements.Pt)
                .cableProperties(GTValues.V[5], 2, 1)
                .itemPipeProperties(512, 4.0f)
                .fluidTemp(2041)
                .buildAndRegister();

        Plutonium239 = new Material.Builder("plutonium")
                .ingot(3).fluid().ore(true)
                .color(0xF03232).iconSet(METALLIC)
                .element(GTElements.Pu239)
                .fluidTemp(913)
                .buildAndRegister();

        Plutonium241 = new Material.Builder("plutonium_241")
                .ingot(3).fluid()
                .color(0xFA4646).iconSet(SHINY)
                .appendFlags(EXT_METAL)
                .element(GTElements.Pu241)
                .fluidTemp(913)
                .buildAndRegister();

        Potassium = new Material.Builder("potassium")
                .dust(1).fluid()
                .color(0xBEDCFF).iconSet(METALLIC)
                .element(GTElements.K)
                .fluidTemp(337)
                .buildAndRegister();

        Praseodymium = new Material.Builder("praseodymium")
                .color(0xCECECE).iconSet(METALLIC)
                .element(GTElements.Pr)
                .buildAndRegister();

        Promethium = new Material.Builder("promethium")
                .iconSet(METALLIC)
                .element(GTElements.Pm)
                .buildAndRegister();

        Protactinium = new Material.Builder("protactinium")
                .color(0xA78B6D).iconSet(METALLIC)
                .element(GTElements.Pa)
                .buildAndRegister();

        Radon = new Material.Builder("radon")
                .fluid(FluidTypes.GAS)
                .color(0xFF39FF)
                .element(GTElements.Rn)
                .buildAndRegister();

        Radium = new Material.Builder("radium")
                .color(0xFFFFCD).iconSet(SHINY)
                .element(GTElements.Ra)
                .buildAndRegister();

        Rhenium = new Material.Builder("rhenium")
                .color(0xB6BAC3).iconSet(SHINY)
                .element(GTElements.Re)
                .buildAndRegister();

        Rhodium = new Material.Builder("rhodium")
                .ingot().fluid()
                .color(0xDC0C58).iconSet(BRIGHT)
                .appendFlags(EXT2_METAL, GENERATE_GEAR, GENERATE_FINE_WIRE)
                .element(GTElements.Rh)
                .blastTemp(2237, GasTier.MID, GTValues.VA[GTValues.EV], 1200)
                .buildAndRegister();

        Roentgenium = new Material.Builder("roentgenium")
                .color(0xE3FDEC).iconSet(SHINY)
                .element(GTElements.Rg)
                .buildAndRegister();

        Rubidium = new Material.Builder("rubidium")
                .color(0xF01E1E).iconSet(SHINY)
                .element(GTElements.Rb)
                .buildAndRegister();

        Ruthenium = new Material.Builder("ruthenium")
                .ingot().fluid()
                .color(0x50ACCD).iconSet(SHINY)
                .flags(GENERATE_FOIL, GENERATE_GEAR)
                .element(GTElements.Ru)
                .blastTemp(2607, GasTier.MID, GTValues.VA[GTValues.EV], 900)
                .buildAndRegister();

        Rutherfordium = new Material.Builder("rutherfordium")
                .color(0xFFF6A1).iconSet(SHINY)
                .element(GTElements.Rf)
                .buildAndRegister();

        Samarium = new Material.Builder("samarium")
                .ingot().fluid()
                .color(0xFFFFCC).iconSet(METALLIC)
                .flags(GENERATE_LONG_ROD)
                .element(GTElements.Sm)
                .blastTemp(5400, GasTier.HIGH, GTValues.VA[GTValues.EV], 1500)
                .fluidTemp(1345)
                .buildAndRegister();

        Scandium = new Material.Builder("scandium")
                .iconSet(METALLIC)
                .element(GTElements.Sc)
                .buildAndRegister();

        Seaborgium = new Material.Builder("seaborgium")
                .color(0x19C5FF).iconSet(SHINY)
                .element(GTElements.Sg)
                .buildAndRegister();

        Selenium = new Material.Builder("selenium")
                .color(0xB6BA6B).iconSet(SHINY)
                .element(GTElements.Se)
                .buildAndRegister();

        Silicon = new Material.Builder("silicon")
                .ingot().fluid()
                .color(0x3C3C50).iconSet(METALLIC)
                .flags(GENERATE_FOIL)
                .element(GTElements.Si)
                .blastTemp(2273) // no gas tier for silicon
                .buildAndRegister();

        Silver = new Material.Builder("silver")
                .ingot().fluid().ore()
                .color(0xDCDCFF).iconSet(SHINY)
                .appendFlags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_FINE_WIRE, GENERATE_RING)
                .element(GTElements.Ag)
                .cableProperties(GTValues.V[3], 1, 1)
                .fluidTemp(1235)
                .buildAndRegister();

        Sodium = new Material.Builder("sodium")
                .dust()
                .color(0x000096).iconSet(METALLIC)
                .element(GTElements.Na)
                .buildAndRegister();

        Strontium = new Material.Builder("strontium")
                .color(0xC8C8C8).iconSet(METALLIC)
                .element(GTElements.Sr)
                .buildAndRegister();

        Sulfur = new Material.Builder("sulfur")
                .dust().ore()
                .color(0xC8C800)
                .flags(FLAMMABLE)
                .element(GTElements.S)
                .buildAndRegister();

        Tantalum = new Material.Builder("tantalum")
                .ingot().fluid()
                .color(0x78788c).iconSet(METALLIC)
                .appendFlags(STD_METAL, GENERATE_FOIL, GENERATE_FINE_WIRE)
                .element(GTElements.Ta)
                .fluidTemp(3290)
                .buildAndRegister();

        Technetium = new Material.Builder("technetium")
                .color(0x545455).iconSet(SHINY)
                .element(GTElements.Tc)
                .buildAndRegister();

        Tellurium = new Material.Builder("tellurium")
                .iconSet(METALLIC)
                .element(GTElements.Te)
                .buildAndRegister();

        Tennessine = new Material.Builder("tennessine")
                .color(0x977FD6).iconSet(SHINY)
                .element(GTElements.Ts)
                .buildAndRegister();

        Terbium = new Material.Builder("terbium")
                .iconSet(METALLIC)
                .element(GTElements.Tb)
                .buildAndRegister();

        Thorium = new Material.Builder("thorium")
                .ingot().fluid().ore()
                .color(0x001E00).iconSet(SHINY)
                .appendFlags(STD_METAL, GENERATE_ROD)
                .element(GTElements.Th)
                .fluidTemp(2023)
                .buildAndRegister();

        Thallium = new Material.Builder("thallium")
                .color(0xC1C1DE).iconSet(SHINY)
                .element(GTElements.Tl)
                .buildAndRegister();

        Thulium = new Material.Builder("thulium")
                .iconSet(METALLIC)
                .element(GTElements.Tm)
                .buildAndRegister();

        Tin = new Material.Builder("tin")
                .ingot(1).fluid(FluidTypes.LIQUID, true).ore()
                .color(0xDCDCDC)
                .appendFlags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_ROTOR, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .element(GTElements.Sn)
                .cableProperties(GTValues.V[1], 1, 1)
                .itemPipeProperties(4096, 0.5f)
                .fluidTemp(505)
                .buildAndRegister();

        Titanium = new Material.Builder("titanium") // todo Ore? Look at EBF recipe here if we do Ti ores
                .ingot(3).fluid()
                .color(0xDCA0F0).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, GENERATE_ROTOR, GENERATE_SMALL_GEAR, GENERATE_GEAR, GENERATE_FRAME)
                .element(GTElements.Ti)
                .toolStats(ToolProperty.Builder.of(8.0F, 6.0F, 1536, 3)
                        .enchantability(14).build())
                .rotorStats(7.0f, 3.0f, 1600)
                .fluidPipeProperties(2426, 150, true)
                .blastTemp(1941, GasTier.MID, GTValues.VA[GTValues.HV], 1500)
                .buildAndRegister();

        Tritium = new Material.Builder("tritium")
                .fluid(FluidTypes.GAS)
                .fluidCustomTexture()
                .iconSet(METALLIC)
                .element(GTElements.T)
                .buildAndRegister();

        Tungsten = new Material.Builder("tungsten")
                .ingot(3).fluid()
                .color(0x323232).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FOIL, GENERATE_GEAR, GENERATE_FRAME)
                .element(GTElements.W)
                .rotorStats(7.0f, 3.0f, 2560)
                .cableProperties(GTValues.V[5], 2, 2)
                .fluidPipeProperties(4618, 50, true, true, false, true)
                .blastTemp(3600, GasTier.MID, GTValues.VA[GTValues.EV], 1800)
                .fluidTemp(3695)
                .buildAndRegister();

        Uranium238 = new Material.Builder("uranium")
                .ingot(3).fluid()
                .color(0x32F032).iconSet(METALLIC)
                .appendFlags(EXT_METAL)
                .element(GTElements.U238)
                .fluidTemp(1405)
                .buildAndRegister();

        Uranium235 = new Material.Builder("uranium_235")
                .ingot(3).fluid()
                .color(0x46FA46).iconSet(SHINY)
                .appendFlags(EXT_METAL)
                .element(GTElements.U235)
                .fluidTemp(1405)
                .buildAndRegister();

        Vanadium = new Material.Builder("vanadium")
                .ingot().fluid()
                .color(0x323232).iconSet(METALLIC)
                .element(GTElements.V)
                .blastTemp(2183, GasTier.MID)
                .buildAndRegister();

        Xenon = new Material.Builder("xenon")
                .fluid(FluidTypes.GAS)
                .color(0x00FFFF).iconSet(GAS)
                .element(GTElements.Xe)
                .buildAndRegister();

        Ytterbium = new Material.Builder("ytterbium")
                .color(0xA7A7A7).iconSet(METALLIC)
                .element(GTElements.Yb)
                .buildAndRegister();

        Yttrium = new Material.Builder("yttrium")
                .ingot().fluid()
                .color(0x76524C).iconSet(METALLIC)
                .element(GTElements.Y)
                .blastTemp(1799)
                .buildAndRegister();

        Zinc = new Material.Builder("zinc")
                .ingot(1).fluid()
                .color(0xEBEBFA).iconSet(METALLIC)
                .appendFlags(STD_METAL, MORTAR_GRINDABLE, GENERATE_FOIL, GENERATE_RING, GENERATE_FINE_WIRE)
                .element(GTElements.Zn)
                .fluidTemp(693)
                .buildAndRegister();

        Zirconium = new Material.Builder("zirconium")
                .color(0xC8FFFF).iconSet(METALLIC)
                .element(GTElements.Zr)
                .buildAndRegister();

        Naquadah = new Material.Builder("naquadah")
                .ingot(4).fluid().fluidCustomTexture().ore()
                .color(0x323232, false).iconSet(METALLIC)
                .appendFlags(EXT_METAL, GENERATE_FOIL, GENERATE_SPRING, GENERATE_FINE_WIRE, GENERATE_BOLT_SCREW)
                .element(GTElements.Nq)
                .rotorStats(6.0f, 4.0f, 1280)
                .cableProperties(GTValues.V[7], 2, 2)
                .fluidPipeProperties(3776, 200, true, false, true, true)
                .blastTemp(5000, GasTier.HIGH, GTValues.VA[GTValues.IV], 600)
                .buildAndRegister();

        NaquadahEnriched = new Material.Builder("enriched_naquadah")
                .ingot(4).fluid().fluidCustomTexture()
                .color(0x3C3C3C, false).iconSet(METALLIC)
                .appendFlags(EXT_METAL, GENERATE_FOIL)
                .element(GTElements.Nq1)
                .blastTemp(7000, GasTier.HIGH, GTValues.VA[GTValues.IV], 1000)
                .buildAndRegister();

        Naquadria = new Material.Builder("naquadria")
                .ingot(3).fluid().fluidCustomTexture()
                .color(0x1E1E1E, false).iconSet(SHINY)
                .appendFlags(EXT_METAL, GENERATE_FOIL, GENERATE_GEAR, GENERATE_FINE_WIRE, GENERATE_BOLT_SCREW)
                .element(GTElements.Nq2)
                .blastTemp(9000, GasTier.HIGH, GTValues.VA[GTValues.ZPM], 1200)
                .buildAndRegister();

        Neutronium = new Material.Builder("neutronium")
                .ingot(6).fluid()
                .color(0xFAFAFA)
                .appendFlags(EXT_METAL, GENERATE_BOLT_SCREW, GENERATE_FRAME)
                .element(GTElements.Nt)
                .toolStats(ToolProperty.Builder.of(180.0F, 100.0F, 65535, 6)
                        .attackSpeed(0.5F).enchantability(33).magnetic().unbreakable().build())
                .rotorStats(24.0f, 12.0f, 655360)
                .fluidPipeProperties(100_000, 5000, true, true, true, true)
                .fluidTemp(100_000)
                .buildAndRegister();

        Tritanium = new Material.Builder("tritanium")
                .ingot(6).fluid()
                .color(0x600000).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, GENERATE_FRAME, GENERATE_RING, GENERATE_SMALL_GEAR, GENERATE_ROUND, GENERATE_FOIL, GENERATE_FINE_WIRE, GENERATE_GEAR)
                .element(GTElements.Tr)
                .cableProperties(GTValues.V[8], 1, 8)
                .rotorStats(20.0f, 6.0f, 10240)
                .fluidTemp(25000)
                .buildAndRegister();

        Duranium = new Material.Builder("duranium")
                .ingot(5).fluid()
                .color(0x4BAFAF).iconSet(BRIGHT)
                .appendFlags(EXT_METAL, GENERATE_FOIL, GENERATE_GEAR)
                .element(GTElements.Dr)
                .toolStats(ToolProperty.Builder.of(14.0F, 12.0F, 8192, 5)
                        .attackSpeed(0.3F).enchantability(33).magnetic().build())
                .fluidPipeProperties(9625, 500, true, true, true, true)
                .fluidTemp(7500)
                .buildAndRegister();

        Trinium = new Material.Builder("trinium")
                .ingot(7).fluid()
                .color(0x9973BD).iconSet(SHINY)
                .flags(GENERATE_FOIL, GENERATE_BOLT_SCREW, GENERATE_GEAR)
                .element(GTElements.Ke)
                .cableProperties(GTValues.V[7], 6, 4)
                .blastTemp(7200, GasTier.HIGH, GTValues.VA[GTValues.LuV], 1500)
                .buildAndRegister();

    }
}
