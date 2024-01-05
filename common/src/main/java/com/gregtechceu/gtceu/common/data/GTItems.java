package com.gregtechceu.gtceu.common.data;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.cover.filter.SimpleFluidFilter;
import com.gregtechceu.gtceu.api.cover.filter.SimpleItemFilter;
import com.gregtechceu.gtceu.api.cover.filter.TagFluidFilter;
import com.gregtechceu.gtceu.api.cover.filter.TagItemFilter;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterial;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.ItemMaterialInfo;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolItem;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.TagPrefixItem;
import com.gregtechceu.gtceu.api.item.component.*;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.MaterialToolTier;
import com.gregtechceu.gtceu.api.item.tool.ToolDefinitionBuilder;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.CompassNode;
import com.gregtechceu.gtceu.api.registry.registrate.CompassSection;
import com.gregtechceu.gtceu.common.data.materials.GTFoods;
import com.gregtechceu.gtceu.common.item.*;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.gregtechceu.gtceu.api.registry.GTRegistries.REGISTRATE;
import static com.gregtechceu.gtceu.common.data.GTCreativeModeTabs.*;
import static com.gregtechceu.gtceu.common.data.GTModels.*;
import static com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName;
import static com.gregtechceu.gtceu.utils.FormattingUtil.toLowerCaseUnder;

/**
 * @author KilaBash
 * @date 2023/2/14
 * @implNote GTItems
 */
public class GTItems {

    //////////////////////////////////////
    //*****     Material Items    ******//
    //////////////////////////////////////

    public static final Map<UnificationEntry, ItemLike> toUnify = new HashMap<>();
    public static final Map<TagPrefix, TagPrefix> purifyMap = new HashMap<>();

    static {
        purifyMap.put(TagPrefix.crushed, TagPrefix.crushedPurified);
        purifyMap.put(TagPrefix.dustImpure, TagPrefix.dust);
        purifyMap.put(TagPrefix.dustPure, TagPrefix.dust);
    }

    public static Table<TagPrefix, Material, ItemEntry<TagPrefixItem>> MATERIAL_ITEMS;

    public static void generateMaterialItems() {
        REGISTRATE.creativeModeTab(() -> MATERIAL_ITEM);
        ImmutableTable.Builder<TagPrefix, Material, ItemEntry<TagPrefixItem>> builder = ImmutableTable.builder();
        for (var tagPrefix : TagPrefix.values()) {
            if (tagPrefix.doGenerateItem()) {
                for (Material material : GTRegistries.MATERIALS) {
                    if (tagPrefix.doGenerateItem(material)) {
                        String first = tagPrefix.invertedName ? toLowerCaseUnder(tagPrefix.name) : material.getName();
                        String last = tagPrefix.invertedName ? material.getName() : toLowerCaseUnder(tagPrefix.name);
                        builder.put(tagPrefix, material, REGISTRATE
                                .item(first + "_" + last, properties -> TagPrefixItem.create(properties, tagPrefix, material))
                                .onRegister(TagPrefixItem::onRegister)
                                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                                .transform(unificationItem(tagPrefix, material))
                                .properties(p -> p.stacksTo(tagPrefix.maxStackSize()))
                                .model(NonNullBiConsumer.noop())
                                .color(() -> TagPrefixItem::tintColor)
                                .onRegister(GTItems::cauldronInteraction)
                                .onRegister(item -> {
                                    switch (tagPrefix.name) {
                                        case "buzzSawBlade", "screwDriverTip", "drillHead", "chainSawHead", "wrenchTip", "turbineBlade" ->
                                                CompassNode.getOrCreate(GTCompassSections.MATERIALS, "tool_heads").addItem(() -> item);
                                        default ->
                                                CompassNode.getOrCreate(GTCompassSections.MATERIALS, FormattingUtil.toLowerCaseUnderscore(tagPrefix.name))
                                                        .iconIfNull(() -> new ItemStackTexture(item)).addTag(tagPrefix.getItemParentTags());
                                    }
                                })
                                .register());
                    }
                }
            }
        }
        MATERIAL_ITEMS = builder.build();
    }

    //////////////////////////////////////
    //*****     Material Tools    ******//
    //////////////////////////////////////
    public final static Table<Material, GTToolType, ItemProviderEntry<IGTTool>> TOOL_ITEMS =
            ArrayTable.create(GTRegistries.MATERIALS.values().stream().filter(mat -> mat.hasProperty(PropertyKey.TOOL)).toList(),
                    GTToolType.getTypes().values().stream().toList());

    public static void generateTools() {
        REGISTRATE.creativeModeTab(() -> TOOL);

        HashMultimap<Integer, Tuple<ResourceLocation, Tier>> tiers = HashMultimap.create();
        for (Tier tier : Tiers.values()) {
            tiers.put(tier.getLevel(), new Tuple<>(getTierName(tier), tier));
        }

        for (Material material : GTRegistries.MATERIALS) {
            if (material.hasProperty(PropertyKey.TOOL)) {
                var tier = material.getToolTier();
                tiers.put(tier.getLevel(), new Tuple<>(GTCEu.id(material.getName()), tier));
            }
        }

        for (Material material : GTRegistries.MATERIALS.values()) {
            if (material.hasProperty(PropertyKey.TOOL)) {
                var property = material.getProperty(PropertyKey.TOOL);
                var tier = material.getToolTier();

                List<ResourceLocation> lower = tiers.values().stream().filter(low -> low.getB().getLevel() == tier.getLevel() - 1).map(Tuple::getA).toList();
                List<ResourceLocation> higher = tiers.values().stream().filter(high -> high.getB().getLevel() == tier.getLevel() + 1).map(Tuple::getA).toList();
                registerToolTier(tier, GTCEu.id(material.getName()), lower, higher);

                for (GTToolType toolType : GTToolType.getTypes().values()) {
                    if (property.hasType(toolType)) {
                        TOOL_ITEMS.put(material, toolType, (ItemProviderEntry<IGTTool>) (ItemProviderEntry<?>) REGISTRATE.item("%s_%s".formatted(tier.material.getName().toLowerCase(Locale.ROOT), toolType.name), p -> toolType.constructor.apply(toolType, tier, material, toolType.toolDefinition, p).asItem())
                                .properties(p -> p.craftRemainder(Items.AIR))
                                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                                .model(NonNullBiConsumer.noop())
                                .color(() -> IGTTool::tintColor)
                                .onRegister(item -> CompassNode.getOrCreate(GTCompassSections.TOOLS, FormattingUtil.toLowerCaseUnderscore(toolType.name)).iconIfNull(() -> new ItemStackTexture(item)).addTag(toolType.itemTags.get(0)))
                                .register());
                    }
                }
            }
        }
    }


    //////////////////////////////////////
    //*******     Misc Items    ********//
    //////////////////////////////////////
    static {
        REGISTRATE.creativeModeTab(() -> ITEM);
    }
    public static ItemEntry<Item> CREDIT_COPPER = REGISTRATE.item("copper_credit", Item::new).lang("Copper Credit").onRegister(compassNodeExist(GTCompassSections.MISC, "credit")).register();
    public static ItemEntry<Item> CREDIT_CUPRONICKEL = REGISTRATE.item("cupronickel_credit", Item::new).lang("Cupronickel Credit").defaultModel().onRegister(compassNodeExist(GTCompassSections.MISC, "credit")).register();
    public static ItemEntry<Item> CREDIT_SILVER = REGISTRATE.item("silver_credit", Item::new).lang("Silver Credit").properties(p -> p.rarity(Rarity.UNCOMMON)).onRegister(compassNodeExist(GTCompassSections.MISC, "credit")).register();
    public static ItemEntry<Item> CREDIT_GOLD = REGISTRATE.item("gold_credit", Item::new).lang("Gold Credit").properties(p -> p.rarity(Rarity.UNCOMMON)).onRegister(compassNodeExist(GTCompassSections.MISC, "credit")).register();
    public static ItemEntry<Item> CREDIT_PLATINUM = REGISTRATE.item("platinum_credit", Item::new).lang("Platinum Credit").properties(p -> p.rarity(Rarity.RARE)).onRegister(compassNodeExist(GTCompassSections.MISC, "credit")).register();
    public static ItemEntry<Item> CREDIT_OSMIUM = REGISTRATE.item("osmium_credit", Item::new).lang("Osmium Credit").properties(p -> p.rarity(Rarity.RARE)).onRegister(compassNodeExist(GTCompassSections.MISC, "credit")).register();
    public static ItemEntry<Item> CREDIT_NAQUADAH = REGISTRATE.item("naquadah_credit", Item::new).lang("Naquadah Credit").properties(p -> p.rarity(Rarity.EPIC)).onRegister(compassNodeExist(GTCompassSections.MISC, "credit")).register();
    public static ItemEntry<Item> CREDIT_NEUTRONIUM = REGISTRATE.item("neutronium_credit", Item::new).lang("Neutronium Credit").properties(p -> p.rarity(Rarity.EPIC)).onRegister(compassNodeExist(GTCompassSections.MISC, "credit")).register();
    public static ItemEntry<Item> COIN_GOLD_ANCIENT = REGISTRATE.item("ancient_gold_coin", Item::new).lang("Ancient Gold Coin").properties(p -> p.rarity(Rarity.RARE))
            .onRegister(compassNode(GTCompassSections.MISC))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Gold, GTValues.M / 4)))).register();
    public static ItemEntry<Item> COIN_DOGE = REGISTRATE.item("doge_coin", Item::new).lang("Doge Coin").properties(p -> p.rarity(Rarity.EPIC))
            .onRegister(compassNode(GTCompassSections.MISC))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Brass, GTValues.M / 4)))).register();
    public static ItemEntry<Item> COIN_CHOCOLATE = REGISTRATE.item("chocolate_coin", Item::new)
            .lang("Chocolate Coin")
            .properties(p -> p.rarity(Rarity.EPIC).food(GTFoods.CHOCOLATE))
            .onRegister(compassNode(GTCompassSections.MISC))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Gold, GTValues.M / 4)))).register();
    public static ItemEntry<Item> COMPRESSED_CLAY = REGISTRATE.item("compressed_clay", Item::new)
            .lang("Compressed Clay")
            .onRegister(compassNode(GTCompassSections.MISC))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, GTValues.M)))).register();
    public static ItemEntry<Item> COMPRESSED_COKE_CLAY = REGISTRATE.item("compressed_coke_clay", Item::new)
            .lang("Compressed Coke Clay")
            .onRegister(compassNode(GTCompassSections.MISC))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, GTValues.M)))).register();
    public static ItemEntry<Item> COMPRESSED_FIRECLAY = REGISTRATE.item("compressed_fireclay", Item::new)
            .lang("Compressed Fireclay")
            .onRegister(compassNode(GTCompassSections.MISC))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Fireclay, GTValues.M)))).register();
    public static ItemEntry<Item> FIRECLAY_BRICK = REGISTRATE.item("firebrick", Item::new)
            .lang("Firebrick")
            .onRegister(compassNode(GTCompassSections.MISC))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Fireclay, GTValues.M)))).register();
    public static ItemEntry<Item> COKE_OVEN_BRICK = REGISTRATE.item("coke_oven_brick", Item::new)
            .lang("Coke Oven Brick")
            .onRegister(compassNode(GTCompassSections.MISC))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, GTValues.M)))).register();
    public static ItemEntry<Item> WOODEN_FORM_EMPTY = REGISTRATE.item("empty_wooden_form", Item::new)
            .lang("Empty Wooden Form")
            .onRegister(compassNode(GTCompassSections.MISC)).register();
    public static ItemEntry<ComponentItem> WOODEN_FORM_BRICK = REGISTRATE.item("brick_wooden_form", ComponentItem::create)
            .lang("Brick Wooden Form")
            .properties(p -> p.craftRemainder(Items.AIR))
            .onRegister(compassNode(GTCompassSections.MISC))
            .onRegister(attach((IRecipeRemainder) ItemStack::copy)).register();

    public static ItemEntry<Item> SHAPE_EMPTY = REGISTRATE.item("empty_mold", Item::new)
            .lang("Empty Mold")
            .onRegister(compassNode(GTCompassSections.MISC))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();

    public static final ItemEntry<Item>[] SHAPE_MOLDS = new ItemEntry[13];
    public static final ItemEntry<Item> SHAPE_MOLD_PLATE;
    public static final ItemEntry<Item> SHAPE_MOLD_GEAR;
    public static final ItemEntry<Item> SHAPE_MOLD_CREDIT;
    public static final ItemEntry<Item> SHAPE_MOLD_BOTTLE;
    public static final ItemEntry<Item> SHAPE_MOLD_INGOT;
    public static final ItemEntry<Item> SHAPE_MOLD_BALL;
    public static final ItemEntry<Item> SHAPE_MOLD_BLOCK;
    public static final ItemEntry<Item> SHAPE_MOLD_NUGGET;
    public static final ItemEntry<Item> SHAPE_MOLD_CYLINDER;
    public static final ItemEntry<Item> SHAPE_MOLD_ANVIL;
    public static final ItemEntry<Item> SHAPE_MOLD_NAME;
    public static final ItemEntry<Item> SHAPE_MOLD_GEAR_SMALL;
    public static final ItemEntry<Item> SHAPE_MOLD_ROTOR;

    static {
        SHAPE_MOLDS[0] = SHAPE_MOLD_PLATE = REGISTRATE.item("plate_casting_mold", Item::new)
                .lang("Casting Mold (Plate)").onRegister(compassNodeExist(GTCompassSections.MISC, "mold")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_MOLDS[1] = SHAPE_MOLD_GEAR = REGISTRATE.item("gear_casting_mold", Item::new)
                .lang("Casting Mold (Gear)").onRegister(compassNodeExist(GTCompassSections.MISC, "mold")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_MOLDS[2] = SHAPE_MOLD_CREDIT = REGISTRATE.item("credit_casting_mold", Item::new)
                .lang("Casting Mold (Coinage)").onRegister(compassNodeExist(GTCompassSections.MISC, "mold")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_MOLDS[3] = SHAPE_MOLD_BOTTLE = REGISTRATE.item("bottle_casting_mold", Item::new)
                .lang("Casting Mold (Bottle)").onRegister(compassNodeExist(GTCompassSections.MISC, "mold")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_MOLDS[4] = SHAPE_MOLD_INGOT = REGISTRATE.item("ingot_casting_mold", Item::new)
                .lang("Casting Mold (Ingot)").onRegister(compassNodeExist(GTCompassSections.MISC, "mold")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_MOLDS[5] = SHAPE_MOLD_BALL = REGISTRATE.item("ball_casting_mold", Item::new)
                .lang("Casting Mold (Ball)").onRegister(compassNodeExist(GTCompassSections.MISC, "mold")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_MOLDS[6] = SHAPE_MOLD_BLOCK = REGISTRATE.item("block_casting_mold", Item::new)
                .lang("Casting Mold (Block)").onRegister(compassNodeExist(GTCompassSections.MISC, "mold")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_MOLDS[7] = SHAPE_MOLD_NUGGET = REGISTRATE.item("nugget_casting_mold", Item::new)
                .lang("Casting Mold (Nugget)").onRegister(compassNodeExist(GTCompassSections.MISC, "mold")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_MOLDS[8] = SHAPE_MOLD_CYLINDER = REGISTRATE.item("cylinder_casting_mold", Item::new)
                .lang("Casting Mold (Cylinder)").onRegister(compassNodeExist(GTCompassSections.MISC, "mold")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_MOLDS[9] = SHAPE_MOLD_ANVIL = REGISTRATE.item("anvil_casting_mold", Item::new)
                .lang("Casting Mold (Anvil)").onRegister(compassNodeExist(GTCompassSections.MISC, "mold")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_MOLDS[10] = SHAPE_MOLD_NAME = REGISTRATE.item("name_casting_mold", Item::new)
                .lang("Casting Mold (Name)").onRegister(compassNodeExist(GTCompassSections.MISC, "mold")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_MOLDS[11] = SHAPE_MOLD_GEAR_SMALL = REGISTRATE.item("small_gear_casting_mold", Item::new)
                .lang("Casting Mold (Small Gear)").onRegister(compassNodeExist(GTCompassSections.MISC, "mold")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_MOLDS[12] = SHAPE_MOLD_ROTOR = REGISTRATE.item("rotor_casting_mold", Item::new)
                .lang("Casting Mold (Rotor)").onRegister(compassNodeExist(GTCompassSections.MISC, "mold")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
    }

    public static final ItemEntry<Item>[] SHAPE_EXTRUDERS = new ItemEntry[27];
    public static ItemEntry<Item> SHAPE_EXTRUDER_PLATE;
    public static ItemEntry<Item> SHAPE_EXTRUDER_ROD;
    public static ItemEntry<Item> SHAPE_EXTRUDER_BOLT;
    public static ItemEntry<Item> SHAPE_EXTRUDER_RING;
    public static ItemEntry<Item> SHAPE_EXTRUDER_CELL;
    public static ItemEntry<Item> SHAPE_EXTRUDER_INGOT;
    public static ItemEntry<Item> SHAPE_EXTRUDER_WIRE;
    public static ItemEntry<Item> SHAPE_EXTRUDER_PIPE_TINY;
    public static ItemEntry<Item> SHAPE_EXTRUDER_PIPE_SMALL;
    public static ItemEntry<Item> SHAPE_EXTRUDER_PIPE_NORMAL;
    public static ItemEntry<Item> SHAPE_EXTRUDER_PIPE_LARGE;
    public static ItemEntry<Item> SHAPE_EXTRUDER_PIPE_HUGE;
    public static ItemEntry<Item> SHAPE_EXTRUDER_BLOCK;
    public static ItemEntry<Item> SHAPE_EXTRUDER_GEAR;
    public static ItemEntry<Item> SHAPE_EXTRUDER_BOTTLE;
    public static ItemEntry<Item> SHAPE_EXTRUDER_FOIL;
    public static ItemEntry<Item> SHAPE_EXTRUDER_GEAR_SMALL;
    public static ItemEntry<Item> SHAPE_EXTRUDER_ROD_LONG;
    public static ItemEntry<Item> SHAPE_EXTRUDER_ROTOR;

    static {
        SHAPE_EXTRUDERS[0] = SHAPE_EXTRUDER_PLATE = REGISTRATE.item("plate_extruder_mold", Item::new)
                .lang("Extruder Mold (Plate)").onRegister(compassNodeExist(GTCompassSections.MISC, "extruder_shape")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[1] = SHAPE_EXTRUDER_ROD = REGISTRATE.item("rod_extruder_mold", Item::new)
                .lang("Extruder Mold (Rod)").onRegister(compassNodeExist(GTCompassSections.MISC, "extruder_shape")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[2] = SHAPE_EXTRUDER_BOLT = REGISTRATE.item("bolt_extruder_mold", Item::new)
                .lang("Extruder Mold (Bolt)").onRegister(compassNodeExist(GTCompassSections.MISC, "extruder_shape")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[3] = SHAPE_EXTRUDER_RING = REGISTRATE.item("ring_extruder_mold", Item::new)
                .lang("Extruder Mold (Ring)").onRegister(compassNodeExist(GTCompassSections.MISC, "extruder_shape")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[4] = SHAPE_EXTRUDER_CELL = REGISTRATE.item("cell_extruder_mold", Item::new)
                .lang("Extruder Mold (Cell)").onRegister(compassNodeExist(GTCompassSections.MISC, "extruder_shape")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[5] = SHAPE_EXTRUDER_INGOT = REGISTRATE.item("ingot_extruder_mold", Item::new)
                .lang("Extruder Mold (Ingot)").onRegister(compassNodeExist(GTCompassSections.MISC, "extruder_shape")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[6] = SHAPE_EXTRUDER_WIRE = REGISTRATE.item("wire_extruder_mold", Item::new)
                .lang("Extruder Mold (Wire)").onRegister(compassNodeExist(GTCompassSections.MISC, "extruder_shape")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[7] = SHAPE_EXTRUDER_PIPE_TINY = REGISTRATE.item("tiny_pipe_extruder_mold", Item::new)
                .lang("Extruder Mold (Tiny Pipe)").onRegister(compassNodeExist(GTCompassSections.MISC, "extruder_shape")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[8] = SHAPE_EXTRUDER_PIPE_SMALL = REGISTRATE.item("small_pipe_extruder_mold", Item::new)
                .lang("Extruder Mold (Small Pipe)").onRegister(compassNodeExist(GTCompassSections.MISC, "extruder_shape")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[9] = SHAPE_EXTRUDER_PIPE_NORMAL = REGISTRATE.item("normal_pipe_extruder_mold", Item::new)
                .lang("Extruder Mold (Normal Pipe)").onRegister(compassNodeExist(GTCompassSections.MISC, "extruder_shape")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[10] = SHAPE_EXTRUDER_PIPE_LARGE = REGISTRATE.item("large_pipe_extruder_mold", Item::new)
                .lang("Extruder Mold (Large Pipe)").onRegister(compassNodeExist(GTCompassSections.MISC, "extruder_shape")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[11] = SHAPE_EXTRUDER_PIPE_HUGE = REGISTRATE.item("huge_pipe_extruder_mold", Item::new)
                .lang("Extruder Mold (Huge Pipe)").onRegister(compassNodeExist(GTCompassSections.MISC, "extruder_shape")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[12] = SHAPE_EXTRUDER_BLOCK = REGISTRATE.item("block_extruder_mold", Item::new)
                .lang("Extruder Mold (Block)").onRegister(compassNodeExist(GTCompassSections.MISC, "extruder_shape")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        // Extruder Shapes index 13-20 (inclusive), id 44-51 (inclusive) are unused
        SHAPE_EXTRUDERS[21] = SHAPE_EXTRUDER_GEAR = REGISTRATE.item("gear_extruder_mold", Item::new)
                .lang("Extruder Mold (Gear)").onRegister(compassNodeExist(GTCompassSections.MISC, "extruder_shape")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[22] = SHAPE_EXTRUDER_BOTTLE = REGISTRATE.item("bottle_extruder_mold", Item::new)
                .lang("Extruder Mold (Bottle)").onRegister(compassNodeExist(GTCompassSections.MISC, "extruder_shape")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[23] = SHAPE_EXTRUDER_FOIL = REGISTRATE.item("foil_extruder_mold", Item::new)
                .lang("Extruder Mold (Foil)").onRegister(compassNodeExist(GTCompassSections.MISC, "extruder_shape")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[24] = SHAPE_EXTRUDER_GEAR_SMALL = REGISTRATE.item("small_gear_extruder_mold", Item::new)
                .lang("Extruder Mold (Small Gear)").onRegister(compassNodeExist(GTCompassSections.MISC, "extruder_shape")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[25] = SHAPE_EXTRUDER_ROD_LONG = REGISTRATE.item("long_rod_extruder_mold", Item::new)
                .lang("Extruder Mold (Long Rod)").onRegister(compassNodeExist(GTCompassSections.MISC, "extruder_shape")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[26] = SHAPE_EXTRUDER_ROTOR = REGISTRATE.item("rotor_extruder_mold", Item::new)
                .lang("Extruder Mold (Rotor)").onRegister(compassNodeExist(GTCompassSections.MISC, "extruder_shape")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
    }
    public static ItemEntry<Item> SPRAY_EMPTY = REGISTRATE.item("empty_spray_can", Item::new).onRegister(compassNode(GTCompassSections.ITEMS)).lang("Spray Can (Empty)").register();
    public static ItemEntry<ComponentItem> SPRAY_SOLVENT = REGISTRATE.item("solvent_spray_can", ComponentItem::create)
            .lang("Spray Can (Solvent)")
            .properties(p -> p.stacksTo(1))
            .onRegister(compassNode(GTCompassSections.ITEMS))
            .onRegister(attach(new ColorSprayBehaviour(() -> SPRAY_EMPTY.asStack(), 1024, -1))).register();

    @Environment(EnvType.CLIENT)
    public static ItemColor cellColor() {
        return (itemStack, index) -> {
            if (index == 1) {
                var held = FluidTransferHelper.getFluidContained(itemStack);
                if (held != null) {
                    // TODO render cell with a real fluid texture in the future?
                    if (held.getFluid() == Fluids.LAVA) {
                        return 0xFFFF7000;
                    }
                    return FluidHelper.getColor(held);
                }
            }
            return -1;
        };
    }

    public static ICustomDescriptionId cellName() {
        return itemStack -> {
            var held = FluidTransferHelper.getFluidContained(itemStack);
            var prefix = LocalizationUtils.format("Empty");
            if (held != null && !held.isEmpty()) {
                prefix = FluidHelper.getDisplayName(held).getString();
            }
            return "%s %s".formatted(prefix, LocalizationUtils.format(itemStack.getItem().getDescriptionId()));
        };
    }

    public static ItemEntry<ComponentItem> FLUID_CELL = REGISTRATE.item("fluid_cell", ComponentItem::create)
            .model(GTModels::cellModel)
            .color(() -> GTItems::cellColor)
            .onRegister(compassNodeExist(GTCompassSections.ITEMS, "empty_cell"))
            .onRegister(modelPredicate(GTCEu.id("fluid_cell"), (itemStack) -> FluidTransferHelper.getFluidContained(itemStack) == null ? 0f : 1f))
            .onRegister(attach(ThermalFluidStats.create((int)FluidHelper.getBucket(), 1800, true, false, false, false, false), new ItemFluidContainer(), cellName())).register();
    public static ItemEntry<ComponentItem> FLUID_CELL_UNIVERSAL = REGISTRATE.item("universal_fluid_cell", ComponentItem::create)
            .lang("Universal Cell")
            .model(GTModels::cellModel)
            .color(() -> GTItems::cellColor)
            .onRegister(compassNodeExist(GTCompassSections.ITEMS, "empty_cell"))
            .onRegister(modelPredicate(GTCEu.id("fluid_cell"), (itemStack) -> FluidTransferHelper.getFluidContained(itemStack) == null ? 0f : 1f))
            .onRegister(attach(cellName(), ThermalFluidStats.create((int)FluidHelper.getBucket(), 1800, true, false, false, false, true), new ItemFluidContainer())).register();
    public static ItemEntry<ComponentItem> FLUID_CELL_LARGE_STEEL = REGISTRATE.item("steel_fluid_cell", ComponentItem::create)
            .lang("Steel Cell")
            .model(GTModels::cellModel)
            .color(() -> GTItems::cellColor)
            .onRegister(compassNodeExist(GTCompassSections.ITEMS, "empty_cell"))
            .onRegister(modelPredicate(GTCEu.id("fluid_cell"), (itemStack) -> FluidTransferHelper.getFluidContained(itemStack) == null ? 0f : 1f))
            .onRegister(attach(cellName(), ThermalFluidStats.create((int)FluidHelper.getBucket() * 8, GTMaterials.Steel.getProperty(PropertyKey.FLUID_PIPE).getMaxFluidTemperature(), true, false, false, false, true), new ItemFluidContainer()))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
    public static ItemEntry<ComponentItem> FLUID_CELL_LARGE_ALUMINIUM = REGISTRATE.item("aluminium_fluid_cell", ComponentItem::create)
            .lang("Aluminium Cell")
            .model(GTModels::cellModel)
            .color(() -> GTItems::cellColor)
            .onRegister(compassNodeExist(GTCompassSections.ITEMS, "empty_cell"))
            .onRegister(modelPredicate(GTCEu.id("fluid_cell"), (itemStack) -> FluidTransferHelper.getFluidContained(itemStack) == null ? 0f : 1f))
            .onRegister(attach(cellName(), ThermalFluidStats.create((int)FluidHelper.getBucket() * 32, GTMaterials.Aluminium.getProperty(PropertyKey.FLUID_PIPE).getMaxFluidTemperature(), true, false, false, false, true), new ItemFluidContainer()))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Aluminium, GTValues.M * 4)))).register();
    public static ItemEntry<ComponentItem> FLUID_CELL_LARGE_STAINLESS_STEEL = REGISTRATE.item("stainless_steel_fluid_cell", ComponentItem::create)
            .lang("Stainless Steel Cell")
            .model(GTModels::cellModel)
            .color(() -> GTItems::cellColor)
            .onRegister(compassNodeExist(GTCompassSections.ITEMS, "empty_cell"))
            .onRegister(modelPredicate(GTCEu.id("fluid_cell"), (itemStack) -> FluidTransferHelper.getFluidContained(itemStack) == null ? 0f : 1f))
            .onRegister(attach(cellName(), ThermalFluidStats.create((int)FluidHelper.getBucket() * 64, GTMaterials.StainlessSteel.getProperty(PropertyKey.FLUID_PIPE).getMaxFluidTemperature(), true, false, false, false, true), new ItemFluidContainer()))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.StainlessSteel, GTValues.M * 6)))).register();
    public static ItemEntry<ComponentItem> FLUID_CELL_LARGE_TITANIUM = REGISTRATE.item("titanium_fluid_cell", ComponentItem::create)
            .lang("Titanium Cell")
            .model(GTModels::cellModel)
            .color(() -> GTItems::cellColor)
            .onRegister(compassNodeExist(GTCompassSections.ITEMS, "empty_cell"))
            .onRegister(modelPredicate(GTCEu.id("fluid_cell"), (itemStack) -> FluidTransferHelper.getFluidContained(itemStack) == null ? 0f : 1f))
            .onRegister(attach(cellName(), ThermalFluidStats.create((int)FluidHelper.getBucket() * 128, GTMaterials.TungstenSteel.getProperty(PropertyKey.FLUID_PIPE).getMaxFluidTemperature(), true, false, false, false, true), new ItemFluidContainer()))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.TungstenSteel, GTValues.M * 6)))).register();
    public static ItemEntry<ComponentItem> FLUID_CELL_LARGE_TUNGSTEN_STEEL = REGISTRATE.item("tungstensteel_fluid_cell", ComponentItem::create)
            .lang("Tungstensteel Cell")
            .model(GTModels::cellModel)
            .color(() -> GTItems::cellColor)
            .properties(p -> p.stacksTo(32))
            .onRegister(compassNodeExist(GTCompassSections.ITEMS, "empty_cell"))
            .onRegister(modelPredicate(GTCEu.id("fluid_cell"), (itemStack) -> FluidTransferHelper.getFluidContained(itemStack) == null ? 0f : 1f))
            .onRegister(attach(cellName(), ThermalFluidStats.create((int)FluidHelper.getBucket() * 512, GTMaterials.TungstenSteel.getProperty(PropertyKey.FLUID_PIPE).getMaxFluidTemperature(), true, false, false, false, true), new ItemFluidContainer()))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.TungstenSteel, GTValues.M * 8)))).register();
    public static ItemEntry<ComponentItem> FLUID_CELL_GLASS_VIAL = REGISTRATE.item("glass_vial", ComponentItem::create)
            .model(GTModels::cellModel)
            .color(() -> GTItems::cellColor)
            .onRegister(compassNode(GTCompassSections.ITEMS))
            .onRegister(modelPredicate(GTCEu.id("fluid_cell"), (itemStack) -> FluidTransferHelper.getFluidContained(itemStack) == null ? 0f : 1f))
            .onRegister(attach(cellName(), ThermalFluidStats.create((int)FluidHelper.getBucket(), 1200, false, true, false, false, true), new ItemFluidContainer()))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Glass, GTValues.M / 4)))).register();

    // TODO Lighter
    public static ItemEntry<Item> TOOL_MATCHES;
    public static ItemEntry<Item> TOOL_MATCHBOX;
    public static ItemEntry<Item> TOOL_LIGHTER_INVAR;
    public static ItemEntry<Item> TOOL_LIGHTER_PLATINUM;

    public static ItemEntry<Item> CARBON_FIBERS = REGISTRATE.item("carbon_fibers", Item::new).onRegister(compassNodeExist(GTCompassSections.MISC, "raw_carbon_fibers")).lang("Raw Carbon Fibers").register();
    public static ItemEntry<Item> CARBON_MESH = REGISTRATE.item("carbon_fiber_mesh", Item::new).onRegister(compassNodeExist(GTCompassSections.MISC, "carbon_fiber_mesh")).lang("Carbon Fiber Mesh").register();
    public static ItemEntry<Item> CARBON_FIBER_PLATE = REGISTRATE.item("carbon_fiber_plate", Item::new).onRegister(compassNodeExist(GTCompassSections.MISC, "carbon_fiber_plate")).lang("Carbon Fiber Plate").register();
    public static ItemEntry<Item> DUCT_TAPE = REGISTRATE.item("duct_tape", Item::new).onRegister(compassNode(GTCompassSections.MISC)).lang("BrainTech Aerospace Advanced Reinforced Duct Tape FAL-84").onRegister(compassNode(GTCompassSections.ITEMS)).register();

    public static ItemEntry<Item> NEUTRON_REFLECTOR = REGISTRATE.item("neutron_reflector", Item::new).onRegister(compassNodeExist(GTCompassSections.ITEMS, "iridium_neutron_reflector")).lang("Iridium Neutron Reflector").register();

    public static ItemEntry<Item> BATTERY_HULL_LV  = REGISTRATE.item("lv_battery_hull", Item::new).lang("Small Battery Hull").onRegister(compassNodeExist(GTCompassSections.MISC, "battery_hull")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.BatteryAlloy, GTValues.M)))).register();
    public static ItemEntry<Item> BATTERY_HULL_MV  = REGISTRATE.item("mv_battery_hull", Item::new).lang("Medium Battery Hull").onRegister(compassNodeExist(GTCompassSections.MISC, "battery_hull")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.BatteryAlloy, GTValues.M * 3)))).register();
    public static ItemEntry<Item> BATTERY_HULL_HV  = REGISTRATE.item("hv_battery_hull", Item::new).lang("Large Battery Hull").onRegister(compassNodeExist(GTCompassSections.MISC, "battery_hull")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.BatteryAlloy, GTValues.M * 9)))).register();
    public static ItemEntry<Item> BATTERY_HULL_SMALL_VANADIUM  = REGISTRATE.item("ev_battery_hull", Item::new).lang("Small Vanadium Battery Hull").onRegister(compassNodeExist(GTCompassSections.MISC, "battery_hull")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.BlueSteel, GTValues.M * 2)))).register();
    public static ItemEntry<Item> BATTERY_HULL_MEDIUM_VANADIUM  = REGISTRATE.item("iv_battery_hull", Item::new).lang("Medium Vanadium Battery Hull").onRegister(compassNodeExist(GTCompassSections.MISC, "battery_hull")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.RoseGold, GTValues.M * 6)))).register();
    public static ItemEntry<Item> BATTERY_HULL_LARGE_VANADIUM  = REGISTRATE.item("luv_battery_hull", Item::new).lang("Large Vanadium Battery Hull").onRegister(compassNodeExist(GTCompassSections.MISC, "battery_hull")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.RedSteel, GTValues.M * 18)))).register();
    public static ItemEntry<Item> BATTERY_HULL_MEDIUM_NAQUADRIA  = REGISTRATE.item("zpm_battery_hull", Item::new).lang("Medium Naquadria Battery Hull").onRegister(compassNodeExist(GTCompassSections.MISC, "battery_hull")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Europium, GTValues.M * 6)))).register();
    public static ItemEntry<Item> BATTERY_HULL_LARGE_NAQUADRIA  = REGISTRATE.item("uv_battery_hull", Item::new).lang("Large Naquadria Battery Hull").onRegister(compassNodeExist(GTCompassSections.MISC, "battery_hull")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Americium, GTValues.M * 18)))).register();

    public static ItemEntry<ComponentItem> BATTERY_ULV_TANTALUM = REGISTRATE.item("tantalum_capacitor", ComponentItem::create)
            .lang("Tantalum Capacitor")
            .onRegister(compassNodeExist(GTCompassSections.BATTERIES, "tantalum_capacitor"))
            .onRegister(attach(ElectricStats.createRechargeableBattery(1000, GTValues.ULV)))
            .tag(CustomTags.ULV_BATTERIES).register();

    public static ItemEntry<ComponentItem> BATTERY_LV_SODIUM = REGISTRATE.item("lv_sodium_battery", ComponentItem::create)
            .lang("Small Sodium Battery")
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(compassNodeExist(GTCompassSections.BATTERIES, "small_sodium_battery"))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(80000, GTValues.LV)))
            .tag(CustomTags.LV_BATTERIES).register();
    public static ItemEntry<ComponentItem> BATTERY_MV_SODIUM = REGISTRATE.item("mv_sodium_battery", ComponentItem::create)
            .lang("Medium Sodium Battery")
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(compassNodeExist(GTCompassSections.BATTERIES, "medium_sodium_battery"))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(360000, GTValues.MV)))
            .tag(CustomTags.MV_BATTERIES).register();
    public static ItemEntry<ComponentItem> BATTERY_HV_SODIUM = REGISTRATE.item("hv_sodium_battery", ComponentItem::create)
            .lang("Large Sodium Battery")
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(compassNodeExist(GTCompassSections.BATTERIES, "large_sodium_battery"))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(1200000, GTValues.HV)))
            .tag(CustomTags.HV_BATTERIES).register();

    public static ItemEntry<ComponentItem> BATTERY_LV_LITHIUM = REGISTRATE.item("lv_lithium_battery", ComponentItem::create)
            .lang("Small Lithium Battery")
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(compassNodeExist(GTCompassSections.BATTERIES, "small_lithium_battery"))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(120000, GTValues.LV)))
            .tag(CustomTags.LV_BATTERIES).register();
    public static ItemEntry<ComponentItem> BATTERY_MV_LITHIUM = REGISTRATE.item("mv_lithium_battery", ComponentItem::create)
            .lang("Medium Lithium Battery")
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(compassNodeExist(GTCompassSections.BATTERIES, "medium_lithium_battery"))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(420000, GTValues.MV)))
            .tag(CustomTags.MV_BATTERIES).register();
    public static ItemEntry<ComponentItem> BATTERY_HV_LITHIUM = REGISTRATE.item("hv_lithium_battery", ComponentItem::create)
            .lang("Large Lithium Battery")
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(compassNodeExist(GTCompassSections.BATTERIES, "large_lithium_battery"))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(1800000, GTValues.HV)))
            .tag(CustomTags.HV_BATTERIES).register();

    public static ItemEntry<ComponentItem> BATTERY_LV_CADMIUM = REGISTRATE.item("lv_cadmium_battery", ComponentItem::create)
            .lang("Small Cadmium Battery")
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(compassNodeExist(GTCompassSections.BATTERIES, "small_cadmium_battery"))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(100000, GTValues.LV)))
            .tag(CustomTags.LV_BATTERIES).register();
    public static ItemEntry<ComponentItem> BATTERY_MV_CADMIUM = REGISTRATE.item("mv_cadmium_battery", ComponentItem::create)
            .lang("Medium Cadmium Battery")
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(compassNodeExist(GTCompassSections.BATTERIES, "medium_cadmium_battery"))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(400000, GTValues.MV)))
            .tag(CustomTags.MV_BATTERIES).register();
    public static ItemEntry<ComponentItem> BATTERY_HV_CADMIUM = REGISTRATE.item("hv_cadmium_battery", ComponentItem::create)
            .lang("Large Cadmium Battery")
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(compassNodeExist(GTCompassSections.BATTERIES, "large_cadmium_battery"))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(1600000, GTValues.HV)))
            .tag(CustomTags.HV_BATTERIES).register();

    public static ItemEntry<ComponentItem> ENERGIUM_CRYSTAL = REGISTRATE.item("energy_crystal", ComponentItem::create)
            .lang("Energium Crystal")
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(compassNodeExist(GTCompassSections.BATTERIES, "energy_crystal"))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(6_400_000L, GTValues.HV)))
            .tag(CustomTags.HV_BATTERIES).register();
    public static ItemEntry<ComponentItem> LAPOTRON_CRYSTAL = REGISTRATE.item("lapotron_crystal", ComponentItem::create)
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(compassNodeExist(GTCompassSections.BATTERIES, "lapotron_crystal"))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(25_000_000L, GTValues.EV)))
            .tag(CustomTags.EV_BATTERIES).register();

    public static ItemEntry<ComponentItem> BATTERY_EV_VANADIUM = REGISTRATE.item("ev_vanadium_battery", ComponentItem::create)
            .lang("Small Vanadium Battery")
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(compassNodeExist(GTCompassSections.BATTERIES, "small_vanadium_battery"))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(10_240_000L, GTValues.EV)))
            .tag(CustomTags.EV_BATTERIES).register();
    public static ItemEntry<ComponentItem> BATTERY_IV_VANADIUM = REGISTRATE.item("iv_vanadium_battery", ComponentItem::create)
            .lang("Medium Vanadium Battery")
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(compassNodeExist(GTCompassSections.BATTERIES, "medium_vanadium_battery"))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(40_960_000L, GTValues.IV)))
            .tag(CustomTags.IV_BATTERIES).register();
    public static ItemEntry<ComponentItem> BATTERY_LUV_VANADIUM = REGISTRATE.item("luv_vanadium_battery", ComponentItem::create)
            .lang("Large Vanadium Battery")
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(compassNodeExist(GTCompassSections.BATTERIES, "large_vanadium_battery"))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(163_840_000L, GTValues.LuV)))
            .tag(CustomTags.LuV_BATTERIES).register();

    public static ItemEntry<ComponentItem> BATTERY_ZPM_NAQUADRIA = REGISTRATE.item("zpm_naquadria_battery", ComponentItem::create)
            .lang("Medium Naquadria Battery")
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(compassNodeExist(GTCompassSections.BATTERIES, "medium_naquadria_battery"))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(655_360_000L, GTValues.ZPM)))
            .tag(CustomTags.ZPM_BATTERIES).register();
    public static ItemEntry<ComponentItem> BATTERY_UV_NAQUADRIA = REGISTRATE.item("uv_naquadria_battery", ComponentItem::create)
            .lang("Large Naquadria Battery")
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(compassNodeExist(GTCompassSections.BATTERIES, "large_naquadria_battery"))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(2_621_440_000L, GTValues.UV)))
            .tag(CustomTags.UV_BATTERIES).register();

    public static ItemEntry<ComponentItem> ENERGY_LAPOTRONIC_ORB = REGISTRATE.item("lapotronic_energy_orb", ComponentItem::create)
            .lang("Lapotronic Energy Orb")
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(compassNodeExist(GTCompassSections.BATTERIES, "lapotronic_energy_orb"))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(250_000_000L, GTValues.IV)))
            .tag(CustomTags.IV_BATTERIES).register();
    public static ItemEntry<ComponentItem> ENERGY_LAPOTRONIC_ORB_CLUSTER = REGISTRATE.item("lapotronic_energy_orb_cluster", ComponentItem::create)
            .lang("Lapotronic Energy Orb Cluster")
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(compassNodeExist(GTCompassSections.BATTERIES, "lapotronic_energy_orb_cluster"))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(1_000_000_000L, GTValues.LuV)))
            .tag(CustomTags.LuV_BATTERIES).register();

    public static ItemEntry<ComponentItem> ENERGY_MODULE = REGISTRATE.item("energy_module", ComponentItem::create)
            .lang("Energy Module")
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(compassNodeExist(GTCompassSections.BATTERIES, "energy_module"))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(4_000_000_000L, GTValues.ZPM)))
            .tag(CustomTags.ZPM_BATTERIES).register();
    public static ItemEntry<ComponentItem> ENERGY_CLUSTER = REGISTRATE.item("energy_cluster", ComponentItem::create)
            .lang("Energy Cluster")
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(compassNodeExist(GTCompassSections.BATTERIES, "energy_cluster"))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(20_000_000_000L, GTValues.UV)))
            .tag(CustomTags.UV_BATTERIES).register();

    public static ItemEntry<ComponentItem> ZERO_POINT_MODULE = REGISTRATE.item("zero_point_module", ComponentItem::create)
            .lang("Zero Point Module")
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(compassNodeExist(GTCompassSections.BATTERIES, "zero_point_module"))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createBattery(2000000000000L, GTValues.ZPM, true))).register();
    public static ItemEntry<ComponentItem> ULTIMATE_BATTERY = REGISTRATE.item("max_battery", ComponentItem::create)
            .lang("Ultimate Battery")
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(compassNodeExist(GTCompassSections.BATTERIES, "ultimate_battery"))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(Long.MAX_VALUE, GTValues.UHV)))
            .tag(CustomTags.UHV_BATTERIES).register();

    public static ItemEntry<Item> ELECTRIC_MOTOR_LV = REGISTRATE.item("lv_electric_motor", Item::new).lang("LV Electric Motor").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "electric_motor")).register();
    public static ItemEntry<Item> ELECTRIC_MOTOR_MV = REGISTRATE.item("mv_electric_motor", Item::new).lang("MV Electric Motor").onRegister(compassNodeExist(GTCompassSections.MISC, "electric_motor")).register();
    public static ItemEntry<Item> ELECTRIC_MOTOR_HV = REGISTRATE.item("hv_electric_motor", Item::new).lang("HV Electric Motor").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "electric_motor")).register();
    public static ItemEntry<Item> ELECTRIC_MOTOR_EV = REGISTRATE.item("ev_electric_motor", Item::new).lang("EV Electric Motor").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "electric_motor")).register();
    public static ItemEntry<Item> ELECTRIC_MOTOR_IV = REGISTRATE.item("iv_electric_motor", Item::new).lang("IV Electric Motor").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "electric_motor")).register();
    public static ItemEntry<Item> ELECTRIC_MOTOR_LuV = REGISTRATE.item("luv_electric_motor", Item::new).lang("LuV Electric Motor").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "electric_motor")).register();
    public static ItemEntry<Item> ELECTRIC_MOTOR_ZPM = REGISTRATE.item("zpm_electric_motor", Item::new).lang("ZPM Electric Motor").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "electric_motor")).register();
    public static ItemEntry<Item> ELECTRIC_MOTOR_UV = REGISTRATE.item("uv_electric_motor", Item::new).lang("UV Electric Motor").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "electric_motor")).register();
    public static ItemEntry<Item> ELECTRIC_MOTOR_UHV = GTCEu.isHighTier() ? REGISTRATE.item("uhv_electric_motor", Item::new).lang("UHV Electric Motor").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "electric_motor")).register() : null;
    public static ItemEntry<Item> ELECTRIC_MOTOR_UEV = GTCEu.isHighTier() ? REGISTRATE.item("uev_electric_motor", Item::new).lang("UEV Electric Motor").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "electric_motor")).register() : null;
    public static ItemEntry<Item> ELECTRIC_MOTOR_UIV = GTCEu.isHighTier() ? REGISTRATE.item("uiv_electric_motor", Item::new).lang("UIV Electric Motor").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "electric_motor")).register() : null;
    public static ItemEntry<Item> ELECTRIC_MOTOR_UXV = GTCEu.isHighTier() ? REGISTRATE.item("uxv_electric_motor", Item::new).lang("UXV Electric Motor").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "electric_motor")).register() : null;
    public static ItemEntry<Item> ELECTRIC_MOTOR_OpV = GTCEu.isHighTier() ? REGISTRATE.item("opv_electric_motor", Item::new).lang("OpV Electric Motor").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "electric_motor")).register() : null;

    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_LV = REGISTRATE.item("lv_electric_pump", ComponentItem::create)
            .lang("LV Electric Pump")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.PUMPS[0])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.electric.pump.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.fluid_transfer_rate", 1280 / 20));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "pump", GTCompassNodes.COVER))
            .register();

    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_MV = REGISTRATE.item("mv_electric_pump", ComponentItem::create)
            .lang("MV Electric Pump")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.PUMPS[1])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.electric.pump.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.fluid_transfer_rate", 1280 * 4 / 20));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "pump", GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_HV = REGISTRATE.item("hv_electric_pump", ComponentItem::create)
            .lang("HV Electric Pump")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.PUMPS[2])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.electric.pump.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.fluid_transfer_rate", 1280 * 16 / 20));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "pump", GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_EV = REGISTRATE.item("ev_electric_pump", ComponentItem::create)
            .lang("EV Electric Pump")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.PUMPS[3])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.electric.pump.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.fluid_transfer_rate", 1280 * 64 / 20));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "pump", GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_IV = REGISTRATE.item("iv_electric_pump", ComponentItem::create)
            .lang("IV Electric Pump")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.PUMPS[4])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.electric.pump.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 4 / 20));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "pump", GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_LuV = REGISTRATE.item("luv_electric_pump", ComponentItem::create)
            .lang("LuV Electric Pump")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.PUMPS[5])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.electric.pump.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 16 / 20));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "pump", GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_ZPM = REGISTRATE.item("zpm_electric_pump", ComponentItem::create)
            .lang("ZPM Electric Pump")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.PUMPS[6])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.electric.pump.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 64 / 20));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "pump", GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_UV = REGISTRATE.item("uv_electric_pump", ComponentItem::create)
            .lang("UV Electric Pump")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.PUMPS[7])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.electric.pump.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 64 * 4 / 20));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "pump", GTCompassNodes.COVER))
            .register();

    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_UHV = GTCEu.isHighTier() ? REGISTRATE.item("uhv_electric_pump", ComponentItem::create)
            .lang("UHV Electric Pump")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.PUMPS[8])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.electric.pump.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 64 * 4 / 20));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "pump", GTCompassNodes.COVER))
            .register() : null;

    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_UEV = GTCEu.isHighTier() ? REGISTRATE.item("uev_electric_pump", ComponentItem::create)
            .lang("UEV Electric Pump")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.PUMPS[9])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.electric.pump.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 64 * 4 / 20));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "pump", GTCompassNodes.COVER))
            .register() : null;

    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_UIV = GTCEu.isHighTier() ? REGISTRATE.item("uiv_electric_pump", ComponentItem::create)
            .lang("UIV Electric Pump")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.PUMPS[10])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.electric.pump.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 64 * 4 / 20));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "pump", GTCompassNodes.COVER))
            .register() : null;

    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_UXV = GTCEu.isHighTier() ? REGISTRATE.item("uxv_electric_pump", ComponentItem::create)
            .lang("UHV Electric Pump")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.PUMPS[11])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.electric.pump.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 64 * 4 / 20));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "pump", GTCompassNodes.COVER))
            .register() : null;

    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_OpV = GTCEu.isHighTier() ? REGISTRATE.item("opv_electric_pump", ComponentItem::create)
            .lang("OpV Electric Pump")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.PUMPS[12])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.electric.pump.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 64 * 4 / 20));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "pump", GTCompassNodes.COVER))
            .register() : null;

    public static ItemEntry<ComponentItem> FLUID_REGULATOR_LV = REGISTRATE.item("lv_fluid_regulator", ComponentItem::create)
            .lang("LV Fluid Regulator")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.FLUID_REGULATORS[0])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.fluid.regulator.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.fluid_transfer_rate", 1280 / 20));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "fluid_regulator"))
            .register();
    public static ItemEntry<ComponentItem> FLUID_REGULATOR_MV = REGISTRATE.item("mv_fluid_regulator", ComponentItem::create)
            .lang("MV Fluid Regulator")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.FLUID_REGULATORS[1])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.fluid.regulator.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.fluid_transfer_rate", 1280 * 4 / 20));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "fluid_regulator"))
            .register();
    public static ItemEntry<ComponentItem> FLUID_REGULATOR_HV = REGISTRATE.item("hv_fluid_regulator", ComponentItem::create)
            .lang("HV Fluid Regulator")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.FLUID_REGULATORS[2])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.fluid.regulator.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.fluid_transfer_rate", 1280 * 16 / 20));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "fluid_regulator"))
            .register();
    public static ItemEntry<ComponentItem> FLUID_REGULATOR_EV = REGISTRATE.item("ev_fluid_regulator", ComponentItem::create)
            .lang("EV Fluid Regulator")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.FLUID_REGULATORS[3])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.fluid.regulator.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.fluid_transfer_rate", 1280 * 64 / 20));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "fluid_regulator"))
            .register();
    public static ItemEntry<ComponentItem> FLUID_REGULATOR_IV = REGISTRATE.item("iv_fluid_regulator", ComponentItem::create)
            .lang("IV Fluid Regulator")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.FLUID_REGULATORS[4])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.fluid.regulator.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 4 / 20));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "fluid_regulator"))
            .register();
    public static ItemEntry<ComponentItem> FLUID_REGULATOR_LUV = REGISTRATE.item("luv_fluid_regulator", ComponentItem::create)
            .lang("LuV Fluid Regulator")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.FLUID_REGULATORS[5])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.fluid.regulator.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 16 / 20));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "fluid_regulator"))
            .register();
    public static ItemEntry<ComponentItem> FLUID_REGULATOR_ZPM = REGISTRATE.item("zpm_fluid_regulator", ComponentItem::create)
            .lang("ZPM Fluid Regulator")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.FLUID_REGULATORS[6])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.fluid.regulator.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 64 / 20));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "fluid_regulator"))
            .register();
    public static ItemEntry<ComponentItem> FLUID_REGULATOR_UV = REGISTRATE.item("uv_fluid_regulator", ComponentItem::create)
            .lang("UV Fluid Regulator")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.FLUID_REGULATORS[7])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.fluid.regulator.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 64 * 4 / 20));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "fluid_regulator"))
            .register();
    public static ItemEntry<ComponentItem> FLUID_REGULATOR_UHV = GTCEu.isHighTier() ? REGISTRATE.item("uhv_fluid_regulator", ComponentItem::create)
            .lang("UHV Fluid Regulator")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.FLUID_REGULATORS[8])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.fluid.regulator.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 64 * 4 / 20));
            })))
            .register() : null;
    public static ItemEntry<ComponentItem> FLUID_REGULATOR_UEV = GTCEu.isHighTier() ? REGISTRATE.item("uev_fluid_regulator", ComponentItem::create)
            .lang("UEV Fluid Regulator")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.FLUID_REGULATORS[9])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.fluid.regulator.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 64 * 4 / 20));
            })))
            .register() : null;
    public static ItemEntry<ComponentItem> FLUID_REGULATOR_UIV = GTCEu.isHighTier() ? REGISTRATE.item("uiv_fluid_regulator", ComponentItem::create)
            .lang("UIV Fluid Regulator")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.FLUID_REGULATORS[10])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.fluid.regulator.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 64 * 4 / 20));
            })))
            .register() : null;
    public static ItemEntry<ComponentItem> FLUID_REGULATOR_UXV = GTCEu.isHighTier() ? REGISTRATE.item("uxv_fluid_regulator", ComponentItem::create)
            .lang("UXV Fluid Regulator")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.FLUID_REGULATORS[11])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.fluid.regulator.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 64 * 4 / 20));
            })))
            .register() : null;
    public static ItemEntry<ComponentItem> FLUID_REGULATOR_OpV = GTCEu.isHighTier() ? REGISTRATE.item("opv_fluid_regulator", ComponentItem::create)
            .lang("OpV Fluid Regulator")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.FLUID_REGULATORS[12])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.fluid.regulator.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 64 * 4 / 20));
            })))
            .register() : null;

    public static ItemEntry<ComponentItem> DYNAMITE; // TODO

    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_LV = REGISTRATE.item("lv_conveyor_module", ComponentItem::create)
            .lang("LV Conveyor Module")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.CONVEYORS[0])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.conveyor.module.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate", 8));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "conveyor", GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_MV = REGISTRATE.item("mv_conveyor_module", ComponentItem::create)
            .lang("MV Conveyor Module")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.CONVEYORS[1])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.conveyor.module.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate", 32));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "conveyor", GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_HV = REGISTRATE.item("hv_conveyor_module", ComponentItem::create)
            .lang("HV Conveyor Module")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.CONVEYORS[2])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.conveyor.module.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate", 64));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "conveyor", GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_EV = REGISTRATE.item("ev_conveyor_module", ComponentItem::create)
            .lang("EV Conveyor Module")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.CONVEYORS[3])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.conveyor.module.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate_stacks", 3));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "conveyor", GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_IV = REGISTRATE.item("iv_conveyor_module", ComponentItem::create)
            .lang("IV Conveyor Module")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.CONVEYORS[4])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.conveyor.module.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate_stacks", 8));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "conveyor", GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_LuV = REGISTRATE.item("luv_conveyor_module", ComponentItem::create)
            .lang("LuV Conveyor Module")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.CONVEYORS[5])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.conveyor.module.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate_stacks", 16));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "conveyor", GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_ZPM = REGISTRATE.item("zpm_conveyor_module", ComponentItem::create)
            .lang("ZPM Conveyor Module")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.CONVEYORS[6])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.conveyor.module.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate_stacks", 16));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "conveyor", GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_UV = REGISTRATE.item("uv_conveyor_module", ComponentItem::create)
            .lang("UV Conveyor Module")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.CONVEYORS[7])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.conveyor.module.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate_stacks", 16));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "conveyor", GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_UHV = GTCEu.isHighTier() ? REGISTRATE.item("uhv_conveyor_module", ComponentItem::create)
            .lang("UHV Conveyor Module")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.CONVEYORS[8])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.conveyor.module.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate_stacks", 16));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "conveyor", GTCompassNodes.COVER))
            .register() : null;
    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_UEV = GTCEu.isHighTier() ? REGISTRATE.item("uev_conveyor_module", ComponentItem::create)
            .lang("UEV Conveyor Module")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.CONVEYORS[9])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.conveyor.module.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate_stacks", 16));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "conveyor", GTCompassNodes.COVER))
            .register() : null;
    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_UIV = GTCEu.isHighTier() ? REGISTRATE.item("uiv_conveyor_module", ComponentItem::create)
            .lang("UIV Conveyor Module")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.CONVEYORS[10])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.conveyor.module.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate_stacks", 16));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "conveyor", GTCompassNodes.COVER))
            .register() : null;
    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_UXV = GTCEu.isHighTier() ? REGISTRATE.item("uxv_conveyor_module", ComponentItem::create)
            .lang("UXV Conveyor Module")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.CONVEYORS[11])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.conveyor.module.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate_stacks", 16));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "conveyor", GTCompassNodes.COVER))
            .register() : null;
    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_OpV = GTCEu.isHighTier() ? REGISTRATE.item("opv_conveyor_module", ComponentItem::create)
            .lang("OpV Conveyor Module")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.CONVEYORS[12])))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.conveyor.module.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate_stacks", 16));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "conveyor", GTCompassNodes.COVER))
            .register() : null;

    public static ItemEntry<Item> ELECTRIC_PISTON_LV= REGISTRATE.item("lv_electric_piston", Item::new).lang("LV Electric Piston").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "piston")).register();
    public static ItemEntry<Item> ELECTRIC_PISTON_MV= REGISTRATE.item("mv_electric_piston", Item::new).lang("MV Electric Piston").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "piston")).register();
    public static ItemEntry<Item> ELECTRIC_PISTON_HV= REGISTRATE.item("hv_electric_piston", Item::new).lang("HV Electric Piston").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "piston")).register();
    public static ItemEntry<Item> ELECTRIC_PISTON_EV= REGISTRATE.item("ev_electric_piston", Item::new).lang("EV Electric Piston").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "piston")).register();
    public static ItemEntry<Item> ELECTRIC_PISTON_IV= REGISTRATE.item("iv_electric_piston", Item::new).lang("IV Electric Piston").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "piston")).register();
    public static ItemEntry<Item> ELECTRIC_PISTON_LUV= REGISTRATE.item("luv_electric_piston", Item::new).lang("LuV Electric Piston").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "piston")).register();
    public static ItemEntry<Item> ELECTRIC_PISTON_ZPM= REGISTRATE.item("zpm_electric_piston", Item::new).lang("ZPM Electric Piston").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "piston")).register();
    public static ItemEntry<Item> ELECTRIC_PISTON_UV= REGISTRATE.item("uv_electric_piston", Item::new).lang("UV Electric Piston").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "piston")).register();
    public static ItemEntry<Item> ELECTRIC_PISTON_UHV= GTCEu.isHighTier() ? REGISTRATE.item("uhv_electric_piston", Item::new).lang("UHV Electric Piston").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "piston")).register() : null;
    public static ItemEntry<Item> ELECTRIC_PISTON_UEV= GTCEu.isHighTier() ? REGISTRATE.item("uev_electric_piston", Item::new).lang("UEV Electric Piston").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "piston")).register() : null;
    public static ItemEntry<Item> ELECTRIC_PISTON_UIV= GTCEu.isHighTier() ? REGISTRATE.item("uiv_electric_piston", Item::new).lang("UIV Electric Piston").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "piston")).register() : null;
    public static ItemEntry<Item> ELECTRIC_PISTON_UXV= GTCEu.isHighTier() ? REGISTRATE.item("uxv_electric_piston", Item::new).lang("UXV Electric Piston").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "piston")).register() : null;
    public static ItemEntry<Item> ELECTRIC_PISTON_OpV= GTCEu.isHighTier() ? REGISTRATE.item("opv_electric_piston", Item::new).lang("OpV Electric Piston").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "piston")).register() : null;

    public static ItemEntry<ComponentItem> ROBOT_ARM_LV = REGISTRATE.item("lv_robot_arm", ComponentItem::create)
            .lang("LV Robot Arm")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.ROBOT_ARMS[0]))).onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.robot.arm.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate", 8));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "robot_arm", GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> ROBOT_ARM_MV = REGISTRATE.item("mv_robot_arm", ComponentItem::create)
            .lang("MV Robot Arm")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.ROBOT_ARMS[1]))).onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.robot.arm.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate", 32));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "robot_arm", GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> ROBOT_ARM_HV = REGISTRATE.item("hv_robot_arm", ComponentItem::create)
            .lang("HV Robot Arm")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.ROBOT_ARMS[2]))).onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.robot.arm.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate", 64));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "robot_arm", GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> ROBOT_ARM_EV = REGISTRATE.item("ev_robot_arm", ComponentItem::create)
            .lang("EV Robot Arm")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.ROBOT_ARMS[3]))).onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.robot.arm.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate_stacks", 3));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "robot_arm", GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> ROBOT_ARM_IV = REGISTRATE.item("iv_robot_arm", ComponentItem::create)
            .lang("IV Robot Arm")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.ROBOT_ARMS[4]))).onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.robot.arm.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate_stacks", 8));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "robot_arm", GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> ROBOT_ARM_LuV = REGISTRATE.item("luv_robot_arm", ComponentItem::create)
            .lang("LuV Robot Arm")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.ROBOT_ARMS[5]))).onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.robot.arm.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate_stacks", 16));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "robot_arm", GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> ROBOT_ARM_ZPM = REGISTRATE.item("zpm_robot_arm", ComponentItem::create)
            .lang("ZPM Robot Arm")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.ROBOT_ARMS[6]))).onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.robot.arm.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate_stacks", 16));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "robot_arm", GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> ROBOT_ARM_UV = REGISTRATE.item("uv_robot_arm", ComponentItem::create)
            .lang("UV Robot Arm")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.ROBOT_ARMS[7]))).onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.robot.arm.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate_stacks", 16));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "robot_arm", GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> ROBOT_ARM_UHV = GTCEu.isHighTier() ? REGISTRATE.item("uhv_robot_arm", ComponentItem::create)
            .lang("UHV Robot Arm")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.ROBOT_ARMS[8]))).onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.robot.arm.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate_stacks", 16));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "robot_arm", GTCompassNodes.COVER))
            .register() : null;
    public static ItemEntry<ComponentItem> ROBOT_ARM_UEV = GTCEu.isHighTier() ? REGISTRATE.item("uev_robot_arm", ComponentItem::create)
            .lang("UEV Robot Arm")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.ROBOT_ARMS[9]))).onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.robot.arm.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate_stacks", 16));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "robot_arm", GTCompassNodes.COVER))
            .register() : null;
    public static ItemEntry<ComponentItem> ROBOT_ARM_UIV = GTCEu.isHighTier() ? REGISTRATE.item("uiv_robot_arm", ComponentItem::create)
            .lang("UIV Robot Arm")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.ROBOT_ARMS[10]))).onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.robot.arm.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate_stacks", 16));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "robot_arm", GTCompassNodes.COVER))
            .register() : null;
    public static ItemEntry<ComponentItem> ROBOT_ARM_UXV = GTCEu.isHighTier() ? REGISTRATE.item("uxv_robot_arm", ComponentItem::create)
            .lang("UXV Robot Arm")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.ROBOT_ARMS[11]))).onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.robot.arm.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate_stacks", 16));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "robot_arm", GTCompassNodes.COVER))
            .register() : null;
    public static ItemEntry<ComponentItem> ROBOT_ARM_OpV = GTCEu.isHighTier() ? REGISTRATE.item("opv_robot_arm", ComponentItem::create)
            .lang("OpV Robot Arm")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.ROBOT_ARMS[12]))).onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.robot.arm.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate_stacks", 16));
            })))
            .onRegister(compassNodeExist(GTCompassSections.COVERS, "robot_arm", GTCompassNodes.COVER))
            .register() : null;

    public static ItemEntry<Item> FIELD_GENERATOR_LV= REGISTRATE.item("lv_field_generator", Item::new).lang("LV Field Generator").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "field_generator")).register();
    public static ItemEntry<Item> FIELD_GENERATOR_MV= REGISTRATE.item("mv_field_generator", Item::new).lang("MV Field Generator").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "field_generator")).register();
    public static ItemEntry<Item> FIELD_GENERATOR_HV= REGISTRATE.item("hv_field_generator", Item::new).lang("HV Field Generator").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "field_generator")).register();
    public static ItemEntry<Item> FIELD_GENERATOR_EV= REGISTRATE.item("ev_field_generator", Item::new).lang("EV Field Generator").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "field_generator")).register();
    public static ItemEntry<Item> FIELD_GENERATOR_IV= REGISTRATE.item("iv_field_generator", Item::new).lang("IV Field Generator").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "field_generator")).register();
    public static ItemEntry<Item> FIELD_GENERATOR_LuV= REGISTRATE.item("luv_field_generator", Item::new).lang("LuV Field Generator").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "field_generator")).register();
    public static ItemEntry<Item> FIELD_GENERATOR_ZPM= REGISTRATE.item("zpm_field_generator", Item::new).lang("ZPM Field Generator").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "field_generator")).register();
    public static ItemEntry<Item> FIELD_GENERATOR_UV= REGISTRATE.item("uv_field_generator", Item::new).lang("UV Field Generator").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "field_generator")).register();
    public static ItemEntry<Item> FIELD_GENERATOR_UHV= GTCEu.isHighTier() ? REGISTRATE.item("uhv_field_generator", Item::new).lang("UHV Field Generator").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "field_generator")).register() : null;
    public static ItemEntry<Item> FIELD_GENERATOR_UEV= GTCEu.isHighTier() ? REGISTRATE.item("uev_field_generator", Item::new).lang("UEV Field Generator").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "field_generator")).register() : null;
    public static ItemEntry<Item> FIELD_GENERATOR_UIV= GTCEu.isHighTier() ? REGISTRATE.item("uiv_field_generator", Item::new).lang("UIV Field Generator").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "field_generator")).register() : null;
    public static ItemEntry<Item> FIELD_GENERATOR_UXV= GTCEu.isHighTier() ? REGISTRATE.item("uxv_field_generator", Item::new).lang("UXV Field Generator").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "field_generator")).register() : null;
    public static ItemEntry<Item> FIELD_GENERATOR_OpV= GTCEu.isHighTier() ? REGISTRATE.item("opv_field_generator", Item::new).lang("OpV Field Generator").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "field_generator")).register() : null;

    public static ItemEntry<Item> EMITTER_LV= REGISTRATE.item("lv_emitter", Item::new).lang("LV Emitter").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "emitter")).register();
    public static ItemEntry<Item> EMITTER_MV= REGISTRATE.item("mv_emitter", Item::new).lang("MV Emitter").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "emitter")).register();
    public static ItemEntry<Item> EMITTER_HV= REGISTRATE.item("hv_emitter", Item::new).lang("HV Emitter").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "emitter")).register();
    public static ItemEntry<Item> EMITTER_EV= REGISTRATE.item("ev_emitter", Item::new).lang("EV Emitter").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "emitter")).register();
    public static ItemEntry<Item> EMITTER_IV= REGISTRATE.item("iv_emitter", Item::new).lang("IV Emitter").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "emitter")).register();
    public static ItemEntry<Item> EMITTER_LuV= REGISTRATE.item("luv_emitter", Item::new).lang("LuV Emitter").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "emitter")).register();
    public static ItemEntry<Item> EMITTER_ZPM= REGISTRATE.item("zpm_emitter", Item::new).lang("ZPM Emitter").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "emitter")).register();
    public static ItemEntry<Item> EMITTER_UV= REGISTRATE.item("uv_emitter", Item::new).lang("UV Emitter").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "emitter")).register();

    public static ItemEntry<Item> SENSOR_LV= REGISTRATE.item("lv_sensor", Item::new).lang("LV Sensor").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "sensor")).register();
    public static ItemEntry<Item> SENSOR_MV= REGISTRATE.item("mv_sensor", Item::new).lang("MV Sensor").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "sensor")).register();
    public static ItemEntry<Item> SENSOR_HV= REGISTRATE.item("hv_sensor", Item::new).lang("HV Sensor").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "sensor")).register();
    public static ItemEntry<Item> SENSOR_EV= REGISTRATE.item("ev_sensor", Item::new).lang("EV Sensor").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "sensor")).register();
    public static ItemEntry<Item> SENSOR_IV= REGISTRATE.item("iv_sensor", Item::new).lang("IV Sensor").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "sensor")).register();
    public static ItemEntry<Item> SENSOR_LuV= REGISTRATE.item("luv_sensor", Item::new).lang("LuV Sensor").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "sensor")).register();
    public static ItemEntry<Item> SENSOR_ZPM= REGISTRATE.item("zpm_sensor", Item::new).lang("ZPM Sensor").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "sensor")).register();
    public static ItemEntry<Item> SENSOR_UV= REGISTRATE.item("uv_sensor", Item::new).lang("UV Sensor").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "sensor")).register();
    public static ItemEntry<Item> SENSOR_UHV= GTCEu.isHighTier() ? REGISTRATE.item("uhv_sensor", Item::new).lang("UHV Sensor").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "sensor")).register() : null;
    public static ItemEntry<Item> SENSOR_UEV= GTCEu.isHighTier() ? REGISTRATE.item("uev_sensor", Item::new).lang("UEV Sensor").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "sensor")).register() : null;
    public static ItemEntry<Item> SENSOR_UIV= GTCEu.isHighTier() ? REGISTRATE.item("uiv_sensor", Item::new).lang("UIV Sensor").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "sensor")).register() : null;
    public static ItemEntry<Item> SENSOR_UXV= GTCEu.isHighTier() ? REGISTRATE.item("uxv_sensor", Item::new).lang("UXV Sensor").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "sensor")).register() : null;
    public static ItemEntry<Item> SENSOR_OpV= GTCEu.isHighTier() ? REGISTRATE.item("opv_sensor", Item::new).lang("OpV Sensor").onRegister(compassNodeExist(GTCompassSections.COMPONENTS, "sensor")).register() : null;


    public static ItemEntry<Item> TOOL_DATA_STICK= REGISTRATE.item("data_stick", Item::new).lang("Data Stick").onRegister(compassNode(GTCompassSections.COMPONENTS)).register();
    public static ItemEntry<Item> TOOL_DATA_ORB= REGISTRATE.item("data_orb", Item::new).lang("Data Orb").onRegister(compassNode(GTCompassSections.COMPONENTS)).register();

    public static final Map<MarkerMaterial, ItemEntry<Item>> GLASS_LENSES = new HashMap<>();

    static {
        for (int i = 0; i < MarkerMaterials.Color.VALUES.length; i++) {
            MarkerMaterial color = MarkerMaterials.Color.VALUES[i];
            if (color != MarkerMaterials.Color.White) {
                GLASS_LENSES.put(color, REGISTRATE.item(String.format("%s_glass_lens", color.toString()), Item::new)
                        .lang("Glass Lens (%s)".formatted(toEnglishName(color.getName())))
                        .transform(unificationItem(TagPrefix.lens, color))
                        .onRegister(compassNodeExist(GTCompassSections.MISC, "glass_lens"))
                        .register());
            }
        }
    }

    public static ItemEntry<Item> SILICON_BOULE= REGISTRATE.item("silicon_boule", Item::new).lang("Monocrystalline Silicon Boule").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "silicon_boule")).register();
    public static ItemEntry<Item> PHOSPHORUS_BOULE= REGISTRATE.item("phosphorus_boule", Item::new).lang("Phosphorus-doped Monocrystalline Silicon Boule").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "silicon_boule")).register();
    public static ItemEntry<Item> NAQUADAH_BOULE= REGISTRATE.item("naquadah_boule", Item::new).lang("Naquadah-doped Monocrystalline Silicon Boule").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "silicon_boule")).register();
    public static ItemEntry<Item> NEUTRONIUM_BOULE= REGISTRATE.item("neutronium_boule", Item::new).lang("Neutronium-doped Monocrystalline Silicon Boule").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "silicon_boule")).register();
    public static ItemEntry<Item> SILICON_WAFER= REGISTRATE.item("silicon_wafer", Item::new).lang("Silicon Wafer").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "wafer")).register();
    public static ItemEntry<Item> PHOSPHORUS_WAFER= REGISTRATE.item("phosphorus_wafer", Item::new).lang("Phosphorus-doped Wafer").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "wafer")).register();
    public static ItemEntry<Item> NAQUADAH_WAFER= REGISTRATE.item("naquadah_wafer", Item::new).lang("Naquadah-doped Wafer").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "wafer")).register();
    public static ItemEntry<Item> NEUTRONIUM_WAFER= REGISTRATE.item("neutronium_wafer", Item::new).lang("Neutronium-doped Wafer").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "wafer")).register();

    public static ItemEntry<Item> CENTRAL_PROCESSING_UNIT_WAFER= REGISTRATE.item("cpu_wafer", Item::new).lang("CPU Wafer").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "wafer")).register();
    public static ItemEntry<Item> RANDOM_ACCESS_MEMORY_WAFER= REGISTRATE.item("ram_wafer", Item::new).lang("RAM Wafer").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "wafer")).register();
    public static ItemEntry<Item> INTEGRATED_LOGIC_CIRCUIT_WAFER= REGISTRATE.item("ilc_wafer", Item::new).lang("ILC Wafer").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "wafer")).register();
    public static ItemEntry<Item> NANO_CENTRAL_PROCESSING_UNIT_WAFER= REGISTRATE.item("nano_cpu_wafer", Item::new).lang("Nano CPU Wafer").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "wafer")).register();
    public static ItemEntry<Item> QUBIT_CENTRAL_PROCESSING_UNIT_WAFER= REGISTRATE.item("qbit_cpu_wafer", Item::new).lang("Qubit CPU Wafer").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "wafer")).register();
    public static ItemEntry<Item> SIMPLE_SYSTEM_ON_CHIP_WAFER= REGISTRATE.item("simple_soc_wafer", Item::new).lang("Simple SoC Wafer").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "wafer")).register();
    public static ItemEntry<Item> SYSTEM_ON_CHIP_WAFER= REGISTRATE.item("soc_wafer", Item::new).lang("SoC Wafer").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "wafer")).register();
    public static ItemEntry<Item> ADVANCED_SYSTEM_ON_CHIP_WAFER= REGISTRATE.item("advanced_soc_wafer", Item::new).lang("ASoC Wafer").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "wafer")).register();
    public static ItemEntry<Item> HIGHLY_ADVANCED_SOC_WAFER= REGISTRATE.item("highly_advanced_soc_wafer", Item::new).lang("HASoC Wafer").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "wafer")).register();
    public static ItemEntry<Item> NAND_MEMORY_CHIP_WAFER= REGISTRATE.item("nand_memory_wafer", Item::new).lang("NAND Memory Wafer").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "wafer")).register();
    public static ItemEntry<Item> NOR_MEMORY_CHIP_WAFER= REGISTRATE.item("nor_memory_wafer", Item::new).lang("NOR Memory Wafer").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "wafer")).register();
    public static ItemEntry<Item> ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER= REGISTRATE.item("ulpic_wafer", Item::new).lang("ULPIC Wafer").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "wafer")).register();
    public static ItemEntry<Item> LOW_POWER_INTEGRATED_CIRCUIT_WAFER= REGISTRATE.item("lpic_wafer", Item::new).lang("LPIC Wafer").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "wafer")).register();
    public static ItemEntry<Item> POWER_INTEGRATED_CIRCUIT_WAFER= REGISTRATE.item("mpic_wafer", Item::new).lang("MPIC Wafer").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "wafer")).register();
    public static ItemEntry<Item> HIGH_POWER_INTEGRATED_CIRCUIT_WAFER= REGISTRATE.item("hpic_wafer", Item::new).lang("HPIC Wafer").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "wafer")).register();
    public static ItemEntry<Item> ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT_WAFER= REGISTRATE.item("uhpic_wafer", Item::new).lang("UHPIC Wafer").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "wafer")).register();

    public static ItemEntry<Item> ENGRAVED_CRYSTAL_CHIP = REGISTRATE.item("engraved_crystal_chip", Item::new).lang("Engraved Crystal Chip").onRegister(compassNode(GTCompassSections.MISC)).register();
    public static ItemEntry<Item> ENGRAVED_LAPOTRON_CHIP = REGISTRATE.item("engraved_lapotron_crystal_chip", Item::new).lang("Engraved Lapotron Crystal Chip").onRegister(compassNode(GTCompassSections.MISC)).register();

    public static ItemEntry<Item> CENTRAL_PROCESSING_UNIT = REGISTRATE.item( "cpu_chip", Item::new).lang("CPU Chip").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "cpus")).register();
    public static ItemEntry<Item> RANDOM_ACCESS_MEMORY = REGISTRATE.item( "ram_chip", Item::new).lang("RAM Chip").register();
    public static ItemEntry<Item> INTEGRATED_LOGIC_CIRCUIT = REGISTRATE.item( "ilc_chip", Item::new).lang("IC Chip").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "cpus")).register();
    public static ItemEntry<Item> NANO_CENTRAL_PROCESSING_UNIT = REGISTRATE.item( "nano_cpu_chip", Item::new).lang("Nano CPU Chip").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "cpus")).register();
    public static ItemEntry<Item> QUBIT_CENTRAL_PROCESSING_UNIT = REGISTRATE.item( "qbit_cpu_chip", Item::new).lang("Qubit CPU Chip").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "cpus")).register();
    public static ItemEntry<Item> SIMPLE_SYSTEM_ON_CHIP = REGISTRATE.item( "simple_soc", Item::new).lang("Simple SoC").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "socs")).register();
    public static ItemEntry<Item> SYSTEM_ON_CHIP = REGISTRATE.item( "soc", Item::new).lang("SoC").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "socs")).register();
    public static ItemEntry<Item> ADVANCED_SYSTEM_ON_CHIP = REGISTRATE.item( "advanced_soc", Item::new).lang("ASoC").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "socs")).register();
    public static ItemEntry<Item> HIGHLY_ADVANCED_SOC = REGISTRATE.item( "highly_advanced_soc", Item::new).lang("HASoC").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "socs")).register();
    public static ItemEntry<Item> NAND_MEMORY_CHIP = REGISTRATE.item( "nand_memory_chip", Item::new).lang("NAND Memory Chip").register();
    public static ItemEntry<Item> NOR_MEMORY_CHIP = REGISTRATE.item( "nor_memory_chip", Item::new).lang("NOR Memory Chip").register();
    public static ItemEntry<Item> ULTRA_LOW_POWER_INTEGRATED_CIRCUIT = REGISTRATE.item( "ulpic_chip", Item::new).lang("ULPIC Chip").register();
    public static ItemEntry<Item> LOW_POWER_INTEGRATED_CIRCUIT = REGISTRATE.item( "lpic_chip", Item::new).lang("LPIC Chip").register();
    public static ItemEntry<Item> POWER_INTEGRATED_CIRCUIT = REGISTRATE.item( "mpic_chip", Item::new).lang("MPIC Chip").register();
    public static ItemEntry<Item> HIGH_POWER_INTEGRATED_CIRCUIT = REGISTRATE.item( "hpic_chip", Item::new).lang("HPIC Chip").register();
    public static ItemEntry<Item> ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT = REGISTRATE.item( "uhpic_chip", Item::new).lang("UHPIC Chip").register();


    public static ItemEntry<Item> RAW_CRYSTAL_CHIP = REGISTRATE.item("raw_crystal_chip", Item::new).lang("Raw Crystal Chip").onRegister(compassNode(GTCompassSections.MISC)).register();
    public static ItemEntry<Item> RAW_CRYSTAL_CHIP_PART = REGISTRATE.item("raw_crystal_chip_parts", Item::new).lang("Raw Crystal Chip Parts").onRegister(compassNode(GTCompassSections.MISC)).register();
    public static ItemEntry<Item> CRYSTAL_CENTRAL_PROCESSING_UNIT = REGISTRATE.item("crystal_cpu", Item::new).lang("Crystal CPU").onRegister(compassNode(GTCompassSections.MISC)).onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "cpus")).register();
    public static ItemEntry<Item> CRYSTAL_SYSTEM_ON_CHIP = REGISTRATE.item("crystal_soc", Item::new).lang("Crystal SoC").onRegister(compassNode(GTCompassSections.MISC)).onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "socs")).register();

    public static ItemEntry<Item> COATED_BOARD = REGISTRATE.item("resin_circuit_board", Item::new).lang("Resin Circuit Board").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "circuit_board")).register();
    public static ItemEntry<Item> PHENOLIC_BOARD = REGISTRATE.item("phenolic_circuit_board", Item::new).lang("Phenolic Circuit Board").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "circuit_board")).register();
    public static ItemEntry<Item> PLASTIC_BOARD = REGISTRATE.item("plastic_circuit_board", Item::new).lang("Plastic Circuit Board").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "circuit_board")).register();
    public static ItemEntry<Item> EPOXY_BOARD = REGISTRATE.item("epoxy_circuit_board", Item::new).lang("Epoxy Circuit Board").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "circuit_board")).register();
    public static ItemEntry<Item> FIBER_BOARD = REGISTRATE.item("fiber_reinforced_circuit_board", Item::new).lang("Fiber-Reinforced Circuit Board").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "circuit_board")).register();
    public static ItemEntry<Item> MULTILAYER_FIBER_BOARD = REGISTRATE.item("multilayer_fiber_reinforced_circuit_board", Item::new).lang("Multi-layer Fiber-Reinforced Circuit Board").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "circuit_board")).register();
    public static ItemEntry<Item> WETWARE_BOARD = REGISTRATE.item("wetware_circuit_board", Item::new).lang("Wetware Circuit Board").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "circuit_board")).register();

    public static ItemEntry<Item> BASIC_CIRCUIT_BOARD = REGISTRATE.item("resin_printed_circuit_board", Item::new).lang("Resin Printed Circuit Board").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "circuit_board")).register();
    public static ItemEntry<Item> GOOD_CIRCUIT_BOARD = REGISTRATE.item("phenolic_printed_circuit_board", Item::new).lang("Phenolic Printed Circuit Board").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "circuit_board")).register();
    public static ItemEntry<Item> PLASTIC_CIRCUIT_BOARD = REGISTRATE.item("plastic_printed_circuit_board", Item::new).lang("Plastic Printed Circuit Board").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "circuit_board")).register();
    public static ItemEntry<Item> ADVANCED_CIRCUIT_BOARD = REGISTRATE.item("epoxy_printed_circuit_board", Item::new).lang("Epoxy Printed Circuit Board").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "circuit_board")).register();
    public static ItemEntry<Item> EXTREME_CIRCUIT_BOARD = REGISTRATE.item("fiber_reinforced_printed_circuit_board", Item::new).lang("Fiber-Reinforced Printed Circuit Board").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "circuit_board")).register();
    public static ItemEntry<Item> ELITE_CIRCUIT_BOARD = REGISTRATE.item("multilayer_fiber_reinforced_printed_circuit_board", Item::new).lang("Multi-layer Fiber-Reinforced Printed Circuit Board").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "circuit_board")).register();
    public static ItemEntry<Item> WETWARE_CIRCUIT_BOARD = REGISTRATE.item("wetware_printed_circuit_board", Item::new).lang("Wetware Printed Circuit Board").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "circuit_board")).register();

    public static ItemEntry<Item> VACUUM_TUBE = REGISTRATE.item("vacuum_tube", Item::new).lang("Vacuum Tube").tag(CustomTags.ULV_CIRCUITS).onRegister(compassNode(GTCompassSections.CIRCUITS)).register();
    public static ItemEntry<Item> GLASS_TUBE = REGISTRATE.item("glass_tube", Item::new).lang("Glass Tube").onRegister(compassNode(GTCompassSections.CIRCUITS)).register();
    public static ItemEntry<Item> TRANSISTOR = REGISTRATE.item("transistor", Item::new).lang("Transistor").tag(CustomTags.TRANSISTORS).onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "components")).register();
    public static ItemEntry<Item> RESISTOR = REGISTRATE.item("resistor", Item::new).lang("Resistor").tag(CustomTags.RESISTORS).onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "components")).register();
    public static ItemEntry<Item> CAPACITOR = REGISTRATE.item("capacitor", Item::new).lang("Capacitor").tag(CustomTags.CAPACITORS).onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "components")).register();
    public static ItemEntry<Item> DIODE = REGISTRATE.item("diode", Item::new).lang("Diode").tag(CustomTags.DIODES).onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "components")).register();
    public static ItemEntry<Item> INDUCTOR = REGISTRATE.item("inductor", Item::new).lang("Inductor").tag(CustomTags.INDUCTORS).onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "components")).register();
    public static ItemEntry<Item> SMD_TRANSISTOR = REGISTRATE.item("smd_transistor", Item::new).lang("SMD Transistor").tag(CustomTags.TRANSISTORS).onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "smd")).register();
    public static ItemEntry<Item> SMD_RESISTOR = REGISTRATE.item("smd_resistor", Item::new).lang("SMD Resistor").tag(CustomTags.RESISTORS).onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "smd")).register();
    public static ItemEntry<Item> SMD_CAPACITOR = REGISTRATE.item("smd_capacitor", Item::new).lang("SMD Capacitor").tag(CustomTags.CAPACITORS).onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "smd")).register();
    public static ItemEntry<Item> SMD_DIODE = REGISTRATE.item("smd_diode", Item::new).lang("SMD Diode").tag(CustomTags.DIODES).onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "smd")).register();
    public static ItemEntry<Item> SMD_INDUCTOR = REGISTRATE.item("smd_inductor", Item::new).lang("SMD Inductor").tag(CustomTags.INDUCTORS).onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "smd")).register();
    public static ItemEntry<Item> ADVANCED_SMD_TRANSISTOR = REGISTRATE.item("advanced_smd_transistor", Item::new).lang("Advanced SMD Transistor").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "smd")).register();
    public static ItemEntry<Item> ADVANCED_SMD_RESISTOR = REGISTRATE.item("advanced_smd_resistor", Item::new).lang("Advanced SMD Resistor").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "smd")).register();
    public static ItemEntry<Item> ADVANCED_SMD_CAPACITOR = REGISTRATE.item("advanced_smd_capacitor", Item::new).lang("Advanced SMD Capacitor").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "smd")).register();
    public static ItemEntry<Item> ADVANCED_SMD_DIODE = REGISTRATE.item("advanced_smd_diode", Item::new).lang("Advanced SMD Diode").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "smd")).register();
    public static ItemEntry<Item> ADVANCED_SMD_INDUCTOR = REGISTRATE.item("advanced_smd_inductor", Item::new).lang("Advanced SMD Inductor").onRegister(compassNodeExist(GTCompassSections.CIRCUITS, "smd")).register();

    // T1: Electronic
    public static ItemEntry<Item> ELECTRONIC_CIRCUIT_LV = REGISTRATE.item("basic_electronic_circuit", Item::new).lang("Basic Electronic Circuit").tag(CustomTags.LV_CIRCUITS).onRegister(compassNode(GTCompassSections.CIRCUITS)).register();
    public static ItemEntry<Item> ELECTRONIC_CIRCUIT_MV = REGISTRATE.item("good_electronic_circuit", Item::new).lang("Good Electronic Circuit").tag(CustomTags.MV_CIRCUITS).onRegister(compassNode(GTCompassSections.CIRCUITS)).register();

    // T2: Integrated
    public static ItemEntry<Item> INTEGRATED_CIRCUIT_LV = REGISTRATE.item("basic_integrated_circuit", Item::new).lang("Basic Integrated Circuit").tag(CustomTags.LV_CIRCUITS).onRegister(compassNode(GTCompassSections.CIRCUITS)).register();
    public static ItemEntry<Item> INTEGRATED_CIRCUIT_MV = REGISTRATE.item("good_integrated_circuit", Item::new).lang("Good Integrated Circuit").tag(CustomTags.MV_CIRCUITS).onRegister(compassNode(GTCompassSections.CIRCUITS)).register();
    public static ItemEntry<Item> INTEGRATED_CIRCUIT_HV = REGISTRATE.item("advanced_integrated_circuit", Item::new).lang("Advanced Integrated Circuit").tag(CustomTags.HV_CIRCUITS).onRegister(compassNode(GTCompassSections.CIRCUITS)).register();

    // ULV/LV easier circuits
    public static ItemEntry<Item> NAND_CHIP_ULV = REGISTRATE.item("nand_chip", Item::new).lang("NAND Chip").tag(CustomTags.ULV_CIRCUITS).onRegister(compassNode(GTCompassSections.CIRCUITS)).register();
    public static ItemEntry<Item> MICROPROCESSOR_LV = REGISTRATE.item("microchip_processor", Item::new).lang("Microchip Processor").tag(CustomTags.LV_CIRCUITS).onRegister(compassNode(GTCompassSections.CIRCUITS)).register();

    // T3: Processor
    public static ItemEntry<Item> PROCESSOR_MV = REGISTRATE.item("micro_processor", Item::new).lang("Microprocessor").tag(CustomTags.MV_CIRCUITS).onRegister(compassNode(GTCompassSections.CIRCUITS)).register();
    public static ItemEntry<Item> PROCESSOR_ASSEMBLY_HV = REGISTRATE.item("micro_processor_assembly", Item::new).lang("Microprocessor Assembly").tag(CustomTags.HV_CIRCUITS).onRegister(compassNode(GTCompassSections.CIRCUITS)).register();
    public static ItemEntry<Item> WORKSTATION_EV = REGISTRATE.item("micro_processor_computer", Item::new).lang("Microprocessor Supercomputer").tag(CustomTags.EV_CIRCUITS).onRegister(compassNode(GTCompassSections.CIRCUITS)).register();
    public static ItemEntry<Item> MAINFRAME_IV = REGISTRATE.item("micro_processor_mainframe", Item::new).lang("Microprocessor Mainframe").tag(CustomTags.IV_CIRCUITS).onRegister(compassNode(GTCompassSections.CIRCUITS)).register();

    // T4: Nano
    public static ItemEntry<Item> NANO_PROCESSOR_HV = REGISTRATE.item("nano_processor", Item::new).lang("Nanoprocessor").tag(CustomTags.HV_CIRCUITS).onRegister(compassNode(GTCompassSections.CIRCUITS)).register();
    public static ItemEntry<Item> NANO_PROCESSOR_ASSEMBLY_EV = REGISTRATE.item("nano_processor_assembly", Item::new).lang("Nanoprocessor Assembly").tag(CustomTags.EV_CIRCUITS).onRegister(compassNode(GTCompassSections.CIRCUITS)).register();
    public static ItemEntry<Item> NANO_COMPUTER_IV = REGISTRATE.item("nano_processor_computer", Item::new).lang("Nanoprocessor Supercomputer").tag(CustomTags.IV_CIRCUITS).onRegister(compassNode(GTCompassSections.CIRCUITS)).register();
    public static ItemEntry<Item> NANO_MAINFRAME_LUV = REGISTRATE.item("nano_processor_mainframe", Item::new).lang("Nanoprocessor Mainframe").tag(CustomTags.LuV_CIRCUITS).onRegister(compassNode(GTCompassSections.CIRCUITS)).register();

    // T5: Quantum
    public static ItemEntry<Item> QUANTUM_PROCESSOR_EV = REGISTRATE.item("quantum_processor", Item::new).lang("Quantum Processor").tag(CustomTags.EV_CIRCUITS).onRegister(compassNode(GTCompassSections.CIRCUITS)).register();
    public static ItemEntry<Item> QUANTUM_ASSEMBLY_IV = REGISTRATE.item("quantum_processor_assembly", Item::new).lang("Quantum Processor Assembly").tag(CustomTags.IV_CIRCUITS).onRegister(compassNode(GTCompassSections.CIRCUITS)).register();
    public static ItemEntry<Item> QUANTUM_COMPUTER_LUV = REGISTRATE.item("quantum_processor_computer", Item::new).lang("Quantum Processor Supercomputer").tag(CustomTags.LuV_CIRCUITS).onRegister(compassNode(GTCompassSections.CIRCUITS)).register();
    public static ItemEntry<Item> QUANTUM_MAINFRAME_ZPM = REGISTRATE.item("quantum_processor_mainframe", Item::new).lang("Quantum Processor Mainframe").tag(CustomTags.ZPM_CIRCUITS).onRegister(compassNode(GTCompassSections.CIRCUITS)).register();

    // T6: Crystal
    public static ItemEntry<Item> CRYSTAL_PROCESSOR_IV = REGISTRATE.item("crystal_processor", Item::new).lang("Crystal Processor").tag(CustomTags.IV_CIRCUITS).onRegister(compassNode(GTCompassSections.CIRCUITS)).register();
    public static ItemEntry<Item> CRYSTAL_ASSEMBLY_LUV = REGISTRATE.item("crystal_processor_assembly", Item::new).lang("Crystal Processor Assembly").tag(CustomTags.LuV_CIRCUITS).onRegister(compassNode(GTCompassSections.CIRCUITS)).register();
    public static ItemEntry<Item> CRYSTAL_COMPUTER_ZPM = REGISTRATE.item("crystal_processor_computer", Item::new).lang("Crystal Processor Supercomputer").tag(CustomTags.ZPM_CIRCUITS).onRegister(compassNode(GTCompassSections.CIRCUITS)).register();
    public static ItemEntry<Item> CRYSTAL_MAINFRAME_UV = REGISTRATE.item("crystal_processor_mainframe", Item::new).lang("Crystal Processor Mainframe").tag(CustomTags.UV_CIRCUITS).onRegister(compassNode(GTCompassSections.CIRCUITS)).register();

    // T7: Wetware
    public static ItemEntry<Item> WETWARE_PROCESSOR_LUV = REGISTRATE.item("wetware_processor", Item::new).lang("Wetware Processor").tag(CustomTags.LuV_CIRCUITS).onRegister(compassNode(GTCompassSections.CIRCUITS)).register();
    public static ItemEntry<Item> WETWARE_PROCESSOR_ASSEMBLY_ZPM = REGISTRATE.item("wetware_processor_assembly", Item::new).lang("Wetware Processor Assembly").tag(CustomTags.ZPM_CIRCUITS).onRegister(compassNode(GTCompassSections.CIRCUITS)).register();
    public static ItemEntry<Item> WETWARE_SUPER_COMPUTER_UV = REGISTRATE.item("wetware_processor_computer", Item::new).lang("Wetware Processor Supercomputer").tag(CustomTags.UV_CIRCUITS).onRegister(compassNode(GTCompassSections.CIRCUITS)).register();
    public static ItemEntry<Item> WETWARE_MAINFRAME_UHV = REGISTRATE.item("wetware_processor_mainframe", Item::new).lang("Wetware Processor Mainframe").tag(CustomTags.UHV_CIRCUITS).onRegister(compassNode(GTCompassSections.CIRCUITS)).register();

    public static ItemEntry<Item> COMPONENT_GRINDER_DIAMOND = REGISTRATE.item("diamond_grinding_head", Item::new).lang("Diamond Grinding Head").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 8), new MaterialStack(GTMaterials.Diamond, GTValues.M * 5)))).onRegister(compassNode(GTCompassSections.MISC)).register();
    public static ItemEntry<Item> COMPONENT_GRINDER_TUNGSTEN = REGISTRATE.item("tungsten_grinding_head", Item::new).lang("Tungsten Grinding Head").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Tungsten, GTValues.M * 4), new MaterialStack(GTMaterials.VanadiumSteel, GTValues.M * 8), new MaterialStack(GTMaterials.Diamond, GTValues.M)))).onRegister(compassNode(GTCompassSections.MISC)).register();

    public static ItemEntry<Item> QUANTUM_EYE = REGISTRATE.item("quantum_eye", Item::new).lang("Quantum Eye").onRegister(compassNode(GTCompassSections.MISC)).register();
    public static ItemEntry<Item> QUANTUM_STAR = REGISTRATE.item("quantum_star", Item::new).lang("Quantum Star").onRegister(compassNode(GTCompassSections.MISC)).register();
    public static ItemEntry<Item> GRAVI_STAR = REGISTRATE.item("gravi_star", Item::new).lang("Gravi-Star").onRegister(compassNode(GTCompassSections.MISC)).register();


    /////////////////////////////////////////
    //***********     COVERS    ***********//
    /////////////////////////////////////////


    public static ItemEntry<ComponentItem> ITEM_FILTER = REGISTRATE.item("item_filter", ComponentItem::create)
            .onRegister(attach(new ItemFilterBehaviour(SimpleItemFilter::loadFilter), new CoverPlaceBehavior(GTCovers.ITEM_FILTER)))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Zinc, GTValues.M * 2), new MaterialStack(GTMaterials.Steel, GTValues.M))))
            .onRegister(compassNode(GTCompassSections.COVERS, GTCompassNodes.COVER)).register();
    public static ItemEntry<ComponentItem> ORE_DICTIONARY_FILTER = REGISTRATE.item("item_tag_filter", ComponentItem::create)
            .lang("Item Tag Filter")
            .onRegister(attach(new ItemFilterBehaviour(TagItemFilter::loadFilter), new CoverPlaceBehavior(GTCovers.ITEM_FILTER)))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Zinc, GTValues.M * 2))))
            .onRegister(compassNode(GTCompassSections.COVERS, GTCompassNodes.COVER)).register();
    public static ItemEntry<ComponentItem> FLUID_FILTER = REGISTRATE.item("fluid_filter", ComponentItem::create)
            .onRegister(attach(new FluidFilterBehaviour(SimpleFluidFilter::loadFilter), new CoverPlaceBehavior(GTCovers.FLUID_FILTER)))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Zinc, GTValues.M * 2))))
            .onRegister(compassNode(GTCompassSections.COVERS, GTCompassNodes.COVER)).register();
    public static ItemEntry<ComponentItem> TAG_FLUID_FILTER = REGISTRATE.item("fluid_tag_filter", ComponentItem::create)
            .lang("Fluid Tag Filter")
            .onRegister(attach(new FluidFilterBehaviour(TagFluidFilter::loadFilter), new CoverPlaceBehavior(GTCovers.FLUID_FILTER)))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Zinc, GTValues.M * 3 / 2))))
            .onRegister(compassNode(GTCompassSections.COVERS, GTCompassNodes.COVER)).register();


    public static ItemEntry<ComponentItem> COVER_MACHINE_CONTROLLER = REGISTRATE.item("machine_controller_cover", ComponentItem::create)
            .lang("Machine Controller")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.MACHINE_CONTROLLER)))
            .onRegister(compassNode(GTCompassSections.COVERS, GTCompassNodes.COVER))
            .register();


    public static ItemEntry<ComponentItem> COVER_ACTIVITY_DETECTOR = REGISTRATE.item("activity_detector_cover", ComponentItem::create)
            .lang("Activity Detector")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.ACTIVITY_DETECTOR)))
            .onRegister(compassNode(GTCompassSections.COVERS, GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> COVER_ACTIVITY_DETECTOR_ADVANCED = REGISTRATE.item("advanced_activity_detector_cover", ComponentItem::create)
            .lang("Advanced Activity Detector")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.ACTIVITY_DETECTOR_ADVANCED)))
            .onRegister(compassNode(GTCompassSections.COVERS, GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> COVER_FLUID_DETECTOR = REGISTRATE.item("fluid_detector_cover", ComponentItem::create)
            .lang("Fluid Detector")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.FLUID_DETECTOR)))
            .onRegister(compassNode(GTCompassSections.COVERS, GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> COVER_FLUID_DETECTOR_ADVANCED = REGISTRATE.item("advanced_fluid_detector_cover", ComponentItem::create)
            .lang("Advanced Fluid Detector")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.FLUID_DETECTOR_ADVANCED)))
            .onRegister(compassNode(GTCompassSections.COVERS, GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> COVER_ITEM_DETECTOR = REGISTRATE.item("item_detector_cover", ComponentItem::create)
            .lang("Item Detector")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.ITEM_DETECTOR)))
            .onRegister(compassNode(GTCompassSections.COVERS, GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> COVER_ITEM_DETECTOR_ADVANCED = REGISTRATE.item("advanced_item_detector_cover", ComponentItem::create)
            .lang("Advanced Item Detector")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.ITEM_DETECTOR_ADVANCED)))
            .onRegister(compassNode(GTCompassSections.COVERS, GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> COVER_ENERGY_DETECTOR = REGISTRATE.item("energy_detector_cover", ComponentItem::create)
            .lang("Energy Detector")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.ENERGY_DETECTOR)))
            .onRegister(compassNode(GTCompassSections.COVERS, GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> COVER_ENERGY_DETECTOR_ADVANCED = REGISTRATE.item("advanced_energy_detector_cover", ComponentItem::create)
            .lang("Advanced Energy Detector")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.ENERGY_DETECTOR_ADVANCED)))
            .onRegister(compassNode(GTCompassSections.COVERS, GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> COVER_MAINTENANCE_DETECTOR = REGISTRATE.item("maintenance_detector_cover", ComponentItem::create)
            .lang("Maintenance Detector")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.MAINTENANCE_DETECTOR)))
            .onRegister(compassNode(GTCompassSections.COVERS, GTCompassNodes.COVER))
            .register();


    public static ItemEntry<ComponentItem> COVER_SCREEN = REGISTRATE.item("computer_monitor_cover", ComponentItem::create)
            .lang("Computer Monitor")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.COMPUTER_MONITOR)))
            .onRegister(compassNode(GTCompassSections.COVERS, GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> COVER_CRAFTING = REGISTRATE.item("crafting_table_cover", ComponentItem::create)
            .lang("Crafting Table Cover")
            .onRegister(compassNode(GTCompassSections.COVERS, GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> COVER_SHUTTER = REGISTRATE.item("shutter_module_cover", ComponentItem::create)
            .lang("Shutter Module")
            .onRegister(compassNode(GTCompassSections.COVERS, GTCompassNodes.COVER))
            .register();

    public static ItemEntry<ComponentItem> COVER_INFINITE_WATER = REGISTRATE.item("infinite_water_cover", ComponentItem::create)
            .lang("Infinite Water Cover")
            .onRegister(compassNode(GTCompassSections.COVERS, GTCompassNodes.COVER))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("gtceu.universal.tooltip.produces_fluid", 16_000 / 20));
            }), new CoverPlaceBehavior(GTCovers.INFINITE_WATER))).register();

    public static ItemEntry<ComponentItem> COVER_ENDER_FLUID_LINK = REGISTRATE.item("ender_fluid_link_cover", ComponentItem::create)
            .lang("Ender Fluid Link")
            .onRegister(compassNode(GTCompassSections.COVERS, GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> COVER_DIGITAL_INTERFACE = REGISTRATE.item("digital_interface_cover", ComponentItem::create)
            .lang("Digital Interface")
            .register();
    public static ItemEntry<ComponentItem> COVER_DIGITAL_INTERFACE_WIRELESS = REGISTRATE.item("wireless_digital_interface_cover", ComponentItem::create)
            .lang("Wireless Digital Interface")
            .register();
    public static ItemEntry<ComponentItem> COVER_FLUID_VOIDING = REGISTRATE.item("fluid_voiding_cover", ComponentItem::create)
            .lang("Fluid Voiding Cover")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.FLUID_VOIDING)))
            .onRegister(compassNode(GTCompassSections.COVERS, GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> COVER_FLUID_VOIDING_ADVANCED = REGISTRATE.item("advanced_fluid_voiding_cover", ComponentItem::create)
            .lang("Advanced Fluid Voiding Cover")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.FLUID_VOIDING_ADVANCED)))
            .onRegister(compassNode(GTCompassSections.COVERS, GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> COVER_ITEM_VOIDING = REGISTRATE.item("item_voiding_cover", ComponentItem::create)
            .lang("Item Voiding Cover")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.ITEM_VOIDING)))
            .onRegister(compassNode(GTCompassSections.COVERS, GTCompassNodes.COVER))
            .register();
    public static ItemEntry<ComponentItem> COVER_ITEM_VOIDING_ADVANCED = REGISTRATE.item("advanced_item_voiding_cover", ComponentItem::create)
            .lang("Advanced Item Voiding Cover")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.ITEM_VOIDING_ADVANCED)))
            .onRegister(compassNode(GTCompassSections.COVERS, GTCompassNodes.COVER))
            .register();

    public static ItemEntry<ComponentItem> COVER_FACADE = REGISTRATE.item("facade_cover", ComponentItem::create)
            .lang("Cover Facade")
            .onRegister(attach(new FacadeItemBehaviour(), new CoverPlaceBehavior(GTCovers.FACADE)))
            .model(NonNullBiConsumer.noop())
            .onRegister(compassNode(GTCompassSections.COVERS, GTCompassNodes.COVER))
            .register();

    // Solar Panels: ID 331-346
    public static ItemEntry<ComponentItem> COVER_SOLAR_PANEL = REGISTRATE.item("solar_panel", ComponentItem::create).lang("Solar Panel").onRegister(attach(new TooltipBehavior(lines -> {
        lines.addAll(LangHandler.getMultiLang("metaitem.cover.solar.panel.tooltip"));
        lines.add(Component.translatable("gtceu.universal.tooltip.voltage_out", 1, GTValues.VNF[GTValues.ULV]));
    }))).onRegister(compassNode(GTCompassSections.COVERS, GTCompassNodes.COVER)).register();
    public static ItemEntry<ComponentItem> COVER_SOLAR_PANEL_ULV = REGISTRATE.item("ulv_solar_panel", ComponentItem::create).lang("Ultra Low Voltage Solar Panel").onRegister(attach(new TooltipBehavior(lines -> {
        lines.addAll(LangHandler.getMultiLang("metaitem.cover.solar.panel.tooltip"));
        lines.add(Component.translatable("gtceu.universal.tooltip.voltage_out", GTValues.V[GTValues.ULV], GTValues.VNF[GTValues.ULV]));
    }))).onRegister(attach(new CoverPlaceBehavior(GTCovers.SOLAR_PANEL[0]))).onRegister(compassNodeExist(GTCompassSections.COVERS, "solar_panel", GTCompassNodes.COVER)).register();
    public static ItemEntry<ComponentItem> COVER_SOLAR_PANEL_LV = REGISTRATE.item("lv_solar_panel", ComponentItem::create).lang("Low Voltage Solar Panel").onRegister(attach(new TooltipBehavior(lines -> {
        lines.addAll(LangHandler.getMultiLang("metaitem.cover.solar.panel.tooltip"));
        lines.add(Component.translatable("gtceu.universal.tooltip.voltage_out", GTValues.V[GTValues.LV], GTValues.VNF[GTValues.LV]));
    }))).onRegister(attach(new CoverPlaceBehavior(GTCovers.SOLAR_PANEL[1]))).onRegister(compassNodeExist(GTCompassSections.COVERS, "solar_panel", GTCompassNodes.COVER)).register();
    public static ItemEntry<ComponentItem> COVER_SOLAR_PANEL_MV = REGISTRATE.item("mv_solar_panel", ComponentItem::create).lang("Medium Voltage Solar Panel").onRegister(attach(new TooltipBehavior(lines -> {
        lines.addAll(LangHandler.getMultiLang("metaitem.cover.solar.panel.tooltip"));
        lines.add(Component.translatable("gtceu.universal.tooltip.voltage_out", GTValues.V[GTValues.MV], GTValues.VNF[GTValues.MV]));
    }))).onRegister(attach(new CoverPlaceBehavior(GTCovers.SOLAR_PANEL[2]))).onRegister(compassNodeExist(GTCompassSections.COVERS, "solar_panel", GTCompassNodes.COVER)).register();
    public static ItemEntry<ComponentItem> COVER_SOLAR_PANEL_HV = REGISTRATE.item("hv_solar_panel", ComponentItem::create).lang("High Voltage Solar Panel").onRegister(attach(new TooltipBehavior(lines -> {
        lines.addAll(LangHandler.getMultiLang("metaitem.cover.solar.panel.tooltip"));
        lines.add(Component.translatable("gtceu.universal.tooltip.voltage_out", GTValues.V[GTValues.HV], GTValues.VNF[GTValues.HV]));
    }))).onRegister(attach(new CoverPlaceBehavior(GTCovers.SOLAR_PANEL[3]))).onRegister(compassNodeExist(GTCompassSections.COVERS, "solar_panel", GTCompassNodes.COVER)).register();
    public static ItemEntry<ComponentItem> COVER_SOLAR_PANEL_EV = REGISTRATE.item("ev_solar_panel", ComponentItem::create).lang("Extreme Voltage Solar Panel").onRegister(attach(new TooltipBehavior(lines -> {
        lines.addAll(LangHandler.getMultiLang("metaitem.cover.solar.panel.tooltip"));
        lines.add(Component.translatable("gtceu.universal.tooltip.voltage_out", GTValues.V[GTValues.EV], GTValues.VNF[GTValues.EV]));
    }))).onRegister(attach(new CoverPlaceBehavior(GTCovers.SOLAR_PANEL[4]))).onRegister(compassNodeExist(GTCompassSections.COVERS, "solar_panel", GTCompassNodes.COVER)).register();
    public static ItemEntry<ComponentItem> COVER_SOLAR_PANEL_IV = REGISTRATE.item("iv_solar_panel", ComponentItem::create).lang("Insane Voltage Solar Panel").onRegister(attach(new TooltipBehavior(lines -> {
        lines.addAll(LangHandler.getMultiLang("metaitem.cover.solar.panel.tooltip"));
        lines.add(Component.translatable("gtceu.universal.tooltip.voltage_out", GTValues.V[GTValues.IV], GTValues.VNF[GTValues.IV]));
    }))).onRegister(attach(new CoverPlaceBehavior(GTCovers.SOLAR_PANEL[5]))).onRegister(compassNodeExist(GTCompassSections.COVERS, "solar_panel", GTCompassNodes.COVER)).register();
    public static ItemEntry<ComponentItem> COVER_SOLAR_PANEL_LUV = REGISTRATE.item("luv_solar_panel", ComponentItem::create).lang("Ludicrous Voltage Solar Panel").onRegister(attach(new TooltipBehavior(lines -> {
        lines.addAll(LangHandler.getMultiLang("metaitem.cover.solar.panel.tooltip"));
        lines.add(Component.translatable("gtceu.universal.tooltip.voltage_out", GTValues.V[GTValues.LuV], GTValues.VNF[GTValues.LuV]));
    }))).onRegister(attach(new CoverPlaceBehavior(GTCovers.SOLAR_PANEL[6]))).onRegister(compassNodeExist(GTCompassSections.COVERS, "solar_panel", GTCompassNodes.COVER)).register();
    public static ItemEntry<ComponentItem> COVER_SOLAR_PANEL_ZPM = REGISTRATE.item("zpm_solar_panel", ComponentItem::create).lang("Zero Point Module Solar Panel").onRegister(attach(new TooltipBehavior(lines -> {
        lines.addAll(LangHandler.getMultiLang("metaitem.cover.solar.panel.tooltip"));
        lines.add(Component.translatable("gtceu.universal.tooltip.voltage_out", GTValues.V[GTValues.ZPM], GTValues.VNF[GTValues.ZPM]));
    }))).onRegister(attach(new CoverPlaceBehavior(GTCovers.SOLAR_PANEL[7]))).onRegister(compassNodeExist(GTCompassSections.COVERS, "solar_panel", GTCompassNodes.COVER)).register();
    public static ItemEntry<ComponentItem> COVER_SOLAR_PANEL_UV = REGISTRATE.item("uv_solar_panel", ComponentItem::create).lang("Ultimate Voltage Solar Panel").onRegister(attach(new TooltipBehavior(lines -> {
        lines.addAll(LangHandler.getMultiLang("metaitem.cover.solar.panel.tooltip"));
        lines.add(Component.translatable("gtceu.universal.tooltip.voltage_out", GTValues.V[GTValues.UV], GTValues.VNF[GTValues.UV]));
    }))).onRegister(attach(new CoverPlaceBehavior(GTCovers.SOLAR_PANEL[8]))).onRegister(compassNodeExist(GTCompassSections.COVERS, "solar_panel", GTCompassNodes.COVER)).register();

    // Plugin
    public static ItemEntry<Item> PLUGIN_TEXT;
    public static ItemEntry<Item> PLUGIN_ONLINE_PIC;
    public static ItemEntry<Item> PLUGIN_FAKE_GUI;
    public static ItemEntry<Item> PLUGIN_ADVANCED_MONITOR;

    public static ItemEntry<ComponentItem> INTEGRATED_CIRCUIT = REGISTRATE.item("programmed_circuit", ComponentItem::create)
            .lang("Programmed Circuit")
            .model(overrideModel(GTCEu.id("circuit"), 33))
            .onRegister(modelPredicate(GTCEu.id("circuit"), (itemStack) -> IntCircuitBehaviour.getCircuitConfiguration(itemStack) / 100f))
            .onRegister(attach(new IntCircuitBehaviour()))
            .onRegister(compassNode(GTCompassSections.MISC))
            .register();


    //    public static ItemEntry<ComponentItem> FOAM_SPRAYER = REGISTRATE.item("foam_sprayer", ComponentItem::create).onRegister(attach(new FoamSprayerBehavior()).setMaxStackSize(1);
    public static ItemEntry<Item> GELLED_TOLUENE = REGISTRATE.item("gelled_toluene", Item::new).onRegister(compassNode(GTCompassSections.MISC)).register();

    public static ItemEntry<Item> BOTTLE_PURPLE_DRINK = REGISTRATE.item("purple_drink", Item::new)
            .lang("Purple Drink")
            .properties(p -> p.food(GTFoods.DRINK))
            .onRegister(compassNode(GTCompassSections.MISC))
            .register();
    public static ItemEntry<ComponentItem> PLANT_BALL = REGISTRATE.item("plant_ball", ComponentItem::create).onRegister(compassNode(GTCompassSections.MISC)).onRegister(burnTime(75)).register();
    public static ItemEntry<ComponentItem> STICKY_RESIN = REGISTRATE.item("sticky_resin", ComponentItem::create).lang("Sticky Resin").onRegister(compassNode(GTCompassSections.MISC)).onRegister(burnTime(200)).register();
    public static ItemEntry<ComponentItem> BIO_CHAFF = REGISTRATE.item("bio_chaff", ComponentItem::create).onRegister(compassNode(GTCompassSections.MISC)).onRegister(burnTime(200)).register();
    public static ItemEntry<Item> ENERGIUM_DUST = REGISTRATE.item("energium_dust", Item::new).onRegister(compassNode(GTCompassSections.MISC)).register();

    public static ItemEntry<Item> POWER_UNIT_LV;
    public static ItemEntry<Item> POWER_UNIT_MV;
    public static ItemEntry<Item> POWER_UNIT_HV;
    public static ItemEntry<Item> POWER_UNIT_EV;
    public static ItemEntry<Item> POWER_UNIT_IV;

    public static ItemEntry<Item> NANO_SABER;
    public static ItemEntry<ComponentItem> PROSPECTOR_LV = REGISTRATE.item("prospector.lv", ComponentItem::create)
            .lang("Ore Prospector (LV)")
            .properties(p -> p.stacksTo(1))
            .onRegister(compassNodeExist(GTCompassSections.ITEMS, "prospector"))
            .onRegister(attach(ElectricStats.createElectricItem(100_000L, GTValues.LV), new ProspectorScannerBehavior(2, GTValues.V[GTValues.LV] / 16L, ProspectorMode.ORE))).register();
    public static ItemEntry<ComponentItem> PROSPECTOR_HV = REGISTRATE.item("prospector.hv", ComponentItem::create)
            .lang("Advanced Prospector (HV)")
            .properties(p -> p.stacksTo(1))
            .onRegister(compassNodeExist(GTCompassSections.ITEMS, "prospector"))
            .onRegister(attach(ElectricStats.createElectricItem(1_600_000L, GTValues.HV), new ProspectorScannerBehavior(3, GTValues.V[GTValues.HV] / 16L, ProspectorMode.ORE, ProspectorMode.FLUID, ConfigHolder.INSTANCE.machines.doBedrockOres ? ProspectorMode.BEDROCK_ORE : null))).register();
    public static ItemEntry<ComponentItem> PROSPECTOR_LUV = REGISTRATE.item("prospector.luv", ComponentItem::create)
            .lang("Super Prospector (LuV)")
            .properties(p -> p.stacksTo(1))
            .onRegister(compassNodeExist(GTCompassSections.ITEMS, "prospector"))
            .onRegister(attach(ElectricStats.createElectricItem(1_000_000_000L, GTValues.LuV), new ProspectorScannerBehavior(5, GTValues.V[GTValues.LuV] / 16L, ProspectorMode.ORE, ProspectorMode.FLUID, ConfigHolder.INSTANCE.machines.doBedrockOres ? ProspectorMode.BEDROCK_ORE : null))).register();

    public static ItemEntry<Item> TRICORDER_SCANNER;
    public static ItemEntry<Item> DEBUG_SCANNER;

    public static ItemEntry<Item> ITEM_MAGNET_LV;
    public static ItemEntry<Item> ITEM_MAGNET_HV;

    public static ItemEntry<Item> WIRELESS;
    public static ItemEntry<Item> CAMERA;
    public static ItemEntry<ComponentItem> TERMINAL = REGISTRATE.item("terminal", ComponentItem::create)
            .lang("Terminal")
            .properties(p -> p.stacksTo(1))
            .onRegister(compassNode(GTCompassSections.ITEMS))
            .onRegister(attach(new TerminalBehavior())).register();

    public static final ItemEntry<Item>[] DYE_ONLY_ITEMS = new ItemEntry[DyeColor.values().length];
    static {
        for (int i = 0; i < DyeColor.values().length; i++) {
            var dyeColor = DyeColor.values()[i];
            DYE_ONLY_ITEMS[i] = REGISTRATE.item("chemical_%s_dye".formatted(dyeColor.getName()), Item::new)
                    .lang("Chemical %s Dye".formatted(toEnglishName(dyeColor.getName())))
                    .tag(TagUtil.createPlatformItemTag("dyes/" + dyeColor.getName(), dyeColor.getName() + "_dyes"))
                    .onRegister(compassNodeExist(GTCompassSections.MISC, "chemical_dye"))
                    .register();
        }
    }

    public static final ItemEntry<ComponentItem>[] SPRAY_CAN_DYES = new ItemEntry[DyeColor.values().length];
    static {
        for (int i = 0; i < DyeColor.values().length; i++) {
            var dyeColor = DyeColor.values()[i];
            SPRAY_CAN_DYES[i] = REGISTRATE.item("%s_dye_spray_can".formatted(dyeColor.getName()), ComponentItem::create)
                    .lang("Spray Can (%s)".formatted(toEnglishName(dyeColor.getName())))
                    .properties(p -> p.stacksTo(1))
                    .onRegister(compassNodeExist(GTCompassSections.ITEMS, "empty_spray_can"))
                    .onRegister(attach(new ColorSprayBehaviour(() -> SPRAY_EMPTY.asStack(), 512, i))).register();
        }
    }

    public static ItemEntry<ComponentItem> TURBINE_ROTOR = REGISTRATE.item("turbine_rotor", ComponentItem::create)
            .properties(p -> p.stacksTo(1))
            .model((ctx, prov) -> createTextureModel(ctx, prov, GTCEu.id("item/tools/turbine")))
            .color(() -> IMaterialPartItem::getItemStackColor)
            .onRegister(compassNode(GTCompassSections.MISC))
            .onRegister(attach(new TurbineRotorBehaviour())).register();

    public static ItemEntry<Item> NEURO_PROCESSOR = REGISTRATE.item("neuro_processing_unit", Item::new).lang("Neuro Processing Unit").onRegister(compassNode(GTCompassSections.MISC)).register();
    public static ItemEntry<Item> STEM_CELLS = REGISTRATE.item("stem_cells", Item::new).onRegister(compassNode(GTCompassSections.MISC)).register();
    public static ItemEntry<Item> PETRI_DISH = REGISTRATE.item("petri_dish", Item::new).onRegister(compassNode(GTCompassSections.MISC)).register();

    public static ItemEntry<ComponentItem> VOLTAGE_COIL_ULV = REGISTRATE.item("ulv_voltage_coil", ComponentItem::create).lang("Ultra Low Voltage Coil").onRegister(compassNodeExist(GTCompassSections.MISC, "coil")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Lead, GTValues.M * 2), new MaterialStack(GTMaterials.IronMagnetic, GTValues.M / 2)))).register();
    public static ItemEntry<ComponentItem> VOLTAGE_COIL_LV = REGISTRATE.item("lv_voltage_coil", ComponentItem::create).lang("Low Voltage Coil").onRegister(compassNodeExist(GTCompassSections.MISC, "coil")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 2), new MaterialStack(GTMaterials.IronMagnetic, GTValues.M / 2)))).register();
    public static ItemEntry<ComponentItem> VOLTAGE_COIL_MV = REGISTRATE.item("mv_voltage_coil", ComponentItem::create).lang("Medium Voltage Coil").onRegister(compassNodeExist(GTCompassSections.MISC, "coil")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Aluminium, GTValues.M * 2), new MaterialStack(GTMaterials.SteelMagnetic, GTValues.M / 2)))).register();
    public static ItemEntry<ComponentItem> VOLTAGE_COIL_HV = REGISTRATE.item("hv_voltage_coil", ComponentItem::create).lang("High Voltage Coil").onRegister(compassNodeExist(GTCompassSections.MISC, "coil")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.BlackSteel, GTValues.M * 2), new MaterialStack(GTMaterials.SteelMagnetic, GTValues.M / 2)))).register();
    public static ItemEntry<ComponentItem> VOLTAGE_COIL_EV = REGISTRATE.item("ev_voltage_coil", ComponentItem::create).lang("Extreme Voltage Coil").onRegister(compassNodeExist(GTCompassSections.MISC, "coil")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.TungstenSteel, GTValues.M * 2), new MaterialStack(GTMaterials.NeodymiumMagnetic, GTValues.M / 2)))).register();
    public static ItemEntry<ComponentItem> VOLTAGE_COIL_IV = REGISTRATE.item("iv_voltage_coil", ComponentItem::create).lang("Insane Voltage Coil").onRegister(compassNodeExist(GTCompassSections.MISC, "coil")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Iridium, GTValues.M * 2), new MaterialStack(GTMaterials.NeodymiumMagnetic, GTValues.M / 2)))).register();
    public static ItemEntry<ComponentItem> VOLTAGE_COIL_LuV = REGISTRATE.item("luv_voltage_coil", ComponentItem::create).lang("Ludicrous Voltage Coil").onRegister(compassNodeExist(GTCompassSections.MISC, "coil")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Osmiridium, GTValues.M * 2), new MaterialStack(GTMaterials.SamariumMagnetic, GTValues.M / 2)))).register();
    public static ItemEntry<ComponentItem> VOLTAGE_COIL_ZPM = REGISTRATE.item("zpm_voltage_coil", ComponentItem::create).lang("Zero Point Module Voltage Coil").onRegister(compassNodeExist(GTCompassSections.MISC, "coil")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Europium, GTValues.M * 2), new MaterialStack(GTMaterials.SamariumMagnetic, GTValues.M / 2)))).register();
    public static ItemEntry<ComponentItem> VOLTAGE_COIL_UV = REGISTRATE.item("uv_voltage_coil", ComponentItem::create).lang("Ultimate Voltage Coil").onRegister(compassNodeExist(GTCompassSections.MISC, "coil")).onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Tritanium, GTValues.M * 2), new MaterialStack(GTMaterials.SamariumMagnetic, GTValues.M / 2)))).register();

    public static ItemEntry<Item> CLIPBOARD;

    // TODO ARMOR
//    public static ArmorMetaItem<?>.ArmorMetaValueItem NIGHTVISION_GOGGLES;
//
//    public static ArmorMetaItem<?>.ArmorMetaValueItem NANO_CHESTPLATE;
//    public static ArmorMetaItem<?>.ArmorMetaValueItem NANO_LEGGINGS;
//    public static ArmorMetaItem<?>.ArmorMetaValueItem NANO_BOOTS;
//    public static ArmorMetaItem<?>.ArmorMetaValueItem NANO_HELMET;
//
//    public static ArmorMetaItem<?>.ArmorMetaValueItem QUANTUM_CHESTPLATE;
//    public static ArmorMetaItem<?>.ArmorMetaValueItem QUANTUM_LEGGINGS;
//    public static ArmorMetaItem<?>.ArmorMetaValueItem QUANTUM_BOOTS;
//    public static ArmorMetaItem<?>.ArmorMetaValueItem QUANTUM_HELMET;
//
//    public static ArmorMetaItem<?>.ArmorMetaValueItem SEMIFLUID_JETPACK;
//    public static ArmorMetaItem<?>.ArmorMetaValueItem ELECTRIC_JETPACK;
//
//    public static ArmorMetaItem<?>.ArmorMetaValueItem ELECTRIC_JETPACK_ADVANCED;
//    public static ArmorMetaItem<?>.ArmorMetaValueItem NANO_CHESTPLATE_ADVANCED;
//    public static ArmorMetaItem<?>.ArmorMetaValueItem QUANTUM_CHESTPLATE_ADVANCED;

    public static ItemEntry<Item> POWER_THRUSTER = REGISTRATE.item("power_thruster", Item::new).properties(p -> p.rarity(Rarity.UNCOMMON)).onRegister(compassNode(GTCompassSections.MISC)).register();
    public static ItemEntry<Item> POWER_THRUSTER_ADVANCED = REGISTRATE.item("advanced_power_thruster", Item::new).lang("Advanced Power Thruster").properties(p -> p.rarity(Rarity.RARE)).onRegister(compassNode(GTCompassSections.MISC)).register();
    public static ItemEntry<Item> GRAVITATION_ENGINE = REGISTRATE.item("gravitation_engine_unit", Item::new).lang("Gravitation Engine Unit").properties(p -> p.rarity(Rarity.EPIC)).onRegister(compassNode(GTCompassSections.MISC)).register();

    public static ItemEntry<Item> SUS_RECORD;
    public static ItemEntry<Item> NAN_CERTIFICATE = REGISTRATE.item("nan_certificate", Item::new).lang("Certificate of Not Being a Noob Anymore").properties(p -> p.rarity(Rarity.EPIC)).onRegister(compassNodeExist(GTCompassSections.MISC, "certificate_of_not_being_a_noob_anymore")).register();

    public static ItemEntry<ComponentItem> FERTILIZER = REGISTRATE.item("fertilizer", ComponentItem::create).onRegister(attach(new FertilizerBehavior())).onRegister(compassNode(GTCompassSections.MISC)).register();
    public static ItemEntry<Item> BLACKLIGHT = REGISTRATE.item("blacklight", Item::new).onRegister(compassNode(GTCompassSections.MISC)).register();

    public static void init() {
        generateMaterialItems();
        generateTools();
    }

    public static <T extends ItemLike> NonNullConsumer<T> compassNode(CompassSection section, CompassNode... preNodes) {
        return item -> CompassNode.getOrCreate(section, item::asItem).addPreNode(preNodes);
    }

    public static <T extends ItemLike> NonNullConsumer<T> compassNodeExist(CompassSection section, String node, CompassNode... preNodes) {
        return item -> CompassNode.getOrCreate(section, node).addPreNode(preNodes).addItem(item::asItem);
    }

    public static <T extends ItemLike> NonNullConsumer<T> materialInfo(ItemMaterialInfo materialInfo) {
        return item -> ChemicalHelper.registerMaterialInfo(item, materialInfo);
    }

    public static <P, T extends Item, S2 extends ItemBuilder<T, P>> NonNullFunction<S2, S2> unificationItem(@Nonnull TagPrefix tagPrefix, @Nonnull Material mat) {
        return builder -> {
            builder.onRegister(item -> {
                UnificationEntry entry = new UnificationEntry(tagPrefix, mat);
                toUnify.put(entry, item);
                ChemicalHelper.registerUnificationItems(entry, item);
            });
            return builder;
        };
    }

    public static <T extends Item> void cauldronInteraction(T item) {
        if (item instanceof TagPrefixItem tagPrefixItem && purifyMap.containsKey(tagPrefixItem.tagPrefix)) {
            CauldronInteraction.WATER.put(item, (state, world, pos, player, hand, stack) -> {
                if (!world.isClientSide) {
                    Item stackItem = stack.getItem();
                    if (stackItem instanceof TagPrefixItem prefixItem) {
                        if (!purifyMap.containsKey(prefixItem.tagPrefix))
                            return InteractionResult.PASS;
                        if (!state.hasProperty(LayeredCauldronBlock.LEVEL)) {
                            return InteractionResult.PASS;
                        }

                        int level = state.getValue(LayeredCauldronBlock.LEVEL);
                        if (level == 0)
                            return InteractionResult.PASS;

                        player.setItemInHand(hand, ChemicalHelper.get(purifyMap.get(prefixItem.tagPrefix), prefixItem.material, stack.getCount()));
                        player.awardStat(Stats.USE_CAULDRON);
                        player.awardStat(Stats.ITEM_USED.get(stackItem));
                        LayeredCauldronBlock.lowerFillLevel(state, world, pos);

                    }
                }


                return InteractionResult.sidedSuccess(world.isClientSide);
            });

        }
    }

    @ExpectPlatform
    public static <T extends ComponentItem> NonNullConsumer<T> burnTime(int burnTime) {
        throw new AssertionError();
    }

    public static <T extends ComponentItem> NonNullConsumer<T> attach(IItemComponent... components) {
        return item -> item.attachComponents(components);
    }

    @ExpectPlatform
    public static <T extends Item> NonNullConsumer<T> modelPredicate(ResourceLocation predicate, Function<ItemStack, Float> property) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerToolTier(MaterialToolTier tier, ResourceLocation id, Collection<ResourceLocation> before, Collection<ResourceLocation> after) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ResourceLocation getTierName(Tier tier) {
        throw new AssertionError();
    }

    @NotNull
    private static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateLangProvider> reverseLangValue() {
        return (ctx, prov) -> {
            var names = Arrays.stream(ctx.getName().split("/.")).collect(Collectors.toList());
            Collections.reverse(names);
            prov.add(ctx.get(), names.stream().map(StringUtils::capitalize).collect(Collectors.joining(" ")));
        };
    }

}
