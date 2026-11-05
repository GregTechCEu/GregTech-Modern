package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.filter.*;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.ItemMaterialData;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.ItemMaterialInfo;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.TagPrefixItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.item.component.*;
import com.gregtechceu.gtceu.api.item.component.prospector.ProspectorMode;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.cover.ConveyorCover;
import com.gregtechceu.gtceu.common.cover.InfiniteWaterCover;
import com.gregtechceu.gtceu.common.cover.PumpCover;
import com.gregtechceu.gtceu.common.data.materials.GTFoods;
import com.gregtechceu.gtceu.common.entity.GTBoat;
import com.gregtechceu.gtceu.common.item.*;
import com.gregtechceu.gtceu.common.item.armor.*;
import com.gregtechceu.gtceu.common.item.behavior.*;
import com.gregtechceu.gtceu.common.item.behavior.LighterBehavior;
import com.gregtechceu.gtceu.common.item.behavior.MachineConfigCopyBehaviour;
import com.gregtechceu.gtceu.common.item.modules.GuiModuleBehaviour;
import com.gregtechceu.gtceu.common.item.modules.ImageModuleBehaviour;
import com.gregtechceu.gtceu.common.item.modules.TextModuleBehaviour;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.integration.ae2.AEItemCompat;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.lang.GTCommonComponents;
import com.gregtechceu.gtceu.utils.lang.LangUtil;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;

import com.google.common.base.Preconditions;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.common.data.GTCreativeModeTabs.*;
import static com.gregtechceu.gtceu.common.data.models.GTModels.*;
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.utils.lang.FormattingUtil.toEnglishName;

@SuppressWarnings("unused")
public class GTItems {

    //////////////////////////////////////
    // ******* Misc Items ********//

    //////////////////////////////////////
    static {
        REGISTRATE.creativeModeTab(() -> ITEM);
    }

    public static ItemEntry<Item> COMPRESSED_CLAY = REGISTRATE.item("compressed_clay", Item::new)
            .lang("Compressed Clay")
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, GTValues.M)))).register();
    public static ItemEntry<Item> COMPRESSED_COKE_CLAY = REGISTRATE.item("compressed_coke_clay", Item::new)
            .lang("Compressed Coke Clay")
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, GTValues.M)))).register();
    public static ItemEntry<Item> COMPRESSED_FIRECLAY = REGISTRATE.item("compressed_fireclay", Item::new)
            .lang("Compressed Fireclay")
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Fireclay, GTValues.M))))
            .register();
    public static ItemEntry<ComponentItem> FIRECLAY_BRICK = REGISTRATE.item("firebrick", ComponentItem::create)
            .transform(nameAndTooltip("Firebrick", "Heat resistant"))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Fireclay, GTValues.M))))
            .register();
    public static ItemEntry<Item> COKE_OVEN_BRICK = REGISTRATE.item("coke_oven_brick", Item::new)
            .lang("Coke Oven Brick")
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, GTValues.M)))).register();
    public static ItemEntry<Item> WOODEN_FORM_EMPTY = REGISTRATE.item("empty_wooden_form", Item::new)
            .lang("Empty Wooden Form")
            .register();
    public static ItemEntry<ComponentItem> WOODEN_FORM_BRICK = REGISTRATE
            .item("brick_wooden_form", ComponentItem::create)
            .lang("Brick Wooden Form")
            .properties(p -> p.stacksTo(1))
            .onRegister(attach((IRecipeRemainder) ItemStack::copy)).register();

    public static ItemEntry<ComponentItem> SHAPE_EMPTY = REGISTRATE.item("empty_mold", ComponentItem::create)
            .transform(nameAndTooltip("Empty Mold", "Raw Plate to make Molds and Extrude Shapes"))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4))))
            .register();

    @SuppressWarnings("unchecked")
    public static final ItemEntry<ComponentItem>[] SHAPE_MOLDS = new ItemEntry[18];
    public static final ItemEntry<ComponentItem> SHAPE_MOLD_PLATE;
    public static final ItemEntry<ComponentItem> SHAPE_MOLD_GEAR;
    public static final ItemEntry<ComponentItem> SHAPE_MOLD_BOTTLE;
    public static final ItemEntry<ComponentItem> SHAPE_MOLD_INGOT;
    public static final ItemEntry<ComponentItem> SHAPE_MOLD_BALL;
    public static final ItemEntry<ComponentItem> SHAPE_MOLD_BLOCK;
    public static final ItemEntry<ComponentItem> SHAPE_MOLD_NUGGET;
    public static final ItemEntry<ComponentItem> SHAPE_MOLD_CYLINDER;
    public static final ItemEntry<ComponentItem> SHAPE_MOLD_ANVIL;
    public static final ItemEntry<ComponentItem> SHAPE_MOLD_NAME;
    public static final ItemEntry<ComponentItem> SHAPE_MOLD_GEAR_SMALL;
    public static final ItemEntry<ComponentItem> SHAPE_MOLD_ROTOR;
    public static final ItemEntry<ComponentItem> SHAPE_MOLD_PILL;
    public static final ItemEntry<ComponentItem> SHAPE_MOLD_TINY_PIPE;
    public static final ItemEntry<ComponentItem> SHAPE_MOLD_SMALL_PIPE;
    public static final ItemEntry<ComponentItem> SHAPE_MOLD_NORMAL_PIPE;
    public static final ItemEntry<ComponentItem> SHAPE_MOLD_LARGE_PIPE;
    public static final ItemEntry<ComponentItem> SHAPE_MOLD_HUGE_PIPE;

    static {
        SHAPE_MOLDS[0] = SHAPE_MOLD_PLATE = REGISTRATE.item("plate_casting_mold", ComponentItem::create)
                .transform(nameAndTooltip("Casting Mold (Plate)", "Mold for making Plates"))
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4))))
                .register();
        SHAPE_MOLDS[1] = SHAPE_MOLD_GEAR = REGISTRATE.item("gear_casting_mold", ComponentItem::create)
                .transform(nameAndTooltip("Casting Mold (Gear)", "Mold for making Gears"))
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4))))
                .register();
        SHAPE_MOLDS[2] = SHAPE_MOLD_BOTTLE = REGISTRATE.item("bottle_casting_mold", ComponentItem::create)
                .transform(nameAndTooltip("Casting Mold (Bottle)", "Mold for making Bottles"))
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4))))
                .register();
        SHAPE_MOLDS[3] = SHAPE_MOLD_INGOT = REGISTRATE.item("ingot_casting_mold", ComponentItem::create)
                .transform(nameAndTooltip("Casting Mold (Ingot)", "Mold for making Ingots"))
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4))))
                .register();
        SHAPE_MOLDS[4] = SHAPE_MOLD_BALL = REGISTRATE.item("ball_casting_mold", ComponentItem::create)
                .transform(nameAndTooltip("Casting Mold (Ball)", "Mold for making Balls"))
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4))))
                .register();
        SHAPE_MOLDS[5] = SHAPE_MOLD_BLOCK = REGISTRATE.item("block_casting_mold", ComponentItem::create)
                .transform(nameAndTooltip("Casting Mold (Block)", "Mold for making Blocks"))
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4))))
                .register();
        SHAPE_MOLDS[6] = SHAPE_MOLD_NUGGET = REGISTRATE.item("nugget_casting_mold", ComponentItem::create)
                .transform(nameAndTooltip("Casting Mold (Nugget)", "Mold for making Nuggets"))
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4))))
                .register();
        SHAPE_MOLDS[7] = SHAPE_MOLD_CYLINDER = REGISTRATE.item("cylinder_casting_mold", ComponentItem::create)
                .transform(nameAndTooltip("Casting Mold (Cylinder)", "Mold for shaping Cylinders"))
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4))))
                .register();
        SHAPE_MOLDS[8] = SHAPE_MOLD_ANVIL = REGISTRATE.item("anvil_casting_mold", ComponentItem::create)
                .transform(nameAndTooltip("Casting Mold (Anvil)", "Mold for shaping Anvils"))
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4))))
                .register();
        SHAPE_MOLDS[9] = SHAPE_MOLD_NAME = REGISTRATE.item("name_casting_mold", ComponentItem::create)
                .transform(nameAndTooltip("Casting Mold (Name)", "Mold for naming Items in the Forming Press (rename Mold with Anvil)"))
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4))))
                .register();
        SHAPE_MOLDS[10] = SHAPE_MOLD_GEAR_SMALL = REGISTRATE.item("small_gear_casting_mold", ComponentItem::create)
                .transform(nameAndTooltip("Casting Mold (Small Gear)", "Mold for making Small Gears"))
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4))))
                .register();
        SHAPE_MOLDS[11] = SHAPE_MOLD_ROTOR = REGISTRATE.item("rotor_casting_mold", ComponentItem::create)
                .transform(nameAndTooltip("Casting Mold (Rotor)", "Mold for making Rotors"))
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4))))
                .register();
        SHAPE_MOLDS[12] = SHAPE_MOLD_PILL = REGISTRATE.item("pill_casting_mold", ComponentItem::create)
                .transform(nameAndTooltip("Casting Mold (Pill)", "Mold for making Pills"))
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4))))
                .register();
        SHAPE_MOLDS[13] = SHAPE_MOLD_TINY_PIPE = REGISTRATE.item("tiny_pipe_casting_mold", ComponentItem::create)
                .transform(nameAndTooltip("Casting Mold (Tiny Pipe)", "Mold for making Tiny Pipes"))
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4))))
                .register();
        SHAPE_MOLDS[14] = SHAPE_MOLD_SMALL_PIPE = REGISTRATE.item("small_pipe_casting_mold", ComponentItem::create)
                .transform(nameAndTooltip("Casting Mold (Small Pipe)", "Mold for making Small Pipes"))
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4))))
                .register();
        SHAPE_MOLDS[15] = SHAPE_MOLD_NORMAL_PIPE = REGISTRATE.item("normal_pipe_casting_mold", ComponentItem::create)
                .transform(nameAndTooltip("Casting Mold (Normal Pipe)", "Mold for making Pipes"))
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4))))
                .register();
        SHAPE_MOLDS[16] = SHAPE_MOLD_LARGE_PIPE = REGISTRATE.item("large_pipe_casting_mold", ComponentItem::create)
                .transform(nameAndTooltip("Casting Mold (Large Pipe)", "Mold for making Large Pipes"))
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4))))
                .register();
        SHAPE_MOLDS[17] = SHAPE_MOLD_HUGE_PIPE = REGISTRATE.item("huge_pipe_casting_mold", ComponentItem::create)
                .transform(nameAndTooltip("Casting Mold (Huge Pipe)", "Mold for making Huge Pipes"))
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4))))
                .register();
    }

    public static final ItemEntry<ComponentItem>[] SHAPE_EXTRUDERS = new ItemEntry[27];
    public static ItemEntry<ComponentItem> SHAPE_EXTRUDER_PLATE;
    public static ItemEntry<ComponentItem> SHAPE_EXTRUDER_RING;
    public static ItemEntry<ComponentItem> SHAPE_EXTRUDER_CELL;
    public static ItemEntry<ComponentItem> SHAPE_EXTRUDER_INGOT;
    public static ItemEntry<ComponentItem> SHAPE_EXTRUDER_WIRE;
    public static ItemEntry<ComponentItem> SHAPE_EXTRUDER_PIPE_TINY;
    public static ItemEntry<ComponentItem> SHAPE_EXTRUDER_PIPE_SMALL;
    public static ItemEntry<ComponentItem> SHAPE_EXTRUDER_PIPE_NORMAL;
    public static ItemEntry<ComponentItem> SHAPE_EXTRUDER_PIPE_LARGE;
    public static ItemEntry<ComponentItem> SHAPE_EXTRUDER_PIPE_HUGE;
    public static ItemEntry<ComponentItem> SHAPE_EXTRUDER_BLOCK;
    public static ItemEntry<ComponentItem> SHAPE_EXTRUDER_BOTTLE;
    public static ItemEntry<ComponentItem> SHAPE_EXTRUDER_FOIL;

    static {
        SHAPE_EXTRUDERS[0] = SHAPE_EXTRUDER_PLATE = REGISTRATE.item("plate_extruder_mold", ComponentItem::create)
                .transform(nameAndTooltip("Extruder Mold (Plate)", "Extruder Shape for making Plates"))
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4))))
                .register();
        SHAPE_EXTRUDERS[3] = SHAPE_EXTRUDER_RING = REGISTRATE.item("ring_extruder_mold", ComponentItem::create)
                .transform(nameAndTooltip("Extruder Mold (Ring)", "Extruder Shape for making Rings"))
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4))))
                .register();
        SHAPE_EXTRUDERS[4] = SHAPE_EXTRUDER_CELL = REGISTRATE.item("cell_extruder_mold", ComponentItem::create)
                .transform(nameAndTooltip("Extruder Mold (Cell)", "Extruder Shape for making Cells"))
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4))))
                .register();
        SHAPE_EXTRUDERS[5] = SHAPE_EXTRUDER_INGOT = REGISTRATE.item("ingot_extruder_mold", ComponentItem::create)
                .transform(nameAndTooltip("Extruder Mold (Ingot)", "Extruder Shape for, wait, can't we just use a Furnace?"))
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4))))
                .register();
        SHAPE_EXTRUDERS[6] = SHAPE_EXTRUDER_WIRE = REGISTRATE.item("wire_extruder_mold", ComponentItem::create)
                .transform(nameAndTooltip("Extruder Mold (Wire)", "Extruder Shape for making Wires"))
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4))))
                .register();
        SHAPE_EXTRUDERS[7] = SHAPE_EXTRUDER_PIPE_TINY = REGISTRATE.item("tiny_pipe_extruder_mold", ComponentItem::create)
                .transform(nameAndTooltip("Extruder Mold (Tiny Pipe)", "Extruder Shape for making Tiny Pipes"))
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4))))
                .register();
        SHAPE_EXTRUDERS[8] = SHAPE_EXTRUDER_PIPE_SMALL = REGISTRATE.item("small_pipe_extruder_mold", ComponentItem::create)
                .transform(nameAndTooltip("Extruder Mold (Small Pipe)", "Extruder Shape for making Small Pipes"))
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4))))
                .register();
        SHAPE_EXTRUDERS[9] = SHAPE_EXTRUDER_PIPE_NORMAL = REGISTRATE.item("normal_pipe_extruder_mold", ComponentItem::create)
                .transform(nameAndTooltip("Extruder Mold (Normal Pipe)", "Extruder Shape for making Pipes"))
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4))))
                .register();
        SHAPE_EXTRUDERS[10] = SHAPE_EXTRUDER_PIPE_LARGE = REGISTRATE.item("large_pipe_extruder_mold", ComponentItem::create)
                .transform(nameAndTooltip("Extruder Mold (Large Pipe)", "Extruder Shape for making Large Pipes"))
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4))))
                .register();
        SHAPE_EXTRUDERS[11] = SHAPE_EXTRUDER_PIPE_HUGE = REGISTRATE.item("huge_pipe_extruder_mold", ComponentItem::create)
                .transform(nameAndTooltip("Extruder Mold (Huge Pipe)", "Extruder Shape for making Huge Pipes"))
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4))))
                .register();
        SHAPE_EXTRUDERS[12] = SHAPE_EXTRUDER_BLOCK = REGISTRATE.item("block_extruder_mold", ComponentItem::create)
                .transform(nameAndTooltip("Extruder Mold (Block)", "Extruder Shape for making Blocks"))
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4))))
                .register();
        // Extruder Shapes index 13-20 (inclusive), id 44-51 (inclusive) are unused
        SHAPE_EXTRUDERS[22] = SHAPE_EXTRUDER_BOTTLE = REGISTRATE.item("bottle_extruder_mold", ComponentItem::create)
                .transform(nameAndTooltip("Extruder Mold (Bottle)", "Extruder Shape for making Bottles"))
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4))))
                .register();
        SHAPE_EXTRUDERS[23] = SHAPE_EXTRUDER_FOIL = REGISTRATE.item("foil_extruder_mold", ComponentItem::create)
                .transform(nameAndTooltip("Extruder Mold (Foil)", "Extruder Shape for making Foils from Non-Metals"))
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4))))
                .register();
    }

    public static ItemEntry<ComponentItem> SPRAY_EMPTY = REGISTRATE.item("empty_spray_can", ComponentItem::create)
            .transform(nameAndTooltip("Spray Can (Empty)", "Can be filled with sprays of various colors"))
            .register();
    public static ItemEntry<ComponentItem> SPRAY_SOLVENT = REGISTRATE.item("solvent_spray_can", ComponentItem::create)
            .lang("Spray Can (Solvent)")
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(new ColorSprayBehaviour(() -> SPRAY_EMPTY.asStack(), 1024, -1))).register();

    public static ItemEntry<ComponentItem> PORTABLE_SCANNER = REGISTRATE.item("portable_scanner", ComponentItem::create)
            .lang("Portable Scanner")
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(ElectricStats.createElectricItem(100_000L, GTValues.MV), new PortableScannerBehavior(0)))
            .register();

    public static ItemEntry<ComponentItem> PORTABLE_DEBUG_SCANNER = REGISTRATE
            .item("portable_debug_scanner", ComponentItem::create)
            .lang("Portable Debug Scanner")
            .properties(p -> p.stacksTo(1))
            .onRegister(
                    attach(ElectricStats.createElectricItem(1_000_000L, GTValues.MV), new PortableScannerBehavior(1)))
            .register();

    @OnlyIn(Dist.CLIENT)
    public static ItemColor cellColor() {
        return (itemStack, index) -> {
            if (index == 1) {
                return FluidUtil.getFluidContained(itemStack)
                        .map(f -> f.getFluid() == Fluids.LAVA ? 0xFFFF7000 : GTUtil.getFluidColor(f))
                        .orElse(-1);
            }
            return -1;
        };
    }

    public static ICustomDescriptionId cellName() {
        return new ICustomDescriptionId() {

            @Override
            public Component getItemName(ItemStack stack) {
                Component prefix = FluidUtil.getFluidContained(stack).map(FluidStack::getDisplayName)
                        .orElse(GTCommonComponents.EMPTY);
                return Component.translatable(stack.getDescriptionId(), prefix);
            }
        };
    }

    public static ItemEntry<ComponentItem> FLUID_CELL = REGISTRATE.item("fluid_cell", ComponentItem::create)
            .lang("%s Fluid Cell")
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .color(() -> GTItems::cellColor)
            .onRegister(attach(
                    ThermalFluidStats.create(FluidType.BUCKET_VOLUME, 1800, true, false, false, false, false),
                    new ItemFluidContainer(), cellName()))
            .register();
    public static ItemEntry<ComponentItem> FLUID_CELL_UNIVERSAL = REGISTRATE
            .item("universal_fluid_cell", ComponentItem::create)
            .lang("%s Universal Cell")
            .color(() -> GTItems::cellColor)
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .onRegister(attach(cellName(),
                    ThermalFluidStats.create(FluidType.BUCKET_VOLUME, 1800, true, false, false, false, true),
                    new ItemFluidContainer()))
            .register();
    public static ItemEntry<ComponentItem> FLUID_CELL_LARGE_STEEL = createFluidCell(GTMaterials.Steel, 8, 4, 64);
    public static ItemEntry<ComponentItem> FLUID_CELL_LARGE_ALUMINIUM = createFluidCell(GTMaterials.Aluminium, 32, 4,
            64);
    public static ItemEntry<ComponentItem> FLUID_CELL_LARGE_STAINLESS_STEEL = createFluidCell(
            GTMaterials.StainlessSteel, 64, 6, 64);
    public static ItemEntry<ComponentItem> FLUID_CELL_LARGE_TITANIUM = createFluidCell(GTMaterials.Titanium, 128, 6,
            64);
    public static ItemEntry<ComponentItem> FLUID_CELL_LARGE_TUNGSTEN_STEEL = createFluidCell(GTMaterials.TungstenSteel,
            512, 8, 32, "%s Tungstensteel Cell");

    public static ItemEntry<ComponentItem> FLUID_CELL_GLASS_VIAL = REGISTRATE.item("glass_vial", ComponentItem::create)
            .lang("%s Glass Vial")
            .color(() -> GTItems::cellColor)
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .onRegister(
                    attach(cellName(),
                            ThermalFluidStats.create(FluidType.BUCKET_VOLUME, 1200, false, true, false, false,
                                    true),
                            new ItemFluidContainer()))
            .register();

    public static ItemEntry<ComponentItem> createFluidCell(Material mat, int capacity, int matSize, int stackSize) {
        return createFluidCell(mat, capacity, matSize, stackSize, "%s " + toEnglishName(mat.getName()) + " Cell");
    }

    public static ItemEntry<ComponentItem> createFluidCell(Material mat, int capacity, int matSize, int stackSize,
                                                           String langValue) {
        var prop = mat.getProperty(PropertyKey.FLUID_PIPE);
        Preconditions.checkArgument(prop != null,
                "Material { %s } does not have Fluid Pipe properties, but is being used to create a Fluid Cell",
                mat.getName());
        return REGISTRATE
                .item("%s_fluid_cell".formatted(mat.getName()), ComponentItem::create)
                .lang(langValue)
                .color(() -> GTItems::cellColor)
                .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
                .properties(p -> p.stacksTo(stackSize))
                .onRegister(attach(cellName(),
                        ThermalFluidStats.create(FluidType.BUCKET_VOLUME * capacity, prop, true),
                        new ItemFluidContainer()))
                .tag(CustomTags.FLUID_CELLS)
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(mat, GTValues.M * matSize))))
                .register();
    }

    public static ItemEntry<ComponentItem> TOOL_MATCHES = REGISTRATE.item("matches", ComponentItem::create)
            .lang("Matches")
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .onRegister(attach(new LighterBehavior(false, false, false)))
            .tag(CustomTags.TOOLS_IGNITER)
            .register();

    public static ItemEntry<ComponentItem> TOOL_MATCHBOX = REGISTRATE.item("matchbox", ComponentItem::create)
            .transform(nameAndTooltip("Matchbox", "This is not a Car"))
            .properties(p -> p.stacksTo(1))
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .onRegister(attach(new LighterBehavior(false, true, false, () -> new ItemStack(Items.PAPER, 1), 16)))
            .tag(CustomTags.TOOLS_IGNITER)
            .register();

    public static ItemEntry<ComponentItem> TOOL_LIGHTER_INVAR = REGISTRATE.item("invar_lighter", ComponentItem::create)
            .lang("Invar Lighter")
            .properties(p -> p.stacksTo(1))
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .onRegister(attach(new LighterBehavior(true, true, true)))
            .onRegister(attach(new FilteredFluidContainer(100, true, x -> x.getFluid().is(CustomTags.LIGHTER_FLUIDS)),
                    new ItemFluidContainer()))
            .onRegister(modelPredicate(GTCEu.id("lighter_open"),
                    (itemStack) -> itemStack.getOrCreateTag().getBoolean(LighterBehavior.LIGHTER_OPEN) ? 1.0f : 0.0f))
            .tag(CustomTags.TOOLS_IGNITER)
            .register();
    public static ItemEntry<ComponentItem> TOOL_LIGHTER_PLATINUM = REGISTRATE
            .item("platinum_lighter", ComponentItem::create)
            .transform(nameAndTooltip("Platinum Lighter", "A known Prank Master is engraved on it"))
            .properties(p -> p.stacksTo(1).rarity(Rarity.UNCOMMON))
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .onRegister(attach(new LighterBehavior(true, true, true)))
            .onRegister(attach(new FilteredFluidContainer(1000, true, x -> x.getFluid().is(CustomTags.LIGHTER_FLUIDS)),
                    new ItemFluidContainer()))
            .onRegister(modelPredicate(GTCEu.id("lighter_open"),
                    (itemStack) -> itemStack.getOrCreateTag().getBoolean(LighterBehavior.LIGHTER_OPEN) ? 1.0f : 0.0f))
            .tag(CustomTags.TOOLS_IGNITER)
            .register();

    public static ItemEntry<Item> CARBON_FIBERS = REGISTRATE.item("carbon_fibers", Item::new)
            .lang("Raw Carbon Fibers")
            .register();
    public static ItemEntry<Item> CARBON_MESH = REGISTRATE.item("carbon_fiber_mesh", Item::new)
            .lang("Carbon Fiber Mesh")
            .register();
    public static ItemEntry<Item> CARBON_FIBER_PLATE = REGISTRATE.item("carbon_fiber_plate", Item::new)
            .lang("Carbon Fiber Plate")
            .register();
    public static ItemEntry<ComponentItem> DUCT_TAPE = REGISTRATE
            .item("duct_tape", ComponentItem::create)
            .transform(nameAndTooltip("BrainTech Aerospace Advanced Reinforced Duct Tape FAL-84",
                    "If you can't fix it with this, use more of it!"))
            .onRegister(attach(new TapeBehaviour()))
            .register();
    public static ItemEntry<ComponentItem> BASIC_TAPE = REGISTRATE
            .item("basic_tape", ComponentItem::create)
            .transform(nameAndTooltip("Tape",
                    "Not strong enough for mechanical issues", "Can be used to pick up crates without dropping their items"))
            .onRegister(attach(new TapeBehaviour()))
            .register();

    public static ItemEntry<ComponentItem> NEUTRON_REFLECTOR = REGISTRATE.item("neutron_reflector", ComponentItem::create)
            .transform(nameAndTooltip("Iridium Neutron Reflector", "Indestructible"))
            .register();

    public static ItemEntry<ComponentItem> BATTERY_HULL_LV = REGISTRATE.item("lv_battery_hull", ComponentItem::create)
            .lang("Small Battery Hull")
            .onRegister(attachBatteryHullTooltip(GTValues.LV))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.BatteryAlloy, GTValues.M))))
            .register();

    public static ItemEntry<ComponentItem> BATTERY_HULL_MV = REGISTRATE.item("mv_battery_hull", ComponentItem::create)
            .lang("Medium Battery Hull")
            .onRegister(attachBatteryHullTooltip(GTValues.MV))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.BatteryAlloy, GTValues.M * 3))))
            .register();

    public static ItemEntry<ComponentItem> BATTERY_HULL_HV = REGISTRATE.item("hv_battery_hull", ComponentItem::create)
            .lang("Large Battery Hull")
            .onRegister(attachBatteryHullTooltip(GTValues.HV))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.BatteryAlloy, GTValues.M * 9))))
            .register();

    public static ItemEntry<ComponentItem> BATTERY_HULL_SMALL_VANADIUM = REGISTRATE.item("ev_battery_hull", ComponentItem::create)
            .lang("Small Vanadium Battery Hull")
            .onRegister(attachBatteryHullTooltip(GTValues.EV))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.RedSteel, GTValues.M * 2))))
            .register();

    public static ItemEntry<ComponentItem> BATTERY_HULL_MEDIUM_VANADIUM = REGISTRATE.item("iv_battery_hull", ComponentItem::create)
            .lang("Medium Vanadium Battery Hull")
            .onRegister(attachBatteryHullTooltip(GTValues.IV))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.RoseGold, GTValues.M * 6))))
            .register();

    public static ItemEntry<ComponentItem> BATTERY_HULL_LARGE_VANADIUM = REGISTRATE.item("luv_battery_hull", ComponentItem::create)
            .lang("Large Vanadium Battery Hull")
            .onRegister(attachBatteryHullTooltip(GTValues.LuV))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.BlueSteel, GTValues.M * 18))))
            .register();

    public static ItemEntry<ComponentItem> BATTERY_HULL_MEDIUM_NAQUADRIA = REGISTRATE.item("zpm_battery_hull", ComponentItem::create)
            .lang("Medium Naquadria Battery Hull")
            .onRegister(attachBatteryHullTooltip(GTValues.ZPM))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Europium, GTValues.M * 6))))
            .register();

    public static ItemEntry<ComponentItem> BATTERY_HULL_LARGE_NAQUADRIA = REGISTRATE.item("uv_battery_hull", ComponentItem::create)
            .lang("Large Naquadria Battery Hull")
            .onRegister(attachBatteryHullTooltip(GTValues.UV))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Americium, GTValues.M * 18))))
            .register();

    private static NonNullConsumer<ComponentItem> attachBatteryHullTooltip(int tier) {
        return i -> i.attachComponents(new TooltipBehavior(Component.translatable("item.gtceu.battery_hull.tooltip", GTValues.VNC[tier])
                .withStyle(ChatFormatting.GRAY)));
    }

    public static ItemEntry<ComponentItem> BATTERY_ULV_TANTALUM = REGISTRATE
            .item("tantalum_capacitor", ComponentItem::create)
            .transform(nameAndTooltip("Tantalum Capacitor", "Reusable Battery"))
            .onRegister(attach(ElectricStats.createRechargeableBattery(1000, GTValues.ULV)))
            .tag(CustomTags.ULV_BATTERIES).register();

    public static ItemEntry<ComponentItem> BATTERY_LV_SODIUM = REGISTRATE
            .item("lv_sodium_battery", ComponentItem::create)
            .transform(nameAndTooltip("Small Sodium Battery", "Reusable Battery"))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(80000, GTValues.LV)))
            .tag(CustomTags.LV_BATTERIES).register();

    public static ItemEntry<ComponentItem> BATTERY_MV_SODIUM = REGISTRATE
            .item("mv_sodium_battery", ComponentItem::create)
            .transform(nameAndTooltip("Medium Sodium Battery", "Reusable Battery"))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(360000, GTValues.MV)))
            .tag(CustomTags.MV_BATTERIES).register();

    public static ItemEntry<ComponentItem> BATTERY_HV_SODIUM = REGISTRATE
            .item("hv_sodium_battery", ComponentItem::create)
            .transform(nameAndTooltip("Large Sodium Battery", "Reusable Battery"))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(1200000, GTValues.HV)))
            .tag(CustomTags.HV_BATTERIES).register();

    public static ItemEntry<ComponentItem> BATTERY_LV_LITHIUM = REGISTRATE
            .item("lv_lithium_battery", ComponentItem::create)
            .transform(nameAndTooltip("Small Lithium Battery", "Reusable Battery"))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(120000, GTValues.LV)))
            .tag(CustomTags.LV_BATTERIES).register();

    public static ItemEntry<ComponentItem> BATTERY_MV_LITHIUM = REGISTRATE
            .item("mv_lithium_battery", ComponentItem::create)
            .transform(nameAndTooltip("Medium Lithium Battery", "Reusable Battery"))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(420000, GTValues.MV)))
            .tag(CustomTags.MV_BATTERIES).register();

    public static ItemEntry<ComponentItem> BATTERY_HV_LITHIUM = REGISTRATE
            .item("hv_lithium_battery", ComponentItem::create)
            .transform(nameAndTooltip("Large Lithium Battery", "Reusable Battery"))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(1800000, GTValues.HV)))
            .tag(CustomTags.HV_BATTERIES).register();

    public static ItemEntry<ComponentItem> BATTERY_LV_CADMIUM = REGISTRATE
            .item("lv_cadmium_battery", ComponentItem::create)
            .transform(nameAndTooltip("Small Cadmium Battery", "Reusable Battery"))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(100000, GTValues.LV)))
            .tag(CustomTags.LV_BATTERIES).register();

    public static ItemEntry<ComponentItem> BATTERY_MV_CADMIUM = REGISTRATE
            .item("mv_cadmium_battery", ComponentItem::create)
            .transform(nameAndTooltip("Medium Cadmium Battery", "Reusable Battery"))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(400000, GTValues.MV)))
            .tag(CustomTags.MV_BATTERIES).register();

    public static ItemEntry<ComponentItem> BATTERY_HV_CADMIUM = REGISTRATE
            .item("hv_cadmium_battery", ComponentItem::create)
            .transform(nameAndTooltip("Large Cadmium Battery", "Reusable Battery"))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(1600000, GTValues.HV)))
            .tag(CustomTags.HV_BATTERIES).register();

    public static ItemEntry<ComponentItem> ENERGIUM_CRYSTAL = REGISTRATE.item("energy_crystal", ComponentItem::create)
            .transform(nameAndTooltip("Energium Crystal", "Reusable Battery"))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(6_400_000L, GTValues.HV)))
            .tag(CustomTags.HV_BATTERIES).register();

    public static ItemEntry<ComponentItem> LAPOTRON_CRYSTAL = REGISTRATE.item("lapotron_crystal", ComponentItem::create)
            .transform(nameAndTooltip("Lapotron Crystal", "Reusable Battery"))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(25_000_000L, GTValues.EV)))
            .tag(CustomTags.EV_BATTERIES).register();

    public static ItemEntry<ComponentItem> BATTERY_EV_VANADIUM = REGISTRATE
            .item("ev_vanadium_battery", ComponentItem::create)
            .transform(nameAndTooltip("Small Vanadium Battery", "Reusable Battery"))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(10_240_000L, GTValues.EV)))
            .tag(CustomTags.EV_BATTERIES).register();

    public static ItemEntry<ComponentItem> BATTERY_IV_VANADIUM = REGISTRATE
            .item("iv_vanadium_battery", ComponentItem::create)
            .transform(nameAndTooltip("Medium Vanadium Battery", "Reusable Battery"))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(40_960_000L, GTValues.IV)))
            .tag(CustomTags.IV_BATTERIES).register();

    public static ItemEntry<ComponentItem> BATTERY_LuV_VANADIUM = REGISTRATE
            .item("luv_vanadium_battery", ComponentItem::create)
            .transform(nameAndTooltip("Large Vanadium Battery", "Reusable Battery"))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(163_840_000L, GTValues.LuV)))
            .tag(CustomTags.LuV_BATTERIES).register();

    public static ItemEntry<ComponentItem> BATTERY_ZPM_NAQUADRIA = REGISTRATE
            .item("zpm_naquadria_battery", ComponentItem::create)
            .transform(nameAndTooltip("Medium Naquadria Battery", "Reusable Battery"))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(655_360_000L, GTValues.ZPM)))
            .tag(CustomTags.ZPM_BATTERIES).register();

    public static ItemEntry<ComponentItem> BATTERY_UV_NAQUADRIA = REGISTRATE
            .item("uv_naquadria_battery", ComponentItem::create)
            .transform(nameAndTooltip("Large Naquadria Battery", "Reusable Battery"))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(2_621_440_000L, GTValues.UV)))
            .tag(CustomTags.UV_BATTERIES).register();

    public static ItemEntry<ComponentItem> ENERGY_LAPOTRONIC_ORB = REGISTRATE
            .item("lapotronic_energy_orb", ComponentItem::create)
            .transform(nameAndTooltip("Lapotronic Energy Orb", "Reusable Battery"))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(250_000_000L, GTValues.IV)))
            .tag(CustomTags.IV_BATTERIES).register();

    public static ItemEntry<ComponentItem> ENERGY_LAPOTRONIC_ORB_CLUSTER = REGISTRATE
            .item("lapotronic_energy_orb_cluster", ComponentItem::create)
            .transform(nameAndTooltip("Lapotronic Energy Orb Cluster", "Reusable Battery"))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(1_000_000_000L, GTValues.LuV)))
            .tag(CustomTags.LuV_BATTERIES).register();

    public static ItemEntry<ComponentItem> ENERGY_MODULE = REGISTRATE.item("energy_module", ComponentItem::create)
            .lang("Energy Module")
            .transform(nameAndTooltip("Energy Module", "Reusable Battery"))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(4_000_000_000L, GTValues.ZPM)))
            .tag(CustomTags.ZPM_BATTERIES).register();

    public static ItemEntry<ComponentItem> ENERGY_CLUSTER = REGISTRATE.item("energy_cluster", ComponentItem::create)
            .lang("Energy Cluster")
            .transform(nameAndTooltip("Energy Cluster", "Reusable Battery"))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(20_000_000_000L, GTValues.UV)))
            .tag(CustomTags.UV_BATTERIES).register();

    public static ItemEntry<ComponentItem> ZERO_POINT_MODULE = REGISTRATE
            .item("zero_point_module", ComponentItem::create)
            .transform(nameAndTooltip("Zero Point Module", "Reusable Battery"))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createBattery(2000000000000L, GTValues.ZPM, true)))
            .register();

    public static ItemEntry<ComponentItem> ULTIMATE_BATTERY = REGISTRATE.item("max_battery", ComponentItem::create)
            .transform(nameAndTooltip("Ultimate Battery", "Fill this to win Minecraft"))
            .model(overrideModel(GTCEu.id("battery"), 8))
            .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
            .onRegister(attach(ElectricStats.createRechargeableBattery(Long.MAX_VALUE, GTValues.UHV)))
            .tag(CustomTags.UHV_BATTERIES).register();

    public static ItemEntry<ComponentItem> ELECTRIC_MOTOR_LV = registerElectricMotor(GTValues.LV);
    public static ItemEntry<ComponentItem> ELECTRIC_MOTOR_MV = registerElectricMotor(GTValues.MV);
    public static ItemEntry<ComponentItem> ELECTRIC_MOTOR_HV = registerElectricMotor(GTValues.HV);
    public static ItemEntry<ComponentItem> ELECTRIC_MOTOR_EV = registerElectricMotor(GTValues.EV);
    public static ItemEntry<ComponentItem> ELECTRIC_MOTOR_IV = registerElectricMotor(GTValues.IV);
    public static ItemEntry<ComponentItem> ELECTRIC_MOTOR_LuV = registerElectricMotor(GTValues.LuV);
    public static ItemEntry<ComponentItem> ELECTRIC_MOTOR_ZPM = registerElectricMotor(GTValues.ZPM);
    public static ItemEntry<ComponentItem> ELECTRIC_MOTOR_UV = registerElectricMotor(GTValues.UV);
    public static ItemEntry<ComponentItem> ELECTRIC_MOTOR_UHV = GTCEuAPI.isHighTier() ?
            registerElectricMotor(GTValues.UHV) : null;
    public static ItemEntry<ComponentItem> ELECTRIC_MOTOR_UEV = GTCEuAPI.isHighTier() ?
            registerElectricMotor(GTValues.UEV) : null;
    public static ItemEntry<ComponentItem> ELECTRIC_MOTOR_UIV = GTCEuAPI.isHighTier() ?
            registerElectricMotor(GTValues.UIV) : null;
    public static ItemEntry<ComponentItem> ELECTRIC_MOTOR_UXV = GTCEuAPI.isHighTier() ?
            registerElectricMotor(GTValues.UXV) : null;
    public static ItemEntry<ComponentItem> ELECTRIC_MOTOR_OpV = GTCEuAPI.isHighTier() ?
            registerElectricMotor(GTValues.OpV) : null;

    private static ItemEntry<ComponentItem> registerElectricMotor(int tier) {
        var builder = REGISTRATE
                .item("%s_electric_motor".formatted(GTValues.VN[tier].toLowerCase()), ComponentItem::create)
                .lang("%s Electric Motor".formatted(GTValues.VN[tier]))
                .onRegister(attach(new TieredBehaviour(tier)));
        if (tier <= GTValues.UV) builder.tag(CustomTags.ELECTRIC_MOTORS);
        return builder.register();
    }

    @SuppressWarnings("unchecked")
    public static final ItemEntry<ComponentItem>[] ELECTRIC_MOTORS = (ItemEntry<ComponentItem>[]) new ItemEntry[] {
            null, ELECTRIC_MOTOR_LV, ELECTRIC_MOTOR_MV, ELECTRIC_MOTOR_HV, ELECTRIC_MOTOR_EV, ELECTRIC_MOTOR_IV,
            ELECTRIC_MOTOR_LuV,
            ELECTRIC_MOTOR_ZPM, ELECTRIC_MOTOR_UV, ELECTRIC_MOTOR_UHV, ELECTRIC_MOTOR_UEV, ELECTRIC_MOTOR_UIV,
            ELECTRIC_MOTOR_UXV, ELECTRIC_MOTOR_OpV, null
    };

    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_LV = registerElectricPump(GTValues.LV);
    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_MV = registerElectricPump(GTValues.MV);
    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_HV = registerElectricPump(GTValues.HV);
    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_EV = registerElectricPump(GTValues.EV);
    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_IV = registerElectricPump(GTValues.IV);
    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_LuV = registerElectricPump(GTValues.LuV);
    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_ZPM = registerElectricPump(GTValues.ZPM);
    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_UV = registerElectricPump(GTValues.UV);
    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_UHV = GTCEuAPI.isHighTier() ?
            registerElectricPump(GTValues.UHV) : null;
    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_UEV = GTCEuAPI.isHighTier() ?
            registerElectricPump(GTValues.UEV) : null;
    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_UIV = GTCEuAPI.isHighTier() ?
            registerElectricPump(GTValues.UIV) : null;
    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_UXV = GTCEuAPI.isHighTier() ?
            registerElectricPump(GTValues.UXV) : null;
    public static ItemEntry<ComponentItem> ELECTRIC_PUMP_OpV = GTCEuAPI.isHighTier() ?
            registerElectricPump(GTValues.OpV) : null;

    private static ItemEntry<ComponentItem> registerElectricPump(int tier) {
        var builder = REGISTRATE
                .item("%s_electric_pump".formatted(GTValues.VN[tier].toLowerCase()), ComponentItem::create)
                .lang("%s Electric Pump".formatted(GTValues.VN[tier]))
                .onRegister(attach(new TieredBehaviour(tier)))
                .onRegister(attach(new CoverPlaceBehavior(GTCovers.PUMPS[tier])))
                .onRegister(attach(new TooltipBehavior(lines -> {
                    lines.add(Component.translatable("item.gtceu.electric.pump.tooltip"));
                    lines.add(GTCommonComponents.fluidTransferRate(PumpCover.PUMP_SCALING.applyAsInt(tier)));
                })));
        if (tier <= GTValues.UV) builder.tag(CustomTags.ELECTRIC_PUMPS);
        return builder.register();
    }

    @SuppressWarnings("unchecked")
    public static final ItemEntry<ComponentItem>[] ELECTRIC_PUMPS = (ItemEntry<ComponentItem>[]) new ItemEntry[] {
            null, ELECTRIC_PUMP_LV, ELECTRIC_PUMP_MV, ELECTRIC_PUMP_HV, ELECTRIC_PUMP_EV, ELECTRIC_PUMP_IV,
            ELECTRIC_PUMP_LuV,
            ELECTRIC_PUMP_ZPM, ELECTRIC_PUMP_UV, ELECTRIC_PUMP_UHV, ELECTRIC_PUMP_UEV, ELECTRIC_PUMP_UIV,
            ELECTRIC_PUMP_UXV, ELECTRIC_PUMP_OpV, null
    };

    public static ItemEntry<ComponentItem> FLUID_REGULATOR_LV = registerFluidRegulator(GTValues.LV);
    public static ItemEntry<ComponentItem> FLUID_REGULATOR_MV = registerFluidRegulator(GTValues.MV);
    public static ItemEntry<ComponentItem> FLUID_REGULATOR_HV = registerFluidRegulator(GTValues.HV);
    public static ItemEntry<ComponentItem> FLUID_REGULATOR_EV = registerFluidRegulator(GTValues.EV);
    public static ItemEntry<ComponentItem> FLUID_REGULATOR_IV = registerFluidRegulator(GTValues.IV);
    public static ItemEntry<ComponentItem> FLUID_REGULATOR_LuV = registerFluidRegulator(GTValues.LuV);
    public static ItemEntry<ComponentItem> FLUID_REGULATOR_ZPM = registerFluidRegulator(GTValues.ZPM);
    public static ItemEntry<ComponentItem> FLUID_REGULATOR_UV = registerFluidRegulator(GTValues.UV);
    public static ItemEntry<ComponentItem> FLUID_REGULATOR_UHV = GTCEuAPI.isHighTier() ?
            registerFluidRegulator(GTValues.UHV) : null;
    public static ItemEntry<ComponentItem> FLUID_REGULATOR_UEV = GTCEuAPI.isHighTier() ?
            registerFluidRegulator(GTValues.UEV) : null;
    public static ItemEntry<ComponentItem> FLUID_REGULATOR_UIV = GTCEuAPI.isHighTier() ?
            registerFluidRegulator(GTValues.UIV) : null;
    public static ItemEntry<ComponentItem> FLUID_REGULATOR_UXV = GTCEuAPI.isHighTier() ?
            registerFluidRegulator(GTValues.UXV) : null;
    public static ItemEntry<ComponentItem> FLUID_REGULATOR_OpV = GTCEuAPI.isHighTier() ?
            registerFluidRegulator(GTValues.OpV) : null;

    private static ItemEntry<ComponentItem> registerFluidRegulator(int tier) {
        var builder = REGISTRATE
                .item("%s_fluid_regulator".formatted(GTValues.VN[tier].toLowerCase()), ComponentItem::create)
                .lang("%s Fluid Regulator".formatted(GTValues.VN[tier]))
                .onRegister(attach(new CoverPlaceBehavior(GTCovers.FLUID_REGULATORS[tier])))
                .onRegister(attach(new TooltipBehavior(lines -> {
                    lines.add(Component.translatable("item.gtceu.fluid.regulator.tooltip"));
                    lines.add(GTCommonComponents.fluidTransferRate(PumpCover.PUMP_SCALING.applyAsInt(tier)));
                })));
        if (tier <= GTValues.UV) builder.tag(CustomTags.FLUID_REGULATORS);
        return builder.register();
    }

    @SuppressWarnings("unchecked")
    public static final ItemEntry<ComponentItem>[] FLUID_REGULATORS = (ItemEntry<ComponentItem>[]) new ItemEntry[] {
            null, FLUID_REGULATOR_LV, FLUID_REGULATOR_MV, FLUID_REGULATOR_HV, FLUID_REGULATOR_EV, FLUID_REGULATOR_IV,
            FLUID_REGULATOR_LuV,
            FLUID_REGULATOR_ZPM, FLUID_REGULATOR_UV, FLUID_REGULATOR_UHV, FLUID_REGULATOR_UEV, FLUID_REGULATOR_UIV,
            FLUID_REGULATOR_UXV, FLUID_REGULATOR_OpV, null
    };

    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_LV = registerConveyorModule(GTValues.LV);
    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_MV = registerConveyorModule(GTValues.MV);
    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_HV = registerConveyorModule(GTValues.HV);
    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_EV = registerConveyorModule(GTValues.EV);
    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_IV = registerConveyorModule(GTValues.IV);
    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_LuV = registerConveyorModule(GTValues.LuV);
    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_ZPM = registerConveyorModule(GTValues.ZPM);
    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_UV = registerConveyorModule(GTValues.UV);
    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_UHV = GTCEuAPI.isHighTier() ?
            registerConveyorModule(GTValues.UHV) : null;
    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_UEV = GTCEuAPI.isHighTier() ?
            registerConveyorModule(GTValues.UEV) : null;
    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_UIV = GTCEuAPI.isHighTier() ?
            registerConveyorModule(GTValues.UIV) : null;
    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_UXV = GTCEuAPI.isHighTier() ?
            registerConveyorModule(GTValues.UXV) : null;
    public static ItemEntry<ComponentItem> CONVEYOR_MODULE_OpV = GTCEuAPI.isHighTier() ?
            registerConveyorModule(GTValues.OpV) : null;

    private static ItemEntry<ComponentItem> registerConveyorModule(int tier) {
        var builder = REGISTRATE
                .item("%s_conveyor_module".formatted(GTValues.VN[tier].toLowerCase()), ComponentItem::create)
                .lang("%s Conveyor Module".formatted(GTValues.VN[tier]))
                .onRegister(attach(new TieredBehaviour(tier)))
                .onRegister(attach(new CoverPlaceBehavior(GTCovers.CONVEYORS[tier])))
                .onRegister(attach(new TooltipBehavior(lines -> {
                    lines.add(Component.translatable("item.gtceu.conveyor.module.tooltip"));
                    lines.add(GTCommonComponents.itemTransferRate(ConveyorCover.CONVEYOR_SCALING.applyAsInt(tier)));
                })));
        if (tier <= GTValues.UV) builder.tag(CustomTags.CONVEYOR_MODULES);
        return builder.register();
    }

    @SuppressWarnings("unchecked")
    public static final ItemEntry<ComponentItem>[] CONVEYOR_MODULES = (ItemEntry<ComponentItem>[]) new ItemEntry[] {
            null, CONVEYOR_MODULE_LV, CONVEYOR_MODULE_MV, CONVEYOR_MODULE_HV, CONVEYOR_MODULE_EV, CONVEYOR_MODULE_IV,
            CONVEYOR_MODULE_LuV,
            CONVEYOR_MODULE_ZPM, CONVEYOR_MODULE_UV, CONVEYOR_MODULE_UHV, CONVEYOR_MODULE_UEV, CONVEYOR_MODULE_UIV,
            CONVEYOR_MODULE_UXV, CONVEYOR_MODULE_OpV, null
    };

    public static ItemEntry<ComponentItem> ELECTRIC_PISTON_LV = registerElectricPiston(GTValues.LV);
    public static ItemEntry<ComponentItem> ELECTRIC_PISTON_MV = registerElectricPiston(GTValues.MV);
    public static ItemEntry<ComponentItem> ELECTRIC_PISTON_HV = registerElectricPiston(GTValues.HV);
    public static ItemEntry<ComponentItem> ELECTRIC_PISTON_EV = registerElectricPiston(GTValues.EV);
    public static ItemEntry<ComponentItem> ELECTRIC_PISTON_IV = registerElectricPiston(GTValues.IV);
    public static ItemEntry<ComponentItem> ELECTRIC_PISTON_LuV = registerElectricPiston(GTValues.LuV);
    public static ItemEntry<ComponentItem> ELECTRIC_PISTON_ZPM = registerElectricPiston(GTValues.ZPM);
    public static ItemEntry<ComponentItem> ELECTRIC_PISTON_UV = registerElectricPiston(GTValues.UV);
    public static ItemEntry<ComponentItem> ELECTRIC_PISTON_UHV = GTCEuAPI.isHighTier() ?
            registerElectricPiston(GTValues.UHV) : null;
    public static ItemEntry<ComponentItem> ELECTRIC_PISTON_UEV = GTCEuAPI.isHighTier() ?
            registerElectricPiston(GTValues.UEV) : null;
    public static ItemEntry<ComponentItem> ELECTRIC_PISTON_UIV = GTCEuAPI.isHighTier() ?
            registerElectricPiston(GTValues.UIV) : null;
    public static ItemEntry<ComponentItem> ELECTRIC_PISTON_UXV = GTCEuAPI.isHighTier() ?
            registerElectricPiston(GTValues.UXV) : null;
    public static ItemEntry<ComponentItem> ELECTRIC_PISTON_OpV = GTCEuAPI.isHighTier() ?
            registerElectricPiston(GTValues.OpV) : null;

    private static ItemEntry<ComponentItem> registerElectricPiston(int tier) {
        var builder = REGISTRATE
                .item("%s_electric_piston".formatted(GTValues.VN[tier].toLowerCase()), ComponentItem::create)
                .lang("%s Electric Piston".formatted(GTValues.VN[tier]))
                .onRegister(attach(new TieredBehaviour(tier)));
        if (tier <= GTValues.UV) builder.tag(CustomTags.ELECTRIC_PISTONS);
        return builder.register();
    }

    @SuppressWarnings("unchecked")
    public static final ItemEntry<ComponentItem>[] ELECTRIC_PISTONS = (ItemEntry<ComponentItem>[]) new ItemEntry[] {
            null, ELECTRIC_PISTON_LV, ELECTRIC_PISTON_MV, ELECTRIC_PISTON_HV, ELECTRIC_PISTON_EV, ELECTRIC_PISTON_IV,
            ELECTRIC_PISTON_LuV,
            ELECTRIC_PISTON_ZPM, ELECTRIC_PISTON_UV, ELECTRIC_PISTON_UHV, ELECTRIC_PISTON_UEV, ELECTRIC_PISTON_UIV,
            ELECTRIC_PISTON_UXV, ELECTRIC_PISTON_OpV, null
    };

    public static ItemEntry<ComponentItem> ROBOT_ARM_LV = registerRobotArm(GTValues.LV);
    public static ItemEntry<ComponentItem> ROBOT_ARM_MV = registerRobotArm(GTValues.MV);
    public static ItemEntry<ComponentItem> ROBOT_ARM_HV = registerRobotArm(GTValues.HV);
    public static ItemEntry<ComponentItem> ROBOT_ARM_EV = registerRobotArm(GTValues.EV);
    public static ItemEntry<ComponentItem> ROBOT_ARM_IV = registerRobotArm(GTValues.IV);
    public static ItemEntry<ComponentItem> ROBOT_ARM_LuV = registerRobotArm(GTValues.LuV);
    public static ItemEntry<ComponentItem> ROBOT_ARM_ZPM = registerRobotArm(GTValues.ZPM);
    public static ItemEntry<ComponentItem> ROBOT_ARM_UV = registerRobotArm(GTValues.UV);
    public static ItemEntry<ComponentItem> ROBOT_ARM_UHV = GTCEuAPI.isHighTier() ? registerRobotArm(GTValues.UHV) :
            null;
    public static ItemEntry<ComponentItem> ROBOT_ARM_UEV = GTCEuAPI.isHighTier() ? registerRobotArm(GTValues.UEV) :
            null;
    public static ItemEntry<ComponentItem> ROBOT_ARM_UIV = GTCEuAPI.isHighTier() ? registerRobotArm(GTValues.UIV) :
            null;
    public static ItemEntry<ComponentItem> ROBOT_ARM_UXV = GTCEuAPI.isHighTier() ? registerRobotArm(GTValues.UXV) :
            null;
    public static ItemEntry<ComponentItem> ROBOT_ARM_OpV = GTCEuAPI.isHighTier() ? registerRobotArm(GTValues.OpV) :
            null;

    private static ItemEntry<ComponentItem> registerRobotArm(int tier) {
        var builder = REGISTRATE.item("%s_robot_arm".formatted(GTValues.VN[tier].toLowerCase()), ComponentItem::create)
                .lang("%s Robot Arm".formatted(tier))
                .onRegister(
                        attach(new CoverPlaceBehavior(GTCovers.ROBOT_ARMS[tier]), new TieredBehaviour(tier)))
                .onRegister(attach(new TooltipBehavior(lines -> {
                    lines.add(Component.translatable("item.gtceu.robot.arm.tooltip"));
                    lines.add(GTCommonComponents.itemTransferRate(ConveyorCover.CONVEYOR_SCALING.applyAsInt(tier)));
                })));
        if (tier <= GTValues.UV) builder.tag(CustomTags.ROBOT_ARMS);
        return builder.register();
    }

    @SuppressWarnings("unchecked")
    public static final ItemEntry<ComponentItem>[] ROBOT_ARMS = (ItemEntry<ComponentItem>[]) new ItemEntry[] {
            null, ROBOT_ARM_LV, ROBOT_ARM_MV, ROBOT_ARM_HV, ROBOT_ARM_EV, ROBOT_ARM_IV, ROBOT_ARM_LuV,
            ROBOT_ARM_ZPM, ROBOT_ARM_UV, ROBOT_ARM_UHV, ROBOT_ARM_UEV, ROBOT_ARM_UIV, ROBOT_ARM_UXV, ROBOT_ARM_OpV, null
    };

    public static ItemEntry<ComponentItem> FIELD_GENERATOR_LV = registerFieldGenerator(GTValues.LV);
    public static ItemEntry<ComponentItem> FIELD_GENERATOR_MV = registerFieldGenerator(GTValues.MV);
    public static ItemEntry<ComponentItem> FIELD_GENERATOR_HV = registerFieldGenerator(GTValues.HV);
    public static ItemEntry<ComponentItem> FIELD_GENERATOR_EV = registerFieldGenerator(GTValues.EV);
    public static ItemEntry<ComponentItem> FIELD_GENERATOR_IV = registerFieldGenerator(GTValues.IV);
    public static ItemEntry<ComponentItem> FIELD_GENERATOR_LuV = registerFieldGenerator(GTValues.LuV);
    public static ItemEntry<ComponentItem> FIELD_GENERATOR_ZPM = registerFieldGenerator(GTValues.ZPM);
    public static ItemEntry<ComponentItem> FIELD_GENERATOR_UV = registerFieldGenerator(GTValues.UV);
    public static ItemEntry<ComponentItem> FIELD_GENERATOR_UHV = GTCEuAPI.isHighTier() ?
            registerFieldGenerator(GTValues.UHV) : null;
    public static ItemEntry<ComponentItem> FIELD_GENERATOR_UEV = GTCEuAPI.isHighTier() ?
            registerFieldGenerator(GTValues.UEV) : null;
    public static ItemEntry<ComponentItem> FIELD_GENERATOR_UIV = GTCEuAPI.isHighTier() ?
            registerFieldGenerator(GTValues.UIV) : null;
    public static ItemEntry<ComponentItem> FIELD_GENERATOR_UXV = GTCEuAPI.isHighTier() ?
            registerFieldGenerator(GTValues.UXV) : null;
    public static ItemEntry<ComponentItem> FIELD_GENERATOR_OpV = GTCEuAPI.isHighTier() ?
            registerFieldGenerator(GTValues.OpV) : null;

    private static ItemEntry<ComponentItem> registerFieldGenerator(int tier) {
        var builder = REGISTRATE
                .item("%s_field_generator".formatted(GTValues.VN[tier].toLowerCase()), ComponentItem::create)
                .lang("%s Field Generator".formatted(GTValues.VN[tier]))
                .onRegister(attach(new TieredBehaviour(tier)));
        if (tier <= GTValues.UV) builder.tag(CustomTags.FIELD_GENERATORS);
        return builder.register();
    }

    @SuppressWarnings("unchecked")
    public static final ItemEntry<ComponentItem>[] FIELD_GENERATORS = (ItemEntry<ComponentItem>[]) new ItemEntry[] {
            null, FIELD_GENERATOR_LV, FIELD_GENERATOR_MV, FIELD_GENERATOR_HV, FIELD_GENERATOR_EV, FIELD_GENERATOR_IV,
            FIELD_GENERATOR_LuV,
            FIELD_GENERATOR_ZPM, FIELD_GENERATOR_UV, FIELD_GENERATOR_UHV, FIELD_GENERATOR_UEV, FIELD_GENERATOR_UIV,
            FIELD_GENERATOR_UXV, FIELD_GENERATOR_OpV, null
    };

    public static ItemEntry<ComponentItem> EMITTER_LV = registerEmitter(GTValues.LV);
    public static ItemEntry<ComponentItem> EMITTER_MV = registerEmitter(GTValues.MV);
    public static ItemEntry<ComponentItem> EMITTER_HV = registerEmitter(GTValues.HV);
    public static ItemEntry<ComponentItem> EMITTER_EV = registerEmitter(GTValues.EV);
    public static ItemEntry<ComponentItem> EMITTER_IV = registerEmitter(GTValues.IV);
    public static ItemEntry<ComponentItem> EMITTER_LuV = registerEmitter(GTValues.LuV);
    public static ItemEntry<ComponentItem> EMITTER_ZPM = registerEmitter(GTValues.ZPM);
    public static ItemEntry<ComponentItem> EMITTER_UV = registerEmitter(GTValues.UV);
    public static ItemEntry<ComponentItem> EMITTER_UHV = GTCEuAPI.isHighTier() ? registerEmitter(GTValues.UHV) : null;
    public static ItemEntry<ComponentItem> EMITTER_UEV = GTCEuAPI.isHighTier() ? registerEmitter(GTValues.UEV) : null;
    public static ItemEntry<ComponentItem> EMITTER_UIV = GTCEuAPI.isHighTier() ? registerEmitter(GTValues.UIV) : null;
    public static ItemEntry<ComponentItem> EMITTER_UXV = GTCEuAPI.isHighTier() ? registerEmitter(GTValues.UXV) : null;
    public static ItemEntry<ComponentItem> EMITTER_OpV = GTCEuAPI.isHighTier() ? registerEmitter(GTValues.OpV) : null;

    private static ItemEntry<ComponentItem> registerEmitter(int tier) {
        var builder = REGISTRATE.item("%s_emitter".formatted(GTValues.VN[tier].toLowerCase()), ComponentItem::create)
                .lang("%s Emitter".formatted(GTValues.VN[tier]))
                .onRegister(attach(new TieredBehaviour(tier)));
        if (tier <= GTValues.UV) builder.tag(CustomTags.EMITTERS);
        return builder.register();
    }

    @SuppressWarnings("unchecked")
    public static ItemEntry<ComponentItem>[] EMITTERS = (ItemEntry<ComponentItem>[]) new ItemEntry[] {
            null, EMITTER_LV, EMITTER_MV, EMITTER_HV, EMITTER_EV, EMITTER_IV, EMITTER_LuV,
            EMITTER_ZPM, EMITTER_UV, EMITTER_UHV, EMITTER_UEV, EMITTER_UIV, EMITTER_UXV, EMITTER_OpV, null
    };

    public static ItemEntry<ComponentItem> SENSOR_LV = registerSensor(GTValues.LV);
    public static ItemEntry<ComponentItem> SENSOR_MV = registerSensor(GTValues.MV);
    public static ItemEntry<ComponentItem> SENSOR_HV = registerSensor(GTValues.HV);
    public static ItemEntry<ComponentItem> SENSOR_EV = registerSensor(GTValues.EV);
    public static ItemEntry<ComponentItem> SENSOR_IV = registerSensor(GTValues.IV);
    public static ItemEntry<ComponentItem> SENSOR_LuV = registerSensor(GTValues.LuV);
    public static ItemEntry<ComponentItem> SENSOR_ZPM = registerSensor(GTValues.ZPM);
    public static ItemEntry<ComponentItem> SENSOR_UV = registerSensor(GTValues.UV);
    public static ItemEntry<ComponentItem> SENSOR_UHV = GTCEuAPI.isHighTier() ? registerSensor(GTValues.UHV) : null;
    public static ItemEntry<ComponentItem> SENSOR_UEV = GTCEuAPI.isHighTier() ? registerSensor(GTValues.UEV) : null;
    public static ItemEntry<ComponentItem> SENSOR_UIV = GTCEuAPI.isHighTier() ? registerSensor(GTValues.UIV) : null;
    public static ItemEntry<ComponentItem> SENSOR_UXV = GTCEuAPI.isHighTier() ? registerSensor(GTValues.UXV) : null;
    public static ItemEntry<ComponentItem> SENSOR_OpV = GTCEuAPI.isHighTier() ? registerSensor(GTValues.OpV) : null;

    private static ItemEntry<ComponentItem> registerSensor(int tier) {
        var builder = REGISTRATE.item("%s_sensor".formatted(GTValues.VN[tier].toLowerCase()), ComponentItem::create)
                .lang("%s Sensor".formatted(GTValues.VN[tier]))
                .onRegister(attach(new TieredBehaviour(tier)));
        if (tier <= GTValues.UV) builder.tag(CustomTags.SENSORS);
        return builder.register();
    }

    @SuppressWarnings("unchecked")
    public static final ItemEntry<ComponentItem>[] SENSORS = (ItemEntry<ComponentItem>[]) new ItemEntry[] {
            null, SENSOR_LV, SENSOR_MV, SENSOR_HV, SENSOR_EV, SENSOR_IV, SENSOR_LuV,
            SENSOR_ZPM, SENSOR_UV, SENSOR_UHV, SENSOR_UEV, SENSOR_UIV, SENSOR_UXV, SENSOR_OpV, null
    };

    public static ItemEntry<ComponentItem> DYNAMITE = REGISTRATE.item("dynamite", ComponentItem::create)
            .lang("Dynamite")
            .onRegister(attach(new DynamiteBehaviour()))
            .tab(TOOL.getKey())
            .register();

    public static ItemEntry<ComponentItem> TOOL_DATA_STICK = REGISTRATE.item("data_stick", ComponentItem::create)
            .transform(nameAndTooltip("Data Stick", "A Low Capacity Data Storage"))
            .onRegister(attach(new DataItemBehavior(false, 8)))
            .register();

    public static ItemEntry<ComponentItem> TOOL_DATA_ORB = REGISTRATE.item("data_orb", ComponentItem::create)
            .transform(nameAndTooltip("Data Orb", "A High Capacity Data Storage"))
            .lang("Data Orb")
            .onRegister(attach(new DataItemBehavior(false, 64)))
            .register();

    public static ItemEntry<ComponentItem> TOOL_DATA_MODULE = REGISTRATE.item("data_module", ComponentItem::create)
            .lang("Data Module")
            .onRegister(attach(new DataItemBehavior(true, 256)))
            .register();

    public static final Map<DyeColor, ItemEntry<Item>> GLASS_LENSES = new HashMap<>();

    static {

        for (DyeColor color : DyeColor.values()) {
            if (color == DyeColor.WHITE) continue;
            GLASS_LENSES.put(color,
                    REGISTRATE.item(String.format("%s_glass_lens", color.getName()), Item::new)
                            .lang("Glass Lens (%s)".formatted(toEnglishName(color.getName())))
                            .tag(CustomTags.LENSES, CustomTags.GLASS_LENS)
                            .tag(TagUtil.createItemTag("lenses/" + color.getName()))
                            .register());
        }
    }

    public static ItemEntry<ComponentItem> SILICON_BOULE = REGISTRATE.item("silicon_boule", ComponentItem::new)
            .transform(nameAndTooltip("Monocrystalline Silicon Boule", "Raw Circuit"))
            .register();
    public static ItemEntry<ComponentItem> PHOSPHORUS_BOULE = REGISTRATE.item("phosphorus_boule", ComponentItem::new)
            .transform(nameAndTooltip("Phosphorus-doped Monocrystalline Silicon Boule", "Raw Circuit"))
            .register();
    public static ItemEntry<ComponentItem> NAQUADAH_BOULE = REGISTRATE.item("naquadah_boule", ComponentItem::new)
            .transform(nameAndTooltip("Naquadah-doped Monocrystalline Silicon Boule", "Raw Circuit"))
            .register();
    public static ItemEntry<ComponentItem> NEUTRONIUM_BOULE = REGISTRATE.item("neutronium_boule", ComponentItem::new)
            .transform(nameAndTooltip("Neutronium-doped Monocrystalline Silicon Boule", "Raw Circuit"))
            .register();

    public static ItemEntry<ComponentItem> SILICON_WAFER = REGISTRATE.item("silicon_wafer", ComponentItem::new)
            .transform(nameAndTooltip("Silicon Wafer", "Raw Circuit"))
            .register();
    public static ItemEntry<ComponentItem> PHOSPHORUS_WAFER = REGISTRATE.item("phosphorus_wafer", ComponentItem::new)
            .transform(nameAndTooltip("Phosphorus-doped Wafer", "Raw Circuit"))
            .register();
    public static ItemEntry<ComponentItem> NAQUADAH_WAFER = REGISTRATE.item("naquadah_wafer", ComponentItem::new)
            .transform(nameAndTooltip("Naquadah-doped Wafer", "Raw Circuit"))
            .register();
    public static ItemEntry<ComponentItem> NEUTRONIUM_WAFER = REGISTRATE.item("neutronium_wafer", ComponentItem::new)
            .transform(nameAndTooltip("Neutronium-doped Wafer", "Raw Circuit"))
            .register();

    public static ItemEntry<ComponentItem> CENTRAL_PROCESSING_UNIT_WAFER = REGISTRATE.item("cpu_wafer", ComponentItem::new)
            .transform(nameAndTooltip("CPU Wafer", "Raw Processing Unit"))
            .register();
    public static ItemEntry<ComponentItem> RANDOM_ACCESS_MEMORY_WAFER = REGISTRATE.item("ram_wafer", ComponentItem::new)
            .transform(nameAndTooltip("RAM Wafer", "Raw Memory"))
            .register();
    public static ItemEntry<ComponentItem> INTEGRATED_LOGIC_CIRCUIT_WAFER = REGISTRATE.item("ilc_wafer", ComponentItem::new)
            .transform(nameAndTooltip("ILC Wafer", "Raw Integrated Circuit"))
            .register();
    public static ItemEntry<ComponentItem> NANO_CENTRAL_PROCESSING_UNIT_WAFER = REGISTRATE.item("nano_cpu_wafer", ComponentItem::new)
            .transform(nameAndTooltip("Nano CPU Wafer", "Raw Nano Circuit"))
            .register();
    public static ItemEntry<ComponentItem> QUBIT_CENTRAL_PROCESSING_UNIT_WAFER = REGISTRATE.item("qbit_cpu_wafer", ComponentItem::new)
            .transform(nameAndTooltip("Qubit CPU Wafer", "Raw Qubit Circuit"))
            .register();
    public static ItemEntry<ComponentItem> SIMPLE_SYSTEM_ON_CHIP_WAFER = REGISTRATE.item("simple_soc_wafer", ComponentItem::new)
            .transform(nameAndTooltip("Simple SoC Wafer", "Raw Simple Circuit"))
            .register();
    public static ItemEntry<ComponentItem> SYSTEM_ON_CHIP_WAFER = REGISTRATE.item("soc_wafer", ComponentItem::new)
            .transform(nameAndTooltip("SoC Wafer", "Raw Basic Circuit"))
            .register();
    public static ItemEntry<ComponentItem> ADVANCED_SYSTEM_ON_CHIP_WAFER = REGISTRATE.item("advanced_soc_wafer", ComponentItem::new)
            .transform(nameAndTooltip("ASoC Wafer", "Raw Advanced Circuit"))
            .register();
    public static ItemEntry<ComponentItem> HIGHLY_ADVANCED_SOC_WAFER = REGISTRATE.item("highly_advanced_soc_wafer", ComponentItem::new)
            .transform(nameAndTooltip("HASoC Wafer", "Raw Highly Advanced Circuit"))
            .register();
    public static ItemEntry<ComponentItem> NAND_MEMORY_CHIP_WAFER = REGISTRATE.item("nand_memory_wafer", ComponentItem::new)
            .transform(nameAndTooltip("NAND Memory Wafer", "Raw Logic Gate"))
            .register();
    public static ItemEntry<ComponentItem> NOR_MEMORY_CHIP_WAFER = REGISTRATE.item("nor_memory_wafer", ComponentItem::new)
            .transform(nameAndTooltip("NOR Memory Wafer", "Raw Logic Gate"))
            .register();
    public static ItemEntry<ComponentItem> ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER = REGISTRATE.item("ulpic_wafer", ComponentItem::new)
            .transform(nameAndTooltip("ULPIC Wafer", "Raw Ultra Low Power Circuit"))
            .register();
    public static ItemEntry<ComponentItem> LOW_POWER_INTEGRATED_CIRCUIT_WAFER = REGISTRATE.item("lpic_wafer", ComponentItem::new)
            .transform(nameAndTooltip("LPIC Wafer", "Raw Low Power Circuit"))
            .register();
    public static ItemEntry<ComponentItem> POWER_INTEGRATED_CIRCUIT_WAFER = REGISTRATE.item("mpic_wafer", ComponentItem::new)
            .transform(nameAndTooltip("MPIC Wafer", "Raw Power Circuit"))
            .register();
    public static ItemEntry<ComponentItem> HIGH_POWER_INTEGRATED_CIRCUIT_WAFER = REGISTRATE.item("hpic_wafer", ComponentItem::new)
            .transform(nameAndTooltip("HPIC Wafer", "Raw High Power Circuit"))
            .register();
    public static ItemEntry<ComponentItem> ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT_WAFER = REGISTRATE.item("uhpic_wafer", ComponentItem::new)
            .transform(nameAndTooltip("UHPIC Wafer", "Raw Ultra High Power Circuit"))
            .register();

    public static ItemEntry<ComponentItem> ENGRAVED_CRYSTAL_CHIP = REGISTRATE.item("engraved_crystal_chip", ComponentItem::new)
            .transform(nameAndTooltip("Engraved Crystal Chip", "Needed for Circuits"))
            .register();
    public static ItemEntry<Item> ENGRAVED_LAPOTRON_CHIP = REGISTRATE.item("engraved_lapotron_crystal_chip", Item::new)
            .lang("Engraved Lapotron Crystal Chip")
            .register();

    public static ItemEntry<ComponentItem> CENTRAL_PROCESSING_UNIT = REGISTRATE.item("cpu_chip", ComponentItem::new)
            .transform(nameAndTooltip("CPU Chip", "Central Processing Unit"))
            .register();

    public static ItemEntry<ComponentItem> RANDOM_ACCESS_MEMORY = REGISTRATE.item("ram_chip", ComponentItem::new)
            .transform(nameAndTooltip("RAM Chip", "Random Access Memory"))
            .register();

    public static ItemEntry<ComponentItem> INTEGRATED_LOGIC_CIRCUIT = REGISTRATE.item("ilc_chip", ComponentItem::new)
            .transform(nameAndTooltip("IC Chip", "Integrated Logic Circuit"))
            .register();

    public static ItemEntry<ComponentItem> NANO_CENTRAL_PROCESSING_UNIT = REGISTRATE.item("nano_cpu_chip", ComponentItem::new)
            .transform(nameAndTooltip("Nano CPU Chip", "Nano Central Processing Unit"))
            .register();

    public static ItemEntry<ComponentItem> QUBIT_CENTRAL_PROCESSING_UNIT = REGISTRATE.item("qbit_cpu_chip", ComponentItem::new)
            .transform(nameAndTooltip("Qubit CPU Chip", "Qubit Central Processing Unit"))
            .register();

    public static ItemEntry<ComponentItem> SIMPLE_SYSTEM_ON_CHIP = REGISTRATE.item("simple_soc", ComponentItem::new)
            .transform(nameAndTooltip("Simple SoC", "Simple System on Chip"))
            .register();

    public static ItemEntry<ComponentItem> SYSTEM_ON_CHIP = REGISTRATE.item("soc", ComponentItem::new)
            .transform(nameAndTooltip("SoC", "System on Chip"))
            .register();

    public static ItemEntry<ComponentItem> ADVANCED_SYSTEM_ON_CHIP = REGISTRATE.item("advanced_soc", ComponentItem::new)
            .transform(nameAndTooltip("ASoC", "Advanced System on Chip"))
            .register();

    public static ItemEntry<ComponentItem> HIGHLY_ADVANCED_SOC = REGISTRATE.item("highly_advanced_soc", ComponentItem::new)
            .transform(nameAndTooltip("HASoC", "Highly Advanced System on Chip"))
            .register();

    public static ItemEntry<ComponentItem> NAND_MEMORY_CHIP = REGISTRATE.item("nand_memory_chip", ComponentItem::new)
            .transform(nameAndTooltip("NAND Memory Chip", "NAND Logic Gate"))
            .register();

    public static ItemEntry<ComponentItem> NOR_MEMORY_CHIP = REGISTRATE.item("nor_memory_chip", ComponentItem::new)
            .transform(nameAndTooltip("NOR Memory Chip", "NOR Logic Gate"))
            .register();

    public static ItemEntry<ComponentItem> ULTRA_LOW_POWER_INTEGRATED_CIRCUIT = REGISTRATE.item("ulpic_chip", ComponentItem::new)
            .lang("ULPIC Chip")
            .transform(nameAndTooltip("ULPIC Chip", "Ultra Low Power IC"))
            .register();

    public static ItemEntry<ComponentItem> LOW_POWER_INTEGRATED_CIRCUIT = REGISTRATE.item("lpic_chip", ComponentItem::new)
            .transform(nameAndTooltip("LPIC Chip", "Low Power IC"))
            .register();

    public static ItemEntry<ComponentItem> POWER_INTEGRATED_CIRCUIT = REGISTRATE.item("mpic_chip", ComponentItem::new)
            .transform(nameAndTooltip("MPIC Chip", "Power IC"))
            .register();

    public static ItemEntry<ComponentItem> HIGH_POWER_INTEGRATED_CIRCUIT = REGISTRATE.item("hpic_chip", ComponentItem::new)
            .transform(nameAndTooltip("HPIC Chip", "High Power IC"))
            .register();

    public static ItemEntry<ComponentItem> ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT = REGISTRATE.item("uhpic_chip", ComponentItem::new)
            .transform(nameAndTooltip("UHPIC Chip", "Ultra High Power IC"))
            .register();

    public static ItemEntry<ComponentItem> RAW_CRYSTAL_CHIP = REGISTRATE.item("raw_crystal_chip", ComponentItem::new)
            .lang("Raw Crystal Chip")
            .transform(nameAndTooltip("Raw Crystal Chip", "Raw Crystal Processor"))
            .register();
    public static ItemEntry<ComponentItem> RAW_CRYSTAL_CHIP_PART = REGISTRATE.item("raw_crystal_chip_parts", ComponentItem::new)
            .transform(nameAndTooltip("Raw Crystal Chip Parts", "Raw Crystal Processor Parts"))
            .register();
    public static ItemEntry<ComponentItem> CRYSTAL_CENTRAL_PROCESSING_UNIT = REGISTRATE.item("crystal_cpu", ComponentItem::new)
            .transform(nameAndTooltip("Crystal CPU", "Crystal Processing Unit"))
            .register();
    public static ItemEntry<ComponentItem> CRYSTAL_SYSTEM_ON_CHIP = REGISTRATE.item("crystal_soc", ComponentItem::new)
            .transform(nameAndTooltip("Crystal SoC", "Crystal System on Chip"))
            .register();

    public static ItemEntry<ComponentItem> COATED_BOARD = REGISTRATE.item("resin_circuit_board", ComponentItem::new)
            .transform(nameAndTooltip("Resin Circuit Board", "A Coated Board"))
            .register();
    public static ItemEntry<ComponentItem> PHENOLIC_BOARD = REGISTRATE.item("phenolic_circuit_board", ComponentItem::new)
            .transform(nameAndTooltip("Phenolic Circuit Board", "A Good Board"))
            .register();
    public static ItemEntry<ComponentItem> PLASTIC_BOARD = REGISTRATE.item("plastic_circuit_board", ComponentItem::new)
            .transform(nameAndTooltip("Plastic Circuit Board", "A Good Board"))
            .register();
    public static ItemEntry<ComponentItem> EPOXY_BOARD = REGISTRATE.item("epoxy_circuit_board", ComponentItem::new)
            .transform(nameAndTooltip("Epoxy Circuit Board", "An Advanced Board"))
            .register();
    public static ItemEntry<ComponentItem> FIBER_BOARD = REGISTRATE.item("fiber_reinforced_circuit_board", ComponentItem::new)
            .transform(nameAndTooltip("Fiber-Reinforced Circuit Board", "An Extreme Board"))
            .register();
    public static ItemEntry<ComponentItem> MULTILAYER_FIBER_BOARD = REGISTRATE
            .item("multilayer_fiber_reinforced_circuit_board", ComponentItem::new)
            .transform(nameAndTooltip("Multi-layer Fiber-Reinforced Circuit Board", "An Elite Board"))
            .register();
    public static ItemEntry<ComponentItem> WETWARE_BOARD = REGISTRATE.item("wetware_circuit_board", ComponentItem::new)
            .transform(nameAndTooltip("Wetware Circuit Board", "The Board that keeps life"))
            .register();

    public static ItemEntry<ComponentItem> BASIC_CIRCUIT_BOARD = REGISTRATE.item("resin_printed_circuit_board", ComponentItem::new)
            .transform(nameAndTooltip("Resin Printed Circuit Board", "A Basic Circuit Board"))
            .register();

    public static ItemEntry<ComponentItem> GOOD_CIRCUIT_BOARD = REGISTRATE.item("phenolic_printed_circuit_board", ComponentItem::new)
            .lang("Phenolic Printed Circuit Board")
            .transform(nameAndTooltip("Phenolic Printed Circuit Board", "A Good Circuit Board"))
            .register();

    public static ItemEntry<ComponentItem> PLASTIC_CIRCUIT_BOARD = REGISTRATE.item("plastic_printed_circuit_board", ComponentItem::new)
            .transform(nameAndTooltip("Plastic Printed Circuit Board", "A Good Circuit Board"))
            .register();

    public static ItemEntry<ComponentItem> ADVANCED_CIRCUIT_BOARD = REGISTRATE.item("epoxy_printed_circuit_board", ComponentItem::new)
            .transform(nameAndTooltip("Epoxy Printed Circuit Board", "An Advanced Circuit Board"))
            .register();

    public static ItemEntry<ComponentItem> EXTREME_CIRCUIT_BOARD = REGISTRATE
            .item("fiber_reinforced_printed_circuit_board", ComponentItem::new)
            .transform(nameAndTooltip("Fiber-Reinforced Printed Circuit Board", "A More Advanced Circuit Board"))
            .register();

    public static ItemEntry<ComponentItem> ELITE_CIRCUIT_BOARD = REGISTRATE
            .item("multilayer_fiber_reinforced_printed_circuit_board", ComponentItem::new)
            .transform(nameAndTooltip("Multi-layer Fiber-Reinforced Printed Circuit Board",
                            "An Elite Circuit Board"))
            .register();

    public static ItemEntry<ComponentItem> WETWARE_CIRCUIT_BOARD = REGISTRATE.item("wetware_printed_circuit_board", ComponentItem::new)
            .transform(nameAndTooltip("Wetware Printed Circuit Board", "The Board that keeps life"))
            .register();

    public static ItemEntry<ComponentItem> VACUUM_TUBE = REGISTRATE.item("vacuum_tube", ComponentItem::new)
            .transform(nameAndTooltip("Vacuum Tube", "Technically a Diode", "§cULV-Tier"))
            .tag(CustomTags.ULV_CIRCUITS)
            .register();

    public static ItemEntry<ComponentItem> GLASS_TUBE = REGISTRATE.item("glass_tube", ComponentItem::new)
            .lang("Glass Tube")
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Glass, GTValues.M))))
            .register();

    public static ItemEntry<ComponentItem> TRANSISTOR = REGISTRATE.item("transistor", ComponentItem::new)
            .transform(nameAndTooltip("Transistor", "Basic Electronic Component"))
            .tag(CustomTags.TRANSISTORS)
            .register();

    public static ItemEntry<ComponentItem> RESISTOR = REGISTRATE.item("resistor", ComponentItem::new)
            .transform(nameAndTooltip("Resistor", "Basic Electronic Component"))
            .tag(CustomTags.RESISTORS)
            .register();

    public static ItemEntry<ComponentItem> CAPACITOR = REGISTRATE.item("capacitor", ComponentItem::new)
            .transform(nameAndTooltip("Capacitor", "Basic Electronic Component"))
            .tag(CustomTags.CAPACITORS)
            .register();

    public static ItemEntry<ComponentItem> DIODE = REGISTRATE.item("diode", ComponentItem::new)
            .transform(nameAndTooltip("Diode", "Basic Electronic Component"))
            .tag(CustomTags.DIODES)
            .register();

    public static ItemEntry<ComponentItem> INDUCTOR = REGISTRATE.item("inductor", ComponentItem::new)
            .transform(nameAndTooltip("Inductor", "A Small Coil"))
            .tag(CustomTags.INDUCTORS)
            .register();

    public static ItemEntry<ComponentItem> SMD_TRANSISTOR = REGISTRATE.item("smd_transistor", ComponentItem::new)
            .transform(nameAndTooltip("SMD Transistor", "Electronic Component"))
            .tag(CustomTags.TRANSISTORS)
            .register();

    public static ItemEntry<ComponentItem> SMD_RESISTOR = REGISTRATE.item("smd_resistor", ComponentItem::new)
            .transform(nameAndTooltip("SMD Resistor", "Electronic Component"))
            .tag(CustomTags.RESISTORS)
            .register();

    public static ItemEntry<ComponentItem> SMD_CAPACITOR = REGISTRATE.item("smd_capacitor", ComponentItem::new)
            .transform(nameAndTooltip("SMD Capacitor", "Electronic Component"))
            .tag(CustomTags.CAPACITORS)
            .register();

    public static ItemEntry<ComponentItem> SMD_DIODE = REGISTRATE.item("smd_diode", ComponentItem::new)
            .transform(nameAndTooltip("SMD Diode", "Electronic Component"))
            .tag(CustomTags.DIODES)
            .register();

    public static ItemEntry<ComponentItem> SMD_INDUCTOR = REGISTRATE.item("smd_inductor", ComponentItem::new)
            .transform(nameAndTooltip("SMD Inductor", "Electronic Component"))
            .tag(CustomTags.INDUCTORS)
            .register();

    public static ItemEntry<ComponentItem> ADVANCED_SMD_TRANSISTOR = REGISTRATE.item("advanced_smd_transistor", ComponentItem::new)
            .transform(nameAndTooltip("Advanced SMD Transistor", "Advanced Electronic Component"))
            .register();

    public static ItemEntry<ComponentItem> ADVANCED_SMD_RESISTOR = REGISTRATE.item("advanced_smd_resistor", ComponentItem::new)
            .transform(nameAndTooltip("Advanced SMD Resistor", "Advanced Electronic Component"))
            .register();

    public static ItemEntry<ComponentItem> ADVANCED_SMD_CAPACITOR = REGISTRATE.item("advanced_smd_capacitor", ComponentItem::new)
            .transform(nameAndTooltip("Advanced SMD Capacitor", "Advanced Electronic Component"))
            .register();

    public static ItemEntry<ComponentItem> ADVANCED_SMD_DIODE = REGISTRATE.item("advanced_smd_diode", ComponentItem::new)
            .transform(nameAndTooltip("Advanced SMD Diode", "Advanced Electronic Component"))
            .register();

    public static ItemEntry<ComponentItem> ADVANCED_SMD_INDUCTOR = REGISTRATE.item("advanced_smd_inductor", ComponentItem::new)
            .transform(nameAndTooltip("Advanced SMD Inductor", "Advanced Electronic Component"))
            .register();

    // T1: Electronic
    public static ItemEntry<ComponentItem> ELECTRONIC_CIRCUIT_LV = REGISTRATE.item("basic_electronic_circuit", ComponentItem::new)
            .transform(nameAndTooltip("Basic Electronic Circuit", "Your First Circuit", "§cLV-Tier Circuit"))
            .tag(CustomTags.LV_CIRCUITS)
            .register();
    public static ItemEntry<ComponentItem> ELECTRONIC_CIRCUIT_MV = REGISTRATE.item("good_electronic_circuit", ComponentItem::new)
            .transform(nameAndTooltip("Good Electronic Circuit", "Your Second Circuit", "§cMV-Tier Circuit"))
            .tag(CustomTags.MV_CIRCUITS)
            .register();

    // T2: Integrated
    public static ItemEntry<ComponentItem> INTEGRATED_CIRCUIT_LV = REGISTRATE.item("basic_integrated_circuit", ComponentItem::new)
            .transform(nameAndTooltip("Basic Integrated Circuit",
                            "Smaller and more powerful", "§6LV-Tier Circuit"))
            .tag(CustomTags.LV_CIRCUITS)
            .register();
    public static ItemEntry<ComponentItem> INTEGRATED_CIRCUIT_MV = REGISTRATE.item("good_integrated_circuit", ComponentItem::new)
            .transform(nameAndTooltip("Good Integrated Circuit",
                            "Smaller and more powerful", "§6MV-Tier Circuit"))
            .tag(CustomTags.MV_CIRCUITS)
            .register();
    public static ItemEntry<ComponentItem> INTEGRATED_CIRCUIT_HV = REGISTRATE.item("advanced_integrated_circuit", ComponentItem::new)
            .transform(nameAndTooltip("Advanced Integrated Circuit",
                            "Smaller and more powerful", "§6HV-Tier Circuit"))
            .tag(CustomTags.HV_CIRCUITS)
            .register();

    // ULV/LV easier circuits
    public static ItemEntry<ComponentItem> NAND_CHIP_ULV = REGISTRATE.item("nand_chip", ComponentItem::new)
            .transform(nameAndTooltip("NAND Chip", "A Superior Simple Circuit", "§6ULV-Tier Circuit"))
            .tag(CustomTags.ULV_CIRCUITS).register();
    public static ItemEntry<ComponentItem> MICROPROCESSOR_LV = REGISTRATE.item("microchip_processor", ComponentItem::new)
            .transform(nameAndTooltip("Microchip Processor", "A Superior Basic Circuit", "§eLV-Tier Circuit"))
            .tag(CustomTags.LV_CIRCUITS)
            .register();

    // T3: Processor
    public static ItemEntry<ComponentItem> PROCESSOR_MV = REGISTRATE.item("micro_processor", ComponentItem::new)
            .transform(nameAndTooltip("Microprocessor", "Amazing Computation Speed!", "§eMV-Tier Circuit"))
            .tag(CustomTags.MV_CIRCUITS)
            .register();

    public static ItemEntry<ComponentItem> PROCESSOR_ASSEMBLY_HV = REGISTRATE.item("micro_processor_assembly", ComponentItem::new)
            .transform(nameAndTooltip("Microprocessor Assembly",
                            "Amazing Computation Speed!", "§eHV-Tier Circuit"))
            .tag(CustomTags.HV_CIRCUITS)
            .register();

    public static ItemEntry<ComponentItem> WORKSTATION_EV = REGISTRATE.item("micro_processor_computer", ComponentItem::new)
            .transform(nameAndTooltip("Microprocessor Supercomputer",
                            "Amazing Computation Speed!", "§eEV-Tier Circuit"))
            .tag(CustomTags.EV_CIRCUITS)
            .register();

    public static ItemEntry<ComponentItem> MAINFRAME_IV = REGISTRATE.item("micro_processor_mainframe", ComponentItem::new)
            .transform(nameAndTooltip("Microprocessor Mainframe",
                            "Amazing Computation Speed!", "§eIV-Tier Circuit"))
            .tag(CustomTags.IV_CIRCUITS)
            .register();

    // T4: Nano
    public static ItemEntry<ComponentItem> NANO_PROCESSOR_HV = REGISTRATE.item("nano_processor", ComponentItem::new)
            .transform(nameAndTooltip("Nanoprocessor", "Smaller than ever", "§bHV-Tier Circuit"))
            .tag(CustomTags.HV_CIRCUITS)
            .register();

    public static ItemEntry<ComponentItem> NANO_PROCESSOR_ASSEMBLY_EV = REGISTRATE.item("nano_processor_assembly", ComponentItem::new)
            .transform(nameAndTooltip("Nanoprocessor Assembly", "Smaller than ever", "§bEV-Tier Circuit"))
            .tag(CustomTags.EV_CIRCUITS)
            .register();
    public static ItemEntry<ComponentItem> NANO_COMPUTER_IV = REGISTRATE.item("nano_processor_computer", ComponentItem::new)
            .transform(nameAndTooltip("Nanoprocessor Supercomputer",
                            "Smaller than ever", "§bIV-Tier Circuit"))
            .tag(CustomTags.IV_CIRCUITS)
            .register();
    public static ItemEntry<ComponentItem> NANO_MAINFRAME_LuV = REGISTRATE.item("nano_processor_mainframe", ComponentItem::new)
            .transform(nameAndTooltip("Nanoprocessor Mainframe", "Smaller than ever", "§bLuV-Tier Circuit"))
            .tag(CustomTags.LuV_CIRCUITS)
            .register();

    // T5: Quantum
    public static ItemEntry<ComponentItem> QUANTUM_PROCESSOR_EV = REGISTRATE.item("quantum_processor", ComponentItem::new)
            .transform(nameAndTooltip("Quantum Processor",
                            "Quantum Computing comes to life!", "§aEV-Tier Circuit"))
            .tag(CustomTags.EV_CIRCUITS)
            .register();
    public static ItemEntry<ComponentItem> QUANTUM_ASSEMBLY_IV = REGISTRATE.item("quantum_processor_assembly", ComponentItem::new)
            .transform(nameAndTooltip("Quantum Processor Assembly",
                            "Quantum Computing comes to life!", "§aIV-Tier Circuit"))
            .tag(CustomTags.IV_CIRCUITS)
            .register();
    public static ItemEntry<ComponentItem> QUANTUM_COMPUTER_LuV = REGISTRATE.item("quantum_processor_computer", ComponentItem::new)
            .transform(nameAndTooltip("Quantum Processor Supercomputer",
                            "Quantum Computing comes to life!", "§aLuV-Tier Circuit"))
            .tag(CustomTags.LuV_CIRCUITS)
            .register();
    public static ItemEntry<ComponentItem> QUANTUM_MAINFRAME_ZPM = REGISTRATE.item("quantum_processor_mainframe", ComponentItem::new)
            .transform(nameAndTooltip("Quantum Processor Mainframe",
                            "Quantum Computing comes to life!", "§aZPM-Tier Circuit"))
            .tag(CustomTags.ZPM_CIRCUITS)
            .register();

    // T6: Crystal
    public static ItemEntry<ComponentItem> CRYSTAL_PROCESSOR_IV = REGISTRATE.item("crystal_processor", ComponentItem::new)
            .transform(nameAndTooltip("Crystal Processor",
                            "Taking Advantage of Crystal Engraving", "§9IV-Tier Circuit"))
            .tag(CustomTags.IV_CIRCUITS)
            .register();
    public static ItemEntry<ComponentItem> CRYSTAL_ASSEMBLY_LuV = REGISTRATE.item("crystal_processor_assembly", ComponentItem::new)
            .transform(nameAndTooltip("Crystal Processor Assembly",
                            "Taking Advantage of Crystal Engraving", "§9LuV-Tier Circuit"))
            .tag(CustomTags.LuV_CIRCUITS)
            .register();
    public static ItemEntry<ComponentItem> CRYSTAL_COMPUTER_ZPM = REGISTRATE.item("crystal_processor_computer", ComponentItem::new)
            .transform(nameAndTooltip("Crystal Processor Supercomputer",
                            "Taking Advantage of Crystal Engraving", "§9ZPM-Tier Circuit"))
            .tag(CustomTags.ZPM_CIRCUITS)
            .register();
    public static ItemEntry<ComponentItem> CRYSTAL_MAINFRAME_UV = REGISTRATE.item("crystal_processor_mainframe", ComponentItem::new)
            .transform(nameAndTooltip("Crystal Processor Mainframe",
                            "Taking Advantage of Crystal Engraving", "§9UV-Tier Circuit"))
            .tag(CustomTags.UV_CIRCUITS)
            .register();

    // T7: Wetware
    public static ItemEntry<ComponentItem> WETWARE_PROCESSOR_LuV = REGISTRATE.item("wetware_processor", ComponentItem::new)
            .transform(nameAndTooltip("Wetware Processor",
                            "You have a feeling like it's watching you", "§4LuV-Tier Circuit"))
            .tag(CustomTags.LuV_CIRCUITS)
            .register();
    public static ItemEntry<ComponentItem> WETWARE_PROCESSOR_ASSEMBLY_ZPM = REGISTRATE
            .item("wetware_processor_assembly", ComponentItem::new)
            .transform(nameAndTooltip("Wetware Processor Assembly",
                            "Can run Minecraft", "§4ZPM-Tier Circuit"))
            .tag(CustomTags.ZPM_CIRCUITS).register();
    public static ItemEntry<ComponentItem> WETWARE_SUPER_COMPUTER_UV = REGISTRATE.item("wetware_processor_computer", ComponentItem::new)
            .transform(nameAndTooltip("Wetware Processor Supercomputer",
                            "Ultimate fusion of Flesh and Machine", "§4UV-Tier Circuit"))
            .tag(CustomTags.UV_CIRCUITS)
            .register();
    public static ItemEntry<ComponentItem> WETWARE_MAINFRAME_UHV = REGISTRATE.item("wetware_processor_mainframe", ComponentItem::new)
            .transform(nameAndTooltip("Wetware Processor Mainframe",
                            "The best Man has ever seen", "§4UHV-Tier Circuit"))
            .tag(CustomTags.UHV_CIRCUITS)
            .register();

    public static ItemEntry<Item> COMPONENT_GRINDER_DIAMOND = REGISTRATE.item("diamond_grinding_head", Item::new)
            .lang("Diamond Grinding Head")
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 8),
                    new MaterialStack(GTMaterials.Diamond, GTValues.M * 5))))
            .register();
    public static ItemEntry<Item> COMPONENT_GRINDER_TUNGSTEN = REGISTRATE.item("tungsten_grinding_head", Item::new)
            .lang("Tungsten Grinding Head")
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Tungsten, GTValues.M * 4),
                    new MaterialStack(GTMaterials.VanadiumSteel, GTValues.M * 8),
                    new MaterialStack(GTMaterials.Diamond, GTValues.M))))
            .register();

    public static ItemEntry<Item> IRON_MINECART_WHEELS = REGISTRATE.item("iron_minecart_wheels", Item::new)
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, GTValues.M))))
            .register();
    public static ItemEntry<Item> STEEL_MINECART_WHEELS = REGISTRATE.item("steel_minecart_wheels", Item::new)
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M))))
            .register();

    public static ItemEntry<ComponentItem> QUANTUM_EYE = REGISTRATE.item("quantum_eye", ComponentItem::new)
            .transform(nameAndTooltip("Quantum Eye", "Improved Ender Eye"))
            .register();
    public static ItemEntry<ComponentItem> QUANTUM_STAR = REGISTRATE.item("quantum_star", ComponentItem::new)
            .transform(nameAndTooltip("Quantum Star", "Improved Nether Star"))
            .register();
    public static ItemEntry<ComponentItem> GRAVI_STAR = REGISTRATE.item("gravi_star", ComponentItem::new)
            .transform(nameAndTooltip("Gravi-Star", "Ultimate Nether Star"))
            .register();

    /////////////////////////////////////////
    // *********** COVERS ***********//
    /// //////////////////////////////////////

    public static ItemEntry<ComponentItem> ITEM_FILTER = REGISTRATE.item("item_filter", ComponentItem::create)
            .transform(nameAndTooltip("Item Filter",
                    "Filters §fItem§r I/O as §fCover§r.", "Can be used as a §fConveyor Module§r and §fRobotic Arm§r upgrade."))
            .onRegister(attach(new FilterBehaviour<>(ItemStack.class, SimpleItemFilter::new),
                    new CoverPlaceBehavior(GTCovers.ITEM_FILTER)))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Zinc, GTValues.M * 2),
                    new MaterialStack(GTMaterials.Steel, GTValues.M))))
            .register();

    public static ItemEntry<ComponentItem> TAG_FILTER = REGISTRATE
            .item("item_tag_filter", ComponentItem::create)
            .transform(nameAndTooltip("Item Tag Filter",
                    "Filters §fItem§r I/O with §fItem Tags§r as §fCover§r.", "Can be used as a §fConveyor Module§r and §fRobotic Arm§r upgrade."))
            .onRegister(attach(
                    new FilterBehaviour<>(ItemStack.class,
                            stack -> new TagFilter<>(stack, ItemStack::getItem, ItemStack::getTags)),
                    new CoverPlaceBehavior(GTCovers.ITEM_FILTER)))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Zinc, GTValues.M * 2))))
            .register();

    public static ItemEntry<ComponentItem> SMART_ITEM_FILTER = REGISTRATE
            .item("item_smart_filter", ComponentItem::create)
            .transform(nameAndTooltip("Smart Item Filter",
                    "Filters §fItem§r I/O with §fMachine Recipes§r as §fCover§r.", "Can be used as a §fConveyor Module§r and §fRobotic Arm§r upgrade."))
            .onRegister(attach(new FilterBehaviour<>(ItemStack.class, SmartItemFilter::new),
                    new CoverPlaceBehavior(GTCovers.ITEM_FILTER)))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Zinc, GTValues.M * 3 / 2))))
            .register();

    public static ItemEntry<ComponentItem> FLUID_FILTER = REGISTRATE.item("fluid_filter", ComponentItem::create)
            .transform(nameAndTooltip("Fluid Filter",
                    "Filters §fFluid§r I/O as §fCover§r.", "Can be used as a §fElectric Pump§r and §fFluid Regulator§r upgrade."))
            .onRegister(attach(new FilterBehaviour<>(FluidStack.class, SimpleFluidFilter::new),
                    new CoverPlaceBehavior(GTCovers.FLUID_FILTER)))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Zinc, GTValues.M * 2))))
            .register();

    public static ItemEntry<ComponentItem> TAG_FLUID_FILTER = REGISTRATE.item("fluid_tag_filter", ComponentItem::create)
            .transform(nameAndTooltip("Fluid Tag Filter",
                    "Filters §fFluid§r I/O with §fFluid Tags§r as §fCover§r.", "Can be used as a §fElectric Pump§r and §fFluid Regulator§r upgrade."))
            .onRegister(attach(
                    new FilterBehaviour<>(FluidStack.class, stack -> new TagFilter<>(stack, FluidStack::getFluid,
                            f -> f.getFluid().defaultFluidState().getTags())),
                    new CoverPlaceBehavior(GTCovers.FLUID_FILTER)))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Zinc, GTValues.M * 3 / 2))))
            .register();

    public static ItemEntry<ComponentItem> COMPOSITE_ITEM_FILTER = REGISTRATE
            .item("composite_item_filter", ComponentItem::create)
            .transform(nameAndTooltip("Composite Item Filter",
                    "TODO: FIX ME"))
            .onRegister(attach(
                    new FilterBehaviour<>(ItemStack.class, stack -> new CompositeFilter<>(stack, ItemStack.class)),
                    new CoverPlaceBehavior(GTCovers.ITEM_FILTER)))
            .register();

    public static ItemEntry<ComponentItem> COMPOSITE_FLUID_FILTER = REGISTRATE
            .item("composite_fluid_filter", ComponentItem::create)
            .transform(nameAndTooltip("Composite Fluid Filter",
                    "TODO: FIX ME"))
            .onRegister(attach(
                    new FilterBehaviour<>(FluidStack.class, stack -> new CompositeFilter<>(stack, FluidStack.class)),
                    new CoverPlaceBehavior(GTCovers.ITEM_FILTER)))
            .register();

    public static ItemEntry<ComponentItem> COVER_WIRELESS_TRANSMITTER = REGISTRATE
            .item("wireless_transmitter_cover", ComponentItem::create)
            .lang("Wireless Transmitter Cover")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.WIRELESS_TRANSMITTER)))
            .register();

    public static ItemEntry<ComponentItem> COVER_MACHINE_CONTROLLER = REGISTRATE
            .item("machine_controller_cover", ComponentItem::create)
            .transform(nameAndTooltip("Machine Controller Cover", "Turns Machines §fON/OFF§r as §fCover§r."))
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.MACHINE_CONTROLLER)))
            .register();

    public static ItemEntry<ComponentItem> COVER_ACTIVITY_DETECTOR = REGISTRATE
            .item("activity_detector_cover", ComponentItem::create)
            .transform(nameAndTooltip("Activity Detector Cover",
                            "Gives out §fActivity Status§r as Redstone as §fCover§r."))
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.ACTIVITY_DETECTOR)))
            .register();
    public static ItemEntry<ComponentItem> COVER_ACTIVITY_DETECTOR_ADVANCED = REGISTRATE
            .item("advanced_activity_detector_cover", ComponentItem::create)
            .transform(nameAndTooltip("Advanced Activity Detector Cover",
                            "Gives out §fMachine Progress§r as Redstone as §fCover§r."))
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.ACTIVITY_DETECTOR_ADVANCED)))
            .register();
    public static ItemEntry<ComponentItem> COVER_FLUID_DETECTOR = REGISTRATE
            .item("fluid_detector_cover", ComponentItem::create)
            .transform(nameAndTooltip("Fluid Detector Cover",
                            "Gives out §fFluid Amount§r as Redstone as §fCover§r."))
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.FLUID_DETECTOR)))
            .register();
    public static ItemEntry<ComponentItem> COVER_FLUID_DETECTOR_ADVANCED = REGISTRATE
            .item("advanced_fluid_detector_cover", ComponentItem::create)
            .transform(nameAndTooltip("Advanced Fluid Detector Cover",
                            "Gives §fRS-Latch§r controlled §fFluid Storage Status§r as Redstone as §fCover§r."))
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.FLUID_DETECTOR_ADVANCED)))
            .register();
    public static ItemEntry<ComponentItem> COVER_ITEM_DETECTOR = REGISTRATE
            .item("item_detector_cover", ComponentItem::create)
            .transform(nameAndTooltip("Item Detector Cover", "Gives out §fItem Amount§r as Redstone as §fCover§r."))
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.ITEM_DETECTOR)))
            .register();
    public static ItemEntry<ComponentItem> COVER_ITEM_DETECTOR_ADVANCED = REGISTRATE
            .item("advanced_item_detector_cover", ComponentItem::create)
            .transform(nameAndTooltip("Advanced Item Detector Cover",
                            "Gives §fRS-Latch§r controlled §fItem Storage Status§r as Redstone as §fCover§r."))
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.ITEM_DETECTOR_ADVANCED)))
            .register();
    public static ItemEntry<ComponentItem> COVER_ENERGY_DETECTOR = REGISTRATE
            .item("energy_detector_cover", ComponentItem::create)
            .transform(nameAndTooltip("Energy Detector Cover",
                            "Gives out §fEnergy Amount§r as Redstone as §fCover§r."))
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.ENERGY_DETECTOR)))
            .register();
    public static ItemEntry<ComponentItem> COVER_ENERGY_DETECTOR_ADVANCED = REGISTRATE
            .item("advanced_energy_detector_cover", ComponentItem::create)
            .transform(nameAndTooltip("Advanced Energy Detector Cover",
                            "Gives §fRS-Latch§r controlled §fEnergy Status§r as Redstone as §fCover§r."))
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.ENERGY_DETECTOR_ADVANCED)))
            .register();
    public static ItemEntry<ComponentItem> COVER_MAINTENANCE_DETECTOR = REGISTRATE
            .item("maintenance_detector_cover", ComponentItem::create)
            .transform(nameAndTooltip("Maintenance Detector Cover",
                            "Gives out §fMaintenance Status§r as Redstone as §fCover§r."))
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.MAINTENANCE_DETECTOR)))
            .register();

    public static ItemEntry<ComponentItem> COVER_SCREEN = REGISTRATE
            .item("computer_monitor_cover", ComponentItem::create)
            .transform(nameAndTooltip("Computer Monitor Cover", "Displays §fData§r as §fCover§r."))
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.COMPUTER_MONITOR)))
            .register();

    public static ItemEntry<ComponentItem> COVER_STORAGE = REGISTRATE
            .item("storage_cover", ComponentItem::create)
            .lang("Storage Cover")
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.COVER_STORAGE)))
            .register();
    public static ItemEntry<ComponentItem> COVER_SHUTTER = REGISTRATE
            .item("shutter_module_cover", ComponentItem::create)
            .transform(nameAndTooltip("Shutter Module Cover",
                            "§fBlocks Transfer§r through attached Side as §fCover§r."))
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.SHUTTER)))
            .register();

    public static ItemEntry<ComponentItem> COVER_INFINITE_WATER = REGISTRATE
            .item("infinite_water_cover", ComponentItem::create)
            .transform(nameAndTooltip("Infinite Water Cover",
                            "Fills attached containers with §9Water§r as §fCover§r."))
            .onRegister(attach(
                    new TooltipBehavior(Component.translatable("common.gtceu.tooltip.produces_fluid",
                            InfiniteWaterCover.AVG_FLUID_PER_TICK)),
                    new CoverPlaceBehavior(GTCovers.INFINITE_WATER)))
            .register();

    public static ItemEntry<ComponentItem> COVER_ENDER_FLUID_LINK = REGISTRATE
            .item("ender_fluid_link_cover", ComponentItem::create)
            .transform(nameAndTooltip("Ender Fluid Link Cover",
                            "Transports §fFluids§r with a §fWireless §dEnder§f Connection§r as §fCover§r."))
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.ENDER_FLUID_LINK)))
            .register();

    public static ItemEntry<ComponentItem> COVER_ENDER_ITEM_LINK = REGISTRATE
            .item("ender_item_link_cover", ComponentItem::create)
            .transform(nameAndTooltip("Ender Item Link Cover",
                            "Transports §fItems§r with a §fWireless §dEnder§f Connection§r as §fCover§r."))
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.ENDER_ITEM_LINK)))
            .register();
    public static ItemEntry<ComponentItem> COVER_ENDER_REDSTONE_LINK = REGISTRATE
            .item("ender_redstone_link_cover", ComponentItem::create)
            .transform(nameAndTooltip("Ender Redstone Link Cover",
                            "Transmits §fRedstone Signal§r with a §fWireless §dEnder§f Connection§r as §fCover§r."))
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.ENDER_REDSTONE_LINK)))
            .register();

    public static ItemEntry<ComponentItem> COVER_FLUID_VOIDING = REGISTRATE
            .item("fluid_voiding_cover", ComponentItem::create)
            .transform(nameAndTooltip("Fluid Voiding Cover",
                    "Voids §fFluids§r as §fCover§r.", "Activate with §fSoft Mallet§r after placement."))
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.FLUID_VOIDING)))
            .register();
    public static ItemEntry<ComponentItem> COVER_FLUID_VOIDING_ADVANCED = REGISTRATE
            .item("advanced_fluid_voiding_cover", ComponentItem::create)
            .transform(nameAndTooltip("Advanced Fluid Voiding Cover",
                    "Voids §fFluids§r with amount control as §fCover§r.", "Activate with §fSoft Mallet§r after placement."))
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.FLUID_VOIDING_ADVANCED)))
            .register();
    public static ItemEntry<ComponentItem> COVER_ITEM_VOIDING = REGISTRATE
            .item("item_voiding_cover", ComponentItem::create)
            .transform(nameAndTooltip("Item Voiding Cover",
                    "Voids §fItems§r as §fCover§r.", "Activate with §fSoft Mallet§r after placement."))
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.ITEM_VOIDING)))
            .register();
    public static ItemEntry<ComponentItem> COVER_ITEM_VOIDING_ADVANCED = REGISTRATE
            .item("advanced_item_voiding_cover", ComponentItem::create)
            .transform(nameAndTooltip("Advanced Item Voiding Cover",
                    "Voids §fItems§r with amount control as §fCover§r.", "Activate with §fSoft Mallet§r after placement."))
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.ITEM_VOIDING_ADVANCED)))
            .register();

    public static ItemEntry<ComponentItem> COVER_FACADE = REGISTRATE.item("facade_cover", ComponentItem::create)
            .transform(nameAndTooltip("%s Cover Facade",
                    "7Decorative Outfit §fCover§r.", "§rCrafted using an Iron Plate and any block"))
            .onRegister(attach(new FacadeItemBehaviour(), new CoverPlaceBehavior(GTCovers.FACADE)))
            .model(NonNullBiConsumer.noop())
            .register();

    // Solar Panels
    public static ItemEntry<ComponentItem> COVER_SOLAR_PANEL = REGISTRATE.item("solar_panel", ComponentItem::create)
            .lang("Solar Panel Cover")
            .onRegister(solarPanelTooltip(GTValues.ULV))
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.SOLAR_PANEL_BASIC)))
            .register();
    public static ItemEntry<ComponentItem> COVER_SOLAR_PANEL_ULV = REGISTRATE
            .item("ulv_solar_panel", ComponentItem::create)
            .lang("Ultra Low Voltage Solar Panel Cover")
            .onRegister(solarPanelTooltip(GTValues.ULV))
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.SOLAR_PANEL[0])))
            .register();

    public static ItemEntry<ComponentItem> COVER_SOLAR_PANEL_LV = REGISTRATE
            .item("lv_solar_panel", ComponentItem::create)
            .lang("Low Voltage Solar Panel Cover")
            .onRegister(solarPanelTooltip(GTValues.LV))
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.SOLAR_PANEL[1])))
            .register();

    public static ItemEntry<ComponentItem> COVER_SOLAR_PANEL_MV = REGISTRATE
            .item("mv_solar_panel", ComponentItem::create)
            .lang("Medium Voltage Solar Panel Cover")
            .onRegister(solarPanelTooltip(GTValues.MV))
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.SOLAR_PANEL[2])))
            .register();

    public static ItemEntry<ComponentItem> COVER_SOLAR_PANEL_HV = REGISTRATE
            .item("hv_solar_panel", ComponentItem::create)
            .lang("High Voltage Solar Panel Cover")
            .onRegister(solarPanelTooltip(GTValues.HV))
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.SOLAR_PANEL[3])))
            .register();

    public static ItemEntry<ComponentItem> COVER_SOLAR_PANEL_EV = REGISTRATE
            .item("ev_solar_panel", ComponentItem::create)
            .lang("Extreme Voltage Solar Panel Cover")
            .onRegister(solarPanelTooltip(GTValues.EV))
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.SOLAR_PANEL[4])))
            .register();
    public static ItemEntry<ComponentItem> COVER_SOLAR_PANEL_IV = REGISTRATE
            .item("iv_solar_panel", ComponentItem::create)
            .lang("Insane Voltage Solar Panel Cover")
            .onRegister(solarPanelTooltip(GTValues.IV))
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.SOLAR_PANEL[5])))
            .register();
    public static ItemEntry<ComponentItem> COVER_SOLAR_PANEL_LuV = REGISTRATE
            .item("luv_solar_panel", ComponentItem::create)
            .lang("Ludicrous Voltage Solar Panel Cover")
            .onRegister(solarPanelTooltip(GTValues.LuV))
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.SOLAR_PANEL[6])))
            .register();
    public static ItemEntry<ComponentItem> COVER_SOLAR_PANEL_ZPM = REGISTRATE
            .item("zpm_solar_panel", ComponentItem::create)
            .lang("Zero Point Module Solar Panel Cover")
            .onRegister(solarPanelTooltip(GTValues.ZPM))
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.SOLAR_PANEL[7])))
            .register();
    public static ItemEntry<ComponentItem> COVER_SOLAR_PANEL_UV = REGISTRATE
            .item("uv_solar_panel", ComponentItem::create)
            .lang("Ultimate Voltage Solar Panel Cover")
            .onRegister(solarPanelTooltip(GTValues.UV))
            .onRegister(attach(new CoverPlaceBehavior(GTCovers.SOLAR_PANEL[8])))
            .register();

    private static NonNullConsumer<ComponentItem> solarPanelTooltip(int tier) {
        return v -> v.attachComponents(new TooltipBehavior(
                Component.translatable("item.gtceu.solar_panel.tooltip.0").withStyle(ChatFormatting.GRAY),
                Component.translatable("item.gtceu.solar_panel.tooltip.1").withStyle(ChatFormatting.GRAY),
                GTCommonComponents.voltage(IO.OUT, tier)
        ));
    }

    // Plugin
    public static ItemEntry<Item> PLUGIN_TEXT;
    public static ItemEntry<Item> PLUGIN_ONLINE_PIC;
    public static ItemEntry<Item> PLUGIN_FAKE_GUI;
    public static ItemEntry<Item> PLUGIN_ADVANCED_MONITOR;

    public static ItemEntry<ComponentItem> PROGRAMMED_CIRCUIT = REGISTRATE
            .item("programmed_circuit", ComponentItem::create)
            .transform(nameAndTooltip("Programmed Circuit", "Use to open configuration GUI",
                    "Shift-Right-Click on a machine", "with a circuit slot to set it to", "this circuit's value."))
            .model(overrideModel(GTCEu.id("circuit"), 33))
            .onRegister(modelPredicate(GTCEu.id("circuit"),
                    (itemStack) -> IntCircuitBehaviour.getCircuitConfiguration(itemStack) / 100f))
            .onRegister(attach(new IntCircuitBehaviour()))
            .tag(CustomTags.SKIP_ITEM_DETECTOR)
            .register();

    /*
     * public static ItemEntry<ComponentItem> FOAM_SPRAYER = REGISTRATE.item("foam_sprayer", ComponentItem::create)
     * .setData(ProviderType.LANG, nameAndMultilineTooltipLang("Foam Sprayer",
     * "Sprays Construction Foam\nUse on a frame to foam connected frames\nFoam can be colored"))
     * .onRegister(attach(new FoamSprayerBehavior()).setMaxStackSize(1);
     */
    public static ItemEntry<ComponentItem> GELLED_TOLUENE = REGISTRATE.item("gelled_toluene", ComponentItem::create)
            .transform(nameAndTooltip("Gelled Toluene", "Raw Explosive"))
            .register();

    public static ItemEntry<ComponentItem> BOTTLE_PURPLE_DRINK = REGISTRATE.item("purple_drink", ComponentItem::create)
            .transform(nameAndTooltip("Purple Drink", "How about Lemonade. Or some Ice Tea? I got Purple Drink!"))
            .onRegister(attach(new FoodStats(GTFoods.DRINK, true, Items.GLASS_BOTTLE::getDefaultInstance)))
            .register();
    public static ItemEntry<ComponentItem> DOUGH = REGISTRATE.item("dough", ComponentItem::create)
            .lang("Dough")
            .onRegister(attach(new FoodStats(
                    new FoodProperties.Builder().nutrition(1)
                            .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 400), .40f)
                            .effect(() -> new MobEffectInstance(MobEffects.POISON, 100), .05f)
                            .build())))
            .tag(CustomTags.WHEAT_DOUGHS)
            .register();
    public static ItemEntry<ComponentItem> PLANT_BALL = REGISTRATE.item("plant_ball", ComponentItem::create)
            .onRegister(burnTime(75)).register();
    public static ItemEntry<ComponentItem> STICKY_RESIN = REGISTRATE.item("sticky_resin", ComponentItem::create)
            .lang("Sticky Resin").onRegister(burnTime(200)).register();
    public static ItemEntry<ComponentItem> BIO_CHAFF = REGISTRATE.item("bio_chaff", ComponentItem::create)
            .onRegister(burnTime(200)).register();
    public static ItemEntry<Item> ENERGIUM_DUST = REGISTRATE.item("energium_dust", Item::new)
            .register();

    public static ItemEntry<ComponentItem> POWER_UNIT_LV = REGISTRATE.item("lv_power_unit", ComponentItem::create)
            .lang("LV Power Unit")
            .properties(p -> p.stacksTo(8))
            .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/tools/power_unit_lv")))
            .onRegister(attach(ElectricStats.createElectricItem(100000L, GTValues.LV)))
            .register();
    public static ItemEntry<ComponentItem> POWER_UNIT_MV = REGISTRATE.item("mv_power_unit", ComponentItem::create)
            .lang("MV Power Unit")
            .properties(p -> p.stacksTo(8))
            .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/tools/power_unit_mv")))
            .onRegister(attach(ElectricStats.createElectricItem(400000L, GTValues.MV)))
            .register();
    public static ItemEntry<ComponentItem> POWER_UNIT_HV = REGISTRATE.item("hv_power_unit", ComponentItem::create)
            .lang("HV Power Unit")
            .properties(p -> p.stacksTo(8))
            .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/tools/power_unit_hv")))
            .onRegister(attach(ElectricStats.createElectricItem(1600000L, GTValues.HV)))
            .register();
    public static ItemEntry<ComponentItem> POWER_UNIT_EV = REGISTRATE.item("ev_power_unit", ComponentItem::create)
            .lang("EV Power Unit")
            .properties(p -> p.stacksTo(8))
            .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/tools/power_unit_ev")))
            .onRegister(attach(ElectricStats.createElectricItem(6400000L, GTValues.EV)))
            .register();
    public static ItemEntry<ComponentItem> POWER_UNIT_IV = REGISTRATE.item("iv_power_unit", ComponentItem::create)
            .lang("IV Power Unit")
            .properties(p -> p.stacksTo(8))
            .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/tools/power_unit_iv")))
            .onRegister(attach(ElectricStats.createElectricItem(25600000L, GTValues.IV)))
            .register();

    public static ItemEntry<Item> MASK_FILTER = REGISTRATE.item("mask_filter", Item::new)
            .lang("Gas Mask Filter")
            .properties(p -> p.stacksTo(1))
            .register();
    // TODO add more medications for specific conditions & then remove them from paracetamol
    public static ItemEntry<ComponentItem> PARACETAMOL_PILL = REGISTRATE.item("paracetamol_pill", ComponentItem::create)
            .lang("Paracetamol Pill")
            .properties(p -> p.food(GTFoods.ANTIDOTE))
            .onRegister(attach(new AntidoteBehavior(10,
                    GTMedicalConditions.CHEMICAL_BURNS,
                    GTMedicalConditions.WEAK_POISON,
                    GTMedicalConditions.NAUSEA,
                    GTMedicalConditions.IRRITANT,
                    GTMedicalConditions.METHANOL_POISONING,
                    GTMedicalConditions.CARBON_MONOXIDE_POISONING)))
            .register();
    public static ItemEntry<ComponentItem> RAD_AWAY_PILL = REGISTRATE.item("rad_away_pill", ComponentItem::create)
            .lang("RadAway™ Pill")
            .properties(p -> p.food(GTFoods.ANTIDOTE))
            .onRegister(attach(new AntidoteBehavior(50, GTMedicalConditions.CARCINOGEN)))
            .register();

    public static ItemEntry<ComponentItem> NANO_SABER = REGISTRATE.item("nano_saber", ComponentItem::create)
            .transform(nameAndTooltip("Nano Saber", "Ryujin no ken wo kurae!"))
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(new NanoSaberBehavior(), ElectricStats.createElectricItem(4_000_000L, GTValues.HV)))
            .model((ctx, prov) -> {
                var rootModel = prov.generated(ctx::getEntry, prov.modLoc("item/nano_saber/normal"));
                prov.getBuilder("item/nano_saber/active")
                        .parent(new ModelFile.UncheckedModelFile("item/handheld"))
                        .texture("layer0", prov.modLoc("item/nano_saber/active"));

                rootModel.override().predicate(NanoSaberBehavior.OVERRIDE_KEY_LOCATION, 1.0f)
                        .model(new ModelFile.UncheckedModelFile(prov.modLoc("item/nano_saber/active")))
                        .end();
            })
            .onRegister(modelPredicate(NanoSaberBehavior.OVERRIDE_KEY_LOCATION,
                    () -> () -> (stack, level, entity, layer) -> NanoSaberBehavior.isItemActive(stack) ? 1.0f : 0.0f))
            .register();

    public static ItemEntry<ComponentItem> PROSPECTOR_LV = REGISTRATE.item("prospector.lv", ComponentItem::create)
            .lang("Ore Prospector (LV)")
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(ElectricStats.createElectricItem(100_000L, GTValues.LV),
                    new ProspectorScannerBehavior(2, GTValues.V[GTValues.LV] / 16L, ProspectorMode.ORE)))
            .register();
    public static ItemEntry<ComponentItem> PROSPECTOR_HV = REGISTRATE.item("prospector.hv", ComponentItem::create)
            .lang("Advanced Prospector (HV)")
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(ElectricStats.createElectricItem(1_600_000L, GTValues.HV),
                    new ProspectorScannerBehavior(3, GTValues.V[GTValues.HV] / 16L, ProspectorMode.ORE,
                            ProspectorMode.FLUID,
                            ConfigHolder.INSTANCE.machines.doBedrockOres ? ProspectorMode.BEDROCK_ORE : null)))
            .register();
    public static ItemEntry<ComponentItem> PROSPECTOR_LuV = REGISTRATE.item("prospector.luv", ComponentItem::create)
            .lang("Super Prospector (LuV)")
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(ElectricStats.createElectricItem(1_000_000_000L, GTValues.LuV),
                    new ProspectorScannerBehavior(5, GTValues.V[GTValues.LuV] / 16L, ProspectorMode.ORE,
                            ProspectorMode.FLUID,
                            ConfigHolder.INSTANCE.machines.doBedrockOres ? ProspectorMode.BEDROCK_ORE : null)))
            .register();

    public static ItemEntry<ComponentItem> ITEM_MAGNET_LV = REGISTRATE.item("lv_item_magnet", ComponentItem::create)
            .lang("LV Item Magnet")
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(ElectricStats.createElectricItem(100_000L, GTValues.LV), new ItemMagnetBehavior(8)))
            .register();
    public static ItemEntry<ComponentItem> ITEM_MAGNET_HV = REGISTRATE.item("hv_item_magnet", ComponentItem::create)
            .lang("HV Item Magnet")
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(ElectricStats.createElectricItem(1_600_000L, GTValues.HV), new ItemMagnetBehavior(32)))
            .register();

    public static ItemEntry<Item> WIRELESS;
    public static ItemEntry<Item> CAMERA;

    public static ItemEntry<ComponentItem> TERMINAL = REGISTRATE.item("terminal", ComponentItem::create)
            .transform(nameAndTooltip("Terminal",
                    "Shift + R-Click on a controller to automatically build a multiblock with items from your inventory"))
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(new TerminalBehavior()))
            .register();

    public static ItemEntry<ComponentItem> MACHINE_MEMORY_CARD = REGISTRATE
            .item("machine_memory_card", ComponentItem::create)
            .lang("Machine Memory Card")
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(new MachineConfigCopyBehaviour()))
            .register();

    public static final Map<DyeColor, ItemEntry<DyeItem>> CHEMICAL_DYES = new Object2ObjectOpenHashMap<>();
    static {
        for (DyeColor color : DyeColor.values()) {
            CHEMICAL_DYES.put(color, REGISTRATE
                    .item("chemical_%s_dye".formatted(color.getName()), (props) -> new DyeItem(color, props))
                    .lang("Chemical %s Dye".formatted(toEnglishName(color.getName())))
                    .tag(Tags.Items.DYES)
                    .tag(color.getTag())
                    .register());
        }
    }

    public static final ItemEntry<ComponentItem>[] SPRAY_CAN_DYES = new ItemEntry[DyeColor.values().length];

    static {
        for (int i = 0; i < DyeColor.values().length; i++) {
            var dyeColor = DyeColor.values()[i];
            SPRAY_CAN_DYES[i] = REGISTRATE.item("%s_dye_spray_can".formatted(dyeColor.getName()), ComponentItem::create)
                    .lang("Spray Can (%s)".formatted(toEnglishName(dyeColor.getName())))
                    .properties(p -> p.stacksTo(1))
                    .onRegister(attach(new ColorSprayBehaviour(() -> SPRAY_EMPTY.asStack(), 512, i))).register();
        }
    }

    public static ItemEntry<ComponentItem> TURBINE_ROTOR = REGISTRATE.item("turbine_rotor", ComponentItem::create)
            .lang("%s Turbine Rotor")
            .properties(p -> p.stacksTo(1))
            .model((ctx, prov) -> createTextureModel(ctx, prov, GTCEu.id("item/tools/turbine")))
            .color(() -> IMaterialPartItem::getItemStackColor)
            .onRegister(attach(new TurbineRotorBehaviour())).register();

    public static ItemEntry<ComponentItem> NEURO_PROCESSOR = REGISTRATE.item("neuro_processing_unit", ComponentItem::new)
            .transform(nameAndTooltip("Neuro Processing Unit", "Neuro CPU"))
            .register();
    public static ItemEntry<ComponentItem> STEM_CELLS = REGISTRATE.item("stem_cells", ComponentItem::new)
            .transform(nameAndTooltip("Stem Cells", "Raw Intelligence"))
            .register();
    public static ItemEntry<ComponentItem> PETRI_DISH = REGISTRATE.item("petri_dish", ComponentItem::new)
            .transform(nameAndTooltip("Petri Dish", "For cultivating Cells"))
            .register();

    public static ItemEntry<ComponentItem> VOLTAGE_COIL_ULV = REGISTRATE.item("ulv_voltage_coil", ComponentItem::create)
            .transform(nameAndTooltip("Ultra Low Voltage Coil", "Primitive Coil"))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Lead, GTValues.M * 2),
                    new MaterialStack(GTMaterials.IronMagnetic, GTValues.M / 2))))
            .register();
    public static ItemEntry<ComponentItem> VOLTAGE_COIL_LV = REGISTRATE.item("lv_voltage_coil", ComponentItem::create)
            .transform(nameAndTooltip("Low Voltage Coil", "Basic Coil"))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 2),
                    new MaterialStack(GTMaterials.IronMagnetic, GTValues.M / 2))))
            .register();
    public static ItemEntry<ComponentItem> VOLTAGE_COIL_MV = REGISTRATE.item("mv_voltage_coil", ComponentItem::create)
            .transform(nameAndTooltip("Medium Voltage Coil", "Good Coil"))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Aluminium, GTValues.M * 2),
                    new MaterialStack(GTMaterials.SteelMagnetic, GTValues.M / 2))))
            .register();
    public static ItemEntry<ComponentItem> VOLTAGE_COIL_HV = REGISTRATE.item("hv_voltage_coil", ComponentItem::create)
            .transform(nameAndTooltip("High Voltage Coil", "Advanced Coil"))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.BlackSteel, GTValues.M * 2),
                    new MaterialStack(GTMaterials.SteelMagnetic, GTValues.M / 2))))
            .register();
    public static ItemEntry<ComponentItem> VOLTAGE_COIL_EV = REGISTRATE.item("ev_voltage_coil", ComponentItem::create)
            .transform(nameAndTooltip("Extreme Voltage Coil", "Extreme Coil"))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Platinum, GTValues.M * 2),
                    new MaterialStack(GTMaterials.NeodymiumMagnetic, GTValues.M / 2))))
            .register();
    public static ItemEntry<ComponentItem> VOLTAGE_COIL_IV = REGISTRATE.item("iv_voltage_coil", ComponentItem::create)
            .transform(nameAndTooltip("Insane Voltage Coil", "Elite Coil"))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Iridium, GTValues.M * 2),
                    new MaterialStack(GTMaterials.NeodymiumMagnetic, GTValues.M / 2))))
            .register();
    public static ItemEntry<ComponentItem> VOLTAGE_COIL_LuV = REGISTRATE.item("luv_voltage_coil", ComponentItem::create)
            .transform(nameAndTooltip("Ludicrous Voltage Coil", "Master Coil"))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Osmiridium, GTValues.M * 2),
                    new MaterialStack(GTMaterials.SamariumMagnetic, GTValues.M / 2))))
            .register();
    public static ItemEntry<ComponentItem> VOLTAGE_COIL_ZPM = REGISTRATE.item("zpm_voltage_coil", ComponentItem::create)
            .transform(nameAndTooltip("Zero Point Module Voltage Coil", "Super Coil"))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Europium, GTValues.M * 2),
                    new MaterialStack(GTMaterials.SamariumMagnetic, GTValues.M / 2))))
            .register();
    public static ItemEntry<ComponentItem> VOLTAGE_COIL_UV = REGISTRATE.item("uv_voltage_coil", ComponentItem::create)
            .transform(nameAndTooltip("Ultimate Voltage Coil", "Ultimate Coil"))
            .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Tritanium, GTValues.M * 2),
                    new MaterialStack(GTMaterials.SamariumMagnetic, GTValues.M / 2))))
            .register();

    public static ItemEntry<Item> CLIPBOARD;

    public static ItemEntry<ArmorComponentItem> NIGHTVISION_GOGGLES = REGISTRATE
            .item("nightvision_goggles",
                    (p) -> new ArmorComponentItem(GTArmorMaterials.GOGGLES, ArmorItem.Type.HELMET, p)
                            .setArmorLogic(new NightvisionGoggles(2,
                                    80_000L * (long) Math.max(1,
                                            Math.pow(1, ConfigHolder.INSTANCE.tools.voltageTierNightVision - 1)),
                                    ConfigHolder.INSTANCE.tools.voltageTierNightVision, ArmorItem.Type.HELMET)))
            .lang("Nightvision Goggles")
            .tag(Tags.Items.ARMORS_HELMETS)
            .register();

    public static ItemEntry<HydraulicArmorItem> HYDRAULIC_HELMET = REGISTRATE.item("hydraulic_helmet",
            p -> new HydraulicArmorItem(GTArmorMaterials.HYDRAULIC, ArmorItem.Type.HELMET, p))
            .lang("Hydraulic Suite Helmet")
            .properties(p -> p.rarity(Rarity.UNCOMMON))
            .tag(Tags.Items.ARMORS_HELMETS)
            .onRegister(attach(new ModularItemComponent(3, GTValues.MV)))
            .register();

    public static ItemEntry<HydraulicArmorItem> HYDRAULIC_CHESTPLATE = REGISTRATE.item("hydraulic_chestplate",
            p -> new HydraulicArmorItem(GTArmorMaterials.HYDRAULIC, ArmorItem.Type.CHESTPLATE, p))
            .lang("Hydraulic Chestplate")
            .properties(p -> p.rarity(Rarity.UNCOMMON))
            .tag(Tags.Items.ARMORS_CHESTPLATES)
            .onRegister(attach(new ModularItemComponent(3, GTValues.MV)))
            .register();

    public static ItemEntry<HydraulicArmorItem> HYDRAULIC_LEGGINGS = REGISTRATE.item("hydraulic_leggings",
            p -> new HydraulicArmorItem(GTArmorMaterials.HYDRAULIC, ArmorItem.Type.LEGGINGS, p))
            .lang("Hydraulic Leggings")
            .properties(p -> p.rarity(Rarity.UNCOMMON))
            .tag(Tags.Items.ARMORS_LEGGINGS)
            .onRegister(attach(new ModularItemComponent(3, GTValues.MV)))
            .register();

    public static ItemEntry<HydraulicArmorItem> HYDRAULIC_BOOTS = REGISTRATE.item("hydraulic_boots",
            p -> new HydraulicArmorItem(GTArmorMaterials.HYDRAULIC, ArmorItem.Type.BOOTS, p))
            .lang("Hydraulic Boots")
            .properties(p -> p.rarity(Rarity.UNCOMMON))
            .tag(Tags.Items.ARMORS_BOOTS)
            .onRegister(attach(new ModularItemComponent(3, GTValues.MV)))
            .register();

    public static ItemEntry<NanoMuscleArmorItem> NANO_CHESTPLATE = REGISTRATE
            .item("nano_fiber_chestplate",
                    (p) -> new NanoMuscleArmorItem(GTArmorMaterials.NANO_MUSCLE, ArmorItem.Type.CHESTPLATE, p))
            .lang("Nano-Fiber Suit Chestplate")
            .properties(p -> p.rarity(Rarity.UNCOMMON))
            .tag(Tags.Items.ARMORS_CHESTPLATES)
            .onRegister(attach(new ModularItemComponent(4, GTValues.EV)))
            .register();
    public static ItemEntry<NanoMuscleArmorItem> NANO_LEGGINGS = REGISTRATE
            .item("nano_fiber_leggings",
                    (p) -> new NanoMuscleArmorItem(GTArmorMaterials.NANO_MUSCLE, ArmorItem.Type.LEGGINGS, p))
            .lang("Nano-Fiber Suit Leggings")
            .properties(p -> p.rarity(Rarity.UNCOMMON))
            .tag(Tags.Items.ARMORS_LEGGINGS)
            .onRegister(attach(new ModularItemComponent(4, GTValues.EV)))
            .register();
    public static ItemEntry<NanoMuscleArmorItem> NANO_BOOTS = REGISTRATE
            .item("nano_fiber_boots",
                    (p) -> new NanoMuscleArmorItem(GTArmorMaterials.NANO_MUSCLE, ArmorItem.Type.BOOTS, p))
            .lang("Nano-Fiber Suit Boots")
            .properties(p -> p.rarity(Rarity.UNCOMMON))
            .tag(Tags.Items.ARMORS_BOOTS)
            .onRegister(attach(new ModularItemComponent(4, GTValues.EV)))
            .register();
    public static ItemEntry<NanoMuscleArmorItem> NANO_HELMET = REGISTRATE
            .item("nano_fiber_helmet",
                    (p) -> new NanoMuscleArmorItem(GTArmorMaterials.NANO_MUSCLE, ArmorItem.Type.HELMET, p))
            .lang("Nano-Fiber Suit Helmet")
            .tag(Tags.Items.ARMORS_HELMETS)
            .properties(p -> p.rarity(Rarity.UNCOMMON))
            .onRegister(attach(new ModularItemComponent(4, GTValues.EV)))
            .register();

    public static ItemEntry<ArmorComponentItem> FACE_MASK = REGISTRATE
            .item("face_mask",
                    (p) -> new ArmorComponentItem(GTArmorMaterials.BAD_PPE_EQUIPMENT, ArmorItem.Type.HELMET, p)
                            .setArmorLogic(new HazmatSuit(ArmorItem.Type.HELMET, "bad_hazmat")))
            .lang("Face Mask")
            .tag(Tags.Items.ARMORS_HELMETS, CustomTags.PPE_ARMOR)
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("tooltip.gtceu.hazard_trigger.protection"));
                tooltips.add(Component.translatable("tooltip.gtceu.hazard_trigger.inhalation"));
            })))
            .register();
    public static ItemEntry<ArmorComponentItem> RUBBER_GLOVES = REGISTRATE
            .item("rubber_gloves",
                    (p) -> new ArmorComponentItem(GTArmorMaterials.BAD_PPE_EQUIPMENT, ArmorItem.Type.HELMET, p)
                            .setArmorLogic(new HazmatSuit(ArmorItem.Type.CHESTPLATE, "bad_hazmat")))
            .lang("Rubber Gloves")
            .tag(Tags.Items.ARMORS_CHESTPLATES, CustomTags.PPE_ARMOR)
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("tooltip.gtceu.hazard_trigger.protection"));
                tooltips.add(Component.translatable("tooltip.gtceu.hazard_trigger.skin_contact"));
            })))
            .register();
    public static ItemEntry<ArmorComponentItem> HAZMAT_CHESTPLATE = REGISTRATE
            .item("hazmat_chestpiece",
                    (p) -> new ArmorComponentItem(GTArmorMaterials.GOOD_PPE_EQUIPMENT, ArmorItem.Type.CHESTPLATE, p)
                            .setArmorLogic(new HazmatSuit(ArmorItem.Type.CHESTPLATE, "hazmat")))
            .lang("Hazardous Materials Suit Chestpiece")
            .properties(p -> p.rarity(Rarity.UNCOMMON))
            .tag(Tags.Items.ARMORS_CHESTPLATES, CustomTags.PPE_ARMOR)
            .register();
    public static ItemEntry<ArmorComponentItem> HAZMAT_LEGGINGS = REGISTRATE
            .item("hazmat_leggings",
                    (p) -> new ArmorComponentItem(GTArmorMaterials.GOOD_PPE_EQUIPMENT, ArmorItem.Type.LEGGINGS, p)
                            .setArmorLogic(new HazmatSuit(ArmorItem.Type.LEGGINGS, "hazmat")))
            .lang("Hazardous Materials Suit Leggings")
            .properties(p -> p.rarity(Rarity.UNCOMMON))
            .tag(Tags.Items.ARMORS_LEGGINGS, CustomTags.PPE_ARMOR)
            .register();
    public static ItemEntry<ArmorComponentItem> HAZMAT_BOOTS = REGISTRATE
            .item("hazmat_boots",
                    (p) -> new ArmorComponentItem(GTArmorMaterials.GOOD_PPE_EQUIPMENT, ArmorItem.Type.BOOTS, p)
                            .setArmorLogic(new HazmatSuit(ArmorItem.Type.BOOTS, "hazmat")))
            .lang("Hazardous Materials Suit Boots")
            .properties(p -> p.rarity(Rarity.UNCOMMON))
            .tag(Tags.Items.ARMORS_BOOTS, CustomTags.PPE_ARMOR)
            .register();
    public static ItemEntry<ArmorComponentItem> HAZMAT_HELMET = REGISTRATE
            .item("hazmat_headpiece",
                    (p) -> new ArmorComponentItem(GTArmorMaterials.GOOD_PPE_EQUIPMENT, ArmorItem.Type.HELMET, p)
                            .setArmorLogic(new HazmatSuit(ArmorItem.Type.HELMET, "hazmat")))
            .lang("Hazardous Materials Suit Headpiece")
            .properties(p -> p.rarity(Rarity.UNCOMMON))
            .tag(Tags.Items.ARMORS_HELMETS)
            .tag(CustomTags.PPE_ARMOR)
            .register();

    public static ItemEntry<QuarkTechArmorItem> QUANTUM_CHESTPLATE = REGISTRATE
            .item("quark_matter_chestplate",
                    (p) -> new QuarkTechArmorItem(GTArmorMaterials.QUARK_TECH, ArmorItem.Type.CHESTPLATE, p))
            .lang("Quark-Matter Suit Chestplate")
            .properties(p -> p.rarity(Rarity.RARE))
            .tag(Tags.Items.ARMORS_CHESTPLATES, CustomTags.PPE_ARMOR,
                    ItemTags.FREEZE_IMMUNE_WEARABLES)
            .tag(CustomTags.PPE_ARMOR)
            .onRegister(attach(new ModularItemComponent(8, GTValues.MAX)))
            .register();
    public static ItemEntry<QuarkTechArmorItem> QUANTUM_LEGGINGS = REGISTRATE
            .item("quark_matter_leggings",
                    (p) -> new QuarkTechArmorItem(GTArmorMaterials.QUARK_TECH, ArmorItem.Type.LEGGINGS, p))
            .lang("Quark-Matter Suit Leggings")
            .properties(p -> p.rarity(Rarity.RARE))
            .tag(Tags.Items.ARMORS_LEGGINGS, CustomTags.PPE_ARMOR)
            .onRegister(attach(new ModularItemComponent(8, GTValues.MAX)))
            .register();
    public static ItemEntry<QuarkTechArmorItem> QUANTUM_BOOTS = REGISTRATE
            .item("quark_matter_boots",
                    (p) -> new QuarkTechArmorItem(GTArmorMaterials.QUARK_TECH, ArmorItem.Type.BOOTS, p))
            .lang("Quark-Matter Suit Boots")
            .properties(p -> p.rarity(Rarity.RARE))
            .tag(Tags.Items.ARMORS_BOOTS, CustomTags.PPE_ARMOR)
            .onRegister(attach(new ModularItemComponent(8, GTValues.MAX)))
            .register();
    public static ItemEntry<QuarkTechArmorItem> QUANTUM_HELMET = REGISTRATE
            .item("quark_matter_helmet",
                    (p) -> new QuarkTechArmorItem(GTArmorMaterials.QUARK_TECH, ArmorItem.Type.HELMET, p))
            .lang("Quark-Matter Suit Helmet")
            .properties(p -> p.rarity(Rarity.RARE))
            .tag(Tags.Items.ARMORS_HELMETS, CustomTags.PPE_ARMOR)
            .onRegister(attach(new ModularItemComponent(8, GTValues.MAX)))
            .register();

    public static ItemEntry<Item> LIQUID_FUEL_THRUSTER = REGISTRATE.item("liquid_fuel_thruster", Item::new)
            .lang("Liquid-Fueled Thruster")
            .register();

    public static ItemEntry<Item> POWER_THRUSTER = REGISTRATE.item("power_thruster", Item::new)
            .properties(p -> p.rarity(Rarity.UNCOMMON)).register();
    public static ItemEntry<Item> POWER_THRUSTER_ADVANCED = REGISTRATE.item("advanced_power_thruster", Item::new)
            .lang("Advanced Power Thruster").properties(p -> p.rarity(Rarity.RARE))
            .register();
    public static ItemEntry<Item> GRAVITATION_ENGINE = REGISTRATE.item("gravitation_engine_unit", Item::new)
            .lang("Gravitation Engine Unit").properties(p -> p.rarity(Rarity.EPIC))
            .register();

    public static ItemEntry<RecordItem> SUS_RECORD = REGISTRATE
            .item("sus_record", p -> new RecordItem(15, GTSoundEntries.SUS_RECORD::getMainEvent, p, 820))
            .lang("Music Disc")
            .properties(p -> p.stacksTo(1).rarity(Rarity.RARE))
            .tag(ItemTags.MUSIC_DISCS)
            .register();
    public static ItemEntry<ComponentItem> NAN_CERTIFICATE = REGISTRATE.item("nan_certificate", ComponentItem::create)
            .lang("Certificate of Not Being a Noob Anymore")
            .transform(nameAndTooltip("Certificate of Not Being a Noob Anymore", "Challenge Accepted!"))
            .properties(p -> p.rarity(Rarity.EPIC))
            .register();

    public static ItemEntry<ComponentItem> FERTILIZER = REGISTRATE.item("fertilizer", ComponentItem::create)
            .onRegister(attach(new FertilizerBehavior())).register();

    public static ItemEntry<ComponentItem> BLACKLIGHT = REGISTRATE.item("blacklight", ComponentItem::create)
            .transform(nameAndTooltip("Blacklight", "Long-Wave §dUltraviolet§r light source"))
            .register();

    public static ItemEntry<GTBoatItem> RUBBER_BOAT = REGISTRATE
            .item("rubber_boat", p -> new GTBoatItem(false, GTBoat.BoatType.RUBBER, new Item.Properties()))
            .lang("Rubber Boat")
            .register();

    public static ItemEntry<GTBoatItem> TREATED_WOOD_BOAT = REGISTRATE
            .item("treated_wood_boat", p -> new GTBoatItem(false, GTBoat.BoatType.TREATED_WOOD, new Item.Properties()))
            .lang("Treated Wood Boat")
            .register();

    public static ItemEntry<GTBoatItem> RUBBER_CHEST_BOAT = REGISTRATE
            .item("rubber_chest_boat", p -> new GTBoatItem(true, GTBoat.BoatType.RUBBER, new Item.Properties()))
            .lang("Rubber Boat with Chest")
            .register();

    public static ItemEntry<GTBoatItem> TREATED_WOOD_CHEST_BOAT = REGISTRATE
            .item("treated_wood_chest_boat",
                    p -> new GTBoatItem(true, GTBoat.BoatType.TREATED_WOOD, new Item.Properties()))
            .lang("Treated Wood Boat with Chest")
            .register();

    public static ItemEntry<ComponentItem> TEXT_MODULE = REGISTRATE.item("text_module", ComponentItem::create)
            .onRegister(attach(new TextModuleBehaviour()))
            .register();

    public static ItemEntry<ComponentItem> IMAGE_MODULE = REGISTRATE.item("image_module", ComponentItem::create)
            .onRegister(attach(new ImageModuleBehaviour()))
            .register();

    public static ItemEntry<ComponentItem> GUI_MODULE = REGISTRATE.item("gui_module", ComponentItem::create)
            .onRegister(attach(new GuiModuleBehaviour()))
            .register();

    public static ItemEntry<Item> SPOILABLE_1;
    public static ItemEntry<Item> SPOILABLE_2;
    public static ItemEntry<Item> SPOILABLE_3;
    public static ItemEntry<Item> SPOILABLE_4;
    public static ItemEntry<Item> SPOILABLE_5;
    public static ItemEntry<Item> ENTITY_SPOILABLE;

    static {
        if (GTCEu.isDev()) {
            SPOILABLE_1 = REGISTRATE.item("spoilable_1", Item::new)
                    .lang("Spoilable 1")
                    .register();
            SPOILABLE_2 = REGISTRATE.item("spoilable_2", Item::new)
                    .lang("Spoilable 2")
                    .register();
            SPOILABLE_3 = REGISTRATE.item("spoilable_3", Item::new)
                    .lang("Spoilable 3")
                    .register();
            SPOILABLE_4 = REGISTRATE.item("spoilable_4", Item::new)
                    .lang("Spoilable 4")
                    .register();
            SPOILABLE_5 = REGISTRATE.item("spoilable_5", Item::new)
                    .lang("Spoilable 5")
                    .register();
            ENTITY_SPOILABLE = REGISTRATE.item("entity_spoilable", Item::new)
                    .lang("Entity Spoilable")
                    .register();
        }
    }

    public static ItemEntry<ComponentItem> MAGNETIC_GOLDEN_CARROT = REGISTRATE
            .item("magnetic_golden_carrot", ComponentItem::create)
            .lang("Magnetic Golden Carrot")
            .onRegister(attach(new FoodStats(new FoodProperties.Builder()
                    .nutrition(Foods.GOLDEN_CARROT.getNutrition())
                    .saturationMod(Foods.GOLDEN_CARROT.getSaturationModifier())
                    .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 20 * 60 * 10, 1), 1)
                    .fast()
                    .alwaysEat()
                    .build())))
            .register();

    public static void init() {
        GTMaterialItems.generateMaterialItems();
        GTMaterialItems.generateTools();
        GTMaterialItems.generateArmors();
    }

    public static void postInit() {
        if (GTCEu.Mods.isAE2Loaded()) {
            AEItemCompat.init();
        }
    }

    /**
     * Adds lang generation for an item name & tooltip(s), and adds the tooltip(s) to the component item. Tooltips are coloured grey by default
     */
    public static <I extends ComponentItem, S extends ItemBuilder<I, GTRegistrate>> NonNullFunction<S, S> nameAndTooltip(String name, String... tooltips) {
        return builder -> {
            builder.setData(ProviderType.LANG, (ctx, prov) -> {
                prov.add(ctx.getEntry().getDescriptionId(), name);
                if (tooltips.length == 1) {
                    prov.add(ctx.getEntry().getDescriptionId() + ".tooltip", tooltips[0]);
                } else {
                    for (var i = 0; i < tooltips.length; i++) {
                        prov.add(ctx.getEntry().getDescriptionId() + ".tooltip." + i, tooltips[i]);
                    }
                }
            });
            builder.onRegister(item -> {
                var tooltipComponents = Arrays.stream(LangUtil.getSingleOrMultiLang(item.getDescriptionId() + ".tooltip"))
                        .map(v -> v.withStyle(ChatFormatting.GRAY))
                        .toArray(MutableComponent[]::new);

                item.attachComponents(new TooltipBehavior(tooltipComponents));
            });
            return builder;
        };
    }

    public static <T extends ItemLike> NonNullConsumer<T> materialInfo(ItemMaterialInfo materialInfo) {
        return item -> ItemMaterialData.registerMaterialInfo(item, materialInfo);
    }

    public static <P, T extends Item,
            S2 extends ItemBuilder<T, P>> NonNullFunction<S2, S2> unificationItem(@NotNull TagPrefix tagPrefix,
                                                                                  @NotNull Material mat) {
        return builder -> {
            builder.onRegister(item -> {
                Supplier<ItemLike> supplier = GTMemoizer.memoize(() -> item);
                MaterialEntry entry = new MaterialEntry(tagPrefix, mat);
                GTMaterialItems.toUnify.put(entry, supplier);
                ItemMaterialData.registerMaterialEntry(supplier, entry);
            });
            return builder;
        };
    }

    public static <T extends Item> void cauldronInteraction(T item) {
        if (item instanceof TagPrefixItem tagPrefixItem &&
                GTMaterialItems.purifyMap.containsKey(tagPrefixItem.tagPrefix)) {
            CauldronInteraction.WATER.put(item, (state, world, pos, player, hand, stack) -> {
                if (!world.isClientSide) {
                    Item stackItem = stack.getItem();
                    if (stackItem instanceof TagPrefixItem prefixItem) {
                        if (!GTMaterialItems.purifyMap.containsKey(prefixItem.tagPrefix))
                            return InteractionResult.PASS;
                        if (!state.hasProperty(LayeredCauldronBlock.LEVEL)) {
                            return InteractionResult.PASS;
                        }

                        int level = state.getValue(LayeredCauldronBlock.LEVEL);
                        if (level == 0)
                            return InteractionResult.PASS;

                        player.setItemInHand(hand,
                                ChemicalHelper.get(GTMaterialItems.purifyMap.get(prefixItem.tagPrefix),
                                        prefixItem.material, stack.getCount()));
                        player.awardStat(Stats.USE_CAULDRON);
                        player.awardStat(Stats.ITEM_USED.get(stackItem));
                        LayeredCauldronBlock.lowerFillLevel(state, world, pos);

                    }
                }

                return InteractionResult.sidedSuccess(world.isClientSide);
            });

        }
    }

    public static <T extends ComponentItem> NonNullConsumer<T> burnTime(int burnTime) {
        return item -> item.burnTime(burnTime);
    }

    public static <T extends IComponentItem> NonNullConsumer<T> attach(IItemComponent components) {
        return item -> item.attachComponents(components);
    }

    public static <T extends IComponentItem> NonNullConsumer<T> attach(IItemComponent... components) {
        return item -> item.attachComponents(components);
    }

    public static <T extends Item> NonNullConsumer<T> modelPredicate(ResourceLocation predicate,
                                                                     StackProperty property) {
        return item -> {
            if (GTCEu.isClientSide()) {
                ItemProperties.register(item, predicate, (itemStack, c, l, i) -> property.apply(itemStack));
            }
        };
    }

    @SuppressWarnings("deprecation")
    public static <T extends Item> NonNullConsumer<T> modelPredicate(ResourceLocation predicate,
                                                                     Supplier<Supplier<ItemPropertyFunction>> property) {
        return item -> {
            if (GTCEu.isClientSide()) {
                ItemProperties.register(item, predicate, property.get().get());
            }
        };
    }

    @FunctionalInterface
    public interface StackProperty {

        float apply(ItemStack stack);
    }
}
