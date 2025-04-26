package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.material.ChemicalHelper;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.ResearchData;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredientExtensions;
import com.gregtechceu.gtceu.api.recipe.kind.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.lookup.*;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.*;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.common.recipe.condition.ResearchCondition;
import com.gregtechceu.gtceu.common.valueprovider.AddedFloat;
import com.gregtechceu.gtceu.common.valueprovider.CastedFloat;
import com.gregtechceu.gtceu.common.valueprovider.FlooredInt;
import com.gregtechceu.gtceu.common.valueprovider.MultipliedFloat;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.item.GTDataComponents;
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
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.*;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;

import it.unimi.dsi.fastutil.objects.*;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

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
        if (content.getContainedCustom() instanceof IntProviderIngredient intProviderIngredient) {
            var newIntProvider = new FlooredInt(
                    new AddedFloat(
                            new MultipliedFloat(
                                    new CastedFloat(intProviderIngredient.getCountProvider()),
                                    ConstantFloat.of((float) modifier.multiplier())),
                            ConstantFloat.of((float) modifier.addition())));
            var newIngredient = IntProviderIngredient.create(intProviderIngredient.getInner(), newIntProvider);
            return new SizedIngredient(newIngredient, 1);
        }
        return content.copyWithCount(modifier.apply(content.count()));
    }

    @Override
    public List<AbstractMapIngredient> convertToMapIngredient(Object obj) {
        List<AbstractMapIngredient> ingredients = new ObjectArrayList<>(1);
        if (obj instanceof SizedIngredient ingredient) {
            ICustomIngredient custom = ingredient.getContainedCustom();

            // all kinds of special cases
            switch (custom) {
                case DataComponentIngredient component when component.isStrict() -> ingredients
                        .addAll(MapItemStackStrictComponentIngredient.from(component));
                case DataComponentIngredient component when !component.isStrict() -> ingredients
                        .addAll(MapItemStackPartialComponentIngredient.from(component));
                case IntCircuitIngredient circuit -> ingredients
                        .addAll(MapItemStackStrictComponentIngredient.from(circuit.convertToData()));
                case IntersectionIngredient intersection -> ingredients
                        .add(new MapItemIntersectionIngredient(intersection));
                case CompoundIngredient compound -> {
                    for (Ingredient inner : compound.children()) {
                        ingredients.addAll(convertToMapIngredient(inner));
                    }
                }
                case IntProviderIngredient intProvider -> {
                    switch (intProvider.getInner().getCustomIngredient()) {
                        case DataComponentIngredient component when component.isStrict() -> ingredients
                                .addAll(MapItemStackStrictComponentIngredient.from(component));
                        case DataComponentIngredient component when !component.isStrict() -> ingredients
                                .addAll(MapItemStackPartialComponentIngredient.from(component));
                        case IntCircuitIngredient circuit -> ingredients
                                .addAll(MapItemStackPartialComponentIngredient.from(circuit.convertToData()));
                        case IntersectionIngredient intersection -> ingredients
                                .add(new MapItemIntersectionIngredient(intersection));
                        case CompoundIngredient compound -> {
                            for (Ingredient inner : compound.children()) {
                                ingredients.addAll(convertToMapIngredient(inner));
                            }
                        }
                        case null, default -> {
                            for (Ingredient.Value value : ingredient.ingredient().getValues()) {
                                if (value instanceof Ingredient.TagValue(TagKey<Item> tag)) {
                                    ingredients.add(new MapItemTagIngredient(tag));
                                } else {
                                    Collection<ItemStack> stacks = value.getItems();
                                    for (ItemStack stack : stacks) {
                                        ingredients.add(new MapItemStackIngredient(stack, ingredient.ingredient()));
                                    }
                                }
                            }
                        }
                    }
                }
                case null, default -> {
                    for (Ingredient.Value value : ingredient.ingredient().getValues()) {
                        if (value instanceof Ingredient.TagValue(TagKey<Item> tag)) {
                            ingredients.add(new MapItemTagIngredient(tag));
                        } else {
                            Collection<ItemStack> stacks = value.getItems();
                            for (ItemStack stack : stacks) {
                                ingredients.add(new MapItemStackIngredient(stack, ingredient.ingredient()));
                            }
                        }
                    }
                }
            }
        } else if (obj instanceof ItemStack stack) {
            ingredients.add(new MapItemStackIngredient(stack));

            stack.getTags().forEach(tag -> ingredients.add(new MapItemTagIngredient(tag)));
            if (!stack.isComponentsPatchEmpty()) {
                // formatter:off
                ingredients
                        .add(new MapItemStackStrictComponentIngredient(stack,
                                (DataComponentIngredient) DataComponentIngredient.of(true, stack)
                                        .getCustomIngredient()));
                DataComponentPredicate.Builder builder = DataComponentPredicate.builder();
                for (var entry : stack.getComponentsPatch().entrySet()) {
                    if (entry.getValue().isEmpty()) continue;
                    builder.expect((DataComponentType) entry.getKey(), entry.getValue().get());
                }
                ingredients.add(new MapItemStackPartialComponentIngredient(stack,
                        (DataComponentIngredient) DataComponentIngredient.of(false, builder.build(), stack.getItem())
                                .getCustomIngredient()));
                // formatter:on
            }
            TagPrefix prefix = ChemicalHelper.getPrefix(stack.getItem());
            if (!prefix.isEmpty() && TagPrefix.ORES.containsKey(prefix)) {
                Material material = ChemicalHelper.getMaterialStack(stack.getItem()).material();
                ingredients.add(new MapItemIntersectionIngredient((IntersectionIngredient) IntersectionIngredient
                        .of(Ingredient.of(prefix.getItemTags(material).getFirst()),
                                Ingredient.of(prefix.getItemParentTags().getFirst()))
                        .getCustomIngredient()));
            }
        }
        return ingredients;
    }

    @Override
    public List<Object> compressIngredients(Collection<Object> ingredients) {
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
                        if (ingredient.test(stack)) {
                            isEqual = true;
                            break;
                        }
                    }
                }
                if (isEqual) continue;
                // formatter:off
                if (ingredient.ingredient().getCustomIngredient() instanceof IntCircuitIngredient) {
                    list.addFirst(ingredient);
                } else if (ingredient.ingredient().getCustomIngredient() instanceof IntProviderIngredient intProvider &&
                        intProvider.getInner().getCustomIngredient() instanceof IntCircuitIngredient) {
                            list.addFirst(ingredient);
                        } else {
                            list.add(ingredient);
                        }
                // formatter:on
            } else if (item instanceof ItemStack stack) {
                boolean isEqual = false;
                for (Object obj : list) {
                    if (obj instanceof Ingredient ingredient) {
                        if (ingredient.test(stack)) {
                            isEqual = true;
                            break;
                        }
                    } else if (obj instanceof ItemStack stack1) {
                        if (ItemStack.isSameItem(stack, stack1)) {
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
    public boolean isRecipeSearchFilter() {
        return true;
    }

    @Override
    public int limitParallel(GTRecipe recipe, IRecipeCapabilityHolder holder, int multiplier) {
        if (holder instanceof ICustomParallel p) return p.limitParallel(recipe, multiplier);

        int minMultiplier = 0;
        int maxMultiplier = multiplier;

        OverlayedItemHandler itemHandler = new OverlayedItemHandler(new CombinedInvWrapper(
                holder.getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).stream()
                        .filter(IItemHandlerModifiable.class::isInstance)
                        .map(IItemHandlerModifiable.class::cast)
                        .toArray(IItemHandlerModifiable[]::new)));

        Object2IntMap<ItemStack> recipeOutputs = GTHashMaps
                .fromItemStackCollection(recipe.getOutputContents(ItemRecipeCapability.CAP)
                        .stream()
                        .map(content -> ItemRecipeCapability.CAP.of(content.getContent()))
                        .filter(ingredient -> !ingredient.ingredient().hasNoItems())
                        .map(ingredient -> ingredient.getItems()[0])
                        .toList());

        while (minMultiplier != maxMultiplier) {
            itemHandler.reset();

            int returnedAmount = 0;
            int amountToInsert;

            for (Object2IntMap.Entry<ItemStack> entry : recipeOutputs.object2IntEntrySet()) {
                // Since multiplier starts at Int.MAX, check here for integer overflow
                if (entry.getIntValue() != 0 && multiplier > Integer.MAX_VALUE / entry.getIntValue()) {
                    amountToInsert = Integer.MAX_VALUE;
                } else {
                    amountToInsert = entry.getIntValue() * multiplier;
                }
                returnedAmount = itemHandler.insertStackedItemStack(entry.getKey(), amountToInsert);
                if (returnedAmount > 0) {
                    break;
                }
            }

            int[] bin = ParallelLogic.adjustMultiplier(returnedAmount == 0, minMultiplier, multiplier, maxMultiplier);
            minMultiplier = bin[0];
            multiplier = bin[1];
            maxMultiplier = bin[2];

        }
        return multiplier;
    }

    @Override
    public int getMaxParallelRatio(IRecipeCapabilityHolder holder, GTRecipe recipe, int parallelAmount) {
        // Find all the items in the combined Item Input inventories and create oversized ItemStacks
        Object2IntMap<ItemStack> ingredientStacks = getIngredientStacks(holder);

        int minMultiplier = Integer.MAX_VALUE;
        // map the recipe ingredients to account for duplicated and notConsumable ingredients.
        // notConsumable ingredients are not counted towards the max ratio
        Object2IntOpenHashMap<SizedIngredient> notConsumableMap = new Object2IntOpenHashMap<>();
        Object2IntOpenHashMap<SizedIngredient> countableMap = new Object2IntOpenHashMap<>();
        for (Content content : recipe.getInputContents(ItemRecipeCapability.CAP)) {
            SizedIngredient recipeIngredient = ItemRecipeCapability.CAP.of(content.content);
            int ingredientCount;
            if (recipeIngredient.ingredient()
                    .getCustomIngredient() instanceof IntProviderIngredient intProviderIngredient) {
                ingredientCount = intProviderIngredient.getSampledCount(GTValues.RNG);
            } else {
                ingredientCount = recipeIngredient.count();
            }
            if (content.chance == 0) {
                notConsumableMap.computeIfPresent(recipeIngredient, (k, v) -> v + ingredientCount);
                notConsumableMap.putIfAbsent(recipeIngredient, ingredientCount);
            } else {
                countableMap.computeIfPresent(recipeIngredient, (k, v) -> v + ingredientCount);
                countableMap.putIfAbsent(recipeIngredient, ingredientCount);
            }
        }

        // Iterate through the recipe inputs, excluding the not consumable ingredients from the inventory map
        for (Object2IntMap.Entry<SizedIngredient> recipeInputEntry : notConsumableMap.object2IntEntrySet()) {
            int needed = recipeInputEntry.getIntValue();
            int available = 0;
            // For every stack in the ingredients gathered from the input bus.
            for (Object2IntMap.Entry<ItemStack> inventoryEntry : ingredientStacks.object2IntEntrySet()) {
                if (recipeInputEntry.getKey().test(inventoryEntry.getKey())) {
                    available = inventoryEntry.getIntValue();
                    if (available > needed) {
                        inventoryEntry.setValue(available - needed);
                        needed -= available;
                        break;
                    } else {
                        inventoryEntry.setValue(0);
                        recipeInputEntry.setValue(needed - available);
                        needed -= available;
                    }
                }
            }
            // We need to check >= available here because of Non-Consumable inputs with stack size. If there is a NC
            // input
            // with size 2, and only 1 in the input, needed will be equal to available, but this situation should still
            // fail
            // as not all inputs are present
            if (needed >= available) {
                return 0;
            }
        }

        // Return the maximum parallel limit here if there are only non-consumed inputs, which are all found in the
        // input bus
        // At this point, we would have already returned 0 if we were missing any non-consumable inputs, so we can omit
        // that check
        if (countableMap.isEmpty() && !notConsumableMap.isEmpty()) {
            return parallelAmount;
        }

        // Iterate through the recipe inputs
        for (Object2IntMap.Entry<SizedIngredient> recipeInputEntry : countableMap.object2IntEntrySet()) {
            int needed = recipeInputEntry.getIntValue();
            int available = 0;
            // For every stack in the ingredients gathered from the input bus.
            for (Object2IntMap.Entry<ItemStack> inventoryEntry : ingredientStacks.object2IntEntrySet()) {
                if (recipeInputEntry.getKey().test(inventoryEntry.getKey())) {
                    available += inventoryEntry.getIntValue();
                    break;
                }
            }
            if (available >= needed) {
                int ratio = Math.min(parallelAmount, available / needed);
                if (ratio < minMultiplier) {
                    minMultiplier = ratio;
                }
            } else {
                return 0;
            }
        }
        return minMultiplier;
    }

    private Object2IntMap<ItemStack> getIngredientStacks(IRecipeCapabilityHolder holder) {
        Object2IntMap<ItemStack> map = new Object2IntOpenCustomHashMap<>(
                ItemStackHashStrategy.comparingAllButCount());
        Object2IntMap<ItemStack> result = new Object2IntOpenHashMap<>();

        var recipeHandlerList = holder.getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP);

        for (IRecipeHandler<?> container : recipeHandlerList) {

            var itemMap = container.getContents().stream().filter(ItemStack.class::isInstance)
                    .map(ItemStack.class::cast)
                    .flatMap(con -> GTHashMaps.fromItemStackCollection(Collections.singleton(con)).object2IntEntrySet()
                            .stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Integer::sum,
                            () -> new Object2IntOpenCustomHashMap<>(ItemStackHashStrategy.comparingAllButCount())));

            if (container.isDistinct()) {
                result.putAll(itemMap);
            } else {
                for (Object2IntMap.Entry<ItemStack> obj : itemMap.object2IntEntrySet()) {
                    map.computeInt(obj.getKey(), (k, v) -> v == null ? obj.getIntValue() : v + obj.getIntValue());
                }
            }
        }
        result.putAll(map);
        return result;
    }

    @Override
    public @NotNull List<Object> createXEIContainerContents(List<Content> contents, GTRecipe recipe, IO io) {
        var entryLists = contents.stream()
                .map(Content::getContent)
                .map(this::of)
                .map(ItemRecipeCapability::mapItem)
                .collect(Collectors.toList());

        List<ItemEntryList> scannerPossibilities = null;
        if (io == IO.OUT && recipe.recipeType.isScanner()) {
            scannerPossibilities = new ArrayList<>();
            // Scanner Output replacing, used for cycling research outputs
            ResearchManager.ResearchItem researchData = null;
            for (Content stack : recipe.getOutputContents(ItemRecipeCapability.CAP)) {
                researchData = ItemRecipeCapability.CAP.of(stack.content).getItems()[0]
                        .get(GTDataComponents.RESEARCH_ITEM);
                if (researchData != null) break;
            }
            if (researchData != null) {
                Collection<GTRecipe> possibleRecipes = researchData.recipeType()
                        .getDataStickEntry(researchData.researchId());
                Set<ItemStack> cache = new ObjectOpenCustomHashSet<>(ItemStackHashStrategy.comparingItem());
                if (possibleRecipes != null) {
                    for (GTRecipe r : possibleRecipes) {
                        Content outputContent = r.getOutputContents(ItemRecipeCapability.CAP).getFirst();
                        ItemStack researchStack = ItemRecipeCapability.CAP.of(outputContent.content).getItems()[0];
                        if (!cache.contains(researchStack)) {
                            cache.add(researchStack);
                            scannerPossibilities.add(ItemStackList.of(researchStack.copyWithCount(1)));
                        }
                    }
                }
                scannerPossibilities.add(entryLists.getFirst());
            }
        }

        if (scannerPossibilities != null && !scannerPossibilities.isEmpty()) {
            entryLists = scannerPossibilities;
        }
        while (entryLists.size() < recipe.recipeType.getMaxOutputs(ItemRecipeCapability.CAP)) entryLists.add(null);

        return new ArrayList<>(entryLists);
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
            final int amount = 1;
            var mapped = tryMapInner(intProvider.getInner(), amount);
            if (mapped != null) return mapped;
        } else if (ingredient.getContainedCustom() instanceof IntersectionIngredient intersection) {
            return mapIntersection(intersection, ingredient.count());
        } else {
            var tagList = tryMapTag(ingredient.ingredient(), 1);
            if (tagList != null) return tagList;
        }
        if (ingredient instanceof SizedIngredient sizedIngredient) {
            final int amount = sizedIngredient.hashCode();
            var mapped = tryMapInner(sizedIngredient.ingredient(), amount);
            if (mapped != null) return mapped;
        }
        ItemStackList stackList = new ItemStackList();
        boolean isIntProvider = ingredient.getContainedCustom() instanceof IntProviderIngredient;

        UnaryOperator<ItemStack> setCount = stack -> isIntProvider ? stack.copyWithCount(1) : stack;
        Arrays.stream(ingredient.getItems())
                .map(setCount)
                .forEach(stackList::add);
        return stackList;
    }

    private static @Nullable ItemEntryList tryMapInner(final Ingredient inner, int amount) {
        if (inner.getCustomIngredient() instanceof IntersectionIngredient intersection)
            return mapIntersection(intersection, amount);
        return tryMapTag(inner, amount);
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
         * @return Limited multiplier
         */
        int limitParallel(GTRecipe recipe, int multiplier);
    }
}
