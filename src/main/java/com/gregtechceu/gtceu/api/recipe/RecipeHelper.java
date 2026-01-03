package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerGroup;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerGroupColor;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerGroupDistinctness;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.Contract;
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

    public static int getRecipeEUtTier(GTRecipe recipe) {
        EnergyStack stack = getRealEUt(recipe);
        long EUt = stack.voltage();
        if (recipe.parallels > 1) EUt /= recipe.parallels;
        return GTUtil.getTierByVoltage(EUt);
    }

    public static int getPreOCRecipeEuTier(GTRecipe recipe) {
        EnergyStack stack = getRealEUt(recipe);
        long EUt = stack.getTotalEU();
        if (recipe.parallels > 1) EUt /= recipe.parallels;
        EUt >>= (recipe.ocLevel * 2);
        return GTUtil.getTierByVoltage(EUt);
    }

    public static <T> List<T> getInputContents(GTRecipeBuilder builder, RecipeCapability<T> capability) {
        return builder.input.getOrDefault(capability, Collections.emptyList()).stream()
                .map(content -> capability.of(content.getContent()))
                .collect(Collectors.toList());
    }

    public static <T> List<T> getInputContents(GTRecipe recipe, RecipeCapability<T> capability) {
        return recipe.getInputContents(capability).stream()
                .map(content -> capability.of(content.getContent()))
                .collect(Collectors.toList());
    }

    public static <T> List<T> getOutputContents(GTRecipeBuilder builder, RecipeCapability<T> capability) {
        return builder.output.getOrDefault(capability, Collections.emptyList()).stream()
                .map(content -> capability.of(content.getContent()))
                .collect(Collectors.toList());
    }

    public static <T> List<T> getOutputContents(GTRecipe recipe, RecipeCapability<T> capability) {
        return recipe.getOutputContents(capability).stream()
                .map(content -> capability.of(content.getContent()))
                .collect(Collectors.toList());
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
    public static List<ItemStack> getInputItems(GTRecipe recipe) {
        return recipe.getInputContents(ItemRecipeCapability.CAP).stream()
                .map(content -> ItemRecipeCapability.CAP.of(content.getContent()))
                .map(ingredient -> ingredient.getItems()[0])
                .collect(Collectors.toList());
    }

    /**
     * get all input fluids from GTRecipes
     *
     * @param recipe GTRecipe
     * @return all input fluids
     */
    public static List<FluidStack> getInputFluids(GTRecipe recipe) {
        return recipe.getInputContents(FluidRecipeCapability.CAP).stream()
                .map(content -> FluidRecipeCapability.CAP.of(content.getContent()))
                .map(ingredient -> ingredient.getStacks()[0])
                .collect(Collectors.toList());
    }

    /**
     * get all output items from GTRecipes
     *
     * @param recipe GTRecipe
     * @return all output items
     */
    public static List<ItemStack> getOutputItems(GTRecipe recipe) {
        return recipe.getOutputContents(ItemRecipeCapability.CAP).stream()
                .map(content -> ItemRecipeCapability.CAP.of(content.getContent()))
                .map(ingredient -> ingredient.getItems()[0])
                .collect(Collectors.toList());
    }

    /**
     * get all output items from GTRecipeBuilder
     *
     * @param builder GTRecipeBuilder
     * @return all output items
     */
    public static List<ItemStack> getOutputItems(GTRecipeBuilder builder) {
        return builder.output.getOrDefault(ItemRecipeCapability.CAP, Collections.emptyList()).stream()
                .map(content -> ItemRecipeCapability.CAP.of(content.getContent()))
                .map(ingredient -> ingredient.getItems()[0])
                .collect(Collectors.toList());
    }

    /**
     * get all output fluids from GTRecipes
     *
     * @param recipe GTRecipe
     * @return all output fluids
     */
    public static List<FluidStack> getOutputFluids(GTRecipe recipe) {
        return recipe.getOutputContents(FluidRecipeCapability.CAP).stream()
                .map(content -> FluidRecipeCapability.CAP.of(content.getContent()))
                .map(ingredient -> ingredient.getStacks()[0])
                .collect(Collectors.toList());
    }

    /**
     * get all output fluids from GTRecipeBuilder
     *
     * @param builder GTRecipeBuilder
     * @return all output fluids
     */
    public static List<FluidStack> getOutputFluids(GTRecipeBuilder builder) {
        return builder.output.getOrDefault(FluidRecipeCapability.CAP, Collections.emptyList()).stream()
                .map(content -> FluidRecipeCapability.CAP.of(content.getContent()))
                .map(ingredient -> ingredient.getStacks()[0])
                .collect(Collectors.toList());
    }

    public static ActionResult matchRecipe(IRecipeCapabilityHolder holder, GTRecipe recipe) {
        return matchRecipe(holder, recipe, false);
    }

    public static ActionResult matchTickRecipe(IRecipeCapabilityHolder holder, GTRecipe recipe) {
        return recipe.hasTick() ? matchRecipe(holder, recipe, true) : ActionResult.SUCCESS;
    }

    private static ActionResult matchRecipe(IRecipeCapabilityHolder holder, GTRecipe recipe, boolean tick) {
        if (!holder.hasCapabilityProxies()) return ActionResult.FAIL_NO_CAPABILITIES;

        var result = handleRecipe(holder, recipe, IO.IN, tick ? recipe.tickInputs : recipe.inputs,
                Collections.emptyMap(), tick, true);
        if (!result.isSuccess()) return result;

        result = handleRecipe(holder, recipe, IO.OUT, tick ? recipe.tickOutputs : recipe.outputs,
                Collections.emptyMap(), tick, true);
        return result;
    }

    public static ActionResult handleRecipeIO(IRecipeCapabilityHolder holder, GTRecipe recipe, IO io,
                                              Map<RecipeCapability<?>, Object2IntMap<?>> chanceCaches) {
        if (!holder.hasCapabilityProxies() || io == IO.BOTH) return ActionResult.FAIL_NO_CAPABILITIES;
        return handleRecipe(holder, recipe, io, io == IO.IN ? recipe.inputs : recipe.outputs, chanceCaches, false,
                false);
    }

    public static ActionResult handleTickRecipeIO(IRecipeCapabilityHolder holder, GTRecipe recipe, IO io,
                                                  Map<RecipeCapability<?>, Object2IntMap<?>> chanceCaches) {
        if (!holder.hasCapabilityProxies() || io == IO.BOTH) return ActionResult.FAIL_NO_CAPABILITIES;
        return handleRecipe(holder, recipe, io, io == IO.IN ? recipe.tickInputs : recipe.tickOutputs, chanceCaches,
                true, false);
    }

    /**
     * Checks if all the contents of the recipe are located in the holder.
     *
     * @param simulated checks that the recipe ingredients are in the holder if true,
     *                  process the recipe contents if false
     */
    public static ActionResult handleRecipe(IRecipeCapabilityHolder holder, GTRecipe recipe, IO io,
                                            Map<RecipeCapability<?>, List<Content>> contents,
                                            Map<RecipeCapability<?>, Object2IntMap<?>> chanceCaches,
                                            boolean isTick, boolean simulated) {
        RecipeRunner runner = new RecipeRunner(recipe, io, isTick, holder, chanceCaches, simulated);
        var result = runner.handle(contents);

        if (result.isSuccess() || result.capability() == null) return result;

        if (!simulated && ConfigHolder.INSTANCE.dev.debug) {
            GTCEu.LOGGER.warn("IO {} Error while handling recipe {} outputs for {}",
                    Component.translatable(io.tooltip).getString(), recipe, holder);
        }
        String key = "gtceu.recipe_logic.insufficient_" + (io == IO.IN ? "in" : "out");
        return ActionResult.fail(Component.translatable(key)
                .append(": ").append(result.capability().getName()), result.capability(), io);
    }

    public static ActionResult matchContents(IRecipeCapabilityHolder holder, GTRecipe recipe) {
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
        if (recipe.conditions.isEmpty()) return ActionResult.SUCCESS;
        Map<RecipeConditionType<?>, List<RecipeCondition>> or = new Reference2ObjectArrayMap<>();
        for (RecipeCondition condition : recipe.conditions) {
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
     * Creates a copy of the recipe matching the trim limits -
     * Returns the recipe itself if no valid trim limits are passed
     */
    @Contract(pure = true)
    public static GTRecipe trimRecipeOutputs(GTRecipe recipe, Reference2IntMap<RecipeCapability<?>> trimLimits) {
        // Fast return early if no trimming desired
        if (trimLimits.isEmpty() || trimLimits.values().intStream().allMatch(integer -> integer == -1)) {
            return recipe;
        }

        GTRecipe copy = recipe.copy();

        copy.outputs.clear();
        copy.outputs.putAll(doTrim(recipe.outputs, trimLimits));
        copy.tickOutputs.clear();
        copy.tickOutputs.putAll(doTrim(recipe.tickOutputs, trimLimits));

        return copy;
    }

    /**
     * Returns the maximum possible recipe outputs from a recipe, divided into regular and chanced outputs
     * Takes into account any specific output limiters, ie macerator slots, to trim down the output list
     * Trims from chanced outputs first, then regular outputs
     *
     * @param trimLimits The limit(s) on the number of outputs
     * @return All recipe outputs, limited by some factor(s)
     */
    @Contract(pure = true)
    public static Map<RecipeCapability<?>, List<Content>> doTrim(Map<RecipeCapability<?>, List<Content>> current,
                                                                 Reference2IntMap<RecipeCapability<?>> trimLimits) {
        Map<RecipeCapability<?>, List<Content>> outputs = new Reference2ObjectOpenHashMap<>(current.size());

        for (var entry : current.entrySet()) {
            var cap = entry.getKey();
            var contents = entry.getValue();
            if (contents.isEmpty()) continue;
            int N = trimLimits.getOrDefault(cap, -1);
            if (N == 0) continue; // Skip this cap if limit is 0

            List<Content> list = outputs.computeIfAbsent(cap, c -> new ArrayList<>());
            if (N == -1) { // Add all if limit is -1/not in map
                list.addAll(contents);
                continue;
            }

            int added = 0;
            List<Content> chanced = new ArrayList<>();
            // Add non-chanced contents with priority and store chanced contents for later
            for (var content : contents) {
                if (added == N) break;
                if (0 < content.chance && content.chance < content.maxChance) {
                    chanced.add(content);
                } else {
                    list.add(content);
                    added++;
                }
            }

            // Add as many chanced contents as needed
            if (added < N) {
                int rem = Math.min(chanced.size(), N - added);
                list.addAll(chanced.subList(0, rem));
            }
        }

        return outputs;
    }

    public static void addToRecipeHandlerMap(RecipeHandlerGroup key, RecipeHandlerList handler,
                                             Map<RecipeHandlerGroup, List<RecipeHandlerList>> map) {
        // If they should bypass this system, add them to the BYPASS_DISTINCT group.
        if (handler.doesCapabilityBypassDistinct()) {
            map.computeIfAbsent(RecipeHandlerGroupDistinctness.BYPASS_DISTINCT, $ -> new ArrayList<>()).add(handler);
            return;
        }
        // Add undyed RHL's to every group that's not distinct, bypass, and also the undyed group itself.
        if (key.equals(RecipeHandlerGroupColor.UNDYED)) {
            for (var entry : map.entrySet()) {
                if (entry.getKey().equals(RecipeHandlerGroupDistinctness.BUS_DISTINCT) ||
                        entry.getKey().equals(RecipeHandlerGroupDistinctness.BYPASS_DISTINCT) ||
                        entry.getKey().equals(RecipeHandlerGroupColor.UNDYED)) {
                    continue;
                }
                entry.getValue().add(handler);
            }
        }
        // Add other RHL's to their own group, or create it (using the undyed group as base) if it does not exist.
        List<RecipeHandlerList> undyed = map.getOrDefault(RecipeHandlerGroupColor.UNDYED, Collections.emptyList());

        map.computeIfAbsent(key, $ -> new ArrayList<>(undyed)).add(handler);
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
}
