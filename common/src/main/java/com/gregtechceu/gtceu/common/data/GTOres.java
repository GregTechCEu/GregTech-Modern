package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.GTLayerPattern;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

/**
 * @author KilaBash
 * @date 2023/3/20
 * @implNote GTOres
 */
public class GTOres {

    //////////////////////////////////////
    //********     End Vein    *********//
    //////////////////////////////////////
    public static RuleTest[] END_RULES = new RuleTest[] { new BlockMatchTest(Blocks.END_STONE) };
    public static final GTOreFeatureEntry BAUXITE_VEIN =
            create("bauxite_vein", 15, 0.04f, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(64)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(END_RULES)
                            .layer(l -> l.weight(1).state(Blocks.END_STONE::defaultBlockState))
                            .layer(l -> l.weight(2).block(ore(oreEndstone, Bauxite)).size(1, 4))
                            .layer(l -> l.weight(1).block(ore(oreEndstone, Ilmenite)).size(1, 2))
                            .layer(l -> l.weight(1).block(ore(oreEndstone, Aluminium)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_END)
                    .parent();

    public static final GTOreFeatureEntry MAGNETITE_VEIN =
            create("magnetite_vein", 25, 0.03f, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(64)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(END_RULES)
                            .layer(l -> l.weight(2).state(Blocks.END_STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(oreEndstone, Magnetite)).size(1, 4))
                            .layer(l -> l.weight(2).block(ore(oreEndstone, VanadiumMagnetite)).size(1, 2))
                            .layer(l -> l.weight(2).block(ore(oreEndstone, Chromite)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(oreEndstone, Gold)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_END)
                    .parent();

    public static final GTOreFeatureEntry NAQUADAH_VEIN =
            create("naquadah_vein", 25, 0.03f, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(64)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(END_RULES)
                            .layer(l -> l.weight(2).state(Blocks.END_STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(oreEndstone, Naquadah)).size(1, 4))
                            .layer(l -> l.weight(1).block(ore(oreEndstone, Plutonium239)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_END)
                    .parent();

    public static final GTOreFeatureEntry PITCHBLENDE_VEIN =
            create("pitchblende_vein", 15, 0.03f, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(64)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(END_RULES)
                            .layer(l -> l.weight(2).state(Blocks.END_STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(oreEndstone, Pitchblende)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(oreEndstone, Uraninite)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_END)
                    .parent();

    public static final GTOreFeatureEntry SCHEELITE_VEIN =
            create("scheelite_vein", 20, 0.02f, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(64)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(END_RULES)
                            .layer(l -> l.weight(2).state(Blocks.END_STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(oreEndstone, Scheelite)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(oreEndstone, Tungstate)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(oreEndstone, Lithium)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_END)
                    .parent();

    public static final GTOreFeatureEntry SHELDONITE_VEIN =
            create("sheldonite_vein", 15, 0.01f, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(64)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(END_RULES)
                            .layer(l -> l.weight(2).state(Blocks.END_STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(oreEndstone, Bornite)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(oreEndstone, Cooperite)).size(1, 1))
                            .layer(l -> l.weight(2).block(ore(oreEndstone, Platinum)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(oreEndstone, Palladium)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_END)
                    .parent();

    //////////////////////////////////////
    //******     Nether Vein     *******//
    //////////////////////////////////////
    public static RuleTest[] NETHER_RULES = new RuleTest[] {OreFeatures.NETHER_ORE_REPLACEABLES };

    public static final GTOreFeatureEntry BANDED_IRON_VEIN =
            create("banded_iron_vein", 20, 0.03f, HeightRangePlacement.uniform(VerticalAnchor.absolute(40), VerticalAnchor.absolute(90)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                            .layer(l -> l.weight(2).state(Blocks.END_STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(oreNetherrack, BrownLimonite)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(oreNetherrack, YellowLimonite)).size(1, 1))
                            .layer(l -> l.weight(2).block(ore(oreNetherrack, BandedIron)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(oreNetherrack, Gold)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_NETHER)
                    .parent();

    public static final GTOreFeatureEntry BERYLLIUM_VEIN =
            create("beryllium_vein", 15, 0.03f, HeightRangePlacement.uniform(VerticalAnchor.absolute(40), VerticalAnchor.absolute(90)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                            .layer(l -> l.weight(2).state(Blocks.END_STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(oreNetherrack, Beryllium)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(oreNetherrack, Emerald)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(oreNetherrack, Thorium)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_NETHER)
                    .parent();

    public static final GTOreFeatureEntry CERTUS_QUARTZ_VEIN =
            create("certus_quartz", 15, 0.04f, HeightRangePlacement.uniform(VerticalAnchor.absolute(40), VerticalAnchor.absolute(90)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                            .layer(l -> l.weight(2).state(Blocks.END_STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(oreNetherrack, Quartzite)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(oreNetherrack, CertusQuartz)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(oreNetherrack, Barite)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_NETHER)
                    .parent();

    public static final GTOreFeatureEntry MANGANESE_VEIN =
            create("manganese_vein", 15, 0.02f, HeightRangePlacement.uniform(VerticalAnchor.absolute(40), VerticalAnchor.absolute(90)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                            .layer(l -> l.weight(2).state(Blocks.END_STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(oreNetherrack, Grossular)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(oreNetherrack, Pyrolusite)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(oreNetherrack, Tantalite)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_NETHER)
                    .parent();

    public static final GTOreFeatureEntry MOLYBDENUM_VEIN =
            create("molybdenum_vein", 15, 0.005f, HeightRangePlacement.uniform(VerticalAnchor.absolute(40), VerticalAnchor.absolute(90)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                            .layer(l -> l.weight(2).state(Blocks.END_STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(oreNetherrack, Wulfenite)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(oreNetherrack, Molybdenite)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(oreNetherrack, Molybdenum)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(oreNetherrack, Powellite)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_NETHER)
                    .parent();

    public static final GTOreFeatureEntry MONAZITE_VEIN =
            create("monazite_vein", 15, 0.03f, HeightRangePlacement.uniform(VerticalAnchor.absolute(40), VerticalAnchor.absolute(90)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                            .layer(l -> l.weight(2).state(Blocks.END_STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(oreNetherrack, Bastnasite)).size(2, 4))
                            .layer(l -> l.weight(1).block(ore(oreNetherrack, Molybdenum)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(oreNetherrack, Neodymium)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_NETHER)
                    .parent();

    public static final GTOreFeatureEntry NETHER_UARTZ_VEIN =
            create("nether_quartz_vein", 20, 0.08f, HeightRangePlacement.uniform(VerticalAnchor.absolute(40), VerticalAnchor.absolute(90)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                            .layer(l -> l.weight(2).state(Blocks.END_STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(oreNetherrack, NetherQuartz)).size(2, 4))
                            .layer(l -> l.weight(1).block(ore(oreNetherrack, Quartzite)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_NETHER)
                    .parent();

    public static final GTOreFeatureEntry REDSTONE_VEIN =
            create("redstone_vein", 20, 006f, HeightRangePlacement.uniform(VerticalAnchor.absolute(40), VerticalAnchor.absolute(90)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                            .layer(l -> l.weight(2).state(Blocks.END_STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(oreNetherrack, Redstone)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(oreNetherrack, Ruby)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(oreNetherrack, Cinnabar)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_NETHER)
                    .parent();

    public static final GTOreFeatureEntry SALTPETER_VEIN =
            create("saltpeter_vein", 15, 0.4f, HeightRangePlacement.uniform(VerticalAnchor.absolute(40), VerticalAnchor.absolute(90)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                            .layer(l -> l.weight(2).state(Blocks.END_STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(oreNetherrack, Saltpeter)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(oreNetherrack, Diatomite)).size(1, 1))
                            .layer(l -> l.weight(2).block(ore(oreNetherrack, Electrotine)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(oreNetherrack, Alunite)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_NETHER)
                    .parent();

    public static final GTOreFeatureEntry SULFUR_VEIN =
            create("sulfur_vein", 20, 0.1f, HeightRangePlacement.uniform(VerticalAnchor.absolute(40), VerticalAnchor.absolute(90)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                            .layer(l -> l.weight(2).state(Blocks.END_STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(oreNetherrack, Sulfur)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(oreNetherrack, Pyrite)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(oreNetherrack, Sphalerite)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_NETHER)
                    .parent();

    public static final GTOreFeatureEntry TETRAHEDRITE_VEIN =
            create("tetrahedrite_vein", 20, 0.07f, HeightRangePlacement.uniform(VerticalAnchor.absolute(40), VerticalAnchor.absolute(90)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                            .layer(l -> l.weight(2).state(Blocks.END_STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(oreNetherrack, Tetrahedrite)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(oreNetherrack, Copper)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(oreNetherrack, Stibnite)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_NETHER)
                    .parent();

    public static final GTOreFeatureEntry TOPAZ_VEIN =
            create("topaz_vein", 15, 0.07f, HeightRangePlacement.uniform(VerticalAnchor.absolute(40), VerticalAnchor.absolute(90)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(NETHER_RULES)
                            .layer(l -> l.weight(2).state(Blocks.END_STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(oreNetherrack, BlueTopaz)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(oreNetherrack, Topaz)).size(1, 1))
                            .layer(l -> l.weight(2).block(ore(oreNetherrack, Chalcocite)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(oreNetherrack, Bornite)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_NETHER)
                    .parent();

    //////////////////////////////////////
    //*****     Overworld Vein     *****//
    //////////////////////////////////////
    public static RuleTest[] OVERWORLD_RULES = new RuleTest[] {OreFeatures.STONE_ORE_REPLACEABLES };

    public static final GTOreFeatureEntry APATITE_VEIN =
            create("apatite_vein", 15, 0.04f, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(70)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(ore, Apatite)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(ore, TricalciumPhosphate)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(ore, Pyrochlore)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_OVERWORLD)
                    .parent();

    public static final GTOreFeatureEntry CASSITERITE_VEIN =
            create("cassiterite_vein", 20, 0.08f, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(70)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(4).block(ore(ore, Tin)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(ore, Cassiterite)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_OVERWORLD)
                    .parent();

    public static final GTOreFeatureEntry COAL_VEIN =
            create("coal_vein", 25, 0.08f, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(70)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(1).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(ore, Coal)).size(2, 4))
                            .build())
                    .biomeTag(BiomeTags.IS_OVERWORLD)
                    .parent();

    public static final GTOreFeatureEntry COPPER_TIN_VEIN =
            create("copper_tin_vein", 20, 0.05f, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(70)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(ore, Chalcopyrite)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(ore, Zeolite)).size(1, 1))
                            .layer(l -> l.weight(2).block(ore(ore, Cassiterite)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(ore, Realgar)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_OVERWORLD)
                    .parent();

    public static final GTOreFeatureEntry COPPER_VEIN =
            create("copper_vein", 20, 0.08f, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(70)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(ore, Chalcopyrite)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(ore, Iron)).size(1, 1))
                            .layer(l -> l.weight(2).block(ore(ore, Pyrite)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(ore, Copper)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_OVERWORLD)
                    .parent();

    public static final GTOreFeatureEntry DIAMOND_VEIN =
            create("diamond_vein", 15, 0.04f, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(70)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(ore, Graphite)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(ore, Diamond)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(ore, Coal)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_OVERWORLD)
                    .parent();

    public static final GTOreFeatureEntry GALENA_VEIN =
            create("galena_vein", 15, 0.04f, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(70)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(ore, Galena)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(ore, Silver)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(ore, Lead)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_OVERWORLD)
                    .parent();

    public static final GTOreFeatureEntry GARNET_TIN_VEIN =
            create("garnet_tin_vein", 20, 0.08f, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(70)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(2).state(Blocks.SAND::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(oreSand, CassiteriteSand)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(oreSand, GarnetSand)).size(1, 1))
                            .layer(l -> l.weight(2).block(ore(oreSand, Asbestos)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(oreSand, Diatomite)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_OVERWORLD)
                    .parent();

    public static final GTOreFeatureEntry GARNET_VEIN =
            create("garnet_vein", 15, 0.04f, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(70)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(2).state(Blocks.REDSTONE_BLOCK::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(oreRedSand, GarnetRed)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(oreRedSand, GarnetYellow)).size(1, 1))
                            .layer(l -> l.weight(2).block(ore(oreRedSand, Amethyst)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(oreRedSand, Opal)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_OVERWORLD)
                    .parent();

    public static final GTOreFeatureEntry IRON_VEIN =
            create("iron_vein", 20, 0.12f, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(70)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(ore, BrownLimonite)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(ore, YellowLimonite)).size(1, 1))
                            .layer(l -> l.weight(2).block(ore(ore, BandedIron)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(ore, Malachite)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_OVERWORLD)
                    .parent();

    public static final GTOreFeatureEntry LAPIS_VEIN =
            create("lapis_vein", 15, 0.04f, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(70)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(ore, Lazurite)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(ore, Sodalite)).size(1, 1))
                            .layer(l -> l.weight(2).block(ore(ore, Lapis)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(ore, Calcite)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_OVERWORLD)
                    .parent();

    public static final GTOreFeatureEntry LUBRICANT_VEIN =
            create("lubricant_vein", 15, 0.04f, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(70)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(ore, Soapstone)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(ore, Talc)).size(1, 1))
                            .layer(l -> l.weight(2).block(ore(oreSand, GlauconiteSand)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(ore, Pentlandite)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_OVERWORLD)
                    .parent();

    public static final GTOreFeatureEntry MAGNETITE_OVER_VEIN =
            create("magnetite_over_vein", 25, 0.08f, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(70)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(ore, Magnetite)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(ore, VanadiumMagnetite)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(ore, Gold)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_OVERWORLD)
                    .parent();

    public static final GTOreFeatureEntry MANGANESE_OVER_VEIN =
            create("manganese_over_vein", 15, 0.02f, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(70)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(ore, Grossular)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(ore, Spessartine)).size(1, 1))
                            .layer(l -> l.weight(2).block(ore(ore, Pyrolusite)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(ore, Tantalite)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_OVERWORLD)
                    .parent();

    public static final GTOreFeatureEntry MICA_VEIN =
            create("mica_vein", 15, 0.02f, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(70)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(ore, Kyanite)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(ore, Mica)).size(1, 1))
                            .layer(l -> l.weight(2).block(ore(ore, Bauxite)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(ore, Pollucite)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_OVERWORLD)
                    .parent();

    public static final GTOreFeatureEntry MINERAL_SAND_VEIN =
            create("mineral_sand_vein", 20, 0.08f, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(70)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(2).state(Blocks.SAND::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(oreSand, BasalticMineralSand)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(oreSand, GraniticMineralSand)).size(1, 1))
                            .layer(l -> l.weight(2).block(ore(oreSand, FullersEarth)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(oreSand, Gypsum)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_OVERWORLD)
                    .parent();

    public static final GTOreFeatureEntry NICKEL_VEIN =
            create("nickel_vein", 15, 0.04f, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(70)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(ore, Garnierite)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(ore, Nickel)).size(1, 1))
                            .layer(l -> l.weight(2).block(ore(ore, Cobaltite)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(ore, Pentlandite)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_OVERWORLD)
                    .parent();

    public static final GTOreFeatureEntry OILSANDS_VEIN =
            create("oilsands_vein", 15, 0.02f, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(70)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(1).state(Blocks.SAND::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(oreSand, Oilsands)).size(2, 4))
                            .build())
                    .biomeTag(BiomeTags.IS_OVERWORLD)
                    .parent();

    public static final GTOreFeatureEntry OLIVINE_VEIN =
            create("olivine_vein", 15, 0.02f, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(70)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(ore, Bentonite)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(ore, Magnetite)).size(1, 1))
                            .layer(l -> l.weight(2).block(ore(ore, Olivine)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(oreSand, GlauconiteSand)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_OVERWORLD)
                    .parent();

    public static final GTOreFeatureEntry REDSONTE_OVER_VEIN =
            create("redstone_over_vein", 20, 0.06f, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(70)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(ore, Redstone)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(ore, Ruby)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(oreSand, Cinnabar)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_OVERWORLD)
                    .parent();

    public static final GTOreFeatureEntry SALTS_VEIN =
            create("salts_vein", 20, 0.05f, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(70)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(ore, RockSalt)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(ore, Salt)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(ore, Lepidolite)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(oreSand, Spodumene)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_OVERWORLD)
                    .parent();

    public static final GTOreFeatureEntry SAPPHIRE_VEIN =
            create("sapphire_vein", 15, 0.06f, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(70)))
                    .layeredDatagenExt()
                    .withLayerPattern(() -> GTLayerPattern.builder(OVERWORLD_RULES)
                            .layer(l -> l.weight(2).state(Blocks.STONE::defaultBlockState))
                            .layer(l -> l.weight(3).block(ore(ore, Almandine)).size(2, 4))
                            .layer(l -> l.weight(2).block(ore(ore, Pyrope)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(ore, Sapphire)).size(1, 1))
                            .layer(l -> l.weight(1).block(ore(oreSand, GreenSapphire)).size(1, 1))
                            .build())
                    .biomeTag(BiomeTags.IS_OVERWORLD)
                    .parent();

    private static GTOreFeatureEntry create(String name, int clusterSize, float frequency, HeightRangePlacement range) {
        return new GTOreFeatureEntry(GTCEu.id(name), clusterSize, frequency, CountPlacement.of(1), range);
    }

    private static Supplier<? extends Block> ore(TagPrefix oreTag, Material material) {
        var block = GTBlocks.MATERIAL_BLOCKS.get(oreTag, material);
        return block == null ? () -> Blocks.AIR : block;
    }

    public static void init() {
    }

}
