package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

// Utils for interacting with recipes without using a machine
public class DummyRecipeUtils {

    public static class DummyEnergyContainer implements IRecipeHandler<EnergyStack> {

        private final long energyCapacity;
        private long energyStored;
        private final long maxAmps;

        public DummyEnergyContainer(long capacity, long stored, long maxAmps) {
            energyStored = stored;
            energyCapacity = capacity;
            this.maxAmps = maxAmps;
        }

        @Override
        public List<EnergyStack> handleRecipeInner(IO io, GTRecipe recipe, List<EnergyStack> left, boolean simulate) {
            for (var it = left.listIterator(); it.hasNext();) {
                EnergyStack stack = it.next();
                if (stack.isEmpty()) {
                    it.remove();
                    continue;
                }

                long totalEU = stack.getTotalEU();
                long canTransfer = Math.min(totalEU, (io == IO.IN ? energyStored : energyCapacity - energyStored));
                if (!simulate) {
                    // invert the EU value if we're doing inputs (inputting *to the recipe* -> removing from handlers)
                    var energyToAdd = (io == IO.IN ? -canTransfer : canTransfer);
                    energyStored = Math.max(0, (energyCapacity - energyStored < energyToAdd) ? energyCapacity :
                            (energyStored + energyToAdd));
                }

                totalEU -= canTransfer;
                if (totalEU <= 0) {
                    it.remove();
                } else {
                    it.set(new EnergyStack(totalEU));
                }

            }

            return left.isEmpty() ? null : left;
        }

        @Override
        public @NotNull List<Object> getContents() {
            return Collections.singletonList(EnergyContainerList.calculateVoltageAmperage(energyStored, maxAmps));
        }

        @Override
        public double getTotalContentAmount() {
            return energyStored;
        }

        @Override
        public RecipeCapability<EnergyStack> getCapability() {
            return EURecipeCapability.CAP;
        }
    }

    public static class DummyItemHandler implements IRecipeHandler<SizedIngredient> {

        @Getter
        public CustomItemStackHandler storage;
        @Getter
        public IO handlerIO;

        public DummyItemHandler(IO io, int slots) {
            handlerIO = io;
            storage = new CustomItemStackHandler(slots);
        }

        public DummyItemHandler(IO io, NonNullList<ItemStack> stacks) {
            handlerIO = io;
            storage = new CustomItemStackHandler(stacks);
        }

        @Override
        public List<SizedIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<SizedIngredient> left,
                                                       boolean simulate) {
            return NotifiableItemStackHandler.handleRecipe(io, recipe, left, simulate, handlerIO, storage);
        }

        @Override
        public @NotNull List<Object> getContents() {
            List<ItemStack> stacks = new ArrayList<>();
            for (int i = 0; i < storage.getSlots(); ++i) {
                ItemStack stack = storage.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    stacks.add(stack);
                }
            }
            return new ArrayList<>(stacks);
        }

        @Override
        public double getTotalContentAmount() {
            long amount = 0;
            for (int i = 0; i < storage.getSlots(); ++i) {
                ItemStack stack = storage.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    amount += stack.getCount();
                }
            }
            return amount;
        }

        @Override
        public RecipeCapability<SizedIngredient> getCapability() {
            return ItemRecipeCapability.CAP;
        }
    }

    public static class DummyRecipeCapabilityHolder implements IRecipeCapabilityHolder {

        @Getter
        protected final Map<IO, List<RecipeHandlerList>> capabilitiesProxy;
        @Getter
        protected final Map<IO, Map<RecipeCapability<?>, List<IRecipeHandler<?>>>> capabilitiesFlat;
        @Getter
        protected final Map<RecipeCapability<?>, Object2IntMap<?>> cacheChances = new IdentityHashMap<>();

        public DummyRecipeCapabilityHolder(RecipeHandlerList... handlers) {
            this.capabilitiesProxy = new EnumMap<>(IO.class);
            this.capabilitiesFlat = new EnumMap<>(IO.class);
            for (RecipeHandlerList handler : handlers) {
                addHandlerList(handler);
            }

            for (RecipeCapability<?> cap : GTRegistries.RECIPE_CAPABILITIES) {
                cacheChances.put(cap, cap.makeChanceCache());
            }
        }
    }
}
