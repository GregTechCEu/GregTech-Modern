package com.gregtechceu.gtceu.integration.overgeared;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraftforge.common.Tags;
import net.stirdrem.overgeared.datagen.ShapedForgingRecipeBuilder;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.Conditions.hasToolProperty;
import static com.tterrag.registrate.providers.RegistrateRecipeProvider.getHasName;
import static com.tterrag.registrate.providers.RegistrateRecipeProvider.has;

public class OvergearedCompatRecipes {

    public static void init(Consumer<FinishedRecipe> consumer) {
        generateRecipes(consumer, GTToolType.PICKAXE, toolHeadPickaxe, 3, "###", "   ", "   ");
        generateRecipes(consumer, GTToolType.AXE, toolHeadAxe, 3, "## ", "#  ", "   ");
        generateRecipes(consumer, GTToolType.SHOVEL, toolHeadShovel, 3, "#  ", "   ", "   ");
        generateRecipes(consumer, GTToolType.HOE, toolHeadHoe, 3, "## ", "   ", "   ");
        generateRecipes(consumer, GTToolType.SWORD, toolBladeSword, 3, "#  ", "#  ", "   ");
    }

    public static void generateRecipes(Consumer<FinishedRecipe> consumer, GTToolType tool, TagPrefix prefix, int hits,
                                       String... patterns) {
        for (Material material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
            if (hasToolProperty.and(mat -> mat.hasFlag(MaterialFlags.GENERATE_PLATE))
                    .and(mat -> mat.getProperty(PropertyKey.TOOL).hasType(tool)).test(material)) {

                if (material.hasHotIngot()) {
                    ShapedForgingRecipeBuilder
                            .shaped(RecipeCategory.MISC, ChemicalHelper.get(prefix, material).getItem(), hits)
                            .pattern(patterns[0])
                            .pattern(patterns[1])
                            .pattern(patterns[2])
                            .define('#', ChemicalHelper.get(ingotHot, material).getItem())
                            .setQuality(true)
                            .showNotification(true)
                            .unlockedBy(getHasName(ChemicalHelper.get(ingotHot, material).getItem()),
                                    has(ChemicalHelper.get(ingotHot, material).getItem()))
                            .save(consumer);
                } else if (!material.hasProperty(PropertyKey.GEM)) {
                    ShapedForgingRecipeBuilder
                            .shaped(RecipeCategory.MISC, ChemicalHelper.get(prefix, material).getItem(), hits)
                            .pattern(patterns[0])
                            .pattern(patterns[1])
                            .pattern(patterns[2])
                            .define('#', ChemicalHelper.get(ingot, material).getItem())
                            .setQuality(true)
                            .showNotification(true)
                            .unlockedBy(getHasName(ChemicalHelper.get(ingot, material).getItem()),
                                    has(ChemicalHelper.get(ingot, material).getItem()))
                            .save(consumer);
                } else if (material.hasProperty(PropertyKey.GEM)) {
                    ShapedForgingRecipeBuilder
                            .shaped(RecipeCategory.MISC, ChemicalHelper.get(prefix, material).getItem(), hits)
                            .pattern(patterns[0])
                            .pattern(patterns[1])
                            .pattern(patterns[2])
                            .define('#', ChemicalHelper.get(gem, material).getItem())
                            .setQuality(true)
                            .showNotification(true)
                            .unlockedBy(getHasName(ChemicalHelper.get(gem, material).getItem()),
                                    has(ChemicalHelper.get(gem, material).getItem()))
                            .save(consumer);
                }

                OvergearedGTToolShapelessRecipeBuilder
                        .shapeless(RecipeCategory.MISC, ToolHelper.get(tool, material).getItem())
                        .requires(ChemicalHelper.get(prefix, material).getItem())
                        .requires(Tags.Items.RODS_WOODEN)
                        .unlockedBy(getHasName(ChemicalHelper.get(prefix, material).getItem()),
                                has(ChemicalHelper.get(prefix, material).getItem()))
                        .save(consumer, GTCEu.id(ToolHelper.get(tool, material).getDescriptionId() + "overgeared_bonus"));
            }
        }
    }
}
