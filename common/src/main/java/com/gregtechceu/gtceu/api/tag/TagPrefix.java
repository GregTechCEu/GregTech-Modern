package com.gregtechceu.gtceu.api.tag;

import com.google.common.base.Preconditions;
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
import org.apache.logging.log4j.util.TriConsumer;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static com.gregtechceu.gtceu.api.tag.TagPrefix.Conditions.*;

@Accessors(chain = true, fluent = true)
public class TagPrefix {
    private final static Map<String, TagPrefix> PREFIXES = new HashMap<>();

    // Regular Ore Prefix. Ore -> Material is a Oneway Operation! Introduced by Eloraam
    public static final TagPrefix ore = new TagPrefix("ore","ores")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty);
    public static final TagPrefix oreGranite = new TagPrefix("oreGranite", "ores", "ores/granite")
            .langValue("Granite %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty);
    public static final TagPrefix oreDiorite = new TagPrefix("oreDiorite", "ores", "ores/diorite")
            .langValue("Diorite %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty);
    public static final TagPrefix oreAndesite = new TagPrefix("oreAndesite", "ores", "ores/andesite")
            .langValue("Andesite %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty);
    public static final TagPrefix oreBlackgranite = new TagPrefix("oreBlackgranite", "ores", "ores/black_granite")
            .langValue("Granite %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty);
    public static final TagPrefix oreRedgranite = new TagPrefix("oreRedgranite", "ores", "ores/red_granite")
            .langValue("Granite %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty);
    public static final TagPrefix oreMarble = new TagPrefix("oreMarble", "ores", "ores/marble")
            .langValue("Marble %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty);
    public static final TagPrefix oreBasalt = new TagPrefix("oreBasalt", "ores", "ores/basalt")
            .langValue("Basalt %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty);
    // In case of an Sand-Ores Mod. Ore -> Material is a Oneway Operation!
    public static final TagPrefix oreSand = new TagPrefix("oreSand", "ores", "ores/sand")
            .langValue("Sand %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty);
    public static final TagPrefix oreRedSand = new TagPrefix("oreRedSand", "ores", "ores/red_sand")
            .langValue("Red Sand %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty);

    // Prefix of the Nether-Ores Mod. Causes Ores to double. Ore -> Material is a Oneway Operation!
    public static final TagPrefix oreNetherrack = new TagPrefix("oreNetherrack", "ores", "ores/netherrack")
            .langValue("Nether %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty);
    // In case of an End-Ores Mod. Ore -> Material is a Oneway Operation!
    public static final TagPrefix oreEndstone = new TagPrefix("oreEndstone", "ores", "ores/endstone")
            .langValue("End %s Ore")
            .materialIconType(MaterialIconType.ore)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty);

    public static final TagPrefix crushedCentrifuged = new TagPrefix("crushedCentrifuged","centrifuged_crushed")
            .langValue("Centrifuged %s Ore")
            .materialIconType(MaterialIconType.crushedCentrifuged)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasOreProperty);
    public static final TagPrefix crushedPurified = new TagPrefix("crushedPurified","crushed_purified")
            .langValue("Purified %s Ore")
            .materialIconType(MaterialIconType.crushedPurified)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasOreProperty);
    public static final TagPrefix crushed = new TagPrefix("crushed")
            .langValue("Crushed %s Ore")
            .materialIconType(MaterialIconType.crushed)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasOreProperty)
            .tooltip((mat, tooltips) -> tooltips.add(Component.translatable("metaitem.crushed.tooltip.purify")));

    // Introduced by Mekanism
    public static final TagPrefix shard = new TagPrefix("shard","shards").unificationEnabled(true);
    public static final TagPrefix clump = new TagPrefix("clump","clumps").unificationEnabled(true);
    public static final TagPrefix reduced = new TagPrefix("reduced").unificationEnabled(true);
    public static final TagPrefix crystalline = new TagPrefix("crystalline").unificationEnabled(true);
    public static final TagPrefix cleanGravel = new TagPrefix("cleanGravel").unificationEnabled(true);
    public static final TagPrefix dirtyGravel = new TagPrefix("dirtyGravel").unificationEnabled(true);

    // A hot Ingot, which has to be cooled down by a Vacuum Freezer.
    public static final TagPrefix ingotHot = new TagPrefix("ingotHot","ingots/hot")
            .langValue("Hot %s Ingot")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.ingotHot)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasBlastProperty.and(mat -> mat.getProperty(PropertyKey.BLAST).getBlastTemperature() > 1750));

    // A regular Ingot. Introduced by Eloraam
    public static final TagPrefix ingot = new TagPrefix("ingot","ingots")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.ingot)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasIngotProperty);

    // A regular Gem worth one Dust. Introduced by Eloraam
    public static final TagPrefix gem = new TagPrefix("gem","gems")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.gem)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasGemProperty);

    // A regular Gem worth one small Dust. Introduced by TerraFirmaCraft
    public static final TagPrefix gemChipped = new TagPrefix("gemChipped", "gems/chipped")
            .langValue("Chipped %s")
            .materialAmount(GTValues.M / 4)
            .materialIconType(MaterialIconType.gemChipped)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasGemProperty.and(unused -> ConfigHolder.recipes.generateLowQualityGems));

    // A regular Gem worth two small Dusts. Introduced by TerraFirmaCraft
    public static final TagPrefix gemFlawed = new TagPrefix("gemFlawed","gems/flawed")
            .langValue("Flawed %s")
            .materialAmount(GTValues.M / 2)
            .materialIconType(MaterialIconType.gemFlawed)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasGemProperty.and(unused -> ConfigHolder.recipes.generateLowQualityGems));

    // A regular Gem worth two Dusts. Introduced by TerraFirmaCraft
    public static final TagPrefix gemFlawless = new TagPrefix("gemFlawless", "gems/flawless")
            .langValue("Flawless %s")
            .materialAmount(GTValues.M * 2)
            .maxStackSize(32)
            .materialIconType(MaterialIconType.gemFlawless)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasGemProperty);

    // A regular Gem worth four Dusts. Introduced by TerraFirmaCraft
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
    public static final TagPrefix dustTiny = new TagPrefix("dustTiny","dusts/tiny")
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

    // Pure Dust worth of one Ingot or Gem. Introduced by Alblaka.
    public static final TagPrefix dustPure = new TagPrefix("dustPure", "dusts/pure")
            .langValue("Purified Pile of %s Dust")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.dustPure)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasOreProperty)
            .tooltip((mat, tooltips) -> tooltips.add(Component.translatable("metaitem.dust.tooltip.purify")));

    public static final TagPrefix dust = new TagPrefix("dust","dusts")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.dust)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasDustProperty);

    // A Nugget. Introduced by Eloraam
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

    // Regular Plate made of one Ingot/Dust. Introduced by Calclavia
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

    // Stick made of half an Ingot. Introduced by Eloraam
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

    public static final TagPrefix gearSmall = new TagPrefix("gearSmall", "gears/small")
            .langValue("Small %s Gear")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.gearSmall)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_SMALL_GEAR));

    // Introduced by me because BuildCraft has ruined the gear Prefix...
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
    public static final TagPrefix toolHeadBuzzSaw = new TagPrefix("toolHeadBuzzSaw")
            .langValue("%s Buzzsaw Blade")
            .materialAmount(GTValues.M * 4)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.toolHeadBuzzSaw)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasNoCraftingToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)));

    // made of 1 Ingots.
    public static final TagPrefix toolHeadScrewdriver = new TagPrefix("toolHeadScrewdriver")
            .langValue("%s Screwdriver Tip")
            .materialAmount(GTValues.M)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.toolHeadScrewdriver)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasNoCraftingToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_LONG_ROD)));

    // made of 4 Ingots.
    public static final TagPrefix toolHeadDrill = new TagPrefix("toolHeadDrill")
            .langValue("%s Drill Tip")
            .materialAmount(GTValues.M * 4)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.toolHeadDrill)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)));

    // made of 2 Ingots.
    public static final TagPrefix toolHeadChainsaw = new TagPrefix("toolHeadChainsaw")
            .langValue("%s Chainsaw Tip")
            .materialAmount(GTValues.M * 2)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.toolHeadChainsaw)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasNoCraftingToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)));

    // made of 4 Ingots.
    public static final TagPrefix toolHeadWrench = new TagPrefix("toolHeadWrench")
            .langValue("%s Wrench Tip")
            .materialAmount(GTValues.M * 4)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.toolHeadWrench)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasNoCraftingToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)));

    // made of 5 Ingots.
    public static final TagPrefix turbineBlade = new TagPrefix("turbineBlade")
            .langValue("%s Turbine Blade")
            .materialAmount(GTValues.M * 10)
            .materialIconType(MaterialIconType.turbineBlade)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasRotorProperty.and(m -> m.hasFlags(MaterialFlags.GENERATE_BOLT_SCREW, MaterialFlags.GENERATE_PLATE) && !m.hasProperty(PropertyKey.GEM)));

    public static final TagPrefix paneGlass = new TagPrefix("paneGlass", "glass_panes")
            .langValue("%s Glass Pane")
            .materialType(MarkerMaterials.Color.Colorless)
            .selfReferencing(true);

    public static final TagPrefix blockGlass = new TagPrefix("blockGlass", "glass")
            .langValue("%s Glass")
            .materialType(MarkerMaterials.Color.Colorless)
            .selfReferencing(true);

    // Storage Block consisting out of 9 Ingots/Gems/Dusts. Introduced by CovertJaguar
    public static final TagPrefix block = new TagPrefix("block", "storage_blocks")
            .langValue("Block of %s")
            .materialAmount(GTValues.M * 9)
            .materialIconType(MaterialIconType.block)
            .unificationEnabled(true);

    // Prefix used for Logs. Usually as "logWood". Introduced by Eloraam
    public static final TagPrefix log = new TagPrefix("log","logs").vanillaTag(true);

    // Prefix for Planks. Usually "plankWood". Introduced by Eloraam
    public static final TagPrefix plank = new TagPrefix("plank","planks").vanillaTag(true);

    // Prefix to determine which kind of Rock this is.
    public static final TagPrefix stone = new TagPrefix("stone")
            .materialType(GTMaterials.Stone)
            .selfReferencing(true);

    public static final TagPrefix frameGt = new TagPrefix("frameGt", "frames")
            .langValue("%s Frame Box")
            .materialAmount(GTValues.M * 2)
            .materialIconType(MaterialIconType.frameGt)
            .unificationEnabled(true)
            .generationCondition(material -> material.hasFlag(MaterialFlags.GENERATE_FRAME));

    public static final TagPrefix pipeTinyFluid = new TagPrefix("pipeTinyFluid").langValue("Tiny %s Fluid Pipe").materialAmount(GTValues.M / 2).unificationEnabled(true);
    public static final TagPrefix pipeSmallFluid = new TagPrefix("pipeSmallFluid").langValue("Small %s Fluid Pipe").materialAmount(GTValues.M).unificationEnabled(true);
    public static final TagPrefix pipeNormalFluid = new TagPrefix("pipeNormalFluid").langValue("Normal %s Fluid Pipe").materialAmount(GTValues.M * 3).unificationEnabled(true);
    public static final TagPrefix pipeLargeFluid = new TagPrefix("pipeLargeFluid").langValue("Large %s Fluid Pipe").materialAmount(GTValues.M * 6).unificationEnabled(true);
    public static final TagPrefix pipeHugeFluid = new TagPrefix("pipeHugeFluid").langValue("Huge %s Fluid Pipe").materialAmount(GTValues.M * 12).unificationEnabled(true);
    public static final TagPrefix pipeQuadrupleFluid = new TagPrefix("pipeQuadrupleFluid").langValue("Quadruple %s Fluid Pipe").materialAmount(GTValues.M * 4).unificationEnabled(true);
    public static final TagPrefix pipeNonupleFluid = new TagPrefix("pipeNonupleFluid").langValue("Nonuple %s Fluid Pipe").materialAmount(GTValues.M * 9).unificationEnabled(true);

    public static final TagPrefix pipeTinyItem = new TagPrefix("pipeTinyItem").langValue("Tiny %s Item Pipe").materialAmount(GTValues.M / 2).unificationEnabled(true);
    public static final TagPrefix pipeSmallItem = new TagPrefix("pipeSmallItem").langValue("Small %s Item Pipe").materialAmount(GTValues.M).unificationEnabled(true);
    public static final TagPrefix pipeNormalItem = new TagPrefix("pipeNormalItem").langValue("Normal %s Item Pipe").materialAmount(GTValues.M * 3).unificationEnabled(true);
    public static final TagPrefix pipeLargeItem = new TagPrefix("pipeLargeItem").langValue("Large %s Item Pipe").materialAmount(GTValues.M * 6).unificationEnabled(true);
    public static final TagPrefix pipeHugeItem = new TagPrefix("pipeHugeItem").langValue("Huge %s Item Pipe").materialAmount(GTValues.M * 12).unificationEnabled(true);

    public static final TagPrefix pipeSmallRestrictive = new TagPrefix("pipeSmallRestrictive").langValue("Small Restrictive %s Item Pipe").materialAmount(GTValues.M).unificationEnabled(true);
    public static final TagPrefix pipeNormalRestrictive = new TagPrefix("pipeNormalRestrictive").langValue("Normal Restrictive %s Item Pipe").materialAmount(GTValues.M * 3).unificationEnabled(true);
    public static final TagPrefix pipeLargeRestrictive = new TagPrefix("pipeLargeRestrictive").langValue("Large Restrictive %s Item Pipe").materialAmount(GTValues.M * 6).unificationEnabled(true);
    public static final TagPrefix pipeHugeRestrictive = new TagPrefix("pipeHugeRestrictive").langValue("Huge Restrictive %s Item Pipe").materialAmount(GTValues.M * 12).unificationEnabled(true);

    public static final TagPrefix wireGtHex = new TagPrefix("wireGtHex").langValue("16x %s Wire").materialAmount(GTValues.M * 8).unificationEnabled(true);
    public static final TagPrefix wireGtOctal = new TagPrefix("wireGtOctal").langValue("8x %s Wire").materialAmount(GTValues.M * 4).unificationEnabled(true);
    public static final TagPrefix wireGtQuadruple = new TagPrefix("wireGtQuadruple").langValue("4x %s Wire").materialAmount(GTValues.M * 2).unificationEnabled(true);
    public static final TagPrefix wireGtDouble = new TagPrefix("wireGtDouble").langValue("2x %s Wire").materialAmount(GTValues.M).unificationEnabled(true);
    public static final TagPrefix wireGtSingle = new TagPrefix("wireGtSingle").langValue("1x %s Wire").materialAmount(GTValues.M / 2).unificationEnabled(true);

    public static final TagPrefix cableGtHex = new TagPrefix("cableGtHex").langValue("16x %s Cable").materialAmount(GTValues.M * 8).unificationEnabled(true);
    public static final TagPrefix cableGtOctal = new TagPrefix("cableGtOctal").langValue("8x %s Cable").materialAmount(GTValues.M * 4).unificationEnabled(true);
    public static final TagPrefix cableGtQuadruple = new TagPrefix("cableGtQuadruple").langValue("4x %s Cable").materialAmount(GTValues.M * 2).unificationEnabled(true);
    public static final TagPrefix cableGtDouble = new TagPrefix("cableGtDouble").langValue("2x %s Cable").materialAmount(GTValues.M).unificationEnabled(true);
    public static final TagPrefix cableGtSingle = new TagPrefix("cableGtSingle").langValue("1x %s Cable").materialAmount(GTValues.M / 2).unificationEnabled(true);

    // Special Prefix used mainly for the Crafting Handler.
    public static final TagPrefix craftingLens = new TagPrefix("craftingLens").langValue("Crafting %s Lens").isMarkerPrefix(true);;
    // Used for the 16 dyes. Introduced by Eloraam
    public static final TagPrefix dye = new TagPrefix("dye","dyes").isMarkerPrefix(true);

    /**
     * Electric Components.
     *
     * @see MarkerMaterials.Tier
     */
    // Introduced by Calclavia
    public static final TagPrefix battery = new TagPrefix("battery").isMarkerPrefix(true);
    // Introduced by Calclavia
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

    public long getMaterialAmount(@Nullable Material material) {

        if(material == null) {
            return this.materialAmount;
        }

        if (this == block) {
            //glowstone and nether quartz blocks use 4 gems (dusts)
            if (material == GTMaterials.Glowstone ||
                    material == GTMaterials.NetherQuartz ||
                    material == GTMaterials.Brick ||
                    material == GTMaterials.Clay)
                return GTValues.M * 4;
                //glass, ice and obsidian gain only one dust
            else if (material == GTMaterials.Glass ||
                    material == GTMaterials.Ice ||
                    material == GTMaterials.Obsidian ||
                    material == GTMaterials.Concrete)
                return GTValues.M;
        } else if (this == stick) {
            if (material == GTMaterials.Blaze)
                return GTValues.M * 4;
            else if (material == GTMaterials.Bone)
                return GTValues.M * 5;
        }

        return materialAmount;
    }

    public static TagPrefix getPrefix(String prefixName) {
        return getPrefix(prefixName, null);
    }

    public static TagPrefix getPrefix(String prefixName, @Nullable TagPrefix replacement) {
        return PREFIXES.getOrDefault(prefixName, replacement);
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
        var prefix = LocalizationUtils.format(getUnlocalizedName());
        var mat = LocalizationUtils.format(material.getUnlocalizedName());
        return prefix.formatted(mat);
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
