package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.RecipeElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class AssemblyLineMachine extends RecipeElectricMultiblockMachine {

    public AssemblyLineMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    List<IRecipeHandler<?>> itemHandlers;
    List<IRecipeHandler<?>> fluidHandlers;

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new AsslineRecipeLogic(this);
    }

    public static Comparator<IMultiPart> partSorter(MultiblockControllerMachine mc) {
        return Comparator.comparing(p -> p.self().getPos(),
                RelativeDirection.RIGHT.getSorter(mc.getFrontFacing(), mc.getUpwardsFacing(), mc.isFlipped()));
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        itemHandlers = getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).stream()
                .filter(IRecipeHandler::shouldSearchContent)
                .toList();

        fluidHandlers = getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP).stream()
                .filter(IRecipeHandler::shouldSearchContent)
                .toList();

    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        itemHandlers = null;
        fluidHandlers = null;
    }

    private boolean checkItemInputs(@NotNull GTRecipe recipe, boolean isTick) {
        var itemInputs = (isTick ? recipe.tickInputs : recipe.inputs).getOrDefault(ItemRecipeCapability.CAP,
                Collections.emptyList());
        if (itemInputs.isEmpty()) return true;
        int inputsSize = itemInputs.size();
        if (itemHandlers.size() < inputsSize) return false;

        var itemInventory = itemHandlers.stream()
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
            var recipeStack = itemInputs.get(i);
            if (!recipeStack.test(itemStack) || recipeStack.getCount() > itemStack.getCount()) {
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
        if (itemHandlers.size() < inputsSize) return ActionResult.FAIL_NO_REASON;

        for (int i = 0; i < inputsSize; i++) {
            var recipeStack = itemInputs.get(i);
            var currentBus = itemHandlers.get(i);
            if (!(currentBus instanceof NotifiableItemStackHandler itemBus)) throw new RuntimeException(
                    "Handler in Assline.consumeItemContent's ItemRecipeCapability.IN was not of type NotifiableItemStackHandler");
            if (!itemBus.handleRecipe(IO.IN, recipe, new ArrayList<>(List.of(recipeStack)), true)) {
                return ActionResult.FAIL_NO_REASON;
            }
        }
        // If we get here, the recipe should be consumable

        for (int i = 0; i < inputsSize; i++) {
            var recipeStack = itemInputs.get(i);
            var currentBus = itemHandlers.get(i);
            if (!(currentBus instanceof NotifiableItemStackHandler itemBus)) throw new RuntimeException(
                    "Handler in Assline.consumeItemContent's ItemRecipeCapability.IN was not of type NotifiableItemStackHandler");
            if (!itemBus.handleRecipe(IO.IN, recipe, new ArrayList<>(List.of(recipeStack)), false)) {
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
        if (fluidHandlers.size() < inputsSize) return false;

        var fluidInventory = fluidHandlers.stream()
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
            var recipeStack = fluidInputs.get(i);
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

        if (fluidHandlers.size() < fluidsSize) return ActionResult.FAIL_NO_REASON;

        for (int i = 0; i < fluidsSize; i++) {
            var recipeStack = fluidInputs.get(i);
            var currentBus = fluidHandlers.get(i);
            if (!(currentBus instanceof NotifiableFluidTank fluidTank)) throw new RuntimeException(
                    "Handler in Assline.consumeItemContent's FluidRecipeCapability.IN was not of type NotifiableFluidTank");
            if (!fluidTank.handleRecipe(IO.IN, recipe, new ArrayList<>(List.of(recipeStack)), true)) {
                return ActionResult.FAIL_NO_REASON;
            }
        }
        // If we get here, the recipe should be consumable

        for (int i = 0; i < fluidsSize; i++) {
            var recipeStack = fluidInputs.get(i);
            var currentBus = fluidHandlers.get(i);
            if (!(currentBus instanceof NotifiableFluidTank fluidTank)) throw new RuntimeException(
                    "Handler in Assline.consumeItemContent's FluidRecipeCapability.IN was not of type NotifiableFluidTank");
            if (!fluidTank.handleRecipe(IO.IN, recipe, new ArrayList<>(List.of(recipeStack)), false)) {
                GTCEu.LOGGER.error(
                        "Recipe in Assline.consumeFluidContents was true when simulating, but false when consuming.");
                return ActionResult.FAIL_NO_REASON;
            }
        }

        return ActionResult.SUCCESS;
    }

    private ActionResult consumeAll(@NotNull GTRecipe recipe, boolean isTick) {
        var config = ConfigHolder.INSTANCE.machines;
        ActionResult result;

        if (config.orderedAssemblyLineItems) {
            result = consumeItemContents(recipe, isTick);
            if (!result.isSuccess()) return result;
            result = consumeFluidContents(recipe, isTick);
            if (!result.isSuccess()) return result;
            var copyWithoutItemsFluids = ((AsslineRecipeLogic)getRecipeLogic()).getCachedCopy(recipe);
            return isTick ?
                    RecipeHelper.handleTickRecipeIO(recipeLogic.getLastGroup(), copyWithoutItemsFluids, IO.IN) :
                    RecipeHelper.handleRecipeIO(recipeLogic.getLastGroup(), copyWithoutItemsFluids, IO.IN);

        } else {
           return isTick ?
                    RecipeHelper.handleTickRecipeIO(recipeLogic.getLastGroup(), recipe, IO.IN) :
                    RecipeHelper.handleRecipeIO(recipeLogic.getLastGroup(), recipe, IO.IN);
        }
    }

    class AsslineRecipeLogic extends RecipeLogic {

        GTRecipe copyWithoutItemsFluids;

        public AsslineRecipeLogic(IRecipeLogicMachine machine) {
            super(machine);
        }

        GTRecipe getCachedCopy(GTRecipe recipe) {
            if (copyWithoutItemsFluids == null || !copyWithoutItemsFluids.id.equals(recipe.id)) {
                copyWithoutItemsFluids = recipe.copy();
                copyWithoutItemsFluids.inputs.remove(ItemRecipeCapability.CAP);
                copyWithoutItemsFluids.tickInputs.remove(ItemRecipeCapability.CAP);
                copyWithoutItemsFluids.inputs.remove(FluidRecipeCapability.CAP);
                copyWithoutItemsFluids.tickInputs.remove(FluidRecipeCapability.CAP);
            }
            return copyWithoutItemsFluids;
        }

        @Override
        protected ActionResult handleRecipeIO(GTRecipe recipe, IO io) {
            if (io == IO.IN) {
                return consumeAll(recipe, false);
            }
            return RecipeHelper.handleRecipeIO(getLastGroup(), recipe, io);
        }

        @Override
        protected ActionResult handleTickRecipeIO(GTRecipe recipe, IO io) {
            if (io == IO.IN) {
                return consumeAll(recipe, true);
            }
            return RecipeHelper.handleTickRecipeIO(getLastGroup(), recipe, io);
        }

        @Override
        protected ActionResult matchRecipe(GTRecipe recipe) {
            // Match by normal inputs first
            ActionResult normalMatch = RecipeHelper.matchContents(getLastGroup(), recipe);
            if (!normalMatch.isSuccess()) return normalMatch;

            var config = ConfigHolder.INSTANCE.machines;
            if (!config.orderedAssemblyLineItems && !config.orderedAssemblyLineFluids) return ActionResult.SUCCESS;
            if (!checkItemInputs(recipe, false)) return ActionResult.fail(
                    Component.translatable("gtceu.recipe_logic.assembly_line_item_inputs_out_of_order"), ItemRecipeCapability.CAP, IO.IN);
            if (!checkItemInputs(recipe, true)) return ActionResult.fail(
                    Component.translatable("gtceu.recipe_logic.assembly_line_item_inputs_out_of_order"), ItemRecipeCapability.CAP, IO.IN);

            if (!config.orderedAssemblyLineFluids) return ActionResult.SUCCESS;
            if (!checkFluidInputs(recipe, false)) return ActionResult.fail(
                    Component.translatable("gtceu.recipe_logic.assembly_line_fluid_inputs_out_of_order"), FluidRecipeCapability.CAP, IO.IN);
            if (!checkFluidInputs(recipe, true)) return ActionResult.fail(
                    Component.translatable("gtceu.recipe_logic.assembly_line_fluid_inputs_out_of_order"), FluidRecipeCapability.CAP, IO.IN);
            return ActionResult.SUCCESS;
        }


    }
}
