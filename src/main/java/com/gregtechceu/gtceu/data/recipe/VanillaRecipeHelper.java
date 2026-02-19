package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.material.ChemicalHelper;
import com.gregtechceu.gtceu.api.material.material.ItemMaterialData;
import com.gregtechceu.gtceu.api.material.material.MarkerMaterial;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.material.material.stack.ItemMaterialInfo;
import com.gregtechceu.gtceu.api.material.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.material.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.common.recipe.builder.*;

import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import it.unimi.dsi.fastutil.chars.Char2IntOpenHashMap;
import it.unimi.dsi.fastutil.chars.CharArraySet;
import it.unimi.dsi.fastutil.chars.CharSet;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import org.jetbrains.annotations.NotNull;

public class VanillaRecipeHelper {

    public static void addSmeltingRecipe(RecipeOutput provider, @NotNull String regName, TagKey<Item> input,
                                         ItemStack output) {
        addSmeltingRecipe(provider, GTCEu.id(regName), input, output);
    }

    public static void addSmeltingRecipe(RecipeOutput provider, @NotNull ResourceLocation regName,
                                         TagKey<Item> input, ItemStack output) {
        addSmeltingRecipe(provider, regName, input, output, 0.0f);
    }

    public static void addSmeltingRecipe(RecipeOutput provider, @NotNull String regName, TagKey<Item> input,
                                         ItemStack output, float experience) {
        addSmeltingRecipe(provider, GTCEu.id(regName), input, output, experience);
    }

    public static void addSmeltingRecipe(RecipeOutput provider, @NotNull String regName, Ingredient input,
                                         ItemStack output, float experience) {
        addSmeltingRecipe(provider, GTCEu.id(regName), input, output, experience);
    }

    public static void addSmeltingRecipe(RecipeOutput provider, @NotNull ResourceLocation regName,
                                         Ingredient input, ItemStack output, float experience) {
        SimpleCookingRecipeBuilder.smelting(regName).input(input).output(output).cookingTime(200).experience(experience)
                .save(provider);
    }

    public static void addSmeltingRecipe(RecipeOutput provider, @NotNull ResourceLocation regName,
                                         TagKey<Item> input, ItemStack output, float experience) {
        SimpleCookingRecipeBuilder.smelting(regName).input(input).output(output).cookingTime(200).experience(experience)
                .save(provider);
    }

    public static void addSmeltingRecipe(RecipeOutput provider, @NotNull String regName, ItemStack input,
                                         ItemStack output) {
        addSmeltingRecipe(provider, GTCEu.id(regName), input, output, 0.0f);
    }

    public static void addSmeltingRecipe(RecipeOutput provider, @NotNull String regName, Item input,
                                         Item output) {
        addSmeltingRecipe(provider, GTCEu.id(regName), input.getDefaultInstance(), output.getDefaultInstance(), 0.0f);
    }

    public static void addSmeltingRecipe(RecipeOutput provider, @NotNull String regName, Item input,
                                         Item output, float experience) {
        addSmeltingRecipe(provider, GTCEu.id(regName), input.getDefaultInstance(), output.getDefaultInstance(),
                experience);
    }

    public static void addSmeltingRecipe(RecipeOutput provider, @NotNull String regName, ItemStack input,
                                         ItemStack output, float experience) {
        addSmeltingRecipe(provider, GTCEu.id(regName), input, output, experience);
    }

    public static void addSmeltingRecipe(RecipeOutput provider, @NotNull ResourceLocation regName,
                                         ItemStack input, ItemStack output, float experience) {
        SimpleCookingRecipeBuilder.smelting(regName).input(input).output(output).cookingTime(200).experience(experience)
                .save(provider);
    }

    public static void addBlastingRecipe(RecipeOutput provider, @NotNull String regName, TagKey<Item> input,
                                         ItemStack output) {
        addBlastingRecipe(provider, GTCEu.id(regName), input, output);
    }

    public static void addBlastingRecipe(RecipeOutput provider, @NotNull ResourceLocation regName,
                                         TagKey<Item> input, ItemStack output) {
        addBlastingRecipe(provider, regName, input, output, 0.0f);
    }

    public static void addBlastingRecipe(RecipeOutput provider, @NotNull String regName, TagKey<Item> input,
                                         ItemStack output, float experience) {
        addBlastingRecipe(provider, GTCEu.id(regName), input, output, experience);
    }

    public static void addBlastingRecipe(RecipeOutput provider, @NotNull String regName, Ingredient input,
                                         ItemStack output, float experience) {
        addBlastingRecipe(provider, GTCEu.id(regName), input, output, experience);
    }

    public static void addBlastingRecipe(RecipeOutput provider, @NotNull ResourceLocation regName,
                                         Ingredient input, ItemStack output, float experience) {
        SimpleCookingRecipeBuilder.smelting(regName).input(input).output(output).cookingTime(200).experience(experience)
                .save(provider);
    }

    public static void addBlastingRecipe(RecipeOutput provider, @NotNull ResourceLocation regName,
                                         TagKey<Item> input, ItemStack output, float experience) {
        SimpleCookingRecipeBuilder.smelting(regName).input(input).output(output).cookingTime(200).experience(experience)
                .save(provider);
    }

    public static void addBlastingRecipe(RecipeOutput provider, @NotNull String regName, ItemStack input,
                                         ItemStack output) {
        addBlastingRecipe(provider, GTCEu.id(regName), input, output, 0.0f);
    }

    public static void addBlastingRecipe(RecipeOutput provider, @NotNull String regName, Item input,
                                         Item output) {
        addBlastingRecipe(provider, GTCEu.id(regName), input.getDefaultInstance(), output.getDefaultInstance(), 0.0f);
    }

    public static void addBlastingRecipe(RecipeOutput provider, @NotNull String regName, Item input,
                                         Item output, float experience) {
        addBlastingRecipe(provider, GTCEu.id(regName), input.getDefaultInstance(), output.getDefaultInstance(),
                experience);
    }

    public static void addBlastingRecipe(RecipeOutput provider, @NotNull String regName, ItemStack input,
                                         ItemStack output, float experience) {
        addBlastingRecipe(provider, GTCEu.id(regName), input, output, experience);
    }

    public static void addBlastingRecipe(RecipeOutput provider, @NotNull ResourceLocation regName,
                                         ItemStack input, ItemStack output, float experience) {
        SimpleCookingRecipeBuilder.blasting(regName).input(input).output(output).cookingTime(200).experience(experience)
                .save(provider);
    }

    public static void addSmokingRecipe(RecipeOutput provider, @NotNull String regName, TagKey<Item> input,
                                        ItemStack output) {
        addSmokingRecipe(provider, GTCEu.id(regName), input, output);
    }

    public static void addSmokingRecipe(RecipeOutput provider, @NotNull ResourceLocation regName,
                                        TagKey<Item> input, ItemStack output) {
        addSmokingRecipe(provider, regName, input, output, 0.0f);
    }

    public static void addSmokingRecipe(RecipeOutput provider, @NotNull String regName, TagKey<Item> input,
                                        ItemStack output, float experience) {
        addSmokingRecipe(provider, GTCEu.id(regName), input, output, experience);
    }

    public static void addSmokingRecipe(RecipeOutput provider, @NotNull String regName, Ingredient input,
                                        ItemStack output, float experience) {
        addSmokingRecipe(provider, GTCEu.id(regName), input, output, experience);
    }

    public static void addSmokingRecipe(RecipeOutput provider, @NotNull ResourceLocation regName,
                                        Ingredient input, ItemStack output, float experience) {
        SimpleCookingRecipeBuilder.smelting(regName).input(input).output(output).cookingTime(200).experience(experience)
                .save(provider);
    }

    public static void addSmokingRecipe(RecipeOutput provider, @NotNull ResourceLocation regName,
                                        TagKey<Item> input, ItemStack output, float experience) {
        SimpleCookingRecipeBuilder.smelting(regName).input(input).output(output).cookingTime(200).experience(experience)
                .save(provider);
    }

    public static void addSmokingRecipe(RecipeOutput provider, @NotNull String regName, ItemStack input,
                                        ItemStack output) {
        addSmokingRecipe(provider, GTCEu.id(regName), input, output, 0.0f);
    }

    public static void addSmokingRecipe(RecipeOutput provider, @NotNull String regName, Item input,
                                        Item output) {
        addSmokingRecipe(provider, GTCEu.id(regName), input.getDefaultInstance(), output.getDefaultInstance(), 0.0f);
    }

    public static void addSmokingRecipe(RecipeOutput provider, @NotNull String regName, Item input,
                                        Item output, float experience) {
        addSmokingRecipe(provider, GTCEu.id(regName), input.getDefaultInstance(), output.getDefaultInstance(),
                experience);
    }

    public static void addSmokingRecipe(RecipeOutput provider, @NotNull String regName, ItemStack input,
                                        ItemStack output, float experience) {
        addSmokingRecipe(provider, GTCEu.id(regName), input, output, experience);
    }

    public static void addSmokingRecipe(RecipeOutput provider, @NotNull ResourceLocation regName,
                                        ItemStack input, ItemStack output, float experience) {
        SimpleCookingRecipeBuilder.smoking(regName).input(input).output(output).cookingTime(200).experience(experience)
                .save(provider);
    }

    public static void addCampfireRecipe(RecipeOutput provider, @NotNull String regName, ItemStack input,
                                         ItemStack output, float experience) {
        addCampfireRecipe(provider, GTCEu.id(regName), input, output, experience);
    }

    public static void addCampfireRecipe(RecipeOutput provider, @NotNull String regName, ItemStack input,
                                         ItemStack output) {
        addCampfireRecipe(provider, GTCEu.id(regName), input, output, 0);
    }

    public static void addCampfireRecipe(RecipeOutput provider, @NotNull ResourceLocation regName,
                                         ItemStack input, ItemStack output, float experience) {
        SimpleCookingRecipeBuilder.campfireCooking(regName).input(input).output(output).cookingTime(100)
                .experience(experience)
                .save(provider);
    }

    public static void addCampfireRecipe(RecipeOutput provider, @NotNull String regName, TagKey<Item> input,
                                         ItemStack output, float experience) {
        addCampfireRecipe(provider, GTCEu.id(regName), input, output, experience);
    }

    public static void addCampfireRecipe(RecipeOutput provider, @NotNull String regName, TagKey<Item> input,
                                         ItemStack output) {
        addCampfireRecipe(provider, GTCEu.id(regName), input, output, 0);
    }

    public static void addCampfireRecipe(RecipeOutput provider, @NotNull ResourceLocation regName,
                                         TagKey<Item> input, ItemStack output, float experience) {
        SimpleCookingRecipeBuilder.campfireCooking(regName).input(input).output(output).cookingTime(100)
                .experience(experience)
                .save(provider);
    }

    /**
     * Adds a shaped recipe which clears the components of the outputs
     *
     * @see VanillaRecipeHelper#addShapedRecipe(RecipeOutput, String, ItemStack, Object...)
     */
    public static void addShapedNBTClearingRecipe(RecipeOutput provider, String regName, ItemStack result,
                                                  Object... recipe) {
        addStrictShapedRecipe(provider, regName, result, recipe);
    }

    /**
     * @see #addShapedRecipe(RecipeOutput, boolean, boolean, ResourceLocation, ItemStack, Object...)
     */
    public static void addShapedRecipe(RecipeOutput provider, @NotNull String regName,
                                       @NotNull ItemStack result, @NotNull Object... recipe) {
        addShapedRecipe(provider, GTCEu.id(regName), result, recipe);
    }

    /**
     * @see #addShapedRecipe(RecipeOutput, boolean, boolean, ResourceLocation, ItemStack, Object...)
     */
    public static void addShapedRecipe(RecipeOutput provider, @NotNull ResourceLocation regName,
                                       @NotNull ItemStack result, @NotNull Object... recipe) {
        addShapedRecipe(provider, false, regName, result, recipe);
    }

    /**
     * @see #addShapedRecipe(RecipeOutput, boolean, boolean, ResourceLocation, ItemStack, Object...)
     */
    public static void addStrictShapedRecipe(RecipeOutput provider, @NotNull String regName,
                                             @NotNull ItemStack result, @NotNull Object... recipe) {
        addStrictShapedRecipe(provider, GTCEu.id(regName), result, recipe);
    }

    /**
     * @see #addShapedRecipe(RecipeOutput, boolean, boolean, ResourceLocation, ItemStack, Object...)
     */
    public static void addStrictShapedRecipe(RecipeOutput provider, @NotNull ResourceLocation regName,
                                             @NotNull ItemStack result, @NotNull Object... recipe) {
        addStrictShapedRecipe(provider, false, regName, result, recipe);
    }

    /**
     * @see #addShapedRecipe(RecipeOutput, boolean, boolean, ResourceLocation, ItemStack, Object...)
     */
    public static void addStrictSizeShapedRecipe(RecipeOutput provider, @NotNull String regName,
                                                 @NotNull ItemStack result, @NotNull Object... recipe) {
        addStrictSizeShapedRecipe(provider, GTCEu.id(regName), result, recipe);
    }

    /**
     * @see #addShapedRecipe(RecipeOutput, boolean, boolean, ResourceLocation, ItemStack, Object...)
     */
    public static void addStrictSizeShapedRecipe(RecipeOutput provider, boolean setMaterialInfoData,
                                                 @NotNull String regName,
                                                 @NotNull ItemStack result, @NotNull Object... recipe) {
        addStrictSizeShapedRecipe(provider, setMaterialInfoData, GTCEu.id(regName), result, recipe);
    }

    /**
     * @see #addShapedRecipe(RecipeOutput, boolean, boolean, ResourceLocation, ItemStack, Object...)
     */
    public static void addStrictSizeShapedRecipe(RecipeOutput provider, @NotNull ResourceLocation regName,
                                                 @NotNull ItemStack result, @NotNull Object... recipe) {
        addStrictSizeShapedRecipe(provider, false, regName, result, recipe);
    }

    /**
     * Adds Shaped Crafting Recipes.
     * <p/>
     * For Enums - {@link Enum#name()} is called.
     * <p/>
     * For {@link MaterialEntry} - {@link MaterialEntry#toString()} is called.
     * <p/>
     * Base tool names are as follows:
     * <ul>
     * <li>{@code 'c'} - {@code craftingToolCrowbar}</li>
     * <li>{@code 'd'} - {@code craftingToolScrewdriver}</li>
     * <li>{@code 'f'} - {@code craftingToolFile}</li>
     * <li>{@code 'h'} - {@code craftingToolHardHammer}</li>
     * <li>{@code 'k'} - {@code craftingToolKnife}</li>
     * <li>{@code 'm'} - {@code craftingToolMortar}</li>
     * <li>{@code 'r'} - {@code craftingToolSoftHammer}</li>
     * <li>{@code 's'} - {@code craftingToolSaw}</li>
     * <li>{@code 'w'} - {@code craftingToolWrench}</li>
     * <li>{@code 'x'} - {@code craftingToolWireCutter}</li>
     * </ul>
     *
     * @param setMaterialInfoData whether to add material decomposition information to the recipe output
     *
     * @param regName             the registry name for the recipe
     * @param result              the output for the recipe
     * @param recipe              the contents of the recipe
     */
    public static void addShapedRecipe(RecipeOutput provider, boolean setMaterialInfoData, boolean isStrict,
                                       @NotNull ResourceLocation regName, @NotNull ItemStack result,
                                       @NotNull Object... recipe) {
        var builder = new ShapedRecipeBuilder(regName).output(result);
        builder.isStrict(isStrict);
        final CharSet tools = ToolHelper.getToolSymbols();
        CharSet foundTools = new CharArraySet(9);
        for (int i = 0; i < recipe.length; i++) {
            var o = recipe[i];
            if (o instanceof String pattern) {
                builder.pattern(pattern);
                for (char c : pattern.toCharArray()) {
                    if (tools.contains(c)) {
                        foundTools.add(c);
                    }
                }
            }
            if (o instanceof String[] pattern) {
                for (String s : pattern) {
                    builder.pattern(s);
                    for (char c : s.toCharArray()) {
                        if (tools.contains(c)) {
                            foundTools.add(c);
                        }
                    }
                }
            }
            if (o instanceof Character sign) {
                var content = recipe[i + 1];
                i++;
                switch (content) {
                    case Ingredient ingredient -> builder.define(sign, ingredient);
                    case ICustomIngredient ingredient -> builder.define(sign, ingredient.toVanilla());
                    case ItemStack itemStack -> builder.define(sign, itemStack);
                    case TagKey<?> key when key.isFor(Registries.ITEM) -> builder.define(sign, (TagKey<Item>) key);
                    case TagPrefix prefix -> {
                        if (!prefix.getItemParentTags().isEmpty()) {
                            builder.define(sign, prefix.getItemParentTags().getFirst());
                        }
                    }
                    case ItemLike itemLike -> builder.define(sign, itemLike);
                    case MaterialEntry(TagPrefix tagPrefix, Material material) -> {
                        TagKey<Item> tag = ChemicalHelper.getTag(tagPrefix, material);
                        if (tag != null) {
                            builder.define(sign, tag);
                        } else builder.define(sign, ChemicalHelper.get(tagPrefix, material));
                    }
                    default -> {}
                }
            }
        }
        for (var it = foundTools.iterator(); it.hasNext();) {
            char c = it.nextChar();
            builder.define(c, ToolHelper.getToolFromSymbol(c).craftingTags.get(0));
        }
        builder.save(provider);

        if (setMaterialInfoData) {
            ItemMaterialData.registerMaterialInfo(result.getItem(), getRecyclingIngredients(result.getCount(), recipe));
        }
    }

    /**
     * @see #addShapedRecipe(RecipeOutput, boolean, boolean, ResourceLocation, ItemStack, Object...)
     */
    public static void addShapedRecipe(RecipeOutput provider, boolean setMaterialInfoData,
                                       @NotNull String regName, @NotNull ItemStack result, @NotNull Object... recipe) {
        addShapedRecipe(provider, setMaterialInfoData, GTCEu.id(regName), result, recipe);
    }

    /**
     * @see #addShapedRecipe(RecipeOutput, boolean, boolean, ResourceLocation, ItemStack, Object...)
     */
    public static void addShapedRecipe(RecipeOutput provider, boolean setMaterialInfoData,
                                       @NotNull ResourceLocation regName, @NotNull ItemStack result,
                                       @NotNull Object... recipe) {
        addShapedRecipe(provider, setMaterialInfoData, false, regName, result, recipe);
    }

    /**
     * @see #addShapedRecipe(RecipeOutput, boolean, boolean, ResourceLocation, ItemStack, Object...)
     */
    public static void addStrictShapedRecipe(RecipeOutput provider, boolean setMaterialInfoData,
                                             @NotNull ResourceLocation regName, @NotNull ItemStack result,
                                             @NotNull Object... recipe) {
        addShapedRecipe(provider, setMaterialInfoData, true, regName, result, recipe);
    }

    /**
     * @see #addShapedRecipe(RecipeOutput, boolean, boolean, ResourceLocation, ItemStack, Object...)
     */
    public static void addStrictSizeShapedRecipe(RecipeOutput provider, boolean setMaterialInfoData,
                                                 @NotNull ResourceLocation regName, @NotNull ItemStack result,
                                                 @NotNull Object... recipe) {
        addShapedRecipe(provider, setMaterialInfoData, true, regName, result, recipe);
    }

    /**
     * @see #addShapedRecipe(RecipeOutput, boolean, boolean, ResourceLocation, ItemStack, Object...)
     */
    public static void addStrictShapedRecipe(RecipeOutput provider, boolean setMaterialInfoData,
                                             @NotNull String regName, @NotNull ItemStack result,
                                             @NotNull Object... recipe) {
        addShapedRecipe(provider, setMaterialInfoData, true, GTCEu.id(regName), result, recipe);
    }

    public static void addShapelessRecipe(RecipeOutput provider, @NotNull String regName,
                                          @NotNull ItemStack result, @NotNull Object... recipe) {
        addShapelessRecipe(provider, GTCEu.id(regName), result, recipe);
    }

    public static void addShapedEnergyTransferRecipe(RecipeOutput provider, boolean setMaterialInfoData,
                                                     boolean overrideCharge, boolean transferMaxCharge,
                                                     @NotNull ResourceLocation regName,
                                                     @NotNull Ingredient chargeIngredient, @NotNull ItemStack result,
                                                     @NotNull Object... recipe) {
        var builder = new ShapedEnergyTransferRecipeBuilder(regName).output(result);
        builder.chargeIngredient(chargeIngredient).overrideCharge(overrideCharge).transferMaxCharge(transferMaxCharge);
        final CharSet tools = ToolHelper.getToolSymbols();
        CharSet foundTools = new CharArraySet(9);
        for (int i = 0; i < recipe.length; i++) {
            var o = recipe[i];
            if (o instanceof String pattern) {
                builder.pattern(pattern);
                for (char c : pattern.toCharArray()) {
                    if (tools.contains(c)) {
                        foundTools.add(c);
                    }
                }
            }
            if (o instanceof String[] pattern) {
                for (String s : pattern) {
                    builder.pattern(s);
                    for (char c : s.toCharArray()) {
                        if (tools.contains(c)) {
                            foundTools.add(c);
                        }
                    }
                }
            }
            if (o instanceof Character sign) {
                var content = recipe[i + 1];
                i++;
                switch (content) {
                    case Ingredient ingredient -> builder.define(sign, ingredient);
                    case ICustomIngredient ingredient -> builder.define(sign, ingredient.toVanilla());
                    case ItemStack itemStack -> builder.define(sign, itemStack);
                    case TagKey<?> key when key.isFor(Registries.ITEM) -> builder.define(sign, (TagKey<Item>) key);
                    case ItemLike itemLike -> builder.define(sign, itemLike);
                    case MaterialEntry(TagPrefix tagPrefix, Material material) -> {
                        TagKey<Item> tag = ChemicalHelper.getTag(tagPrefix, material);
                        if (tag != null) {
                            builder.define(sign, tag);
                        } else builder.define(sign, ChemicalHelper.get(tagPrefix, material));
                    }
                    default -> {}
                }
            }
        }
        for (var it = foundTools.iterator(); it.hasNext();) {
            char c = it.nextChar();
            builder.define(c, ToolHelper.getToolFromSymbol(c).craftingTags.get(0));
        }
        builder.save(provider);

        if (setMaterialInfoData) {
            ItemMaterialData.registerMaterialInfo(result.getItem(), getRecyclingIngredients(result.getCount(), recipe));
        }
    }

    public static void addShapedEnergyTransferRecipe(RecipeOutput provider, boolean setMaterialInfoData,
                                                     boolean overrideCharge, boolean transferMaxCharge,
                                                     @NotNull String regName, @NotNull Ingredient chargeIngredient,
                                                     @NotNull ItemStack result, @NotNull Object... recipe) {
        addShapedEnergyTransferRecipe(provider, setMaterialInfoData, overrideCharge, transferMaxCharge,
                GTCEu.id(regName), chargeIngredient, result, recipe);
    }

    public static void addShapedFluidContainerRecipe(RecipeOutput provider, boolean setMaterialInfoData,
                                                     boolean isStrict,
                                                     @NotNull ResourceLocation regName, @NotNull ItemStack result,
                                                     @NotNull Object... recipe) {
        var builder = new ShapedFluidContainerRecipeBuilder(regName).output(result);
        builder.isStrict(isStrict);
        final CharSet tools = ToolHelper.getToolSymbols();
        CharSet foundTools = new CharArraySet(9);
        for (int i = 0; i < recipe.length; i++) {
            var o = recipe[i];
            if (o instanceof String pattern) {
                builder.pattern(pattern);
                for (char c : pattern.toCharArray()) {
                    if (tools.contains(c)) {
                        foundTools.add(c);
                    }
                }
            }
            if (o instanceof String[] pattern) {
                for (String s : pattern) {
                    builder.pattern(s);
                    for (char c : s.toCharArray()) {
                        if (tools.contains(c)) {
                            foundTools.add(c);
                        }
                    }
                }
            }
            if (o instanceof Character sign) {
                var content = recipe[i + 1];
                i++;
                switch (content) {
                    case Ingredient ingredient -> builder.define(sign, ingredient);
                    case ICustomIngredient ingredient -> builder.define(sign, ingredient.toVanilla());
                    case ItemStack itemStack -> builder.define(sign, itemStack);
                    case TagKey<?> key when key.isFor(Registries.ITEM) -> builder.define(sign, (TagKey<Item>) key);
                    case TagPrefix prefix -> {
                        if (!prefix.getItemParentTags().isEmpty()) {
                            builder.define(sign, prefix.getItemParentTags().getFirst());
                        }
                    }
                    case ItemLike itemLike -> builder.define(sign, itemLike);
                    case MaterialEntry(TagPrefix tagPrefix, Material material) -> {
                        TagKey<Item> tag = ChemicalHelper.getTag(tagPrefix, material);
                        if (tag != null) {
                            builder.define(sign, tag);
                        } else builder.define(sign, ChemicalHelper.get(tagPrefix, material));
                    }
                    default -> {}
                }
            }
        }
        for (var it = foundTools.iterator(); it.hasNext();) {
            char c = it.nextChar();
            builder.define(c, ToolHelper.getToolFromSymbol(c).craftingTags.get(0));
        }

        builder.save(provider);

        if (setMaterialInfoData) {
            ItemMaterialData.registerMaterialInfo(result.getItem(), getRecyclingIngredients(result.getCount(), recipe));
        }
    }

    public static void addShapedFluidContainerRecipe(RecipeOutput provider, boolean setMaterialInfoData,
                                                     @NotNull String regName, @NotNull ItemStack result,
                                                     @NotNull Object... recipe) {
        addShapedFluidContainerRecipe(provider, setMaterialInfoData, GTCEu.id(regName), result, recipe);
    }

    public static void addShapedFluidContainerRecipe(RecipeOutput provider, boolean setMaterialInfoData,
                                                     @NotNull ResourceLocation regName, @NotNull ItemStack result,

                                                     @NotNull Object... recipe) {
        addShapedFluidContainerRecipe(provider, setMaterialInfoData, false, regName, result, recipe);
    }

    public static void addShapedFluidContainerRecipe(RecipeOutput provider, @NotNull String regName,
                                                     @NotNull ItemStack result,
                                                     @NotNull Object... recipe) {
        addShapedFluidContainerRecipe(provider, GTCEu.id(regName), result, recipe);
    }

    public static void addShapedFluidContainerRecipe(RecipeOutput provider,
                                                     @NotNull ResourceLocation regName,
                                                     @NotNull ItemStack result,
                                                     @NotNull Object... recipe) {
        addShapedFluidContainerRecipe(provider, false, regName, result, recipe);
    }

    /**
     * Adds a shapeless recipe which clears the components of the outputs
     *
     * @see VanillaRecipeHelper#addShapelessRecipe(RecipeOutput, String, ItemStack, Object...)
     */
    public static void addShapelessNBTClearingRecipe(RecipeOutput provider, @NotNull String regName,
                                                     @NotNull ItemStack result,
                                                     @NotNull Object... recipe) {
        addShapelessRecipe(provider, regName, result, recipe);
    }

    public static void addShapelessRecipe(RecipeOutput provider, @NotNull ResourceLocation regName,
                                          @NotNull ItemStack result, @NotNull Object... recipe) {
        var builder = new ShapelessRecipeBuilder(regName).output(result);
        for (Object content : recipe) {
            if (content instanceof Ingredient ingredient) {
                builder.requires(ingredient);
            } else if (content instanceof ItemStack itemStack) {
                builder.requires(itemStack);
            } else if (content instanceof TagKey<?> key) {
                builder.requires((TagKey<Item>) key);
            } else if (content instanceof ItemLike itemLike) {
                builder.requires(itemLike);
            } else if (content instanceof MaterialEntry entry) {
                TagKey<Item> tag = ChemicalHelper.getTag(entry.tagPrefix(), entry.material());
                if (tag != null) {
                    builder.requires(tag);
                } else builder.requires(ChemicalHelper.get(entry.tagPrefix(), entry.material()));
            } else if (content instanceof ItemProviderEntry<?, ?> entry) {
                builder.requires(entry.asStack());
            } else if (content instanceof Character c) {
                builder.requires(ToolHelper.getToolFromSymbol(c).craftingTags.get(0));
            }
        }
        builder.save(provider);
    }

    public static void addSmithingTransformRecipe(RecipeOutput provider, @NotNull ResourceLocation regName,
                                                  @NotNull Item result, @NotNull ItemLike baseInput,
                                                  @NotNull ItemLike template, @NotNull ItemLike addition,
                                                  @NotNull RecipeCategory category) {
        SmithingTransformRecipeBuilder
                .smithing(Ingredient.of(template), Ingredient.of(baseInput), Ingredient.of(addition), category, result)
                .unlocks(String.format("has_%s", baseInput), InventoryChangeTrigger.TriggerInstance.hasItems(baseInput))
                .save(provider, regName);
    }

    public static void addSmithingTransformRecipe(RecipeOutput provider, @NotNull String regName,
                                                  @NotNull Item result, @NotNull ItemLike baseInput,
                                                  @NotNull ItemLike template, @NotNull ItemLike addition) {
        addSmithingTransformRecipe(provider, GTCEu.id(regName), result, baseInput, template, addition,
                RecipeCategory.MISC);
    }

    public static void addToolUpgradingRecipe(@NotNull RecipeOutput provider, @NotNull GTToolType tool,
                                              @NotNull Material upgradeMaterial, @NotNull Material baseMaterial,
                                              @NotNull ItemLike template, @NotNull ItemLike addition) {
        ItemStack upgradeToolStack = ToolHelper.get(tool, upgradeMaterial);
        ItemStack baseToolStack = ToolHelper.get(tool, baseMaterial);

        if (upgradeToolStack.isEmpty() || baseToolStack.isEmpty()) return;

        VanillaRecipeHelper.addSmithingTransformRecipe(provider,
                String.format("%s_%s_smithing_transform_from_%s", upgradeMaterial.getName(), tool.name,
                        baseMaterial.getName()),
                upgradeToolStack.getItem(), baseToolStack.getItem(),
                template, addition);
    }

    /**
     * @param material the material to check
     * @return if the material is a wood
     */
    public static boolean isMaterialWood(@NotNull Material material) {
        return !material.isNull() && material.hasProperty(PropertyKey.WOOD);
    }

    public static ItemMaterialInfo getRecyclingIngredients(int outputCount, @NotNull Object... recipe) {
        Char2IntOpenHashMap inputCountMap = new Char2IntOpenHashMap();
        Reference2LongOpenHashMap<Material> materialStacksExploded = new Reference2LongOpenHashMap<>();

        int itr = 0;
        while (recipe[itr] instanceof String s) {
            for (char c : s.toCharArray()) {
                if (ToolHelper.getToolFromSymbol(c) != null) continue; // skip tools
                inputCountMap.addTo(c, 1);
            }
            itr++;
        }

        char lastChar = ' ';
        for (int i = itr; i < recipe.length; i++) {
            Object ingredient = recipe[i];

            // Track the current working ingredient symbol
            if (ingredient instanceof Character) {
                lastChar = (char) ingredient;
                continue;
            }

            // Should never happen if recipe is formatted correctly
            // In the case that it isn't, this error should be handled
            // by an earlier method call parsing the recipe.
            if (lastChar == ' ') return null;

            ItemLike itemLike;
            switch (ingredient) {
                case Ingredient ingr -> {
                    if (ingr.hasNoItems()) continue;
                    ItemStack stack = ingr.getItems()[0];
                    if (stack.isEmpty()) continue;
                    itemLike = stack.getItem();
                }
                case ICustomIngredient custom -> {
                    Ingredient ingr = custom.toVanilla();
                    if (ingr.hasNoItems()) continue;
                    ItemStack stack = ingr.getItems()[0];
                    if (stack.isEmpty()) continue;
                    itemLike = stack.getItem();
                }
                case ItemStack itemStack -> itemLike = itemStack.getItem();
                case TagKey<?> key -> {
                    continue; // todo can this be improved?
                }
                case ItemLike like -> itemLike = like;
                case MaterialEntry(TagPrefix tagPrefix, Material material) -> {
                    ItemStack stack = ChemicalHelper.get(tagPrefix, material);
                    if (stack == ItemStack.EMPTY) continue;
                    itemLike = stack.getItem();
                }
                default -> {
                    continue; // throw out bad entries
                }
            }

            // First try to get ItemMaterialInfo
            ItemMaterialInfo info = ItemMaterialData.getMaterialInfo(itemLike);
            if (info != null) {
                for (MaterialStack ms : info.getMaterials()) {
                    if (!(ms.material() instanceof MarkerMaterial)) {
                        addMaterialStack(materialStacksExploded, inputCountMap.get(lastChar), outputCount, ms);
                    }
                }
                continue;
            }

            // Then try to get a single Material (UnificationEntry needs this, for example)
            MaterialStack materialStack = ChemicalHelper.getMaterialStack(itemLike);
            if (!materialStack.isEmpty() && !(materialStack.material() instanceof MarkerMaterial)) {
                addMaterialStack(materialStacksExploded, inputCountMap.get(lastChar), outputCount, materialStack);
            }

            // Gather any secondary materials if this item has an OrePrefix
            TagPrefix prefix = ChemicalHelper.getPrefix(itemLike);
            if (!prefix.isEmpty() && !prefix.secondaryMaterials().isEmpty()) {
                for (MaterialStack ms : prefix.secondaryMaterials()) {
                    addMaterialStack(materialStacksExploded, inputCountMap.get(lastChar), outputCount, ms);
                }
            }
        }

        return new ItemMaterialInfo(materialStacksExploded);
    }

    private static void addMaterialStack(@NotNull Reference2LongOpenHashMap<Material> materialStacksExploded,
                                         int inputCount, int outputCount, @NotNull MaterialStack ms) {
        materialStacksExploded.addTo(ms.material(), (ms.amount() * inputCount / outputCount));
    }
}
