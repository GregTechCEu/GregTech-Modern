package com.gregtechceu.gtceu.common.data.materials;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ArmorProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty.GasTier;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttributes;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.common.data.GTElements;
import com.gregtechceu.gtceu.common.data.GTMedicalConditions;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class ElementMaterials {

    public static void register() {
        Actinium = REGISTRATE.material("actinium")
                .color(0xC3D1FF).secondaryColor(0x397090).iconSet(METALLIC)
                .element(GTElements.Ac)
                .buildAndRegister();

        Aluminium = REGISTRATE.material("aluminium")
                .ingot()
                .liquid(new FluidBuilder().temperature(933))
                .ore()
                .color(0x7db9d8).secondaryColor(0x756ac9c)
                .appendFlags(EXT2_METAL, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_FRAME,
                        GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .element(GTElements.Al)
                .toolStats(ToolProperty.Builder.of(6.0F, 7.5F, 768, 2)
                        .enchantability(14).build())
                .rotorStats(100, 140, 2.0f, 128)
                .cableProperties(V[EV], 1, 1)
                .fluidPipeProperties(1166, 100, true)
                .blast(1700, GasTier.LOW)
                .buildAndRegister();

        Americium = REGISTRATE.material("americium")
                .ingot(3)
                .liquid(new FluidBuilder().temperature(1449))
                .plasma()
                .color(0x287869).iconSet(RADIOACTIVE)
                .appendFlags(EXT_METAL, GENERATE_FOIL, GENERATE_FINE_WIRE)
                .element(GTElements.Am)
                .itemPipeProperties(64, 64)
                .buildAndRegister();

        Antimony = REGISTRATE.material("antimony")
                .ingot()
                .liquid(new FluidBuilder().temperature(904))
                .color(0xeaeaff).secondaryColor(0x8181bd).iconSet(SHINY)
                .flags(MORTAR_GRINDABLE)
                .element(GTElements.Sb)
                .buildAndRegister();

        Argon = REGISTRATE.material("argon")
                .gas().plasma()
                .color(0x00FF00)
                .element(GTElements.Ar)
                .buildAndRegister();

        Arsenic = REGISTRATE.material("arsenic")
                .dust()
                .gas(new FluidBuilder()
                        .state(FluidState.GAS)
                        .temperature(887))
                .color(0x9c9c8d).secondaryColor(0x676756)
                .element(GTElements.As)
                .hazard(HazardProperty.HazardTrigger.INHALATION, GTMedicalConditions.ARSENICOSIS)
                .buildAndRegister();

        Astatine = REGISTRATE.material("astatine")
                .color(0x65204f).secondaryColor(0x17212b)
                .element(GTElements.At)
                .buildAndRegister();

        Barium = REGISTRATE.material("barium")
                .dust()
                .color(0xede192).secondaryColor(0xa7ad4d).iconSet(METALLIC)
                .element(GTElements.Ba)
                .buildAndRegister();

        Berkelium = REGISTRATE.material("berkelium")
                .color(0x645A88).iconSet(RADIOACTIVE)
                .element(GTElements.Bk)
                .buildAndRegister();

        Beryllium = REGISTRATE.material("beryllium")
                .ingot()
                .liquid(new FluidBuilder().temperature(1560))
                .ore()
                .color(0x73d73d).secondaryColor(0x184537).iconSet(METALLIC)
                .appendFlags(STD_METAL)
                .element(GTElements.Be)
                .buildAndRegister();

        Bismuth = REGISTRATE.material("bismuth")
                .ingot(1)
                .liquid(new FluidBuilder().temperature(545))
                .color(0x5fdddd).secondaryColor(0x517385).iconSet(METALLIC)
                .element(GTElements.Bi)
                .buildAndRegister();

        Bohrium = REGISTRATE.material("bohrium")
                .color(0xde67ff).secondaryColor(0xDC57FF).iconSet(RADIOACTIVE)
                .element(GTElements.Bh)
                .buildAndRegister();

        Boron = REGISTRATE.material("boron")
                .dust()
                .color(0xbffdbf).secondaryColor(0x6d7058)
                .element(GTElements.B)
                .buildAndRegister();

        Bromine = REGISTRATE.material("bromine")
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x912200).secondaryColor(0x080101).iconSet(SHINY)
                .element(GTElements.Br)
                .buildAndRegister();

        Caesium = REGISTRATE.material("caesium")
                .dust()
                .color(0xd1821c).secondaryColor(0x231f14).iconSet(SHINY)
                .element(GTElements.Cs)
                .buildAndRegister();

        Calcium = REGISTRATE.material("calcium")
                .dust()
                .color(0xFFF5DE).secondaryColor(0xa4a4a4).iconSet(METALLIC)
                .element(GTElements.Ca)
                .buildAndRegister();

        Californium = REGISTRATE.material("californium")
                .color(0xA85A12).iconSet(RADIOACTIVE)
                .element(GTElements.Cf)
                .buildAndRegister();

        Carbon = REGISTRATE.material("carbon")
                .dust()
                .liquid(new FluidBuilder().temperature(4600))
                .color(0x333030).secondaryColor(0x221c1c)
                .element(GTElements.C)
                .buildAndRegister();

        Cadmium = REGISTRATE.material("cadmium")
                .dust()
                .color(0x636377).secondaryColor(0x431a34).iconSet(SHINY)
                .element(GTElements.Cd)
                .hazard(HazardProperty.HazardTrigger.ANY, GTMedicalConditions.POISON)
                .buildAndRegister();

        Cerium = REGISTRATE.material("cerium")
                .dust()
                .liquid(new FluidBuilder().temperature(1068))
                .color(0x87917D).secondaryColor(0x5e6458).iconSet(METALLIC)
                .element(GTElements.Ce)
                .buildAndRegister();

        Chlorine = REGISTRATE.material("chlorine")
                .gas(new FluidBuilder().state(FluidState.GAS).customStill())
                .element(GTElements.Cl)
                // TODO hazard
                .buildAndRegister();

        Chromium = REGISTRATE.material("chromium")
                .ingot(3)
                .liquid(new FluidBuilder().temperature(2180))
                .color(0xf3e0ea).secondaryColor(0x441f2e).iconSet(SHINY)
                .appendFlags(EXT_METAL, GENERATE_ROTOR)
                .element(GTElements.Cr)
                .rotorStats(130, 155, 3.0f, 512)
                .fluidPipeProperties(2180, 35, true, true, false, false)
                .blast(1700, GasTier.LOW)
                .hazard(HazardProperty.HazardTrigger.SKIN_CONTACT, GTMedicalConditions.CARCINOGEN)
                .buildAndRegister();

        Cobalt = REGISTRATE.material("cobalt")
                .ingot()
                .liquid(new FluidBuilder().temperature(1768))
                .ore() // leave for TiCon ore processing
                .color(0x5050FA).secondaryColor(0x2d2d7a).iconSet(METALLIC)
                .appendFlags(EXT_METAL, GENERATE_FINE_WIRE)
                .element(GTElements.Co)
                .cableProperties(V[LV], 2, 2)
                .itemPipeProperties(2560, 2.0f)
                .buildAndRegister();

        Copernicium = REGISTRATE.material("copernicium")
                .color(0x565c5d).secondaryColor(0xffd34b).iconSet(RADIOACTIVE)
                .element(GTElements.Cn)
                // .radioactiveHazard(1)
                .buildAndRegister();

        Copper = REGISTRATE.material("copper")
                .ingot(1)
                .liquid(new FluidBuilder().temperature(1358))
                .ore()
                .color(0xe77c56).secondaryColor(0xe4673e).iconSet(BRIGHT)
                .appendFlags(EXT_METAL, MORTAR_GRINDABLE, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_RING,
                        GENERATE_FINE_WIRE, GENERATE_ROTOR)
                .element(GTElements.Cu)
                .cableProperties(V[MV], 1, 2)
                .fluidPipeProperties(1696, 6, true)
                .buildAndRegister();

        Curium = REGISTRATE.material("curium")
                .color(0x7B544E).iconSet(RADIOACTIVE)
                .element(GTElements.Cm)
                // .radioactiveHazard(1)
                .buildAndRegister();

        Darmstadtium = REGISTRATE.material("darmstadtium")
                .ingot().fluid()
                .color(0x578062).iconSet(RADIOACTIVE)
                .appendFlags(EXT2_METAL, GENERATE_ROTOR, GENERATE_DENSE, GENERATE_SMALL_GEAR)
                .element(GTElements.Ds)
                .buildAndRegister();

        Deuterium = REGISTRATE.material("deuterium")
                .gas(new FluidBuilder().state(FluidState.GAS).customStill())
                .element(GTElements.D)
                .buildAndRegister();

        Dubnium = REGISTRATE.material("dubnium")
                .color(0xc7ddde).secondaryColor(0x00f3ff).iconSet(RADIOACTIVE)
                .element(GTElements.Db)
                .buildAndRegister();

        Dysprosium = REGISTRATE.material("dysprosium")
                .color(0x6a664b).secondaryColor(0x423307)
                .iconSet(METALLIC)
                .element(GTElements.Dy)
                .buildAndRegister();

        Einsteinium = REGISTRATE.material("einsteinium")
                .color(0xCE9F00).iconSet(RADIOACTIVE)
                .element(GTElements.Es)
                .buildAndRegister();

        Erbium = REGISTRATE.material("erbium")
                .color(0xeccbdb).secondaryColor(0x5d625a)
                .iconSet(METALLIC)
                .element(GTElements.Er)
                .buildAndRegister();

        Europium = REGISTRATE.material("europium")
                .ingot()
                .liquid(new FluidBuilder().temperature(1099))
                .color(0x20FFFF).secondaryColor(0x429393).iconSet(METALLIC)
                .appendFlags(STD_METAL, GENERATE_LONG_ROD, GENERATE_FINE_WIRE, GENERATE_SPRING, GENERATE_SPRING_SMALL,
                        GENERATE_FOIL, GENERATE_FRAME)
                .element(GTElements.Eu)
                .cableProperties(V[UHV], 2, 32)
                .fluidPipeProperties(7750, 300, true)
                .blast(b -> b.temp(6000, GasTier.MID)
                        .blastStats(VA[IV], 180)
                        .vacuumStats(VA[HV]))
                .buildAndRegister();

        Fermium = REGISTRATE.material("fermium")
                .color(0xc99fe7).secondaryColor(0x890085).iconSet(METALLIC)
                .element(GTElements.Fm)
                // .radioactiveHazard(1)
                .buildAndRegister();

        Flerovium = REGISTRATE.material("flerovium")
                .color(0x2a384e).secondaryColor(0xd2ff00)
                .iconSet(RADIOACTIVE)
                .element(GTElements.Fl)
                .buildAndRegister();

        Fluorine = REGISTRATE.material("fluorine")
                .gas(new FluidBuilder().state(FluidState.GAS).customStill())
                .element(GTElements.F)
                .hazard(HazardProperty.HazardTrigger.SKIN_CONTACT, GTMedicalConditions.CHEMICAL_BURNS, false)
                .buildAndRegister();

        Francium = REGISTRATE.material("francium")
                .color(0xAAAAAA).secondaryColor(0x0000ff).iconSet(RADIOACTIVE)
                .element(GTElements.Fr)
                .buildAndRegister();

        Gadolinium = REGISTRATE.material("gadolinium")
                .color(0x828a7a).secondaryColor(0x363420).iconSet(METALLIC)
                .element(GTElements.Gd)
                .buildAndRegister();

        Gallium = REGISTRATE.material("gallium")
                .ingot()
                .liquid(new FluidBuilder().temperature(303))
                .color(0x7a84ca).secondaryColor(0x13132e).iconSet(SHINY)
                .appendFlags(STD_METAL, GENERATE_FOIL)
                .element(GTElements.Ga)
                .buildAndRegister();

        Germanium = REGISTRATE.material("germanium")
                .color(0x4a4a4a).secondaryColor(0x2d2612).iconSet(SHINY)
                .element(GTElements.Ge)
                .buildAndRegister();

        Gold = REGISTRATE.material("gold")
                .ingot()
                .liquid(new FluidBuilder().temperature(1337))
                .ore()
                .color(0xfdf55f).secondaryColor(0xf25833).iconSet(SHINY)
                .appendFlags(EXT2_METAL, GENERATE_RING, MORTAR_GRINDABLE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES,
                        GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE, GENERATE_FOIL)
                .element(GTElements.Au)
                .cableProperties(V[HV], 3, 2)
                .fluidPipeProperties(1671, 25, true, true, false, false)
                .buildAndRegister();

        Hafnium = REGISTRATE.material("hafnium")
                .color(0x99999A).secondaryColor(0x2b4a3a).iconSet(SHINY)
                .element(GTElements.Hf)
                .buildAndRegister();

        Hassium = REGISTRATE.material("hassium")
                .color(0x738786).secondaryColor(0x62ffd5)
                .iconSet(RADIOACTIVE)
                .element(GTElements.Hs)
                .buildAndRegister();

        Holmium = REGISTRATE.material("holmium")
                .color(0xf6fc9c).secondaryColor(0xa3a3a3)
                .iconSet(METALLIC)
                .element(GTElements.Ho)
                .buildAndRegister();

        Hydrogen = REGISTRATE.material("hydrogen")
                .gas()
                .color(0x0000B5)
                .element(GTElements.H)
                .buildAndRegister();

        Helium = REGISTRATE.material("helium")
                .gas(new FluidBuilder().state(FluidState.GAS).customStill())
                .plasma()
                .liquid(new FluidBuilder()
                        .temperature(4)
                        .color(0xFCFF90)
                        .name("liquid_helium")
                        .translation("gtceu.fluid.liquid_generic"))
                .element(GTElements.He)
                .buildAndRegister();
        Helium.getProperty(PropertyKey.FLUID).setPrimaryKey(FluidStorageKeys.GAS);

        Helium3 = REGISTRATE.material("helium_3")
                .gas(new FluidBuilder()
                        .customStill()
                        .translation("gtceu.fluid.generic"))
                .element(GTElements.He3)
                .buildAndRegister();

        Indium = REGISTRATE.material("indium")
                .ingot()
                .liquid(new FluidBuilder().temperature(430))
                .color(0x5c3588).secondaryColor(0x2b0b4a).iconSet(SHINY)
                .element(GTElements.In)
                .buildAndRegister();

        Iodine = REGISTRATE.material("iodine")
                .dust()
                .color(0x3e4467).secondaryColor(0x021e40).iconSet(SHINY)
                .element(GTElements.I)
                .buildAndRegister();

        Iridium = REGISTRATE.material("iridium")
                .ingot(3)
                .liquid(new FluidBuilder().temperature(2719))
                .color(0x99fede).secondaryColor(0x6cd1cf).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, GENERATE_FINE_WIRE, GENERATE_GEAR, GENERATE_FRAME)
                .element(GTElements.Ir)
                .rotorStats(130, 115, 3.0f, 2560)
                .fluidPipeProperties(3398, 250, true, false, true, false)
                .blast(b -> b.temp(4500, GasTier.HIGH)
                        .blastStats(VA[IV], 1100)
                        .vacuumStats(VA[EV], 250))
                .buildAndRegister();

        Iron = REGISTRATE.material("iron")
                .ingot()
                .liquid(new FluidBuilder().temperature(1811))
                .plasma()
                .ore()
                .color(0xeeeeee).secondaryColor(0x979797).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_ROTOR, GENERATE_SMALL_GEAR, GENERATE_GEAR,
                        GENERATE_SPRING_SMALL, GENERATE_SPRING, GENERATE_ROUND, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES,
                        BLAST_FURNACE_CALCITE_TRIPLE)
                .element(GTElements.Fe)
                .toolStats(ToolProperty.Builder.of(2.0F, 2.0F, 256, 2)
                        .enchantability(14).addTypes(GTToolType.MORTAR).build())
                .rotorStats(115, 115, 2.5f, 256)
                .cableProperties(V[MV], 2, 3)
                .buildAndRegister();

        Krypton = REGISTRATE.material("krypton")
                .gas(new FluidBuilder()
                        .customStill()
                        .translation("gtceu.fluid.generic"))
                .color(0x80FF80)
                .element(GTElements.Kr)
                .buildAndRegister();

        Lanthanum = REGISTRATE.material("lanthanum")
                .dust()
                .liquid(new FluidBuilder().temperature(1193))
                .color(0xd17d50).secondaryColor(0x4a3560).iconSet(METALLIC)
                .element(GTElements.La)
                .buildAndRegister();

        Lawrencium = REGISTRATE.material("lawrencium")
                .color(0x5D7575)
                .iconSet(RADIOACTIVE)
                .element(GTElements.Lr)
                .buildAndRegister();

        Lead = REGISTRATE.material("lead")
                .ingot(1)
                .liquid(new FluidBuilder().temperature(600))
                .ore()
                .color(0x7e6f82).secondaryColor(0x290633)
                .appendFlags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_ROTOR, GENERATE_SPRING, GENERATE_SPRING_SMALL,
                        GENERATE_FINE_WIRE)
                .element(GTElements.Pb)
                .cableProperties(V[ULV], 2, 2)
                .fluidPipeProperties(1200, 32, true)
                .hazard(HazardProperty.HazardTrigger.INHALATION, GTMedicalConditions.WEAK_POISON)
                .buildAndRegister();

        Lithium = REGISTRATE.material("lithium")
                .dust()
                .liquid(new FluidBuilder().temperature(454))
                .ore()
                .color(0xd7e7ee).secondaryColor(0xBDC7DB)
                .element(GTElements.Li)
                .buildAndRegister();

        Livermorium = REGISTRATE.material("livermorium")
                .color(0x939393).secondaryColor(0xff5e5e).iconSet(RADIOACTIVE)
                .element(GTElements.Lv)
                .buildAndRegister();

        Lutetium = REGISTRATE.material("lutetium")
                .dust()
                .liquid(new FluidBuilder().temperature(1925))
                .color(0x00ccff).secondaryColor(0x4c687a).iconSet(METALLIC)
                .element(GTElements.Lu)
                .buildAndRegister();

        Magnesium = REGISTRATE.material("magnesium")
                .dust()
                .liquid(new FluidBuilder().temperature(923))
                .color(0xd6e3ff).secondaryColor(0x594d19).iconSet(FINE)
                .element(GTElements.Mg)
                .buildAndRegister();

        Mendelevium = REGISTRATE.material("mendelevium")
                .color(0x1D4ACF).iconSet(RADIOACTIVE)
                .element(GTElements.Md)
                .buildAndRegister();

        Manganese = REGISTRATE.material("manganese")
                .ingot()
                .liquid(new FluidBuilder().temperature(1519))
                .color(0x88a669).secondaryColor(0xCDE1B9)
                .appendFlags(STD_METAL, GENERATE_FOIL, GENERATE_BOLT_SCREW)
                .element(GTElements.Mn)
                .rotorStats(100, 115, 2.0f, 512)
                .buildAndRegister();

        Meitnerium = REGISTRATE.material("meitnerium")
                .color(0x4f3c82).secondaryColor(0x6e90ff).iconSet(RADIOACTIVE)
                .element(GTElements.Mt)
                .buildAndRegister();

        Mercury = REGISTRATE.material("mercury")
                .fluid()
                .color(0xE6DCDC).iconSet(DULL)
                .element(GTElements.Hg)
                .hazard(HazardProperty.HazardTrigger.ANY, GTMedicalConditions.WEAK_POISON)
                .buildAndRegister();

        Molybdenum = REGISTRATE.material("molybdenum")
                .ingot()
                .liquid(new FluidBuilder().temperature(2896))
                .ore()
                .color(0xc1c1ce).secondaryColor(0x404068).iconSet(SHINY)
                .element(GTElements.Mo)
                .flags(GENERATE_FOIL, GENERATE_BOLT_SCREW)
                .rotorStats(100, 115, 2.0f, 512)
                .buildAndRegister();

        Moscovium = REGISTRATE.material("moscovium")
                .color(0x2a1b40).secondaryColor(0xbd91ff).iconSet(RADIOACTIVE)
                .element(GTElements.Mc)
                .buildAndRegister();

        Neodymium = REGISTRATE.material("neodymium")
                .ingot().fluid().ore()
                .color(0x6c5863).secondaryColor(0x2c1919).iconSet(METALLIC)
                .appendFlags(STD_METAL, GENERATE_ROD, GENERATE_BOLT_SCREW)
                .element(GTElements.Nd)
                .rotorStats(100, 115, 2.0f, 512)
                .blast(1297, GasTier.MID)
                .buildAndRegister();

        Neon = REGISTRATE.material("neon")
                .gas()
                .color(0xFAB4B4)
                .element(GTElements.Ne)
                .buildAndRegister();

        Neptunium = REGISTRATE.material("neptunium")
                .color(0x284D7B).iconSet(RADIOACTIVE)
                .element(GTElements.Np)
                // .radioactiveHazard(1)
                .buildAndRegister();

        Nickel = REGISTRATE.material("nickel")
                .ingot()
                .liquid(new FluidBuilder().temperature(1728))
                .plasma()
                .ore()
                .color(0xccdff5).secondaryColor(0x59563a).iconSet(METALLIC)
                .appendFlags(STD_METAL, MORTAR_GRINDABLE)
                .element(GTElements.Ni)
                .cableProperties(V[LV], 3, 3)
                .itemPipeProperties(2048, 1.0f)
                .buildAndRegister();

        Nihonium = REGISTRATE.material("nihonium")
                .color(0x323957).secondaryColor(0xbfabff).iconSet(RADIOACTIVE)
                .element(GTElements.Nh)
                .buildAndRegister();

        Niobium = REGISTRATE.material("niobium")
                .ingot().fluid()
                .color(0xb494b4).secondaryColor(0x4b3f4d).iconSet(BRIGHT)
                .element(GTElements.Nb)
                .blast(b -> b.temp(2750, GasTier.MID)
                        .blastStats(VA[HV], 900))
                .buildAndRegister();

        Nitrogen = REGISTRATE.material("nitrogen")
                .gas().plasma()
                .color(0x00BFC1)
                .element(GTElements.N)
                .buildAndRegister();

        Nobelium = REGISTRATE.material("nobelium")
                .color(0x3e4758).secondaryColor(0x43deff)
                .iconSet(RADIOACTIVE)
                .element(GTElements.No)
                .buildAndRegister();

        Oganesson = REGISTRATE.material("oganesson")
                .color(0x443936).secondaryColor(0xff1dbd).iconSet(RADIOACTIVE)
                .element(GTElements.Og)
                .buildAndRegister();

        Osmium = REGISTRATE.material("osmium")
                .ingot(4)
                .liquid(new FluidBuilder().temperature(3306))
                .color(0x54afff).secondaryColor(0x6e6eff).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, GENERATE_FOIL)
                .element(GTElements.Os)
                .rotorStats(160, 185, 4.0f, 1280)
                .cableProperties(V[LuV], 4, 2)
                .itemPipeProperties(256, 8.0f)
                .blast(b -> b.temp(4500, GasTier.HIGH)
                        .blastStats(VA[LuV], 1000)
                        .vacuumStats(VA[EV], 300))
                .buildAndRegister();

        Oxygen = REGISTRATE.material("oxygen")
                .gas()
                .liquid(new FluidBuilder()
                        .temperature(85)
                        .color(0x6688DD)
                        .name("liquid_oxygen")
                        .translation("gtceu.fluid.liquid_generic"))
                .plasma()
                .color(0x4CC3FF)
                .element(GTElements.O)
                .buildAndRegister();
        Oxygen.getProperty(PropertyKey.FLUID).setPrimaryKey(FluidStorageKeys.GAS);

        Palladium = REGISTRATE.material("palladium")
                .ingot().fluid().ore()
                .color(0xbd92b5).secondaryColor(0x535b14).iconSet(SHINY)
                .appendFlags(EXT_METAL, GENERATE_FOIL, GENERATE_FINE_WIRE)
                .element(GTElements.Pd)
                .blast(b -> b.temp(1828, GasTier.LOW)
                        .blastStats(VA[HV], 900)
                        .vacuumStats(VA[HV], 150))
                .buildAndRegister();

        Phosphorus = REGISTRATE.material("phosphorus")
                .dust()
                .color(0x77332c).secondaryColor(0x220202)
                .element(GTElements.P)
                .buildAndRegister();

        Polonium = REGISTRATE.material("polonium")
                .color(0x163b27).secondaryColor(0x00ff78)
                .iconSet(RADIOACTIVE)
                .element(GTElements.Po)
                // .radioactiveHazard(1)
                .buildAndRegister();

        Platinum = REGISTRATE.material("platinum")
                .ingot()
                .liquid(new FluidBuilder().temperature(2041))
                .ore()
                .color(0xfff4ba).secondaryColor(0x8d8d71).iconSet(SHINY)
                .appendFlags(EXT2_METAL, GENERATE_FOIL, GENERATE_FINE_WIRE, GENERATE_RING, GENERATE_SPRING_SMALL,
                        GENERATE_SPRING)
                .element(GTElements.Pt)
                .cableProperties(V[IV], 2, 1)
                .itemPipeProperties(512, 4.0f)
                .buildAndRegister();

        Plutonium239 = REGISTRATE.material("plutonium")
                .ingot(3)
                .liquid(new FluidBuilder().temperature(913))
                .ore(true)
                .color(0xba2727).secondaryColor(0x222730).iconSet(RADIOACTIVE)
                .element(GTElements.Pu239)
                .radioactiveHazard(1.5f)
                .buildAndRegister();

        Plutonium241 = REGISTRATE.material("plutonium_241")
                .ingot(3)
                .liquid(new FluidBuilder().temperature(913))
                .color(0xff4c4c).secondaryColor(0x222730).iconSet(RADIOACTIVE)
                .appendFlags(EXT_METAL)
                .element(GTElements.Pu241)
                .radioactiveHazard(1.5f)
                .buildAndRegister();

        Potassium = REGISTRATE.material("potassium")
                .dust(1)
                .liquid(new FluidBuilder().temperature(337))
                .color(0xd2e1f2).secondaryColor(0x6189b8).iconSet(METALLIC)
                .element(GTElements.K)
                .buildAndRegister();

        Praseodymium = REGISTRATE.material("praseodymium")
                .color(0x718060).secondaryColor(0x3f3447).iconSet(METALLIC)
                .element(GTElements.Pr)
                .buildAndRegister();

        Promethium = REGISTRATE.material("promethium")
                .color(0x814947).secondaryColor(0xd0ff71)
                .iconSet(RADIOACTIVE)
                .element(GTElements.Pm)
                // .radioactiveHazard(1)
                .buildAndRegister();

        Protactinium = REGISTRATE.material("protactinium")
                .color(0xA78B6D).iconSet(RADIOACTIVE)
                .element(GTElements.Pa)
                // .radioactiveHazard(1)
                .buildAndRegister();

        Radon = REGISTRATE.material("radon")
                .gas()
                .color(0xFF39FF)
                .element(GTElements.Rn)
                .radioactiveHazard(1)
                .buildAndRegister();

        Radium = REGISTRATE.material("radium")
                .color(0x838361).secondaryColor(0x89ff21).iconSet(RADIOACTIVE)
                .element(GTElements.Ra)
                // .radioactiveHazard(1)
                .buildAndRegister();

        Rhenium = REGISTRATE.material("rhenium")
                .color(0xcbcfd7).secondaryColor(0x37393d).iconSet(SHINY)
                .element(GTElements.Re)
                .buildAndRegister();

        Rhodium = REGISTRATE.material("rhodium")
                .ingot().fluid()
                .color(0xfd46b1).secondaryColor(0xDC0C58).iconSet(BRIGHT)
                .appendFlags(EXT2_METAL, GENERATE_GEAR, GENERATE_FINE_WIRE)
                .element(GTElements.Rh)
                .blast(b -> b.temp(2237, GasTier.MID)
                        .blastStats(VA[EV], 1200)
                        .vacuumStats(VA[HV]))
                .buildAndRegister();

        Roentgenium = REGISTRATE.material("roentgenium")
                .color(0x388c48).secondaryColor(0x198a92).iconSet(RADIOACTIVE)
                .element(GTElements.Rg)
                .buildAndRegister();

        Rubidium = REGISTRATE.material("rubidium")
                .color(0xde0f0f).secondaryColor(0x3a1f1f).iconSet(SHINY)
                .element(GTElements.Rb)
                .buildAndRegister();

        Ruthenium = REGISTRATE.material("ruthenium")
                .ingot().fluid()
                .color(0xa2cde0).secondaryColor(0x3c7285).iconSet(SHINY)
                .flags(GENERATE_FOIL, GENERATE_GEAR)
                .element(GTElements.Ru)
                .blast(b -> b.temp(2607, GasTier.MID)
                        .blastStats(VA[EV], 900)
                        .vacuumStats(VA[HV], 200))
                .buildAndRegister();

        Rutherfordium = REGISTRATE.material("rutherfordium")
                .color(0x6b6157).secondaryColor(0xFFF6A1).iconSet(RADIOACTIVE)
                .element(GTElements.Rf)
                .buildAndRegister();

        Samarium = REGISTRATE.material("samarium")
                .ingot()
                .liquid(new FluidBuilder().temperature(1345))
                .color(0xc2c289).secondaryColor(0x235254).iconSet(METALLIC)
                .flags(GENERATE_LONG_ROD)
                .element(GTElements.Sm)
                .blast(b -> b.temp(5400, GasTier.HIGH)
                        .blastStats(VA[EV], 1500)
                        .vacuumStats(VA[HV], 200))
                .buildAndRegister();

        Scandium = REGISTRATE.material("scandium")
                .color(0xb1b2ac).secondaryColor(0x1c3433)
                .iconSet(METALLIC)
                .element(GTElements.Sc)
                .buildAndRegister();

        Seaborgium = REGISTRATE.material("seaborgium")
                .color(0x19C5FF).secondaryColor(0xff19b2).iconSet(RADIOACTIVE)
                .element(GTElements.Sg)
                .buildAndRegister();

        Selenium = REGISTRATE.material("selenium")
                .color(0xffdf77).secondaryColor(0x055d28).iconSet(SHINY)
                .element(GTElements.Se)
                .buildAndRegister();

        Silicon = REGISTRATE.material("silicon")
                .ingot().fluid()
                .color(0x707078).secondaryColor(0x10293b).iconSet(METALLIC)
                .flags(GENERATE_FOIL)
                .element(GTElements.Si)
                .blast(2273) // no gas tier for silicon
                .buildAndRegister();

        Silver = REGISTRATE.material("silver")
                .ingot()
                .liquid(new FluidBuilder().temperature(1235))
                .ore()
                .color(0xDCDCFF).secondaryColor(0x5a4705).iconSet(SHINY)
                .appendFlags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_FINE_WIRE, GENERATE_RING)
                .element(GTElements.Ag)
                .cableProperties(V[HV], 1, 1)
                .buildAndRegister();

        Sodium = REGISTRATE.material("sodium")
                .dust()
                .color(0x7c80ff).secondaryColor(0x2b30a3).iconSet(METALLIC)
                .element(GTElements.Na)
                .buildAndRegister();

        Strontium = REGISTRATE.material("strontium")
                .color(0x7a7953).secondaryColor(0x4c0b06).iconSet(METALLIC)
                .element(GTElements.Sr)
                .buildAndRegister();

        Sulfur = REGISTRATE.material("sulfur")
                .dust().ore()
                .color(0xfdff31).secondaryColor(0xffb400)
                .flags(FLAMMABLE)
                .element(GTElements.S)
                .buildAndRegister();

        Tantalum = REGISTRATE.material("tantalum")
                .ingot()
                .liquid(new FluidBuilder().temperature(3290))
                .color(0xa8a7c6).secondaryColor(0x1f2b20).iconSet(METALLIC)
                .appendFlags(STD_METAL, GENERATE_FOIL, GENERATE_FINE_WIRE)
                .element(GTElements.Ta)
                .buildAndRegister();

        Technetium = REGISTRATE.material("technetium")
                .color(0x7430e1).secondaryColor(0x7430e1).iconSet(RADIOACTIVE)
                .element(GTElements.Tc)
                // .radioactiveHazard(1)
                .buildAndRegister();

        Tellurium = REGISTRATE.material("tellurium")
                .color(0x8fea66).secondaryColor(0x00bfff)
                .iconSet(RADIOACTIVE)
                .element(GTElements.Te)
                .buildAndRegister();

        Tennessine = REGISTRATE.material("tennessine")
                .color(0x785cc4).secondaryColor(0x7959d4).iconSet(RADIOACTIVE)
                .element(GTElements.Ts)
                .buildAndRegister();

        Terbium = REGISTRATE.material("terbium")
                .color(0xcedab4).secondaryColor(0x263640)
                .iconSet(METALLIC)
                .element(GTElements.Tb)
                .buildAndRegister();

        Thorium = REGISTRATE.material("thorium")
                .ingot()
                .liquid(new FluidBuilder().temperature(2023))
                .ore()
                .color(0x25411b).secondaryColor(0x051E05).iconSet(SHINY)
                .appendFlags(STD_METAL, GENERATE_ROD)
                .element(GTElements.Th)
                .buildAndRegister();

        Thallium = REGISTRATE.material("thallium")
                .color(0x5d6b8e).secondaryColor(0x815b63).iconSet(SHINY)
                .element(GTElements.Tl)
                // .poison(PoisonProperty.PoisonType.CONTACT)
                .buildAndRegister();

        Thulium = REGISTRATE.material("thulium")
                .color(0x467681).secondaryColor(0x682c2c)
                .iconSet(METALLIC)
                .element(GTElements.Tm)
                .buildAndRegister();

        Tin = REGISTRATE.material("tin")
                .ingot(1)
                .liquid(new FluidBuilder().temperature(505))
                .plasma()
                .ore()
                .color(0xfafeff).secondaryColor(0x4e676c)
                .appendFlags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_ROTOR, GENERATE_SPRING, GENERATE_SPRING_SMALL,
                        GENERATE_FINE_WIRE)
                .element(GTElements.Sn)
                .cableProperties(V[LV], 1, 1)
                .itemPipeProperties(4096, 0.5f)
                .buildAndRegister();

        Titanium = REGISTRATE.material("titanium") // todo Ore? Look at EBF recipe here if we do Ti ores
                .ingot(3).fluid()
                .color(0xed8eea).secondaryColor(0xff64bc).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, GENERATE_ROTOR, GENERATE_SMALL_GEAR, GENERATE_GEAR, GENERATE_FRAME)
                .element(GTElements.Ti)
                .toolStats(ToolProperty.Builder.of(8.0F, 6.0F, 1536, 3)
                        .enchantability(14).build())
                .armorStats(ArmorProperty.Builder.of(48, new int[] { 4, 9, 7, 4 })
                        .enchantability(18).toughness(5.0f).knockbackResistance(0.4f).build())
                .rotorStats(130, 115, 3.0f, 1600)
                .fluidPipeProperties(2426, 150, true)
                .blast(b -> b.temp(1941, GasTier.MID)
                        .blastStats(VA[HV], 1500)
                        .vacuumStats(VA[HV]))
                .buildAndRegister();

        Tritium = REGISTRATE.material("tritium")
                .gas(new FluidBuilder().state(FluidState.GAS).customStill())
                .color(0xff316b).secondaryColor(0xd00000)
                .iconSet(METALLIC)
                .element(GTElements.T)
                .radioactiveHazard(1)
                .buildAndRegister();

        Tungsten = REGISTRATE.material("tungsten")
                .ingot(3)
                .liquid(new FluidBuilder().temperature(3695))
                .color(0x3b3a32).secondaryColor(0x2a2800).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FOIL, GENERATE_GEAR,
                        GENERATE_FRAME)
                .element(GTElements.W)
                .rotorStats(130, 115, 3.0f, 2560)
                .cableProperties(V[IV], 2, 2)
                .fluidPipeProperties(4618, 50, true, true, false, true)
                .blast(b -> b.temp(3600, GasTier.MID)
                        .blastStats(VA[EV], 1800)
                        .vacuumStats(VA[HV], 300))
                .buildAndRegister();

        Uranium238 = REGISTRATE.material("uranium")
                .ingot(3)
                .liquid(new FluidBuilder().temperature(1405))
                .color(0x1d891d).secondaryColor(0x33342c).iconSet(RADIOACTIVE)
                .appendFlags(EXT_METAL)
                .element(GTElements.U238)
                .radioactiveHazard(1)
                .buildAndRegister();

        Uranium235 = REGISTRATE.material("uranium_235")
                .ingot(3)
                .liquid(new FluidBuilder().temperature(1405))
                .color(0x46FA46).secondaryColor(0x33342c).iconSet(RADIOACTIVE)
                .appendFlags(EXT_METAL)
                .element(GTElements.U235)
                .radioactiveHazard(1)
                .buildAndRegister();

        Vanadium = REGISTRATE.material("vanadium")
                .ingot().fluid()
                .color(0x696d76).secondaryColor(0x240808).iconSet(METALLIC)
                .element(GTElements.V)
                .blast(2183, GasTier.MID)
                .buildAndRegister();

        Xenon = REGISTRATE.material("xenon")
                .gas()
                .color(0x00FFFF)
                .element(GTElements.Xe)
                .buildAndRegister();

        Ytterbium = REGISTRATE.material("ytterbium")
                .color(0xA7A7A7).iconSet(METALLIC)
                .element(GTElements.Yb)
                .buildAndRegister();

        Yttrium = REGISTRATE.material("yttrium")
                .ingot().fluid()
                .color(0x7d8072).secondaryColor(0x15161a).iconSet(METALLIC)
                .element(GTElements.Y)
                .blast(1799)
                .buildAndRegister();

        Zinc = REGISTRATE.material("zinc")
                .ingot(1)
                .liquid(new FluidBuilder().temperature(693))
                .color(0xEBEBFA).secondaryColor(0x232c30).iconSet(METALLIC)
                .appendFlags(STD_METAL, MORTAR_GRINDABLE, GENERATE_FOIL, GENERATE_RING, GENERATE_FINE_WIRE)
                .element(GTElements.Zn)
                .buildAndRegister();

        Zirconium = REGISTRATE.material("zirconium")
                .color(0xb99b7e).secondaryColor(0x271813).iconSet(METALLIC)
                .element(GTElements.Zr)
                .buildAndRegister();

        Naquadah = REGISTRATE.material("naquadah")
                .ingot(4)
                .liquid(new FluidBuilder().customStill())
                .ore()
                .color(0x323232, false).secondaryColor(0x1e251b).iconSet(METALLIC)
                .appendFlags(EXT_METAL, GENERATE_FOIL, GENERATE_SPRING, GENERATE_FINE_WIRE, GENERATE_BOLT_SCREW)
                .element(GTElements.Nq)
                .rotorStats(160, 105, 4.0f, 1280)
                .cableProperties(V[ZPM], 2, 2)
                .fluidPipeProperties(3776, 200, true, false, true, true)
                .blast(b -> b.temp(5000, GasTier.HIGH)
                        .blastStats(VA[IV], 600)
                        .vacuumStats(VA[EV], 150))
                .buildAndRegister();

        NaquadahEnriched = REGISTRATE.material("enriched_naquadah")
                .ingot(4)
                .liquid(new FluidBuilder().customStill())
                .color(0x3C3C3C, false).secondaryColor(0x122f06).iconSet(METALLIC)
                .appendFlags(EXT_METAL, GENERATE_FOIL)
                .element(GTElements.Nq1)
                .blast(b -> b.temp(7000, GasTier.HIGH)
                        .blastStats(VA[IV], 1000)
                        .vacuumStats(VA[EV], 150))
                .buildAndRegister();

        Naquadria = REGISTRATE.material("naquadria")
                .ingot(3)
                .liquid(new FluidBuilder().customStill())
                .color(0x1E1E1E, false).secondaryColor(0x59b3ff).iconSet(RADIOACTIVE)
                .appendFlags(EXT_METAL, GENERATE_FOIL, GENERATE_GEAR, GENERATE_FINE_WIRE, GENERATE_BOLT_SCREW)
                .element(GTElements.Nq2)
                .blast(b -> b.temp(9000, GasTier.HIGH)
                        .blastStats(VA[ZPM], 1200)
                        .vacuumStats(VA[LuV], 200))
                .radioactiveHazard(3)
                .buildAndRegister();

        Neutronium = REGISTRATE.material("neutronium")
                .ingot(6)
                .liquid(new FluidBuilder().temperature(100_000))
                .color(0xFFFFFF).secondaryColor(0x000000)
                .appendFlags(EXT_METAL, GENERATE_BOLT_SCREW, GENERATE_FRAME, GENERATE_GEAR, GENERATE_LONG_ROD)
                .element(GTElements.Nt)
                .toolStats(ToolProperty.Builder.of(180.0F, 100.0F, 65535, 6)
                        .attackSpeed(0.5F).enchantability(33).magnetic().unbreakable().build())
                .rotorStats(400, 250, 12.0f, 655360)
                .fluidPipeProperties(100_000, 5000, true, true, true, true)
                .radioactiveHazard(10)
                .buildAndRegister();

        Tritanium = REGISTRATE.material("tritanium")
                .ingot(6)
                .liquid(new FluidBuilder().temperature(25_000))
                .color(0xc35769).secondaryColor(0x210840).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, GENERATE_FRAME, GENERATE_RING, GENERATE_SMALL_GEAR, GENERATE_ROUND,
                        GENERATE_FOIL, GENERATE_FINE_WIRE, GENERATE_GEAR)
                .element(GTElements.Tr)
                .cableProperties(V[UV], 1, 8)
                .rotorStats(220, 220, 6.0f, 10240)
                .buildAndRegister();

        Duranium = REGISTRATE.material("duranium")
                .ingot(5)
                .liquid(new FluidBuilder().temperature(7500))
                .color(0xf3e7a9).secondaryColor(0x9c9487).iconSet(BRIGHT)
                .appendFlags(EXT_METAL, GENERATE_FOIL, GENERATE_GEAR, GENERATE_BOLT_SCREW, GENERATE_LONG_ROD)
                .element(GTElements.Dr)
                .toolStats(ToolProperty.Builder.of(14.0F, 12.0F, 8192, 5)
                        .attackSpeed(0.3F).enchantability(33).magnetic().build())
                .fluidPipeProperties(9625, 500, true, true, true, true)
                .buildAndRegister();

        Trinium = REGISTRATE.material("trinium")
                .ingot(7).fluid()
                .color(0x81808a).secondaryColor(0x351d4b).iconSet(SHINY)
                .flags(GENERATE_FOIL, GENERATE_BOLT_SCREW, GENERATE_GEAR, GENERATE_SPRING)
                .element(GTElements.Ke)
                .cableProperties(V[ZPM], 6, 4)
                .blast(b -> b.temp(7200, GasTier.HIGH)
                        .blastStats(VA[LuV], 1500)
                        .vacuumStats(VA[IV], 300))
                .buildAndRegister();
    }
}
