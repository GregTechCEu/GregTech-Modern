package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.feature.*;
import com.gregtechceu.gtceu.api.machine.trait.*;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.machine.trait.CleanroomReceiverTrait;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.ISubscription;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.*;

public abstract class WorkableTieredMachine extends TieredEnergyMachine implements IRecipeLogicMachine,
                                            IMufflableMachine, IOverclockMachine {

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
    protected final CleanroomReceiverTrait cleanroomReceiver;
    @SaveField
    public final NotifiableItemStackHandler importItems, exportItems;
    @SaveField
    public final NotifiableFluidTank importFluids, exportFluids;
    @SaveField
    public final NotifiableComputationContainer importComputation, exportComputation;
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

    public WorkableTieredMachine(BlockEntityCreationInfo info, int tier,
                                 RecipeLogic recipeLogic, int importSlots,
                                 int exportSlots,
                                 int fluidImportSlots, int fluidExportSlots, Int2IntFunction tankScalingFunction) {
        super(info, tier);
        this.overclockTier = getMaxOverclockTier();
        this.recipeTypes = getDefinition().getRecipeTypes();
        this.activeRecipeType = 0;
        this.capabilitiesProxy = new EnumMap<>(IO.class);
        this.capabilitiesFlat = new EnumMap<>(IO.class);
        this.traitSubscriptions = new ArrayList<>();
        this.cleanroomReceiver = attachTrait(new CleanroomReceiverTrait());
        this.recipeLogic = attachTrait(recipeLogic);
        this.importItems = attachTrait(new NotifiableItemStackHandler(importSlots, IO.IN, IO.BOTH));
        this.exportItems = attachTrait(new NotifiableItemStackHandler(exportSlots, IO.OUT));
        this.importFluids = attachTrait(
                new NotifiableFluidTank(fluidImportSlots, tankScalingFunction.applyAsInt(getTier()),
                        IO.IN, IO.BOTH));
        this.exportFluids = attachTrait(
                new NotifiableFluidTank(fluidExportSlots, tankScalingFunction.applyAsInt(getTier()),
                        IO.OUT));
        this.importComputation = attachTrait(new NotifiableComputationContainer(IO.IN, true));
        this.exportComputation = attachTrait(new NotifiableComputationContainer(IO.OUT, false));
    }

    public WorkableTieredMachine(BlockEntityCreationInfo info, int tier, Int2IntFunction tankScalingFunction) {
        super(info, tier);
        this.overclockTier = getMaxOverclockTier();
        this.recipeTypes = getDefinition().getRecipeTypes();
        this.activeRecipeType = 0;
        this.capabilitiesProxy = new EnumMap<>(IO.class);
        this.capabilitiesFlat = new EnumMap<>(IO.class);
        this.traitSubscriptions = new ArrayList<>();
        this.cleanroomReceiver = attachTrait(new CleanroomReceiverTrait());
        this.recipeLogic = attachTrait(new RecipeLogic());
        this.importItems = attachTrait(
                new NotifiableItemStackHandler(getRecipeType().getMaxInputs(ItemRecipeCapability.CAP),
                        IO.IN));
        this.exportItems = attachTrait(
                new NotifiableItemStackHandler(getRecipeType().getMaxOutputs(ItemRecipeCapability.CAP),
                        IO.OUT));
        this.importFluids = attachTrait(new NotifiableFluidTank(getRecipeType().getMaxInputs(FluidRecipeCapability.CAP),
                tankScalingFunction.applyAsInt(getTier()), IO.IN));
        this.exportFluids = attachTrait(
                new NotifiableFluidTank(getRecipeType().getMaxOutputs(FluidRecipeCapability.CAP),
                        tankScalingFunction.applyAsInt(getTier()), IO.OUT));
        this.importComputation = attachTrait(new NotifiableComputationContainer(IO.IN, true));
        this.exportComputation = attachTrait(new NotifiableComputationContainer(IO.OUT, false));
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    @Override
    public void onLoad() {
        super.onLoad();
        // attach self traits
        Map<IO, List<IRecipeHandler<?>>> ioTraits = new EnumMap<>(IO.class);

        for (MachineTrait trait : getAllTraits()) {
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
    }

    //////////////////////////////////////
    // ********** MISC ***********//
    //////////////////////////////////////

    @Override
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        importItems.dropInventoryInWorld();
        exportItems.dropInventoryInWorld();
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

            recipeLogic.updateSound();
        }
    }

    @Override
    public boolean keepSubscribing() {
        return false;
    }

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
