package com.gregtechceu.gtceu.api.machine.multiblock;

import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomProvider;
import com.gregtechceu.gtceu.api.machine.feature.IMufflableMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IWorkableMultiController;
import com.gregtechceu.gtceu.api.machine.trait.IRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/3/3
 * @implNote WorkableMultiblockMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class WorkableMultiblockMachine extends MultiblockControllerMachine
                                                implements IWorkableMultiController, IMufflableMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            WorkableMultiblockMachine.class, MultiblockControllerMachine.MANAGED_FIELD_HOLDER);
    @Nullable
    @Getter
    @Setter
    private ICleanroomProvider cleanroom;
    @Getter
    @Persisted
    @DescSynced
    public final RecipeLogic recipeLogic;
    @Getter
    private final GTRecipeType[] recipeTypes;
    @Getter
    @Setter
    @Persisted
    private int activeRecipeType;
    @Getter
    protected final Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> capabilitiesProxy;
    protected final List<ISubscription> traitSubscriptions;
    @Getter
    @Setter
    @Persisted
    @DescSynced
    protected boolean isMuffled;
    protected boolean previouslyMuffled = true;
    @Nullable
    @Getter
    protected LongSet activeBlocks;

    public WorkableMultiblockMachine(IMachineBlockEntity holder, Object... args) {
        super(holder);
        this.recipeTypes = getDefinition().getRecipeTypes();
        this.activeRecipeType = 0;
        this.recipeLogic = createRecipeLogic(args);
        this.capabilitiesProxy = Tables.newCustomTable(new EnumMap<>(IO.class), IdentityHashMap::new);
        this.traitSubscriptions = new ArrayList<>();
    }

    //////////////////////////////////////
    // ***** Initialization ******//
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
    // *** Multiblock LifeCycle ***//
    //////////////////////////////////////
    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        // attach parts' traits
        activeBlocks = getMultiblockState().getMatchContext().getOrDefault("vaBlocks", LongSets.emptySet());
        capabilitiesProxy.clear();
        traitSubscriptions.forEach(ISubscription::unsubscribe);
        traitSubscriptions.clear();
        Map<Long, IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap", Long2ObjectMaps::emptyMap);
        for (IMultiPart part : getParts()) {
            IO io = ioMap.getOrDefault(part.self().getPos().asLong(), IO.BOTH);
            if (io == IO.NONE) continue;
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
        activeBlocks = null;
        capabilitiesProxy.clear();
        traitSubscriptions.forEach(ISubscription::unsubscribe);
        traitSubscriptions.clear();
        // reset recipe Logic
        recipeLogic.resetRecipeLogic();
    }

    @Override
    public void onPartUnload() {
        super.onPartUnload();
        updateActiveBlocks(false);
        activeBlocks = null;
        capabilitiesProxy.clear();
        traitSubscriptions.forEach(ISubscription::unsubscribe);
        traitSubscriptions.clear();
        // fine some parts invalid now.
        // but we shouldn't reset recipe logic rn.
        // if it's due to chunk unload, we should just wait for it to be valid again.
        recipeLogic.updateTickSubscription();
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

    @Nullable
    @Override
    public final GTRecipe doModifyRecipe(GTRecipe recipe) {
        for (IMultiPart part : getParts()) {
            recipe = part.modifyRecipe(recipe);
            if (recipe == null) return null;
        }
        return getRealRecipe(recipe);
    }

    @Nullable
    protected GTRecipe getRealRecipe(GTRecipe recipe) {
        return self().getDefinition().getRecipeModifier().applyModifier(self(), recipe);
    }

    public void updateActiveBlocks(boolean active) {
        if (activeBlocks != null) {
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
    }

    @Override
    public boolean keepSubscribing() {
        return false;
    }

    @Override
    public void notifyStatusChanged(RecipeLogic.Status oldStatus, RecipeLogic.Status newStatus) {
        IWorkableMultiController.super.notifyStatusChanged(oldStatus, newStatus);
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
        for (IMultiPart part : getParts()) {
            part.afterWorking(this);
        }
        IWorkableMultiController.super.afterWorking();
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        for (IMultiPart part : getParts()) {
            if (!part.beforeWorking(this)) {
                return false;
            }
        }
        return IWorkableMultiController.super.beforeWorking(recipe);
    }

    @Override
    public boolean onWorking() {
        for (IMultiPart part : getParts()) {
            if (!part.onWorking(this)) {
                return false;
            }
        }
        return IWorkableMultiController.super.onWorking();
    }

    @Override
    public void onWaiting() {
        for (IMultiPart part : getParts()) {
            part.onWaiting(this);
        }
        IWorkableMultiController.super.onWaiting();
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        if (!isWorkingAllowed) {
            for (IMultiPart part : getParts()) {
                part.onPaused(this);
            }
        }
        IWorkableMultiController.super.setWorkingEnabled(isWorkingAllowed);
    }

    @NotNull
    public GTRecipeType getRecipeType() {
        return recipeTypes[activeRecipeType];
    }
}
