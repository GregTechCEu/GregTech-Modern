package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.trait.*;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.recipe.content.ContentListMap;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.api.recipe.ingredient.fluid.FluidIngredient;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class RecipeHelper {

    public static EnergyStack getRealEUt(@NotNull GTRecipe recipe) {
        EnergyStack stack = recipe.getInputEUt();
        if (!stack.isEmpty()) return stack;
        return recipe.getOutputEUt();
    }

    public static EnergyStack getRealEUt(@NotNull GTRecipeDefinition recipe) {
        EnergyStack stack = getInputEUt(recipe);
        if (!stack.isEmpty()) return stack;
        return getOutputEUt(recipe);
    }

    /**
     * Get a pair of the absolute EU/t value this recipe inputs or outputs and if it's input or output
     *
     * @param recipe
     * @return A pair of {@code (EnergyStack, isInput)}
     */
    public static EnergyStack.WithIO getRealEUtWithIO(@NotNull GTRecipe recipe) {
        EnergyStack stack = recipe.getInputEUt();
        if (!stack.isEmpty()) return new EnergyStack.WithIO(stack, IO.IN);
        return new EnergyStack.WithIO(recipe.getOutputEUt(), IO.OUT);
    }

    public static EnergyStack.WithIO getRealEUtWithIO(@NotNull GTRecipeDefinition recipe) {
        EnergyStack stack = getInputEUt(recipe);
        if (!stack.isEmpty()) return new EnergyStack.WithIO(stack, IO.IN);
        return new EnergyStack.WithIO(getOutputEUt(recipe), IO.OUT);
    }

    public static int getRecipeEUtTier(GTRecipe recipe) {
        EnergyStack stack = getRealEUt(recipe);
        long EUt = stack.voltage();
        if (recipe.parallels > 1) EUt /= recipe.parallels;
        return GTUtil.getTierByVoltage(EUt);
    }

    public static int getRecipeEUtTier(GTRecipeDefinition recipe) {
        return GTUtil.getTierByVoltage(getRealEUt(recipe).voltage());
    }

    public static int getPreOCRecipeEuTier(GTRecipeDefinition recipe) {
        return GTUtil.getTierByVoltage(getRealEUt(recipe).getTotalEU());
    }

    public static int getPreOCRecipeEuTier(GTRecipe recipe) {
        EnergyStack stack = getRealEUt(recipe);
        long EUt = stack.getTotalEU();
        if (recipe.parallels > 1) EUt /= recipe.parallels;
        EUt >>= (recipe.ocLevel * 2);
        return GTUtil.getTierByVoltage(EUt);
    }

    private static EnergyStack getInputEUt(GTRecipeDefinition recipe) {
        return calculateEUt(recipe.tickInputs);
    }

    private static EnergyStack getOutputEUt(GTRecipeDefinition recipe) {
        return calculateEUt(recipe.tickOutputs);
    }

    private static EnergyStack calculateEUt(ContentListMap contents) {
        var outputs = contents.get(EURecipeCapability.CAP);
        if (outputs == null) return EnergyStack.EMPTY;
        long v = 0, a = 0;
        for (var stack : outputs) {
            v += stack.voltage();
            a += stack.amperage();
        }
        return new EnergyStack(v, a);
    }

    /*
     * Those who use these methods should note that these methods do not guarantee that the returned values are valid,
     * because the relevant data, such as tag information, may not be loaded at the time these methods are called.
     * Methods for getting Recipe Builder input items or fluids are not provided, as these data are not yet loaded when
     * they are needed.
     */

    /**
     * get all input items from GTRecipes
     *
     * @param recipe GTRecipe
     * @return all input items
     */
    public static List<ItemStack> getInputItems(GTRecipe recipe, boolean simulate) {
        return recipe.getInputContents(ItemRecipeCapability.CAP).stream()
                .map(ingredient -> simulate ? ingredient.getItems()[0] : ingredient.toStack())
                .collect(Collectors.toList());
    }

    /**
     * get all input fluids from GTRecipes
     *
     * @param recipe GTRecipe
     * @return all input fluids
     */
    public static List<FluidStack> getInputFluids(GTRecipe recipe, boolean simulate) {
        return recipe.getInputContents(FluidRecipeCapability.CAP).stream()
                .map(ingredient -> simulate ? ingredient.getFluids()[0] : ingredient.toStack())
                .collect(Collectors.toList());
    }

    /**
     * get all output items from GTRecipes
     *
     * @param recipe GTRecipe
     * @return all output items
     */
    public static List<ItemStack> getOutputItems(GTRecipe recipe, boolean simulate) {
        return recipe.getOutputContents(ItemRecipeCapability.CAP).stream()
                .map(ingredient -> simulate ? ingredient.getItems()[0] : ingredient.toStack())
                .collect(Collectors.toList());
    }

    /**
     * get all output fluids from GTRecipes
     *
     * @param recipe GTRecipe
     * @return all output fluids
     */
    public static List<FluidStack> getOutputFluids(GTRecipe recipe, boolean simulate) {
        return recipe.getOutputContents(FluidRecipeCapability.CAP).stream()
                .map(ingredient -> simulate ? ingredient.getFluids()[0] : ingredient.toStack())
                .collect(Collectors.toList());
    }

    public static ActionResult matchRecipe(RecipeHandlerGroup holder, GTRecipe recipe) {
        return matchRecipe(holder, recipe, false);
    }

    public static ActionResult matchTickRecipe(RecipeHandlerGroup holder, GTRecipe recipe) {
        return recipe.hasTick() ? matchRecipe(holder, recipe, true) : ActionResult.SUCCESS;
    }

    private static ActionResult matchRecipe(RecipeHandlerGroup holder, GTRecipe recipe, boolean tick) {
        if (holder.isEmpty()) return ActionResult.FAIL_NO_CAPABILITIES;

        var result = handleRecipe(holder, recipe, IO.IN, tick ? recipe.tickInputs : recipe.inputs, true);
        if (!result.isSuccess()) return result;

        result = handleRecipe(holder, recipe, IO.OUT, tick ? recipe.tickOutputs : recipe.outputs, true);
        return result;
    }

    public static ActionResult handleRecipeIO(RecipeHandlerGroup holder, GTRecipe recipe, IO io) {
        if (holder.isEmpty() || io == IO.BOTH) return ActionResult.FAIL_NO_CAPABILITIES;
        return handleRecipe(holder, recipe, io, io == IO.IN ? recipe.inputs : recipe.outputs, false);
    }

    public static ActionResult handleTickRecipeIO(RecipeHandlerGroup holder, GTRecipe recipe, IO io) {
        if (holder.isEmpty() || io == IO.BOTH) return ActionResult.FAIL_NO_CAPABILITIES;
        return handleRecipe(holder, recipe, io, io == IO.IN ? recipe.tickInputs : recipe.tickOutputs, false);
    }

    /**
     * Checks if all the contents of the recipe are located in the group.
     *
     * @param simulated checks that the recipe ingredients are in the group if true,
     *                  process the recipe contents if false
     */
    public static ActionResult handleRecipe(RecipeHandlerGroup group, GTRecipe recipe, IO io,
                                            ContentListMap contents,
                                            boolean simulated) {
        var recipeContents = contents.copy();
        group.handleRecipe(io, recipe, recipeContents, simulated);
        ActionResult result = ActionResult.SUCCESS;
        for (var entry : recipeContents.entrySet()) {
            // void excess real output contents if it can be voided
            if (!simulated && io == IO.OUT && group.getOutputVoid().test(entry.getKey())) {
                entry.getValue().clear();
            }
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                result = ActionResult.fail(null, entry.getKey(), io);
                break;
            }
        }

        if (result.isSuccess() || result.capability() == null) return result;

        if (!simulated && ConfigHolder.INSTANCE.dev.debug) {
            GTCEu.LOGGER.warn("IO {} Error while handling recipe {} outputs for {}",
                    Component.translatable(io.tooltip).getString(), recipe, group);
        }
        String key = "gtceu.recipe_logic.insufficient_" + (io == IO.IN ? "in" : "out");
        return ActionResult.fail(Component.translatable(key)
                .append(": ").append(result.capability().getName()), result.capability(), io);
    }

    public static ActionResult matchContents(RecipeHandlerGroup holder, GTRecipe recipe) {
        var match = matchRecipe(holder, recipe);
        if (!match.isSuccess()) return match;

        return matchTickRecipe(holder, recipe);
    }

    /**
     * Check whether all conditions of a recipe are valid
     *
     * @param recipe      the recipe to test
     * @param recipeLogic the logic to test against the conditions
     * @return the list of failed conditions, or success if all conditions are satisfied
     */
    public static ActionResult checkConditions(GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        return checkConditions(recipe, recipeLogic, false);
    }

    @SuppressWarnings("rawtypes")
    public static ActionResult checkConditions(GTRecipe recipe, @NotNull RecipeLogic recipeLogic, boolean onlyCheckPerTick) {
        if (recipe.conditions.isEmpty()) return ActionResult.SUCCESS;
        Map<RecipeConditionType<?>, List<RecipeCondition>> or = new Reference2ObjectArrayMap<>();
        for (RecipeCondition condition : recipe.conditions) {
            if (onlyCheckPerTick && !condition.perTick()) continue;
            if (condition.isOr()) {
                or.computeIfAbsent(condition.getType(), type -> new ArrayList<>()).add(condition);
            } else if (!condition.check(recipe, recipeLogic)) {
                return ActionResult.fail(Component.translatable("gtceu.recipe_logic.condition_fails")
                        .append(": ")
                        .append(condition.getTooltips()), null, null);
            }
        }

        for (List<RecipeCondition> conditions : or.values()) {
            boolean passed = conditions.isEmpty();
            MutableComponent component = Component.translatable("gtceu.recipe_logic.condition_fails")
                    .append(": ");
            for (RecipeCondition condition : conditions) {

                passed = condition.check(recipe, recipeLogic);
                if (passed) break;
                else component.append(condition.getTooltips());
            }

            if (!passed) {
                return ActionResult.fail(component, null, null);
            }
        }
        return ActionResult.SUCCESS;
    }

    /**
     * Returns the maximum possible recipe outputs from a recipe, divided into regular and chanced outputs
     * Takes into account any specific output limiters, ie macerator slots, to trim down the output list
     * Trims from chanced outputs first, then regular outputs
     *
     * @param trimLimits The limit(s) on the number of outputs
     */
    public static void doTrim(ContentListMap current, Reference2IntMap<RecipeCapability<?>> trimLimits) {
        current.forEachEntry(new ContentListMap.EntryConsumer() {
            @Override
            public <T> void accept(RecipeCapability<T> cap, List<T> contents) {
                int N = trimLimits.getOrDefault(cap, -1);
                if(N < 0) return;

                int toTrim = contents.size() - N;
                if(toTrim <= 0) return;

                //trim chanced first
                var iter = contents.iterator();
                while (iter.hasNext()) {
                    var content = iter.next();
                    if(cap.isChanced(content)) {
                        iter.remove();
                        toTrim--;
                    }
                    if(toTrim <= 0) return;
                }

                iter = contents.iterator();
                while (iter.hasNext()) {
                    iter.next();
                    if(toTrim > 0) {
                        iter.remove();
                        toTrim--;
                    }
                    else break;
                }
            }
        });
    }

    public static int getRatioForDistillery(FluidIngredient fluidInput, FluidIngredient fluidOutput,
                                            @Nullable ItemStack output) {
        int[] divisors = new int[] { 2, 5, 10, 25, 50 };
        int ratio = -1;

        for (int divisor : divisors) {

            if (!isFluidStackDivisibleForDistillery(fluidInput, divisor))
                continue;

            if (!isFluidStackDivisibleForDistillery(fluidOutput, divisor))
                continue;

            if (output != null && output.getCount() % divisor != 0)
                continue;

            ratio = divisor;
        }

        return Math.max(1, ratio);
    }

    public static boolean isFluidStackDivisibleForDistillery(FluidIngredient fluidStack, int divisor) {
        return fluidStack.getAmount() % divisor == 0 && fluidStack.getAmount() / divisor >= 25;
    }

    public static void replaceEUwithSteam(GTRecipe recipe, double conversionRate) {
        long totalEU = recipe.getInputEUt().getTotalEU();
        int totalSteam = GTMath.saturatedCast((long) Math.ceil(totalEU * conversionRate));
        if(totalSteam > 0) {
            recipe.tickInputs.remove(EURecipeCapability.CAP);
            recipe.tickInputs.add(FluidRecipeCapability.CAP, FluidIngredient.of(GTMaterials.Steam.getFluidTag(), totalSteam));
        }
    }
}
