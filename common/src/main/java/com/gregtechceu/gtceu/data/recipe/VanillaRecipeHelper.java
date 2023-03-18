package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.data.recipe.builder.ShapedRecipeBuilder;
import com.gregtechceu.gtceu.data.recipe.builder.ShapelessRecipeBuilder;
import com.gregtechceu.gtceu.data.recipe.builder.SmeltingRecipeBuilder;
import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.chars.CharSet;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2023/2/21
 * @implNote VanillaRecipeHelper
 */
public class VanillaRecipeHelper {
    public static void addSmeltingRecipe(Consumer<FinishedRecipe> provider, @Nonnull String regName, TagKey<Item> input, ItemStack output) {
        new SmeltingRecipeBuilder(GTCEu.id(regName.toLowerCase())).input(input).output(output).save(provider);
    }

    private static final Char2ObjectMap<TagKey<Item>> TOOLS = new Char2ObjectArrayMap<>();
    static {
        TOOLS.put('c', GTToolType.CROWBAR.itemTag);
        TOOLS.put('d', GTToolType.SCREWDRIVER.itemTag);
        TOOLS.put('f', GTToolType.FILE.itemTag);
        TOOLS.put('h', GTToolType.HARD_HAMMER.itemTag);
        TOOLS.put('k', GTToolType.KNIFE.itemTag);
        TOOLS.put('m', GTToolType.MORTAR.itemTag);
        TOOLS.put('r', GTToolType.SOFT_MALLET.itemTag);
        TOOLS.put('s', GTToolType.SAW.itemTag);
        TOOLS.put('w', GTToolType.WRENCH.itemTag);
        TOOLS.put('x', GTToolType.WIRE_CUTTER.itemTag);
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
     * <li>{@code 'c'} -  {@code craftingToolCrowbar}</li>
     * <li>{@code 'd'} -  {@code craftingToolScrewdriver}</li>
     * <li>{@code 'f'} -  {@code craftingToolFile}</li>
     * <li>{@code 'h'} -  {@code craftingToolHardHammer}</li>
     * <li>{@code 'k'} -  {@code craftingToolKnife}</li>
     * <li>{@code 'm'} -  {@code craftingToolMortar}</li>
     * <li>{@code 'r'} -  {@code craftingToolSoftHammer}</li>
     * <li>{@code 's'} -  {@code craftingToolSaw}</li>
     * <li>{@code 'w'} -  {@code craftingToolWrench}</li>
     * <li>{@code 'x'} -  {@code craftingToolWireCutter}</li>
     * </ul>
     *
     * @param regName the registry name for the recipe
     * @param result  the output for the recipe
     * @param recipe  the contents of the recipe
     */
    public static void addShapedRecipe(Consumer<FinishedRecipe> provider, @Nonnull String regName, @Nonnull ItemStack result, @Nonnull Object... recipe) {
        var builder = new ShapedRecipeBuilder(GTCEu.id(regName.toLowerCase())).output(result);
        CharSet set = new CharOpenHashSet();
        for (int i = 0; i < recipe.length; i++) {
            var o = recipe[i];
            if (o instanceof String pattern) {
                builder.pattern(pattern);
                for (Character c : TOOLS.keySet()) {
                    if (pattern.indexOf(c) >= 0) {
                        set.add(c.charValue());
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
                    builder.define(sign, ChemicalHelper.getTag(entry.tagPrefix, entry.material));
                }
            }
        }
        for (Character c : set) {
            builder.define(c, TOOLS.get(c.charValue()));
        }
        builder.save(provider);
    }

    public static void addShapelessRecipe(Consumer<FinishedRecipe> provider, @Nonnull String regName, @Nonnull ItemStack result, @Nonnull Object... recipe) {
        var builder = new ShapelessRecipeBuilder(GTCEu.id(regName.toLowerCase())).output(result);
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
                builder.requires(ChemicalHelper.getTag(entry.tagPrefix, entry.material));
            }
        }
        builder.save(provider);
    }
}
