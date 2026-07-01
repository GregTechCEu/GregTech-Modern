package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.feature.*;
import com.gregtechceu.gtceu.api.machine.trait.*;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerList;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class RecipeTieredMachine extends WorkableTieredMachine implements IRecipeLogicMachine,
                                        IMachineLife, IMufflableMachine, IOverclockMachine {

    @Getter
    public final RecipeLogic recipeLogic;
    @Getter
    public final GTRecipeType[] recipeTypes;
    @Getter
    @Setter
    @Persisted
    public int activeRecipeType;
    @Getter
    public final Int2IntFunction tankScalingFunction;
    @Nullable
    @Getter
    @Setter
    private ICleanroomProvider cleanroom;
    @Persisted
    public final NotifiableItemStackHandler importItems;
    @Persisted
    public final NotifiableItemStackHandler exportItems;
    @Persisted
    public final NotifiableFluidTank importFluids;
    @Persisted
    public final NotifiableFluidTank exportFluids;
    @Getter
    protected RecipeHandlerList recipeHandlerList;
    @Persisted
    @Getter
    protected int overclockTier;
    protected final List<ISubscription> traitSubscriptions;
    @Persisted
    @DescSynced
    @Getter
    @Setter
    protected boolean isMuffled;
    protected boolean previouslyMuffled = true;

    public RecipeTieredMachine(IMachineBlockEntity holder, int tier, Int2IntFunction tankScalingFunction,
                               Object... args) {
        super(holder, tier, args);
        this.overclockTier = tier;
        this.recipeLogic = (RecipeLogic) this.workLogic;
        this.recipeTypes = getDefinition().getRecipeTypes();
        this.activeRecipeType = 0;
        this.tankScalingFunction = tankScalingFunction;

        this.traitSubscriptions = new ArrayList<>();
        this.importItems = createImportItemHandler(args);
        this.exportItems = createExportItemHandler(args);
        this.importFluids = createImportFluidHandler(args);
        this.exportFluids = createExportFluidHandler(args);
    }

    protected NotifiableItemStackHandler createImportItemHandler(Object... args) {
        return new NotifiableItemStackHandler(this, getRecipeType().getMaxInputs(ItemRecipeCapability.CAP), IO.IN);
    }

    protected NotifiableItemStackHandler createExportItemHandler(Object... args) {
        return new NotifiableItemStackHandler(this, getRecipeType().getMaxOutputs(ItemRecipeCapability.CAP), IO.OUT);
    }

    protected NotifiableFluidTank createImportFluidHandler(Object... args) {
        return new NotifiableFluidTank(this, getRecipeType().getMaxInputs(FluidRecipeCapability.CAP),
                this.tankScalingFunction.applyAsInt(this.getTier()), IO.IN);
    }

    protected NotifiableFluidTank createExportFluidHandler(Object... args) {
        return new NotifiableFluidTank(this, getRecipeType().getMaxOutputs(FluidRecipeCapability.CAP),
                this.tankScalingFunction.applyAsInt(this.getTier()), IO.OUT);
    }

    protected NetworkedComputationContainer createImportComputationContainer(Object... args) {
        return new NetworkedComputationContainer(this, IO.IN);
    }

    protected NetworkedComputationContainer createExportComputationContainer(Object... args) {
        return new NetworkedComputationContainer(this, IO.OUT);
    }

    @Override
    protected RecipeLogic createWorkLogic(Object... args) {
        return createRecipeLogic();
    }

    protected RecipeLogic createRecipeLogic() {
        return new RecipeLogic(this);
    }

    @Override
    public final void serverRunningTick() {}

    @Override
    public @NotNull List<RecipeHandlerList> getRecipeHandlerLists() {
        return List.of(recipeHandlerList);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        List<IRecipeHandler<?>> list = new ArrayList<>();
        for (MachineTrait trait : getTraits()) {
            if (trait instanceof IRecipeHandler<?> handlerTrait) {
                list.add(handlerTrait);
            }
        }
        recipeHandlerList = RecipeHandlerList.of(list);
        traitSubscriptions.add(recipeHandlerList.subscribe(recipeLogic::updateTickSubscription));
    }

    @Override
    public void onUnload() {
        super.onUnload();
        traitSubscriptions.forEach(ISubscription::unsubscribe);
        traitSubscriptions.clear();
        recipeHandlerList = null;
        recipeLogic.inValid();
    }

    @Override
    public void onMachineRemoved() {
        clearInventory(importItems.storage);
        clearInventory(exportItems.storage);
    }

    @Override
    public void setOverclockTier(int tier) {
        if (!isRemote() && tier >= 0 && tier <= getTier()) {
            this.overclockTier = tier;
            this.recipeLogic.markLastRecipeDirty();
        }
    }

    @Override
    public boolean alwaysTryModifyRecipe() {
        return false;
    }

    @Override
    public long getOverclockVoltage() {
        return Math.min(GTValues.V[getOverclockTier()],
                Math.max(energyContainer.getInputVoltage(), energyContainer.getOutputVoltage()));
    }

    @Override
    public void clientTick() {
        super.clientTick();
        if (previouslyMuffled != isMuffled) {
            previouslyMuffled = isMuffled;

            if (recipeLogic != null)
                recipeLogic.updateSound();
        }
    }

    @NotNull
    public GTRecipeType getRecipeType() {
        return recipeTypes[activeRecipeType];
    }

    @ApiStatus.Internal
    @VisibleForTesting
    public void setRecipeType(GTRecipeType newType) {
        recipeTypes[activeRecipeType] = newType;
    }

    @Override
    public int getProgress() {
        return getRecipeLogic().getProgress();
    }

    @Override
    public int getMaxProgress() {
        return getRecipeLogic().getMaxProgress();
    }
}
