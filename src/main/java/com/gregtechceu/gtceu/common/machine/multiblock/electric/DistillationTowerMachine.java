package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

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
        getDefinition().setPartSorter(Comparator.comparingInt(p -> p.self().getPos().getY()));
        getDefinition().setAllowExtendedFacing(false);
        super.onStructureFormed();
        var parts = getParts().stream()
                .filter(part -> PartAbility.EXPORT_FLUIDS.isApplicable(part.self().getBlockState().getBlock()))
                .toList();

        if (!parts.isEmpty()) {
            // Loop from controller y + offset -> highest output hatch
            int y = getPos().getY() + yOffset;
            int maxY = parts.get(parts.size() - 1).self().getPos().getY();
            fluidOutputs = new ObjectArrayList<>(maxY - y);
            for (int outputIndex = 0; y <= maxY; ++y) {
                if (parts.size() <= outputIndex) {
                    fluidOutputs.add(VoidFluidHandler.INSTANCE);
                    continue;
                }

                var part = parts.get(outputIndex);
                if (part.self().getPos().getY() == y) {
                    part.getRecipeHandlers().stream()
                            .filter(IFluidHandler.class::isInstance)
                            .findFirst()
                            .ifPresentOrElse(h -> {
                                fluidOutputs.add((IFluidHandler) h);
                                if (firstValid == null) firstValid = (IFluidHandler) h;
                            },
                                    () -> fluidOutputs.add(VoidFluidHandler.INSTANCE));
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
        }
    }

    @Override
    public void onStructureInvalid() {
        fluidOutputs = null;
        firstValid = null;
        super.onStructureInvalid();
    }

    public int limitParallel(GTRecipe recipe, int multiplier) {
        int minMultiplier = 0;
        int maxMultiplier = multiplier;

        var maxAmount = recipe.getOutputContents(FluidRecipeCapability.CAP).stream()
                .map(Content::getContent)
                .map(FluidRecipeCapability.CAP::of)
                .filter(i -> !i.isEmpty())
                .map(i -> i.getStacks()[0])
                .mapToInt(FluidStack::getAmount)
                .max()
                .orElse(0);

        if (maxAmount == 0) return multiplier;

        while (minMultiplier != maxMultiplier) {
            if (multiplier > Integer.MAX_VALUE / maxAmount) multiplier = Integer.MAX_VALUE / maxAmount;
            GTRecipe copy = recipe.copy(ContentModifier.multiplier(multiplier), false);
            boolean filled = getRecipeLogic().applyFluidOutputs(copy, FluidAction.SIMULATE);
            int[] bin = ParallelLogic.adjustMultiplier(filled, minMultiplier, multiplier, maxMultiplier);
            minMultiplier = bin[0];
            multiplier = bin[1];
            maxMultiplier = bin[2];
        }
        return multiplier;
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
        @Nullable
        public Iterator<GTRecipe> searchRecipe() {
            var recipeType = machine.getRecipeType();
            if (recipeType == GTRecipeTypes.DISTILLERY_RECIPES) return super.searchRecipe();

            // Do recipe searching ourselves so we can match the outputs how we want
            IRecipeCapabilityHolder holder = this.machine;
            if (!holder.hasProxies()) return null;
            var iterator = recipeType.getLookup().getRecipeIterator(holder, recipe -> !recipe.isFuel &&
                    this.matchDTRecipe(recipe, holder).isSuccess() && recipe.matchTickRecipe(holder).isSuccess());

            boolean any = false;
            while (iterator.hasNext()) {
                GTRecipe recipe = iterator.next();
                if (recipe == null) continue;
                any = true;
                break;
            }

            if (any) {
                iterator.reset();
                return iterator;
            }

            for (GTRecipeType.ICustomRecipeLogic logic : recipeType.getCustomRecipeLogicRunners()) {
                GTRecipe recipe = logic.createCustomRecipe(holder);
                if (recipe != null) return Collections.singleton(recipe).iterator();
            }
            return Collections.emptyIterator();
        }

        @Override
        public void findAndHandleRecipe() {
            lastFailedMatches = null;
            if (!recipeDirty && lastRecipe != null &&
                    matchDTRecipe(lastRecipe, this.machine).isSuccess() &&
                    lastRecipe.matchTickRecipe(this.machine).isSuccess() &&
                    lastRecipe.checkConditions(this).isSuccess()) {
                var recipe = lastRecipe;
                lastRecipe = null;
                lastOriginRecipe = null;
                setupRecipe(recipe);
            } else {
                workingRecipe = null;
                lastRecipe = null;
                lastOriginRecipe = null;
                handleSearchingRecipes(searchRecipe());
            }
        }

        @Override
        public boolean checkMatchedRecipeAvailable(GTRecipe match) {
            var matchCopy = match.copy();
            var modified = machine.fullModifyRecipe(matchCopy, ocParams, ocResult);
            if (modified != null) {
                if (modified.checkConditions(this).isSuccess() &&
                        matchDTRecipe(modified, machine).isSuccess() &&
                        modified.matchTickRecipe(machine).isSuccess()) {
                    setupRecipe(modified);
                }
                if (lastRecipe != null && getStatus() == Status.WORKING) {
                    lastOriginRecipe = match;
                    lastFailedMatches = null;
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onRecipeFinish() {
            machine.afterWorking();
            if (lastRecipe != null) {
                lastRecipe.postWorking(machine);
                handleRecipeIO(lastRecipe, IO.OUT);
                if (machine.alwaysTryModifyRecipe()) {
                    if (lastOriginRecipe != null) {
                        var modified = machine.fullModifyRecipe(lastOriginRecipe.copy(), ocParams, ocResult);
                        if (modified == null) markLastRecipeDirty();
                        else lastRecipe = modified;
                    } else {
                        markLastRecipeDirty();
                    }
                }

                if (!recipeDirty && !suspendAfterFinish &&
                        matchDTRecipe(lastRecipe, this.machine).isSuccess() &&
                        lastRecipe.matchTickRecipe(this.machine).isSuccess() &&
                        lastRecipe.checkConditions(this).isSuccess()) {
                    setupRecipe(lastRecipe);
                } else {
                    if (suspendAfterFinish) {
                        setStatus(Status.SUSPEND);
                        suspendAfterFinish = false;
                    } else {
                        setStatus(Status.IDLE);
                    }
                    progress = 0;
                    duration = 0;
                    isActive = false;
                }
            }
        }

        private GTRecipe.ActionResult matchDTRecipe(GTRecipe recipe, IRecipeCapabilityHolder holder) {
            var result = recipe.matchRecipeContents(IO.IN, holder, recipe.inputs, false);
            if (!result.isSuccess()) return result;

            var items = recipe.getOutputContents(ItemRecipeCapability.CAP);
            if (!items.isEmpty()) {
                Map<RecipeCapability<?>, List<Content>> out = Map.of(ItemRecipeCapability.CAP, items);
                result = recipe.matchRecipeContents(IO.OUT, holder, out, false);
                if (!result.isSuccess()) return result;
            }

            if (!applyFluidOutputs(recipe, FluidAction.SIMULATE)) {
                return GTRecipe.ActionResult.fail(() -> Component.translatable("gtceu.recipe_logic.insufficient_out")
                        .append(": ")
                        .append(FluidRecipeCapability.CAP.getName()));
            }

            return GTRecipe.ActionResult.SUCCESS;
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
        protected boolean handleRecipeIO(GTRecipe recipe, IO io) {
            if (io != IO.OUT) {
                if (super.handleRecipeIO(recipe, io)) {
                    updateWorkingRecipe(recipe);
                    return true;
                }
                this.workingRecipe = null;
                return false;
            }
            var items = recipe.getOutputContents(ItemRecipeCapability.CAP);
            if (!items.isEmpty()) {
                Map<RecipeCapability<?>, List<Content>> out = Map.of(ItemRecipeCapability.CAP, items);
                recipe.handleRecipe(io, this.machine, false, out, chanceCaches);
            }
            return applyFluidOutputs(recipe, FluidAction.EXECUTE);
        }

        private boolean applyFluidOutputs(GTRecipe recipe, FluidAction action) {
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
                if (filled != fluid.getAmount()) valid = false;
                if (action.simulate() && !valid) break;
            }
            return valid;
        }
    }
}
