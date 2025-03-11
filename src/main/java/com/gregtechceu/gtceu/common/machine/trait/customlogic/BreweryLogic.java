package com.gregtechceu.gtceu.common.machine.trait.customlogic;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.fluid.potion.PotionFluidHelper;
import com.gregtechceu.gtceu.core.mixins.PotionBrewingAccessor;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.gregtechceu.gtceu.api.GTValues.*;

@SuppressWarnings("deprecation")
public class BreweryLogic implements GTRecipeType.ICustomRecipeLogic {

    @Override
    public @Nullable GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
        // TODO: Make this use generic IRecipeHandlers and not Item/FluidHandlers - i.e. pattern buffer
        List<ItemStack> itemStacks = new ArrayList<>();
        for (var handler : holder.getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP)) {
            for (var content : handler.getContents()) {
                if (content instanceof ItemStack stack && !stack.isEmpty()) {
                    itemStacks.add(stack);
                }
            }
        }

        if (itemStacks.isEmpty()) return null;

        List<FluidStack> fluidStacks = new ArrayList<>();
        for (var handler : holder.getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP)) {
            for (var content : handler.getContents()) {
                if (content instanceof FluidStack stack && !stack.isEmpty()) {
                    fluidStacks.add(stack);
                }
            }
        }

        if (fluidStacks.isEmpty()) return null;

        for (var itemStack : itemStacks) {
            for (PotionBrewing.Mix<Potion> mix : PotionBrewingAccessor.getPotionMixes()) {
                // test item ingredient first
                if (!mix.ingredient.test(itemStack)) {
                    continue;
                }
                FluidStack fromFluid = PotionFluidHelper.getFluidFromPotion(mix.from.get(),
                        PotionFluidHelper.MB_PER_RECIPE);
                var fromTag = TagUtil
                        .createFluidTag(BuiltInRegistries.FLUID.getKey(fromFluid.getFluid()).getPath());

                // then match fluid input
                boolean found = false;
                for (var fluidStack : fluidStacks) {
                    if (fluidStack.getFluid().is(fromTag) &&
                            Objects.equals(fromFluid.getTag(), fluidStack.getTag())) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    continue;
                }

                FluidStack toFluid = PotionFluidHelper.getFluidFromPotion(mix.to.get(),
                        PotionFluidHelper.MB_PER_RECIPE);

                return GTRecipeTypes.BREWING_RECIPES.recipeBuilder("potion_vanilla_" + mix.to.get().getName(""))
                        .inputItems(mix.ingredient)
                        .inputFluids(fromFluid)
                        .outputFluids(toFluid)
                        .duration(400)
                        .EUt(VHA[MV])
                        .buildRawRecipe();
            }

            for (IBrewingRecipe recipe : BrewingRecipeRegistry.getRecipes()) {
                if (!(recipe instanceof BrewingRecipe impl) || !impl.isIngredient(itemStack)) {
                    continue;
                }

                FluidIngredient fromFluid = PotionFluidHelper.getPotionFluidIngredientFrom(impl.getInput(),
                        PotionFluidHelper.MB_PER_RECIPE);

                boolean found = false;
                for (var fluidStack : fluidStacks) {
                    if (fromFluid.test(fluidStack)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    continue;
                }

                FluidStack toFluid = PotionFluidHelper.getFluidFromPotionItem(impl.getOutput(),
                        PotionFluidHelper.MB_PER_RECIPE);

                String name = toFluid.getFluid().builtInRegistryHolder().key().location().getPath();
                Potion output = PotionUtils.getPotion(impl.getOutput());
                if (output != null) {
                    name = output.getName("");
                }

                return GTRecipeTypes.BREWING_RECIPES.recipeBuilder("potion_forge_" + name)
                        .inputItems(impl.getIngredient())
                        .inputFluids(fromFluid)
                        .outputFluids(toFluid)
                        .duration(400)
                        .EUt(VHA[MV])
                        .buildRawRecipe();

            }
        }
        return null;
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

            FluidIngredient fromFluid = PotionFluidHelper.getPotionFluidIngredientFrom(impl.getInput(),
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
