package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.event.PostMaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlag;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MaterialRegistryEntry;
import com.gregtechceu.gtceu.common.data.materials.*;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;

import net.minecraft.core.Holder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jspecify.annotations.NullUnmarked;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
@NullUnmarked
public class GTMaterials {

    public static Map<DyeColor, MaterialRegistryEntry> DYE_MATERIALS = new Object2ObjectOpenHashMap<>();
    public static MaterialRegistryEntry[] VOLTAGE_COMMON_MATERIALS;

    public static void init(IEventBus modBus) {
        ElementMaterials.register();
        FirstDegreeMaterials.register();
        OrganicChemistryMaterials.register();
        UnknownCompositionMaterials.register();
        SecondDegreeMaterials.register();
        HigherDegreeMaterials.register();

        // Gregicality Multiblocks
        GCYMMaterials.register();

        modBus.register(GTMaterials.class);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void modifyMaterials(PostMaterialEvent event) {
        /*
         * Register info for cyclical references
         */
        MaterialFlagAddition.register();

        DYE_MATERIALS.put(DyeColor.WHITE, DyeWhite);
        DYE_MATERIALS.put(DyeColor.ORANGE, DyeOrange);
        DYE_MATERIALS.put(DyeColor.MAGENTA, DyeMagenta);
        DYE_MATERIALS.put(DyeColor.LIGHT_BLUE, DyeLightBlue);
        DYE_MATERIALS.put(DyeColor.YELLOW, DyeYellow);
        DYE_MATERIALS.put(DyeColor.LIME, DyeLime);
        DYE_MATERIALS.put(DyeColor.PINK, DyePink);
        DYE_MATERIALS.put(DyeColor.GRAY, DyeGray);
        DYE_MATERIALS.put(DyeColor.LIGHT_GRAY, DyeLightGray);
        DYE_MATERIALS.put(DyeColor.CYAN, DyeCyan);
        DYE_MATERIALS.put(DyeColor.PURPLE, DyePurple);
        DYE_MATERIALS.put(DyeColor.BLUE, DyeBlue);
        DYE_MATERIALS.put(DyeColor.BROWN, DyeBrown);
        DYE_MATERIALS.put(DyeColor.GREEN, DyeGreen);
        DYE_MATERIALS.put(DyeColor.RED, DyeRed);
        DYE_MATERIALS.put(DyeColor.BLACK, DyeBlack);

        VOLTAGE_COMMON_MATERIALS = new MaterialRegistryEntry[] {
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

    private static void excludeAllGems(Holder<Material> material, ItemLike... items) {
        gem.setIgnored(material, items);
        excludeAllGemsButNormal(material);
    }

    private static void excludeAllGemsButNormal(Holder<Material> material) {
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

    /**
     * Direct Elements
     */
    public static MaterialRegistryEntry Actinium;
    public static MaterialRegistryEntry Aluminium;
    public static MaterialRegistryEntry Americium;
    public static MaterialRegistryEntry Antimony;
    public static MaterialRegistryEntry Argon;
    public static MaterialRegistryEntry Arsenic;
    public static MaterialRegistryEntry Astatine;
    public static MaterialRegistryEntry Barium;
    public static MaterialRegistryEntry Berkelium;
    public static MaterialRegistryEntry Beryllium;
    public static MaterialRegistryEntry Bismuth;
    public static MaterialRegistryEntry Bohrium;
    public static MaterialRegistryEntry Boron;
    public static MaterialRegistryEntry Bromine;
    public static MaterialRegistryEntry Caesium;
    public static MaterialRegistryEntry Calcium;
    public static MaterialRegistryEntry Californium;
    public static MaterialRegistryEntry Carbon;
    public static MaterialRegistryEntry Cadmium;
    public static MaterialRegistryEntry Cerium;
    public static MaterialRegistryEntry Chlorine;
    public static MaterialRegistryEntry Chromium;
    public static MaterialRegistryEntry Cobalt;
    public static MaterialRegistryEntry Copernicium;
    public static MaterialRegistryEntry Copper;
    public static MaterialRegistryEntry Curium;
    public static MaterialRegistryEntry Darmstadtium;
    public static MaterialRegistryEntry Deuterium;
    public static MaterialRegistryEntry Dubnium;
    public static MaterialRegistryEntry Dysprosium;
    public static MaterialRegistryEntry Einsteinium;
    public static MaterialRegistryEntry Erbium;
    public static MaterialRegistryEntry Europium;
    public static MaterialRegistryEntry Fermium;
    public static MaterialRegistryEntry Flerovium;
    public static MaterialRegistryEntry Fluorine;
    public static MaterialRegistryEntry Francium;
    public static MaterialRegistryEntry Gadolinium;
    public static MaterialRegistryEntry Gallium;
    public static MaterialRegistryEntry Germanium;
    public static MaterialRegistryEntry Gold;
    public static MaterialRegistryEntry Hafnium;
    public static MaterialRegistryEntry Hassium;
    public static MaterialRegistryEntry Holmium;
    public static MaterialRegistryEntry Hydrogen;
    public static MaterialRegistryEntry Helium;
    public static MaterialRegistryEntry Helium3;
    public static MaterialRegistryEntry Indium;
    public static MaterialRegistryEntry Iodine;
    public static MaterialRegistryEntry Iridium;
    public static MaterialRegistryEntry Iron;
    public static MaterialRegistryEntry Krypton;
    public static MaterialRegistryEntry Lanthanum;
    public static MaterialRegistryEntry Lawrencium;
    public static MaterialRegistryEntry Lead;
    public static MaterialRegistryEntry Lithium;
    public static MaterialRegistryEntry Livermorium;
    public static MaterialRegistryEntry Lutetium;
    public static MaterialRegistryEntry Magnesium;
    public static MaterialRegistryEntry Mendelevium;
    public static MaterialRegistryEntry Manganese;
    public static MaterialRegistryEntry Meitnerium;
    public static MaterialRegistryEntry Mercury;
    public static MaterialRegistryEntry Molybdenum;
    public static MaterialRegistryEntry Moscovium;
    public static MaterialRegistryEntry Neodymium;
    public static MaterialRegistryEntry Neon;
    public static MaterialRegistryEntry Neptunium;
    public static MaterialRegistryEntry Nickel;
    public static MaterialRegistryEntry Nihonium;
    public static MaterialRegistryEntry Niobium;
    public static MaterialRegistryEntry Nitrogen;
    public static MaterialRegistryEntry Nobelium;
    public static MaterialRegistryEntry Oganesson;
    public static MaterialRegistryEntry Osmium;
    public static MaterialRegistryEntry Oxygen;
    public static MaterialRegistryEntry Palladium;
    public static MaterialRegistryEntry Phosphorus;
    public static MaterialRegistryEntry Polonium;
    public static MaterialRegistryEntry Platinum;
    public static MaterialRegistryEntry Plutonium239;
    public static MaterialRegistryEntry Plutonium241;
    public static MaterialRegistryEntry Potassium;
    public static MaterialRegistryEntry Praseodymium;
    public static MaterialRegistryEntry Promethium;
    public static MaterialRegistryEntry Protactinium;
    public static MaterialRegistryEntry Radon;
    public static MaterialRegistryEntry Radium;
    public static MaterialRegistryEntry Rhenium;
    public static MaterialRegistryEntry Rhodium;
    public static MaterialRegistryEntry Roentgenium;
    public static MaterialRegistryEntry Rubidium;
    public static MaterialRegistryEntry Ruthenium;
    public static MaterialRegistryEntry Rutherfordium;
    public static MaterialRegistryEntry Samarium;
    public static MaterialRegistryEntry Scandium;
    public static MaterialRegistryEntry Seaborgium;
    public static MaterialRegistryEntry Selenium;
    public static MaterialRegistryEntry Silicon;
    public static MaterialRegistryEntry Silver;
    public static MaterialRegistryEntry Sodium;
    public static MaterialRegistryEntry Strontium;
    public static MaterialRegistryEntry Sulfur;
    public static MaterialRegistryEntry Tantalum;
    public static MaterialRegistryEntry Technetium;
    public static MaterialRegistryEntry Tellurium;
    public static MaterialRegistryEntry Tennessine;
    public static MaterialRegistryEntry Terbium;
    public static MaterialRegistryEntry Thorium;
    public static MaterialRegistryEntry Thallium;
    public static MaterialRegistryEntry Thulium;
    public static MaterialRegistryEntry Tin;
    public static MaterialRegistryEntry Titanium;
    public static MaterialRegistryEntry Tritium;
    public static MaterialRegistryEntry Tungsten;
    public static MaterialRegistryEntry Uranium238;
    public static MaterialRegistryEntry Uranium235;
    public static MaterialRegistryEntry Vanadium;
    public static MaterialRegistryEntry Xenon;
    public static MaterialRegistryEntry Ytterbium;
    public static MaterialRegistryEntry Yttrium;
    public static MaterialRegistryEntry Zinc;
    public static MaterialRegistryEntry Zirconium;

    /**
     * Fantasy Elements
     */
    public static MaterialRegistryEntry Naquadah;
    public static MaterialRegistryEntry NaquadahEnriched;
    public static MaterialRegistryEntry Naquadria;
    public static MaterialRegistryEntry Neutronium;
    public static MaterialRegistryEntry Tritanium;
    public static MaterialRegistryEntry Duranium;
    public static MaterialRegistryEntry Trinium;

    /**
     * First Degree Compounds
     */
    public static MaterialRegistryEntry Almandine;
    public static MaterialRegistryEntry Andradite;
    public static MaterialRegistryEntry AnnealedCopper;
    public static MaterialRegistryEntry Asbestos;
    public static MaterialRegistryEntry Ash;
    public static MaterialRegistryEntry Hematite;
    public static MaterialRegistryEntry BatteryAlloy;
    public static MaterialRegistryEntry BlueTopaz;
    public static MaterialRegistryEntry Bone;
    public static MaterialRegistryEntry Brass;
    public static MaterialRegistryEntry Bronze;
    public static MaterialRegistryEntry Goethite;
    public static MaterialRegistryEntry Calcite;
    public static MaterialRegistryEntry Cassiterite;
    public static MaterialRegistryEntry CassiteriteSand;
    public static MaterialRegistryEntry Chalcopyrite;
    public static MaterialRegistryEntry Charcoal;
    public static MaterialRegistryEntry Chromite;
    public static MaterialRegistryEntry Cinnabar;
    public static MaterialRegistryEntry Water;
    public static MaterialRegistryEntry Coal;
    public static MaterialRegistryEntry Cobaltite;
    public static MaterialRegistryEntry Cooperite;
    public static MaterialRegistryEntry Cupronickel;
    public static MaterialRegistryEntry DarkAsh;
    public static MaterialRegistryEntry Diamond;
    public static MaterialRegistryEntry Electrum;
    public static MaterialRegistryEntry Emerald;
    public static MaterialRegistryEntry Galena;
    public static MaterialRegistryEntry Garnierite;
    public static MaterialRegistryEntry GreenSapphire;
    public static MaterialRegistryEntry Grossular;
    public static MaterialRegistryEntry Ice;
    public static MaterialRegistryEntry Ilmenite;
    public static MaterialRegistryEntry Rutile;
    public static MaterialRegistryEntry Bauxite;
    public static MaterialRegistryEntry Invar;
    public static MaterialRegistryEntry Kanthal;
    public static MaterialRegistryEntry Lazurite;
    public static MaterialRegistryEntry Magnalium;
    public static MaterialRegistryEntry Magnesite;
    public static MaterialRegistryEntry Magnetite;
    public static MaterialRegistryEntry Molybdenite;
    public static MaterialRegistryEntry Nichrome;
    public static MaterialRegistryEntry NiobiumNitride;
    public static MaterialRegistryEntry NiobiumTitanium;
    public static MaterialRegistryEntry Obsidian;
    public static MaterialRegistryEntry Phosphate;
    public static MaterialRegistryEntry SterlingSilver;
    public static MaterialRegistryEntry RoseGold;
    public static MaterialRegistryEntry BlackBronze;
    public static MaterialRegistryEntry BismuthBronze;
    public static MaterialRegistryEntry Biotite;
    public static MaterialRegistryEntry Powellite;
    public static MaterialRegistryEntry Pyrite;
    public static MaterialRegistryEntry Pyrolusite;
    public static MaterialRegistryEntry Pyrope;
    public static MaterialRegistryEntry RockSalt;
    public static MaterialRegistryEntry RTMAlloy;
    public static MaterialRegistryEntry Ruridit;
    public static MaterialRegistryEntry Rubber;
    public static MaterialRegistryEntry Ruby;
    public static MaterialRegistryEntry Salt;
    public static MaterialRegistryEntry Saltpeter;
    public static MaterialRegistryEntry Sapphire;
    public static MaterialRegistryEntry Scheelite;
    public static MaterialRegistryEntry Sodalite;
    public static MaterialRegistryEntry AluminiumSulfite;
    public static MaterialRegistryEntry Tantalite;
    public static MaterialRegistryEntry Coke;
    public static MaterialRegistryEntry Netherite;

    public static MaterialRegistryEntry SolderingAlloy;
    public static MaterialRegistryEntry Spessartine;
    public static MaterialRegistryEntry Sphalerite;
    public static MaterialRegistryEntry StainlessSteel;
    public static MaterialRegistryEntry Steel;
    public static MaterialRegistryEntry Stibnite;
    public static MaterialRegistryEntry Tetrahedrite;
    public static MaterialRegistryEntry TinAlloy;
    public static MaterialRegistryEntry Topaz;
    public static MaterialRegistryEntry Tungstate;
    public static MaterialRegistryEntry Ultimet;
    public static MaterialRegistryEntry Uraninite;
    public static MaterialRegistryEntry Uvarovite;
    public static MaterialRegistryEntry VanadiumGallium;
    public static MaterialRegistryEntry WroughtIron;
    public static MaterialRegistryEntry Wulfenite;
    public static MaterialRegistryEntry YellowLimonite;
    public static MaterialRegistryEntry YttriumBariumCuprate;
    public static MaterialRegistryEntry NetherQuartz;
    public static MaterialRegistryEntry CertusQuartz;
    public static MaterialRegistryEntry Quartzite;
    public static MaterialRegistryEntry Graphite;
    public static MaterialRegistryEntry Graphene;
    public static MaterialRegistryEntry TungsticAcid;
    public static MaterialRegistryEntry Osmiridium;
    public static MaterialRegistryEntry LithiumChloride;
    public static MaterialRegistryEntry CalciumChloride;
    public static MaterialRegistryEntry Bornite;
    public static MaterialRegistryEntry Chalcocite;

    public static MaterialRegistryEntry GalliumArsenide;
    public static MaterialRegistryEntry Potash;
    public static MaterialRegistryEntry SodaAsh;
    public static MaterialRegistryEntry IndiumGalliumPhosphide;
    public static MaterialRegistryEntry NickelZincFerrite;
    public static MaterialRegistryEntry SiliconDioxide;
    public static MaterialRegistryEntry MagnesiumChloride;
    public static MaterialRegistryEntry SodiumSulfide;
    public static MaterialRegistryEntry PhosphorusPentoxide;
    public static MaterialRegistryEntry Quicklime;
    public static MaterialRegistryEntry SodiumBisulfate;
    public static MaterialRegistryEntry FerriteMixture;
    public static MaterialRegistryEntry Magnesia;
    public static MaterialRegistryEntry PlatinumGroupSludge;
    public static MaterialRegistryEntry Realgar;
    public static MaterialRegistryEntry SodiumBicarbonate;
    public static MaterialRegistryEntry PotassiumDichromate;
    public static MaterialRegistryEntry ChromiumTrioxide;
    public static MaterialRegistryEntry AntimonyTrioxide;
    public static MaterialRegistryEntry Zincite;
    public static MaterialRegistryEntry CupricOxide;
    public static MaterialRegistryEntry CobaltOxide;
    public static MaterialRegistryEntry ArsenicTrioxide;
    public static MaterialRegistryEntry Massicot;
    public static MaterialRegistryEntry Ferrosilite;
    public static MaterialRegistryEntry MetalMixture;
    public static MaterialRegistryEntry SodiumHydroxide;
    public static MaterialRegistryEntry SodiumPersulfate;
    public static MaterialRegistryEntry Bastnasite;
    public static MaterialRegistryEntry Pentlandite;
    public static MaterialRegistryEntry Spodumene;
    public static MaterialRegistryEntry Lepidolite;
    public static MaterialRegistryEntry GlauconiteSand;
    public static MaterialRegistryEntry Malachite;
    public static MaterialRegistryEntry Mica;
    public static MaterialRegistryEntry Barite;
    public static MaterialRegistryEntry Alunite;
    public static MaterialRegistryEntry Talc;
    public static MaterialRegistryEntry Soapstone;
    public static MaterialRegistryEntry Kyanite;
    public static MaterialRegistryEntry IronMagnetic;
    public static MaterialRegistryEntry TungstenCarbide;
    public static MaterialRegistryEntry CarbonDioxide;
    public static MaterialRegistryEntry TitaniumTetrachloride;
    public static MaterialRegistryEntry NitrogenDioxide;
    public static MaterialRegistryEntry HydrogenSulfide;
    public static MaterialRegistryEntry NitricAcid;
    public static MaterialRegistryEntry SulfuricAcid;
    public static MaterialRegistryEntry PhosphoricAcid;
    public static MaterialRegistryEntry SulfurTrioxide;
    public static MaterialRegistryEntry SulfurDioxide;
    public static MaterialRegistryEntry CarbonMonoxide;
    public static MaterialRegistryEntry HypochlorousAcid;
    public static MaterialRegistryEntry Ammonia;
    public static MaterialRegistryEntry HydrofluoricAcid;
    public static MaterialRegistryEntry NitricOxide;
    public static MaterialRegistryEntry Iron3Chloride;
    public static MaterialRegistryEntry Iron2Chloride;
    public static MaterialRegistryEntry UraniumHexafluoride;
    public static MaterialRegistryEntry EnrichedUraniumHexafluoride;
    public static MaterialRegistryEntry DepletedUraniumHexafluoride;
    public static MaterialRegistryEntry NitrousOxide;
    public static MaterialRegistryEntry EnderPearl;
    public static MaterialRegistryEntry PotassiumFeldspar;
    public static MaterialRegistryEntry NeodymiumMagnetic;
    public static MaterialRegistryEntry HydrochloricAcid;
    public static MaterialRegistryEntry Steam;
    public static MaterialRegistryEntry DistilledWater;
    public static MaterialRegistryEntry SodiumPotassium;
    public static MaterialRegistryEntry SamariumMagnetic;
    public static MaterialRegistryEntry ManganesePhosphide;
    public static MaterialRegistryEntry MagnesiumDiboride;
    public static MaterialRegistryEntry MercuryBariumCalciumCuprate;
    public static MaterialRegistryEntry UraniumTriplatinum;
    public static MaterialRegistryEntry SamariumIronArsenicOxide;
    public static MaterialRegistryEntry IndiumTinBariumTitaniumCuprate;
    public static MaterialRegistryEntry UraniumRhodiumDinaquadide;
    public static MaterialRegistryEntry EnrichedNaquadahTriniumEuropiumDuranide;
    public static MaterialRegistryEntry RutheniumTriniumAmericiumNeutronate;
    public static MaterialRegistryEntry PlatinumRaw;
    public static MaterialRegistryEntry InertMetalMixture;
    public static MaterialRegistryEntry RhodiumSulfate;
    public static MaterialRegistryEntry RutheniumTetroxide;
    public static MaterialRegistryEntry OsmiumTetroxide;
    public static MaterialRegistryEntry IridiumChloride;
    public static MaterialRegistryEntry FluoroantimonicAcid;
    public static MaterialRegistryEntry TitaniumTrifluoride;
    public static MaterialRegistryEntry CalciumPhosphide;
    public static MaterialRegistryEntry IndiumPhosphide;
    public static MaterialRegistryEntry BariumSulfide;
    public static MaterialRegistryEntry TriniumSulfide;
    public static MaterialRegistryEntry ZincSulfide;
    public static MaterialRegistryEntry GalliumSulfide;
    public static MaterialRegistryEntry AntimonyTrifluoride;
    public static MaterialRegistryEntry EnrichedNaquadahSulfate;
    public static MaterialRegistryEntry NaquadriaSulfate;
    public static MaterialRegistryEntry Pyrochlore;
    public static MaterialRegistryEntry PotassiumHydroxide;
    public static MaterialRegistryEntry PotassiumIodide;
    public static MaterialRegistryEntry PotassiumFerrocyanide;
    public static MaterialRegistryEntry CalciumFerrocyanide;
    public static MaterialRegistryEntry CalciumHydroxide;
    public static MaterialRegistryEntry CalciumCarbonate;
    public static MaterialRegistryEntry PotassiumCyanide;
    public static MaterialRegistryEntry PotassiumCarbonate;
    public static MaterialRegistryEntry HydrogenCyanide;
    public static MaterialRegistryEntry FormicAcid;
    public static MaterialRegistryEntry PotassiumSulfate;
    public static MaterialRegistryEntry PrussianBlue;
    public static MaterialRegistryEntry Formaldehyde;
    public static MaterialRegistryEntry Glycolonitrile;
    public static MaterialRegistryEntry DiethylenetriaminePentaacetonitrile;
    public static MaterialRegistryEntry DiethylenetriaminepentaaceticAcid;
    public static MaterialRegistryEntry SodiumNitrite;
    public static MaterialRegistryEntry HydrogenPeroxide;
    public static MaterialRegistryEntry IlmeniteSlag;

    /**
     * Organic chemistry
     */
    public static MaterialRegistryEntry SiliconeRubber;
    public static MaterialRegistryEntry RawRubber;
    public static MaterialRegistryEntry RawStyreneButadieneRubber;
    public static MaterialRegistryEntry StyreneButadieneRubber;
    public static MaterialRegistryEntry PolyvinylAcetate;
    public static MaterialRegistryEntry ReinforcedEpoxyResin;
    public static MaterialRegistryEntry PolyvinylChloride;
    public static MaterialRegistryEntry PolyphenyleneSulfide;
    public static MaterialRegistryEntry GlycerylTrinitrate;
    public static MaterialRegistryEntry Polybenzimidazole;
    public static MaterialRegistryEntry Polydimethylsiloxane;
    public static MaterialRegistryEntry Polyethylene;
    public static MaterialRegistryEntry Epoxy;
    public static MaterialRegistryEntry Polycaprolactam;
    public static MaterialRegistryEntry Polytetrafluoroethylene;
    public static MaterialRegistryEntry Sugar;
    public static MaterialRegistryEntry Methane;
    public static MaterialRegistryEntry Epichlorohydrin;
    public static MaterialRegistryEntry Monochloramine;
    public static MaterialRegistryEntry Chloroform;
    public static MaterialRegistryEntry Cumene;
    public static MaterialRegistryEntry Tetrafluoroethylene;
    public static MaterialRegistryEntry Chloromethane;
    public static MaterialRegistryEntry AllylChloride;
    public static MaterialRegistryEntry Isoprene;
    public static MaterialRegistryEntry Propane;
    public static MaterialRegistryEntry Propene;
    public static MaterialRegistryEntry Ethane;
    public static MaterialRegistryEntry Butene;
    public static MaterialRegistryEntry Butane;
    public static MaterialRegistryEntry DissolvedCalciumAcetate;
    public static MaterialRegistryEntry VinylAcetate;
    public static MaterialRegistryEntry MethylAcetate;
    public static MaterialRegistryEntry Ethenone;
    public static MaterialRegistryEntry Tetranitromethane;
    public static MaterialRegistryEntry Dimethylamine;
    public static MaterialRegistryEntry Dimethylhydrazine;
    public static MaterialRegistryEntry DinitrogenTetroxide;
    public static MaterialRegistryEntry Dimethyldichlorosilane;
    public static MaterialRegistryEntry Styrene;
    public static MaterialRegistryEntry Butadiene;
    public static MaterialRegistryEntry Dichlorobenzene;
    public static MaterialRegistryEntry AceticAcid;
    public static MaterialRegistryEntry Phenol;
    public static MaterialRegistryEntry BisphenolA;
    public static MaterialRegistryEntry VinylChloride;
    public static MaterialRegistryEntry Ethylene;
    public static MaterialRegistryEntry Benzene;
    public static MaterialRegistryEntry Acetone;
    public static MaterialRegistryEntry Glycerol;
    public static MaterialRegistryEntry Methanol;
    public static MaterialRegistryEntry Ethanol;
    public static MaterialRegistryEntry Toluene;
    public static MaterialRegistryEntry DiphenylIsophtalate;
    public static MaterialRegistryEntry PhthalicAcid;
    public static MaterialRegistryEntry Dimethylbenzene;
    public static MaterialRegistryEntry Diaminobenzidine;
    public static MaterialRegistryEntry Dichlorobenzidine;
    public static MaterialRegistryEntry Nitrochlorobenzene;
    public static MaterialRegistryEntry Chlorobenzene;
    public static MaterialRegistryEntry Octane;
    public static MaterialRegistryEntry EthylTertButylEther;
    public static MaterialRegistryEntry Ethylbenzene;
    public static MaterialRegistryEntry Naphthalene;
    public static MaterialRegistryEntry Nitrobenzene;
    public static MaterialRegistryEntry Cyclohexane;
    public static MaterialRegistryEntry NitrosylChloride;
    public static MaterialRegistryEntry CyclohexanoneOxime;
    public static MaterialRegistryEntry Caprolactam;
    public static MaterialRegistryEntry PlatinumSludgeResidue;
    public static MaterialRegistryEntry PalladiumRaw;
    public static MaterialRegistryEntry RarestMetalMixture;
    public static MaterialRegistryEntry AmmoniumChloride;
    public static MaterialRegistryEntry AcidicOsmiumSolution;
    public static MaterialRegistryEntry RhodiumPlatedPalladium;
    public static MaterialRegistryEntry Butyraldehyde;
    public static MaterialRegistryEntry PolyvinylButyral;
    public static MaterialRegistryEntry Biphenyl;
    public static MaterialRegistryEntry PolychlorinatedBiphenyl;
    public static MaterialRegistryEntry AceticAnhydride;
    public static MaterialRegistryEntry AminoPhenol;
    public static MaterialRegistryEntry Paracetamol;
    public static MaterialRegistryEntry AmmoniumFormate;
    public static MaterialRegistryEntry Formamide;

    /**
     * Not possible to determine exact Components
     */
    public static MaterialRegistryEntry WoodGas;
    public static MaterialRegistryEntry WoodVinegar;
    public static MaterialRegistryEntry WoodTar;
    public static MaterialRegistryEntry CharcoalByproducts;
    public static MaterialRegistryEntry Biomass;
    public static MaterialRegistryEntry BioDiesel;
    public static MaterialRegistryEntry FermentedBiomass;
    public static MaterialRegistryEntry Creosote;
    public static MaterialRegistryEntry Diesel;
    public static MaterialRegistryEntry RocketFuel;
    public static MaterialRegistryEntry Glue;
    public static MaterialRegistryEntry Lubricant;
    public static MaterialRegistryEntry McGuffium239;
    public static MaterialRegistryEntry IndiumConcentrate;
    public static MaterialRegistryEntry SeedOil;
    public static MaterialRegistryEntry DrillingFluid;
    public static MaterialRegistryEntry ConstructionFoam;

    public static MaterialRegistryEntry Oil;
    public static MaterialRegistryEntry HeavyOil;
    public static MaterialRegistryEntry RawOil;
    public static MaterialRegistryEntry LightOil;
    public static MaterialRegistryEntry NaturalGas;
    public static MaterialRegistryEntry SulfuricHeavyFuel;
    public static MaterialRegistryEntry HeavyFuel;
    public static MaterialRegistryEntry LightlyHydroCrackedHeavyFuel;
    public static MaterialRegistryEntry SeverelyHydroCrackedHeavyFuel;
    public static MaterialRegistryEntry LightlySteamCrackedHeavyFuel;
    public static MaterialRegistryEntry SeverelySteamCrackedHeavyFuel;
    public static MaterialRegistryEntry SulfuricLightFuel;
    public static MaterialRegistryEntry LightFuel;
    public static MaterialRegistryEntry LightlyHydroCrackedLightFuel;
    public static MaterialRegistryEntry SeverelyHydroCrackedLightFuel;
    public static MaterialRegistryEntry LightlySteamCrackedLightFuel;
    public static MaterialRegistryEntry SeverelySteamCrackedLightFuel;
    public static MaterialRegistryEntry SulfuricNaphtha;
    public static MaterialRegistryEntry Naphtha;
    public static MaterialRegistryEntry LightlyHydroCrackedNaphtha;
    public static MaterialRegistryEntry SeverelyHydroCrackedNaphtha;
    public static MaterialRegistryEntry LightlySteamCrackedNaphtha;
    public static MaterialRegistryEntry SeverelySteamCrackedNaphtha;
    public static MaterialRegistryEntry SulfuricGas;
    public static MaterialRegistryEntry RefineryGas;
    public static MaterialRegistryEntry LightlyHydroCrackedGas;
    public static MaterialRegistryEntry SeverelyHydroCrackedGas;
    public static MaterialRegistryEntry LightlySteamCrackedGas;
    public static MaterialRegistryEntry SeverelySteamCrackedGas;
    public static MaterialRegistryEntry HydroCrackedEthane;
    public static MaterialRegistryEntry HydroCrackedEthylene;
    public static MaterialRegistryEntry HydroCrackedPropene;
    public static MaterialRegistryEntry HydroCrackedPropane;
    public static MaterialRegistryEntry HydroCrackedButane;
    public static MaterialRegistryEntry HydroCrackedButene;
    public static MaterialRegistryEntry HydroCrackedButadiene;
    public static MaterialRegistryEntry SteamCrackedEthane;
    public static MaterialRegistryEntry SteamCrackedEthylene;
    public static MaterialRegistryEntry SteamCrackedPropene;
    public static MaterialRegistryEntry SteamCrackedPropane;
    public static MaterialRegistryEntry SteamCrackedButane;
    public static MaterialRegistryEntry SteamCrackedButene;
    public static MaterialRegistryEntry SteamCrackedButadiene;
    public static MaterialRegistryEntry LPG;

    public static MaterialRegistryEntry RawGrowthMedium;
    public static MaterialRegistryEntry SterileGrowthMedium;
    public static MaterialRegistryEntry Bacteria;
    public static MaterialRegistryEntry BacterialSludge;
    public static MaterialRegistryEntry EnrichedBacterialSludge;
    public static MaterialRegistryEntry Mutagen;
    public static MaterialRegistryEntry GelatinMixture;
    public static MaterialRegistryEntry RawGasoline;
    public static MaterialRegistryEntry Gasoline;
    public static MaterialRegistryEntry HighOctaneGasoline;
    public static MaterialRegistryEntry CoalGas;
    public static MaterialRegistryEntry CoalTar;
    public static MaterialRegistryEntry Gunpowder;
    public static MaterialRegistryEntry Oilsands;
    public static MaterialRegistryEntry RareEarth;
    public static MaterialRegistryEntry Stone;
    public static MaterialRegistryEntry Lava;
    public static MaterialRegistryEntry Glowstone;
    public static MaterialRegistryEntry NetherStar;
    public static MaterialRegistryEntry Endstone;
    public static MaterialRegistryEntry Netherrack;
    public static MaterialRegistryEntry CetaneBoostedDiesel;
    public static MaterialRegistryEntry Collagen;
    public static MaterialRegistryEntry Gelatin;
    public static MaterialRegistryEntry Agar;
    public static MaterialRegistryEntry Andesite;
    public static MaterialRegistryEntry Milk;
    public static MaterialRegistryEntry Cocoa;
    public static MaterialRegistryEntry Wheat;
    public static MaterialRegistryEntry Meat;
    public static MaterialRegistryEntry Wood;
    public static MaterialRegistryEntry TreatedWood;
    public static MaterialRegistryEntry Paper;
    public static MaterialRegistryEntry FishOil;
    public static MaterialRegistryEntry RubySlurry;
    public static MaterialRegistryEntry SapphireSlurry;
    public static MaterialRegistryEntry GreenSapphireSlurry;
    public static MaterialRegistryEntry DyeBlack;
    public static MaterialRegistryEntry DyeRed;
    public static MaterialRegistryEntry DyeGreen;
    public static MaterialRegistryEntry DyeBrown;
    public static MaterialRegistryEntry DyeBlue;
    public static MaterialRegistryEntry DyePurple;
    public static MaterialRegistryEntry DyeCyan;
    public static MaterialRegistryEntry DyeLightGray;
    public static MaterialRegistryEntry DyeGray;
    public static MaterialRegistryEntry DyePink;
    public static MaterialRegistryEntry DyeLime;
    public static MaterialRegistryEntry DyeYellow;
    public static MaterialRegistryEntry DyeLightBlue;
    public static MaterialRegistryEntry DyeMagenta;
    public static MaterialRegistryEntry DyeOrange;
    public static MaterialRegistryEntry DyeWhite;

    public static MaterialRegistryEntry ImpureEnrichedNaquadahSolution;
    public static MaterialRegistryEntry EnrichedNaquadahSolution;
    public static MaterialRegistryEntry AcidicEnrichedNaquadahSolution;
    public static MaterialRegistryEntry EnrichedNaquadahWaste;
    public static MaterialRegistryEntry ImpureNaquadriaSolution;
    public static MaterialRegistryEntry NaquadriaSolution;
    public static MaterialRegistryEntry AcidicNaquadriaSolution;
    public static MaterialRegistryEntry NaquadriaWaste;
    public static MaterialRegistryEntry Lapotron;
    public static MaterialRegistryEntry UUMatter;
    public static MaterialRegistryEntry PCBCoolant;
    public static MaterialRegistryEntry Sculk;
    public static MaterialRegistryEntry Wax;
    public static MaterialRegistryEntry BauxiteSlurry;
    public static MaterialRegistryEntry CrackedBauxiteSlurry;
    public static MaterialRegistryEntry BauxiteSludge;
    public static MaterialRegistryEntry DecalcifiedBauxiteSludge;
    public static MaterialRegistryEntry BauxiteSlag;

    /**
     * Second Degree Compounds
     */
    public static MaterialRegistryEntry Glass;
    public static MaterialRegistryEntry Perlite;
    public static MaterialRegistryEntry Borax;
    public static MaterialRegistryEntry Olivine;
    public static MaterialRegistryEntry Opal;
    public static MaterialRegistryEntry Amethyst;
    public static MaterialRegistryEntry EchoShard;
    public static MaterialRegistryEntry Lapis;
    public static MaterialRegistryEntry Blaze;
    public static MaterialRegistryEntry Apatite;
    public static MaterialRegistryEntry BlackSteel;
    public static MaterialRegistryEntry DamascusSteel;
    public static MaterialRegistryEntry TungstenSteel;
    public static MaterialRegistryEntry CobaltBrass;
    public static MaterialRegistryEntry TricalciumPhosphate;
    public static MaterialRegistryEntry GarnetRed;
    public static MaterialRegistryEntry GarnetYellow;
    public static MaterialRegistryEntry Marble;
    public static MaterialRegistryEntry Deepslate;
    public static MaterialRegistryEntry RedGranite;
    public static MaterialRegistryEntry Blackstone;
    public static MaterialRegistryEntry VanadiumMagnetite;
    public static MaterialRegistryEntry QuartzSand;
    public static MaterialRegistryEntry Pollucite;
    public static MaterialRegistryEntry Bentonite;
    public static MaterialRegistryEntry FullersEarth;
    public static MaterialRegistryEntry Pitchblende;
    public static MaterialRegistryEntry Monazite;
    public static MaterialRegistryEntry Mirabilite;
    public static MaterialRegistryEntry ActivatedCarbon;
    public static MaterialRegistryEntry Trona;
    public static MaterialRegistryEntry Gypsum;
    public static MaterialRegistryEntry Zeolite;
    public static MaterialRegistryEntry Concrete;
    public static MaterialRegistryEntry SteelMagnetic;
    public static MaterialRegistryEntry VanadiumSteel;
    public static MaterialRegistryEntry Potin;
    public static MaterialRegistryEntry BorosilicateGlass;
    public static MaterialRegistryEntry NaquadahAlloy;
    public static MaterialRegistryEntry SulfuricNickelSolution;
    public static MaterialRegistryEntry SulfuricCopperSolution;
    public static MaterialRegistryEntry LeadZincSolution;
    public static MaterialRegistryEntry NitrationMixture;
    public static MaterialRegistryEntry DilutedSulfuricAcid;
    public static MaterialRegistryEntry DilutedHydrochloricAcid;
    public static MaterialRegistryEntry Flint;
    public static MaterialRegistryEntry Air;
    public static MaterialRegistryEntry LiquidAir;
    public static MaterialRegistryEntry NetherAir;
    public static MaterialRegistryEntry LiquidNetherAir;
    public static MaterialRegistryEntry EnderAir;
    public static MaterialRegistryEntry LiquidEnderAir;
    public static MaterialRegistryEntry AquaRegia;
    public static MaterialRegistryEntry SaltWater;
    public static MaterialRegistryEntry Clay;
    public static MaterialRegistryEntry Redstone;
    public static MaterialRegistryEntry Dichloroethane;
    public static MaterialRegistryEntry Diethylenetriamine;
    public static MaterialRegistryEntry Tuff;

    /**
     * Third Degree Materials
     */
    public static MaterialRegistryEntry Electrotine;
    public static MaterialRegistryEntry EnderEye;
    public static MaterialRegistryEntry Diatomite;
    public static MaterialRegistryEntry RedSteel;
    public static MaterialRegistryEntry BlueSteel;
    public static MaterialRegistryEntry Basalt;
    public static MaterialRegistryEntry GraniticMineralSand;
    public static MaterialRegistryEntry Redrock;
    public static MaterialRegistryEntry GarnetSand;
    public static MaterialRegistryEntry HSSG;
    public static MaterialRegistryEntry IridiumMetalResidue;
    public static MaterialRegistryEntry Granite;
    public static MaterialRegistryEntry Brick;
    public static MaterialRegistryEntry Fireclay;
    public static MaterialRegistryEntry Diorite;

    /**
     * Fourth Degree Materials
     */
    public static MaterialRegistryEntry RedAlloy;
    public static MaterialRegistryEntry BlueAlloy;
    public static MaterialRegistryEntry BasalticMineralSand;
    public static MaterialRegistryEntry HSSE;
    public static MaterialRegistryEntry HSSS;
    public static MaterialRegistryEntry RadAway;

    /**
     * GCYM Materials
     */
    public static MaterialRegistryEntry TantalumCarbide;
    public static MaterialRegistryEntry HSLASteel;
    public static MaterialRegistryEntry MolybdenumDisilicide;
    public static MaterialRegistryEntry Zeron100;
    public static MaterialRegistryEntry WatertightSteel;
    public static MaterialRegistryEntry IncoloyMA956;
    public static MaterialRegistryEntry MaragingSteel300;
    public static MaterialRegistryEntry HastelloyX;
    public static MaterialRegistryEntry Stellite100;
    public static MaterialRegistryEntry TitaniumCarbide;
    public static MaterialRegistryEntry TitaniumTungstenCarbide;
    public static MaterialRegistryEntry HastelloyC276;
}
