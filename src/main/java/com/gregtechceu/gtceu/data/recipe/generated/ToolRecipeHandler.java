package com.gregtechceu.gtceu.data.recipe.generated;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.recipe.ToolHeadReplaceRecipe;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
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

import com.google.common.collect.ImmutableList;
import com.tterrag.registrate.util.entry.ItemEntry;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;

public class ToolRecipeHandler {

    public static Map<Integer, ItemEntry<? extends Item>> motorItems = new HashMap<>();
    public static Map<Integer, Material> baseMaterials = new HashMap<>();
    public static Map<Integer, List<ItemEntry<? extends Item>>> batteryItems = new HashMap<>();
    public static Map<Integer, ItemEntry<? extends Item>> powerUnitItems = new HashMap<>();
    private static final Material[] softMaterials = new Material[] {
            GTMaterials.Wood, GTMaterials.Rubber, GTMaterials.Polyethylene,
            GTMaterials.Polytetrafluoroethylene, GTMaterials.Polybenzimidazole,
            GTMaterials.SiliconeRubber, GTMaterials.StyreneButadieneRubber
    };

    public static void init(Consumer<FinishedRecipe> provider) {
        initializeGTItems();
        TagPrefix.plate.executeHandler(provider, PropertyKey.TOOL, ToolRecipeHandler::processTool);
        TagPrefix.plate.executeHandler(provider, PropertyKey.TOOL, ToolRecipeHandler::processElectricTool);
        registerPowerUnitRecipes(provider);
        registerCustomToolRecipes(provider);
    }

    public static void initializeGTItems() {
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

        powerUnitItems.put(GTValues.LV, GTItems.POWER_UNIT_LV);
        powerUnitItems.put(GTValues.MV, GTItems.POWER_UNIT_MV);
        powerUnitItems.put(GTValues.HV, GTItems.POWER_UNIT_HV);
        powerUnitItems.put(GTValues.EV, GTItems.POWER_UNIT_EV);
        powerUnitItems.put(GTValues.IV, GTItems.POWER_UNIT_IV);

        batteryItems.put(GTValues.ULV, Collections.singletonList(GTItems.BATTERY_ULV_TANTALUM));
        batteryItems.put(GTValues.LV,
                ImmutableList.of(GTItems.BATTERY_LV_LITHIUM, GTItems.BATTERY_LV_CADMIUM, GTItems.BATTERY_LV_SODIUM));
        batteryItems.put(GTValues.MV,
                ImmutableList.of(GTItems.BATTERY_MV_LITHIUM, GTItems.BATTERY_MV_CADMIUM, GTItems.BATTERY_MV_SODIUM));
        batteryItems.put(GTValues.HV, ImmutableList.of(GTItems.BATTERY_HV_LITHIUM, GTItems.BATTERY_HV_CADMIUM,
                GTItems.BATTERY_HV_SODIUM, GTItems.ENERGIUM_CRYSTAL));
        batteryItems.put(GTValues.EV, ImmutableList.of(GTItems.BATTERY_EV_VANADIUM, GTItems.LAPOTRON_CRYSTAL));
        batteryItems.put(GTValues.IV, ImmutableList.of(GTItems.BATTERY_IV_VANADIUM, GTItems.ENERGY_LAPOTRONIC_ORB));
        batteryItems.put(GTValues.LuV,
                ImmutableList.of(GTItems.BATTERY_LuV_VANADIUM, GTItems.ENERGY_LAPOTRONIC_ORB_CLUSTER));
        batteryItems.put(GTValues.ZPM, ImmutableList.of(GTItems.BATTERY_ZPM_NAQUADRIA, GTItems.ENERGY_MODULE));
        batteryItems.put(GTValues.UV, ImmutableList.of(GTItems.BATTERY_UV_NAQUADRIA, GTItems.ENERGY_CLUSTER));

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

    public static void registerPowerUnitRecipes(Consumer<FinishedRecipe> provider) {
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
                            'S', new UnificationEntry(screw, baseMaterials.get(tier)),
                            'P', new UnificationEntry(plate, baseMaterials.get(tier)),
                            'G', new UnificationEntry(gearSmall, baseMaterials.get(tier)),
                            'B', batteryStack);
                }
            }
        }
    }

    private static void processTool(TagPrefix prefix, Material material, ToolProperty property,
                                    Consumer<FinishedRecipe> provider) {
        ItemStack stick = new ItemStack(Items.STICK);
        UnificationEntry plate = new UnificationEntry(TagPrefix.plate, material);
        UnificationEntry ingot = new UnificationEntry(
                material.hasProperty(PropertyKey.GEM) ? TagPrefix.gem : TagPrefix.ingot, material);

        if (material.hasFlag(GENERATE_PLATE)) {

            addToolRecipe(provider, material, GTToolType.MINING_HAMMER, true,
                    "PPf", "PPS", "PPh",
                    'P', plate,
                    'S', stick);

            addToolRecipe(provider, material, GTToolType.SPADE, false,
                    "fPh", "PSP", " S ",
                    'P', plate,
                    'S', stick);

            addToolRecipe(provider, material, GTToolType.SAW, false,
                    "PPS", "fhS",
                    'P', plate,
                    'S', stick);

            addToolRecipe(provider, material, GTToolType.AXE, false,
                    "PIh", "PS ", "fS ",
                    'P', plate,
                    'I', ingot,
                    'S', stick);

            addToolRecipe(provider, material, GTToolType.HOE, false,
                    "PIh", "fS ", " S ",
                    'P', plate,
                    'I', ingot,
                    'S', stick);

            addToolRecipe(provider, material, GTToolType.PICKAXE, false,
                    "PII", "fSh", " S ",
                    'P', plate,
                    'I', ingot,
                    'S', stick);

            addToolRecipe(provider, material, GTToolType.SCYTHE, false,
                    "PPI", "fSh", " S ",
                    'P', plate,
                    'I', ingot,
                    'S', stick);

            addToolRecipe(provider, material, GTToolType.SHOVEL, false,
                    "fPh", " S ", " S ",
                    'P', plate,
                    'S', stick);

            addToolRecipe(provider, material, GTToolType.SWORD, false,
                    " P ", "fPh", " S ",
                    'P', plate,
                    'S', stick);

            addToolRecipe(provider, material, GTToolType.HARD_HAMMER, true,
                    "II ", "IIS", "II ",
                    'I', ingot,
                    'S', stick);

            addToolRecipe(provider, material, GTToolType.FILE, true,
                    " P ", " P ", " S ",
                    'P', plate,
                    'S', stick);

            addToolRecipe(provider, material, GTToolType.KNIFE, false,
                    "fPh", " S ",
                    'P', plate,
                    'S', stick);

            addToolRecipe(provider, material, GTToolType.WRENCH, false,
                    "PhP", " P ", " P ",
                    'P', plate);
        } else {
            GTCEu.LOGGER.info("Did not find plate for " + material.getName() +
                    ", skipping mining hammer, spade, saw, axe, hoe, pickaxe, scythe, shovel, sword, hammer, file, knife, wrench recipes");
        }

        if (material.hasFlag(GENERATE_ROD)) {
            UnificationEntry rod = new UnificationEntry(TagPrefix.rod, material);

            if (material.hasFlag(GENERATE_PLATE)) {
                addToolRecipe(provider, material, GTToolType.BUTCHERY_KNIFE, false,
                        "PPf", "PP ", "Sh ",
                        'P', plate,
                        'S', rod);

                if (material.hasFlag(GENERATE_BOLT_SCREW)) {
                    addToolRecipe(provider, material, GTToolType.WIRE_CUTTER, false,
                            "PfP", "hPd", "STS",
                            'P', plate,
                            'T', new UnificationEntry(TagPrefix.screw, material),
                            'S', rod);
                } else if (!ArrayUtils.contains(softMaterials, material)) {
                    GTCEu.LOGGER
                            .info("Did not find bolt for " + material.getName() + ", skipping wirecutter recipe");
                }
            } else {
                GTCEu.LOGGER.info("Did not find plate for " + material.getName() +
                        ", skipping wirecutter, butchery knife recipes");
            }

            addToolRecipe(provider, material, GTToolType.SCREWDRIVER, true,
                    " fS", " Sh", "W  ",
                    'S', rod,
                    'W', stick);

            addDyeableToolRecipe(provider, material, GTToolType.CROWBAR, true,
                    "hDS", "DSD", "SDf",
                    'S', rod);
        } else if (!ArrayUtils.contains(softMaterials, material)) {
            GTCEu.LOGGER.info("Did not find rod for " + material.getName() +
                    ", skipping wirecutter, butchery knife, screwdriver, crowbar recipes");
        }
    }

    private static void processElectricTool(TagPrefix prefix, Material material, ToolProperty property,
                                            Consumer<FinishedRecipe> provider) {
        final int voltageMultiplier = material.getBlastTemperature() > 2800 ? GTValues.VA[GTValues.LV] :
                GTValues.VA[GTValues.ULV];
        TagPrefix toolPrefix;

        if (material.hasFlag(GENERATE_PLATE)) {
            final UnificationEntry plate = new UnificationEntry(TagPrefix.plate, material);
            final UnificationEntry steelPlate = new UnificationEntry(TagPrefix.plate, GTMaterials.Steel);
            final UnificationEntry steelRing = new UnificationEntry(TagPrefix.ring, GTMaterials.Steel);

            // drill
            if (property.hasType(GTToolType.DRILL_LV)) {
                toolPrefix = TagPrefix.toolHeadDrill;
                VanillaRecipeHelper.addShapedRecipe(provider, String.format("drill_head_%s", material.getName()),
                        ChemicalHelper.get(toolPrefix, material),
                        "XSX", "XSX", "ShS",
                        'X', plate,
                        'S', steelPlate);

                addElectricToolRecipe(toolPrefix, material, new GTToolType[] { GTToolType.DRILL_LV, GTToolType.DRILL_MV,
                        GTToolType.DRILL_HV, GTToolType.DRILL_EV, GTToolType.DRILL_IV }, provider);
            }

            // chainsaw
            if (property.hasType(GTToolType.CHAINSAW_LV)) {
                toolPrefix = TagPrefix.toolHeadChainsaw;
                VanillaRecipeHelper.addShapedRecipe(provider, String.format("chainsaw_head_%s", material.getName()),
                        ChemicalHelper.get(toolPrefix, material),
                        "SRS", "XhX", "SRS",
                        'X', plate,
                        'S', steelPlate,
                        'R', steelRing);

                addElectricToolRecipe(toolPrefix, material, new GTToolType[] { GTToolType.CHAINSAW_LV }, provider);
            }

            // wrench
            if (property.hasType(GTToolType.WRENCH_LV)) {
                toolPrefix = TagPrefix.toolHeadWrench;
                addElectricToolRecipe(toolPrefix, material,
                        new GTToolType[] { GTToolType.WRENCH_LV, GTToolType.WRENCH_HV, GTToolType.WRENCH_IV },
                        provider);

                VanillaRecipeHelper.addShapedRecipe(provider, String.format("wrench_head_%s", material.getName()),
                        ChemicalHelper.get(toolPrefix, material),
                        "hXW", "XRX", "WXd",
                        'X', plate,
                        'R', steelRing,
                        'W', new UnificationEntry(TagPrefix.screw, GTMaterials.Steel));
            }

            // electric wire cutters
            if (property.hasType(GTToolType.WIRE_CUTTER_LV)) {
                toolPrefix = toolHeadWireCutter;
                addElectricToolRecipe(toolPrefix, material,
                        new GTToolType[] { GTToolType.WIRE_CUTTER_LV, GTToolType.WIRE_CUTTER_HV,
                                GTToolType.WIRE_CUTTER_IV },
                        provider);

                VanillaRecipeHelper.addShapedRecipe(provider, String.format("wirecutter_head_%s", material.getName()),
                        ChemicalHelper.get(toolPrefix, material),
                        "XfX", "X X", "SRS",
                        'X', plate,
                        'R', steelRing,
                        'S', new UnificationEntry(screw, GTMaterials.Steel));
            }

            // buzzsaw
            if (property.hasType(GTToolType.BUZZSAW)) {
                toolPrefix = TagPrefix.toolHeadBuzzSaw;
                addElectricToolRecipe(toolPrefix, material, new GTToolType[] { GTToolType.BUZZSAW }, provider);

                VanillaRecipeHelper.addShapedRecipe(provider, String.format("buzzsaw_blade_%s", material.getName()),
                        ChemicalHelper.get(toolPrefix, material),
                        "sXh", "X X", "fXx",
                        'X', plate);

                if (material.hasFlag(GENERATE_GEAR)) {
                    GTRecipeTypes.LATHE_RECIPES.recipeBuilder("buzzsaw_gear_" + material.getName())
                            .inputItems(TagPrefix.gear, material)
                            .outputItems(toolPrefix, material)
                            .duration((int) material.getMass() * 4)
                            .EUt(8L * voltageMultiplier)
                            .save(provider);
                } else {
                    GTCEu.LOGGER.info("Did not find gear for " + material.getName() +
                            ", skipping gear -> buzzsaw blade recipe");
                }
            }
        } else {
            GTCEu.LOGGER.info("Did not find plate for " + material.getName() +
                    ", skipping electric drill, chainsaw, wrench, wirecutter, buzzsaw recipe");
        }

        // screwdriver
        if (property.hasType(GTToolType.SCREWDRIVER_LV)) {
            if (material.hasFlag(GENERATE_LONG_ROD)) {
                toolPrefix = TagPrefix.toolHeadScrewdriver;
                addElectricToolRecipe(toolPrefix, material, new GTToolType[] { GTToolType.SCREWDRIVER_LV }, provider);

                VanillaRecipeHelper.addShapedRecipe(provider, String.format("screwdriver_tip_%s", material.getName()),
                        ChemicalHelper.get(toolPrefix, material),
                        "fR", " h",
                        'R', new UnificationEntry(TagPrefix.rodLong, material));
            } else {
                GTCEu.LOGGER.info("Did not find long rod for " + material.getName() +
                        ", skipping electric screwdriver recipe");
            }
        }
    }

    public static void addElectricToolRecipe(TagPrefix toolHead, Material material, GTToolType[] toolItems,
                                             Consumer<FinishedRecipe> provider) {
        for (GTToolType toolType : toolItems) {
            if (!material.getProperty(PropertyKey.TOOL).hasType(toolType)) continue;

            int tier = toolType.electricTier;
            ItemStack powerUnitStack = powerUnitItems.get(tier).asStack();
            IElectricItem powerUnit = GTCapabilityHelper.getElectricItem(powerUnitStack);
            ItemStack tool = GTMaterialItems.TOOL_ITEMS.get(material, toolType).get().get(0, powerUnit.getMaxCharge());
            VanillaRecipeHelper.addShapedEnergyTransferRecipe(provider,
                    true, true, true,
                    String.format("%s_%s", material.getName(), toolType.name),
                    Ingredient.of(powerUnitStack),
                    tool,
                    "wHd", " U ",
                    'H', new UnificationEntry(toolHead, material),
                    'U', powerUnitStack);
        }
    }

    public static void addToolRecipe(Consumer<FinishedRecipe> provider, @NotNull Material material,
                                     @NotNull GTToolType tool, boolean mirrored, Object... recipe) {
        ItemStack toolStack = ToolHelper.get(tool, material);
        if (toolStack.isEmpty()) return;
        if (mirrored) { // todo mirrored
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("%s_%s", tool.name, material.getName()),
                    toolStack, recipe);
        } else {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("%s_%s", tool.name, material.getName()),
                    toolStack, recipe);
        }
    }

    /**
     * {@code D} is inferred as the dye key
     */
    public static void addDyeableToolRecipe(Consumer<FinishedRecipe> provider, @NotNull Material material,
                                            @NotNull GTToolType tool, boolean mirrored, Object... recipe) {
        ItemStack toolStack = ToolHelper.get(tool, material);
        if (toolStack.isEmpty()) return;
        for (var color : MarkerMaterials.Color.COLORS.entrySet()) {
            ToolHelper.getToolTag(toolStack).putInt(ToolHelper.TINT_COLOR_KEY, color.getKey().getTextColor());
            Object[] recipeWithDye = ArrayUtils.addAll(recipe, 'D',
                    new UnificationEntry(TagPrefix.dye, color.getValue()));

            if (mirrored) { // todo mirrored
                VanillaRecipeHelper.addShapedRecipe(provider,
                        String.format("%s_%s_%s", tool.name, material.getName(), color.getKey().getSerializedName()),
                        toolStack, recipeWithDye);
            } else {
                VanillaRecipeHelper.addShapedRecipe(provider,
                        String.format("%s_%s_%s", tool.name, material.getName(), color.getKey().getSerializedName()),
                        toolStack, recipeWithDye);
            }
        }
    }

    public static void registerCustomToolRecipes(Consumer<FinishedRecipe> provider) {
        registerFlintToolRecipes(provider);
        registerMortarRecipes(provider);
        registerSoftToolRecipes(provider);
        registerElectricRecipes(provider);

        SpecialRecipeBuilder.special(ToolHeadReplaceRecipe.SERIALIZER).save(provider,
                "gtceu:crafting/replace_tool_head");
    }

    private static void registerFlintToolRecipes(Consumer<FinishedRecipe> provider) {
        final UnificationEntry flint = new UnificationEntry(TagPrefix.gem, GTMaterials.Flint);
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

    private static void registerMortarRecipes(Consumer<FinishedRecipe> provider) {
        for (Material material : new Material[] {
                GTMaterials.Bronze, GTMaterials.Iron, GTMaterials.Invar, GTMaterials.Steel,
                GTMaterials.DamascusSteel, GTMaterials.CobaltBrass, GTMaterials.WroughtIron }) {

            addToolRecipe(provider, material, GTToolType.MORTAR, false,
                    " I ", "SIS", "SSS",
                    'I',
                    new UnificationEntry(material.hasProperty(PropertyKey.GEM) ? TagPrefix.gem : TagPrefix.ingot,
                            material),
                    'S', new ItemStack(Blocks.STONE));
        }
    }

    private static void registerSoftToolRecipes(Consumer<FinishedRecipe> provider) {
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
                        'I', new UnificationEntry(TagPrefix.ingot, material),
                        'S', stick);

                VanillaRecipeHelper.addShapedRecipe(provider, String.format("plunger_%s", material.getName()),
                        ToolHelper.getAndSetToolData(GTToolType.PLUNGER, material, 128 * (i << 1), 1, 4F, 0F),
                        "xPP", " SP", "S f",
                        'P', new UnificationEntry(TagPrefix.plate, material),
                        'S', rod);
            }
        }
    }

    private static void registerElectricRecipes(Consumer<FinishedRecipe> provider) {
        for (ItemEntry<? extends Item> batteryItem : batteryItems.get(LV)) {
            VanillaRecipeHelper.addShapedEnergyTransferRecipe(provider, true, false, true,
                    "prospector_lv_" + batteryItem.getId().getPath(),
                    Ingredient.of(batteryItem), GTItems.PROSPECTOR_LV.asStack(),
                    "EPS", "CDC", "PBP",
                    'E', GTItems.EMITTER_LV.asStack(),
                    'P', new UnificationEntry(plate, GTMaterials.Steel),
                    'S', GTItems.SENSOR_LV.asStack(),
                    'D', new UnificationEntry(plate, GTMaterials.Glass),
                    'C', CustomTags.LV_CIRCUITS,
                    'B', batteryItem.asStack());

            VanillaRecipeHelper.addShapedEnergyTransferRecipe(provider, true, false, true,
                    "lv_magnet_" + batteryItem.getId().getPath(),
                    Ingredient.of(batteryItem), GTItems.ITEM_MAGNET_LV.asStack(),
                    "MwM", "MBM", "CPC",
                    'M', new UnificationEntry(rod, GTMaterials.SteelMagnetic),
                    'P', new UnificationEntry(plate, GTMaterials.Steel),
                    'C', new UnificationEntry(cableGtSingle, GTMaterials.Tin),
                    'B', batteryItem.asStack());
        }

        for (ItemEntry<? extends Item> batteryItem : batteryItems.get(MV)) {
            VanillaRecipeHelper.addShapedEnergyTransferRecipe(provider, true, false, true,
                    "portable_scanner_" + batteryItem.getId().getPath(),
                    Ingredient.of(batteryItem), GTItems.PORTABLE_SCANNER.asStack(),
                    "EPS", "CDC", "PBP",
                    'E', GTItems.EMITTER_MV.asStack(),
                    'P', new UnificationEntry(plate, GTMaterials.Aluminium),
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
                    'P', new UnificationEntry(plate, GTMaterials.StainlessSteel),
                    'S', GTItems.SENSOR_HV.asStack(),
                    'D', GTItems.COVER_SCREEN.asStack(),
                    'C', CustomTags.HV_CIRCUITS,
                    'B', batteryItem.asStack());

            VanillaRecipeHelper.addShapedEnergyTransferRecipe(provider, true, false, true,
                    "hv_magnet_" + batteryItem.getId().getPath(),
                    Ingredient.of(batteryItem), GTItems.ITEM_MAGNET_HV.asStack(),
                    "MwM", "MBM", "CPC",
                    'M', new UnificationEntry(rod, GTMaterials.NeodymiumMagnetic),
                    'P', new UnificationEntry(plate, GTMaterials.StainlessSteel),
                    'C', new UnificationEntry(cableGtSingle, GTMaterials.Gold),
                    'B', batteryItem.asStack());
        }

        for (ItemEntry<? extends Item> batteryItem : batteryItems.get(LuV)) {
            VanillaRecipeHelper.addShapedEnergyTransferRecipe(provider, true, false, true,
                    "prospector_luv_" + batteryItem.getId().getPath(),
                    Ingredient.of(batteryItem), GTItems.PROSPECTOR_LuV.asStack(),
                    "EPS", "CDC", "PBP",
                    'E', GTItems.EMITTER_LuV.asStack(),
                    'P', new UnificationEntry(plate, GTMaterials.RhodiumPlatedPalladium),
                    'S', GTItems.SENSOR_LuV.asStack(),
                    'D', GTItems.COVER_SCREEN.asStack(),
                    'C', CustomTags.LuV_CIRCUITS,
                    'B', batteryItem.asStack());
        }
    }
}
