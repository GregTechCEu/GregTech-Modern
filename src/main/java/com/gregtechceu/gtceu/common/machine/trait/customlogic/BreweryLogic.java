package com.gregtechceu.gtceu.common.machine.trait.customlogic;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
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
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.gregtechceu.gtceu.api.GTValues.*;

@SuppressWarnings("deprecation")
public class BreweryLogic implements GTRecipeType.ICustomRecipeLogic {

    @Override
    public @Nullable GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
        var handlerLists = holder.getCapabilitiesForIO(IO.IN);
        List<RecipeHandlerList> distinct = new ArrayList<>();
        List<IRecipeHandler<?>> notDistinctItems = new ArrayList<>();
        List<IRecipeHandler<?>> notDistinctFluids = new ArrayList<>();

        for (var handlerList : handlerLists) {
            if (handlerList.isDistinct()) {
                if (handlerList.hasCapability(ItemRecipeCapability.CAP) &&
                        handlerList.hasCapability(FluidRecipeCapability.CAP)) {
                    distinct.add(handlerList);
                }
            } else {
                notDistinctItems.addAll(handlerList.getCapability(ItemRecipeCapability.CAP));
                notDistinctFluids.addAll(handlerList.getCapability(FluidRecipeCapability.CAP));
            }
        }

        List<ItemStack> itemStacks = new ArrayList<>();
        List<FluidStack> fluidStacks = new ArrayList<>();

        for (var handlerList : distinct) {
            itemStacks.clear();
            fluidStacks.clear();
            if (!collect(handlerList.getCapability(ItemRecipeCapability.CAP),
                    handlerList.getCapability(FluidRecipeCapability.CAP),
                    itemStacks, fluidStacks)) {
                continue;
            }

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
                    for (var fluidStack : fluidStacks) {
                        if (fluidStack.isFluidEqual(fromFluid) || (fluidStack.getFluid().is(fromTag) &&
                                Objects.equals(fromFluid.getTag(), fluidStack.getTag()))) {
                            return vanillaPotionRecipe(mix, fromFluid);
                        }
                    }
                }

                for (IBrewingRecipe recipe : BrewingRecipeRegistry.getRecipes()) {
                    if (!(recipe instanceof BrewingRecipe impl) || !impl.isIngredient(itemStack)) {
                        continue;
                    }
                    FluidIngredient fromFluid = PotionFluidHelper.getPotionFluidIngredientFrom(impl.getInput(),
                            PotionFluidHelper.MB_PER_RECIPE);

                    for (var fluidStack : fluidStacks) {
                        if (fromFluid.test(fluidStack)) {
                            return forgePotionRecipe(impl, fromFluid);
                        }
                    }
                }
            }
        }

        itemStacks.clear();
        fluidStacks.clear();
        collect(notDistinctItems, notDistinctFluids, itemStacks, fluidStacks);

        ReferenceOpenHashSet<PotionBrewing.Mix<Potion>> mixesWithIngredient = new ReferenceOpenHashSet<>();
        ReferenceOpenHashSet<BrewingRecipe> brewsWithIngredient = new ReferenceOpenHashSet<>();
        Reference2ObjectOpenHashMap<PotionBrewing.Mix<Potion>, FluidStack> mixesWithInput = new Reference2ObjectOpenHashMap<>();
        Reference2ObjectOpenHashMap<BrewingRecipe, FluidIngredient> brewsWithInput = new Reference2ObjectOpenHashMap<>();

        for (PotionBrewing.Mix<Potion> mix : PotionBrewingAccessor.getPotionMixes()) {
            for (var itemStack : itemStacks) {
                if (mix.ingredient.test(itemStack)) {
                    mixesWithIngredient.add(mix);
                    break;
                }
            }

            for (var fluidStack : fluidStacks) {
                FluidStack fromFluid = PotionFluidHelper.getFluidFromPotion(mix.from.get(),
                        PotionFluidHelper.MB_PER_RECIPE);
                var fromTag = TagUtil.createFluidTag(BuiltInRegistries.FLUID.getKey(fromFluid.getFluid()).getPath());
                if (fluidStack.isFluidEqual(fromFluid) ||
                        (fluidStack.getFluid().is(fromTag) &&
                                Objects.equals(fromFluid.getTag(), fluidStack.getTag()))) {
                    if (mixesWithIngredient.contains(mix)) {
                        return vanillaPotionRecipe(mix, fromFluid);
                    } else {
                        mixesWithInput.put(mix, fromFluid);
                        break;
                    }
                }
            }
        }

        for (IBrewingRecipe recipe : BrewingRecipeRegistry.getRecipes()) {
            if (!(recipe instanceof BrewingRecipe brew)) continue;
            for (var itemStack : itemStacks) {
                if (brew.isIngredient(itemStack)) {
                    brewsWithIngredient.add(brew);
                    break;
                }
            }

            for (var fluidStack : fluidStacks) {
                FluidIngredient fromFluid = PotionFluidHelper.getPotionFluidIngredientFrom(brew.getInput(),
                        PotionFluidHelper.MB_PER_RECIPE);
                if (fromFluid.test(fluidStack)) {
                    if (brewsWithIngredient.contains(brew)) {
                        return forgePotionRecipe(brew, fromFluid);
                    } else {
                        brewsWithInput.put(brew, fromFluid);
                        break;
                    }
                }
            }
        }

        if (mixesWithIngredient.isEmpty() &&
                mixesWithInput.isEmpty() &&
                brewsWithIngredient.isEmpty() &&
                brewsWithInput.isEmpty()) {
            return null;
        }

        for (var handlerList : distinct) {
            itemStacks.clear();
            fluidStacks.clear();
            collect(handlerList.getCapability(ItemRecipeCapability.CAP),
                    handlerList.getCapability(FluidRecipeCapability.CAP),
                    itemStacks, fluidStacks);
            if (!mixesWithInput.isEmpty() || !brewsWithInput.isEmpty()) {
                for (var itemStack : itemStacks) {
                    for (var entry : mixesWithInput.reference2ObjectEntrySet()) {
                        var mix = entry.getKey();
                        var fluid = entry.getValue();
                        if (mix.ingredient.test(itemStack)) return vanillaPotionRecipe(mix, fluid);
                    }

                    for (var entry : brewsWithInput.reference2ObjectEntrySet()) {
                        var mix = entry.getKey();
                        var fluid = entry.getValue();
                        if (mix.isIngredient(itemStack)) return forgePotionRecipe(mix, fluid);
                    }
                }
            }

            if (!mixesWithIngredient.isEmpty() || !brewsWithIngredient.isEmpty()) {
                for (var fluidStack : fluidStacks) {
                    for (var mix : mixesWithIngredient) {
                        FluidStack fromFluid = PotionFluidHelper.getFluidFromPotion(mix.from.get(),
                                PotionFluidHelper.MB_PER_RECIPE);
                        var fromTag = TagUtil
                                .createFluidTag(BuiltInRegistries.FLUID.getKey(fromFluid.getFluid()).getPath());
                        if (fluidStack.isFluidEqual(fromFluid) ||
                                (fluidStack.getFluid().is(fromTag) &&
                                        Objects.equals(fromFluid.getTag(), fluidStack.getTag()))) {
                            return vanillaPotionRecipe(mix, fromFluid);
                        }
                    }

                    for (var brew : brewsWithIngredient) {
                        FluidIngredient fromFluid = PotionFluidHelper.getPotionFluidIngredientFrom(brew.getInput(),
                                PotionFluidHelper.MB_PER_RECIPE);
                        if (fromFluid.test(fluidStack)) {
                            return forgePotionRecipe(brew, fromFluid);
                        }
                    }
                }
            }
        }

        return null;
    }

    private static @NotNull GTRecipe forgePotionRecipe(BrewingRecipe brew, FluidIngredient fromFluid) {
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

    private static boolean collect(List<IRecipeHandler<?>> itemHandlers, List<IRecipeHandler<?>> fluidHandlers,
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
