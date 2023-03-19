package com.gregtechceu.gtceu.data.recipe.handler;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import net.minecraft.data.recipes.FinishedRecipe;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey.GEM;
import static com.gregtechceu.gtceu.api.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.libs.GTItems.TOOL_ITEMS;
import static com.gregtechceu.gtceu.common.libs.GTMaterials.Wood;

public class ToolRecipeHandler {

    public static void init(Consumer<FinishedRecipe> provider) {
        TagPrefix.plate.executeHandler(PropertyKey.TOOL, (tagPrefix, material, property) -> processTool(tagPrefix, material, property, provider));
    }

    private static void processTool(TagPrefix prefix, Material material, ToolProperty property, Consumer<FinishedRecipe> provider) {
        UnificationEntry ingot = new UnificationEntry(material.hasProperty(GEM) ? gem : TagPrefix.ingot, material);
        UnificationEntry stick = new UnificationEntry(TagPrefix.stick, Wood);
        UnificationEntry plate = material.hasProperty(GEM) ? ingot : new UnificationEntry(TagPrefix.plate, material);

        if (property.hasType(GTToolType.MORTAR)) {
            addToolRecipe(provider, material, GTToolType.MORTAR, property,
                    " I ", "SIS", "SSS",
                    'I', ingot,
                    'S', stone);
        }

        if (property.hasType(GTToolType.SOFT_MALLET)) {
            if (material == Wood) {
                addToolRecipe(provider, material, GTToolType.SOFT_MALLET, property,
                        "II ", "IIS", "II ",
                        'I', new UnificationEntry(plank, material),
                        'S', stick);
            } else {
                addToolRecipe(provider, material, GTToolType.SOFT_MALLET, property,
                        "II ", "IIS", "II ",
                        'I', ingot,
                        'S', stick);
            }
        }

        if (material.hasFlag(GENERATE_PLATE)) {
//            addToolRecipe(provider, material, GTToolType.MINING_HAMMER, property,
//                    "PPf", "PPS", "PPh",
//                    'P', plate,
//                    'S', stick);

//            addToolRecipe(provider, material, GTToolType.SPADE, property,
//                    "fPh", "PSP", " S ",
//                    'P', plate,
//                    'S', stick);

            if (property.hasType(GTToolType.SAW))
                addToolRecipe(provider, material, GTToolType.SAW, property,
                    "PPS", "fhS",
                    'P', plate,
                    'S', stick);

//            addToolRecipe(provider, material, GTToolType.AXE, property,
//                    "PIh", "PS ", "fS ",
//                    'P', plate,
//                    'I', ingot,
//                    'S', stick);

//            addToolRecipe(provider, material, GTToolType.HOE, property,
//                    "PIh", "fS ", " S ",
//                    'P', plate,
//                    'I', ingot,
//                    'S', stick);

//            addToolRecipe(provider, material, GTToolType.PICKAXE, property,
//                    "PII", "fSh", " S ",
//                    'P', plate,
//                    'I', ingot,
//                    'S', stick);

            if (property.hasType(GTToolType.SCYTHE))
                addToolRecipe(provider, material, GTToolType.SCYTHE, property,
                        "PPI", "fSh", " S ",
                        'P', plate,
                        'I', ingot,
                        'S', stick);

//            addToolRecipe(provider, material, GTToolType.SHOVEL, property,
//                    "fPh", " S ", " S ",
//                    'P', plate,
//                    'S', stick);

//            addToolRecipe(provider, material, GTToolType.SWORD, property,
//                    " P ", "fPh", " S ",
//                    'P', plate,
//                    'S', stick);

            if (property.hasType(GTToolType.HARD_HAMMER))
                addToolRecipe(provider, material, GTToolType.HARD_HAMMER, property,
                        "II ", "IIS", "II ",
                        'I', ingot,
                        'S', stick);

            if (property.hasType(GTToolType.FILE))
                addToolRecipe(provider, material, GTToolType.FILE, property,
                        " P ", " P " , " S ",
                        'P', plate,
                        'S', stick);

            if (property.hasType(GTToolType.KNIFE))
                addToolRecipe(provider, material, GTToolType.KNIFE, property,
                        "fPh", " S ",
                        'P', plate,
                        'S', stick);

            if (property.hasType(GTToolType.WRENCH))
                addToolRecipe(provider, material, GTToolType.WRENCH, property,
                        "PhP", " P ", " P ",
                        'P', plate);
        }

        if (material.hasFlag(GENERATE_ROD)) {
            UnificationEntry rod = new UnificationEntry(TagPrefix.stick, material);

            if (material.hasFlag(GENERATE_PLATE)) {
                if (property.hasType(GTToolType.BUTCHERY_KNIFE))
                    addToolRecipe(provider, material, GTToolType.BUTCHERY_KNIFE, property,
                            "PPf", "PP ", "Sh ",
                            'P', plate,
                            'S', rod);

                if (material.hasFlag(GENERATE_BOLT_SCREW) && property.hasType(GTToolType.WIRE_CUTTER)) {
                    addToolRecipe(provider, material, GTToolType.WIRE_CUTTER, property,
                            "PfP", "hPd", "STS",
                            'P', plate,
                            'T', new UnificationEntry(screw, material),
                            'S', rod);
                }
            }

            if (property.hasType(GTToolType.SCREWDRIVER))
                addToolRecipe(provider, material, GTToolType.SCREWDRIVER, property,
                        " fS", " Sh", "W  ",
                        'S', rod,
                        'W', stick);

            if (property.hasType(GTToolType.CROWBAR))
                addToolRecipe(provider, material, GTToolType.CROWBAR, property,
                        "hDS", "DSD", "SDf",
                        'S', rod,
                        'D', new UnificationEntry(dye, MarkerMaterials.Color.Blue));
        }
    }


    public static void addToolRecipe(Consumer<FinishedRecipe> provider, @Nonnull Material material, @Nonnull GTToolType type, ToolProperty property, Object... recipe) {
        var tool = TOOL_ITEMS.get(property.getTier(material), type);
        if (tool == null) {
            GTCEu.LOGGER.error("Couldn't find tool {}, {}", material, type);
            return;
        }
        VanillaRecipeHelper.addShapedRecipe(provider, String.format("%s_%s", type.name, material),
                tool.asStack(), recipe);
    }

}
