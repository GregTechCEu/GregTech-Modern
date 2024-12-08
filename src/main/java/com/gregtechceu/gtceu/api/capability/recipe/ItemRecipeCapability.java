package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.ResearchData;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.*;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI;
import com.gregtechceu.gtceu.api.transfer.item.CycleItemStackHandler;
import com.gregtechceu.gtceu.api.transfer.item.TagOrCycleItemStackHandler;
import com.gregtechceu.gtceu.common.recipe.condition.ResearchCondition;
import com.gregtechceu.gtceu.common.valueprovider.AddedFloat;
import com.gregtechceu.gtceu.common.valueprovider.CastedFloat;
import com.gregtechceu.gtceu.common.valueprovider.FlooredInt;
import com.gregtechceu.gtceu.common.valueprovider.MultipliedFloat;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.IngredientAccessor;
import com.gregtechceu.gtceu.core.mixins.IntersectionIngredientAccessor;
import com.gregtechceu.gtceu.core.mixins.TagValueAccessor;
import com.gregtechceu.gtceu.integration.xei.widgets.GTRecipeWidget;
import com.gregtechceu.gtceu.utils.*;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.jei.IngredientIO;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IntersectionIngredient;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
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
public class ItemRecipeCapability extends RecipeCapability<Ingredient> {

    public final static ItemRecipeCapability CAP = new ItemRecipeCapability();

    protected ItemRecipeCapability() {
        super("item", 0xFFD96106, true, 0, SerializerIngredient.INSTANCE);
    }

    @Override
    public Ingredient copyInner(Ingredient content) {
        return SizedIngredient.copy(content);
    }

    @Override
    public Ingredient copyWithModifier(Ingredient content, ContentModifier modifier) {
        if (content instanceof SizedIngredient sizedIngredient) {
            return SizedIngredient.create(sizedIngredient.getInner(),
                    modifier.apply(sizedIngredient.getAmount()).intValue());
        } else if (content instanceof IntProviderIngredient intProviderIngredient) {
            return new IntProviderIngredient(intProviderIngredient.getInner(),
                    new FlooredInt(
                            new AddedFloat(
                                    new MultipliedFloat(
                                            new CastedFloat(intProviderIngredient.getCountProvider()),
                                            ConstantFloat.of((float) modifier.getMultiplier())),
                                    ConstantFloat.of((float) modifier.getAddition()))));
        }
        return SizedIngredient.create(content, modifier.apply(1).intValue());
    }

    @Override
    public List<AbstractMapIngredient> convertToMapIngredient(Object obj) {
        List<AbstractMapIngredient> ingredients = new ObjectArrayList<>(1);
        if (obj instanceof Ingredient ingredient) {

            // all kinds of special cases
            if (ingredient instanceof StrictNBTIngredient nbt) {
                ingredients.addAll(MapItemStackNBTIngredient.from(nbt));
            } else if (ingredient instanceof PartialNBTIngredient nbt) {
                ingredients.addAll(MapItemStackPartialNBTIngredient.from(nbt));
            } else if (ingredient instanceof SizedIngredient sized) {
                if (sized.getInner() instanceof StrictNBTIngredient nbt) {
                    ingredients.addAll(MapItemStackNBTIngredient.from(nbt));
                } else if (sized.getInner() instanceof PartialNBTIngredient nbt) {
                    ingredients.addAll(MapItemStackPartialNBTIngredient.from(nbt));
                } else if (sized.getInner() instanceof IntersectionIngredient intersection) {
                    ingredients.add(new MapIntersectionIngredient(intersection));
                } else {
                    for (Ingredient.Value value : ((IngredientAccessor) sized.getInner()).getValues()) {
                        if (value instanceof Ingredient.TagValue tagValue) {
                            ingredients.add(new MapItemTagIngredient(((TagValueAccessor) tagValue).getTag()));
                        } else {
                            Collection<ItemStack> stacks = value.getItems();
                            for (ItemStack stack : stacks) {
                                ingredients.add(new MapItemStackIngredient(stack, sized.getInner()));
                            }
                        }
                    }
                }
            } else if (ingredient instanceof IntProviderIngredient intProvider) {
                if (intProvider.getInner() instanceof StrictNBTIngredient nbt) {
                    ingredients.addAll(MapItemStackNBTIngredient.from(nbt));
                } else if (intProvider.getInner() instanceof PartialNBTIngredient nbt) {
                    ingredients.addAll(MapItemStackPartialNBTIngredient.from(nbt));
                } else if (intProvider.getInner() instanceof IntersectionIngredient intersection) {
                    ingredients.add(new MapIntersectionIngredient(intersection));
                } else {
                    for (Ingredient.Value value : ((IngredientAccessor) intProvider.getInner()).getValues()) {
                        if (value instanceof Ingredient.TagValue tagValue) {
                            ingredients.add(new MapItemTagIngredient(((TagValueAccessor) tagValue).getTag()));
                        } else {
                            Collection<ItemStack> stacks = value.getItems();
                            for (ItemStack stack : stacks) {
                                ingredients.add(new MapItemStackIngredient(stack, intProvider.getInner()));
                            }
                        }
                    }
                }
            } else if (ingredient instanceof IntersectionIngredient intersection) {
                ingredients.add(new MapIntersectionIngredient(intersection));
            } else {
                for (Ingredient.Value value : ((IngredientAccessor) ingredient).getValues()) {
                    if (value instanceof Ingredient.TagValue tagValue) {
                        ingredients.add(new MapItemTagIngredient(((TagValueAccessor) tagValue).getTag()));
                    } else {
                        Collection<ItemStack> stacks = value.getItems();
                        for (ItemStack stack : stacks) {
                            ingredients.add(new MapItemStackIngredient(stack, ingredient));
                        }
                    }
                }
            }
        } else if (obj instanceof ItemStack stack) {
            ingredients.add(new MapItemStackIngredient(stack));

            stack.getTags().forEach(tag -> ingredients.add(new MapItemTagIngredient(tag)));
            if (stack.hasTag()) {
                ingredients.add(new MapItemStackNBTIngredient(stack, StrictNBTIngredient.of(stack)));
            }
            if (stack.getShareTag() != null) {
                ingredients.add(new MapItemStackPartialNBTIngredient(stack,
                        PartialNBTIngredient.of(stack.getItem(), stack.getShareTag())));
            }
            TagPrefix prefix = ChemicalHelper.getPrefix(stack.getItem());
            if (prefix != null && TagPrefix.ORES.containsKey(prefix)) {
                Material material = ChemicalHelper.getMaterial(stack.getItem()).material();
                ingredients.add(new MapIntersectionIngredient((IntersectionIngredient) IntersectionIngredient.of(
                        Ingredient.of(prefix.getItemTags(material)[0]), Ingredient.of(prefix.getItemParentTags()[0]))));
            }
        }
        return ingredients;
    }

    @Override
    public List<Object> compressIngredients(Collection<Object> ingredients) {
        List<Object> list = new ObjectArrayList<>(ingredients.size());
        for (Object item : ingredients) {
            if (item instanceof Ingredient ingredient) {
                boolean isEqual = false;
                for (Object obj : list) {
                    if (obj instanceof Ingredient ingredient1) {
                        if (IngredientEquality.ingredientEquals(ingredient, ingredient1)) {
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
                //@formatter:off
                if (ingredient instanceof IntCircuitIngredient) {
                    list.add(0, ingredient);
                } else if (ingredient instanceof SizedIngredient sized &&
                        sized.getInner() instanceof IntCircuitIngredient) {
                    list.add(0, ingredient);
                } else if (ingredient instanceof IntProviderIngredient intProvider &&
                        intProvider.getInner() instanceof IntCircuitIngredient) {
                    list.add(0, ingredient);
                } else {
                    list.add(ingredient);
                }
                //@formatter:on
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
                Objects.requireNonNullElseGet(holder.getCapabilitiesProxy().get(IO.OUT, ItemRecipeCapability.CAP),
                        Collections::emptyList)
                        .stream()
                        .filter(IItemHandlerModifiable.class::isInstance)
                        .map(IItemHandlerModifiable.class::cast)
                        .toArray(IItemHandlerModifiable[]::new)));

        Object2IntMap<ItemStack> recipeOutputs = GTHashMaps
                .fromItemStackCollection(recipe.getOutputContents(ItemRecipeCapability.CAP)
                        .stream()
                        .map(content -> ItemRecipeCapability.CAP.of(content.getContent()))
                        .filter(ingredient -> !ingredient.isEmpty())
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
        Object2IntOpenHashMap<Ingredient> notConsumableMap = new Object2IntOpenHashMap<>();
        Object2IntOpenHashMap<Ingredient> countableMap = new Object2IntOpenHashMap<>();
        for (Content content : recipe.getInputContents(ItemRecipeCapability.CAP)) {
            Ingredient recipeIngredient = ItemRecipeCapability.CAP.of(content.content);
            int ingredientCount;
            if (recipeIngredient instanceof SizedIngredient sizedIngredient) {
                ingredientCount = sizedIngredient.getAmount();
            } else if (recipeIngredient instanceof IntProviderIngredient intProviderIngredient) {
                ingredientCount = intProviderIngredient.getSampledCount(GTValues.RNG);
            } else {
                ingredientCount = 1;
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

        List<IRecipeHandler<?>> recipeHandlerList = Objects
                .requireNonNullElseGet(holder.getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP),
                        Collections::<IRecipeHandler<?>>emptyList)
                .stream()
                .filter(handler -> !handler.isProxy()).toList();

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
                Collection<GTRecipe> possibleRecipes = researchData.getFirst()
                        .getDataStickEntry(researchData.getSecond());
                if (possibleRecipes != null) {
                    for (GTRecipe r : possibleRecipes) {
                        ItemStack researchItem = ItemRecipeCapability.CAP
                                .of(r.getOutputContents(ItemRecipeCapability.CAP).get(0).content).getItems()[0];
                        researchItem = researchItem.copy();
                        researchItem.setCount(1);
                        boolean didMatch = false;
                        for (Either<List<Pair<TagKey<Item>, Integer>>, List<ItemStack>> stacks : scannerPossibilities) {
                            for (ItemStack stack : stacks.map(
                                    tag -> tag
                                            .stream()
                                            .flatMap(key -> BuiltInRegistries.ITEM.getTag(key.getFirst()).stream())
                                            .flatMap(holders -> holders.stream()
                                                    .map(holder -> new ItemStack(holder.get())))
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
        // noinspection unchecked
        return new TagOrCycleItemStackHandler(
                (List<Either<List<Pair<TagKey<Item>, Integer>>, List<ItemStack>>>) contents);
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
                                ItemStack dataStick = entry.getDataItem().copy();
                                ResearchManager.writeResearchToNBT(dataStick.getOrCreateTag(), entry.getResearchId(),
                                        recipeType);
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
                float chance = (float) recipeType.getChanceFunction()
                        .getBoostedChance(content, recipeTier, chanceTier) / content.maxChance;
                slot.setXEIChance(chance);
                slot.setOnAddedTooltips((w, tooltips) -> {
                    GTRecipeWidget.setConsumedChance(content,
                            recipe.getChanceLogicForCapability(this, io, isTickSlot(index, io, recipe)),
                            tooltips, recipeTier, chanceTier, recipeType.getChanceFunction());
                    //@formatter:off
                    if (this.of(content.content) instanceof IntProviderIngredient ingredient) {
                        IntProvider countProvider = ingredient.getCountProvider();
                        tooltips.add(Component.translatable("gtceu.gui.content.count_range",
                                countProvider.getMinValue(), countProvider.getMaxValue())
                                .withStyle(ChatFormatting.GOLD));
                    } else if (this.of(content.content) instanceof SizedIngredient sizedIngredient &&
                            sizedIngredient.getInner() instanceof IntProviderIngredient ingredient) {
                        IntProvider countProvider = ingredient.getCountProvider();
                        tooltips.add(Component.translatable("gtceu.gui.content.count_range",
                                countProvider.getMinValue(), countProvider.getMaxValue())
                                .withStyle(ChatFormatting.GOLD));
                    }
                    //@formatter:on
                    if (isTickSlot(index, io, recipe)) {
                        tooltips.add(Component.translatable("gtceu.gui.content.per_tick"));
                    }
                });
                if (io == IO.IN && (content.chance == 0 || this.of(content.content) instanceof IntCircuitIngredient)) {
                    slot.setIngredientIO(IngredientIO.CATALYST);
                }
            }
        }
    }

    // Maps ingredients to Either <(Tag with count), ItemStack>s
    @SuppressWarnings("deprecation")
    private static Either<List<Pair<TagKey<Item>, Integer>>, List<ItemStack>> mapItem(final Ingredient ingredient) {
        if (ingredient instanceof SizedIngredient sizedIngredient) {
            final int amount = sizedIngredient.getAmount();
            if (sizedIngredient.getInner() instanceof IntersectionIngredient intersection) {
                List<Ingredient> children = ((IntersectionIngredientAccessor) intersection).getChildren();
                if (children.isEmpty()) {
                    return Either.right(null);
                }
                var childEither = mapItem(children.get(0));
                return Either.right(childEither.map(tags -> {
                    List<ItemStack> tagItems = tags.stream()
                            .map(pair -> Pair.of(BuiltInRegistries.ITEM.getTag(pair.getFirst()).stream(),
                                    pair.getSecond()))
                            .flatMap(pair -> pair.getFirst().flatMap(
                                    tag -> tag.stream().map(holder -> new ItemStack(holder.value(), pair.getSecond()))))
                            .collect(Collectors.toList());
                    ListIterator<ItemStack> iterator = tagItems.listIterator();
                    iteratorLoop:
                    while (iterator.hasNext()) {
                        var item = iterator.next();
                        for (int i = 1; i < children.size(); ++i) {
                            if (!children.get(i).test(item)) {
                                iterator.remove();
                                continue iteratorLoop;
                            }
                        }
                        iterator.set(item.copyWithCount(amount));
                    }
                    return tagItems;
                }, items -> {
                    items = new ArrayList<>(items);
                    ListIterator<ItemStack> iterator = items.listIterator();
                    iteratorLoop:
                    while (iterator.hasNext()) {
                        var item = iterator.next();
                        for (int i = 1; i < children.size(); ++i) {
                            if (!children.get(i).test(item)) {
                                iterator.remove();
                                continue iteratorLoop;
                            }
                        }
                        iterator.set(item.copyWithCount(amount));
                    }
                    return items;
                }));
            } else if (((IngredientAccessor) sizedIngredient.getInner()).getValues().length > 0 &&
                    ((IngredientAccessor) sizedIngredient.getInner())
                            .getValues()[0] instanceof Ingredient.TagValue tagValue) {
                                return Either.left(List.of(Pair.of(((TagValueAccessor) tagValue).getTag(), amount)));
                            }
        } else if (ingredient instanceof IntersectionIngredient intersection) {
            // Map intersection ingredients to the items inside, as recipe viewers don't support them.
            List<Ingredient> children = ((IntersectionIngredientAccessor) intersection).getChildren();
            if (children.isEmpty()) {
                return Either.right(null);
            }
            var childEither = mapItem(children.get(0));
            return Either.right(childEither.map(tags -> {
                List<ItemStack> tagItems = tags.stream()
                        .map(pair -> Pair.of(BuiltInRegistries.ITEM.getTag(pair.getFirst()).stream(), pair.getSecond()))
                        .flatMap(pair -> pair.getFirst().flatMap(
                                tag -> tag.stream().map(holder -> new ItemStack(holder.value(), pair.getSecond()))))
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
        } else if (ingredient instanceof IntProviderIngredient intProvider) {
            final int amount = 1;
            if (intProvider.getInner() instanceof IntersectionIngredient intersection) {
                List<Ingredient> children = ((IntersectionIngredientAccessor) intersection).getChildren();
                if (children.isEmpty()) {
                    return Either.right(null);
                }
                var childEither = mapItem(children.get(0));
                return Either.right(childEither.map(tags -> {
                    List<ItemStack> tagItems = tags.stream()
                            .map(pair -> Pair.of(BuiltInRegistries.ITEM.getTag(pair.getFirst()).stream(),
                                    pair.getSecond()))
                            .flatMap(pair -> pair.getFirst().flatMap(
                                    tag -> tag.stream().map(holder -> new ItemStack(holder.value(), pair.getSecond()))))
                            .collect(Collectors.toList());
                    ListIterator<ItemStack> iterator = tagItems.listIterator();
                    iteratorLoop:
                    while (iterator.hasNext()) {
                        var item = iterator.next();
                        for (int i = 1; i < children.size(); ++i) {
                            if (!children.get(i).test(item)) {
                                iterator.remove();
                                continue iteratorLoop;
                            }
                        }
                        iterator.set(item.copyWithCount(amount));
                    }
                    return tagItems;
                }, items -> {
                    items = new ArrayList<>(items);
                    ListIterator<ItemStack> iterator = items.listIterator();
                    iteratorLoop:
                    while (iterator.hasNext()) {
                        var item = iterator.next();
                        for (int i = 1; i < children.size(); ++i) {
                            if (!children.get(i).test(item)) {
                                iterator.remove();
                                continue iteratorLoop;
                            }
                        }
                        iterator.set(item.copyWithCount(amount));
                    }
                    return items;
                }));
            } else if (((IngredientAccessor) intProvider.getInner()).getValues().length > 0 &&
                    ((IngredientAccessor) intProvider.getInner())
                            .getValues()[0] instanceof Ingredient.TagValue tagValue) {
                                return Either.left(List.of(Pair.of(((TagValueAccessor) tagValue).getTag(), amount)));
                            }

        } else if (((IngredientAccessor) ingredient).getValues().length > 0 &&
                ((IngredientAccessor) ingredient).getValues()[0] instanceof Ingredient.TagValue tagValue) {
                    return Either.left(List.of(Pair.of(((TagValueAccessor) tagValue).getTag(), 1)));
                }
        return Either.right(Arrays.stream(ingredient.getItems()).map(stack -> {
            //@formatter:off
            if (ingredient instanceof IntProviderIngredient) {
                stack.setCount(1);
            } else if (ingredient instanceof SizedIngredient sized &&
                    sized.getInner() instanceof IntProviderIngredient) {
                stack.setCount(1);
            }
            //@formatter:on
            return stack;
        }).toList());
    }

    @Override
    public Object2IntMap<Ingredient> makeChanceCache() {
        return new Object2IntOpenCustomHashMap<>(IngredientEquality.IngredientHashStrategy.INSTANCE);
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
