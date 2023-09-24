package com.gregtechceu.gtceu.common.data.materials;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.fluid.FluidTypes;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty.GasTier;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import net.minecraft.world.item.enchantment.Enchantments;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class FirstDegreeMaterials {

    public static void register() {
        Almandine = new Material.Builder("almandine")
                .gem(1).ore(3, 1)
                .color(0xFF0000)
                .components(Aluminium, 2, Iron, 3, Silicon, 3, Oxygen, 12)
                .buildAndRegister();

        Andradite = new Material.Builder("andradite")
                .gem(1)
                .color(0x967800).iconSet(RUBY)
                .components(Calcium, 3, Iron, 2, Silicon, 3, Oxygen, 12)
                .buildAndRegister();

        AnnealedCopper = new Material.Builder("annealed_copper")
                .ingot().fluid()
                .color(0xFF8D3B).iconSet(BRIGHT)
                .appendFlags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_FINE_WIRE)
                .components(Copper, 1)
                .cableProperties(GTValues.V[2], 1, 1)
                .fluidTemp(1358)
                .buildAndRegister();
        Copper.getProperty(PropertyKey.INGOT).setArcSmeltingInto(AnnealedCopper);

        Asbestos = new Material.Builder("asbestos")
                .dust(1).ore(3, 1)
                .color(0xE6E6E6)
                .components(Magnesium, 3, Silicon, 2, Hydrogen, 4, Oxygen, 9)
                .buildAndRegister();

        Ash = new Material.Builder("ash")
                .dust(1)
                .color(0x969696)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 1)
                .buildAndRegister();

        Hematite = new Material.Builder("hematite")
                .dust().ore()
                .color(0x915A5A)
                .components(Iron, 2, Oxygen, 3)
                .buildAndRegister();

        BatteryAlloy = new Material.Builder("battery_alloy")
                .ingot(1).fluid()
                .color(0x9C7CA0)
                .appendFlags(EXT_METAL)
                .components(Lead, 4, Antimony, 1)
                .fluidTemp(660)
                .buildAndRegister();

        BlueTopaz = new Material.Builder("blue_topaz")
                .gem(3).ore(2, 1)
                .color(0x7B96DC).iconSet(GEM_HORIZONTAL)
                .appendFlags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .components(Aluminium, 2, Silicon, 1, Fluorine, 2, Hydrogen, 2, Oxygen, 6)
                .buildAndRegister();

        Bone = new Material.Builder("bone")
                .dust(1)
                .color(0xFAFAFA)
                .flags(MORTAR_GRINDABLE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .components(Calcium, 1)
                .buildAndRegister();

        Brass = new Material.Builder("brass")
                .ingot(1).fluid()
                .color(0xFFB400).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, MORTAR_GRINDABLE)
                .components(Zinc, 1, Copper, 3)
                .rotorStats(8.0f, 3.0f, 152)
                .itemPipeProperties(2048, 1)
                .fluidTemp(1160)
                .buildAndRegister();

        Bronze = new Material.Builder("bronze")
                .ingot().fluid()
                .color(0xFF8000).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_ROTOR, GENERATE_FRAME, GENERATE_SMALL_GEAR, GENERATE_FOIL, GENERATE_GEAR)
                .components(Tin, 1, Copper, 3)
                .toolStats(ToolProperty.Builder.of(3.0F, 2.0F, 192, 2)
                        .enchantability(18).addTypes(GTToolType.MORTAR).build())
                .rotorStats(6.0f, 2.5f, 192)
                .fluidPipeProperties(1696, 20, true)
                .fluidTemp(1357)
                .buildAndRegister();

        Goethite = new Material.Builder("goethite")
                .dust(1).ore()
                .color(0xC86400).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_CENTRIFUGING, BLAST_FURNACE_CALCITE_TRIPLE)
                .components(Iron, 1, Hydrogen, 1, Oxygen, 2)
                .buildAndRegister();

        Calcite = new Material.Builder("calcite")
                .dust(1).ore()
                .color(0xFAE6DC)
                .components(Calcium, 1, Carbon, 1, Oxygen, 3)
                .buildAndRegister();

        Cassiterite = new Material.Builder("cassiterite")
                .dust(1).ore(2, 1)
                .color(0xDCDCDC).iconSet(METALLIC)
                .components(Tin, 1, Oxygen, 2)
                .buildAndRegister();

        CassiteriteSand = new Material.Builder("cassiterite_sand")
                .dust(1).ore(2, 1)
                .color(0xDCDCDC).iconSet(SAND)
                .components(Tin, 1, Oxygen, 2)
                .buildAndRegister();

        Chalcopyrite = new Material.Builder("chalcopyrite")
                .dust(1).ore()
                .color(0xA07828)
                .components(Copper, 1, Iron, 1, Sulfur, 2)
                .buildAndRegister();

        Charcoal = new Material.Builder("charcoal")
                .gem(1, 1600) //default charcoal burn time in vanilla
                .color(0x644646).iconSet(FINE)
                .flags(FLAMMABLE, NO_SMELTING, NO_SMASHING, MORTAR_GRINDABLE)
                .components(Carbon, 1)
                .buildAndRegister();

        Chromite = new Material.Builder("chromite")
                .dust(1).ore()
                .color(0x23140F).iconSet(METALLIC)
                .components(Iron, 1, Chromium, 2, Oxygen, 4)
                .buildAndRegister();

        Cinnabar = new Material.Builder("cinnabar")
                .gem(1).ore()
                .color(0x960000).iconSet(EMERALD)
                .flags(CRYSTALLIZABLE, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Mercury, 1, Sulfur, 1)
                .buildAndRegister();

        Water = new Material.Builder("water")
                .fluid()
                .color(0x0000FF)
                .flags(DISABLE_DECOMPOSITION)
                .components(Hydrogen, 2, Oxygen, 1)
                .fluidTemp(300)
                .buildAndRegister();

        LiquidOxygen = new Material.Builder("liquid_oxygen")
                .fluid()
                .color(0x6688DD)
                .flags(DISABLE_DECOMPOSITION)
                .components(Oxygen, 1)
                .fluidTemp(85)
                .buildAndRegister();

        Coal = new Material.Builder("coal")
                .gem(1, 1600).ore(2, 1) //default coal burn time in vanilla
                .color(0x464646).iconSet(LIGNITE)
                .flags(FLAMMABLE, NO_SMELTING, NO_SMASHING, MORTAR_GRINDABLE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, DISABLE_DECOMPOSITION)
                .components(Carbon, 1)
                .buildAndRegister();

        Cobaltite = new Material.Builder("cobaltite")
                .dust(1).ore()
                .color(0x5050FA).iconSet(METALLIC)
                .components(Cobalt, 1, Arsenic, 1, Sulfur, 1)
                .buildAndRegister();

        Cooperite = new Material.Builder("cooperite")
                .dust(1).ore()
                .color(0xFFFFC8).iconSet(METALLIC)
                .components(Platinum, 3, Nickel, 1, Sulfur, 1, Palladium, 1)
                .buildAndRegister();

        Cupronickel = new Material.Builder("cupronickel")
                .ingot(1).fluid()
                .color(0xE39680).iconSet(METALLIC)
                .appendFlags(EXT_METAL, GENERATE_SPRING, GENERATE_FINE_WIRE)
                .components(Copper, 1, Nickel, 1)
                .itemPipeProperties(2048, 1)
                .cableProperties(GTValues.V[2], 1, 1)
                .fluidTemp(1542)
                .buildAndRegister();

        DarkAsh = new Material.Builder("dark_ash")
                .dust(1)
                .color(0x323232)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 1)
                .buildAndRegister();

        Diamond = new Material.Builder("diamond")
                .gem(3).ore()
                .color(0xC8FFFF).iconSet(DIAMOND)
                .flags(GENERATE_BOLT_SCREW, GENERATE_LENS, GENERATE_GEAR, NO_SMASHING, NO_SMELTING,
                        HIGH_SIFTER_OUTPUT, DISABLE_DECOMPOSITION, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .components(Carbon, 1)
                .toolStats(ToolProperty.Builder.of(6.0F, 7.0F, 768, 3)
                        .attackSpeed(0.1F).enchantability(18).build())
                .buildAndRegister();

        Electrum = new Material.Builder("electrum")
                .ingot().fluid()
                .color(0xFFFF64).iconSet(SHINY)
                .appendFlags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_FINE_WIRE, GENERATE_RING)
                .components(Silver, 1, Gold, 1)
                .itemPipeProperties(1024, 2)
                .cableProperties(GTValues.V[3], 2, 2)
                .fluidTemp(1285)
                .buildAndRegister();

        Emerald = new Material.Builder("emerald")
                .gem().ore(2, 1)
                .color(0x50FF50).iconSet(EMERALD)
                .appendFlags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, GENERATE_LENS)
                .components(Beryllium, 3, Aluminium, 2, Silicon, 6, Oxygen, 18)
                .buildAndRegister();

        Galena = new Material.Builder("galena")
                .dust(3).ore()
                .color(0x643C64)
                .flags(NO_SMELTING)
                .components(Lead, 1, Sulfur, 1)
                .buildAndRegister();

        Garnierite = new Material.Builder("garnierite")
                .dust(3).ore()
                .color(0x32C846).iconSet(METALLIC)
                .components(Nickel, 1, Oxygen, 1)
                .buildAndRegister();

        GreenSapphire = new Material.Builder("green_sapphire")
                .gem().ore()
                .color(0x64C882).iconSet(GEM_HORIZONTAL)
                .appendFlags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .components(Aluminium, 2, Oxygen, 3)
                .buildAndRegister();

        Grossular = new Material.Builder("grossular")
                .gem(1).ore(3, 1)
                .color(0xC86400).iconSet(RUBY)
                .components(Calcium, 3, Aluminium, 2, Silicon, 3, Oxygen, 12)
                .buildAndRegister();

        Ice = new Material.Builder("ice")
                .dust(0).fluid().fluidCustomTexture()
                .color(0xC8C8FF, false).iconSet(SHINY)
                .flags(NO_SMASHING, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, DISABLE_DECOMPOSITION)
                .components(Hydrogen, 2, Oxygen, 1)
                .fluidTemp(273)
                .buildAndRegister();

        Ilmenite = new Material.Builder("ilmenite")
                .dust(3).ore()
                .color(0x463732).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Iron, 1, Titanium, 1, Oxygen, 3)
                .buildAndRegister();

        Rutile = new Material.Builder("rutile")
                .gem()
                .color(0xD40D5C).iconSet(GEM_HORIZONTAL)
                .flags(DISABLE_DECOMPOSITION)
                .components(Titanium, 1, Oxygen, 2)
                .buildAndRegister();

        Bauxite = new Material.Builder("bauxite")
                .dust(1).ore()
                .color(0xC86400)
                .flags(DISABLE_DECOMPOSITION)
                .components(Aluminium, 2, Oxygen, 3)
                .buildAndRegister();

        Invar = new Material.Builder("invar")
                .ingot().fluid()
                .color(0xB4B478).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_FRAME, GENERATE_GEAR)
                .components(Iron, 2, Nickel, 1)
                .toolStats(ToolProperty.Builder.of(4.0F, 3.0F, 384, 2)
                        .addTypes(GTToolType.MORTAR)
                        .enchantability(18)
                        .enchantment(Enchantments.BANE_OF_ARTHROPODS, 3)
                        .enchantment(Enchantments.BLOCK_EFFICIENCY, 1).build())
                .rotorStats(7.0f, 3.0f, 512)
                .fluidTemp(1916)
                .buildAndRegister();

        Kanthal = new Material.Builder("kanthal")
                .ingot().fluid()
                .color(0xC2D2DF).iconSet(METALLIC)
                .appendFlags(EXT_METAL, GENERATE_SPRING)
                .components(Iron, 1, Aluminium, 1, Chromium, 1)
                .cableProperties(GTValues.V[3], 4, 3)
                .blastTemp(1800, GasTier.LOW, GTValues.VA[GTValues.HV], 900)
                .fluidTemp(1708)
                .buildAndRegister();

        Lazurite = new Material.Builder("lazurite")
                .gem(1).ore(6, 4)
                .color(0x6478FF).iconSet(LAPIS)
                .flags(GENERATE_PLATE, NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE, GENERATE_ROD, DECOMPOSITION_BY_ELECTROLYZING)
                .components(Aluminium, 6, Silicon, 6, Calcium, 8, Sodium, 8)
                .buildAndRegister();

        Magnalium = new Material.Builder("magnalium")
                .ingot().fluid()
                .color(0xC8BEFF)
                .appendFlags(EXT2_METAL)
                .components(Magnesium, 1, Aluminium, 2)
                .rotorStats(6.0f, 2.0f, 256)
                .itemPipeProperties(1024, 2)
                .fluidTemp(929)
                .buildAndRegister();

        Magnesite = new Material.Builder("magnesite")
                .dust().ore()
                .color(0xFAFAB4).iconSet(METALLIC)
                .components(Magnesium, 1, Carbon, 1, Oxygen, 3)
                .buildAndRegister();

        Magnetite = new Material.Builder("magnetite")
                .dust().ore()
                .color(0x1E1E1E).iconSet(METALLIC)
                .components(Iron, 3, Oxygen, 4)
                .buildAndRegister();

        Molybdenite = new Material.Builder("molybdenite")
                .dust().ore()
                .color(0x191919).iconSet(METALLIC)
                .components(Molybdenum, 1, Sulfur, 2)
                .buildAndRegister();

        Nichrome = new Material.Builder("nichrome")
                .ingot().fluid()
                .color(0xCDCEF6).iconSet(METALLIC)
                .appendFlags(EXT_METAL, GENERATE_SPRING)
                .components(Nickel, 4, Chromium, 1)
                .cableProperties(GTValues.V[4], 4, 4)
                .blastTemp(2700, GasTier.LOW, GTValues.VA[GTValues.HV], 1300)
                .fluidTemp(1818)
                .buildAndRegister();

        NiobiumNitride = new Material.Builder("niobium_nitride")
                .ingot().fluid()
                .color(0x1D291D)
                .appendFlags(EXT_METAL, GENERATE_FOIL)
                .components(Niobium, 1, Nitrogen, 1)
                .cableProperties(GTValues.V[6], 1, 1)
                .blastTemp(2846, GasTier.MID)
                .buildAndRegister();

        NiobiumTitanium = new Material.Builder("niobium_titanium")
                .ingot().fluid()
                .color(0x1D1D29)
                .appendFlags(EXT2_METAL, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FOIL, GENERATE_FINE_WIRE)
                .components(Niobium, 1, Titanium, 1)
                .fluidPipeProperties(5900, 175, true)
                .cableProperties(GTValues.V[6], 4, 2)
                .blastTemp(4500, GasTier.HIGH, GTValues.VA[GTValues.HV], 1500)
                .fluidTemp(2345)
                .buildAndRegister();

        Obsidian = new Material.Builder("obsidian")
                .dust(3)
                .color(0x503264)
                .flags(NO_SMASHING, EXCLUDE_BLOCK_CRAFTING_RECIPES, GENERATE_PLATE)
                .components(Magnesium, 1, Iron, 1, Silicon, 2, Oxygen, 4)
                .buildAndRegister();

        Phosphate = new Material.Builder("phosphate")
                .dust(1)
                .color(0xFFFF00)
                .flags(NO_SMASHING, NO_SMELTING, FLAMMABLE, EXPLOSIVE)
                .components(Phosphorus, 1, Oxygen, 4)
                .buildAndRegister();

        PlatinumRaw = new Material.Builder("platinum_raw")
                .dust()
                .color(0xFFFFC8).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Platinum, 1, Chlorine, 2)
                .buildAndRegister();

        SterlingSilver = new Material.Builder("sterling_silver")
                .ingot().fluid()
                .color(0xFADCE1).iconSet(SHINY)
                .appendFlags(EXT2_METAL)
                .components(Copper, 1, Silver, 4)
                .toolStats(ToolProperty.Builder.of(3.0F, 8.0F, 768, 2)
                        .attackSpeed(0.3F).enchantability(33)
                        .enchantment(Enchantments.SMITE, 3).build())
                .rotorStats(13.0f, 2.0f, 196)
                .itemPipeProperties(1024, 2)
                .blastTemp(1700, GasTier.LOW, GTValues.VA[GTValues.MV], 1000)
                .fluidTemp(1258)
                .buildAndRegister();

        RoseGold = new Material.Builder("rose_gold")
                .ingot().fluid()
                .color(0xFFE61E).iconSet(SHINY)
                .appendFlags(EXT2_METAL, GENERATE_RING)
                .components(Copper, 1, Gold, 4)
                .toolStats(ToolProperty.Builder.of(12.0F, 2.0F, 768, 2)
                        .enchantability(33)
                        .enchantment(Enchantments.BLOCK_FORTUNE, 2).build())
                .rotorStats(14.0f, 2.0f, 152)
                .itemPipeProperties(1024, 2)
                .blastTemp(1600, GasTier.LOW, GTValues.VA[GTValues.MV], 1000)
                .fluidTemp(1341)
                .buildAndRegister();

        BlackBronze = new Material.Builder("black_bronze")
                .ingot().fluid()
                .color(0x64327D)
                .appendFlags(EXT2_METAL, GENERATE_GEAR)
                .components(Gold, 1, Silver, 1, Copper, 3)
                .rotorStats(12.0f, 2.0f, 256)
                .itemPipeProperties(1024, 2)
                .blastTemp(2000, GasTier.LOW, GTValues.VA[GTValues.MV], 1000)
                .fluidTemp(1328)
                .buildAndRegister();

        BismuthBronze = new Material.Builder("bismuth_bronze")
                .ingot().fluid()
                .color(0x647D7D)
                .appendFlags(EXT2_METAL)
                .components(Bismuth, 1, Zinc, 1, Copper, 3)
                .rotorStats(8.0f, 3.0f, 256)
                .blastTemp(1100, GasTier.LOW, GTValues.VA[GTValues.MV], 1000)
                .fluidTemp(1036)
                .buildAndRegister();

        Biotite = new Material.Builder("biotite")
                .dust(1)
                .color(0x141E14).iconSet(METALLIC)
                .components(Potassium, 1, Magnesium, 3, Aluminium, 3, Fluorine, 2, Silicon, 3, Oxygen, 10)
                .buildAndRegister();

        Powellite = new Material.Builder("powellite")
                .dust().ore()
                .color(0xFFFF00)
                .components(Calcium, 1, Molybdenum, 1, Oxygen, 4)
                .buildAndRegister();

        Pyrite = new Material.Builder("pyrite")
                .dust(1).ore()
                .color(0x967828).iconSet(ROUGH)
                .flags(BLAST_FURNACE_CALCITE_DOUBLE)
                .components(Iron, 1, Sulfur, 2)
                .buildAndRegister();

        Pyrolusite = new Material.Builder("pyrolusite")
                .dust().ore()
                .color(0x9696AA)
                .components(Manganese, 1, Oxygen, 2)
                .buildAndRegister();

        Pyrope = new Material.Builder("pyrope")
                .gem().ore(3, 1)
                .color(0x783264).iconSet(RUBY)
                .components(Aluminium, 2, Magnesium, 3, Silicon, 3, Oxygen, 12)
                .buildAndRegister();

        RockSalt = new Material.Builder("rock_salt")
                .gem(1).ore(2, 1)
                .color(0xF0C8C8).iconSet(FINE)
                .flags(NO_SMASHING)
                .components(Potassium, 1, Chlorine, 1)
                .buildAndRegister();

        Ruridit = new Material.Builder("ruridit")
                .ingot(3)
                .colorAverage().iconSet(BRIGHT)
                .flags(GENERATE_FINE_WIRE, GENERATE_GEAR, GENERATE_LONG_ROD)
                .components(Ruthenium, 2, Iridium, 1)
                .blastTemp(4500, GasTier.HIGH, GTValues.VA[GTValues.EV], 1600)
                .buildAndRegister();

        Ruby = new Material.Builder("ruby")
                .gem().ore()
                .color(0xFF6464).iconSet(RUBY)
                .appendFlags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, GENERATE_LENS)
                .components(Chromium, 1, Aluminium, 2, Oxygen, 3)
                .buildAndRegister();

        Salt = new Material.Builder("salt")
                .gem(1).ore(2, 1)
                .color(0xFAFAFA).iconSet(FINE)
                .flags(NO_SMASHING)
                .components(Sodium, 1, Chlorine, 1)
                .buildAndRegister();

        Saltpeter = new Material.Builder("saltpeter")
                .dust(1).ore(2, 1)
                .color(0xE6E6E6).iconSet(FINE)
                .flags(NO_SMASHING, NO_SMELTING, FLAMMABLE)
                .components(Potassium, 1, Nitrogen, 1, Oxygen, 3)
                .buildAndRegister();

        Sapphire = new Material.Builder("sapphire")
                .gem().ore()
                .color(0x6464C8).iconSet(GEM_VERTICAL)
                .appendFlags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, GENERATE_LENS)
                .components(Aluminium, 2, Oxygen, 3)
                .buildAndRegister();

        Scheelite = new Material.Builder("scheelite")
                .dust(3).ore()
                .color(0xC88C14)
                .flags(DISABLE_DECOMPOSITION)
                .components(Calcium, 1, Tungsten, 1, Oxygen, 4)
                .buildAndRegister()
                .setFormula("Ca(WO3)O", true);

        Sodalite = new Material.Builder("sodalite")
                .gem(1).ore(6, 4)
                .color(0x1414FF).iconSet(LAPIS)
                .flags(GENERATE_PLATE, GENERATE_ROD, NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE, DECOMPOSITION_BY_ELECTROLYZING)
                .components(Aluminium, 3, Silicon, 3, Sodium, 4, Chlorine, 1)
                .buildAndRegister();

        AluminiumSulfite = new Material.Builder("aluminium_sulfite")
                .dust()
                .color(0xCC4BBB).iconSet(DULL)
                .components(Aluminium, 2, Sulfur, 3, Oxygen, 9)
                .buildAndRegister().setFormula("Al2(SO3)3", true);

        Tantalite = new Material.Builder("tantalite")
                .dust(3).ore()
                .color(0x915028).iconSet(METALLIC)
                .components(Manganese, 1, Tantalum, 2, Oxygen, 6)
                .buildAndRegister();

        Coke = new Material.Builder("coke")
                .gem(2, 3200) // 2x burn time of coal
                .color(0x666666).iconSet(LIGNITE)
                .flags(FLAMMABLE, NO_SMELTING, NO_SMASHING, MORTAR_GRINDABLE)
                .components(Carbon, 1)
                .buildAndRegister();

        SolderingAlloy = new Material.Builder("soldering_alloy")
                .ingot(1).fluid()
                .color(0x9696A0)
                .components(Tin, 6, Lead, 3, Antimony, 1)
                .fluidTemp(544)
                .buildAndRegister();

        Spessartine = new Material.Builder("spessartine")
                .gem().ore(3, 1)
                .color(0xFF6464).iconSet(RUBY)
                .components(Aluminium, 2, Manganese, 3, Silicon, 3, Oxygen, 12)
                .buildAndRegister();

        Sphalerite = new Material.Builder("sphalerite")
                .dust(1).ore()
                .color(0xFFFFFF)
                .flags(DISABLE_DECOMPOSITION)
                .components(Zinc, 1, Sulfur, 1)
                .buildAndRegister();

        StainlessSteel = new Material.Builder("stainless_steel")
                .ingot(3).fluid()
                .color(0xC8C8DC).iconSet(SHINY)
                .appendFlags(EXT2_METAL, GENERATE_ROTOR, GENERATE_SMALL_GEAR, GENERATE_FRAME, GENERATE_LONG_ROD, GENERATE_FOIL, GENERATE_GEAR)
                .components(Iron, 6, Chromium, 1, Manganese, 1, Nickel, 1)
                .toolStats(ToolProperty.Builder.of(7.0F, 5.0F, 1024, 3)
                        .enchantability(14).build())
                .rotorStats(7.0f, 4.0f, 480)
                .fluidPipeProperties(2428, 75, true, true, true, false)
                .blastTemp(1700, GasTier.LOW, GTValues.VA[GTValues.HV], 1100)
                .fluidTemp(2011)
                .buildAndRegister();

        Steel = new Material.Builder("steel")
                .ingot(3).fluid()
                .color(0x808080).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_ROTOR, GENERATE_SMALL_GEAR, GENERATE_SPRING,
                        GENERATE_SPRING_SMALL, GENERATE_FRAME, DISABLE_DECOMPOSITION, GENERATE_FINE_WIRE, GENERATE_GEAR)
                .components(Iron, 1)
                .toolStats(ToolProperty.Builder.of(5.0F, 3.0F, 512, 3)
                        .addTypes(GTToolType.MORTAR)
                        .enchantability(14).build())
                .rotorStats(6.0f, 3.0f, 512)
                .fluidPipeProperties(1855, 75, true)
                .cableProperties(GTValues.V[4], 2, 2)
                .blastTemp(1000, null, GTValues.VA[GTValues.MV], 800) // no gas tier for steel
                .fluidTemp(2046)
                .buildAndRegister();

        Stibnite = new Material.Builder("stibnite")
                .dust().ore()
                .color(0x464646).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Antimony, 2, Sulfur, 3)
                .buildAndRegister();

        // Free ID 326

        Tetrahedrite = new Material.Builder("tetrahedrite")
                .dust().ore()
                .color(0xC82000)
                .components(Copper, 3, Antimony, 1, Sulfur, 3, Iron, 1)
                .buildAndRegister();

        TinAlloy = new Material.Builder("tin_alloy")
                .ingot().fluid()
                .color(0xC8C8C8).iconSet(METALLIC)
                .appendFlags(EXT2_METAL)
                .components(Tin, 1, Iron, 1)
                .fluidPipeProperties(1572, 20, true)
                .fluidTemp(1258)
                .buildAndRegister();

        Topaz = new Material.Builder("topaz")
                .gem(3).ore()
                .color(0xFF8000).iconSet(GEM_HORIZONTAL)
                .appendFlags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .components(Aluminium, 2, Silicon, 1, Fluorine, 1, Hydrogen, 2)
                .buildAndRegister();

        Tungstate = new Material.Builder("tungstate")
                .dust(3).ore()
                .color(0x373223)
                .flags(DISABLE_DECOMPOSITION)
                .components(Tungsten, 1, Lithium, 2, Oxygen, 4)
                .buildAndRegister()
                .setFormula("Li2(WO3)O", true);

        Ultimet = new Material.Builder("ultimet")
                .ingot(4).fluid()
                .color(0xB4B4E6).iconSet(SHINY)
                .appendFlags(EXT2_METAL, GENERATE_GEAR)
                .components(Cobalt, 5, Chromium, 2, Nickel, 1, Molybdenum, 1)
                .toolStats(ToolProperty.Builder.of(10.0F, 7.0F, 2048, 4)
                        .attackSpeed(0.1F).enchantability(21).build())
                .rotorStats(9.0f, 4.0f, 2048)
                .itemPipeProperties(128, 16)
                .blastTemp(2700, GasTier.MID, GTValues.VA[GTValues.HV], 1300)
                .fluidTemp(1980)
                .buildAndRegister();

        Uraninite = new Material.Builder("uraninite")
                .dust(3).ore(true)
                .color(0x232323).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Uranium238, 1, Oxygen, 2)
                .buildAndRegister()
                .setFormula("UO2", true);

        Uvarovite = new Material.Builder("uvarovite")
                .gem()
                .color(0xB4ffB4).iconSet(RUBY)
                .components(Calcium, 3, Chromium, 2, Silicon, 3, Oxygen, 12)
                .buildAndRegister();

        VanadiumGallium = new Material.Builder("vanadium_gallium")
                .ingot().fluid()
                .color(0x80808C).iconSet(SHINY)
                .appendFlags(STD_METAL, GENERATE_FOIL, GENERATE_SPRING, GENERATE_SPRING_SMALL)
                .components(Vanadium, 3, Gallium, 1)
                .cableProperties(GTValues.V[7], 4, 2)
                .blastTemp(4500, GasTier.HIGH, GTValues.VA[GTValues.EV], 1200)
                .fluidTemp(1712)
                .buildAndRegister();

        WroughtIron = new Material.Builder("wrought_iron")
                .ingot().fluid()
                .color(0xC8B4B4).iconSet(METALLIC)
                .appendFlags(EXT_METAL, GENERATE_GEAR, GENERATE_FOIL, MORTAR_GRINDABLE, GENERATE_RING, GENERATE_LONG_ROD, GENERATE_BOLT_SCREW, DISABLE_DECOMPOSITION, BLAST_FURNACE_CALCITE_TRIPLE)
                .components(Iron, 1)
                .toolStats(ToolProperty.Builder.of(2.0F, 2.0F, 384, 2)
                        .addTypes(GTToolType.MORTAR)
                        .attackSpeed(-0.2F).enchantability(5).build())
                .rotorStats(6.0f, 3.5f, 384)
                .fluidTemp(2011)
                .buildAndRegister();
        Iron.getProperty(PropertyKey.INGOT).setSmeltingInto(WroughtIron);
        Iron.getProperty(PropertyKey.INGOT).setArcSmeltingInto(WroughtIron);

        Wulfenite = new Material.Builder("wulfenite")
                .dust(3).ore()
                .color(0xFF8000)
                .components(Lead, 1, Molybdenum, 1, Oxygen, 4)
                .buildAndRegister();

        YellowLimonite = new Material.Builder("yellow_limonite")
                .dust().ore()
                .color(0xC8C800).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_CENTRIFUGING, BLAST_FURNACE_CALCITE_DOUBLE)
                .components(Iron, 1, Hydrogen, 1, Oxygen, 2)
                .buildAndRegister();

        YttriumBariumCuprate = new Material.Builder("yttrium_barium_cuprate")
                .ingot().fluid()
                .color(0x504046).iconSet(METALLIC)
                .appendFlags(EXT_METAL, GENERATE_FINE_WIRE, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FOIL, GENERATE_BOLT_SCREW)
                .components(Yttrium, 1, Barium, 2, Copper, 3, Oxygen, 7)
                .cableProperties(GTValues.V[8], 4, 4)
                .blastTemp(4500, GasTier.HIGH) // todo redo this EBF process
                .fluidTemp(1799)
                .buildAndRegister();

        NetherQuartz = new Material.Builder("nether_quartz")
                .gem(1).ore(2, 1)
                .color(0xE6D2D2).iconSet(QUARTZ)
                .flags(GENERATE_PLATE, NO_SMELTING, CRYSTALLIZABLE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, DISABLE_DECOMPOSITION)
                .components(Silicon, 1, Oxygen, 2)
                .buildAndRegister();

        CertusQuartz = new Material.Builder("certus_quartz")
                .gem(1).ore(2, 1)
                .color(0xD2D2E6).iconSet(CERTUS)
                .flags(GENERATE_PLATE, NO_SMELTING, CRYSTALLIZABLE, DISABLE_DECOMPOSITION)
                .components(Silicon, 1, Oxygen, 2)
                .buildAndRegister();

        Quartzite = new Material.Builder("quartzite")
                .gem(1).ore(2, 1)
                .color(0xD2E6D2).iconSet(QUARTZ)
                .flags(NO_SMELTING, CRYSTALLIZABLE, DISABLE_DECOMPOSITION, GENERATE_PLATE)
                .components(Silicon, 1, Oxygen, 2)
                .buildAndRegister();

        Graphite = new Material.Builder("graphite")
                .ore()
                .color(0x808080)
                .flags(NO_SMELTING, FLAMMABLE, DISABLE_DECOMPOSITION)
                .components(Carbon, 1)
                .buildAndRegister();

        Graphene = new Material.Builder("graphene")
                .dust().ingot()
                .color(0x808080).iconSet(SHINY)
                .flags(DISABLE_DECOMPOSITION, GENERATE_FOIL)
                .components(Carbon, 1)
                .cableProperties(GTValues.V[5], 1, 1)
                .buildAndRegister();

        TungsticAcid = new Material.Builder("tungstic_acid")
                .dust()
                .color(0xBCC800).iconSet(SHINY)
                .flags(DISABLE_DECOMPOSITION)
                .components(Hydrogen, 2, Tungsten, 1, Oxygen, 4)
                .buildAndRegister();

        Osmiridium = new Material.Builder("osmiridium")
                .ingot(3).fluid()
                .color(0x6464FF).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_ROTOR, GENERATE_ROUND, GENERATE_FINE_WIRE, GENERATE_GEAR)
                .components(Iridium, 3, Osmium, 1)
                .rotorStats(9.0f, 3.0f, 3152)
                .itemPipeProperties(64, 32)
                .blastTemp(4500, GasTier.HIGH, GTValues.VA[GTValues.LuV], 900)
                .fluidTemp(3012)
                .buildAndRegister();

        LithiumChloride = new Material.Builder("lithium_chloride")
                .dust()
                .color(0xDEDEFA).iconSet(FINE)
                .components(Lithium, 1, Chlorine, 1)
                .buildAndRegister();

        CalciumChloride = new Material.Builder("calcium_chloride")
                .dust()
                .color(0xEBEBFA).iconSet(FINE)
                .components(Calcium, 1, Chlorine, 2)
                .buildAndRegister();

        Bornite = new Material.Builder("bornite")
                .dust(1).ore()
                .color(0x97662B).iconSet(METALLIC)
                .components(Copper, 5, Iron, 1, Sulfur, 4)
                .buildAndRegister();

        Chalcocite = new Material.Builder("chalcocite")
                .dust().ore()
                .color(0x353535).iconSet(GEM_VERTICAL)
                .components(Copper, 2, Sulfur, 1)
                .buildAndRegister();

        // Free ID 349

        // Free ID 350

        GalliumArsenide = new Material.Builder("gallium_arsenide")
                .ingot(1).fluid()
                .color(0xA0A0A0)
                .appendFlags(STD_METAL, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Arsenic, 1, Gallium, 1)
                .blastTemp(1200, GasTier.LOW, GTValues.VA[GTValues.MV], 1200)
                .fluidTemp(1511)
                .buildAndRegister();

        Potash = new Material.Builder("potash")
                .dust(1)
                .color(0x784137)
                .components(Potassium, 2, Oxygen, 1)
                .buildAndRegister();

        SodaAsh = new Material.Builder("soda_ash")
                .dust(1)
                .color(0xDCDCFF)
                .components(Sodium, 2, Carbon, 1, Oxygen, 3)
                .buildAndRegister();

        IndiumGalliumPhosphide = new Material.Builder("indium_gallium_phosphide")
                .ingot(1).fluid()
                .color(0xA08CBE)
                .appendFlags(STD_METAL, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Indium, 1, Gallium, 1, Phosphorus, 1)
                .fluidTemp(350)
                .buildAndRegister();

        NickelZincFerrite = new Material.Builder("nickel_zinc_ferrite")
                .ingot(0).fluid()
                .color(0x3C3C3C).iconSet(METALLIC)
                .flags(GENERATE_RING)
                .components(Nickel, 1, Zinc, 1, Iron, 4, Oxygen, 8)
                .fluidTemp(1410)
                .buildAndRegister();

        SiliconDioxide = new Material.Builder("silicon_dioxide")
                .dust(1)
                .color(0xC8C8C8).iconSet(QUARTZ)
                .flags(NO_SMASHING, NO_SMELTING)
                .components(Silicon, 1, Oxygen, 2)
                .buildAndRegister();

        MagnesiumChloride = new Material.Builder("magnesium_chloride")
                .dust(1)
                .color(0xD40D5C)
                .components(Magnesium, 1, Chlorine, 2)
                .buildAndRegister();

        SodiumSulfide = new Material.Builder("sodium_sulfide")
                .dust(1)
                .color(0xFFE680)
                .components(Sodium, 2, Sulfur, 1)
                .buildAndRegister();

        PhosphorusPentoxide = new Material.Builder("phosphorus_pentoxide")
                .dust(1)
                .color(0xDCDC00)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Phosphorus, 4, Oxygen, 10)
                .buildAndRegister();

        Quicklime = new Material.Builder("quicklime")
                .dust(1)
                .color(0xF0F0F0)
                .components(Calcium, 1, Oxygen, 1)
                .buildAndRegister();

        SodiumBisulfate = new Material.Builder("sodium_bisulfate")
                .dust(1)
                .color(0x004455)
                .flags(DISABLE_DECOMPOSITION)
                .components(Sodium, 1, Hydrogen, 1, Sulfur, 1, Oxygen, 4)
                .buildAndRegister();

        FerriteMixture = new Material.Builder("ferrite_mixture")
                .dust(1)
                .color(0xB4B4B4).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Nickel, 1, Zinc, 1, Iron, 4)
                .buildAndRegister();

        Magnesia = new Material.Builder("magnesia")
                .dust(1)
                .color(0x887878)
                .components(Magnesium, 1, Oxygen, 1)
                .buildAndRegister();

        PlatinumGroupSludge = new Material.Builder("platinum_group_sludge")
                .dust(1)
                .color(0x001E00).iconSet(FINE)
                .flags(DISABLE_DECOMPOSITION)
                .buildAndRegister();

        Realgar = new Material.Builder("realgar")
                .gem().ore()
                .color(0x9D2123).iconSet(EMERALD)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Arsenic, 4, Sulfur, 4)
                .buildAndRegister();

        SodiumBicarbonate = new Material.Builder("sodium_bicarbonate")
                .dust(1)
                .color(0x565b96).iconSet(ROUGH)
                .components(Sodium, 1, Hydrogen, 1, Carbon, 1, Oxygen, 3)
                .buildAndRegister();

        PotassiumDichromate = new Material.Builder("potassium_dichromate")
                .dust(1)
                .color(0xFF084E)
                .components(Potassium, 2, Chromium, 2, Oxygen, 7)
                .buildAndRegister();

        ChromiumTrioxide = new Material.Builder("chromium_trioxide")
                .dust(1)
                .color(0xFFE4E1)
                .components(Chromium, 1, Oxygen, 3)
                .buildAndRegister();

        AntimonyTrioxide = new Material.Builder("antimony_trioxide")
                .dust(1)
                .color(0xE6E6F0)
                .components(Antimony, 2, Oxygen, 3)
                .buildAndRegister();

        Zincite = new Material.Builder("zincite")
                .dust(1)
                .color(0xFFFFF5)
                .components(Zinc, 1, Oxygen, 1)
                .buildAndRegister();

        CupricOxide = new Material.Builder("cupric_oxide")
                .dust(1)
                .color(0x0F0F0F)
                .components(Copper, 1, Oxygen, 1)
                .buildAndRegister();

        CobaltOxide = new Material.Builder("cobalt_oxide")
                .dust(1)
                .color(0x788000)
                .components(Cobalt, 1, Oxygen, 1)
                .buildAndRegister();

        ArsenicTrioxide = new Material.Builder("arsenic_trioxide")
                .dust(1)
                .iconSet(ROUGH)
                .components(Arsenic, 2, Oxygen, 3)
                .buildAndRegister();

        Massicot = new Material.Builder("massicot")
                .dust(1)
                .color(0xFFDD55)
                .components(Lead, 1, Oxygen, 1)
                .buildAndRegister();

        Ferrosilite = new Material.Builder("ferrosilite")
                .dust(1)
                .color(0x97632A)
                .components(Iron, 1, Silicon, 1, Oxygen, 3)
                .buildAndRegister();

        MetalMixture = new Material.Builder("metal_mixture")
                .dust(1)
                .color(0x502d16).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .buildAndRegister();

        SodiumHydroxide = new Material.Builder("sodium_hydroxide")
                .dust(1)
                .color(0x003380)
                .flags(DISABLE_DECOMPOSITION)
                .components(Sodium, 1, Oxygen, 1, Hydrogen, 1)
                .buildAndRegister();

        SodiumPersulfate = new Material.Builder("sodium_persulfate")
                .fluid().fluidCustomTexture()
                .components(Sodium, 2, Sulfur, 2, Oxygen, 8)
                .buildAndRegister();

        Bastnasite = new Material.Builder("bastnasite")
                .dust().ore(2, 1)
                .color(0xC86E2D).iconSet(FINE)
                .components(Cerium, 1, Carbon, 1, Fluorine, 1, Oxygen, 3)
                .buildAndRegister();

        Pentlandite = new Material.Builder("pentlandite")
                .dust().ore()
                .color(0xA59605)
                .components(Nickel, 9, Sulfur, 8)
                .buildAndRegister();

        Spodumene = new Material.Builder("spodumene")
                .dust().ore()
                .color(0xBEAAAA)
                .components(Lithium, 1, Aluminium, 1, Silicon, 2, Oxygen, 6)
                .buildAndRegister();

        Lepidolite = new Material.Builder("lepidolite")
                .dust().ore(2, 1)
                .color(0xF0328C).iconSet(FINE)
                .components(Potassium, 1, Lithium, 3, Aluminium, 4, Fluorine, 2, Oxygen, 10)
                .buildAndRegister();

        // Free ID 383

        GlauconiteSand = new Material.Builder("glauconite_sand")
                .dust().ore(3, 1)
                .color(0x82B43C).iconSet(SAND)
                .components(Potassium, 1, Magnesium, 2, Aluminium, 4, Hydrogen, 2, Oxygen, 12)
                .buildAndRegister();

        Malachite = new Material.Builder("malachite")
                .gem().ore()
                .color(0x055F05).iconSet(LAPIS)
                .components(Copper, 2, Carbon, 1, Hydrogen, 2, Oxygen, 5)
                .buildAndRegister();

        Mica = new Material.Builder("mica")
                .dust().ore(2, 1)
                .color(0xC3C3CD).iconSet(FINE)
                .components(Potassium, 1, Aluminium, 3, Silicon, 3, Fluorine, 2, Oxygen, 10)
                .buildAndRegister();

        Barite = new Material.Builder("barite")
                .dust().ore()
                .color(0xE6EBEB)
                .components(Barium, 1, Sulfur, 1, Oxygen, 4)
                .buildAndRegister();

        Alunite = new Material.Builder("alunite")
                .dust().ore(3, 1)
                .color(0xE1B441).iconSet(METALLIC)
                .components(Potassium, 1, Aluminium, 3, Silicon, 2, Hydrogen, 6, Oxygen, 14)
                .buildAndRegister();

        // Free ID 389

        // Free ID 390

        // Free ID 391

        Talc = new Material.Builder("talc")
                .dust().ore(2, 1)
                .color(0x5AB45A).iconSet(FINE)
                .components(Magnesium, 3, Silicon, 4, Hydrogen, 2, Oxygen, 12)
                .buildAndRegister();

        Soapstone = new Material.Builder("soapstone")
                .dust(1).ore(3, 1)
                .color(0x5F915F)
                .components(Magnesium, 3, Silicon, 4, Hydrogen, 2, Oxygen, 12)
                .buildAndRegister();

        Kyanite = new Material.Builder("kyanite")
                .dust().ore()
                .color(0x6E6EFA).iconSet(FLINT)
                .components(Aluminium, 2, Silicon, 1, Oxygen, 5)
                .buildAndRegister();

        IronMagnetic = new Material.Builder("magnetic_iron")
                .ingot()
                .color(0xC8C8C8).iconSet(MAGNETIC)
                .flags(GENERATE_BOLT_SCREW, IS_MAGNETIC)
                .components(Iron, 1)
                .ingotSmeltInto(Iron)
                .arcSmeltInto(WroughtIron)
                .macerateInto(Iron)
                .buildAndRegister();
        Iron.getProperty(PropertyKey.INGOT).setMagneticMaterial(IronMagnetic);

        TungstenCarbide = new Material.Builder("tungsten_carbide")
                .ingot(4).fluid()
                .color(0x330066).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, GENERATE_FOIL, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_FRAME, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Tungsten, 1, Carbon, 1)
                .toolStats(ToolProperty.Builder.of(60.0F, 2.0F, 1024, 4)
                        .enchantability(21).build())
                .rotorStats(12.0f, 4.0f, 1280)
                .fluidPipeProperties(3837, 200, true)
                .blastTemp(3058, GasTier.MID, GTValues.VA[GTValues.HV], 1500)
                .buildAndRegister();

        CarbonDioxide = new Material.Builder("carbon_dioxide")
                .fluid(FluidTypes.GAS)
                .color(0xA9D0F5)
                .components(Carbon, 1, Oxygen, 2)
                .buildAndRegister();

        TitaniumTetrachloride = new Material.Builder("titanium_tetrachloride")
                .fluid().fluidCustomTexture()
                .color(0xD40D5C)
                .flags(DISABLE_DECOMPOSITION)
                .components(Titanium, 1, Chlorine, 4)
                .buildAndRegister();

        NitrogenDioxide = new Material.Builder("nitrogen_dioxide")
                .fluid(FluidTypes.GAS)
                .color(0x85FCFF).iconSet(GAS)
                .components(Nitrogen, 1, Oxygen, 2)
                .buildAndRegister();

        HydrogenSulfide = new Material.Builder("hydrogen_sulfide")
                .fluid(FluidTypes.GAS).fluidCustomTexture()
                .components(Hydrogen, 2, Sulfur, 1)
                .buildAndRegister();

        NitricAcid = new Material.Builder("nitric_acid")
                .fluid(FluidTypes.ACID)
                .color(0xCCCC00)
                .flags(DISABLE_DECOMPOSITION)
                .components(Hydrogen, 1, Nitrogen, 1, Oxygen, 3)
                .buildAndRegister();

        SulfuricAcid = new Material.Builder("sulfuric_acid")
                .fluid(FluidTypes.ACID).fluidCustomTexture()
                .flags(DISABLE_DECOMPOSITION)
                .components(Hydrogen, 2, Sulfur, 1, Oxygen, 4)
                .buildAndRegister();

        PhosphoricAcid = new Material.Builder("phosphoric_acid")
                .fluid(FluidTypes.ACID)
                .color(0xDCDC01)
                .flags(DISABLE_DECOMPOSITION)
                .components(Hydrogen, 3, Phosphorus, 1, Oxygen, 4)
                .buildAndRegister();

        SulfurTrioxide = new Material.Builder("sulfur_trioxide")
                .fluid(FluidTypes.GAS)
                .color(0xA0A014)
                .components(Sulfur, 1, Oxygen, 3)
                .buildAndRegister();

        SulfurDioxide = new Material.Builder("sulfur_dioxide")
                .fluid(FluidTypes.GAS)
                .color(0xC8C819)
                .components(Sulfur, 1, Oxygen, 2)
                .buildAndRegister();

        CarbonMonoxide = new Material.Builder("carbon_monoxide")
                .fluid(FluidTypes.GAS)
                .color(0x0E4880)
                .components(Carbon, 1, Oxygen, 1)
                .buildAndRegister();

        HypochlorousAcid = new Material.Builder("hypochlorous_acid")
                .fluid(FluidTypes.ACID)
                .color(0x6F8A91)
                .components(Hydrogen, 1, Chlorine, 1, Oxygen, 1)
                .buildAndRegister();

        Ammonia = new Material.Builder("ammonia")
                .fluid(FluidTypes.GAS)
                .color(0x3F3480)
                .components(Nitrogen, 1, Hydrogen, 3)
                .buildAndRegister();

        HydrofluoricAcid = new Material.Builder("hydrofluoric_acid")
                .fluid(FluidTypes.ACID)
                .color(0x0088AA)
                .components(Hydrogen, 1, Fluorine, 1)
                .buildAndRegister();

        NitricOxide = new Material.Builder("nitric_oxide")
                .fluid(FluidTypes.GAS)
                .color(0x7DC8F0)
                .components(Nitrogen, 1, Oxygen, 1)
                .buildAndRegister();

        Iron3Chloride = new Material.Builder("iron_iii_chloride")
                .fluid()
                .color(0x060B0B)
                .flags(DECOMPOSITION_BY_ELECTROLYZING)
                .components(Iron, 1, Chlorine, 3)
                .buildAndRegister();

        UraniumHexafluoride = new Material.Builder("uranium_hexafluoride")
                .fluid(FluidTypes.GAS)
                .color(0x42D126)
                .flags(DISABLE_DECOMPOSITION)
                .components(Uranium238, 1, Fluorine, 6)
                .buildAndRegister()
                .setFormula("UF6", true);

        EnrichedUraniumHexafluoride = new Material.Builder("enriched_uranium_hexafluoride")
                .fluid(FluidTypes.GAS)
                .color(0x4BF52A)
                .flags(DISABLE_DECOMPOSITION)
                .components(Uranium235, 1, Fluorine, 6)
                .buildAndRegister();

        DepletedUraniumHexafluoride = new Material.Builder("depleted_uranium_hexafluoride")
                .fluid(FluidTypes.GAS)
                .color(0x74BA66)
                .flags(DISABLE_DECOMPOSITION)
                .components(Uranium238, 1, Fluorine, 6)
                .buildAndRegister();

        NitrousOxide = new Material.Builder("nitrous_oxide")
                .fluid(FluidTypes.GAS)
                .color(0x7DC8FF)
                .components(Nitrogen, 2, Oxygen, 1)
                .buildAndRegister();

        EnderPearl = new Material.Builder("ender_pearl")
                .gem(1)
                .color(0x6CDCC8)
                .flags(NO_SMASHING, NO_SMELTING, GENERATE_PLATE)
                .components(Beryllium, 1, Potassium, 4, Nitrogen, 5)
                .buildAndRegister();

        PotassiumFeldspar = new Material.Builder("potassium_feldspar")
                .dust(1)
                .color(0x782828).iconSet(FINE)
                .components(Potassium, 1, Aluminium, 1, Silicon, 1, Oxygen, 8)
                .buildAndRegister();

        NeodymiumMagnetic = new Material.Builder("magnetic_neodymium")
                .ingot()
                .color(0x646464).iconSet(MAGNETIC)
                .flags(GENERATE_ROD, IS_MAGNETIC)
                .components(Neodymium, 1)
                .ingotSmeltInto(Neodymium)
                .arcSmeltInto(Neodymium)
                .macerateInto(Neodymium)
                .buildAndRegister();
        Neodymium.getProperty(PropertyKey.INGOT).setMagneticMaterial(NeodymiumMagnetic);

        HydrochloricAcid = new Material.Builder("hydrochloric_acid")
                .fluid(FluidTypes.ACID).fluidCustomTexture()
                .components(Hydrogen, 1, Chlorine, 1)
                .buildAndRegister();

        Steam = new Material.Builder("steam")
                .fluid(FluidTypes.GAS, true).fluidCustomTexture()
                .flags(DISABLE_DECOMPOSITION)
                .components(Hydrogen, 2, Oxygen, 1)
                .fluidTemp(373)
                .buildAndRegister();

        DistilledWater = new Material.Builder("distilled_water")
                .fluid()
                .color(0x4A94FF)
                .flags(DISABLE_DECOMPOSITION)
                .components(Hydrogen, 2, Oxygen, 1)
                .buildAndRegister();

        SodiumPotassium = new Material.Builder("sodium_potassium")
                .fluid()
                .color(0x64FCB4)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Sodium, 1, Potassium, 1)
                .buildAndRegister();

        SamariumMagnetic = new Material.Builder("magnetic_samarium")
                .ingot()
                .color(0xFFFFCD).iconSet(MAGNETIC)
                .flags(GENERATE_LONG_ROD, IS_MAGNETIC)
                .components(Samarium, 1)
                .ingotSmeltInto(Samarium)
                .arcSmeltInto(Samarium)
                .macerateInto(Samarium)
                .buildAndRegister();
        Samarium.getProperty(PropertyKey.INGOT).setMagneticMaterial(SamariumMagnetic);

        ManganesePhosphide = new Material.Builder("manganese_phosphide")
                .ingot().fluid()
                .color(0xE1B454).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_ELECTROLYZING)
                .components(Manganese, 1, Phosphorus, 1)
                .cableProperties(GTValues.V[GTValues.LV], 2, 0, true, 78)
                .blastTemp(1200, GasTier.LOW)
                .fluidTemp(1368)
                .buildAndRegister();

        MagnesiumDiboride = new Material.Builder("magnesium_diboride")
                .ingot().fluid()
                .color(0x331900).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_ELECTROLYZING)
                .components(Magnesium, 1, Boron, 2)
                .cableProperties(GTValues.V[GTValues.MV], 4, 0, true, 78)
                .blastTemp(2500, GasTier.LOW, GTValues.VA[GTValues.HV], 1000)
                .fluidTemp(1103)
                .buildAndRegister();

        MercuryBariumCalciumCuprate = new Material.Builder("mercury_barium_calcium_cuprate")
                .ingot().fluid()
                .color(0x555555).iconSet(SHINY)
                .flags(DECOMPOSITION_BY_ELECTROLYZING)
                .components(Mercury, 1, Barium, 2, Calcium, 2, Copper, 3, Oxygen, 8)
                .cableProperties(GTValues.V[GTValues.HV], 4, 0, true, 78)
                .blastTemp(3300, GasTier.LOW, GTValues.VA[GTValues.HV], 1500)
                .fluidTemp(1075)
                .buildAndRegister();

        UraniumTriplatinum = new Material.Builder("uranium_triplatinum")
                .ingot().fluid()
                .color(0x008700).iconSet(SHINY)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Uranium238, 1, Platinum, 3)
                .cableProperties(GTValues.V[GTValues.EV], 6, 0, true, 30)
                .blastTemp(4400, GasTier.MID, GTValues.VA[GTValues.EV], 1000)
                .fluidTemp(1882)
                .buildAndRegister()
                .setFormula("UPt3", true);

        SamariumIronArsenicOxide = new Material.Builder("samarium_iron_arsenic_oxide")
                .ingot().fluid()
                .color(0x330033).iconSet(SHINY)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Samarium, 1, Iron, 1, Arsenic, 1, Oxygen, 1)
                .cableProperties(GTValues.V[GTValues.IV], 6, 0, true, 30)
                .blastTemp(5200, GasTier.MID, GTValues.VA[GTValues.EV], 1500)
                .fluidTemp(1347)
                .buildAndRegister();

        IndiumTinBariumTitaniumCuprate = new Material.Builder("indium_tin_barium_titanium_cuprate")
                .ingot().fluid()
                .color(0x994C00).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_ELECTROLYZING, GENERATE_FINE_WIRE)
                .components(Indium, 4, Tin, 2, Barium, 2, Titanium, 1, Copper, 7, Oxygen, 14)
                .cableProperties(GTValues.V[GTValues.LuV], 8, 0, true, 5)
                .blastTemp(6000, GasTier.HIGH, GTValues.VA[GTValues.IV], 1000)
                .fluidTemp(1012)
                .buildAndRegister();

        UraniumRhodiumDinaquadide = new Material.Builder("uranium_rhodium_dinaquadide")
                .ingot().fluid()
                .color(0x0A0A0A)
                .flags(DECOMPOSITION_BY_CENTRIFUGING, GENERATE_FINE_WIRE)
                .components(Uranium238, 1, Rhodium, 1, Naquadah, 2)
                .cableProperties(GTValues.V[GTValues.ZPM], 8, 0, true, 5)
                .blastTemp(9000, GasTier.HIGH, GTValues.VA[GTValues.IV], 1500)
                .fluidTemp(3410)
                .buildAndRegister()
                .setFormula("URhNq2", true);

        EnrichedNaquadahTriniumEuropiumDuranide = new Material.Builder("enriched_naquadah_trinium_europium_duranide")
                .ingot().fluid()
                .color(0x7D9673).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_CENTRIFUGING, GENERATE_FINE_WIRE)
                .components(NaquadahEnriched, 4, Trinium, 3, Europium, 2, Duranium, 1)
                .cableProperties(GTValues.V[GTValues.UV], 16, 0, true, 3)
                .blastTemp(9900, GasTier.HIGH, GTValues.VA[GTValues.LuV], 1000)
                .fluidTemp(5930)
                .buildAndRegister();

        RutheniumTriniumAmericiumNeutronate = new Material.Builder("ruthenium_trinium_americium_neutronate")
                .ingot().fluid()
                .color(0xFFFFFF).iconSet(BRIGHT)
                .flags(DECOMPOSITION_BY_ELECTROLYZING)
                .components(Ruthenium, 1, Trinium, 2, Americium, 1, Neutronium, 2, Oxygen, 8)
                .cableProperties(GTValues.V[GTValues.UHV], 24, 0, true, 3)
                .blastTemp(10800, GasTier.HIGHER)
                .fluidTemp(23691)
                .buildAndRegister();

        InertMetalMixture = new Material.Builder("inert_metal_mixture")
                .dust()
                .color(0xE2AE72).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Rhodium, 1, Ruthenium, 1, Oxygen, 4)
                .buildAndRegister();

        RhodiumSulfate = new Material.Builder("rhodium_sulfate")
                .fluid()
                .color(0xEEAA55)
                .flags(DISABLE_DECOMPOSITION)
                .components(Rhodium, 2, Sulfur, 3, Oxygen, 12)
                .fluidTemp(1128)
                .buildAndRegister().setFormula("Rh2(SO4)3", true);

        RutheniumTetroxide = new Material.Builder("ruthenium_tetroxide")
                .dust()
                .color(0xC7C7C7)
                .flags(DISABLE_DECOMPOSITION)
                .components(Ruthenium, 1, Oxygen, 4)
                .buildAndRegister();

        OsmiumTetroxide = new Material.Builder("osmium_tetroxide")
                .dust()
                .color(0xACAD71).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Osmium, 1, Oxygen, 4)
                .buildAndRegister();

        IridiumChloride = new Material.Builder("iridium_chloride")
                .dust()
                .color(0x013220).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Iridium, 1, Chlorine, 3)
                .buildAndRegister();

        FluoroantimonicAcid = new Material.Builder("fluoroantimonic_acid")
                .fluid(FluidTypes.ACID).fluidCustomTexture()
                .components(Hydrogen, 2, Antimony, 1, Fluorine, 7)
                .buildAndRegister();

        TitaniumTrifluoride = new Material.Builder("titanium_trifluoride")
                .dust()
                .color(0x8F00FF).iconSet(SHINY)
                .flags(DISABLE_DECOMPOSITION)
                .components(Titanium, 1, Fluorine, 3)
                .buildAndRegister();

        CalciumPhosphide = new Material.Builder("calcium_phosphide")
                .dust()
                .color(0xA52A2A).iconSet(METALLIC)
                .components(Calcium, 1, Phosphorus, 1)
                .buildAndRegister();

        IndiumPhosphide = new Material.Builder("indium_phosphide")
                .dust()
                .color(0x582E5C).iconSet(SHINY)
                .flags(DISABLE_DECOMPOSITION)
                .components(Indium, 1, Phosphorus, 1)
                .buildAndRegister();

        BariumSulfide = new Material.Builder("barium_sulfide")
                .dust()
                .color(0xF0EAD6).iconSet(METALLIC)
                .components(Barium, 1, Sulfur, 1)
                .buildAndRegister();

        TriniumSulfide = new Material.Builder("trinium_sulfide")
                .dust()
                .color(0xE68066).iconSet(SHINY)
                .flags(DISABLE_DECOMPOSITION)
                .components(Trinium, 1, Sulfur, 1)
                .buildAndRegister();

        ZincSulfide = new Material.Builder("zinc_sulfide")
                .dust()
                .color(0xFFFFF6).iconSet(DULL)
                .components(Zinc, 1, Sulfur, 1)
                .buildAndRegister();

        GalliumSulfide = new Material.Builder("gallium_sulfide")
                .dust()
                .color(0xFFF59E).iconSet(SHINY)
                .components(Gallium, 1, Sulfur, 1)
                .buildAndRegister();

        AntimonyTrifluoride = new Material.Builder("antimony_trifluoride")
                .dust()
                .color(0xF7EABC).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Antimony, 1, Fluorine, 3)
                .buildAndRegister();

        EnrichedNaquadahSulfate = new Material.Builder("enriched_naquadah_sulfate")
                .dust()
                .color(0x2E2E1C).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(NaquadahEnriched, 1, Sulfur, 1, Oxygen, 4)
                .buildAndRegister();

        NaquadriaSulfate = new Material.Builder("naquadria_sulfate")
                .dust()
                .color(0x006633).iconSet(SHINY)
                .flags(DISABLE_DECOMPOSITION)
                .components(Naquadria, 1, Sulfur, 1, Oxygen, 4)
                .buildAndRegister();

        Pyrochlore = new Material.Builder("pyrochlore")
                .dust().ore()
                .color(0x2B1100).iconSet(METALLIC)
                .flags()
                .components(Calcium, 2, Niobium, 2, Oxygen, 7)
                .buildAndRegister();

        LiquidHelium = new Material.Builder("liquid_helium")
                .fluid()
                .color(0xFCFF90)
                .flags(DISABLE_DECOMPOSITION)
                .components(Helium, 1)
                .fluidTemp(4)
                .buildAndRegister();
    }
}
