package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.*;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IRangedIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredientExtensions;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.fluid.*;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.common.valueprovider.*;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.core.component.DataComponentPatch;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.*;

import brachy.modularui.integration.recipeviewer.entry.fluid.FluidEntryList;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidStackList;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidTagList;
import it.unimi.dsi.fastutil.objects.*;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

import static com.gregtechceu.gtceu.api.recipe.RecipeHelper.addToRecipeHandlerMap;

@ExtensionMethod(SizedIngredientExtensions.class)
public class FluidRecipeCapability extends RecipeCapability<SizedFluidIngredient> {

    public final static FluidRecipeCapability CAP = new FluidRecipeCapability();

    protected FluidRecipeCapability() {
        super(GTCEu.id("fluid"), 0xFF3C70EE, true, 1, SerializerFluidIngredient.INSTANCE);
    }

    @Override
    public SizedFluidIngredient copyInner(SizedFluidIngredient content) {
        return content.copy();
    }

    @Override
    public SizedFluidIngredient copyWithModifier(SizedFluidIngredient content, ContentModifier modifier) {
        if (content.ingredient().hasNoFluids()) return content.copy();
        if (content.ingredient() instanceof IntProviderFluidIngredient provider) {
            IntProviderFluidIngredient copy = IntProviderFluidIngredient.of(provider.getInner(),
                    ModifiedIntProvider.of(provider.getCountProvider(), modifier));
            return new SizedFluidIngredient(copy, 1);
        }
        return content.copyWithAmount(modifier.apply(content.amount()));
    }

    public IntProviderFluidIngredient copyWithModifier(IntProviderFluidIngredient content, ContentModifier modifier) {
        if (content.hasNoFluids()) return content.copy();
        return IntProviderFluidIngredient.of(content.getInner(),
                ModifiedIntProvider.of(content.getCountProvider(), modifier));
    }

    @Override
    public List<Object> compressIngredients(@Unmodifiable Collection<Object> ingredients) {
        List<Object> list = new ObjectArrayList<>(ingredients.size());
        for (Object item : ingredients) {
            if (item instanceof SizedFluidIngredient fluid) {
                boolean isEqual = false;
                for (Object obj : list) {
                    if (obj instanceof SizedFluidIngredient SizedFluidIngredient) {
                        if (fluid.equals(SizedFluidIngredient)) {
                            isEqual = true;
                            break;
                        }
                    } else if (obj instanceof FluidStack fluidStack) {
                        if (fluid.ingredient().test(fluidStack)) {
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
                    if (obj instanceof SizedFluidIngredient fluidIngredient) {
                        if (fluidIngredient.ingredient().test(fluidStack)) {
                            isEqual = true;
                            break;
                        }
                    } else if (obj instanceof FluidStack stack) {
                        if (FluidStack.isSameFluidSameComponents(fluidStack, stack)) {
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
        if (object instanceof FluidIngredient fluidIngredient) {
            return CustomFluidMapIngredient.from(fluidIngredient);
        } else if (object instanceof SizedFluidIngredient ingredient) {
            return getDefaultMapIngredient(ingredient.ingredient());
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
        if (holder instanceof ICustomParallel p) return p.limitFluidParallel(recipe, multiplier, tick);
        var outputContents = (tick ? recipe.tickOutputs : recipe.outputs).get(this);
        if (outputContents == null || outputContents.isEmpty()) return multiplier;

        if (!holder.hasCapabilityProxies()) return 0;

        var handlers = holder.getCapabilitiesFlat(IO.OUT, this);
        if (handlers.isEmpty()) return 0;

        int minMultiplier = 0;
        int maxMultiplier = multiplier;

        int maxAmount = 0;
        List<SizedFluidIngredient> ingredients = new ArrayList<>(outputContents.size());
        for (var content : outputContents) {
            var ing = this.of(content.content());
            int amount;
            if (ing.ingredient() instanceof IRangedIngredient provider)
                amount = provider.getMaxRoll();
            else amount = ing.amount();
            maxAmount = Math.max(maxAmount, amount);
            ingredients.add(ing);
        }
        if (maxAmount == 0) return multiplier;
        if (multiplier > Integer.MAX_VALUE / maxAmount) {
            maxMultiplier = multiplier = Integer.MAX_VALUE / maxAmount;
        }

        while (minMultiplier != maxMultiplier) {
            List<SizedFluidIngredient> copied = new ArrayList<>();
            for (final var ing : ingredients) {
                copied.add(this.copyWithModifier(ing, ContentModifier.multiplier(multiplier)));
            }

            for (var handler : handlers) {
                // noinspection unchecked
                copied = (List<SizedFluidIngredient>) handler.handleRecipe(IO.OUT, recipe, copied, true);
                if (copied.isEmpty()) break;
            }
            int[] bin = ParallelLogic.adjustMultiplier(copied.isEmpty(), minMultiplier, multiplier, maxMultiplier);
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

        // Find all the fluids in the combined Fluid Input inventories and create oversized FluidStacks
        List<Object2LongMap<FluidStack>> inventoryGroups = getInputContents(holder);
        if (inventoryGroups.isEmpty()) return 0;

        // map the recipe ingredients to account for duplicated and notConsumable ingredients.
        // notConsumable ingredients are not counted towards the max ratio

        var nonConsumables = new Object2LongOpenHashMap<SizedFluidIngredient>();
        var consumables = new Object2LongOpenHashMap<SizedFluidIngredient>();
        for (Content content : inputs) {
            SizedFluidIngredient ing = of(content.content());

            int amount;
            if (ing.ingredient() instanceof IRangedIngredient provider) {
                amount = provider.getMaxRoll();
            } else {
                amount = ing.amount();
            }

            if (content.chance() == 0) {
                nonConsumables.addTo(ing, amount);
            } else {
                boolean has = false;
                for (var recipeIng : consumables.object2LongEntrySet()) {
                    var stack = ing.getFluids()[0];
                    if (recipeIng.getKey().ingredient().test(stack)) {
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

        int maxMultiplier = 0;
        // Check every inventory group
        for (var group : inventoryGroups) {
            // Check for enough NC in inventory group
            boolean satisfied = true;
            for (var ncEntry : Object2LongMaps.fastIterable(nonConsumables)) {
                SizedFluidIngredient ingredient = ncEntry.getKey();
                long needed = ncEntry.getLongValue();
                for (var stackEntry : Object2LongMaps.fastIterable(group)) {
                    if (ingredient.test(stackEntry.getKey())) {
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
                SizedFluidIngredient ingredient = cEntry.getKey();
                final long needed = cEntry.getLongValue();
                final long maxNeeded = needed * limit;
                long available = 0;
                // Search stacks in our inventory group, summing them up
                for (var stackEntry : Object2LongMaps.fastIterable(group)) {
                    if (ingredient.test(stackEntry.getKey())) {
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

    private static List<Object2LongMap<FluidStack>> getInputContents(IRecipeCapabilityHolder holder) {
        var handlerLists = holder.getCapabilitiesForIO(IO.IN);
        if (handlerLists.isEmpty()) return Collections.emptyList();

        Map<RecipeHandlerGroup, List<RecipeHandlerList>> handlerGroups = new HashMap<>();
        for (var handler : handlerLists) {
            if (!handler.hasCapability(FluidRecipeCapability.CAP)) continue;
            addToRecipeHandlerMap(handler.getGroup(), handler, handlerGroups);
        }

        List<RecipeHandlerList> distinctHandlerLists = handlerGroups.getOrDefault(
                RecipeHandlerGroupDistinctness.BUS_DISTINCT,
                Collections.emptyList());
        List<Object2LongMap<FluidStack>> invs = new ArrayList<>(distinctHandlerLists.size() + 1);
        // Handle distinct groups first, adding an inventory based on their contents individually.
        for (RecipeHandlerList handlerList : distinctHandlerLists) {
            var handlers = handlerList.getCapability(FluidRecipeCapability.CAP);
            Object2LongOpenHashMap<FluidStack> distinctInv = new Object2LongOpenHashMap<>();

            for (IRecipeHandler<?> handler : handlers) {
                for (var content : handler.getContents()) {
                    if (content instanceof FluidStack stack && !stack.isEmpty()) {
                        distinctInv.addTo(stack, stack.getAmount());
                    }
                }
            }
            if (!distinctInv.isEmpty()) invs.add(distinctInv);
        }

        // Then handle other groups. The logic of undyed hatches belonging to
        // everything has already been taken care of by addToRecipeMap()
        for (Map.Entry<RecipeHandlerGroup, List<RecipeHandlerList>> handlerListEntry : handlerGroups.entrySet()) {
            if (handlerListEntry.getKey() == RecipeHandlerGroupDistinctness.BUS_DISTINCT) continue;

            Object2LongOpenHashMap<FluidStack> inventory = new Object2LongOpenHashMap<>();
            for (RecipeHandlerList handlerList : handlerListEntry.getValue()) {
                var handlers = handlerList.getCapability(FluidRecipeCapability.CAP);
                for (var handler : handlers) {
                    for (var content : handler.getContents()) {
                        if (content instanceof FluidStack stack && !stack.isEmpty()) {
                            inventory.addTo(stack, stack.getAmount());
                        }
                    }
                }
            }
            if (!inventory.isEmpty()) invs.add(inventory);
        }

        return invs;
    }

    // Maps fluids to a FluidEntryList for XEI: either a FluidTagList or a FluidStackList
    public static FluidEntryList mapIngredientToEntryList(SizedFluidIngredient ingredient) {
        int amount;
        if (ingredient.ingredient() instanceof IRangedIngredient provider) {
            amount = provider.getMaxRoll();
        } else {
            amount = ingredient.amount();
        }

        if (ingredient.ingredient() instanceof IntersectionFluidIngredient intersection) {
            return mapIntersection(intersection, amount);
        } else if (ingredient.ingredient() instanceof TagFluidIngredient tag) {
            return FluidTagList.of(tag.tag(), amount, DataComponentPatch.EMPTY);
        } else if (ingredient.ingredient() instanceof DataComponentFluidIngredient component) {
            var key = component.fluids().unwrapKey();
            if (key.isPresent()) {
                return FluidTagList.of(key.get(), amount, component.components().asPatch());
            }
        }
        return FluidStackList.of(Arrays.asList(ingredient.getFluids()));
    }

    // Map intersection ingredients to the items inside, as recipe viewers don't support them.
    private static FluidEntryList mapIntersection(final IntersectionFluidIngredient intersection, int amount) {
        List<FluidIngredient> children = intersection.children();
        if (children.isEmpty()) return new FluidStackList();

        var childList = mapIngredientToEntryList(new SizedFluidIngredient(children.getFirst(), amount));
        FluidStackList stackList = new FluidStackList();
        for (var stack : childList.getStacks()) {
            if (children.stream().skip(1).allMatch(child -> child.test(stack))) {
                if (amount > 0) stackList.add(stack.copyWithAmount(amount));
                else stackList.add(stack.copy());
            }
        }
        return stackList;
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

    @Override
    public List<NotifiableFluidTank> getCapabilityHandlers(MetaMachine machine) {
        return machine.getTraits(NotifiableFluidTank.TYPE);
    }

    @SuppressWarnings("unchecked")
    public List<NotifiableFluidTank> getCapabilityHandlers(MetaMachine machine, IO io) {
        return getCapabilityHandlers(machine).stream()
                .filter(v -> v.getHandlerIO() == io).toList();
    }
}
