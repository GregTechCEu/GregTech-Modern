package com.gregtechceu.gtceu.api.machine.multiblock;

import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomProvider;
import com.gregtechceu.gtceu.api.machine.feature.IMufflableMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IWorkableMultiController;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;

import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerList;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;

import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
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
public abstract class WorkableMultiblockMachine extends MultiblockControllerMachine
                                                implements IWorkableMultiController, IMufflableMachine {

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
    protected final List<RecipeHandlerList> recipeHandlerLists;
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

    @Getter
    @Persisted
    @DescSynced
    protected VoidingMode voidingMode = VoidingMode.VOID_NONE;

    public WorkableMultiblockMachine(IMachineBlockEntity holder, Object... args) {
        super(holder);
        this.recipeTypes = getDefinition().getRecipeTypes();
        this.activeRecipeType = 0;
        this.recipeLogic = createRecipeLogic(args);
        this.recipeHandlerLists = new ArrayList<>();
        this.traitSubscriptions = new ArrayList<>();
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

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
        recipeHandlerLists.clear();
        recipeLogic.resetLastGroup();
        traitSubscriptions.forEach(ISubscription::unsubscribe);
        traitSubscriptions.clear();

        for (IMultiPart part : getParts()) {
            var handlerLists = part.getRecipeHandlers();
            handlerLists.forEach(h -> traitSubscriptions.add(h.subscribe(recipeLogic::updateTickSubscription)));
            recipeHandlerLists.addAll(handlerLists);
        }

        // attach self traits
        List<IRecipeHandler<?>> list = new ArrayList<>();
        for (MachineTrait trait : getTraits()) {
            if (trait instanceof IRecipeHandler<?> handlerTrait) {
                list.add(handlerTrait);
            }
        }
        var selfHandlerList = RecipeHandlerList.of(list);
        recipeHandlerLists.add(selfHandlerList);
        traitSubscriptions.add(selfHandlerList.subscribe(recipeLogic::updateTickSubscription));

        // schedule recipe logic
        recipeLogic.updateTickSubscription();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        updateActiveBlocks(false);
        activeBlocks = null;
        recipeHandlerLists.clear();
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
        recipeHandlerLists.clear();
        recipeLogic.resetLastGroup();
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
    public final Component modifyRecipe(GTRecipe recipe, RecipeHandlerGroup group) {
        for (IMultiPart part : getParts()) {
            var failReason = part.modifyRecipe(recipe);
            if (failReason != null) return failReason;
        }
        for(var modifier: self().getDefinition().getRecipeModifiers()) {
            var failReason = modifier.apply(self(), group, recipe);
            if (failReason != null) return failReason;
        }
        return null;
    }

    public void updateActiveBlocks(boolean active) {
        if (activeBlocks != null) {
            for (long pos : activeBlocks) {
                var blockPos = BlockPos.of(pos);
                var blockState = getLevel().getBlockState(blockPos);
                if (blockState.hasProperty(GTBlockStateProperties.ACTIVE)) {
                    var newState = blockState.setValue(GTBlockStateProperties.ACTIVE, active);
                    if (newState != blockState) {
                        getLevel().setBlock(blockPos, newState, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
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
        for (IMultiPart part : getParts()) {
            MachineRenderState state = part.self().getRenderState();
            if (state.hasProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS)) {
                part.self().setRenderState(state.setValue(GTMachineModelProperties.RECIPE_LOGIC_STATUS, newStatus));
            }
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
    public Component beforeWorking(@Nullable GTRecipe recipe) {
        for (IMultiPart part : getParts()) {
            Component failReason = part.beforeWorking(this);
            if (failReason != null) {
                return failReason;
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

    @Override
    public void setVoidingMode(VoidingMode mode) {
        voidingMode = mode;
        getRecipeLogic().updateTickSubscription();
    }
}
