package com.gregtechceu.gtceu.integration.ae2.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.trait.IRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;

import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
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
    protected final NotifiableRecipeHandlerTrait<SizedIngredient> itemInputHandler;

    @Getter
    protected final NotifiableRecipeHandlerTrait<SizedFluidIngredient> fluidInputHandler;

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

    public List<SizedIngredient> handleItemInner(
                                                 RecipeHolder<GTRecipe> recipe, List<SizedIngredient> left,
                                                 boolean simulate) {
        var internalInv = getMachine().getInternalInventory();
        if (recipe.id().equals(lockedRecipeId) && lockedSlot >= 0) {
            return internalInv[lockedSlot].handleItemInternal(left, simulate);
        }

        this.lockedRecipeId = recipe.id();
        List<SizedIngredient> contents = left;
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

    public List<SizedFluidIngredient> handleFluidInner(
                                                       RecipeHolder<GTRecipe> recipe, List<SizedFluidIngredient> left,
                                                       boolean simulate) {
        var internalInv = getMachine().getInternalInventory();
        if (recipe.id().equals(lockedRecipeId) && lockedSlot >= 0) {
            return internalInv[lockedSlot].handleFluidInternal(left, simulate);
        }

        this.lockedRecipeId = recipe.id();
        List<SizedFluidIngredient> contents = left;
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

    public class ItemInputHandler extends NotifiableRecipeHandlerTrait<SizedIngredient> {

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
        public List<SizedIngredient> handleRecipeInner(IO io, RecipeHolder<GTRecipe> recipe, List<SizedIngredient> left,
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
        public RecipeCapability<SizedIngredient> getCapability() {
            return ItemRecipeCapability.CAP;
        }

        @Override
        public void preWorking(IRecipeCapabilityHolder holder, IO io, RecipeHolder<GTRecipe> recipe) {
            super.preWorking(holder, io, recipe);
            lockedRecipeId = null;
        }
    }

    public class FluidInputHandler extends NotifiableRecipeHandlerTrait<SizedFluidIngredient> {

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
        public List<SizedFluidIngredient> handleRecipeInner(IO io,
                                                            RecipeHolder<GTRecipe> recipe,
                                                            List<SizedFluidIngredient> left,
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
        public RecipeCapability<SizedFluidIngredient> getCapability() {
            return FluidRecipeCapability.CAP;
        }

        @Override
        public void preWorking(IRecipeCapabilityHolder holder, IO io, RecipeHolder<GTRecipe> recipe) {
            super.preWorking(holder, io, recipe);
            lockedRecipeId = null;
        }
    }

    private static List<SizedIngredient> copyIngredients(List<SizedIngredient> ingredients) {
        List<SizedIngredient> result = new ObjectArrayList<>(ingredients.size());
        for (SizedIngredient ingredient : ingredients) {
            result.add(ItemRecipeCapability.CAP.copyInner(ingredient));
        }
        return result;
    }

    private static List<SizedFluidIngredient> copyFluidIngredients(List<SizedFluidIngredient> ingredients) {
        List<SizedFluidIngredient> result = new ObjectArrayList<>(ingredients.size());
        for (SizedFluidIngredient ingredient : ingredients) {
            result.add(FluidRecipeCapability.CAP.copyInner(ingredient));
        }
        return result;
    }

    public static Pair<Object2IntOpenHashMap<Item>, Object2IntOpenHashMap<Fluid>> mergeInternalSlot(MEPatternBufferPartMachine.InternalSlot[] internalSlots) {
        Object2IntOpenHashMap<Item> items = new Object2IntOpenHashMap<>();
        Object2IntOpenHashMap<Fluid> fluids = new Object2IntOpenHashMap<>();
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
