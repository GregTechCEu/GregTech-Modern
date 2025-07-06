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
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import com.tterrag.registrate.util.entry.ItemEntry;
import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;

public final class ToolRecipeHandler {

    public static final Int2ReferenceMap<ItemEntry<? extends Item>> powerUnitItems = new Int2ReferenceArrayMap<>(
            GTValues.tiersBetween(GTValues.LV, GTValues.IV),
            new ItemEntry[] { GTItems.POWER_UNIT_LV, GTItems.POWER_UNIT_MV, GTItems.POWER_UNIT_HV,
                    GTItems.POWER_UNIT_EV, GTItems.POWER_UNIT_IV });

    public static final Material[] softMaterials = new Material[] {
            GTMaterials.Wood, GTMaterials.Rubber, GTMaterials.Polyethylene,
            GTMaterials.Polytetrafluoroethylene, GTMaterials.Polybenzimidazole,
            GTMaterials.SiliconeRubber, GTMaterials.StyreneButadieneRubber
    };

    private ToolRecipeHandler() {}

    public static void run(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        ToolProperty property = material.getProperty(PropertyKey.TOOL);
        if (property == null) {
            return;
        }

        processTool(provider, material);
        processElectricTool(provider, property, material);
    }

    private static void processTool(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(plate)) {
            return;
        }

        ItemStack stick = new ItemStack(Items.STICK);
        MaterialEntry plate = new MaterialEntry(TagPrefix.plate, material);
        MaterialEntry ingot = new MaterialEntry(
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

            addArmorRecipe(provider, material, ArmorItem.Type.HELMET,
                    "PPP", "PhP",
                    'P', plate);
            addArmorRecipe(provider, material, ArmorItem.Type.CHESTPLATE,
                    "PhP", "PPP", "PPP",
                    'P', plate);
            addArmorRecipe(provider, material, ArmorItem.Type.LEGGINGS,
                    "PPP", "PhP", "P P",
                    'P', plate);
            addArmorRecipe(provider, material, ArmorItem.Type.BOOTS,
                    "P P", "PhP",
                    'P', plate);
        } else {
            GTCEu.LOGGER.info(
                    "Did not find plate for {}, skipping mining hammer, spade, saw, axe, hoe, pickaxe, scythe, shovel, sword, hammer, file, knife, wrench recipes",
                    material.getName());
        }

        if (material.hasFlag(GENERATE_ROD)) {
            MaterialEntry rod = new MaterialEntry(TagPrefix.rod, material);

            if (material.hasFlag(GENERATE_PLATE)) {
                addToolRecipe(provider, material, GTToolType.BUTCHERY_KNIFE, false,
                        "PPf", "PP ", "Sh ",
                        'P', plate,
                        'S', rod);

                if (material.hasFlag(GENERATE_BOLT_SCREW)) {
                    addToolRecipe(provider, material, GTToolType.WIRE_CUTTER, false,
                            "PfP", "hPd", "STS",
                            'P', plate,
                            'T', new MaterialEntry(TagPrefix.screw, material),
                            'S', rod);
                } else if (!ArrayUtils.contains(softMaterials, material)) {
                    GTCEu.LOGGER
                            .info("Did not find bolt for {}, skipping wirecutter recipe", material.getName());
                }
            } else {
                GTCEu.LOGGER.info("Did not find plate for {}, skipping wirecutter, butchery knife recipes",
                        material.getName());
            }

            addToolRecipe(provider, material, GTToolType.SCREWDRIVER, true,
                    " fS", " Sh", "W  ",
                    'S', rod,
                    'W', stick);

            addDyeableToolRecipe(provider, material, GTToolType.CROWBAR, true,
                    "hDS", "DSD", "SDf",
                    'S', rod);
        } else if (!ArrayUtils.contains(softMaterials, material)) {
            GTCEu.LOGGER.warn("Did not find rod for " + material.getName() +
                    ", skipping wirecutter, butchery knife, screwdriver, crowbar recipes");
        }
    }

    private static void processElectricTool(@NotNull Consumer<FinishedRecipe> provider, @NotNull ToolProperty property,
                                            @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(plate)) {
            return;
        }

        final int voltageMultiplier = material.getBlastTemperature() > 2800 ? GTValues.VA[GTValues.LV] :
                GTValues.VA[GTValues.ULV];
        TagPrefix toolPrefix;

        if (material.hasFlag(GENERATE_PLATE)) {
            final MaterialEntry plate = new MaterialEntry(TagPrefix.plate, material);
            final MaterialEntry steelPlate = new MaterialEntry(TagPrefix.plate, GTMaterials.Steel);
            final MaterialEntry steelRing = new MaterialEntry(TagPrefix.ring, GTMaterials.Steel);

            // drill
            if (property.hasType(GTToolType.DRILL_LV)) {
                toolPrefix = TagPrefix.toolHeadDrill;
                VanillaRecipeHelper.addShapedRecipe(provider, String.format("drill_head_%s", material.getName()),
                        ChemicalHelper.get(toolPrefix, material),
                        "XSX", "XSX", "ShS",
                        'X', plate,
                        'S', steelPlate);

                addElectricToolRecipe(provider, toolPrefix, new GTToolType[] { GTToolType.DRILL_LV, GTToolType.DRILL_MV,
                        GTToolType.DRILL_HV, GTToolType.DRILL_EV, GTToolType.DRILL_IV }, material);
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

                addElectricToolRecipe(provider, toolPrefix, new GTToolType[] { GTToolType.CHAINSAW_LV }, material);
            }

            // wrench
            if (property.hasType(GTToolType.WRENCH_LV)) {
                toolPrefix = TagPrefix.toolHeadWrench;
                addElectricToolRecipe(provider, toolPrefix,
                        new GTToolType[] { GTToolType.WRENCH_LV, GTToolType.WRENCH_HV, GTToolType.WRENCH_IV },
                        material);

                VanillaRecipeHelper.addShapedRecipe(provider, String.format("wrench_head_%s", material.getName()),
                        ChemicalHelper.get(toolPrefix, material),
                        "hXW", "XRX", "WXd",
                        'X', plate,
                        'R', steelRing,
                        'W', new MaterialEntry(TagPrefix.screw, GTMaterials.Steel));
            }

            // electric wire cutters
            if (property.hasType(GTToolType.WIRE_CUTTER_LV)) {
                toolPrefix = toolHeadWireCutter;
                addElectricToolRecipe(provider, toolPrefix,
                        new GTToolType[] { GTToolType.WIRE_CUTTER_LV, GTToolType.WIRE_CUTTER_HV,
                                GTToolType.WIRE_CUTTER_IV },
                        material);

                VanillaRecipeHelper.addShapedRecipe(provider, String.format("wirecutter_head_%s", material.getName()),
                        ChemicalHelper.get(toolPrefix, material),
                        "XfX", "X X", "SRS",
                        'X', plate,
                        'R', steelRing,
                        'S', new MaterialEntry(screw, GTMaterials.Steel));
            }

            // buzzsaw
            if (property.hasType(GTToolType.BUZZSAW)) {
                toolPrefix = TagPrefix.toolHeadBuzzSaw;
                addElectricToolRecipe(provider, toolPrefix, new GTToolType[] { GTToolType.BUZZSAW }, material);

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
                    GTCEu.LOGGER.warn("Did not find gear for " + material.getName() +
                            ", skipping gear -> buzzsaw blade recipe");
                }
            }
        } else {
            GTCEu.LOGGER.warn("Did not find plate for " + material.getName() +
                    ", skipping electric drill, chainsaw, wrench, wirecutter, buzzsaw recipe");
        }

        // screwdriver
        if (property.hasType(GTToolType.SCREWDRIVER_LV)) {
            if (material.hasFlag(GENERATE_LONG_ROD)) {
                toolPrefix = TagPrefix.toolHeadScrewdriver;
                addElectricToolRecipe(provider, toolPrefix, new GTToolType[] { GTToolType.SCREWDRIVER_LV }, material);

                VanillaRecipeHelper.addShapedRecipe(provider, String.format("screwdriver_tip_%s", material.getName()),
                        ChemicalHelper.get(toolPrefix, material),
                        "fR", " h",
                        'R', new MaterialEntry(TagPrefix.rodLong, material));
            } else {
                GTCEu.LOGGER.warn("Did not find long rod for " + material.getName() +
                        ", skipping electric screwdriver recipe");
            }
        }
    }

    private static void addElectricToolRecipe(@NotNull Consumer<FinishedRecipe> provider, @NotNull TagPrefix toolHead,
                                              @NotNull GTToolType @NotNull [] toolItems,
                                              @NotNull Material material) {
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
                    'H', new MaterialEntry(toolHead, material),
                    'U', powerUnitStack);
        }
    }

    public static void addToolRecipe(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material,
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

    public static void addArmorRecipe(Consumer<FinishedRecipe> provider, @NotNull Material material,
                                      @NotNull ArmorItem.Type armor, Object... recipe) {
        ItemStack armorStack = ToolHelper.getArmor(armor, material);
        if (armorStack.isEmpty()) return;
        VanillaRecipeHelper.addShapedRecipe(provider, String.format("%s_%s", armor.getName(), material.getName()),
                armorStack, recipe);
    }

    /**
     * {@code D} is inferred as the dye key
     */
    public static void addDyeableToolRecipe(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material,
                                            @NotNull GTToolType tool, boolean mirrored, Object... recipe) {
        ItemStack toolStack = ToolHelper.get(tool, material);
        if (toolStack.isEmpty()) return;
        for (var color : MarkerMaterials.Color.COLORS.entrySet()) {
            ToolHelper.getToolTag(toolStack).putInt(ToolHelper.TINT_COLOR_KEY, color.getKey().getTextColor());
            Object[] recipeWithDye = ArrayUtils.addAll(recipe, 'D',
                    new MaterialEntry(TagPrefix.dye, color.getValue()));

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
}
