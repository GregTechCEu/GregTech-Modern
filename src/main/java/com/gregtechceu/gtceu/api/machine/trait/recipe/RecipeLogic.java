package com.gregtechceu.gtceu.api.machine.trait.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.ConsumedInputsData;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.sound.AutoReleasedSound;
import com.gregtechceu.gtceu.api.sync_system.ClassSyncData;
import com.gregtechceu.gtceu.api.sync_system.annotations.ClientFieldChangeListener;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.gtceu.ChanceCacheTransformer;
import com.gregtechceu.gtceu.common.cover.MachineControllerCover;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.*;

public class RecipeLogic extends MachineTrait implements IWorkable {

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

    public @Nullable List<GTRecipe> lastFailedMatches;

    @Getter
    @SaveField
    @SyncToClient
    private Status status = Status.IDLE;

    @SaveField
    @SyncToClient
    @RerenderOnChanged
    protected boolean isActive;

    /**
     * Why the machine isn't running: either why the in-flight recipe stalled, or
     * why the closest-matching candidate was rejected. Cleared once the machine starts working again.
     */
    @Nullable
    @Getter
    protected Component bestFailureReason;

    /** The recipe {@link #bestFailureReason} belongs to. */
    @Nullable
    @Getter
    protected GTRecipe bestFailureRecipe;

    @Getter
    protected double bestFailureScore = Double.NEGATIVE_INFINITY;
    /**
     * unsafe, it may not be found from {@link RecipeManager}. Do not index it.
     */
    @Nullable
    @Getter
    @SaveField
    protected GTRecipe lastRecipe;
    @Nullable
    @Getter
    @SaveField
    @SyncToClient
    protected GTRecipe lastUnrolledRecipe;
    /**
     * safe, it is the origin recipe before {@link IRecipeLogicMachine#fullModifyRecipe(GTRecipe)}'
     * which can be found
     * from {@link RecipeManager}.
     */
    @Nullable
    @Getter
    @SaveField
    protected GTRecipe lastOriginRecipe;

    @Nullable
    @Getter
    protected GTRecipe startingRecipe;

    @Getter
    @SaveField
    protected int consecutiveRecipes = 0; // Consecutive recipes that have been run

    @SaveField
    @Getter
    protected int progress;
    @Getter
    @SaveField
    protected int duration;
    @Getter(onMethod_ = @VisibleForTesting)
    protected boolean recipeDirty;
    @SaveField
    @Getter
    protected long totalContinuousRunningTime;
    protected int runAttempt = 0;
    protected int runDelay = 0;
    @SaveField
    @Getter
    @Setter
    protected boolean suspendAfterFinish = false;
    @Getter
    @SaveField(nbtKey = "chance_cache")
    protected final IdentityHashMap<RecipeCapability<?>, Object2IntMap<?>> chanceCaches = makeChanceCaches();
    protected @Nullable TickableSubscription subscription;
    protected @Nullable Object workingSound;

    /**
     * If recipe progress should decrease when machine is waiting for pertick ingredients. (e.g. lack of EU)
     */
    @Getter
    @Setter
    protected boolean regressWhenWaiting;

    /**
     * Whether the recipe logic should keep subscribing tick logic when no recipe is available after one cycle.
     * if false. you should call {@link RecipeLogic#updateTickSubscription()} manually later to active recipe logic
     * again.
     */
    @Getter
    @Setter
    protected boolean keepSubscribing = true;

    /**
     * If recipe modifiers should always been applied before setting up a recipe.<br>
     * If true, recipe modifiers will always be applied, even if the previous recipe can be run again.<br>
     * If false, the previous recipe will be run again without reapplying modifiers.<br>
     * Defaults to true, so that recipes will always attempt to update OC, parallels, etc.
     */
    protected boolean alwaysTryModifyRecipe = true;

    @Getter
    @SaveField
    protected ConsumedInputsData consumedInputs = new ConsumedInputsData();

    public RecipeLogic() {
        super();
    }

    public IRecipeLogicMachine getRLMachine() {
        return (IRecipeLogicMachine) getMachine();
    }

    @Override
    protected List<Class<?>> validMachineClasses() {
        return List.of(IRecipeLogicMachine.class);
    }

    @SuppressWarnings("unused")
    @ClientFieldChangeListener(fieldName = "status")
    protected void onStatusSynced() {
        scheduleRenderUpdate();
        updateSound();
    }

    /**
     * Call it to abort current recipe and reset the first state.
     */
    public void resetRecipeLogic() {
        recipeDirty = false;
        lastRecipe = null;
        lastUnrolledRecipe = null;
        lastOriginRecipe = null;
        consecutiveRecipes = 0;
        progress = 0;
        duration = 0;
        isActive = false;
        lastFailedMatches = null;
        clearFailureReason();
        if (status != Status.SUSPEND) {
            setStatus(Status.IDLE);
        }
        updateTickSubscription();
        getSyncDataHolder().resyncAllFields();
    }

    @Override
    public void onTraitAttached() {
        super.onTraitAttached();
        regressWhenWaiting = getMachine().getDefinition().isRegressWhenWaiting();
    }

    @Override
    public void onMachineLoad() {
        super.onMachineLoad();
        updateTickSubscription();
    }

    public void updateTickSubscription() {
        if (isSuspend() || !getRLMachine().isRecipeLogicAvailable()) {
            if (subscription != null) {
                subscription.unsubscribe();
                subscription = null;
            }
        } else {
            subscription = getMachine().subscribeServerTick(subscription, this::serverTick);
        }
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void setProgressDelta(int delta) {
        setProgress(getProgress() + delta);
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
                    if (runDelay > 0) {
                        runDelay--;
                    } else {
                        handleRecipeWorking();
                    }
                }
                if (progress >= duration) {
                    onRecipeFinish();
                }
            } else if (lastRecipe != null) {
                findAndHandleRecipe();
            } else if (!keepSubscribing || getMachine().getOffsetTimer() % 5 == 0) {
                findAndHandleRecipe();
                if (lastFailedMatches != null) {
                    for (GTRecipe match : lastFailedMatches) {
                        if (checkMatchedRecipeAvailable(match)) break;
                    }
                }
            }
        }
        boolean unsubscribe = false;
        if (isSuspend()) {
            // Machine is paused and can unsubscribe
            unsubscribe = true;
        } else if (lastRecipe == null && isIdle() && !keepSubscribing && !recipeDirty &&
                lastFailedMatches == null) {
                    // No recipes available and the machine wants to unsubscribe until notified
                    unsubscribe = true;
                }
        if (unsubscribe && subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    protected ActionResult matchRecipe(GTRecipe recipe) {
        return RecipeHelper.matchContents(getRLMachine(), recipe);
    }

    protected ActionResult checkRecipe(GTRecipe recipe) {
        var conditionResult = RecipeHelper.checkConditions(recipe, this);
        if (!conditionResult.isSuccess()) return conditionResult;

        return matchRecipe(recipe);
    }

    public boolean checkMatchedRecipeAvailable(GTRecipe match) {
        var modified = getRLMachine().fullModifyRecipe(match);
        if (modified != null) {
            var recipeMatch = checkRecipe(modified);
            if (recipeMatch.isSuccess()) {
                setupRecipe(modified);
            } else {
                recordFailureReason(match, recipeMatch.reason(), recipeMatch.score());
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
        assert lastRecipe != null;
        var conditionResult = RecipeHelper.checkConditions(lastRecipe, this);
        if (conditionResult.isSuccess()) {
            var handleTick = handleTickRecipe(lastRecipe);
            if (handleTick.isSuccess()) {
                setStatus(Status.WORKING);
                if (!getRLMachine().onWorking()) {
                    this.interruptRecipe();
                    return;
                }
                progress++;
                totalContinuousRunningTime++;
            } else {
                setWaiting(handleTick.reason());

                // Machine isn't getting enough power, suspend after 5 attempts.
                if (handleTick.io() == IO.IN && handleTick.capability() == EURecipeCapability.CAP) {
                    runAttempt++;
                    runAttempt = (int) GTMath.clamp(runAttempt, 0, 5);
                    if (runAttempt == 5) {
                        boolean preventPowerFail = false;
                        if (getMachine() instanceof MultiblockControllerMachine) {
                            var covers = getMachine().getCoverContainer().getCovers();
                            for (var cover : covers) {
                                if (cover instanceof MachineControllerCover mcc) {
                                    if (mcc.preventPowerFail()) {
                                        preventPowerFail = true;
                                        break;
                                    }
                                }
                            }
                        }

                        if (getMachine() instanceof MultiblockControllerMachine && !preventPowerFail) {
                            runAttempt = 0;
                            // The reason recorded by setWaiting above carries over into SUSPEND.
                            setStatus(Status.SUSPEND);
                        }
                    }
                    runDelay = runAttempt * 60;
                }
            }
        } else {
            setWaiting(conditionResult.reason());
        }
        if (isWaiting() || isSuspend()) {
            regressRecipe();
        }
    }

    protected void regressRecipe() {
        if (progress > 0 && regressWhenWaiting) {
            this.progress = 1;
        }
    }

    public Iterator<GTRecipe> searchRecipe() {
        return getRLMachine().getRecipeType().searchRecipe(getRLMachine(), r -> true);
    }

    public void findAndHandleRecipe() {
        lastFailedMatches = null;
        clearFailureReason();

        // try to execute last recipe if possible
        GTRecipe last = lastUnrolledRecipe;
        if (!recipeDirty && last != null) {
            var lastCheck = checkRecipe(last);
            if (lastCheck.isSuccess()) {
                lastRecipe = null;
                lastUnrolledRecipe = null;
                lastOriginRecipe = null;
                setupRecipe(last);
                recipeDirty = false;
                return;
            }
            recordFailureReason(last, lastCheck.reason(), Double.POSITIVE_INFINITY);
        }

        // try to find and handle a new recipe
        lastRecipe = null;
        lastUnrolledRecipe = null;
        lastOriginRecipe = null;
        handleSearchingRecipes(searchRecipe());
        syncDataHolder.markClientSyncFieldDirty("lastUnrolledRecipe");
        recipeDirty = false;
    }

    protected void handleSearchingRecipes(Iterator<GTRecipe> matches) {
        while (matches.hasNext()) {
            GTRecipe match = matches.next();

            // If a new recipe was found, cache found recipe.
            if (checkMatchedRecipeAvailable(match))
                return;

            if (!matchRecipe(match).isSuccess()) {
                continue;
            }

            // cache matching recipes.
            if (lastFailedMatches == null) {
                lastFailedMatches = new ArrayList<>();
            }
            lastFailedMatches.add(match);
        }
    }

    public ActionResult handleTickRecipe(GTRecipe recipe) {
        if (!recipe.hasTick()) return ActionResult.SUCCESS;

        var result = RecipeHelper.matchTickRecipe(getRLMachine(), recipe);
        if (!result.isSuccess()) return result;

        if (lastUnrolledRecipe == null) {
            GTCEu.LOGGER.warn("Last Displayed Recipe is null! Ingredients may roll incorrectly.");
            this.lastUnrolledRecipe = lastRecipe.copy();
            syncDataHolder.markClientSyncFieldDirty("lastUnrolledRecipe");
            markLastRecipeDirty();
        }
        GTRecipe runningRecipe = RecipeHelper.doTickPrerolls(recipe, chanceCaches, lastUnrolledRecipe);

        result = handleTickRecipeIO(runningRecipe, IO.IN);
        if (!result.isSuccess()) return result;

        result = handleTickRecipeIO(runningRecipe, IO.OUT);
        return result;
    }

    public void setupRecipe(GTRecipe recipe) {
        if (!getRLMachine().beforeWorking(recipe)) {
            setStatus(Status.IDLE);
            consecutiveRecipes = 0;
            progress = 0;
            duration = 0;
            isActive = false;
            syncDataHolder.resyncAllFields();
            return;
        }
        if (lastRecipe != null && !recipe.equals(lastRecipe)) {
            chanceCaches.clear();
        }
        lastUnrolledRecipe = recipe.copy();
        syncDataHolder.markClientSyncFieldDirty("lastUnrolledRecipe");
        GTRecipe runningRecipe = RecipeHelper.doPrerolls(recipe, chanceCaches);
        startingRecipe = runningRecipe;
        consumedInputs.clear();
        var handledIO = handleRecipeIO(runningRecipe, IO.IN);
        if (handledIO.isSuccess()) {
            if (lastRecipe != null && !runningRecipe.equals(lastRecipe)) {
                chanceCaches.clear();
            }
            clearFailureReason();
            recipeDirty = false;
            lastRecipe = runningRecipe;
            setStatus(Status.WORKING);
            progress = 0;
            duration = runningRecipe.duration;
            isActive = true;
            syncDataHolder.resyncAllFields();
        } else {
            lastRecipe = null;
            lastUnrolledRecipe = null;
            syncDataHolder.markClientSyncFieldDirty("lastUnrolledRecipe");
        }
    }

    public void setStatus(Status status) {
        if (isRemote()) return;
        if (this.status != status) {
            if (this.status == Status.WORKING) {
                this.totalContinuousRunningTime = 0;
            }
            if ((status == Status.WAITING || status == Status.SUSPEND) && suspendAfterFinish) {
                status = Status.SUSPEND;
                suspendAfterFinish = false;
            }
            getRLMachine().recipeLogicStatusChanged(this.status, status);
            this.status = status;
            syncDataHolder.markClientSyncFieldDirty("status");
            setRenderState(getRenderState().setValue(GTMachineModelProperties.RECIPE_LOGIC_STATUS, status));
            updateTickSubscription();
            if (this.status == Status.WORKING || this.status == Status.IDLE) {
                clearFailureReason();
            }
        }
    }

    public void setWaiting(@Nullable Component reason) {
        setStatus(Status.WAITING);
        clearFailureReason();
        recordFailureReason(lastRecipe, reason, Double.POSITIVE_INFINITY);
        getRLMachine().onWaiting();
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
        if (isRemote()) return;
        if (!isWorkingAllowed && getStatus() == Status.IDLE) {
            clearFailureReason();
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
        getRLMachine().afterWorking();
        if (lastRecipe != null) {
            runAttempt = 0;
            runDelay = 0;
            consecutiveRecipes++;
            handleRecipeIO(lastRecipe, IO.OUT);
            // Don't ready the next recipe after finish if suspend is set
            // so that the modifiers won't be applied until re-starting.
            if (suspendAfterFinish) {
                setStatus(Status.SUSPEND);
                consecutiveRecipes = 0;
                progress = 0;
                duration = 0;
                isActive = false;
                // Force a recipe recheck.
                lastRecipe = null;
                lastUnrolledRecipe = null;
                syncDataHolder.resyncAllFields();
                return;
            }
            if (alwaysTryModifyRecipe) {
                if (lastOriginRecipe != null) {
                    var modified = getRLMachine().fullModifyRecipe(lastOriginRecipe.copy());
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
            var recipeCheck = checkRecipe(lastRecipe);
            if (!recipeDirty && recipeCheck.isSuccess()) {
                setupRecipe(lastRecipe);
            } else {
                setStatus(Status.IDLE);
                consecutiveRecipes = 0;
                progress = 0;
                duration = 0;
                isActive = false;
                syncDataHolder.resyncAllFields();
            }
        }
    }

    protected ActionResult handleRecipeIO(GTRecipe recipe, IO io) {
        return RecipeHelper.handleRecipeIO(getRLMachine(), recipe, io, this.chanceCaches);
    }

    protected ActionResult handleTickRecipeIO(GTRecipe recipe, IO io) {
        return RecipeHelper.handleTickRecipeIO(getRLMachine(), recipe, io, this.chanceCaches);
    }

    /**
     * Interrupt current recipe without io.
     */
    public void interruptRecipe() {
        getRLMachine().afterWorking();
        if (lastRecipe != null) {
            setStatus(Status.IDLE);
            progress = 0;
            duration = 0;
        }
    }

    //////////////////////////////////////
    // ******** MISC *********//
    //////////////////////////////////////
    @OnlyIn(Dist.CLIENT)
    public void updateSound() {
        if (isWorking() && getRLMachine().shouldWorkingPlaySound()) {
            var sound = getRLMachine().getRecipeType().getSound();
            if (workingSound instanceof AutoReleasedSound soundEntry) {
                if (soundEntry.soundEntry == sound && !soundEntry.isStopped()) {
                    return;
                }
                soundEntry.release();
                workingSound = null;
            }
            if (sound != null) {
                workingSound = sound.playAutoReleasedSound(
                        () -> getRLMachine().shouldWorkingPlaySound() && isWorking() && !getMachine().isRemoved() &&
                                getMachine().getLevel().isLoaded(getMachine().getBlockPos()) &&
                                MetaMachine.getMachine(getMachine().getLevel(), getMachine().getBlockPos()) ==
                                        getMachine(),
                        getMachine().getBlockPos(), true, 0, 1, 1);
            }
        } else if (workingSound instanceof AutoReleasedSound soundEntry) {
            soundEntry.release();
            workingSound = null;
        }
    }

    protected IdentityHashMap<RecipeCapability<?>, Object2IntMap<?>> makeChanceCaches() {
        IdentityHashMap<RecipeCapability<?>, Object2IntMap<?>> map = new IdentityHashMap<>();
        for (RecipeCapability<?> cap : GTRegistries.RECIPE_CAPABILITIES) {
            map.put(cap, cap.makeChanceCache());
        }
        return map;
    }

    static {
        ClassSyncData.getClassData(RecipeLogic.class).setCustomTransformerForField("chanceCaches",
                new ChanceCacheTransformer());
    }

    public static void putFailureReason(Object machine, GTRecipe recipe, Component reason) {
        if (machine instanceof IRecipeLogicMachine rlm) {
            putFailureReason(rlm.getRecipeLogic(), recipe, reason, Double.POSITIVE_INFINITY);
        }
    }

    public static void putFailureReason(RecipeLogic logic, GTRecipe recipe, Component reason, double score) {
        logic.recordFailureReason(recipe, reason, score);
    }

    /**
     * Record a failure reason as the one to display, along with the recipe it belongs to.
     */
    protected void recordFailureReason(@Nullable GTRecipe recipe, @Nullable Component reason, double score) {
        if (reason != null && !reason.getString().isBlank()) {
            if (score > bestFailureScore) {
                bestFailureScore = score;
                bestFailureReason = reason;
                bestFailureRecipe = recipe;
            }
        }
    }

    /** Forget the currently-displayed failure reason. */
    protected void clearFailureReason() {
        bestFailureReason = null;
        bestFailureRecipe = null;
        bestFailureScore = Double.NEGATIVE_INFINITY;
    }
}
