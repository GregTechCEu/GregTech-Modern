package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.*;
import com.gregtechceu.gtceu.api.data.worldgen.generator.indicators.SurfaceIndicatorGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.generator.veins.NoopVeinGenerator;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import lombok.Getter;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.ore;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.oreNetherrack;
import static com.gregtechceu.gtceu.api.data.worldgen.generator.veins.DikeVeinGenerator.DikeBlockDefinition;
import static com.gregtechceu.gtceu.api.data.worldgen.generator.veins.VeinedVeinGenerator.VeinBlockDefinition;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

@SuppressWarnings("unused")
public class GTOres {

    /**
     * The size of the largest registered vein.
     * This becomes available after all veins have been loaded.
     */
    @Getter
    private static int largestVeinSize = 0;

    @Getter
    private static int largestIndicatorOffset = 0;

    private static final Map<ResourceLocation, GTOreDefinition> toReRegister = new HashMap<>();

    //////////////////////////////////////
    // ******** End Vein *********//
    //////////////////////////////////////
    public static RuleTest[] END_RULES = new RuleTest[] { WorldGeneratorUtils.END_ORE_REPLACEABLES };

    public static final GTOreDefinition BAUXITE_VEIN_END = create("bauxite_vein_end", vein -> vein
            .clusterSize(UniformInt.of(32, 40)).density(0.3f).weight(40)
            .layer(WorldGenLayers.ENDSTONE)
            .heightRangeUniform(10, 80)
            .biomes(BiomeTags.IS_END)
            .layeredVeinGenerator(generator -> generator
                    .withLayerPattern(() -> GTLayerPattern.builder(END_RULES)
                            .layer(l -> l.weight(2).state(Blocks.END_STONE::defaultBlockState).size(1, 6))
                            .layer(l -> l.weight(2).mat(Bauxite).size(1, 4))
                            .layer(l -> l.weight(1).mat(Ilmenite).size(1, 2))
                            .layer(l -> l.weight(1).mat(Aluminium).size(1, 1))
                            .build()))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Bauxite)
                    .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)));

    public static final GTOreDefinition MAGNETITE_VEIN_END = create("magnetite_vein_end", vein -> vein
            .clusterSize(UniformInt.of(38, 44)).density(0.15f).weight(30)
            .layer(WorldGenLayers.ENDSTONE)
            .heightRangeUniform(20, 80)
            .biomes(BiomeTags.IS_END)
            .layeredVeinGenerator(generator -> generator
                    .withLayerPattern(() -> GTLayerPattern.builder(END_RULES)
                            .layer(l -> l.weight(3).mat(Magnetite).size(1, 4))
                            .layer(l -> l.weight(2).mat(VanadiumMagnetite).size(1, 2))
                            .layer(l -> l.weight(2).mat(Chromite).size(1, 1))
                            .layer(l -> l.weight(1).mat(Gold).size(1, 1))
                            .build()))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Magnetite)
                    .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)));

    public static final GTOreDefinition NAQUADAH_VEIN = create("naquadah_vein", vein -> vein
            .clusterSize(UniformInt.of(48, 80)).density(0.25f).weight(30)
            .layer(WorldGenLayers.ENDSTONE)
            .heightRangeUniform(10, 90)
            .biomes(BiomeTags.IS_END)
            .cuboidVeinGenerator(generator -> generator
                    .top(b -> b.mat(Naquadah).size(2))
                    .middle(b -> b.mat(Naquadah).size(3))
                    .bottom(b -> b.mat(Naquadah).size(2))
                    .spread(b -> b.mat(Plutonium239)))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Naquadah)
                    .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)));

    public static final GTOreDefinition PITCHBLENDE_VEIN = create("pitchblende_vein_end", vein -> vein
            .clusterSize(UniformInt.of(32, 64)).density(0.25f).weight(30)
            .layer(WorldGenLayers.ENDSTONE)
            .heightRangeUniform(30, 60)
            .biomes(BiomeTags.IS_END)
            .cuboidVeinGenerator(generator -> generator
                    .top(b -> b.mat(Pitchblende).size(2))
                    .middle(b -> b.mat(Pitchblende).size(3))
                    .bottom(b -> b.mat(Pitchblende).size(2))
                    .spread(b -> b.mat(Uraninite)))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Pitchblende)
                    .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)));

    public static final GTOreDefinition SCHEELITE_VEIN = create("scheelite_vein", vein -> vein
            .clusterSize(UniformInt.of(50, 64)).density(0.7f).weight(20)
            .layer(WorldGenLayers.ENDSTONE)
            .heightRangeUniform(20, 60)
            .biomes(BiomeTags.IS_END)
            .dikeVeinGenerator(generator -> generator
                    .withBlock(new DikeBlockDefinition(Scheelite, 3, 20, 60))
                    .withBlock(new DikeBlockDefinition(Tungstate, 2, 35, 55))
                    .withBlock(new DikeBlockDefinition(Lithium, 1, 20, 40)))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Scheelite)
                    .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)));

    public static final GTOreDefinition SHELDONITE_VEIN = create("sheldonite_vein", vein -> vein
            .clusterSize(UniformInt.of(25, 29)).density(0.2f).weight(10)
            .layer(WorldGenLayers.ENDSTONE)
            .heightRangeUniform(5, 50)
            .biomes(BiomeTags.IS_END)
            .layeredVeinGenerator(generator -> generator
                    .withLayerPattern(() -> GTLayerPattern.builder(END_RULES)
                            .layer(l -> l.weight(3).mat(Bornite).size(2, 4))
                            .layer(l -> l.weight(2).mat(Cooperite).size(1, 1))
                            .layer(l -> l.weight(2).mat(Platinum).size(1, 1))
                            .layer(l -> l.weight(1).mat(Palladium).size(1, 1))
                            .build()))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Platinum)
                    .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)));

    //////////////////////////////////////
    // ****** Nether Vein *******//
    //////////////////////////////////////
    public static RuleTest[] NETHER_RULES = new RuleTest[] { new TagMatchTest(BlockTags.NETHER_CARVER_REPLACEABLES) };

    public static final GTOreDefinition BANDED_IRON_VEIN = create("banded_iron_vein", vein -> vein
            .clusterSize(UniformInt.of(40, 52)).density(1.0f).weight(30)
            .layer(WorldGenLayers.NETHERRACK)
            .heightRangeUniform(20, 40)
            .biomes(BiomeTags.IS_NETHER)
            .veinedVeinGenerator(generator -> generator
                    .oreBlock(new VeinBlockDefinition(Goethite, 3))
                    .oreBlock(new VeinBlockDefinition(Limonite, 2))
                    .oreBlock(new VeinBlockDefinition(Hematite, 2))
                    .rareBlock(new VeinBlockDefinition(Gold, 1))
                    .rareBlockChance(0.075f)
                    .veininessThreshold(0.01f)
                    .maxRichnessThreshold(0.175f)
                    .minRichness(0.7f)
                    .maxRichness(1.0f)
                    .edgeRoundoffBegin(3)
                    .maxEdgeRoundoff(0.1f))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Goethite)
                    .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)));

    public static final GTOreDefinition BERYLLIUM_VEIN = create("beryllium_vein", vein -> vein
            .clusterSize(UniformInt.of(50, 64)).density(0.75f).weight(30)
            .layer(WorldGenLayers.NETHERRACK)
            .heightRangeUniform(5, 30)
            .biomes(BiomeTags.IS_NETHER)
            .dikeVeinGenerator(generator -> generator
                    .withBlock(new DikeBlockDefinition(Beryllium, 3, 5, 30))
                    .withBlock(new DikeBlockDefinition(Emerald, 2, 5, 19))
                    .withBlock(new DikeBlockDefinition(Thorium, 1, 16, 30)))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Beryllium)
                    .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)));

    public static final GTOreDefinition CERTUS_QUARTZ_VEIN = create("certus_quartz", vein -> vein
            .clusterSize(UniformInt.of(25, 29)).density(0.25f).weight(40)
            .layer(WorldGenLayers.NETHERRACK)
            .heightRangeUniform(80, 120)
            .biomes(BiomeTags.IS_NETHER)
            .layeredVeinGenerator(generator -> generator
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                            .layer(l -> l.weight(3).mat(Quartzite).size(2, 4))
                            .layer(l -> l.weight(2).mat(CertusQuartz).size(1, 1))
                            .layer(l -> l.weight(1).mat(Barite).size(1, 1))
                            .build()))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(CertusQuartz)
                    .placement(SurfaceIndicatorGenerator.IndicatorPlacement.BELOW)));

    public static final GTOreDefinition MANGANESE_VEIN = create("manganese_vein", vein -> vein
            .clusterSize(UniformInt.of(50, 64)).density(0.75f).weight(20)
            .layer(WorldGenLayers.NETHERRACK)
            .heightRangeUniform(20, 30)
            .biomes(BiomeTags.IS_NETHER)
            .dikeVeinGenerator(generator -> generator
                    .withBlock(new DikeBlockDefinition(Grossular, 3, 20, 30))
                    .withBlock(new DikeBlockDefinition(Pyrolusite, 2, 20, 26))
                    .withBlock(new DikeBlockDefinition(Tantalite, 1, 24, 30)))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Grossular)
                    .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)));

    public static final GTOreDefinition MOLYBDENUM_VEIN = create("molybdenum_vein", vein -> vein
            .clusterSize(UniformInt.of(25, 29)).density(0.25f).weight(5)
            .layer(WorldGenLayers.NETHERRACK)
            .heightRangeUniform(20, 50)
            .biomes(BiomeTags.IS_NETHER)
            .layeredVeinGenerator(generator -> generator
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                            .layer(l -> l.weight(3).mat(Wulfenite).size(2, 4))
                            .layer(l -> l.weight(2).mat(Molybdenite).size(1, 1))
                            .layer(l -> l.weight(1).mat(Molybdenum).size(1, 1))
                            .layer(l -> l.weight(1).mat(Powellite).size(1, 1))
                            .build()))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Molybdenum)
                    .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)));

    public static final GTOreDefinition MONAZITE_VEIN = create("monazite_vein", vein -> vein
            .clusterSize(UniformInt.of(25, 29)).density(0.25f).weight(30)
            .layer(WorldGenLayers.NETHERRACK)
            .heightRangeUniform(20, 40)
            .biomes(BiomeTags.IS_NETHER)
            .layeredVeinGenerator(generator -> generator
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                            .layer(l -> l.weight(3).mat(Bastnasite).size(2, 4))
                            .layer(l -> l.weight(1).mat(Monazite).size(1, 1))
                            .layer(l -> l.weight(1).mat(Neodymium).size(1, 1))
                            .build()))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Bastnasite)
                    .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)));

    public static final GTOreDefinition NETHER_QUARTZ_VEIN = create("nether_quartz_vein", vein -> vein
            .clusterSize(UniformInt.of(32, 40)).density(0.2f).weight(80)
            .layer(WorldGenLayers.NETHERRACK)
            .heightRangeUniform(40, 80)
            .biomes(BiomeTags.IS_NETHER)
            .layeredVeinGenerator(generator -> generator
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                            .layer(l -> l.weight(3).mat(NetherQuartz).size(2, 4))
                            .layer(l -> l.weight(1).mat(Quartzite).size(1, 1))
                            .build()))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(NetherQuartz)
                    .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)));

    public static final GTOreDefinition REDSTONE_VEIN = create("redstone_vein", vein -> vein
            .clusterSize(UniformInt.of(32, 40)).density(0.2f).weight(60)
            .layer(WorldGenLayers.NETHERRACK)
            .heightRangeUniform(5, 40)
            .biomes(BiomeTags.IS_NETHER)
            .layeredVeinGenerator(generator -> generator
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                            .layer(l -> l.weight(3).mat(Redstone).size(2, 4))
                            .layer(l -> l.weight(2).mat(Ruby).size(1, 1))
                            .layer(l -> l.weight(1).mat(Cinnabar).size(1, 1))
                            .build()))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Redstone)
                    .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)));

    public static final GTOreDefinition SALTPETER_VEIN = create("saltpeter_vein", vein -> vein
            .clusterSize(UniformInt.of(32, 40)).density(0.25f).weight(40)
            .layer(WorldGenLayers.NETHERRACK)
            .heightRangeUniform(5, 45)
            .biomes(BiomeTags.IS_NETHER)
            .layeredVeinGenerator(generator -> generator
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                            .layer(l -> l.weight(3).mat(Saltpeter).size(2, 4))
                            .layer(l -> l.weight(2).mat(Diatomite).size(1, 1))
                            .layer(l -> l.weight(2).mat(Electrotine).size(1, 1))
                            .layer(l -> l.weight(1).mat(Alunite).size(1, 1))
                            .build()))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Saltpeter)
                    .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)));

    public static final GTOreDefinition SULFUR_VEIN = create("sulfur_vein", vein -> vein
            .clusterSize(UniformInt.of(32, 40)).density(0.2f).weight(100)
            .layer(WorldGenLayers.NETHERRACK)
            .heightRangeUniform(10, 30)
            .biomes(BiomeTags.IS_NETHER)
            .layeredVeinGenerator(generator -> generator
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                            .layer(l -> l.weight(3).mat(Sulfur).size(2, 4))
                            .layer(l -> l.weight(2).mat(Pyrite).size(1, 1))
                            .layer(l -> l.weight(1).mat(Sphalerite).size(1, 1))
                            .build()))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Sulfur)
                    .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)));

    public static final GTOreDefinition TETRAHEDRITE_VEIN = create("tetrahedrite_vein", vein -> vein
            .clusterSize(UniformInt.of(40, 52)).density(1.0f).weight(70)
            .layer(WorldGenLayers.NETHERRACK)
            .heightRangeUniform(80, 120)
            .biomes(BiomeTags.IS_NETHER)
            .veinedVeinGenerator(generator -> generator
                    .oreBlock(new VeinBlockDefinition(Tetrahedrite, 4))
                    .oreBlock(new VeinBlockDefinition(Copper, 2))
                    .rareBlock(new VeinBlockDefinition(Stibnite, 1))
                    .rareBlockChance(0.15f)
                    .veininessThreshold(0.01f)
                    .maxRichnessThreshold(0.175f)
                    .minRichness(0.7f)
                    .maxRichness(1.0f)
                    .edgeRoundoffBegin(3)
                    .maxEdgeRoundoff(0.1f))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Tetrahedrite)
                    .placement(SurfaceIndicatorGenerator.IndicatorPlacement.BELOW)));

    public static final GTOreDefinition TOPAZ_VEIN = create("topaz_vein", vein -> vein
            .clusterSize(UniformInt.of(25, 29)).density(0.25f).weight(70)
            .layer(WorldGenLayers.NETHERRACK)
            .heightRangeUniform(80, 120)
            .biomes(BiomeTags.IS_NETHER)
            .layeredVeinGenerator(generator -> generator
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                            .layer(l -> l.weight(3).mat(BlueTopaz).size(2, 4))
                            .layer(l -> l.weight(2).mat(Topaz).size(1, 1))
                            .layer(l -> l.weight(2).mat(Chalcocite).size(1, 1))
                            .layer(l -> l.weight(1).mat(Bornite).size(1, 1))
                            .build()))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Topaz)
                    .placement(SurfaceIndicatorGenerator.IndicatorPlacement.BELOW)));

    //////////////////////////////////////
    // ***** Overworld Vein *****//
    //////////////////////////////////////

    //////////////////////////////////////
    // ***** Stone *****//
    //////////////////////////////////////
    public static RuleTest[] OVERWORLD_RULES = new RuleTest[] { new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES) };

    public static final GTOreDefinition APATITE_VEIN = create("apatite_vein", vein -> vein
            .clusterSize(UniformInt.of(32, 40)).density(0.25f).weight(40)
            .layer(WorldGenLayers.STONE)
            .heightRangeUniform(10, 80)
            .biomes(BiomeTags.IS_OVERWORLD)
            .layeredVeinGenerator(generator -> generator
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(3).mat(Apatite).size(2, 4))
                            .layer(l -> l.weight(2).mat(TricalciumPhosphate).size(1, 1))
                            .layer(l -> l.weight(1).mat(Pyrochlore).size(1, 1))
                            .build()))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Apatite)
                    .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)));

    public static final GTOreDefinition CASSITERITE_VEIN = create("cassiterite_vein", vein -> vein
            .clusterSize(UniformInt.of(40, 52)).density(1.0f).weight(80)
            .layer(WorldGenLayers.STONE)
            .heightRangeUniform(10, 80)
            .biomes(BiomeTags.IS_OVERWORLD)
            .veinedVeinGenerator(generator -> generator
                    .oreBlock(new VeinBlockDefinition(Tin, 4))
                    .rareBlock(new VeinBlockDefinition(Cassiterite, 2))
                    .rareBlockChance(0.33f)
                    .veininessThreshold(0.01f)
                    .maxRichnessThreshold(0.175f)
                    .minRichness(0.7f)
                    .maxRichness(1.0f)
                    .edgeRoundoffBegin(3)
                    .maxEdgeRoundoff(0.1f))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Cassiterite)));

    public static final GTOreDefinition COAL_VEIN = create("coal_vein", vein -> vein
            .clusterSize(UniformInt.of(38, 44)).density(0.25f).weight(80)
            .layer(WorldGenLayers.STONE)
            .heightRangeUniform(10, 140)
            .biomes(BiomeTags.IS_OVERWORLD)
            .layeredVeinGenerator(generator -> generator
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(3).mat(Coal).size(2, 4))
                            .build()))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Coal)));

    public static final GTOreDefinition COPPER_TIN_VEIN = create("copper_tin_vein", vein -> vein
            .clusterSize(UniformInt.of(40, 52)).density(1.0f).weight(50)
            .layer(WorldGenLayers.STONE)
            .heightRangeUniform(-10, 160)
            .biomes(BiomeTags.IS_OVERWORLD)
            .veinedVeinGenerator(generator -> generator
                    .oreBlock(new VeinBlockDefinition(Chalcopyrite, 5))
                    .oreBlock(new VeinBlockDefinition(Zeolite, 2))
                    .oreBlock(new VeinBlockDefinition(Cassiterite, 2))
                    .rareBlock(new VeinBlockDefinition(Realgar, 1))
                    .rareBlockChance(0.1f)
                    .veininessThreshold(0.01f)
                    .maxRichnessThreshold(0.175f)
                    .minRichness(0.7f)
                    .maxRichness(1.0f)
                    .edgeRoundoffBegin(3)
                    .maxEdgeRoundoff(0.1f))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Chalcopyrite)));

    public static final GTOreDefinition GALENA_VEIN = create("galena_vein", vein -> vein
            .clusterSize(UniformInt.of(32, 40)).density(0.25f).weight(40)
            .layer(WorldGenLayers.STONE)
            .heightRangeUniform(-15, 45)
            .biomes(BiomeTags.IS_OVERWORLD)
            .layeredVeinGenerator(generator -> generator
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(3).mat(Galena).size(2, 4))
                            .layer(l -> l.weight(2).mat(Silver).size(1, 1))
                            .layer(l -> l.weight(1).mat(Lead).size(1, 1))
                            .build()))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Galena)));

    public static final GTOreDefinition GARNET_TIN_VEIN = create("garnet_tin_vein", vein -> vein
            .clusterSize(UniformInt.of(32, 40)).density(0.4f).weight(80)
            .layer(WorldGenLayers.STONE)
            .heightRangeUniform(30, 60)
            .biomes(BiomeTags.IS_OVERWORLD)
            .layeredVeinGenerator(generator -> generator
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(3).mat(CassiteriteSand).size(2, 4))
                            .layer(l -> l.weight(2).mat(GarnetSand).size(1, 1))
                            .layer(l -> l.weight(2).mat(Asbestos).size(1, 1))
                            .layer(l -> l.weight(1).mat(Diatomite).size(1, 1))
                            .build()))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(GarnetSand)));

    public static final GTOreDefinition GARNET_VEIN = create("garnet_vein", vein -> vein
            .clusterSize(UniformInt.of(50, 64)).density(0.75f).weight(40)
            .layer(WorldGenLayers.STONE)
            .heightRangeUniform(-10, 50)
            .biomes(BiomeTags.IS_OVERWORLD)
            .dikeVeinGenerator(generator -> generator
                    .withBlock(new DikeBlockDefinition(GarnetRed, 3, -10, 50))
                    .withBlock(new DikeBlockDefinition(GarnetYellow, 2, -10, 50))
                    .withBlock(new DikeBlockDefinition(Amethyst, 2, -10, 22))
                    .withBlock(new DikeBlockDefinition(Opal, 1, 18, 50)))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(GarnetRed)
                    .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)));

    public static final GTOreDefinition IRON_VEIN = create("iron_vein", vein -> vein
            .clusterSize(UniformInt.of(40, 52)).density(1.0f).weight(120)
            .layer(WorldGenLayers.STONE)
            .heightRangeUniform(-10, 60)
            .biomes(BiomeTags.IS_OVERWORLD)
            .veinedVeinGenerator(generator -> generator
                    .oreBlock(new VeinBlockDefinition(Goethite, 5))
                    .oreBlock(new VeinBlockDefinition(Limonite, 2))
                    .oreBlock(new VeinBlockDefinition(Hematite, 2))
                    .oreBlock(new VeinBlockDefinition(Malachite, 1))
                    .veininessThreshold(0.01f)
                    .maxRichnessThreshold(0.175f)
                    .minRichness(0.7f)
                    .maxRichness(1.0f)
                    .edgeRoundoffBegin(3)
                    .maxEdgeRoundoff(0.1f))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Goethite)));

    public static final GTOreDefinition LUBRICANT_VEIN = create("lubricant_vein", vein -> vein
            .clusterSize(UniformInt.of(25, 29)).density(0.25f).weight(40)
            .layer(WorldGenLayers.STONE)
            .heightRangeUniform(0, 50)
            .biomes(BiomeTags.IS_OVERWORLD)
            .layeredVeinGenerator(generator -> generator
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(3).mat(Soapstone).size(2, 4))
                            .layer(l -> l.weight(2).mat(Talc).size(1, 1))
                            .layer(l -> l.weight(2).mat(GlauconiteSand).size(1, 1))
                            .layer(l -> l.weight(1).mat(Pentlandite).size(1, 1))
                            .build()))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Talc)));

    public static final GTOreDefinition MAGNETITE_VEIN_OW = create("magnetite_vein_ow", vein -> vein
            .clusterSize(UniformInt.of(38, 44)).density(0.15f).weight(80)
            .layer(WorldGenLayers.STONE)
            .heightRangeUniform(10, 60)
            .biomes(BiomeTags.IS_OVERWORLD)
            .layeredVeinGenerator(generator -> generator
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(3).mat(Magnetite).size(2, 4))
                            .layer(l -> l.weight(2).mat(VanadiumMagnetite).size(1, 1))
                            .layer(l -> l.weight(1).mat(Gold).size(1, 1))
                            .build()))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Magnetite)));

    public static final GTOreDefinition MINERAL_SAND_VEIN = create("mineral_sand_vein", vein -> vein
            .clusterSize(UniformInt.of(32, 40)).density(0.2f).weight(80)
            .layer(WorldGenLayers.STONE)
            .heightRangeUniform(15, 60)
            .biomes(BiomeTags.IS_OVERWORLD)
            .layeredVeinGenerator(generator -> generator
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(3).mat(BasalticMineralSand).size(2, 4))
                            .layer(l -> l.weight(2).mat(GraniticMineralSand).size(1, 1))
                            .layer(l -> l.weight(2).mat(FullersEarth).size(1, 1))
                            .layer(l -> l.weight(1).mat(Gypsum).size(1, 1))
                            .build()))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(BasalticMineralSand)));

    public static final GTOreDefinition NICKEL_VEIN = create("nickel_vein", vein -> vein
            .clusterSize(UniformInt.of(32, 40)).density(0.25f).weight(40)
            .layer(WorldGenLayers.STONE)
            .heightRangeUniform(-10, 60)
            .biomes(BiomeTags.IS_OVERWORLD)
            .layeredVeinGenerator(generator -> generator
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(3).mat(Garnierite).size(2, 4))
                            .layer(l -> l.weight(2).mat(Nickel).size(1, 1))
                            .layer(l -> l.weight(2).mat(Cobaltite).size(1, 1))
                            .layer(l -> l.weight(1).mat(Pentlandite).size(1, 1))
                            .build()))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Nickel)));

    public static final GTOreDefinition SALTS_VEIN = create("salts_vein", vein -> vein
            .clusterSize(UniformInt.of(32, 40)).density(0.2f).weight(50)
            .layer(WorldGenLayers.STONE)
            .heightRangeUniform(30, 70)
            .biomes(BiomeTags.IS_OVERWORLD)
            .layeredVeinGenerator(generator -> generator
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(3).mat(RockSalt).size(2, 4))
                            .layer(l -> l.weight(2).mat(Salt).size(1, 1))
                            .layer(l -> l.weight(1).mat(Lepidolite).size(1, 1))
                            .layer(l -> l.weight(1).mat(Spodumene).size(1, 1))
                            .build()))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Salt)));

    public static final GTOreDefinition OILSANDS_VEIN = create("oilsands_vein", vein -> vein
            .clusterSize(UniformInt.of(25, 29)).density(0.3f).weight(40)
            .layer(WorldGenLayers.STONE)
            .heightRangeUniform(30, 80)
            .biomes(BiomeTags.IS_OVERWORLD)
            .layeredVeinGenerator(generator -> generator
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(3).mat(Oilsands).size(2, 4))
                            .layer(l -> l.weight(2).mat(Oilsands).size(1, 1))
                            .layer(l -> l.weight(1).mat(Oilsands).size(1, 1))
                            .layer(l -> l.weight(1).mat(Oilsands).size(1, 1))
                            .build()))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Oilsands)));

    //////////////////////////////////////
    // ***** Deepslate *****//
    //////////////////////////////////////
    public static RuleTest[] DEEPSLATE_RULES = new RuleTest[] {
            new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES) };

    public static final GTOreDefinition COPPER_VEIN = create("copper_vein", vein -> vein
            .clusterSize(UniformInt.of(40, 52)).density(1.0f).weight(80)
            .layer(WorldGenLayers.DEEPSLATE)
            .heightRangeUniform(-40, 10)
            .biomes(BiomeTags.IS_OVERWORLD)
            .veinedVeinGenerator(generator -> generator
                    .oreBlock(new VeinBlockDefinition(Chalcopyrite, 5))
                    .oreBlock(new VeinBlockDefinition(Iron, 2))
                    .oreBlock(new VeinBlockDefinition(Pyrite, 2))
                    .oreBlock(new VeinBlockDefinition(Copper, 2))
                    .veininessThreshold(0.01f)
                    .maxRichnessThreshold(0.175f)
                    .minRichness(0.7f)
                    .maxRichness(1.0f)
                    .edgeRoundoffBegin(3)
                    .maxEdgeRoundoff(0.1f))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Copper)));

    public static final GTOreDefinition DIAMOND_VEIN = create("diamond_vein", vein -> vein
            .clusterSize(UniformInt.of(32, 40)).density(0.25f).weight(40)
            .layer(WorldGenLayers.DEEPSLATE)
            .heightRangeUniform(-55, -30)
            .biomes(BiomeTags.IS_OVERWORLD)
            .classicVeinGenerator(generator -> generator
                    .primary(b -> b.mat(Graphite).size(4))
                    .secondary(b -> b.mat(Graphite).size(3))
                    .between(b -> b.mat(Diamond).size(3))
                    .sporadic(b -> b.mat(Coal)))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Diamond)
                    .density(0.1f)
                    .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)
                    .radius(2)));

    public static final GTOreDefinition LAPIS_VEIN = create("lapis_vein", vein -> vein
            .clusterSize(UniformInt.of(40, 52)).density(0.75f).weight(40)
            .layer(WorldGenLayers.DEEPSLATE)
            .heightRangeUniform(-60, 10)
            .biomes(BiomeTags.IS_OVERWORLD)
            .dikeVeinGenerator(generator -> generator
                    .withBlock(new DikeBlockDefinition(Lazurite, 3, -60, 10))
                    .withBlock(new DikeBlockDefinition(Sodalite, 2, -50, 0))
                    .withBlock(new DikeBlockDefinition(Lapis, 2, -50, 0))
                    .withBlock(new DikeBlockDefinition(Calcite, 1, -40, 10)))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Lapis)
                    .density(0.15f)
                    .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)
                    .radius(3)));

    public static final GTOreDefinition MANGANESE_VEIN_OW = create("manganese_vein_ow", vein -> vein
            .clusterSize(UniformInt.of(50, 64)).density(0.75f).weight(20)
            .layer(WorldGenLayers.DEEPSLATE)
            .heightRangeUniform(-30, 0)
            .biomes(BiomeTags.IS_OVERWORLD)
            .dikeVeinGenerator(generator -> generator
                    .withBlock(new DikeBlockDefinition(Grossular, 3, -50, -5))
                    .withBlock(new DikeBlockDefinition(Spessartine, 2, -40, -15))
                    .withBlock(new DikeBlockDefinition(Pyrolusite, 2, -40, -15))
                    .withBlock(new DikeBlockDefinition(Tantalite, 1, -30, -5)))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Grossular)
                    .density(0.15f)
                    .radius(3)));

    public static final GTOreDefinition MICA_VEIN = create("mica_vein", vein -> vein
            .clusterSize(UniformInt.of(32, 40)).density(0.25f).weight(20)
            .layer(WorldGenLayers.DEEPSLATE)
            .heightRangeUniform(-40, -10)
            .biomes(BiomeTags.IS_OVERWORLD)
            .layeredVeinGenerator(generator -> generator
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(3).mat(Kyanite).size(2, 4))
                            .layer(l -> l.weight(2).mat(Mica).size(1, 1))
                            .layer(l -> l.weight(2).mat(Bauxite).size(1, 1))
                            .layer(l -> l.weight(1).mat(Pollucite).size(1, 1))
                            .build()))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Mica)
                    .radius(3)));

    public static final GTOreDefinition OLIVINE_VEIN = create("olivine_vein", vein -> vein
            .clusterSize(UniformInt.of(32, 40)).density(0.25f).weight(20)
            .layer(WorldGenLayers.DEEPSLATE)
            .heightRangeUniform(-20, 10)
            .biomes(BiomeTags.IS_OVERWORLD)
            .layeredVeinGenerator(generator -> generator
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(3).mat(Bentonite).size(2, 4))
                            .layer(l -> l.weight(2).mat(Magnesite).size(1, 1))
                            .layer(l -> l.weight(2).mat(Olivine).size(1, 1))
                            .layer(l -> l.weight(1).mat(GlauconiteSand).size(1, 1))
                            .build()))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Olivine)
                    .density(0.15f)
                    .radius(3)));

    public static final GTOreDefinition REDSTONE_VEIN_OW = create("redstone_vein_ow", vein -> vein
            .clusterSize(UniformInt.of(32, 40)).density(0.2f).weight(60)
            .layer(WorldGenLayers.DEEPSLATE)
            .heightRangeUniform(-65, -10)
            .biomes(BiomeTags.IS_OVERWORLD)
            .layeredVeinGenerator(generator -> generator
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(3).mat(Redstone).size(2, 4))
                            .layer(l -> l.weight(2).mat(Ruby).size(1, 1))
                            .layer(l -> l.weight(1).mat(Cinnabar).size(1, 1))
                            .build()))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Redstone)));

    public static final GTOreDefinition SAPPHIRE_VEIN = create("sapphire_vein", vein -> vein
            .clusterSize(UniformInt.of(25, 29)).density(0.25f).weight(60)
            .layer(WorldGenLayers.DEEPSLATE)
            .heightRangeUniform(-40, 0)
            .biomes(BiomeTags.IS_OVERWORLD)
            .layeredVeinGenerator(generator -> generator
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(3).mat(Almandine).size(2, 4))
                            .layer(l -> l.weight(2).mat(Pyrope).size(1, 1))
                            .layer(l -> l.weight(1).mat(Sapphire).size(1, 1))
                            .layer(l -> l.weight(1).mat(GreenSapphire).size(1, 1))
                            .build()))
            .surfaceIndicatorGenerator(indicator -> indicator
                    .surfaceRock(Sapphire)
                    .density(0.15f)
                    .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)
                    .radius(3)));

    private static GTOreDefinition create(String name, Consumer<GTOreDefinition> config) {
        return create(GTCEu.id(name), config);
    }

    public static GTOreDefinition create(ResourceLocation name, Consumer<GTOreDefinition> config) {
        GTOreDefinition def = blankOreDefinition();
        config.accept(def);

        def.register(name);
        toReRegister.put(name, def);

        return def;
    }

    private static Supplier<? extends Block> ore(TagPrefix oreTag, Material material) {
        var block = GTMaterialBlocks.MATERIAL_BLOCKS.get(oreTag, material);
        if (block == null) {
            ResourceLocation oreKey;
            if (oreTag == ore) {
                oreKey = new ResourceLocation("%s_ore".formatted(material.getName()));
            } else if (oreTag == oreNetherrack) {
                oreKey = new ResourceLocation("nether_%s_ore".formatted(material.getName()));
            } else {
                oreKey = new ResourceLocation("%s_%s_ore".formatted(oreTag.name, material.getName()));
            }
            return BuiltInRegistries.BLOCK.containsKey(oreKey) ? () -> BuiltInRegistries.BLOCK.get(oreKey) :
                    () -> Blocks.AIR;
        }
        return block;
    }

    public static void init() {
        toReRegister.forEach(GTRegistries.ORE_VEINS::registerOrOverride);
    }

    public static void updateLargestVeinSize() {
        // map to average of min & max values.
        GTOres.largestVeinSize = GTRegistries.ORE_VEINS.values().stream()
                .map(GTOreDefinition::clusterSize)
                .mapToInt(intProvider -> (intProvider.getMinValue() + intProvider.getMaxValue()) / 2)
                .max()
                .orElse(0);

        GTOres.largestIndicatorOffset = GTRegistries.ORE_VEINS.values().stream()
                .flatMapToInt(definition -> definition.indicatorGenerators().stream()
                        .mapToInt(indicatorGenerator -> indicatorGenerator.getSearchRadiusModifier(
                                (int) Math.ceil(definition.clusterSize().getMinValue() / 2.0))))
                .max()
                .orElse(0);
    }

    public static GTOreDefinition blankOreDefinition() {
        return new GTOreDefinition(
                ConstantInt.ZERO, 0, 0, IWorldGenLayer.NOWHERE, Set.of(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(0)),
                0, HolderSet::direct, BiomeWeightModifier.EMPTY, NoopVeinGenerator.INSTANCE,
                new ArrayList<>());
    }
}
