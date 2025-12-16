package com.gregtechceu.gtceu.common.data.materials;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty.GasTier;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
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

public class SecondDegreeMaterials {

    public static void register() {
        Glass = new Material.Builder(GTCEu.id("glass"))
                .gem(0)
                .liquid(new FluidBuilder()
                        .temperature(1200)
                        .customStill())
                .color(0xffffff).iconSet(GLASS)
                .flags(GENERATE_LENS, NO_SMASHING, EXCLUDE_BLOCK_CRAFTING_RECIPES, DECOMPOSITION_BY_CENTRIFUGING)
                .components(SiliconDioxide, 1)
                .buildAndRegister();

        Perlite = new Material.Builder(GTCEu.id("perlite"))
                .dust(1)
                .color(0xeee0e0).secondaryColor(0xc1b9a9)
                .components(Obsidian, 2, Water, 1)
                .buildAndRegister();

        ActivatedCarbon = new Material.Builder(GTCEu.id("activated_carbon"))
                .dust(1)
                .color(0x212125).secondaryColor(0x15151a)
                .components(Carbon, 1)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .buildAndRegister();

        Borax = new Material.Builder(GTCEu.id("borax"))
                .dust(1)
                .color(0xFAFAFA).secondaryColor(0xd7e7e7).iconSet(FINE)
                .components(Sodium, 2, Boron, 4, Water, 10, Oxygen, 7)
                .buildAndRegister();

        SaltWater = new Material.Builder(GTCEu.id("salt_water"))
                .fluid()
                .color(0x0000C8)
                .flags(DISABLE_DECOMPOSITION)
                .components(Salt, 1, Water, 1)
                .buildAndRegister();

        Olivine = new Material.Builder(GTCEu.id("olivine"))
                .gem().ore(2, 1)
                .color(0xa7e404).secondaryColor(0x166439).iconSet(RUBY)
                .appendFlags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .components(Magnesium, 1, Iron, 1, SiliconDioxide, 2)
                .buildAndRegister();

        Opal = new Material.Builder(GTCEu.id("opal"))
                .gem().ore()
                .color(0xf9e3ea).secondaryColor(0x16bbe0).iconSet(OPAL)
                .appendFlags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, DECOMPOSITION_BY_CENTRIFUGING)
                .components(SiliconDioxide, 1)
                .buildAndRegister();

        Amethyst = new Material.Builder(GTCEu.id("amethyst"))
                .gem(3).ore()
                .color(0xcfa0f3).secondaryColor(0x734fbc).iconSet(RUBY)
                .appendFlags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, GENERATE_LENS)
                .components(SiliconDioxide, 4, Iron, 1)
                .buildAndRegister();

        EchoShard = new Material.Builder(GTCEu.id("echo_shard"))
                .gem(3)
                .color(0x002b2d).iconSet(RUBY)
                .appendFlags(EXT_METAL, NO_SMASHING, NO_SMELTING, GENERATE_ROD)
                .components(SiliconDioxide, 3, Sculk, 2)
                .buildAndRegister();

        Lapis = new Material.Builder(GTCEu.id("lapis"))
                .gem(1).ore(6, 4)
                .color(0x85a9ff).secondaryColor(0x2a7fff).iconSet(LAPIS)
                .flags(NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE, NO_WORKING, DECOMPOSITION_BY_ELECTROLYZING,
                        EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES,
                        GENERATE_PLATE, GENERATE_ROD)
                .components(Lazurite, 12, Sodalite, 2, Pyrite, 1, Calcite, 1)
                .buildAndRegister();

        Blaze = new Material.Builder(GTCEu.id("blaze"))
                .dust(1)
                .liquid(new FluidBuilder()
                        .temperature(4000)
                        .customStill())
                .color(0xfff94d, false).secondaryColor(0xff330c).iconSet(FINE)
                .flags(NO_SMELTING, MORTAR_GRINDABLE, DECOMPOSITION_BY_CENTRIFUGING) // todo burning flag
                .components(DarkAsh, 1, Sulfur, 1)
                .buildAndRegister();

        Apatite = new Material.Builder(GTCEu.id("apatite"))
                .gem(1).ore(4, 2)
                .color(0x06cdf1).secondaryColor(0x701c07).iconSet(DIAMOND)
                .flags(NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE, GENERATE_BOLT_SCREW, DISABLE_DECOMPOSITION)
                .components(Calcium, 5, Phosphate, 3, Chlorine, 1)
                .buildAndRegister();

        BlackSteel = new Material.Builder(GTCEu.id("black_steel"))
                .ingot().fluid()
                .color(0x666666).secondaryColor(0x1a120e).iconSet(METALLIC)
                .appendFlags(EXT_METAL, GENERATE_FINE_WIRE, GENERATE_GEAR, GENERATE_FRAME)
                .components(Nickel, 1, BlackBronze, 1, Steel, 3)
                .cableProperties(V[EV], 3, 2)
                .blast(1758, GasTier.LOW)
                .buildAndRegister();

        DamascusSteel = new Material.Builder(GTCEu.id("damascus_steel"))
                .ingot(3).fluid()
                .color(0x6E6E6E).secondaryColor(0x302222).iconSet(METALLIC)
                .appendFlags(EXT_METAL, GENERATE_BOLT_SCREW, GENERATE_LONG_ROD, GENERATE_GEAR)
                .components(Steel, 1)
                .toolStats(ToolProperty.Builder.of(6.0F, 4.0F, 1024, 3)
                        .addTypes(GTToolType.MORTAR)
                        .attackSpeed(0.3F).enchantability(33)
                        .enchantment(Enchantments.MOB_LOOTING, 3)
                        .enchantment(Enchantments.BLOCK_FORTUNE, 3).build())
                .blast(1500, GasTier.LOW)
                .buildAndRegister();

        TungstenSteel = new Material.Builder(GTCEu.id("tungsten_steel"))
                .langValue("Tungstensteel")
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
                .buildAndRegister();

        CobaltBrass = new Material.Builder(GTCEu.id("cobalt_brass"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1202))
                .color(0xbaa365).secondaryColor(0x596338).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, GENERATE_GEAR)
                .components(Brass, 7, Aluminium, 1, Cobalt, 1)
                .toolStats(ToolProperty.Builder.of(2.5F, 2.0F, 1024, 2)
                        .addTypes(GTToolType.MORTAR)
                        .attackSpeed(-0.2F).enchantability(5).build())
                .rotorStats(100, 120, 2.0f, 256)
                .itemPipeProperties(2048, 1)
                .buildAndRegister();

        TricalciumPhosphate = new Material.Builder(GTCEu.id("tricalcium_phosphate"))
                .dust().ore(3, 1)
                .color(0xfffddb).secondaryColor(0xFFFF00).iconSet(FLINT)
                .flags(NO_SMASHING, NO_SMELTING, FLAMMABLE, EXPLOSIVE, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Calcium, 3, Phosphate, 2)
                .buildAndRegister();

        GarnetRed = new Material.Builder(GTCEu.id("red_garnet"))
                .gem().ore(4, 1)
                .color(0x950c15).secondaryColor(0x510b04).iconSet(RUBY)
                .appendFlags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Pyrope, 3, Almandine, 5, Spessartine, 8)
                .buildAndRegister();

        GarnetYellow = new Material.Builder(GTCEu.id("yellow_garnet"))
                .gem().ore(4, 1)
                .color(0xf6ff09).secondaryColor(0xe7a800).iconSet(RUBY)
                .appendFlags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Andradite, 5, Grossular, 8, Uvarovite, 3)
                .buildAndRegister();

        Marble = new Material.Builder(GTCEu.id("marble"))
                .dust()
                .color(0xf0f5f4).secondaryColor(0xb3b3b3).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Magnesium, 1, Calcite, 7)
                .buildAndRegister();

        Deepslate = new Material.Builder(GTCEu.id("deepslate"))
                .dust()
                .color(0x797979).secondaryColor(0x2f2f37).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(SiliconDioxide, 4, Biotite, 1)
                .buildAndRegister();

        GraniteRed = new Material.Builder(GTCEu.id("granite_red"))
                .dust()
                .color(0xFF0080).iconSet(ROUGH)
                .flags(NO_SMASHING)
                .components(Aluminium, 2, PotassiumFeldspar, 1, Oxygen, 3)
                .buildAndRegister();

        VanadiumMagnetite = new Material.Builder(GTCEu.id("vanadium_magnetite"))
                .dust().ore()
                .color(0x505d70).secondaryColor(0x170322).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Magnetite, 1, Vanadium, 1)
                .buildAndRegister();

        QuartzSand = new Material.Builder(GTCEu.id("quartz_sand"))
                .dust(1)
                .color(0xf8efe3).secondaryColor(0xe6c1bb).iconSet(SAND)
                .flags(DISABLE_DECOMPOSITION)
                .components(CertusQuartz, 1, Quartzite, 1)
                .buildAndRegister();

        Pollucite = new Material.Builder(GTCEu.id("pollucite"))
                .dust().ore()
                .color(0xeed9e1).secondaryColor(0x72a6a7)
                .components(Caesium, 2, Aluminium, 2, Silicon, 4, Water, 2, Oxygen, 12)
                .buildAndRegister();

        Bentonite = new Material.Builder(GTCEu.id("bentonite"))
                .dust().ore(3, 1)
                .color(0xede8a3).secondaryColor(0xcdb44c).iconSet(ROUGH)
                .flags(DISABLE_DECOMPOSITION)
                .components(Sodium, 1, Magnesium, 6, Silicon, 12, Hydrogen, 6, Water, 5, Oxygen, 36)
                .buildAndRegister();

        FullersEarth = new Material.Builder(GTCEu.id("fullers_earth"))
                .langValue("Fuller's Earth")
                .dust().ore(2, 1)
                .color(0xf3efbb).secondaryColor(0xb8d066).iconSet(FINE)
                .components(Magnesium, 2, Silicon, 4, Oxygen, 14, Hydrogen, 4, Water, 1)
                .buildAndRegister();

        Pitchblende = new Material.Builder(GTCEu.id("pitchblende"))
                .dust(3).ore(true)
                .color(0xffd647).secondaryColor(0x0d1e2f)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Uraninite, 3, Thorium, 1, Lead, 1)
                .buildAndRegister()
                .setFormula("(UO2)3ThPb", true);

        Monazite = new Material.Builder(GTCEu.id("monazite"))
                .gem(1).ore(4, 2, true)
                .color(0xd0ee98).secondaryColor(0x520505).iconSet(DIAMOND)
                .flags(NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE)
                .components(RareEarth, 1, Phosphate, 1)
                .buildAndRegister();

        Mirabilite = new Material.Builder(GTCEu.id("mirabilite"))
                .dust()
                .color(0xf9e7e7).secondaryColor(0xb57a7a)
                .components(Sodium, 2, Sulfur, 1, Water, 10, Oxygen, 4)
                .buildAndRegister();

        Trona = new Material.Builder(GTCEu.id("trona"))
                .dust(1).ore(2, 1)
                .color(0xe6e6a5).secondaryColor(0x87875F).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Sodium, 3, Carbon, 2, Hydrogen, 1, Water, 2, Oxygen, 6)
                .buildAndRegister();

        Gypsum = new Material.Builder(GTCEu.id("gypsum"))
                .dust(1).ore()
                .color(0xfffaec).secondaryColor(0x71570a)
                .components(Calcium, 1, Sulfur, 1, Water, 2, Oxygen, 4)
                .buildAndRegister();

        Zeolite = new Material.Builder(GTCEu.id("zeolite"))
                .dust().ore(3, 1)
                .color(0xf2e3e0).secondaryColor(0xeabeb4)
                .components(Sodium, 2, Aluminium, 2, Silicon, 3, Oxygen, 10, Water, 2)
                .buildAndRegister();

        Concrete = new Material.Builder(GTCEu.id("concrete"))
                .dust()
                .liquid(new FluidBuilder().temperature(286))
                .color(0xfaf3e8).secondaryColor(0xbbbaba).iconSet(ROUGH)
                .flags(NO_SMASHING, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .components(Stone, 1)
                .buildAndRegister();

        SteelMagnetic = new Material.Builder(GTCEu.id("magnetic_steel"))
                .ingot()
                .color(0xa7a7a7).secondaryColor(0x121c37).iconSet(MAGNETIC)
                .flags(GENERATE_ROD, IS_MAGNETIC, GENERATE_DENSE)
                .components(Steel, 1)
                .ingotSmeltInto(Steel)
                .arcSmeltInto(Steel)
                .macerateInto(Steel)
                .buildAndRegister();
        Steel.getProperty(PropertyKey.INGOT).setMagneticMaterial(SteelMagnetic);

        VanadiumSteel = new Material.Builder(GTCEu.id("vanadium_steel"))
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
                .buildAndRegister();

        Potin = new Material.Builder(GTCEu.id("potin"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1084))
                .color(0xaaada3).secondaryColor(0x5e3320).iconSet(METALLIC)
                .appendFlags(EXT2_METAL, GENERATE_GEAR)
                .components(Copper, 6, Tin, 2, Lead, 1)
                .fluidPipeProperties(1456, 40, true)
                .buildAndRegister();

        BorosilicateGlass = new Material.Builder(GTCEu.id("borosilicate_glass"))
                .ingot(1)
                .liquid(new FluidBuilder().temperature(1921))
                .color(0xFAFAFA).secondaryColor(0xfaf5c0).iconSet(SHINY)
                .flags(GENERATE_FINE_WIRE, GENERATE_PLATE)
                .components(Boron, 1, SiliconDioxide, 7)
                .buildAndRegister();

        Andesite = new Material.Builder(GTCEu.id("andesite"))
                .dust()
                .color(0xa8aa9a).iconSet(ROUGH)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Asbestos, 4, Saltpeter, 1)
                .removeHazard()
                .buildAndRegister();

        NaquadahAlloy = new Material.Builder(GTCEu.id("naquadah_alloy"))
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
                .buildAndRegister();

        SulfuricNickelSolution = new Material.Builder(GTCEu.id("sulfuric_nickel_solution"))
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x3EB640)
                .components(Nickel, 1, Oxygen, 1, SulfuricAcid, 1)
                .buildAndRegister();

        SulfuricCopperSolution = new Material.Builder(GTCEu.id("sulfuric_copper_solution"))
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x48A5C0)
                .components(Copper, 1, Oxygen, 1, SulfuricAcid, 1)
                .buildAndRegister();

        LeadZincSolution = new Material.Builder(GTCEu.id("lead_zinc_solution"))
                .liquid(new FluidBuilder().customStill())
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Lead, 1, Silver, 1, Zinc, 1, Sulfur, 3, Water, 1)
                .buildAndRegister();

        NitrationMixture = new Material.Builder(GTCEu.id("nitration_mixture"))
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0xE6E2AB)
                .flags(DISABLE_DECOMPOSITION)
                .components(NitricAcid, 1, SulfuricAcid, 1)
                .buildAndRegister();

        DilutedSulfuricAcid = new Material.Builder(GTCEu.id("diluted_sulfuric_acid"))
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0xC07820)
                .flags(DISABLE_DECOMPOSITION)
                .components(SulfuricAcid, 2, Water, 1)
                .buildAndRegister();

        DilutedHydrochloricAcid = new Material.Builder(GTCEu.id("diluted_hydrochloric_acid"))
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x99A7A3)
                .flags(DISABLE_DECOMPOSITION)
                .components(HydrochloricAcid, 1, Water, 1)
                .buildAndRegister();

        Flint = new Material.Builder(GTCEu.id("flint"))
                .gem(1)
                .color(0xc7c7c7).secondaryColor(0x212121).iconSet(FLINT)
                .flags(NO_SMASHING, MORTAR_GRINDABLE, DECOMPOSITION_BY_CENTRIFUGING)
                .components(SiliconDioxide, 1)
                .toolStats(ToolProperty.Builder.of(0.0F, 1.0F, 64, 1)
                        .types(GTToolType.MORTAR, GTToolType.KNIFE, GTToolType.AXE, GTToolType.PICKAXE, GTToolType.HOE,
                                GTToolType.SWORD, GTToolType.SHOVEL)
                        .enchantability(5).ignoreCraftingTools()
                        .enchantment(Enchantments.FIRE_ASPECT, 1).build())
                .buildAndRegister();

        Air = new Material.Builder(GTCEu.id("air"))
                .gas(new FluidBuilder().customStill())
                .color(0xA9D0F5)
                .flags(DISABLE_DECOMPOSITION)
                .components(Nitrogen, 78, Oxygen, 21, Argon, 9)
                .buildAndRegister();

        LiquidAir = new Material.Builder(GTCEu.id("liquid_air"))
                .liquid(new FluidBuilder().temperature(97))
                .color(0xA9D0F5)
                .flags(DISABLE_DECOMPOSITION)
                .components(Nitrogen, 70, Oxygen, 22, CarbonDioxide, 5, Helium, 2, Argon, 1, Ice, 1)
                .buildAndRegister();

        NetherAir = new Material.Builder(GTCEu.id("nether_air"))
                .gas()
                .color(0x4C3434)
                .flags(DISABLE_DECOMPOSITION)
                .components(CarbonMonoxide, 78, HydrogenSulfide, 21, Neon, 9)
                .hazard(HazardProperty.HazardTrigger.INHALATION, GTMedicalConditions.CHEMICAL_BURNS)
                .buildAndRegister();

        LiquidNetherAir = new Material.Builder(GTCEu.id("liquid_nether_air"))
                .liquid(new FluidBuilder().temperature(58))
                .color(0x4C3434)
                .flags(DISABLE_DECOMPOSITION)
                .components(CarbonMonoxide, 144, CoalGas, 20, HydrogenSulfide, 15, SulfurDioxide, 15, Helium3, 5, Neon,
                        1, Ash, 1)
                .buildAndRegister();

        EnderAir = new Material.Builder(GTCEu.id("ender_air"))
                .gas()
                .color(0x283454)
                .flags(DISABLE_DECOMPOSITION)
                .components(NitrogenDioxide, 78, Deuterium, 21, Xenon, 9)
                .buildAndRegister();

        LiquidEnderAir = new Material.Builder(GTCEu.id("liquid_ender_air"))
                .liquid(new FluidBuilder().temperature(36))
                .color(0x283454)
                .flags(DISABLE_DECOMPOSITION)
                .components(NitrogenDioxide, 122, Deuterium, 50, Helium, 15, Tritium, 10, Krypton, 1, Xenon, 1, Radon,
                        1, EnderPearl, 1)
                .buildAndRegister();

        AquaRegia = new Material.Builder(GTCEu.id("aqua_regia"))
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0xFFB132)
                .flags(DISABLE_DECOMPOSITION)
                .components(NitricAcid, 1, HydrochloricAcid, 2)
                .buildAndRegister();

        PlatinumSludgeResidue = new Material.Builder(GTCEu.id("platinum_sludge_residue"))
                .dust()
                .color(0x5e4b40).secondaryColor(0x4b403d).iconSet(FINE)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(SiliconDioxide, 2, Gold, 3)
                .buildAndRegister();

        PalladiumRaw = new Material.Builder(GTCEu.id("palladium_raw"))
                .dust()
                .color(0x5d4e1a).secondaryColor(0x33352d).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Palladium, 1, Ammonia, 1)
                .buildAndRegister();

        RarestMetalMixture = new Material.Builder(GTCEu.id("rarest_metal_mixture"))
                .dust()
                .color(0xca8832).secondaryColor(0xb21900).iconSet(SHINY)
                .flags(DISABLE_DECOMPOSITION)
                .components(Iridium, 1, Osmium, 1, Oxygen, 4, Water, 1)
                .buildAndRegister();

        AmmoniumChloride = new Material.Builder(GTCEu.id("ammonium_chloride"))
                .dust()
                .color(0x60a1c5).secondaryColor(0x48619c)
                .components(Ammonia, 1, HydrochloricAcid, 1)
                .buildAndRegister()
                .setFormula("NH4Cl", true);

        AcidicOsmiumSolution = new Material.Builder(GTCEu.id("acidic_osmium_solution"))
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0xDAC5C5)
                .flags(DISABLE_DECOMPOSITION)
                .components(Osmium, 1, Oxygen, 4, Water, 1, HydrochloricAcid, 1)
                .buildAndRegister();

        RhodiumPlatedPalladium = new Material.Builder(GTCEu.id("rhodium_plated_palladium"))
                .ingot().fluid()
                .color(0xd1d1d1).secondaryColor(0x000000).iconSet(SHINY)
                .appendFlags(EXT2_METAL, GENERATE_ROTOR, GENERATE_DENSE, GENERATE_SMALL_GEAR)
                .components(Palladium, 3, Rhodium, 1)
                .rotorStats(130, 155, 3.0f, 1024)
                .blast(b -> b.temp(4500, GasTier.HIGH)
                        .blastStats(VA[IV], 1200)
                        .vacuumStats(VA[EV], 300))
                .buildAndRegister();

        Clay = new Material.Builder(GTCEu.id("clay"))
                .dust(1)
                .color(0xbec9e8).secondaryColor(0x373944).iconSet(ROUGH)
                .flags(MORTAR_GRINDABLE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .components(Sodium, 2, Lithium, 1, Aluminium, 2, Silicon, 2, Water, 6)
                .buildAndRegister();

        Redstone = new Material.Builder(GTCEu.id("redstone"))
                .dust().ore(5, 1, true)
                .liquid(new FluidBuilder().temperature(500))
                .color(0xff0000).secondaryColor(0x340605).iconSet(ROUGH)
                .flags(GENERATE_PLATE, NO_SMASHING, NO_SMELTING, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES,
                        EXCLUDE_PLATE_COMPRESSOR_RECIPE, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Silicon, 1, Pyrite, 5, Ruby, 1, Mercury, 3)
                .removeHazard()
                .buildAndRegister();

        Dichloroethane = new Material.Builder(GTCEu.id("dichloroethane"))
                .liquid()
                .color(0xafc979)
                .flags(DECOMPOSITION_BY_ELECTROLYZING)
                .components(Carbon, 2, Hydrogen, 4, Chlorine, 2)
                .buildAndRegister();

        Diethylenetriamine = new Material.Builder(GTCEu.id("diethylenetriamine"))
                .liquid()
                .color(0xa9d9a7)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 13, Nitrogen, 3)
                .hazard(HazardProperty.HazardTrigger.ANY, GTMedicalConditions.CHEMICAL_BURNS)
                .buildAndRegister();
    }
}
