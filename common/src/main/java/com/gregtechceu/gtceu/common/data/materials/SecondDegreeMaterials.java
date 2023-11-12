package com.gregtechceu.gtceu.common.data.materials;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty.GasTier;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttributes;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import net.minecraft.world.item.enchantment.Enchantments;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class SecondDegreeMaterials {

    public static void register() {

        Glass = new Material.Builder("glass")
                .gem(0)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder()
                        .temperature(1200)
                        .customStill())
                .color(0xFAFAFA).iconSet(GLASS)
                .flags(GENERATE_LENS, NO_SMASHING, EXCLUDE_BLOCK_CRAFTING_RECIPES, DECOMPOSITION_BY_CENTRIFUGING)
                .components(SiliconDioxide, 1)
                .buildAndRegister();

        Perlite = new Material.Builder("perlite")
                .dust(1)
                .color(0x1E141E)
                .components(Obsidian, 2, Water, 1)
                .buildAndRegister();

        Borax = new Material.Builder("borax")
                .dust(1)
                .color(0xFAFAFA).secondaryColor(0xd7e7e7).iconSet(FINE)
                .components(Sodium, 2, Boron, 4, Water, 10, Oxygen, 7)
                .buildAndRegister();

        SaltWater = new Material.Builder("salt_water")
                .fluid()
                .color(0x0000C8)
                .flags(DISABLE_DECOMPOSITION)
                .components(Salt, 1, Water, 1)
                .buildAndRegister();

        Olivine = new Material.Builder("olivine")
                .gem().ore(2, 1)
                .color(0x96FF96).iconSet(RUBY)
                .appendFlags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .components(Magnesium, 2, Iron, 1, SiliconDioxide, 2)
                .buildAndRegister();

        Opal = new Material.Builder("opal")
                .gem().ore()
                .color(0x0000FF).iconSet(OPAL)
                .appendFlags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, DECOMPOSITION_BY_CENTRIFUGING)
                .components(SiliconDioxide, 1)
                .buildAndRegister();

        Amethyst = new Material.Builder("amethyst")
                .gem(3).ore()
                .color(0x8464BC).iconSet(RUBY)
                .appendFlags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .components(SiliconDioxide, 4, Iron, 1)
                .buildAndRegister();

        Lapis = new Material.Builder("lapis")
                .gem(1).ore(6, 4)
                .color(0x4646DC).iconSet(LAPIS)
                .flags(NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE, NO_WORKING, DECOMPOSITION_BY_ELECTROLYZING, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES,
                        GENERATE_PLATE, GENERATE_ROD)
                .components(Lazurite, 12, Sodalite, 2, Pyrite, 1, Calcite, 1)
                .buildAndRegister();

        Blaze = new Material.Builder("blaze")
                .dust(1)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder()
                        .temperature(4000)
                        .customStill())
                .color(0xfff94d, false).secondaryColor(0xff330c).iconSet(FINE)
                .flags(NO_SMELTING, MORTAR_GRINDABLE, DECOMPOSITION_BY_CENTRIFUGING) //todo burning flag
                .components(DarkAsh, 1, Sulfur, 1)
                .buildAndRegister();

        // Free ID 2009

        Apatite = new Material.Builder("apatite")
                .gem(1).ore(4, 2)
                .color(0xC8C8FF).iconSet(DIAMOND)
                .flags(NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE, GENERATE_BOLT_SCREW, DISABLE_DECOMPOSITION)
                .components(Calcium, 5, Phosphate, 3, Chlorine, 1)
                .buildAndRegister();

        BlackSteel = new Material.Builder("black_steel")
                .ingot().fluid()
                .color(0x666666).secondaryColor(0x1a120e).iconSet(METALLIC)
                .appendFlags(EXT_METAL, GENERATE_FINE_WIRE, GENERATE_GEAR, GENERATE_FRAME)
                .components(Nickel, 1, BlackBronze, 1, Steel, 3)
                .cableProperties(GTValues.V[4], 3, 2)
                .blastTemp(1200, GasTier.LOW)
                .buildAndRegister();

        DamascusSteel = new Material.Builder("damascus_steel")
                .ingot(3).fluid()
                .color(0x6E6E6E).secondaryColor(0x302222).iconSet(METALLIC)
                .appendFlags(EXT_METAL)
                .components(Steel, 1)
                .toolStats(ToolProperty.Builder.of(6.0F, 4.0F, 1024, 3)
                        .addTypes(GTToolType.MORTAR)
                        .attackSpeed(0.3F).enchantability(33)
                        .enchantment(Enchantments.MOB_LOOTING, 3)
                        .enchantment(Enchantments.BLOCK_FORTUNE, 3).build())
                .blastTemp(1500, GasTier.LOW)
                .buildAndRegister();

        TungstenSteel = new Material.Builder("tungsten_steel")
                .ingot(4).fluid()
                .color(0xadb8df).secondaryColor(0x03192f).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, GENERATE_ROTOR, GENERATE_SMALL_GEAR, GENERATE_DENSE, GENERATE_FRAME, GENERATE_SPRING, GENERATE_FOIL, GENERATE_FINE_WIRE, GENERATE_GEAR)
                .components(Steel, 1, Tungsten, 1)
                .toolStats(ToolProperty.Builder.of(9.0F, 7.0F, 2048, 4)
                        .enchantability(14).build())
                .rotorStats(8.0f, 4.0f, 2560)
                .fluidPipeProperties(3587, 225, true)
                .cableProperties(GTValues.V[5], 3, 2)
                .blastTemp(3000, GasTier.MID, GTValues.VA[GTValues.EV], 1000)
                .buildAndRegister();

        CobaltBrass = new Material.Builder("cobalt_brass")
                .ingot()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(1202))
                .color(0xbbac82).secondaryColor(0x596338).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, GENERATE_GEAR)
                .components(Brass, 7, Aluminium, 1, Cobalt, 1)
                .toolStats(ToolProperty.Builder.of(2.5F, 2.0F, 1024, 2)
                        .addTypes(GTToolType.MORTAR)
                        .attackSpeed(-0.2F).enchantability(5).build())
                .rotorStats(8.0f, 2.0f, 256)
                .itemPipeProperties(2048, 1)
                .buildAndRegister();

        TricalciumPhosphate = new Material.Builder("tricalcium_phosphate")
                .dust().ore(3, 1)
                .color(0xFFFF00).iconSet(FLINT)
                .flags(NO_SMASHING, NO_SMELTING, FLAMMABLE, EXPLOSIVE, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Calcium, 3, Phosphate, 2)
                .buildAndRegister();

        GarnetRed = new Material.Builder("red_garnet")
                .gem().ore(4, 1)
                .color(0xC85050).iconSet(RUBY)
                .appendFlags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Pyrope, 3, Almandine, 5, Spessartine, 8)
                .buildAndRegister();

        GarnetYellow = new Material.Builder("yellow_garnet")
                .gem().ore(4, 1)
                .color(0xC8C850).iconSet(RUBY)
                .appendFlags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Andradite, 5, Grossular, 8, Uvarovite, 3)
                .buildAndRegister();

        Marble = new Material.Builder("marble")
                .dust()
                .color(0xf0f5f4).secondaryColor(0xb3b3b3).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Magnesium, 1, Calcite, 7)
                .buildAndRegister();

        Deepslate = new Material.Builder("deepslate")
                .dust()
                .color(0x797979).secondaryColor(0x2f2f37).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(SiliconDioxide, 4, Biotite, 1)
                .buildAndRegister();

        GraniteRed = new Material.Builder("granite_red")
                .dust()
                .color(0xFF0080).iconSet(ROUGH)
                .flags(NO_SMASHING)
                .components(Aluminium, 2, PotassiumFeldspar, 1, Oxygen, 3)
                .buildAndRegister();

        // Free ID 2021

        VanadiumMagnetite = new Material.Builder("vanadium_magnetite")
                .dust().ore()
                .color(0x8a94a2).secondaryColor(0x170322).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Magnetite, 1, Vanadium, 1)
                .buildAndRegister();

        QuartzSand = new Material.Builder("quartz_sand")
                .dust(1)
                .color(0xC8C8C8).iconSet(SAND)
                .flags(DISABLE_DECOMPOSITION)
                .components(CertusQuartz, 1, Quartzite, 1)
                .buildAndRegister();

        Pollucite = new Material.Builder("pollucite")
                .dust().ore()
                .color(0xF0D2D2)
                .components(Caesium, 2, Aluminium, 2, Silicon, 4, Water, 2, Oxygen, 12)
                .buildAndRegister();

        // Free ID 2025

        Bentonite = new Material.Builder("bentonite")
                .dust().ore(3, 1)
                .color(0xede8a3).secondaryColor(0xcdb44c).iconSet(ROUGH)
                .flags(DISABLE_DECOMPOSITION)
                .components(Sodium, 1, Magnesium, 6, Silicon, 12, Hydrogen, 4, Water, 5, Oxygen, 36)
                .buildAndRegister();

        FullersEarth = new Material.Builder("fullers_earth")
                .dust().ore(2, 1)
                .color(0xf3efbb).secondaryColor(0xb8d066).iconSet(FINE)
                .components(Magnesium, 1, Silicon, 4, Hydrogen, 1, Water, 4, Oxygen, 11)
                .buildAndRegister();

        Pitchblende = new Material.Builder("pitchblende")
                .dust(3).ore(true)
                .color(0xC8D200)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Uraninite, 3, Thorium, 1, Lead, 1)
                .buildAndRegister()
                .setFormula("(UO2)3ThPb", true);

        Monazite = new Material.Builder("monazite")
                .gem(1).ore(4, 2, true)
                .color(0x324632).iconSet(DIAMOND)
                .flags(NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE)
                .components(RareEarth, 1, Phosphate, 1)
                .buildAndRegister();

        Mirabilite = new Material.Builder("mirabilite")
                .dust()
                .color(0xF0FAD2)
                .components(Sodium, 2, Sulfur, 1, Water, 10, Oxygen, 4)
                .buildAndRegister();

        Trona = new Material.Builder("trona")
                .dust(1).ore(2, 1)
                .color(0xe7e7b4).secondaryColor(0x87875F).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Sodium, 3, Carbon, 2, Hydrogen, 1, Water, 2, Oxygen, 6)
                .buildAndRegister();

        Gypsum = new Material.Builder("gypsum")
                .dust(1).ore()
                .color(0xE6E6FA)
                .components(Calcium, 1, Sulfur, 1, Water, 2, Oxygen, 4)
                .buildAndRegister();

        Zeolite = new Material.Builder("zeolite")
                .dust().ore(3, 1)
                .color(0xF0E6E6)
                .flags(DISABLE_DECOMPOSITION)
                .components(Sodium, 1, Calcium, 4, Silicon, 27, Aluminium, 9, Water, 28, Oxygen, 72)
                .buildAndRegister();

        Concrete = new Material.Builder("concrete")
                .dust()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(286))
                .color(0xfaf3e8).secondaryColor(0xbbbaba).iconSet(ROUGH)
                .flags(NO_SMASHING, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .components(Stone, 1)
                .buildAndRegister();

        SteelMagnetic = new Material.Builder("magnetic_steel")
                .ingot()
                .color(0xa7a7a7).secondaryColor(0x121c37).iconSet(MAGNETIC)
                .flags(GENERATE_ROD, IS_MAGNETIC)
                .components(Steel, 1)
                .ingotSmeltInto(Steel)
                .arcSmeltInto(Steel)
                .macerateInto(Steel)
                .buildAndRegister();
        Steel.getProperty(PropertyKey.INGOT).setMagneticMaterial(SteelMagnetic);

        VanadiumSteel = new Material.Builder("vanadium_steel")
                .ingot(3)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(2073))
                .color(0xd4d1f1).secondaryColor(0x19140d).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, GENERATE_FOIL, GENERATE_GEAR)
                .components(Vanadium, 1, Chromium, 1, Steel, 7)
                .toolStats(ToolProperty.Builder.of(3.0F, 3.0F, 1536, 3)
                        .attackSpeed(-0.2F).enchantability(5).build())
                .rotorStats(7.0f, 3.0f, 1920)
                .fluidPipeProperties(2073, 50, true, true, false, false)
                .blastTemp(1453, GasTier.LOW)
                .buildAndRegister();

        Potin = new Material.Builder("potin")
                .ingot()
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(1084))
                .color(0xaaada3).secondaryColor(0x5e3320).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, GENERATE_GEAR)
                .components(Copper, 6, Tin, 2, Lead, 1)
                .fluidPipeProperties(1456, 32, true)
                .buildAndRegister();

        BorosilicateGlass = new Material.Builder("borosilicate_glass")
                .ingot(1)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(1921))
                .color(0xFAFAFA).secondaryColor(0xfaf5c0).iconSet(SHINY)
                .flags(GENERATE_FINE_WIRE, GENERATE_PLATE)
                .components(Boron, 1, SiliconDioxide, 7)
                .buildAndRegister();

        Andesite = new Material.Builder("andesite")
                .dust()
                .color(0xa8aa9a).iconSet(ROUGH)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Asbestos, 4, Saltpeter, 1)
                .buildAndRegister();

        NaquadahAlloy = new Material.Builder("naquadah_alloy")
                .ingot(5).fluid()
                .color(0x323232).secondaryColor(0x301131).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, GENERATE_SPRING, GENERATE_RING, GENERATE_ROTOR, GENERATE_SMALL_GEAR, GENERATE_FRAME, GENERATE_DENSE, GENERATE_FOIL, GENERATE_GEAR)
                .components(Naquadah, 2, Osmiridium, 1, Trinium, 1)
                .toolStats(ToolProperty.Builder.of(40.0F, 12.0F, 3072, 5)
                        .attackSpeed(0.3F).enchantability(33).magnetic().build())
                .rotorStats(8.0f, 5.0f, 5120)
                .cableProperties(GTValues.V[8], 2, 4)
                .blastTemp(7200, GasTier.HIGH, GTValues.VA[GTValues.LuV], 1000)
                .buildAndRegister();

        SulfuricNickelSolution = new Material.Builder("sulfuric_nickel_solution")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x3EB640)
                .components(Nickel, 1, Oxygen, 1, SulfuricAcid, 1)
                .buildAndRegister();

        SulfuricCopperSolution = new Material.Builder("sulfuric_copper_solution")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x48A5C0)
                .components(Copper, 1, Oxygen, 1, SulfuricAcid, 1)
                .buildAndRegister();

        LeadZincSolution = new Material.Builder("lead_zinc_solution")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().customStill())
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Lead, 1, Silver, 1, Zinc, 1, Sulfur, 3, Water, 1)
                .buildAndRegister();

        NitrationMixture = new Material.Builder("nitration_mixture")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0xE6E2AB)
                .flags(DISABLE_DECOMPOSITION)
                .components(NitricAcid, 1, SulfuricAcid, 1)
                .buildAndRegister();

        DilutedSulfuricAcid = new Material.Builder("diluted_sulfuric_acid")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0xC07820)
                .flags(DISABLE_DECOMPOSITION)
                .components(SulfuricAcid, 2, Water, 1)
                .buildAndRegister();

        DilutedHydrochloricAcid = new Material.Builder("diluted_hydrochloric_acid")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x99A7A3)
                .flags(DISABLE_DECOMPOSITION)
                .components(HydrochloricAcid, 1, Water, 1)
                .buildAndRegister();

        Flint = new Material.Builder("flint")
                .gem(1)
                .color(0x002040).iconSet(FLINT)
                .flags(NO_SMASHING, MORTAR_GRINDABLE, DECOMPOSITION_BY_CENTRIFUGING)
                .components(SiliconDioxide, 1)
                .toolStats(ToolProperty.Builder.of(0.0F, 1.0F, 64, 1)
                        .types(GTToolType.MORTAR, GTToolType.KNIFE)
                        .enchantability(5).ignoreCraftingTools()
                        .enchantment(Enchantments.FIRE_ASPECT, 2).build())
                .buildAndRegister();

        Air = new Material.Builder("air")
                .fluid(FluidStorageKeys.GAS, new FluidBuilder().customStill())
                .color(0xA9D0F5)
                .flags(DISABLE_DECOMPOSITION)
                .components(Nitrogen, 78, Oxygen, 21, Argon, 9)
                .buildAndRegister();

        LiquidAir = new Material.Builder("liquid_air")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(97))
                .color(0xA9D0F5)
                .flags(DISABLE_DECOMPOSITION)
                .components(Nitrogen, 70, Oxygen, 22, CarbonDioxide, 5, Helium, 2, Argon, 1, Ice, 1)
                .buildAndRegister();

        NetherAir = new Material.Builder("nether_air")
                .gas()
                .color(0x4C3434)
                .flags(DISABLE_DECOMPOSITION)
                .components(CarbonMonoxide, 78, HydrogenSulfide, 21, Neon, 9)
                .buildAndRegister();

        LiquidNetherAir = new Material.Builder("liquid_nether_air")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(58))
                .color(0x4C3434)
                .flags(DISABLE_DECOMPOSITION)
                .components(CarbonMonoxide, 144, CoalGas, 20, HydrogenSulfide, 15, SulfurDioxide, 15, Helium3, 5, Neon, 1, Ash, 1)
                .buildAndRegister();

        EnderAir = new Material.Builder("ender_air")
                .gas()
                .color(0x283454)
                .flags(DISABLE_DECOMPOSITION)
                .components(NitrogenDioxide, 78, Deuterium, 21, Xenon, 9)
                .buildAndRegister();

        LiquidEnderAir = new Material.Builder("liquid_ender_air")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(36))
                .color(0x283454)
                .flags(DISABLE_DECOMPOSITION)
                .components(NitrogenDioxide, 122, Deuterium, 50, Helium, 15, Tritium, 10, Krypton, 1, Xenon, 1, Radon, 1, EnderPearl, 1)
                .buildAndRegister();

        AquaRegia = new Material.Builder("aqua_regia")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0xFFB132)
                .flags(DISABLE_DECOMPOSITION)
                .components(NitricAcid, 1, HydrochloricAcid, 2)
                .buildAndRegister();

        PlatinumSludgeResidue = new Material.Builder("platinum_sludge_residue")
                .dust()
                .color(0x827951)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(SiliconDioxide, 2, Gold, 3)
                .buildAndRegister();

        PalladiumRaw = new Material.Builder("palladium_raw")
                .dust()
                .color(0x5d4e1a).secondaryColor(0x33352d).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Palladium, 1, Ammonia, 1)
                .buildAndRegister();

        RarestMetalMixture = new Material.Builder("rarest_metal_mixture")
                .dust()
                .color(0xca8832).secondaryColor(0xb21900).iconSet(SHINY)
                .flags(DISABLE_DECOMPOSITION)
                .components(Iridium, 1, Osmium, 1, Oxygen, 4, Water, 1)
                .buildAndRegister();

        AmmoniumChloride = new Material.Builder("ammonium_chloride")
                .dust()
                .color(0x60a1c5).secondaryColor(0x48619c)
                .components(Ammonia, 1, HydrochloricAcid, 1)
                .buildAndRegister()
                .setFormula("NH4Cl", true);

        AcidicOsmiumSolution = new Material.Builder("acidic_osmium_solution")
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0xDAC5C5)
                .flags(DISABLE_DECOMPOSITION)
                .components(Osmium, 1, Oxygen, 4, Water, 1, HydrochloricAcid, 1)
                .buildAndRegister();

        RhodiumPlatedPalladium = new Material.Builder("rhodium_plated_palladium")
                .ingot().fluid()
                .color(0xd1d1d1).secondaryColor(0x000000).iconSet(SHINY)
                .appendFlags(EXT2_METAL, GENERATE_ROTOR, GENERATE_DENSE, GENERATE_SMALL_GEAR)
                .components(Palladium, 3, Rhodium, 1)
                .rotorStats(12.0f, 3.0f, 1024)
                .blastTemp(4500, GasTier.HIGH, GTValues.VA[GTValues.IV], 1200)
                .buildAndRegister();

        Clay = new Material.Builder("clay")
                .dust(1)
                .color(0xbec9e8).secondaryColor(0x373944).iconSet(ROUGH)
                .flags(MORTAR_GRINDABLE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .components(Sodium, 2, Lithium, 1, Aluminium, 2, Silicon, 2, Water, 6)
                .buildAndRegister();

        Redstone = new Material.Builder("redstone")
                .dust().ore(5, 1, true)
                .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(500))
                .color(0xff0000).secondaryColor(0x340605).iconSet(ROUGH)
                .flags(GENERATE_PLATE, NO_SMASHING, NO_SMELTING, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES,
                        EXCLUDE_PLATE_COMPRESSOR_RECIPE, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Silicon, 1, Pyrite, 5, Ruby, 1, Mercury, 3)
                .buildAndRegister();
    }
}
