package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;
import com.gregtechceu.gtceu.api.sound.AutoReleasedSound;

import com.lowdragmc.lowdraglib.misc.SyncableMap;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.*;

import static com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerList.UNDYED;

public class RecipeLogic extends WorkLogic {

    public static final EnumProperty<WorkLogic.Status> STATUS_PROPERTY = GTMachineModelProperties.RECIPE_LOGIC_STATUS;

    public final IRecipeLogicMachine machine;

    @Getter
    @DescSynced
    protected final SyncableMap<ResourceLocation, Component> failureReasonsMap = new SyncableMap<>() {};

    @Nullable
    @Getter
    @Persisted
    @DescSynced
    protected GTRecipe lastRecipe;

    @Persisted
    protected int lastGroupColor = UNDYED;

    protected RecipeHandlerGroup lastGroup;

    @Nullable
    @Getter
    protected GTRecipeDefinition lastOriginRecipe;

    @Getter
    @Persisted
    protected int progress;

    @Getter
    @Persisted
    protected int duration;

    @Getter(onMethod_ = @VisibleForTesting)
    protected boolean recipeDirty;

    @OnlyIn(Dist.CLIENT)
    protected AutoReleasedSound workingSound;

    public RecipeLogic(IRecipeLogicMachine machine) {
        super(machine);
        this.machine = machine;
    }

    /**
     * Call it to abort current recipe and reset the first state.
     */
    public void reset() {
        super.reset();
        recipeDirty = false;
        lastRecipe = null;
        lastOriginRecipe = null;
        lastGroup = null;
        lastGroupColor= UNDYED;
        progress = 0;
        duration = 0;
        failureReasonsMap.clear();

    }

    public double getProgressPercent() {
        return duration == 0 ? 0.0 : progress / (duration * 1.0);
    }

    /**
     * it should be called on the server side restrictively.
     */
    public RecipeManager getRecipeManager() {
        return GTCEu.getMinecraftServer().getRecipeManager();
    }

    @Override
    public void serverTick() {
        if (!isSuspend()) {
            if (!isIdle() && lastRecipe != null) {
                if (progress < duration) {
                    handleRecipeWorking();
                }
                if (progress >= duration) {
                    onRecipeFinish();
                }
            } else {
                findAndHandleRecipe();
            }
        }

        if ((isSuspend() || (isIdle() && !machine.keepSubscribing()))) {
            unsubscribeTick();
        }
    }

    protected ActionResult matchRecipe(GTRecipe recipe) {
        return RecipeHelper.matchContents(getLastGroup(), recipe);
    }

    protected ActionResult checkRecipe(GTRecipe recipe) {
        var conditionResult = RecipeHelper.checkConditions(recipe, this);
        if (!conditionResult.isSuccess()) return conditionResult;

        return matchRecipe(recipe);
    }

    public boolean checkMatchedRecipeAvailable(GTRecipeDefinition match) {
        var modified = match.toRuntime();
        var failReason = machine.modifyRecipe(modified, getLastGroup());
        if (failReason == null) {
            var recipeMatch = checkRecipe(modified);
            if (recipeMatch.isSuccess()) {
                setupRecipe(modified);
            } else {
                failureReasonsMap.put(match.id, recipeMatch.reason());
            }
            if (lastRecipe != null && getStatus() == Status.WORKING) {
                lastOriginRecipe = match;
                return true;
            }
        }
        else {
            failureReasonsMap.put(match.id, failReason);
        }
        return false;
    }

    public void handleRecipeWorking() {
        assert lastRecipe != null;
        var conditionResult = RecipeHelper.checkConditions(lastRecipe, this, true);
        if (conditionResult.isSuccess()) {
            var handleTick = handleTickRecipe(lastRecipe);
            if (handleTick.isSuccess()) {
                setStatus(Status.WORKING);
                if (!machine.onWorking()) {
                    this.interruptRecipe();
                    return;
                }
                progress++;
            } else {
                setWaiting(handleTick.reason());
            }
        } else {
            setWaiting(conditionResult.reason());
        }
        if (isWaiting() || isSuspend()) {
            regressRecipe();
        }
    }

    protected void regressRecipe() {
        if (progress > 0 && machine.regressWhenWaiting()) {
            this.progress = 1;
        }
    }

    public void findAndHandleRecipe() {
        failureReasonsMap.clear();
        recipeDirty = false;
        lastRecipe = null;
        lastOriginRecipe = null;
        for(var group: machine.getRecipeHandlerGroups()) {
            if(machine.getRecipeType().findRecipe(group, this::checkMatchedRecipeAvailable) != null) {
                lastGroup = group;
                if(group.getColor() != UNDYED) lastGroupColor = group.getColor();
                break;
            }
        }
    }

    public void resetLastGroup() {
        lastGroup = null;
    }

    public RecipeHandlerGroup getLastGroup() {
        if(lastGroup == null) {
            var groups = machine.getRecipeHandlerGroups();
            lastGroup = groups.stream()
                    .filter(group -> group.getColor() == lastGroupColor)
                    .findFirst()
                    .orElse(groups.get(0));
        }
        return lastGroup;
    }

    public ActionResult handleTickRecipe(GTRecipe recipe) {
        if (!recipe.hasTick()) return ActionResult.SUCCESS;

        var result = RecipeHelper.matchTickRecipe(getLastGroup(), recipe);
        if (!result.isSuccess()) return result;

        result = handleTickRecipeIO(recipe, IO.IN);
        if (!result.isSuccess()) return result;

        result = handleTickRecipeIO(recipe, IO.OUT);
        return result;
    }

    public void setupRecipe(GTRecipe recipe) {
        var failReason = machine.beforeWorking(recipe);
        if (failReason != null) {
            setStatus(Status.IDLE);
            progress = 0;
            duration = 0;
            failureReasonsMap.put(recipe.id, failReason);
            return;
        }
        var handledIO = handleRecipeIO(recipe, IO.IN);
        if (handledIO.isSuccess()) {
            failureReasonsMap.clear();
            recipeDirty = false;
            lastRecipe = recipe;
            setStatus(Status.WORKING);
            progress = 0;
            duration = recipe.duration;
        }
    }

    @Override
    protected void onWaiting() {
        machine.onWaiting();
    }

    /**
     * mark current handling recipe (if exist) as dirty.
     * do not try it immediately in the next round
     */
    public void markLastRecipeDirty() {
        this.recipeDirty = true;
    }

    @Override
    public boolean isWorkingEnabled() {
        return !isSuspend() && !isSuspendAfterFinish();
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        if (!isWorkingAllowed && getStatus() == Status.IDLE) {
            setStatus(Status.SUSPEND);
        } else {
            setSuspendAfterFinish(!isWorkingAllowed);
            if (isWorkingAllowed) {
                if (lastRecipe != null && duration > 0) {
                    setStatus(Status.WORKING);
                } else {
                    setStatus(Status.IDLE);
                }
            }
        }
    }

    public int getMaxProgress() {
        return duration;
    }

    public void onRecipeFinish() {
        machine.afterWorking();
        if (lastRecipe != null) {
            handleRecipeIO(lastRecipe, IO.OUT);
            if (suspendAfterFinish) {
                setStatus(Status.SUSPEND);
                suspendAfterFinish = false;
            }
            else {
                if(!recipeDirty) {
                    if (lastOriginRecipe != null && machine.alwaysTryModifyRecipe()) {
                        lastRecipe = lastOriginRecipe.toRuntime();
                        var failReason = machine.modifyRecipe(lastRecipe, getLastGroup());
                        if(failReason != null) {
                            failureReasonsMap.put(lastOriginRecipe.id, failReason);
                            lastRecipe = null;
                        }
                    }
                    if (lastRecipe != null && checkRecipe(lastRecipe).isSuccess()) {
                        setupRecipe(lastRecipe);
                        return;
                    }
                }
                setStatus(Status.IDLE);
            }
        }
        progress = 0;
        duration = 0;
        lastRecipe = null;

    }

    protected ActionResult handleRecipeIO(GTRecipe recipe, IO io) {
        return RecipeHelper.handleRecipeIO(getLastGroup(), recipe, io);
    }

    protected ActionResult handleTickRecipeIO(GTRecipe recipe, IO io) {
        return RecipeHelper.handleTickRecipeIO(getLastGroup(), recipe, io);
    }

    /**
     * Interrupt current recipe without io.
     */
    public void interruptRecipe() {
        machine.afterWorking();
        if (lastRecipe != null) {
            setStatus(Status.IDLE);
            progress = 0;
            duration = 0;
        }
    }

    // Remains for legacy + for subclasses
    public void inValid() {}

    //////////////////////////////////////
    // ******** MISC *********//
    //////////////////////////////////////
    @OnlyIn(Dist.CLIENT)
    @Override
    public void updateSound() {
        if (isWorking() && machine.shouldWorkingPlaySound()) {
            var sound = machine.getRecipeType().getSound();
            if (workingSound != null) {
                if (workingSound.soundEntry == sound && !workingSound.isStopped()) {
                    return;
                }
                workingSound.release();
                workingSound = null;
            }
            if (sound != null) {
                workingSound = sound.playAutoReleasedSound(
                        () -> machine.shouldWorkingPlaySound() && isWorking() && !getMachine().isInValid() &&
                                getMachine().getLevel().isLoaded(getMachine().getPos()) &&
                                MetaMachine.getMachine(getMachine().getLevel(), getMachine().getPos()) == getMachine(),
                        getMachine().getPos(), true, 0, 1, 1);
            }
        } else if (workingSound != null) {
            workingSound.release();
            workingSound = null;
        }
    }

    @Override
    public List<Component> getFancyTooltip() {
        if (isWaiting() && waitingReason != null) {
            return List.of(waitingReason);
        }
        if (isIdle() && !failureReasonsMap.isEmpty()) {
            return new ArrayList<>(failureReasonsMap.values());
        }
        return Collections.emptyList();
    }


    public boolean hasCustomProgressLine() {
        return false;
    }

    public @Nullable Component getCustomProgressLine() {
        return null;
    }

    @Override
    public boolean showFancyTooltip() {
        return waitingReason != null || !failureReasonsMap.isEmpty();
    }


}
