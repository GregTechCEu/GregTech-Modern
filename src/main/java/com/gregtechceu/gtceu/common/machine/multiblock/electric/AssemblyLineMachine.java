package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class AssemblyLineMachine extends WorkableElectricMultiblockMachine {

    @Accessors(fluent = true)
    @Getter
    @Persisted
    protected boolean allowCircuitSlots;

    public AssemblyLineMachine(IMachineBlockEntity holder, boolean allowCircuitSlots) {
        super(holder);
        this.allowCircuitSlots = allowCircuitSlots;
    }

    public AssemblyLineMachine(IMachineBlockEntity holder) {
        this(holder, false);
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new AsslineRecipeLogic(this);
    }

    public static Comparator<IMultiPart> partSorter(MultiblockControllerMachine mc) {
        return Comparator.comparing(p -> p.self().getPos(),
                RelativeDirection.RIGHT.getSorter(mc.getFrontFacing(), mc.getUpwardsFacing(), mc.isFlipped()));
    }

    private boolean checkItemInputs(@NotNull GTRecipe recipe, boolean isTick) {
        var itemInputs = (isTick ? recipe.tickInputs : recipe.inputs).getOrDefault(ItemRecipeCapability.CAP,
                Collections.emptyList());
        if (itemInputs.isEmpty()) return true;
        int inputsSize = itemInputs.size();
        var itemHandlers = getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP);
        if (itemHandlers.size() < inputsSize) return false;

        var itemInventory = itemHandlers.stream()
                .filter(IRecipeHandler::shouldSearchContent)
                .map(container -> container.getContents().stream()
                        .filter(ItemStack.class::isInstance)
                        .map(ItemStack.class::cast)
                        .filter(s -> !s.isEmpty())
                        .findFirst())
                .limit(inputsSize)
                .map(o -> o.orElse(ItemStack.EMPTY))
                .toList();

        if (itemInventory.size() < inputsSize) return false;

        for (int i = 0; i < inputsSize; i++) {
            var itemStack = itemInventory.get(i);
            Ingredient recipeStack = ItemRecipeCapability.CAP.of(itemInputs.get(i).content);
            if (!recipeStack.test(itemStack)) {
                return false;
            }
        }

        return true;
    }

    private ActionResult consumeItemContents(@NotNull GTRecipe recipe, boolean isTick) {
        var itemInputs = (isTick ? recipe.tickInputs : recipe.inputs).getOrDefault(ItemRecipeCapability.CAP,
                Collections.emptyList());
        if (itemInputs.isEmpty()) return ActionResult.SUCCESS;
        int inputsSize = itemInputs.size();
        var itemHandlers = getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP);
        if (itemHandlers.size() < inputsSize) return ActionResult.FAIL_NO_REASON;

        var itemInventory = itemHandlers.stream()
                .filter(IRecipeHandler::shouldSearchContent).toList();

        if (itemInventory.size() < inputsSize) return ActionResult.FAIL_NO_REASON;

        for (int i = 0; i < inputsSize; i++) {
            Ingredient recipeStack = ItemRecipeCapability.CAP.of(itemInputs.get(i).content);
            var currentBus = itemInventory.get(i);
            if (!(currentBus instanceof NotifiableItemStackHandler itemBus)) throw new RuntimeException(
                    "Handler in Assline.consumeItemContent's ItemRecipeCapability.IN was not of type NotifiableItemStackHandler");
            var left = itemBus.handleRecipeInner(IO.IN, recipe, new ArrayList<>(List.of(recipeStack)), true);
            if (!(left == null || left.isEmpty())) return ActionResult.FAIL_NO_REASON;
        }
        // If we get here, the recipe should be consumable

        for (int i = 0; i < inputsSize; i++) {
            Ingredient recipeStack = ItemRecipeCapability.CAP.of(itemInputs.get(i).content);
            var currentBus = itemInventory.get(i);
            if (!(currentBus instanceof NotifiableItemStackHandler itemBus)) throw new RuntimeException(
                    "Handler in Assline.consumeItemContent's ItemRecipeCapability.IN was not of type NotifiableItemStackHandler");
            var left = itemBus.handleRecipeInner(IO.IN, recipe, new ArrayList<>(List.of(recipeStack)), false);
            if (!(left == null || left.isEmpty())) {
                GTCEu.LOGGER.error(
                        "Recipe in Assline.consumeItemContents was true when simulating, but false when consuming.");
                return ActionResult.FAIL_NO_REASON;
            }
        }

        return ActionResult.SUCCESS;
    }

    private boolean checkFluidInputs(@NotNull GTRecipe recipe, boolean isTick) {
        var fluidInputs = (isTick ? recipe.tickInputs : recipe.inputs).getOrDefault(FluidRecipeCapability.CAP,
                Collections.emptyList());
        if (fluidInputs.isEmpty()) return true;
        int inputsSize = fluidInputs.size();
        var fluidHandlers = getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP);
        if (fluidHandlers.size() < inputsSize) return false;

        var fluidInventory = fluidHandlers.stream()
                .filter(IRecipeHandler::shouldSearchContent)
                .map(container -> container.getContents().stream()
                        .filter(FluidStack.class::isInstance)
                        .map(FluidStack.class::cast)
                        .filter(f -> !f.isEmpty())
                        .findFirst())
                .limit(inputsSize)
                .map(o -> o.orElse(FluidStack.EMPTY))
                .toList();

        if (fluidInventory.size() < inputsSize) return false;

        for (int i = 0; i < inputsSize; i++) {
            var fluidStack = fluidInventory.get(i);
            FluidIngredient recipeStack = FluidRecipeCapability.CAP.of(fluidInputs.get(i).content);
            if (!recipeStack.test(fluidStack) || recipeStack.getAmount() > fluidStack.getAmount()) {
                return false;
            }
        }
        return true;
    }

    private ActionResult consumeFluidContents(@NotNull GTRecipe recipe, boolean isTick) {
        var fluidInputs = (isTick ? recipe.tickInputs : recipe.inputs).getOrDefault(FluidRecipeCapability.CAP,
                Collections.emptyList());
        if (fluidInputs.isEmpty()) return ActionResult.SUCCESS;
        int fluidsSize = fluidInputs.size();
        var fluidHandlers = getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP);
        if (fluidHandlers.size() < fluidsSize) return ActionResult.FAIL_NO_REASON;

        var fluidInventory = fluidHandlers.stream()
                .filter(IRecipeHandler::shouldSearchContent).toList();

        if (fluidInventory.size() < fluidsSize) return ActionResult.FAIL_NO_REASON;

        for (int i = 0; i < fluidsSize; i++) {
            FluidIngredient recipeStack = FluidRecipeCapability.CAP.of(fluidInputs.get(i).content);
            var currentBus = fluidInventory.get(i);
            if (!(currentBus instanceof NotifiableFluidTank fluidTank)) throw new RuntimeException(
                    "Handler in Assline.consumeItemContent's FluidRecipeCapability.IN was not of type NotifiableFluidTank");
            var left = fluidTank.handleRecipeInner(IO.IN, recipe, new ArrayList<>(List.of(recipeStack)), true);
            if (!(left == null || left.isEmpty())) return ActionResult.FAIL_NO_REASON;
        }
        // If we get here, the recipe should be consumable

        for (int i = 0; i < fluidsSize; i++) {
            FluidIngredient recipeStack = FluidRecipeCapability.CAP.of(fluidInputs.get(i).content);
            var currentBus = fluidInventory.get(i);
            if (!(currentBus instanceof NotifiableFluidTank fluidTank)) throw new RuntimeException(
                    "Handler in Assline.consumeItemContent's FluidRecipeCapability.IN was not of type NotifiableFluidTank");
            var left = fluidTank.handleRecipeInner(IO.IN, recipe, new ArrayList<>(List.of(recipeStack)), false);
            if (!(left == null || left.isEmpty())) {
                GTCEu.LOGGER.error(
                        "Recipe in Assline.consumeFluidContents was true when simulating, but false when consuming.");
                return ActionResult.FAIL_NO_REASON;
            }
        }

        return ActionResult.SUCCESS;
    }

    private ActionResult consumeAll(@NotNull GTRecipe recipe, boolean isTick,
                                    Map<RecipeCapability<?>, Object2IntMap<?>> chanceCaches) {
        GTRecipe copyWithItems = recipe.copy();
        copyWithItems.inputs.clear();
        copyWithItems.tickInputs.clear();

        GTRecipe copyWithFluids = recipe.copy();
        copyWithFluids.inputs.clear();
        copyWithFluids.tickInputs.clear();

        GTRecipe copyWithoutItemsFluids = recipe.copy();
        copyWithoutItemsFluids.inputs.clear();
        copyWithoutItemsFluids.tickInputs.clear();

        for (var entry : recipe.inputs.entrySet()) {
            if (entry.getKey().equals(FluidRecipeCapability.CAP)) {
                copyWithFluids.inputs.put(entry.getKey(), entry.getValue());
            } else if (entry.getKey().equals(ItemRecipeCapability.CAP)) {
                copyWithItems.inputs.put(entry.getKey(), entry.getValue());
            } else {
                copyWithoutItemsFluids.inputs.put(entry.getKey(), entry.getValue());
            }
        }
        for (var entry : recipe.tickInputs.entrySet()) {
            if (entry.getKey().equals(FluidRecipeCapability.CAP)) {
                copyWithFluids.tickInputs.put(entry.getKey(), entry.getValue());
            } else if (entry.getKey().equals(ItemRecipeCapability.CAP)) {
                copyWithItems.tickInputs.put(entry.getKey(), entry.getValue());
            } else {
                copyWithoutItemsFluids.tickInputs.put(entry.getKey(), entry.getValue());
            }
        }
        var config = ConfigHolder.INSTANCE.machines;
        ActionResult result;
        if (config.orderedAssemblyLineItems) {
            result = consumeItemContents(copyWithItems, isTick);
        } else {
            result = isTick ?
                    RecipeHelper.handleTickRecipeIO(this, copyWithItems, IO.IN, chanceCaches) :
                    RecipeHelper.handleRecipeIO(this, copyWithItems, IO.IN, chanceCaches);
        }
        if (!result.isSuccess()) return result;

        if (config.orderedAssemblyLineFluids) {
            result = consumeFluidContents(copyWithFluids, isTick);
        } else {
            result = isTick ?
                    RecipeHelper.handleTickRecipeIO(this, copyWithFluids, IO.IN, chanceCaches) :
                    RecipeHelper.handleRecipeIO(this, copyWithFluids, IO.IN, chanceCaches);
        }
        if (!result.isSuccess()) return result;

        return isTick ?
                RecipeHelper.handleTickRecipeIO(this, copyWithoutItemsFluids, IO.IN, chanceCaches) :
                RecipeHelper.handleRecipeIO(this, copyWithoutItemsFluids, IO.IN, chanceCaches);
    }

    class AsslineRecipeLogic extends RecipeLogic {

        public AsslineRecipeLogic(IRecipeLogicMachine machine) {
            super(machine);
        }

        @Override
        protected ActionResult handleRecipeIO(GTRecipe recipe, IO io) {
            if (io.equals(IO.IN)) {
                return consumeAll(recipe, false, this.getChanceCaches());
            }
            return RecipeHelper.handleRecipeIO(machine, recipe, io, this.chanceCaches);
        }

        @Override
        protected ActionResult handleTickRecipeIO(GTRecipe recipe, IO io) {
            if (io.equals(IO.IN)) {
                return consumeAll(recipe, true, this.getChanceCaches());
            }
            return RecipeHelper.handleTickRecipeIO(machine, recipe, io, this.chanceCaches);
        }

        @Override
        protected ActionResult matchRecipe(GTRecipe recipe) {
            // Match by normal inputs first
            ActionResult normalMatch = RecipeHelper.matchContents(machine, recipe);
            if (!normalMatch.isSuccess()) return normalMatch;

            var config = ConfigHolder.INSTANCE.machines;
            if (!config.orderedAssemblyLineItems && !config.orderedAssemblyLineFluids) return ActionResult.SUCCESS;
            if (!checkItemInputs(recipe, false)) return ActionResult.FAIL_NO_REASON;
            if (!checkItemInputs(recipe, true)) return ActionResult.FAIL_NO_REASON;

            if (!config.orderedAssemblyLineFluids) return ActionResult.SUCCESS;
            if (!checkFluidInputs(recipe, false)) return ActionResult.FAIL_NO_REASON;
            if (!checkFluidInputs(recipe, true)) return ActionResult.FAIL_NO_REASON;
            return ActionResult.SUCCESS;
        }
    }
}
