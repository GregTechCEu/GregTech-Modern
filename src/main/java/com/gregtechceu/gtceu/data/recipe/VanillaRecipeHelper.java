package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterial;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.ItemMaterialInfo;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.data.recipe.builder.*;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import it.unimi.dsi.fastutil.chars.*;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author KilaBash
 * @date 2023/2/21
 * @implNote VanillaRecipeHelper
 */
public class VanillaRecipeHelper {

    public static void addSmeltingRecipe(Consumer<FinishedRecipe> provider, @NotNull String regName, TagKey<Item> input,
                                         ItemStack output) {
        addSmeltingRecipe(provider, GTCEu.id(regName), input, output);
    }

    public static void addSmeltingRecipe(Consumer<FinishedRecipe> provider, @NotNull ResourceLocation regName,
                                         TagKey<Item> input, ItemStack output) {
        addSmeltingRecipe(provider, regName, input, output, 0.0f);
    }

    public static void addSmeltingRecipe(Consumer<FinishedRecipe> provider, @NotNull String regName, TagKey<Item> input,
                                         ItemStack output, float experience) {
        addSmeltingRecipe(provider, GTCEu.id(regName), input, output, experience);
    }

    public static void addSmeltingRecipe(Consumer<FinishedRecipe> provider, @NotNull String regName, Ingredient input,
                                         ItemStack output, float experience) {
        addSmeltingRecipe(provider, GTCEu.id(regName), input, output, experience);
    }

    public static void addSmeltingRecipe(Consumer<FinishedRecipe> provider, @NotNull ResourceLocation regName,
                                         Ingredient input, ItemStack output, float experience) {
        new SmeltingRecipeBuilder(regName).input(input).output(output).cookingTime(200).experience(experience)
                .save(provider);
    }

    public static void addSmeltingRecipe(Consumer<FinishedRecipe> provider, @NotNull ResourceLocation regName,
                                         TagKey<Item> input, ItemStack output, float experience) {
        new SmeltingRecipeBuilder(regName).input(input).output(output).cookingTime(200).experience(experience)
                .save(provider);
    }

    public static void addBlastingRecipe(Consumer<FinishedRecipe> provider, @NotNull String regName, TagKey<Item> input,
                                         ItemStack output, float experience) {
        addBlastingRecipe(provider, GTCEu.id(regName), input, output, experience);
    }

    public static void addBlastingRecipe(Consumer<FinishedRecipe> provider, @NotNull String regName, Ingredient input,
                                         ItemStack output, float experience) {
        addBlastingRecipe(provider, GTCEu.id(regName), input, output, experience);
    }

    public static void addBlastingRecipe(Consumer<FinishedRecipe> provider, @NotNull ResourceLocation regName,
                                         Ingredient input, ItemStack output, float experience) {
        new BlastingRecipeBuilder(regName).input(input).output(output).cookingTime(100).experience(experience)
                .save(provider);
    }

    public static void addBlastingRecipe(Consumer<FinishedRecipe> provider, @NotNull ResourceLocation regName,
                                         TagKey<Item> input, ItemStack output, float experience) {
        new BlastingRecipeBuilder(regName).input(input).output(output).cookingTime(100).experience(experience)
                .save(provider);
    }

    public static void addSmokingRecipe(Consumer<FinishedRecipe> provider, @NotNull String regName, TagKey<Item> input,
                                        ItemStack output, float experience) {
        addSmokingRecipe(provider, GTCEu.id(regName), input, output, experience);
    }

    public static void addSmokingRecipe(Consumer<FinishedRecipe> provider, @NotNull String regName, ItemStack input,
                                        ItemStack output, float experience) {
        addSmokingRecipe(provider, GTCEu.id(regName), input, output, experience);
    }

    public static void addSmokingRecipe(Consumer<FinishedRecipe> provider, @NotNull String regName, TagKey<Item> input,
                                        ItemStack output) {
        addSmokingRecipe(provider, GTCEu.id(regName), input, output, 0);
    }

    public static void addSmokingRecipe(Consumer<FinishedRecipe> provider, @NotNull String regName, ItemStack input,
                                        ItemStack output) {
        addSmokingRecipe(provider, GTCEu.id(regName), input, output, 0);
    }

    public static void addSmokingRecipe(Consumer<FinishedRecipe> provider, @NotNull ResourceLocation regName,
                                        TagKey<Item> input, ItemStack output, float experience) {
        new SmokingRecipeBuilder(regName).input(input).output(output).cookingTime(100).experience(experience)
                .save(provider);
    }

    public static void addSmokingRecipe(Consumer<FinishedRecipe> provider, @NotNull ResourceLocation regName,
                                        ItemStack input, ItemStack output, float experience) {
        new SmokingRecipeBuilder(regName).input(input).output(output).cookingTime(100).experience(experience)
                .save(provider);
    }

    public static void addCampfireRecipe(Consumer<FinishedRecipe> provider, @NotNull String regName, ItemStack input,
                                         ItemStack output, float experience) {
        addCampfireRecipe(provider, GTCEu.id(regName), input, output, experience);
    }

    public static void addCampfireRecipe(Consumer<FinishedRecipe> provider, @NotNull String regName, ItemStack input,
                                         ItemStack output) {
        addCampfireRecipe(provider, GTCEu.id(regName), input, output, 0);
    }

    public static void addCampfireRecipe(Consumer<FinishedRecipe> provider, @NotNull ResourceLocation regName,
                                         ItemStack input, ItemStack output, float experience) {
        new CampfireRecipeBuilder(regName).input(input).output(output).cookingTime(100).experience(experience)
                .save(provider);
    }

    public static void addCampfireRecipe(Consumer<FinishedRecipe> provider, @NotNull String regName, TagKey<Item> input,
                                         ItemStack output, float experience) {
        addCampfireRecipe(provider, GTCEu.id(regName), input, output, experience);
    }

    public static void addCampfireRecipe(Consumer<FinishedRecipe> provider, @NotNull String regName, TagKey<Item> input,
                                         ItemStack output) {
        addCampfireRecipe(provider, GTCEu.id(regName), input, output, 0);
    }

    public static void addCampfireRecipe(Consumer<FinishedRecipe> provider, @NotNull ResourceLocation regName,
                                         TagKey<Item> input, ItemStack output, float experience) {
        new CampfireRecipeBuilder(regName).input(input).output(output).cookingTime(100).experience(experience)
                .save(provider);
    }

    public static void addSmeltingRecipe(Consumer<FinishedRecipe> provider, @NotNull String regName, ItemStack input,
                                         ItemStack output) {
        addSmeltingRecipe(provider, GTCEu.id(regName), input, output, 0.0f);
    }

    public static void addSmeltingRecipe(Consumer<FinishedRecipe> provider, @NotNull String regName, Item input,
                                         Item output) {
        addSmeltingRecipe(provider, GTCEu.id(regName), input.getDefaultInstance(), output.getDefaultInstance(), 0.0f);
    }

    public static void addSmeltingRecipe(Consumer<FinishedRecipe> provider, @NotNull String regName, Item input,
                                         Item output, float experience) {
        addSmeltingRecipe(provider, GTCEu.id(regName), input.getDefaultInstance(), output.getDefaultInstance(),
                experience);
    }

    public static void addSmeltingRecipe(Consumer<FinishedRecipe> provider, @NotNull String regName, ItemStack input,
                                         ItemStack output, float experience) {
        addSmeltingRecipe(provider, GTCEu.id(regName), input, output, experience);
    }

    public static void addSmeltingRecipe(Consumer<FinishedRecipe> provider, @NotNull ResourceLocation regName,
                                         ItemStack input, ItemStack output, float experience) {
        new SmeltingRecipeBuilder(regName).input(input).output(output).cookingTime(200).experience(experience)
                .save(provider);
    }

    /**
     * Adds a shaped recipe which clears the nbt of the outputs
     *
     * @see VanillaRecipeHelper#addShapedRecipe(Consumer, String, ItemStack, Object...)
     */
    public static void addShapedNBTClearingRecipe(Consumer<FinishedRecipe> provider, String regName, ItemStack result,
                                                  Object... recipe) {
        addStrictShapedRecipe(provider, regName, result, recipe);
    }

    public static void addShapedRecipe(Consumer<FinishedRecipe> provider, @NotNull String regName,
                                       @NotNull ItemStack result, @NotNull Object... recipe) {
        addShapedRecipe(provider, GTCEu.id(regName), result, recipe);
    }

    public static void addShapedRecipe(Consumer<FinishedRecipe> provider, @NotNull ResourceLocation regName,
                                       @NotNull ItemStack result, @NotNull Object... recipe) {
        addShapedRecipe(provider, false, regName, result, recipe);
    }

    public static void addStrictShapedRecipe(Consumer<FinishedRecipe> provider, @NotNull String regName,
                                             @NotNull ItemStack result, @NotNull Object... recipe) {
        addStrictShapedRecipe(provider, GTCEu.id(regName), result, recipe);
    }

    public static void addStrictShapedRecipe(Consumer<FinishedRecipe> provider, @NotNull ResourceLocation regName,
                                             @NotNull ItemStack result, @NotNull Object... recipe) {
        addStrictShapedRecipe(provider, false, regName, result, recipe);
    }

    /**
     * Adds Shaped Crafting Recipes.
     * <p/>
     * For Enums - {@link Enum#name()} is called.
     * <p/>
     * For {@link UnificationEntry} - {@link UnificationEntry#toString()} is called.
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
     * @param regName the registry name for the recipe
     * @param result  the output for the recipe
     * @param recipe  the contents of the recipe
     */
    public static void addShapedRecipe(Consumer<FinishedRecipe> provider, boolean withUnificationData, boolean isStrict,
                                       @NotNull ResourceLocation regName, @NotNull ItemStack result,
                                       @NotNull Object... recipe) {
        var builder = new ShapedRecipeBuilder(regName).output(result);
        builder.isStrict(isStrict);
        CharSet set = new CharOpenHashSet();
        for (int i = 0; i < recipe.length; i++) {
            var o = recipe[i];
            if (o instanceof String pattern) {
                builder.pattern(pattern);
                for (Character c : ToolHelper.getToolSymbols()) {
                    if (pattern.indexOf(c) >= 0) {
                        set.add(c.charValue());
                    }
                }
            }
            if (o instanceof String[] pattern) {
                for (String s : pattern) {
                    builder.pattern(s);
                    for (Character c : ToolHelper.getToolSymbols()) {
                        if (s.indexOf(c) >= 0) {
                            set.add(c.charValue());
                        }
                    }
                }
            }
            if (o instanceof Character sign) {
                var content = recipe[i + 1];
                i++;
                if (content instanceof Ingredient ingredient) {
                    builder.define(sign, ingredient);
                } else if (content instanceof ItemStack itemStack) {
                    builder.define(sign, itemStack);
                } else if (content instanceof TagKey<?> key) {
                    builder.define(sign, (TagKey<Item>) key);
                } else if (content instanceof TagPrefix prefix) {
                    if (prefix.getItemParentTags().length > 0) {
                        builder.define(sign, prefix.getItemParentTags()[0]);
                    }
                } else if (content instanceof ItemLike itemLike) {
                    builder.define(sign, itemLike);
                } else if (content instanceof UnificationEntry entry) {
                    TagKey<Item> tag = ChemicalHelper.getTag(entry.tagPrefix, entry.material);
                    if (tag != null) {
                        builder.define(sign, tag);
                    } else builder.define(sign, ChemicalHelper.get(entry.tagPrefix, entry.material));
                } else if (content instanceof ItemProviderEntry<?> entry) {
                    builder.define(sign, entry.asStack());
                }
            }
        }
        for (Character c : set) {
            builder.define(c, ToolHelper.getToolFromSymbol(c).itemTags.get(0));
        }
        builder.save(provider);

        if (withUnificationData) {
            ChemicalHelper.registerMaterialInfo(result.getItem(), getRecyclingIngredients(result.getCount(), recipe));
        }
    }

    public static void addShapedRecipe(Consumer<FinishedRecipe> provider, boolean withUnificationData,
                                       @NotNull String regName, @NotNull ItemStack result, @NotNull Object... recipe) {
        addShapedRecipe(provider, withUnificationData, GTCEu.id(regName), result, recipe);
    }

    public static void addShapedRecipe(Consumer<FinishedRecipe> provider, boolean withUnificationData,
                                       @NotNull ResourceLocation regName, @NotNull ItemStack result,
                                       @NotNull Object... recipe) {
        addShapedRecipe(provider, withUnificationData, false, regName, result, recipe);
    }

    public static void addStrictShapedRecipe(Consumer<FinishedRecipe> provider, boolean withUnificationData,
                                             @NotNull ResourceLocation regName, @NotNull ItemStack result,
                                             @NotNull Object... recipe) {
        addShapedRecipe(provider, withUnificationData, true, regName, result, recipe);
    }

    public static void addShapelessRecipe(Consumer<FinishedRecipe> provider, @NotNull String regName,
                                          @NotNull ItemStack result, @NotNull Object... recipe) {
        addShapelessRecipe(provider, GTCEu.id(regName), result, recipe);
    }

    public static void addShapedEnergyTransferRecipe(Consumer<FinishedRecipe> provider, boolean withUnificationData,
                                                     boolean overrideCharge, boolean transferMaxCharge,
                                                     @NotNull ResourceLocation regName,
                                                     @NotNull Ingredient chargeIngredient, @NotNull ItemStack result,
                                                     @NotNull Object... recipe) {
        var builder = new ShapedEnergyTransferRecipeBuilder(regName).output(result);
        builder.chargeIngredient(chargeIngredient).overrideCharge(overrideCharge).transferMaxCharge(transferMaxCharge);
        CharSet set = new CharOpenHashSet();
        for (int i = 0; i < recipe.length; i++) {
            var o = recipe[i];
            if (o instanceof String pattern) {
                builder.pattern(pattern);
                for (Character c : ToolHelper.getToolSymbols()) {
                    if (pattern.indexOf(c) >= 0) {
                        set.add(c.charValue());
                    }
                }
            }
            if (o instanceof String[] pattern) {
                for (String s : pattern) {
                    builder.pattern(s);
                    for (Character c : ToolHelper.getToolSymbols()) {
                        if (s.indexOf(c) >= 0) {
                            set.add(c.charValue());
                        }
                    }
                }
            }
            if (o instanceof Character sign) {
                var content = recipe[i + 1];
                i++;
                if (content instanceof Ingredient ingredient) {
                    builder.define(sign, ingredient);
                } else if (content instanceof ItemStack itemStack) {
                    builder.define(sign, itemStack);
                } else if (content instanceof TagKey<?> key) {
                    builder.define(sign, (TagKey<Item>) key);
                } else if (content instanceof ItemLike itemLike) {
                    builder.define(sign, itemLike);
                } else if (content instanceof UnificationEntry entry) {
                    TagKey<Item> tag = ChemicalHelper.getTag(entry.tagPrefix, entry.material);
                    if (tag != null) {
                        builder.define(sign, tag);
                    } else builder.define(sign, ChemicalHelper.get(entry.tagPrefix, entry.material));
                } else if (content instanceof ItemProviderEntry<?> entry) {
                    builder.define(sign, entry.asStack());
                }
            }
        }
        for (Character c : set) {
            builder.define(c, ToolHelper.getToolFromSymbol(c).itemTags.get(0));
        }
        builder.save(provider);

        if (withUnificationData) {
            ChemicalHelper.registerMaterialInfo(result.getItem(), getRecyclingIngredients(result.getCount(), recipe));
        }
    }

    public static void addShapedEnergyTransferRecipe(Consumer<FinishedRecipe> provider, boolean withUnificationData,
                                                     boolean overrideCharge, boolean transferMaxCharge,
                                                     @NotNull String regName, @NotNull Ingredient chargeIngredient,
                                                     @NotNull ItemStack result, @NotNull Object... recipe) {
        addShapedEnergyTransferRecipe(provider, withUnificationData, overrideCharge, transferMaxCharge,
                GTCEu.id(regName), chargeIngredient, result, recipe);
    }

    public static void addShapedFluidContainerRecipe(Consumer<FinishedRecipe> provider, boolean withUnificationData,
                                                     boolean isStrict,
                                                     @NotNull ResourceLocation regName, @NotNull ItemStack result,
                                                     @NotNull Object... recipe) {
        var builder = new ShapedFluidContainerRecipeBuilder(regName).output(result);
        builder.isStrict(isStrict);
        CharSet set = new CharOpenHashSet();
        for (int i = 0; i < recipe.length; i++) {
            var o = recipe[i];
            if (o instanceof String pattern) {
                builder.pattern(pattern);
                for (Character c : ToolHelper.getToolSymbols()) {
                    if (pattern.indexOf(c) >= 0) {
                        set.add(c.charValue());
                    }
                }
            }
            if (o instanceof String[] pattern) {
                for (String s : pattern) {
                    builder.pattern(s);
                    for (Character c : ToolHelper.getToolSymbols()) {
                        if (s.indexOf(c) >= 0) {
                            set.add(c.charValue());
                        }
                    }
                }
            }
            if (o instanceof Character sign) {
                var content = recipe[i + 1];
                i++;
                if (content instanceof Ingredient ingredient) {
                    builder.define(sign, ingredient);
                } else if (content instanceof ItemStack itemStack) {
                    builder.define(sign, itemStack);
                } else if (content instanceof TagKey<?> key) {
                    builder.define(sign, (TagKey<Item>) key);
                } else if (content instanceof TagPrefix prefix) {
                    if (prefix.getItemParentTags().length > 0) {
                        builder.define(sign, prefix.getItemParentTags()[0]);
                    }
                } else if (content instanceof ItemLike itemLike) {
                    builder.define(sign, itemLike);
                } else if (content instanceof UnificationEntry entry) {
                    TagKey<Item> tag = ChemicalHelper.getTag(entry.tagPrefix, entry.material);
                    if (tag != null) {
                        builder.define(sign, tag);
                    } else builder.define(sign, ChemicalHelper.get(entry.tagPrefix, entry.material));
                } else if (content instanceof ItemProviderEntry<?> entry) {
                    builder.define(sign, entry.asStack());
                }
            }
        }
        for (Character c : set) {
            builder.define(c, ToolHelper.getToolFromSymbol(c).itemTags.get(0));
        }

        builder.save(provider);

        if (withUnificationData) {
            ChemicalHelper.registerMaterialInfo(result.getItem(), getRecyclingIngredients(result.getCount(), recipe));
        }
    }

    public static void addShapedFluidContainerRecipe(Consumer<FinishedRecipe> provider, boolean withUnificationData,
                                                     @NotNull String regName, @NotNull ItemStack result,
                                                     @NotNull Object... recipe) {
        addShapedFluidContainerRecipe(provider, withUnificationData, GTCEu.id(regName), result, recipe);
    }

    public static void addShapedFluidContainerRecipe(Consumer<FinishedRecipe> provider, boolean withUnificationData,
                                                     @NotNull ResourceLocation regName, @NotNull ItemStack result,

                                                     @NotNull Object... recipe) {
        addShapedFluidContainerRecipe(provider, withUnificationData, false, regName, result, recipe);
    }

    public static void addShapedFluidContainerRecipe(Consumer<FinishedRecipe> provider, @NotNull String regName,
                                                     @NotNull ItemStack result,
                                                     @NotNull Object... recipe) {
        addShapedFluidContainerRecipe(provider, GTCEu.id(regName), result, recipe);
    }

    public static void addShapedFluidContainerRecipe(Consumer<FinishedRecipe> provider,
                                                     @NotNull ResourceLocation regName,
                                                     @NotNull ItemStack result,
                                                     @NotNull Object... recipe) {
        addShapedFluidContainerRecipe(provider, false, regName, result, recipe);
    }

    /**
     * Adds a shapeless recipe which clears the nbt of the outputs
     *
     * @see VanillaRecipeHelper#addShapelessRecipe(Consumer, String, ItemStack, Object...)
     */
    public static void addShapelessNBTClearingRecipe(Consumer<FinishedRecipe> provider, @NotNull String regName,
                                                     @NotNull ItemStack result,
                                                     @NotNull Object... recipe) {
        addShapelessRecipe(provider, regName, result, recipe);
    }

    public static void addShapelessRecipe(Consumer<FinishedRecipe> provider, @NotNull ResourceLocation regName,
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
            } else if (content instanceof UnificationEntry entry) {
                TagKey<Item> tag = ChemicalHelper.getTag(entry.tagPrefix, entry.material);
                if (tag != null) {
                    builder.requires(tag);
                } else builder.requires(ChemicalHelper.get(entry.tagPrefix, entry.material));
            } else if (content instanceof ItemProviderEntry<?> entry) {
                builder.requires(entry.asStack());
            } else if (content instanceof Character c) {
                builder.requires(ToolHelper.getToolFromSymbol(c).itemTags.get(0));
            }
        }
        builder.save(provider);
    }

    /**
     * @param material the material to check
     * @return if the material is a wood
     */
    public static boolean isMaterialWood(@Nullable Material material) {
        return material != null && material.hasProperty(PropertyKey.WOOD);
    }

    public static ItemMaterialInfo getRecyclingIngredients(int outputCount, @NotNull Object... recipe) {
        Char2IntOpenHashMap inputCountMap = new Char2IntOpenHashMap();
        Object2LongMap<Material> materialStacksExploded = new Object2LongOpenHashMap<>();

        int itr = 0;
        while (recipe[itr] instanceof String s) {
            for (char c : s.toCharArray()) {
                if (ToolHelper.getToolFromSymbol(c) != null) continue; // skip tools
                int count = inputCountMap.getOrDefault(c, 0);
                inputCountMap.put(c, count + 1);
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
            if (ingredient instanceof Ingredient ingr) {
                ItemStack[] stacks = ingr.getItems();
                if (stacks.length == 0) continue;
                ItemStack stack = stacks[0];
                if (stack == ItemStack.EMPTY) continue;
                itemLike = stack.getItem();
            } else if (ingredient instanceof ItemStack itemStack) {
                itemLike = itemStack.getItem();
            } else if (ingredient instanceof TagKey<?> key) {
                continue; // todo can this be improved?
            } else if (ingredient instanceof ItemLike) {
                itemLike = (ItemLike) ingredient;
            } else if (ingredient instanceof UnificationEntry entry) {
                ItemStack stack = ChemicalHelper.get(entry.tagPrefix, entry.material);
                if (stack == ItemStack.EMPTY) continue;
                itemLike = stack.getItem();
            } else if (ingredient instanceof ItemProviderEntry<?> entry) {
                itemLike = entry.asItem();
            } else continue; // throw out bad entries

            // First try to get ItemMaterialInfo
            ItemMaterialInfo info = ChemicalHelper.getMaterialInfo(itemLike);
            if (info != null) {
                for (MaterialStack ms : info.getMaterials()) {
                    if (!(ms.material() instanceof MarkerMaterial)) {
                        addMaterialStack(materialStacksExploded, inputCountMap, ms, lastChar);
                    }
                }
                continue;
            }

            // Then try to get a single Material (UnificationEntry needs this, for example)
            MaterialStack materialStack = ChemicalHelper.getMaterial(itemLike);
            if (materialStack != null && !(materialStack.material() instanceof MarkerMaterial)) {
                addMaterialStack(materialStacksExploded, inputCountMap, materialStack, lastChar);
            }

            // Gather any secondary materials if this item has an OrePrefix
            TagPrefix prefix = ChemicalHelper.getPrefix(itemLike);
            if (prefix != null && !prefix.secondaryMaterials().isEmpty()) {
                for (MaterialStack ms : prefix.secondaryMaterials()) {
                    addMaterialStack(materialStacksExploded, inputCountMap, ms, lastChar);
                }
            }
        }

        return new ItemMaterialInfo(materialStacksExploded.entrySet().stream()
                .map(e -> new MaterialStack(e.getKey(), e.getValue() / outputCount))
                .sorted(Comparator.comparingLong(m -> -m.amount()))
                .collect(Collectors.toList()));
    }

    private static void addMaterialStack(@NotNull Object2LongMap<Material> materialStacksExploded,
                                         @NotNull Char2IntFunction inputCountMap, @NotNull MaterialStack ms, char c) {
        long amount = materialStacksExploded.getOrDefault(ms.material(), 0L);
        materialStacksExploded.put(ms.material(), (ms.amount() * inputCountMap.get(c)) + amount);
    }
}
