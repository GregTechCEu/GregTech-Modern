package com.gregtechceu.gtceu.integration.ae2.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.trait.IRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;

import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MEPatternBufferRecipeHandler extends MachineTrait {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MEPatternBufferPartMachine.class);
    private ResourceLocation lockedRecipeId;
    private int lockedSlot;
    protected List<Runnable> listeners = new ArrayList<>();

    @Getter
    protected final NotifiableRecipeHandlerTrait<Ingredient> itemInputHandler;

    @Getter
    protected final NotifiableRecipeHandlerTrait<FluidIngredient> fluidInputHandler;

    public MEPatternBufferRecipeHandler(MEPatternBufferPartMachine ioBuffer) {
        super(ioBuffer);
        this.itemInputHandler = new ItemInputHandler(ioBuffer);
        this.fluidInputHandler = new FluidInputHandler(ioBuffer);
    }

    public void onChanged() {
        listeners.forEach(Runnable::run);
    }

    @Override
    public MEPatternBufferPartMachine getMachine() {
        return (MEPatternBufferPartMachine) super.getMachine();
    }

    public List<Ingredient> handleItemInner(
                                            GTRecipe recipe, List<Ingredient> left, boolean simulate) {
        var internalInv = getMachine().getInternalInventory();
        if (recipe.id.equals(lockedRecipeId) && lockedSlot >= 0) {
            return internalInv[lockedSlot].handleItemInternal(left, simulate);
        }

        this.lockedRecipeId = recipe.id;
        List<Ingredient> contents = left;
        for (int i = 0; i < internalInv.length; i++) {
            if (internalInv[i].isItemEmpty()) continue;
            contents = internalInv[i].handleItemInternal(contents, simulate);
            if (contents == null) {
                this.lockedSlot = i;
                return contents;
            }
            contents = copyIngredients(left);
        }
        this.lockedSlot = -1;
        return left;
    }

    public List<FluidIngredient> handleFluidInner(
                                                  GTRecipe recipe, List<FluidIngredient> left, boolean simulate) {
        var internalInv = getMachine().getInternalInventory();
        if (recipe.id.equals(lockedRecipeId) && lockedSlot >= 0) {
            return internalInv[lockedSlot].handleFluidInternal(left, simulate);
        }

        this.lockedRecipeId = recipe.id;
        List<FluidIngredient> contents = left;
        for (int i = 0; i < internalInv.length; i++) {
            if (internalInv[i].isFluidEmpty()) continue;
            contents = internalInv[i].handleFluidInternal(contents, simulate);

            if (contents == null) {
                this.lockedSlot = i;
                return contents;
            }
            contents = copyFluidIngredients(left);
        }
        this.lockedSlot = -1;
        return left;
    }

    @SuppressWarnings("rawtypes")
    public List<IRecipeHandlerTrait> getRecipeHandlers() {
        return List.of(fluidInputHandler, itemInputHandler);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public class ItemInputHandler extends NotifiableRecipeHandlerTrait<Ingredient> {

        public ItemInputHandler(MEPatternBufferPartMachine machine) {
            super(machine);
        }

        public MEPatternBufferPartMachine getMachine() {
            return (MEPatternBufferPartMachine) this.machine;
        }

        @Override
        public IO getHandlerIO() {
            return IO.IN;
        }

        @Override
        public ISubscription addChangedListener(Runnable listener) {
            listeners.add(listener);
            return () -> listeners.remove(listener);
        }

        @Override
        public List<Ingredient> handleRecipeInner(IO io, GTRecipe recipe, List<Ingredient> left,
                                                  @Nullable String slotName, boolean simulate) {
            if (io != IO.IN) return left;
            var machine = getMachine();
            machine.getCircuitInventorySimulated().handleRecipeInner(io, recipe, left, slotName, simulate);
            machine.getShareInventory().handleRecipeInner(io, recipe, left, slotName, simulate);
            return handleItemInner(recipe, left, simulate);
        }

        @Override
        public List<Object> getContents() {
            return Arrays.stream(getMachine().getInternalInventory())
                    .map(MEPatternBufferPartMachine.InternalSlot::getItemInputs)
                    .flatMap(Arrays::stream)
                    .collect(Collectors.toList());
        }

        @Override
        public double getTotalContentAmount() {
            return Arrays.stream(getMachine().getInternalInventory())
                    .map(MEPatternBufferPartMachine.InternalSlot::getItemInputs)
                    .flatMap(Arrays::stream)
                    .mapToLong(ItemStack::getCount)
                    .sum();
        }

        @Override
        public int getPriority() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isDistinct() {
            return true;
        }

        @Override
        public RecipeCapability<Ingredient> getCapability() {
            return ItemRecipeCapability.CAP;
        }

        @Override
        public void preWorking(IRecipeCapabilityHolder holder, IO io, GTRecipe recipe) {
            super.preWorking(holder, io, recipe);
            lockedRecipeId = null;
        }
    }

    public class FluidInputHandler extends NotifiableRecipeHandlerTrait<FluidIngredient> {

        public FluidInputHandler(MEPatternBufferPartMachine machine) {
            super(machine);
        }

        public MEPatternBufferPartMachine getMachine() {
            return (MEPatternBufferPartMachine) this.machine;
        }

        @Override
        public IO getHandlerIO() {
            return IO.IN;
        }

        @Override
        public ISubscription addChangedListener(Runnable listener) {
            listeners.add(listener);
            return () -> listeners.remove(listener);
        }

        @Override
        public List<FluidIngredient> handleRecipeInner(
                                                       IO io,
                                                       GTRecipe recipe,
                                                       List<FluidIngredient> left,
                                                       @Nullable String slotName,
                                                       boolean simulate) {
            if (io != IO.IN) return left;
            getMachine().getShareTank().handleRecipeInner(io, recipe, left, slotName, simulate);
            return handleFluidInner(recipe, left, simulate);
        }

        @Override
        public List<Object> getContents() {
            return Arrays.stream(getMachine().getInternalInventory())
                    .map(MEPatternBufferPartMachine.InternalSlot::getFluidInputs)
                    .flatMap(Arrays::stream)
                    .collect(Collectors.toList());
        }

        @Override
        public double getTotalContentAmount() {
            return Arrays.stream(getMachine().getInternalInventory())
                    .map(MEPatternBufferPartMachine.InternalSlot::getFluidInputs)
                    .flatMap(Arrays::stream)
                    .mapToLong(FluidStack::getAmount)
                    .sum();
        }

        @Override
        public int getPriority() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isDistinct() {
            return true;
        }

        @Override
        public RecipeCapability<FluidIngredient> getCapability() {
            return FluidRecipeCapability.CAP;
        }

        @Override
        public void preWorking(IRecipeCapabilityHolder holder, IO io, GTRecipe recipe) {
            super.preWorking(holder, io, recipe);
            lockedRecipeId = null;
        }
    }

    private static List<Ingredient> copyIngredients(List<Ingredient> ingredients) {
        List<Ingredient> result = new ObjectArrayList<>(ingredients.size());
        for (Ingredient ingredient : ingredients) {
            result.add(ItemRecipeCapability.CAP.copyInner(ingredient));
        }
        return result;
    }

    private static List<FluidIngredient> copyFluidIngredients(List<FluidIngredient> ingredients) {
        List<FluidIngredient> result = new ObjectArrayList<>(ingredients.size());
        for (FluidIngredient ingredient : ingredients) {
            result.add(FluidRecipeCapability.CAP.copyInner(ingredient));
        }
        return result;
    }

    public static Pair<Object2LongOpenHashMap<Item>, Object2LongOpenHashMap<Fluid>> mergeInternalSlot(
                                                                                                      MEPatternBufferPartMachine.InternalSlot[] internalSlots) {
        Object2LongOpenHashMap<Item> items = new Object2LongOpenHashMap<>();
        Object2LongOpenHashMap<Fluid> fluids = new Object2LongOpenHashMap<>();
        for (MEPatternBufferPartMachine.InternalSlot internalSlot : internalSlots) {
            for (ItemStack stack : internalSlot.getItemInputs()) {
                items.addTo(stack.getItem(), stack.getCount());
            }
            for (FluidStack stack : internalSlot.getFluidInputs()) {
                fluids.addTo(stack.getFluid(), stack.getAmount());
            }
        }
        return new ImmutablePair<>(items, fluids);
    }
}
