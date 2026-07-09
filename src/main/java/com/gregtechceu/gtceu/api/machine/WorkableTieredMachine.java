package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.feature.*;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableComputationContainer;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.recipe.IRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerList;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
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

/**
 * A tiered energy machine with recipe logic and item/fluid IO.
 */
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

    /**
     * Creates a {@link WorkableTieredMachine}.
     * 
     * @param info                {@link BlockEntityCreationInfo}
     * @param tier                Machine tier.
     * @param recipeLogic         The recipe logic to use.
     * @param importSlots         The amount of item input slots this machine should have (can be 0).
     * @param exportSlots         The amount of item output slots this machine should have (can be 0).
     * @param fluidImportSlots    The amount of fluid input slots this machine should have (can be 0).
     * @param fluidExportSlots    The amount of fluid output slots this machine should have (can be 0).
     * @param energyEmitter       If this machine should input or output energy.
     * @param tankScalingFunction The tank scaling function which determines the capaacity of fluid slots.
     */
    public WorkableTieredMachine(BlockEntityCreationInfo info, int tier,
                                 RecipeLogic recipeLogic, int importSlots,
                                 int exportSlots,
                                 int fluidImportSlots, int fluidExportSlots, boolean energyEmitter,
                                 Int2IntFunction tankScalingFunction) {
        super(info, tier, energyEmitter);
        this.overclockTier = getMaxOverclockTier();
        this.recipeTypes = getDefinition().getRecipeTypes();
        this.activeRecipeType = 0;
        this.capabilitiesProxy = new EnumMap<>(IO.class);
        this.capabilitiesFlat = new EnumMap<>(IO.class);
        this.traitSubscriptions = new ArrayList<>();
        this.cleanroomReceiver = attachTrait(new CleanroomReceiverTrait());
        this.recipeLogic = attachTrait(recipeLogic);
        this.recipeLogic.setKeepSubscribing(false);
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

    /**
     * Creates a {@link WorkableTieredMachine} with default settings.<br>
     * The amount of item and fluid input and output slots is determined by the machine's recipe type.
     *
     * @param info                {@link BlockEntityCreationInfo}
     * @param tier                Machine tier.
     * @param energyEmitter       If this machine should input or output energy.
     * @param tankScalingFunction The tank scaling function which determines the capaacity of fluid slots.
     */
    public WorkableTieredMachine(BlockEntityCreationInfo info, int tier, boolean energyEmitter,
                                 Int2IntFunction tankScalingFunction) {
        super(info, tier, energyEmitter);
        this.overclockTier = getMaxOverclockTier();
        this.recipeTypes = getDefinition().getRecipeTypes();
        this.activeRecipeType = 0;
        this.capabilitiesProxy = new EnumMap<>(IO.class);
        this.capabilitiesFlat = new EnumMap<>(IO.class);
        this.traitSubscriptions = new ArrayList<>();
        this.cleanroomReceiver = attachTrait(new CleanroomReceiverTrait());
        this.recipeLogic = attachTrait(new RecipeLogic());
        this.recipeLogic.setKeepSubscribing(false);
        this.importItems = attachTrait(
                new NotifiableItemStackHandler(getRecipeType().getMaxInputs(ItemRecipeCapability.CAP),
                        IO.IN, IO.BOTH));
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

        for (var trait : getTraitHolder().getTraitsByInterface(IRecipeHandlerTrait.class)) {
            ioTraits.computeIfAbsent(trait.getHandlerIO(), i -> new ArrayList<>()).add(trait);
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

    public GTRecipeType getRecipeType() {
        int index = activeRecipeType >= 0 && activeRecipeType < recipeTypes.length ? activeRecipeType : 0;
        return recipeTypes[index];
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
