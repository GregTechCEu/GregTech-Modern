package com.gregtechceu.gtceu.common.machine.multiblock.electric.research;

import com.gregtechceu.gtceu.api.capability.IObjectHolder;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationReceiver;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.capability.recipe.CWURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;
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
            IOpticalComputationProvider provider = part.self().holder.self()
                    .getCapability(GTCapability.CAPABILITY_COMPUTATION_PROVIDER).resolve().orElse(null);
            if (provider != null) {
                this.computationProvider = provider;
            }
            if (part instanceof IObjectHolder objectHolder) {
                this.getCapabilitiesProxy().put(IO.IN,
                        List.of(RecipeHandlerList.of(IO.IN, objectHolder.getAsHandler())));
            }
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
    public boolean dampingWhenWaiting() {
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

    private static class ResearchStationRecipeLogic extends RecipeLogic {

        public ResearchStationRecipeLogic(ResearchStationMachine metaTileEntity) {
            super(metaTileEntity);
        }

        @NotNull
        @Override
        public ResearchStationMachine getMachine() {
            return (ResearchStationMachine) super.getMachine();
        }

        // Custom recipe matching logic to override output space test
        @Nullable
        @Override
        public Iterator<GTRecipe> searchRecipe() {
            IRecipeCapabilityHolder holder = this.machine;
            if (!holder.hasCapabilityProxies()) return null;
            var iterator = machine.getRecipeType().getLookup().getRecipeIterator(holder, recipe -> {
                if (!holder.hasCapabilityProxies()) return false;
                var result = RecipeHelper.handleRecipe(IO.IN, holder, recipe, recipe.inputs, Collections.emptyMap(),
                        false, false);
                if (!result.isSuccess()) return false;
                if (recipe.hasTick()) {
                    result = RecipeHelper.handleRecipe(IO.IN, holder, recipe, recipe.tickInputs,
                            Collections.emptyMap(), false, false);
                    return result.isSuccess();
                }
                return true;
            });
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

            for (GTRecipeType.ICustomRecipeLogic logic : machine.getRecipeType().getCustomRecipeLogicRunners()) {
                GTRecipe recipe = logic.createCustomRecipe(holder);
                if (recipe != null) return Collections.singleton(recipe).iterator();
            }
            return Collections.emptyIterator();
        }

        @Override
        public boolean checkMatchedRecipeAvailable(GTRecipe match) {
            var modified = machine.fullModifyRecipe(match);
            if (modified != null) {
                if (!modified.inputs.containsKey(CWURecipeCapability.CAP) &&
                        !modified.tickInputs.containsKey(CWURecipeCapability.CAP)) {
                    return true;
                }
                // skip "can fit" checks, it can always fit
                var conditions = RecipeHelper.checkConditions(modified, this).stream().filter(v -> !v.isSuccess())
                        .findFirst();
                if (conditions.isEmpty() &&
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

        public RecipeHelper.ActionResult matchRecipeNoOutput(GTRecipe recipe, IRecipeCapabilityHolder holder) {
            if (!holder.hasCapabilityProxies()) return RecipeHelper.ActionResult.FAIL_NO_CAPABILITIES;
            return RecipeHelper.handleRecipe(IO.IN, holder, recipe, recipe.inputs, Collections.emptyMap(), false,
                    true);
        }

        public RecipeHelper.ActionResult matchTickRecipeNoOutput(GTRecipe recipe, IRecipeCapabilityHolder holder) {
            if (recipe.hasTick()) {
                if (!holder.hasCapabilityProxies())
                    return RecipeHelper.ActionResult.FAIL_NO_CAPABILITIES;
                return RecipeHelper.handleRecipe(IO.IN, holder, recipe, recipe.tickInputs, Collections.emptyMap(),
                        false, true);
            }
            return RecipeHelper.ActionResult.SUCCESS;
        }

        @Override
        public void setupRecipe(GTRecipe recipe) {
            // lock the object holder on recipe start
            IObjectHolder holder = getMachine().getObjectHolder();
            holder.setLocked(true);

            // Do RecipeLogic#setupRecipe but without any i/o
            if (!machine.beforeWorking(recipe)) {
                return;
            }
            RecipeHelper.preWorking(this.machine, recipe);

            // do not consume inputs here, consume them on completion
            recipeDirty = false;
            lastRecipe = recipe;
            setStatus(Status.WORKING);
            progress = 0;
            duration = recipe.duration;
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
                outputItem = ItemRecipeCapability.CAP
                        .of(getLastRecipe().getOutputContents(ItemRecipeCapability.CAP).get(0).content).getItems()[0];
            }
            holder.setDataItem(outputItem);
            holder.setLocked(false);
        }

        @Override
        protected RecipeHelper.ActionResult handleRecipeIO(GTRecipe recipe, IO io) {
            if (io != IO.OUT) {
                return super.handleRecipeIO(recipe, io);
            }
            return RecipeHelper.ActionResult.SUCCESS;
        }

        @Override
        protected RecipeHelper.ActionResult handleTickRecipeIO(GTRecipe recipe, IO io) {
            if (io != IO.OUT) {
                return super.handleTickRecipeIO(recipe, io);
            }
            return RecipeHelper.ActionResult.SUCCESS;
        }
    }
}
