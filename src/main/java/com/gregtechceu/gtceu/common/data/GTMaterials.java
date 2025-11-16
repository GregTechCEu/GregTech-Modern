package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterial;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlag;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.materials.*;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;

/**
 * Material Registration.
 * <p>
 * All Material Builders should follow this general formatting:
 * <p>
 * material = new MaterialBuilder(id, name)
 * .ingot().fluid().ore() <--- types
 * .color().iconSet() <--- appearance
 * .flags() <--- special generation
 * .element() / .components() <--- composition
 * .toolStats() <---
 * .oreByProducts() | additional properties
 * ... <---
 * .blastTemp() <--- blast temperature
 * .build();
 * <p>
 * Use defaults to your advantage! Some defaults:
 * - iconSet: DULL
 * - color: 0xFFFFFF
 */
public class GTMaterials {

    public static Material[] CHEMICAL_DYES;
    public static Material[] VOLTAGE_COMMON_MATERIALS;

    public static void init() {
        MarkerMaterials.register();

        ElementMaterials.register();
        FirstDegreeMaterials.register();
        OrganicChemistryMaterials.register();
        UnknownCompositionMaterials.register();
        SecondDegreeMaterials.register();
        HigherDegreeMaterials.register();

        // Gregicality Multiblocks
        GCYMMaterials.register();

        /*
         * Register info for cyclical references
         */
        MaterialFlagAddition.register();

        CHEMICAL_DYES = new Material[] {
                DyeWhite, DyeOrange,
                DyeMagenta, DyeLightBlue,
                DyeYellow, DyeLime,
                DyePink, DyeGray,
                DyeLightGray, DyeCyan,
                DyePurple, DyeBlue,
                DyeBrown, DyeGreen,
                DyeRed, DyeBlack
        };

        VOLTAGE_COMMON_MATERIALS = new Material[] {
                WroughtIron,
                Steel,
                Aluminium,
                StainlessSteel,
                Titanium,
                TungstenSteel,
                RhodiumPlatedPalladium,
                NaquadahAlloy,
                Darmstadtium,
                Neutronium
        };

        gemExquisite.setIgnored(Sugar);
        gemFlawless.setIgnored(Sugar);

        gem.setIgnored(Diamond, Items.DIAMOND);
        gem.setIgnored(Emerald, Items.EMERALD);
        gem.setIgnored(Lapis, Items.LAPIS_LAZULI);
        gem.setIgnored(NetherQuartz, Items.QUARTZ);
        gem.setIgnored(Coal, Items.COAL);
        gem.setIgnored(Amethyst, Items.AMETHYST_SHARD);
        gem.setIgnored(EchoShard, Items.ECHO_SHARD);
        excludeAllGems(Charcoal, Items.CHARCOAL);
        excludeAllGems(Flint, Items.FLINT);
        excludeAllGems(EnderPearl, Items.ENDER_PEARL);
        excludeAllGems(EnderEye, Items.ENDER_EYE);
        excludeAllGems(NetherStar, Items.NETHER_STAR);
        excludeAllGemsButNormal(Lapotron);

        dust.setIgnored(Redstone, Items.REDSTONE);
        dust.setIgnored(Glowstone, Items.GLOWSTONE_DUST);
        dust.setIgnored(Gunpowder, Items.GUNPOWDER);
        dust.setIgnored(Sugar, Items.SUGAR);
        dust.setIgnored(Bone, Items.BONE_MEAL);
        dust.setIgnored(Blaze, Items.BLAZE_POWDER);

        rod.setIgnored(Wood, Items.STICK);
        rod.setIgnored(Bone, Items.BONE);
        rod.setIgnored(Blaze, Items.BLAZE_ROD);
        rod.setIgnored(Paper);

        ingot.setIgnored(Iron, Items.IRON_INGOT);
        ingot.setIgnored(Gold, Items.GOLD_INGOT);
        ingot.setIgnored(Copper, Items.COPPER_INGOT);
        ingot.setIgnored(Netherite, Items.NETHERITE_INGOT);
        ingot.setIgnored(Brick, Items.BRICK);
        ingot.setIgnored(Wax, Items.HONEYCOMB);

        nugget.setIgnored(Gold, Items.GOLD_NUGGET);
        nugget.setIgnored(Iron, Items.IRON_NUGGET);

        plate.setIgnored(Paper, Items.PAPER);

        block.setIgnored(Iron, Blocks.IRON_BLOCK);
        block.setIgnored(Gold, Blocks.GOLD_BLOCK);
        block.setIgnored(Copper, Blocks.COPPER_BLOCK);
        block.setIgnored(Netherite, Items.NETHERITE_BLOCK);
        block.setIgnored(Lapis, Blocks.LAPIS_BLOCK);
        block.setIgnored(Emerald, Blocks.EMERALD_BLOCK);
        block.setIgnored(Redstone, Blocks.REDSTONE_BLOCK);
        block.setIgnored(Diamond, Blocks.DIAMOND_BLOCK);
        block.setIgnored(Coal, Blocks.COAL_BLOCK);
        block.setIgnored(Amethyst, Blocks.AMETHYST_BLOCK);
        block.setIgnored(Glass, Blocks.GLASS);
        block.setIgnored(Glowstone, Blocks.GLOWSTONE);
        block.setIgnored(Oilsands);
        block.setIgnored(Wood);
        block.setIgnored(TreatedWood);
        block.setIgnored(RawRubber);
        block.setIgnored(Clay, Blocks.CLAY);
        block.setIgnored(Brick, Blocks.BRICKS);
        block.setIgnored(Bone, Blocks.BONE_BLOCK);
        block.setIgnored(NetherQuartz, Blocks.QUARTZ_BLOCK);
        block.setIgnored(Ice, Blocks.ICE);
        block.setIgnored(Concrete, Blocks.WHITE_CONCRETE, Blocks.ORANGE_CONCRETE, Blocks.MAGENTA_CONCRETE,
                Blocks.LIGHT_BLUE_CONCRETE, Blocks.YELLOW_CONCRETE, Blocks.LIME_CONCRETE,
                Blocks.PINK_CONCRETE, Blocks.GRAY_CONCRETE, Blocks.LIGHT_GRAY_CONCRETE, Blocks.CYAN_CONCRETE,
                Blocks.PURPLE_CONCRETE, Blocks.BLUE_CONCRETE,
                Blocks.BROWN_CONCRETE, Blocks.GREEN_CONCRETE, Blocks.RED_CONCRETE, Blocks.BLACK_CONCRETE);
        block.setIgnored(Blaze);
        block.setIgnored(Lapotron);
        block.setIgnored(Wax, Blocks.HONEYCOMB_BLOCK);

        rock.setIgnored(Marble, GTMemoizer.memoizeBlockSupplier(() -> GTBlocks.MARBLE.get()));
        rock.setIgnored(Granite, Blocks.GRANITE);
        rock.setIgnored(Granite, Blocks.POLISHED_GRANITE);
        rock.setIgnored(GraniteRed, GTMemoizer.memoizeBlockSupplier(() -> GTBlocks.RED_GRANITE.get()));
        rock.setIgnored(Andesite, Blocks.ANDESITE);
        rock.setIgnored(Andesite, Blocks.POLISHED_ANDESITE);
        rock.setIgnored(Diorite, Blocks.DIORITE);
        rock.setIgnored(Diorite, Blocks.POLISHED_DIORITE);
        rock.setIgnored(Stone, Blocks.STONE);
        rock.setIgnored(Calcite, Blocks.CALCITE);
        rock.setIgnored(Netherrack, Blocks.NETHERRACK);
        rock.setIgnored(Obsidian, Blocks.OBSIDIAN);
        rock.setIgnored(Endstone, Blocks.END_STONE);
        rock.setIgnored(Deepslate, Blocks.DEEPSLATE);
        rock.setIgnored(Basalt, Blocks.BASALT);
        rock.setIgnored(Blackstone, Blocks.BLACKSTONE);
        block.setIgnored(Sculk, Blocks.SCULK);
        block.setIgnored(Concrete, GTMemoizer.memoizeBlockSupplier(() -> GTBlocks.DARK_CONCRETE.get()));
        block.setIgnored(Concrete, GTMemoizer.memoizeBlockSupplier(() -> GTBlocks.LIGHT_CONCRETE.get()));

        for (TagPrefix prefix : ORES.keySet()) {
            TagPrefix.OreType oreType = ORES.get(prefix);
            if (oreType.material() != null) {
                prefix.addSecondaryMaterial(new MaterialStack(oreType.material().get(), dust.materialAmount()));
            }
        }

        crushed.addSecondaryMaterial(new MaterialStack(Stone, dust.materialAmount()));

        toolHeadDrill.addSecondaryMaterial(new MaterialStack(Steel, plate.materialAmount() * 4));
        toolHeadChainsaw
                .addSecondaryMaterial(new MaterialStack(Steel, plate.materialAmount() * 4 + ring.materialAmount() * 2));
        toolHeadWrench
                .addSecondaryMaterial(new MaterialStack(Steel, ring.materialAmount() + screw.materialAmount() * 2));
        toolHeadWireCutter
                .addSecondaryMaterial(new MaterialStack(Steel, ring.materialAmount() + screw.materialAmount() * 2));

        pipeTinyFluid.setIgnored(Wood);
        pipeHugeFluid.setIgnored(Wood);
        pipeQuadrupleFluid.setIgnored(Wood);
        pipeNonupleFluid.setIgnored(Wood);
        pipeTinyFluid.setIgnored(TreatedWood);
        pipeHugeFluid.setIgnored(TreatedWood);
        pipeQuadrupleFluid.setIgnored(TreatedWood);
        pipeNonupleFluid.setIgnored(TreatedWood);

        pipeSmallRestrictive.addSecondaryMaterial(new MaterialStack(Iron, ring.materialAmount() * 2));
        pipeNormalRestrictive.addSecondaryMaterial(new MaterialStack(Iron, ring.materialAmount() * 2));
        pipeLargeRestrictive.addSecondaryMaterial(new MaterialStack(Iron, ring.materialAmount() * 2));
        pipeHugeRestrictive.addSecondaryMaterial(new MaterialStack(Iron, ring.materialAmount() * 2));

        cableGtSingle.addSecondaryMaterial(new MaterialStack(Rubber, plate.materialAmount()));
        cableGtDouble.addSecondaryMaterial(new MaterialStack(Rubber, plate.materialAmount()));
        cableGtQuadruple.addSecondaryMaterial(new MaterialStack(Rubber, plate.materialAmount() * 2));
        cableGtOctal.addSecondaryMaterial(new MaterialStack(Rubber, plate.materialAmount() * 3));
        cableGtHex.addSecondaryMaterial(new MaterialStack(Rubber, plate.materialAmount() * 5));

        plateDouble.setIgnored(BorosilicateGlass);
        plateDouble.setIgnored(Wood);
        plateDouble.setIgnored(TreatedWood);
        plate.setIgnored(BorosilicateGlass);
        foil.setIgnored(BorosilicateGlass);

        dye.setIgnored(DyeBlack, Items.BLACK_DYE);
        dye.setIgnored(DyeRed, Items.RED_DYE);
        dye.setIgnored(DyeGreen, Items.GREEN_DYE);
        dye.setIgnored(DyeBrown, Items.BROWN_DYE);
        dye.setIgnored(DyeBlue, Items.BLUE_DYE);
        dye.setIgnored(DyePurple, Items.PURPLE_DYE);
        dye.setIgnored(DyeCyan, Items.CYAN_DYE);
        dye.setIgnored(DyeLightGray, Items.LIGHT_GRAY_DYE);
        dye.setIgnored(DyeGray, Items.GRAY_DYE);
        dye.setIgnored(DyePink, Items.PINK_DYE);
        dye.setIgnored(DyeLime, Items.LIME_DYE);
        dye.setIgnored(DyeYellow, Items.YELLOW_DYE);
        dye.setIgnored(DyeLightBlue, Items.LIGHT_BLUE_DYE);
        dye.setIgnored(DyeMagenta, Items.MAGENTA_DYE);
        dye.setIgnored(DyeOrange, Items.ORANGE_DYE);
        dye.setIgnored(DyeWhite, Items.WHITE_DYE);

        // register vanilla materials

        rawOre.setIgnored(Gold, Items.RAW_GOLD);
        rawOre.setIgnored(Iron, Items.RAW_IRON);
        rawOre.setIgnored(Copper, Items.RAW_COPPER);
        rawOreBlock.setIgnored(Gold, Blocks.RAW_GOLD_BLOCK);
        rawOreBlock.setIgnored(Iron, Blocks.RAW_IRON_BLOCK);
        rawOreBlock.setIgnored(Copper, Blocks.RAW_COPPER_BLOCK);

        block.modifyMaterialAmount(Amethyst, 4);
        block.modifyMaterialAmount(EchoShard, 4);
        block.modifyMaterialAmount(Glowstone, 4);
        block.modifyMaterialAmount(NetherQuartz, 4);
        block.modifyMaterialAmount(CertusQuartz, 4);
        block.modifyMaterialAmount(Brick, 4);
        block.modifyMaterialAmount(Clay, 4);

        block.modifyMaterialAmount(Concrete, 1);
        block.modifyMaterialAmount(Glass, 1);
        block.modifyMaterialAmount(Ice, 1);
        block.modifyMaterialAmount(Obsidian, 1);
        block.modifyMaterialAmount(Sculk, 1);
        block.modifyMaterialAmount(Wax, 4);

        rod.modifyMaterialAmount(Blaze, 4);
        rod.modifyMaterialAmount(Bone, 5);
    }

    @NotNull
    public static Material get(String name) {
        var mat = GTCEuAPI.materialManager.getMaterial(name);
        // material could be null here due to the registry grabbing a material that isn't in the map
        if (mat == null) {
            GTCEu.LOGGER.warn("{} is not a known Material", name);
            return GTMaterials.NULL;
        }
        return mat;
    }

    private static void excludeAllGems(Material material, ItemLike... items) {
        gem.setIgnored(material, items);
        excludeAllGemsButNormal(material);
    }

    private static void excludeAllGemsButNormal(Material material) {
        gemChipped.setIgnored(material);
        gemFlawed.setIgnored(material);
        gemFlawless.setIgnored(material);
        gemExquisite.setIgnored(material);
    }

    public static final List<MaterialFlag> STD_METAL = new ArrayList<>();
    public static final List<MaterialFlag> EXT_METAL = new ArrayList<>();
    public static final List<MaterialFlag> EXT2_METAL = new ArrayList<>();

    static {
        STD_METAL.add(GENERATE_PLATE);

        EXT_METAL.addAll(STD_METAL);
        EXT_METAL.add(GENERATE_ROD);

        EXT2_METAL.addAll(EXT_METAL);
        EXT2_METAL.addAll(Arrays.asList(GENERATE_LONG_ROD, GENERATE_BOLT_SCREW));
    }

    public static final MarkerMaterial NULL = new MarkerMaterial(GTCEu.id("null"));

    /**
     * Direct Elements
     */
    public static Material Actinium;
    public static Material Aluminium;
    public static Material Americium;
    public static Material Antimony;
    public static Material Argon;
    public static Material Arsenic;
    public static Material Astatine;
    public static Material Barium;
    public static Material Berkelium;
    public static Material Beryllium;
    public static Material Bismuth;
    public static Material Bohrium;
    public static Material Boron;
    public static Material Bromine;
    public static Material Caesium;
    public static Material Calcium;
    public static Material Californium;
    public static Material Carbon;
    public static Material Cadmium;
    public static Material Cerium;
    public static Material Chlorine;
    public static Material Chromium;
    public static Material Cobalt;
    public static Material Copernicium;
    public static Material Copper;
    public static Material Curium;
    public static Material Darmstadtium;
    public static Material Deuterium;
    public static Material Dubnium;
    public static Material Dysprosium;
    public static Material Einsteinium;
    public static Material Erbium;
    public static Material Europium;
    public static Material Fermium;
    public static Material Flerovium;
    public static Material Fluorine;
    public static Material Francium;
    public static Material Gadolinium;
    public static Material Gallium;
    public static Material Germanium;
    public static Material Gold;
    public static Material Hafnium;
    public static Material Hassium;
    public static Material Holmium;
    public static Material Hydrogen;
    public static Material Helium;
    public static Material Helium3;
    public static Material Indium;
    public static Material Iodine;
    public static Material Iridium;
    public static Material Iron;
    public static Material Krypton;
    public static Material Lanthanum;
    public static Material Lawrencium;
    public static Material Lead;
    public static Material Lithium;
    public static Material Livermorium;
    public static Material Lutetium;
    public static Material Magnesium;
    public static Material Mendelevium;
    public static Material Manganese;
    public static Material Meitnerium;
    public static Material Mercury;
    public static Material Molybdenum;
    public static Material Moscovium;
    public static Material Neodymium;
    public static Material Neon;
    public static Material Neptunium;
    public static Material Nickel;
    public static Material Nihonium;
    public static Material Niobium;
    public static Material Nitrogen;
    public static Material Nobelium;
    public static Material Oganesson;
    public static Material Osmium;
    public static Material Oxygen;
    public static Material Palladium;
    public static Material Phosphorus;
    public static Material Polonium;
    public static Material Platinum;
    public static Material Plutonium239;
    public static Material Plutonium241;
    public static Material Potassium;
    public static Material Praseodymium;
    public static Material Promethium;
    public static Material Protactinium;
    public static Material Radon;
    public static Material Radium;
    public static Material Rhenium;
    public static Material Rhodium;
    public static Material Roentgenium;
    public static Material Rubidium;
    public static Material Ruthenium;
    public static Material Rutherfordium;
    public static Material Samarium;
    public static Material Scandium;
    public static Material Seaborgium;
    public static Material Selenium;
    public static Material Silicon;
    public static Material Silver;
    public static Material Sodium;
    public static Material Strontium;
    public static Material Sulfur;
    public static Material Tantalum;
    public static Material Technetium;
    public static Material Tellurium;
    public static Material Tennessine;
    public static Material Terbium;
    public static Material Thorium;
    public static Material Thallium;
    public static Material Thulium;
    public static Material Tin;
    public static Material Titanium;
    public static Material Tritium;
    public static Material Tungsten;
    public static Material Uranium238;
    public static Material Uranium235;
    public static Material Vanadium;
    public static Material Xenon;
    public static Material Ytterbium;
    public static Material Yttrium;
    public static Material Zinc;
    public static Material Zirconium;

    /**
     * Fantasy Elements
     */
    public static Material Naquadah;
    public static Material NaquadahEnriched;
    public static Material Naquadria;
    public static Material Neutronium;
    public static Material Tritanium;
    public static Material Duranium;
    public static Material Trinium;

    /**
     * First Degree Compounds
     */
    public static Material Almandine;
    public static Material Andradite;
    public static Material AnnealedCopper;
    public static Material Asbestos;
    public static Material Ash;
    public static Material Hematite;
    public static Material BatteryAlloy;
    public static Material BlueTopaz;
    public static Material Bone;
    public static Material Brass;
    public static Material Bronze;
    public static Material Goethite;
    public static Material Calcite;
    public static Material Cassiterite;
    public static Material CassiteriteSand;
    public static Material Chalcopyrite;
    public static Material Charcoal;
    public static Material Chromite;
    public static Material Cinnabar;
    public static Material Water;
    public static Material Coal;
    public static Material Cobaltite;
    public static Material Cooperite;
    public static Material Cupronickel;
    public static Material DarkAsh;
    public static Material Diamond;
    public static Material Electrum;
    public static Material Emerald;
    public static Material Galena;
    public static Material Garnierite;
    public static Material GreenSapphire;
    public static Material Grossular;
    public static Material Ice;
    public static Material Ilmenite;
    public static Material Rutile;
    public static Material Bauxite;
    public static Material Invar;
    public static Material Kanthal;
    public static Material Lazurite;
    public static Material Magnalium;
    public static Material Magnesite;
    public static Material Magnetite;
    public static Material Molybdenite;
    public static Material Nichrome;
    public static Material NiobiumNitride;
    public static Material NiobiumTitanium;
    public static Material Obsidian;
    public static Material Phosphate;
    public static Material SterlingSilver;
    public static Material RoseGold;
    public static Material BlackBronze;
    public static Material BismuthBronze;
    public static Material Biotite;
    public static Material Powellite;
    public static Material Pyrite;
    public static Material Pyrolusite;
    public static Material Pyrope;
    public static Material RockSalt;
    public static Material RTMAlloy;
    public static Material Ruridit;
    public static Material Rubber;
    public static Material Ruby;
    public static Material Salt;
    public static Material Saltpeter;
    public static Material Sapphire;
    public static Material Scheelite;
    public static Material Sodalite;
    public static Material AluminiumSulfite;
    public static Material Tantalite;
    public static Material Coke;
    public static Material Netherite;

    public static Material SolderingAlloy;
    public static Material Spessartine;
    public static Material Sphalerite;
    public static Material StainlessSteel;
    public static Material Steel;
    public static Material Stibnite;
    public static Material Tetrahedrite;
    public static Material TinAlloy;
    public static Material Topaz;
    public static Material Tungstate;
    public static Material Ultimet;
    public static Material Uraninite;
    public static Material Uvarovite;
    public static Material VanadiumGallium;
    public static Material WroughtIron;
    public static Material Wulfenite;
    public static Material Limonite;
    @Deprecated
    public static Material YellowLimonite;
    public static Material YttriumBariumCuprate;
    public static Material NetherQuartz;
    public static Material CertusQuartz;
    public static Material Quartzite;
    public static Material Graphite;
    public static Material Graphene;
    public static Material TungsticAcid;
    public static Material Osmiridium;
    public static Material LithiumChloride;
    public static Material CalciumChloride;
    public static Material Bornite;
    public static Material Chalcocite;

    public static Material GalliumArsenide;
    public static Material Potash;
    public static Material SodaAsh;
    public static Material IndiumGalliumPhosphide;
    public static Material NickelZincFerrite;
    public static Material SiliconDioxide;
    public static Material MagnesiumChloride;
    public static Material SodiumSulfide;
    public static Material PhosphorusPentoxide;
    public static Material Quicklime;
    public static Material SodiumBisulfate;
    public static Material FerriteMixture;
    public static Material Magnesia;
    public static Material PlatinumGroupSludge;
    public static Material Realgar;
    public static Material SodiumBicarbonate;
    public static Material PotassiumDichromate;
    public static Material ChromiumTrioxide;
    public static Material AntimonyTrioxide;
    public static Material Zincite;
    public static Material CupricOxide;
    public static Material CobaltOxide;
    public static Material ArsenicTrioxide;
    public static Material Massicot;
    public static Material Ferrosilite;
    public static Material MetalMixture;
    public static Material SodiumHydroxide;
    public static Material SodiumPersulfate;
    public static Material Bastnasite;
    public static Material Pentlandite;
    public static Material Spodumene;
    public static Material Lepidolite;
    public static Material GlauconiteSand;
    public static Material Malachite;
    public static Material Mica;
    public static Material Barite;
    public static Material Alunite;
    public static Material Talc;
    public static Material Soapstone;
    public static Material Kyanite;
    public static Material IronMagnetic;
    public static Material TungstenCarbide;
    public static Material CarbonDioxide;
    public static Material TitaniumTetrachloride;
    public static Material NitrogenDioxide;
    public static Material HydrogenSulfide;
    public static Material NitricAcid;
    public static Material SulfuricAcid;
    public static Material PhosphoricAcid;
    public static Material SulfurTrioxide;
    public static Material SulfurDioxide;
    public static Material CarbonMonoxide;
    public static Material HypochlorousAcid;
    public static Material Ammonia;
    public static Material HydrofluoricAcid;
    public static Material NitricOxide;
    public static Material Iron3Chloride;
    public static Material Iron2Chloride;
    public static Material UraniumHexafluoride;
    public static Material EnrichedUraniumHexafluoride;
    public static Material DepletedUraniumHexafluoride;
    public static Material NitrousOxide;
    public static Material EnderPearl;
    public static Material PotassiumFeldspar;
    public static Material NeodymiumMagnetic;
    public static Material HydrochloricAcid;
    public static Material Steam;
    public static Material DistilledWater;
    public static Material SodiumPotassium;
    public static Material SamariumMagnetic;
    public static Material ManganesePhosphide;
    public static Material MagnesiumDiboride;
    public static Material MercuryBariumCalciumCuprate;
    public static Material UraniumTriplatinum;
    public static Material SamariumIronArsenicOxide;
    public static Material IndiumTinBariumTitaniumCuprate;
    public static Material UraniumRhodiumDinaquadide;
    public static Material EnrichedNaquadahTriniumEuropiumDuranide;
    public static Material RutheniumTriniumAmericiumNeutronate;
    public static Material PlatinumRaw;
    public static Material InertMetalMixture;
    public static Material RhodiumSulfate;
    public static Material RutheniumTetroxide;
    public static Material OsmiumTetroxide;
    public static Material IridiumChloride;
    public static Material FluoroantimonicAcid;
    public static Material TitaniumTrifluoride;
    public static Material CalciumPhosphide;
    public static Material IndiumPhosphide;
    public static Material BariumSulfide;
    public static Material TriniumSulfide;
    public static Material ZincSulfide;
    public static Material GalliumSulfide;
    public static Material AntimonyTrifluoride;
    public static Material EnrichedNaquadahSulfate;
    public static Material NaquadriaSulfate;
    public static Material Pyrochlore;
    public static Material PotassiumHydroxide;
    public static Material PotassiumIodide;
    public static Material PotassiumFerrocyanide;
    public static Material CalciumFerrocyanide;
    public static Material CalciumHydroxide;
    public static Material CalciumCarbonate;
    public static Material PotassiumCyanide;
    public static Material PotassiumCarbonate;
    public static Material HydrogenCyanide;
    public static Material FormicAcid;
    public static Material PotassiumSulfate;
    public static Material PrussianBlue;
    public static Material Formaldehyde;
    public static Material Glycolonitrile;
    public static Material DiethylenetriaminePentaacetonitrile;
    public static Material DiethylenetriaminepentaaceticAcid;
    public static Material SodiumNitrite;
    public static Material HydrogenPeroxide;
    public static Material IlmeniteSlag;

    /**
     * Organic chemistry
     */
    public static Material SiliconeRubber;
    public static Material RawRubber;
    public static Material RawStyreneButadieneRubber;
    public static Material StyreneButadieneRubber;
    public static Material PolyvinylAcetate;
    public static Material ReinforcedEpoxyResin;
    public static Material PolyvinylChloride;
    public static Material PolyphenyleneSulfide;
    public static Material GlycerylTrinitrate;
    public static Material Polybenzimidazole;
    public static Material Polydimethylsiloxane;
    public static Material Polyethylene;
    public static Material Epoxy;
    public static Material Polycaprolactam;
    public static Material Polytetrafluoroethylene;
    public static Material Sugar;
    public static Material Methane;
    public static Material Epichlorohydrin;
    public static Material Monochloramine;
    public static Material Chloroform;
    public static Material Cumene;
    public static Material Tetrafluoroethylene;
    public static Material Chloromethane;
    public static Material AllylChloride;
    public static Material Isoprene;
    public static Material Propane;
    public static Material Propene;
    public static Material Ethane;
    public static Material Butene;
    public static Material Butane;
    public static Material DissolvedCalciumAcetate;
    public static Material VinylAcetate;
    public static Material MethylAcetate;
    public static Material Ethenone;
    public static Material Tetranitromethane;
    public static Material Dimethylamine;
    public static Material Dimethylhydrazine;
    public static Material DinitrogenTetroxide;
    public static Material Dimethyldichlorosilane;
    public static Material Styrene;
    public static Material Butadiene;
    public static Material Dichlorobenzene;
    public static Material AceticAcid;
    public static Material Phenol;
    public static Material BisphenolA;
    public static Material VinylChloride;
    public static Material Ethylene;
    public static Material Benzene;
    public static Material Acetone;
    public static Material Glycerol;
    public static Material Methanol;
    public static Material Ethanol;
    public static Material Toluene;
    public static Material DiphenylIsophtalate;
    public static Material PhthalicAcid;
    public static Material Dimethylbenzene;
    public static Material Diaminobenzidine;
    public static Material Dichlorobenzidine;
    public static Material Nitrochlorobenzene;
    public static Material Chlorobenzene;
    public static Material Octane;
    public static Material EthylTertButylEther;
    public static Material Ethylbenzene;
    public static Material Naphthalene;
    public static Material Nitrobenzene;
    public static Material Cyclohexane;
    public static Material NitrosylChloride;
    public static Material CyclohexanoneOxime;
    public static Material Caprolactam;
    public static Material PlatinumSludgeResidue;
    public static Material PalladiumRaw;
    public static Material RarestMetalMixture;
    public static Material AmmoniumChloride;
    public static Material AcidicOsmiumSolution;
    public static Material RhodiumPlatedPalladium;
    public static Material Butyraldehyde;
    public static Material PolyvinylButyral;
    public static Material Biphenyl;
    public static Material PolychlorinatedBiphenyl;
    public static Material AceticAnhydride;
    public static Material AminoPhenol;
    public static Material Paracetamol;
    public static Material AmmoniumFormate;
    public static Material Formamide;

    /**
     * Not possible to determine exact Components
     */
    public static Material WoodGas;
    public static Material WoodVinegar;
    public static Material WoodTar;
    public static Material CharcoalByproducts;
    public static Material Biomass;
    public static Material BioDiesel;
    public static Material FermentedBiomass;
    public static Material Creosote;
    public static Material Diesel;
    public static Material RocketFuel;
    public static Material Glue;
    public static Material Lubricant;
    public static Material McGuffium239;
    public static Material IndiumConcentrate;
    public static Material SeedOil;
    public static Material DrillingFluid;
    public static Material ConstructionFoam;

    public static Material Oil;
    public static Material OilHeavy;
    public static Material RawOil;
    public static Material OilLight;
    public static Material NaturalGas;
    public static Material SulfuricHeavyFuel;
    public static Material HeavyFuel;
    public static Material LightlyHydroCrackedHeavyFuel;
    public static Material SeverelyHydroCrackedHeavyFuel;
    public static Material LightlySteamCrackedHeavyFuel;
    public static Material SeverelySteamCrackedHeavyFuel;
    public static Material SulfuricLightFuel;
    public static Material LightFuel;
    public static Material LightlyHydroCrackedLightFuel;
    public static Material SeverelyHydroCrackedLightFuel;
    public static Material LightlySteamCrackedLightFuel;
    public static Material SeverelySteamCrackedLightFuel;
    public static Material SulfuricNaphtha;
    public static Material Naphtha;
    public static Material LightlyHydroCrackedNaphtha;
    public static Material SeverelyHydroCrackedNaphtha;
    public static Material LightlySteamCrackedNaphtha;
    public static Material SeverelySteamCrackedNaphtha;
    public static Material SulfuricGas;
    public static Material RefineryGas;
    public static Material LightlyHydroCrackedGas;
    public static Material SeverelyHydroCrackedGas;
    public static Material LightlySteamCrackedGas;
    public static Material SeverelySteamCrackedGas;
    public static Material HydroCrackedEthane;
    public static Material HydroCrackedEthylene;
    public static Material HydroCrackedPropene;
    public static Material HydroCrackedPropane;
    public static Material HydroCrackedButane;
    public static Material HydroCrackedButene;
    public static Material HydroCrackedButadiene;
    public static Material SteamCrackedEthane;
    public static Material SteamCrackedEthylene;
    public static Material SteamCrackedPropene;
    public static Material SteamCrackedPropane;
    public static Material SteamCrackedButane;
    public static Material SteamCrackedButene;
    public static Material SteamCrackedButadiene;
    public static Material LPG;

    public static Material RawGrowthMedium;
    public static Material SterileGrowthMedium;
    public static Material Bacteria;
    public static Material BacterialSludge;
    public static Material EnrichedBacterialSludge;
    public static Material Mutagen;
    public static Material GelatinMixture;
    public static Material RawGasoline;
    public static Material Gasoline;
    public static Material HighOctaneGasoline;
    public static Material CoalGas;
    public static Material CoalTar;
    public static Material Gunpowder;
    public static Material Oilsands;
    public static Material RareEarth;
    public static Material Stone;
    public static Material Lava;
    public static Material Glowstone;
    public static Material NetherStar;
    public static Material Endstone;
    public static Material Netherrack;
    public static Material CetaneBoostedDiesel;
    public static Material Collagen;
    public static Material Gelatin;
    public static Material Agar;
    public static Material Andesite;
    public static Material Milk;
    public static Material Cocoa;
    public static Material Wheat;
    public static Material Meat;
    public static Material Wood;
    public static Material TreatedWood;
    public static Material Paper;
    public static Material FishOil;
    public static Material RubySlurry;
    public static Material SapphireSlurry;
    public static Material GreenSapphireSlurry;
    public static Material DyeBlack;
    public static Material DyeRed;
    public static Material DyeGreen;
    public static Material DyeBrown;
    public static Material DyeBlue;
    public static Material DyePurple;
    public static Material DyeCyan;
    public static Material DyeLightGray;
    public static Material DyeGray;
    public static Material DyePink;
    public static Material DyeLime;
    public static Material DyeYellow;
    public static Material DyeLightBlue;
    public static Material DyeMagenta;
    public static Material DyeOrange;
    public static Material DyeWhite;

    public static Material ImpureEnrichedNaquadahSolution;
    public static Material EnrichedNaquadahSolution;
    public static Material AcidicEnrichedNaquadahSolution;
    public static Material EnrichedNaquadahWaste;
    public static Material ImpureNaquadriaSolution;
    public static Material NaquadriaSolution;
    public static Material AcidicNaquadriaSolution;
    public static Material NaquadriaWaste;
    public static Material Lapotron;
    public static Material UUMatter;
    public static Material PCBCoolant;
    public static Material Sculk;
    public static Material Wax;
    public static Material BauxiteSlurry;
    public static Material CrackedBauxiteSlurry;
    public static Material BauxiteSludge;
    public static Material DecalcifiedBauxiteSludge;
    public static Material BauxiteSlag;

    /**
     * Second Degree Compounds
     */
    public static Material Glass;
    public static Material Perlite;
    public static Material Borax;
    public static Material Olivine;
    public static Material Opal;
    public static Material Amethyst;
    public static Material EchoShard;
    public static Material Lapis;
    public static Material Blaze;
    public static Material Apatite;
    public static Material BlackSteel;
    public static Material DamascusSteel;
    public static Material TungstenSteel;
    public static Material CobaltBrass;
    public static Material TricalciumPhosphate;
    public static Material GarnetRed;
    public static Material GarnetYellow;
    public static Material Marble;
    public static Material Deepslate;
    public static Material GraniteRed;
    public static Material Blackstone;
    public static Material VanadiumMagnetite;
    public static Material QuartzSand;
    public static Material Pollucite;
    public static Material Bentonite;
    public static Material FullersEarth;
    public static Material Pitchblende;
    public static Material Monazite;
    public static Material Mirabilite;
    public static Material ActivatedCarbon;
    public static Material Trona;
    public static Material Gypsum;
    public static Material Zeolite;
    public static Material Concrete;
    public static Material SteelMagnetic;
    public static Material VanadiumSteel;
    public static Material Potin;
    public static Material BorosilicateGlass;
    public static Material NaquadahAlloy;
    public static Material SulfuricNickelSolution;
    public static Material SulfuricCopperSolution;
    public static Material LeadZincSolution;
    public static Material NitrationMixture;
    public static Material DilutedSulfuricAcid;
    public static Material DilutedHydrochloricAcid;
    public static Material Flint;
    public static Material Air;
    public static Material LiquidAir;
    public static Material NetherAir;
    public static Material LiquidNetherAir;
    public static Material EnderAir;
    public static Material LiquidEnderAir;
    public static Material AquaRegia;
    public static Material SaltWater;
    public static Material Clay;
    public static Material Redstone;
    public static Material Dichloroethane;
    public static Material Diethylenetriamine;

    /**
     * Third Degree Materials
     */
    public static Material Electrotine;
    public static Material EnderEye;
    public static Material Diatomite;
    public static Material RedSteel;
    public static Material BlueSteel;
    public static Material Basalt;
    public static Material GraniticMineralSand;
    public static Material Redrock;
    public static Material GarnetSand;
    public static Material HSSG;
    public static Material IridiumMetalResidue;
    public static Material Granite;
    public static Material Brick;
    public static Material Fireclay;
    public static Material Diorite;

    /**
     * Fourth Degree Materials
     */
    public static Material RedAlloy;
    public static Material BlueAlloy;
    public static Material BasalticMineralSand;
    public static Material HSSE;
    public static Material HSSS;
    public static Material RadAway;

    /**
     * GCYM Materials
     */
    public static Material TantalumCarbide;
    public static Material HSLASteel;
    public static Material MolybdenumDisilicide;
    public static Material Zeron100;
    public static Material WatertightSteel;
    public static Material IncoloyMA956;
    public static Material MaragingSteel300;
    public static Material HastelloyX;
    public static Material Stellite100;
    public static Material TitaniumCarbide;
    public static Material TitaniumTungstenCarbide;
    public static Material HastelloyC276;
}
