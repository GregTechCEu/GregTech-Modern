package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote GTRecipe
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GTRecipe implements net.minecraft.world.item.crafting.Recipe<Container> {

    public final GTRecipeType recipeType;
    @Getter
    @Setter
    public ResourceLocation id;
    public final Map<RecipeCapability<?>, List<Content>> inputs;
    public final Map<RecipeCapability<?>, List<Content>> outputs;
    public final Map<RecipeCapability<?>, List<Content>> tickInputs;
    public final Map<RecipeCapability<?>, List<Content>> tickOutputs;

    public final Map<RecipeCapability<?>, ChanceLogic> inputChanceLogics;
    public final Map<RecipeCapability<?>, ChanceLogic> outputChanceLogics;
    public final Map<RecipeCapability<?>, ChanceLogic> tickInputChanceLogics;
    public final Map<RecipeCapability<?>, ChanceLogic> tickOutputChanceLogics;

    public final List<RecipeCondition> conditions;
    // for KubeJS. actual type is List<IngredientAction>.
    // Must be List<?> to not cause crashes without KubeJS.
    public final List<?> ingredientActions;
    @NotNull
    public CompoundTag data;
    public int duration;
    public int parallels = 1;
    public int ocLevel = 0;
    public final GTRecipeCategory recipeCategory;
    @Getter
    public boolean isFuel;

    public GTRecipe(GTRecipeType recipeType,
                    Map<RecipeCapability<?>, List<Content>> inputs,
                    Map<RecipeCapability<?>, List<Content>> outputs,
                    Map<RecipeCapability<?>, List<Content>> tickInputs,
                    Map<RecipeCapability<?>, List<Content>> tickOutputs,
                    Map<RecipeCapability<?>, ChanceLogic> inputChanceLogics,
                    Map<RecipeCapability<?>, ChanceLogic> outputChanceLogics,
                    Map<RecipeCapability<?>, ChanceLogic> tickInputChanceLogics,
                    Map<RecipeCapability<?>, ChanceLogic> tickOutputChanceLogics,
                    List<RecipeCondition> conditions,
                    List<?> ingredientActions,
                    @NotNull CompoundTag data,
                    int duration,
                    boolean isFuel,
                    @NotNull GTRecipeCategory recipeCategory) {
        this(recipeType, null, inputs, outputs, tickInputs, tickOutputs,
                inputChanceLogics, outputChanceLogics, tickInputChanceLogics, tickOutputChanceLogics,
                conditions, ingredientActions, data, duration, isFuel, recipeCategory);
    }

    public GTRecipe(GTRecipeType recipeType,
                    @Nullable ResourceLocation id,
                    Map<RecipeCapability<?>, List<Content>> inputs,
                    Map<RecipeCapability<?>, List<Content>> outputs,
                    Map<RecipeCapability<?>, List<Content>> tickInputs,
                    Map<RecipeCapability<?>, List<Content>> tickOutputs,
                    Map<RecipeCapability<?>, ChanceLogic> inputChanceLogics,
                    Map<RecipeCapability<?>, ChanceLogic> outputChanceLogics,
                    Map<RecipeCapability<?>, ChanceLogic> tickInputChanceLogics,
                    Map<RecipeCapability<?>, ChanceLogic> tickOutputChanceLogics,
                    List<RecipeCondition> conditions,
                    List<?> ingredientActions,
                    @NotNull CompoundTag data,
                    int duration,
                    boolean isFuel,
                    @NotNull GTRecipeCategory recipeCategory) {
        this.recipeType = recipeType;
        this.id = id;

        this.inputs = inputs;
        this.outputs = outputs;
        this.tickInputs = tickInputs;
        this.tickOutputs = tickOutputs;

        this.inputChanceLogics = inputChanceLogics;
        this.outputChanceLogics = outputChanceLogics;
        this.tickInputChanceLogics = tickInputChanceLogics;
        this.tickOutputChanceLogics = tickOutputChanceLogics;

        this.conditions = conditions;
        this.ingredientActions = ingredientActions;
        this.data = data;
        this.duration = duration;
        this.isFuel = isFuel;
        this.recipeCategory = (recipeCategory != GTRecipeCategory.DEFAULT) ? recipeCategory : recipeType.getCategory();
    }

    public Map<RecipeCapability<?>, List<Content>> copyContents(Map<RecipeCapability<?>, List<Content>> contents,
                                                                @Nullable ContentModifier modifier) {
        Map<RecipeCapability<?>, List<Content>> copyContents = new HashMap<>();
        for (var entry : contents.entrySet()) {
            var contentList = entry.getValue();
            var cap = entry.getKey();
            if (contentList != null && !contentList.isEmpty()) {
                List<Content> contentsCopy = new ArrayList<>();
                for (Content content : contentList) {
                    contentsCopy.add(content.copy(cap, modifier));
                }
                copyContents.put(entry.getKey(), contentsCopy);
            }
        }
        return copyContents;
    }

    public GTRecipe copy() {
        return new GTRecipe(recipeType, id,
                copyContents(inputs, null), copyContents(outputs, null),
                copyContents(tickInputs, null), copyContents(tickOutputs, null),
                new HashMap<>(inputChanceLogics), new HashMap<>(outputChanceLogics),
                new HashMap<>(tickInputChanceLogics), new HashMap<>(tickOutputChanceLogics),
                new ArrayList<>(conditions), new ArrayList<>(ingredientActions), data, duration, isFuel,
                recipeCategory);
    }

    public GTRecipe copy(ContentModifier modifier) {
        return copy(modifier, true);
    }

    public GTRecipe copy(ContentModifier modifier, boolean modifyDuration) {
        var copied = new GTRecipe(recipeType, id,
                copyContents(inputs, modifier), copyContents(outputs, modifier),
                copyContents(tickInputs, modifier), copyContents(tickOutputs, modifier),
                new HashMap<>(inputChanceLogics), new HashMap<>(outputChanceLogics),
                new HashMap<>(tickInputChanceLogics), new HashMap<>(tickOutputChanceLogics),
                new ArrayList<>(conditions),
                new ArrayList<>(ingredientActions), data, duration, isFuel, recipeCategory);
        if (modifyDuration) {
            copied.duration = modifier.apply(this.duration).intValue();
        }
        copied.parallels = parallels;
        return copied;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return GTRecipeSerializer.SERIALIZER;
    }

    @Override
    public @NotNull GTRecipeType getType() {
        return recipeType;
    }

    @Override
    public boolean matches(@NotNull Container pContainer, @NotNull Level pLevel) {
        return false;
    }

    @Override
    public ItemStack assemble(Container inventory, RegistryAccess registryManager) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryManager) {
        return ItemStack.EMPTY;
    }

    ///////////////////////////////////////////////////////////////
    // **********************internal logic********************* //
    ///////////////////////////////////////////////////////////////

    public List<Content> getInputContents(RecipeCapability<?> capability) {
        return inputs.getOrDefault(capability, Collections.emptyList());
    }

    public List<Content> getOutputContents(RecipeCapability<?> capability) {
        return outputs.getOrDefault(capability, Collections.emptyList());
    }

    public List<Content> getTickInputContents(RecipeCapability<?> capability) {
        return tickInputs.getOrDefault(capability, Collections.emptyList());
    }

    public List<Content> getTickOutputContents(RecipeCapability<?> capability) {
        return tickOutputs.getOrDefault(capability, Collections.emptyList());
    }

    public ActionResult matchRecipe(IRecipeCapabilityHolder holder) {
        return matchRecipe(holder, false);
    }

    public ActionResult matchTickRecipe(IRecipeCapabilityHolder holder) {
        return hasTick() ? matchRecipe(holder, true) : ActionResult.SUCCESS;
    }

    private ActionResult matchRecipe(IRecipeCapabilityHolder holder, boolean tick) {
        if (!holder.hasProxies()) return ActionResult.FAIL_NO_REASON;

        var result = matchRecipeContents(IO.IN, holder, tick ? tickInputs : inputs, tick);
        if (!result.isSuccess()) return result;

        result = matchRecipeContents(IO.OUT, holder, tick ? tickOutputs : outputs, tick);
        if (!result.isSuccess()) return result;

        return ActionResult.SUCCESS;
    }

    public ActionResult matchRecipeContents(IO io, IRecipeCapabilityHolder holder,
                                            Map<RecipeCapability<?>, List<Content>> contents,
                                            boolean isTick) {
        RecipeRunner runner = new RecipeRunner(this, io, isTick, holder, Collections.emptyMap(), true);
        for (Map.Entry<RecipeCapability<?>, List<Content>> entry : contents.entrySet()) {
            var result = runner.handle(entry);
            if (result == null)
                continue;

            if (result.result().content != null || !result.result().slots.isEmpty()) {
                if (io == IO.IN) {
                    return ActionResult.fail(() -> Component.translatable("gtceu.recipe_logic.insufficient_in")
                            .append(": ").append(result.capability().getName()), 0f);
                } else if (io == IO.OUT) {
                    return ActionResult.fail(() -> Component.translatable("gtceu.recipe_logic.insufficient_out")
                            .append(": ").append(result.capability().getName()), 0f);
                } else {
                    return ActionResult.FAIL_NO_REASON;
                }
            }
        }
        return ActionResult.SUCCESS;
    }

    public boolean handleTickRecipeIO(IO io, IRecipeCapabilityHolder holder,
                                      Map<RecipeCapability<?>, Object2IntMap<?>> chanceCaches) {
        if (!holder.hasProxies() || io == IO.BOTH) return false;
        return handleRecipe(io, holder, true, io == IO.IN ? tickInputs : tickOutputs, chanceCaches);
    }

    public boolean handleRecipeIO(IO io, IRecipeCapabilityHolder holder,
                                  Map<RecipeCapability<?>, Object2IntMap<?>> chanceCaches) {
        if (!holder.hasProxies() || io == IO.BOTH) return false;
        return handleRecipe(io, holder, false, io == IO.IN ? inputs : outputs, chanceCaches);
    }

    public boolean handleRecipe(IO io, IRecipeCapabilityHolder holder, boolean isTick,
                                Map<RecipeCapability<?>, List<Content>> contents,
                                Map<RecipeCapability<?>, Object2IntMap<?>> chanceCaches) {
        RecipeRunner runner = new RecipeRunner(this, io, isTick, holder, chanceCaches, false);
        for (Map.Entry<RecipeCapability<?>, List<Content>> entry : contents.entrySet()) {
            var handled = runner.handle(entry);
            if (handled == null)
                continue;

            if (handled.result().content != null || !handled.result().slots.isEmpty()) {
                GTCEu.LOGGER.warn("io error while handling a recipe {} outputs. holder: {}", id, holder);
                return false;
            }
        }
        return true;
    }

    public boolean hasTick() {
        return !tickInputs.isEmpty() || !tickOutputs.isEmpty();
    }

    public void preWorking(IRecipeCapabilityHolder holder) {
        handlePre(inputs, holder, IO.IN);
        handlePre(outputs, holder, IO.OUT);
    }

    public void postWorking(IRecipeCapabilityHolder holder) {
        handlePost(inputs, holder, IO.IN);
        handlePost(outputs, holder, IO.OUT);
    }

    public void handlePre(Map<RecipeCapability<?>, List<Content>> contents, IRecipeCapabilityHolder holder, IO io) {
        contents.forEach(((capability, tuples) -> {
            if (holder.getCapabilitiesProxy().contains(io, capability)) {
                for (IRecipeHandler<?> capabilityProxy : holder.getCapabilitiesProxy().get(io, capability)) {
                    capabilityProxy.preWorking(holder, io, this);
                }
            } else if (holder.getCapabilitiesProxy().contains(IO.BOTH, capability)) {
                for (IRecipeHandler<?> capabilityProxy : holder.getCapabilitiesProxy().get(IO.BOTH, capability)) {
                    capabilityProxy.preWorking(holder, io, this);
                }
            }
        }));
    }

    public void handlePost(Map<RecipeCapability<?>, List<Content>> contents, IRecipeCapabilityHolder holder, IO io) {
        contents.forEach(((capability, tuples) -> {
            if (holder.getCapabilitiesProxy().contains(io, capability)) {
                for (IRecipeHandler<?> capabilityProxy : holder.getCapabilitiesProxy().get(io, capability)) {
                    capabilityProxy.postWorking(holder, io, this);
                }
            } else if (holder.getCapabilitiesProxy().contains(IO.BOTH, capability)) {
                for (IRecipeHandler<?> capabilityProxy : holder.getCapabilitiesProxy().get(IO.BOTH, capability)) {
                    capabilityProxy.postWorking(holder, io, this);
                }
            }
        }));
    }

    public ActionResult checkConditions(@NotNull RecipeLogic recipeLogic) {
        if (conditions.isEmpty()) return ActionResult.SUCCESS;
        Map<RecipeConditionType<?>, List<RecipeCondition>> or = new HashMap<>();
        for (RecipeCondition condition : conditions) {
            if (condition.isOr()) {
                or.computeIfAbsent(condition.getType(), type -> new ArrayList<>()).add(condition);
            } else if (condition.test(this, recipeLogic) == condition.isReverse()) {
                return ActionResult.fail(() -> Component.translatable("gtceu.recipe_logic.condition_fails").append(": ")
                        .append(condition.getTooltips()));
            }
        }
        for (List<RecipeCondition> conditions : or.values()) {
            if (conditions.stream().allMatch(condition -> condition.test(this, recipeLogic) == condition.isReverse())) {
                return ActionResult.fail(() -> Component.translatable("gtceu.recipe_logic.condition_fails"));
            }
        }
        return ActionResult.SUCCESS;
    }

    /**
     * Trims the recipe outputs, chanced outputs, and fluid outputs based on the performing Machine's trim limit.
     */
    public GTRecipe trimRecipeOutputs(Map<RecipeCapability<?>, Integer> trimLimits) {
        // Fast return early if no trimming desired
        if (trimLimits.isEmpty() || trimLimits.values().stream().allMatch(integer -> integer == -1)) {
            return this;
        }

        GTRecipe current = this.copy();

        GTRecipeBuilder builder = new GTRecipeBuilder(current, this.recipeType);

        builder.output.clear();
        builder.tickOutput.clear();

        Map<RecipeCapability<?>, List<Content>> recipeOutputs = doTrim(current.outputs, trimLimits);
        Map<RecipeCapability<?>, List<Content>> recipeTickOutputs = doTrim(current.tickOutputs, trimLimits);

        builder.output.putAll(recipeOutputs);
        builder.tickOutput.putAll(recipeTickOutputs);

        return builder.buildRawRecipe();
    }

    /**
     * Returns the maximum possible recipe outputs from a recipe, divided into regular and chanced outputs
     * Takes into account any specific output limiters, ie macerator slots, to trim down the output list
     * Trims from chanced outputs first, then regular outputs
     *
     * @param trimLimits The limit(s) on the number of outputs, -1 for disabled.
     * @return All recipe outputs, limited by some factor(s)
     */
    public Map<RecipeCapability<?>, List<Content>> doTrim(Map<RecipeCapability<?>, List<Content>> current,
                                                          Map<RecipeCapability<?>, Integer> trimLimits) {
        Map<RecipeCapability<?>, List<Content>> outputs = new HashMap<>();

        Set<RecipeCapability<?>> trimmed = new HashSet<>();
        for (Map.Entry<RecipeCapability<?>, Integer> entry : trimLimits.entrySet()) {
            RecipeCapability<?> key = entry.getKey();

            if (!current.containsKey(key)) continue;
            List<Content> nonChanced = new ArrayList<>();
            List<Content> chanced = new ArrayList<>();
            for (Content content : current.getOrDefault(key, List.of())) {
                if (content.chance <= 0 || content.chance >= content.maxChance) nonChanced.add(content);
                else chanced.add(content);
            }

            int outputLimit = entry.getValue();
            if (outputLimit == -1) {
                outputs.computeIfAbsent(key, $ -> new ArrayList<>()).addAll(nonChanced);
            }
            // If just the regular outputs would satisfy the outputLimit
            else if (nonChanced.size() >= outputLimit) {
                outputs.computeIfAbsent(key, $ -> new ArrayList<>())
                        .addAll(nonChanced.stream()
                                .map(cont -> cont.copy(key, null))
                                .toList()
                                .subList(0, outputLimit));

                chanced.clear();
            }
            // If the regular outputs and chanced outputs are required to satisfy the outputLimit
            else if (!nonChanced.isEmpty() && (nonChanced.size() + chanced.size()) >= outputLimit) {
                outputs.computeIfAbsent(key, $ -> new ArrayList<>())
                        .addAll(nonChanced.stream().map(cont -> cont.copy(key, null)).toList());

                // Calculate the number of chanced outputs after adding all the regular outputs
                int numChanced = outputLimit - nonChanced.size();

                chanced = chanced.subList(0, Math.min(numChanced, chanced.size()));
            }
            // There are only chanced outputs to satisfy the outputLimit
            else if (nonChanced.isEmpty()) {
                chanced = chanced.subList(0, Math.min(outputLimit, chanced.size()));
            }
            // The number of outputs + chanced outputs is lower than the trim number, so just add everything
            else {
                outputs.computeIfAbsent(key, $ -> new ArrayList<>())
                        .addAll(nonChanced.stream().map(cont -> cont.copy(key, null)).toList());
                // Chanced outputs are taken care of in the original copy
            }

            if (!chanced.isEmpty())
                outputs.computeIfAbsent(key, $ -> new ArrayList<>())
                        .addAll(chanced.stream().map(cont -> cont.copy(key, null)).toList());

            trimmed.add(key);
        }
        for (Map.Entry<RecipeCapability<?>, List<Content>> entry : current.entrySet()) {
            if (trimmed.contains(entry.getKey())) continue;
            outputs.computeIfAbsent(entry.getKey(), $ -> new ArrayList<>()).addAll(entry.getValue());
        }

        return outputs;
    }

    /**
     * Get the chance logic for a recipe capability + io + tick io combination
     * 
     * @param cap the recipe capability to get the chance logic for
     * @param io  the {@link IO} of the chanche per-tick logic or the normal one
     * @return the chance logic for the aforementioned combination. Defaults to {@link ChanceLogic#OR}.
     */
    public ChanceLogic getChanceLogicForCapability(RecipeCapability<?> cap, IO io, boolean isTick) {
        if (io == IO.OUT) {
            if (isTick) {
                return tickOutputChanceLogics.getOrDefault(cap, ChanceLogic.OR);
            } else {
                return outputChanceLogics.getOrDefault(cap, ChanceLogic.OR);
            }
        } else if (io == IO.IN) {
            if (isTick) {
                return tickInputChanceLogics.getOrDefault(cap, ChanceLogic.OR);
            } else {
                return inputChanceLogics.getOrDefault(cap, ChanceLogic.OR);
            }
        }
        return ChanceLogic.OR;
    }

    /**
     *
     * @param isSuccess     is action success
     * @param reason        if fail, fail reason
     * @param expectingRate if recipe matching fail, the expecting rate of one cap.
     *                      <br>
     *                      For example, recipe require 300eu and 10 apples, and left 100eu and 5 apples after recipe
     *                      searching.
     *                      <br>
     *                      EU Missing Rate : 300 / (300 - 100) = 1.5
     *                      <br>
     *                      Item Missing Rate : 10 / (10 - 5) = 2
     *                      <br>
     *                      return max expecting rate --- 2
     */
    public static record ActionResult(boolean isSuccess, @Nullable Supplier<Component> reason, float expectingRate) {

        public final static ActionResult SUCCESS = new ActionResult(true, null, 0);
        public final static ActionResult FAIL_NO_REASON = new ActionResult(true, null, 0);

        public static ActionResult fail(@Nullable Supplier<Component> component) {
            return new ActionResult(false, component, 0);
        }

        public static ActionResult fail(@Nullable Supplier<Component> component, float expectingRate) {
            return new ActionResult(false, component, expectingRate);
        }
    }

    public boolean checkRecipeValid() {
        return checkItemValid(inputs, "input") && checkItemValid(outputs, "output") &&
                checkItemValid(tickInputs, "tickInput") && checkItemValid(tickOutputs, "tickOutput");
    }

    private boolean checkItemValid(Map<RecipeCapability<?>, List<Content>> contents, String name) {
        for (Content content : contents.getOrDefault(ItemRecipeCapability.CAP, Collections.emptyList())) {
            var items = ItemRecipeCapability.CAP.of(content.content).getItems();
            if (items.length == 0) {
                GTCEu.LOGGER.error("recipe {} {} item length is 0", id, name);
                return false;
            } else if (Arrays.stream(items).anyMatch(ItemStack::isEmpty)) {
                GTCEu.LOGGER.error("recipe {} {} item is empty", id, name);
                return false;
            }
        }
        return true;
    }

    // Just check id as there *should* only ever be 1 instance of a recipe with this id.
    // If this doesn't work, fix.
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GTRecipe recipe)) return false;
        return this.id.equals(recipe.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
