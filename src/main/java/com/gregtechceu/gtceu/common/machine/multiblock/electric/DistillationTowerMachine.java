package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.IVoidable;
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
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;
import com.gregtechceu.gtceu.api.recipe.ingredient.OldFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.VoidFluidHandler;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DistillationTowerMachine extends WorkableElectricMultiblockMachine {

    private static final List<RecipeCapability<?>> FLUID_CAP = List.of(FluidRecipeCapability.CAP);
    private static final List<RecipeCapability<?>> EU_CAP = List.of(EURecipeCapability.CAP);
    private static final List<RecipeCapability<?>> EU_AND_FLUID_CAP = List.of(EURecipeCapability.CAP,
            FluidRecipeCapability.CAP);

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
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        IVoidable.attachConfigurators(configuratorPanel, this);

        configuratorPanel.attachConfigurators(new IFancyConfiguratorButton.Toggle(
                GuiTextures.BUTTON_BATCH.getSubTexture(0, 0, 1, 0.5),
                GuiTextures.BUTTON_BATCH.getSubTexture(0, 0.5, 1, 0.5),
                this::isBatchEnabled,
                (cd, p) -> setBatchEnabled(p))
                .setTooltipsSupplier(
                        p -> List.of(
                                Component.translatable("gtceu.machine.batch_" + (p ? "enabled" : "disabled")))));

        configuratorPanel.attachConfigurators(new IFancyConfiguratorButton.Toggle(
                GuiTextures.BUTTON_POWER.getSubTexture(0, 0, 1, 0.5),
                GuiTextures.BUTTON_POWER.getSubTexture(0, 0.5, 1, 0.5),
                this::isWorkingEnabled, (clickData, pressed) -> setWorkingEnabled(pressed))
                .setTooltipsSupplier(pressed -> List.of(
                        Component.translatable(
                                pressed ? "behaviour.soft_hammer.enabled" : "behaviour.soft_hammer.disabled"))));
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

    public static ModifierFunction recipeModifier(MetaMachine machine, RecipeHandlerGroup group,
                                                  GTRecipe recipe) {
        if (!(machine instanceof DistillationTowerMachine tower)) {
            return RecipeModifier.nullWrongType(DistillationTowerMachine.class, machine);
        }

        int maxParallel = tower.getParallelHatch()
                .map(IParallelHatch::getCurrentParallel)
                .orElse(1);
        var parallelResult = tower.getMaxParallel(group, recipe, maxParallel, Collections.emptyList(), FLUID_CAP);
        if (parallelResult.failReason() != null) return ModifierFunction.cancel(parallelResult.failReason());
        int parallel = parallelResult.amount();
        if (parallel == 0) return ModifierFunction.NULL;

        int batch = 1;
        if (tower.isBatchEnabled() && recipe.duration < ConfigHolder.INSTANCE.machines.batchDuration) {
            int desiredBatch = ConfigHolder.INSTANCE.machines.batchDuration / recipe.duration;
            int maxTotalRuns = saturatedMultiply(parallel, desiredBatch);
            var batchResult = tower.getMaxParallel(group, recipe, maxTotalRuns, EU_CAP, EU_AND_FLUID_CAP);
            if (batchResult.failReason() != null) return ModifierFunction.cancel(batchResult.failReason());
            if (batchResult.amount() == 0) return ModifierFunction.NULL;
            batch = Math.max(1, batchResult.amount() / parallel);
        }

        int contentMultiplier = saturatedMultiply(parallel, batch);
        if (contentMultiplier == 1) return ModifierFunction.IDENTITY;
        return ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(contentMultiplier))
                .eutMultiplier(parallel)
                .durationMultiplier(batch)
                .parallels(parallel)
                .batchParallels(batch)
                .build();
    }

    private ParallelResult getMaxParallel(RecipeHandlerGroup group, GTRecipe recipe, int parallelLimit,
                                          List<RecipeCapability<?>> inputCapsToSkip,
                                          List<RecipeCapability<?>> outputCapsToSkip) {
        if (parallelLimit <= 1) return new ParallelResult(parallelLimit, null);

        int maxInput = ParallelLogic.getMaxByInput(group, recipe, parallelLimit, inputCapsToSkip);
        if (maxInput == 0) return new ParallelResult(0, null);

        int maxOutput = ParallelLogic.limitByOutputMerging(group, recipe, maxInput, outputCapsToSkip);
        if (maxOutput == 0) return new ParallelResult(0, null);

        return limitByFluidOutputs(group, recipe, maxOutput);
    }

    private ParallelResult limitByFluidOutputs(RecipeHandlerGroup group, GTRecipe recipe, int parallelLimit) {
        if (parallelLimit <= 0) return new ParallelResult(0, null);

        var fluids = getOutputFluids(recipe);
        if (fluids.isEmpty()) return new ParallelResult(parallelLimit, null);

        var voidMode = getVoidingMode();
        boolean canVoidFluids = voidMode.canVoid(FluidRecipeCapability.CAP);
        boolean distilleryRecipe = recipe.recipeType == GTRecipeTypes.DISTILLERY_RECIPES;
        if (!distilleryRecipe && fluids.size() > fluidOutputs.size() && !canVoidFluids) {
            Component reason = tooManyFluidOutputsReason(fluids.size(), fluidOutputs.size());
            RecipeLogic.putFailureReason(group, recipe, reason);
            return new ParallelResult(0, reason);
        }
        if (!distilleryRecipe && canVoidFluids) return new ParallelResult(parallelLimit, null);

        int max = parallelLimit;
        if (distilleryRecipe) {
            var handler = firstValid;
            if (handler == null) {
                Component reason = insufficientFluidOutputReason();
                RecipeLogic.putFailureReason(group, recipe, reason);
                return new ParallelResult(0, reason);
            }
            max = Math.min(max, getFluidParallelLimit(handler, fluids.get(0), parallelLimit));
        } else {
            int limit = fluids.size();
            for (int i = 0; i < limit; ++i) {
                var handler = fluidOutputs.get(i);
                max = Math.min(max, getFluidParallelLimit(handler, fluids.get(i), parallelLimit));
            }
        }

        if (max == 0 && !canVoidFluids) {
            Component reason = insufficientFluidOutputReason();
            RecipeLogic.putFailureReason(group, recipe, reason);
            return new ParallelResult(0, reason);
        }
        return new ParallelResult(max, null);
    }

    private static int getFluidParallelLimit(IFluidHandler handler, FluidStack fluid, int parallelLimit) {
        if (fluid.isEmpty()) return parallelLimit;
        if (handler == VoidFluidHandler.INSTANCE) return 0;

        long capacity = 0;
        for (int tank = 0; tank < handler.getTanks(); ++tank) {
            FluidStack stored = handler.getFluidInTank(tank);
            if (stored.isEmpty()) {
                if (handler.isFluidValid(tank, fluid)) {
                    capacity += handler.getTankCapacity(tank);
                }
            } else if (stored.isFluidEqual(fluid)) {
                capacity += Math.max(0, handler.getTankCapacity(tank) - stored.getAmount());
            }
            if (capacity >= (long) fluid.getAmount() * parallelLimit) return parallelLimit;
        }
        return (int) Math.min(parallelLimit, capacity / fluid.getAmount());
    }

    private static int saturatedMultiply(int left, int right) {
        if (left <= 0 || right <= 0) return 0;
        if (left > Integer.MAX_VALUE / right) return Integer.MAX_VALUE;
        return left * right;
    }

    private static List<FluidStack> getOutputFluids(GTRecipe recipe) {
        return recipe.getOutputContents(FluidRecipeCapability.CAP)
                .stream()
                .map(Content::getContent)
                .map(FluidRecipeCapability.CAP::of)
                .filter(ingredient -> !ingredient.isEmpty() && ingredient.getFluids().length > 0)
                .map(OldFluidIngredient::getStacks)
                .map(stacks -> stacks[0])
                .toList();
    }

    private static Component insufficientFluidOutputReason() {
        return Component.translatable("gtceu.recipe_logic.insufficient_out")
                .append(": ")
                .append(FluidRecipeCapability.CAP.getName());
    }

    private static Component tooManyFluidOutputsReason(int outputs, int layers) {
        // TODO: add translation
        return Component.translatable("gtceu.recipe_logic.distillation_tower.too_many_fluid_outputs", outputs, layers);
    }

    private record

    ParallelResult(int amount, @Nullable Component failReason) {
    }

    public static class DistillationTowerLogic extends RecipeLogic {

        @Nullable
        @Persisted
        @DescSynced
        GTRecipe workingRecipe = null;

        public DistillationTowerLogic(IRecipeLogicMachine machine) {
            super(machine);
        }

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

            return RecipeHelper.matchTickRecipe(getLastGroup(), recipe);
        }

        @Override
        protected void handleSearchingRecipes(Iterator<GTRecipe> matches) {
            workingRecipe = null;
            super.handleSearchingRecipes(matches);
        }

        private ActionResult matchDTRecipe(GTRecipe recipe) {
            var result = RecipeHelper.handleRecipe(getLastGroup(), recipe, IO.IN, recipe.inputs, false, true);
            if (!result.isSuccess()) return result;

            var items = recipe.getOutputContents(ItemRecipeCapability.CAP);
            if (!items.isEmpty()) {
                Map<RecipeCapability<?>, List<Content>> out = Map.of(ItemRecipeCapability.CAP, items);
                result = RecipeHelper.handleRecipe(getLastGroup(), recipe, IO.OUT, out, false, true);
                if (!result.isSuccess()) return result;
            }

            var fluids = getOutputFluids(recipe);
            var voidMode = machine.getVoidingMode();
            boolean distilleryRecipe = recipe.recipeType == GTRecipeTypes.DISTILLERY_RECIPES;
            if (hasTooManyFluidOutputs(fluids, distilleryRecipe, voidMode)) {
                return ActionResult.fail(tooManyFluidOutputsReason(fluids.size(), getMachine().getFluidOutputs().size()),
                        FluidRecipeCapability.CAP, IO.OUT);
            }
            if (!applyFluidOutputs(fluids, distilleryRecipe, FluidAction.SIMULATE, voidMode)) {
                return ActionResult.fail(insufficientFluidOutputReason(), FluidRecipeCapability.CAP, IO.OUT);
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
                var result = RecipeHelper.handleRecipe(getLastGroup(), recipe, io, out, false, false);
                if (!result.isSuccess()) return result;
            }

            var fluids = getOutputFluids(recipe);
            var voidMode = this.machine.getVoidingMode();
            boolean distilleryRecipe = recipe.recipeType == GTRecipeTypes.DISTILLERY_RECIPES;
            if (hasTooManyFluidOutputs(fluids, distilleryRecipe, voidMode)) {
                return ActionResult.fail(tooManyFluidOutputsReason(fluids.size(), getMachine().getFluidOutputs().size()),
                        FluidRecipeCapability.CAP, IO.OUT);
            }
            if (applyFluidOutputs(fluids, distilleryRecipe, FluidAction.EXECUTE, voidMode)) {
                workingRecipe = null;
                return ActionResult.SUCCESS;
            }

            return ActionResult.fail(insufficientFluidOutputReason(), FluidRecipeCapability.CAP, IO.OUT);
        }

        private boolean applyFluidOutputs(List<FluidStack> fluids, boolean distilleryRecipe, FluidAction action,
                                          VoidingMode voidMode) {
            // Distillery recipes should output to the first non-void handler
            if (distilleryRecipe) {
                if (fluids.isEmpty()) {
                    return true;
                }
                var fluid = fluids.get(0);
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
                var fluid = fluids.get(i);
                int filled = (handler instanceof NotifiableFluidTank nft) ?
                        nft.fillInternal(fluid, action) :
                        handler.fill(fluid, action);
                if (filled != fluid.getAmount() && !voidMode.canVoid(FluidRecipeCapability.CAP)) valid = false;
                if (action.simulate() && !valid) break;
            }
            return valid;
        }

        private boolean hasTooManyFluidOutputs(List<FluidStack> fluids, boolean distilleryRecipe, VoidingMode voidMode) {
            return !distilleryRecipe && fluids.size() > getMachine().getFluidOutputs().size() &&
                    !voidMode.canVoid(FluidRecipeCapability.CAP);
        }
    }
}
