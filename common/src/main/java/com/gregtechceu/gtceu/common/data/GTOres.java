package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.*;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.ore;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.oreNetherrack;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

/**
 * @author KilaBash
 * @date 2023/3/20
 * @implNote GTOres
 */
@SuppressWarnings("unused")
public class GTOres {

    static {
        VeinGenerators.registerAddonGenerators();
    }

    //////////////////////////////////////
    //********     End Vein    *********//
    //////////////////////////////////////
    public static RuleTest[] END_RULES = new RuleTest[] { WorldGeneratorUtils.END_ORE_REPLACEABLES };
    public static final GTOreFeatureEntry BAUXITE_VEIN =
            create("bauxite_vein", 25, 0.25f, 40, WorldGenLayers.ENDSTONE, GTOres::end, HeightRangePlacement.uniform(VerticalAnchor.absolute(10), VerticalAnchor.absolute(80)))
                    .biomes(BiomeTags.IS_END)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(END_RULES)
                           // .layer(l -> l.weight(1).state(Blocks.END_STONE::defaultBlockState))
                            .layer(l -> l.weight(2).mat(Bauxite).size(1, 4))
                            .layer(l -> l.weight(1).mat(Ilmenite).size(1, 2))
                            .layer(l -> l.weight(1).mat(Aluminium).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry MAGNETITE_VEIN =
            create("magnetite_vein", 35, 0.15f, 30, WorldGenLayers.ENDSTONE, GTOres::end, HeightRangePlacement.uniform(VerticalAnchor.absolute(20), VerticalAnchor.absolute(80)))
                    .biomes(BiomeTags.IS_END)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(END_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.END_STONE::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Magnetite).size(1, 4))
                            .layer(l -> l.weight(2).mat(VanadiumMagnetite).size(1, 2))
                            .layer(l -> l.weight(2).mat(Chromite).size(1, 1))
                            .layer(l -> l.weight(1).mat(Gold).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry NAQUADAH_VEIN =
            create("naquadah_vein", 35, 0.15f, 30, WorldGenLayers.ENDSTONE, GTOres::end, HeightRangePlacement.uniform(VerticalAnchor.absolute(10), VerticalAnchor.absolute(90)))
                    .biomes(BiomeTags.IS_END)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(END_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.END_STONE::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Naquadah).size(1, 4))
                            .layer(l -> l.weight(1).mat(Plutonium239).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry PITCHBLENDE_VEIN =
            create("pitchblende_vein", 25, 0.25f, 30, WorldGenLayers.ENDSTONE, GTOres::end, HeightRangePlacement.uniform(VerticalAnchor.absolute(30), VerticalAnchor.absolute(60)))
                    .biomes(BiomeTags.IS_END)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(END_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.END_STONE::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Pitchblende).size(2, 4))
                            .layer(l -> l.weight(2).mat(Uraninite).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry SCHEELITE_VEIN =
            create("scheelite_vein", 30, 0.2f, 20, WorldGenLayers.ENDSTONE, GTOres::end, HeightRangePlacement.uniform(VerticalAnchor.absolute(20), VerticalAnchor.absolute(60)))
                    .biomes(BiomeTags.IS_END)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(END_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.END_STONE::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Scheelite).size(2, 4))
                            .layer(l -> l.weight(2).mat(Tungstate).size(1, 1))
                            .layer(l -> l.weight(1).mat(Lithium).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry SHELDONITE_VEIN =
            create("sheldonite_vein", 25, 0.2f, 10, WorldGenLayers.ENDSTONE, GTOres::end, HeightRangePlacement.uniform(VerticalAnchor.absolute(5), VerticalAnchor.absolute(50)))
                    .biomes(BiomeTags.IS_END)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(END_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.END_STONE::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Bornite).size(2, 4))
                            .layer(l -> l.weight(2).mat(Cooperite).size(1, 1))
                            .layer(l -> l.weight(2).mat(Platinum).size(1, 1))
                            .layer(l -> l.weight(1).mat(Palladium).size(1, 1))
                            .build())
                    .parent();

    //////////////////////////////////////
    //******     Nether Vein     *******//
    //////////////////////////////////////
    public static RuleTest[] NETHER_RULES = new RuleTest[] { new TagMatchTest(BlockTags.NETHER_CARVER_REPLACEABLES) };

    public static final GTOreFeatureEntry BANDED_IRON_VEIN =
            create("banded_iron_vein", 30, 0.2f, 30, WorldGenLayers.NETHERRACK, GTOres::nether, HeightRangePlacement.uniform(VerticalAnchor.absolute(20), VerticalAnchor.absolute(40)))
                    .biomes(BiomeTags.IS_NETHER)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                            //.layer(l -> l.weight(2).state(Blocks.NETHERRACK::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Goethite).size(2, 4))
                            .layer(l -> l.weight(2).mat(YellowLimonite).size(1, 1))
                            .layer(l -> l.weight(2).mat(Hematite).size(1, 1))
                            .layer(l -> l.weight(1).mat(Gold).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry BERYLLIUM_VEIN =
            create("beryllium_vein", 25, 0.25f, 30, WorldGenLayers.NETHERRACK, GTOres::nether, HeightRangePlacement.uniform(VerticalAnchor.absolute(5), VerticalAnchor.absolute(30)))
                    .biomes(BiomeTags.IS_NETHER)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.NETHERRACK::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Beryllium).size(2, 4))
                            .layer(l -> l.weight(2).mat(Emerald).size(1, 1))
                            .layer(l -> l.weight(1).mat(Thorium).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry CERTUS_QUARTZ_VEIN =
            create("certus_quartz", 25, 0.25f, 40, WorldGenLayers.NETHERRACK, GTOres::nether, HeightRangePlacement.uniform(VerticalAnchor.absolute(80), VerticalAnchor.absolute(120)))
                    .biomes(BiomeTags.IS_NETHER)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.NETHERRACK::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Quartzite).size(2, 4))
                            .layer(l -> l.weight(2).mat(CertusQuartz).size(1, 1))
                            .layer(l -> l.weight(1).mat(Barite).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry MANGANESE_VEIN =
            create("manganese_vein", 25, 0.25f, 20, WorldGenLayers.NETHERRACK, GTOres::nether, HeightRangePlacement.uniform(VerticalAnchor.absolute(20), VerticalAnchor.absolute(30)))
                    .biomes(BiomeTags.IS_NETHER)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.NETHERRACK::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Grossular).size(2, 4))
                            .layer(l -> l.weight(2).mat(Pyrolusite).size(1, 1))
                            .layer(l -> l.weight(1).mat(Tantalite).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry MOLYBDENUM_VEIN =
            create("molybdenum_vein", 25, 0.25f, 5, WorldGenLayers.NETHERRACK, GTOres::nether, HeightRangePlacement.uniform(VerticalAnchor.absolute(20), VerticalAnchor.absolute(50)))
                    .biomes(BiomeTags.IS_NETHER)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.NETHERRACK::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Wulfenite).size(2, 4))
                            .layer(l -> l.weight(2).mat(Molybdenite).size(1, 1))
                            .layer(l -> l.weight(1).mat(Molybdenum).size(1, 1))
                            .layer(l -> l.weight(1).mat(Powellite).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry MONAZITE_VEIN =
            create("monazite_vein", 25, 0.25f, 30, WorldGenLayers.NETHERRACK, GTOres::nether, HeightRangePlacement.uniform(VerticalAnchor.absolute(20), VerticalAnchor.absolute(40)))
                    .biomes(BiomeTags.IS_NETHER)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.NETHERRACK::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Bastnasite).size(2, 4))
                            .layer(l -> l.weight(1).mat(Molybdenum).size(1, 1))
                            .layer(l -> l.weight(1).mat(Neodymium).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry NETHER_QUARTZ_VEIN =
            create("nether_quartz_vein", 30, 0.2f, 80, WorldGenLayers.NETHERRACK, GTOres::nether, HeightRangePlacement.uniform(VerticalAnchor.absolute(40), VerticalAnchor.absolute(80)))
                    .biomes(BiomeTags.IS_NETHER)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.NETHERRACK::defaultBlockState))
                            .layer(l -> l.weight(3).mat(NetherQuartz).size(2, 4))
                            .layer(l -> l.weight(1).mat(Quartzite).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry REDSTONE_VEIN =
            create("redstone_vein", 30, 0.2f, 60, WorldGenLayers.NETHERRACK, GTOres::nether, HeightRangePlacement.uniform(VerticalAnchor.absolute(5), VerticalAnchor.absolute(40)))
                    .biomes(BiomeTags.IS_NETHER)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.NETHERRACK::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Redstone).size(2, 4))
                            .layer(l -> l.weight(2).mat(Ruby).size(1, 1))
                            .layer(l -> l.weight(1).mat(Cinnabar).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry SALTPETER_VEIN =
            create("saltpeter_vein", 25, 0.25f, 40, WorldGenLayers.NETHERRACK, GTOres::nether, HeightRangePlacement.uniform(VerticalAnchor.absolute(5), VerticalAnchor.absolute(45)))
                    .biomes(BiomeTags.IS_NETHER)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.NETHERRACK::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Saltpeter).size(2, 4))
                            .layer(l -> l.weight(2).mat(Diatomite).size(1, 1))
                            .layer(l -> l.weight(2).mat(Electrotine).size(1, 1))
                            .layer(l -> l.weight(1).mat(Alunite).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry SULFUR_VEIN =
            create("sulfur_vein", 30, 0.2f, 100, WorldGenLayers.NETHERRACK, GTOres::nether, HeightRangePlacement.uniform(VerticalAnchor.absolute(10), VerticalAnchor.absolute(30)))
                    .biomes(BiomeTags.IS_NETHER)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.NETHERRACK::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Sulfur).size(2, 4))
                            .layer(l -> l.weight(2).mat(Pyrite).size(1, 1))
                            .layer(l -> l.weight(1).mat(Sphalerite).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry TETRAHEDRITE_VEIN =
            create("tetrahedrite_vein", 30, 0.2f, 70, WorldGenLayers.NETHERRACK, GTOres::nether, HeightRangePlacement.uniform(VerticalAnchor.absolute(80), VerticalAnchor.absolute(120)))
                    .biomes(BiomeTags.IS_NETHER)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.NETHERRACK::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Tetrahedrite).size(2, 4))
                            .layer(l -> l.weight(2).mat(Copper).size(1, 1))
                            .layer(l -> l.weight(1).mat(Stibnite).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry TOPAZ_VEIN =
            create("topaz_vein", 25, 0.25f, 70, WorldGenLayers.NETHERRACK, GTOres::nether, HeightRangePlacement.uniform(VerticalAnchor.absolute(80), VerticalAnchor.absolute(120)))
                    .biomes(BiomeTags.IS_NETHER)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.NETHERRACK::defaultBlockState))
                            .layer(l -> l.weight(3).mat(BlueTopaz).size(2, 4))
                            .layer(l -> l.weight(2).mat(Topaz).size(1, 1))
                            .layer(l -> l.weight(2).mat(Chalcocite).size(1, 1))
                            .layer(l -> l.weight(1).mat(Bornite).size(1, 1))
                            .build())
                    .parent();

    //////////////////////////////////////
    //*****     Overworld Vein     *****//
    //////////////////////////////////////
    public static RuleTest[] OVERWORLD_RULES = new RuleTest[] { new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES)};
    public static RuleTest[] DEEPSLATE_RULES = new RuleTest[] { new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES) };

    public static final GTOreFeatureEntry APATITE_VEIN =
            create("apatite_vein", 25, 0.25f, 40, WorldGenLayers.STONE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(10), VerticalAnchor.absolute(60)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Apatite).size(2, 4))
                            .layer(l -> l.weight(2).mat(TricalciumPhosphate).size(1, 1))
                            .layer(l -> l.weight(1).mat(Pyrochlore).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry CASSITERITE_VEIN =
            create("cassiterite_vein", 30, 0.2f, 80, WorldGenLayers.STONE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(10), VerticalAnchor.absolute(60)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(4).mat(Tin).size(2, 4))
                            .layer(l -> l.weight(2).mat(Cassiterite).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry COAL_VEIN =
            create("coal_vein", 35, 0.25f, 80, WorldGenLayers.STONE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(10), VerticalAnchor.absolute(80)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                           // .layer(l -> l.weight(1).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Coal).size(2, 4))
                            .layer(l -> l.weight(3).mat(Coal).size(2, 4))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry COPPER_TIN_VEIN =
            create("copper_tin_vein", 30, 0.2f, 50, WorldGenLayers.STONE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(-10), VerticalAnchor.absolute(160)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Chalcopyrite).size(2, 4))
                            .layer(l -> l.weight(2).mat(Zeolite).size(1, 1))
                            .layer(l -> l.weight(2).mat(Cassiterite).size(1, 1))
                            .layer(l -> l.weight(1).mat(Realgar).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry COPPER_VEIN =
            create("copper_vein", 30, 0.2f, 80, WorldGenLayers.STONE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(-15), VerticalAnchor.absolute(60)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Chalcopyrite).size(2, 4))
                            .layer(l -> l.weight(2).mat(Iron).size(1, 1))
                            .layer(l -> l.weight(2).mat(Pyrite).size(1, 1))
                            .layer(l -> l.weight(1).mat(Copper).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry DIAMOND_VEIN =
            create("diamond_vein", 25, 0.25f, 40, WorldGenLayers.STONE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(-65), VerticalAnchor.absolute(20)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Graphite).size(2, 4))
                            .layer(l -> l.weight(2).mat(Diamond).size(1, 1))
                            .layer(l -> l.weight(1).mat(Coal).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry GALENA_VEIN =
            create("galena_vein", 25, 0.25f, 40, WorldGenLayers.STONE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(-15), VerticalAnchor.absolute(45)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Galena).size(2, 4))
                            .layer(l -> l.weight(2).mat(Silver).size(1, 1))
                            .layer(l -> l.weight(1).mat(Lead).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry GARNET_TIN_VEIN =
            create("garnet_tin_vein", 30, 0.2f, 80, WorldGenLayers.STONE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(30), VerticalAnchor.absolute(60)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(2).state(Blocks.SAND::defaultBlockState))
                            .layer(l -> l.weight(3).mat(CassiteriteSand).size(2, 4))
                            .layer(l -> l.weight(2).mat(GarnetSand).size(1, 1))
                            .layer(l -> l.weight(2).mat(Asbestos).size(1, 1))
                            .layer(l -> l.weight(1).mat(Diatomite).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry GARNET_VEIN =
            create("garnet_vein", 25, 0.25f, 40, WorldGenLayers.STONE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(-10), VerticalAnchor.absolute(30)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            // .layer(l -> l.weight(2).state(Blocks.SAND::defaultBlockState))
                            .layer(l -> l.weight(3).mat(GarnetRed).size(2, 4))
                            .layer(l -> l.weight(2).mat(GarnetYellow).size(1, 1))
                            .layer(l -> l.weight(2).mat(Amethyst).size(1, 1))
                            .layer(l -> l.weight(1).mat(Opal).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry IRON_VEIN =
            create("iron_vein", 30, 0.2f, 120, WorldGenLayers.STONE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(-10), VerticalAnchor.absolute(40)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Goethite).size(2, 4))
                            .layer(l -> l.weight(2).mat(YellowLimonite).size(1, 1))
                            .layer(l -> l.weight(2).mat(Hematite).size(1, 1))
                            .layer(l -> l.weight(1).mat(Malachite).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry LAPIS_VEIN =
            create("lapis_vein", 25, 0.25f, 40, WorldGenLayers.STONE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(50)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Lazurite).size(2, 4))
                            .layer(l -> l.weight(2).mat(Sodalite).size(1, 1))
                            .layer(l -> l.weight(2).mat(Lapis).size(1, 1))
                            .layer(l -> l.weight(1).mat(Calcite).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry LUBRICANT_VEIN =
            create("lubricant_vein", 25, 0.25f, 40, WorldGenLayers.STONE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(50)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Soapstone).size(2, 4))
                            .layer(l -> l.weight(2).mat(Talc).size(1, 1))
                            .layer(l -> l.weight(2).mat(GlauconiteSand).size(1, 1))
                            .layer(l -> l.weight(1).mat(Pentlandite).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry MAGNETITE_OVER_VEIN =
            create("magnetite_over_vein", 35, 0.15f, 80, WorldGenLayers.STONE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(10), VerticalAnchor.absolute(60)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Magnetite).size(2, 4))
                            .layer(l -> l.weight(2).mat(VanadiumMagnetite).size(1, 1))
                            .layer(l -> l.weight(1).mat(Gold).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry MANGANESE_OVER_VEIN =
            create("manganese_over_vein", 25, 0.25f, 20, WorldGenLayers.STONE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(30)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Grossular).size(2, 4))
                            .layer(l -> l.weight(2).mat(Spessartine).size(1, 1))
                            .layer(l -> l.weight(2).mat(Pyrolusite).size(1, 1))
                            .layer(l -> l.weight(1).mat(Tantalite).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry MICA_VEIN =
            create("mica_vein", 25, 0.25f, 20, WorldGenLayers.STONE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(40)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Kyanite).size(2, 4))
                            .layer(l -> l.weight(2).mat(Mica).size(1, 1))
                            .layer(l -> l.weight(2).mat(Bauxite).size(1, 1))
                            .layer(l -> l.weight(1).mat(Pollucite).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry MINERAL_SAND_VEIN =
            create("mineral_sand_vein", 30, 0.2f, 80, WorldGenLayers.STONE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(15), VerticalAnchor.absolute(60)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(2).state(Blocks.SAND::defaultBlockState))
                            .layer(l -> l.weight(3).mat(BasalticMineralSand).size(2, 4))
                            .layer(l -> l.weight(2).mat(GraniticMineralSand).size(1, 1))
                            .layer(l -> l.weight(2).mat(FullersEarth).size(1, 1))
                            .layer(l -> l.weight(1).mat(Gypsum).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry NICKEL_VEIN =
            create("nickel_vein", 25, 0.25f, 40, WorldGenLayers.STONE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(-10), VerticalAnchor.absolute(40)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Garnierite).size(2, 4))
                            .layer(l -> l.weight(2).mat(Nickel).size(1, 1))
                            .layer(l -> l.weight(2).mat(Cobaltite).size(1, 1))
                            .layer(l -> l.weight(1).mat(Pentlandite).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry OILSANDS_VEIN =
            create("oilsands_vein", 25, 0.3f, 20, WorldGenLayers.STONE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(30), VerticalAnchor.absolute(80)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(1).state(Blocks.SAND::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Oilsands).size(2, 4))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry OLIVINE_VEIN =
            create("olivine_vein", 25, 0.25f, 20, WorldGenLayers.STONE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(-10), VerticalAnchor.absolute(40)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Bentonite).size(2, 4))
                            .layer(l -> l.weight(2).mat(Magnetite).size(1, 1))
                            .layer(l -> l.weight(2).mat(Olivine).size(1, 1))
                            .layer(l -> l.weight(1).mat(GlauconiteSand).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry REDSTONE_OVER_VEIN =
            create("redstone_over_vein", 30, 0.2f, 60, WorldGenLayers.STONE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(-15), VerticalAnchor.absolute(40)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Redstone).size(2, 4))
                            .layer(l -> l.weight(2).mat(Ruby).size(1, 1))
                            .layer(l -> l.weight(1).mat(Cinnabar).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry SALTS_VEIN =
            create("salts_vein", 30, 0.2f, 50, WorldGenLayers.STONE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(30), VerticalAnchor.absolute(70)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).mat(RockSalt).size(2, 4))
                            .layer(l -> l.weight(2).mat(Salt).size(1, 1))
                            .layer(l -> l.weight(1).mat(Lepidolite).size(1, 1))
                            .layer(l -> l.weight(1).mat(Spodumene).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreFeatureEntry SAPPHIRE_VEIN =
            create("sapphire_vein", 25, 0.25f, 60, WorldGenLayers.STONE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(-10), VerticalAnchor.absolute(40)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Almandine).size(2, 4))
                            .layer(l -> l.weight(2).mat(Pyrope).size(1, 1))
                            .layer(l -> l.weight(1).mat(Sapphire).size(1, 1))
                            .layer(l -> l.weight(1).mat(GreenSapphire).size(1, 1))
                            .build())
                    .parent();

    private static GTOreFeatureEntry create(String name, int clusterSize, float density, int weight, WorldGenLayers layer, Supplier<HolderSet<DimensionType>> dimensionFilter, HeightRangePlacement range) {
        return new GTOreFeatureEntry(GTCEu.id(name), clusterSize, density, weight, layer, dimensionFilter, range, 0.0F, null, null, null);
    }

    private static Supplier<? extends Block> ore(TagPrefix oreTag, Material material) {
        var block = GTBlocks.MATERIAL_BLOCKS.get(oreTag, material);
        if (block == null) {
            ResourceLocation oreKey;
            if (oreTag == ore) {
                oreKey = new ResourceLocation("%s_ore".formatted(material.getName()));
            } else if (oreTag == oreNetherrack) {
                oreKey = new ResourceLocation("nether_%s_ore".formatted(material.getName()));
            } else {
                oreKey = new ResourceLocation("%s_%s_ore".formatted(oreTag.name, material.getName()));
            }
            return BuiltInRegistries.BLOCK.containsKey(oreKey) ? () -> BuiltInRegistries.BLOCK.get(oreKey) : () -> Blocks.AIR;
        }
        return block;
    }

    public static HolderSet<DimensionType> overworld() {
        return HolderSet.direct(GTRegistries.builtinRegistry().registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(BuiltinDimensionTypes.OVERWORLD));
    }

    public static HolderSet<DimensionType> nether() {
        return HolderSet.direct(GTRegistries.builtinRegistry().registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(BuiltinDimensionTypes.NETHER));
    }

    public static HolderSet<DimensionType> end() {
        return HolderSet.direct(GTRegistries.builtinRegistry().registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(BuiltinDimensionTypes.END));
    }

    public static void init() {
    }

}
