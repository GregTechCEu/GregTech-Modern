package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyTooltip;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.sound.AutoReleasedSound;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.UpdateListener;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.*;

import static com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerList.UNDYED;

public class RecipeLogic extends MachineTrait implements IWorkable, IFancyTooltip {

    public enum Status implements StringRepresentable {

        IDLE("idle"),
        WORKING("working"),
        WAITING("waiting"),
        SUSPEND("suspend");

        @Getter
        private final String serializedName;

        Status(String name) {
            this.serializedName = name;
        }
    }

    public static final EnumProperty<RecipeLogic.Status> STATUS_PROPERTY = GTMachineModelProperties.RECIPE_LOGIC_STATUS;
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(RecipeLogic.class);

    public final IRecipeLogicMachine machine;

    @Getter
    @Persisted
    @DescSynced
    @UpdateListener(methodName = "onStatusSynced")
    private Status status = Status.IDLE;

    @Persisted
    @DescSynced
    @UpdateListener(methodName = "onActiveSynced")
    protected boolean isActive;

    @Getter
    @Nullable
    @Persisted
    @DescSynced
    private Component waitingReason = null;

    @Getter
    @DescSynced
    protected final List<Component> failureReasons = new ArrayList<>();

    @Getter
    protected final Map<GTRecipe, Component> failureReasonMap = new HashMap<>();
    /**
     * unsafe, it may not be found from {@link RecipeManager}. Do not index it.
     */
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
    @Persisted
    protected GTRecipe lastOriginRecipe;
    @Persisted
    @Getter
    @Setter
    @DescSynced
    protected int progress;
    @Getter
    @Persisted
    @DescSynced
    protected int duration;
    @Getter(onMethod_ = @VisibleForTesting)
    protected boolean recipeDirty;

    @Persisted
    @Setter
    @Getter
    protected boolean suspendAfterFinish = false;
    protected TickableSubscription subscription;

    @OnlyIn(Dist.CLIENT)
    protected AutoReleasedSound workingSound;

    public RecipeLogic(IRecipeLogicMachine machine) {
        super(machine.self());
        this.machine = machine;
    }

    @SuppressWarnings("unused")
    protected void onStatusSynced(Status newValue, Status oldValue) {
        scheduleRenderUpdate();
        updateSound();
    }

    @SuppressWarnings("unused")
    protected void onActiveSynced(boolean newActive, boolean oldActive) {
        scheduleRenderUpdate();
    }

    /**
     * Call it to abort current recipe and reset the first state.
     */
    public void resetRecipeLogic() {
        recipeDirty = false;
        lastRecipe = null;
        lastOriginRecipe = null;
        lastGroup = null;
        lastGroupColor= UNDYED;
        progress = 0;
        duration = 0;
        isActive = false;
        waitingReason = null;
        failureReasons.clear();
        if (status != Status.SUSPEND) {
            setStatus(Status.IDLE);
        }
        updateTickSubscription();
    }

    @Override
    public void onMachineLoad() {
        super.onMachineLoad();
        updateTickSubscription();
    }

    public void updateTickSubscription() {
        if (isSuspend() || !machine.isRecipeLogicAvailable()) {
            if (subscription != null) {
                subscription.unsubscribe();
                subscription = null;
            }
        } else {
            subscription = getMachine().subscribeServerTick(subscription, this::serverTick);
        }
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
        if (isIdle()) {
            failureReasons.clear();
            failureReasons.addAll(failureReasonMap.values());
        }
        if ((isSuspend() || (isIdle() && !machine.keepSubscribing())) && subscription != null) {
            subscription.unsubscribe();
            subscription = null;
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

    public boolean checkMatchedRecipeAvailable(GTRecipe match) {
        var modified = machine.fullModifyRecipe(match, getLastGroup());
        if (modified != null) {
            var recipeMatch = checkRecipe(modified);
            if (recipeMatch.isSuccess()) {
                setupRecipe(modified);
            } else {
                putFailureReason(this, match, recipeMatch.reason());
            }
            if (lastRecipe != null && getStatus() == Status.WORKING) {
                lastOriginRecipe = match;
                return true;
            }
        }
        return false;
    }

    public void handleRecipeWorking() {
        assert lastRecipe != null;
        var conditionResult = RecipeHelper.checkConditions(lastRecipe, this);
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

    public @NotNull Iterator<GTRecipe> searchRecipe() {
        for(var group: machine.getRecipeHandlerGroups()) {
            var iterator = machine.getRecipeType().searchRecipe(group, r -> true);
            if(iterator.hasNext()) {
                lastGroup = group;
                if(group.getColor() != UNDYED) lastGroupColor = group.getColor();
                return iterator;
            }
        }
        return Collections.emptyIterator();
    }

    public void findAndHandleRecipe() {
        failureReasonMap.clear();
        recipeDirty = false;
        lastRecipe = null;
        lastOriginRecipe = null;
        handleSearchingRecipes(searchRecipe());
    }

    protected void handleSearchingRecipes(@NotNull Iterator<GTRecipe> matches) {
        while (matches.hasNext()) {
            GTRecipe match = matches.next();
            if (match == null) continue;

            // If a new recipe was found, cache found recipe.
            if (checkMatchedRecipeAvailable(match))
                return;
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
        if (!machine.beforeWorking(recipe)) {
            setStatus(Status.IDLE);
            progress = 0;
            duration = 0;
            isActive = false;
            return;
        }
        var handledIO = handleRecipeIO(recipe, IO.IN);
        if (handledIO.isSuccess()) {
            failureReasonMap.clear();
            failureReasons.clear();
            recipeDirty = false;
            lastRecipe = recipe;
            setStatus(Status.WORKING);
            progress = 0;
            duration = recipe.duration;
            isActive = true;
        }
    }

    public void setStatus(Status status) {
        if (this.status != status) {
            if ((status == Status.WAITING || status == Status.SUSPEND) && suspendAfterFinish) {
                status = Status.SUSPEND;
                suspendAfterFinish = false;
            }
            machine.notifyStatusChanged(this.status, status);
            this.status = status;
            setRenderState(getRenderState().setValue(GTMachineModelProperties.RECIPE_LOGIC_STATUS, status));
            updateTickSubscription();
            if (this.status != Status.WAITING) {
                waitingReason = null;
            }
        }
    }

    public void setWaiting(@Nullable Component reason) {
        setStatus(Status.WAITING);
        waitingReason = reason;
        machine.onWaiting();
    }

    /**
     * mark current handling recipe (if exist) as dirty.
     * do not try it immediately in the next round
     */
    public void markLastRecipeDirty() {
        this.recipeDirty = true;
    }

    public boolean isWorking() {
        return status == Status.WORKING;
    }

    public boolean isIdle() {
        return status == Status.IDLE;
    }

    public boolean isWaiting() {
        return status == Status.WAITING;
    }

    public boolean isSuspend() {
        return status == Status.SUSPEND;
    }

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

    @Override
    public int getMaxProgress() {
        return duration;
    }

    public boolean isActive() {
        return isWorking() || isWaiting() || (isSuspend() && isActive);
    }

    public boolean hasCustomProgressLine() {
        return false;
    }

    /**
     * Show the customized progress line instead of the regular duration progress time in the machine display.
     * <p>
     * Must override and return {@code true} in {@link #hasCustomProgressLine()}.
     *
     * @return the customized progress line
     */
    public @Nullable Component getCustomProgressLine() {
        return null;
    }

    public void onRecipeFinish() {
        machine.afterWorking();
        if (lastRecipe != null) {
            handleRecipeIO(lastRecipe, IO.OUT);
            // Don't ready the next recipe after finish if suspend is set
            // so that the modifiers won't be applied until re-starting.
            if (suspendAfterFinish) {
                setStatus(Status.SUSPEND);
                progress = 0;
                duration = 0;
                isActive = false;
                // Force a recipe recheck.
                lastRecipe = null;
                return;
            }
            if (machine.alwaysTryModifyRecipe()) {
                if (lastOriginRecipe != null) {
                    var modified = machine.fullModifyRecipe(lastOriginRecipe.copy(), getLastGroup());
                    if (modified == null) {
                        markLastRecipeDirty();
                    } else {
                        lastRecipe = modified;
                    }
                } else {
                    markLastRecipeDirty();
                }
            }
            // try it again

            if (!recipeDirty && checkRecipe(lastRecipe).isSuccess()) {
                setupRecipe(lastRecipe);
            } else {
                setStatus(Status.IDLE);
                progress = 0;
                duration = 0;
                isActive = false;
            }
        }
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

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    //////////////////////////////////////
    // ******** MISC *********//
    //////////////////////////////////////
    @OnlyIn(Dist.CLIENT)
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
    public IGuiTexture getFancyTooltipIcon() {
        if (showFancyTooltip()) {
            return GuiTextures.INSUFFICIENT_INPUT;
        }
        return IGuiTexture.EMPTY;
    }

    @Override
    public List<Component> getFancyTooltip() {
        if (isWaiting() && waitingReason != null) {
            return List.of(waitingReason);
        }
        if (isIdle() && !failureReasons.isEmpty()) {
            return failureReasons;
        }
        return Collections.emptyList();
    }

    @Override
    public boolean showFancyTooltip() {
        return waitingReason != null || !failureReasons.isEmpty();
    }

    public static void putFailureReason(Object machine, GTRecipe recipe, Component reason) {
        if (machine instanceof IRecipeLogicMachine rlm) {
            putFailureReason(rlm.getRecipeLogic(), recipe, reason);
        }
    }

    public static void putFailureReason(RecipeLogic logic, GTRecipe recipe, Component reason) {
        var map = logic.getFailureReasonMap();
        if (map.containsKey(recipe)) {
            if (reason != ModifierFunction.DEFAULT_FAILURE) {
                map.put(recipe, reason);
            }
        } else {
            map.put(recipe, reason);
        }
    }
}
