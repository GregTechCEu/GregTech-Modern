package com.gregtechceu.gtceu.api.data.tag;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.IMaterialProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.util.TriConsumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.*;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.Conditions.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.LoaderType.*;

@Accessors(chain = true, fluent = true)
public class TagPrefix {

    private final static Map<String, TagPrefix> PREFIXES = new HashMap<>();
    public static final Map<TagPrefix, Supplier<BlockState>> ORES = new HashMap<>();

    public static final TagPrefix ore = new TagPrefix("stone")
            .defaultTagPath(FORGE, "ores/%s")
            .defaultTagPath(FABRIC, "%s_ores")
            .langValue("%s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty)
            .registerOre(Blocks.STONE::defaultBlockState);

    public static final TagPrefix oreGranite = new TagPrefix("granite")
            .defaultTagPath(FORGE, "ores/%s")
            .defaultTagPath(FABRIC, "%s_ores")
            .langValue("Granite %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty)
            .registerOre(Blocks.GRANITE::defaultBlockState);

    public static final TagPrefix oreDiorite = new TagPrefix("diorite")
            .defaultTagPath(FORGE, "ores/%s")
            .defaultTagPath(FABRIC, "%s_ores")
            .langValue("Diorite %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty)
            .registerOre(Blocks.DIORITE::defaultBlockState);

    public static final TagPrefix oreAndesite = new TagPrefix("andesite")
            .defaultTagPath(FORGE, "ores/%s")
            .defaultTagPath(FABRIC, "%s_ores")
            .langValue("Andesite %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty)
            .registerOre(Blocks.ANDESITE::defaultBlockState);

    // todo move to doubling, since this is a nether block?
    public static final TagPrefix oreBasalt = new TagPrefix("basalt")
            .defaultTagPath(FORGE, "ores/%s")
            .defaultTagPath(FABRIC, "%s_ores")
            .langValue("Basalt %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty)
            .registerOre(Blocks.BASALT::defaultBlockState);

    public static final TagPrefix oreDeepslate = new TagPrefix("deepslate")
            .defaultTagPath(FORGE, "ores/%s")
            .defaultTagPath(FABRIC, "%s_ores")
            .langValue("Deepslate %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty)
            .registerOre(Blocks.DEEPSLATE::defaultBlockState);

    public static final TagPrefix oreSand = new TagPrefix("sand")
            .defaultTagPath(FORGE, "ores/%s")
            .defaultTagPath(FABRIC, "%s_ores")
            .langValue("Sand %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty)
            .registerOre(Blocks.SAND::defaultBlockState);

    public static final TagPrefix oreRedSand = new TagPrefix("redSand")
            .defaultTagPath(FORGE, "ores/%s")
            .defaultTagPath(FABRIC, "%s_ores")
            .langValue("Red Sand %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty)
            .registerOre(Blocks.RED_SAND::defaultBlockState);

    public static final TagPrefix oreNetherrack = new TagPrefix("netherrack")
            .prefixTagPath(FORGE, "%s_ores/%s")
            .prefixTagPath(FABRIC, "%s_%s_ores")
            .langValue("Nether %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty)
            .registerOre(Blocks.NETHERRACK::defaultBlockState);

    public static final TagPrefix oreEndstone = new TagPrefix("endstone")
            .prefixTagPath(FORGE, "%s_ores/%s")
            .prefixTagPath(FABRIC, "%s_%s_ores")
            .langValue("End %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty)
            .registerOre(Blocks.END_STONE::defaultBlockState);

    public static final TagPrefix crushedRefined = new TagPrefix("crushedRefined")
            .defaultTagPath(FORGE, "refined_ores/%s")
            .defaultTagPath(FABRIC, "%s_refined_ores")
            .langValue("Refined %s Ore")
            .materialIconType(MaterialIconType.crushedRefined)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasOreProperty);

    public static final TagPrefix crushedPurified = new TagPrefix("crushedPurified")
            .defaultTagPath(FORGE, "purified_ores/%s")
            .defaultTagPath(FABRIC, "%s_purified_ores")
            .langValue("Purified %s Ore")
            .materialIconType(MaterialIconType.crushedPurified)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasOreProperty);

    public static final TagPrefix crushed = new TagPrefix("crushed")
            .defaultTagPath(FORGE, "crushed_ores/%s")
            .defaultTagPath(FABRIC, "%s_crushed_ores")
            .langValue("Crushed %s Ore")
            .materialIconType(MaterialIconType.crushed)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasOreProperty)
            .tooltip((mat, tooltips) -> tooltips.add(Component.translatable("metaitem.crushed.tooltip.purify")));

    // A hot Ingot, which has to be cooled down by a Vacuum Freezer.
    public static final TagPrefix ingotHot = new TagPrefix("ingotHot")
            .defaultTagPath(FORGE, "ingots/hot/%s")
            .defaultTagPath(FABRIC, "%s_hot_ingots")
            .langValue("Hot %s Ingot")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.ingotHot)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasBlastProperty.and(mat -> mat.getProperty(PropertyKey.BLAST).getBlastTemperature() > 1750));

    // A regular Ingot.
    public static final TagPrefix ingot = new TagPrefix("ingot")
            .defaultTagPath(FORGE, "ingots/%s")
            .defaultTagPath(FABRIC, "%s_ingots")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.ingot)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasIngotProperty);

    // A regular Gem worth one Dust.
    public static final TagPrefix gem = new TagPrefix("gem")
            .defaultTagPath(FORGE, "gems/%s")
            .defaultTagPath(FABRIC, "%s_gems")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.gem)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasGemProperty);

    // A regular Gem worth one small Dust.
    public static final TagPrefix gemChipped = new TagPrefix("gemChipped")
            .defaultTagPath(FORGE, "gems/chipped/%s")
            .defaultTagPath(FABRIC, "%s_chipped_gems")
            .langValue("Chipped %s")
            .materialAmount(GTValues.M / 4)
            .materialIconType(MaterialIconType.gemChipped)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasGemProperty.and(unused -> ConfigHolder.recipes.generateLowQualityGems));

    // A regular Gem worth two small Dusts.
    public static final TagPrefix gemFlawed = new TagPrefix("gemFlawed")
            .defaultTagPath(FORGE, "gems/flawed/%s")
            .defaultTagPath(FABRIC, "%s_flawed_gems")
            .langValue("Flawed %s")
            .materialAmount(GTValues.M / 2)
            .materialIconType(MaterialIconType.gemFlawed)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasGemProperty.and(unused -> ConfigHolder.recipes.generateLowQualityGems));

    // A regular Gem worth two Dusts.
    public static final TagPrefix gemFlawless = new TagPrefix("gemFlawless")
            .defaultTagPath(FORGE, "gems/flawless/%s")
            .defaultTagPath(FABRIC, "%s_flawless_gems")
            .langValue("Flawless %s")
            .materialAmount(GTValues.M * 2)
            .maxStackSize(32)
            .materialIconType(MaterialIconType.gemFlawless)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasGemProperty);

    // A regular Gem worth four Dusts.
    public static final TagPrefix gemExquisite = new TagPrefix("gemExquisite")
            .defaultTagPath(FORGE, "gems/exquisite/%s")
            .defaultTagPath(FABRIC, "%s_exquisite_gems")
            .langValue("Exquisite %s")
            .materialAmount(GTValues.M * 4)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.gemExquisite)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasGemProperty);

    // 1/4th of a Dust.
    public static final TagPrefix dustSmall = new TagPrefix("dustSmall")
            .defaultTagPath(FORGE, "dusts/small/%s")
            .defaultTagPath(FABRIC, "%s_small_dusts")
            .langValue("Small Pile of %s Dust")
            .materialAmount(GTValues.M / 4)
            .materialIconType(MaterialIconType.dustSmall)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasDustProperty);

    // 1/9th of a Dust.
    public static final TagPrefix dustTiny = new TagPrefix("dustTiny")
            .defaultTagPath(FORGE, "dusts/tiny/%s")
            .defaultTagPath(FABRIC, "%s_tiny_dusts")
            .langValue("Tiny Pile of %s Dust")
            .materialAmount(GTValues.M / 9)
            .materialIconType(MaterialIconType.dustTiny)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasDustProperty);

    // Dust with impurities. 1 Unit of Main Material and 1/9 - 1/4 Unit of secondary Material
    public static final TagPrefix dustImpure = new TagPrefix("dustImpure")
            .defaultTagPath(FORGE, "dusts/impure/%s")
            .defaultTagPath(FABRIC, "%s_impure_dusts")
            .langValue("Impure Pile of %s Dust")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.dustImpure)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasOreProperty)
            .tooltip((mat, tooltips) -> tooltips.add(Component.translatable("metaitem.dust.tooltip.purify")));

    // Pure Dust worth of one Ingot or Gem.
    public static final TagPrefix dustPure = new TagPrefix("dustPure")
            .defaultTagPath(FORGE, "dusts/pure/%s")
            .defaultTagPath(FABRIC, "%s_pure_dusts")
            .langValue("Purified Pile of %s Dust")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.dustPure)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasOreProperty)
            .tooltip((mat, tooltips) -> tooltips.add(Component.translatable("metaitem.dust.tooltip.purify")));

    public static final TagPrefix dust = new TagPrefix("dust")
            .defaultTagPath(FORGE, "dusts/%s")
            .defaultTagPath(FABRIC, "%s_dusts")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.dust)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasDustProperty);

    // A Nugget.
    public static final TagPrefix nugget = new TagPrefix("nugget")
            .defaultTagPath(FORGE, "nuggets/%s")
            .defaultTagPath(FABRIC, "%s_nuggets")
            .materialAmount(GTValues.M / 9)
            .materialIconType(MaterialIconType.nugget)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasIngotProperty);

    // 9 Plates combined in one Item.
    public static final TagPrefix plateDense = new TagPrefix("plateDense")
            .defaultTagPath(FORGE, "plates/dense/%s")
            .defaultTagPath(FABRIC, "%s_dense_plates")
            .langValue("Dense %s Plate")
            .materialAmount(GTValues.M * 9)
            .maxStackSize(7)
            .materialIconType(MaterialIconType.plateDense)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_DENSE) && !mat.hasFlag(MaterialFlags.NO_SMASHING));

    // 2 Plates combined in one Item
    public static final TagPrefix plateDouble = new TagPrefix("plateDouble")
            .defaultTagPath(FORGE, "plates/double/%s")
            .defaultTagPath(FABRIC, "%s_double_plates")
            .langValue("Double %s Plate")
            .materialAmount(GTValues.M * 2)
            .maxStackSize(32)
            .materialIconType(MaterialIconType.plateDouble)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasIngotProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE) && !mat.hasFlag(MaterialFlags.NO_SMASHING)));

    // Regular Plate made of one Ingot/Dust.
    public static final TagPrefix plate = new TagPrefix("plate")
            .defaultTagPath(FORGE, "plates/%s")
            .defaultTagPath(FABRIC, "%s_plates")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.plate)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE));

    // Round made of 1 Nugget
    public static final TagPrefix round = new TagPrefix("round")
            .defaultTagPath(FORGE, "rounds/%s")
            .defaultTagPath(FABRIC, "%s_rounds")
            .materialAmount(GTValues.M / 9)
            .materialIconType(MaterialIconType.round)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_ROUND));

    // Foil made of 1/4 Ingot/Dust.
    public static final TagPrefix foil = new TagPrefix("foil")
            .defaultTagPath(FORGE, "foils/%s")
            .defaultTagPath(FABRIC, "%s_foils")
            .materialAmount(GTValues.M / 4)
            .materialIconType(MaterialIconType.foil)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_FOIL));

    // Stick made of an Ingot.
    public static final TagPrefix rodLong = new TagPrefix("longRod")
            .defaultTagPath(FORGE, "rods/long/%s")
            .defaultTagPath(FABRIC, "%s_long_rods")
            .langValue("Long %s Rod")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.stickLong)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_LONG_ROD));

    // Stick made of half an Ingot.
    public static final TagPrefix rod = new TagPrefix("rod")
            .defaultTagPath(FORGE, "rods/%s")
            .defaultTagPath(FABRIC, "%s_rods")
            .langValue("%s Rod")
            .materialAmount(GTValues.M / 2)
            .materialIconType(MaterialIconType.stick)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_ROD));

    // consisting out of 1/8 Ingot or 1/4 Stick.
    public static final TagPrefix bolt = new TagPrefix("bolt")
            .defaultTagPath(FORGE, "bolts/%s")
            .defaultTagPath(FABRIC, "%s_bolts")
            .materialAmount(GTValues.M / 8)
            .materialIconType(MaterialIconType.bolt)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_BOLT_SCREW));

    // consisting out of 1/9 Ingot.
    public static final TagPrefix screw = new TagPrefix("screw")
            .defaultTagPath(FORGE, "screws/%s")
            .defaultTagPath(FABRIC, "%s_screws")
            .materialAmount(GTValues.M / 9)
            .materialIconType(MaterialIconType.screw)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_BOLT_SCREW));

    // consisting out of 1/2 Stick.
    public static final TagPrefix ring = new TagPrefix("ring")
            .defaultTagPath(FORGE, "rings/%s")
            .defaultTagPath(FABRIC, "%s_rings")
            .materialAmount(GTValues.M / 4)
            .materialIconType(MaterialIconType.ring)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_RING));

    // consisting out of 1 Fine Wire.
    public static final TagPrefix springSmall = new TagPrefix("springSmall")
            .defaultTagPath(FORGE, "springs/small/%s")
            .defaultTagPath(FABRIC, "%s_small_springs")
            .langValue("Small %s Spring")
            .materialAmount(GTValues.M / 4)
            .materialIconType(MaterialIconType.springSmall)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_SPRING_SMALL) && !mat.hasFlag(MaterialFlags.NO_SMASHING));

    // consisting out of 2 Sticks.
    public static final TagPrefix spring = new TagPrefix("spring")
            .defaultTagPath(FORGE, "springs/%s")
            .defaultTagPath(FABRIC, "%s_springs")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.spring)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_SPRING) && !mat.hasFlag(MaterialFlags.NO_SMASHING));

    // consisting out of 1/8 Ingot or 1/4 Wire.
    public static final TagPrefix wireFine = new TagPrefix("wireFine")
            .defaultTagPath(FORGE, "wires/fine/%s")
            .defaultTagPath(FABRIC, "%s_fine_wires")
            .langValue("Fine %s Wire")
            .materialAmount(GTValues.M / 8)
            .materialIconType(MaterialIconType.wireFine)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_FINE_WIRE));

    // consisting out of 4 Plates, 1 Ring and 1 Screw.
    public static final TagPrefix rotor = new TagPrefix("rotor")
            .defaultTagPath(FORGE, "rotors/%s")
            .defaultTagPath(FABRIC, "%s_rotors")
            .materialAmount(GTValues.M * 4)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.rotor)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_ROTOR));

    // Consisting of 1 Plate.
    public static final TagPrefix gearSmall = new TagPrefix("gearSmall")
            .defaultTagPath(FORGE, "gears/small/%s")
            .defaultTagPath(FABRIC, "%s_small_gears")
            .langValue("Small %s Gear")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.gearSmall)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_SMALL_GEAR));

    // Consisting of 4 Plates.
    public static final TagPrefix gear = new TagPrefix("gear")
            .defaultTagPath(FORGE, "gears/%s")
            .defaultTagPath(FABRIC, "%s_gears")
            .materialAmount(GTValues.M * 4)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.gear)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_GEAR));

    // 3/4 of a Plate or Gem used to shape a Lens. Normally only used on Transparent Materials.
    public static final TagPrefix lens = new TagPrefix("lens")
            .defaultTagPath(FORGE, "lenses/%s")
            .defaultTagPath(FABRIC, "%s_lenses")
            .materialAmount((GTValues.M * 3) / 4)
            .materialIconType(MaterialIconType.lens)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_LENS));

    // made of 4 Ingots.
    public static final TagPrefix toolHeadBuzzSaw = new TagPrefix("toolHeadBuzzSaw")
            .itemTable(() -> GTItems.MATERIAL_ITEMS)
            .langValue("%s Buzzsaw Blade")
            .materialAmount(GTValues.M * 4)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.toolHeadBuzzSaw)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasNoCraftingToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)));

    // made of 1 Ingots.
    public static final TagPrefix toolHeadScrewdriver = new TagPrefix("toolHeadScrewdriver")
            .itemTable(() -> GTItems.MATERIAL_ITEMS)
            .langValue("%s Screwdriver Tip")
            .materialAmount(GTValues.M)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.toolHeadScrewdriver)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasNoCraftingToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_LONG_ROD)));

    // made of 4 Ingots.
    public static final TagPrefix toolHeadDrill = new TagPrefix("toolHeadDrill")
            .itemTable(() -> GTItems.MATERIAL_ITEMS)
            .langValue("%s Drill Tip")
            .materialAmount(GTValues.M * 4)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.toolHeadDrill)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)));

    // made of 2 Ingots.
    public static final TagPrefix toolHeadChainsaw = new TagPrefix("toolHeadChainsaw")
            .itemTable(() -> GTItems.MATERIAL_ITEMS)
            .langValue("%s Chainsaw Tip")
            .materialAmount(GTValues.M * 2)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.toolHeadChainsaw)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasNoCraftingToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)));

    // made of 4 Ingots.
    public static final TagPrefix toolHeadWrench = new TagPrefix("toolHeadWrench")
            .itemTable(() -> GTItems.MATERIAL_ITEMS)
            .langValue("%s Wrench Tip")
            .materialAmount(GTValues.M * 4)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.toolHeadWrench)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasNoCraftingToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)));

    // made of 5 Ingots.
    public static final TagPrefix turbineBlade = new TagPrefix("turbineBlade")
            .itemTable(() -> GTItems.MATERIAL_ITEMS)
            .langValue("%s Turbine Blade")
            .materialAmount(GTValues.M * 10)
            .materialIconType(MaterialIconType.turbineBlade)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasRotorProperty.and(m -> m.hasFlags(MaterialFlags.GENERATE_BOLT_SCREW, MaterialFlags.GENERATE_PLATE) && !m.hasProperty(PropertyKey.GEM)));

    // Storage Block consisting out of 9 Ingots/Gems/Dusts.
    public static final TagPrefix block = new TagPrefix("block")
            .defaultTagPath(FORGE, "storage_blocks/%s")
            .defaultTagPath(FABRIC, "%s_blocks")
            .langValue("Block of %s")
            .materialAmount(GTValues.M * 9)
            .materialIconType(MaterialIconType.block)
            .unificationEnabled(true);

    public static final TagPrefix frameGt = new TagPrefix("frameGt")
            .defaultTagPath(FORGE, "frames/%s")
            .defaultTagPath(FABRIC, "%s_frames")
            .langValue("%s Frame Box")
            .materialAmount(GTValues.M * 2)
            .materialIconType(MaterialIconType.frameGt)
            .unificationEnabled(true)
            .generationCondition(material -> material.hasFlag(MaterialFlags.GENERATE_FRAME));

    // Pipes
    public static final TagPrefix pipeTinyFluid = new TagPrefix("pipeTinyFluid").itemTable(() -> GTBlocks.FLUID_PIPE_BLOCKS).langValue("Tiny %s Fluid Pipe").materialAmount(GTValues.M / 2).unificationEnabled(true);
    public static final TagPrefix pipeSmallFluid = new TagPrefix("pipeSmallFluid").itemTable(() -> GTBlocks.FLUID_PIPE_BLOCKS).langValue("Small %s Fluid Pipe").materialAmount(GTValues.M).unificationEnabled(true);
    public static final TagPrefix pipeNormalFluid = new TagPrefix("pipeNormalFluid").itemTable(() -> GTBlocks.FLUID_PIPE_BLOCKS).langValue("Normal %s Fluid Pipe").materialAmount(GTValues.M * 3).unificationEnabled(true);
    public static final TagPrefix pipeLargeFluid = new TagPrefix("pipeLargeFluid").itemTable(() -> GTBlocks.FLUID_PIPE_BLOCKS).langValue("Large %s Fluid Pipe").materialAmount(GTValues.M * 6).unificationEnabled(true);
    public static final TagPrefix pipeHugeFluid = new TagPrefix("pipeHugeFluid").itemTable(() -> GTBlocks.FLUID_PIPE_BLOCKS).langValue("Huge %s Fluid Pipe").materialAmount(GTValues.M * 12).unificationEnabled(true);

    public static final TagPrefix pipeQuadrupleFluid = new TagPrefix("pipeQuadrupleFluid").itemTable(() -> GTBlocks.FLUID_PIPE_BLOCKS).langValue("Quadruple %s Fluid Pipe").materialAmount(GTValues.M * 4).unificationEnabled(true);
    public static final TagPrefix pipeNonupleFluid = new TagPrefix("pipeNonupleFluid").itemTable(() -> GTBlocks.FLUID_PIPE_BLOCKS).langValue("Nonuple %s Fluid Pipe").materialAmount(GTValues.M * 9).unificationEnabled(true);

    // TODO Item pipes
    //public static final TagPrefix pipeTinyItem = new TagPrefix("pipeTinyItem").itemTable(() -> GTBlocks.ITEM_PIPE_BLOCKS).langValue("Tiny %s Item Pipe").materialAmount(GTValues.M / 2).unificationEnabled(true);
    //public static final TagPrefix pipeSmallItem = new TagPrefix("pipeSmallItem").itemTable(() -> GTBlocks.ITEM_PIPE_BLOCKS).langValue("Small %s Item Pipe").materialAmount(GTValues.M).unificationEnabled(true);
    //public static final TagPrefix pipeNormalItem = new TagPrefix("pipeNormalItem").itemTable(() -> GTBlocks.ITEM_PIPE_BLOCKS).langValue("Normal %s Item Pipe").materialAmount(GTValues.M * 3).unificationEnabled(true);
    //public static final TagPrefix pipeLargeItem = new TagPrefix("pipeLargeItem").itemTable(() -> GTBlocks.ITEM_PIPE_BLOCKS).langValue("Large %s Item Pipe").materialAmount(GTValues.M * 6).unificationEnabled(true);
    //public static final TagPrefix pipeHugeItem = new TagPrefix("pipeHugeItem").itemTable(() -> GTBlocks.ITEM_PIPE_BLOCKS).langValue("Huge %s Item Pipe").materialAmount(GTValues.M * 12).unificationEnabled(true);

    //public static final TagPrefix pipeSmallRestrictive = new TagPrefix("pipeSmallRestrictive").itemTable(() -> GTBlocks.ITEM_PIPE_BLOCKS).langValue("Small Restrictive %s Item Pipe").materialAmount(GTValues.M).unificationEnabled(true);
    //public static final TagPrefix pipeNormalRestrictive = new TagPrefix("pipeNormalRestrictive").itemTable(() -> GTBlocks.ITEM_PIPE_BLOCKS).langValue("Normal Restrictive %s Item Pipe").materialAmount(GTValues.M * 3).unificationEnabled(true);
    //public static final TagPrefix pipeLargeRestrictive = new TagPrefix("pipeLargeRestrictive").itemTable(() -> GTBlocks.ITEM_PIPE_BLOCKS).langValue("Large Restrictive %s Item Pipe").materialAmount(GTValues.M * 6).unificationEnabled(true);
    //public static final TagPrefix pipeHugeRestrictive = new TagPrefix("pipeHugeRestrictive").itemTable(() -> GTBlocks.ITEM_PIPE_BLOCKS).langValue("Huge Restrictive %s Item Pipe").materialAmount(GTValues.M * 12).unificationEnabled(true);

    // Wires and cables
    public static final TagPrefix wireGtHex = new TagPrefix("wireGtHex").itemTable(() -> GTBlocks.CABLE_BLOCKS).langValue("16x %s Wire").materialAmount(GTValues.M * 8).unificationEnabled(true);
    public static final TagPrefix wireGtOctal = new TagPrefix("wireGtOctal").itemTable(() -> GTBlocks.CABLE_BLOCKS).langValue("8x %s Wire").materialAmount(GTValues.M * 4).unificationEnabled(true);
    public static final TagPrefix wireGtQuadruple = new TagPrefix("wireGtQuadruple").itemTable(() -> GTBlocks.CABLE_BLOCKS).langValue("4x %s Wire").materialAmount(GTValues.M * 2).unificationEnabled(true);
    public static final TagPrefix wireGtDouble = new TagPrefix("wireGtDouble").itemTable(() -> GTBlocks.CABLE_BLOCKS).langValue("2x %s Wire").materialAmount(GTValues.M).unificationEnabled(true);
    public static final TagPrefix wireGtSingle = new TagPrefix("wireGtSingle").itemTable(() -> GTBlocks.CABLE_BLOCKS).langValue("1x %s Wire").materialAmount(GTValues.M / 2).unificationEnabled(true);

    public static final TagPrefix cableGtHex = new TagPrefix("cableGtHex").itemTable(() -> GTBlocks.CABLE_BLOCKS).langValue("16x %s Cable").materialAmount(GTValues.M * 8).unificationEnabled(true);
    public static final TagPrefix cableGtOctal = new TagPrefix("cableGtOctal").itemTable(() -> GTBlocks.CABLE_BLOCKS).langValue("8x %s Cable").materialAmount(GTValues.M * 4).unificationEnabled(true);
    public static final TagPrefix cableGtQuadruple = new TagPrefix("cableGtQuadruple").itemTable(() -> GTBlocks.CABLE_BLOCKS).langValue("4x %s Cable").materialAmount(GTValues.M * 2).unificationEnabled(true);
    public static final TagPrefix cableGtDouble = new TagPrefix("cableGtDouble").itemTable(() -> GTBlocks.CABLE_BLOCKS).langValue("2x %s Cable").materialAmount(GTValues.M).unificationEnabled(true);
    public static final TagPrefix cableGtSingle = new TagPrefix("cableGtSingle").itemTable(() -> GTBlocks.CABLE_BLOCKS).langValue("1x %s Cable").materialAmount(GTValues.M / 2).unificationEnabled(true);


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

    protected enum LoaderType {
        FORGE ((prefix, type) -> prefix.forgeTags.add(type)),
        FABRIC ((prefix, type) -> prefix.fabricTags.add(type));

        private final BiConsumer<TagPrefix, TagType> applyTagType;

        LoaderType(BiConsumer<TagPrefix, TagType> applyTagType) {
            this.applyTagType = applyTagType;
        }

        private void apply(TagPrefix prefix, TagType type) {
            applyTagType.accept(prefix, type);
        }
    }

    @Getter
    public final String name;

    private final List<TagType> forgeTags = new ArrayList<>();
    private final List<TagType> fabricTags = new ArrayList<>();
    @Setter @Getter
    public String langValue;

    @Getter
    @Setter(value = AccessLevel.PROTECTED)
    private long materialAmount = -1;

    @Setter(value = AccessLevel.PROTECTED)
    private boolean unificationEnabled;
    @Setter(value = AccessLevel.PROTECTED)
    private boolean generateItem;

    @Setter(value = AccessLevel.PROTECTED)
    private @Nullable
    Predicate<Material> generationCondition;

    @Nullable @Getter
    @Setter(value = AccessLevel.PROTECTED)
    private MaterialIconType materialIconType;

    @Setter(AccessLevel.PROTECTED)
    private Supplier<Table<TagPrefix, Material, ? extends Supplier<? extends ItemLike>>> itemTable;

    @Nullable @Getter
    @Setter(value = AccessLevel.PROTECTED)
    private BiConsumer<Material, List<Component>> tooltip;

    private final Set<Material> ignoredMaterials = new HashSet<>();

    @Getter
    @Setter(value = AccessLevel.PROTECTED)
    private int maxStackSize = 64;

    @Getter
    private final List<MaterialStack> secondaryMaterials = new ArrayList<>();

    protected TagPrefix(String name) {
        this.name = name;
        this.langValue = "%s " + FormattingUtil.toEnglishName(FormattingUtil.toLowerCaseUnder(name));
        PREFIXES.put(name, this);
    }

    public void addSecondaryMaterial(MaterialStack secondaryMaterial) {
        Preconditions.checkNotNull(secondaryMaterial, "secondaryMaterial");
        secondaryMaterials.add(secondaryMaterial);
    }

    protected TagPrefix registerOre(Supplier<BlockState> stoneType) {
        ORES.put(this, stoneType);
        return this;
    }

    protected TagPrefix defaultTagPath(LoaderType loader, String path) {
        loader.apply(this, TagType.withDefaultFormatter(path));
        return this;
    }

    protected TagPrefix prefixTagPath(LoaderType loader, String path) {
        loader.apply(this, TagType.withPrefixFormatter(path));
        return this;
    }

    protected TagPrefix customTagPath(LoaderType loader, String path, BiFunction<TagPrefix, Material, TagKey<Item>> formatter) {
        loader.apply(this, TagType.withCustomFormatter(path, formatter));
        return this;
    }

    /**
     * Mappings between materials and their corresponding material amount
     */
    private static final Map<UnificationEntry, Long> MATERIAL_AMOUNT_MAP = ImmutableMap.ofEntries(

            // Blocks (4 materials)
            Map.entry(new UnificationEntry(TagPrefix.block, GTMaterials.Amethyst), GTValues.M * 4),
            Map.entry(new UnificationEntry(TagPrefix.block, GTMaterials.Brick), GTValues.M * 4),
            Map.entry(new UnificationEntry(TagPrefix.block, GTMaterials.Clay), GTValues.M * 4),
            Map.entry(new UnificationEntry(TagPrefix.block, GTMaterials.Glowstone), GTValues.M * 4),
            Map.entry(new UnificationEntry(TagPrefix.block, GTMaterials.NetherQuartz), GTValues.M * 4),

            // Blocks (1 material)
            Map.entry(new UnificationEntry(TagPrefix.block, GTMaterials.Concrete), GTValues.M),
            Map.entry(new UnificationEntry(TagPrefix.block, GTMaterials.Glass), GTValues.M),
            Map.entry(new UnificationEntry(TagPrefix.block, GTMaterials.Ice), GTValues.M),
            Map.entry(new UnificationEntry(TagPrefix.block, GTMaterials.Obsidian), GTValues.M),

            // Stick materials
            Map.entry(new UnificationEntry(TagPrefix.rod, GTMaterials.Blaze), GTValues.M * 4),
            Map.entry(new UnificationEntry(TagPrefix.rod, GTMaterials.Bone), GTValues.M * 5)

    );

    public long getMaterialAmount(@Nullable Material material) {
        UnificationEntry key = new UnificationEntry(this, material);
        return MATERIAL_AMOUNT_MAP.getOrDefault(key, materialAmount);
    }

    public static TagPrefix getPrefix(String prefixName) {
        return getPrefix(prefixName, null);
    }

    public static TagPrefix getPrefix(String prefixName, @Nullable TagPrefix replacement) {
        return PREFIXES.getOrDefault(prefixName, replacement);
    }

    @SuppressWarnings("unchecked")
    public TagKey<Item>[] getItemTags(@Nonnull Material mat) {
        return (Platform.isForge() ? forgeTags : fabricTags).stream().map(type -> type.getTag(this, mat)).toArray(TagKey[]::new);
    }

    public boolean hasItemTable() {
        return itemTable != null;
    }

    @SuppressWarnings("unchecked")
    public Supplier<ItemLike> getItemFromTable(Material material) {
        return (Supplier<ItemLike>) itemTable.get().get(this, material);
    }

    public boolean doGenerateItem() {
        return generateItem;
    }

    public boolean doGenerateItem(Material material) {
        return generateItem && !isIgnored(material) && (generationCondition == null || generationCondition.test(material));
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
