package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterial;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlag;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.holder.HolderRegistryEntry;
import com.gregtechceu.gtceu.common.data.materials.*;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;

import net.minecraft.core.Holder;
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
 * material = new MaterialBuilder(id, name)<br>
 * .ingot().fluid().ore() <--- types<br>
 * .color().iconSet() <--- appearance<br>
 * .flags() <--- special generation<br>
 * .element() / .components() <--- composition<br>
 * .toolStats() <---<br>
 * .oreByProducts() | additional properties<br>
 * ... <---<br>
 * .blastTemp() <--- blast temperature<br>
 * .build();
 * </p>
 *
 * <p>
 * Use defaults to your advantage! Some defaults:
 * <ul>
 *     <li>iconSet: DULL</li>
 *     <li>color: 0xFFFFFF</li>
 * </ul>
 * </p>
 */
public class GTMaterials {

    public static HolderRegistryEntry<Material>[] CHEMICAL_DYES;
    public static HolderRegistryEntry<Material>[] VOLTAGE_COMMON_MATERIALS;

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

        // noinspection unchecked
        CHEMICAL_DYES = new HolderRegistryEntry[] {
                DyeWhite, DyeOrange,
                DyeMagenta, DyeLightBlue,
                DyeYellow, DyeLime,
                DyePink, DyeGray,
                DyeLightGray, DyeCyan,
                DyePurple, DyeBlue,
                DyeBrown, DyeGreen,
                DyeRed, DyeBlack
        };

        // noinspection unchecked
        VOLTAGE_COMMON_MATERIALS = new HolderRegistryEntry[] {
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

        gemExquisite.get().setIgnored(Sugar);
        gemFlawless.get().setIgnored(Sugar);

        gem.get().setIgnored(Diamond, Items.DIAMOND);
        gem.get().setIgnored(Emerald, Items.EMERALD);
        gem.get().setIgnored(Lapis, Items.LAPIS_LAZULI);
        gem.get().setIgnored(NetherQuartz, Items.QUARTZ);
        gem.get().setIgnored(Coal, Items.COAL);
        gem.get().setIgnored(Amethyst, Items.AMETHYST_SHARD);
        gem.get().setIgnored(EchoShard, Items.ECHO_SHARD);
        excludeAllGems(Charcoal, Items.CHARCOAL);
        excludeAllGems(Flint, Items.FLINT);
        excludeAllGems(EnderPearl, Items.ENDER_PEARL);
        excludeAllGems(EnderEye, Items.ENDER_EYE);
        excludeAllGems(NetherStar, Items.NETHER_STAR);
        excludeAllGemsButNormal(Lapotron);

        dust.get().setIgnored(Redstone, Items.REDSTONE);
        dust.get().setIgnored(Glowstone, Items.GLOWSTONE_DUST);
        dust.get().setIgnored(Gunpowder, Items.GUNPOWDER);
        dust.get().setIgnored(Sugar, Items.SUGAR);
        dust.get().setIgnored(Bone, Items.BONE_MEAL);
        dust.get().setIgnored(Blaze, Items.BLAZE_POWDER);

        rod.get().setIgnored(Wood, Items.STICK);
        rod.get().setIgnored(Bone, Items.BONE);
        rod.get().setIgnored(Blaze, Items.BLAZE_ROD);
        rod.get().setIgnored(Paper);

        ingot.get().setIgnored(Iron, Items.IRON_INGOT);
        ingot.get().setIgnored(Gold, Items.GOLD_INGOT);
        ingot.get().setIgnored(Copper, Items.COPPER_INGOT);
        ingot.get().setIgnored(Netherite, Items.NETHERITE_INGOT);
        ingot.get().setIgnored(Brick, Items.BRICK);
        ingot.get().setIgnored(Wax, Items.HONEYCOMB);

        nugget.get().setIgnored(Gold, Items.GOLD_NUGGET);
        nugget.get().setIgnored(Iron, Items.IRON_NUGGET);

        plate.get().setIgnored(Paper, Items.PAPER);

        block.get().setIgnored(Iron, Blocks.IRON_BLOCK);
        block.get().setIgnored(Gold, Blocks.GOLD_BLOCK);
        block.get().setIgnored(Copper, Blocks.COPPER_BLOCK);
        block.get().setIgnored(Netherite, Items.NETHERITE_BLOCK);
        block.get().setIgnored(Lapis, Blocks.LAPIS_BLOCK);
        block.get().setIgnored(Emerald, Blocks.EMERALD_BLOCK);
        block.get().setIgnored(Redstone, Blocks.REDSTONE_BLOCK);
        block.get().setIgnored(Diamond, Blocks.DIAMOND_BLOCK);
        block.get().setIgnored(Coal, Blocks.COAL_BLOCK);
        block.get().setIgnored(Amethyst, Blocks.AMETHYST_BLOCK);
        block.get().setIgnored(Glass, Blocks.GLASS);
        block.get().setIgnored(Glowstone, Blocks.GLOWSTONE);
        block.get().setIgnored(Oilsands);
        block.get().setIgnored(Wood);
        block.get().setIgnored(TreatedWood);
        block.get().setIgnored(RawRubber);
        block.get().setIgnored(Clay, Blocks.CLAY);
        block.get().setIgnored(Brick, Blocks.BRICKS);
        block.get().setIgnored(Bone, Blocks.BONE_BLOCK);
        block.get().setIgnored(NetherQuartz, Blocks.QUARTZ_BLOCK);
        block.get().setIgnored(Ice, Blocks.ICE);
        block.get().setIgnored(Concrete, Blocks.WHITE_CONCRETE, Blocks.ORANGE_CONCRETE, Blocks.MAGENTA_CONCRETE,
                Blocks.LIGHT_BLUE_CONCRETE, Blocks.YELLOW_CONCRETE, Blocks.LIME_CONCRETE,
                Blocks.PINK_CONCRETE, Blocks.GRAY_CONCRETE, Blocks.LIGHT_GRAY_CONCRETE, Blocks.CYAN_CONCRETE,
                Blocks.PURPLE_CONCRETE, Blocks.BLUE_CONCRETE,
                Blocks.BROWN_CONCRETE, Blocks.GREEN_CONCRETE, Blocks.RED_CONCRETE, Blocks.BLACK_CONCRETE);
        block.get().setIgnored(Blaze);
        block.get().setIgnored(Lapotron);
        block.get().setIgnored(Wax, Blocks.HONEYCOMB_BLOCK);

        rock.get().setIgnored(Marble, GTMemoizer.memoizeBlockSupplier(() -> GTBlocks.MARBLE.get()));
        rock.get().setIgnored(Granite, Blocks.GRANITE);
        rock.get().setIgnored(Granite, Blocks.POLISHED_GRANITE);
        rock.get().setIgnored(GraniteRed, GTMemoizer.memoizeBlockSupplier(() -> GTBlocks.RED_GRANITE.get()));
        rock.get().setIgnored(Andesite, Blocks.ANDESITE);
        rock.get().setIgnored(Andesite, Blocks.POLISHED_ANDESITE);
        rock.get().setIgnored(Diorite, Blocks.DIORITE);
        rock.get().setIgnored(Diorite, Blocks.POLISHED_DIORITE);
        rock.get().setIgnored(Stone, Blocks.STONE);
        rock.get().setIgnored(Calcite, Blocks.CALCITE);
        rock.get().setIgnored(Netherrack, Blocks.NETHERRACK);
        rock.get().setIgnored(Obsidian, Blocks.OBSIDIAN);
        rock.get().setIgnored(Endstone, Blocks.END_STONE);
        rock.get().setIgnored(Deepslate, Blocks.DEEPSLATE);
        rock.get().setIgnored(Basalt, Blocks.BASALT);
        rock.get().setIgnored(Blackstone, Blocks.BLACKSTONE);
        block.get().setIgnored(Sculk, Blocks.SCULK);
        block.get().setIgnored(Concrete, GTMemoizer.memoizeBlockSupplier(() -> GTBlocks.DARK_CONCRETE.get()));
        block.get().setIgnored(Concrete, GTMemoizer.memoizeBlockSupplier(() -> GTBlocks.LIGHT_CONCRETE.get()));

        final long dustMaterialAmount = dust.get().materialAmount();
        final long plateMaterialAmount = plate.get().materialAmount();
        final long ringMaterialAmount = ring.get().materialAmount();
        final long screwMaterialAmount = screw.get().materialAmount();

        for (TagPrefix prefix : ORES.keySet()) {
            TagPrefix.OreType oreType = ORES.get(prefix);
            if (oreType.material() != null) {
                prefix.addSecondaryMaterial(new MaterialStack(oreType.material().get(), dustMaterialAmount));
            }
        }

        crushed.get().addSecondaryMaterial(new MaterialStack(Stone, dustMaterialAmount));

        toolHeadDrill.get().addSecondaryMaterial(new MaterialStack(Steel, plateMaterialAmount * 4));
        toolHeadChainsaw.get().addSecondaryMaterial(new MaterialStack(Steel, plateMaterialAmount * 4 + ringMaterialAmount * 2));
        toolHeadWrench.get().addSecondaryMaterial(new MaterialStack(Steel, ringMaterialAmount + screwMaterialAmount * 2));
        toolHeadWireCutter.get().addSecondaryMaterial(new MaterialStack(Steel, ringMaterialAmount + screwMaterialAmount * 2));

        pipeTinyFluid.get().setIgnored(Wood);
        pipeHugeFluid.get().setIgnored(Wood);
        pipeQuadrupleFluid.get().setIgnored(Wood);
        pipeNonupleFluid.get().setIgnored(Wood);
        pipeTinyFluid.get().setIgnored(TreatedWood);
        pipeHugeFluid.get().setIgnored(TreatedWood);
        pipeQuadrupleFluid.get().setIgnored(TreatedWood);
        pipeNonupleFluid.get().setIgnored(TreatedWood);

        pipeSmallRestrictive.get().addSecondaryMaterial(new MaterialStack(Iron, ringMaterialAmount * 2));
        pipeNormalRestrictive.get().addSecondaryMaterial(new MaterialStack(Iron, ringMaterialAmount * 2));
        pipeLargeRestrictive.get().addSecondaryMaterial(new MaterialStack(Iron, ringMaterialAmount * 2));
        pipeHugeRestrictive.get().addSecondaryMaterial(new MaterialStack(Iron, ringMaterialAmount * 2));

        cableGtSingle.get().addSecondaryMaterial(new MaterialStack(Rubber, plateMaterialAmount));
        cableGtDouble.get().addSecondaryMaterial(new MaterialStack(Rubber, plateMaterialAmount));
        cableGtQuadruple.get().addSecondaryMaterial(new MaterialStack(Rubber, plateMaterialAmount * 2));
        cableGtOctal.get().addSecondaryMaterial(new MaterialStack(Rubber, plateMaterialAmount * 3));
        cableGtHex.get().addSecondaryMaterial(new MaterialStack(Rubber, plateMaterialAmount * 5));

        plateDouble.get().setIgnored(BorosilicateGlass);
        plateDouble.get().setIgnored(Wood);
        plateDouble.get().setIgnored(TreatedWood);
        plate.get().setIgnored(BorosilicateGlass);
        foil.get().setIgnored(BorosilicateGlass);

        dye.get().setIgnored(DyeBlack, Items.BLACK_DYE);
        dye.get().setIgnored(DyeRed, Items.RED_DYE);
        dye.get().setIgnored(DyeGreen, Items.GREEN_DYE);
        dye.get().setIgnored(DyeBrown, Items.BROWN_DYE);
        dye.get().setIgnored(DyeBlue, Items.BLUE_DYE);
        dye.get().setIgnored(DyePurple, Items.PURPLE_DYE);
        dye.get().setIgnored(DyeCyan, Items.CYAN_DYE);
        dye.get().setIgnored(DyeLightGray, Items.LIGHT_GRAY_DYE);
        dye.get().setIgnored(DyeGray, Items.GRAY_DYE);
        dye.get().setIgnored(DyePink, Items.PINK_DYE);
        dye.get().setIgnored(DyeLime, Items.LIME_DYE);
        dye.get().setIgnored(DyeYellow, Items.YELLOW_DYE);
        dye.get().setIgnored(DyeLightBlue, Items.LIGHT_BLUE_DYE);
        dye.get().setIgnored(DyeMagenta, Items.MAGENTA_DYE);
        dye.get().setIgnored(DyeOrange, Items.ORANGE_DYE);
        dye.get().setIgnored(DyeWhite, Items.WHITE_DYE);

        // register vanilla materials

        rawOre.get().setIgnored(Gold, Items.RAW_GOLD);
        rawOre.get().setIgnored(Iron, Items.RAW_IRON);
        rawOre.get().setIgnored(Copper, Items.RAW_COPPER);
        rawOreBlock.get().setIgnored(Gold, Blocks.RAW_GOLD_BLOCK);
        rawOreBlock.get().setIgnored(Iron, Blocks.RAW_IRON_BLOCK);
        rawOreBlock.get().setIgnored(Copper, Blocks.RAW_COPPER_BLOCK);

        block.get().modifyMaterialAmount(Amethyst, 4);
        block.get().modifyMaterialAmount(EchoShard, 4);
        block.get().modifyMaterialAmount(Glowstone, 4);
        block.get().modifyMaterialAmount(NetherQuartz, 4);
        block.get().modifyMaterialAmount(CertusQuartz, 4);
        block.get().modifyMaterialAmount(Brick, 4);
        block.get().modifyMaterialAmount(Clay, 4);

        block.get().modifyMaterialAmount(Concrete, 1);
        block.get().modifyMaterialAmount(Glass, 1);
        block.get().modifyMaterialAmount(Ice, 1);
        block.get().modifyMaterialAmount(Obsidian, 1);
        block.get().modifyMaterialAmount(Sculk, 1);
        block.get().modifyMaterialAmount(Wax, 4);

        rod.get().modifyMaterialAmount(Blaze, 4);
        rod.get().modifyMaterialAmount(Bone, 5);
    }

    @SuppressWarnings("ConstantValue")
    @NotNull
    public static Holder<Material> get(ResourceLocation name) {
        var mat = GTRegistries.MATERIALS.get(name);
        // material could be null here due to the registry grabbing a material that isn't in the map
        if (mat == null) {
            GTCEu.LOGGER.warn("{} is not a known Material", name);
            return GTMaterials.NULL;
        }
        return mat.getHolder();
    }

    private static void excludeAllGems(HolderRegistryEntry<Material> material, ItemLike... items) {
        gem.get().setIgnored(material, items);
        excludeAllGemsButNormal(material);
    }

    private static void excludeAllGemsButNormal(HolderRegistryEntry<Material> material) {
        gemChipped.get().setIgnored(material);
        gemFlawed.get().setIgnored(material);
        gemFlawless.get().setIgnored(material);
        gemExquisite.get().setIgnored(material);
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

    public static final HolderRegistryEntry<Material> NULL = GTRegistration.REGISTRATE.simple("null", GTRegistries.Keys.MATERIAL, () -> new MarkerMaterial(GTCEu.id("null")));

    public static Material nullMaterial() {
        return NULL.get();
    }

    /**
     * Direct Elements
     */
    public static HolderRegistryEntry<Material> Actinium;
    public static HolderRegistryEntry<Material> Aluminium;
    public static HolderRegistryEntry<Material> Americium;
    public static HolderRegistryEntry<Material> Antimony;
    public static HolderRegistryEntry<Material> Argon;
    public static HolderRegistryEntry<Material> Arsenic;
    public static HolderRegistryEntry<Material> Astatine;
    public static HolderRegistryEntry<Material> Barium;
    public static HolderRegistryEntry<Material> Berkelium;
    public static HolderRegistryEntry<Material> Beryllium;
    public static HolderRegistryEntry<Material> Bismuth;
    public static HolderRegistryEntry<Material> Bohrium;
    public static HolderRegistryEntry<Material> Boron;
    public static HolderRegistryEntry<Material> Bromine;
    public static HolderRegistryEntry<Material> Caesium;
    public static HolderRegistryEntry<Material> Calcium;
    public static HolderRegistryEntry<Material> Californium;
    public static HolderRegistryEntry<Material> Carbon;
    public static HolderRegistryEntry<Material> Cadmium;
    public static HolderRegistryEntry<Material> Cerium;
    public static HolderRegistryEntry<Material> Chlorine;
    public static HolderRegistryEntry<Material> Chromium;
    public static HolderRegistryEntry<Material> Cobalt;
    public static HolderRegistryEntry<Material> Copernicium;
    public static HolderRegistryEntry<Material> Copper;
    public static HolderRegistryEntry<Material> Curium;
    public static HolderRegistryEntry<Material> Darmstadtium;
    public static HolderRegistryEntry<Material> Deuterium;
    public static HolderRegistryEntry<Material> Dubnium;
    public static HolderRegistryEntry<Material> Dysprosium;
    public static HolderRegistryEntry<Material> Einsteinium;
    public static HolderRegistryEntry<Material> Erbium;
    public static HolderRegistryEntry<Material> Europium;
    public static HolderRegistryEntry<Material> Fermium;
    public static HolderRegistryEntry<Material> Flerovium;
    public static HolderRegistryEntry<Material> Fluorine;
    public static HolderRegistryEntry<Material> Francium;
    public static HolderRegistryEntry<Material> Gadolinium;
    public static HolderRegistryEntry<Material> Gallium;
    public static HolderRegistryEntry<Material> Germanium;
    public static HolderRegistryEntry<Material> Gold;
    public static HolderRegistryEntry<Material> Hafnium;
    public static HolderRegistryEntry<Material> Hassium;
    public static HolderRegistryEntry<Material> Holmium;
    public static HolderRegistryEntry<Material> Hydrogen;
    public static HolderRegistryEntry<Material> Helium;
    public static HolderRegistryEntry<Material> Helium3;
    public static HolderRegistryEntry<Material> Indium;
    public static HolderRegistryEntry<Material> Iodine;
    public static HolderRegistryEntry<Material> Iridium;
    public static HolderRegistryEntry<Material> Iron;
    public static HolderRegistryEntry<Material> Krypton;
    public static HolderRegistryEntry<Material> Lanthanum;
    public static HolderRegistryEntry<Material> Lawrencium;
    public static HolderRegistryEntry<Material> Lead;
    public static HolderRegistryEntry<Material> Lithium;
    public static HolderRegistryEntry<Material> Livermorium;
    public static HolderRegistryEntry<Material> Lutetium;
    public static HolderRegistryEntry<Material> Magnesium;
    public static HolderRegistryEntry<Material> Mendelevium;
    public static HolderRegistryEntry<Material> Manganese;
    public static HolderRegistryEntry<Material> Meitnerium;
    public static HolderRegistryEntry<Material> Mercury;
    public static HolderRegistryEntry<Material> Molybdenum;
    public static HolderRegistryEntry<Material> Moscovium;
    public static HolderRegistryEntry<Material> Neodymium;
    public static HolderRegistryEntry<Material> Neon;
    public static HolderRegistryEntry<Material> Neptunium;
    public static HolderRegistryEntry<Material> Nickel;
    public static HolderRegistryEntry<Material> Nihonium;
    public static HolderRegistryEntry<Material> Niobium;
    public static HolderRegistryEntry<Material> Nitrogen;
    public static HolderRegistryEntry<Material> Nobelium;
    public static HolderRegistryEntry<Material> Oganesson;
    public static HolderRegistryEntry<Material> Osmium;
    public static HolderRegistryEntry<Material> Oxygen;
    public static HolderRegistryEntry<Material> Palladium;
    public static HolderRegistryEntry<Material> Phosphorus;
    public static HolderRegistryEntry<Material> Polonium;
    public static HolderRegistryEntry<Material> Platinum;
    public static HolderRegistryEntry<Material> Plutonium239;
    public static HolderRegistryEntry<Material> Plutonium241;
    public static HolderRegistryEntry<Material> Potassium;
    public static HolderRegistryEntry<Material> Praseodymium;
    public static HolderRegistryEntry<Material> Promethium;
    public static HolderRegistryEntry<Material> Protactinium;
    public static HolderRegistryEntry<Material> Radon;
    public static HolderRegistryEntry<Material> Radium;
    public static HolderRegistryEntry<Material> Rhenium;
    public static HolderRegistryEntry<Material> Rhodium;
    public static HolderRegistryEntry<Material> Roentgenium;
    public static HolderRegistryEntry<Material> Rubidium;
    public static HolderRegistryEntry<Material> Ruthenium;
    public static HolderRegistryEntry<Material> Rutherfordium;
    public static HolderRegistryEntry<Material> Samarium;
    public static HolderRegistryEntry<Material> Scandium;
    public static HolderRegistryEntry<Material> Seaborgium;
    public static HolderRegistryEntry<Material> Selenium;
    public static HolderRegistryEntry<Material> Silicon;
    public static HolderRegistryEntry<Material> Silver;
    public static HolderRegistryEntry<Material> Sodium;
    public static HolderRegistryEntry<Material> Strontium;
    public static HolderRegistryEntry<Material> Sulfur;
    public static HolderRegistryEntry<Material> Tantalum;
    public static HolderRegistryEntry<Material> Technetium;
    public static HolderRegistryEntry<Material> Tellurium;
    public static HolderRegistryEntry<Material> Tennessine;
    public static HolderRegistryEntry<Material> Terbium;
    public static HolderRegistryEntry<Material> Thorium;
    public static HolderRegistryEntry<Material> Thallium;
    public static HolderRegistryEntry<Material> Thulium;
    public static HolderRegistryEntry<Material> Tin;
    public static HolderRegistryEntry<Material> Titanium;
    public static HolderRegistryEntry<Material> Tritium;
    public static HolderRegistryEntry<Material> Tungsten;
    public static HolderRegistryEntry<Material> Uranium238;
    public static HolderRegistryEntry<Material> Uranium235;
    public static HolderRegistryEntry<Material> Vanadium;
    public static HolderRegistryEntry<Material> Xenon;
    public static HolderRegistryEntry<Material> Ytterbium;
    public static HolderRegistryEntry<Material> Yttrium;
    public static HolderRegistryEntry<Material> Zinc;
    public static HolderRegistryEntry<Material> Zirconium;

    /**
     * Fantasy Elements
     */
    public static HolderRegistryEntry<Material> Naquadah;
    public static HolderRegistryEntry<Material> NaquadahEnriched;
    public static HolderRegistryEntry<Material> Naquadria;
    public static HolderRegistryEntry<Material> Neutronium;
    public static HolderRegistryEntry<Material> Tritanium;
    public static HolderRegistryEntry<Material> Duranium;
    public static HolderRegistryEntry<Material> Trinium;

    /**
     * First Degree Compounds
     */
    public static HolderRegistryEntry<Material> Almandine;
    public static HolderRegistryEntry<Material> Andradite;
    public static HolderRegistryEntry<Material> AnnealedCopper;
    public static HolderRegistryEntry<Material> Asbestos;
    public static HolderRegistryEntry<Material> Ash;
    public static HolderRegistryEntry<Material> Hematite;
    public static HolderRegistryEntry<Material> BatteryAlloy;
    public static HolderRegistryEntry<Material> BlueTopaz;
    public static HolderRegistryEntry<Material> Bone;
    public static HolderRegistryEntry<Material> Brass;
    public static HolderRegistryEntry<Material> Bronze;
    public static HolderRegistryEntry<Material> Goethite;
    public static HolderRegistryEntry<Material> Calcite;
    public static HolderRegistryEntry<Material> Cassiterite;
    public static HolderRegistryEntry<Material> CassiteriteSand;
    public static HolderRegistryEntry<Material> Chalcopyrite;
    public static HolderRegistryEntry<Material> Charcoal;
    public static HolderRegistryEntry<Material> Chromite;
    public static HolderRegistryEntry<Material> Cinnabar;
    public static HolderRegistryEntry<Material> Water;
    public static HolderRegistryEntry<Material> Coal;
    public static HolderRegistryEntry<Material> Cobaltite;
    public static HolderRegistryEntry<Material> Cooperite;
    public static HolderRegistryEntry<Material> Cupronickel;
    public static HolderRegistryEntry<Material> DarkAsh;
    public static HolderRegistryEntry<Material> Diamond;
    public static HolderRegistryEntry<Material> Electrum;
    public static HolderRegistryEntry<Material> Emerald;
    public static HolderRegistryEntry<Material> Galena;
    public static HolderRegistryEntry<Material> Garnierite;
    public static HolderRegistryEntry<Material> GreenSapphire;
    public static HolderRegistryEntry<Material> Grossular;
    public static HolderRegistryEntry<Material> Ice;
    public static HolderRegistryEntry<Material> Ilmenite;
    public static HolderRegistryEntry<Material> Rutile;
    public static HolderRegistryEntry<Material> Bauxite;
    public static HolderRegistryEntry<Material> Invar;
    public static HolderRegistryEntry<Material> Kanthal;
    public static HolderRegistryEntry<Material> Lazurite;
    public static HolderRegistryEntry<Material> Magnalium;
    public static HolderRegistryEntry<Material> Magnesite;
    public static HolderRegistryEntry<Material> Magnetite;
    public static HolderRegistryEntry<Material> Molybdenite;
    public static HolderRegistryEntry<Material> Nichrome;
    public static HolderRegistryEntry<Material> NiobiumNitride;
    public static HolderRegistryEntry<Material> NiobiumTitanium;
    public static HolderRegistryEntry<Material> Obsidian;
    public static HolderRegistryEntry<Material> Phosphate;
    public static HolderRegistryEntry<Material> SterlingSilver;
    public static HolderRegistryEntry<Material> RoseGold;
    public static HolderRegistryEntry<Material> BlackBronze;
    public static HolderRegistryEntry<Material> BismuthBronze;
    public static HolderRegistryEntry<Material> Biotite;
    public static HolderRegistryEntry<Material> Powellite;
    public static HolderRegistryEntry<Material> Pyrite;
    public static HolderRegistryEntry<Material> Pyrolusite;
    public static HolderRegistryEntry<Material> Pyrope;
    public static HolderRegistryEntry<Material> RockSalt;
    public static HolderRegistryEntry<Material> RTMAlloy;
    public static HolderRegistryEntry<Material> Ruridit;
    public static HolderRegistryEntry<Material> Rubber;
    public static HolderRegistryEntry<Material> Ruby;
    public static HolderRegistryEntry<Material> Salt;
    public static HolderRegistryEntry<Material> Saltpeter;
    public static HolderRegistryEntry<Material> Sapphire;
    public static HolderRegistryEntry<Material> Scheelite;
    public static HolderRegistryEntry<Material> Sodalite;
    public static HolderRegistryEntry<Material> AluminiumSulfite;
    public static HolderRegistryEntry<Material> Tantalite;
    public static HolderRegistryEntry<Material> Coke;
    public static HolderRegistryEntry<Material> Netherite;

    public static HolderRegistryEntry<Material> SolderingAlloy;
    public static HolderRegistryEntry<Material> Spessartine;
    public static HolderRegistryEntry<Material> Sphalerite;
    public static HolderRegistryEntry<Material> StainlessSteel;
    public static HolderRegistryEntry<Material> Steel;
    public static HolderRegistryEntry<Material> Stibnite;
    public static HolderRegistryEntry<Material> Tetrahedrite;
    public static HolderRegistryEntry<Material> TinAlloy;
    public static HolderRegistryEntry<Material> Topaz;
    public static HolderRegistryEntry<Material> Tungstate;
    public static HolderRegistryEntry<Material> Ultimet;
    public static HolderRegistryEntry<Material> Uraninite;
    public static HolderRegistryEntry<Material> Uvarovite;
    public static HolderRegistryEntry<Material> VanadiumGallium;
    public static HolderRegistryEntry<Material> WroughtIron;
    public static HolderRegistryEntry<Material> Wulfenite;
    public static HolderRegistryEntry<Material> Limonite;
    @Deprecated
    public static HolderRegistryEntry<Material> YellowLimonite;
    public static HolderRegistryEntry<Material> YttriumBariumCuprate;
    public static HolderRegistryEntry<Material> NetherQuartz;
    public static HolderRegistryEntry<Material> CertusQuartz;
    public static HolderRegistryEntry<Material> Quartzite;
    public static HolderRegistryEntry<Material> Graphite;
    public static HolderRegistryEntry<Material> Graphene;
    public static HolderRegistryEntry<Material> TungsticAcid;
    public static HolderRegistryEntry<Material> Osmiridium;
    public static HolderRegistryEntry<Material> LithiumChloride;
    public static HolderRegistryEntry<Material> CalciumChloride;
    public static HolderRegistryEntry<Material> Bornite;
    public static HolderRegistryEntry<Material> Chalcocite;

    public static HolderRegistryEntry<Material> GalliumArsenide;
    public static HolderRegistryEntry<Material> Potash;
    public static HolderRegistryEntry<Material> SodaAsh;
    public static HolderRegistryEntry<Material> IndiumGalliumPhosphide;
    public static HolderRegistryEntry<Material> NickelZincFerrite;
    public static HolderRegistryEntry<Material> SiliconDioxide;
    public static HolderRegistryEntry<Material> MagnesiumChloride;
    public static HolderRegistryEntry<Material> SodiumSulfide;
    public static HolderRegistryEntry<Material> PhosphorusPentoxide;
    public static HolderRegistryEntry<Material> Quicklime;
    public static HolderRegistryEntry<Material> SodiumBisulfate;
    public static HolderRegistryEntry<Material> FerriteMixture;
    public static HolderRegistryEntry<Material> Magnesia;
    public static HolderRegistryEntry<Material> PlatinumGroupSludge;
    public static HolderRegistryEntry<Material> Realgar;
    public static HolderRegistryEntry<Material> SodiumBicarbonate;
    public static HolderRegistryEntry<Material> PotassiumDichromate;
    public static HolderRegistryEntry<Material> ChromiumTrioxide;
    public static HolderRegistryEntry<Material> AntimonyTrioxide;
    public static HolderRegistryEntry<Material> Zincite;
    public static HolderRegistryEntry<Material> CupricOxide;
    public static HolderRegistryEntry<Material> CobaltOxide;
    public static HolderRegistryEntry<Material> ArsenicTrioxide;
    public static HolderRegistryEntry<Material> Massicot;
    public static HolderRegistryEntry<Material> Ferrosilite;
    public static HolderRegistryEntry<Material> MetalMixture;
    public static HolderRegistryEntry<Material> SodiumHydroxide;
    public static HolderRegistryEntry<Material> SodiumPersulfate;
    public static HolderRegistryEntry<Material> Bastnasite;
    public static HolderRegistryEntry<Material> Pentlandite;
    public static HolderRegistryEntry<Material> Spodumene;
    public static HolderRegistryEntry<Material> Lepidolite;
    public static HolderRegistryEntry<Material> GlauconiteSand;
    public static HolderRegistryEntry<Material> Malachite;
    public static HolderRegistryEntry<Material> Mica;
    public static HolderRegistryEntry<Material> Barite;
    public static HolderRegistryEntry<Material> Alunite;
    public static HolderRegistryEntry<Material> Talc;
    public static HolderRegistryEntry<Material> Soapstone;
    public static HolderRegistryEntry<Material> Kyanite;
    public static HolderRegistryEntry<Material> IronMagnetic;
    public static HolderRegistryEntry<Material> TungstenCarbide;
    public static HolderRegistryEntry<Material> CarbonDioxide;
    public static HolderRegistryEntry<Material> TitaniumTetrachloride;
    public static HolderRegistryEntry<Material> NitrogenDioxide;
    public static HolderRegistryEntry<Material> HydrogenSulfide;
    public static HolderRegistryEntry<Material> NitricAcid;
    public static HolderRegistryEntry<Material> SulfuricAcid;
    public static HolderRegistryEntry<Material> PhosphoricAcid;
    public static HolderRegistryEntry<Material> SulfurTrioxide;
    public static HolderRegistryEntry<Material> SulfurDioxide;
    public static HolderRegistryEntry<Material> CarbonMonoxide;
    public static HolderRegistryEntry<Material> HypochlorousAcid;
    public static HolderRegistryEntry<Material> Ammonia;
    public static HolderRegistryEntry<Material> HydrofluoricAcid;
    public static HolderRegistryEntry<Material> NitricOxide;
    public static HolderRegistryEntry<Material> Iron3Chloride;
    public static HolderRegistryEntry<Material> Iron2Chloride;
    public static HolderRegistryEntry<Material> UraniumHexafluoride;
    public static HolderRegistryEntry<Material> EnrichedUraniumHexafluoride;
    public static HolderRegistryEntry<Material> DepletedUraniumHexafluoride;
    public static HolderRegistryEntry<Material> NitrousOxide;
    public static HolderRegistryEntry<Material> EnderPearl;
    public static HolderRegistryEntry<Material> PotassiumFeldspar;
    public static HolderRegistryEntry<Material> NeodymiumMagnetic;
    public static HolderRegistryEntry<Material> HydrochloricAcid;
    public static HolderRegistryEntry<Material> Steam;
    public static HolderRegistryEntry<Material> DistilledWater;
    public static HolderRegistryEntry<Material> SodiumPotassium;
    public static HolderRegistryEntry<Material> SamariumMagnetic;
    public static HolderRegistryEntry<Material> ManganesePhosphide;
    public static HolderRegistryEntry<Material> MagnesiumDiboride;
    public static HolderRegistryEntry<Material> MercuryBariumCalciumCuprate;
    public static HolderRegistryEntry<Material> UraniumTriplatinum;
    public static HolderRegistryEntry<Material> SamariumIronArsenicOxide;
    public static HolderRegistryEntry<Material> IndiumTinBariumTitaniumCuprate;
    public static HolderRegistryEntry<Material> UraniumRhodiumDinaquadide;
    public static HolderRegistryEntry<Material> EnrichedNaquadahTriniumEuropiumDuranide;
    public static HolderRegistryEntry<Material> RutheniumTriniumAmericiumNeutronate;
    public static HolderRegistryEntry<Material> PlatinumRaw;
    public static HolderRegistryEntry<Material> InertMetalMixture;
    public static HolderRegistryEntry<Material> RhodiumSulfate;
    public static HolderRegistryEntry<Material> RutheniumTetroxide;
    public static HolderRegistryEntry<Material> OsmiumTetroxide;
    public static HolderRegistryEntry<Material> IridiumChloride;
    public static HolderRegistryEntry<Material> FluoroantimonicAcid;
    public static HolderRegistryEntry<Material> TitaniumTrifluoride;
    public static HolderRegistryEntry<Material> CalciumPhosphide;
    public static HolderRegistryEntry<Material> IndiumPhosphide;
    public static HolderRegistryEntry<Material> BariumSulfide;
    public static HolderRegistryEntry<Material> TriniumSulfide;
    public static HolderRegistryEntry<Material> ZincSulfide;
    public static HolderRegistryEntry<Material> GalliumSulfide;
    public static HolderRegistryEntry<Material> AntimonyTrifluoride;
    public static HolderRegistryEntry<Material> EnrichedNaquadahSulfate;
    public static HolderRegistryEntry<Material> NaquadriaSulfate;
    public static HolderRegistryEntry<Material> Pyrochlore;
    public static HolderRegistryEntry<Material> PotassiumHydroxide;
    public static HolderRegistryEntry<Material> PotassiumIodide;
    public static HolderRegistryEntry<Material> PotassiumFerrocyanide;
    public static HolderRegistryEntry<Material> CalciumFerrocyanide;
    public static HolderRegistryEntry<Material> CalciumHydroxide;
    public static HolderRegistryEntry<Material> CalciumCarbonate;
    public static HolderRegistryEntry<Material> PotassiumCyanide;
    public static HolderRegistryEntry<Material> PotassiumCarbonate;
    public static HolderRegistryEntry<Material> HydrogenCyanide;
    public static HolderRegistryEntry<Material> FormicAcid;
    public static HolderRegistryEntry<Material> PotassiumSulfate;
    public static HolderRegistryEntry<Material> PrussianBlue;
    public static HolderRegistryEntry<Material> Formaldehyde;
    public static HolderRegistryEntry<Material> Glycolonitrile;
    public static HolderRegistryEntry<Material> DiethylenetriaminePentaacetonitrile;
    public static HolderRegistryEntry<Material> DiethylenetriaminepentaaceticAcid;
    public static HolderRegistryEntry<Material> SodiumNitrite;
    public static HolderRegistryEntry<Material> HydrogenPeroxide;
    public static HolderRegistryEntry<Material> IlmeniteSlag;

    /**
     * Organic chemistry
     */
    public static HolderRegistryEntry<Material> SiliconeRubber;
    public static HolderRegistryEntry<Material> RawRubber;
    public static HolderRegistryEntry<Material> RawStyreneButadieneRubber;
    public static HolderRegistryEntry<Material> StyreneButadieneRubber;
    public static HolderRegistryEntry<Material> PolyvinylAcetate;
    public static HolderRegistryEntry<Material> ReinforcedEpoxyResin;
    public static HolderRegistryEntry<Material> PolyvinylChloride;
    public static HolderRegistryEntry<Material> PolyphenyleneSulfide;
    public static HolderRegistryEntry<Material> GlycerylTrinitrate;
    public static HolderRegistryEntry<Material> Polybenzimidazole;
    public static HolderRegistryEntry<Material> Polydimethylsiloxane;
    public static HolderRegistryEntry<Material> Polyethylene;
    public static HolderRegistryEntry<Material> Epoxy;
    public static HolderRegistryEntry<Material> Polycaprolactam;
    public static HolderRegistryEntry<Material> Polytetrafluoroethylene;
    public static HolderRegistryEntry<Material> Sugar;
    public static HolderRegistryEntry<Material> Methane;
    public static HolderRegistryEntry<Material> Epichlorohydrin;
    public static HolderRegistryEntry<Material> Monochloramine;
    public static HolderRegistryEntry<Material> Chloroform;
    public static HolderRegistryEntry<Material> Cumene;
    public static HolderRegistryEntry<Material> Tetrafluoroethylene;
    public static HolderRegistryEntry<Material> Chloromethane;
    public static HolderRegistryEntry<Material> AllylChloride;
    public static HolderRegistryEntry<Material> Isoprene;
    public static HolderRegistryEntry<Material> Propane;
    public static HolderRegistryEntry<Material> Propene;
    public static HolderRegistryEntry<Material> Ethane;
    public static HolderRegistryEntry<Material> Butene;
    public static HolderRegistryEntry<Material> Butane;
    public static HolderRegistryEntry<Material> DissolvedCalciumAcetate;
    public static HolderRegistryEntry<Material> VinylAcetate;
    public static HolderRegistryEntry<Material> MethylAcetate;
    public static HolderRegistryEntry<Material> Ethenone;
    public static HolderRegistryEntry<Material> Tetranitromethane;
    public static HolderRegistryEntry<Material> Dimethylamine;
    public static HolderRegistryEntry<Material> Dimethylhydrazine;
    public static HolderRegistryEntry<Material> DinitrogenTetroxide;
    public static HolderRegistryEntry<Material> Dimethyldichlorosilane;
    public static HolderRegistryEntry<Material> Styrene;
    public static HolderRegistryEntry<Material> Butadiene;
    public static HolderRegistryEntry<Material> Dichlorobenzene;
    public static HolderRegistryEntry<Material> AceticAcid;
    public static HolderRegistryEntry<Material> Phenol;
    public static HolderRegistryEntry<Material> BisphenolA;
    public static HolderRegistryEntry<Material> VinylChloride;
    public static HolderRegistryEntry<Material> Ethylene;
    public static HolderRegistryEntry<Material> Benzene;
    public static HolderRegistryEntry<Material> Acetone;
    public static HolderRegistryEntry<Material> Glycerol;
    public static HolderRegistryEntry<Material> Methanol;
    public static HolderRegistryEntry<Material> Ethanol;
    public static HolderRegistryEntry<Material> Toluene;
    public static HolderRegistryEntry<Material> DiphenylIsophtalate;
    public static HolderRegistryEntry<Material> PhthalicAcid;
    public static HolderRegistryEntry<Material> Dimethylbenzene;
    public static HolderRegistryEntry<Material> Diaminobenzidine;
    public static HolderRegistryEntry<Material> Dichlorobenzidine;
    public static HolderRegistryEntry<Material> Nitrochlorobenzene;
    public static HolderRegistryEntry<Material> Chlorobenzene;
    public static HolderRegistryEntry<Material> Octane;
    public static HolderRegistryEntry<Material> EthylTertButylEther;
    public static HolderRegistryEntry<Material> Ethylbenzene;
    public static HolderRegistryEntry<Material> Naphthalene;
    public static HolderRegistryEntry<Material> Nitrobenzene;
    public static HolderRegistryEntry<Material> Cyclohexane;
    public static HolderRegistryEntry<Material> NitrosylChloride;
    public static HolderRegistryEntry<Material> CyclohexanoneOxime;
    public static HolderRegistryEntry<Material> Caprolactam;
    public static HolderRegistryEntry<Material> PlatinumSludgeResidue;
    public static HolderRegistryEntry<Material> PalladiumRaw;
    public static HolderRegistryEntry<Material> RarestMetalMixture;
    public static HolderRegistryEntry<Material> AmmoniumChloride;
    public static HolderRegistryEntry<Material> AcidicOsmiumSolution;
    public static HolderRegistryEntry<Material> RhodiumPlatedPalladium;
    public static HolderRegistryEntry<Material> Butyraldehyde;
    public static HolderRegistryEntry<Material> PolyvinylButyral;
    public static HolderRegistryEntry<Material> Biphenyl;
    public static HolderRegistryEntry<Material> PolychlorinatedBiphenyl;
    public static HolderRegistryEntry<Material> AceticAnhydride;
    public static HolderRegistryEntry<Material> AminoPhenol;
    public static HolderRegistryEntry<Material> Paracetamol;
    public static HolderRegistryEntry<Material> AmmoniumFormate;
    public static HolderRegistryEntry<Material> Formamide;

    /**
     * Not possible to determine exact Components
     */
    public static HolderRegistryEntry<Material> WoodGas;
    public static HolderRegistryEntry<Material> WoodVinegar;
    public static HolderRegistryEntry<Material> WoodTar;
    public static HolderRegistryEntry<Material> CharcoalByproducts;
    public static HolderRegistryEntry<Material> Biomass;
    public static HolderRegistryEntry<Material> BioDiesel;
    public static HolderRegistryEntry<Material> FermentedBiomass;
    public static HolderRegistryEntry<Material> Creosote;
    public static HolderRegistryEntry<Material> Diesel;
    public static HolderRegistryEntry<Material> RocketFuel;
    public static HolderRegistryEntry<Material> Glue;
    public static HolderRegistryEntry<Material> Lubricant;
    public static HolderRegistryEntry<Material> McGuffium239;
    public static HolderRegistryEntry<Material> IndiumConcentrate;
    public static HolderRegistryEntry<Material> SeedOil;
    public static HolderRegistryEntry<Material> DrillingFluid;
    public static HolderRegistryEntry<Material> ConstructionFoam;

    public static HolderRegistryEntry<Material> Oil;
    public static HolderRegistryEntry<Material> OilHeavy;
    public static HolderRegistryEntry<Material> RawOil;
    public static HolderRegistryEntry<Material> OilLight;
    public static HolderRegistryEntry<Material> NaturalGas;
    public static HolderRegistryEntry<Material> SulfuricHeavyFuel;
    public static HolderRegistryEntry<Material> HeavyFuel;
    public static HolderRegistryEntry<Material> LightlyHydroCrackedHeavyFuel;
    public static HolderRegistryEntry<Material> SeverelyHydroCrackedHeavyFuel;
    public static HolderRegistryEntry<Material> LightlySteamCrackedHeavyFuel;
    public static HolderRegistryEntry<Material> SeverelySteamCrackedHeavyFuel;
    public static HolderRegistryEntry<Material> SulfuricLightFuel;
    public static HolderRegistryEntry<Material> LightFuel;
    public static HolderRegistryEntry<Material> LightlyHydroCrackedLightFuel;
    public static HolderRegistryEntry<Material> SeverelyHydroCrackedLightFuel;
    public static HolderRegistryEntry<Material> LightlySteamCrackedLightFuel;
    public static HolderRegistryEntry<Material> SeverelySteamCrackedLightFuel;
    public static HolderRegistryEntry<Material> SulfuricNaphtha;
    public static HolderRegistryEntry<Material> Naphtha;
    public static HolderRegistryEntry<Material> LightlyHydroCrackedNaphtha;
    public static HolderRegistryEntry<Material> SeverelyHydroCrackedNaphtha;
    public static HolderRegistryEntry<Material> LightlySteamCrackedNaphtha;
    public static HolderRegistryEntry<Material> SeverelySteamCrackedNaphtha;
    public static HolderRegistryEntry<Material> SulfuricGas;
    public static HolderRegistryEntry<Material> RefineryGas;
    public static HolderRegistryEntry<Material> LightlyHydroCrackedGas;
    public static HolderRegistryEntry<Material> SeverelyHydroCrackedGas;
    public static HolderRegistryEntry<Material> LightlySteamCrackedGas;
    public static HolderRegistryEntry<Material> SeverelySteamCrackedGas;
    public static HolderRegistryEntry<Material> HydroCrackedEthane;
    public static HolderRegistryEntry<Material> HydroCrackedEthylene;
    public static HolderRegistryEntry<Material> HydroCrackedPropene;
    public static HolderRegistryEntry<Material> HydroCrackedPropane;
    public static HolderRegistryEntry<Material> HydroCrackedButane;
    public static HolderRegistryEntry<Material> HydroCrackedButene;
    public static HolderRegistryEntry<Material> HydroCrackedButadiene;
    public static HolderRegistryEntry<Material> SteamCrackedEthane;
    public static HolderRegistryEntry<Material> SteamCrackedEthylene;
    public static HolderRegistryEntry<Material> SteamCrackedPropene;
    public static HolderRegistryEntry<Material> SteamCrackedPropane;
    public static HolderRegistryEntry<Material> SteamCrackedButane;
    public static HolderRegistryEntry<Material> SteamCrackedButene;
    public static HolderRegistryEntry<Material> SteamCrackedButadiene;
    public static HolderRegistryEntry<Material> LPG;

    public static HolderRegistryEntry<Material> RawGrowthMedium;
    public static HolderRegistryEntry<Material> SterileGrowthMedium;
    public static HolderRegistryEntry<Material> Bacteria;
    public static HolderRegistryEntry<Material> BacterialSludge;
    public static HolderRegistryEntry<Material> EnrichedBacterialSludge;
    public static HolderRegistryEntry<Material> Mutagen;
    public static HolderRegistryEntry<Material> GelatinMixture;
    public static HolderRegistryEntry<Material> RawGasoline;
    public static HolderRegistryEntry<Material> Gasoline;
    public static HolderRegistryEntry<Material> HighOctaneGasoline;
    public static HolderRegistryEntry<Material> CoalGas;
    public static HolderRegistryEntry<Material> CoalTar;
    public static HolderRegistryEntry<Material> Gunpowder;
    public static HolderRegistryEntry<Material> Oilsands;
    public static HolderRegistryEntry<Material> RareEarth;
    public static HolderRegistryEntry<Material> Stone;
    public static HolderRegistryEntry<Material> Lava;
    public static HolderRegistryEntry<Material> Glowstone;
    public static HolderRegistryEntry<Material> NetherStar;
    public static HolderRegistryEntry<Material> Endstone;
    public static HolderRegistryEntry<Material> Netherrack;
    public static HolderRegistryEntry<Material> CetaneBoostedDiesel;
    public static HolderRegistryEntry<Material> Collagen;
    public static HolderRegistryEntry<Material> Gelatin;
    public static HolderRegistryEntry<Material> Agar;
    public static HolderRegistryEntry<Material> Andesite;
    public static HolderRegistryEntry<Material> Milk;
    public static HolderRegistryEntry<Material> Cocoa;
    public static HolderRegistryEntry<Material> Wheat;
    public static HolderRegistryEntry<Material> Meat;
    public static HolderRegistryEntry<Material> Wood;
    public static HolderRegistryEntry<Material> TreatedWood;
    public static HolderRegistryEntry<Material> Paper;
    public static HolderRegistryEntry<Material> FishOil;
    public static HolderRegistryEntry<Material> RubySlurry;
    public static HolderRegistryEntry<Material> SapphireSlurry;
    public static HolderRegistryEntry<Material> GreenSapphireSlurry;
    public static HolderRegistryEntry<Material> DyeBlack;
    public static HolderRegistryEntry<Material> DyeRed;
    public static HolderRegistryEntry<Material> DyeGreen;
    public static HolderRegistryEntry<Material> DyeBrown;
    public static HolderRegistryEntry<Material> DyeBlue;
    public static HolderRegistryEntry<Material> DyePurple;
    public static HolderRegistryEntry<Material> DyeCyan;
    public static HolderRegistryEntry<Material> DyeLightGray;
    public static HolderRegistryEntry<Material> DyeGray;
    public static HolderRegistryEntry<Material> DyePink;
    public static HolderRegistryEntry<Material> DyeLime;
    public static HolderRegistryEntry<Material> DyeYellow;
    public static HolderRegistryEntry<Material> DyeLightBlue;
    public static HolderRegistryEntry<Material> DyeMagenta;
    public static HolderRegistryEntry<Material> DyeOrange;
    public static HolderRegistryEntry<Material> DyeWhite;

    public static HolderRegistryEntry<Material> ImpureEnrichedNaquadahSolution;
    public static HolderRegistryEntry<Material> EnrichedNaquadahSolution;
    public static HolderRegistryEntry<Material> AcidicEnrichedNaquadahSolution;
    public static HolderRegistryEntry<Material> EnrichedNaquadahWaste;
    public static HolderRegistryEntry<Material> ImpureNaquadriaSolution;
    public static HolderRegistryEntry<Material> NaquadriaSolution;
    public static HolderRegistryEntry<Material> AcidicNaquadriaSolution;
    public static HolderRegistryEntry<Material> NaquadriaWaste;
    public static HolderRegistryEntry<Material> Lapotron;
    public static HolderRegistryEntry<Material> UUMatter;
    public static HolderRegistryEntry<Material> PCBCoolant;
    public static HolderRegistryEntry<Material> Sculk;
    public static HolderRegistryEntry<Material> Wax;
    public static HolderRegistryEntry<Material> BauxiteSlurry;
    public static HolderRegistryEntry<Material> CrackedBauxiteSlurry;
    public static HolderRegistryEntry<Material> BauxiteSludge;
    public static HolderRegistryEntry<Material> DecalcifiedBauxiteSludge;
    public static HolderRegistryEntry<Material> BauxiteSlag;

    /**
     * Second Degree Compounds
     */
    public static HolderRegistryEntry<Material> Glass;
    public static HolderRegistryEntry<Material> Perlite;
    public static HolderRegistryEntry<Material> Borax;
    public static HolderRegistryEntry<Material> Olivine;
    public static HolderRegistryEntry<Material> Opal;
    public static HolderRegistryEntry<Material> Amethyst;
    public static HolderRegistryEntry<Material> EchoShard;
    public static HolderRegistryEntry<Material> Lapis;
    public static HolderRegistryEntry<Material> Blaze;
    public static HolderRegistryEntry<Material> Apatite;
    public static HolderRegistryEntry<Material> BlackSteel;
    public static HolderRegistryEntry<Material> DamascusSteel;
    public static HolderRegistryEntry<Material> TungstenSteel;
    public static HolderRegistryEntry<Material> CobaltBrass;
    public static HolderRegistryEntry<Material> TricalciumPhosphate;
    public static HolderRegistryEntry<Material> GarnetRed;
    public static HolderRegistryEntry<Material> GarnetYellow;
    public static HolderRegistryEntry<Material> Marble;
    public static HolderRegistryEntry<Material> Deepslate;
    public static HolderRegistryEntry<Material> GraniteRed;
    public static HolderRegistryEntry<Material> Blackstone;
    public static HolderRegistryEntry<Material> VanadiumMagnetite;
    public static HolderRegistryEntry<Material> QuartzSand;
    public static HolderRegistryEntry<Material> Pollucite;
    public static HolderRegistryEntry<Material> Bentonite;
    public static HolderRegistryEntry<Material> FullersEarth;
    public static HolderRegistryEntry<Material> Pitchblende;
    public static HolderRegistryEntry<Material> Monazite;
    public static HolderRegistryEntry<Material> Mirabilite;
    public static HolderRegistryEntry<Material> ActivatedCarbon;
    public static HolderRegistryEntry<Material> Trona;
    public static HolderRegistryEntry<Material> Gypsum;
    public static HolderRegistryEntry<Material> Zeolite;
    public static HolderRegistryEntry<Material> Concrete;
    public static HolderRegistryEntry<Material> SteelMagnetic;
    public static HolderRegistryEntry<Material> VanadiumSteel;
    public static HolderRegistryEntry<Material> Potin;
    public static HolderRegistryEntry<Material> BorosilicateGlass;
    public static HolderRegistryEntry<Material> NaquadahAlloy;
    public static HolderRegistryEntry<Material> SulfuricNickelSolution;
    public static HolderRegistryEntry<Material> SulfuricCopperSolution;
    public static HolderRegistryEntry<Material> LeadZincSolution;
    public static HolderRegistryEntry<Material> NitrationMixture;
    public static HolderRegistryEntry<Material> DilutedSulfuricAcid;
    public static HolderRegistryEntry<Material> DilutedHydrochloricAcid;
    public static HolderRegistryEntry<Material> Flint;
    public static HolderRegistryEntry<Material> Air;
    public static HolderRegistryEntry<Material> LiquidAir;
    public static HolderRegistryEntry<Material> NetherAir;
    public static HolderRegistryEntry<Material> LiquidNetherAir;
    public static HolderRegistryEntry<Material> EnderAir;
    public static HolderRegistryEntry<Material> LiquidEnderAir;
    public static HolderRegistryEntry<Material> AquaRegia;
    public static HolderRegistryEntry<Material> SaltWater;
    public static HolderRegistryEntry<Material> Clay;
    public static HolderRegistryEntry<Material> Redstone;
    public static HolderRegistryEntry<Material> Dichloroethane;
    public static HolderRegistryEntry<Material> Diethylenetriamine;
    public static HolderRegistryEntry<Material> Tuff;

    /**
     * Third Degree Materials
     */
    public static HolderRegistryEntry<Material> Electrotine;
    public static HolderRegistryEntry<Material> EnderEye;
    public static HolderRegistryEntry<Material> Diatomite;
    public static HolderRegistryEntry<Material> RedSteel;
    public static HolderRegistryEntry<Material> BlueSteel;
    public static HolderRegistryEntry<Material> Basalt;
    public static HolderRegistryEntry<Material> GraniticMineralSand;
    public static HolderRegistryEntry<Material> Redrock;
    public static HolderRegistryEntry<Material> GarnetSand;
    public static HolderRegistryEntry<Material> HSSG;
    public static HolderRegistryEntry<Material> IridiumMetalResidue;
    public static HolderRegistryEntry<Material> Granite;
    public static HolderRegistryEntry<Material> Brick;
    public static HolderRegistryEntry<Material> Fireclay;
    public static HolderRegistryEntry<Material> Diorite;

    /**
     * Fourth Degree Materials
     */
    public static HolderRegistryEntry<Material> RedAlloy;
    public static HolderRegistryEntry<Material> BlueAlloy;
    public static HolderRegistryEntry<Material> BasalticMineralSand;
    public static HolderRegistryEntry<Material> HSSE;
    public static HolderRegistryEntry<Material> HSSS;
    public static HolderRegistryEntry<Material> RadAway;

    /**
     * GCYM Materials
     */
    public static HolderRegistryEntry<Material> TantalumCarbide;
    public static HolderRegistryEntry<Material> HSLASteel;
    public static HolderRegistryEntry<Material> MolybdenumDisilicide;
    public static HolderRegistryEntry<Material> Zeron100;
    public static HolderRegistryEntry<Material> WatertightSteel;
    public static HolderRegistryEntry<Material> IncoloyMA956;
    public static HolderRegistryEntry<Material> MaragingSteel300;
    public static HolderRegistryEntry<Material> HastelloyX;
    public static HolderRegistryEntry<Material> Stellite100;
    public static HolderRegistryEntry<Material> TitaniumCarbide;
    public static HolderRegistryEntry<Material> TitaniumTungstenCarbide;
    public static HolderRegistryEntry<Material> HastelloyC276;
}
