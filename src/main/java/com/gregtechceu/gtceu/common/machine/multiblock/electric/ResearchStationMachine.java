package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.capability.IObjectHolder;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationHatch;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationReceiver;
import com.gregtechceu.gtceu.api.capability.recipe.CWURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.common.machine.trait.computation.ComputationRecipeLogic;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ResearchStationMachine extends WorkableElectricMultiblockMachine implements IOpticalComputationReceiver {
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
        // capture all energy containers
        List<IOpticalComputationHatch> providers = new ArrayList<>();
        List<IObjectHolder> holders = new ArrayList<>();
        for (IMultiPart part : getParts()) {
            if (part instanceof IOpticalComputationHatch hatch) providers.add(hatch);
            if (part instanceof IObjectHolder objectHolder) holders.add(objectHolder);
        }
        if (!providers.isEmpty()) {
            computationProvider = providers.get(0);
        }
        if (!holders.isEmpty()) {
            objectHolder = holders.get(0);
            // cannot set in initializeAbilities since super() calls it before setting the objectHolder field here
            this.getCapabilitiesProxy().put(IO.IN, ItemRecipeCapability.CAP, Collections.singletonList(objectHolder.getAsHandler()));
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
        List<IObjectHolder> holders = new ArrayList<>();
        for (IMultiPart part : getParts()) {
            if (part instanceof IObjectHolder objectHolder) holders.add(objectHolder);
        }
        if (holders.size() >= 1 && holders.get(0) == objectHolder) {
            objectHolder.setLocked(false);
        }
        objectHolder = null;
        super.onStructureInvalid();
    }

    private static class ResearchStationRecipeLogic extends ComputationRecipeLogic {

        public ResearchStationRecipeLogic(ResearchStationMachine metaTileEntity) {
            super(metaTileEntity, ComputationType.SPORADIC);
        }

        @NotNull
        @Override
        public ResearchStationMachine getMachine() {
            return (ResearchStationMachine) super.getMachine();
        }

        @Override
        protected boolean checkMatchedRecipeAvailable(GTRecipe match) {
            var modified = machine.fullModifyRecipe(match);
            if (modified != null) {
                if (!modified.inputs.containsKey(CWURecipeCapability.CAP) && !modified.tickInputs.containsKey(CWURecipeCapability.CAP)) {
                    return true;
                }
                IOpticalComputationProvider provider = getComputationProvider();
                int recipeCWUt = CWURecipeCapability.CAP.of(modified.inputs.containsKey(CWURecipeCapability.CAP) ? modified.getInputContents(CWURecipeCapability.CAP).get(0).content : modified.getTickInputContents(CWURecipeCapability.CAP).get(0).content);
                var thing = provider.requestCWUt(recipeCWUt, true) >= recipeCWUt;
                if (!thing) {
                    return false;
                }

                // skip "can fit" checks, it can always fit
                if (modified.checkConditions(this).isSuccess() &&
                        this.matchRecipeNoOutput(modified, machine).isSuccess() &&
                        this.matchTickRecipeNoOutput(modified, machine).isSuccess()) {
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

        public GTRecipe.ActionResult matchRecipeNoOutput(GTRecipe recipe, IRecipeCapabilityHolder holder) {
            if (!holder.hasProxies()) return GTRecipe.ActionResult.FAIL_NO_REASON;
            var result = recipe.matchRecipe(IO.IN, holder, recipe.inputs, false);
            if (!result.isSuccess()) return result;
            return GTRecipe.ActionResult.SUCCESS;
        }

        public GTRecipe.ActionResult matchTickRecipeNoOutput(GTRecipe recipe, IRecipeCapabilityHolder holder) {
            if (recipe.hasTick()) {
                if (!holder.hasProxies()) return GTRecipe.ActionResult.FAIL_NO_REASON;
                var result = recipe.matchRecipe(IO.IN, holder, recipe.tickInputs, false);
                if (!result.isSuccess()) return result;
            }
            return GTRecipe.ActionResult.SUCCESS;
        }

        // lock the object holder on recipe start
        @Override
        public void setupRecipe(GTRecipe recipe) {
            IObjectHolder holder = getMachine().getObjectHolder();
            holder.setLocked(true);

            if (handleFuelRecipe()) {
                machine.beforeWorking();
                recipe.preWorking(this.machine);

                // do not consume inputs here, consume them on completion
                recipeDirty = false;
                lastRecipe = recipe;
                setStatus(Status.WORKING);
                progress = 0;
                duration = recipe.duration;
            }

            this.recipeCWUt = recipe.getTickInputContents(CWURecipeCapability.CAP).stream().map(Content::getContent).map(CWURecipeCapability.CAP::of).reduce(0, Integer::sum);
            this.isDurationTotalCWU = !recipe.getInputContents(CWURecipeCapability.CAP).isEmpty();
        }

        // "replace" the items in the slots rather than outputting elsewhere
        // unlock the object holder
        @Override
        public void onRecipeFinish() {
            super.onRecipeFinish();
            IObjectHolder holder = getMachine().getObjectHolder();
            holder.setHeldItem(ItemStack.EMPTY);

            ItemStack outputItem = ItemStack.EMPTY;
            if (lastRecipe.getOutputContents(ItemRecipeCapability.CAP).size() >= 1) {
                outputItem = ItemRecipeCapability.CAP.of(getLastRecipe().getOutputContents(ItemRecipeCapability.CAP).get(0).content).getItems()[0];
            }
            holder.setDataItem(outputItem);
            holder.setLocked(false);
        }
    }
}
