package com.gregtechceu.gtceu.common.machine.trait.customlogic;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;
import com.gregtechceu.gtceu.api.recipe.ingredient.OldFluidIngredient;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.fluid.potion.PotionFluidHelper;
import com.gregtechceu.gtceu.core.mixins.PotionBrewingAccessor;

import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.gregtechceu.gtceu.api.GTValues.*;

// TODO: Make these static recipes
@SuppressWarnings("deprecation")
public enum BreweryLogic implements GTRecipeType.ICustomRecipeLogic {

    INSTANCE;

    private static final Function<Fluid, TagKey<Fluid>> FLUID_TAGS = Util
            .memoize(fluid -> TagUtil.createFluidTag(BuiltInRegistries.FLUID.getKey(fluid).getPath()));
    private static final Function<PotionBrewing.Mix<Potion>, FluidStack> MIX_INPUTS = Util
            .memoize(mix -> PotionFluidHelper.getFluidFromPotion(mix.from.get(), PotionFluidHelper.MB_PER_RECIPE));
    private static final Function<BrewingRecipe, OldFluidIngredient> BREW_INGREDIENTS = Util.memoize(
            brew -> PotionFluidHelper.getPotionFluidIngredientFrom(brew.getInput(), PotionFluidHelper.MB_PER_RECIPE));

    @Override
    public @Nullable GTRecipe createCustomRecipe(RecipeHandlerGroup holder) {
        var itemHandlers = holder.getInputHandlerMap().getOrDefault(ItemRecipeCapability.CAP, List.of());
        var fluidHandlers = holder.getInputHandlerMap().getOrDefault(FluidRecipeCapability.CAP, List.of());
        if (itemHandlers.isEmpty() || fluidHandlers.isEmpty()) return null;

        List<ItemStack> itemStacks = new ArrayList<>();
        List<FluidStack> fluidStacks = new ArrayList<>();

        if (!collect(itemHandlers, fluidHandlers, itemStacks, fluidStacks)) return null;

        for (PotionBrewing.Mix<Potion> mix : PotionBrewingAccessor.getPotionMixes()) {
            FluidStack fromFluid = MIX_INPUTS.apply(mix);
            for (var itemStack : itemStacks) {
                if (!mix.ingredient.test(itemStack)) continue;
                for (var fluidStack : fluidStacks) {
                    if (testMixFluid(fluidStack, fromFluid)) {
                        return vanillaPotionRecipe(mix, fromFluid);
                    }
                }
            }
        }

        for (IBrewingRecipe recipe : BrewingRecipeRegistry.getRecipes()) {
            if (!(recipe instanceof BrewingRecipe brew)) continue;
            OldFluidIngredient fromFluid = BREW_INGREDIENTS.apply(brew);
            for (var itemStack : itemStacks) {
                if (!brew.isIngredient(itemStack)) continue;
                for (var fluidStack : fluidStacks) {
                    if (fromFluid.test(fluidStack)) {
                        return forgePotionRecipe(brew, fromFluid);
                    }
                }
            }
        }

        return null;
    }

    private static boolean testMixFluid(FluidStack fluidStack, FluidStack fromFluid) {
        var fromTag = FLUID_TAGS.apply(fromFluid.getFluid());
        return (fluidStack.getFluid() == fromFluid.getFluid() || fluidStack.getFluid().is(fromTag)) &&
                Objects.equals(fromFluid.getTag(), fluidStack.getTag());
    }

    private static @NotNull GTRecipe forgePotionRecipe(BrewingRecipe brew, OldFluidIngredient fromFluid) {
        FluidStack toFluid = PotionFluidHelper.getFluidFromPotionItem(brew.getOutput(),
                PotionFluidHelper.MB_PER_RECIPE);
        String name;
        Potion output = PotionUtils.getPotion(brew.getOutput());
        if (output != Potions.EMPTY) {
            name = output.getName("");
        } else {
            name = toFluid.getFluid().builtInRegistryHolder().key().location().getPath();
        }

        return GTRecipeTypes.BREWING_RECIPES.recipeBuilder("potion_forge_" + name)
                .inputItems(brew.getIngredient())
                .inputFluids(fromFluid)
                .outputFluids(toFluid)
                .duration(400)
                .EUt(VHA[MV])
                .buildRawRecipe();
    }

    private static @NotNull GTRecipe vanillaPotionRecipe(PotionBrewing.Mix<Potion> mix, FluidStack fromFluid) {
        FluidStack toFluid = PotionFluidHelper.getFluidFromPotion(mix.to.get(), PotionFluidHelper.MB_PER_RECIPE);
        return GTRecipeTypes.BREWING_RECIPES.recipeBuilder("potion_vanilla_" + mix.to.get().getName(""))
                .inputItems(mix.ingredient)
                .inputFluids(fromFluid)
                .outputFluids(toFluid)
                .duration(400)
                .EUt(VHA[MV])
                .buildRawRecipe();
    }

    private static boolean collect(List<? extends IRecipeHandler<?>> itemHandlers,
                                   List<? extends IRecipeHandler<?>> fluidHandlers,
                                   List<ItemStack> itemStacks, List<FluidStack> fluidStacks) {
        for (var handler : itemHandlers) {
            for (var content : handler.getContents()) {
                if (content instanceof ItemStack stack && !stack.isEmpty()) {
                    itemStacks.add(stack);
                }
            }
        }

        for (var handler : fluidHandlers) {
            for (var content : handler.getContents()) {
                if (content instanceof FluidStack stack && !stack.isEmpty()) {
                    fluidStacks.add(stack);
                }
            }
        }

        return !(itemStacks.isEmpty() || fluidStacks.isEmpty());
    }

    @Override
    public void buildRepresentativeRecipes() {
        int index = 0;
        for (PotionBrewing.Mix<Potion> mix : PotionBrewingAccessor.getPotionMixes()) {
            FluidStack fromFluid = PotionFluidHelper.getFluidFromPotion(mix.from.get(),
                    PotionFluidHelper.MB_PER_RECIPE);
            FluidStack toFluid = PotionFluidHelper.getFluidFromPotion(mix.to.get(), PotionFluidHelper.MB_PER_RECIPE);

            GTRecipe recipe = GTRecipeTypes.BREWING_RECIPES
                    .recipeBuilder("potion_vanilla_" + mix.to.get().getName("") + "_" + index++)
                    .inputItems(mix.ingredient)
                    .inputFluids(fromFluid)
                    .outputFluids(toFluid)
                    .duration(400)
                    // is this a good voltage?
                    .EUt(VHA[MV])
                    .buildRawRecipe();
            // for EMI to detect it's a synthetic recipe (not ever in JSON)
            recipe.setId(recipe.getId().withPrefix("/"));
            GTRecipeTypes.BREWING_RECIPES.addToMainCategory(recipe);
        }

        for (IBrewingRecipe brewingRecipe : BrewingRecipeRegistry.getRecipes()) {
            if (!(brewingRecipe instanceof BrewingRecipe impl)) {
                continue;
            }

            OldFluidIngredient fromFluid = PotionFluidHelper.getPotionFluidIngredientFrom(impl.getInput(),
                    PotionFluidHelper.MB_PER_RECIPE);
            FluidStack toFluid = PotionFluidHelper.getFluidFromPotionItem(impl.getOutput(),
                    PotionFluidHelper.MB_PER_RECIPE);

            String name = toFluid.getFluid().builtInRegistryHolder().key().location().getPath();
            Potion output = PotionUtils.getPotion(impl.getOutput());
            if (output != null) {
                name = output.getName("");
            }

            GTRecipe recipe = GTRecipeTypes.BREWING_RECIPES.recipeBuilder("potion_forge_" + name + "_" + index++)
                    .inputItems(impl.getIngredient())
                    .inputFluids(fromFluid)
                    .outputFluids(toFluid)
                    .duration(400)
                    .EUt(VHA[MV])
                    .buildRawRecipe();
            // for EMI to detect it's a synthetic recipe (not ever in JSON)
            recipe.setId(recipe.getId().withPrefix("/"));
            GTRecipeTypes.BREWING_RECIPES.addToMainCategory(recipe);
        }
    }
}
