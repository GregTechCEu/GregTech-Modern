package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.feature.*;
import com.gregtechceu.gtceu.api.machine.trait.*;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.syncsystem.annotations.SaveField;
import com.gregtechceu.gtceu.syncsystem.annotations.SyncToClient;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.ISubscription;

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
public abstract class WorkableTieredMachine extends TieredEnergyMachine implements IRecipeLogicMachine,
                                            IMachineLife, IMufflableMachine, IOverclockMachine {

    @Getter
    @SaveField
    @SyncToClient
    public final RecipeLogic recipeLogic;
    @Getter
    public final GTRecipeType[] recipeTypes;
    @Getter
    @Setter
    @SaveField
    public int activeRecipeType;
    @Getter
    public final Int2IntFunction tankScalingFunction;
    @Nullable
    @Getter
    @Setter
    private ICleanroomProvider cleanroom;
    @SaveField
    public final NotifiableItemStackHandler importItems;
    @SaveField
    public final NotifiableItemStackHandler exportItems;
    @SaveField
    public final NotifiableFluidTank importFluids;
    @SaveField
    public final NotifiableFluidTank exportFluids;
    @SaveField
    public final NotifiableComputationContainer importComputation;
    @SaveField
    public final NotifiableComputationContainer exportComputation;
    @Getter
    protected final Map<IO, List<RecipeHandlerList>> capabilitiesProxy;
    @Getter
    protected final Map<IO, Map<RecipeCapability<?>, List<IRecipeHandler<?>>>> capabilitiesFlat;
    @SaveField
    @Getter
    protected int overclockTier;
    protected final List<ISubscription> traitSubscriptions;
    @SaveField
    @SyncToClient
    @Getter
    protected boolean isMuffled;
    protected boolean previouslyMuffled = true;

    public static class WorkableTieredMachineTraits extends TieredEnergyMachineTraits {
        public RecipeLogic recipeLogic(WorkableTieredMachine machine) {
            return new RecipeLogic(machine);
        }

        public NotifiableItemStackHandler importItemHandler(WorkableTieredMachine machine) {
            return new NotifiableItemStackHandler(machine, machine.getRecipeType().getMaxInputs(ItemRecipeCapability.CAP), IO.IN);
        }

        public NotifiableItemStackHandler exportItemHandler(WorkableTieredMachine machine) {
            return new NotifiableItemStackHandler(machine, machine.getRecipeType().getMaxOutputs(ItemRecipeCapability.CAP), IO.OUT);
        }

        public NotifiableFluidTank importFluidTank(WorkableTieredMachine machine) {
            return new NotifiableFluidTank(machine, machine.getRecipeType().getMaxInputs(FluidRecipeCapability.CAP),
                    machine.tankScalingFunction.applyAsInt(machine.getTier()), IO.IN);
        }

        public NotifiableFluidTank exportFluidTank(WorkableTieredMachine machine) {
            return new NotifiableFluidTank(machine, machine.getRecipeType().getMaxOutputs(FluidRecipeCapability.CAP),
                    machine.tankScalingFunction.applyAsInt(machine.getTier()), IO.OUT);
        }

        public NotifiableComputationContainer importComputation(WorkableTieredMachine machine) {
            return new NotifiableComputationContainer(machine, IO.IN, true);
        }

        public NotifiableComputationContainer exportComputation(WorkableTieredMachine machine) {
            return new NotifiableComputationContainer(machine, IO.OUT, false);
        }
    }

    public WorkableTieredMachine(IMachineBlockEntity holder, int tier, Int2IntFunction tankScalingFunction, WorkableTieredMachineTraits traits) {
        super(holder, tier, traits);
        this.overclockTier = getMaxOverclockTier();
        this.recipeTypes = getDefinition().getRecipeTypes();
        this.activeRecipeType = 0;
        this.tankScalingFunction = tankScalingFunction;
        this.capabilitiesProxy = new EnumMap<>(IO.class);
        this.capabilitiesFlat = new EnumMap<>(IO.class);
        this.traitSubscriptions = new ArrayList<>();
        this.recipeLogic = traits.recipeLogic(this);
        this.importItems = traits.importItemHandler(this);
        this.exportItems = traits.exportItemHandler(this);
        this.importFluids = traits.importFluidTank(this);
        this.exportFluids = traits.exportFluidTank(this);
        this.importComputation = traits.importComputation(this);
        this.exportComputation = traits.exportComputation(this);
    }

    public WorkableTieredMachine(IMachineBlockEntity holder, int tier, Int2IntFunction tankScalingFunction) {
        this(holder, tier, tankScalingFunction, new WorkableTieredMachineTraits());
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    @Override
    protected NotifiableEnergyContainer createEnergyContainer(Object... args) {
        long tierVoltage = GTValues.V[getTier()];
        if (isEnergyEmitter()) {
            return RecipeAmperageEnergyContainer.makeEmitterContainer(this, tierVoltage * 64L,
                    tierVoltage, getMaxInputOutputAmperage());
        } else {
            return RecipeAmperageEnergyContainer.makeReceiverContainer(this, tierVoltage * 64L,
                    tierVoltage, getMaxInputOutputAmperage());
        }
    }

    protected NotifiableItemStackHandler createImportItemHandler(Object... args) {
        return new NotifiableItemStackHandler(this, getRecipeType().getMaxInputs(ItemRecipeCapability.CAP), IO.IN);
    }

    protected NotifiableItemStackHandler createExportItemHandler(Object... args) {
        return new NotifiableItemStackHandler(this, getRecipeType().getMaxOutputs(ItemRecipeCapability.CAP), IO.OUT);
    }

    protected RecipeLogic createRecipeLogic(Object... args) {
        return new RecipeLogic(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        // attach self traits
        Map<IO, List<IRecipeHandler<?>>> ioTraits = new EnumMap<>(IO.class);

        for (MachineTrait trait : getTraits()) {
            if (trait instanceof IRecipeHandlerTrait<?> handlerTrait) {
                ioTraits.computeIfAbsent(handlerTrait.getHandlerIO(), i -> new ArrayList<>()).add(handlerTrait);
            }
        }

        for (var entry : ioTraits.entrySet()) {
            var handlerList = RecipeHandlerList.of(entry.getKey(), entry.getValue());
            this.addHandlerList(handlerList);
            traitSubscriptions.add(handlerList.subscribe(recipeLogic::updateTickSubscription));
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        traitSubscriptions.forEach(ISubscription::unsubscribe);
        traitSubscriptions.clear();
        capabilitiesProxy.clear();
        capabilitiesFlat.clear();
        recipeLogic.inValid();
    }

    //////////////////////////////////////
    // ********** MISC ***********//
    //////////////////////////////////////

    @Override
    public void onMachineRemoved() {
        clearInventory(importItems.storage);
        clearInventory(exportItems.storage);
    }

    public void setMuffled(boolean muffled) {
        isMuffled = muffled;
        syncDataHolder.markClientSyncFieldDirty("isMuffled");
    }

    //////////////////////////////////////
    // ******** OVERCLOCK *********//
    //////////////////////////////////////

    @Override
    public int getMaxOverclockTier() {
        return GTUtil.getTierByVoltage(Math.max(energyContainer.getInputVoltage(), energyContainer.getOutputVoltage()));
    }

    @Override
    public int getMinOverclockTier() {
        return 0;
    }

    @Override
    public void setOverclockTier(int tier) {
        if (!isRemote() && tier >= getMinOverclockTier() && tier <= getMaxOverclockTier()) {
            this.overclockTier = tier;
            this.recipeLogic.markLastRecipeDirty();
        }
    }

    @Override
    public long getOverclockVoltage() {
        return Math.min(GTValues.V[getOverclockTier()],
                Math.max(energyContainer.getInputVoltage(), energyContainer.getOutputVoltage()));
    }

    //////////////////////////////////////
    // ****** RECIPE LOGIC *******//
    //////////////////////////////////////

    @Override
    public void clientTick() {
        super.clientTick();
        if (previouslyMuffled != isMuffled) {
            previouslyMuffled = isMuffled;

            if (recipeLogic != null)
                recipeLogic.updateSound();
        }
    }

    @Override
    public boolean keepSubscribing() {
        return false;
    }

    @NotNull
    public GTRecipeType getRecipeType() {
        return recipeTypes[activeRecipeType];
    }

    /**
     * Sets a recipe type of the machine.
     * FOR INTERNAL / TESTING USE ONLY!
     * NOT SUPPORTED FOR PRODUCTION USE!
     *
     * @param newType The new recipe type
     */
    @ApiStatus.Internal
    @VisibleForTesting
    public void setRecipeType(GTRecipeType newType) {
        recipeTypes[activeRecipeType] = newType;
    }
}
