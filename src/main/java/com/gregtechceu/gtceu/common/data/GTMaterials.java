package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterial;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlag;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MaterialEntry;
import com.gregtechceu.gtceu.common.data.materials.*;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;

import net.minecraft.resources.ResourceLocation;
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

    public static MaterialEntry[] CHEMICAL_DYES;
    public static MaterialEntry[] VOLTAGE_COMMON_MATERIALS;

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

        CHEMICAL_DYES = new MaterialEntry[] {
                DyeWhite, DyeOrange,
                DyeMagenta, DyeLightBlue,
                DyeYellow, DyeLime,
                DyePink, DyeGray,
                DyeLightGray, DyeCyan,
                DyePurple, DyeBlue,
                DyeBrown, DyeGreen,
                DyeRed, DyeBlack
        };

        VOLTAGE_COMMON_MATERIALS = new MaterialEntry[] {
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
        excludeAllGems(Wax, Items.HONEYCOMB);
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
        rock.setIgnored(RedGranite, GTMemoizer.memoizeBlockSupplier(() -> GTBlocks.RED_GRANITE.get()));
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
                prefix.addSecondaryMaterial(new MaterialStack(oreType.material(), dust.materialAmount()));
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
        var mat = GTRegistries.MATERIALS.get(ResourceLocation.parse(name));
        // material could be null here due to the registry grabbing a material that isn't in the map
        if (mat == null || mat.isNull()) {
            GTCEu.LOGGER.warn("{} is not a known Material", name);
            return GTMaterials.NULL.get();
        }
        return mat;
    }

    private static void excludeAllGems(MaterialEntry material, ItemLike... items) {
        gem.setIgnored(material, items);
        excludeAllGemsButNormal(material);
    }

    private static void excludeAllGemsButNormal(MaterialEntry material) {
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

    public static final MaterialEntry NULL = GTRegistration.REGISTRATE.entry("null", (callback) -> new Material.Builder<>(GTRegistration.REGISTRATE, GTRegistration.REGISTRATE, "null", callback) {
                @Override
                public @NotNull Material createEntry() {
                    return new MarkerMaterial(GTCEu.id("null"));
                }
            })
            .register();

    /**
     * Direct Elements
     */
    public static MaterialEntry Actinium;
    public static MaterialEntry Aluminium;
    public static MaterialEntry Americium;
    public static MaterialEntry Antimony;
    public static MaterialEntry Argon;
    public static MaterialEntry Arsenic;
    public static MaterialEntry Astatine;
    public static MaterialEntry Barium;
    public static MaterialEntry Berkelium;
    public static MaterialEntry Beryllium;
    public static MaterialEntry Bismuth;
    public static MaterialEntry Bohrium;
    public static MaterialEntry Boron;
    public static MaterialEntry Bromine;
    public static MaterialEntry Caesium;
    public static MaterialEntry Calcium;
    public static MaterialEntry Californium;
    public static MaterialEntry Carbon;
    public static MaterialEntry Cadmium;
    public static MaterialEntry Cerium;
    public static MaterialEntry Chlorine;
    public static MaterialEntry Chromium;
    public static MaterialEntry Cobalt;
    public static MaterialEntry Copernicium;
    public static MaterialEntry Copper;
    public static MaterialEntry Curium;
    public static MaterialEntry Darmstadtium;
    public static MaterialEntry Deuterium;
    public static MaterialEntry Dubnium;
    public static MaterialEntry Dysprosium;
    public static MaterialEntry Einsteinium;
    public static MaterialEntry Erbium;
    public static MaterialEntry Europium;
    public static MaterialEntry Fermium;
    public static MaterialEntry Flerovium;
    public static MaterialEntry Fluorine;
    public static MaterialEntry Francium;
    public static MaterialEntry Gadolinium;
    public static MaterialEntry Gallium;
    public static MaterialEntry Germanium;
    public static MaterialEntry Gold;
    public static MaterialEntry Hafnium;
    public static MaterialEntry Hassium;
    public static MaterialEntry Holmium;
    public static MaterialEntry Hydrogen;
    public static MaterialEntry Helium;
    public static MaterialEntry Helium3;
    public static MaterialEntry Indium;
    public static MaterialEntry Iodine;
    public static MaterialEntry Iridium;
    public static MaterialEntry Iron;
    public static MaterialEntry Krypton;
    public static MaterialEntry Lanthanum;
    public static MaterialEntry Lawrencium;
    public static MaterialEntry Lead;
    public static MaterialEntry Lithium;
    public static MaterialEntry Livermorium;
    public static MaterialEntry Lutetium;
    public static MaterialEntry Magnesium;
    public static MaterialEntry Mendelevium;
    public static MaterialEntry Manganese;
    public static MaterialEntry Meitnerium;
    public static MaterialEntry Mercury;
    public static MaterialEntry Molybdenum;
    public static MaterialEntry Moscovium;
    public static MaterialEntry Neodymium;
    public static MaterialEntry Neon;
    public static MaterialEntry Neptunium;
    public static MaterialEntry Nickel;
    public static MaterialEntry Nihonium;
    public static MaterialEntry Niobium;
    public static MaterialEntry Nitrogen;
    public static MaterialEntry Nobelium;
    public static MaterialEntry Oganesson;
    public static MaterialEntry Osmium;
    public static MaterialEntry Oxygen;
    public static MaterialEntry Palladium;
    public static MaterialEntry Phosphorus;
    public static MaterialEntry Polonium;
    public static MaterialEntry Platinum;
    public static MaterialEntry Plutonium239;
    public static MaterialEntry Plutonium241;
    public static MaterialEntry Potassium;
    public static MaterialEntry Praseodymium;
    public static MaterialEntry Promethium;
    public static MaterialEntry Protactinium;
    public static MaterialEntry Radon;
    public static MaterialEntry Radium;
    public static MaterialEntry Rhenium;
    public static MaterialEntry Rhodium;
    public static MaterialEntry Roentgenium;
    public static MaterialEntry Rubidium;
    public static MaterialEntry Ruthenium;
    public static MaterialEntry Rutherfordium;
    public static MaterialEntry Samarium;
    public static MaterialEntry Scandium;
    public static MaterialEntry Seaborgium;
    public static MaterialEntry Selenium;
    public static MaterialEntry Silicon;
    public static MaterialEntry Silver;
    public static MaterialEntry Sodium;
    public static MaterialEntry Strontium;
    public static MaterialEntry Sulfur;
    public static MaterialEntry Tantalum;
    public static MaterialEntry Technetium;
    public static MaterialEntry Tellurium;
    public static MaterialEntry Tennessine;
    public static MaterialEntry Terbium;
    public static MaterialEntry Thorium;
    public static MaterialEntry Thallium;
    public static MaterialEntry Thulium;
    public static MaterialEntry Tin;
    public static MaterialEntry Titanium;
    public static MaterialEntry Tritium;
    public static MaterialEntry Tungsten;
    public static MaterialEntry Uranium238;
    public static MaterialEntry Uranium235;
    public static MaterialEntry Vanadium;
    public static MaterialEntry Xenon;
    public static MaterialEntry Ytterbium;
    public static MaterialEntry Yttrium;
    public static MaterialEntry Zinc;
    public static MaterialEntry Zirconium;

    /**
     * Fantasy Elements
     */
    public static MaterialEntry Naquadah;
    public static MaterialEntry NaquadahEnriched;
    public static MaterialEntry Naquadria;
    public static MaterialEntry Neutronium;
    public static MaterialEntry Tritanium;
    public static MaterialEntry Duranium;
    public static MaterialEntry Trinium;

    /**
     * First Degree Compounds
     */
    public static MaterialEntry Almandine;
    public static MaterialEntry Andradite;
    public static MaterialEntry AnnealedCopper;
    public static MaterialEntry Asbestos;
    public static MaterialEntry Ash;
    public static MaterialEntry Hematite;
    public static MaterialEntry BatteryAlloy;
    public static MaterialEntry BlueTopaz;
    public static MaterialEntry Bone;
    public static MaterialEntry Brass;
    public static MaterialEntry Bronze;
    public static MaterialEntry Goethite;
    public static MaterialEntry Calcite;
    public static MaterialEntry Cassiterite;
    public static MaterialEntry CassiteriteSand;
    public static MaterialEntry Chalcopyrite;
    public static MaterialEntry Charcoal;
    public static MaterialEntry Chromite;
    public static MaterialEntry Cinnabar;
    public static MaterialEntry Water;
    public static MaterialEntry Coal;
    public static MaterialEntry Cobaltite;
    public static MaterialEntry Cooperite;
    public static MaterialEntry Cupronickel;
    public static MaterialEntry DarkAsh;
    public static MaterialEntry Diamond;
    public static MaterialEntry Electrum;
    public static MaterialEntry Emerald;
    public static MaterialEntry Galena;
    public static MaterialEntry Garnierite;
    public static MaterialEntry GreenSapphire;
    public static MaterialEntry Grossular;
    public static MaterialEntry Ice;
    public static MaterialEntry Ilmenite;
    public static MaterialEntry Rutile;
    public static MaterialEntry Bauxite;
    public static MaterialEntry Invar;
    public static MaterialEntry Kanthal;
    public static MaterialEntry Lazurite;
    public static MaterialEntry Magnalium;
    public static MaterialEntry Magnesite;
    public static MaterialEntry Magnetite;
    public static MaterialEntry Molybdenite;
    public static MaterialEntry Nichrome;
    public static MaterialEntry NiobiumNitride;
    public static MaterialEntry NiobiumTitanium;
    public static MaterialEntry Obsidian;
    public static MaterialEntry Phosphate;
    public static MaterialEntry SterlingSilver;
    public static MaterialEntry RoseGold;
    public static MaterialEntry BlackBronze;
    public static MaterialEntry BismuthBronze;
    public static MaterialEntry Biotite;
    public static MaterialEntry Powellite;
    public static MaterialEntry Pyrite;
    public static MaterialEntry Pyrolusite;
    public static MaterialEntry Pyrope;
    public static MaterialEntry RockSalt;
    public static MaterialEntry RTMAlloy;
    public static MaterialEntry Ruridit;
    public static MaterialEntry Rubber;
    public static MaterialEntry Ruby;
    public static MaterialEntry Salt;
    public static MaterialEntry Saltpeter;
    public static MaterialEntry Sapphire;
    public static MaterialEntry Scheelite;
    public static MaterialEntry Sodalite;
    public static MaterialEntry AluminiumSulfite;
    public static MaterialEntry Tantalite;
    public static MaterialEntry Coke;
    public static MaterialEntry Netherite;

    public static MaterialEntry SolderingAlloy;
    public static MaterialEntry Spessartine;
    public static MaterialEntry Sphalerite;
    public static MaterialEntry StainlessSteel;
    public static MaterialEntry Steel;
    public static MaterialEntry Stibnite;
    public static MaterialEntry Tetrahedrite;
    public static MaterialEntry TinAlloy;
    public static MaterialEntry Topaz;
    public static MaterialEntry Tungstate;
    public static MaterialEntry Ultimet;
    public static MaterialEntry Uraninite;
    public static MaterialEntry Uvarovite;
    public static MaterialEntry VanadiumGallium;
    public static MaterialEntry WroughtIron;
    public static MaterialEntry Wulfenite;
    public static MaterialEntry Limonite;
    public static MaterialEntry YellowLimonite;
    public static MaterialEntry YttriumBariumCuprate;
    public static MaterialEntry NetherQuartz;
    public static MaterialEntry CertusQuartz;
    public static MaterialEntry Quartzite;
    public static MaterialEntry Graphite;
    public static MaterialEntry Graphene;
    public static MaterialEntry TungsticAcid;
    public static MaterialEntry Osmiridium;
    public static MaterialEntry LithiumChloride;
    public static MaterialEntry CalciumChloride;
    public static MaterialEntry Bornite;
    public static MaterialEntry Chalcocite;

    public static MaterialEntry GalliumArsenide;
    public static MaterialEntry Potash;
    public static MaterialEntry SodaAsh;
    public static MaterialEntry IndiumGalliumPhosphide;
    public static MaterialEntry NickelZincFerrite;
    public static MaterialEntry SiliconDioxide;
    public static MaterialEntry MagnesiumChloride;
    public static MaterialEntry SodiumSulfide;
    public static MaterialEntry PhosphorusPentoxide;
    public static MaterialEntry Quicklime;
    public static MaterialEntry SodiumBisulfate;
    public static MaterialEntry FerriteMixture;
    public static MaterialEntry Magnesia;
    public static MaterialEntry PlatinumGroupSludge;
    public static MaterialEntry Realgar;
    public static MaterialEntry SodiumBicarbonate;
    public static MaterialEntry PotassiumDichromate;
    public static MaterialEntry ChromiumTrioxide;
    public static MaterialEntry AntimonyTrioxide;
    public static MaterialEntry Zincite;
    public static MaterialEntry CupricOxide;
    public static MaterialEntry CobaltOxide;
    public static MaterialEntry ArsenicTrioxide;
    public static MaterialEntry Massicot;
    public static MaterialEntry Ferrosilite;
    public static MaterialEntry MetalMixture;
    public static MaterialEntry SodiumHydroxide;
    public static MaterialEntry SodiumPersulfate;
    public static MaterialEntry Bastnasite;
    public static MaterialEntry Pentlandite;
    public static MaterialEntry Spodumene;
    public static MaterialEntry Lepidolite;
    public static MaterialEntry GlauconiteSand;
    public static MaterialEntry Malachite;
    public static MaterialEntry Mica;
    public static MaterialEntry Barite;
    public static MaterialEntry Alunite;
    public static MaterialEntry Talc;
    public static MaterialEntry Soapstone;
    public static MaterialEntry Kyanite;
    public static MaterialEntry IronMagnetic;
    public static MaterialEntry TungstenCarbide;
    public static MaterialEntry CarbonDioxide;
    public static MaterialEntry TitaniumTetrachloride;
    public static MaterialEntry NitrogenDioxide;
    public static MaterialEntry HydrogenSulfide;
    public static MaterialEntry NitricAcid;
    public static MaterialEntry SulfuricAcid;
    public static MaterialEntry PhosphoricAcid;
    public static MaterialEntry SulfurTrioxide;
    public static MaterialEntry SulfurDioxide;
    public static MaterialEntry CarbonMonoxide;
    public static MaterialEntry HypochlorousAcid;
    public static MaterialEntry Ammonia;
    public static MaterialEntry HydrofluoricAcid;
    public static MaterialEntry NitricOxide;
    public static MaterialEntry Iron3Chloride;
    public static MaterialEntry Iron2Chloride;
    public static MaterialEntry UraniumHexafluoride;
    public static MaterialEntry EnrichedUraniumHexafluoride;
    public static MaterialEntry DepletedUraniumHexafluoride;
    public static MaterialEntry NitrousOxide;
    public static MaterialEntry EnderPearl;
    public static MaterialEntry PotassiumFeldspar;
    public static MaterialEntry NeodymiumMagnetic;
    public static MaterialEntry HydrochloricAcid;
    public static MaterialEntry Steam;
    public static MaterialEntry DistilledWater;
    public static MaterialEntry SodiumPotassium;
    public static MaterialEntry SamariumMagnetic;
    public static MaterialEntry ManganesePhosphide;
    public static MaterialEntry MagnesiumDiboride;
    public static MaterialEntry MercuryBariumCalciumCuprate;
    public static MaterialEntry UraniumTriplatinum;
    public static MaterialEntry SamariumIronArsenicOxide;
    public static MaterialEntry IndiumTinBariumTitaniumCuprate;
    public static MaterialEntry UraniumRhodiumDinaquadide;
    public static MaterialEntry EnrichedNaquadahTriniumEuropiumDuranide;
    public static MaterialEntry RutheniumTriniumAmericiumNeutronate;
    public static MaterialEntry PlatinumRaw;
    public static MaterialEntry InertMetalMixture;
    public static MaterialEntry RhodiumSulfate;
    public static MaterialEntry RutheniumTetroxide;
    public static MaterialEntry OsmiumTetroxide;
    public static MaterialEntry IridiumChloride;
    public static MaterialEntry FluoroantimonicAcid;
    public static MaterialEntry TitaniumTrifluoride;
    public static MaterialEntry CalciumPhosphide;
    public static MaterialEntry IndiumPhosphide;
    public static MaterialEntry BariumSulfide;
    public static MaterialEntry TriniumSulfide;
    public static MaterialEntry ZincSulfide;
    public static MaterialEntry GalliumSulfide;
    public static MaterialEntry AntimonyTrifluoride;
    public static MaterialEntry EnrichedNaquadahSulfate;
    public static MaterialEntry NaquadriaSulfate;
    public static MaterialEntry Pyrochlore;
    public static MaterialEntry PotassiumHydroxide;
    public static MaterialEntry PotassiumIodide;
    public static MaterialEntry PotassiumFerrocyanide;
    public static MaterialEntry CalciumFerrocyanide;
    public static MaterialEntry CalciumHydroxide;
    public static MaterialEntry CalciumCarbonate;
    public static MaterialEntry PotassiumCyanide;
    public static MaterialEntry PotassiumCarbonate;
    public static MaterialEntry HydrogenCyanide;
    public static MaterialEntry FormicAcid;
    public static MaterialEntry PotassiumSulfate;
    public static MaterialEntry PrussianBlue;
    public static MaterialEntry Formaldehyde;
    public static MaterialEntry Glycolonitrile;
    public static MaterialEntry DiethylenetriaminePentaacetonitrile;
    public static MaterialEntry DiethylenetriaminepentaaceticAcid;
    public static MaterialEntry SodiumNitrite;
    public static MaterialEntry HydrogenPeroxide;
    public static MaterialEntry IlmeniteSlag;

    /**
     * Organic chemistry
     */
    public static MaterialEntry SiliconeRubber;
    public static MaterialEntry RawRubber;
    public static MaterialEntry RawStyreneButadieneRubber;
    public static MaterialEntry StyreneButadieneRubber;
    public static MaterialEntry PolyvinylAcetate;
    public static MaterialEntry ReinforcedEpoxyResin;
    public static MaterialEntry PolyvinylChloride;
    public static MaterialEntry PolyphenyleneSulfide;
    public static MaterialEntry GlycerylTrinitrate;
    public static MaterialEntry Polybenzimidazole;
    public static MaterialEntry Polydimethylsiloxane;
    public static MaterialEntry Polyethylene;
    public static MaterialEntry Epoxy;
    public static MaterialEntry Polycaprolactam;
    public static MaterialEntry Polytetrafluoroethylene;
    public static MaterialEntry Sugar;
    public static MaterialEntry Methane;
    public static MaterialEntry Epichlorohydrin;
    public static MaterialEntry Monochloramine;
    public static MaterialEntry Chloroform;
    public static MaterialEntry Cumene;
    public static MaterialEntry Tetrafluoroethylene;
    public static MaterialEntry Chloromethane;
    public static MaterialEntry AllylChloride;
    public static MaterialEntry Isoprene;
    public static MaterialEntry Propane;
    public static MaterialEntry Propene;
    public static MaterialEntry Ethane;
    public static MaterialEntry Butene;
    public static MaterialEntry Butane;
    public static MaterialEntry DissolvedCalciumAcetate;
    public static MaterialEntry VinylAcetate;
    public static MaterialEntry MethylAcetate;
    public static MaterialEntry Ethenone;
    public static MaterialEntry Tetranitromethane;
    public static MaterialEntry Dimethylamine;
    public static MaterialEntry Dimethylhydrazine;
    public static MaterialEntry DinitrogenTetroxide;
    public static MaterialEntry Dimethyldichlorosilane;
    public static MaterialEntry Styrene;
    public static MaterialEntry Butadiene;
    public static MaterialEntry Dichlorobenzene;
    public static MaterialEntry AceticAcid;
    public static MaterialEntry Phenol;
    public static MaterialEntry BisphenolA;
    public static MaterialEntry VinylChloride;
    public static MaterialEntry Ethylene;
    public static MaterialEntry Benzene;
    public static MaterialEntry Acetone;
    public static MaterialEntry Glycerol;
    public static MaterialEntry Methanol;
    public static MaterialEntry Ethanol;
    public static MaterialEntry Toluene;
    public static MaterialEntry DiphenylIsophtalate;
    public static MaterialEntry PhthalicAcid;
    public static MaterialEntry Dimethylbenzene;
    public static MaterialEntry Diaminobenzidine;
    public static MaterialEntry Dichlorobenzidine;
    public static MaterialEntry Nitrochlorobenzene;
    public static MaterialEntry Chlorobenzene;
    public static MaterialEntry Octane;
    public static MaterialEntry EthylTertButylEther;
    public static MaterialEntry Ethylbenzene;
    public static MaterialEntry Naphthalene;
    public static MaterialEntry Nitrobenzene;
    public static MaterialEntry Cyclohexane;
    public static MaterialEntry NitrosylChloride;
    public static MaterialEntry CyclohexanoneOxime;
    public static MaterialEntry Caprolactam;
    public static MaterialEntry PlatinumSludgeResidue;
    public static MaterialEntry PalladiumRaw;
    public static MaterialEntry RarestMetalMixture;
    public static MaterialEntry AmmoniumChloride;
    public static MaterialEntry AcidicOsmiumSolution;
    public static MaterialEntry RhodiumPlatedPalladium;
    public static MaterialEntry Butyraldehyde;
    public static MaterialEntry PolyvinylButyral;
    public static MaterialEntry Biphenyl;
    public static MaterialEntry PolychlorinatedBiphenyl;
    public static MaterialEntry AceticAnhydride;
    public static MaterialEntry AminoPhenol;
    public static MaterialEntry Paracetamol;
    public static MaterialEntry AmmoniumFormate;
    public static MaterialEntry Formamide;

    /**
     * Not possible to determine exact Components
     */
    public static MaterialEntry WoodGas;
    public static MaterialEntry WoodVinegar;
    public static MaterialEntry WoodTar;
    public static MaterialEntry CharcoalByproducts;
    public static MaterialEntry Biomass;
    public static MaterialEntry BioDiesel;
    public static MaterialEntry FermentedBiomass;
    public static MaterialEntry Creosote;
    public static MaterialEntry Diesel;
    public static MaterialEntry RocketFuel;
    public static MaterialEntry Glue;
    public static MaterialEntry Lubricant;
    public static MaterialEntry McGuffium239;
    public static MaterialEntry IndiumConcentrate;
    public static MaterialEntry SeedOil;
    public static MaterialEntry DrillingFluid;
    public static MaterialEntry ConstructionFoam;

    public static MaterialEntry Oil;
    public static MaterialEntry HeavyOil;
    public static MaterialEntry RawOil;
    public static MaterialEntry LightOil;
    public static MaterialEntry NaturalGas;
    public static MaterialEntry SulfuricHeavyFuel;
    public static MaterialEntry HeavyFuel;
    public static MaterialEntry LightlyHydroCrackedHeavyFuel;
    public static MaterialEntry SeverelyHydroCrackedHeavyFuel;
    public static MaterialEntry LightlySteamCrackedHeavyFuel;
    public static MaterialEntry SeverelySteamCrackedHeavyFuel;
    public static MaterialEntry SulfuricLightFuel;
    public static MaterialEntry LightFuel;
    public static MaterialEntry LightlyHydroCrackedLightFuel;
    public static MaterialEntry SeverelyHydroCrackedLightFuel;
    public static MaterialEntry LightlySteamCrackedLightFuel;
    public static MaterialEntry SeverelySteamCrackedLightFuel;
    public static MaterialEntry SulfuricNaphtha;
    public static MaterialEntry Naphtha;
    public static MaterialEntry LightlyHydroCrackedNaphtha;
    public static MaterialEntry SeverelyHydroCrackedNaphtha;
    public static MaterialEntry LightlySteamCrackedNaphtha;
    public static MaterialEntry SeverelySteamCrackedNaphtha;
    public static MaterialEntry SulfuricGas;
    public static MaterialEntry RefineryGas;
    public static MaterialEntry LightlyHydroCrackedGas;
    public static MaterialEntry SeverelyHydroCrackedGas;
    public static MaterialEntry LightlySteamCrackedGas;
    public static MaterialEntry SeverelySteamCrackedGas;
    public static MaterialEntry HydroCrackedEthane;
    public static MaterialEntry HydroCrackedEthylene;
    public static MaterialEntry HydroCrackedPropene;
    public static MaterialEntry HydroCrackedPropane;
    public static MaterialEntry HydroCrackedButane;
    public static MaterialEntry HydroCrackedButene;
    public static MaterialEntry HydroCrackedButadiene;
    public static MaterialEntry SteamCrackedEthane;
    public static MaterialEntry SteamCrackedEthylene;
    public static MaterialEntry SteamCrackedPropene;
    public static MaterialEntry SteamCrackedPropane;
    public static MaterialEntry SteamCrackedButane;
    public static MaterialEntry SteamCrackedButene;
    public static MaterialEntry SteamCrackedButadiene;
    public static MaterialEntry LPG;

    public static MaterialEntry RawGrowthMedium;
    public static MaterialEntry SterileGrowthMedium;
    public static MaterialEntry Bacteria;
    public static MaterialEntry BacterialSludge;
    public static MaterialEntry EnrichedBacterialSludge;
    public static MaterialEntry Mutagen;
    public static MaterialEntry GelatinMixture;
    public static MaterialEntry RawGasoline;
    public static MaterialEntry Gasoline;
    public static MaterialEntry HighOctaneGasoline;
    public static MaterialEntry CoalGas;
    public static MaterialEntry CoalTar;
    public static MaterialEntry Gunpowder;
    public static MaterialEntry Oilsands;
    public static MaterialEntry RareEarth;
    public static MaterialEntry Stone;
    public static MaterialEntry Lava;
    public static MaterialEntry Glowstone;
    public static MaterialEntry NetherStar;
    public static MaterialEntry Endstone;
    public static MaterialEntry Netherrack;
    public static MaterialEntry CetaneBoostedDiesel;
    public static MaterialEntry Collagen;
    public static MaterialEntry Gelatin;
    public static MaterialEntry Agar;
    public static MaterialEntry Andesite;
    public static MaterialEntry Milk;
    public static MaterialEntry Cocoa;
    public static MaterialEntry Wheat;
    public static MaterialEntry Meat;
    public static MaterialEntry Wood;
    public static MaterialEntry TreatedWood;
    public static MaterialEntry Paper;
    public static MaterialEntry FishOil;
    public static MaterialEntry RubySlurry;
    public static MaterialEntry SapphireSlurry;
    public static MaterialEntry GreenSapphireSlurry;
    public static MaterialEntry DyeBlack;
    public static MaterialEntry DyeRed;
    public static MaterialEntry DyeGreen;
    public static MaterialEntry DyeBrown;
    public static MaterialEntry DyeBlue;
    public static MaterialEntry DyePurple;
    public static MaterialEntry DyeCyan;
    public static MaterialEntry DyeLightGray;
    public static MaterialEntry DyeGray;
    public static MaterialEntry DyePink;
    public static MaterialEntry DyeLime;
    public static MaterialEntry DyeYellow;
    public static MaterialEntry DyeLightBlue;
    public static MaterialEntry DyeMagenta;
    public static MaterialEntry DyeOrange;
    public static MaterialEntry DyeWhite;

    public static MaterialEntry ImpureEnrichedNaquadahSolution;
    public static MaterialEntry EnrichedNaquadahSolution;
    public static MaterialEntry AcidicEnrichedNaquadahSolution;
    public static MaterialEntry EnrichedNaquadahWaste;
    public static MaterialEntry ImpureNaquadriaSolution;
    public static MaterialEntry NaquadriaSolution;
    public static MaterialEntry AcidicNaquadriaSolution;
    public static MaterialEntry NaquadriaWaste;
    public static MaterialEntry Lapotron;
    public static MaterialEntry UUMatter;
    public static MaterialEntry PCBCoolant;
    public static MaterialEntry Sculk;
    public static MaterialEntry Wax;
    public static MaterialEntry BauxiteSlurry;
    public static MaterialEntry CrackedBauxiteSlurry;
    public static MaterialEntry BauxiteSludge;
    public static MaterialEntry DecalcifiedBauxiteSludge;
    public static MaterialEntry BauxiteSlag;

    /**
     * Second Degree Compounds
     */
    public static MaterialEntry Glass;
    public static MaterialEntry Perlite;
    public static MaterialEntry Borax;
    public static MaterialEntry Olivine;
    public static MaterialEntry Opal;
    public static MaterialEntry Amethyst;
    public static MaterialEntry EchoShard;
    public static MaterialEntry Lapis;
    public static MaterialEntry Blaze;
    public static MaterialEntry Apatite;
    public static MaterialEntry BlackSteel;
    public static MaterialEntry DamascusSteel;
    public static MaterialEntry TungstenSteel;
    public static MaterialEntry CobaltBrass;
    public static MaterialEntry TricalciumPhosphate;
    public static MaterialEntry GarnetRed;
    public static MaterialEntry GarnetYellow;
    public static MaterialEntry Marble;
    public static MaterialEntry Deepslate;
    public static MaterialEntry RedGranite;
    public static MaterialEntry Blackstone;
    public static MaterialEntry VanadiumMagnetite;
    public static MaterialEntry QuartzSand;
    public static MaterialEntry Pollucite;
    public static MaterialEntry Bentonite;
    public static MaterialEntry FullersEarth;
    public static MaterialEntry Pitchblende;
    public static MaterialEntry Monazite;
    public static MaterialEntry Mirabilite;
    public static MaterialEntry ActivatedCarbon;
    public static MaterialEntry Trona;
    public static MaterialEntry Gypsum;
    public static MaterialEntry Zeolite;
    public static MaterialEntry Concrete;
    public static MaterialEntry SteelMagnetic;
    public static MaterialEntry VanadiumSteel;
    public static MaterialEntry Potin;
    public static MaterialEntry BorosilicateGlass;
    public static MaterialEntry NaquadahAlloy;
    public static MaterialEntry SulfuricNickelSolution;
    public static MaterialEntry SulfuricCopperSolution;
    public static MaterialEntry LeadZincSolution;
    public static MaterialEntry NitrationMixture;
    public static MaterialEntry DilutedSulfuricAcid;
    public static MaterialEntry DilutedHydrochloricAcid;
    public static MaterialEntry Flint;
    public static MaterialEntry Air;
    public static MaterialEntry LiquidAir;
    public static MaterialEntry NetherAir;
    public static MaterialEntry LiquidNetherAir;
    public static MaterialEntry EnderAir;
    public static MaterialEntry LiquidEnderAir;
    public static MaterialEntry AquaRegia;
    public static MaterialEntry SaltWater;
    public static MaterialEntry Clay;
    public static MaterialEntry Redstone;
    public static MaterialEntry Dichloroethane;
    public static MaterialEntry Diethylenetriamine;
    public static MaterialEntry Tuff;

    /**
     * Third Degree Materials
     */
    public static MaterialEntry Electrotine;
    public static MaterialEntry EnderEye;
    public static MaterialEntry Diatomite;
    public static MaterialEntry RedSteel;
    public static MaterialEntry BlueSteel;
    public static MaterialEntry Basalt;
    public static MaterialEntry GraniticMineralSand;
    public static MaterialEntry Redrock;
    public static MaterialEntry GarnetSand;
    public static MaterialEntry HSSG;
    public static MaterialEntry IridiumMetalResidue;
    public static MaterialEntry Granite;
    public static MaterialEntry Brick;
    public static MaterialEntry Fireclay;
    public static MaterialEntry Diorite;

    /**
     * Fourth Degree Materials
     */
    public static MaterialEntry RedAlloy;
    public static MaterialEntry BlueAlloy;
    public static MaterialEntry BasalticMineralSand;
    public static MaterialEntry HSSE;
    public static MaterialEntry HSSS;
    public static MaterialEntry RadAway;

    /**
     * GCYM Materials
     */
    public static MaterialEntry TantalumCarbide;
    public static MaterialEntry HSLASteel;
    public static MaterialEntry MolybdenumDisilicide;
    public static MaterialEntry Zeron100;
    public static MaterialEntry WatertightSteel;
    public static MaterialEntry IncoloyMA956;
    public static MaterialEntry MaragingSteel300;
    public static MaterialEntry HastelloyX;
    public static MaterialEntry Stellite100;
    public static MaterialEntry TitaniumCarbide;
    public static MaterialEntry TitaniumTungstenCarbide;
    public static MaterialEntry HastelloyC276;
}
