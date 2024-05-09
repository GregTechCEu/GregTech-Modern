package com.gregtechceu.gtceu.api.tag;

import com.google.common.base.Preconditions;
import com.google.common.collect.Table;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.material.ChemicalHelper;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.material.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.material.material.properties.IMaterialProperty;
import com.gregtechceu.gtceu.api.material.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.material.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.data.block.GTBlocks;
import com.gregtechceu.gtceu.data.item.GTItems;
import com.gregtechceu.gtceu.data.material.GTMaterials;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.integration.GTOreByProduct;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.SupplierMemoizer;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.function.*;

import static com.gregtechceu.gtceu.api.tag.TagPrefix.Conditions.*;

@SuppressWarnings("unused")
@Accessors(chain = true, fluent = true)
public class TagPrefix {

    public final static Map<String, TagPrefix> PREFIXES = new HashMap<>();
    public static final Map<TagPrefix, OreType> ORES = new Object2ObjectLinkedOpenHashMap<>();

    public static final Codec<TagPrefix> CODEC = Codec.STRING.flatXmap(str -> Optional.ofNullable(get(str)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "invalid TagPrefix: " + str)), prefix -> DataResult.success(prefix.name));

    public static void init() {
        AddonFinder.getAddons().forEach(IGTAddon::registerTagPrefixes);
        if (GTCEu.isKubeJSLoaded()) {
            GTRegistryInfo.registerFor(GTRegistryInfo.TAG_PREFIX.registryKey);
        }
    }

    public static TagPrefix get(String name) {
        return PREFIXES.get(name);
    }

    public static final TagPrefix ore = oreTagPrefix("stone", BlockTags.MINEABLE_WITH_PICKAXE)
            .langValue("%s Ore")
            .registerOre(Blocks.STONE::defaultBlockState, () -> GTMaterials.Stone, BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(3.0F, 3.0F), new ResourceLocation("block/stone"), false, false, true);

    public static final TagPrefix oreGranite = oreTagPrefix("granite", BlockTags.MINEABLE_WITH_PICKAXE)
            .langValue("Granite %s Ore")
            .registerOre(Blocks.GRANITE::defaultBlockState, () -> GTMaterials.Granite, BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).requiresCorrectToolForDrops().strength(3.0F, 3.0F), new ResourceLocation("block/granite"));

    public static final TagPrefix oreDiorite = oreTagPrefix("diorite", BlockTags.MINEABLE_WITH_PICKAXE)
            .langValue("Diorite %s Ore")
            .registerOre(Blocks.DIORITE::defaultBlockState, () -> GTMaterials.Diorite, BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).requiresCorrectToolForDrops().strength(3.0F, 3.0F), new ResourceLocation("block/diorite"));

    public static final TagPrefix oreAndesite = oreTagPrefix("andesite", BlockTags.MINEABLE_WITH_PICKAXE)
            .langValue("Andesite %s Ore")
            .registerOre(Blocks.ANDESITE::defaultBlockState, () -> GTMaterials.Andesite, BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).requiresCorrectToolForDrops().strength(3.0F, 3.0F), new ResourceLocation("block/andesite"));

    public static final TagPrefix oreRedGranite = oreTagPrefix("red_granite", BlockTags.MINEABLE_WITH_PICKAXE)
        .langValue("Red Granite %s Ore")
        .registerOre(() -> GTBlocks.RED_GRANITE.getDefaultState(), () -> GTMaterials.GraniteRed, BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_RED).requiresCorrectToolForDrops().strength(3.0F, 3.0F), GTCEu.id("block/red_granite"));

    public static final TagPrefix oreMarble = oreTagPrefix("marble", BlockTags.MINEABLE_WITH_PICKAXE)
        .langValue("Red Granite %s Ore")
        .registerOre(() -> GTBlocks.MARBLE.getDefaultState(), () -> GTMaterials.Marble, BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).requiresCorrectToolForDrops().strength(3.0F, 3.0F), GTCEu.id("block/marble"));

    public static final TagPrefix oreDeepslate = oreTagPrefix("deepslate", BlockTags.MINEABLE_WITH_PICKAXE)
            .langValue("Deepslate %s Ore")
            .registerOre(Blocks.DEEPSLATE::defaultBlockState, () -> GTMaterials.Deepslate, BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).requiresCorrectToolForDrops().strength(4.5F, 3.0F), new ResourceLocation("block/deepslate"), false, false, true);

    public static final TagPrefix oreTuff = oreTagPrefix("tuff", BlockTags.MINEABLE_WITH_PICKAXE)
            .langValue("Tuff %s Ore")
            .registerOre(Blocks.TUFF::defaultBlockState, null, BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_GRAY).requiresCorrectToolForDrops().strength(3.0F, 3.0F), new ResourceLocation("block/tuff"));

    public static final TagPrefix oreSand = oreTagPrefix("sand", BlockTags.MINEABLE_WITH_SHOVEL)
            .langValue("Sand %s Ore")
            .registerOre(Blocks.SAND::defaultBlockState, () -> GTMaterials.SiliconDioxide, BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND), new ResourceLocation("block/sand"), false, true, false);

    public static final TagPrefix oreRedSand = oreTagPrefix("redSand", BlockTags.MINEABLE_WITH_SHOVEL)
            .langValue("Red Sand %s Ore")
            .registerOre(Blocks.RED_SAND::defaultBlockState, () -> GTMaterials.SiliconDioxide, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND), new ResourceLocation("block/red_sand"), false, true, false);

    public static final TagPrefix oreGravel = oreTagPrefix("gravel", BlockTags.MINEABLE_WITH_SHOVEL)
            .langValue("Gravel %s Ore")
            .registerOre(Blocks.GRAVEL::defaultBlockState, () -> GTMaterials.Flint, BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.SNARE).strength(0.6F).sound(SoundType.GRAVEL), new ResourceLocation("block/gravel"), false, true, false);

    public static final TagPrefix oreBasalt = oreTagPrefix("basalt", BlockTags.MINEABLE_WITH_PICKAXE)
            .langValue("Basalt %s Ore")
            .registerOre(Blocks.BASALT::defaultBlockState, () -> GTMaterials.Basalt, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.5F, 4.2F).sound(SoundType.BASALT), new ResourceLocation("block/basalt"), true);

    public static final TagPrefix oreNetherrack = oreTagPrefix("netherrack", BlockTags.MINEABLE_WITH_PICKAXE)
            .langValue("Nether %s Ore")
            .registerOre(Blocks.NETHERRACK::defaultBlockState, () -> GTMaterials.Netherrack, BlockBehaviour.Properties.of().mapColor(MapColor.NETHER).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F).sound(SoundType.NETHER_ORE), new ResourceLocation("block/netherrack"), true, false, true);

    public static final TagPrefix oreEndstone = oreTagPrefix("endstone", BlockTags.MINEABLE_WITH_PICKAXE)
            .langValue("End %s Ore")
            .registerOre(Blocks.END_STONE::defaultBlockState, () -> GTMaterials.Endstone, BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(4.5F, 9.0F), new ResourceLocation("block/end_stone"), true, false, true);

    public static final TagPrefix rawOre = new TagPrefix("raw", true)
            .idPattern("raw_%s")
            .defaultTagPath("raw_materials/%s")
            .unformattedTagPath("raw_materials")
            .langValue("Raw %s")
            .materialIconType(MaterialIconType.rawOre)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasOreProperty);

    public static final TagPrefix rawOreBlock = new TagPrefix("rawOreBlock")
            .idPattern("raw_%s_block")
            .defaultTagPath("storage_blocks/raw_%s")
            .langValue("Block of Raw %s")
            .materialIconType(MaterialIconType.rawOreBlock)
            .miningToolTag(BlockTags.MINEABLE_WITH_PICKAXE)
            .unificationEnabled(true)
            .generateBlock(true)
            .generationCondition(hasOreProperty);

    public static final TagPrefix crushedRefined = new TagPrefix("refinedOre")
            .idPattern("refined_%s_ore")
            .defaultTagPath("refined_ores/%s")
            .defaultTagPath("refined_ores")
            .langValue("Refined %s Ore")
            .materialIconType(MaterialIconType.crushedRefined)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasOreProperty);

    public static final TagPrefix crushedPurified = new TagPrefix("purifiedOre")
            .idPattern("purified_%s_ore")
            .defaultTagPath("purified_ores/%s")
            .defaultTagPath("purified_ores")
            .langValue("Purified %s Ore")
            .materialIconType(MaterialIconType.crushedPurified)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasOreProperty);

    public static final TagPrefix crushed = new TagPrefix("crushedOre")
            .idPattern("crushed_%s_ore")
            .defaultTagPath("crushed_ores/%s")
            .unformattedTagPath("crushed_ores")
            .langValue("Crushed %s Ore")
            .materialIconType(MaterialIconType.crushed)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasOreProperty)
            .tooltip((mat, tooltips) -> tooltips.add(Component.translatable("metaitem.crushed.tooltip.purify")));

    // A hot Ingot, which has to be cooled down by a Vacuum Freezer.
    public static final TagPrefix ingotHot = new TagPrefix("hotIngot")
            .idPattern("hot_%s_ingot")
            .defaultTagPath("hot_ingots/%s")
            .unformattedTagPath("hot_ingots")
            .langValue("Hot %s Ingot")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.ingotHot)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasBlastProperty.and(mat -> mat.getProperty(PropertyKey.BLAST).getBlastTemperature() > 1750));

    // A regular Ingot.
    public static final TagPrefix ingot = new TagPrefix("ingot")
            .defaultTagPath("ingots/%s")
            .unformattedTagPath("ingots")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.ingot)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasIngotProperty);

    // A regular Gem worth one Dust.
    public static final TagPrefix gem = new TagPrefix("gem")
            .defaultTagPath("gems/%s")
            .unformattedTagPath("gems")
            .langValue("%s")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.gem)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasGemProperty);

    // A regular Gem worth one small Dust.
    public static final TagPrefix gemChipped = new TagPrefix("chippedGem")
            .idPattern("chipped_%s_gem")
            .defaultTagPath("chipped_gems/%s")
            .unformattedTagPath("chipped_gems")
            .langValue("Chipped %s")
            .materialAmount(GTValues.M / 4)
            .materialIconType(MaterialIconType.gemChipped)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasGemProperty.and(unused -> ConfigHolder.INSTANCE.recipes.generateLowQualityGems));

    // A regular Gem worth two small Dusts.
    public static final TagPrefix gemFlawed = new TagPrefix("flawedGem")
            .idPattern("flawed_%s_gem")
            .defaultTagPath("flawed_gems/%s")
            .unformattedTagPath("flawed_gems")
            .langValue("Flawed %s")
            .materialAmount(GTValues.M / 2)
            .materialIconType(MaterialIconType.gemFlawed)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasGemProperty.and(unused -> ConfigHolder.INSTANCE.recipes.generateLowQualityGems));

    // A regular Gem worth two Dusts.
    public static final TagPrefix gemFlawless = new TagPrefix("flawlessGem")
            .idPattern("flawless_%s_gem")
            .defaultTagPath("flawless_gems/%s")
            .unformattedTagPath("flawless_gems")
            .langValue("Flawless %s")
            .materialAmount(GTValues.M * 2)
            .maxStackSize(32)
            .materialIconType(MaterialIconType.gemFlawless)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasGemProperty);

    // A regular Gem worth four Dusts.
    public static final TagPrefix gemExquisite = new TagPrefix("exquisiteGem")
            .idPattern("exquisite_%s_gem")
            .defaultTagPath("exquisite_gems/%s")
            .unformattedTagPath("exquisite_gems")
            .langValue("Exquisite %s")
            .materialAmount(GTValues.M * 4)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.gemExquisite)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasGemProperty);

    // 1/4th of a Dust.
    public static final TagPrefix dustSmall = new TagPrefix("smallDust")
            .idPattern("small_%s_dust")
            .defaultTagPath("small_dusts/%s")
            .unformattedTagPath("small_dusts")
            .langValue("Small Pile of %s Dust")
            .materialAmount(GTValues.M / 4)
            .materialIconType(MaterialIconType.dustSmall)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasDustProperty);

    // 1/9th of a Dust.
    public static final TagPrefix dustTiny = new TagPrefix("tinyDust")
            .idPattern("tiny_%s_dust")
            .defaultTagPath("tiny_dusts/%s")
            .unformattedTagPath("tiny_dusts")
            .langValue("Tiny Pile of %s Dust")
            .materialAmount(GTValues.M / 9)
            .materialIconType(MaterialIconType.dustTiny)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasDustProperty);

    // Dust with impurities. 1 Unit of Main Material and 1/9 - 1/4 Unit of secondary Material
    public static final TagPrefix dustImpure = new TagPrefix("impureDust")
            .idPattern("impure_%s_dust")
            .defaultTagPath("impure_dusts/%s")
            .unformattedTagPath("impure_dusts")
            .langValue("Impure Pile of %s Dust")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.dustImpure)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasOreProperty)
            .tooltip((mat, tooltips) -> tooltips.add(Component.translatable("metaitem.dust.tooltip.purify")));

    // Pure Dust worth of one Ingot or Gem.
    public static final TagPrefix dustPure = new TagPrefix("pureDust")
            .idPattern("pure_%s_dust")
            .defaultTagPath("pure_dusts/%s")
            .unformattedTagPath("pure_dusts")
            .langValue("Purified Pile of %s Dust")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.dustPure)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasOreProperty)
            .tooltip((mat, tooltips) -> tooltips.add(Component.translatable("metaitem.dust.tooltip.purify")));

    public static final TagPrefix dust = new TagPrefix("dust")
            .defaultTagPath("dusts/%s")
            .unformattedTagPath("dusts")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.dust)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasDustProperty);

    // A Nugget.
    public static final TagPrefix nugget = new TagPrefix("nugget")
            .defaultTagPath("nuggets/%s")
            .unformattedTagPath("nuggets")
            .materialAmount(GTValues.M / 9)
            .materialIconType(MaterialIconType.nugget)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasIngotProperty);

    // 9 Plates combined in one Item.
    public static final TagPrefix plateDense = new TagPrefix("densePlate")
            .idPattern("dense_%s_plate")
            .defaultTagPath("dense_plates/%s")
            .unformattedTagPath("dense_plates")
            .langValue("Dense %s Plate")
            .materialAmount(GTValues.M * 9)
            .maxStackSize(7)
            .materialIconType(MaterialIconType.plateDense)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_DENSE));

    // 2 Plates combined in one Item
    public static final TagPrefix plateDouble = new TagPrefix("doublePlate")
            .idPattern("double_%s_plate")
            .defaultTagPath("double_plates/%s")
            .unformattedTagPath("double_plates")
            .langValue("Double %s Plate")
            .materialAmount(GTValues.M * 2)
            .maxStackSize(32)
            .materialIconType(MaterialIconType.plateDouble)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasIngotProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE) && !mat.hasFlag(MaterialFlags.NO_SMASHING)));

    // Regular Plate made of one Ingot/Dust.
    public static final TagPrefix plate = new TagPrefix("plate")
            .defaultTagPath("plates/%s")
            .unformattedTagPath("plates")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.plate)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE));

    // Round made of 1 Nugget
    public static final TagPrefix round = new TagPrefix("round")
            .defaultTagPath("rounds/%s")
            .unformattedTagPath("rounds")
            .materialAmount(GTValues.M / 9)
            .materialIconType(MaterialIconType.round)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_ROUND));

    // Foil made of 1/4 Ingot/Dust.
    public static final TagPrefix foil = new TagPrefix("foil")
            .defaultTagPath("foils/%s")
            .unformattedTagPath("foils")
            .materialAmount(GTValues.M / 4)
            .materialIconType(MaterialIconType.foil)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_FOIL));

    // Stick made of an Ingot.
    public static final TagPrefix rodLong = new TagPrefix("longRod")
            .idPattern("long_%s_rod")
            .defaultTagPath("rods/long/%s")
            .unformattedTagPath("rods/long")
            .langValue("Long %s Rod")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.stickLong)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_LONG_ROD));

    // Stick made of half an Ingot.
    public static final TagPrefix rod = new TagPrefix("rod")
            .defaultTagPath("rods/%s")
            .unformattedTagPath("rods")
            .langValue("%s Rod")
            .materialAmount(GTValues.M / 2)
            .materialIconType(MaterialIconType.stick)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_ROD));

    // consisting out of 1/8 Ingot or 1/4 Stick.
    public static final TagPrefix bolt = new TagPrefix("bolt")
            .defaultTagPath("bolts/%s")
            .unformattedTagPath("bolts")
            .materialAmount(GTValues.M / 8)
            .materialIconType(MaterialIconType.bolt)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_BOLT_SCREW));

    // consisting out of 1/9 Ingot.
    public static final TagPrefix screw = new TagPrefix("screw")
            .defaultTagPath("screws/%s")
            .unformattedTagPath("screws")
            .materialAmount(GTValues.M / 9)
            .materialIconType(MaterialIconType.screw)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_BOLT_SCREW));

    // consisting out of 1/2 Stick.
    public static final TagPrefix ring = new TagPrefix("ring")
            .defaultTagPath("rings/%s")
            .unformattedTagPath("rings")
            .materialAmount(GTValues.M / 4)
            .materialIconType(MaterialIconType.ring)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_RING));

    // consisting out of 1 Fine Wire.
    public static final TagPrefix springSmall = new TagPrefix("smallSpring")
            .idPattern("small_%s_spring")
            .defaultTagPath("small_springs/%s")
            .unformattedTagPath("small_springs")
            .langValue("Small %s Spring")
            .materialAmount(GTValues.M / 4)
            .materialIconType(MaterialIconType.springSmall)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_SPRING_SMALL) && !mat.hasFlag(MaterialFlags.NO_SMASHING));

    // consisting out of 2 Sticks.
    public static final TagPrefix spring = new TagPrefix("spring")
            .defaultTagPath("springs/%s")
            .unformattedTagPath("springs")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.spring)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_SPRING) && !mat.hasFlag(MaterialFlags.NO_SMASHING));

    // consisting out of 1/8 Ingot or 1/4 Wire.
    public static final TagPrefix wireFine = new TagPrefix("fineWire")
            .idPattern("fine_%s_wire")
            .defaultTagPath("fine_wires/%s")
            .unformattedTagPath("fine_wires")
            .langValue("Fine %s Wire")
            .materialAmount(GTValues.M / 8)
            .materialIconType(MaterialIconType.wireFine)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_FINE_WIRE));

    // consisting out of 4 Plates, 1 Ring and 1 Screw.
    public static final TagPrefix rotor = new TagPrefix("rotor")
            .defaultTagPath("rotors/%s")
            .unformattedTagPath("rotors")
            .materialAmount(GTValues.M * 4)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.rotor)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_ROTOR));

    // Consisting of 1 Plate.
    public static final TagPrefix gearSmall = new TagPrefix("smallGear")
            .idPattern("small_%s_gear")
            .defaultTagPath("small_gears/%s")
            .unformattedTagPath("small_gears")
            .langValue("Small %s Gear")
            .materialAmount(GTValues.M)
            .materialIconType(MaterialIconType.gearSmall)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_SMALL_GEAR));

    // Consisting of 4 Plates.
    public static final TagPrefix gear = new TagPrefix("gear")
            .defaultTagPath("gears/%s")
            .unformattedTagPath("gears")
            .materialAmount(GTValues.M * 4)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.gear)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_GEAR));

    // 3/4 of a Plate or Gem used to shape a Lens. Normally only used on Transparent Materials.
    public static final TagPrefix lens = new TagPrefix("lens")
            .defaultTagPath("lenses/%s")
            .unformattedTagPath("lenses")
            .materialAmount((GTValues.M * 3) / 4)
            .materialIconType(MaterialIconType.lens)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(MaterialFlags.GENERATE_LENS));

    public static final TagPrefix dye = new TagPrefix("dye")
            .defaultTagPath("dyes/%s")
            .unformattedTagPath("dyes")
            .materialAmount(-1);

    // made of 4 Ingots.
    public static final TagPrefix toolHeadBuzzSaw = new TagPrefix("buzzSawBlade")
            .itemTable(() -> GTItems.MATERIAL_ITEMS)
            .langValue("%s Buzzsaw Blade")
            .materialAmount(GTValues.M * 4)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.toolHeadBuzzSaw)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasNoCraftingToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)).and(mat -> mat.getProperty(PropertyKey.TOOL).hasType(GTToolType.BUZZSAW)));

    // made of 1 Ingots.
    public static final TagPrefix toolHeadScrewdriver = new TagPrefix("screwdriverTip")
            .itemTable(() -> GTItems.MATERIAL_ITEMS)
            .langValue("%s Screwdriver Tip")
            .materialAmount(GTValues.M)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.toolHeadScrewdriver)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasNoCraftingToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_LONG_ROD)).and(mat -> mat.getProperty(PropertyKey.TOOL).hasType(GTToolType.SCREWDRIVER_LV)));

    // made of 4 Ingots.
    public static final TagPrefix toolHeadDrill = new TagPrefix("drillHead")
            .itemTable(() -> GTItems.MATERIAL_ITEMS)
            .langValue("%s Drill Head")
            .materialAmount(GTValues.M * 4)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.toolHeadDrill)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)).and(mat -> mat.getProperty(PropertyKey.TOOL).hasType(GTToolType.DRILL_LV)));

    // made of 2 Ingots.
    public static final TagPrefix toolHeadChainsaw = new TagPrefix("chainsawHead")
            .itemTable(() -> GTItems.MATERIAL_ITEMS)
            .langValue("%s Chainsaw Head")
            .materialAmount(GTValues.M * 2)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.toolHeadChainsaw)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasNoCraftingToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)).and(mat -> mat.getProperty(PropertyKey.TOOL).hasType(GTToolType.CHAINSAW_LV)));

    // made of 4 Ingots.
    public static final TagPrefix toolHeadWrench = new TagPrefix("wrenchTip")
            .itemTable(() -> GTItems.MATERIAL_ITEMS)
            .langValue("%s Wrench Tip")
            .materialAmount(GTValues.M * 4)
            .maxStackSize(16)
            .materialIconType(MaterialIconType.toolHeadWrench)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(hasNoCraftingToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE)).and(mat -> mat.getProperty(PropertyKey.TOOL).hasType(GTToolType.WRENCH_LV)));

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
            .defaultTagPath("storage_blocks/%s")
            .unformattedTagPath("storage_blocks")
            .langValue("Block of %s")
            .materialAmount(GTValues.M * 9)
            .materialIconType(MaterialIconType.block)
            .miningToolTag(BlockTags.MINEABLE_WITH_PICKAXE)
            .generateBlock(true)
            .generationCondition(material -> material.hasProperty(PropertyKey.INGOT) || material.hasProperty(PropertyKey.GEM) || material.hasFlag(MaterialFlags.FORCE_GENERATE_BLOCK))
            .unificationEnabled(true);

    public static final TagPrefix log = new TagPrefix("log")
        .unformattedTagPath("logs", true);
    public static final TagPrefix planks = new TagPrefix("planks")
        .unformattedTagPath("planks", true);
    public static final TagPrefix slab = new TagPrefix("slab")
        .unformattedTagPath("slabs", true);
    public static final TagPrefix stairs = new TagPrefix("stairs")
        .unformattedTagPath("stairs", true);
    public static final TagPrefix fence = new TagPrefix("fence")
        .unformattedTagPath("fences");
    public static final TagPrefix fenceGate = new TagPrefix("fenceGate")
        .unformattedTagPath("fence_gates");
    public static final TagPrefix door = new TagPrefix("door")
        .unformattedTagPath("doors", true);

    // Prefix to determine which kind of Rock this is.
    // Also has a base tag path of only the material, for things like obsidian etc.
    public static final TagPrefix rock = new TagPrefix("rock")
            .defaultTagPath("%s")
            .langValue("%s")
            .miningToolTag(BlockTags.MINEABLE_WITH_PICKAXE)
            .unificationEnabled(true)
            .generateBlock(true) // generate a block but not really, for TagPrefix#setIgnoredBlock
            .generationCondition((material) -> false);

    public static final TagPrefix frameGt = new TagPrefix("frame")
            .defaultTagPath("frames/%s")
            .langValue("%s Frame")
            .materialAmount(GTValues.M * 2)
            .materialIconType(MaterialIconType.frameGt)
            .miningToolTag(CustomTags.MINEABLE_WITH_WRENCH)
            .unificationEnabled(true)
            .generateBlock(true)
            .blockProperties(() -> RenderType::translucent, p -> p.noOcclusion())
            .generationCondition(material -> material.hasProperty(PropertyKey.DUST) && material.hasFlag(MaterialFlags.GENERATE_FRAME));

    // Pipes
    public static final TagPrefix pipeTinyFluid = new TagPrefix("pipeTinyFluid").itemTable(() -> GTBlocks.FLUID_PIPE_BLOCKS).langValue("Tiny %s Fluid Pipe").miningToolTag(CustomTags.MINEABLE_WITH_WRENCH).materialAmount(GTValues.M / 2).unificationEnabled(true);
    public static final TagPrefix pipeSmallFluid = new TagPrefix("pipeSmallFluid").itemTable(() -> GTBlocks.FLUID_PIPE_BLOCKS).langValue("Small %s Fluid Pipe").miningToolTag(CustomTags.MINEABLE_WITH_WRENCH).materialAmount(GTValues.M).unificationEnabled(true);
    public static final TagPrefix pipeNormalFluid = new TagPrefix("pipeNormalFluid").itemTable(() -> GTBlocks.FLUID_PIPE_BLOCKS).langValue("Normal %s Fluid Pipe").miningToolTag(CustomTags.MINEABLE_WITH_WRENCH).materialAmount(GTValues.M * 3).unificationEnabled(true);
    public static final TagPrefix pipeLargeFluid = new TagPrefix("pipeLargeFluid").itemTable(() -> GTBlocks.FLUID_PIPE_BLOCKS).langValue("Large %s Fluid Pipe").miningToolTag(CustomTags.MINEABLE_WITH_WRENCH).materialAmount(GTValues.M * 6).unificationEnabled(true);
    public static final TagPrefix pipeHugeFluid = new TagPrefix("pipeHugeFluid").itemTable(() -> GTBlocks.FLUID_PIPE_BLOCKS).langValue("Huge %s Fluid Pipe").miningToolTag(CustomTags.MINEABLE_WITH_WRENCH).materialAmount(GTValues.M * 12).unificationEnabled(true);

    public static final TagPrefix pipeQuadrupleFluid = new TagPrefix("pipeQuadrupleFluid").itemTable(() -> GTBlocks.FLUID_PIPE_BLOCKS).langValue("Quadruple %s Fluid Pipe").miningToolTag(CustomTags.MINEABLE_WITH_WRENCH).materialAmount(GTValues.M * 4).unificationEnabled(true);
    public static final TagPrefix pipeNonupleFluid = new TagPrefix("pipeNonupleFluid").itemTable(() -> GTBlocks.FLUID_PIPE_BLOCKS).langValue("Nonuple %s Fluid Pipe").miningToolTag(CustomTags.MINEABLE_WITH_WRENCH).materialAmount(GTValues.M * 9).unificationEnabled(true);

    public static final TagPrefix pipeSmallItem = new TagPrefix("pipeSmallItem").itemTable(() -> GTBlocks.ITEM_PIPE_BLOCKS).langValue("Small %s Item Pipe").miningToolTag(CustomTags.MINEABLE_WITH_WRENCH).materialAmount(GTValues.M).unificationEnabled(true);
    public static final TagPrefix pipeNormalItem = new TagPrefix("pipeNormalItem").itemTable(() -> GTBlocks.ITEM_PIPE_BLOCKS).langValue("Normal %s Item Pipe").miningToolTag(CustomTags.MINEABLE_WITH_WRENCH).materialAmount(GTValues.M * 3).unificationEnabled(true);
    public static final TagPrefix pipeLargeItem = new TagPrefix("pipeLargeItem").itemTable(() -> GTBlocks.ITEM_PIPE_BLOCKS).langValue("Large %s Item Pipe").miningToolTag(CustomTags.MINEABLE_WITH_WRENCH).materialAmount(GTValues.M * 6).unificationEnabled(true);
    public static final TagPrefix pipeHugeItem = new TagPrefix("pipeHugeItem").itemTable(() -> GTBlocks.ITEM_PIPE_BLOCKS).langValue("Huge %s Item Pipe").miningToolTag(CustomTags.MINEABLE_WITH_WRENCH).materialAmount(GTValues.M * 12).unificationEnabled(true);

    public static final TagPrefix pipeSmallRestrictive = new TagPrefix("pipeSmallRestrictive").itemTable(() -> GTBlocks.ITEM_PIPE_BLOCKS).langValue("Small Restrictive %s Item Pipe").miningToolTag(CustomTags.MINEABLE_WITH_WRENCH).materialAmount(GTValues.M).unificationEnabled(true);
    public static final TagPrefix pipeNormalRestrictive = new TagPrefix("pipeNormalRestrictive").itemTable(() -> GTBlocks.ITEM_PIPE_BLOCKS).langValue("Normal Restrictive %s Item Pipe").miningToolTag(CustomTags.MINEABLE_WITH_WRENCH).materialAmount(GTValues.M * 3).unificationEnabled(true);
    public static final TagPrefix pipeLargeRestrictive = new TagPrefix("pipeLargeRestrictive").itemTable(() -> GTBlocks.ITEM_PIPE_BLOCKS).langValue("Large Restrictive %s Item Pipe").miningToolTag(CustomTags.MINEABLE_WITH_WRENCH).materialAmount(GTValues.M * 6).unificationEnabled(true);
    public static final TagPrefix pipeHugeRestrictive = new TagPrefix("pipeHugeRestrictive").itemTable(() -> GTBlocks.ITEM_PIPE_BLOCKS).langValue("Huge Restrictive %s Item Pipe").miningToolTag(CustomTags.MINEABLE_WITH_WRENCH).materialAmount(GTValues.M * 12).unificationEnabled(true);

    // Wires and cables
    public static final TagPrefix wireGtHex = new TagPrefix("wireGtHex").itemTable(() -> GTBlocks.CABLE_BLOCKS).langValue("16x %s Wire").miningToolTag(CustomTags.MINEABLE_WITH_WIRE_CUTTER).materialAmount(GTValues.M * 8).materialIconType(MaterialIconType.wire).unificationEnabled(true);
    public static final TagPrefix wireGtOctal = new TagPrefix("wireGtOctal").itemTable(() -> GTBlocks.CABLE_BLOCKS).langValue("8x %s Wire").miningToolTag(CustomTags.MINEABLE_WITH_WIRE_CUTTER).materialAmount(GTValues.M * 4).materialIconType(MaterialIconType.wire).unificationEnabled(true);
    public static final TagPrefix wireGtQuadruple = new TagPrefix("wireGtQuadruple").itemTable(() -> GTBlocks.CABLE_BLOCKS).langValue("4x %s Wire").miningToolTag(CustomTags.MINEABLE_WITH_WIRE_CUTTER).materialAmount(GTValues.M * 2).materialIconType(MaterialIconType.wire).unificationEnabled(true);
    public static final TagPrefix wireGtDouble = new TagPrefix("wireGtDouble").itemTable(() -> GTBlocks.CABLE_BLOCKS).langValue("2x %s Wire").miningToolTag(CustomTags.MINEABLE_WITH_WIRE_CUTTER).materialAmount(GTValues.M).materialIconType(MaterialIconType.wire).unificationEnabled(true);
    public static final TagPrefix wireGtSingle = new TagPrefix("wireGtSingle").itemTable(() -> GTBlocks.CABLE_BLOCKS).langValue("1x %s Wire").miningToolTag(CustomTags.MINEABLE_WITH_WIRE_CUTTER).materialAmount(GTValues.M / 2).materialIconType(MaterialIconType.wire).unificationEnabled(true);

    public static final TagPrefix cableGtHex = new TagPrefix("cableGtHex").itemTable(() -> GTBlocks.CABLE_BLOCKS).langValue("16x %s Cable").miningToolTag(CustomTags.MINEABLE_WITH_WIRE_CUTTER).materialAmount(GTValues.M * 8).unificationEnabled(true);
    public static final TagPrefix cableGtOctal = new TagPrefix("cableGtOctal").itemTable(() -> GTBlocks.CABLE_BLOCKS).langValue("8x %s Cable").miningToolTag(CustomTags.MINEABLE_WITH_WIRE_CUTTER).materialAmount(GTValues.M * 4).unificationEnabled(true);
    public static final TagPrefix cableGtQuadruple = new TagPrefix("cableGtQuadruple").itemTable(() -> GTBlocks.CABLE_BLOCKS).langValue("4x %s Cable").miningToolTag(CustomTags.MINEABLE_WITH_WIRE_CUTTER).materialAmount(GTValues.M * 2).unificationEnabled(true);
    public static final TagPrefix cableGtDouble = new TagPrefix("cableGtDouble").itemTable(() -> GTBlocks.CABLE_BLOCKS).langValue("2x %s Cable").miningToolTag(CustomTags.MINEABLE_WITH_WIRE_CUTTER).materialAmount(GTValues.M).unificationEnabled(true);
    public static final TagPrefix cableGtSingle = new TagPrefix("cableGtSingle").itemTable(() -> GTBlocks.CABLE_BLOCKS).langValue("1x %s Cable").miningToolTag(CustomTags.MINEABLE_WITH_WIRE_CUTTER).materialAmount(GTValues.M / 2).unificationEnabled(true);


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

    public record OreType(Supplier<BlockState> stoneType, Supplier<Material> material, Supplier<BlockBehaviour.Properties> template, ResourceLocation baseModelLocation, boolean isDoubleDrops, boolean isSand, boolean shouldDropAsItem) { }
    public record BlockProperties(Supplier<Supplier<RenderType>> renderType, UnaryOperator<BlockBehaviour.Properties> properties) {}

    @Getter
    public final String name;
    @Getter @Setter
    private String idPattern;
    @Getter
    public final boolean invertedName;

    protected final List<TagType> tags = new ArrayList<>();
    @Setter @Getter
    public String langValue;

    @Getter
    @Setter
    private long materialAmount = -1;

    @Setter
    private boolean unificationEnabled;
    @Setter
    private boolean generateItem;
    @Setter
    private boolean generateBlock;
    @Getter @Setter
    private BlockProperties blockProperties = new BlockProperties(() -> RenderType::translucent, UnaryOperator.identity());

    @Getter
    @Setter
    private @Nullable
    Predicate<Material> generationCondition;

    @Nullable @Getter
    @Setter
    private MaterialIconType materialIconType;

    @Setter
    private Supplier<Table<TagPrefix, Material, ? extends Supplier<? extends ItemLike>>> itemTable;

    @Nullable @Getter
    @Setter
    private BiConsumer<Material, List<Component>> tooltip;

    private final Map<Material, Supplier<? extends ItemLike>[]> ignoredMaterials = new HashMap<>();
    private final Object2FloatMap<Material> materialAmounts = new Object2FloatOpenHashMap<>();

    @Getter
    @Setter
    private int maxStackSize = 64;

    @Getter
    private final List<MaterialStack> secondaryMaterials = new ArrayList<>();

    @Getter
    protected final Set<TagKey<Block>> miningToolTag = new HashSet<>();

    public TagPrefix(String name) {
        this(name, false);
    }

    public TagPrefix(String name, boolean invertedName) {
        this.name = name;
        this.idPattern = "%s_" + FormattingUtil.toLowerCaseUnder(name);
        this.invertedName = invertedName;
        this.langValue = "%s " + FormattingUtil.toEnglishName(FormattingUtil.toLowerCaseUnder(name));
        PREFIXES.put(name, this);
    }

    public static TagPrefix oreTagPrefix(String name, TagKey<Block> miningToolTag) {
        return new TagPrefix(name)
            .defaultTagPath("ores/%s")
            .prefixOnlyTagPath("ores_in_ground/%s")
            .unformattedTagPath("ores")
            .materialIconType(MaterialIconType.ore)
            .miningToolTag(miningToolTag)
            .unificationEnabled(true)
            .generationCondition(hasOreProperty);
    }

    public void addSecondaryMaterial(MaterialStack secondaryMaterial) {
        Preconditions.checkNotNull(secondaryMaterial, "secondaryMaterial");
        secondaryMaterials.add(secondaryMaterial);
    }

    public TagPrefix registerOre(Supplier<BlockState> stoneType, Supplier<Material> material, BlockBehaviour.Properties properties, ResourceLocation baseModelLocation) {
        return registerOre(stoneType, material, properties, baseModelLocation, false);
    }

    public TagPrefix registerOre(Supplier<BlockState> stoneType, Supplier<Material> material, BlockBehaviour.Properties properties, ResourceLocation baseModelLocation, boolean doubleDrops) {
        return registerOre(stoneType, material, properties, baseModelLocation, doubleDrops, false, false);
    }

    public TagPrefix registerOre(Supplier<BlockState> stoneType, Supplier<Material> material, BlockBehaviour.Properties properties, ResourceLocation baseModelLocation, boolean doubleDrops, boolean isSand, boolean shouldDropAsItem) {
        return registerOre(stoneType, material, () -> properties, baseModelLocation, doubleDrops, isSand, shouldDropAsItem);
    }

    public TagPrefix registerOre(Supplier<BlockState> stoneType, Supplier<Material> material, Supplier<BlockBehaviour.Properties> properties, ResourceLocation baseModelLocation, boolean doubleDrops, boolean isSand, boolean shouldDropAsItem) {
        ORES.put(this, new OreType(stoneType, material, properties, baseModelLocation, doubleDrops, isSand, shouldDropAsItem));
        if (shouldDropAsItem) {
            GTOreByProduct.addOreByProductPrefix(this);
        }
        return this;
    }

    public TagPrefix defaultTagPath(String path) {
        return this.defaultTagPath(path, false);
    }

    public TagPrefix defaultTagPath(String path, boolean isVanilla) {
        this.tags.add(TagType.withDefaultFormatter(path, isVanilla));
        return this;
    }

    public TagPrefix prefixTagPath(String path) {
        this.tags.add(TagType.withPrefixFormatter(path));
        return this;
    }

    public TagPrefix prefixOnlyTagPath(String path) {
        this.tags.add(TagType.withPrefixOnlyFormatter(path));
        return this;
    }

    public TagPrefix unformattedTagPath(String path) {
        return unformattedTagPath(path, false);
    }

    public TagPrefix unformattedTagPath(String path, boolean isVanilla) {
        this.tags.add(TagType.withNoFormatter(path, isVanilla));
        return this;
    }

    public TagPrefix customTagPath(String path, BiFunction<TagPrefix, Material, TagKey<Item>> formatter) {
        this.tags.add(TagType.withCustomFormatter(path, formatter));
        return this;
    }

    public TagPrefix miningToolTag(TagKey<Block> tag) {
        this.miningToolTag.add(tag);
        return this;
    }

    public TagPrefix blockProperties(Supplier<Supplier<RenderType>> renderType, UnaryOperator<BlockBehaviour.Properties> properties) {
        return this.blockProperties(new BlockProperties(renderType, properties));
    }

    public long getMaterialAmount(@Nullable Material material) {
        if (material == null || !isAmountModified(material)) {
            return this.materialAmount;
        }
        return (long) (GTValues.M * materialAmounts.getFloat(material));
    }

    public static TagPrefix getPrefix(String prefixName) {
        return getPrefix(prefixName, null);
    }

    public static TagPrefix getPrefix(String prefixName, @Nullable TagPrefix replacement) {
        return PREFIXES.getOrDefault(prefixName, replacement);
    }

    @SuppressWarnings("unchecked")
    public TagKey<Item>[] getItemParentTags() {
        return tags.stream().filter(TagType::isParentTag).map(type -> type.getTag(this, null)).toArray(TagKey[]::new);
    }

    @SuppressWarnings("unchecked")
    public TagKey<Item>[] getItemTags(@NotNull Material mat) {
        return tags.stream().filter(type -> !type.isParentTag()).map(type -> type.getTag(this, mat)).toArray(TagKey[]::new);
    }

    @SuppressWarnings("unchecked")
    public TagKey<Item>[] getAllItemTags(@NotNull Material mat) {
        return tags.stream().map(type -> type.getTag(this, mat)).toArray(TagKey[]::new);
    }

    @SuppressWarnings("unchecked")
    public TagKey<Block>[] getBlockTags(@NotNull Material mat) {
        return tags.stream().filter(type -> !type.isParentTag()).map(type -> type.getTag(this, mat)).map(itemTagKey -> TagKey.create(Registries.BLOCK, itemTagKey.location())).toArray(TagKey[]::new);
    }

    @SuppressWarnings("unchecked")
    public TagKey<Block>[] getAllBlockTags(@NotNull Material mat) {
        return tags.stream().map(type -> type.getTag(this, mat)).map(itemTagKey -> TagKey.create(Registries.BLOCK, itemTagKey.location())).toArray(TagKey[]::new);
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
        return generateItem && !isIgnored(material) && (generationCondition == null || generationCondition.test(material)) || (hasItemTable() && this.itemTable.get() != null && getItemFromTable(material) != null);
    }

    public boolean doGenerateBlock() {
        return generateBlock;
    }

    public boolean doGenerateBlock(Material material) {
        return generateBlock && !isIgnored(material) && (generationCondition == null || generationCondition.test(material)) || hasItemTable() && this.itemTable.get() != null && getItemFromTable(material) != null;
    }

    @FunctionalInterface
    public interface MaterialRecipeHandler<T extends IMaterialProperty<T>> {
        void accept(TagPrefix prefix, Material material, T property, RecipeOutput provider);
    }

    public <T extends IMaterialProperty<T>> void executeHandler(RecipeOutput provider, PropertyKey<T> propertyKey, MaterialRecipeHandler<T> handler) {
        for (Material material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
            if (material.hasProperty(propertyKey) && !material.hasFlag(MaterialFlags.NO_UNIFICATION) && !ChemicalHelper.get(this, material).isEmpty()) {
                handler.accept(this, material, material.getProperty(propertyKey), provider);
            }
        }
    }

    public String getUnlocalizedName() {
        return "tagprefix." + FormattingUtil.toLowerCaseUnderscore(name);
    }

    public MutableComponent getLocalizedName(Material material) {
        return Component.translatable(getUnlocalizedName(material), material.getLocalizedName());
    }

    public String getUnlocalizedName(Material material) {
        String formattedPrefix = FormattingUtil.toLowerCaseUnderscore(this.name);
        String matSpecificKey = String.format("item.%s.%s", material.getModid(), this.idPattern.formatted(material.getName()));
        if (LocalizationUtils.exist(matSpecificKey)) {
            return matSpecificKey;
        }
        if(material.hasProperty(PropertyKey.POLYMER)) {
            String localizationKey = String.format("tagprefix.polymer.%s", formattedPrefix);
            // Not every polymer tag prefix gets a special name
            if(LocalizationUtils.exist(localizationKey)) {
                return localizationKey;
            }
        }

        return getUnlocalizedName();
    }

    public boolean isIgnored(Material material) {
        return ignoredMaterials.containsKey(material);
    }

    @SafeVarargs
    public final void setIgnored(Material material, Supplier<? extends ItemLike>... items) {
        ignoredMaterials.put(material, items);
        if (items.length > 0) {
            ChemicalHelper.registerUnificationItems(this, material, items);
        }
    }

    @SuppressWarnings("unchecked")
    public void setIgnored(Material material, ItemLike... items) {
        // go through setIgnoredBlock to wrap if this is a block prefix
        if (this.doGenerateBlock()) {
            this.setIgnoredBlock(material, Arrays.stream(items).filter(Block.class::isInstance).map(Block.class::cast).toArray(Block[]::new));
        } else {
            this.setIgnored(material, Arrays.stream(items).map(item -> (Supplier<ItemLike>) () -> item).toArray(Supplier[]::new));
        }
    }

    @SuppressWarnings("unchecked")
    public void setIgnoredBlock(Material material, Block... items) {
        this.setIgnored(material, Arrays.stream(items).map(block -> SupplierMemoizer.memoizeBlockSupplier(() -> block)).toArray(Supplier[]::new));
    }

    @SuppressWarnings("unchecked")
    public void setIgnored(Material material) {
        this.ignoredMaterials.put(material, new Supplier[0]);
    }

    public void removeIgnored(Material material) {
        ignoredMaterials.remove(material);
    }

    public Map<Material, Supplier<? extends ItemLike>[]> getIgnored() {
        return new HashMap<>(ignoredMaterials);
    }

    public boolean isAmountModified(Material material) {
        return materialAmounts.containsKey(material);
    }

    public void modifyMaterialAmount(@NotNull Material material, float amount) {
        materialAmounts.put(material, amount);
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
