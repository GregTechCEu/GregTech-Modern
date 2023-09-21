package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.*;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import lombok.Getter;
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
import static com.gregtechceu.gtceu.api.data.worldgen.generator.DikeVeinGenerator.DikeBlockDefinition;
import static com.gregtechceu.gtceu.api.data.worldgen.generator.VeinedVeinGenerator.VeinBlockDefinition;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

/**
 * @author KilaBash
 * @date 2023/3/20
 * @implNote GTOres
 */
@SuppressWarnings("unused")
public class GTOres {
    /**
     * The size of the largest registered vein.
     * This becomes available after all veins have been loaded.
     */
    @Getter
    private static int largestVeinSize = 0;

    static {
        VeinGenerators.registerAddonGenerators();
    }

    //////////////////////////////////////
    //********     End Vein    *********//
    //////////////////////////////////////
    public static RuleTest[] END_RULES = new RuleTest[] { WorldGeneratorUtils.END_ORE_REPLACEABLES };

    public static final GTOreDefinition BAUXITE_VEIN_END =
            create("bauxite_vein_end", 30, 0.3f, 40, WorldGenLayers.ENDSTONE, GTOres::end, HeightRangePlacement.uniform(VerticalAnchor.absolute(10), VerticalAnchor.absolute(80)))
                    .biomes(BiomeTags.IS_END)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(END_RULES)
                            .layer(l -> l.weight(2).state(Blocks.END_STONE::defaultBlockState).size(1, 6))
                            .layer(l -> l.weight(2).mat(Bauxite).size(1, 4))
                            .layer(l -> l.weight(1).mat(Ilmenite).size(1, 2))
                            .layer(l -> l.weight(1).mat(Aluminium).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreDefinition MAGNETITE_VEIN_END =
            create("magnetite_vein_end", 35, 0.15f, 30, WorldGenLayers.ENDSTONE, GTOres::end, HeightRangePlacement.uniform(VerticalAnchor.absolute(20), VerticalAnchor.absolute(80)))
                    .biomes(BiomeTags.IS_END)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(END_RULES)
                            .layer(l -> l.weight(3).mat(Magnetite).size(1, 4))
                            .layer(l -> l.weight(2).mat(VanadiumMagnetite).size(1, 2))
                            .layer(l -> l.weight(2).mat(Chromite).size(1, 1))
                            .layer(l -> l.weight(1).mat(Gold).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreDefinition NAQUADAH_VEIN =
            create("naquadah_vein", 44, 1.0f, 30, WorldGenLayers.ENDSTONE, GTOres::end, HeightRangePlacement.uniform(VerticalAnchor.absolute(10), VerticalAnchor.absolute(90)))
                    .biomes(BiomeTags.IS_END)
                    .veinedVeinGenerator()
                    .oreBlock(new VeinBlockDefinition(Naquadah, 3))
                    .rareBlock(new VeinBlockDefinition(Plutonium239, 1))
                    .rareBlockChance(0.25f)
                    .veininessThreshold(0.1f)
                    .maxRichnessThreshold(0.3f)
                    .minRichness(0.3f)
                    .maxRichness(0.5f)
                    .edgeRoundoffBegin(10)
                    .minYLevel(10)
                    .maxYLevel(90)
                    .parent();

    public static final GTOreDefinition PITCHBLENDE_VEIN =
            create("pitchblende_vein_end", 40, 1.0f, 30, WorldGenLayers.ENDSTONE, GTOres::end, HeightRangePlacement.uniform(VerticalAnchor.absolute(30), VerticalAnchor.absolute(60)))
                    .biomes(BiomeTags.IS_END)
                    .veinedVeinGenerator()
                    .oreBlock(new VeinBlockDefinition(Pitchblende, 3))
                    .rareBlock(new VeinBlockDefinition(Uraninite, 2))
                    .rareBlockChance(0.33f)
                    .veininessThreshold(0.1f)
                    .maxRichnessThreshold(0.4f)
                    .minRichness(0.2f)
                    .maxRichness(0.5f)
                    .edgeRoundoffBegin(12)
                    .minYLevel(30)
                    .maxYLevel(60)
                    .parent();

    public static final GTOreDefinition SCHEELITE_VEIN =
            create("scheelite_vein", 60, 0.2f, 20, WorldGenLayers.ENDSTONE, GTOres::end, HeightRangePlacement.uniform(VerticalAnchor.absolute(20), VerticalAnchor.absolute(60)))
                    .biomes(BiomeTags.IS_END)
                    .dikeVeinGenerator()
                    .withBlock(new DikeBlockDefinition(Scheelite, 3, 20, 60))
                    .withBlock(new DikeBlockDefinition(Tungstate, 2, 35, 55))
                    .withBlock(new DikeBlockDefinition(Lithium, 1, 20, 40))
                    .minYLevel(20)
                    .maxYLevel(60)
                    .parent();

    public static final GTOreDefinition SHELDONITE_VEIN =
            create("sheldonite_vein", 25, 0.2f, 10, WorldGenLayers.ENDSTONE, GTOres::end, HeightRangePlacement.uniform(VerticalAnchor.absolute(5), VerticalAnchor.absolute(50)))
                    .biomes(BiomeTags.IS_END)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(END_RULES)
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

    public static final GTOreDefinition BANDED_IRON_VEIN =
            create("banded_iron_vein", 44, 1.0f, 30, WorldGenLayers.NETHERRACK, GTOres::nether, HeightRangePlacement.uniform(VerticalAnchor.absolute(20), VerticalAnchor.absolute(40)))
                    .biomes(BiomeTags.IS_NETHER)
                    .veinedVeinGenerator()
                    .oreBlock(new VeinBlockDefinition(Goethite, 3))
                    .oreBlock(new VeinBlockDefinition(YellowLimonite, 2))
                    .oreBlock(new VeinBlockDefinition(Hematite, 2))
                    .rareBlock(new VeinBlockDefinition(Gold, 1))
                    .rareBlockChance(0.075f)
                    .veininessThreshold(0.1f)
                    .maxRichnessThreshold(0.4f)
                    .minRichness(0.2f)
                    .maxRichness(0.5f)
                    .edgeRoundoffBegin(12)
                    .minYLevel(20)
                    .maxYLevel(40)
                    .parent();

    public static final GTOreDefinition BERYLLIUM_VEIN =
            create("beryllium_vein", 50, 0.25f, 30, WorldGenLayers.NETHERRACK, GTOres::nether, HeightRangePlacement.uniform(VerticalAnchor.absolute(5), VerticalAnchor.absolute(30)))
                    .biomes(BiomeTags.IS_NETHER)
                    .dikeVeinGenerator()
                    .withBlock(new DikeBlockDefinition(Beryllium, 3, 5, 30))
                    .withBlock(new DikeBlockDefinition(Emerald, 2, 5, 19))
                    .withBlock(new DikeBlockDefinition(Emerald, 2, 16, 30))
                    .minYLevel(5)
                    .maxYLevel(30)
                    .parent();

    public static final GTOreDefinition CERTUS_QUARTZ_VEIN =
            create("certus_quartz", 25, 0.25f, 40, WorldGenLayers.NETHERRACK, GTOres::nether, HeightRangePlacement.uniform(VerticalAnchor.absolute(80), VerticalAnchor.absolute(120)))
                    .biomes(BiomeTags.IS_NETHER)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                            .layer(l -> l.weight(3).mat(Quartzite).size(2, 4))
                            .layer(l -> l.weight(2).mat(CertusQuartz).size(1, 1))
                            .layer(l -> l.weight(1).mat(Barite).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreDefinition MANGANESE_VEIN =
            create("manganese_vein", 50, 0.25f, 20, WorldGenLayers.NETHERRACK, GTOres::nether, HeightRangePlacement.uniform(VerticalAnchor.absolute(20), VerticalAnchor.absolute(30)))
                    .biomes(BiomeTags.IS_NETHER)
                    .dikeVeinGenerator()
                    .withBlock(new DikeBlockDefinition(Grossular, 3, 20, 30))
                    .withBlock(new DikeBlockDefinition(Pyrolusite, 2, 20, 26))
                    .withBlock(new DikeBlockDefinition(Tantalite, 1, 24, 30))
                    .minYLevel(20)
                    .maxYLevel(30)
                    .parent();

    public static final GTOreDefinition MOLYBDENUM_VEIN =
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

    public static final GTOreDefinition MONAZITE_VEIN =
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

    public static final GTOreDefinition NETHER_QUARTZ_VEIN =
            create("nether_quartz_vein", 30, 0.2f, 80, WorldGenLayers.NETHERRACK, GTOres::nether, HeightRangePlacement.uniform(VerticalAnchor.absolute(40), VerticalAnchor.absolute(80)))
                    .biomes(BiomeTags.IS_NETHER)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                            .layer(l -> l.weight(2).state(Blocks.NETHERRACK::defaultBlockState).size(2, 4))
                            .layer(l -> l.weight(3).mat(NetherQuartz).size(2, 4))
                            .layer(l -> l.weight(1).mat(Quartzite).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreDefinition REDSTONE_VEIN =
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

    public static final GTOreDefinition SALTPETER_VEIN =
            create("saltpeter_vein", 25, 0.25f, 40, WorldGenLayers.NETHERRACK, GTOres::nether, HeightRangePlacement.uniform(VerticalAnchor.absolute(5), VerticalAnchor.absolute(45)))
                    .biomes(BiomeTags.IS_NETHER)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                            .layer(l -> l.weight(2).state(Blocks.NETHERRACK::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Saltpeter).size(2, 4))
                            .layer(l -> l.weight(2).mat(Diatomite).size(1, 1))
                            .layer(l -> l.weight(2).mat(Electrotine).size(1, 1))
                            .layer(l -> l.weight(1).mat(Alunite).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreDefinition SULFUR_VEIN =
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

    public static final GTOreDefinition TETRAHEDRITE_VEIN =
            create("tetrahedrite_vein", 44, 1.0f, 70, WorldGenLayers.NETHERRACK, GTOres::nether, HeightRangePlacement.uniform(VerticalAnchor.absolute(80), VerticalAnchor.absolute(120)))
                    .biomes(BiomeTags.IS_NETHER)
                    .veinedVeinGenerator()
                    .oreBlock(new VeinBlockDefinition(Tetrahedrite, 4))
                    .oreBlock(new VeinBlockDefinition(Copper, 2))
                    .rareBlock(new VeinBlockDefinition(Stibnite, 1))
                    .rareBlockChance(0.15f)
                    .veininessThreshold(0.1f)
                    .maxRichnessThreshold(0.4f)
                    .minRichness(0.2f)
                    .maxRichness(0.5f)
                    .edgeRoundoffBegin(12)
                    .minYLevel(80)
                    .maxYLevel(120)
                    .parent();

    public static final GTOreDefinition TOPAZ_VEIN =
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

    //////////////////////////////////////
    //*****          Stone         *****//
    //////////////////////////////////////
    public static RuleTest[] OVERWORLD_RULES = new RuleTest[] { new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES) };

    public static final GTOreDefinition APATITE_VEIN =
            create("apatite_vein", 25, 0.25f, 40, WorldGenLayers.STONE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(10), VerticalAnchor.absolute(80)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                           // .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).mat(Apatite).size(2, 4))
                            .layer(l -> l.weight(2).mat(TricalciumPhosphate).size(1, 1))
                            .layer(l -> l.weight(1).mat(Pyrochlore).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreDefinition CASSITERITE_VEIN =
            create("cassiterite_vein", 44, 1.0f, 80, WorldGenLayers.STONE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(10), VerticalAnchor.absolute(80)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .veinedVeinGenerator()
                    .oreBlock(new VeinBlockDefinition(Tin, 4))
                    .rareBlock(new VeinBlockDefinition(Cassiterite, 2))
                    .rareBlockChance(0.33f)
                    .veininessThreshold(0.1f)
                    .maxRichnessThreshold(0.4f)
                    .minRichness(0.2f)
                    .maxRichness(0.5f)
                    .edgeRoundoffBegin(12)
                    .minYLevel(10)
                    .maxYLevel(60)
                    .parent();

    public static final GTOreDefinition COAL_VEIN =
            create("coal_vein", 35, 0.25f, 80, WorldGenLayers.STONE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(10), VerticalAnchor.absolute(140)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(3).mat(Coal).size(2, 4))
                            .layer(l -> l.weight(3).mat(Coal).size(2, 4))
                            .build())
                    .parent();

    public static final GTOreDefinition COPPER_TIN_VEIN =
            create("copper_tin_vein", 44, 1.0f, 50, WorldGenLayers.STONE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(-10), VerticalAnchor.absolute(160)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .veinedVeinGenerator()
                    .oreBlock(new VeinBlockDefinition(Chalcopyrite, 5))
                    .oreBlock(new VeinBlockDefinition(Zeolite, 2))
                    .oreBlock(new VeinBlockDefinition(Cassiterite, 2))
                    .rareBlock(new VeinBlockDefinition(Realgar, 1))
                    .rareBlockChance(0.05f)
                    .veininessThreshold(0.1f)
                    .maxRichnessThreshold(0.4f)
                    .minRichness(0.2f)
                    .maxRichness(0.5f)
                    .edgeRoundoffBegin(12)
                    .minYLevel(-10)
                    .maxYLevel(160)
                    .parent();

    public static final GTOreDefinition GALENA_VEIN =
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

    public static final GTOreDefinition GARNET_TIN_VEIN =
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

    public static final GTOreDefinition GARNET_VEIN =
            create("garnet_vein", 50, 0.25f, 40, WorldGenLayers.STONE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(-10), VerticalAnchor.absolute(50)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .dikeVeinGenerator()
                    .withBlock(new DikeBlockDefinition(GarnetRed, 3, -10, 50))
                    .withBlock(new DikeBlockDefinition(GarnetYellow, 2, -10, 50))
                    .withBlock(new DikeBlockDefinition(Amethyst, 2, -10, 22))
                    .withBlock(new DikeBlockDefinition(Opal, 1, 18, 50))
                    .minYLevel(-10)
                    .minYLevel(50)
                    .parent();

    public static final GTOreDefinition IRON_VEIN =
            create("iron_vein", 44, 1.0f, 120, WorldGenLayers.STONE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(-10), VerticalAnchor.absolute(60)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .veinedVeinGenerator()
                    .oreBlock(new VeinBlockDefinition(Goethite, 5))
                    .oreBlock(new VeinBlockDefinition(YellowLimonite, 2))
                    .oreBlock(new VeinBlockDefinition(Hematite, 2))
                    .oreBlock(new VeinBlockDefinition(Malachite, 1))
                    .veininessThreshold(0.1f)
                    .maxRichnessThreshold(0.4f)
                    .minRichness(0.2f)
                    .maxRichness(0.5f)
                    .edgeRoundoffBegin(12)
                    .minYLevel(-10)
                    .maxYLevel(60)
                    .parent();

    public static final GTOreDefinition LUBRICANT_VEIN =
            create("lubricant_vein", 25, 0.25f, 40, WorldGenLayers.STONE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(50)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(3).mat(Soapstone).size(2, 4))
                            .layer(l -> l.weight(2).mat(Talc).size(1, 1))
                            .layer(l -> l.weight(2).mat(GlauconiteSand).size(1, 1))
                            .layer(l -> l.weight(1).mat(Pentlandite).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreDefinition MAGNETITE_VEIN_OW =
            create("magnetite_vein_ow", 35, 0.15f, 80, WorldGenLayers.STONE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(10), VerticalAnchor.absolute(60)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(3).mat(Magnetite).size(2, 4))
                            .layer(l -> l.weight(2).mat(VanadiumMagnetite).size(1, 1))
                            .layer(l -> l.weight(1).mat(Gold).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreDefinition MINERAL_SAND_VEIN =
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

    public static final GTOreDefinition NICKEL_VEIN =
            create("nickel_vein", 25, 0.25f, 40, WorldGenLayers.STONE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(-10), VerticalAnchor.absolute(60)))
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

    public static final GTOreDefinition SALTS_VEIN =
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

    //////////////////////////////////////
    //*****        Deepslate       *****//
    //////////////////////////////////////
    public static RuleTest[] DEEPSLATE_RULES = new RuleTest[] { new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES) };

    public static final GTOreDefinition COPPER_VEIN =
            create("copper_vein", 44, 1.0f, 80, WorldGenLayers.DEEPSLATE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(-40), VerticalAnchor.absolute(10)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .veinedVeinGenerator()
                    .oreBlock(new VeinBlockDefinition(Chalcopyrite, 5))
                    .oreBlock(new VeinBlockDefinition(Iron, 2))
                    .oreBlock(new VeinBlockDefinition(Pyrite, 2))
                    .oreBlock(new VeinBlockDefinition(Copper, 2))
                    .veininessThreshold(0.1f)
                    .maxRichnessThreshold(0.4f)
                    .minRichness(0.2f)
                    .maxRichness(0.5f)
                    .edgeRoundoffBegin(12)
                    .minYLevel(-40)
                    .maxYLevel(-10)
                    .parent();

    public static final GTOreDefinition DIAMOND_VEIN =
            create("diamond_vein", 25, 0.25f, 40, WorldGenLayers.DEEPSLATE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(-65), VerticalAnchor.absolute(-30)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(3).mat(Graphite).size(2, 4))
                            .layer(l -> l.weight(2).mat(Diamond).size(1, 1))
                            .layer(l -> l.weight(1).mat(Coal).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreDefinition LAPIS_VEIN =
            create("lapis_vein", 50, 0.25f, 40, WorldGenLayers.DEEPSLATE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(-60), VerticalAnchor.absolute(10)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .dikeVeinGenerator()
                    .withBlock(new DikeBlockDefinition(Lazurite, 3, -60, 10))
                    .withBlock(new DikeBlockDefinition(Sodalite, 2, -50, 0))
                    .withBlock(new DikeBlockDefinition(Lapis, 2, -50, 0))
                    .withBlock(new DikeBlockDefinition(Calcite, 1, -40, 10))
                    .parent();

    public static final GTOreDefinition MANGANESE_VEIN_OW =
            create("manganese_vein_ow", 50, 0.25f, 20, WorldGenLayers.DEEPSLATE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(-30), VerticalAnchor.absolute(0)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .dikeVeinGenerator()
                    .withBlock(new DikeBlockDefinition(Grossular, 3, 20, 30))
                    .withBlock(new DikeBlockDefinition(Spessartine, 2, 20, 30))
                    .withBlock(new DikeBlockDefinition(Pyrolusite, 2, 20, 26))
                    .withBlock(new DikeBlockDefinition(Tantalite, 1, 24, 30))
                    .minYLevel(-50)
                    .maxYLevel(-5)
                    .parent();

    public static final GTOreDefinition MICA_VEIN =
            create("mica_vein", 25, 0.25f, 20, WorldGenLayers.DEEPSLATE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(-40), VerticalAnchor.absolute(-10)))
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

    public static final GTOreDefinition OLIVINE_VEIN =
            create("olivine_vein", 25, 0.25f, 20, WorldGenLayers.DEEPSLATE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(-20), VerticalAnchor.absolute(10)))
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

    public static final GTOreDefinition REDSTONE_VEIN_OW =
            create("redstone_vein_ow", 30, 0.2f, 60, WorldGenLayers.DEEPSLATE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(-65), VerticalAnchor.absolute(-10)))
                    .biomes(BiomeTags.IS_OVERWORLD)
                    .layeredVeinGenerator()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(3).mat(Redstone).size(2, 4))
                            .layer(l -> l.weight(2).mat(Ruby).size(1, 1))
                            .layer(l -> l.weight(1).mat(Cinnabar).size(1, 1))
                            .build())
                    .parent();

    public static final GTOreDefinition SAPPHIRE_VEIN =
            create("sapphire_vein", 25, 0.25f, 60, WorldGenLayers.DEEPSLATE, GTOres::overworld, HeightRangePlacement.uniform(VerticalAnchor.absolute(-40), VerticalAnchor.absolute(0)))
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

    private static GTOreDefinition create(String name, int clusterSize, float density, int weight, WorldGenLayers layer, Supplier<HolderSet<DimensionType>> dimensionFilter, HeightRangePlacement range) {
        return new GTOreDefinition(GTCEu.id(name), clusterSize, density, weight, layer, dimensionFilter, range, 0.0F, null, null, null);
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

    public static void updateLargestVeinSize() {
        GTOres.largestVeinSize = GTRegistries.ORE_VEINS.values().stream()
                .map(GTOreDefinition::getClusterSize)
                .max(Integer::compareTo)
                .orElse(0);
    }
}
