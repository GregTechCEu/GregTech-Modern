package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyTooltip;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.sound.AutoReleasedSound;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.syncdata.IEnhancedManaged;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.UpdateListener;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.*;

public class RecipeLogic extends MachineTrait implements IEnhancedManaged, IWorkable, IFancyTooltip {

    public enum Status {
        IDLE,
        WORKING,
        WAITING,
        SUSPEND
    }

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(RecipeLogic.class);

    public final IRecipeLogicMachine machine;
    public List<GTRecipe> lastFailedMatches;

    @Getter
    @Persisted
    @DescSynced
    @UpdateListener(methodName = "onStatusSynced")
    private Status status = Status.IDLE;

    @Persisted
    @DescSynced
    @UpdateListener(methodName = "onActiveSynced")
    protected boolean isActive;

    @Nullable
    @Persisted
    @DescSynced
    private Component waitingReason = null;
    /**
     * unsafe, it may not be found from {@link RecipeManager}. Do not index it.
     */
    @Nullable
    @Getter
    @Persisted
    @DescSynced
    protected GTRecipe lastRecipe;
    @Getter
    @Persisted
    @DescSynced
    protected int consecutiveRecipes = 0; // Consecutive recipes that have been run
    /**
     * safe, it is the origin recipe before {@link IRecipeLogicMachine#fullModifyRecipe(GTRecipe)}'
     * which can be found
     * from {@link RecipeManager}.
     */
    @Nullable
    @Getter
    @Persisted
    protected GTRecipe lastOriginRecipe;
    @Persisted
    @Getter
    @Setter
    protected int progress;
    @Getter
    @Persisted
    protected int duration;
    @Getter
    @Persisted
    protected int fuelTime;
    @Getter
    @Persisted
    protected int fuelMaxTime;
    @Getter(onMethod_ = @VisibleForTesting)
    protected boolean recipeDirty;
    @Persisted
    @Getter
    protected long totalContinuousRunningTime;
    @Persisted
    @Setter
    protected boolean suspendAfterFinish = false;
    @Getter
    protected final Map<RecipeCapability<?>, Object2IntMap<?>> chanceCaches = makeChanceCaches();
    protected TickableSubscription subscription;
    protected Object workingSound;

    public RecipeLogic(IRecipeLogicMachine machine) {
        super(machine.self());
        this.machine = machine;
    }

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("unused")
    protected void onStatusSynced(Status newValue, Status oldValue) {
        getMachine().scheduleRenderUpdate();
        updateSound();
    }

    @OnlyIn(Dist.CLIENT)
    protected void onActiveSynced(boolean newActive, boolean oldActive) {
        getMachine().scheduleRenderUpdate();
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
        consecutiveRecipes = 0;
        progress = 0;
        duration = 0;
        isActive = false;
        fuelTime = 0;
        lastFailedMatches = null;
        if (status != Status.SUSPEND)
            status = Status.IDLE;
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

    public void serverTick() {
        if (!isSuspend()) {
            if (!isIdle() && lastRecipe != null) {
                if (progress < duration) {
                    handleRecipeWorking();
                }
                if (progress >= duration) {
                    onRecipeFinish();
                }
            } else if (lastRecipe != null) {
                findAndHandleRecipe();
            } else if (!machine.keepSubscribing() || getMachine().getOffsetTimer() % 5 == 0) {
                findAndHandleRecipe();
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
            boolean unsubscribe = false;
            if (isSuspend()) {
                unsubscribe = true;
            } else if (lastRecipe == null && isIdle() && !machine.keepSubscribing() && !recipeDirty &&
                    lastFailedMatches == null) {
                        // machine isn't working enabled
                        // or
                        // there is no available recipes, so it will wait for notification.
                        unsubscribe = true;
                    }

            if (unsubscribe && subscription != null) {
                subscription.unsubscribe();
                subscription = null;
            }
        }
    }

    public boolean checkMatchedRecipeAvailable(GTRecipe match) {
        var matchCopy = match.copy();
        var modified = machine.fullModifyRecipe(matchCopy);
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
                    if (!machine.onWorking()) {
                        this.interruptRecipe();
                        return;
                    }
                    progress++;
                    totalContinuousRunningTime++;
                } else {
                    setWaiting(result.reason().get());
                }
            } else {
                setWaiting(Component.translatable("gtceu.recipe_logic.insufficient_fuel"));
            }
        } else {
            setWaiting(result.reason().get());
        }
        if (isWaiting()) {
            doDamping();
        }
        if (last == Status.WORKING && getStatus() != Status.WORKING) {
            lastRecipe.postWorking(machine);
        } else if (last != Status.WORKING && getStatus() == Status.WORKING) {
            lastRecipe.preWorking(machine);
        }
    }

    protected void doDamping() {
        if (progress > 0 && machine.dampingWhenWaiting()) {
            if (ConfigHolder.INSTANCE.machines.recipeProgressLowEnergy) {
                this.progress = 1;
            } else {
                this.progress = Math.max(1, progress - 2);
            }
        }
    }

    public Iterator<GTRecipe> searchRecipe() {
        return machine.getRecipeType().searchRecipe(this.machine);
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
            lastRecipe = null;
            lastOriginRecipe = null;
            handleSearchingRecipes(searchRecipe());
        }
        recipeDirty = false;
    }

    protected void handleSearchingRecipes(Iterator<GTRecipe> matches) {
        while (matches != null && matches.hasNext()) {
            GTRecipe match = matches.next();
            if (match == null) continue;

            // If a new recipe was found, cache found recipe.
            if (checkMatchedRecipeAvailable(match))
                return;

            // cache matching recipes.
            if (lastFailedMatches == null) {
                lastFailedMatches = new ArrayList<>();
            }
            lastFailedMatches.add(match);
        }
    }

    public boolean handleFuelRecipe() {
        if (!needFuel() || fuelTime > 0) return true;
        Iterator<GTRecipe> iterator = machine.getRecipeType().searchFuelRecipe(machine);

        while (iterator != null && iterator.hasNext()) {
            GTRecipe recipe = iterator.next();
            if (recipe == null) continue;

            if (recipe.checkConditions(this).isSuccess() && handleRecipeIO(recipe, IO.IN)) {
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
                handleTickRecipeIO(recipe, IO.IN);
                handleTickRecipeIO(recipe, IO.OUT);
            } else {
                return result;
            }
        }
        return GTRecipe.ActionResult.SUCCESS;
    }

    public void setupRecipe(GTRecipe recipe) {
        if (handleFuelRecipe()) {
            if (!machine.beforeWorking(recipe)) {
                setStatus(Status.IDLE);
                consecutiveRecipes = 0;
                progress = 0;
                duration = 0;
                isActive = false;
                return;
            }
            recipe.preWorking(this.machine);
            if (handleRecipeIO(recipe, IO.IN)) {
                if (lastRecipe != null && !recipe.equals(lastRecipe)) {
                    chanceCaches.clear();
                }
                recipeDirty = false;
                lastRecipe = recipe;
                setStatus(Status.WORKING);
                progress = 0;
                duration = recipe.duration;
                isActive = true;
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
        return isWorking() || isWaiting() || (isSuspend() && isActive);
    }

    @Deprecated
    public boolean isHasNotEnoughEnergy() {
        return isWaiting();
    }

    public void onRecipeFinish() {
        machine.afterWorking();
        if (lastRecipe != null) {
            consecutiveRecipes++;
            lastRecipe.postWorking(this.machine);
            handleRecipeIO(lastRecipe, IO.OUT);
            if (machine.alwaysTryModifyRecipe()) {
                if (lastOriginRecipe != null) {
                    var modified = machine.fullModifyRecipe(lastOriginRecipe.copy());
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
            if (!recipeDirty && !suspendAfterFinish &&
                    lastRecipe.matchRecipe(this.machine).isSuccess() &&
                    lastRecipe.matchTickRecipe(this.machine).isSuccess() &&
                    lastRecipe.checkConditions(this).isSuccess()) {
                setupRecipe(lastRecipe);
            } else {
                if (suspendAfterFinish) {
                    setStatus(Status.SUSPEND);
                    suspendAfterFinish = false;
                } else {
                    setStatus(Status.IDLE);
                }
                consecutiveRecipes = 0;
                progress = 0;
                duration = 0;
                isActive = false;
            }
        }
    }

    protected boolean handleRecipeIO(GTRecipe recipe, IO io) {
        return recipe.handleRecipeIO(io, this.machine, this.chanceCaches);
    }

    protected boolean handleTickRecipeIO(GTRecipe recipe, IO io) {
        return recipe.handleTickRecipeIO(io, this.machine, this.chanceCaches);
    }

    /**
     * Interrupt current recipe without io.
     */
    public void interruptRecipe() {
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
    // ******** MISC *********//
    //////////////////////////////////////
    @OnlyIn(Dist.CLIENT)
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
                workingSound = sound.playAutoReleasedSound(
                        () -> machine.shouldWorkingPlaySound() && isWorking() && !getMachine().isInValid() &&
                                getMachine().getLevel().isLoaded(getMachine().getPos()) &&
                                MetaMachine.getMachine(getMachine().getLevel(), getMachine().getPos()) == getMachine(),
                        getMachine().getPos(), true, 0, 1, 1);
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

    protected Map<RecipeCapability<?>, Object2IntMap<?>> makeChanceCaches() {
        Map<RecipeCapability<?>, Object2IntMap<?>> map = new IdentityHashMap<>();
        for (RecipeCapability<?> cap : GTRegistries.RECIPE_CAPABILITIES.values()) {
            map.put(cap, cap.makeChanceCache());
        }
        return map;
    }

    @Override
    public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        CompoundTag chanceCache = new CompoundTag();
        this.chanceCaches.forEach((cap, cache) -> {
            ListTag cacheTag = new ListTag();
            for (var entry : cache.object2IntEntrySet()) {
                CompoundTag compoundTag = new CompoundTag();
                var obj = cap.serializer.toNbtGeneric(cap.of(entry.getKey()));
                compoundTag.put("entry", obj);
                compoundTag.putInt("cached_chance", entry.getIntValue());
                cacheTag.add(compoundTag);
            }
            chanceCache.put(cap.name, cacheTag);
        });
        tag.put("chance_cache", chanceCache);
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        CompoundTag chanceCache = tag.getCompound("chance_cache");
        for (String key : chanceCache.getAllKeys()) {
            RecipeCapability<?> cap = GTRegistries.RECIPE_CAPABILITIES.get(key);
            if (cap == null) continue; // Necessary since we removed a RecipeCapability when nuking Create
            // noinspection rawtypes
            Object2IntMap map = this.chanceCaches.computeIfAbsent(cap, RecipeCapability::makeChanceCache);

            ListTag chanceTag = chanceCache.getList(key, Tag.TAG_COMPOUND);
            for (int i = 0; i < chanceTag.size(); ++i) {
                CompoundTag chanceKey = chanceTag.getCompound(i);
                var entry = cap.serializer.fromNbt(chanceKey.get("entry"));
                int value = chanceKey.getInt("cached_chance");
                // noinspection unchecked
                map.put(entry, value);
            }
        }
        this.chanceCaches.forEach((cap, cache) -> {
            ListTag cacheTag = new ListTag();
            for (var entry : cache.object2IntEntrySet()) {
                CompoundTag compoundTag = new CompoundTag();
                var obj = cap.serializer.toNbtGeneric(cap.of(entry.getKey()));
                compoundTag.put("entry", obj);
                compoundTag.putInt("cached_chance", entry.getIntValue());
                cacheTag.add(compoundTag);
            }
            chanceCache.put(cap.name, cacheTag);
        });
        tag.put("chance_cache", chanceCache);
    }
}
