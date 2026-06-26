package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerGroup;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerGroupDistinctness;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.ResearchData;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredientExtensions;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.*;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.recipe.condition.ResearchCondition;
import com.gregtechceu.gtceu.common.valueprovider.*;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.xei.entry.item.ItemEntryList;
import com.gregtechceu.gtceu.integration.xei.entry.item.ItemStackList;
import com.gregtechceu.gtceu.integration.xei.entry.item.ItemTagList;
import com.gregtechceu.gtceu.integration.xei.handlers.item.CycleItemEntryHandler;
import com.gregtechceu.gtceu.integration.xei.widgets.GTRecipeWidget;
import com.gregtechceu.gtceu.utils.*;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.jei.IngredientIO;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.*;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import it.unimi.dsi.fastutil.objects.*;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static com.gregtechceu.gtceu.api.recipe.RecipeHelper.addToRecipeHandlerMap;

@ExtensionMethod(SizedIngredientExtensions.class)
public class ItemRecipeCapability extends RecipeCapability<SizedIngredient> {

    public final static ItemRecipeCapability CAP = new ItemRecipeCapability();

    protected ItemRecipeCapability() {
        super("item", 0xFFD96106, true, 0, SerializerIngredient.INSTANCE);
    }

    @Override
    public SizedIngredient copyInner(SizedIngredient content) {
        return content;
    }

    @Override
    public SizedIngredient copyWithModifier(SizedIngredient content, ContentModifier modifier) {
        if (content.getContainedCustom() instanceof IntProviderIngredient provider) {
            return new SizedIngredient(IntProviderIngredient.of(provider.getInner(),
                    ModifiedIntProvider.of(provider.getCountProvider(), modifier)).toVanilla(), content.count());
        }
        return content.copyWithCount(modifier.apply(content.count()));
    }

    public IntProviderIngredient copyWithModifier(IntProviderIngredient content, ContentModifier modifier) {
        return IntProviderIngredient.of(content.getInner(),
                ModifiedIntProvider.of(content.getCountProvider(), modifier));
    }

    @Override
    public List<Object> compressIngredients(@Unmodifiable Collection<Object> ingredients) {
        List<Object> list = new ObjectArrayList<>(ingredients.size());
        for (Object item : ingredients) {
            if (item instanceof SizedIngredient ingredient) {
                boolean isEqual = false;
                for (Object obj : list) {
                    if (obj instanceof SizedIngredient ingredient1) {
                        if (ingredient.ingredient().equals(ingredient1.ingredient())) {
                            isEqual = true;
                            break;
                        }
                    } else if (obj instanceof ItemStack stack) {
                        if (ingredient.ingredient().test(stack)) {
                            isEqual = true;
                            break;
                        }
                    }
                }
                if (isEqual) continue;
                // spotless:off
                if (ingredient.getContainedCustom() instanceof IntCircuitIngredient) {
                    list.addFirst(ingredient);
                } else if (ingredient.getContainedCustom() instanceof IntProviderIngredient intProvider &&
                        intProvider.getInner().getCustomIngredient() instanceof IntCircuitIngredient) {
                    list.addFirst(ingredient);
                } else {
                    list.add(ingredient);
                }
                // spotless:on
            } else if (item instanceof ItemStack stack) {
                boolean isEqual = false;
                for (Object obj : list) {
                    if (obj instanceof Ingredient ingredient) {
                        if (ingredient.test(stack)) {
                            isEqual = true;
                            break;
                        }
                    } else if (obj instanceof ItemStack stack1) {
                        if (ItemStack.isSameItemSameComponents(stack, stack1)) {
                            isEqual = true;
                            break;
                        }
                    }
                }
                if (isEqual) continue;
                list.add(stack);
            }
        }
        return list;
    }

    @Override
    public @Nullable List<AbstractMapIngredient> getDefaultMapIngredient(Object object) {
        if (object instanceof Ingredient ingredient) {
            return CustomItemMapIngredient.from(ingredient);
        } else if (object instanceof SizedIngredient sized) {
            return getDefaultMapIngredient(sized.ingredient());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isRecipeSearchFilter() {
        return true;
    }

    @Override
    public int limitMaxParallelByOutput(IRecipeCapabilityHolder holder, GTRecipe recipe, int multiplier, boolean tick) {
        if (holder instanceof ICustomParallel p) return p.limitItemParallel(recipe, multiplier, tick);
        var outputContents = (tick ? recipe.tickOutputs : recipe.outputs).get(this);
        if (outputContents == null || outputContents.isEmpty()) return multiplier;

        if (!holder.hasCapabilityProxies()) return 0;

        var handlers = holder.getCapabilitiesFlat(IO.OUT, this);
        if (handlers.isEmpty()) return 0;

        int minMultiplier = 0;
        int maxMultiplier = multiplier;

        int maxCount = 0;
        List<SizedIngredient> ingredients = new ArrayList<>(outputContents.size());
        for (var content : outputContents) {
            var ing = of(content.content);

            int count;
            if (ing.getContainedCustom() instanceof IntProviderIngredient provider) {
                count = provider.getCountProvider().getMaxValue();
            } else {
                count = ing.count();
            }

            maxCount = Math.max(maxCount, count);
            ingredients.add(ing);
        }

        if (maxCount == 0) return multiplier;
        if (multiplier > Integer.MAX_VALUE / maxCount) {
            maxMultiplier = multiplier = Integer.MAX_VALUE / maxCount;
        }

        while (minMultiplier != maxMultiplier) {
            List<SizedIngredient> copied = new ArrayList<>();
            for (final var ing : ingredients) {
                copied.add(copyWithModifier(ing, ContentModifier.multiplier(multiplier)));
            }
            for (var handler : handlers) {
                // noinspection unchecked
                copied = (List<SizedIngredient>) handler.handleRecipe(IO.OUT, recipe, copied, true);
                if (copied == null) break;
            }
            int[] bin = ParallelLogic.adjustMultiplier(copied == null, minMultiplier, multiplier, maxMultiplier);
            minMultiplier = bin[0];
            multiplier = bin[1];
            maxMultiplier = bin[2];
        }

        return multiplier;
    }

    @Override
    public int getMaxParallelByInput(IRecipeCapabilityHolder holder, GTRecipe recipe, int limit, boolean tick) {
        if (!holder.hasCapabilityProxies()) return 0;

        var inputs = (tick ? recipe.tickInputs : recipe.inputs).get(this);
        if (inputs == null || inputs.isEmpty()) return limit;

        // Find all the items in the combined Item Input inventories and create oversized ItemStacks
        List<Object2LongMap<ItemStack>> inventoryGroups = getInputContents(holder);
        if (inventoryGroups.isEmpty()) return 0;

        // map the recipe ingredients to account for duplicated and notConsumable ingredients.
        // notConsumable ingredients are not counted towards the max ratio

        var nonConsumables = new Object2LongOpenHashMap<SizedIngredient>();
        var consumables = new Object2LongOpenHashMap<SizedIngredient>();
        for (Content content : inputs) {
            SizedIngredient ing = of(content.content);
            if (ing.getContainedCustom() instanceof IntCircuitIngredient) continue;

            int count;
            if (ing.getContainedCustom() instanceof IntProviderIngredient provider)
                count = provider.getCountProvider().getMaxValue();
            else count = ing.count();

            if (content.chance == 0) {
                nonConsumables.addTo(ing, count);
            } else {
                boolean has = false;
                for (var recipeIng : consumables.object2LongEntrySet()) {
                    var stack = ing.getItems()[0];
                    if (recipeIng.getKey().test(stack)) {
                        recipeIng.setValue(recipeIng.getLongValue() + stack.getCount());
                        has = true;
                        break;
                    }
                }
                if (!has) {
                    consumables.addTo(ing, count);
                }
            }
        }

        // is this even possible
        if (consumables.isEmpty() && nonConsumables.isEmpty()) return limit;

        int maxMultiplier = 0;
        // Check every inventory group
        for (var group : inventoryGroups) {
            // Check for enough NC in inventory group
            boolean satisfied = true;
            for (var ncEntry : Object2LongMaps.fastIterable(nonConsumables)) {
                SizedIngredient ingredient = ncEntry.getKey();
                long needed = ncEntry.getLongValue();
                for (var stackEntry : Object2LongMaps.fastIterable(group)) {
                    if (ingredient.ingredient().test(stackEntry.getKey())) {
                        long count = stackEntry.getLongValue();
                        long lesser = Math.min(needed, count);
                        count -= lesser;
                        needed -= lesser;
                        stackEntry.setValue(count);
                        if (needed == 0) break;
                    }
                }
                if (needed > 0) {
                    satisfied = false;
                    break;
                }
            }
            // Not enough NC -> skip this inventory
            if (!satisfied) continue;
            // Satisfied NC + no consumables -> early return
            if (consumables.isEmpty()) return limit;

            int invMultiplier = Integer.MAX_VALUE;
            // Loop over all consumables

            for (var cEntry : Object2LongMaps.fastIterable(consumables)) {
                SizedIngredient ingredient = cEntry.getKey();
                final long needed = cEntry.getLongValue();
                final long maxNeeded = needed * limit;
                long available = 0;
                // Search stacks in our inventory group, summing them up
                for (var stackEntry : Object2LongMaps.fastIterable(group)) {
                    if (ingredient.ingredient().test(stackEntry.getKey())) {
                        available += stackEntry.getLongValue();
                        // We can stop if we already have enough for max parallel
                        if (available >= maxNeeded) break;
                    }
                }
                // ratio will equal 0 if available < needed
                int ratio = GTMath.saturatedCast(Math.min(limit, available / needed));
                invMultiplier = Math.min(invMultiplier, ratio);
                // Not enough of this ingredient in this group -> skip inventory
                if (ratio == 0) break;
            }
            // We found an inventory group that can do max parallel -> early return
            if (invMultiplier == limit) return limit;
            maxMultiplier = Math.max(maxMultiplier, invMultiplier);
        }

        return maxMultiplier;
    }

    private static List<Object2LongMap<ItemStack>> getInputContents(IRecipeCapabilityHolder holder) {
        var handlerLists = holder.getCapabilitiesForIO(IO.IN);
        if (handlerLists.isEmpty()) return Collections.emptyList();

        Map<RecipeHandlerGroup, List<RecipeHandlerList>> handlerGroups = new HashMap<>();
        for (var handler : handlerLists) {
            if (!handler.hasCapability(ItemRecipeCapability.CAP)) continue;
            addToRecipeHandlerMap(handler.getGroup(), handler, handlerGroups);
        }

        final var strat = ItemStackHashStrategy.comparingAllButCount();

        List<RecipeHandlerList> distinctHandlerLists = handlerGroups.getOrDefault(
                RecipeHandlerGroupDistinctness.BUS_DISTINCT,
                Collections.emptyList());
        List<Object2LongMap<ItemStack>> invs = new ArrayList<>(distinctHandlerLists.size() + 1);
        // Handle distinct groups first, adding an inventory based on their contents individually.
        for (RecipeHandlerList handlerList : distinctHandlerLists) {
            var handlers = handlerList.getCapability(ItemRecipeCapability.CAP);
            Object2LongOpenCustomHashMap<ItemStack> distinctInv = new Object2LongOpenCustomHashMap<>(strat);

            for (IRecipeHandler<?> handler : handlers) {
                for (var content : handler.getContents()) {
                    if (content instanceof ItemStack stack && !stack.isEmpty()) {
                        distinctInv.addTo(stack, stack.getCount());
                    }
                }
            }
            if (!distinctInv.isEmpty()) invs.add(distinctInv);
        }

        // Then handle other groups. The logic of undyed buses belonging to
        // everything has already been taken care of by addToRecipeMap()
        for (Map.Entry<RecipeHandlerGroup, List<RecipeHandlerList>> handlerListEntry : handlerGroups.entrySet()) {
            if (handlerListEntry.getKey() == RecipeHandlerGroupDistinctness.BUS_DISTINCT) continue;

            Object2LongOpenCustomHashMap<ItemStack> inventory = new Object2LongOpenCustomHashMap<>(strat);
            for (RecipeHandlerList handlerList : handlerListEntry.getValue()) {
                var handlers = handlerList.getCapability(ItemRecipeCapability.CAP);
                for (var handler : handlers) {
                    for (var content : handler.getContents()) {
                        if (content instanceof ItemStack stack && !stack.isEmpty()) {
                            inventory.addTo(stack, stack.getCount());
                        }
                    }
                }
            }
            if (!inventory.isEmpty()) invs.add(inventory);
        }

        return invs;
    }

    @Override
    public @NotNull List<Object> createXEIContainerContents(List<Content> contents, GTRecipe recipe, IO io) {
        List<Object> entryLists = contents.stream()
                .map(Content::getContent)
                .map(this::of)
                .map(ItemRecipeCapability::mapItem)
                .collect(Collectors.toList());

        if (io == IO.OUT && recipe.recipeType.isScanner()) {
            List<Object> scannerPossibilities = new ArrayList<>();
            // Scanner Output replacing, used for cycling research outputs
            ResearchManager.ResearchItem researchData = null;
            for (Content stack : recipe.getOutputContents(this)) {
                ItemStack[] stacks = this.of(stack.content).getItems();
                if (stacks.length == 0 || stacks[0].isEmpty()) continue;

                researchData = stacks[0].get(GTDataComponents.RESEARCH_ITEM);
                if (researchData != null) break;
            }
            if (researchData != null) {
                Collection<GTRecipe> possibleRecipes = researchData.recipeType()
                        .getDataStickEntry(researchData.researchId());
                Set<ItemStack> cache = new ObjectOpenCustomHashSet<>(ItemStackHashStrategy.comparingItem());
                if (possibleRecipes != null) {
                    for (GTRecipe r : possibleRecipes) {
                        var outputs = r.getOutputContents(this);
                        if (outputs.isEmpty()) continue;

                        Content outputContent = outputs.get(0);
                        ItemStack[] stacks = this.of(outputContent.content).getItems();
                        if (stacks.length == 0) continue;

                        ItemStack researchStack = stacks[0];
                        if (!researchStack.isEmpty() && !cache.contains(researchStack)) {
                            cache.add(researchStack);
                            scannerPossibilities.add(ItemStackList.of(researchStack.copyWithCount(1)));
                        }
                    }
                }
                scannerPossibilities.add(entryLists.getFirst());
                entryLists = scannerPossibilities;
            }
        }

        while (entryLists.size() < recipe.recipeType.getMaxOutputs(this)) entryLists.add(null);
        return entryLists;
    }

    public Object createXEIContainer(List<?> contents) {
        // cast is safe if you don't pass the wrong thing.
        // noinspection unchecked
        return new CycleItemEntryHandler((List<ItemEntryList>) contents);
    }

    @NotNull
    @Override
    public Widget createWidget() {
        SlotWidget slot = new SlotWidget();
        slot.initTemplate();
        return slot;
    }

    @NotNull
    @Override
    public Class<? extends Widget> getWidgetClass() {
        return SlotWidget.class;
    }

    @Override
    public void applyWidgetInfo(@NotNull Widget widget,
                                int index,
                                boolean isXEI,
                                IO io,
                                GTRecipeTypeUI.@UnknownNullability("null when storage == null") RecipeHolder recipeHolder,
                                @NotNull GTRecipeType recipeType,
                                @UnknownNullability("null when content == null") GTRecipe recipe,
                                @Nullable Content content,
                                @Nullable Object storage, int recipeTier, int chanceTier) {
        if (widget instanceof SlotWidget slot) {
            if (storage instanceof IItemHandlerModifiable items) {
                if (index >= 0 && index < items.getSlots()) {
                    slot.setHandlerSlot(items, index);
                    slot.setIngredientIO(io == IO.IN ? IngredientIO.INPUT : IngredientIO.OUTPUT);
                    slot.setCanTakeItems(!isXEI);
                    slot.setCanPutItems(!isXEI && io.support(IO.IN));
                }
                // 1 over container size.
                // If in a recipe viewer and a research slot can be added, add it.
                if (isXEI && recipeType.isHasResearchSlot() && index == items.getSlots()) {
                    if (ConfigHolder.INSTANCE.machines.enableResearch) {
                        ResearchCondition condition = recipeHolder.conditions().stream()
                                .filter(ResearchCondition.class::isInstance).findAny()
                                .map(ResearchCondition.class::cast).orElse(null);
                        if (condition != null) {
                            List<ItemStack> dataItems = new ArrayList<>();
                            for (ResearchData.ResearchEntry entry : condition.data) {
                                ItemStack dataStick = entry.dataItem().copy();
                                dataStick.set(GTDataComponents.RESEARCH_ITEM,
                                        new ResearchManager.ResearchItem(entry.researchId(), recipeType));
                                dataItems.add(dataStick);
                            }
                            CycleItemEntryHandler handler = CycleItemEntryHandler.createFromStacks(List.of(dataItems));
                            slot.setHandlerSlot(handler, 0);
                            slot.setIngredientIO(IngredientIO.CATALYST);
                            slot.setCanTakeItems(false);
                            slot.setCanPutItems(false);
                        }
                    }
                }
            }
            if (content != null) {
                float chance = (float) recipeType.getChanceFunction()
                        .getBoostedChance(content, recipeTier, chanceTier) / content.maxChance;
                slot.setXEIChance(chance);
                slot.setOnAddedTooltips((w, tooltips) -> {
                    GTRecipeWidget.setConsumedChance(content,
                            recipe.getChanceLogicForCapability(this, io, isTickSlot(index, io, recipe)),
                            tooltips, recipeTier, chanceTier, recipeType.getChanceFunction());
                    // spotless:off
                    if (this.of(content.content).getContainedCustom() instanceof IntProviderIngredient ingredient) {
                        IntProvider countProvider = ingredient.getCountProvider();
                        tooltips.add(Component.translatable("gtceu.gui.content.count_range",
                                        countProvider.getMinValue(), countProvider.getMaxValue())
                                .withStyle(ChatFormatting.GOLD));
                    } else if (this.of(content.content) instanceof SizedIngredient sizedIngredient &&
                            sizedIngredient.getContainedCustom() instanceof IntProviderIngredient ingredient) {
                        IntProvider countProvider = ingredient.getCountProvider();
                        tooltips.add(Component.translatable("gtceu.gui.content.count_range",
                                        countProvider.getMinValue(), countProvider.getMaxValue())
                                .withStyle(ChatFormatting.GOLD));
                    }
                    // spotless:on
                    if (isTickSlot(index, io, recipe)) {
                        tooltips.add(Component.translatable("gtceu.gui.content.per_tick"));
                    }
                });
                if (io == IO.IN && (content.chance == 0 ||
                        this.of(content.content).getContainedCustom() instanceof IntCircuitIngredient)) {
                    slot.setIngredientIO(IngredientIO.CATALYST);
                }
            }
        }
    }

    // Maps ingredients to an ItemEntryList for XEI: either an ItemTagList or an ItemStackList
    private static ItemEntryList mapItem(final SizedIngredient ingredient) {
        if (ingredient.getContainedCustom() instanceof IntProviderIngredient intProvider) {
            var mapped = tryMapInner(intProvider.getInner(), 1);
            if (mapped != null) return mapped;
        } else if (ingredient.getContainedCustom() instanceof IntersectionIngredient intersection) {
            return mapIntersection(intersection, ingredient.count());
        } else if (!ingredient.ingredient().isCustom()) {
            var tagList = tryMapTag(ingredient.ingredient(), ingredient.count());
            if (tagList != null) return tagList;
        }
        ItemStackList stackList = new ItemStackList();
        boolean isIntProvider = ingredient.getContainedCustom() instanceof IntProviderIngredient;

        UnaryOperator<ItemStack> setCount = stack -> isIntProvider ? stack.copyWithCount(1) : stack;
        Arrays.stream(ingredient.getItems())
                .map(setCount)
                .forEach(stackList::add);
        return stackList;
    }

    @SuppressWarnings("SameParameterValue")
    private static @Nullable ItemEntryList tryMapInner(final Ingredient inner, int count) {
        if (inner.getCustomIngredient() instanceof IntProviderIngredient intProvider) {
            var mapped = tryMapInner(intProvider.getInner(), 1);
            if (mapped != null) return mapped;
        } else if (inner.getCustomIngredient() instanceof IntersectionIngredient intersection) {
            return mapIntersection(intersection, count);
        } else if (!inner.isCustom()) {
            var tagList = tryMapTag(inner, count);
            if (tagList != null) return tagList;
        }
        ItemStackList stackList = new ItemStackList();
        boolean isIntProvider = inner.getCustomIngredient() instanceof IntProviderIngredient;

        UnaryOperator<ItemStack> setCount = (stack) -> isIntProvider ? stack.copyWithCount(1) : stack;
        Arrays.stream(inner.getItems())
                .map(setCount)
                .forEach(stackList::add);
        return stackList;
    }

    // Map intersection ingredients to the items inside, as recipe viewers don't support them.
    private static ItemEntryList mapIntersection(final IntersectionIngredient intersection, int amount) {
        List<Ingredient> children = intersection.children();
        if (children.isEmpty()) return new ItemStackList();

        var childList = mapItem(new SizedIngredient(children.getFirst(), amount));
        ItemStackList stackList = new ItemStackList();
        for (var stack : childList.getStacks()) {
            if (children.stream().skip(1).allMatch(child -> child.test(stack))) {
                if (amount > 0) stackList.add(stack.copyWithCount(amount));
                else stackList.add(stack.copy());
            }
        }
        return stackList;
    }

    private static @Nullable ItemTagList tryMapTag(final Ingredient ingredient, int amount) {
        if (ingredient.isCustom()) {
            GTCEu.LOGGER.error("tried to map unexpected ingredient type {}", ingredient.getCustomIngredient());
            return null;
        }

        var values = ingredient.getValues();
        if (values.length > 0 && values[0] instanceof Ingredient.TagValue(TagKey<Item> tag)) {
            return ItemTagList.of(tag, amount, DataComponentPatch.EMPTY);
        }
        return null;
    }

    public interface ICustomParallel {

        /**
         * Custom impl of the parallel limiter used by ParallelLogic to limit by outputs
         *
         * @param recipe     Recipe
         * @param multiplier Initial multiplier
         * @param tick       Tick or not
         * @return Limited multiplier
         */
        int limitItemParallel(GTRecipe recipe, int multiplier, boolean tick);
    }

    // Items should be respected for distinct checks
    @Override
    public boolean shouldBypassDistinct() {
        return false;
    }
}
