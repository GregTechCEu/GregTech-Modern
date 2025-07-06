package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.cover.filter.SimpleItemFilter;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.recipe.ToolHeadReplaceRecipe;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.item.ItemMagnetBehavior;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.utils.ToolItemHelper;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

import com.tterrag.registrate.util.entry.ItemEntry;
import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.GTValues.LuV;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.data.recipe.generated.ToolRecipeHandler.*;

public final class CustomToolRecipes {

    private static final Int2ReferenceMap<ItemEntry<? extends Item>> motorItems = new Int2ReferenceArrayMap<>();
    private static final Int2ReferenceMap<Material> baseMaterials = new Int2ReferenceArrayMap<>();
    private static final Int2ReferenceMap<List<ItemEntry<? extends Item>>> batteryItems = new Int2ReferenceArrayMap<>();

    private CustomToolRecipes() {}

    public static void init(@NotNull Consumer<FinishedRecipe> provider) {
        initializeGTItems();
        registerPowerUnitRecipes(provider);
        registerCustomToolRecipes(provider);
    }

    private static void initializeGTItems() {
        motorItems.put(GTValues.LV, GTItems.ELECTRIC_MOTOR_LV);
        motorItems.put(GTValues.MV, GTItems.ELECTRIC_MOTOR_MV);
        motorItems.put(GTValues.HV, GTItems.ELECTRIC_MOTOR_HV);
        motorItems.put(GTValues.EV, GTItems.ELECTRIC_MOTOR_EV);
        motorItems.put(GTValues.IV, GTItems.ELECTRIC_MOTOR_IV);

        baseMaterials.put(GTValues.LV, GTMaterials.Steel);
        baseMaterials.put(GTValues.MV, GTMaterials.Aluminium);
        baseMaterials.put(GTValues.HV, GTMaterials.StainlessSteel);
        baseMaterials.put(GTValues.EV, GTMaterials.Titanium);
        baseMaterials.put(GTValues.IV, GTMaterials.TungstenSteel);

        batteryItems.put(GTValues.ULV, List.of(GTItems.BATTERY_ULV_TANTALUM));
        batteryItems.put(GTValues.LV,
                List.of(GTItems.BATTERY_LV_LITHIUM, GTItems.BATTERY_LV_CADMIUM, GTItems.BATTERY_LV_SODIUM));
        batteryItems.put(GTValues.MV,
                List.of(GTItems.BATTERY_MV_LITHIUM, GTItems.BATTERY_MV_CADMIUM, GTItems.BATTERY_MV_SODIUM));
        batteryItems.put(GTValues.HV, List.of(GTItems.BATTERY_HV_LITHIUM, GTItems.BATTERY_HV_CADMIUM,
                GTItems.BATTERY_HV_SODIUM, GTItems.ENERGIUM_CRYSTAL));
        batteryItems.put(GTValues.EV, List.of(GTItems.BATTERY_EV_VANADIUM, GTItems.LAPOTRON_CRYSTAL));
        batteryItems.put(GTValues.IV, List.of(GTItems.BATTERY_IV_VANADIUM, GTItems.ENERGY_LAPOTRONIC_ORB));
        batteryItems.put(GTValues.LuV,
                List.of(GTItems.BATTERY_LuV_VANADIUM, GTItems.ENERGY_LAPOTRONIC_ORB_CLUSTER));
        batteryItems.put(GTValues.ZPM, List.of(GTItems.BATTERY_ZPM_NAQUADRIA, GTItems.ENERGY_MODULE));
        batteryItems.put(GTValues.UV, List.of(GTItems.BATTERY_UV_NAQUADRIA, GTItems.ENERGY_CLUSTER));

        ToolHeadReplaceRecipe.setToolHeadForTool(toolHeadDrill, GTToolType.DRILL_LV);
        ToolHeadReplaceRecipe.setToolHeadForTool(toolHeadDrill, GTToolType.DRILL_MV);
        ToolHeadReplaceRecipe.setToolHeadForTool(toolHeadDrill, GTToolType.DRILL_HV);
        ToolHeadReplaceRecipe.setToolHeadForTool(toolHeadDrill, GTToolType.DRILL_EV);
        ToolHeadReplaceRecipe.setToolHeadForTool(toolHeadDrill, GTToolType.DRILL_IV);
        ToolHeadReplaceRecipe.setToolHeadForTool(toolHeadChainsaw, GTToolType.CHAINSAW_LV);
        ToolHeadReplaceRecipe.setToolHeadForTool(toolHeadWrench, GTToolType.WRENCH_LV);
        ToolHeadReplaceRecipe.setToolHeadForTool(toolHeadWrench, GTToolType.WRENCH_HV);
        ToolHeadReplaceRecipe.setToolHeadForTool(toolHeadWrench, GTToolType.WRENCH_IV);
        ToolHeadReplaceRecipe.setToolHeadForTool(toolHeadBuzzSaw, GTToolType.BUZZSAW);
        ToolHeadReplaceRecipe.setToolHeadForTool(toolHeadScrewdriver, GTToolType.SCREWDRIVER_LV);
        ToolHeadReplaceRecipe.setToolHeadForTool(toolHeadWireCutter, GTToolType.WIRE_CUTTER_LV);
        ToolHeadReplaceRecipe.setToolHeadForTool(toolHeadWireCutter, GTToolType.WIRE_CUTTER_HV);
        ToolHeadReplaceRecipe.setToolHeadForTool(toolHeadWireCutter, GTToolType.WIRE_CUTTER_IV);
    }

    private static void registerPowerUnitRecipes(@NotNull Consumer<FinishedRecipe> provider) {
        for (int tier : powerUnitItems.keySet()) {
            List<ItemEntry<? extends Item>> tieredBatteryItems = batteryItems.get(tier);
            for (ItemEntry<? extends Item> batteryItem : tieredBatteryItems) {
                if (powerUnitItems.get(tier) != null) {
                    ItemStack batteryStack = batteryItem.asStack();
                    long maxCharge = GTCapabilityHelper.getElectricItem(batteryStack).getMaxCharge();
                    ItemStack powerUnitStack = ToolItemHelper.getMaxChargeOverrideStack(powerUnitItems.get(tier).get(),
                            maxCharge);
                    String recipeName = String.format("%s_%s",
                            BuiltInRegistries.ITEM.getKey(powerUnitItems.get(tier).get()).getPath(),
                            BuiltInRegistries.ITEM.getKey(batteryItem.get()).getPath());

                    VanillaRecipeHelper.addShapedEnergyTransferRecipe(provider, true, false, true, recipeName,
                            Ingredient.of(batteryStack), powerUnitStack,
                            "S d", "GMG", "PBP",
                            'M', motorItems.get(tier).asStack(),
                            'S', new MaterialEntry(screw, baseMaterials.get(tier)),
                            'P', new MaterialEntry(plate, baseMaterials.get(tier)),
                            'G', new MaterialEntry(gearSmall, baseMaterials.get(tier)),
                            'B', batteryStack);
                }
            }
        }
    }

    private static void registerCustomToolRecipes(@NotNull Consumer<FinishedRecipe> provider) {
        registerFlintToolRecipes(provider);
        registerMortarRecipes(provider);
        registerSoftToolRecipes(provider);
        registerElectricRecipes(provider);

        SpecialRecipeBuilder.special(ToolHeadReplaceRecipe.SERIALIZER).save(provider,
                "gtceu:crafting/replace_tool_head");
    }

    private static void registerFlintToolRecipes(@NotNull Consumer<FinishedRecipe> provider) {
        final MaterialEntry flint = new MaterialEntry(TagPrefix.gem, GTMaterials.Flint);
        final ItemStack stick = new ItemStack(Items.STICK);

        addToolRecipe(provider, GTMaterials.Flint, GTToolType.MORTAR, false,
                " I ", "SIS", "SSS",
                'I', flint,
                'S', new ItemStack(Blocks.STONE));

        addToolRecipe(provider, GTMaterials.Flint, GTToolType.SWORD, false,
                "I", "I", "S",
                'I', flint,
                'S', stick);

        addToolRecipe(provider, GTMaterials.Flint, GTToolType.PICKAXE, false,
                "III", " S ", " S ",
                'I', flint,
                'S', stick);

        addToolRecipe(provider, GTMaterials.Flint, GTToolType.SHOVEL, false,
                "I", "S", "S",
                'I', flint,
                'S', stick);

        addToolRecipe(provider, GTMaterials.Flint, GTToolType.AXE, true,
                "II", "IS", " S",
                'I', flint,
                'S', stick);

        addToolRecipe(provider, GTMaterials.Flint, GTToolType.HOE, true,
                "II", " S", " S",
                'I', flint,
                'S', stick);

        addToolRecipe(provider, GTMaterials.Flint, GTToolType.KNIFE, false,
                "I", "S",
                'I', flint,
                'S', stick);
    }

    private static void registerMortarRecipes(@NotNull Consumer<FinishedRecipe> provider) {
        for (Material material : new Material[] {
                GTMaterials.Bronze, GTMaterials.Iron, GTMaterials.Invar, GTMaterials.Steel,
                GTMaterials.DamascusSteel, GTMaterials.CobaltBrass, GTMaterials.WroughtIron }) {

            addToolRecipe(provider, material, GTToolType.MORTAR, false,
                    " I ", "SIS", "SSS",
                    'I',
                    new MaterialEntry(material.hasProperty(PropertyKey.GEM) ? TagPrefix.gem : TagPrefix.ingot,
                            material),
                    'S', new ItemStack(Blocks.STONE));
        }
    }

    private static void registerSoftToolRecipes(@NotNull Consumer<FinishedRecipe> provider) {
        final ItemStack stick = new ItemStack(Items.STICK);

        for (int i = 0; i < softMaterials.length; i++) {
            Material material = softMaterials[i];

            if (material.hasProperty(PropertyKey.WOOD)) {
                // todo allow these 3 to be mirrored
                VanillaRecipeHelper.addShapedRecipe(provider, String.format("soft_mallet_%s", material.getName()),
                        ToolHelper.get(GTToolType.SOFT_MALLET, material),
                        "II ", "IIS", "II ",
                        'I', ItemTags.PLANKS,
                        'S', stick);
            } else {
                VanillaRecipeHelper.addShapedRecipe(provider, String.format("soft_mallet_%s", material.getName()),
                        ToolHelper.get(GTToolType.SOFT_MALLET, material),
                        "II ", "IIS", "II ",
                        'I', new MaterialEntry(TagPrefix.ingot, material),
                        'S', stick);

                VanillaRecipeHelper.addShapedRecipe(provider, String.format("plunger_%s", material.getName()),
                        ToolHelper.getAndSetToolData(GTToolType.PLUNGER, material, 128 * (i << 1), 1, 4F, 0F),
                        "xPP", " SP", "S f",
                        'P', new MaterialEntry(TagPrefix.plate, material),
                        'S', rod);
            }
        }
    }

    private static void registerElectricRecipes(@NotNull Consumer<FinishedRecipe> provider) {
        for (ItemEntry<? extends Item> batteryItem : batteryItems.get(LV)) {
            VanillaRecipeHelper.addShapedEnergyTransferRecipe(provider, true, false, true,
                    "prospector_lv_" + batteryItem.getId().getPath(),
                    Ingredient.of(batteryItem), GTItems.PROSPECTOR_LV.asStack(),
                    "EPS", "CDC", "PBP",
                    'E', GTItems.EMITTER_LV.asStack(),
                    'P', new MaterialEntry(plate, GTMaterials.Steel),
                    'S', GTItems.SENSOR_LV.asStack(),
                    'D', new MaterialEntry(plate, GTMaterials.Glass),
                    'C', CustomTags.LV_CIRCUITS,
                    'B', batteryItem.asStack());

            {
                var magnetStack = GTItems.ITEM_MAGNET_LV.asStack();
                var tag = magnetStack.getOrCreateTag();
                var filter = (SimpleItemFilter) ItemFilter
                        .loadFilter(ItemMagnetBehavior.Filter.SIMPLE.getFilter(magnetStack));
                filter.setBlackList(true);
                tag.put(ItemMagnetBehavior.FILTER_TAG, filter.saveFilter());
                VanillaRecipeHelper.addShapedEnergyTransferRecipe(provider, true, false, true,
                        "lv_magnet_" + batteryItem.getId().getPath(),
                        Ingredient.of(batteryItem), magnetStack,
                        "MwM", "MBM", "CPC",
                        'M', new MaterialEntry(rod, GTMaterials.SteelMagnetic),
                        'P', new MaterialEntry(plate, GTMaterials.Steel),
                        'C', new MaterialEntry(cableGtSingle, GTMaterials.Tin),
                        'B', batteryItem.asStack());
            }
        }

        for (ItemEntry<? extends Item> batteryItem : batteryItems.get(MV)) {
            VanillaRecipeHelper.addShapedEnergyTransferRecipe(provider, true, false, true,
                    "portable_scanner_" + batteryItem.getId().getPath(),
                    Ingredient.of(batteryItem), GTItems.PORTABLE_SCANNER.asStack(),
                    "EPS", "CDC", "PBP",
                    'E', GTItems.EMITTER_MV.asStack(),
                    'P', new MaterialEntry(plate, GTMaterials.Aluminium),
                    'S', GTItems.SENSOR_MV.asStack(),
                    'D', GTItems.COVER_SCREEN.asStack(),
                    'C', CustomTags.MV_CIRCUITS,
                    'B', batteryItem.asStack());
        }

        for (ItemEntry<? extends Item> batteryItem : batteryItems.get(HV)) {
            VanillaRecipeHelper.addShapedEnergyTransferRecipe(provider, true, false, true,
                    "prospector_hv_" + batteryItem.getId().getPath(),
                    Ingredient.of(batteryItem), GTItems.PROSPECTOR_HV.asStack(),
                    "EPS", "CDC", "PBP",
                    'E', GTItems.EMITTER_HV.asStack(),
                    'P', new MaterialEntry(plate, GTMaterials.StainlessSteel),
                    'S', GTItems.SENSOR_HV.asStack(),
                    'D', GTItems.COVER_SCREEN.asStack(),
                    'C', CustomTags.HV_CIRCUITS,
                    'B', batteryItem.asStack());

            {
                var magnetStack = GTItems.ITEM_MAGNET_HV.asStack();
                var tag = magnetStack.getOrCreateTag();
                var filter = (SimpleItemFilter) ItemFilter
                        .loadFilter(ItemMagnetBehavior.Filter.SIMPLE.getFilter(magnetStack));
                filter.setBlackList(true);
                tag.put(ItemMagnetBehavior.FILTER_TAG, filter.saveFilter());
                VanillaRecipeHelper.addShapedEnergyTransferRecipe(provider, true, false, true,
                        "hv_magnet_" + batteryItem.getId().getPath(),
                        Ingredient.of(batteryItem), magnetStack,
                        "MwM", "MBM", "CPC",
                        'M', new MaterialEntry(rod, GTMaterials.NeodymiumMagnetic),
                        'P', new MaterialEntry(plate, GTMaterials.StainlessSteel),
                        'C', new MaterialEntry(cableGtSingle, GTMaterials.Gold),
                        'B', batteryItem.asStack());
            }
        }

        for (ItemEntry<? extends Item> batteryItem : batteryItems.get(LuV)) {
            VanillaRecipeHelper.addShapedEnergyTransferRecipe(provider, true, false, true,
                    "prospector_luv_" + batteryItem.getId().getPath(),
                    Ingredient.of(batteryItem), GTItems.PROSPECTOR_LuV.asStack(),
                    "EPS", "CDC", "PBP",
                    'E', GTItems.EMITTER_LuV.asStack(),
                    'P', new MaterialEntry(plate, GTMaterials.RhodiumPlatedPalladium),
                    'S', GTItems.SENSOR_LuV.asStack(),
                    'D', GTItems.COVER_SCREEN.asStack(),
                    'C', CustomTags.LuV_CIRCUITS,
                    'B', batteryItem.asStack());
        }
    }
}
