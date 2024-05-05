package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.material.ChemicalHelper;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.ResearchData;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.*;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI;
import com.gregtechceu.gtceu.common.recipe.ResearchCondition;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.GTRecipeWidget;
import com.gregtechceu.gtceu.utils.*;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.misc.ItemTransferList;
import com.lowdragmc.lowdraglib.utils.CycleItemStackHandler;
import com.lowdragmc.lowdraglib.utils.TagOrCycleItemStackTransfer;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.IntersectionIngredient;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote ItemRecipeCapability
 */
public class ItemRecipeCapability extends RecipeCapability<SizedIngredient> {

    public final static ItemRecipeCapability CAP = new ItemRecipeCapability();

    protected ItemRecipeCapability() {
        super("item", 0xFFD96106, true, 0, SerializerIngredient.INSTANCE);
    }

    @Override
    public SizedIngredient copyInner(SizedIngredient content) {
        if (content instanceof SizedIngredient sizedIngredient) {
            return new SizedIngredient(sizedIngredient.ingredient(), sizedIngredient.count());
        } else if (content.ingredient().getCustomIngredient() instanceof IntCircuitIngredient circuit) {
            return new SizedIngredient(circuit.copy().toVanilla(), 1);
        }
        return content;
    }

    @Override
    public SizedIngredient copyWithModifier(SizedIngredient content, ContentModifier modifier) {
        return content instanceof SizedIngredient sizedIngredient ? new SizedIngredient(sizedIngredient.ingredient(), modifier.apply(sizedIngredient.count()).intValue()) : new SizedIngredient(content.ingredient(), modifier.apply(1).intValue());
    }

    @Override
    public List<AbstractMapIngredient> convertToMapIngredient(Object obj) {
        List<AbstractMapIngredient> ingredients = new ObjectArrayList<>(1);
        if (obj instanceof SizedIngredient ingredient) {

            // all kinds of special cases
            switch (ingredient.ingredient().getCustomIngredient()) {
                case
                    DataComponentIngredient component when component.isStrict() ->
                    ingredients.addAll(MapItemStackDataComponentIngredient.from(component.toVanilla()));
                case
                    DataComponentIngredient component when !component.isStrict() ->
                    ingredients.addAll(MapItemStackWeakDataComponentIngredient.from(component.toVanilla()));
                case
                    IntCircuitIngredient circuit ->
                    ingredients.addAll(MapItemStackDataComponentIngredient.from(circuit));
                case
                    IntersectionIngredient intersection ->
                    ingredients.add(new MapIntersectionIngredient(intersection));
                case
                    CompoundIngredient compound -> {
                    for (Ingredient inner : compound.children()) {
                        ingredients.addAll(convertToMapIngredient(inner));
                    }
                }
                case
                    null,
                    default -> {
                    for (Ingredient.Value value : ingredient.ingredient().getValues()) {
                        if (value instanceof Ingredient.TagValue tagValue) {
                            ingredients.add(new MapItemTagIngredient(tagValue.tag()));
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
            if (!stack.getComponents().equals(stack.getPrototype())) {
                ingredients.add(new MapItemStackDataComponentIngredient(stack, DataComponentIngredient.of(true, stack)));
                ingredients.add(new MapItemStackWeakDataComponentIngredient(stack, DataComponentIngredient.of(false, stack.getComponents(), stack.getItem())));
            }
            TagPrefix prefix = ChemicalHelper.getPrefix(stack.getItem());
            if (prefix != null && TagPrefix.ORES.containsKey(prefix)) {
                Material material = ChemicalHelper.getMaterial(stack.getItem()).material();
                ingredients.add(new MapIntersectionIngredient((IntersectionIngredient) IntersectionIngredient.of(Ingredient.of(prefix.getItemTags(material)[0]), Ingredient.of(prefix.getItemParentTags()[0])).getCustomIngredient()));
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
                if (ingredient.ingredient().getCustomIngredient() instanceof IntCircuitIngredient) {
                    list.addFirst(ingredient);
                } else {
                    list.add(ingredient);
                }
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
        int minMultiplier = 0;
        int maxMultiplier = multiplier;

        OverlayedItemHandler itemHandler = new OverlayedItemHandler(new ItemTransferList(
            Objects.requireNonNullElseGet(holder.getCapabilitiesProxy().get(IO.OUT, ItemRecipeCapability.CAP), Collections::emptyList)
                .stream()
                .filter(IItemHandlerModifiable.class::isInstance)
                .map(IItemHandlerModifiable.class::cast)
                .toList()
        ));

        Object2IntMap<ItemStack> recipeOutputs = GTHashMaps.fromItemStackCollection(recipe.getOutputContents(ItemRecipeCapability.CAP)
            .stream()
            .map(ItemRecipeCapability.CAP::of)
            .filter(ingredient -> !ingredient.ingredient().isEmpty())
            .map(ingredient -> ingredient.getItems()[0])
            .toList()
        );

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
        Object2IntMap<ItemStack> ingredientStacks = Objects.requireNonNullElseGet(holder.getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP), Collections::<IRecipeHandler<?>>emptyList)
            .stream()
            .map(container -> container.getContents().stream().filter(ItemStack.class::isInstance).map(ItemStack.class::cast).toList())
            .flatMap(container -> GTHashMaps.fromItemStackCollection(container).object2IntEntrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Integer::sum, () -> new Object2IntOpenCustomHashMap<>(ItemStackHashStrategy.comparingAllButCount())));

        int minMultiplier = Integer.MAX_VALUE;
        // map the recipe ingredients to account for duplicated and notConsumable ingredients.
        // notConsumable ingredients are not counted towards the max ratio
        Object2IntOpenHashMap<Ingredient> notConsumableMap = new Object2IntOpenHashMap<>();
        Object2IntOpenHashMap<Ingredient> countableMap = new Object2IntOpenHashMap<>();
        for (Content content : recipe.getInputContents(ItemRecipeCapability.CAP)) {
            SizedIngredient recipeIngredient = ItemRecipeCapability.CAP.of(content.content);
            int ingredientCount = recipeIngredient.count();
            if (content.chance == 0.0f) {
                notConsumableMap.computeIfPresent(recipeIngredient.ingredient(), (k, v) -> v + ingredientCount);
                notConsumableMap.putIfAbsent(recipeIngredient.ingredient(), ingredientCount);
            } else {
                countableMap.computeIfPresent(recipeIngredient.ingredient(), (k, v) -> v + ingredientCount);
                countableMap.putIfAbsent(recipeIngredient.ingredient(), ingredientCount);
            }
        }

        // Iterate through the recipe inputs, excluding the not consumable ingredients from the inventory map
        for (Object2IntMap.Entry<Ingredient> recipeInputEntry : notConsumableMap.object2IntEntrySet()) {
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
        for (Object2IntMap.Entry<Ingredient> recipeInputEntry : countableMap.object2IntEntrySet()) {
            int needed = recipeInputEntry.getIntValue();
            int available = 0;
            // For every stack in the ingredients gathered from the input bus.
            for (Object2IntMap.Entry<ItemStack> inventoryEntry : ingredientStacks.object2IntEntrySet()) {
                if (recipeInputEntry.getKey().test(inventoryEntry.getKey())) {
                    available += inventoryEntry.getIntValue();
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

    @Override
    public @NotNull List<Object> createXEIContainerContents(List<Content> contents, GTRecipe recipe, IO io) {
        var outputStacks = contents.stream().map(content -> content.content)
            .map(this::of)
            .map(ItemRecipeCapability::mapItem)
            .collect(Collectors.toList());

        List<Either<List<Pair<TagKey<Item>, Integer>>, List<ItemStack>>> scannerPossibilities = null;
        if (io == IO.OUT && recipe.recipeType.isScanner()) {
            scannerPossibilities = new ArrayList<>();
            // Scanner Output replacing, used for cycling research outputs
            Pair<GTRecipeType, String> researchData = null;
            for (Content stack : recipe.getOutputContents(ItemRecipeCapability.CAP)) {
                researchData = ResearchManager.readResearchId(ItemRecipeCapability.CAP.of(stack.content).getItems()[0]);
                if (researchData != null) break;
            }
            if (researchData != null) {
                Collection<GTRecipe> possibleRecipes = researchData.getFirst().getDataStickEntry(researchData.getSecond());
                if (possibleRecipes != null) {
                    for (GTRecipe r : possibleRecipes) {
                        ItemStack researchItem = ItemRecipeCapability.CAP.of(r.getOutputContents(ItemRecipeCapability.CAP).get(0).content).getItems()[0];
                        researchItem = researchItem.copy();
                        researchItem.setCount(1);
                        boolean didMatch = false;
                        for (Either<List<Pair<TagKey<Item>, Integer>>, List<ItemStack>> stacks : scannerPossibilities) {
                            for (ItemStack stack : stacks.map(
                                tag -> tag
                                    .stream()
                                    .flatMap(key -> BuiltInRegistries.ITEM.getTag(key.getFirst()).stream())
                                    .flatMap(holders -> holders.stream().map(holder -> new ItemStack(holder.value())))
                                    .collect(Collectors.toList()),
                                Function.identity())) {
                                if (ItemStack.isSameItem(stack, researchItem)) {
                                    didMatch = true;
                                    break;
                                }
                            }
                        }
                        if (!didMatch) scannerPossibilities.add(Either.right(List.of(researchItem)));
                    }
                }
                scannerPossibilities.add(outputStacks.get(0));
            }
        }

        if (scannerPossibilities != null && !scannerPossibilities.isEmpty()) {
            outputStacks = scannerPossibilities;
        }
        while (outputStacks.size() < recipe.recipeType.getMaxOutputs(ItemRecipeCapability.CAP)) outputStacks.add(null);

        return new ArrayList<>(outputStacks);
    }

    public Object createXEIContainer(List<?> contents) {
        // cast is safe if you don't pass the wrong thing.
        //noinspection unchecked
        return new TagOrCycleItemStackTransfer((List<Either<List<Pair<TagKey<Item>, Integer>>, List<ItemStack>>>) contents);
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
                                @Nullable Object storage) {
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
                        ResearchCondition condition = recipeHolder.conditions().stream().filter(ResearchCondition.class::isInstance).findAny().map(ResearchCondition.class::cast).orElse(null);
                        if (condition != null) {
                            List<ItemStack> dataItems = new ArrayList<>();
                            for (ResearchData.ResearchEntry entry : condition.data) {
                                ItemStack dataStick = entry.getDataItem().copy();
                                ResearchManager.writeResearchToComponent(dataStick, entry.getResearchId(), recipeType);
                                dataItems.add(dataStick);
                            }
                            CycleItemStackHandler handler = new CycleItemStackHandler(List.of(dataItems));
                            slot.setHandlerSlot(handler, 0);
                            slot.setIngredientIO(IngredientIO.INPUT);
                            slot.setCanTakeItems(false);
                            slot.setCanPutItems(false);
                        }
                    }
                }
            }
            if (content != null) {
                slot.setXEIChance(content.chance);
                slot.setOnAddedTooltips((w, tooltips) -> {
                    GTRecipeWidget.setConsumedChance(content, tooltips);
                    if (index >= recipe.getOutputContents(this).size()) {
                        tooltips.add(Component.translatable("gtceu.gui.content.per_tick"));
                    }
                });
            }
        }
    }

    // Maps ingredients to Either <(Tag with count), ItemStack>s
    private static Either<List<Pair<TagKey<Item>, Integer>>, List<ItemStack>> mapItem(SizedIngredient ingredient) {
        if (ingredient instanceof SizedIngredient sizedIngredient) {
            final int amount = sizedIngredient.count();
            if (sizedIngredient.ingredient().getCustomIngredient() instanceof IntersectionIngredient intersection) {
                List<Ingredient> children = intersection.children();
                if (children.isEmpty()) {
                    return Either.right(null);
                }
                var childEither = mapItem(children.get(0));
                return Either.right(childEither.map(tags -> {
                    List<ItemStack> tagItems = tags.stream()
                        .map(pair -> Pair.of(BuiltInRegistries.ITEM.getTag(pair.getFirst()).stream(), pair.getSecond()))
                        .flatMap(pair -> pair.getFirst().flatMap(tag -> tag.stream().map(holder -> new ItemStack(holder.value(), pair.getSecond()))))
                        .collect(Collectors.toList());
                    ListIterator<ItemStack> iterator = tagItems.listIterator();
                    while (iterator.hasNext()) {
                        var item = iterator.next();
                        for (int i = 1; i < children.size(); ++i) {
                            if (!children.get(i).test(item)) {
                                iterator.remove();
                                break;
                            }
                        }
                        iterator.set(item.copyWithCount(amount));
                    }
                    return tagItems;
                }, items -> {
                    items = new ArrayList<>(items);
                    ListIterator<ItemStack> iterator = items.listIterator();
                    while (iterator.hasNext()) {
                        var item = iterator.next();
                        for (int i = 1; i < children.size(); ++i) {
                            if (!children.get(i).test(item)) {
                                iterator.remove();
                                break;
                            }
                        }
                        iterator.set(item.copyWithCount(amount));
                    }
                    return items;
                }));
            } else if (sizedIngredient.ingredient().getCustomIngredient() != null) {
                return Either.right(sizedIngredient.ingredient().getCustomIngredient().getItems().toList());
            } else if (sizedIngredient.ingredient().getValues().length > 0 && sizedIngredient.ingredient().getValues()[0] instanceof Ingredient.TagValue tagValue) {
                return Either.left(List.of(Pair.of(tagValue.tag(), amount)));
            }
        }
        return Either.right(Arrays.stream(ingredient.getItems()).toList());
    }

    public static Either<List<Pair<TagKey<Item>, Integer>>, List<ItemStack>> mapItem(Ingredient ingredient) {
         if (ingredient.getCustomIngredient() instanceof IntersectionIngredient intersection) {
            // Map intersection ingredients to the items inside, as recipe viewers don't support them.
            List<Ingredient> children = intersection.children();
            if (children.isEmpty()) {
                return Either.right(null);
            }
            var childEither = mapItem(children.get(0));
            return Either.right(childEither.map(tags -> {
                List<ItemStack> tagItems = tags.stream()
                    .map(pair -> Pair.of(BuiltInRegistries.ITEM.getTag(pair.getFirst()).stream(), pair.getSecond()))
                    .flatMap(pair -> pair.getFirst().flatMap(tag -> tag.stream().map(holder -> new ItemStack(holder.value(), pair.getSecond()))))
                    .collect(Collectors.toList());
                ListIterator<ItemStack> iterator = tagItems.listIterator();
                while (iterator.hasNext()) {
                    var item = iterator.next();
                    for (int i = 1; i < children.size(); ++i) {
                        if (!children.get(i).test(item)) {
                            iterator.remove();
                            break;
                        }
                    }
                }
                return tagItems;
            }, items -> {
                items = new ArrayList<>(items);
                ListIterator<ItemStack> iterator = items.listIterator();
                while (iterator.hasNext()) {
                    var item = iterator.next();
                    for (int i = 1; i < children.size(); ++i) {
                        if (!children.get(i).test(item)) {
                            iterator.remove();
                            break;
                        }
                    }
                }
                return items;
            }));
        } else if (ingredient.getCustomIngredient() != null) {
             return Either.right(ingredient.getCustomIngredient().getItems().toList());
         } else if (ingredient.getValues().length > 0 && ingredient.getValues()[0] instanceof Ingredient.TagValue tagValue) {
            return Either.left(List.of(Pair.of(tagValue.tag(), 1)));
        }
        return Either.right(Arrays.stream(ingredient.getItems()).toList());
    }
}
