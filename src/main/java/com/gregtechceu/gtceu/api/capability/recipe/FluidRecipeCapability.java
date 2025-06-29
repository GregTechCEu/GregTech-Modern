package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.MapFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.MapFluidTagIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.client.TooltipsHandler;
import com.gregtechceu.gtceu.integration.xei.entry.fluid.FluidEntryList;
import com.gregtechceu.gtceu.integration.xei.entry.fluid.FluidStackList;
import com.gregtechceu.gtceu.integration.xei.entry.fluid.FluidTagList;
import com.gregtechceu.gtceu.integration.xei.handlers.fluid.CycleFluidEntryHandler;
import com.gregtechceu.gtceu.integration.xei.widgets.GTRecipeWidget;
import com.gregtechceu.gtceu.utils.OverlayingFluidStorage;

import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.jei.IngredientIO;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;
import java.util.stream.Collectors;

public class FluidRecipeCapability extends RecipeCapability<FluidIngredient> {

    public final static FluidRecipeCapability CAP = new FluidRecipeCapability();

    protected FluidRecipeCapability() {
        super("fluid", 0xFF3C70EE, true, 1, SerializerFluidIngredient.INSTANCE);
    }

    @Override
    public FluidIngredient copyInner(FluidIngredient content) {
        return content.copy();
    }

    @Override
    public FluidIngredient copyWithModifier(FluidIngredient content, ContentModifier modifier) {
        if (content.isEmpty()) return content.copy();
        FluidIngredient copy = content.copy();
        copy.setAmount(modifier.apply(copy.getAmount()));
        return copy;
    }

    @Override
    public List<AbstractMapIngredient> convertToMapIngredient(Object obj) {
        List<AbstractMapIngredient> ingredients = new ObjectArrayList<>(1);
        if (obj instanceof FluidIngredient ingredient) {
            for (FluidIngredient.Value value : ingredient.values) {
                if (value instanceof FluidIngredient.TagValue tagValue) {
                    ingredients.add(new MapFluidTagIngredient(tagValue.getTag()));
                } else {
                    Collection<Fluid> fluids = value.getFluids();
                    for (Fluid fluid : fluids) {
                        ingredients.add(new MapFluidIngredient(
                                new FluidStack(fluid, ingredient.getAmount(), ingredient.getNbt())));
                    }
                }
            }
        } else if (obj instanceof FluidStack stack) {
            ingredients.add(new MapFluidIngredient(stack));
            // noinspection deprecation
            stack.getFluid().builtInRegistryHolder().tags()
                    .forEach(tag -> ingredients.add(new MapFluidTagIngredient(tag)));
        }

        return ingredients;
    }

    @Override
    public List<Object> compressIngredients(Collection<Object> ingredients) {
        List<Object> list = new ObjectArrayList<>(ingredients.size());
        for (Object item : ingredients) {
            if (item instanceof FluidIngredient fluid) {
                boolean isEqual = false;
                for (Object obj : list) {
                    if (obj instanceof FluidIngredient fluidIngredient) {
                        if (fluid.equals(fluidIngredient)) {
                            isEqual = true;
                            break;
                        }
                    } else if (obj instanceof FluidStack fluidStack) {
                        if (fluid.test(fluidStack)) {
                            isEqual = true;
                            break;
                        }
                    }
                }
                if (isEqual) continue;
                list.add(fluid);
            } else if (item instanceof FluidStack fluidStack) {
                boolean isEqual = false;
                for (Object obj : list) {
                    if (obj instanceof FluidIngredient fluidIngredient) {
                        if (fluidIngredient.test(fluidStack)) {
                            isEqual = true;
                            break;
                        }
                    } else if (obj instanceof FluidStack stack) {
                        if (fluidStack.isFluidEqual(stack)) {
                            isEqual = true;
                            break;
                        }
                    }
                }
                if (isEqual) continue;
                list.add(fluidStack);
            }
        }
        return list;
    }

    @Override
    public boolean isRecipeSearchFilter() {
        return true;
    }

    @Override
    public int limitMaxParallelByOutput(IRecipeCapabilityHolder holder, GTRecipe recipe, int multiplier, boolean tick) {
        if (holder instanceof ICustomParallel p) return p.limitFluidParallel(recipe, multiplier, tick);
        var outputContents = (tick ? recipe.tickOutputs : recipe.outputs).get(this);
        if (outputContents == null || outputContents.isEmpty()) return multiplier;

        if (!holder.hasCapabilityProxies()) return 0;

        var handlers = holder.getCapabilitiesFlat(IO.OUT, this);
        if (handlers.isEmpty()) return 0;

        int minMultiplier = 0;
        int maxMultiplier = multiplier;

        int maxAmount = 0;
        List<FluidIngredient> ingredients = new ArrayList<>(outputContents.size());
        for (var content : outputContents) {
            var ing = this.of(content.content);
            maxAmount = Math.max(maxAmount, ing.getAmount());
            ingredients.add(ing);
        }
        if (maxAmount == 0) return multiplier;
        if (multiplier > Integer.MAX_VALUE / maxAmount) {
            maxMultiplier = multiplier = Integer.MAX_VALUE / maxAmount;
        }

        while (minMultiplier != maxMultiplier) {
            List<FluidIngredient> copied = new ArrayList<>();
            for (final var ing : ingredients) {
                copied.add(this.copyWithModifier(ing, ContentModifier.multiplier(multiplier)));
            }

            for (var handler : handlers) {
                // noinspection unchecked
                copied = (List<FluidIngredient>) handler.handleRecipe(IO.OUT, recipe, copied, true);
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

        List<Object2IntMap<FluidStack>> inventoryGroups = getInputContents(holder);
        if (inventoryGroups.isEmpty()) return 0;

        var nonConsumables = new Object2IntOpenHashMap<FluidIngredient>();
        var consumables = new Object2IntOpenHashMap<FluidIngredient>();
        for (Content content : inputs) {
            FluidIngredient ingredient = of(content.content);
            if (content.chance == 0) nonConsumables.addTo(ingredient, ingredient.getAmount());
            else consumables.addTo(ingredient, ingredient.getAmount());
        }

        if (consumables.isEmpty() && nonConsumables.isEmpty()) return limit;

        int maxMultiplier = 0;
        for (var group : inventoryGroups) {
            boolean satisfied = true;
            for (var ncEntry : Object2IntMaps.fastIterable(nonConsumables)) {
                FluidIngredient ingredient = ncEntry.getKey();
                int needed = ncEntry.getIntValue();
                for (var stackEntry : Object2IntMaps.fastIterable(group)) {
                    if (ingredient.test(stackEntry.getKey())) {
                        int count = stackEntry.getIntValue();
                        int lesser = Math.min(needed, count);
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
            for (var inputEntry : Object2IntMaps.fastIterable(consumables)) {
                FluidIngredient ingredient = inputEntry.getKey();
                final int needed = inputEntry.getIntValue();
                final int maxNeeded = needed * limit;
                int available = 0;
                // Search stacks in our inventory group, summing them up
                for (var stackEntry : Object2IntMaps.fastIterable(group)) {
                    if (ingredient.test(stackEntry.getKey())) {
                        available += stackEntry.getIntValue();
                        // We can stop if we already have enough for max parallel
                        if (available >= maxNeeded) break;
                    }
                }
                // ratio will equal 0 if available < needed
                int ratio = Math.min(limit, available / needed);
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

    private static List<Object2IntMap<FluidStack>> getInputContents(IRecipeCapabilityHolder holder) {
        var handlerLists = holder.getCapabilitiesForIO(IO.IN);
        if (handlerLists.isEmpty()) return Collections.emptyList();
        List<RecipeHandlerList> distinct = new ArrayList<>();
        List<IRecipeHandler<?>> indistinct = new ArrayList<>();

        for (var handlerList : handlerLists) {
            if (handlerList.isDistinct() && handlerList.hasCapability(FluidRecipeCapability.CAP)) {
                distinct.add(handlerList);
            } else if (handlerList.hasCapability(FluidRecipeCapability.CAP)) {
                indistinct.addAll(handlerList.getCapability(FluidRecipeCapability.CAP));
            }
        }

        List<Object2IntMap<FluidStack>> invs = new ArrayList<>(distinct.size() + 1);
        Object2IntOpenHashMap<FluidStack> combined = new Object2IntOpenHashMap<>();
        for (var handler : indistinct) {
            if (!handler.shouldSearchContent()) continue;
            for (var content : handler.getContents()) {
                if (content instanceof FluidStack stack && !stack.isEmpty()) {
                    combined.addTo(stack, stack.getAmount());
                }
            }
        }

        for (var handlerList : distinct) {
            var handlers = handlerList.getCapability(FluidRecipeCapability.CAP);
            // Clone has the desired effect here - it will shallow copy the keys, which we don't change and deep copy
            // the values, as they are primitives.
            var inventory = combined.clone();
            for (var handler : handlers) {
                if (!handler.shouldSearchContent()) continue;
                for (var content : handler.getContents()) {
                    if (content instanceof FluidStack stack && !stack.isEmpty()) {
                        inventory.addTo(stack, stack.getAmount());
                    }
                }
            }
            if (!inventory.isEmpty()) invs.add(inventory);
        }

        if (!combined.isEmpty()) invs.add(combined);
        return invs;
    }

    @Override
    public @NotNull List<Object> createXEIContainerContents(List<Content> contents, GTRecipe recipe, IO io) {
        return contents.stream().map(content -> content.content)
                .map(this::of)
                .map(FluidRecipeCapability::mapFluid)
                .collect(Collectors.toList());
    }

    public Object createXEIContainer(List<?> contents) {
        // cast is safe if you don't pass the wrong thing.
        // noinspection unchecked
        return new CycleFluidEntryHandler((List<FluidEntryList>) contents);
    }

    @NotNull
    @Override
    public Widget createWidget() {
        TankWidget tank = new TankWidget();
        tank.initTemplate();
        tank.setFillDirection(ProgressTexture.FillDirection.ALWAYS_FULL);
        return tank;
    }

    @NotNull
    @Override
    public Class<? extends Widget> getWidgetClass() {
        return TankWidget.class;
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
        if (widget instanceof TankWidget tank) {
            if (storage instanceof CycleFluidEntryHandler cycleHandler) {
                tank.setFluidTank(cycleHandler, index);
            } else if (storage instanceof IFluidHandlerModifiable fluidHandler) {
                tank.setFluidTank(new OverlayingFluidStorage(fluidHandler, index));
            }
            tank.setIngredientIO(io == IO.IN ? IngredientIO.INPUT : IngredientIO.OUTPUT);
            tank.setAllowClickFilled(!isXEI);
            tank.setAllowClickDrained(!isXEI && io.support(IO.IN));
            if (isXEI) tank.setShowAmount(false);
            if (content != null) {
                float chance = (float) recipeType.getChanceFunction()
                        .getBoostedChance(content, recipeTier, chanceTier) / content.maxChance;
                tank.setXEIChance(chance);
                tank.setOnAddedTooltips((w, tooltips) -> {
                    FluidIngredient ingredient = FluidRecipeCapability.CAP.of(content.content);
                    if (!isXEI && ingredient.getStacks().length > 0) {
                        FluidStack stack = ingredient.getStacks()[0];
                        TooltipsHandler.appendFluidTooltips(stack, tooltips::add, TooltipFlag.NORMAL);
                    }

                    GTRecipeWidget.setConsumedChance(content,
                            recipe.getChanceLogicForCapability(this, io, isTickSlot(index, io, recipe)),
                            tooltips, recipeTier, chanceTier, recipeType.getChanceFunction());
                    if (isTickSlot(index, io, recipe)) {
                        tooltips.add(Component.translatable("gtceu.gui.content.per_tick"));
                    }
                });
                if (io == IO.IN && (content.chance == 0)) {
                    tank.setIngredientIO(IngredientIO.CATALYST);
                }
            }
        }
    }

    // Maps fluids to a FluidEntryList for XEI: either a FluidTagList or a FluidStackList
    public static FluidEntryList mapFluid(FluidIngredient ingredient) {
        int amount = ingredient.getAmount();
        CompoundTag tag = ingredient.getNbt();

        FluidTagList tags = new FluidTagList();
        FluidStackList fluids = new FluidStackList();
        for (FluidIngredient.Value value : ingredient.values) {
            if (value instanceof FluidIngredient.TagValue tagValue) {
                tags.add(tagValue.getTag(), amount, ingredient.getNbt());
            } else {
                fluids.addAll(value.getFluids().stream().map(fluid -> new FluidStack(fluid, amount, tag)).toList());
            }
        }
        if (!tags.isEmpty()) {
            return tags;
        } else {
            return fluids;
        }
    }

    @Override
    public Object2IntMap<FluidIngredient> makeChanceCache() {
        return super.makeChanceCache();
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
        int limitFluidParallel(GTRecipe recipe, int multiplier, boolean tick);
    }
}
