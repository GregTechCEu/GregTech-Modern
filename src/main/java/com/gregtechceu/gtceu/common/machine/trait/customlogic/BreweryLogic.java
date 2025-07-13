package com.gregtechceu.gtceu.common.machine.trait.customlogic;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.kind.GTRecipe;
import com.gregtechceu.gtceu.api.tag.TagUtil;
import com.gregtechceu.gtceu.common.fluid.potion.PotionFluidHelper;
import com.gregtechceu.gtceu.core.mixins.PotionBrewingAccessor;
import com.gregtechceu.gtceu.data.recipe.GTRecipeTypes;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static com.gregtechceu.gtceu.api.GTValues.*;

// TODO: Make these static recipes
@SuppressWarnings("deprecation")
public enum BreweryLogic implements GTRecipeType.ICustomRecipeLogic {

    INSTANCE;

    private static final Function<Fluid, TagKey<Fluid>> FLUID_TAGS = Util
            .memoize(fluid -> TagUtil.createFluidTag(BuiltInRegistries.FLUID.getKey(fluid).getPath()));
    private static final Function<PotionBrewing.Mix<Potion>, FluidStack> MIX_INPUTS = Util
            .memoize(mix -> PotionFluidHelper.getFluidFromPotion(mix.from(), PotionFluidHelper.MB_PER_RECIPE));
    private static final Function<BrewingRecipe, SizedFluidIngredient> BREW_INGREDIENTS = Util.memoize(
            brew -> PotionFluidHelper.getPotionFluidIngredientFrom(brew.getInput(), PotionFluidHelper.MB_PER_RECIPE));

    @Override
    public @Nullable GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
        var handlerLists = holder.getCapabilitiesForIO(IO.IN);
        if (handlerLists.isEmpty()) return null;
        List<RecipeHandlerList> distinct = new ArrayList<>();
        List<IRecipeHandler<?>> notDistinctItems = new ArrayList<>();
        List<IRecipeHandler<?>> notDistinctFluids = new ArrayList<>();

        for (var handlerList : handlerLists) {
            if (handlerList.isDistinct()) {
                distinct.add(handlerList);
            } else {
                notDistinctItems.addAll(handlerList.getCapability(ItemRecipeCapability.CAP));
                notDistinctFluids.addAll(handlerList.getCapability(FluidRecipeCapability.CAP));
            }
        }

        if (distinct.isEmpty() && notDistinctItems.isEmpty() && notDistinctFluids.isEmpty()) return null;

        List<ItemStack> itemStacks = new ArrayList<>();
        List<FluidStack> fluidStacks = new ArrayList<>();

        for (var handlerList : distinct) {
            itemStacks.clear();
            fluidStacks.clear();
            if (!collect(handlerList, itemStacks, fluidStacks)) continue;

            for (var itemStack : itemStacks) {
                for (PotionBrewing.Mix<Potion> mix : ((PotionBrewingAccessor) getPotionBrewing()).getPotionMixes()) {
                    // test item ingredient first
                    if (!mix.ingredient().test(itemStack)) {
                        continue;
                    }
                    FluidStack fromFluid = MIX_INPUTS.apply(mix);
                    // then match fluid input
                    for (var fluidStack : fluidStacks) {
                        if (testMixFluid(fluidStack, fromFluid)) {
                            return vanillaPotionRecipe(mix, fromFluid);
                        }
                    }
                }

                for (IBrewingRecipe recipe : getPotionBrewing().getRecipes()) {
                    if (!(recipe instanceof BrewingRecipe brew) || !brew.isIngredient(itemStack)) {
                        continue;
                    }
                    SizedFluidIngredient fromFluid = BREW_INGREDIENTS.apply(brew);

                    for (var fluidStack : fluidStacks) {
                        if (fromFluid.test(fluidStack)) {
                            return forgePotionRecipe(brew, fromFluid);
                        }
                    }
                }
            }
        }

        if (notDistinctItems.isEmpty() && notDistinctFluids.isEmpty()) return null;

        itemStacks.clear();
        fluidStacks.clear();
        collect(notDistinctItems, notDistinctFluids, itemStacks, fluidStacks);
        if (itemStacks.isEmpty() && fluidStacks.isEmpty()) return null;

        ReferenceOpenHashSet<PotionBrewing.Mix<Potion>> mixesWithIngredient = new ReferenceOpenHashSet<>();
        ReferenceOpenHashSet<BrewingRecipe> brewsWithIngredient = new ReferenceOpenHashSet<>();
        Reference2ObjectOpenHashMap<PotionBrewing.Mix<Potion>, FluidStack> mixesWithInput = new Reference2ObjectOpenHashMap<>();
        Reference2ObjectOpenHashMap<BrewingRecipe, SizedFluidIngredient> brewsWithInput = new Reference2ObjectOpenHashMap<>();

        for (PotionBrewing.Mix<Potion> mix : ((PotionBrewingAccessor) getPotionBrewing()).getPotionMixes()) {
            for (var itemStack : itemStacks) {
                if (mix.ingredient().test(itemStack)) {
                    mixesWithIngredient.add(mix);
                    break;
                }
            }

            for (var fluidStack : fluidStacks) {
                FluidStack fromFluid = MIX_INPUTS.apply(mix);
                if (testMixFluid(fluidStack, fromFluid)) {
                    if (mixesWithIngredient.contains(mix)) {
                        return vanillaPotionRecipe(mix, fromFluid);
                    } else {
                        mixesWithInput.put(mix, fromFluid);
                        break;
                    }
                }
            }
        }

        for (IBrewingRecipe recipe : getPotionBrewing().getRecipes()) {
            if (!(recipe instanceof BrewingRecipe brew)) continue;
            for (var itemStack : itemStacks) {
                if (brew.isIngredient(itemStack)) {
                    brewsWithIngredient.add(brew);
                    break;
                }
            }

            for (var fluidStack : fluidStacks) {
                SizedFluidIngredient fromFluid = BREW_INGREDIENTS.apply(brew);
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
            collect(handlerList, itemStacks, fluidStacks);
            if (!mixesWithInput.isEmpty() || !brewsWithInput.isEmpty()) {
                for (var itemStack : itemStacks) {
                    for (var entry : mixesWithInput.reference2ObjectEntrySet()) {
                        var mix = entry.getKey();
                        var fluid = entry.getValue();
                        if (mix.ingredient().test(itemStack)) return vanillaPotionRecipe(mix, fluid);
                    }

                    for (var entry : brewsWithInput.reference2ObjectEntrySet()) {
                        var brew = entry.getKey();
                        var fluid = entry.getValue();
                        if (brew.isIngredient(itemStack)) return forgePotionRecipe(brew, fluid);
                    }
                }
            }

            if (!mixesWithIngredient.isEmpty() || !brewsWithIngredient.isEmpty()) {
                for (var fluidStack : fluidStacks) {
                    for (var mix : mixesWithIngredient) {
                        FluidStack fromFluid = MIX_INPUTS.apply(mix);
                        if (testMixFluid(fluidStack, fromFluid)) {
                            return vanillaPotionRecipe(mix, fromFluid);
                        }
                    }

                    for (var brew : brewsWithIngredient) {
                        SizedFluidIngredient fromFluid = BREW_INGREDIENTS.apply(brew);
                        if (fromFluid.test(fluidStack)) {
                            return forgePotionRecipe(brew, fromFluid);
                        }
                    }
                }
            }
        }

        return null;
    }

    private static boolean testMixFluid(FluidStack fluidStack, FluidStack fromFluid) {
        var fromTag = FLUID_TAGS.apply(fromFluid.getFluid());
        return (fluidStack.getFluid() == fromFluid.getFluid() || fluidStack.getFluid().is(fromTag)) &&
                Objects.equals(fromFluid.getComponents(), fluidStack.getComponents());
    }

    private static @NotNull GTRecipe forgePotionRecipe(BrewingRecipe brew, SizedFluidIngredient fromFluid) {
        FluidStack toFluid = PotionFluidHelper.getFluidFromPotionItem(brew.getOutput(),
                PotionFluidHelper.MB_PER_RECIPE);
        String name;
        PotionContents output = brew.getOutput().get(DataComponents.POTION_CONTENTS);
        if (output != PotionContents.EMPTY) {
            name = Potion.getName(output.potion(), "");
        } else {
            name = toFluid.getFluid().builtInRegistryHolder().key().location().getPath();
        }

        return GTRecipeTypes.BREWING_RECIPES.recipeBuilder("potion_forge_" + name)
                .inputItems(brew.getIngredient())
                .inputFluids(fromFluid)
                .outputFluids(toFluid)
                .duration(400)
                .EUt(VHA[MV])
                .build();
    }

    private static @NotNull GTRecipe vanillaPotionRecipe(PotionBrewing.Mix<Potion> mix, FluidStack fromFluid) {
        FluidStack toFluid = PotionFluidHelper.getFluidFromPotion(mix.to(), PotionFluidHelper.MB_PER_RECIPE);
        return GTRecipeTypes.BREWING_RECIPES
                .recipeBuilder("potion_vanilla_" + Potion.getName(Optional.of(mix.to()), ""))
                .inputItems(mix.ingredient())
                .inputFluids(fromFluid)
                .outputFluids(toFluid)
                .duration(400)
                .EUt(VHA[MV])
                .build();
    }

    private static boolean collect(RecipeHandlerList rhl, List<ItemStack> itemStacks, List<FluidStack> fluidStacks) {
        return collect(rhl.getCapability(ItemRecipeCapability.CAP),
                rhl.getCapability(FluidRecipeCapability.CAP),
                itemStacks, fluidStacks);
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
        for (PotionBrewing.Mix<Potion> mix : ((PotionBrewingAccessor) getPotionBrewing()).getPotionMixes()) {
            FluidStack fromFluid = PotionFluidHelper.getFluidFromPotion(mix.from(),
                    PotionFluidHelper.MB_PER_RECIPE);
            FluidStack toFluid = PotionFluidHelper.getFluidFromPotion(mix.to(), PotionFluidHelper.MB_PER_RECIPE);

            GTRecipe recipe = GTRecipeTypes.BREWING_RECIPES
                    .recipeBuilder("potion_vanilla_" + Potion.getName(Optional.of(mix.to()), "") + "_" + index++)
                    .inputItems(mix.ingredient())
                    .inputFluids(fromFluid)
                    .outputFluids(toFluid)
                    .duration(400)
                    // is this a good voltage?
                    .EUt(VHA[MV])
                    .build();
            // for EMI to detect it's a synthetic recipe (not ever in JSON)
            recipe.setId(recipe.getId().withPrefix("/"));
            GTRecipeTypes.BREWING_RECIPES.addToMainCategory(recipe);
        }

        for (IBrewingRecipe brewingRecipe : getPotionBrewing().getRecipes()) {
            if (!(brewingRecipe instanceof BrewingRecipe impl) ||
                    !impl.getOutput().has(DataComponents.POTION_CONTENTS)) {
                continue;
            }

            SizedFluidIngredient fromFluid = PotionFluidHelper.getPotionFluidIngredientFrom(impl.getInput(),
                    PotionFluidHelper.MB_PER_RECIPE);
            FluidStack toFluid = PotionFluidHelper.getFluidFromPotionItem(impl.getOutput(),
                    PotionFluidHelper.MB_PER_RECIPE);

            String name = toFluid.getFluid().builtInRegistryHolder().key().location().getPath();

            Optional<Holder<Potion>> output = impl.getOutput()
                    .getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)
                    .potion();
            if (output.isPresent()) {
                name = Potion.getName(output, "");
            }

            GTRecipe recipe = GTRecipeTypes.BREWING_RECIPES.recipeBuilder("potion_forge_" + name + "_" + index++)
                    .inputItems(impl.getIngredient())
                    .inputFluids(fromFluid)
                    .outputFluids(toFluid)
                    .duration(400)
                    .EUt(VHA[MV])
                    .build();
            // for EMI to detect it's a synthetic recipe (not ever in JSON)
            recipe.setId(recipe.getId().withPrefix("/"));
            GTRecipeTypes.BREWING_RECIPES.addToMainCategory(recipe);
        }
    }

    private static PotionBrewing getPotionBrewing() {
        if (GTCEu.isClientThread()) {
            return Minecraft.getInstance().getConnection().potionBrewing();
        } else {
            return GTCEu.getMinecraftServer().potionBrewing();
        }
    }
}
