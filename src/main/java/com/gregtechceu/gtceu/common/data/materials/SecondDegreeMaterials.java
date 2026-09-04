package com.gregtechceu.gtceu.common.data.materials;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty.GasTier;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttributes;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.common.data.GTMedicalConditions;

import net.minecraft.world.item.enchantment.Enchantments;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class SecondDegreeMaterials {

    public static void register() {
        Glass = REGISTRATE.material("glass", builder -> builder
                .gem(0)
                .liquid(new FluidBuilder()
                        .temperature(1200)
                        .customStill())
                .color(0xffffff).iconSet(GLASS)
                .flags(GENERATE_LENS, NO_SMASHING, EXCLUDE_BLOCK_CRAFTING_RECIPES, DECOMPOSITION_BY_CENTRIFUGING)
                .components(SiliconDioxide, 1)
        );

        Perlite = REGISTRATE.material("perlite", builder -> builder
                .dust(1)
                .color(0xeee0e0).secondaryColor(0xc1b9a9)
                .components(Obsidian, 2, Water, 1)
        );

        ActivatedCarbon = REGISTRATE.material("activated_carbon", builder -> builder
                .dust(1)
                .color(0x212125).secondaryColor(0x15151a)
                .components(Carbon, 1)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
        );

        Borax = REGISTRATE.material("borax", builder -> builder
                .dust(1)
                .color(0xFAFAFA).secondaryColor(0xd7e7e7).iconSet(FINE)
                .components(Sodium, 2, Boron, 4, Water, 10, Oxygen, 7)
        );

        SaltWater = REGISTRATE.material("salt_water", builder -> builder
                .fluid()
                .color(0x0000C8)
                .flags(DISABLE_DECOMPOSITION)
                .components(Salt, 1, Water, 1)
        );

        Olivine = REGISTRATE.material("olivine", builder -> builder
                .gem().ore(2, 1)
                .color(0xa7e404).secondaryColor(0x166439).iconSet(RUBY)
                .appendFlags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .components(Magnesium, 1, Iron, 1, SiliconDioxide, 2)
        );

        Opal = REGISTRATE.material("opal", builder -> builder
                .gem().ore()
                .color(0xf9e3ea).secondaryColor(0x16bbe0).iconSet(OPAL)
                .appendFlags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, DECOMPOSITION_BY_CENTRIFUGING)
                .components(SiliconDioxide, 1)
        );

        Amethyst = REGISTRATE.material("amethyst", builder -> builder
                .gem(3).ore()
                .color(0xcfa0f3).secondaryColor(0x734fbc).iconSet(RUBY)
                .appendFlags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, GENERATE_LENS)
                .components(SiliconDioxide, 4, Iron, 1)
        );

        EchoShard = REGISTRATE.material("echo_shard", builder -> builder
                .gem(3)
                .color(0x002b2d).iconSet(RUBY)
                .appendFlags(EXT_METAL, NO_SMASHING, NO_SMELTING, GENERATE_ROD)
                .components(SiliconDioxide, 3, Sculk, 2)
        );

        Lapis = REGISTRATE.material("lapis", builder -> builder
                .gem(1).ore(6, 4)
                .color(0x85a9ff).secondaryColor(0x2a7fff).iconSet(LAPIS)
                .flags(NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE, NO_WORKING, DECOMPOSITION_BY_ELECTROLYZING,
                        EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES,
                        GENERATE_PLATE, GENERATE_ROD)
                .components(Lazurite, 12, Sodalite, 2, Pyrite, 1, Calcite, 1)
        );

        Blaze = REGISTRATE.material("blaze", builder -> builder
                .dust(1)
                .liquid(new FluidBuilder()
                        .temperature(4000)
                        .customStill())
                .color(0xfff94d, false).secondaryColor(0xff330c).iconSet(FINE)
                .flags(NO_SMELTING, MORTAR_GRINDABLE, DECOMPOSITION_BY_CENTRIFUGING) // todo burning flag
                .components(DarkAsh, 1, Sulfur, 1)
        );

        Apatite = REGISTRATE.material("apatite", builder -> builder
                .gem(1).ore(4, 2)
                .color(0x06cdf1).secondaryColor(0x701c07).iconSet(DIAMOND)
                .flags(NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE, DISABLE_DECOMPOSITION)
                .components(Calcium, 5, Phosphate, 3, Chlorine, 1)
        );

        BlackSteel = REGISTRATE.material("black_steel", builder -> builder
                .ingot().fluid()
                .color(0x666666).secondaryColor(0x1a120e).iconSet(METALLIC)
                .appendFlags(EXT_METAL, GENERATE_FINE_WIRE, GENERATE_GEAR, GENERATE_FRAME)
                .components(Nickel, 1, BlackBronze, 1, Steel, 3)
                .cableProperties(V[EV], 3, 2)
                .blast(1758, GasTier.LOW)
        );

        DamascusSteel = REGISTRATE.material("damascus_steel", builder -> builder
                .ingot(3).fluid()
                .color(0x6E6E6E).secondaryColor(0x302222).iconSet(METALLIC)
                .appendFlags(EXT_METAL, GENERATE_BOLT_SCREW, GENERATE_LONG_ROD, GENERATE_GEAR)
                .components(Steel, 1)
                .toolStats(ToolProperty.Builder.of(6.0F, 4.0F, 1024, 3)
                        .addTypes(GTToolType.MORTAR)
                        .attackSpeed(0.3F).enchantability(33)
                        .enchantment(Enchantments.LOOTING, 3)
                        .enchantment(Enchantments.FORTUNE, 3).build())
                .blast(1500, GasTier.LOW)
        );

        TungstenSteel = REGISTRATE.material("tungsten_steel", builder -> builder
                .ingot(4).fluid()
                .color(0x687ece).secondaryColor(0x03192f).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, GENERATE_ROTOR, GENERATE_SMALL_GEAR, GENERATE_DENSE, GENERATE_FRAME,
                        GENERATE_SPRING, GENERATE_FOIL, GENERATE_FINE_WIRE, GENERATE_GEAR)
                .components(Steel, 1, Tungsten, 1)
                .toolStats(ToolProperty.Builder.of(9.0F, 7.0F, 2048, 4)
                        .enchantability(14).build())
                .rotorStats(160, 120, 4.0f, 2560)
                .fluidPipeProperties(3587, 225, true)
                .cableProperties(V[IV], 3, 2)
                .blast(b -> b.temp(3000, GasTier.MID)
                        .blastStats(VA[EV], 1000)
                        .vacuumStats(VA[HV]))
        );

        CobaltBrass = REGISTRATE.material("cobalt_brass", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(1202))
                .color(0xbaa365).secondaryColor(0x596338).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, GENERATE_GEAR)
                .components(Brass, 7, Aluminium, 1, Cobalt, 1)
                .toolStats(ToolProperty.Builder.of(2.5F, 2.0F, 1024, 2)
                        .addTypes(GTToolType.MORTAR)
                        .attackSpeed(5.8F).enchantability(5).build())
                .rotorStats(100, 120, 2.0f, 256)
                .itemPipeProperties(2048, 1)
        );

        TricalciumPhosphate = REGISTRATE.material("tricalcium_phosphate", builder -> builder
                .dust().ore(3, 1)
                .color(0xfffddb).secondaryColor(0xFFFF00).iconSet(FLINT)
                .flags(NO_SMASHING, NO_SMELTING, FLAMMABLE, EXPLOSIVE, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Calcium, 3, Phosphate, 2)
        );

        GarnetRed = REGISTRATE.material("red_garnet", builder -> builder
                .gem().ore(4, 1)
                .color(0x950c15).secondaryColor(0x510b04).iconSet(RUBY)
                .appendFlags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Pyrope, 3, Almandine, 5, Spessartine, 8)
        );

        GarnetYellow = REGISTRATE.material("yellow_garnet", builder -> builder
                .gem().ore(4, 1)
                .color(0xf6ff09).secondaryColor(0xe7a800).iconSet(RUBY)
                .appendFlags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Andradite, 5, Grossular, 8, Uvarovite, 3)
        );

        Marble = REGISTRATE.material("marble", builder -> builder
                .dust()
                .color(0xf0f5f4).secondaryColor(0xb3b3b3).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Magnesium, 1, Calcite, 7)
        );

        Deepslate = REGISTRATE.material("deepslate", builder -> builder
                .dust()
                .color(0x797979).secondaryColor(0x2f2f37).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(SiliconDioxide, 4, Biotite, 1)
        );

        RedGranite = REGISTRATE.material("red_granite", builder -> builder
                .dust()
                .color(0xFF0080).iconSet(ROUGH)
                .flags(NO_SMASHING)
                .components(Aluminium, 2, PotassiumFeldspar, 1, Oxygen, 3)
        );

        VanadiumMagnetite = REGISTRATE.material("vanadium_magnetite", builder -> builder
                .dust().ore()
                .color(0x505d70).secondaryColor(0x170322).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Magnetite, 1, Vanadium, 1)
        );

        QuartzSand = REGISTRATE.material("quartz_sand", builder -> builder
                .dust(1)
                .color(0xf8efe3).secondaryColor(0xe6c1bb).iconSet(SAND)
                .flags(DISABLE_DECOMPOSITION)
                .components(CertusQuartz, 1, Quartzite, 1)
        );

        Pollucite = REGISTRATE.material("pollucite", builder -> builder
                .dust().ore()
                .color(0xeed9e1).secondaryColor(0x72a6a7)
                .components(Caesium, 2, Aluminium, 2, Silicon, 4, Water, 2, Oxygen, 12)
        );

        Bentonite = REGISTRATE.material("bentonite", builder -> builder
                .dust().ore(3, 1)
                .color(0xede8a3).secondaryColor(0xcdb44c).iconSet(ROUGH)
                .flags(DISABLE_DECOMPOSITION)
                .components(Sodium, 1, Magnesium, 6, Silicon, 12, Hydrogen, 6, Water, 5, Oxygen, 36)
        );

        FullersEarth = REGISTRATE.material("fullers_earth", builder -> builder
                .dust().ore(2, 1)
                .color(0xf3efbb).secondaryColor(0xb8d066).iconSet(FINE)
                .components(Magnesium, 2, Silicon, 4, Oxygen, 14, Hydrogen, 4, Water, 1)
        );

        Pitchblende = REGISTRATE.material("pitchblende", builder -> builder
                .dust(3).ore(true)
                .color(0xffd647).secondaryColor(0x0d1e2f)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Uraninite, 3, Thorium, 1, Lead, 1)
                .formula("(UO2)3ThPb", true)
        );

        Monazite = REGISTRATE.material("monazite", builder -> builder
                .gem(1).ore(4, 2, true)
                .color(0xd0ee98).secondaryColor(0x520505).iconSet(DIAMOND)
                .flags(NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE)
                .components(RareEarth, 1, Phosphate, 1)
        );

        Mirabilite = REGISTRATE.material("mirabilite", builder -> builder
                .dust()
                .color(0xf9e7e7).secondaryColor(0xb57a7a)
                .components(Sodium, 2, Sulfur, 1, Water, 10, Oxygen, 4)
        );

        Trona = REGISTRATE.material("trona", builder -> builder
                .dust(1).ore(2, 1)
                .color(0xe6e6a5).secondaryColor(0x87875F).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Sodium, 3, Carbon, 2, Hydrogen, 1, Water, 2, Oxygen, 6)
        );

        Gypsum = REGISTRATE.material("gypsum", builder -> builder
                .dust(1).ore()
                .color(0xfffaec).secondaryColor(0x71570a)
                .components(Calcium, 1, Sulfur, 1, Water, 2, Oxygen, 4)
        );

        Zeolite = REGISTRATE.material("zeolite", builder -> builder
                .dust().ore(3, 1)
                .color(0xf2e3e0).secondaryColor(0xeabeb4)
                .components(Sodium, 2, Aluminium, 2, Silicon, 3, Oxygen, 10, Water, 2)
        );

        Concrete = REGISTRATE.material("concrete", builder -> builder
                .dust()
                .liquid(new FluidBuilder().temperature(286))
                .color(0xfaf3e8).secondaryColor(0xbbbaba).iconSet(ROUGH)
                .flags(NO_SMASHING, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .components(Stone, 1)
        );

        SteelMagnetic = REGISTRATE.material("magnetic_steel", builder -> builder
                .ingot()
                .color(0xa7a7a7).secondaryColor(0x121c37).iconSet(MAGNETIC)
                .flags(GENERATE_ROD, IS_MAGNETIC, GENERATE_DENSE)
                .components(Steel, 1)
                .ingotSmeltInto(Steel)
                .arcSmeltInto(Steel)
                .macerateInto(Steel)
        );

        VanadiumSteel = REGISTRATE.material("vanadium_steel", builder -> builder
                .ingot(3)
                .liquid(new FluidBuilder().temperature(2073))
                .color(0xb59fcc).secondaryColor(0x19140d).iconSet(SHINY)
                .appendFlags(EXT2_METAL, GENERATE_FOIL, GENERATE_GEAR)
                .components(Vanadium, 1, Chromium, 1, Steel, 7)
                .toolStats(ToolProperty.Builder.of(3.0F, 3.0F, 1536, 3)
                        .attackSpeed(-0.2F).enchantability(5).build())
                .rotorStats(130, 115, 3.0f, 1920)
                .fluidPipeProperties(2073, 50, true, true, false, false)
                .blast(1453, GasTier.LOW)
        );

        Potin = REGISTRATE.material("potin", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(1084))
                .color(0xaaada3).secondaryColor(0x5e3320).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, GENERATE_GEAR)
                .components(Copper, 6, Tin, 2, Lead, 1)
                .fluidPipeProperties(1456, 40, true)
        );

        BorosilicateGlass = REGISTRATE.material("borosilicate_glass", builder -> builder
                .ingot(1)
                .liquid(new FluidBuilder().temperature(1921))
                .color(0xFAFAFA).secondaryColor(0xfaf5c0).iconSet(SHINY)
                .flags(GENERATE_FINE_WIRE, GENERATE_PLATE)
                .components(Boron, 1, SiliconDioxide, 7)
        );

        Andesite = REGISTRATE.material("andesite", builder -> builder
                .dust()
                .color(0xa8aa9a).iconSet(ROUGH)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Asbestos, 4, Saltpeter, 1)
                .removeHazard()
        );

        NaquadahAlloy = REGISTRATE.material("naquadah_alloy", builder -> builder
                .ingot(5).fluid()
                .color(0x323232).secondaryColor(0x301131).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, GENERATE_SPRING, GENERATE_RING, GENERATE_ROTOR, GENERATE_SMALL_GEAR,
                        GENERATE_FRAME, GENERATE_DENSE, GENERATE_FOIL, GENERATE_GEAR)
                .components(Naquadah, 2, Osmiridium, 1, Trinium, 1)
                .toolStats(ToolProperty.Builder.of(40.0F, 12.0F, 3072, 5)
                        .attackSpeed(0.3F).enchantability(33).magnetic().build())
                .rotorStats(190, 120, 5.0f, 5120)
                .cableProperties(V[UV], 2, 4)
                .blast(b -> b.temp(7200, GasTier.HIGH)
                        .blastStats(VA[LuV], 1000)
                        .vacuumStats(VA[IV], 300))
        );

        SulfuricNickelSolution = REGISTRATE.material("sulfuric_nickel_solution", builder -> builder
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x3EB640)
                .components(Nickel, 1, Oxygen, 1, SulfuricAcid, 1)
        );

        SulfuricCopperSolution = REGISTRATE.material("sulfuric_copper_solution", builder -> builder
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x48A5C0)
                .components(Copper, 1, Oxygen, 1, SulfuricAcid, 1)
        );

        LeadZincSolution = REGISTRATE.material("lead_zinc_solution", builder -> builder
                .liquid(new FluidBuilder().customStill())
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Lead, 1, Silver, 1, Zinc, 1, Sulfur, 3, Water, 1)
        );

        NitrationMixture = REGISTRATE.material("nitration_mixture", builder -> builder
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0xE6E2AB)
                .flags(DISABLE_DECOMPOSITION)
                .components(NitricAcid, 1, SulfuricAcid, 1)
        );

        DilutedSulfuricAcid = REGISTRATE.material("diluted_sulfuric_acid", builder -> builder
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0xC07820)
                .flags(DISABLE_DECOMPOSITION)
                .components(SulfuricAcid, 2, Water, 1)
        );

        DilutedHydrochloricAcid = REGISTRATE.material("diluted_hydrochloric_acid", builder -> builder
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x99A7A3)
                .flags(DISABLE_DECOMPOSITION)
                .components(HydrochloricAcid, 1, Water, 1)
        );

        Flint = REGISTRATE.material("flint", builder -> builder
                .gem(1)
                .color(0xc7c7c7).secondaryColor(0x212121).iconSet(FLINT)
                .flags(NO_SMASHING, MORTAR_GRINDABLE, DECOMPOSITION_BY_CENTRIFUGING)
                .components(SiliconDioxide, 1)
                .toolStats(ToolProperty.Builder.of(0.0F, 1.0F, 64, 1)
                        .types(GTToolType.MORTAR, GTToolType.KNIFE, GTToolType.AXE, GTToolType.PICKAXE, GTToolType.HOE,
                                GTToolType.SWORD, GTToolType.SHOVEL)
                        .enchantability(5).ignoreCraftingTools()
                        .enchantment(Enchantments.FIRE_ASPECT, 1).build())
        );

        Air = REGISTRATE.material("air", builder -> builder
                .gas(new FluidBuilder().customStill())
                .color(0xA9D0F5)
                .flags(DISABLE_DECOMPOSITION)
                .components(Nitrogen, 78, Oxygen, 21, Argon, 9)
        );

        LiquidAir = REGISTRATE.material("liquid_air", builder -> builder
                .liquid(new FluidBuilder().temperature(97))
                .color(0xA9D0F5)
                .flags(DISABLE_DECOMPOSITION)
                .components(Nitrogen, 70, Oxygen, 22, CarbonDioxide, 5, Helium, 2, Argon, 1, Ice, 1)
        );

        NetherAir = REGISTRATE.material("nether_air", builder -> builder
                .gas()
                .color(0x4C3434)
                .flags(DISABLE_DECOMPOSITION)
                .components(CarbonMonoxide, 78, HydrogenSulfide, 21, Neon, 9)
                .hazard(HazardProperty.HazardTrigger.INHALATION, GTMedicalConditions.CHEMICAL_BURNS)
        );

        LiquidNetherAir = REGISTRATE.material("liquid_nether_air", builder -> builder
                .liquid(new FluidBuilder().temperature(58))
                .color(0x4C3434)
                .flags(DISABLE_DECOMPOSITION)
                .components(CarbonMonoxide, 144, CoalGas, 20, HydrogenSulfide, 15, SulfurDioxide, 15, Helium3, 5, Neon,
                        1, Ash, 1)
        );

        EnderAir = REGISTRATE.material("ender_air", builder -> builder
                .gas()
                .color(0x283454)
                .flags(DISABLE_DECOMPOSITION)
                .components(NitrogenDioxide, 78, Deuterium, 21, Xenon, 9)
        );

        LiquidEnderAir = REGISTRATE.material("liquid_ender_air", builder -> builder
                .liquid(new FluidBuilder().temperature(36))
                .color(0x283454)
                .flags(DISABLE_DECOMPOSITION)
                .components(NitrogenDioxide, 122, Deuterium, 50, Helium, 15, Tritium, 10, Krypton, 1, Xenon, 1, Radon,
                        1, EnderPearl, 1)
        );

        AquaRegia = REGISTRATE.material("aqua_regia", builder -> builder
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0xFFB132)
                .flags(DISABLE_DECOMPOSITION)
                .components(NitricAcid, 1, HydrochloricAcid, 2)
        );

        PlatinumSludgeResidue = REGISTRATE.material("platinum_sludge_residue", builder -> builder
                .dust()
                .color(0x5e4b40).secondaryColor(0x4b403d).iconSet(FINE)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(SiliconDioxide, 2, Gold, 3)
        );

        PalladiumRaw = REGISTRATE.material("palladium_raw", builder -> builder
                .dust()
                .color(0x5d4e1a).secondaryColor(0x33352d).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Palladium, 1, Ammonia, 1)
        );

        RarestMetalMixture = REGISTRATE.material("rarest_metal_mixture", builder -> builder
                .dust()
                .color(0xca8832).secondaryColor(0xb21900).iconSet(SHINY)
                .flags(DISABLE_DECOMPOSITION)
                .components(Iridium, 1, Osmium, 1, Oxygen, 4, Water, 1)
        );

        AmmoniumChloride = REGISTRATE.material("ammonium_chloride", builder -> builder
                .dust()
                .color(0x60a1c5).secondaryColor(0x48619c)
                .components(Ammonia, 1, HydrochloricAcid, 1)
                .formula("NH4Cl", true)
        );

        AcidicOsmiumSolution = REGISTRATE.material("acidic_osmium_solution", builder -> builder
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0xDAC5C5)
                .flags(DISABLE_DECOMPOSITION)
                .components(Osmium, 1, Oxygen, 4, Water, 1, HydrochloricAcid, 1)
        );

        RhodiumPlatedPalladium = REGISTRATE.material("rhodium_plated_palladium", builder -> builder
                .ingot().fluid()
                .color(0xd1d1d1).secondaryColor(0x000000).iconSet(SHINY)
                .appendFlags(EXT2_METAL, GENERATE_ROTOR, GENERATE_DENSE, GENERATE_SMALL_GEAR)
                .components(Palladium, 3, Rhodium, 1)
                .rotorStats(130, 155, 3.0f, 1024)
                .blast(b -> b.temp(4500, GasTier.HIGH)
                        .blastStats(VA[IV], 1200)
                        .vacuumStats(VA[EV], 300))
        );

        Clay = REGISTRATE.material("clay", builder -> builder
                .dust(1)
                .color(0xbec9e8).secondaryColor(0x373944).iconSet(ROUGH)
                .flags(MORTAR_GRINDABLE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .components(Sodium, 2, Lithium, 1, Aluminium, 2, Silicon, 2, Water, 6)
        );

        Redstone = REGISTRATE.material("redstone", builder -> builder
                .dust().ore(5, 1, true)
                .liquid(new FluidBuilder().temperature(500))
                .color(0xff0000).secondaryColor(0x340605).iconSet(ROUGH)
                .flags(GENERATE_PLATE, NO_SMASHING, NO_SMELTING, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES,
                        EXCLUDE_PLATE_COMPRESSOR_RECIPE, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Silicon, 1, Pyrite, 5, Ruby, 1, Mercury, 3)
                .removeHazard()
        );

        Dichloroethane = REGISTRATE.material("dichloroethane", builder -> builder
                .liquid()
                .color(0xafc979)
                .flags(DECOMPOSITION_BY_ELECTROLYZING)
                .components(Carbon, 2, Hydrogen, 4, Chlorine, 2)
        );

        Diethylenetriamine = REGISTRATE.material("diethylenetriamine", builder -> builder
                .liquid()
                .color(0xa9d9a7)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 13, Nitrogen, 3)
                .hazard(HazardProperty.HazardTrigger.ANY, GTMedicalConditions.CHEMICAL_BURNS)
        );

        Tuff = REGISTRATE.material("tuff", builder -> builder
                .dust()
                .color(0x75756a).secondaryColor(0x8a8a80).iconSet(ROUGH)
                .flags(NO_SMASHING)
                .components(Ash, 2, PotassiumFeldspar, 1)
        );
    }
}
