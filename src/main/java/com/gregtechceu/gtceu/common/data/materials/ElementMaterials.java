package com.gregtechceu.gtceu.common.data.materials;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty.GasTier;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.common.data.GTElements;

import static com.gregtechceu.gtceu.api.GTValues.LV;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;


public class ElementMaterials {

    public static void register() {
        Actinium = new Material.Builder(GTCEu.id("actinium"))
                .color(0xC3D1FF).secondaryColor(0x353d41).iconSet(METALLIC)
                .element(GTElements.Ac)
                .buildAndRegister();

        Aluminium = new Material.Builder(GTCEu.id("aluminium"))
                .ingot()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(933))
                .ore()
                .color(0xb6e5ff).secondaryColor(0x7ca29b)
                .appendFlags(EXT2_METAL, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .element(GTElements.Al)
                .toolStats(ToolProperty.Builder.of(6.0F, 7.5F, 768, 2)
                        .enchantability(14).build())
                .rotorStats(10.0f, 2.0f, 128)
                .cableProperties(GTValues.V[4], 1, 1)
                .fluidPipeProperties(1166, 100, true)
                .blastTemp(1700, GasTier.LOW)
                .buildAndRegister();

        Americium = new Material.Builder(GTCEu.id("americium"))
                .ingot(3)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(1449))
                .color(0x287869).iconSet(RADIOACTIVE)
                .appendFlags(EXT_METAL, GENERATE_FOIL, GENERATE_FINE_WIRE)
                .element(GTElements.Am)
                .itemPipeProperties(64, 64)
                .buildAndRegister();

        Antimony = new Material.Builder(GTCEu.id("antimony"))
                .ingot()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(904))
                .color(0xeaeaff).secondaryColor(0xa2a2bc).iconSet(SHINY)
                .flags(MORTAR_GRINDABLE)
                .element(GTElements.Sb)
                .buildAndRegister();

        Argon = new Material.Builder(GTCEu.id("argon"))
                .gas().plasma()
                .color(0x00FF00)
                .element(GTElements.Ar)
                .buildAndRegister();

        Arsenic = new Material.Builder(GTCEu.id("arsenic"))
                .dust()
                .fluid(FluidStorageKeys.GAS, new FluidBuilder()
                        .state(FluidState.GAS)
                        .temperature(887))
                .color(0x9c9c8d).secondaryColor(0x676756)
                .element(GTElements.As)
                .buildAndRegister();

        Astatine = new Material.Builder(GTCEu.id("astatine"))
                .color(0xffd52e).secondaryColor(0x17212b)
                .element(GTElements.At)
                .buildAndRegister();

        Barium = new Material.Builder(GTCEu.id("barium"))
                .dust()
                .color(0xede192).secondaryColor(0x5d9b8d).iconSet(METALLIC)
                .element(GTElements.Ba)
                .buildAndRegister();

        Berkelium = new Material.Builder(GTCEu.id("berkelium"))
                .color(0x645A88).iconSet(RADIOACTIVE)
                .element(GTElements.Bk)
                .buildAndRegister();

        Beryllium = new Material.Builder(GTCEu.id("beryllium"))
                .ingot()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(1560))
                .ore()
                .color(0x98d677).secondaryColor(0x254d40).iconSet(METALLIC)
                .appendFlags(STD_METAL)
                .element(GTElements.Be)
                .buildAndRegister();

        Bismuth = new Material.Builder(GTCEu.id("bismuth"))
                .ingot(1)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(545))
                .color(0x89dbdb).secondaryColor(0x845c6a).iconSet(METALLIC)
                .element(GTElements.Bi)
                .buildAndRegister();

        Bohrium = new Material.Builder(GTCEu.id("bohrium"))
                .color(0x4c3e50).secondaryColor(0xDC57FF).iconSet(RADIOACTIVE)
                .element(GTElements.Bh)
                .buildAndRegister();

        Boron = new Material.Builder(GTCEu.id("boron"))
                .dust()
                .color(0xd7f7d7).secondaryColor(0x5f6152)
                .element(GTElements.B)
                .buildAndRegister();

        Bromine = new Material.Builder(GTCEu.id("bromine"))
                .color(0x912200).secondaryColor(0x080101).iconSet(SHINY)
                .element(GTElements.Br)
                .buildAndRegister();

        Caesium = new Material.Builder(GTCEu.id("caesium"))
                .dust()
                .color(0xe9e5d2).secondaryColor(0xaa9864).iconSet(SHINY)
                .element(GTElements.Cs)
                .buildAndRegister();

        Calcium = new Material.Builder(GTCEu.id("calcium"))
                .dust()
                .color(0xFFF5DE).secondaryColor(0xa4a4a4).iconSet(METALLIC)
                .element(GTElements.Ca)
                .buildAndRegister();

        Californium = new Material.Builder(GTCEu.id("californium"))
                .color(0xA85A12).iconSet(RADIOACTIVE)
                .element(GTElements.Cf)
                .buildAndRegister();

        Carbon = new Material.Builder(GTCEu.id("carbon"))
                .dust()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(4600))
                .color(0x333030).secondaryColor(0x221c1c)
                .element(GTElements.C)
                .buildAndRegister();

        Cadmium = new Material.Builder(GTCEu.id("cadmium"))
                .dust()
                .color(0x636377).secondaryColor(0x412738).iconSet(SHINY)
                .element(GTElements.Cd)
                .buildAndRegister();

        Cerium = new Material.Builder(GTCEu.id("cerium"))
                .dust()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(1068))
                .color(0xd3d7c3).secondaryColor(0x87917D).iconSet(METALLIC)
                .element(GTElements.Ce)
                .buildAndRegister();

        Chlorine = new Material.Builder(GTCEu.id("chlorine"))
                .fluid(FluidStorageKeys.GAS, new FluidBuilder().state(FluidState.GAS).customStill())
                .element(GTElements.Cl)
                .buildAndRegister();

        Chromium = new Material.Builder(GTCEu.id("chromium"))
                .ingot(3)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(2180))
                .color(0xf3e0ea).secondaryColor(0x441f2e).iconSet(SHINY)
                .appendFlags(EXT_METAL, GENERATE_ROTOR)
                .element(GTElements.Cr)
                .rotorStats(12.0f, 3.0f, 512)
                .fluidPipeProperties(2180, 35, true, true, false, false)
                .blastTemp(1700, GasTier.LOW)
                .buildAndRegister();

        Cobalt = new Material.Builder(GTCEu.id("cobalt"))
                .ingot()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(1768))
                .ore() // leave for TiCon ore processing
                .color(0xf1e2d1).secondaryColor(0x1d1dd6).iconSet(METALLIC)
                .appendFlags(EXT_METAL, GENERATE_FINE_WIRE)
                .element(GTElements.Co)
                .cableProperties(GTValues.V[LV], 2, 2)
                .itemPipeProperties(2560, 2.0f)
                .buildAndRegister();

        Copernicium = new Material.Builder(GTCEu.id("copernicium"))
                .color(0x565c5d).secondaryColor(0xffd34b).iconSet(RADIOACTIVE)
                .element(GTElements.Cn)
                .buildAndRegister();

        Copper = new Material.Builder(GTCEu.id("copper"))
                .ingot(1)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(1358))
                .ore()
                .color(0xe77c56).secondaryColor(0xe4673e).iconSet(BRIGHT)
                .appendFlags(EXT_METAL, MORTAR_GRINDABLE, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .element(GTElements.Cu)
                .cableProperties(GTValues.V[2], 1, 2)
                .fluidPipeProperties(1696, 6, true)
                .buildAndRegister();

        Curium = new Material.Builder(GTCEu.id("curium"))
                .color(0x7B544E).iconSet(RADIOACTIVE)
                .element(GTElements.Cm)
                .buildAndRegister();

        Darmstadtium = new Material.Builder(GTCEu.id("darmstadtium"))
                .ingot().fluid()
                .color(0x578062).iconSet(RADIOACTIVE)
                .appendFlags(EXT2_METAL, GENERATE_ROTOR, GENERATE_DENSE, GENERATE_SMALL_GEAR)
                .element(GTElements.Ds)
                .buildAndRegister();

        Deuterium = new Material.Builder(GTCEu.id("deuterium"))
                .fluid(FluidStorageKeys.GAS, new FluidBuilder().state(FluidState.GAS).customStill())
                .element(GTElements.D)
                .buildAndRegister();

        Dubnium = new Material.Builder(GTCEu.id("dubnium"))
                .color(0xc7ddde).secondaryColor(0x00f3ff).iconSet(RADIOACTIVE)
                .element(GTElements.Db)
                .buildAndRegister();

        Dysprosium = new Material.Builder(GTCEu.id("dysprosium"))
                .color(0x6a664b).secondaryColor(0x423307)
                .iconSet(METALLIC)
                .element(GTElements.Dy)
                .buildAndRegister();

        Einsteinium = new Material.Builder(GTCEu.id("einsteinium"))
                .color(0xCE9F00).iconSet(RADIOACTIVE)
                .element(GTElements.Es)
                .buildAndRegister();

        Erbium = new Material.Builder(GTCEu.id("erbium"))
                .color(0xeccbdb).secondaryColor(0x5d625a)
                .iconSet(METALLIC)
                .element(GTElements.Er)
                .buildAndRegister();

        Europium = new Material.Builder(GTCEu.id("europium"))
                .ingot()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(1099))
                .color(0x988b33).secondaryColor(0x032a52).iconSet(METALLIC)
                .appendFlags(STD_METAL, GENERATE_LONG_ROD, GENERATE_FINE_WIRE, GENERATE_SPRING, GENERATE_FOIL, GENERATE_FRAME)
                .element(GTElements.Eu)
                .cableProperties(GTValues.V[GTValues.UHV], 2, 32)
                .fluidPipeProperties(7750, 300, true)
                .blastTemp(6000, GasTier.MID, GTValues.VA[GTValues.IV], 180)
                .buildAndRegister();

        Fermium = new Material.Builder(GTCEu.id("fermium"))
                .color(0xc99fe7).secondaryColor(0x3e0022).iconSet(METALLIC)
                .element(GTElements.Fm)
                .buildAndRegister();

        Flerovium = new Material.Builder(GTCEu.id("flerovium"))
                .color(0x393d43).secondaryColor(0xd2ff00)
                .iconSet(RADIOACTIVE)
                .element(GTElements.Fl)
                .buildAndRegister();

        Fluorine = new Material.Builder(GTCEu.id("fluorine"))
                .fluid(FluidStorageKeys.GAS, new FluidBuilder().state(FluidState.GAS).customStill())
                .element(GTElements.F)
                .buildAndRegister();

        Francium = new Material.Builder(GTCEu.id("francium"))
                .color(0xAAAAAA).secondaryColor(0x0000ff).iconSet(RADIOACTIVE)
                .element(GTElements.Fr)
                .buildAndRegister();

        Gadolinium = new Material.Builder(GTCEu.id("gadolinium"))
                .color(0x828a7a).secondaryColor(0x363420).iconSet(METALLIC)
                .element(GTElements.Gd)
                .buildAndRegister();

        Gallium = new Material.Builder(GTCEu.id("gallium"))
                .ingot()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(303))
                .color(0xdadbf3).secondaryColor(0x787955).iconSet(SHINY)
                .appendFlags(STD_METAL, GENERATE_FOIL)
                .element(GTElements.Ga)
                .buildAndRegister();

        Germanium = new Material.Builder(GTCEu.id("germanium"))
                .color(0xe1e1e1).secondaryColor(0x6a6248).iconSet(SHINY)
                .element(GTElements.Ge)
                .buildAndRegister();

        Gold = new Material.Builder(GTCEu.id("gold"))
                .ingot()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(1337))
                .ore()
                .color(0xfdf55f).secondaryColor(0xf25833).iconSet(SHINY)
                .appendFlags(EXT2_METAL, GENERATE_RING, MORTAR_GRINDABLE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE, GENERATE_FOIL)
                .element(GTElements.Au)
                .cableProperties(GTValues.V[3], 3, 2)
                .fluidPipeProperties(1671, 25, true, true, false, false)
                .buildAndRegister();

        Hafnium = new Material.Builder(GTCEu.id("hafnium"))
                .color(0x99999A).secondaryColor(0x2b4a3a).iconSet(SHINY)
                .element(GTElements.Hf)
                .buildAndRegister();

        Hassium = new Material.Builder(GTCEu.id("hassium"))
                .color(0x78766f).secondaryColor(0x09ebaf)
                .iconSet(RADIOACTIVE)
                .element(GTElements.Hs)
                .buildAndRegister();

        Holmium = new Material.Builder(GTCEu.id("holmium"))
                .color(0x5c706d).secondaryColor(0x1d2b2d)
                .iconSet(METALLIC)
                .element(GTElements.Ho)
                .buildAndRegister();

        Hydrogen = new Material.Builder(GTCEu.id("hydrogen"))
                .gas()
                .color(0x0000B5)
                .element(GTElements.H)
                .buildAndRegister();

        Helium = new Material.Builder(GTCEu.id("helium"))
                .fluid(FluidStorageKeys.GAS, new FluidBuilder().state(FluidState.GAS).customStill())
                .plasma()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder()
                        .temperature(4)
                        .color(0xFCFF90)
                        .name("liquid_helium")
                        .translation("gtceu.fluid.liquid_generic"))
                .element(GTElements.He)
                .buildAndRegister();
        Helium.getProperty(PropertyKey.FLUID).setPrimaryKey(FluidStorageKeys.GAS);

        Helium3 = new Material.Builder(GTCEu.id("helium_3"))
                .fluid(FluidStorageKeys.GAS, new FluidBuilder()
                        .customStill()
                        .translation("gtceu.fluid.generic"))
                .element(GTElements.He3)
                .buildAndRegister();

        Indium = new Material.Builder(GTCEu.id("indium"))
                .ingot()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(430))
                .color(0xd0c8d9).secondaryColor(0x400080).iconSet(SHINY)
                .element(GTElements.In)
                .buildAndRegister();

        Iodine = new Material.Builder(GTCEu.id("iodine"))
                .color(0x67686d).secondaryColor(0x773000).iconSet(SHINY)
                .element(GTElements.I)
                .buildAndRegister();

        Iridium = new Material.Builder(GTCEu.id("iridium"))
                .ingot(3)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(2719))
                .color(0xfdfce9).secondaryColor(0x3d011b).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, GENERATE_FINE_WIRE, GENERATE_GEAR, GENERATE_FRAME)
                .element(GTElements.Ir)
                .rotorStats(7.0f, 3.0f, 2560)
                .fluidPipeProperties(3398, 250, true, false, true, false)
                .blastTemp(4500, GasTier.HIGH, GTValues.VA[GTValues.IV], 1100)
                .buildAndRegister();

        Iron = new Material.Builder(GTCEu.id("iron"))
                .ingot()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(1811))
                .plasma()
                .ore()
                .color(0xeeeeee).secondaryColor(0x979797).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_ROTOR, GENERATE_SMALL_GEAR, GENERATE_GEAR, GENERATE_SPRING_SMALL, GENERATE_SPRING, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, BLAST_FURNACE_CALCITE_TRIPLE)
                .element(GTElements.Fe)
                .toolStats(ToolProperty.Builder.of(2.0F, 2.0F, 256, 2)
                        .enchantability(14).addTypes(GTToolType.MORTAR).build())
                .rotorStats(7.0f, 2.5f, 256)
                .cableProperties(GTValues.V[2], 2, 3)
                .buildAndRegister();

        Krypton = new Material.Builder(GTCEu.id("krypton"))
                .fluid(FluidStorageKeys.GAS, new FluidBuilder()
                        .customStill()
                        .translation("gtceu.fluid.generic"))
                .color(0x80FF80)
                .element(GTElements.Kr)
                .buildAndRegister();

        Lanthanum = new Material.Builder(GTCEu.id("lanthanum"))
                .dust()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(1193))
                .color(0xe8e0c2).secondaryColor(0x5D7575).iconSet(METALLIC)
                .element(GTElements.La)
                .buildAndRegister();

        Lawrencium = new Material.Builder(GTCEu.id("lawrencium"))
                .color(0x5D7575)
                .iconSet(RADIOACTIVE)
                .element(GTElements.Lr)
                .buildAndRegister();

        Lead = new Material.Builder(GTCEu.id("lead"))
                .ingot(1)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(600))
                .ore()
                .color(0x7e6f82).secondaryColor(0x290633)
                .appendFlags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_ROTOR, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .element(GTElements.Pb)
                .cableProperties(GTValues.V[0], 2, 2)
                .fluidPipeProperties(1200, 32, true)
                .buildAndRegister();

        Lithium = new Material.Builder(GTCEu.id("lithium"))
                .dust()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(454))
                .ore()
                .color(0xd7e7ee).secondaryColor(0xBDC7DB)
                .element(GTElements.Li)
                .buildAndRegister();

        Livermorium = new Material.Builder(GTCEu.id("livermorium"))
                .color(0x939393).secondaryColor(0xff8b8b).iconSet(RADIOACTIVE)
                .element(GTElements.Lv)
                .buildAndRegister();

        Lutetium = new Material.Builder(GTCEu.id("lutetium"))
                .dust()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(1925))
                .color(0xe6faea).secondaryColor(0x231809).iconSet(METALLIC)
                .element(GTElements.Lu)
                .buildAndRegister();

        Magnesium = new Material.Builder(GTCEu.id("magnesium"))
                .dust()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(923))
                .color(0xffd6d6).secondaryColor(0x594d19).iconSet(FINE)
                .element(GTElements.Mg)
                .buildAndRegister();

        Mendelevium = new Material.Builder(GTCEu.id("mendelevium"))
                .color(0x1D4ACF).iconSet(RADIOACTIVE)
                .element(GTElements.Md)
                .buildAndRegister();

        Manganese = new Material.Builder(GTCEu.id("manganese"))
                .ingot()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(1519))
                .color(0xEEEEEE).secondaryColor(0xCDE1B9)
                .appendFlags(STD_METAL, GENERATE_FOIL, GENERATE_BOLT_SCREW)
                .element(GTElements.Mn)
                .rotorStats(7.0f, 2.0f, 512)
                .buildAndRegister();

        Meitnerium = new Material.Builder(GTCEu.id("meitnerium"))
                .color(0x454854).secondaryColor(0x6e90ff).iconSet(RADIOACTIVE)
                .element(GTElements.Mt)
                .buildAndRegister();

        Mercury = new Material.Builder(GTCEu.id("mercury"))
                .fluid()
                .color(0xE6DCDC).iconSet(DULL)
                .element(GTElements.Hg)
                .buildAndRegister();

        Molybdenum = new Material.Builder(GTCEu.id("molybdenum"))
                .ingot()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(2896))
                .ore()
                .color(0xc1c1ce).secondaryColor(0x404068).iconSet(SHINY)
                .element(GTElements.Mo)
                .flags(GENERATE_FOIL, GENERATE_BOLT_SCREW)
                .rotorStats(7.0f, 2.0f, 512)
                .buildAndRegister();

        Moscovium = new Material.Builder(GTCEu.id("moscovium"))
                .color(0x2a1b40).secondaryColor(0xbd91ff).iconSet(RADIOACTIVE)
                .element(GTElements.Mc)
                .buildAndRegister();

        Neodymium = new Material.Builder(GTCEu.id("neodymium"))
                .ingot().fluid().ore()
                .color(0x9a8b94).secondaryColor(0x2c2c2c).iconSet(METALLIC)
                .appendFlags(STD_METAL, GENERATE_ROD, GENERATE_BOLT_SCREW)
                .element(GTElements.Nd)
                .rotorStats(7.0f, 2.0f, 512)
                .blastTemp(1297, GasTier.MID)
                .buildAndRegister();

        Neon = new Material.Builder(GTCEu.id("neon"))
                .gas()
                .color(0xFAB4B4)
                .element(GTElements.Ne)
                .buildAndRegister();

        Neptunium = new Material.Builder(GTCEu.id("neptunium"))
                .color(0x284D7B).iconSet(RADIOACTIVE)
                .element(GTElements.Np)
                .buildAndRegister();

        Nickel = new Material.Builder(GTCEu.id("nickel"))
                .ingot()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(1728))
                .plasma()
                .ore()
                .color(0xccdff5).secondaryColor(0x59563a).iconSet(METALLIC)
                .appendFlags(STD_METAL, MORTAR_GRINDABLE)
                .element(GTElements.Ni)
                .cableProperties(GTValues.V[LV], 3, 3)
                .itemPipeProperties(2048, 1.0f)
                .buildAndRegister();

        Nihonium = new Material.Builder(GTCEu.id("nihonium"))
                .color(0x323957).secondaryColor(0xa68bff).iconSet(RADIOACTIVE)
                .element(GTElements.Nh)
                .buildAndRegister();

        Niobium = new Material.Builder(GTCEu.id("niobium"))
                .ingot().fluid()
                .color(0xcbd6ea).secondaryColor(0x3f5b2a).iconSet(BRIGHT)
                .element(GTElements.Nb)
                .blastTemp(2750, GasTier.MID, GTValues.VA[GTValues.HV], 900)
                .buildAndRegister();

        Nitrogen = new Material.Builder(GTCEu.id("nitrogen"))
                .gas().plasma()
                .color(0x00BFC1)
                .element(GTElements.N)
                .buildAndRegister();

        Nobelium = new Material.Builder(GTCEu.id("nobelium"))
                .color(0x3e4758).secondaryColor(0x43deff)
                .iconSet(RADIOACTIVE)
                .element(GTElements.No)
                .buildAndRegister();

        Oganesson = new Material.Builder(GTCEu.id("oganesson"))
                .color(0x443936).secondaryColor(0xff1dbd).iconSet(RADIOACTIVE)
                .element(GTElements.Og)
                .buildAndRegister();

        Osmium = new Material.Builder(GTCEu.id("osmium"))
                .ingot(4)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(3306))
                .color(0xf9f9f9).secondaryColor(0x307fc2).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, GENERATE_FOIL)
                .element(GTElements.Os)
                .rotorStats(16.0f, 4.0f, 1280)
                .cableProperties(GTValues.V[6], 4, 2)
                .itemPipeProperties(256, 8.0f)
                .blastTemp(4500, GasTier.HIGH, GTValues.VA[GTValues.LuV], 1000)
                .buildAndRegister();

        Oxygen = new Material.Builder(GTCEu.id("oxygen"))
                .gas()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder()
                        .temperature(85)
                        .color(0x6688DD)
                        .name("liquid_oxygen")
                        .translation("gtceu.fluid.liquid_generic"))
                .plasma()
                .color(0x4CC3FF)
                .element(GTElements.O)
                .buildAndRegister();
        Oxygen.getProperty(PropertyKey.FLUID).setPrimaryKey(FluidStorageKeys.GAS);

        Palladium = new Material.Builder(GTCEu.id("palladium"))
                .ingot().fluid().ore()
                .color(0xA0A0A0).secondaryColor(0x4b4a3a).iconSet(SHINY)
                .appendFlags(EXT_METAL, GENERATE_FOIL, GENERATE_FINE_WIRE)
                .element(GTElements.Pd)
                .blastTemp(1828, GasTier.LOW, GTValues.VA[GTValues.HV], 900)
                .buildAndRegister();

        Phosphorus = new Material.Builder(GTCEu.id("phosphorus"))
                .dust()
                .color(0x77332c).secondaryColor(0x220202)
                .element(GTElements.P)
                .buildAndRegister();

        Polonium = new Material.Builder(GTCEu.id("polonium"))
                .color(0x163b27).secondaryColor(0x00ff78)
                .iconSet(RADIOACTIVE)
                .element(GTElements.Po)
                .buildAndRegister();

        Platinum = new Material.Builder(GTCEu.id("platinum"))
                .ingot()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(2041))
                .ore()
                .color(0xfff9da).secondaryColor(0x4e4e45).iconSet(SHINY)
                .appendFlags(EXT2_METAL, GENERATE_FOIL, GENERATE_FINE_WIRE, GENERATE_RING)
                .element(GTElements.Pt)
                .cableProperties(GTValues.V[5], 2, 1)
                .itemPipeProperties(512, 4.0f)
                .buildAndRegister();

        Plutonium239 = new Material.Builder(GTCEu.id("plutonium"))
                .ingot(3)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(913))
                .ore(true)
                .color(0xba2727).secondaryColor(0x222730).iconSet(RADIOACTIVE)
                .element(GTElements.Pu239)
                .buildAndRegister();

        Plutonium241 = new Material.Builder(GTCEu.id("plutonium_241"))
                .ingot(3)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(913))
                .color(0xfa7272).secondaryColor(0x222730).iconSet(RADIOACTIVE)
                .appendFlags(EXT_METAL)
                .element(GTElements.Pu241)
                .buildAndRegister();

        Potassium = new Material.Builder(GTCEu.id("potassium"))
                .dust(1)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(337))
                .color(0xd2e1f2).secondaryColor(0x6189b8).iconSet(METALLIC)
                .element(GTElements.K)
                .buildAndRegister();

        Praseodymium = new Material.Builder(GTCEu.id("praseodymium"))
                .color(0xCECECE).secondaryColor(0x424d33).iconSet(METALLIC)
                .element(GTElements.Pr)
                .buildAndRegister();

        Promethium = new Material.Builder(GTCEu.id("promethium"))
                .color(0x786160).secondaryColor(0xe7ffb8)
                .iconSet(RADIOACTIVE)
                .element(GTElements.Pm)
                .buildAndRegister();

        Protactinium = new Material.Builder(GTCEu.id("protactinium"))
                .color(0xA78B6D).iconSet(RADIOACTIVE)
                .element(GTElements.Pa)
                .buildAndRegister();

        Radon = new Material.Builder(GTCEu.id("radon"))
                .gas()
                .color(0xFF39FF)
                .element(GTElements.Rn)
                .buildAndRegister();

        Radium = new Material.Builder(GTCEu.id("radium"))
                .color(0x838361).secondaryColor(0x90ff2d).iconSet(RADIOACTIVE)
                .element(GTElements.Ra)
                .buildAndRegister();

        Rhenium = new Material.Builder(GTCEu.id("rhenium"))
                .color(0xcbcfd7).secondaryColor(0x37393d).iconSet(SHINY)
                .element(GTElements.Re)
                .buildAndRegister();

        Rhodium = new Material.Builder(GTCEu.id("rhodium"))
                .ingot().fluid()
                .color(0xf36bba).secondaryColor(0xDC0C58).iconSet(BRIGHT)
                .appendFlags(EXT2_METAL, GENERATE_GEAR, GENERATE_FINE_WIRE)
                .element(GTElements.Rh)
                .blastTemp(2237, GasTier.MID, GTValues.VA[GTValues.EV], 1200)
                .buildAndRegister();

        Roentgenium = new Material.Builder(GTCEu.id("roentgenium"))
                .color(0x3e4840).secondaryColor(0xE3FDEC).iconSet(RADIOACTIVE)
                .element(GTElements.Rg)
                .buildAndRegister();

        Rubidium = new Material.Builder(GTCEu.id("rubidium"))
                .color(0xbdb0b0).secondaryColor(0x451c1c).iconSet(SHINY)
                .element(GTElements.Rb)
                .buildAndRegister();

        Ruthenium = new Material.Builder(GTCEu.id("ruthenium"))
                .ingot().fluid()
                .color(0xc7ced1).secondaryColor(0x3c7285).iconSet(SHINY)
                .flags(GENERATE_FOIL, GENERATE_GEAR)
                .element(GTElements.Ru)
                .blastTemp(2607, GasTier.MID, GTValues.VA[GTValues.EV], 900)
                .buildAndRegister();

        Rutherfordium = new Material.Builder(GTCEu.id("rutherfordium"))
                .color(0x6b6157).secondaryColor(0xFFF6A1).iconSet(RADIOACTIVE)
                .element(GTElements.Rf)
                .buildAndRegister();

        Samarium = new Material.Builder(GTCEu.id("samarium"))
                .ingot()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(1345))
                .color(0xc5c5b3).secondaryColor(0x183e3f).iconSet(METALLIC)
                .flags(GENERATE_LONG_ROD)
                .element(GTElements.Sm)
                .blastTemp(5400, GasTier.HIGH, GTValues.VA[GTValues.EV], 1500)
                .buildAndRegister();

        Scandium = new Material.Builder(GTCEu.id("scandium"))
                .color(0xb1b2ac).secondaryColor(0x1c3433)
                .iconSet(METALLIC)
                .element(GTElements.Sc)
                .buildAndRegister();

        Seaborgium = new Material.Builder(GTCEu.id("seaborgium"))
                .color(0x807c76).secondaryColor(0x19C5FF).iconSet(RADIOACTIVE)
                .element(GTElements.Sg)
                .buildAndRegister();

        Selenium = new Material.Builder(GTCEu.id("selenium"))
                .color(0x58587b).secondaryColor(0x401b24).iconSet(SHINY)
                .element(GTElements.Se)
                .buildAndRegister();

        Silicon = new Material.Builder(GTCEu.id("silicon"))
                .ingot().fluid()
                .color(0xaaaab5).secondaryColor(0x10293b).iconSet(METALLIC)
                .flags(GENERATE_FOIL)
                .element(GTElements.Si)
                .blastTemp(2273) // no gas tier for silicon
                .buildAndRegister();

        Silver = new Material.Builder(GTCEu.id("silver"))
                .ingot()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(1235))
                .ore()
                .color(0xDCDCFF).secondaryColor(0x5a4705).iconSet(SHINY)
                .appendFlags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_FINE_WIRE, GENERATE_RING)
                .element(GTElements.Ag)
                .cableProperties(GTValues.V[3], 1, 1)
                .buildAndRegister();

        Sodium = new Material.Builder(GTCEu.id("sodium"))
                .dust()
                .color(0xabb1ba).secondaryColor(0x2b30a3).iconSet(METALLIC)
                .element(GTElements.Na)
                .buildAndRegister();

        Strontium = new Material.Builder(GTCEu.id("strontium"))
                .color(0x7a7953).secondaryColor(0x4c0b06).iconSet(METALLIC)
                .element(GTElements.Sr)
                .buildAndRegister();

        Sulfur = new Material.Builder(GTCEu.id("sulfur"))
                .dust().ore()
                .color(0xfdff31).secondaryColor(0xffb400)
                .flags(FLAMMABLE)
                .element(GTElements.S)
                .buildAndRegister();

        Tantalum = new Material.Builder(GTCEu.id("tantalum"))
                .ingot()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(3290))
                .color(0xa8a7c6).secondaryColor(0x1f2b20).iconSet(METALLIC)
                .appendFlags(STD_METAL, GENERATE_FOIL, GENERATE_FINE_WIRE)
                .element(GTElements.Ta)
                .buildAndRegister();

        Technetium = new Material.Builder(GTCEu.id("technetium"))
                .color(0xb1d0d8).secondaryColor(0xd7fce2).iconSet(RADIOACTIVE)
                .element(GTElements.Tc)
                .buildAndRegister();

        Tellurium = new Material.Builder(GTCEu.id("tellurium"))
                .color(0xd9e8d2).secondaryColor(0x0018ff)
                .iconSet(RADIOACTIVE)
                .element(GTElements.Te)
                .buildAndRegister();

        Tennessine = new Material.Builder(GTCEu.id("tennessine"))
                .color(0x768189).secondaryColor(0xbca3ff).iconSet(RADIOACTIVE)
                .element(GTElements.Ts)
                .buildAndRegister();

        Terbium = new Material.Builder(GTCEu.id("terbium"))
                .color(0xcedab4).secondaryColor(0x263640)
                .iconSet(METALLIC)
                .element(GTElements.Tb)
                .buildAndRegister();

        Thorium = new Material.Builder(GTCEu.id("thorium"))
                .ingot()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(2023))
                .ore()
                .color(0x323528).secondaryColor(0x051E05).iconSet(SHINY)
                .appendFlags(STD_METAL, GENERATE_ROD)
                .element(GTElements.Th)
                .buildAndRegister();

        Thallium = new Material.Builder(GTCEu.id("thallium"))
                .color(0xc1c9de).secondaryColor(0x1e576a).iconSet(SHINY)
                .element(GTElements.Tl)
                .buildAndRegister();

        Thulium = new Material.Builder(GTCEu.id("thulium"))
                .color(0xafb0a4).secondaryColor(0x420b0b)
                .iconSet(METALLIC)
                .element(GTElements.Tm)
                .buildAndRegister();

        Tin = new Material.Builder(GTCEu.id("tin"))
                .ingot(1)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(505))
                .ore()
                .color(0xfafeff).secondaryColor(0x4e676c)
                .appendFlags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_ROTOR, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .element(GTElements.Sn)
                .cableProperties(GTValues.V[1], 1, 1)
                .itemPipeProperties(4096, 0.5f)
                .buildAndRegister();

        Titanium = new Material.Builder(GTCEu.id("titanium")) // todo Ore? Look at EBF recipe here if we do Ti ores
                .ingot(3).fluid()
                .color(0xe8b1fa).secondaryColor(0xd8d5d9).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, GENERATE_ROTOR, GENERATE_SMALL_GEAR, GENERATE_GEAR, GENERATE_FRAME)
                .element(GTElements.Ti)
                .toolStats(ToolProperty.Builder.of(8.0F, 6.0F, 1536, 3)
                        .enchantability(14).build())
                .rotorStats(7.0f, 3.0f, 1600)
                .fluidPipeProperties(2426, 150, true)
                .blastTemp(1941, GasTier.MID, GTValues.VA[GTValues.HV], 1500)
                .buildAndRegister();

        Tritium = new Material.Builder(GTCEu.id("tritium"))
                .fluid(FluidStorageKeys.GAS, new FluidBuilder().state(FluidState.GAS).customStill())
                .color(0xff316b).secondaryColor(0xd00000)
                .iconSet(METALLIC)
                .element(GTElements.T)
                .buildAndRegister();

        Tungsten = new Material.Builder(GTCEu.id("tungsten"))
                .ingot(3)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(3695))
                .color(0x3b3a32).secondaryColor(0x2a2800).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FOIL, GENERATE_GEAR, GENERATE_FRAME)
                .element(GTElements.W)
                .rotorStats(7.0f, 3.0f, 2560)
                .cableProperties(GTValues.V[5], 2, 2)
                .fluidPipeProperties(4618, 50, true, true, false, true)
                .blastTemp(3600, GasTier.MID, GTValues.VA[GTValues.EV], 1800)
                .buildAndRegister();

        Uranium238 = new Material.Builder(GTCEu.id("uranium"))
                .ingot(3)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(1405))
                .color(0x1d891d).secondaryColor(0x33342c).iconSet(RADIOACTIVE)
                .appendFlags(EXT_METAL)
                .element(GTElements.U238)
                .buildAndRegister();

        Uranium235 = new Material.Builder(GTCEu.id("uranium_235"))
                .ingot(3)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(1405))
                .color(0x46FA46).secondaryColor(0x33342c).iconSet(RADIOACTIVE)
                .appendFlags(EXT_METAL)
                .element(GTElements.U235)
                .buildAndRegister();

        Vanadium = new Material.Builder(GTCEu.id("vanadium"))
                .ingot().fluid()
                .color(0x696d76).secondaryColor(0x240808).iconSet(METALLIC)
                .element(GTElements.V)
                .blastTemp(2183, GasTier.MID)
                .buildAndRegister();

        Xenon = new Material.Builder(GTCEu.id("xenon"))
                .gas()
                .color(0x00FFFF)
                .element(GTElements.Xe)
                .buildAndRegister();

        Ytterbium = new Material.Builder(GTCEu.id("ytterbium"))
                .color(0xA7A7A7).iconSet(METALLIC)
                .element(GTElements.Yb)
                .buildAndRegister();

        Yttrium = new Material.Builder(GTCEu.id("yttrium"))
                .ingot().fluid()
                .color(0x7d8072).secondaryColor(0x15161a).iconSet(METALLIC)
                .element(GTElements.Y)
                .blastTemp(1799)
                .buildAndRegister();

        Zinc = new Material.Builder(GTCEu.id("zinc"))
                .ingot(1)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(693))
                .color(0xEBEBFA).secondaryColor(0x232c30).iconSet(METALLIC)
                .appendFlags(STD_METAL, MORTAR_GRINDABLE, GENERATE_FOIL, GENERATE_RING, GENERATE_FINE_WIRE)
                .element(GTElements.Zn)
                .buildAndRegister();

        Zirconium = new Material.Builder(GTCEu.id("zirconium"))
                .color(0xfff0e2).secondaryColor(0x271813).iconSet(METALLIC)
                .element(GTElements.Zr)
                .buildAndRegister();

        Naquadah = new Material.Builder(GTCEu.id("naquadah"))
                .ingot(4)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().customStill())
                .ore()
                .color(0x323232, false).secondaryColor(0x1e251b).iconSet(METALLIC)
                .appendFlags(EXT_METAL, GENERATE_FOIL, GENERATE_SPRING, GENERATE_FINE_WIRE, GENERATE_BOLT_SCREW)
                .element(GTElements.Nq)
                .rotorStats(6.0f, 4.0f, 1280)
                .cableProperties(GTValues.V[7], 2, 2)
                .fluidPipeProperties(3776, 200, true, false, true, true)
                .blastTemp(5000, GasTier.HIGH, GTValues.VA[GTValues.IV], 600)
                .buildAndRegister();

        NaquadahEnriched = new Material.Builder(GTCEu.id("enriched_naquadah"))
                .ingot(4)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().customStill())
                .color(0x3C3C3C, false).secondaryColor(0x122f06).iconSet(METALLIC)
                .appendFlags(EXT_METAL, GENERATE_FOIL)
                .element(GTElements.Nq1)
                .blastTemp(7000, GasTier.HIGH, GTValues.VA[GTValues.IV], 1000)
                .buildAndRegister();

        Naquadria = new Material.Builder(GTCEu.id("naquadria"))
                .ingot(3)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().customStill())
                .color(0x1E1E1E, false).secondaryColor(0x59b3ff).iconSet(RADIOACTIVE)
                .appendFlags(EXT_METAL, GENERATE_FOIL, GENERATE_GEAR, GENERATE_FINE_WIRE, GENERATE_BOLT_SCREW)
                .element(GTElements.Nq2)
                .blastTemp(9000, GasTier.HIGH, GTValues.VA[GTValues.ZPM], 1200)
                .buildAndRegister();

        Neutronium = new Material.Builder(GTCEu.id("neutronium"))
                .ingot(6)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(100_000))
                .color(0xFFFFFF).secondaryColor(0x000000)
                .appendFlags(EXT_METAL, GENERATE_BOLT_SCREW, GENERATE_FRAME)
                .element(GTElements.Nt)
                .toolStats(ToolProperty.Builder.of(180.0F, 100.0F, 65535, 6)
                        .attackSpeed(0.5F).enchantability(33).magnetic().unbreakable().build())
                .rotorStats(24.0f, 12.0f, 655360)
                .fluidPipeProperties(100_000, 5000, true, true, true, true)
                .buildAndRegister();

        Tritanium = new Material.Builder(GTCEu.id("tritanium"))
                .ingot(6)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(25_000))
                .color(0xc35769).secondaryColor(0x210840).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, GENERATE_FRAME, GENERATE_RING, GENERATE_SMALL_GEAR, GENERATE_ROUND, GENERATE_FOIL, GENERATE_FINE_WIRE, GENERATE_GEAR)
                .element(GTElements.Tr)
                .cableProperties(GTValues.V[8], 1, 8)
                .rotorStats(20.0f, 6.0f, 10240)
                .buildAndRegister();

        Duranium = new Material.Builder(GTCEu.id("duranium"))
                .ingot(5)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(7500))
                .color(0xf3e7a9).secondaryColor(0x9c9487).iconSet(BRIGHT)
                .appendFlags(EXT_METAL, GENERATE_FOIL, GENERATE_GEAR)
                .element(GTElements.Dr)
                .toolStats(ToolProperty.Builder.of(14.0F, 12.0F, 8192, 5)
                        .attackSpeed(0.3F).enchantability(33).magnetic().build())
                .fluidPipeProperties(9625, 500, true, true, true, true)
                .buildAndRegister();

        Trinium = new Material.Builder(GTCEu.id("trinium"))
                .ingot(7).fluid()
                .color(0x81808a).secondaryColor(0x351d4b).iconSet(SHINY)
                .flags(GENERATE_FOIL, GENERATE_BOLT_SCREW, GENERATE_GEAR)
                .element(GTElements.Ke)
                .cableProperties(GTValues.V[7], 6, 4)
                .blastTemp(7200, GasTier.HIGH, GTValues.VA[GTValues.LuV], 1500)
                .buildAndRegister();

    }
}
