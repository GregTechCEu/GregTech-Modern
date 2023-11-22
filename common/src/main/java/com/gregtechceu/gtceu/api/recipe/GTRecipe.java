package com.gregtechceu.gtceu.api.recipe;

import com.google.common.collect.Table;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.feature.IVoidable;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote GTRecipe
 */
@SuppressWarnings({"ConstantValue", "rawtypes", "unchecked"})
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GTRecipe implements net.minecraft.world.item.crafting.Recipe<Container> {
    public final GTRecipeType recipeType;
    public final ResourceLocation id;
    public final Map<RecipeCapability<?>, List<Content>> inputs;
    public final Map<RecipeCapability<?>, List<Content>> outputs;
    public final Map<RecipeCapability<?>, List<Content>> tickInputs;
    public final Map<RecipeCapability<?>, List<Content>> tickOutputs;
    public final List<RecipeCondition> conditions;
    public CompoundTag data;
    public int duration;
    @Getter
    public boolean isFuel;

    public GTRecipe(GTRecipeType recipeType, ResourceLocation id, Map<RecipeCapability<?>, List<Content>> inputs, Map<RecipeCapability<?>, List<Content>> outputs, Map<RecipeCapability<?>, List<Content>> tickInputs, Map<RecipeCapability<?>, List<Content>> tickOutputs, List<RecipeCondition> conditions, CompoundTag data, int duration, boolean isFuel) {
        this.recipeType = recipeType;
        this.id = id;
        this.inputs = inputs;
        this.outputs = outputs;
        this.tickInputs = tickInputs;
        this.tickOutputs = tickOutputs;
        this.conditions = conditions;
        this.data = data;
        this.duration = duration;
        this.isFuel = isFuel;
    }

    public Map<RecipeCapability<?>, List<Content>> copyContents(Map<RecipeCapability<?>, List<Content>> contents, @Nullable ContentModifier modifier) {
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
        return new GTRecipe(recipeType, id, copyContents(inputs, null), copyContents(outputs, null), copyContents(tickInputs, null), copyContents(tickOutputs, null), conditions, data, duration, isFuel);
    }

    public GTRecipe copy(ContentModifier modifier) {
        return copy(modifier, true);
    }

    public GTRecipe copy(ContentModifier modifier, boolean modifyDuration) {
        var copied = new GTRecipe(recipeType, id, copyContents(inputs, modifier), copyContents(outputs, modifier), copyContents(tickInputs, modifier), copyContents(tickOutputs, modifier), conditions, data, duration, isFuel);
        if (modifyDuration) {
            copied.duration = modifier.apply(this.duration).intValue();
        }
        return copied;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return GTRecipeSerializer.SERIALIZER;
    }

    @Override
    public @NotNull RecipeType<?> getType() {
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
        if (!holder.hasProxies()) return ActionResult.FAIL_NO_REASON;
        var result = matchRecipe(IO.IN, holder, inputs, false);
        if (!result.isSuccess()) return result;
        result = matchRecipe(IO.OUT, holder, outputs, false);
        if (!result.isSuccess()) return result;
        return ActionResult.SUCCESS;
    }

    public ActionResult matchTickRecipe(IRecipeCapabilityHolder holder) {
        if (hasTick()) {
            if (!holder.hasProxies()) return ActionResult.FAIL_NO_REASON;
            var result = matchRecipe(IO.IN, holder, tickInputs, false);
            if (!result.isSuccess()) return result;
            result = matchRecipe(IO.OUT, holder, tickOutputs, false);
            if (!result.isSuccess()) return result;
        }
        return ActionResult.SUCCESS;
    }

    public ActionResult matchRecipe(IO io, IRecipeCapabilityHolder holder, Map<RecipeCapability<?>, List<Content>> contents, boolean calculateExpectingRate) {
        Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> capabilityProxies = holder.getCapabilitiesProxy();
        for (Map.Entry<RecipeCapability<?>, List<Content>> entry : contents.entrySet()) {
            Set<IRecipeHandler<?>> used = new HashSet<>();
            List content = new ArrayList<>();
            Map<String, List> contentSlot = new HashMap<>();
            for (Content cont : entry.getValue()) {
                if (cont.slotName == null) {
                    content.add(cont.content);
                } else {
                    contentSlot.computeIfAbsent(cont.slotName, s -> new ArrayList<>()).add(cont.content);
                }
            }
            RecipeCapability<?> capability = entry.getKey();
            content = content.stream().map(capability::copyContent).toList();
            if (content.isEmpty() && contentSlot.isEmpty()) continue;
            if (content.isEmpty()) content = null;

            var result = handlerContentsInternal(io, io, capabilityProxies, capability, used, content, contentSlot, content, contentSlot, true);
            if (result.getA() == null && result.getB().isEmpty()) continue;
            result = handlerContentsInternal(IO.BOTH, io, capabilityProxies, capability, used, result.getA(), result.getB(), content, contentSlot, true);

            if (result.getA() != null || !result.getB().isEmpty()) {
                var expectingRate = 0f;
                // TODO calculateExpectingRate
//                if (calculateExpectingRate) {
//                    if (result.getA() != null) {
//                        expectingRate = Math.max(capability.calculateAmount(result.getA()), expectingRate);
//                    }
//                    if (!result.getB().isEmpty()) {
//                        for (var c : result.getB().values()) {
//                            expectingRate = Math.max(capability.calculateAmount(c), expectingRate);
//                        }
//                    }
//                }
                if (io == IO.IN) {
                    return ActionResult.fail(() -> Component.translatable("gtceu.recipe_logic.insufficient_in").append(": ").append(capability.getTraslateComponent()), expectingRate);
                } else if (io == IO.OUT) {
                    return ActionResult.fail(() -> Component.translatable("gtceu.recipe_logic.insufficient_out").append(": ").append(capability.getTraslateComponent()), expectingRate);
                } else {
                    return ActionResult.FAIL_NO_REASON;
                }
            }
        }
        return ActionResult.SUCCESS;
    }

    public boolean handleTickRecipeIO(IO io, IRecipeCapabilityHolder holder) {
        if (!holder.hasProxies() || io == IO.BOTH) return false;
        return handleRecipe(io, holder, io == IO.IN ? tickInputs : tickOutputs);
    }

    public boolean handleRecipeIO(IO io, IRecipeCapabilityHolder holder) {
        if (!holder.hasProxies() || io == IO.BOTH) return false;
        return handleRecipe(io, holder, io == IO.IN ? inputs : outputs);
    }

    public boolean handleRecipe(IO io, IRecipeCapabilityHolder holder, Map<RecipeCapability<?>, List<Content>> contents) {
        Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> capabilityProxies = holder.getCapabilitiesProxy();
        for (Map.Entry<RecipeCapability<?>, List<Content>> entry : contents.entrySet()) {
            Set<IRecipeHandler<?>> used = new HashSet<>();
            List content = new ArrayList<>();
            Map<String, List> contentSlot = new HashMap<>();
            List contentSearch = new ArrayList<>();
            Map<String, List> contentSlotSearch = new HashMap<>();
            for (Content cont : entry.getValue()) {
                if (cont.slotName == null) {
                    contentSearch.add(cont.content);
                } else {
                    contentSlotSearch.computeIfAbsent(cont.slotName, s -> new ArrayList<>()).add(cont.content);
                }
                if (cont.chance >= 1 || GTValues.RNG.nextFloat() < (cont.chance + holder.getChanceTier() * cont.tierChanceBoost)) { // chance input
                    if (cont.slotName == null) {
                        content.add(cont.content);
                    } else {
                        contentSlot.computeIfAbsent(cont.slotName, s -> new ArrayList<>()).add(cont.content);
                    }
                }
            }
            RecipeCapability<?> capability = entry.getKey();
            content = content.stream().map(capability::copyContent).toList();
            if (content.isEmpty() && contentSlot.isEmpty()) continue;
            if (content.isEmpty()) content = null;

            var result = handlerContentsInternal(io, io, capabilityProxies, capability, used, content, contentSlot, contentSearch, contentSlotSearch, false);
            if (result.getA() == null && result.getB().isEmpty()) continue;
            result = handlerContentsInternal(IO.BOTH, io, capabilityProxies, capability, used, result.getA(), result.getB(), contentSearch, contentSlotSearch, false);

            if (result.getA() != null || !result.getB().isEmpty()) {
                GTCEu.LOGGER.warn("io error while handling a recipe {} outputs. holder: {}", id, holder);
                return false;
            }
        }
        return true;
    }

    private Tuple<List, Map<String, List>> handlerContentsInternal(
            IO capIO, IO io, Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> capabilityProxies,
            RecipeCapability<?> capability, Set<IRecipeHandler<?>> used,
            List content, Map<String, List> contentSlot,
            List contentSearch, Map<String, List> contentSlotSearch,
            boolean simulate) {
        if (capabilityProxies.contains(capIO, capability)) {
            var handlers = capabilityProxies.get(capIO, capability);
            // handle distinct first
            for (IRecipeHandler<?> handler : handlers) {
                if (!handler.isDistinct()) continue;
                var result = handler.handleRecipe(io, this, contentSearch, null, true);
                if (result == null) {
                    // check distint slot handler
                    if (handler.getSlotNames() != null && handler.getSlotNames().containsAll(contentSlotSearch.keySet())) {
                        boolean success = true;
                        for (var entry : contentSlotSearch.entrySet()) {
                            List<?> left = handler.handleRecipe(io, this, entry.getValue(), entry.getKey(), true);
                            if (left != null) {
                                success = false;
                                break;
                            }
                        }
                        if (success) {
                            if (!simulate) {
                                for (var entry : contentSlot.entrySet()) {
                                    handler.handleRecipe(io, this, entry.getValue(), entry.getKey(), false);
                                }
                            }
                            contentSlot.clear();
                        }
                    }
                    if (contentSlot.isEmpty()) {
                        if (!simulate) {
                            handler.handleRecipe(io, this, content, null, false);
                        }
                        content = null;
                    }
                }
                if (content == null && contentSlot.isEmpty()) {
                    break;
                }
            }
            if (content != null || !contentSlot.isEmpty()) {
                // handle undistinct later
                for (IRecipeHandler<?> proxy : handlers) {
                    if (used.contains(proxy) || proxy.isDistinct()) continue;
                    used.add(proxy);
                    if (content != null) {
                        content = proxy.handleRecipe(io, this, content, null, simulate);
                    }
                    if (proxy.getSlotNames() != null) {
                        Iterator<String> iterator = contentSlot.keySet().iterator();
                        while (iterator.hasNext()) {
                            String key = iterator.next();
                            if (proxy.getSlotNames().contains(key)) {
                                List<?> left = proxy.handleRecipe(io, this, contentSlot.get(key), key, simulate);
                                if (left == null) iterator.remove();
                            }
                        }
                    }
                    if (content == null && contentSlot.isEmpty()) break;
                }
            }
        }
        return new Tuple<>(content, contentSlot);
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

    public ActionResult checkConditions(@Nonnull RecipeLogic recipeLogic) {
        if (conditions.isEmpty()) return ActionResult.SUCCESS;
        Map<String, List<RecipeCondition>> or = new HashMap<>();
        for (RecipeCondition condition : conditions) {
            if (condition.isOr()) {
                or.computeIfAbsent(condition.getType(), type -> new ArrayList<>()).add(condition);
            } else if (condition.test(this, recipeLogic) == condition.isReverse()) {
                return ActionResult.fail(() -> Component.translatable("gtceu.recipe_logic.condition_fails").append(": ").append(condition.getTooltips()));
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
    public Map<RecipeCapability<?>, List<Content>> doTrim(Map<RecipeCapability<?>, List<Content>> current, Map<RecipeCapability<?>, Integer> trimLimits) {
        Map<RecipeCapability<?>, List<Content>> outputs = new HashMap<>();

        Set<RecipeCapability<?>> trimmed = new HashSet<>();
        for (Map.Entry<RecipeCapability<?>, Integer> entry : trimLimits.entrySet()) {
            RecipeCapability<?> key = entry.getKey();

            if (!current.containsKey(key)) continue;
            List<Content> nonChanced = new ArrayList<>();
            List<Content> chanced = new ArrayList<>();
            for (Content content : current.getOrDefault(key, List.of())) {
                if (content.chance <= 0 || content.chance >= 1) nonChanced.add(content);
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
                outputs.computeIfAbsent(key, $ -> new ArrayList<>()).addAll(nonChanced.stream().map(cont -> cont.copy(key, null)).toList());

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
                outputs.computeIfAbsent(key, $ -> new ArrayList<>()).addAll(nonChanced.stream().map(cont -> cont.copy(key, null)).toList());
                // Chanced outputs are taken care of in the original copy
            }

            if (!chanced.isEmpty())
                outputs.computeIfAbsent(key, $ -> new ArrayList<>()).addAll(chanced.stream().map(cont -> cont.copy(key, null)).toList());

            trimmed.add(key);
        }
        for (Map.Entry<RecipeCapability<?>, List<Content>> entry : current.entrySet()) {
            if (trimmed.contains(entry.getKey())) continue;
            outputs.computeIfAbsent(entry.getKey(), $ -> new ArrayList<>()).addAll(entry.getValue());
        }

        return outputs;
    }

    /**
     *
     * @param isSuccess is action success
     * @param reason if fail, fail reason
     * @param expectingRate if recipe matching fail, the expecting rate of one cap.
     *                    <br>
     *                    For example, recipe require 300eu and 10 apples, and left 100eu and 5 apples after recipe searching.
     *                    <br>
     *                    EU Missing Rate : 300 / (300 - 100) = 1.5
     *                    <br>
     *                    Item Missing Rate : 10 / (10 - 5) = 2
     *                    <br>
     *                    return max expecting rate --- 2
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
        return checkItemValid(inputs, "input") && checkItemValid(outputs, "output") && checkItemValid(tickInputs, "tickInput") && checkItemValid(tickOutputs, "tickOutput");
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
}
