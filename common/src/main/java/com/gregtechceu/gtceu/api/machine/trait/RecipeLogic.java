package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.sound.AutoReleasedSound;
import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.syncdata.IManaged;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.crafting.RecipeManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RecipeLogic extends MachineTrait implements IManaged, IWorkable {
    public enum Status {
        IDLE, WORKING, WAITING, SUSPEND
    }

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(RecipeLogic.class);

    public final IRecipeLogicMachine machine;
    public List<GTRecipe> lastFailedMatches;

    @Persisted @DescSynced
    private Status status = Status.IDLE;

    /**
     * unsafe, it may not be found from {@link RecipeManager}. Do not index it.
     */
    @Nullable @Getter @Persisted
    public GTRecipe lastRecipe;
    @Persisted
    @Getter
    public int progress;
    @Persisted
    public int duration;
    @Persisted
    public int fuelTime;
    @Persisted
    public int fuelMaxTime;
    public long timeStamp;
    protected boolean recipeDirty;
    protected TickableSubscription subscription;
    @Environment(EnvType.CLIENT)
    protected AutoReleasedSound workingSound;

    public RecipeLogic(IRecipeLogicMachine machine) {
        super(machine.self());
        this.machine = machine;
        this.timeStamp = Long.MIN_VALUE;
        if (getMachine().isRemote()) {
            addSyncUpdateListener("status", this::onStatusSynced);
        }
    }

    @Environment(EnvType.CLIENT)
    protected void onStatusSynced(String fieldName, Status newValue, Status oldValue) {
        getMachine().scheduleRenderUpdate();
        updateSound();
    }

    /**
     * Call it to abort current recipe and reset the first state.
     */
    public void resetRecipeLogic() {
        recipeDirty = false;
        lastRecipe = null;
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
        return machine.getRecipeType().isFuelRecipeType();
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
                    handleRecipeWorking();
                    if (progress == duration) {
                        onRecipeFinish();
                    }
                }
            } else if (lastRecipe != null) {
                findAndHandleRecipe();
            } else if (!machine.keepSubscribing() || getMachine().getOffsetTimer() % 5 == 0) {
                executeDirty(this::findAndHandleRecipe);
                if (lastFailedMatches != null) {
                    for (GTRecipe recipe : lastFailedMatches) {
                        if (recipe.checkConditions(this)) {
                            setupRecipe(recipe);
                        }
                        if (lastRecipe != null && getStatus() == Status.WORKING) {
                            lastFailedMatches = null;
                            break;
                        }
                    }
                }
            }
        }
        if (fuelTime > 0) {
            fuelTime--;
        } else {
            if (isSuspend() || (lastRecipe == null && isIdle() && !machine.keepSubscribing() && !recipeDirty)) {
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

    public void handleRecipeWorking() {
        Status last = this.status;
        assert lastRecipe != null;
        if (lastRecipe.checkConditions(this) && handleFuelRecipe()) {
            if (handleTickRecipe(lastRecipe)) {
                setStatus(Status.WORKING);
                machine.onWorking();
                progress++;
            } else {
                setStatus(Status.WAITING);
                machine.onWaiting();
                if (progress > 0 && machine.dampingWhenWaiting()) {
                    progress--;
                }
            }
//            var sound = machine.getRecipeType().getSound();
//            if (sound != null && machine.self().getOffsetTimer() % 20 == 0) {
//                sound.playOnServer(machine.self().getLevel(), machine.self().getPos());
//            }
        } else {
            setStatus(Status.WAITING);
            machine.onWaiting();
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
        if (!recipeDirty && lastRecipe != null && lastRecipe.matchRecipe(this.machine) && lastRecipe.matchTickRecipe(this.machine) && lastRecipe.checkConditions(this)) {
            GTRecipe recipe = lastRecipe;
            lastRecipe = null;
            setupRecipe(recipe);
        } else { // try to find and handle a new recipe
            List<GTRecipe> matches = searchRecipe();

            lastRecipe = null;
            for (GTRecipe match : matches) {
                // try to modify recipe by machine, such as overclock, tier checking.
                match = machine.modifyRecipe(match);
                if (match == null) continue;

                if (match.checkConditions(this)) {
                    setupRecipe(match);
                }

                if (lastRecipe != null && getStatus() == Status.WORKING) {
                    lastFailedMatches = null;
                    break;
                }

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
            if (recipe.checkConditions(this) && recipe.handleRecipeIO(IO.IN, this.machine)) {
                fuelMaxTime = recipe.duration;
                fuelTime = fuelMaxTime;
            }
            if (fuelTime > 0) return true;
        }
        return false;
    }

    public boolean handleTickRecipe(GTRecipe recipe) {
        if (recipe.hasTick()) {
            if (recipe.matchTickRecipe(this.machine)) {
                recipe.handleTickRecipeIO(IO.IN, this.machine);
                recipe.handleTickRecipeIO(IO.OUT, this.machine);
            } else {
                return false;
            }
        }
        return true;
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
            machine.notifyStatusChanged(this.status, status);
            this.status = status;
            updateTickSubscription();
        }
    }

    /**
     * mark current handling recipe (if exist) as dirty.
     * do not try it immediately in the next round
     */
    public void markLastRecipeDirty() {
        this.recipeDirty = true;
    }

    public Status getStatus() {
        return status;
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

    public boolean isHasNotEnoughEnergy() {
        return isWaiting();
    }

    public void onRecipeFinish() {
        machine.afterWorking();
        if (lastRecipe != null) {
            lastRecipe.postWorking(this.machine);
            lastRecipe.handleRecipeIO(IO.OUT, this.machine);
            // try it again
            if (!recipeDirty && lastRecipe.matchRecipe(this.machine) && lastRecipe.matchTickRecipe(this.machine) && lastRecipe.checkConditions(this)) {
                setupRecipe(lastRecipe);
            } else {
                setStatus(Status.IDLE);
                progress = 0;
                duration = 0;
            }

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

    @Environment(EnvType.CLIENT)
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
                workingSound = sound.playAutoReleasedSound(() ->
                        machine.shouldWorkingPlaySound()
                                && isWorking()
                                && !getMachine().isInValid()
                                && getMachine().getLevel().isLoaded(getMachine().getPos())
                                && MetaMachine.getMachine(getMachine().getLevel(), getMachine().getPos()) == getMachine(), getMachine().getPos(), true, 0, 1, 1);
            }
        } else if (workingSound != null) {
            workingSound.release();
            workingSound = null;
        }
    }

}
