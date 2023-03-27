package com.gregtechceu.gtceu.api.tag;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.IMaterialProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.util.TriConsumer;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.tag.TagPrefix.Conditions.*;

@Accessors(chain = true, fluent = true)
public class TagPrefix {
    private final static Map<String, TagPrefix> PREFIXES = new HashMap<>();
    public final static Map<TagPrefix, Supplier<BlockState>> ORES = new HashMap<>();

    // Regular Ore Prefix. Ore -> Material is a Oneway Operation!
    public static final TagPrefix ore = new TagPrefix("ore","ores")
            .registerOre(Blocks.STONE::defaultBlockState)
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty);
    public static final TagPrefix oreGranite = new TagPrefix("oreGranite", "ores", "ores/granite")
            .registerOre(Blocks.GRANITE::defaultBlockState)
            .langValue("Granite %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty);
    public static final TagPrefix oreDiorite = new TagPrefix("oreDiorite", "ores", "ores/diorite")
            .registerOre(Blocks.DIORITE::defaultBlockState)
            .langValue("Diorite %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty);
    public static final TagPrefix oreAndesite = new TagPrefix("oreAndesite", "ores", "ores/andesite")
            .registerOre(Blocks.ANDESITE::defaultBlockState)
            .langValue("Andesite %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty);
    public static final TagPrefix oreBlackgranite = new TagPrefix("oreBlackgranite", "ores", "ores/black_granite")
//            .registerOre(Blocks.::defaultBlockState) TODO BLACK GRANITE?
            .langValue("Granite %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty);
    public static final TagPrefix oreRedgranite = new TagPrefix("oreRedgranite", "ores", "ores/red_granite")
//            .registerOre(Blocks.::defaultBlockState) TODO BLACK GRANITE?
            .langValue("Granite %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty);
    public static final TagPrefix oreMarble = new TagPrefix("oreMarble", "ores", "ores/marble")
//            .registerOre(Blocks.::defaultBlockState) TODO BLACK GRANITE?
            .langValue("Marble %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty);
    public static final TagPrefix oreBasalt = new TagPrefix("oreBasalt", "ores", "ores/basalt")
            .registerOre(Blocks.BASALT::defaultBlockState)
            .langValue("Basalt %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty);
    public static final TagPrefix oreDeepslate = new TagPrefix("oreDeepslate", "ores", "ores/deepslate")
            .langValue("Deepslate %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty);
    // In case of an Sand-Ores Mod. Ore -> Material is a Oneway Operation!
    public static final TagPrefix oreSand = new TagPrefix("oreSand", "ores", "ores/sand")
            .registerOre(Blocks.SAND::defaultBlockState)
            .langValue("Sand %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty);
    public static final TagPrefix oreRedSand = new TagPrefix("oreRedSand", "ores", "ores/red_sand")
            .registerOre(Blocks.RED_SAND::defaultBlockState)
            .langValue("Red Sand %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty);

    // Prefix of the Nether-Ores Mod. Causes Ores to double. Ore -> Material is a Oneway Operation!
    public static final TagPrefix oreNetherrack = new TagPrefix("oreNetherrack", "ores", "ores/netherrack")
            .registerOre(Blocks.NETHERRACK::defaultBlockState)
            .langValue("Nether %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty);
    // In case of an End-Ores Mod. Ore -> Material is a Oneway Operation!
    public static final TagPrefix oreEndstone = new TagPrefix("oreEndstone", "ores", "ores/endstone")
            .registerOre(Blocks.END_STONE::defaultBlockState)
            .langValue("End %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty);

    public static final TagPrefix crushedRefined = new TagPrefix("crushedRefined", "crushed_refined_ores")
            .langValue("Refined %s Ore")
            .materialIconType(MaterialIconType.crushedRefined)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasOreProperty);
    public static final TagPrefix crushedPurified = new TagPrefix("crushedPurified", "crushed_purified_ores")
            .langValue("Purified %s Ore")
            .materialIconType(MaterialIconType.crushedPurified)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasOreProperty);
    public static final TagPrefix crushed = new TagPrefix("crushed", "crushed_ores")
            .langValue("Crushed %s Ore")
            .materialIconType(MaterialIconType.crushed)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasOreProperty)
            .tooltip((mat, tooltips) -> tooltips.add(Component.translatable("metaitem.crushed.tooltip.purify")));

    // A hot Ingot, which has to be cooled down by a Vacuum Freezer.
    public static final TagPrefix ingotHot = new TagPrefix("ingotHot", "ingots/hot")
            .langValue("Hot %s Ingot")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.ingotHot)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasBlastProperty.and(mat -> mat.getProperty(PropertyKey.BLAST).getBlastTemperature() > 1750));

    // A regular Ingot.
    public static final TagPrefix ingot = new TagPrefix("ingot", "ingots")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.ingot)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasIngotProperty);

    // A regular Gem worth one Dust.
    public static final TagPrefix gem = new TagPrefix("gem", "gems")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.gem)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasGemProperty);

    // A regular Gem worth one small Dust.
    public static final TagPrefix gemChipped = new TagPrefix("gemChipped", "gems/chipped")
            .langValue("Chipped %s")
            .materialAmount(GTValues.M / 4)
            .materialIconType(MaterialIconType.gemChipped)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasGemProperty.and(unused -> ConfigHolder.recipes.generateLowQualityGems));

    // A regular Gem worth two small Dusts.
    public static final TagPrefix gemFlawed = new TagPrefix("gemFlawed", "gems/flawed")
            .langValue("Flawed %s")
            .materialAmount(GTValues.M / 2)
            .materialIconType(MaterialIconType.gemFlawed)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasGemProperty.and(unused -> ConfigHolder.recipes.generateLowQualityGems));

    // A regular Gem worth two Dusts.
    public static final TagPrefix gemFlawless = new TagPrefix("gemFlawless", "gems/flawless")
            .langValue("Flawless %s")
            .materialAmount(GTValues.M * 2)
            .maxStackSize(32)
            .materialIconType(MaterialIconType.gemFlawless)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasGemProperty);

    // A regular Gem worth four Dusts.
    public static final TagPrefix gemExquisite = new TagPrefix("gemExquisite", "gems/exquisite")
            .langValue("Exquisite %s")
            .materialAmount(GTValues.M * 4)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.gemExquisite)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasGemProperty);

    // 1/4th of a Dust.
    public static final TagPrefix dustSmall = new TagPrefix("dustSmall", "dusts/small")
            .langValue("Small Pile of %s Dust")
            .materialAmount(GTValues.M / 4)
            .materialIconType(MaterialIconType.dustSmall)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasDustProperty);

    // 1/9th of a Dust.
    public static final TagPrefix dustTiny = new TagPrefix("dustTiny", "dusts/tiny")
            .langValue("Tiny Pile of %s Dust")
            .materialAmount(GTValues.M / 9)
            .materialIconType(MaterialIconType.dustTiny)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasDustProperty);

    // Dust with impurities. 1 Unit of Main Material and 1/9 - 1/4 Unit of secondary Material
    public static final TagPrefix dustImpure = new TagPrefix("dustImpure", "dusts/impure")
            .langValue("Impure Pile of %s Dust")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.dustImpure)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasOreProperty)
            .tooltip((mat, tooltips) -> tooltips.add(Component.translatable("metaitem.dust.tooltip.purify")));

    // Pure Dust worth of one Ingot or Gem.
    public static final TagPrefix dustPure = new TagPrefix("dustPure", "dusts/pure")
            .langValue("Purified Pile of %s Dust")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.dustPure)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasOreProperty)
            .tooltip((mat, tooltips) -> tooltips.add(Component.translatable("metaitem.dust.tooltip.purify")));

    public static final TagPrefix dust = new TagPrefix("dust", "dusts")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.dust)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasDustProperty);

    // A Nugget.
    public static final TagPrefix nugget = new TagPrefix("nugget", "nuggets")
            .materialAmount(GTValues.M / 9)
            .materialIconType(MaterialIconType.nugget)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasIngotProperty);

    // 9 Plates combined in one Item.
    public static final TagPrefix plateDense = new TagPrefix("plateDense", "plates/dense")
            .langValue("Dense %s Plate")
            .materialAmount(GTValues.M * 9)
            .maxStackSize(7)
            .materialIconType(MaterialIconType.plateDense)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_DENSE) && !mat.hasFlag(MaterialFlags.NO_SMASHING));

    // 2 Plates combined in one Item
    public static final TagPrefix plateDouble = new TagPrefix("plateDouble", "plates/double")
            .langValue("Double %s Plate")
            .materialAmount(GTValues.M * 2)
            .maxStackSize(32)
            .materialIconType(MaterialIconType.plateDouble)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasIngotProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE) && !mat.hasFlag(MaterialFlags.NO_SMASHING)));

    // Regular Plate made of one Ingot/Dust.
    public static final TagPrefix plate = new TagPrefix("plate", "plates")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.plate)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE));


    // Round made of 1 Nugget
    public static final TagPrefix round = new TagPrefix("round", "rounds")
            .materialAmount(GTValues.M / 9)
            .materialIconType(MaterialIconType.round)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_ROUND));

    // Foil made of 1/4 Ingot/Dust.
    public static final TagPrefix foil = new TagPrefix("foil", "foils")
            .materialAmount(GTValues.M / 4)
            .materialIconType(MaterialIconType.foil)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_FOIL));

    // Stick made of an Ingot.
    public static final TagPrefix stickLong = new TagPrefix("stickLong", "sticks/long")
            .langValue("Long %s Rod")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.stickLong)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_LONG_ROD));

    // Stick made of half an Ingot.
    public static final TagPrefix stick = new TagPrefix("stick", "sticks")
            .langValue("%s Rod")
            .materialAmount(GTValues.M / 2)
            .materialIconType(MaterialIconType.stick)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_ROD));

    // consisting out of 1/8 Ingot or 1/4 Stick.
    public static final TagPrefix bolt = new TagPrefix("bolt", "bolts")
            .materialAmount(GTValues.M / 8)
            .materialIconType(MaterialIconType.bolt)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_BOLT_SCREW));

    // consisting out of 1/9 Ingot.
    public static final TagPrefix screw = new TagPrefix("screw", "screws")
            .materialAmount(GTValues.M / 9)
            .materialIconType(MaterialIconType.screw)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_BOLT_SCREW));

    // consisting out of 1/2 Stick.
    public static final TagPrefix ring = new TagPrefix("ring", "rings")
            .materialAmount(GTValues.M / 4)
            .materialIconType(MaterialIconType.ring)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_RING));

    // consisting out of 1 Fine Wire.
    public static final TagPrefix springSmall = new TagPrefix("springSmall", "springs/small")
            .langValue("Small %s Spring")
            .materialAmount(GTValues.M / 4)
            .materialIconType(MaterialIconType.springSmall)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_SPRING_SMALL) && !mat.hasFlag(MaterialFlags.NO_SMASHING));

    // consisting out of 2 Sticks.
    public static final TagPrefix spring = new TagPrefix("spring", "springs")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.spring)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_SPRING) && !mat.hasFlag(MaterialFlags.NO_SMASHING));

    // consisting out of 1/8 Ingot or 1/4 Wire.
    public static final TagPrefix wireFine = new TagPrefix("wireFine", "wires/fine")
            .langValue("Fine %s Wire")
            .materialAmount(GTValues.M / 8)
            .materialIconType(MaterialIconType.wireFine)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_FINE_WIRE));

    // consisting out of 4 Plates, 1 Ring and 1 Screw.
    public static final TagPrefix rotor = new TagPrefix("rotor", "rotors")
            .materialAmount(GTValues.M * 4)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.rotor)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_ROTOR));

    // Consisting of 1 Plate.
    public static final TagPrefix gearSmall = new TagPrefix("gearSmall", "gears/small")
            .langValue("Small %s Gear")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.gearSmall)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_SMALL_GEAR));

    // Consisting of 4 Plates.
    public static final TagPrefix gear = new TagPrefix("gear", "gears")
            .materialAmount(GTValues.M * 4)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.gear)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_GEAR));

    // 3/4 of a Plate or Gem used to shape a Lens. Normally only used on Transparent Materials.
    public static final TagPrefix lens = new TagPrefix("lens")
            .materialAmount((GTValues.M * 3) / 4)
            .materialIconType(MaterialIconType.lens)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_LENS));


    // made of 4 Ingots.
    public static final TagPrefix toolHeadBuzzSaw = new TagPrefix("toolHeadBuzzSaw", "tool_heads/buzzsaw")
            .langValue("%s Buzzsaw Blade")
            .materialAmount(GTValues.M * 4)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.toolHeadBuzzSaw)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasNoCraftingToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)));

    // made of 1 Ingots.
    public static final TagPrefix toolHeadScrewdriver = new TagPrefix("toolHeadScrewdriver", "tool_heads/screwdriver")
            .langValue("%s Screwdriver Tip")
            .materialAmount(GTValues.M)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.toolHeadScrewdriver)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasNoCraftingToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_LONG_ROD)));

    // made of 4 Ingots.
    public static final TagPrefix toolHeadDrill = new TagPrefix("toolHeadDrill", "tool_heads/drill")
            .langValue("%s Drill Tip")
            .materialAmount(GTValues.M * 4)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.toolHeadDrill)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)));

    // made of 2 Ingots.
    public static final TagPrefix toolHeadChainsaw = new TagPrefix("toolHeadChainsaw", "tool_heads/chainsaw")
            .langValue("%s Chainsaw Tip")
            .materialAmount(GTValues.M * 2)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.toolHeadChainsaw)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasNoCraftingToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)));

    // made of 4 Ingots.
    public static final TagPrefix toolHeadWrench = new TagPrefix("toolHeadWrench", "tool_heads/wrench")
            .langValue("%s Wrench Tip")
            .materialAmount(GTValues.M * 4)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.toolHeadWrench)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasNoCraftingToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)));

    // made of 5 Ingots.
    public static final TagPrefix turbineBlade = new TagPrefix("turbineBlade", "turbine_blades")
            .langValue("%s Turbine Blade")
            .materialAmount(GTValues.M * 10)
            .materialIconType(MaterialIconType.turbineBlade)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasRotorProperty.and(m -> m.hasFlags(MaterialFlags.GENERATE_BOLT_SCREW, MaterialFlags.GENERATE_PLATE) && !m.hasProperty(PropertyKey.GEM)));

    // Storage Block consisting out of 9 Ingots/Gems/Dusts.
    public static final TagPrefix block = new TagPrefix("block", "storage_blocks")
            .langValue("Block of %s")
            .materialAmount(GTValues.M * 9)
            .materialIconType(MaterialIconType.block)
            .unificationEnabled(true);

    // Prefix to determine which kind of Rock this is.
    // todo remove?
    public static final TagPrefix stone = new TagPrefix("stone")
            .materialType(GTMaterials.Stone)
            .selfReferencing(true);

    public static final TagPrefix frameGt = new TagPrefix("frameGt", "frames")
            .langValue("%s Frame Box")
            .materialAmount(GTValues.M * 2)
            .materialIconType(MaterialIconType.frameGt)
            .unificationEnabled(true)
            .generationCondition(material -> material.hasFlag(MaterialFlags.GENERATE_FRAME));

    public static final TagPrefix pipeTinyFluid = new TagPrefix("pipeTinyFluid", "fluid_pipes/tiny").langValue("Tiny %s Fluid Pipe").materialAmount(GTValues.M / 2).unificationEnabled(true);
    public static final TagPrefix pipeSmallFluid = new TagPrefix("pipeSmallFluid", "fluid_pipes/small").langValue("Small %s Fluid Pipe").materialAmount(GTValues.M).unificationEnabled(true);
    public static final TagPrefix pipeNormalFluid = new TagPrefix("pipeNormalFluid", "fluid_pipes/normal").langValue("Normal %s Fluid Pipe").materialAmount(GTValues.M * 3).unificationEnabled(true);
    public static final TagPrefix pipeLargeFluid = new TagPrefix("pipeLargeFluid", "fluid_pipes/large").langValue("Large %s Fluid Pipe").materialAmount(GTValues.M * 6).unificationEnabled(true);
    public static final TagPrefix pipeHugeFluid = new TagPrefix("pipeHugeFluid", "fluid_pipes/huge").langValue("Huge %s Fluid Pipe").materialAmount(GTValues.M * 12).unificationEnabled(true);
    public static final TagPrefix pipeQuadrupleFluid = new TagPrefix("pipeQuadrupleFluid", "fluid_pipes/quadruple").langValue("Quadruple %s Fluid Pipe").materialAmount(GTValues.M * 4).unificationEnabled(true);
    public static final TagPrefix pipeNonupleFluid = new TagPrefix("pipeNonupleFluid", "fluid_pipes/nonuple").langValue("Nonuple %s Fluid Pipe").materialAmount(GTValues.M * 9).unificationEnabled(true);

    public static final TagPrefix pipeTinyItem = new TagPrefix("pipeTinyItem", "item_pipes/tiny").langValue("Tiny %s Item Pipe").materialAmount(GTValues.M / 2).unificationEnabled(true);
    public static final TagPrefix pipeSmallItem = new TagPrefix("pipeSmallItem", "item_pipes/small").langValue("Small %s Item Pipe").materialAmount(GTValues.M).unificationEnabled(true);
    public static final TagPrefix pipeNormalItem = new TagPrefix("pipeNormalItem", "item_pipes/normal").langValue("Normal %s Item Pipe").materialAmount(GTValues.M * 3).unificationEnabled(true);
    public static final TagPrefix pipeLargeItem = new TagPrefix("pipeLargeItem", "item_pipes/large").langValue("Large %s Item Pipe").materialAmount(GTValues.M * 6).unificationEnabled(true);
    public static final TagPrefix pipeHugeItem = new TagPrefix("pipeHugeItem", "item_pipes/huge").langValue("Huge %s Item Pipe").materialAmount(GTValues.M * 12).unificationEnabled(true);

    public static final TagPrefix pipeSmallRestrictive = new TagPrefix("pipeSmallRestrictive", "item_pipes/small_restrictive").langValue("Small Restrictive %s Item Pipe").materialAmount(GTValues.M).unificationEnabled(true);
    public static final TagPrefix pipeNormalRestrictive = new TagPrefix("pipeNormalRestrictive", "item_pipes/normal_restrictive").langValue("Normal Restrictive %s Item Pipe").materialAmount(GTValues.M * 3).unificationEnabled(true);
    public static final TagPrefix pipeLargeRestrictive = new TagPrefix("pipeLargeRestrictive", "item_pipes/large_restrictive").langValue("Large Restrictive %s Item Pipe").materialAmount(GTValues.M * 6).unificationEnabled(true);
    public static final TagPrefix pipeHugeRestrictive = new TagPrefix("pipeHugeRestrictive", "item_pipes/huge_restrictive").langValue("Huge Restrictive %s Item Pipe").materialAmount(GTValues.M * 12).unificationEnabled(true);

    public static final TagPrefix wireGtHex = new TagPrefix("wireGtHex", "wires/hex").langValue("16x %s Wire").materialAmount(GTValues.M * 8).unificationEnabled(true);
    public static final TagPrefix wireGtOctal = new TagPrefix("wireGtOctal", "wires/octal").langValue("8x %s Wire").materialAmount(GTValues.M * 4).unificationEnabled(true);
    public static final TagPrefix wireGtQuadruple = new TagPrefix("wireGtQuadruple", "wires/quadruple").langValue("4x %s Wire").materialAmount(GTValues.M * 2).unificationEnabled(true);
    public static final TagPrefix wireGtDouble = new TagPrefix("wireGtDouble", "wires/double").langValue("2x %s Wire").materialAmount(GTValues.M).unificationEnabled(true);
    public static final TagPrefix wireGtSingle = new TagPrefix("wireGtSingle", "wires/single").langValue("1x %s Wire").materialAmount(GTValues.M / 2).unificationEnabled(true);

    public static final TagPrefix cableGtHex = new TagPrefix("cableGtHex", "cables/hex").langValue("16x %s Cable").materialAmount(GTValues.M * 8).unificationEnabled(true);
    public static final TagPrefix cableGtOctal = new TagPrefix("cableGtOctal", "cables/octal").langValue("8x %s Cable").materialAmount(GTValues.M * 4).unificationEnabled(true);
    public static final TagPrefix cableGtQuadruple = new TagPrefix("cableGtQuadruple", "cables/quadruple").langValue("4x %s Cable").materialAmount(GTValues.M * 2).unificationEnabled(true);
    public static final TagPrefix cableGtDouble = new TagPrefix("cableGtDouble", "cables/double").langValue("2x %s Cable").materialAmount(GTValues.M).unificationEnabled(true);
    public static final TagPrefix cableGtSingle = new TagPrefix("cableGtSingle", "cables/single").langValue("1x %s Cable").materialAmount(GTValues.M / 2).unificationEnabled(true);

    // Special Prefix used mainly for the Crafting Handler.
    // Used for the 16 dyes.
    public static final TagPrefix dye = new TagPrefix("dye", "dyes").isMarkerPrefix(true);

    // Used for when a crafting-only tag should be added to a random assortment of items.
    // Ex:
    // - Chest and Trapped Chest
    // - Piston and Sticky Piston
    //
    // Commonly used with MarkerMaterials.Misc though not exclusively
    public static final TagPrefix crafting = new TagPrefix("crafting").isMarkerPrefix(true);

    /**
     * Electric Components.
     *
     * @see MarkerMaterials.Tier
     */
    public static final TagPrefix battery = new TagPrefix("battery").isMarkerPrefix(true);
    public static final TagPrefix circuit = new TagPrefix("circuit").unificationEnabled(true).isMarkerPrefix(true);
    public static final TagPrefix component = new TagPrefix("component").unificationEnabled(true);

    public static class Flags {
        public static final long ENABLE_UNIFICATION = 1;
        public static final long SELF_REFERENCING = 1 << 1;
        public static final long GENERATE_ITEM = 1 << 2;
        public static final long VANILLA_TAG = 1 << 3;
    }

    @FunctionalInterface
    public interface ITagRegistrationHandler {
        void processMaterial(TagPrefix tagPrefix, Material material);
    }

    public static class Conditions {
        public static final Predicate<Material> hasToolProperty = mat -> mat.hasProperty(PropertyKey.TOOL);
        public static final Predicate<Material> hasNoCraftingToolProperty = hasToolProperty.and(mat -> !mat.getProperty(PropertyKey.TOOL).isIgnoreCraftingTools());
        public static final Predicate<Material> hasOreProperty = mat -> mat.hasProperty(PropertyKey.ORE);
        public static final Predicate<Material> hasGemProperty = mat -> mat.hasProperty(PropertyKey.GEM);
        public static final Predicate<Material> hasDustProperty = mat -> mat.hasProperty(PropertyKey.DUST);
        public static final Predicate<Material> hasIngotProperty = mat -> mat.hasProperty(PropertyKey.INGOT);
        public static final Predicate<Material> hasBlastProperty = mat -> mat.hasProperty(PropertyKey.BLAST);
        public static final Predicate<Material> hasRotorProperty = mat -> mat.hasProperty(PropertyKey.ROTOR);
    }

    @Getter
    public final String name;
    public final String[] tagPaths;
    @Setter @Getter
    public String langValue;

    @Getter
    @Setter(value = AccessLevel.PROTECTED)
    private long materialAmount = -1;

    @Setter(value = AccessLevel.PROTECTED)
    private boolean unificationEnabled;
    @Setter(value = AccessLevel.PROTECTED)
    private boolean selfReferencing;
    @Setter(value = AccessLevel.PROTECTED)
    private boolean generateItem;
    @Setter(value = AccessLevel.PROTECTED)
    private boolean vanillaTag;

    @Setter(value = AccessLevel.PROTECTED)
    private @Nullable
    Predicate<Material> generationCondition;

    @Nullable @Getter
    @Setter(value = AccessLevel.PROTECTED)
    private MaterialIconType materialIconType;


    /**
     * Contains a default material type for self-referencing OrePrefix
     * For self-referencing prefixes, it is always guaranteed for it to be not null
     * <p>
     * NOTE: Ore registrations with self-referencing OrePrefix still can occur with other materials
     */
    @Nullable @Getter
    @Setter(value = AccessLevel.PROTECTED)
    private Material materialType;

    @Nullable @Getter
    @Setter(value = AccessLevel.PROTECTED)
    private BiConsumer<Material, List<Component>> tooltip;

    private final Set<Material> ignoredMaterials = new HashSet<>();

    @Getter
    @Setter(value = AccessLevel.PROTECTED)
    private boolean isMarkerPrefix = false;

    @Getter
    @Setter(value = AccessLevel.PROTECTED)
    private int maxStackSize = 64;

    @Getter
    private final List<MaterialStack> secondaryMaterials = new ArrayList<>();

    protected TagPrefix(String name, String... tagPaths) {
        this.name = name;
        this.tagPaths = tagPaths.length > 0 ? tagPaths : new String[]{FormattingUtil.toLowerCaseUnder(name)};
        this.langValue = "%s " + FormattingUtil.toEnglishName(FormattingUtil.toLowerCaseUnder(name));
        PREFIXES.put(name, this);
    }

    public void addSecondaryMaterial(MaterialStack secondaryMaterial) {
        Preconditions.checkNotNull(secondaryMaterial, "secondaryMaterial");
        secondaryMaterials.add(secondaryMaterial);
    }

    /**
     * Mappings between materials and their corresponding material amount
     */
    private static final Map<Material, Long> MATERIAL_AMOUNT_MAP = ImmutableMap.ofEntries(

            // Blocks (4 materials)
            Map.entry(GTMaterials.Amethyst, GTValues.M * 4),
            Map.entry(GTMaterials.Brick, GTValues.M * 4),
            Map.entry(GTMaterials.Clay, GTValues.M * 4),
            Map.entry(GTMaterials.Glowstone, GTValues.M * 4),
            Map.entry(GTMaterials.NetherQuartz, GTValues.M * 4),

            // Blocks (1 material)
            Map.entry(GTMaterials.Concrete, GTValues.M),
            Map.entry(GTMaterials.Glass, GTValues.M),
            Map.entry(GTMaterials.Ice, GTValues.M),
            Map.entry(GTMaterials.Obsidian, GTValues.M),

            // Stick materials
            Map.entry(GTMaterials.Blaze, GTValues.M * 4),
            Map.entry(GTMaterials.Bone, GTValues.M * 5)

    );

    public long getMaterialAmount(@Nullable Material material) {
        return MATERIAL_AMOUNT_MAP.getOrDefault(material, materialAmount);
    }

    public static TagPrefix getPrefix(String prefixName) {
        return getPrefix(prefixName, null);
    }

    public static TagPrefix getPrefix(String prefixName, @Nullable TagPrefix replacement) {
        return PREFIXES.getOrDefault(prefixName, replacement);
    }

    public TagPrefix registerOre(Supplier<BlockState> stone) {
        ORES.put(this, stone);
        return this;
    }


    @SuppressWarnings("unchecked")
    public TagKey<Item>[] getItemTags() {
        return (TagKey<Item>[]) Arrays.stream(tagPaths).map(path -> TagUtil.createItemTag(path, vanillaTag)).toArray(TagKey[]::new);
    }

    @SuppressWarnings("unchecked")
    public TagKey<Block>[] getBlockTags() {
        return (TagKey<Block>[]) Arrays.stream(tagPaths).map(path -> TagUtil.createBlockTag(path, vanillaTag)).toArray(TagKey[]::new);
    }

    @SuppressWarnings("unchecked")
    public TagKey<Item>[] getSubItemTags(String path) {
        return (TagKey<Item>[]) Arrays.stream(tagPaths).map(root -> TagUtil.createItemTag(root + "/" + path, vanillaTag)).toArray(TagKey[]::new);
    }

    public TagKey<Item>[] getSubItemTags(Material mat) {
        return getSubItemTags(mat.getName());
    }

    @SuppressWarnings("unchecked")
    public TagKey<Block>[] getSubBlockTags(String path) {
        return (TagKey<Block>[]) Arrays.stream(tagPaths).map(root -> TagUtil.createBlockTag(root + "/" + path, vanillaTag)).toArray(TagKey[]::new);
    }

    public TagKey<Block>[] getSubBlockTags(Material mat) {
        return getSubBlockTags(mat.getName());
    }

    public boolean doGenerateItem() {
        return generateItem;
    }

    public boolean doGenerateItem(Material material) {
        return generateItem && !selfReferencing && !isIgnored(material) && (generationCondition == null || generationCondition.test(material));
    }

    public <T extends IMaterialProperty<T>> void executeHandler(PropertyKey<T> propertyKey, TriConsumer<TagPrefix, Material, T> handler) {
        for (Material material : GTRegistries.MATERIALS) {
            if (material.hasProperty(propertyKey) && !material.hasFlag(MaterialFlags.NO_UNIFICATION) && !ChemicalHelper.get(this, material).isEmpty()) {
                handler.accept(this, material, material.getProperty(propertyKey));
            }
        }
    }

    public String getUnlocalizedName() {
        return "tagprefix." + FormattingUtil.toLowerCaseUnderscore(name);
    }

    public String getLocalNameForItem(Material material) {
        return LocalizationUtils.format(getUnlocalizedName(), LocalizationUtils.format(material.getUnlocalizedName()));
    }

    private String findUnlocalizedName(Material material) {
        if(material.hasProperty(PropertyKey.POLYMER)) {
            String localizationKey = String.format("item.material.oreprefix.polymer.%s", this.name);
            // Not every polymer ore prefix gets a special name
            if(LocalizationUtils.exist(localizationKey)) {
                return localizationKey;
            }
        }

        return String.format("item.material.oreprefix.%s", this.name);
    }

    public boolean isIgnored(Material material) {
        return ignoredMaterials.contains(material);
    }

    public void setIgnored(Material material, ItemLike... items) {
        ignoredMaterials.add(material);
        if (items.length > 0) {
            ChemicalHelper.registerUnificationItems(this, material, items);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagPrefix tagPrefix = (TagPrefix) o;
        return name.equals(tagPrefix.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public static Collection<TagPrefix> values() {
        return PREFIXES.values();
    }

    @Override
    public String toString() {
        return name;
    }
}
