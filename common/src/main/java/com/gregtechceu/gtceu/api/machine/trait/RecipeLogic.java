package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyTooltip;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.sound.AutoReleasedSound;
import com.gregtechceu.gtceu.api.syncdata.IEnhancedManaged;
import com.gregtechceu.gtceu.api.syncdata.UpdateListener;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.VisibleForTesting;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecipeLogic extends MachineTrait implements IEnhancedManaged, IWorkable, IFancyTooltip {

    public enum Status {
        IDLE, WORKING, WAITING, SUSPEND
    }

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(RecipeLogic.class);

    public final IRecipeLogicMachine machine;
    public List<GTRecipe> lastFailedMatches;

    @Getter @Persisted @DescSynced @UpdateListener(methodName = "onStatusSynced")
    private Status status = Status.IDLE;

    @Nullable
    @Persisted @DescSynced
    private Component waitingReason = null;
    /**
     * unsafe, it may not be found from {@link RecipeManager}. Do not index it.
     */
    @Nullable @Getter @Persisted
    protected GTRecipe lastRecipe;
    /**
     * safe, it is the origin recipe before {@link IRecipeLogicMachine#fullModifyRecipe(GTRecipe)}' which can be found from {@link RecipeManager}.
     */
    @Nullable @Getter @Persisted
    protected GTRecipe lastOriginRecipe;
    @Persisted
    @Getter
    protected int progress;
    @Getter @Persisted
    protected int duration;
    @Getter @Persisted
    protected int fuelTime;
    @Getter @Persisted
    protected int fuelMaxTime;
    @Getter
    protected long timeStamp;
    @Getter(onMethod_ = @VisibleForTesting)
    protected boolean recipeDirty;
    @Persisted
    @Getter
    protected long totalContinuousRunningTime;
    protected TickableSubscription subscription;
    protected Object workingSound;

    public RecipeLogic(IRecipeLogicMachine machine) {
        super(machine.self());
        this.machine = machine;
        this.timeStamp = Long.MIN_VALUE;
    }

    @Environment(EnvType.CLIENT)
    @SuppressWarnings("unused")
    protected void onStatusSynced(Status newValue, Status oldValue) {
        getMachine().scheduleRenderUpdate();
        updateSound();
    }

    @Override
    public void scheduleRenderUpdate() {
        getMachine().scheduleRenderUpdate();
    }

    /**
     * Call it to abort current recipe and reset the first state.
     */
    public void resetRecipeLogic() {
        recipeDirty = false;
        lastRecipe = null;
        lastOriginRecipe = null;
        progress = 0;
        duration = 0;
        fuelTime = 0;
        lastFailedMatches = null;
        status = Status.IDLE;
        this.timeStamp = Long.MIN_VALUE;
        updateTickSubscription();
    }

    @Override
    public void onMachineLoad() {
        super.onMachineLoad();
        updateTickSubscription();
    }

    public void updateTickSubscription() {
        if ((isSuspend() && fuelTime == 0) || !machine.isRecipeLogicAvailable()) {
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

    public boolean needFuel() {
        if (machine.getRecipeType().isFuelRecipeType()){
            return true;
        }
        return false;
    }

    /**
     * it should be called on the server side restrictively.
     */
    public RecipeManager getRecipeManager() {
        return Platform.getMinecraftServer().getRecipeManager();
    }

    public long getLatestTimeStamp() {
        return machine.self().getLevel().getGameTime();
    }

    public void serverTick() {
        if (!isSuspend()) {
            if (!isIdle() && lastRecipe != null) {
                if (isWaiting() && getMachine().getOffsetTimer() % 5 == 0) {
                    executeDirty(this::handleRecipeWorking);
                } else {
                    if (progress < duration) {
                        handleRecipeWorking();
                    }
                    if (progress >= duration) {
                        onRecipeFinish();
                    }
                }
            } else if (lastRecipe != null) {
                findAndHandleRecipe();
            } else if (!machine.keepSubscribing() || getMachine().getOffsetTimer() % 5 == 0) {
                executeDirty(this::findAndHandleRecipe);
                if (lastFailedMatches != null) {
                    for (GTRecipe match : lastFailedMatches) {
                        if (checkMatchedRecipeAvailable(match)) break;
                    }
                }
            }
        }
        if (fuelTime > 0) {
            fuelTime--;
        } else {
            if (isSuspend() || (lastRecipe == null && isIdle() && !machine.keepSubscribing() && !recipeDirty && lastFailedMatches == null)) {
                // machine isn't working enabled
                // or
                // there is no available recipes, so it will wait for notification.
                if (subscription != null) {
                    subscription.unsubscribe();
                    subscription = null;
                }
            }
        }
    }

    protected boolean checkMatchedRecipeAvailable(GTRecipe match) {
        var modified = machine.fullModifyRecipe(match);
        if (modified != null) {
            if (modified.checkConditions(this).isSuccess() &&
                    modified.matchRecipe(machine).isSuccess() &&
                    modified.matchTickRecipe(machine).isSuccess()) {
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

    public void handleRecipeWorking() {
        Status last = this.status;
        assert lastRecipe != null;
        var result = lastRecipe.checkConditions(this);
        if (result.isSuccess()) {
            if (handleFuelRecipe()) {
                result = handleTickRecipe(lastRecipe);
                if (result.isSuccess()) {
                    setStatus(Status.WORKING);
                    machine.onWorking();
                    progress++;
                    totalContinuousRunningTime++;
                } else {
                    setWaiting(result.reason().get());
                    if (progress > 0 && machine.dampingWhenWaiting()) {
                        if (ConfigHolder.INSTANCE.machines.recipeProgressLowEnergy) {
                            this.progress = 1;
                        } else {
                            this.progress = Math.max(1, progress - 2);
                        }
                    }
                }
            } else {
                setWaiting(Component.translatable("gtceu.recipe_logic.insufficient_fuel"));
            }
        } else {
            setWaiting(result.reason().get());
        }
        if (last == Status.WORKING && getStatus() != Status.WORKING) {
            lastRecipe.postWorking(machine);
        } else if (last != Status.WORKING && getStatus() == Status.WORKING) {
            lastRecipe.preWorking(machine);
        }
    }

    private void executeDirty(Runnable changed) {
        long latestTS = getLatestTimeStamp();
        if (latestTS < timeStamp) {
            timeStamp = latestTS;
            changed.run();
        } else {
            if (machine.hasProxies() && checkDirty(latestTS)) {
                changed.run();
            }
        }
    }

    public boolean checkDirty(long latestTS) {
        boolean execute = false;
        for (var handlers : machine.getCapabilitiesProxy().values()) {
            if (handlers != null) {
                for (var handler : handlers) {
                    if (handler.getTimeStamp() < latestTS) {
                        handler.setTimeStamp(latestTS);
                    }
                    if (handler.getTimeStamp() > timeStamp) {
                        execute = true;
                        timeStamp = handler.getTimeStamp();
                    }
                }
            }
        }
        return execute;
    }

    protected List<GTRecipe> searchRecipe() {
        return machine.getRecipeType().searchRecipe(getRecipeManager(), this.machine);
    }

    public void findAndHandleRecipe() {
        lastFailedMatches = null;
        // try to execute last recipe if possible
        if (!recipeDirty && lastRecipe != null &&
                lastRecipe.matchRecipe(this.machine).isSuccess() &&
                lastRecipe.matchTickRecipe(this.machine).isSuccess() &&
                lastRecipe.checkConditions(this).isSuccess()) {
            GTRecipe recipe = lastRecipe;
            lastRecipe = null;
            lastOriginRecipe = null;
            setupRecipe(recipe);
        } else { // try to find and handle a new recipe
            List<GTRecipe> matches = searchRecipe();

            lastRecipe = null;
            lastOriginRecipe = null;
            for (GTRecipe match : matches) {
                // try to modify recipe by machine, such as overclock, tier checking.
                if (checkMatchedRecipeAvailable(match)) break;
                // cache matching recipes.
                if (lastFailedMatches == null) {
                    lastFailedMatches = new ArrayList<>();
                }
                lastFailedMatches.add(match);
            }
        }
        recipeDirty = false;
    }

    public boolean handleFuelRecipe() {
        if (!needFuel() || fuelTime > 0) return true;
        for (GTRecipe recipe : machine.getRecipeType().searchFuelRecipe(getRecipeManager(), machine)) {
            if (recipe.checkConditions(this).isSuccess() && recipe.handleRecipeIO(IO.IN, this.machine)) {
                fuelMaxTime = recipe.duration;
                fuelTime = fuelMaxTime;
            }
            if (fuelTime > 0) return true;
        }
        return false;
    }

    public GTRecipe.ActionResult handleTickRecipe(GTRecipe recipe) {
        if (recipe.hasTick()) {
            var result = recipe.matchTickRecipe(this.machine);
            if (result.isSuccess()) {
                recipe.handleTickRecipeIO(IO.IN, this.machine);
                recipe.handleTickRecipeIO(IO.OUT, this.machine);
            } else {
                return result;
            }
        }
        return GTRecipe.ActionResult.SUCCESS;
    }

    public void setupRecipe(GTRecipe recipe) {
        if (handleFuelRecipe()) {
            machine.beforeWorking();
            recipe.preWorking(this.machine);
            if (recipe.handleRecipeIO(IO.IN, this.machine)) {
                recipeDirty = false;
                lastRecipe = recipe;
                setStatus(Status.WORKING);
                progress = 0;
                duration = recipe.duration;
            }
        }
    }

    public void setStatus(Status status) {
        if (this.status != status) {
            if (this.status == Status.WORKING) {
                this.totalContinuousRunningTime = 0;
            }
            machine.notifyStatusChanged(this.status, status);
            this.status = status;
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
        return !isSuspend();
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        if (!isWorkingAllowed) {
            setStatus(Status.SUSPEND);
        } else {
            if (lastRecipe != null && duration > 0) {
                setStatus(Status.WORKING);
            } else {
                setStatus(Status.IDLE);
            }
        }
    }

    @Override
    public int getMaxProgress() {
        return duration;
    }

    public boolean isActive() {
        return isWorking() || isWaiting() || (isSuspend() && lastRecipe != null && duration > 0);
    }

    @Deprecated
    public boolean isHasNotEnoughEnergy() {
        return isWaiting();
    }

    public void onRecipeFinish() {
        machine.afterWorking();
        if (lastRecipe != null) {
            lastRecipe.postWorking(this.machine);
            lastRecipe.handleRecipeIO(IO.OUT, this.machine);
            if (machine.alwaysTryModifyRecipe()) {
                if (lastOriginRecipe != null) {
                    var modified = machine.fullModifyRecipe(lastOriginRecipe);
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
            if (!recipeDirty &&
                    lastRecipe.matchRecipe(this.machine).isSuccess() &&
                    lastRecipe.matchTickRecipe(this.machine).isSuccess() &&
                    lastRecipe.checkConditions(this).isSuccess()) {
                setupRecipe(lastRecipe);
            } else {
                setStatus(Status.IDLE);
                progress = 0;
                duration = 0;
            }
        }
    }

    /**
     * Interrupt current recipe without io.
     */
    public void interruptRecipe(){
        machine.afterWorking();
        if (lastRecipe != null) {
            lastRecipe.postWorking(this.machine);
            setStatus(Status.IDLE);
            progress = 0;
            duration = 0;
        }
    }

    public void inValid() {
        if (lastRecipe != null && isWorking()) {
            lastRecipe.postWorking(machine);
        }
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    //////////////////////////////////////
    //********       MISC      *********//
    //////////////////////////////////////
    @Environment(EnvType.CLIENT)
    public void updateSound() {
        if (isWorking() && machine.shouldWorkingPlaySound()) {
            var sound = machine.getRecipeType().getSound();
            if (workingSound instanceof AutoReleasedSound soundEntry) {
                if (soundEntry.soundEntry == sound && !soundEntry.isStopped()) {
                    return;
                }
                soundEntry.release();
                workingSound = null;
            }
            if (sound != null) {
                workingSound = sound.playAutoReleasedSound(() ->
                        machine.shouldWorkingPlaySound()
                                && isWorking()
                                && !getMachine().isInValid()
                                && getMachine().getLevel().isLoaded(getMachine().getPos())
                                && MetaMachine.getMachine(getMachine().getLevel(), getMachine().getPos()) == getMachine(), getMachine().getPos(), true, 0, 1, 1);
            }
        } else if (workingSound instanceof AutoReleasedSound soundEntry) {
            soundEntry.release();
            workingSound = null;
        }
    }

    @Override
    public IGuiTexture getFancyTooltipIcon() {
        if (isWaiting()) {
            return GuiTextures.INSUFFICIENT_INPUT;
        }
        return IGuiTexture.EMPTY;
    }

    @Override
    public List<Component> getFancyTooltip() {
        if (isWaiting() && waitingReason != null) {
            return List.of(waitingReason);
        }
        return Collections.emptyList();
    }

    @Override
    public boolean showFancyTooltip() {
        return isWaiting();
    }
}
