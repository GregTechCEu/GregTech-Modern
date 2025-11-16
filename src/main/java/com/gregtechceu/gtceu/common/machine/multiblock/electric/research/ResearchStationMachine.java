package com.gregtechceu.gtceu.common.machine.multiblock.electric.research;

import com.gregtechceu.gtceu.api.capability.IObjectHolder;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationReceiver;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.capability.recipe.CWURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ResearchStationMachine extends WorkableElectricMultiblockMachine
                                    implements IOpticalComputationReceiver, IDisplayUIMachine {

    @Getter
    private IOpticalComputationProvider computationProvider;
    @Getter
    private IObjectHolder objectHolder;

    public ResearchStationMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new ResearchStationRecipeLogic(this);
    }

    @Override
    public ResearchStationRecipeLogic getRecipeLogic() {
        return (ResearchStationRecipeLogic) super.getRecipeLogic();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        for (IMultiPart part : getParts()) {
            if (part instanceof IObjectHolder iObjectHolder) {
                if (iObjectHolder.getFrontFacing() != getFrontFacing().getOpposite()) {
                    onStructureInvalid();
                    return;
                }
                this.objectHolder = iObjectHolder;
            }

            part.self().holder.self()
                    .getCapability(GTCapability.CAPABILITY_COMPUTATION_PROVIDER)
                    .ifPresent(provider -> this.computationProvider = provider);
        }

        // should never happen, but would rather do this than have an obscure NPE
        if (computationProvider == null || objectHolder == null) {
            onStructureInvalid();
        }
    }

    @Override
    public boolean checkPattern() {
        boolean isFormed = super.checkPattern();
        if (isFormed && objectHolder != null && objectHolder.getFrontFacing() != getFrontFacing().getOpposite()) {
            onStructureInvalid();
        }
        return isFormed;
    }

    @Override
    public void onStructureInvalid() {
        computationProvider = null;
        // recheck the ability to make sure it wasn't the one broken
        for (IMultiPart part : getParts()) {
            if (part instanceof IObjectHolder holder) {
                if (holder == objectHolder) {
                    objectHolder.setLocked(false);
                }
            }
        }
        objectHolder = null;
        super.onStructureInvalid();
    }

    @Override
    public boolean regressWhenWaiting() {
        return false;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive())
                .setWorkingStatusKeys("gtceu.multiblock.idling", "gtceu.multiblock.work_paused",
                        "gtceu.multiblock.research_station.researching")
                .addEnergyUsageLine(energyContainer)
                .addEnergyTierLine(tier)
                .addWorkingStatusLine()
                // .addComputationUsageExactLine(computationProvider.getMaxCWUt()) // TODO: (Onion)
                .addProgressLineOnlyPercent(recipeLogic.getProgressPercent());
    }

    public static class ResearchStationRecipeLogic extends RecipeLogic {

        public ResearchStationRecipeLogic(ResearchStationMachine metaTileEntity) {
            super(metaTileEntity);
        }

        @NotNull
        @Override
        public ResearchStationMachine getMachine() {
            return (ResearchStationMachine) super.getMachine();
        }

        // skip "can fit" checks, it can always fit
        @Override
        protected ActionResult matchRecipe(GTRecipe recipe) {
            var match = matchRecipeNoOutput(recipe);
            if (!match.isSuccess()) return match;

            return matchTickRecipeNoOutput(recipe);
        }

        @Override
        public boolean checkMatchedRecipeAvailable(GTRecipe match) {
            var modified = machine.fullModifyRecipe(match);
            if (modified != null) {
                // What is the point of this
                if (!modified.inputs.containsKey(CWURecipeCapability.CAP) &&
                        !modified.tickInputs.containsKey(CWURecipeCapability.CAP)) {
                    return true;
                }
                var recipeMatch = checkRecipe(modified);
                if (recipeMatch.isSuccess()) {
                    setupRecipe(modified);
                } else {
                    setWaiting(recipeMatch.reason());
                }
                if (lastRecipe != null && getStatus() == Status.WORKING) {
                    lastOriginRecipe = match;
                    lastFailedMatches = null;
                    return true;
                }
            }
            return false;
        }

        protected ActionResult matchRecipeNoOutput(GTRecipe recipe) {
            if (!machine.hasCapabilityProxies()) return ActionResult.FAIL_NO_CAPABILITIES;
            return RecipeHelper.handleRecipe(machine, recipe, IO.IN, recipe.inputs, Collections.emptyMap(), false,
                    true);
        }

        protected ActionResult matchTickRecipeNoOutput(GTRecipe recipe) {
            if (recipe.hasTick()) {
                if (!machine.hasCapabilityProxies()) return ActionResult.FAIL_NO_CAPABILITIES;
                return RecipeHelper.handleRecipe(machine, recipe, IO.IN, recipe.tickInputs, Collections.emptyMap(),
                        false, true);
            }
            return ActionResult.SUCCESS;
        }

        // Handle RecipeIO manually
        @Override
        protected ActionResult handleRecipeIO(GTRecipe recipe, IO io) {
            if (io == IO.IN) {
                // lock the object holder on recipe start
                IObjectHolder holder = getMachine().getObjectHolder();
                holder.setLocked(true);
                return ActionResult.SUCCESS;
            }

            // "replace" the items in the slots rather than outputting elsewhere
            // unlock the object holder
            IObjectHolder holder = getMachine().getObjectHolder();
            if (lastRecipe == null) {
                holder.setLocked(false);
                return ActionResult.SUCCESS;
            }

            holder.setHeldItem(ItemStack.EMPTY);
            ItemStack outputItem = ItemStack.EMPTY;
            var contents = lastRecipe.getOutputContents(ItemRecipeCapability.CAP);
            if (!contents.isEmpty()) {
                outputItem = ItemRecipeCapability.CAP.of(contents.get(0).content).getItems()[0];
            }
            if (!outputItem.isEmpty()) {
                holder.setDataItem(outputItem.copy());
            }
            holder.setLocked(false);
            return ActionResult.SUCCESS;
        }

        @Override
        protected ActionResult handleTickRecipeIO(GTRecipe recipe, IO io) {
            if (io != IO.OUT) {
                return super.handleTickRecipeIO(recipe, io);
            }
            return ActionResult.SUCCESS;
        }
    }
}
