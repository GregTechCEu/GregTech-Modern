package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.ResearchData;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.*;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI;
import com.gregtechceu.gtceu.common.recipe.ResearchCondition;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.GTRecipeWidget;
import com.gregtechceu.gtceu.utils.IngredientEquality;
import com.gregtechceu.gtceu.utils.ResearchManager;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.utils.CycleItemStackHandler;
import com.lowdragmc.lowdraglib.utils.TagOrCycleItemStackTransfer;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
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
        if (obj instanceof Ingredient ingredient) {

            // all kinds of special cases
            if (ingredient.getCustomIngredient() instanceof DataComponentIngredient component && component.isStrict()) {
                ingredients.addAll(MapItemStackDataComponentIngredient.from(component.toVanilla()));
            } else if (ingredient.getCustomIngredient() instanceof DataComponentIngredient component && !component.isStrict()) {
                ingredients.addAll(MapItemStackDataComponentIngredient.from(component.toVanilla()));
            } else if (ingredient.getCustomIngredient() instanceof IntCircuitIngredient circuit) {
                ingredients.addAll(MapItemStackDataComponentIngredient.from(circuit));
            }  else if (ingredient.getCustomIngredient() instanceof IntersectionIngredient intersection) {
                ingredients.add(new MapIntersectionIngredient(intersection));
            } else if (ingredient.getCustomIngredient() instanceof CompoundIngredient compound) {
                for (Ingredient inner : compound.children()) {
                    ingredients.addAll(convertToMapIngredient(inner));
                }
            } else {
                for (Ingredient.Value value : ingredient.getValues()) {
                    if (value instanceof Ingredient.TagValue tagValue) {
                        ingredients.add(new MapItemTagIngredient(tagValue.tag()));
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
            ingredients.add(new MapItemStackDataComponentIngredient(stack, DataComponentIngredient.of(true, stack)));
            ingredients.add(new MapItemStackPartialDataComponentIngredient(stack, DataComponentIngredient.of(false, stack.getComponents(), stack.getItem())));
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
                if (ingredient.getCustomIngredient() instanceof IntCircuitIngredient) {
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
        } else if (ingredient.getValues().length > 0 && ingredient.getValues()[0] instanceof Ingredient.TagValue tagValue) {
            return Either.left(List.of(Pair.of(tagValue.tag(), 1)));
        }
        return Either.right(Arrays.stream(ingredient.getItems()).toList());
    }
}
