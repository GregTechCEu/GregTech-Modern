package com.gregtechceu.gtceu.api.data.tag;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
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
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.Conditions.*;

@Accessors(chain = true, fluent = true)
public class TagPrefix {

    private final static Map<String, TagPrefix> PREFIXES = new HashMap<>();
    public static final Map<TagPrefix, Supplier<BlockState>> ORES = new HashMap<>();
    private static final int FORGE_TAG = 0;
    private static final int FABRIC_TAG = 1;

    public static final TagPrefix ore = new TagPrefix("stone")
            .defaultTagPath(FORGE_TAG, "ores/%s")
            .defaultTagPath(FABRIC_TAG, "%s_ores")
            .langValue("%s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty)
            .registerOre(Blocks.STONE::defaultBlockState);

    public static final TagPrefix oreGranite = new TagPrefix("granite")
            .defaultTagPath(FORGE_TAG, "ores/%s")
            .defaultTagPath(FABRIC_TAG, "%s_ores")
            .langValue("Granite %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty)
            .registerOre(Blocks.GRANITE::defaultBlockState);

    public static final TagPrefix oreDiorite = new TagPrefix("diorite")
            .defaultTagPath(FORGE_TAG, "ores/%s")
            .defaultTagPath(FABRIC_TAG, "%s_ores")
            .langValue("Diorite %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty)
            .registerOre(Blocks.DIORITE::defaultBlockState);

    public static final TagPrefix oreAndesite = new TagPrefix("andesite")
            .defaultTagPath(FORGE_TAG, "ores/%s")
            .defaultTagPath(FABRIC_TAG, "%s_ores")
            .langValue("Andesite %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty)
            .registerOre(Blocks.ANDESITE::defaultBlockState);

    // todo move to doubling, since this is a nether block?
    public static final TagPrefix oreBasalt = new TagPrefix("basalt")
            .defaultTagPath(FORGE_TAG, "ores/%s")
            .defaultTagPath(FABRIC_TAG, "%s_ores")
            .langValue("Basalt %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty)
            .registerOre(Blocks.BASALT::defaultBlockState);

    public static final TagPrefix oreDeepslate = new TagPrefix("deepslate")
            .defaultTagPath(FORGE_TAG, "ores/%s")
            .defaultTagPath(FABRIC_TAG, "%s_ores")
            .langValue("Deepslate %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty)
            .registerOre(Blocks.DEEPSLATE::defaultBlockState);

    public static final TagPrefix oreSand = new TagPrefix("sand")
            .defaultTagPath(FORGE_TAG, "ores/%s")
            .defaultTagPath(FABRIC_TAG, "%s_ores")
            .langValue("Sand %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty)
            .registerOre(Blocks.SAND::defaultBlockState);

    public static final TagPrefix oreRedSand = new TagPrefix("redSand")
            .defaultTagPath(FORGE_TAG, "ores/%s")
            .defaultTagPath(FABRIC_TAG, "%s_ores")
            .langValue("Red Sand %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty)
            .registerOre(Blocks.RED_SAND::defaultBlockState);

    public static final TagPrefix oreNetherrack = new TagPrefix("netherrack")
            .prefixTagPath(FORGE_TAG, "%s_ores/%s")
            .prefixTagPath(FABRIC_TAG, "%s_%s_ores")
            .langValue("Nether %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty)
            .registerOre(Blocks.NETHERRACK::defaultBlockState);

    public static final TagPrefix oreEndstone = new TagPrefix("endstone")
            .prefixTagPath(FORGE_TAG, "%s_ores/%s")
            .prefixTagPath(FABRIC_TAG, "%s_%s_ores")
            .langValue("End %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty)
            .registerOre(Blocks.END_STONE::defaultBlockState);

    public static final TagPrefix crushedRefined = new TagPrefix("crushedRefined")
            .defaultTagPath(FORGE_TAG, "refined_ores/%s")
            .defaultTagPath(FABRIC_TAG, "%s_refined_ores")
            .langValue("Refined %s Ore")
            .materialIconType(MaterialIconType.crushedRefined)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasOreProperty);

    public static final TagPrefix crushedPurified = new TagPrefix("crushedPurified")
            .defaultTagPath(FORGE_TAG, "purified_ores/%s")
            .defaultTagPath(FABRIC_TAG, "%s_purified_ores")
            .langValue("Purified %s Ore")
            .materialIconType(MaterialIconType.crushedPurified)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasOreProperty);

    public static final TagPrefix crushed = new TagPrefix("crushed")
            .defaultTagPath(FORGE_TAG, "crushed_ores/%s")
            .defaultTagPath(FABRIC_TAG, "%s_crushed_ores")
            .langValue("Crushed %s Ore")
            .materialIconType(MaterialIconType.crushed)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasOreProperty)
            .tooltip((mat, tooltips) -> tooltips.add(Component.translatable("metaitem.crushed.tooltip.purify")));

    // A hot Ingot, which has to be cooled down by a Vacuum Freezer.
    public static final TagPrefix ingotHot = new TagPrefix("ingotHot")
            .defaultTagPath(FORGE_TAG, "ingots/hot/%s")
            .defaultTagPath(FABRIC_TAG, "%s_hot_ingots")
            .langValue("Hot %s Ingot")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.ingotHot)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasBlastProperty.and(mat -> mat.getProperty(PropertyKey.BLAST).getBlastTemperature() > 1750));

    // A regular Ingot.
    public static final TagPrefix ingot = new TagPrefix("ingot")
            .defaultTagPath(FORGE_TAG, "ingots/%s")
            .defaultTagPath(FABRIC_TAG, "%s_ingots")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.ingot)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasIngotProperty);

    // A regular Gem worth one Dust.
    public static final TagPrefix gem = new TagPrefix("gem")
            .defaultTagPath(FORGE_TAG, "gems/%s")
            .defaultTagPath(FABRIC_TAG, "%s_gems")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.gem)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasGemProperty);

    // A regular Gem worth one small Dust.
    public static final TagPrefix gemChipped = new TagPrefix("gemChipped")
            .defaultTagPath(FORGE_TAG, "gems/chipped/%s")
            .defaultTagPath(FABRIC_TAG, "%s_chipped_gems")
            .langValue("Chipped %s")
            .materialAmount(GTValues.M / 4)
            .materialIconType(MaterialIconType.gemChipped)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasGemProperty.and(unused -> ConfigHolder.recipes.generateLowQualityGems));

    // A regular Gem worth two small Dusts.
    public static final TagPrefix gemFlawed = new TagPrefix("gemFlawed")
            .defaultTagPath(FORGE_TAG, "gems/flawed/%s")
            .defaultTagPath(FABRIC_TAG, "%s_flawed_gems")
            .langValue("Flawed %s")
            .materialAmount(GTValues.M / 2)
            .materialIconType(MaterialIconType.gemFlawed)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasGemProperty.and(unused -> ConfigHolder.recipes.generateLowQualityGems));

    // A regular Gem worth two Dusts.
    public static final TagPrefix gemFlawless = new TagPrefix("gemFlawless")
            .defaultTagPath(FORGE_TAG, "gems/flawless/%s")
            .defaultTagPath(FABRIC_TAG, "%s_flawless_gems")
            .langValue("Flawless %s")
            .materialAmount(GTValues.M * 2)
            .maxStackSize(32)
            .materialIconType(MaterialIconType.gemFlawless)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasGemProperty);

    // A regular Gem worth four Dusts.
    public static final TagPrefix gemExquisite = new TagPrefix("gemExquisite")
            .defaultTagPath(FORGE_TAG, "gems/exquisite/%s")
            .defaultTagPath(FABRIC_TAG, "%s_exquisite_gems")
            .langValue("Exquisite %s")
            .materialAmount(GTValues.M * 4)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.gemExquisite)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasGemProperty);

    // 1/4th of a Dust.
    public static final TagPrefix dustSmall = new TagPrefix("dustSmall")
            .defaultTagPath(FORGE_TAG, "dusts/small/%s")
            .defaultTagPath(FABRIC_TAG, "%s_small_dusts")
            .langValue("Small Pile of %s Dust")
            .materialAmount(GTValues.M / 4)
            .materialIconType(MaterialIconType.dustSmall)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasDustProperty);

    // 1/9th of a Dust.
    public static final TagPrefix dustTiny = new TagPrefix("dustTiny")
            .defaultTagPath(FORGE_TAG, "dusts/tiny/%s")
            .defaultTagPath(FABRIC_TAG, "%s_tiny_dusts")
            .langValue("Tiny Pile of %s Dust")
            .materialAmount(GTValues.M / 9)
            .materialIconType(MaterialIconType.dustTiny)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasDustProperty);

    // Dust with impurities. 1 Unit of Main Material and 1/9 - 1/4 Unit of secondary Material
    public static final TagPrefix dustImpure = new TagPrefix("dustImpure")
            .defaultTagPath(FORGE_TAG, "dusts/impure/%s")
            .defaultTagPath(FABRIC_TAG, "%s_impure_dusts")
            .langValue("Impure Pile of %s Dust")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.dustImpure)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasOreProperty)
            .tooltip((mat, tooltips) -> tooltips.add(Component.translatable("metaitem.dust.tooltip.purify")));

    // Pure Dust worth of one Ingot or Gem.
    public static final TagPrefix dustPure = new TagPrefix("dustPure")
            .defaultTagPath(FORGE_TAG, "dusts/pure/%s")
            .defaultTagPath(FABRIC_TAG, "%s_pure_dusts")
            .langValue("Purified Pile of %s Dust")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.dustPure)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasOreProperty)
            .tooltip((mat, tooltips) -> tooltips.add(Component.translatable("metaitem.dust.tooltip.purify")));

    public static final TagPrefix dust = new TagPrefix("dust")
            .defaultTagPath(FORGE_TAG, "dusts/%s")
            .defaultTagPath(FABRIC_TAG, "%s_dusts")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.dust)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasDustProperty);

    // A Nugget.
    public static final TagPrefix nugget = new TagPrefix("nugget")
            .defaultTagPath(FORGE_TAG, "nuggets/%s")
            .defaultTagPath(FABRIC_TAG, "%s_nuggets")
            .materialAmount(GTValues.M / 9)
            .materialIconType(MaterialIconType.nugget)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasIngotProperty);

    // 9 Plates combined in one Item.
    public static final TagPrefix plateDense = new TagPrefix("plateDense")
            .defaultTagPath(FORGE_TAG, "plates/dense/%s")
            .defaultTagPath(FABRIC_TAG, "%s_dense_plates")
            .langValue("Dense %s Plate")
            .materialAmount(GTValues.M * 9)
            .maxStackSize(7)
            .materialIconType(MaterialIconType.plateDense)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_DENSE) && !mat.hasFlag(MaterialFlags.NO_SMASHING));

    // 2 Plates combined in one Item
    public static final TagPrefix plateDouble = new TagPrefix("plateDouble")
            .defaultTagPath(FORGE_TAG, "plates/double/%s")
            .defaultTagPath(FABRIC_TAG, "%s_double_plates")
            .langValue("Double %s Plate")
            .materialAmount(GTValues.M * 2)
            .maxStackSize(32)
            .materialIconType(MaterialIconType.plateDouble)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasIngotProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE) && !mat.hasFlag(MaterialFlags.NO_SMASHING)));

    // Regular Plate made of one Ingot/Dust.
    public static final TagPrefix plate = new TagPrefix("plate")
            .defaultTagPath(FORGE_TAG, "plates/%s")
            .defaultTagPath(FABRIC_TAG, "%s_plates")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.plate)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE));

    // Round made of 1 Nugget
    public static final TagPrefix round = new TagPrefix("round")
            .defaultTagPath(FORGE_TAG, "rounds/%s")
            .defaultTagPath(FABRIC_TAG, "%s_rounds")
            .materialAmount(GTValues.M / 9)
            .materialIconType(MaterialIconType.round)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_ROUND));

    // Foil made of 1/4 Ingot/Dust.
    public static final TagPrefix foil = new TagPrefix("foil")
            .defaultTagPath(FORGE_TAG, "foils/%s")
            .defaultTagPath(FABRIC_TAG, "%s_foils")
            .materialAmount(GTValues.M / 4)
            .materialIconType(MaterialIconType.foil)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_FOIL));

    // Stick made of an Ingot.
    public static final TagPrefix rodLong = new TagPrefix("longRod")
            .defaultTagPath(FORGE_TAG, "rods/long/%s")
            .defaultTagPath(FABRIC_TAG, "%s_long_rods")
            .langValue("Long %s Rod")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.stickLong)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_LONG_ROD));

    // Stick made of half an Ingot.
    public static final TagPrefix rod = new TagPrefix("rod")
            .defaultTagPath(FORGE_TAG, "rods/%s")
            .defaultTagPath(FABRIC_TAG, "%s_rods")
            .langValue("%s Rod")
            .materialAmount(GTValues.M / 2)
            .materialIconType(MaterialIconType.stick)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_ROD));

    // consisting out of 1/8 Ingot or 1/4 Stick.
    public static final TagPrefix bolt = new TagPrefix("bolt")
            .defaultTagPath(FORGE_TAG, "bolts/%s")
            .defaultTagPath(FABRIC_TAG, "%s_bolts")
            .materialAmount(GTValues.M / 8)
            .materialIconType(MaterialIconType.bolt)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_BOLT_SCREW));

    // consisting out of 1/9 Ingot.
    public static final TagPrefix screw = new TagPrefix("screw")
            .defaultTagPath(FORGE_TAG, "screws/%s")
            .defaultTagPath(FABRIC_TAG, "%s_screws")
            .materialAmount(GTValues.M / 9)
            .materialIconType(MaterialIconType.screw)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_BOLT_SCREW));

    // consisting out of 1/2 Stick.
    public static final TagPrefix ring = new TagPrefix("ring")
            .defaultTagPath(FORGE_TAG, "rings/%s")
            .defaultTagPath(FABRIC_TAG, "%s_rings")
            .materialAmount(GTValues.M / 4)
            .materialIconType(MaterialIconType.ring)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_RING));

    // consisting out of 1 Fine Wire.
    public static final TagPrefix springSmall = new TagPrefix("springSmall")
            .defaultTagPath(FORGE_TAG, "springs/small/%s")
            .defaultTagPath(FABRIC_TAG, "%s_small_springs")
            .langValue("Small %s Spring")
            .materialAmount(GTValues.M / 4)
            .materialIconType(MaterialIconType.springSmall)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_SPRING_SMALL) && !mat.hasFlag(MaterialFlags.NO_SMASHING));

    // consisting out of 2 Sticks.
    public static final TagPrefix spring = new TagPrefix("spring")
            .defaultTagPath(FORGE_TAG, "springs/%s")
            .defaultTagPath(FABRIC_TAG, "%s_springs")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.spring)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_SPRING) && !mat.hasFlag(MaterialFlags.NO_SMASHING));

    // consisting out of 1/8 Ingot or 1/4 Wire.
    public static final TagPrefix wireFine = new TagPrefix("wireFine")
            .defaultTagPath(FORGE_TAG, "wires/fine/%s")
            .defaultTagPath(FABRIC_TAG, "%s_fine_wires")
            .langValue("Fine %s Wire")
            .materialAmount(GTValues.M / 8)
            .materialIconType(MaterialIconType.wireFine)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_FINE_WIRE));

    // consisting out of 4 Plates, 1 Ring and 1 Screw.
    public static final TagPrefix rotor = new TagPrefix("rotor")
            .defaultTagPath(FORGE_TAG, "rotors/%s")
            .defaultTagPath(FABRIC_TAG, "%s_rotors")
            .materialAmount(GTValues.M * 4)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.rotor)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_ROTOR));

    // Consisting of 1 Plate.
    public static final TagPrefix gearSmall = new TagPrefix("gearSmall")
            .defaultTagPath(FORGE_TAG, "gears/small/%s")
            .defaultTagPath(FABRIC_TAG, "%s_small_gears")
            .langValue("Small %s Gear")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.gearSmall)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_SMALL_GEAR));

    // Consisting of 4 Plates.
    public static final TagPrefix gear = new TagPrefix("gear")
            .defaultTagPath(FORGE_TAG, "gears/%s")
            .defaultTagPath(FABRIC_TAG, "%s_gears")
            .materialAmount(GTValues.M * 4)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.gear)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_GEAR));

    // 3/4 of a Plate or Gem used to shape a Lens. Normally only used on Transparent Materials.
    public static final TagPrefix lens = new TagPrefix("lens")
            .defaultTagPath(FORGE_TAG, "lenses/%s")
            .defaultTagPath(FABRIC_TAG, "%s_lenses")
            .materialAmount((GTValues.M * 3) / 4)
            .materialIconType(MaterialIconType.lens)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_LENS));

    // made of 4 Ingots.
    public static final TagPrefix toolHeadBuzzSaw = new TagPrefix("toolHeadBuzzSaw")
            .defaultTagPath(FORGE_TAG, "tool_heads/buzzsaw/%s")
            .defaultTagPath(FABRIC_TAG, "%s_buzzsaw_tool_heads")
            .langValue("%s Buzzsaw Blade")
            .materialAmount(GTValues.M * 4)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.toolHeadBuzzSaw)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasNoCraftingToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)));

    // made of 1 Ingots.
    public static final TagPrefix toolHeadScrewdriver = new TagPrefix("toolHeadScrewdriver")
            .defaultTagPath(FORGE_TAG, "tool_heads/screwdriver/%s")
            .defaultTagPath(FABRIC_TAG, "%s_screwdriver_tool_heads")
            .langValue("%s Screwdriver Tip")
            .materialAmount(GTValues.M)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.toolHeadScrewdriver)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasNoCraftingToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_LONG_ROD)));

    // made of 4 Ingots.
    public static final TagPrefix toolHeadDrill = new TagPrefix("toolHeadDrill")
            .defaultTagPath(FORGE_TAG, "tool_heads/drill/%s")
            .defaultTagPath(FABRIC_TAG, "%s_drill_tool_heads")
            .langValue("%s Drill Tip")
            .materialAmount(GTValues.M * 4)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.toolHeadDrill)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)));

    // made of 2 Ingots.
    public static final TagPrefix toolHeadChainsaw = new TagPrefix("toolHeadChainsaw")
            .defaultTagPath(FORGE_TAG, "tool_heads/chainsaw/%s")
            .defaultTagPath(FABRIC_TAG, "%s_chainsaw_tool_heads")
            .langValue("%s Chainsaw Tip")
            .materialAmount(GTValues.M * 2)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.toolHeadChainsaw)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasNoCraftingToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)));

    // made of 4 Ingots.
    public static final TagPrefix toolHeadWrench = new TagPrefix("toolHeadWrench")
            .defaultTagPath(FORGE_TAG, "tool_heads/wrench/%s")
            .defaultTagPath(FABRIC_TAG, "%s_wrench_tool_heads")
            .langValue("%s Wrench Tip")
            .materialAmount(GTValues.M * 4)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.toolHeadWrench)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasNoCraftingToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)));

    // test of 'tag prefix' without any tags
    // made of 5 Ingots.
    public static final TagPrefix turbineBlade = new TagPrefix("turbineBlade")
            .langValue("%s Turbine Blade")
            .materialAmount(GTValues.M * 10)
            .materialIconType(MaterialIconType.turbineBlade)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasRotorProperty.and(m -> m.hasFlags(MaterialFlags.GENERATE_BOLT_SCREW, MaterialFlags.GENERATE_PLATE) && !m.hasProperty(PropertyKey.GEM)));

    // Storage Block consisting out of 9 Ingots/Gems/Dusts.
    public static final TagPrefix block = new TagPrefix("block")
            .defaultTagPath(FORGE_TAG, "storage_blocks/%s")
            .defaultTagPath(FABRIC_TAG, "%s_blocks")
            .langValue("Block of %s")
            .materialAmount(GTValues.M * 9)
            .materialIconType(MaterialIconType.block)
            .unificationEnabled(true);

    public static final TagPrefix frameGt = new TagPrefix("frameGt")
            .defaultTagPath(FORGE_TAG, "frames/%s")
            .defaultTagPath(FABRIC_TAG, "%s_frames")
            .langValue("%s Frame Box")
            .materialAmount(GTValues.M * 2)
            .materialIconType(MaterialIconType.frameGt)
            .unificationEnabled(true)
            .generationCondition(material -> material.hasFlag(MaterialFlags.GENERATE_FRAME));

    // Pipes
    public static final TagPrefix pipeTinyFluid = new TagPrefix("pipeTinyFluid")
            .defaultTagPath(FORGE_TAG, "fluid_pipes/tiny/%s")
            .defaultTagPath(FABRIC_TAG, "%s_tiny_fluid_pipes")
            .langValue("Tiny %s Fluid Pipe")
            .materialAmount(GTValues.M / 2)
            .unificationEnabled(true);

    public static final TagPrefix pipeSmallFluid = new TagPrefix("pipeSmallFluid")
            .defaultTagPath(FORGE_TAG, "fluid_pipes/small/%s")
            .defaultTagPath(FABRIC_TAG, "%s_small_fluid_pipes")
            .langValue("Small %s Fluid Pipe")
            .materialAmount(GTValues.M)
            .unificationEnabled(true);

    public static final TagPrefix pipeNormalFluid = new TagPrefix("pipeNormalFluid")
            .defaultTagPath(FORGE_TAG, "fluid_pipes/normal/%s")
            .defaultTagPath(FABRIC_TAG, "%s_normal_fluid_pipes")
            .langValue("Normal %s Fluid Pipe")
            .materialAmount(GTValues.M * 3)
            .unificationEnabled(true);

    public static final TagPrefix pipeLargeFluid = new TagPrefix("pipeLargeFluid")
            .defaultTagPath(FORGE_TAG, "fluid_pipes/large/%s")
            .defaultTagPath(FABRIC_TAG, "%s_large_fluid_pipes")
            .langValue("Large %s Fluid Pipe")
            .materialAmount(GTValues.M * 6)
            .unificationEnabled(true);

    public static final TagPrefix pipeHugeFluid = new TagPrefix("pipeHugeFluid")
            .defaultTagPath(FORGE_TAG, "fluid_pipes/huge/%s")
            .defaultTagPath(FABRIC_TAG, "%s_huge_fluid_pipes")
            .langValue("Huge %s Fluid Pipe")
            .materialAmount(GTValues.M * 12)
            .unificationEnabled(true);

    public static final TagPrefix pipeQuadrupleFluid = new TagPrefix("pipeQuadrupleFluid")
            .defaultTagPath(FORGE_TAG, "fluid_pipes/quadruple/%s")
            .defaultTagPath(FABRIC_TAG, "%s_quadruple_fluid_pipes")
            .langValue("Quadruple %s Fluid Pipe")
            .materialAmount(GTValues.M * 4)
            .unificationEnabled(true);

    public static final TagPrefix pipeNonupleFluid = new TagPrefix("pipeNonupleFluid")
            .defaultTagPath(FORGE_TAG, "fluid_pipes/nonuple/%s")
            .defaultTagPath(FABRIC_TAG, "%s_nonuple_fluid_pipes")
            .langValue("Nonuple %s Fluid Pipe")
            .materialAmount(GTValues.M * 9)
            .unificationEnabled(true);

    public static final TagPrefix pipeTinyItem = new TagPrefix("pipeTinyItem")
            .defaultTagPath(FORGE_TAG, "item_pipes/tiny/%s")
            .defaultTagPath(FABRIC_TAG, "%s_tiny_item_pipes")
            .langValue("Tiny %s Item Pipe")
            .materialAmount(GTValues.M / 2)
            .unificationEnabled(true);

    public static final TagPrefix pipeSmallItem = new TagPrefix("pipeSmallItem")
            .defaultTagPath(FORGE_TAG, "item_pipes/small/%s")
            .defaultTagPath(FABRIC_TAG, "%s_small_item_pipes")
            .langValue("Small %s Item Pipe")
            .materialAmount(GTValues.M)
            .unificationEnabled(true);

    public static final TagPrefix pipeNormalItem = new TagPrefix("pipeNormalItem")
            .defaultTagPath(FORGE_TAG, "item_pipes/normal/%s")
            .defaultTagPath(FABRIC_TAG, "%s_normal_item_pipes")
            .langValue("Normal %s Item Pipe")
            .materialAmount(GTValues.M * 3)
            .unificationEnabled(true);

    public static final TagPrefix pipeLargeItem = new TagPrefix("pipeLargeItem")
            .defaultTagPath(FORGE_TAG, "item_pipes/large/%s")
            .defaultTagPath(FABRIC_TAG, "%s_large_item_pipes")
            .langValue("Large %s Item Pipe")
            .materialAmount(GTValues.M * 6)
            .unificationEnabled(true);

    public static final TagPrefix pipeHugeItem = new TagPrefix("pipeHugeItem")
            .defaultTagPath(FORGE_TAG, "item_pipes/huge/%s")
            .defaultTagPath(FABRIC_TAG, "%s_huge_item_pipes")
            .langValue("Huge %s Item Pipe")
            .materialAmount(GTValues.M * 12)
            .unificationEnabled(true);

    public static final TagPrefix pipeSmallRestrictive = new TagPrefix("pipeSmallRestrictive")
            .defaultTagPath(FORGE_TAG, "item_pipes/small_restrictive/%s")
            .defaultTagPath(FABRIC_TAG, "%s_small_restrictive_item_pipes")
            .langValue("Small Restrictive %s Item Pipe")
            .materialAmount(GTValues.M)
            .unificationEnabled(true);

    public static final TagPrefix pipeNormalRestrictive = new TagPrefix("pipeNormalRestrictive")
            .defaultTagPath(FORGE_TAG, "item_pipes/normal_restrictive/%s")
            .defaultTagPath(FABRIC_TAG, "%s_normal_restrictive_item_pipes")
            .langValue("Normal Restrictive %s Item Pipe")
            .materialAmount(GTValues.M * 3)
            .unificationEnabled(true);

    public static final TagPrefix pipeLargeRestrictive = new TagPrefix("pipeLargeRestrictive")
            .defaultTagPath(FORGE_TAG, "item_pipes/large_restrictive/%s")
            .defaultTagPath(FABRIC_TAG, "%s_large_restrictive_item_pipes")
            .langValue("Large Restrictive %s Item Pipe")
            .materialAmount(GTValues.M * 6)
            .unificationEnabled(true);

    public static final TagPrefix pipeHugeRestrictive = new TagPrefix("pipeHugeRestrictive")
            .defaultTagPath(FORGE_TAG, "item_pipes/huge_restrictive/%s")
            .defaultTagPath(FABRIC_TAG, "%s_huge_restrictive_item_pipes")
            .langValue("Huge Restrictive %s Item Pipe")
            .materialAmount(GTValues.M * 12)
            .unificationEnabled(true);

    // Wires and cables
    public static final TagPrefix wireGtHex = new TagPrefix("wireGtHex")
            .defaultTagPath(FORGE_TAG, "wires/hex/%s")
            .defaultTagPath(FABRIC_TAG, "%s_hex_wires")
            .langValue("16x %s Wire")
            .materialAmount(GTValues.M * 8)
            .unificationEnabled(true);

    public static final TagPrefix wireGtOctal = new TagPrefix("wireGtOctal")
            .defaultTagPath(FORGE_TAG, "wires/octal/%s")
            .defaultTagPath(FABRIC_TAG, "%s_octal_wires")
            .langValue("8x %s Wire")
            .materialAmount(GTValues.M * 4)
            .unificationEnabled(true);

    public static final TagPrefix wireGtQuadruple = new TagPrefix("wireGtQuadruple")
            .defaultTagPath(FORGE_TAG, "wires/quadruple/%s")
            .defaultTagPath(FABRIC_TAG, "%s_quadruple_wires")
            .langValue("4x %s Wire")
            .materialAmount(GTValues.M * 2)
            .unificationEnabled(true);

    public static final TagPrefix wireGtDouble = new TagPrefix("wireGtDouble")
            .defaultTagPath(FORGE_TAG, "wires/double/%s")
            .defaultTagPath(FABRIC_TAG, "%s_double_wires")
            .langValue("2x %s Wire")
            .materialAmount(GTValues.M)
            .unificationEnabled(true);

    public static final TagPrefix wireGtSingle = new TagPrefix("wireGtSingle")
            .defaultTagPath(FORGE_TAG, "wires/single/%s")
            .defaultTagPath(FABRIC_TAG, "%s_single_wires")
            .langValue("1x %s Wire")
            .materialAmount(GTValues.M / 2)
            .unificationEnabled(true);

    public static final TagPrefix cableGtHex = new TagPrefix("cableGtHex")
            .defaultTagPath(FORGE_TAG, "cables/hex/%s")
            .defaultTagPath(FABRIC_TAG, "%s_hex_cables")
            .langValue("16x %s Cable")
            .materialAmount(GTValues.M * 8)
            .unificationEnabled(true);

    public static final TagPrefix cableGtOctal = new TagPrefix("cableGtOctal")
            .defaultTagPath(FORGE_TAG, "cables/octal/%s")
            .defaultTagPath(FABRIC_TAG, "%s_octal_cables")
            .langValue("8x %s Cable")
            .materialAmount(GTValues.M * 4)
            .unificationEnabled(true);

    public static final TagPrefix cableGtQuadruple = new TagPrefix("cableGtQuadruple")
            .defaultTagPath(FORGE_TAG, "cables/quadruple/%s")
            .defaultTagPath(FABRIC_TAG, "%s_quadruple_cables")
            .langValue("4x %s Cable")
            .materialAmount(GTValues.M * 2)
            .unificationEnabled(true);

    public static final TagPrefix cableGtDouble = new TagPrefix("cableGtDouble")
            .defaultTagPath(FORGE_TAG, "cables/double/%s")
            .defaultTagPath(FABRIC_TAG, "%s_double_cables")
            .langValue("2x %s Cable")
            .materialAmount(GTValues.M)
            .unificationEnabled(true);

    public static final TagPrefix cableGtSingle = new TagPrefix("cableGtSingle")
            .defaultTagPath(FORGE_TAG, "cables/single/%s")
            .defaultTagPath(FABRIC_TAG, "%s_single_cables")
            .langValue("1x %s Cable")
            .materialAmount(GTValues.M / 2)
            .unificationEnabled(true);


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
    private final Map<Integer, TagType> tagPaths = new HashMap<>();
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

    protected TagPrefix defaultTagPath(int loader, String path) {
        return tagPath(loader, TagType.withDefaultFormatter(path));
    }

    protected TagPrefix prefixTagPath(int loader, String path) {
        return tagPath(loader, TagType.withPrefixFormatter(path));
    }

    protected TagPrefix customTagPath(int loader, String path, BiFunction<TagPrefix, Material, TagKey<Item>> formatter) {
        return tagPath(loader, TagType.withCustomFormatter(path, formatter));
    }

    private TagPrefix tagPath(int loader, TagType tagType) {
        tagPaths.put(loader, tagType);
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
        return (TagKey<Item>[]) tagPaths.entrySet().stream()
                .filter(e -> Platform.isForge() ? e.getKey() == FORGE_TAG : e.getKey() == FABRIC_TAG)
                .map(Map.Entry::getValue)
                .map(type -> type.getTag(this, mat))
                .toArray(TagKey[]::new);
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
