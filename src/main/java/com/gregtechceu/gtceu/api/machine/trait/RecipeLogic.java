package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyTooltip;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.sound.AutoReleasedSound;
import com.gregtechceu.gtceu.api.sync_system.annotations.ClientFieldChangeListener;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformer;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformers;
import com.gregtechceu.gtceu.common.cover.MachineControllerCover;
import com.gregtechceu.gtceu.utils.GTMath;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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

public class RecipeLogic extends MachineTrait implements IWorkable, IFancyTooltip {

    public static final MachineTraitType<RecipeLogic> TYPE = new MachineTraitType<>(RecipeLogic.class, false);

    @Override
    public MachineTraitType<RecipeLogic> getTraitType() {
        return TYPE;
    }

    protected static class ChanceCacheMap extends IdentityHashMap<RecipeCapability<?>, Object2IntMap<?>> {}

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

    @Getter
    @Nullable
    @SaveField
    @SyncToClient
    private Component waitingReason = null;

    @Getter
    @SyncToClient
    protected final List<Component> failureReasons = new ArrayList<>();

    @Getter
    protected final Map<GTRecipe, Component> failureReasonMap = new HashMap<>();
    /**
     * unsafe, it may not be found from {@link RecipeManager}. Do not index it.
     */
    @Nullable
    @Getter
    @SaveField
    @SyncToClient
    protected GTRecipe lastRecipe;
    @Getter
    @SaveField
    @SyncToClient
    protected int consecutiveRecipes = 0; // Consecutive recipes that have been run
    /**
     * safe, it is the origin recipe before {@link IRecipeLogicMachine#fullModifyRecipe(GTRecipe)}'
     * which can be found
     * from {@link RecipeManager}.
     */
    @Nullable
    @Getter
    @SaveField
    protected GTRecipe lastOriginRecipe;
    @SaveField
    @Getter
    @SyncToClient
    protected int progress;
    @Getter
    @SyncToClient
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
    protected final ChanceCacheMap chanceCaches = makeChanceCaches();
    protected @Nullable TickableSubscription subscription;
    protected @Nullable Object workingSound;

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
        lastOriginRecipe = null;
        consecutiveRecipes = 0;
        progress = 0;
        duration = 0;
        isActive = false;
        lastFailedMatches = null;
        waitingReason = null;
        failureReasons.clear();
        if (status != Status.SUSPEND) {
            setStatus(Status.IDLE);
        }
        updateTickSubscription();
        getSyncDataHolder().resyncAllFields();
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
        syncDataHolder.markClientSyncFieldDirty("progress");
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
            } else if (!getRLMachine().keepSubscribing() || getMachine().getOffsetTimer() % 5 == 0) {
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
        } else if (lastRecipe == null && isIdle() && !getRLMachine().keepSubscribing() && !recipeDirty &&
                lastFailedMatches == null) {
                    // No recipes available and the machine wants to unsubscribe until notified
                    unsubscribe = true;
                }
        if (isIdle()) {
            failureReasons.clear();
            failureReasons.addAll(failureReasonMap.values());
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
                putFailureReason(this, match, recipeMatch.reason());
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
        if (progress > 0 && getRLMachine().regressWhenWaiting()) {
            this.progress = 1;
        }
    }

    public Iterator<GTRecipe> searchRecipe() {
        return getRLMachine().getRecipeType().searchRecipe(getRLMachine(), r -> true);
    }

    public void findAndHandleRecipe() {
        lastFailedMatches = null;

        // try to execute last recipe if possible
        if (!recipeDirty && lastRecipe != null && checkRecipe(lastRecipe).isSuccess()) {
            GTRecipe recipe = lastRecipe;
            lastRecipe = null;
            lastOriginRecipe = null;
            setupRecipe(recipe);
        } else {
            // try to find and handle a new recipe
            failureReasonMap.clear();
            lastRecipe = null;
            lastOriginRecipe = null;
            handleSearchingRecipes(searchRecipe());
        }
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

        result = handleTickRecipeIO(recipe, IO.IN);
        if (!result.isSuccess()) return result;

        result = handleTickRecipeIO(recipe, IO.OUT);
        return result;
    }

    public void setupRecipe(GTRecipe recipe) {
        if (!getRLMachine().beforeWorking(recipe)) {
            setStatus(Status.IDLE);
            consecutiveRecipes = 0;
            progress = 0;
            duration = 0;
            isActive = false;
            return;
        }
        var handledIO = handleRecipeIO(recipe, IO.IN);
        if (handledIO.isSuccess()) {
            if (lastRecipe != null && !recipe.equals(lastRecipe)) {
                chanceCaches.clear();
            }
            failureReasonMap.clear();
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
            if (this.status == Status.WORKING) {
                this.totalContinuousRunningTime = 0;
            }
            if ((status == Status.WAITING || status == Status.SUSPEND) && suspendAfterFinish) {
                status = Status.SUSPEND;
                suspendAfterFinish = false;
            }
            getRLMachine().notifyStatusChanged(this.status, status);
            this.status = status;
            syncDataHolder.markClientSyncFieldDirty("status");
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
                return;
            }
            if (getRLMachine().alwaysTryModifyRecipe()) {
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

    protected ChanceCacheMap makeChanceCaches() {
        ChanceCacheMap map = new ChanceCacheMap();
        for (RecipeCapability<?> cap : GTRegistries.RECIPE_CAPABILITIES.values()) {
            map.put(cap, cap.makeChanceCache());
        }
        return map;
    }

    static {
        ValueTransformers.registerTransformer(ChanceCacheMap.class, new ValueTransformer<ChanceCacheMap>() {

            @Override
            public Tag serializeNBT(ChanceCacheMap value,
                                    TransformerContext<ChanceCacheMap> context) {
                CompoundTag chanceCache = new CompoundTag();
                if (context.currentValue() == null) return chanceCache;

                context.currentValue().forEach((cap, cache) -> {
                    ListTag cacheTag = new ListTag();
                    for (var entry : cache.object2IntEntrySet()) {
                        CompoundTag compoundTag = new CompoundTag();
                        var obj = cap.contentToNbt(entry.getKey());
                        compoundTag.put("entry", obj);
                        compoundTag.putInt("cached_chance", entry.getIntValue());
                        cacheTag.add(compoundTag);
                    }
                    chanceCache.put(cap.name, cacheTag);
                });

                return chanceCache;
            }

            @Override
            public @Nullable ChanceCacheMap deserializeNBT(Tag tag,
                                                           TransformerContext<ChanceCacheMap> context) {
                CompoundTag chanceCache = ValueTransformer.assertTagType(CompoundTag.class, tag, context);
                if (context.currentValue() != null) {
                    for (String key : chanceCache.getAllKeys()) {
                        RecipeCapability<?> cap = GTRegistries.RECIPE_CAPABILITIES.get(key);
                        // Necessary since a RecipeCapability was removed when removing Create support, and for future
                        // removals
                        if (cap == null) continue;
                        // noinspection rawtypes
                        Object2IntMap map = context.currentValue().computeIfAbsent(cap,
                                RecipeCapability::makeChanceCache);

                        ListTag chanceTag = chanceCache.getList(key, Tag.TAG_COMPOUND);
                        for (int i = 0; i < chanceTag.size(); ++i) {
                            CompoundTag chanceKey = chanceTag.getCompound(i);
                            var entry = cap.serializer.fromNbt(chanceKey.get("entry"));
                            int value = chanceKey.getInt("cached_chance");
                            // noinspection unchecked
                            map.put(entry, value);
                        }
                    }
                }
                return context.currentValue();
            }
        });
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
