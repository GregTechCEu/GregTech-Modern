package com.gregtechceu.gtceu.api.machine.multiblock;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.capability.IMaintenanceHatch;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomProvider;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomReceiver;
import com.gregtechceu.gtceu.api.machine.feature.IMufflableMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenance;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMufflerMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.trait.IRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

/**
 * @author KilaBash
 * @date 2023/3/3
 * @implNote WorkableMultiblockMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class WorkableMultiblockMachine extends MultiblockControllerMachine implements IRecipeLogicMachine, IMufflableMachine, IMaintenance, ICleanroomReceiver {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(WorkableMultiblockMachine.class, MultiblockControllerMachine.MANAGED_FIELD_HOLDER);
    private static final int minimumMaintenanceTime = 3456000; // 48 real-life hours = 3456000 ticks

    @Getter
    @Persisted
    @DescSynced
    public final RecipeLogic recipeLogic;
    @Getter
    public final GTRecipeType recipeType;
    @Getter
    protected final Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> capabilitiesProxy;
    protected final List<ISubscription> traitSubscriptions;

    @Nullable @Getter @Setter
    private ICleanroomProvider cleanroom;

    @Persisted @DescSynced @Getter @Setter
    protected boolean isMuffled;

    @Persisted @DescSynced
    private int timeActive;
    /**
     * This value stores whether each of the 5 maintenance problems have been fixed.
     * A value of 0 means the problem is not fixed, else it is fixed
     * Value positions correspond to the following from left to right: 0=Wrench, 1=Screwdriver, 2=Soft Mallet, 3=Hard Hammer, 4=Wire Cutter, 5=Crowbar
     */
    @Persisted @DescSynced
    protected byte maintenanceProblems;

    // Used for data preservation with Maintenance Hatch
    @Getter @Persisted @DescSynced
    private boolean storedTaped = false;

    public WorkableMultiblockMachine(IMachineBlockEntity holder, Object... args) {
        super(holder);
        this.recipeType = getDefinition().getRecipeType();
        this.recipeLogic = createRecipeLogic(args);
        this.capabilitiesProxy = Tables.newCustomTable(new EnumMap<>(IO.class), HashMap::new);
        this.traitSubscriptions = new ArrayList<>();
        this.maintenanceProblems = 0b000000;
    }

    //////////////////////////////////////
    //*****     Initialization    ******//
    //////////////////////////////////////

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onUnload() {
        super.onUnload();
        traitSubscriptions.forEach(ISubscription::unsubscribe);
        traitSubscriptions.clear();
        recipeLogic.inValid();
    }

    protected RecipeLogic createRecipeLogic(Object... args) {
        return new RecipeLogic(this);
    }

    //////////////////////////////////////
    //***    Multiblock LifeCycle    ***//
    //////////////////////////////////////
    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        // attach parts' traits
        capabilitiesProxy.clear();
        traitSubscriptions.forEach(ISubscription::unsubscribe);
        traitSubscriptions.clear();
        Map<Long, IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap", Long2ObjectMaps::emptyMap);
        for (IMultiPart part : parts) {
            if (part instanceof IMaintenanceHatch maintenanceHatch) {
                if (maintenanceHatch.startWithoutProblems()) {
                    this.maintenanceProblems = (byte) 0b111111;
                    this.timeActive = 0;
                }
                readMaintenanceData(maintenanceHatch);
                if (storedTaped) {
                    maintenanceHatch.setTaped(true);
                    storeTaped(false);
                }
                continue;
            }

            IO io = ioMap.getOrDefault(part.self().getPos().asLong(), IO.BOTH);
            if(io == IO.NONE) continue;
            for (var handler : part.getRecipeHandlers()) {
                // If IO not compatible
                if (io != IO.BOTH && handler.getHandlerIO() != IO.BOTH && io != handler.getHandlerIO()) continue;
                var handlerIO = io == IO.BOTH ? handler.getHandlerIO() : io;
                if (!capabilitiesProxy.contains(handlerIO, handler.getCapability())) {
                    capabilitiesProxy.put(handlerIO, handler.getCapability(), new ArrayList<>());
                }
                capabilitiesProxy.get(handlerIO, handler.getCapability()).add(handler);
                traitSubscriptions.add(handler.addChangedListener(recipeLogic::updateTickSubscription));
            }
        }
        // attach self traits
        for (MachineTrait trait : getTraits()) {
            if (trait instanceof IRecipeHandlerTrait<?> handlerTrait) {
                if (!capabilitiesProxy.contains(handlerTrait.getHandlerIO(), handlerTrait.getCapability())) {
                    capabilitiesProxy.put(handlerTrait.getHandlerIO(), handlerTrait.getCapability(), new ArrayList<>());
                }
                capabilitiesProxy.get(handlerTrait.getHandlerIO(), handlerTrait.getCapability()).add(handlerTrait);
                traitSubscriptions.add(handlerTrait.addChangedListener(recipeLogic::updateTickSubscription));
            }
        }
        // schedule recipe logic
        recipeLogic.updateTickSubscription();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        updateActiveBlocks(false);
        capabilitiesProxy.clear();
        traitSubscriptions.forEach(ISubscription::unsubscribe);
        traitSubscriptions.clear();
        //reset recipe Logic
        recipeLogic.resetRecipeLogic();
    }

    @Override
    public void onPartUnload() {
        super.onPartUnload();
        updateActiveBlocks(false);
        capabilitiesProxy.clear();
        traitSubscriptions.forEach(ISubscription::unsubscribe);
        traitSubscriptions.clear();
        // fine some parts invalid now.
        // but we shouldn't reset recipe logic rn.
        // if it's due to chunk unload, we should just wait for it to be valid again.
        recipeLogic.updateTickSubscription();
    }

    //////////////////////////////////////
    //******     RECIPE LOGIC    *******//
    //////////////////////////////////////

    public void updateActiveBlocks(boolean active) {
        LongSet activeBlocks = getMultiblockState().getMatchContext().getOrDefault("vaBlocks", LongSets.emptySet());
        for (Long pos : activeBlocks) {
            var blockPos = BlockPos.of(pos);
            var blockState = getLevel().getBlockState(blockPos);
            if (blockState.getBlock() instanceof ActiveBlock block) {
                var newState = block.changeActive(blockState, active);
                if (newState != blockState) {
                    getLevel().setBlockAndUpdate(blockPos, newState);
                }
            }
        }
    }

    @Override
    public boolean keepSubscribing() {
        return false;
    }

    @Override
    public void notifyStatusChanged(RecipeLogic.Status oldStatus, RecipeLogic.Status newStatus) {
        IRecipeLogicMachine.super.notifyStatusChanged(oldStatus, newStatus);
        if (newStatus == RecipeLogic.Status.WORKING || oldStatus == RecipeLogic.Status.WORKING) {
            updateActiveBlocks(newStatus == RecipeLogic.Status.WORKING);
        }
    }

    @Override
    public boolean isRecipeLogicAvailable() {
        return isFormed && !getMultiblockState().hasError();
    }


    @Override
    public void afterWorking() {
        IRecipeLogicMachine.super.afterWorking();
        if (getDefinition().getRecoveryItems() != null) {
            for (IMultiPart part : parts) {
                if (part instanceof IMufflerMachine muffler) {
                    muffler.recoverItemsTable(getDefinition().getRecoveryItems().get());
                    break;
                }
            }
        }
        if (ConfigHolder.INSTANCE.machines.enableMaintenance && hasMaintenanceMechanics()) {
            for (IMultiPart part : parts) {
                if (part instanceof IMaintenanceHatch maintenanceHatch) {
                    // increase total on time
                    this.calculateMaintenance(maintenanceHatch, this.recipeLogic.progress);
                    break;
                }
            }
        }
    }

    /**
     * Used to calculate whether a maintenance problem should happen based on machine time active
     *
     * @param duration in ticks to add to the counter of active time
     */
    public void calculateMaintenance(IMaintenanceHatch maintenanceHatch, int duration) {
        if (maintenanceHatch.isFullAuto()) {
            return;
        }

        timeActive += duration * maintenanceHatch.getTimeMultiplier();
        if (minimumMaintenanceTime - timeActive <= 0) {
            if (GTValues.RNG.nextFloat() - 0.75f >= 0) {
                causeMaintenanceProblems();
                maintenanceHatch.setTaped(false);
                timeActive = timeActive - minimumMaintenanceTime;
            }
        }
    }

    @Override
    public byte getMaintenanceProblems() {
        return ConfigHolder.INSTANCE.machines.enableMaintenance ? maintenanceProblems : 0b111111;
    }

    @Override
    public int getNumMaintenanceProblems() {
        return ConfigHolder.INSTANCE.machines.enableMaintenance ? 6 - Integer.bitCount(maintenanceProblems) : 0;
    }

    @Override
    public boolean hasMaintenanceProblems() {
        return ConfigHolder.INSTANCE.machines.enableMaintenance && this.maintenanceProblems < 63;
    }

    @Override
    public void setMaintenanceFixed(int index) {
        this.maintenanceProblems |= 1 << index;

    }

    @Override
    public void causeMaintenanceProblems() {
        this.maintenanceProblems &= ~(1 << ((int) (GTValues.RNG.nextFloat() * 5)));
    }

    @Override
    public void storeTaped(boolean isTaped) {
        this.storedTaped = isTaped;
    }

    /**
     * reads maintenance data from a maintenance hatch
     *
     * @param hatch is the hatch to read the data from
     */
    private void readMaintenanceData(IMaintenanceHatch hatch) {
        if (hatch.hasMaintenanceData()) {
            Tuple<Byte, Integer> data = hatch.readMaintenanceData();
            this.maintenanceProblems = data.getA();
            this.timeActive = data.getB();
        }
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return true;
    }
}
