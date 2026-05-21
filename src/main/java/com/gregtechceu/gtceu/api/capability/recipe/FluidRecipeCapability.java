package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.fluid.*;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI;
import com.gregtechceu.gtceu.client.TooltipsHandler;
import com.gregtechceu.gtceu.common.valueprovider.*;
import com.gregtechceu.gtceu.integration.xei.entry.fluid.FluidEntryList;
import com.gregtechceu.gtceu.integration.xei.entry.fluid.FluidStackList;
import com.gregtechceu.gtceu.integration.xei.entry.fluid.FluidTagList;
import com.gregtechceu.gtceu.integration.xei.handlers.fluid.CycleFluidEntryHandler;
import com.gregtechceu.gtceu.integration.xei.widgets.GTRecipeWidget;
import com.gregtechceu.gtceu.utils.GTMath;

import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.jei.IngredientIO;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.Unmodifiable;

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
        if (content instanceof IntProviderFluidIngredient provider) {
            return IntProviderFluidIngredient.of(provider.getInner(),
                    ModifiedIntProvider.of(provider.getCountProvider(), modifier));
        }
        FluidIngredient copy = content.copy();
        copy.setAmount(modifier.apply(copy.getAmount()));
        return copy;
    }

    @Override
    public List<Object> compressIngredients(@Unmodifiable Collection<Object> ingredients) {
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
    public @Nullable List<AbstractMapIngredient> getDefaultMapIngredient(Object object) {
        if (object instanceof FluidIngredient ingredient) {
            return FluidStackMapIngredient.from(ingredient);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isRecipeSearchFilter() {
        return true;
    }

    @Override
    public int limitMaxParallelByOutput(RecipeHandlerGroup holder, GTRecipe recipe, int multiplier, boolean tick) {
        var outputContents = (tick ? recipe.tickOutputs : recipe.outputs).get(this);
        if (outputContents == null || outputContents.isEmpty()) return multiplier;

        var handlers = holder.getOutputHandlerMap().get(this);
        if (handlers == null || handlers.isEmpty()) return 0;

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
    public int getMaxParallelByInput(RecipeHandlerGroup holder, GTRecipe recipe, int limit, boolean tick) {
        var inputs = (tick ? recipe.tickInputs : recipe.inputs).get(this);
        if (inputs == null || inputs.isEmpty()) return limit;

        // Find all the fluids in the combined Fluid Input inventories and create oversized FluidStacks
        Object2LongMap<FluidStack> inventory = getInputContents(holder);
        if (inventory.isEmpty()) return 0;

        // map the recipe ingredients to account for duplicated and notConsumable ingredients.
        // notConsumable ingredients are not counted towards the max ratio
        var nonConsumables = new Object2LongOpenHashMap<FluidIngredient>();
        var consumables = new Object2LongOpenHashMap<FluidIngredient>();
        for (Content content : inputs) {
            FluidIngredient ing = of(content.content);

            int amount;
            if (ing instanceof IntProviderFluidIngredient provider) amount = provider.getCountProvider().getMaxValue();
            else amount = ing.getAmount();

            if (content.chance == 0) {
                nonConsumables.addTo(ing, amount);
            } else {
                boolean has = false;
                for (var recipeIng : consumables.object2LongEntrySet()) {
                    var stack = ing.getStacks()[0];
                    if (recipeIng.getKey().test(stack)) {
                        recipeIng.setValue(recipeIng.getLongValue() + stack.getAmount());
                        has = true;
                        break;
                    }
                }
                if (!has) {
                    consumables.addTo(ing, amount);
                }
            }
        }

        // is this even possible
        if (consumables.isEmpty() && nonConsumables.isEmpty()) return limit;

        // Check for enough NC in inventory
        for (var ncEntry : Object2LongMaps.fastIterable(nonConsumables)) {
            FluidIngredient ingredient = ncEntry.getKey();
            long needed = ncEntry.getLongValue();
            for (var stackEntry : Object2LongMaps.fastIterable(inventory)) {
                if (ingredient.test(stackEntry.getKey())) {
                    long count = stackEntry.getLongValue();
                    long lesser = Math.min(needed, count);
                    count -= lesser;
                    needed -= lesser;
                    stackEntry.setValue(count);
                    if (needed == 0) break;
                }
            }
            if (needed > 0) return 0;
        }
        // Satisfied NC + no consumables -> early return
        if (consumables.isEmpty()) return limit;

        int maxMultiplier = Integer.MAX_VALUE;
        // Loop over all consumables
        for (var cEntry : Object2LongMaps.fastIterable(consumables)) {
            FluidIngredient ingredient = cEntry.getKey();
            final long needed = cEntry.getLongValue();
            final long maxNeeded = needed * limit;
            long available = 0;
            // Search stacks in our inventory, summing them up
            for (var stackEntry : Object2LongMaps.fastIterable(inventory)) {
                if (ingredient.test(stackEntry.getKey())) {
                    available += stackEntry.getLongValue();
                    // We can stop if we already have enough for max parallel
                    if (available >= maxNeeded) break;
                }
            }
            // ratio will equal 0 if available < needed
            int ratio = GTMath.saturatedCast(Math.min(limit, available / needed));
            maxMultiplier = Math.min(maxMultiplier, ratio);
            if (ratio == 0) break;
        }

        return maxMultiplier;
    }

    private static Object2LongMap<FluidStack> getInputContents(RecipeHandlerGroup holder) {
        var handlers = holder.getInputHandlerMap().get(FluidRecipeCapability.CAP);

        Object2LongOpenHashMap<FluidStack> inventory = new Object2LongOpenHashMap<>();
        if (handlers == null || handlers.isEmpty()) return inventory;
        for (var handler : handlers) {
            for (var content : handler.getContents()) {
                if (content instanceof FluidStack stack && !stack.isEmpty()) {
                    inventory.addTo(stack, stack.getAmount());
                }
            }
        }
        return inventory;
    }

    @Override
    public @NotNull List<Object> createXEIContainerContents(List<Content> contents, GTRecipe recipe, IO io) {
        List<Object> entryLists = contents.stream()
                .map(Content::getContent)
                .map(this::of)
                .map(FluidRecipeCapability::mapFluid)
                .collect(Collectors.toList());

        while (entryLists.size() < recipe.recipeType.getMaxOutputs(this)) entryLists.add(null);
        return entryLists;
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
            if (storage instanceof IFluidHandler fluidHandler) {
                tank.setFluidTank(fluidHandler, index);
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
                    if (ingredient instanceof IntProviderFluidIngredient provider) {
                        IntProvider countProvider = provider.getCountProvider();
                        tooltips.add(Component.translatable("gtceu.gui.content.fluid_range",
                                countProvider.getMinValue(), countProvider.getMaxValue())
                                .withStyle(ChatFormatting.GOLD));
                    }
                    GTRecipeWidget.setConsumedChance(content,
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
        int amount;
        if (ingredient instanceof IntProviderFluidIngredient) {
            amount = 1;
        } else {
            amount = ingredient.getAmount();
        }
        CompoundTag tag = ingredient.getNbt();

        FluidTagList tags = new FluidTagList();
        FluidStackList fluids = new FluidStackList();
        for (FluidIngredient.Value value : ingredient.values) {
            if (value instanceof FluidIngredient.TagValue tagValue) {
                tags.add(tagValue.tag(), amount, ingredient.getNbt());
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

    // Fluids should be respected for distinct checks
    @Override
    public boolean shouldBypassDistinct() {
        return false;
    }
}
