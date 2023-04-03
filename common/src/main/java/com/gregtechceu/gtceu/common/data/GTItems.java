package com.gregtechceu.gtceu.common.data;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.cover.filter.SimpleFluidFilter;
import com.gregtechceu.gtceu.api.cover.filter.SimpleItemFilter;
import com.gregtechceu.gtceu.api.cover.filter.TagFluidFilter;
import com.gregtechceu.gtceu.api.cover.filter.TagItemFilter;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.*;
import com.gregtechceu.gtceu.common.item.*;
import com.gregtechceu.gtceu.common.data.materials.GTFoods;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterial;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.ItemMaterialInfo;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.item.GTToolItem;
import com.gregtechceu.gtceu.api.item.TagPrefixItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.MaterialToolTier;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.data.data.LangHandler;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.client.model.generators.ModelFile;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.gregtechceu.gtceu.api.registry.GTRegistries.REGISTRATE;
import static com.gregtechceu.gtceu.common.data.GTCreativeModeTabs.*;
import static com.gregtechceu.gtceu.utils.FormattingUtil.*;

/**
 * @author KilaBash
 * @date 2023/2/14
 * @implNote GTItems
 */
public class GTItems {

    //////////////////////////////////////
    //*****     Material Items    ******//
    //////////////////////////////////////
    public final static Table<TagPrefix, Material, ItemEntry<TagPrefixItem>> MATERIAL_ITEMS;

    static {
        REGISTRATE.creativeModeTab(() -> MATERIAL_ITEM);
        ImmutableTable.Builder<TagPrefix, Material, ItemEntry<TagPrefixItem>> builder = ImmutableTable.builder();
        for (var tagPrefix : TagPrefix.values()) {
            if (tagPrefix.doGenerateItem()) {
                for (Material material : GTRegistries.MATERIALS) {
                    if (tagPrefix.doGenerateItem(material)) {
                        builder.put(tagPrefix, material, REGISTRATE
                                .item(toLowerCaseUnder(tagPrefix.name) + "." + material.getName(), properties -> new TagPrefixItem(properties, tagPrefix, material))
                                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                                .transform(unificationItem(tagPrefix, material))
                                .properties(p -> p.stacksTo(tagPrefix.maxStackSize()))
                                .model(NonNullBiConsumer.noop())
                                .color(() -> () -> TagPrefixItem::tintColor)
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
    public final static Table<MaterialToolTier, GTToolType, ItemEntry<GTToolItem>> TOOL_ITEMS =
            ArrayTable.create(GTRegistries.MATERIALS.values().stream().filter(mat -> mat.hasProperty(PropertyKey.TOOL)).map(Material::getToolTier).toList(),
                    Arrays.stream(GTToolType.values()).toList());
    static {
        REGISTRATE.creativeModeTab(() -> TOOL);

        for (Material material : GTRegistries.MATERIALS.values()) {
            if (material.hasProperty(PropertyKey.TOOL)) {
                var property = material.getProperty(PropertyKey.TOOL);
                var tier = material.getToolTier();
                for (GTToolType toolType : GTToolType.values()) {
                    if (property.hasType(toolType)) {
                        TOOL_ITEMS.put(tier, toolType, REGISTRATE.item("%s_%s".formatted(toolType.name, tier.material.getName().toLowerCase()), p -> GTToolItem.create(toolType, tier, p))
                                .properties(p -> p.craftRemainder(Items.AIR))
                                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                                .model(NonNullBiConsumer.noop())
                                .color(() -> () -> GTToolItem::tintColor)
                                .tag(toolType.itemTag)
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
    public static ItemEntry<Item> CREDIT_COPPER = REGISTRATE.item("credit.copper", Item::new).lang("Copper Credit").register();
    public static ItemEntry<Item> CREDIT_CUPRONICKEL = REGISTRATE.item("credit.cupronickel", Item::new).lang("Cupronickel Credit").defaultModel().register();
    public static ItemEntry<Item> CREDIT_SILVER = REGISTRATE.item("credit.silver", Item::new).lang("Silver Credit").properties(p -> p.rarity(Rarity.UNCOMMON)).register();
    public static ItemEntry<Item> CREDIT_GOLD = REGISTRATE.item("credit.gold", Item::new).lang("Gold Credit").properties(p -> p.rarity(Rarity.UNCOMMON)).register();
    public static ItemEntry<Item> CREDIT_PLATINUM = REGISTRATE.item("credit.platinum", Item::new).lang("Platinum Credit").properties(p -> p.rarity(Rarity.RARE)).register();
    public static ItemEntry<Item> CREDIT_OSMIUM = REGISTRATE.item("credit.osmium", Item::new).lang("Osmium Credit").properties(p -> p.rarity(Rarity.RARE)).register();
    public static ItemEntry<Item> CREDIT_NAQUADAH = REGISTRATE.item("credit.naquadah", Item::new).lang("Naquadah Credit").properties(p -> p.rarity(Rarity.EPIC)).register();
    public static ItemEntry<Item> CREDIT_NEUTRONIUM = REGISTRATE.item("credit.neutronium", Item::new).lang("Neutronium Credit").properties(p -> p.rarity(Rarity.EPIC)).register();
    public static ItemEntry<Item> COIN_GOLD_ANCIENT = REGISTRATE.item("coin.gold.ancient", Item::new).lang("Ancient Gold Coin").properties(p -> p.rarity(Rarity.RARE))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Gold, GTValues.M / 4)))).register();
    public static ItemEntry<Item> COIN_DOGE = REGISTRATE.item("coin.doge", Item::new).lang("Dog Coin").properties(p -> p.rarity(Rarity.EPIC))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Brass, GTValues.M / 4)))).register();
    public static ItemEntry<Item> COIN_CHOCOLATE = REGISTRATE.item("coin.chocolate", Item::new)
            .lang("Chocolate Coin")
            .properties(p -> p.rarity(Rarity.EPIC).food(GTFoods.CHOCOLATE))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Gold, GTValues.M / 4)))).register();
    public static ItemEntry<Item> COMPRESSED_CLAY = REGISTRATE.item("compressed.clay", Item::new)
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, GTValues.M)))).register();
    public static ItemEntry<Item> COMPRESSED_COKE_CLAY = REGISTRATE.item("compressed.coke_clay", Item::new)
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, GTValues.M)))).register();
    public static ItemEntry<Item> COMPRESSED_FIRECLAY = REGISTRATE.item("compressed.fireclay", Item::new)
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Fireclay, GTValues.M)))).register();
    public static ItemEntry<Item> FIRECLAY_BRICK = REGISTRATE.item("brick.fireclay", Item::new)
            .lang("Firebrick")
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Fireclay, GTValues.M)))).register();
    public static ItemEntry<Item> COKE_OVEN_BRICK = REGISTRATE.item("brick.coke", Item::new)
            .lang("Coke Oven Brick")
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, GTValues.M)))).register();
    public static ItemEntry<Item> WOODEN_FORM_EMPTY = REGISTRATE.item("wooden_form.empty", Item::new).lang("Empty Wooden Form").register();
    public static ItemEntry<ComponentItem> WOODEN_FORM_BRICK = REGISTRATE.item("wooden_form.brick", ComponentItem::create)
            .lang("Brick Wooden Form")
            .properties(p -> p.craftRemainder(Items.AIR))
            .onRegister(attach((IRecipeRemainder) itemStack -> itemStack)).register();

    public static ItemEntry<Item> SHAPE_EMPTY = REGISTRATE.item("shape.empty", Item::new)
            .lang("Empty Shape Plate")
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();

    public static final ItemEntry<Item>[] SHAPE_MOLDS = new ItemEntry[13];
    public static ItemEntry<Item> SHAPE_MOLD_PLATE;
    public static ItemEntry<Item> SHAPE_MOLD_GEAR;
    public static ItemEntry<Item> SHAPE_MOLD_CREDIT;
    public static ItemEntry<Item> SHAPE_MOLD_BOTTLE;
    public static ItemEntry<Item> SHAPE_MOLD_INGOT;
    public static ItemEntry<Item> SHAPE_MOLD_BALL;
    public static ItemEntry<Item> SHAPE_MOLD_BLOCK;
    public static ItemEntry<Item> SHAPE_MOLD_NUGGET;
    public static ItemEntry<Item> SHAPE_MOLD_CYLINDER;
    public static ItemEntry<Item> SHAPE_MOLD_ANVIL;
    public static ItemEntry<Item> SHAPE_MOLD_NAME;
    public static ItemEntry<Item> SHAPE_MOLD_GEAR_SMALL;
    public static ItemEntry<Item> SHAPE_MOLD_ROTOR;

    static {
        SHAPE_MOLDS[0] = SHAPE_MOLD_PLATE = REGISTRATE.item("shape.mold.plate", Item::new)
                .lang("Mold (Plate)").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_MOLDS[1] = SHAPE_MOLD_GEAR = REGISTRATE.item("shape.mold.gear", Item::new)
                .lang("Mold (Gear)").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_MOLDS[2] = SHAPE_MOLD_CREDIT = REGISTRATE.item("shape.mold.credit", Item::new)
                .lang("Mold (Coinage)").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_MOLDS[3] = SHAPE_MOLD_BOTTLE = REGISTRATE.item("shape.mold.bottle", Item::new)
                .lang("Mold (Bottle)").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_MOLDS[4] = SHAPE_MOLD_INGOT = REGISTRATE.item("shape.mold.ingot", Item::new)
                .lang("Mold (Ingot)").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_MOLDS[5] = SHAPE_MOLD_BALL = REGISTRATE.item("shape.mold.ball", Item::new)
                .lang("Mold (Ball)").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_MOLDS[6] = SHAPE_MOLD_BLOCK = REGISTRATE.item("shape.mold.block", Item::new)
                .lang("Mold (Block)").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_MOLDS[7] = SHAPE_MOLD_NUGGET = REGISTRATE.item("shape.mold.nugget", Item::new)
                .lang("Mold (Nugget)").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_MOLDS[8] = SHAPE_MOLD_CYLINDER = REGISTRATE.item("shape.mold.cylinder", Item::new)
                .lang("Mold (Cylinder)").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_MOLDS[9] = SHAPE_MOLD_ANVIL = REGISTRATE.item("shape.mold.anvil", Item::new)
                .lang("Mold (Anvil)").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_MOLDS[10] = SHAPE_MOLD_NAME = REGISTRATE.item("shape.mold.name", Item::new)
                .lang("Mold (Name)").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_MOLDS[11] = SHAPE_MOLD_GEAR_SMALL = REGISTRATE.item("shape.mold.gear.small", Item::new)
                .lang("Mold (Small Gear)").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_MOLDS[12] = SHAPE_MOLD_ROTOR = REGISTRATE.item("shape.mold.rotor", Item::new)
                .lang("Mold (Rotor)").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
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
        SHAPE_EXTRUDERS[0] = SHAPE_EXTRUDER_PLATE = REGISTRATE.item("shape.extruder.plate", Item::new)
                .lang("Extruder Shape (Plate)").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[1] = SHAPE_EXTRUDER_ROD = REGISTRATE.item("shape.extruder.rod", Item::new)
                .lang("Extruder Shape (Rod)").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[2] = SHAPE_EXTRUDER_BOLT = REGISTRATE.item("shape.extruder.bolt", Item::new)
                .lang("Extruder Shape (Bold)").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[3] = SHAPE_EXTRUDER_RING = REGISTRATE.item("shape.extruder.ring", Item::new)
                .lang("Extruder Shape (Ring)").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[4] = SHAPE_EXTRUDER_CELL = REGISTRATE.item("shape.extruder.cell", Item::new)
                .lang("Extruder Shape (Cell)").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[5] = SHAPE_EXTRUDER_INGOT = REGISTRATE.item("shape.extruder.ingot", Item::new)
                .lang("Extruder Shape (Ingot)").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[6] = SHAPE_EXTRUDER_WIRE = REGISTRATE.item("shape.extruder.wire", Item::new)
                .lang("Extruder Shape (Wire)").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[7] = SHAPE_EXTRUDER_PIPE_TINY = REGISTRATE.item("shape.extruder.pipe.tiny", Item::new)
                .lang("Extruder Shape (Tiny Pipe)").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[8] = SHAPE_EXTRUDER_PIPE_SMALL = REGISTRATE.item("shape.extruder.pipe.small", Item::new)
                .lang("Extruder Shape (Small Pipe)").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[9] = SHAPE_EXTRUDER_PIPE_NORMAL = REGISTRATE.item("shape.extruder.pipe.normal", Item::new)
                .lang("Extruder Shape (Normal Pipe)").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[10] = SHAPE_EXTRUDER_PIPE_LARGE = REGISTRATE.item("shape.extruder.pipe.large", Item::new)
                .lang("Extruder Shape (Large Pipe)").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[11] = SHAPE_EXTRUDER_PIPE_HUGE = REGISTRATE.item("shape.extruder.pipe.huge", Item::new)
                .lang("Extruder Shape (Huge Pipe)").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[12] = SHAPE_EXTRUDER_BLOCK = REGISTRATE.item("shape.extruder.block", Item::new)
                .lang("Extruder Shape (Block)").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        // Extruder Shapes index 13-20 (inclusive), id 44-51 (inclusive) are unused
        SHAPE_EXTRUDERS[21] = SHAPE_EXTRUDER_GEAR = REGISTRATE.item("shape.extruder.gear", Item::new)
                .lang("Extruder Shape (Gear)").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[22] = SHAPE_EXTRUDER_BOTTLE = REGISTRATE.item("shape.extruder.bottle", Item::new)
                .lang("Extruder Shape (Bottle)").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[23] = SHAPE_EXTRUDER_FOIL = REGISTRATE.item("shape.extruder.foil", Item::new)
                .lang("Extruder Shape (Foil)").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[24] = SHAPE_EXTRUDER_GEAR_SMALL = REGISTRATE.item("shape.extruder.gear_small", Item::new)
                .lang("Extruder Shape (Small Gear)").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[25] = SHAPE_EXTRUDER_ROD_LONG = REGISTRATE.item("shape.extruder.rod_long", Item::new)
                .lang("Extruder Shape (Long Rod)").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
        SHAPE_EXTRUDERS[26] = SHAPE_EXTRUDER_ROTOR = REGISTRATE.item("shape.extruder.rotor", Item::new)
                .lang("Extruder Shape (Rotor)").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
    }
    public static ItemEntry<Item> SPRAY_EMPTY = REGISTRATE.item("spray.empty", Item::new).lang("Spray Can (Empty)").register();
    public static ItemEntry<ComponentItem> SPRAY_SOLVENT = REGISTRATE.item("spray.solvent", ComponentItem::create)
            .lang("Spray Can (Solvent)")
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(new ColorSprayBehaviour(() -> SPRAY_EMPTY.asStack(), 1024, -1))).register();
    public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelProvider> cellModel() {
        return (ctx, prov) -> {
            // empty model
            prov.getBuilder("item/" + prov.name(ctx::getEntry) + "_empty").parent(new ModelFile.UncheckedModelFile("item/generated"))
                    .texture("layer0", prov.modLoc("item/%s/base".formatted(prov.name(ctx))));

            // filled model
            prov.getBuilder("item/" + prov.name(ctx::getEntry) + "_filled").parent(new ModelFile.UncheckedModelFile("item/generated"))
                    .texture("layer0", prov.modLoc("item/%s/base".formatted(prov.name(ctx))))
                    .texture("layer1", prov.modLoc("item/%s/overlay".formatted(prov.name(ctx))));

            // root model
            prov.generated(ctx::getEntry, prov.modLoc("item/%s/base".formatted(prov.name(ctx))))
                    .override().predicate(GTCEu.id("fluid_cell"), 0)
                    .model(new ModelFile.UncheckedModelFile(prov.modLoc("item/%s_empty".formatted(prov.name(ctx)))))
                    .end()
                    .override().predicate(GTCEu.id("fluid_cell"), 1)
                    .model(new ModelFile.UncheckedModelFile(prov.modLoc("item/%s_filled".formatted(prov.name(ctx)))))
                    .end();
        };
    }

    public static int cellColor(ItemStack itemStack, int index) {
        if (index == 1) {
            var held = FluidTransferHelper.getFluidContained(itemStack);
            if (held != null) {
                return FluidHelper.getColor(held);
            }
        }
        return -1;
    }

    public static ICustomDescriptionId cellName() {
        return itemStack -> {
            var held = FluidTransferHelper.getFluidContained(itemStack);
            var prefix = LocalizationUtils.format("fluid_cell.empty");
            if (held != null && !held.isEmpty()) {
                prefix = FluidHelper.getDisplayName(held).getString();
            }
            return "%s %s".formatted(prefix, LocalizationUtils.format(itemStack.getItem().getDescriptionId()));
        };
    }

    public static ItemEntry<ComponentItem> FLUID_CELL = REGISTRATE.item("fluid_cell", ComponentItem::create)
            .model(cellModel())
            .color(() -> () -> GTItems::cellColor)
            .onRegister(modelPredicate(GTCEu.id("fluid_cell"), (itemStack) -> FluidTransferHelper.getFluidContained(itemStack) == null ? 0f : 1f))
            .onRegister(attach(ThermalFluidStats.create((int)FluidHelper.getBucket(), 1800, true, false, false, false, false), new ItemFluidContainer(), cellName())).register();
    public static ItemEntry<ComponentItem> FLUID_CELL_UNIVERSAL = REGISTRATE.item("fluid_cell.universal", ComponentItem::create)
            .lang("Universal Cell")
            .model(cellModel())
            .color(() -> () -> GTItems::cellColor)
            .onRegister(modelPredicate(GTCEu.id("fluid_cell"), (itemStack) -> FluidTransferHelper.getFluidContained(itemStack) == null ? 0f : 1f))
            .onRegister(attach(cellName(), ThermalFluidStats.create((int)FluidHelper.getBucket(), 1800, true, false, false, false, true), new ItemFluidContainer())).register();
    public static ItemEntry<ComponentItem> FLUID_CELL_LARGE_STEEL = REGISTRATE.item("large_fluid_cell.steel", ComponentItem::create)
            .lang("Steel Cell")
            .model(cellModel())
            .color(() -> () -> GTItems::cellColor)
            .onRegister(modelPredicate(GTCEu.id("fluid_cell"), (itemStack) -> FluidTransferHelper.getFluidContained(itemStack) == null ? 0f : 1f))
            .onRegister(attach(cellName(), ThermalFluidStats.create((int)FluidHelper.getBucket() * 8, GTMaterials.Steel.getProperty(PropertyKey.FLUID_PIPE).getMaxFluidTemperature(), true, false, false, false, true), new ItemFluidContainer()))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
    public static ItemEntry<ComponentItem> FLUID_CELL_LARGE_ALUMINIUM = REGISTRATE.item("large_fluid_cell.aluminium", ComponentItem::create)
            .lang("Aluminium Cell")
            .model(cellModel())
            .color(() -> () -> GTItems::cellColor)
            .onRegister(modelPredicate(GTCEu.id("fluid_cell"), (itemStack) -> FluidTransferHelper.getFluidContained(itemStack) == null ? 0f : 1f))
            .onRegister(attach(cellName(), ThermalFluidStats.create((int)FluidHelper.getBucket() * 32, GTMaterials.Aluminium.getProperty(PropertyKey.FLUID_PIPE).getMaxFluidTemperature(), true, false, false, false, true), new ItemFluidContainer()))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Aluminium, GTValues.M * 4)))).register();
    public static ItemEntry<ComponentItem> FLUID_CELL_LARGE_STAINLESS_STEEL = REGISTRATE.item("large_fluid_cell.stainless_steel", ComponentItem::create)
            .lang("Stainless Steel Cell")
            .model(cellModel())
            .color(() -> () -> GTItems::cellColor)
            .onRegister(modelPredicate(GTCEu.id("fluid_cell"), (itemStack) -> FluidTransferHelper.getFluidContained(itemStack) == null ? 0f : 1f))
            .onRegister(attach(cellName(), ThermalFluidStats.create((int)FluidHelper.getBucket() * 64, GTMaterials.StainlessSteel.getProperty(PropertyKey.FLUID_PIPE).getMaxFluidTemperature(), true, false, false, false, true), new ItemFluidContainer()))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.StainlessSteel, GTValues.M * 6)))).register();
    public static ItemEntry<ComponentItem> FLUID_CELL_LARGE_TITANIUM = REGISTRATE.item("large_fluid_cell.titanium", ComponentItem::create)
            .lang("Titanium Cell")
            .model(cellModel())
            .color(() -> () -> GTItems::cellColor)
            .onRegister(modelPredicate(GTCEu.id("fluid_cell"), (itemStack) -> FluidTransferHelper.getFluidContained(itemStack) == null ? 0f : 1f))
            .onRegister(attach(cellName(), ThermalFluidStats.create((int)FluidHelper.getBucket() * 128, GTMaterials.TungstenSteel.getProperty(PropertyKey.FLUID_PIPE).getMaxFluidTemperature(), true, false, false, false, true), new ItemFluidContainer()))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.TungstenSteel, GTValues.M * 6)))).register();
    public static ItemEntry<ComponentItem> FLUID_CELL_LARGE_TUNGSTEN_STEEL = REGISTRATE.item("large_fluid_cell.tungstensteel", ComponentItem::create)
            .lang("Tungstensteel Cell")
            .model(cellModel())
            .color(() -> () -> GTItems::cellColor)
            .properties(p -> p.stacksTo(32))
            .onRegister(modelPredicate(GTCEu.id("fluid_cell"), (itemStack) -> FluidTransferHelper.getFluidContained(itemStack) == null ? 0f : 1f))
            .onRegister(attach(cellName(), ThermalFluidStats.create((int)FluidHelper.getBucket() * 512, GTMaterials.TungstenSteel.getProperty(PropertyKey.FLUID_PIPE).getMaxFluidTemperature(), true, false, false, false, true), new ItemFluidContainer()))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.TungstenSteel, GTValues.M * 8)))).register();
    public static ItemEntry<ComponentItem> FLUID_CELL_GLASS_VIAL = REGISTRATE.item("glass_vial", ComponentItem::create)
            .model(cellModel())
            .color(() -> () -> GTItems::cellColor)
            .onRegister(modelPredicate(GTCEu.id("fluid_cell"), (itemStack) -> FluidTransferHelper.getFluidContained(itemStack) == null ? 0f : 1f))
            .onRegister(attach(cellName(), ThermalFluidStats.create((int)FluidHelper.getBucket() * 1000, 1200, false, true, false, false, true), new ItemFluidContainer()))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Glass, GTValues.M * 4)))).register();

    // TODO Lighter
    public static ItemEntry<Item> TOOL_MATCHES;
    public static ItemEntry<Item> TOOL_MATCHBOX;
    public static ItemEntry<Item> TOOL_LIGHTER_INVAR;
    public static ItemEntry<Item> TOOL_LIGHTER_PLATINUM;

    public static ItemEntry<Item> CARBON_FIBERS = REGISTRATE.item("carbon.fibers", Item::new).lang("Raw Carbon Fibers").register();
    public static ItemEntry<Item> CARBON_MESH = REGISTRATE.item("carbon.mesh", Item::new).lang("Carbon Fiber Mesh").register();
    public static ItemEntry<Item> CARBON_FIBER_PLATE = REGISTRATE.item("carbon.plate", Item::new).lang("Carbon Fiber Plate").register();
    public static ItemEntry<Item> DUCT_TAPE = REGISTRATE.item("duct_tape", Item::new).lang("BrainTech Aerospace Advanced Reinforced Duct Tape FAL-84").register();

    public static ItemEntry<Item> NEUTRON_REFLECTOR = REGISTRATE.item("neutron_reflector", Item::new).lang("Iridium Neutron Reflector").register();

    public static ItemEntry<Item> BATTERY_HULL_LV  = REGISTRATE.item("battery.hull.lv", Item::new).lang("Small Battery Hull").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.BatteryAlloy, GTValues.M)))).register();
    public static ItemEntry<Item> BATTERY_HULL_MV  = REGISTRATE.item("battery.hull.mv", Item::new).lang("Medium Battery Hull").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.BatteryAlloy, GTValues.M * 3)))).register();
    public static ItemEntry<Item> BATTERY_HULL_HV  = REGISTRATE.item("battery.hull.hv", Item::new).lang("Large Battery Hull").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.BatteryAlloy, GTValues.M * 9)))).register();
    public static ItemEntry<Item> BATTERY_HULL_SMALL_VANADIUM  = REGISTRATE.item("battery.hull.ev", Item::new).lang("Small Vanadium Battery Hull").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.BlueSteel, GTValues.M * 2)))).register();
    public static ItemEntry<Item> BATTERY_HULL_MEDIUM_VANADIUM  = REGISTRATE.item("battery.hull.iv", Item::new).lang("Medium Vanadium Battery Hull").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.RoseGold, GTValues.M * 6)))).register();
    public static ItemEntry<Item> BATTERY_HULL_LARGE_VANADIUM  = REGISTRATE.item("battery.hull.luv", Item::new).lang("Large Vanadium Battery Hull").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.RedSteel, GTValues.M * 18)))).register();
    public static ItemEntry<Item> BATTERY_HULL_MEDIUM_NAQUADRIA  = REGISTRATE.item("battery.hull.zpm", Item::new).lang("Medium Naquadria Battery Hull").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Europium, GTValues.M * 6)))).register();
    public static ItemEntry<Item> BATTERY_HULL_LARGE_NAQUADRIA  = REGISTRATE.item("battery.hull.uv", Item::new).lang("Large Naquadria Battery Hull").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Americium, GTValues.M * 18)))).register();

    public static ItemEntry<ComponentItem> BATTERY_ULV_TANTALUM = REGISTRATE.item("battery.re.ulv.tantalum", ComponentItem::create)
            .lang("Tantalum Capacitor")
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(ElectricStats.createRechargeableBattery(1000, GTValues.ULV)))
            .tag(CustomTags.ULV_BATTERIES).register();

    public static ItemEntry<ComponentItem> BATTERY_LV_SODIUM = REGISTRATE.item("battery.re.lv.sodium", ComponentItem::create)
            .lang("Small Sodium Battery")
            .properties(p -> p.stacksTo(1))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(80000, GTValues.LV)))
            .tag(CustomTags.LV_BATTERIES).register();
    public static ItemEntry<ComponentItem> BATTERY_MV_SODIUM = REGISTRATE.item("battery.re.mv.sodium", ComponentItem::create)
            .lang("Medium Sodium Battery")
            .properties(p -> p.stacksTo(1))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(360000, GTValues.MV)))
            .tag(CustomTags.MV_BATTERIES).register();
    public static ItemEntry<ComponentItem> BATTERY_HV_SODIUM = REGISTRATE.item("battery.re.hv.sodium", ComponentItem::create)
            .lang("Large Sodium Battery")
            .properties(p -> p.stacksTo(1))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(1200000, GTValues.HV)))
            .tag(CustomTags.HV_BATTERIES).register();

    public static ItemEntry<ComponentItem> BATTERY_LV_LITHIUM = REGISTRATE.item("battery.re.lv.lithium", ComponentItem::create)
            .lang("Small Lithium Battery")
            .properties(p -> p.stacksTo(1))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(120000, GTValues.LV)))
            .tag(CustomTags.LV_BATTERIES).register();
    public static ItemEntry<ComponentItem> BATTERY_MV_LITHIUM = REGISTRATE.item("battery.re.mv.lithium", ComponentItem::create)
            .lang("Medium Lithium Battery")
            .properties(p -> p.stacksTo(1))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(420000, GTValues.MV)))
            .tag(CustomTags.MV_BATTERIES).register();
    public static ItemEntry<ComponentItem> BATTERY_HV_LITHIUM = REGISTRATE.item("battery.re.hv.lithium", ComponentItem::create)
            .lang("Large Lithium Battery")
            .properties(p -> p.stacksTo(1))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(1800000, GTValues.HV)))
            .tag(CustomTags.HV_BATTERIES).register();

    public static ItemEntry<ComponentItem> BATTERY_LV_CADMIUM = REGISTRATE.item("battery.re.lv.cadmium", ComponentItem::create)
            .lang("Small Cadmium Battery")
            .properties(p -> p.stacksTo(1))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(100000, GTValues.LV)))
            .tag(CustomTags.LV_BATTERIES).register();
    public static ItemEntry<ComponentItem> BATTERY_MV_CADMIUM = REGISTRATE.item("battery.re.mv.cadmium", ComponentItem::create)
            .lang("Medium Cadmium Battery")
            .properties(p -> p.stacksTo(1))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(400000, GTValues.MV)))
            .tag(CustomTags.MV_BATTERIES).register();
    public static ItemEntry<ComponentItem> BATTERY_HV_CADMIUM = REGISTRATE.item("battery.re.hv.cadmium", ComponentItem::create)
            .lang("Large Cadmium Battery")
            .properties(p -> p.stacksTo(1))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(1600000, GTValues.HV)))
            .tag(CustomTags.HV_BATTERIES).register();

    public static ItemEntry<ComponentItem> ENERGIUM_CRYSTAL = REGISTRATE.item("energy_crystal", ComponentItem::create)
            .lang("Energium Crystal")
            .properties(p -> p.stacksTo(1))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(6_400_000L, GTValues.HV)))
            .tag(CustomTags.HV_BATTERIES).register();
    public static ItemEntry<ComponentItem> LAPOTRON_CRYSTAL = REGISTRATE.item("lapotron_crystal", ComponentItem::create)
            .properties(p -> p.stacksTo(1))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(25_000_000L, GTValues.EV)))
            .tag(CustomTags.EV_BATTERIES).register();

    public static ItemEntry<ComponentItem> BATTERY_EV_VANADIUM = REGISTRATE.item("battery.ev.vanadium", ComponentItem::create)
            .lang("Small Vanadium Battery")
            .properties(p -> p.stacksTo(1))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(10_240_000L, GTValues.EV)))
            .tag(CustomTags.EV_BATTERIES).register();
    public static ItemEntry<ComponentItem> BATTERY_IV_VANADIUM = REGISTRATE.item("battery.iv.vanadium", ComponentItem::create)
            .lang("Medium Vanadium Battery")
            .properties(p -> p.stacksTo(1))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(40_960_000L, GTValues.IV)))
            .tag(CustomTags.IV_BATTERIES).register();
    public static ItemEntry<ComponentItem> BATTERY_LUV_VANADIUM = REGISTRATE.item("battery.luv.vanadium", ComponentItem::create)
            .lang("Large Vanadium Battery")
            .properties(p -> p.stacksTo(1))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(163_840_000L, GTValues.LuV)))
            .tag(CustomTags.LuV_BATTERIES).register();

    public static ItemEntry<ComponentItem> BATTERY_ZPM_NAQUADRIA = REGISTRATE.item("battery.zpm.naquadria", ComponentItem::create)
            .lang("Medium Naquadria Battery")
            .properties(p -> p.stacksTo(1))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(655_360_000L, GTValues.ZPM)))
            .tag(CustomTags.ZPM_BATTERIES).register();
    public static ItemEntry<ComponentItem> BATTERY_UV_NAQUADRIA = REGISTRATE.item("battery.uv.naquadria", ComponentItem::create)
            .lang("Large Naquadria Battery")
            .properties(p -> p.stacksTo(1))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(2_621_440_000L, GTValues.UV)))
            .tag(CustomTags.UV_BATTERIES).register();

    public static ItemEntry<ComponentItem> ENERGY_LAPOTRONIC_ORB = REGISTRATE.item("energy.lapotronic_orb", ComponentItem::create)
            .lang("Lapotronic Energy Orb")
            .properties(p -> p.stacksTo(1))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(250_000_000L, GTValues.IV)))
            .tag(CustomTags.IV_BATTERIES).register();
    public static ItemEntry<ComponentItem> ENERGY_LAPOTRONIC_ORB_CLUSTER = REGISTRATE.item("energy.lapotronic_orb_cluster", ComponentItem::create)
            .lang("Lapotronic Energy Orb Cluster")
            .properties(p -> p.stacksTo(1))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(1_000_000_000L, GTValues.LuV)))
            .tag(CustomTags.LuV_BATTERIES).register();

    public static ItemEntry<ComponentItem> ENERGY_MODULE = REGISTRATE.item("energy.module", ComponentItem::create)
            .properties(p -> p.stacksTo(1))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(4_000_000_000L, GTValues.ZPM)))
            .tag(CustomTags.ZPM_BATTERIES).register();
    public static ItemEntry<ComponentItem> ENERGY_CLUSTER = REGISTRATE.item("energy.cluster", ComponentItem::create)
            .properties(p -> p.stacksTo(1))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(20_000_000_000L, GTValues.UV)))
            .tag(CustomTags.UV_BATTERIES).register();

    public static ItemEntry<ComponentItem> ZERO_POINT_MODULE = REGISTRATE.item("zpm", ComponentItem::create)
            .lang("Zero Point Module")
            .properties(p -> p.stacksTo(1))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createBattery(2000000000000L, GTValues.ZPM, true))).register();
    public static ItemEntry<ComponentItem> ULTIMATE_BATTERY = REGISTRATE.item("max_battery", ComponentItem::create)
            .properties(p -> p.stacksTo(1))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(Long.MAX_VALUE, GTValues.UHV)))
            .tag(CustomTags.UHV_BATTERIES).register();

    public static ItemEntry<Item> ELECTRIC_MOTOR_LV = REGISTRATE.item("electric.motor.lv", Item::new).lang("LV Electric Motor").register();
    public static ItemEntry<Item> ELECTRIC_MOTOR_MV = REGISTRATE.item("electric.motor.mv", Item::new).lang("MV Electric Motor").register();
    public static ItemEntry<Item> ELECTRIC_MOTOR_HV = REGISTRATE.item("electric.motor.hv", Item::new).lang("HV Electric Motor").register();
    public static ItemEntry<Item> ELECTRIC_MOTOR_EV = REGISTRATE.item("electric.motor.ev", Item::new).lang("EV Electric Motor").register();
    public static ItemEntry<Item> ELECTRIC_MOTOR_IV = REGISTRATE.item("electric.motor.iv", Item::new).lang("IV Electric Motor").register();
    public static ItemEntry<Item> ELECTRIC_MOTOR_LuV = REGISTRATE.item("electric.motor.luv", Item::new).lang("LuV Electric Motor").register();
    public static ItemEntry<Item> ELECTRIC_MOTOR_ZPM = REGISTRATE.item("electric.motor.zpm", Item::new).lang("ZPM Electric Motor").register();
    public static ItemEntry<Item> ELECTRIC_MOTOR_UV = REGISTRATE.item("electric.motor.uv", Item::new).lang("UV Electric Motor").register();

    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_LV = REGISTRATE.item("electric.pump.lv", ComponentItem::create).lang("LV Electric Pump").onRegister(attach(new CoverPlaceBehavior(GTCovers.PUMPS[0]), new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.electric.pump.tooltip"));
        lines.add(Component.translatable("gregtech.universal.tooltip.fluid_transfer_rate", 1280 / 20));
    }))).register();

    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_MV = REGISTRATE.item("electric.pump.mv", ComponentItem::create).lang("MV Electric Pump").onRegister(attach(new CoverPlaceBehavior(GTCovers.PUMPS[1]), new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.electric.pump.tooltip"));
        lines.add(Component.translatable("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 4 / 20));
   }))).register();
    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_HV = REGISTRATE.item("electric.pump.hv", ComponentItem::create).lang("V Electric Pump").onRegister(attach(new CoverPlaceBehavior(GTCovers.PUMPS[2]), new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.electric.pump.tooltip"));
        lines.add(Component.translatable("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 16 / 20));
   }))).register();
    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_EV = REGISTRATE.item("electric.pump.ev", ComponentItem::create).lang("EV Electric Pump").onRegister(attach(new CoverPlaceBehavior(GTCovers.PUMPS[3]), new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.electric.pump.tooltip"));
        lines.add(Component.translatable("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 64 / 20));
   }))).register();
    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_IV = REGISTRATE.item("electric.pump.iv", ComponentItem::create).lang("IV Electric Pump").onRegister(attach(new CoverPlaceBehavior(GTCovers.PUMPS[4]), new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.electric.pump.tooltip"));
        lines.add(Component.translatable("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 4 / 20));
   }))).register();
    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_LuV = REGISTRATE.item("electric.pump.luv", ComponentItem::create).lang("LuV Electric Pump").onRegister(attach(new CoverPlaceBehavior(GTCovers.PUMPS[5]), new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.electric.pump.tooltip"));
        lines.add(Component.translatable("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 16 / 20));
   }))).register();
    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_ZPM = REGISTRATE.item("electric.pump.zpm", ComponentItem::create).lang("ZPM Electric Pump").onRegister(attach(new CoverPlaceBehavior(GTCovers.PUMPS[5]), new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.electric.pump.tooltip"));
        lines.add(Component.translatable("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 64 / 20));
   }))).register();
    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_UV = REGISTRATE.item("electric.pump.uv", ComponentItem::create).lang("UV Electric Pump").onRegister(attach(new CoverPlaceBehavior(GTCovers.PUMPS[5]), new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.electric.pump.tooltip"));
        lines.add(Component.translatable("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 64 * 4 / 20));
   }))).register();

    public static ItemEntry<ComponentItem> FLUID_REGULATOR_LV = REGISTRATE.item("fluid.regulator.lv", ComponentItem::create).lang("LV Fluid Regulator").onRegister(attach(new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.fluid.regulator.tooltip"));
        lines.add(Component.translatable("gregtech.universal.tooltip.fluid_transfer_rate", 1280 / 20));
    }))).register();
    public static ItemEntry<ComponentItem> FLUID_REGULATOR_MV = REGISTRATE.item("fluid.regulator.mv", ComponentItem::create).lang("MV Fluid Regulator").onRegister(attach(new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.fluid.regulator.tooltip"));
        lines.add(Component.translatable("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 4 / 20));
    }))).register();
    public static ItemEntry<ComponentItem> FLUID_REGULATOR_HV = REGISTRATE.item("fluid.regulator.hv", ComponentItem::create).lang("HV Fluid Regulator").onRegister(attach(new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.fluid.regulator.tooltip"));
        lines.add(Component.translatable("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 16 / 20));
    }))).register();
    public static ItemEntry<ComponentItem> FLUID_REGULATOR_EV = REGISTRATE.item("fluid.regulator.ev", ComponentItem::create).lang("EV Fluid Regulator").onRegister(attach(new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.fluid.regulator.tooltip"));
        lines.add(Component.translatable("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 64 / 20));
    }))).register();
    public static ItemEntry<ComponentItem> FLUID_REGULATOR_IV = REGISTRATE.item("fluid.regulator.iv", ComponentItem::create).lang("IV Fluid Regulator").onRegister(attach(new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.fluid.regulator.tooltip"));
        lines.add(Component.translatable("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 4 / 20));
    }))).register();
    public static ItemEntry<ComponentItem> FLUID_REGULATOR_LUV = REGISTRATE.item("fluid.regulator.luv", ComponentItem::create).lang("LuV Fluid Regulator").onRegister(attach(new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.fluid.regulator.tooltip"));
        lines.add(Component.translatable("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 16 / 20));
    }))).register();
    public static ItemEntry<ComponentItem> FLUID_REGULATOR_ZPM = REGISTRATE.item("fluid.regulator.zpm", ComponentItem::create).lang("ZPM Fluid Regulator").onRegister(attach(new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.fluid.regulator.tooltip"));
        lines.add(Component.translatable("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 64 / 20));
    }))).register();
    public static ItemEntry<ComponentItem> FLUID_REGULATOR_UV = REGISTRATE.item("fluid.regulator.uv", ComponentItem::create).lang("UV Fluid Regulator").onRegister(attach(new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.fluid.regulator.tooltip"));
        lines.add(Component.translatable("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 64 * 4/ 20));
    }))).register();

    public static ItemEntry<ComponentItem> DYNAMITE; // TODO

    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_LV = REGISTRATE.item("conveyor.module.lv", ComponentItem::create).lang("LV Conveyor Module").onRegister(attach(new CoverPlaceBehavior(GTCovers.CONVEYORS[0]), new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.conveyor.module.tooltip"));
        lines.add(Component.translatable("gregtech.universal.tooltip.item_transfer_rate", 8));
    }))).register();
    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_MV = REGISTRATE.item("conveyor.module.mv", ComponentItem::create).lang("MV Conveyor Module").onRegister(attach(new CoverPlaceBehavior(GTCovers.CONVEYORS[1]), new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.conveyor.module.tooltip"));
        lines.add(Component.translatable("gregtech.universal.tooltip.item_transfer_rate", 32));
    }))).register();
    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_HV = REGISTRATE.item("conveyor.module.hv", ComponentItem::create).lang("HV Conveyor Module").onRegister(attach(new CoverPlaceBehavior(GTCovers.CONVEYORS[2]), new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.conveyor.module.tooltip"));
        lines.add(Component.translatable("gregtech.universal.tooltip.item_transfer_rate", 128));
    }))).register();
    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_EV = REGISTRATE.item("conveyor.module.ev", ComponentItem::create).lang("EV Conveyor Module").onRegister(attach(new CoverPlaceBehavior(GTCovers.CONVEYORS[3]), new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.conveyor.module.tooltip"));
        lines.add(Component.translatable("gregtech.universal.tooltip.item_transfer_rate_stacks", 8));
    }))).register();
    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_IV = REGISTRATE.item("conveyor.module.iv", ComponentItem::create).lang("IV Conveyor Module").onRegister(attach(new CoverPlaceBehavior(GTCovers.CONVEYORS[4]), new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.conveyor.module.tooltip"));
        lines.add(Component.translatable("gregtech.universal.tooltip.item_transfer_rate_stacks", 32));
    }))).register();
    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_LuV = REGISTRATE.item("conveyor.module.luv", ComponentItem::create).lang("LuV Conveyor Module").onRegister(attach(new CoverPlaceBehavior(GTCovers.CONVEYORS[5]), new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.conveyor.module.tooltip"));
        lines.add(Component.translatable("gregtech.universal.tooltip.item_transfer_rate_stacks", 128));
    }))).register();
    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_ZPM = REGISTRATE.item("conveyor.module.zpm", ComponentItem::create).lang("ZPM Conveyor Module").onRegister(attach(new CoverPlaceBehavior(GTCovers.CONVEYORS[5]), new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.conveyor.module.tooltip"));
        lines.add(Component.translatable("gregtech.universal.tooltip.item_transfer_rate_stacks", 128));
    }))).register();
    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_UV = REGISTRATE.item("conveyor.module.uv", ComponentItem::create).lang("UV Conveyor Module").onRegister(attach(new CoverPlaceBehavior(GTCovers.CONVEYORS[5]), new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.conveyor.module.tooltip"));
        lines.add(Component.translatable("gregtech.universal.tooltip.item_transfer_rate_stacks", 128));
    }))).register();

    public static ItemEntry<Item> ELECTRIC_PISTON_LV= REGISTRATE.item("electric.piston.lv", Item::new).lang("LV Electric Piston").register();
    public static ItemEntry<Item> ELECTRIC_PISTON_MV= REGISTRATE.item("electric.piston.mv", Item::new).lang("MV Electric Piston").register();
    public static ItemEntry<Item> ELECTRIC_PISTON_HV= REGISTRATE.item("electric.piston.hv", Item::new).lang("HV Electric Piston").register();
    public static ItemEntry<Item> ELECTRIC_PISTON_EV= REGISTRATE.item("electric.piston.ev", Item::new).lang("EV Electric Piston").register();
    public static ItemEntry<Item> ELECTRIC_PISTON_IV= REGISTRATE.item("electric.piston.iv", Item::new).lang("IV Electric Piston").register();
    public static ItemEntry<Item> ELECTRIC_PISTON_LUV= REGISTRATE.item("electric.piston.luv", Item::new).lang("LuV Electric Piston").register();
    public static ItemEntry<Item> ELECTRIC_PISTON_ZPM= REGISTRATE.item("electric.piston.zpm", Item::new).lang("ZPM Electric Piston").register();
    public static ItemEntry<Item> ELECTRIC_PISTON_UV= REGISTRATE.item("electric.piston.uv", Item::new).lang("UV Electric Piston").register();

    public static ItemEntry<ComponentItem> ROBOT_ARM_LV = REGISTRATE.item("robot.arm.lv", ComponentItem::create).lang("LV Robot Arm").onRegister(attach(new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.robot.arm.tooltip"));
        lines.add(Component.translatable("gregtech.universal.tooltip.item_transfer_rate", 8));
    }))).register();
    public static ItemEntry<ComponentItem> ROBOT_ARM_MV = REGISTRATE.item("robot.arm.mv", ComponentItem::create).lang("MV Robot Arm").onRegister(attach(new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.robot.arm.tooltip"));
        lines.add(Component.translatable("gregtech.universal.tooltip.item_transfer_rate", 32));
    }))).register();
    public static ItemEntry<ComponentItem> ROBOT_ARM_HV = REGISTRATE.item("robot.arm.hv", ComponentItem::create).lang("HV Robot Arm").onRegister(attach(new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.robot.arm.tooltip"));
        lines.add(Component.translatable("gregtech.universal.tooltip.item_transfer_rate", 64));
    }))).register();
    public static ItemEntry<ComponentItem> ROBOT_ARM_EV = REGISTRATE.item("robot.arm.ev", ComponentItem::create).lang("EV Robot Arm").onRegister(attach(new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.robot.arm.tooltip"));
        lines.add(Component.translatable("gregtech.universal.tooltip.item_transfer_rate_stacks", 3));
    }))).register();
    public static ItemEntry<ComponentItem> ROBOT_ARM_IV = REGISTRATE.item("robot.arm.iv", ComponentItem::create).lang("IV Robot Arm").onRegister(attach(new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.robot.arm.tooltip"));
        lines.add(Component.translatable("gregtech.universal.tooltip.item_transfer_rate_stacks", 8));
    }))).register();
    public static ItemEntry<ComponentItem> ROBOT_ARM_LuV = REGISTRATE.item("robot.arm.luv", ComponentItem::create).lang("LuV Robot Arm").onRegister(attach(new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.robot.arm.tooltip"));
        lines.add(Component.translatable("gregtech.universal.tooltip.item_transfer_rate_stacks", 16));
    }))).register();
    public static ItemEntry<ComponentItem> ROBOT_ARM_ZPM = REGISTRATE.item("robot.arm.zpm", ComponentItem::create).lang("ZPM Robot Arm").onRegister(attach(new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.robot.arm.tooltip"));
        lines.add(Component.translatable("gregtech.universal.tooltip.item_transfer_rate_stacks", 16));
    }))).register();
    public static ItemEntry<ComponentItem> ROBOT_ARM_UV = REGISTRATE.item("robot.arm.uv", ComponentItem::create).lang("UV Robot Arm").onRegister(attach(new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.robot.arm.tooltip"));
        lines.add(Component.translatable("gregtech.universal.tooltip.item_transfer_rate_stacks", 16));
    }))).register();

    public static ItemEntry<Item> FIELD_GENERATOR_LV= REGISTRATE.item("field.generator.lv", Item::new).lang("LV Field Generator").register();
    public static ItemEntry<Item> FIELD_GENERATOR_MV= REGISTRATE.item("field.generator.mv", Item::new).lang("MV Field Generator").register();
    public static ItemEntry<Item> FIELD_GENERATOR_HV= REGISTRATE.item("field.generator.hv", Item::new).lang("HV Field Generator").register();
    public static ItemEntry<Item> FIELD_GENERATOR_EV= REGISTRATE.item("field.generator.ev", Item::new).lang("EV Field Generator").register();
    public static ItemEntry<Item> FIELD_GENERATOR_IV= REGISTRATE.item("field.generator.iv", Item::new).lang("IV Field Generator").register();
    public static ItemEntry<Item> FIELD_GENERATOR_LuV= REGISTRATE.item("field.generator.luv", Item::new).lang("LuV Field Generator").register();
    public static ItemEntry<Item> FIELD_GENERATOR_ZPM= REGISTRATE.item("field.generator.zpm", Item::new).lang("ZPM Field Generator").register();
    public static ItemEntry<Item> FIELD_GENERATOR_UV= REGISTRATE.item("field.generator.uv", Item::new).lang("UV Field Generator").register();

    public static ItemEntry<Item> EMITTER_LV= REGISTRATE.item("emitter.lv", Item::new).lang("LV Emitter").register();
    public static ItemEntry<Item> EMITTER_MV= REGISTRATE.item("emitter.mv", Item::new).lang("MV Emitter").register();
    public static ItemEntry<Item> EMITTER_HV= REGISTRATE.item("emitter.hv", Item::new).lang("HV Emitter").register();
    public static ItemEntry<Item> EMITTER_EV= REGISTRATE.item("emitter.ev", Item::new).lang("EV Emitter").register();
    public static ItemEntry<Item> EMITTER_IV= REGISTRATE.item("emitter.iv", Item::new).lang("IV Emitter").register();
    public static ItemEntry<Item> EMITTER_LuV= REGISTRATE.item("emitter.luv", Item::new).lang("LuV Emitter").register();
    public static ItemEntry<Item> EMITTER_ZPM= REGISTRATE.item("emitter.zpm", Item::new).lang("ZPM Emitter").register();
    public static ItemEntry<Item> EMITTER_UV= REGISTRATE.item("emitter.uv", Item::new).lang("UV Emitter").register();

    public static ItemEntry<Item> SENSOR_LV= REGISTRATE.item("sensor.lv", Item::new).lang("LV Sensor").register();
    public static ItemEntry<Item> SENSOR_MV= REGISTRATE.item("sensor.mv", Item::new).lang("MV Sensor").register();
    public static ItemEntry<Item> SENSOR_HV= REGISTRATE.item("sensor.hv", Item::new).lang("HV Sensor").register();
    public static ItemEntry<Item> SENSOR_EV= REGISTRATE.item("sensor.ev", Item::new).lang("MV Sensor").register();
    public static ItemEntry<Item> SENSOR_IV= REGISTRATE.item("sensor.iv", Item::new).lang("IV Sensor").register();
    public static ItemEntry<Item> SENSOR_LuV= REGISTRATE.item("sensor.luv", Item::new).lang("LuV Sensor").register();
    public static ItemEntry<Item> SENSOR_ZPM= REGISTRATE.item("sensor.zpm", Item::new).lang("ZPM Sensor").register();
    public static ItemEntry<Item> SENSOR_UV= REGISTRATE.item("sensor.uv", Item::new).lang("UV Sensor").register();

    public static ItemEntry<Item> TOOL_DATA_STICK= REGISTRATE.item("tool.datastick", Item::new).lang("Data Stick").register();
    public static ItemEntry<Item> TOOL_DATA_ORB= REGISTRATE.item("tool.dataorb", Item::new).lang("Data Orb").register();

    public static final Map<MarkerMaterial, ItemEntry<Item>> GLASS_LENSES = new HashMap<>();

    static {
        for (int i = 0; i < MarkerMaterials.Color.VALUES.length; i++) {
            MarkerMaterial color = MarkerMaterials.Color.VALUES[i];
            if (color != MarkerMaterials.Color.White) {
                GLASS_LENSES.put(color, REGISTRATE.item(String.format("glass_lens.%s", color.toString()), Item::new)
                        .lang("Glass Lens (%s)".formatted(toEnglishName(color.getName())))
                        .transform(unificationItem(TagPrefix.lens, color))
                        .register());
            }
        }
    }

    public static ItemEntry<Item> SILICON_BOULE= REGISTRATE.item("boule.silicon", Item::new).lang("Monocrystalline Silicon Boule").register();
    public static ItemEntry<Item> GLOWSTONE_BOULE= REGISTRATE.item("boule.glowstone", Item::new).lang("Glowstone-doped Monocrystalline Silicon Boule").register();
    public static ItemEntry<Item> NAQUADAH_BOULE= REGISTRATE.item("boule.naquadah", Item::new).lang("Naquadah-doped Monocrystalline Silicon Boule").register();
    public static ItemEntry<Item> NEUTRONIUM_BOULE= REGISTRATE.item("boule.neutronium", Item::new).lang("Neutronium-doped Monocrystalline Silicon Boule").register();
    public static ItemEntry<Item> SILICON_WAFER= REGISTRATE.item("wafer.silicon", Item::new).lang("Silicon Wafer").register();
    public static ItemEntry<Item> GLOWSTONE_WAFER= REGISTRATE.item("wafer.glowstone", Item::new).lang("Glowstone-doped Wafer").register();
    public static ItemEntry<Item> NAQUADAH_WAFER= REGISTRATE.item("wafer.naquadah", Item::new).lang("Naquadah-doped Wafer").register();
    public static ItemEntry<Item> NEUTRONIUM_WAFER= REGISTRATE.item("wafer.neutronium", Item::new).lang("Neutronium-doped Wafer").register();

    public static ItemEntry<Item> CENTRAL_PROCESSING_UNIT_WAFER= REGISTRATE.item("wafer.central_processing_unit", Item::new).lang("CPU Wafer").register();
    public static ItemEntry<Item> RANDOM_ACCESS_MEMORY_WAFER= REGISTRATE.item("wafer.random_access_memory", Item::new).lang("RAM Wafer").register();
    public static ItemEntry<Item> INTEGRATED_LOGIC_CIRCUIT_WAFER= REGISTRATE.item("wafer.integrated_logic_circuit", Item::new).lang("Integrated Logic Circuit Wafer").register();
    public static ItemEntry<Item> NANO_CENTRAL_PROCESSING_UNIT_WAFER= REGISTRATE.item("wafer.nano_central_processing_unit", Item::new).lang("Nano CPU Wafer").register();
    public static ItemEntry<Item> QUBIT_CENTRAL_PROCESSING_UNIT_WAFER= REGISTRATE.item("wafer.qbit_central_processing_unit", Item::new).lang("Qubit CPU").register();
    public static ItemEntry<Item> SIMPLE_SYSTEM_ON_CHIP_WAFER= REGISTRATE.item("wafer.simple_system_on_chip", Item::new).lang("Simple SoC Wafer").register();
    public static ItemEntry<Item> SYSTEM_ON_CHIP_WAFER= REGISTRATE.item("wafer.system_on_chip", Item::new).lang("SoC Wafer").register();
    public static ItemEntry<Item> ADVANCED_SYSTEM_ON_CHIP_WAFER= REGISTRATE.item("wafer.advanced_system_on_chip", Item::new).lang("ASoC Wafer").register();
    public static ItemEntry<Item> HIGHLY_ADVANCED_SOC_WAFER= REGISTRATE.item("wafer.highly_advanced_system_on_chip", Item::new).lang("HASoC Wafer").register();
    public static ItemEntry<Item> NAND_MEMORY_CHIP_WAFER= REGISTRATE.item("wafer.nand_memory_chip", Item::new).lang("NAND Wafer").register();
    public static ItemEntry<Item> NOR_MEMORY_CHIP_WAFER= REGISTRATE.item("wafer.nor_memory_chip", Item::new).lang("NOR Wafer").register();
    public static ItemEntry<Item> ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER= REGISTRATE.item("wafer.ultra_low_power_integrated_circuit", Item::new).lang("ULPIC Wafer").register();
    public static ItemEntry<Item> LOW_POWER_INTEGRATED_CIRCUIT_WAFER= REGISTRATE.item("wafer.low_power_integrated_circuit", Item::new).lang("LPIC Wafer").register();
    public static ItemEntry<Item> POWER_INTEGRATED_CIRCUIT_WAFER= REGISTRATE.item("wafer.power_integrated_circuit", Item::new).lang("PIC Wafer").register();
    public static ItemEntry<Item> HIGH_POWER_INTEGRATED_CIRCUIT_WAFER= REGISTRATE.item("wafer.high_power_integrated_circuit", Item::new).lang("HPIC Wafer").register();
    public static ItemEntry<Item> ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT_WAFER= REGISTRATE.item("wafer.ultra_high_power_integrated_circuit", Item::new).lang("UHPIC Wafer").register();

    public static ItemEntry<Item> ENGRAVED_CRYSTAL_CHIP = REGISTRATE.item("engraved.crystal_chip", Item::new).register();
    public static ItemEntry<Item> ENGRAVED_LAPOTRON_CHIP = REGISTRATE.item("engraved.lapotron_chip", Item::new).register();

    public static ItemEntry<Item> CENTRAL_PROCESSING_UNIT = REGISTRATE.item( "plate.central_processing_unit", Item::new).lang("CPU").register();
    public static ItemEntry<Item> RANDOM_ACCESS_MEMORY = REGISTRATE.item( "plate.random_access_memory", Item::new).lang("RAM").register();
    public static ItemEntry<Item> INTEGRATED_LOGIC_CIRCUIT = REGISTRATE.item( "plate.integrated_logic_circuit", Item::new).lang("Integrated Circuit").register();
    public static ItemEntry<Item> NANO_CENTRAL_PROCESSING_UNIT = REGISTRATE.item( "plate.nano_central_processing_unit", Item::new).lang("Nano CPU").register();
    public static ItemEntry<Item> QUBIT_CENTRAL_PROCESSING_UNIT = REGISTRATE.item( "plate.qbit_central_processing_unit", Item::new).lang("Qubit CPU").register();
    public static ItemEntry<Item> SIMPLE_SYSTEM_ON_CHIP = REGISTRATE.item( "plate.simple_system_on_chip", Item::new).lang("Simple SoC").register();
    public static ItemEntry<Item> SYSTEM_ON_CHIP = REGISTRATE.item( "plate.system_on_chip", Item::new).lang("SoC").register();
    public static ItemEntry<Item> ADVANCED_SYSTEM_ON_CHIP = REGISTRATE.item( "plate.advanced_system_on_chip", Item::new).lang("ASoC").register();
    public static ItemEntry<Item> HIGHLY_ADVANCED_SOC = REGISTRATE.item( "plate.highly_advanced_system_on_chip", Item::new).lang("HASoC").register();
    public static ItemEntry<Item> NAND_MEMORY_CHIP = REGISTRATE.item( "plate.nand_memory_chip", Item::new).lang("NAND").register();
    public static ItemEntry<Item> NOR_MEMORY_CHIP = REGISTRATE.item( "plate.nor_memory_chip", Item::new).lang("NOR").register();
    public static ItemEntry<Item> ULTRA_LOW_POWER_INTEGRATED_CIRCUIT = REGISTRATE.item( "plate.ultra_low_power_integrated_circuit", Item::new).lang("ULPIC").register();
    public static ItemEntry<Item> LOW_POWER_INTEGRATED_CIRCUIT = REGISTRATE.item( "plate.low_power_integrated_circuit", Item::new).lang("LPIC").register();
    public static ItemEntry<Item> POWER_INTEGRATED_CIRCUIT = REGISTRATE.item( "plate.power_integrated_circuit", Item::new).lang("PIC").register();
    public static ItemEntry<Item> HIGH_POWER_INTEGRATED_CIRCUIT = REGISTRATE.item( "plate.high_power_integrated_circuit", Item::new).lang("HPIC").register();
    public static ItemEntry<Item> ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT = REGISTRATE.item( "plate.ultra_high_power_integrated_circuit", Item::new).lang("UHPIC").register();
    

    public static ItemEntry<Item> RAW_CRYSTAL_CHIP = REGISTRATE.item("crystal.raw", Item::new).lang("Raw Crystal Chip").register();
    public static ItemEntry<Item> RAW_CRYSTAL_CHIP_PART = REGISTRATE.item("crystal.raw_chip", Item::new).lang("Raw Crystal Chip Parts").register();
    public static ItemEntry<Item> CRYSTAL_CENTRAL_PROCESSING_UNIT = REGISTRATE.item("crystal.central_processing_unit", Item::new).lang("Crystal CPU").register();
    public static ItemEntry<Item> CRYSTAL_SYSTEM_ON_CHIP = REGISTRATE.item("crystal.system_on_chip", Item::new).lang("Crystal SoC").register();

    public static ItemEntry<Item> COATED_BOARD = REGISTRATE.item("board.coated", Item::new).lang("Coated Circuit Board").register();
    public static ItemEntry<Item> PHENOLIC_BOARD = REGISTRATE.item("board.phenolic", Item::new).lang("Phenolic Circuit Board").register();
    public static ItemEntry<Item> PLASTIC_BOARD = REGISTRATE.item("board.plastic", Item::new).lang("Plastic Circuit Board").register();
    public static ItemEntry<Item> EPOXY_BOARD = REGISTRATE.item("board.epoxy", Item::new).lang("Epoxy Circuit Board").register();
    public static ItemEntry<Item> FIBER_BOARD = REGISTRATE.item("board.fiber_reinforced", Item::new).lang("Fiber-Reinforced Circuit Board").register();
    public static ItemEntry<Item> MULTILAYER_FIBER_BOARD = REGISTRATE.item("board.multilayer.fiber_reinforced", Item::new).lang("Multi-layer Fiber-Reinforced Circuit Board").register();
    public static ItemEntry<Item> WETWARE_BOARD = REGISTRATE.item("board.wetware", Item::new).lang("Wetware Lifesupport Circuit Board").register();

    public static ItemEntry<Item> BASIC_CIRCUIT_BOARD = REGISTRATE.item("circuit_board.basic", Item::new).lang("Circuit Board").register();
    public static ItemEntry<Item> GOOD_CIRCUIT_BOARD = REGISTRATE.item("circuit_board.good", Item::new).lang("Good Circuit Board").register();
    public static ItemEntry<Item> PLASTIC_CIRCUIT_BOARD = REGISTRATE.item("circuit_board.plastic", Item::new).lang("Plastic Circuit Board").register();
    public static ItemEntry<Item> ADVANCED_CIRCUIT_BOARD = REGISTRATE.item("circuit_board.advanced", Item::new).lang("Advanced Circuit Board").register();
    public static ItemEntry<Item> EXTREME_CIRCUIT_BOARD = REGISTRATE.item("circuit_board.extreme", Item::new).lang("Extreme Circuit Board").register();
    public static ItemEntry<Item> ELITE_CIRCUIT_BOARD = REGISTRATE.item("circuit_board.elite", Item::new).lang("Elite Circuit Board").register();
    public static ItemEntry<Item> WETWARE_CIRCUIT_BOARD = REGISTRATE.item("circuit_board.wetware", Item::new).lang("Master Circuit Board").register();

    public static ItemEntry<Item> VACUUM_TUBE = REGISTRATE.item("circuit.vacuum_tube", Item::new).lang("Vacuum Tube").tag(CustomTags.ULV_CIRCUITS).register();
    public static ItemEntry<Item> GLASS_TUBE = REGISTRATE.item("component.glass.tube", Item::new).lang("Glass Tube").register();
    public static ItemEntry<Item> TRANSISTOR = REGISTRATE.item("component.transistor", Item::new).lang("Transistor").tag(CustomTags.TRANSISTORS).register();
    public static ItemEntry<Item> RESISTOR = REGISTRATE.item("component.resistor", Item::new).lang("Resistor").tag(CustomTags.RESISTORS).register();
    public static ItemEntry<Item> CAPACITOR = REGISTRATE.item("component.capacitor", Item::new).lang("Capacitor").tag(CustomTags.CAPACITORS).register();
    public static ItemEntry<Item> DIODE = REGISTRATE.item("component.diode", Item::new).lang("Diode").tag(CustomTags.DIODES).register();
    public static ItemEntry<Item> INDUCTOR = REGISTRATE.item("component.inductor", Item::new).lang("Inductor").tag(CustomTags.INDUCTORS).register();
    public static ItemEntry<Item> SMD_TRANSISTOR = REGISTRATE.item("component.smd.transistor", Item::new).lang("SMD Transistor").tag(CustomTags.TRANSISTORS).register();
    public static ItemEntry<Item> SMD_RESISTOR = REGISTRATE.item("component.smd.resistor", Item::new).lang("SMD Resistor").tag(CustomTags.RESISTORS).register();
    public static ItemEntry<Item> SMD_CAPACITOR = REGISTRATE.item("component.smd.capacitor", Item::new).lang("SMD Capacitor").tag(CustomTags.CAPACITORS).register();
    public static ItemEntry<Item> SMD_DIODE = REGISTRATE.item("component.smd.diode", Item::new).lang("SMD Diode").tag(CustomTags.DIODES).register();
    public static ItemEntry<Item> SMD_INDUCTOR = REGISTRATE.item("component.smd.inductor", Item::new).lang("SMD Inductor").tag(CustomTags.INDUCTORS).register();
    public static ItemEntry<Item> ADVANCED_SMD_TRANSISTOR = REGISTRATE.item("component.advanced_smd.transistor", Item::new).lang("Advanced SMD Transistor").register();
    public static ItemEntry<Item> ADVANCED_SMD_RESISTOR = REGISTRATE.item("component.advanced_smd.resistor", Item::new).lang("Advanced SMD Resistor").register();
    public static ItemEntry<Item> ADVANCED_SMD_CAPACITOR = REGISTRATE.item("component.advanced_smd.capacitor", Item::new).lang("Advanced SMD Capacitor").register();
    public static ItemEntry<Item> ADVANCED_SMD_DIODE = REGISTRATE.item("component.advanced_smd.diode", Item::new).lang("Advanced SMD Diode").register();
    public static ItemEntry<Item> ADVANCED_SMD_INDUCTOR = REGISTRATE.item("component.advanced_smd.inductor", Item::new).lang("Advanced SMD Inductor").register();

    // T1: Electronic
    public static ItemEntry<Item> ELECTRONIC_CIRCUIT_LV = REGISTRATE.item("circuit.electronic", Item::new).lang("Electronic Circuit").tag(CustomTags.LV_CIRCUITS).register();
    public static ItemEntry<Item> ELECTRONIC_CIRCUIT_MV = REGISTRATE.item("circuit.good_electronic", Item::new).lang("Good Electronic Circuit").tag(CustomTags.MV_CIRCUITS).register();

    // T2: Integrated
    public static ItemEntry<Item> INTEGRATED_CIRCUIT_LV = REGISTRATE.item("circuit.basic_integrated", Item::new).lang("Integrated Logic Circuit").tag(CustomTags.LV_CIRCUITS).register();
    public static ItemEntry<Item> INTEGRATED_CIRCUIT_MV = REGISTRATE.item("circuit.good_integrated", Item::new).lang("Good Integrated Circuit").tag(CustomTags.MV_CIRCUITS).register();
    public static ItemEntry<Item> INTEGRATED_CIRCUIT_HV = REGISTRATE.item("circuit.advanced_integrated", Item::new).lang("Advanced Integrated Circuit").tag(CustomTags.HV_CIRCUITS).register();

    // ULV/LV easier circuits
    public static ItemEntry<Item> NAND_CHIP_ULV = REGISTRATE.item("circuit.nand_chip", Item::new).lang("NAND Chip").tag(CustomTags.ULV_CIRCUITS).register();
    public static ItemEntry<Item> MICROPROCESSOR_LV = REGISTRATE.item("circuit.microprocessor", Item::new).lang("Microprocessor").tag(CustomTags.LV_CIRCUITS).register();

    // T3: Processor
    public static ItemEntry<Item> PROCESSOR_MV = REGISTRATE.item("circuit.processor", Item::new).lang("Integrated Processor").tag(CustomTags.MV_CIRCUITS).register();
    public static ItemEntry<Item> PROCESSOR_ASSEMBLY_HV = REGISTRATE.item("circuit.assembly", Item::new).lang("Processor Assembly").tag(CustomTags.HV_CIRCUITS).register();
    public static ItemEntry<Item> WORKSTATION_EV = REGISTRATE.item("circuit.workstation", Item::new).lang("Workstation").tag(CustomTags.EV_CIRCUITS).register();
    public static ItemEntry<Item> MAINFRAME_IV = REGISTRATE.item("circuit.mainframe", Item::new).lang("Mainframe").tag(CustomTags.IV_CIRCUITS).register();

    // T4: Nano
    public static ItemEntry<Item> NANO_PROCESSOR_HV = REGISTRATE.item("circuit.nano_processor", Item::new).lang("Nanoprocessor").tag(CustomTags.HV_CIRCUITS).register();
    public static ItemEntry<Item> NANO_PROCESSOR_ASSEMBLY_EV = REGISTRATE.item("circuit.nano_assembly", Item::new).lang("Nanoprocessor Assembly").tag(CustomTags.EV_CIRCUITS).register();
    public static ItemEntry<Item> NANO_COMPUTER_IV = REGISTRATE.item("circuit.nano_computer", Item::new).lang("Nano Supercomputer").tag(CustomTags.IV_CIRCUITS).register();
    public static ItemEntry<Item> NANO_MAINFRAME_LUV = REGISTRATE.item("circuit.nano_mainframe", Item::new).lang("Nanoprocessor Mainframe").tag(CustomTags.LuV_CIRCUITS).register();

    // T5: Quantum
    public static ItemEntry<Item> QUANTUM_PROCESSOR_EV = REGISTRATE.item("circuit.quantum_processor", Item::new).lang("Quantumprocessor").tag(CustomTags.EV_CIRCUITS).register();
    public static ItemEntry<Item> QUANTUM_ASSEMBLY_IV = REGISTRATE.item("circuit.quantum_assembly", Item::new).lang("Quantumprocessor Assembly").tag(CustomTags.IV_CIRCUITS).register();
    public static ItemEntry<Item> QUANTUM_COMPUTER_LUV = REGISTRATE.item("circuit.quantum_computer", Item::new).lang("Quantum Supercomputer").tag(CustomTags.LuV_CIRCUITS).register();
    public static ItemEntry<Item> QUANTUM_MAINFRAME_ZPM = REGISTRATE.item("circuit.quantum_mainframe", Item::new).lang("Quantumprocessor Mainframe").tag(CustomTags.ZPM_CIRCUITS).register();

    // T6: Crystal
    public static ItemEntry<Item> CRYSTAL_PROCESSOR_IV = REGISTRATE.item("circuit.crystal_processor", Item::new).lang("Crystal Processor").tag(CustomTags.IV_CIRCUITS).register();
    public static ItemEntry<Item> CRYSTAL_ASSEMBLY_LUV = REGISTRATE.item("circuit.crystal_assembly", Item::new).lang("Crystal Processor Assembly").tag(CustomTags.LuV_CIRCUITS).register();
    public static ItemEntry<Item> CRYSTAL_COMPUTER_ZPM = REGISTRATE.item("circuit.crystal_computer", Item::new).lang("Crystal Supercomputer").tag(CustomTags.ZPM_CIRCUITS).register();
    public static ItemEntry<Item> CRYSTAL_MAINFRAME_UV = REGISTRATE.item("circuit.crystal_mainframe", Item::new).lang("Crystal Processor Mainframe").tag(CustomTags.UV_CIRCUITS).register();

    // T7: Wetware
    public static ItemEntry<Item> WETWARE_PROCESSOR_LUV = REGISTRATE.item("circuit.wetware_processor", Item::new).lang("Wetware Processor").tag(CustomTags.LuV_CIRCUITS).register();
    public static ItemEntry<Item> WETWARE_PROCESSOR_ASSEMBLY_ZPM = REGISTRATE.item("circuit.wetware_assembly", Item::new).lang("Wetware Assembly").tag(CustomTags.ZPM_CIRCUITS).register();
    public static ItemEntry<Item> WETWARE_SUPER_COMPUTER_UV = REGISTRATE.item("circuit.wetware_computer", Item::new).lang("Wetware Supercomputer").tag(CustomTags.UV_CIRCUITS).register();
    public static ItemEntry<Item> WETWARE_MAINFRAME_UHV = REGISTRATE.item("circuit.wetware_mainframe", Item::new).tag(CustomTags.UHV_CIRCUITS).register();

    public static ItemEntry<Item> COMPONENT_GRINDER_DIAMOND = REGISTRATE.item("component.grinder.diamond", Item::new).lang("Diamond Grinding Head").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 8), new MaterialStack(GTMaterials.Diamond, GTValues.M * 5)))).register();
    public static ItemEntry<Item> COMPONENT_GRINDER_TUNGSTEN = REGISTRATE.item("component.grinder.tungsten", Item::new).lang("Tungsten Grinding Head").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Tungsten, GTValues.M * 4), new MaterialStack(GTMaterials.VanadiumSteel, GTValues.M * 8), new MaterialStack(GTMaterials.Diamond, GTValues.M)))).register();

    public static ItemEntry<Item> QUANTUM_EYE = REGISTRATE.item("quantumeye", Item::new).lang("Quantum Eye").register();
    public static ItemEntry<Item> QUANTUM_STAR = REGISTRATE.item("quantumstar", Item::new).lang("Quantum Star").register();
    public static ItemEntry<Item> GRAVI_STAR = REGISTRATE.item("gravistar", Item::new).lang("Gravi Star").register();


    public static ItemEntry<ComponentItem> ITEM_FILTER = REGISTRATE.item("item_filter", ComponentItem::create)
            .onRegister(attach(new ItemFilterBehaviour(SimpleItemFilter::loadFilter), new CoverPlaceBehavior(GTCovers.ITEM_FILTER)))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Zinc, GTValues.M * 2), new MaterialStack(GTMaterials.Steel, GTValues.M)))).register();
    public static ItemEntry<ComponentItem> ORE_DICTIONARY_FILTER = REGISTRATE.item("ore_dictionary_filter", ComponentItem::create)
            .onRegister(attach(new ItemFilterBehaviour(TagItemFilter::loadFilter), new CoverPlaceBehavior(GTCovers.ITEM_FILTER)))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Zinc, GTValues.M * 2)))).register();
    public static ItemEntry<ComponentItem> FLUID_FILTER = REGISTRATE.item("fluid_filter", ComponentItem::create)
            .onRegister(attach(new FluidFilterBehaviour(SimpleFluidFilter::loadFilter), new CoverPlaceBehavior(GTCovers.FLUID_FILTER)))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Zinc, GTValues.M * 2)))).register();
    public static ItemEntry<ComponentItem> TAG_FLUID_FILTER = REGISTRATE.item("tag_fluid_filter", ComponentItem::create)
            .onRegister(attach(new FluidFilterBehaviour(TagFluidFilter::loadFilter), new CoverPlaceBehavior(GTCovers.FLUID_FILTER)))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Zinc, GTValues.M * 3 / 2)))).register();

    public static ItemEntry<Item> COVER_MACHINE_CONTROLLER = REGISTRATE.item("cover.controller", Item::new).lang("Machine Controller").register();
    public static ItemEntry<Item> COVER_ACTIVITY_DETECTOR = REGISTRATE.item("cover.activity.detector", Item::new).lang("Activity Detector").register();
    public static ItemEntry<Item> COVER_ACTIVITY_DETECTOR_ADVANCED = REGISTRATE.item("cover.activity.detector_advanced", Item::new).lang("Advanced Activity Detector").register();
    public static ItemEntry<Item> COVER_FLUID_DETECTOR = REGISTRATE.item("cover.fluid.detector", Item::new).lang("Fluid Detector").register();
    public static ItemEntry<Item> COVER_FLUID_DETECTOR_ADVANCED = REGISTRATE.item("cover.fluid.detector.advanced", Item::new).lang("Advanced Fluid Detector").register();
    public static ItemEntry<Item> COVER_ITEM_DETECTOR = REGISTRATE.item("cover.item.detector", Item::new).lang("Item Detector").register();
    public static ItemEntry<Item> COVER_ITEM_DETECTOR_ADVANCED = REGISTRATE.item("cover.item.detector.advanced", Item::new).lang("Advanced Item Detector").register();
    public static ItemEntry<Item> COVER_ENERGY_DETECTOR = REGISTRATE.item("cover.energy.detector", Item::new).lang("Energy Detector").register();
    public static ItemEntry<Item> COVER_ENERGY_DETECTOR_ADVANCED = REGISTRATE.item("cover.energy.detector.advanced", Item::new).lang("Advanced Energy Detector").register();
    public static ItemEntry<Item> COVER_SCREEN = REGISTRATE.item("cover.screen", Item::new).lang("Computer Monitor").register();
    public static ItemEntry<Item> COVER_CRAFTING = REGISTRATE.item("cover.crafting", Item::new).lang("Crafting Table Cover").register();
    public static ItemEntry<Item> COVER_SHUTTER = REGISTRATE.item("cover.shutter", Item::new).lang("Shutter Module").register();
    public static ItemEntry<ComponentItem> COVER_INFINITE_WATER = REGISTRATE.item("cover.infinite_water", ComponentItem::create).lang("Infinite Water Cover").onRegister(attach(new TooltipBehavior(lines -> {
        lines.add(Component.translatable("metaitem.cover.infinite_water.tooltip.1"));
        lines.add(Component.translatable("gtceu.universal.tooltip.produces_fluid", 16_000 / 20));
    }), new CoverPlaceBehavior(GTCovers.INFINITE_WATER))).register();
    public static ItemEntry<Item> COVER_ENDER_FLUID_LINK = REGISTRATE.item("cover.ender_fluid_link", Item::new).lang("Ender Fluid Link").register();
    public static ItemEntry<Item> COVER_DIGITAL_INTERFACE = REGISTRATE.item("cover.digital", Item::new).lang("Digital Interface").register();
    public static ItemEntry<Item> COVER_DIGITAL_INTERFACE_WIRELESS = REGISTRATE.item("cover.digital.wireless", Item::new).lang("Wireless Digital Interface").register();
    public static ItemEntry<Item> COVER_FLUID_VOIDING = REGISTRATE.item("cover.fluid.voiding", Item::new).lang("Fluid Voiding Cover").register();
    public static ItemEntry<Item> COVER_FLUID_VOIDING_ADVANCED = REGISTRATE.item("cover.fluid.voiding.advanced", Item::new).lang("Advanced Fluid Voiding Cover").register();
    public static ItemEntry<Item> COVER_ITEM_VOIDING = REGISTRATE.item("cover.item.voiding", Item::new).lang("Item Voiding Cover").register();
    public static ItemEntry<Item> COVER_ITEM_VOIDING_ADVANCED = REGISTRATE.item("cover.item.voiding.advanced", Item::new).lang("Advanced Item Voiding Cover").register();

    public static ItemEntry<ComponentItem> COVER_FACADE = REGISTRATE.item("cover.facade", ComponentItem::create)
            .lang("Cover Facade")
            .onRegister(attach(new FacadeItemBehaviour(), new CoverPlaceBehavior(GTCovers.FACADE)))
            .model(NonNullBiConsumer.noop())
            .register();

    // Solar Panels: ID 331-346
    public static ItemEntry<ComponentItem> COVER_SOLAR_PANEL = REGISTRATE.item("cover.solar.panel", ComponentItem::create).lang("Solar Panel").onRegister(attach(new TooltipBehavior(lines -> {
        lines.addAll(Arrays.asList(LangHandler.getMultiLang("metaitem.cover.solar.panel.tooltip")));
        lines.add(Component.translatable("gregtech.universal.tooltip.voltage_out", 1, GTValues.VNF[GTValues.ULV]));
    }))).register();
    public static ItemEntry<ComponentItem> COVER_SOLAR_PANEL_ULV = REGISTRATE.item("cover.solar.panel.ulv", ComponentItem::create).lang("Ultra Low Voltage Solar Panel").onRegister(attach(new TooltipBehavior(lines -> {
        lines.addAll(Arrays.asList(LangHandler.getMultiLang("metaitem.cover.solar.panel.tooltip")));
        lines.add(Component.translatable("gregtech.universal.tooltip.voltage_out", GTValues.V[GTValues.ULV], GTValues.VNF[GTValues.ULV]));
    }))).register();
    public static ItemEntry<ComponentItem> COVER_SOLAR_PANEL_LV = REGISTRATE.item("cover.solar.panel.lv", ComponentItem::create).lang("Low Voltage Solar Panel").onRegister(attach(new TooltipBehavior(lines -> {
        lines.addAll(Arrays.asList(LangHandler.getMultiLang("metaitem.cover.solar.panel.tooltip")));
        lines.add(Component.translatable("gregtech.universal.tooltip.voltage_out", GTValues.V[GTValues.LV], GTValues.VNF[GTValues.LV]));
    }))).register();
    public static ItemEntry<ComponentItem> COVER_SOLAR_PANEL_MV = REGISTRATE.item("cover.solar.panel.mv", ComponentItem::create).lang("Medium Voltage Solar Panel").onRegister(attach(new TooltipBehavior(lines -> {
        lines.addAll(Arrays.asList(LangHandler.getMultiLang("metaitem.cover.solar.panel.tooltip")));
        lines.add(Component.translatable("gregtech.universal.tooltip.voltage_out", GTValues.V[GTValues.MV], GTValues.VNF[GTValues.MV]));
    }))).register();
    public static ItemEntry<ComponentItem> COVER_SOLAR_PANEL_HV = REGISTRATE.item("cover.solar.panel.hv", ComponentItem::create).lang("High Voltage Solar Panel").onRegister(attach(new TooltipBehavior(lines -> {
        lines.addAll(Arrays.asList(LangHandler.getMultiLang("metaitem.cover.solar.panel.tooltip")));
        lines.add(Component.translatable("gregtech.universal.tooltip.voltage_out", GTValues.V[GTValues.HV], GTValues.VNF[GTValues.HV]));
    }))).register();
    public static ItemEntry<ComponentItem> COVER_SOLAR_PANEL_EV = REGISTRATE.item("cover.solar.panel.ev", ComponentItem::create).lang("Extreme Voltage Solar Panel").onRegister(attach(new TooltipBehavior(lines -> {
        lines.addAll(Arrays.asList(LangHandler.getMultiLang("metaitem.cover.solar.panel.tooltip")));
        lines.add(Component.translatable("gregtech.universal.tooltip.voltage_out", GTValues.V[GTValues.EV], GTValues.VNF[GTValues.EV]));
    }))).register();
    public static ItemEntry<ComponentItem> COVER_SOLAR_PANEL_IV = REGISTRATE.item("cover.solar.panel.iv", ComponentItem::create).lang("Insane Voltage Solar Panel").onRegister(attach(new TooltipBehavior(lines -> {
        lines.addAll(Arrays.asList(LangHandler.getMultiLang("metaitem.cover.solar.panel.tooltip")));
        lines.add(Component.translatable("gregtech.universal.tooltip.voltage_out", GTValues.V[GTValues.IV], GTValues.VNF[GTValues.IV]));
    }))).register();
    public static ItemEntry<ComponentItem> COVER_SOLAR_PANEL_LUV = REGISTRATE.item("cover.solar.panel.luv", ComponentItem::create).lang("Ludicrous Voltage Solar Panel").onRegister(attach(new TooltipBehavior(lines -> {
        lines.addAll(Arrays.asList(LangHandler.getMultiLang("metaitem.cover.solar.panel.tooltip")));
        lines.add(Component.translatable("gregtech.universal.tooltip.voltage_out", GTValues.V[GTValues.LuV], GTValues.VNF[GTValues.LuV]));
    }))).register();
    public static ItemEntry<ComponentItem> COVER_SOLAR_PANEL_ZPM = REGISTRATE.item("cover.solar.panel.zpm", ComponentItem::create).lang("Zero Point Module Solar Panel").onRegister(attach(new TooltipBehavior(lines -> {
        lines.addAll(Arrays.asList(LangHandler.getMultiLang("metaitem.cover.solar.panel.tooltip")));
        lines.add(Component.translatable("gregtech.universal.tooltip.voltage_out", GTValues.V[GTValues.ZPM], GTValues.VNF[GTValues.ZPM]));
    }))).register();
    public static ItemEntry<ComponentItem> COVER_SOLAR_PANEL_UV = REGISTRATE.item("cover.solar.panel.uv", ComponentItem::create).lang("Ultimate Voltage Solar Panel").onRegister(attach(new TooltipBehavior(lines -> {
        lines.addAll(Arrays.asList(LangHandler.getMultiLang("metaitem.cover.solar.panel.tooltip")));
        lines.add(Component.translatable("gregtech.universal.tooltip.voltage_out", GTValues.V[GTValues.UV], GTValues.VNF[GTValues.UV]));
    }))).register();

    // Plugin
    public static ItemEntry<Item> PLUGIN_TEXT;
    public static ItemEntry<Item> PLUGIN_ONLINE_PIC;
    public static ItemEntry<Item> PLUGIN_FAKE_GUI;
    public static ItemEntry<Item> PLUGIN_ADVANCED_MONITOR;

    public static ItemEntry<ComponentItem> INTEGRATED_CIRCUIT = REGISTRATE.item("circuit.integrated", ComponentItem::create)
            .lang("Programmed Circuit")
            .model(overrideModel(GTCEu.id("circuit"), 33))
            .onRegister(modelPredicate(GTCEu.id("circuit"), (itemStack) -> IntCircuitBehaviour.getCircuitConfiguration(itemStack) / 100f))
            .onRegister(attach(new IntCircuitBehaviour()))
            .register();


//    public static ItemEntry<ComponentItem> FOAM_SPRAYER = REGISTRATE.item("foam_sprayer", ComponentItem::create).onRegister(attach(new FoamSprayerBehavior()).setMaxStackSize(1);
    public static ItemEntry<Item> GELLED_TOLUENE = REGISTRATE.item("gelled_toluene", Item::new).register();

    public static ItemEntry<Item> BOTTLE_PURPLE_DRINK = REGISTRATE.item("bottle.purple.drink", Item::new)
            .lang("Purple Drink")
            .properties(p -> p.food(GTFoods.DRINK))
            .register();
    public static ItemEntry<ComponentItem> PLANT_BALL = REGISTRATE.item("plant_ball", ComponentItem::create).onRegister(burnTime(75)).register();
    public static ItemEntry<ComponentItem> STICKY_RESIN = REGISTRATE.item("rubber_drop", ComponentItem::create).lang("Sticky Resin").onRegister(burnTime(200)).register();
    public static ItemEntry<ComponentItem> BIO_CHAFF = REGISTRATE.item("bio_chaff", ComponentItem::create).onRegister(burnTime(200)).register();
    public static ItemEntry<Item> ENERGIUM_DUST = REGISTRATE.item("energium_dust", Item::new).register();

    public static ItemEntry<Item> POWER_UNIT_LV;
    public static ItemEntry<Item> POWER_UNIT_MV;
    public static ItemEntry<Item> POWER_UNIT_HV;
    public static ItemEntry<Item> POWER_UNIT_EV;
    public static ItemEntry<Item> POWER_UNIT_IV;

    public static ItemEntry<Item> NANO_SABER;
    public static ItemEntry<Item> PROSPECTOR_LV;
    public static ItemEntry<Item> PROSPECTOR_HV;
    public static ItemEntry<Item> PROSPECTOR_LUV;

    public static ItemEntry<Item> TRICORDER_SCANNER;
    public static ItemEntry<Item> DEBUG_SCANNER;

    public static ItemEntry<Item> ITEM_MAGNET_LV;
    public static ItemEntry<Item> ITEM_MAGNET_HV;

    public static ItemEntry<Item> WIRELESS;
    public static ItemEntry<Item> CAMERA;
    public static ItemEntry<Item> TERMINAL;

    public static final ItemEntry<Item>[] DYE_ONLY_ITEMS = new ItemEntry[DyeColor.values().length];
    static {
        for (int i = 0; i < DyeColor.values().length; i++) {
            var dyeColor = DyeColor.values()[i];
            DYE_ONLY_ITEMS[i] = REGISTRATE.item("dye." + dyeColor.getName(), Item::new)
                    .lang("Chemical %s Dye".formatted(toEnglishName(dyeColor.getName())))
                    .tag(TagUtil.createItemTag("dye." + dyeColor.getName())).register();
        }
    }
    
    public static final ItemEntry<ComponentItem>[] SPRAY_CAN_DYES = new ItemEntry[DyeColor.values().length];
    static {
        for (int i = 0; i < DyeColor.values().length; i++) {
            var dyeColor = DyeColor.values()[i];
            SPRAY_CAN_DYES[i] = REGISTRATE.item("spray.can.dyes." + dyeColor.getName(), ComponentItem::create)
                    .lang("Spray Can (%s)".formatted(toEnglishName(dyeColor.getName())))
                    .properties(p -> p.stacksTo(1))
                    .onRegister(attach(new ColorSprayBehaviour(() -> SPRAY_EMPTY.asStack(), 512, i))).register();
        }
    }
    
    public static ItemEntry<Item> TURBINE_ROTOR;

    public static ItemEntry<Item> NEURO_PROCESSOR = REGISTRATE.item("processor.neuro", Item::new).lang("Neuro Processing Unit").register();
    public static ItemEntry<Item> STEM_CELLS = REGISTRATE.item("stem_cells", Item::new).register();
    public static ItemEntry<Item> PETRI_DISH = REGISTRATE.item("petri_dish", Item::new).register();

    public static ItemEntry<ComponentItem> VOLTAGE_COIL_ULV = REGISTRATE.item("voltage_coil.ulv", ComponentItem::create).lang("Ultra Low Voltage Coil").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Lead, GTValues.M * 2), new MaterialStack(GTMaterials.IronMagnetic, GTValues.M / 2)))).register();
    public static ItemEntry<ComponentItem> VOLTAGE_COIL_LV = REGISTRATE.item("voltage_coil.lv", ComponentItem::create).lang("Low Voltage Coil").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 2), new MaterialStack(GTMaterials.IronMagnetic, GTValues.M / 2)))).register();
    public static ItemEntry<ComponentItem> VOLTAGE_COIL_MV = REGISTRATE.item("voltage_coil.mv", ComponentItem::create).lang("Medium Voltage Coil").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Aluminium, GTValues.M * 2), new MaterialStack(GTMaterials.SteelMagnetic, GTValues.M / 2)))).register();
    public static ItemEntry<ComponentItem> VOLTAGE_COIL_HV = REGISTRATE.item("voltage_coil.hv", ComponentItem::create).lang("High Voltage Coil").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.BlackSteel, GTValues.M * 2), new MaterialStack(GTMaterials.SteelMagnetic, GTValues.M / 2)))).register();
    public static ItemEntry<ComponentItem> VOLTAGE_COIL_EV = REGISTRATE.item("voltage_coil.ev", ComponentItem::create).lang("Extreme Voltage Coil").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.TungstenSteel, GTValues.M * 2), new MaterialStack(GTMaterials.NeodymiumMagnetic, GTValues.M / 2)))).register();
    public static ItemEntry<ComponentItem> VOLTAGE_COIL_IV = REGISTRATE.item("voltage_coil.iv", ComponentItem::create).lang("Insane Voltage Coil").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Iridium, GTValues.M * 2), new MaterialStack(GTMaterials.NeodymiumMagnetic, GTValues.M / 2)))).register();
    public static ItemEntry<ComponentItem> VOLTAGE_COIL_LuV = REGISTRATE.item("voltage_coil.luv", ComponentItem::create).lang("Ludicrous Voltage Coil").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Osmiridium, GTValues.M * 2), new MaterialStack(GTMaterials.SamariumMagnetic, GTValues.M / 2)))).register();
    public static ItemEntry<ComponentItem> VOLTAGE_COIL_ZPM = REGISTRATE.item("voltage_coil.zpm", ComponentItem::create).lang("Zero Point Module Voltage Coil").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Europium, GTValues.M * 2), new MaterialStack(GTMaterials.SamariumMagnetic, GTValues.M / 2)))).register();
    public static ItemEntry<ComponentItem> VOLTAGE_COIL_UV = REGISTRATE.item("voltage_coil.uv", ComponentItem::create).lang("Ultimate Voltage Coil").onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Tritanium, GTValues.M * 2), new MaterialStack(GTMaterials.SamariumMagnetic, GTValues.M / 2)))).register();

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

    public static ItemEntry<Item> POWER_THRUSTER = REGISTRATE.item("power_thruster", Item::new).properties(p -> p.rarity(Rarity.UNCOMMON)).register();
    public static ItemEntry<Item> POWER_THRUSTER_ADVANCED = REGISTRATE.item("power_thruster_advanced", Item::new).lang("Advanced Power Thruster").properties(p -> p.rarity(Rarity.RARE)).register();
    public static ItemEntry<Item> GRAVITATION_ENGINE = REGISTRATE.item("gravitation_engine", Item::new).lang("Gravitation Engine Unit").properties(p -> p.rarity(Rarity.EPIC)).register();

    public static ItemEntry<Item> SUS_RECORD;
    public static ItemEntry<Item> NAN_CERTIFICATE = REGISTRATE.item("nan.certificate", Item::new).lang("Certificate of Not Being a Noob Anymore").properties(p -> p.rarity(Rarity.EPIC)).register();

    public static ItemEntry<ComponentItem> FERTILIZER = REGISTRATE.item("fertilizer", ComponentItem::create).onRegister(attach(new FertilizerBehavior())).register();
    public static ItemEntry<Item> BLACKLIGHT = REGISTRATE.item("blacklight", Item::new).register();

    public static void init() {

    }

    public static <T extends ItemLike> NonNullConsumer<T> materialInfo(ItemMaterialInfo materialInfo) {
        return item -> ChemicalHelper.registerMaterialInfo(item, materialInfo);
    }


    public static <P, T extends Item, S2 extends ItemBuilder<T, P>> NonNullFunction<S2, S2> unificationItem(@Nonnull TagPrefix tagPrefix, @Nonnull Material mat) {
        return builder -> {
            builder.onRegister(item -> ChemicalHelper.registerUnificationItems(tagPrefix, mat, item));
            return builder;
        };
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

    //
    public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelProvider> overrideModel(ResourceLocation predicate, int modelNumber) {
        if (modelNumber <= 0) return NonNullBiConsumer.noop();
        return (ctx, prov) -> {
            var rootModel = prov.generated(ctx::getEntry, prov.modLoc("item/%s/1".formatted(prov.name(ctx))));
            for (int i = 0; i < modelNumber; i++) {
                var subModelBuilder = prov.getBuilder("item/" + prov.name(ctx::getEntry) + "/" + i).parent(new ModelFile.UncheckedModelFile("item/generated"));
                subModelBuilder.texture("layer0", prov.modLoc("item/%s/%d".formatted(prov.name(ctx), i + 1)));

                rootModel = rootModel.override().predicate(predicate, i / 100f).model(new ModelFile.UncheckedModelFile(prov.modLoc("item/%s/%d".formatted(prov.name(ctx), i)))).end();
            }
        };
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
