package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.VoidFluidHandler;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DistillationTowerMachine extends WorkableElectricMultiblockMachine
                                      implements FluidRecipeCapability.ICustomParallel {

    @Getter
    private List<IFluidHandler> fluidOutputs;
    @Getter
    @Nullable
    private IFluidHandler firstValid = null;
    private final int yOffset;

    public DistillationTowerMachine(IMachineBlockEntity holder) {
        this(holder, 1);
    }

    /**
     * Construct DT Machine
     * 
     * @param holder  BlockEntity holder
     * @param yOffset The Y difference between the controller and the first fluid output
     */
    public DistillationTowerMachine(IMachineBlockEntity holder, int yOffset) {
        super(holder);
        this.yOffset = yOffset;
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new DistillationTowerLogic(this);
    }

    @Override
    public DistillationTowerLogic getRecipeLogic() {
        return (DistillationTowerLogic) super.getRecipeLogic();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        final int startY = getPos().getY() + yOffset;
        List<IMultiPart> parts = getParts().stream()
                .filter(part -> PartAbility.EXPORT_FLUIDS.isApplicable(part.self().getBlockState().getBlock()))
                .filter(part -> part.self().getPos().getY() >= startY)
                .toList();

        if (!parts.isEmpty()) {
            // Loop from controller y + offset -> the highest output hatch
            int maxY = parts.get(parts.size() - 1).self().getPos().getY();
            fluidOutputs = new ObjectArrayList<>(maxY - startY);
            int outputIndex = 0;
            for (int y = startY; y <= maxY; ++y) {
                if (parts.size() <= outputIndex) {
                    fluidOutputs.add(VoidFluidHandler.INSTANCE);
                    continue;
                }

                var part = parts.get(outputIndex);
                if (part.self().getPos().getY() == y) {
                    var handler = part.getRecipeHandlers().get(0).getCapability(FluidRecipeCapability.CAP)
                            .stream()
                            .filter(IFluidHandler.class::isInstance)
                            .findFirst()
                            .map(IFluidHandler.class::cast)
                            .orElse(VoidFluidHandler.INSTANCE);
                    addOutput(handler);
                    outputIndex++;
                } else if (part.self().getPos().getY() > y) {
                    fluidOutputs.add(VoidFluidHandler.INSTANCE);
                } else {
                    GTCEu.LOGGER.error(
                            "The Distillation Tower at {} has a fluid export hatch with an unexpected Y position",
                            getPos());
                    onStructureInvalid();
                    return;
                }
            }
        } else onStructureInvalid();
    }

    private void addOutput(IFluidHandler handler) {
        fluidOutputs.add(handler);
        if (firstValid == null && handler != VoidFluidHandler.INSTANCE) firstValid = handler;
    }

    @Override
    public void onStructureInvalid() {
        fluidOutputs = null;
        firstValid = null;
        super.onStructureInvalid();
    }

    @Override
    public int limitFluidParallel(GTRecipe recipe, int multiplier, boolean tick) {
        int minMultiplier = 0;
        int maxMultiplier = multiplier;

        var contents = (tick ? recipe.tickInputs : recipe.inputs).get(FluidRecipeCapability.CAP);
        if (contents == null || contents.isEmpty()) return multiplier;

        int maxAmount = contents.stream()
                .map(Content::getContent)
                .map(FluidRecipeCapability.CAP::of)
                .filter(i -> !i.isEmpty())
                .mapToInt(FluidIngredient::getAmount)
                .max()
                .orElse(0);

        if (maxAmount == 0) return multiplier;
        if (multiplier > Integer.MAX_VALUE / maxAmount) {
            maxMultiplier = multiplier = Integer.MAX_VALUE / maxAmount;
        }

        while (minMultiplier != maxMultiplier) {
            GTRecipe copy = modifyOutputs(recipe, ContentModifier.multiplier(multiplier));
            boolean filled = getRecipeLogic().applyFluidOutputs(copy, FluidAction.SIMULATE, getVoidingMode());
            int[] bin = ParallelLogic.adjustMultiplier(filled, minMultiplier, multiplier, maxMultiplier);
            minMultiplier = bin[0];
            multiplier = bin[1];
            maxMultiplier = bin[2];
        }
        return multiplier;
    }

    private static GTRecipe modifyOutputs(GTRecipe recipe, ContentModifier cm) {
        return new GTRecipe(recipe.recipeType, recipe.id, recipe.inputs, cm.applyContents(recipe.outputs),
                recipe.tickInputs, cm.applyContents(recipe.tickOutputs), recipe.inputChanceLogics,
                recipe.outputChanceLogics,
                recipe.tickInputChanceLogics, recipe.tickOutputChanceLogics, recipe.conditions,
                recipe.ingredientActions,
                recipe.data, recipe.duration, recipe.recipeCategory);
    }

    public static class DistillationTowerLogic extends RecipeLogic {

        @Nullable
        @Persisted
        @DescSynced
        GTRecipe workingRecipe = null;

        public DistillationTowerLogic(IRecipeLogicMachine machine) {
            super(machine);
        }

        @NotNull
        @Override
        public DistillationTowerMachine getMachine() {
            return (DistillationTowerMachine) super.getMachine();
        }

        // Copy of lastRecipe with fluid outputs trimmed, for output displays like Jade or GUI text
        @Override
        public @Nullable GTRecipe getLastRecipe() {
            return workingRecipe;
        }

        @Override
        protected ActionResult matchRecipe(GTRecipe recipe) {
            var match = matchDTRecipe(recipe);
            if (!match.isSuccess()) return match;

            return RecipeHelper.matchTickRecipe(this.machine, recipe);
        }

        @Override
        protected void handleSearchingRecipes(Iterator<GTRecipe> matches) {
            workingRecipe = null;
            super.handleSearchingRecipes(matches);
        }

        private ActionResult matchDTRecipe(GTRecipe recipe) {
            var result = RecipeHelper.handleRecipe(machine, recipe, IO.IN, recipe.inputs,
                    Collections.emptyMap(), false, true);
            if (!result.isSuccess()) return result;

            var items = recipe.getOutputContents(ItemRecipeCapability.CAP);
            if (!items.isEmpty()) {
                Map<RecipeCapability<?>, List<Content>> out = Map.of(ItemRecipeCapability.CAP, items);
                result = RecipeHelper.handleRecipe(machine, recipe, IO.OUT, out, Collections.emptyMap(), false, true);
                if (!result.isSuccess()) return result;
            }

            if (!applyFluidOutputs(recipe, FluidAction.SIMULATE, machine.getVoidingMode())) {
                return ActionResult.fail(Component.translatable("gtceu.recipe_logic.insufficient_out")
                        .append(": ")
                        .append(FluidRecipeCapability.CAP.getName()), FluidRecipeCapability.CAP, IO.OUT);
            }

            return ActionResult.SUCCESS;
        }

        private void updateWorkingRecipe(GTRecipe recipe) {
            if (recipe.recipeType == GTRecipeTypes.DISTILLERY_RECIPES) {
                this.workingRecipe = recipe;
                return;
            }

            this.workingRecipe = recipe.copy();
            var contents = recipe.getOutputContents(FluidRecipeCapability.CAP);
            var outputs = getMachine().getFluidOutputs();
            List<Content> trimmed = new ArrayList<>(12);
            for (int i = 0; i < Math.min(contents.size(), outputs.size()); ++i) {
                if (!(outputs.get(i) instanceof VoidFluidHandler)) trimmed.add(contents.get(i));
            }
            this.workingRecipe.outputs.put(FluidRecipeCapability.CAP, trimmed);
        }

        @Override
        protected ActionResult handleRecipeIO(GTRecipe recipe, IO io) {
            if (io != IO.OUT) {
                var handleIO = super.handleRecipeIO(recipe, io);
                if (handleIO.isSuccess()) {
                    updateWorkingRecipe(recipe);
                } else {
                    this.workingRecipe = null;
                }
                return handleIO;
            }

            var items = recipe.getOutputContents(ItemRecipeCapability.CAP);
            if (!items.isEmpty()) {
                Map<RecipeCapability<?>, List<Content>> out = Map.of(ItemRecipeCapability.CAP, items);
                RecipeHelper.handleRecipe(this.machine, recipe, io, out, chanceCaches, false, false);
            }

            if (applyFluidOutputs(recipe, FluidAction.EXECUTE, this.machine.getVoidingMode())) {
                workingRecipe = null;
                return ActionResult.SUCCESS;
            }

            return ActionResult.fail(Component.translatable("gtceu.recipe_logic.insufficient_out")
                    .append(": ")
                    .append(FluidRecipeCapability.CAP.getName()), FluidRecipeCapability.CAP, IO.OUT);
        }

        private boolean applyFluidOutputs(GTRecipe recipe, FluidAction action, VoidingMode voidMode) {
            var fluids = recipe.getOutputContents(FluidRecipeCapability.CAP)
                    .stream()
                    .map(Content::getContent)
                    .map(FluidRecipeCapability.CAP::of)
                    .toList();

            // Distillery recipes should output to the first non-void handler
            if (recipe.recipeType == GTRecipeTypes.DISTILLERY_RECIPES) {
                var fluid = fluids.get(0).getStacks()[0];
                var handler = getMachine().getFirstValid();
                if (handler == null) return false;
                int filled = (handler instanceof NotifiableFluidTank nft) ?
                        nft.fillInternal(fluid, action) :
                        handler.fill(fluid, action);
                return filled == fluid.getAmount();
            }

            boolean valid = true;
            var outputs = getMachine().getFluidOutputs();
            for (int i = 0; i < Math.min(fluids.size(), outputs.size()); ++i) {
                var handler = outputs.get(i);
                var fluid = fluids.get(i).getStacks()[0];
                int filled = (handler instanceof NotifiableFluidTank nft) ?
                        nft.fillInternal(fluid, action) :
                        handler.fill(fluid, action);
                if (filled != fluid.getAmount() && !voidMode.canVoid(FluidRecipeCapability.CAP)) valid = false;
                if (action.simulate() && !valid) break;
            }
            return valid;
        }
    }
}
