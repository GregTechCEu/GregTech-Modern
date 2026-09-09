package com.gregtechceu.gtceu.api.machine.multiblock;

import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.feature.IMufflableMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.feature.IRecipeLogicModifierTrait;
import com.gregtechceu.gtceu.api.machine.trait.recipe.IRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerList;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.common.machine.trait.CleanroomReceiverTrait;
import com.gregtechceu.gtceu.common.mui.GTMultiblockTextUtil;
import com.gregtechceu.gtceu.utils.ISubscription;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.PanelSyncManager;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.*;

/**
 * The base class for multiblocks with recipe logic.
 */
public abstract class WorkableMultiblockMachine extends MultiblockControllerMachine
                                                implements IRecipeLogicMachine, IMufflableMachine {

    @Getter
    protected final CleanroomReceiverTrait cleanroomReceiver;
    @Getter
    @SaveField
    @SyncToClient
    public final RecipeLogic recipeLogic;
    @Getter
    private final GTRecipeType[] recipeTypes;
    @Getter
    @Setter
    @SaveField
    private int activeRecipeType;
    @Getter
    protected final Map<IO, List<RecipeHandlerList>> capabilitiesProxy;
    @Getter
    protected final Map<IO, Map<RecipeCapability<?>, List<IRecipeHandler<?>>>> capabilitiesFlat;
    protected final List<ISubscription> traitSubscriptions;
    @Getter
    @SaveField
    @SyncToClient
    protected boolean isMuffled;
    protected boolean previouslyMuffled = true;
    @Nullable
    @Getter
    protected LongSet activeBlocks;

    @Getter
    @SaveField
    @SyncToClient
    protected VoidingMode voidingMode = VoidingMode.VOID_NONE;

    public WorkableMultiblockMachine(BlockEntityCreationInfo info,
                                     RecipeLogic recipeLogic) {
        super(info);
        this.recipeTypes = getDefinition().getRecipeTypes();
        this.activeRecipeType = 0;
        this.cleanroomReceiver = attachTrait(new CleanroomReceiverTrait());
        this.recipeLogic = attachTrait(recipeLogic);
        this.recipeLogic.setKeepSubscribing(false);
        this.capabilitiesProxy = new EnumMap<>(IO.class);
        this.capabilitiesFlat = new EnumMap<>(IO.class);
        this.traitSubscriptions = new ArrayList<>();
    }

    public WorkableMultiblockMachine(BlockEntityCreationInfo info) {
        this(info, new RecipeLogic());
    }

    public void setMuffled(boolean muffled) {
        isMuffled = muffled;
        syncDataHolder.markClientSyncFieldDirty("isMuffled");
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    @Override
    public void onUnload() {
        super.onUnload();
        traitSubscriptions.forEach(ISubscription::unsubscribe);
        traitSubscriptions.clear();
    }

    //////////////////////////////////////
    // *** Multiblock LifeCycle ***//
    //////////////////////////////////////
    @Override
    @MustBeInvokedByOverriders
    public void formStructure(String substructureName) {
        super.formStructure(substructureName);
        // attach parts' traits
        var cache = patternStates.get(substructureName).getCache();
        for (var entry : cache.long2ObjectEntrySet()) {
            if (entry.getValue().getBlockState().getBlock() instanceof ActiveBlock) {
                if (activeBlocks == null) activeBlocks = new LongOpenHashSet();
                activeBlocks.add(entry.getLongKey());
            }
        }

        capabilitiesProxy.clear();
        capabilitiesFlat.clear();
        traitSubscriptions.forEach(ISubscription::unsubscribe);
        traitSubscriptions.clear();
        for (MultiblockPartMachine part : getParts()) {

            var handlerLists = part.getRecipeHandlers();
            for (var handlerList : handlerLists) {
                this.addHandlerList(handlerList);
                traitSubscriptions.add(handlerList.subscribe(recipeLogic::updateTickSubscription));
            }
        }

        // attach self traits
        Map<IO, List<IRecipeHandler<?>>> ioTraits = new EnumMap<>(IO.class);
        for (var trait : getTraitHolder().getTraitsByInterface(IRecipeHandlerTrait.class)) {
            ioTraits.computeIfAbsent(trait.getHandlerIO(), i -> new ArrayList<>()).add(trait);
        }

        for (var entry : ioTraits.entrySet()) {
            var handlerList = RecipeHandlerList.of(entry.getKey(), entry.getValue());
            this.addHandlerList(handlerList);
            traitSubscriptions.add(handlerList.subscribe(recipeLogic::updateTickSubscription));
        }
        // schedule recipe logic
        recipeLogic.updateTickSubscription();
    }

    @Override
    @MustBeInvokedByOverriders
    public void invalidateStructure(String name) {
        super.invalidateStructure(name);
        updateActiveBlocks(false);
        activeBlocks = null;
        capabilitiesProxy.clear();
        capabilitiesFlat.clear();
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
        capabilitiesFlat.clear();
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

            recipeLogic.updateSound();
        }
    }

    @Nullable
    @Override
    @MustBeInvokedByOverriders
    public GTRecipe doModifyRecipe(GTRecipe recipe) {
        recipe = self().getDefinition().getRecipeModifier().applyModifier(self(), recipe);
        if (recipe == null) return null;

        for (MultiblockPartMachine part : getParts()) {
            recipe = part.modifyRecipe(recipe);
            if (recipe == null) return null;
        }

        for (var rlTrait : self().getTraitHolder().getTraitsByInterface(IRecipeLogicModifierTrait.class)) {
            recipe = rlTrait.modifyRecipe(recipe);
            if (recipe == null) return null;
        }
        return recipe;
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
    public void recipeLogicStatusChanged(RecipeLogic.Status oldStatus, RecipeLogic.Status newStatus) {
        IRecipeLogicMachine.super.recipeLogicStatusChanged(oldStatus, newStatus);
        if (shouldUpdateActiveBlocks()) {
            updateActiveBlocks(newStatus == RecipeLogic.Status.WORKING);
        }
        for (MultiblockPartMachine part : getParts()) {
            part.recipeLogicStatusChanged(oldStatus, newStatus);
            MachineRenderState state = part.getRenderState();
            if (state.hasProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS)) {
                part.setRenderState(state.setValue(GTMachineModelProperties.RECIPE_LOGIC_STATUS, newStatus));
            }
        }
    }

    /**
     * If the multiblock should update all active blocks in its structure
     */
    public boolean shouldUpdateActiveBlocks() {
        return true;
    }

    @Override
    public boolean isRecipeLogicAvailable() {
        return isFormed && !getDefaultPatternState().hasErrors();
    }

    @Override
    public void afterWorking() {
        for (MultiblockPartMachine part : getParts()) {
            part.afterWorking(this);
        }
        IRecipeLogicMachine.super.afterWorking();
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        for (MultiblockPartMachine part : getParts()) {
            if (!part.beforeWorking(this)) {
                return false;
            }
        }
        return IRecipeLogicMachine.super.beforeWorking(recipe);
    }

    @Override
    public boolean onWorking() {
        for (MultiblockPartMachine part : getParts()) {
            if (!part.onWorking(this)) {
                return false;
            }
        }
        return IRecipeLogicMachine.super.onWorking();
    }

    public GTRecipeType getRecipeType() {
        int index = activeRecipeType >= 0 && activeRecipeType < recipeTypes.length ? activeRecipeType : 0;
        return recipeTypes[index];
    }

    public void cycleActiveRecipeType() {
        int newActiveRecipeType = (getActiveRecipeType() + 1) % recipeTypes.length;
        setActiveRecipeType(newActiveRecipeType);
        getRecipeLogic().updateTickSubscription();
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

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = super.getWidgetsForDisplay(syncManager);
        widgets.add(GTMultiblockTextUtil.addUnformedWarning(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addProgressLine(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addWorkingStatusLine(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addRecipeTypeField(this, syncManager));
        widgets.addAll(getDefinition().getAdditionalDisplay().apply(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addParallelLine(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addBatchModeLine(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addSubtickParallelsLine(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addTotalRunsLine(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addOutputLines(this, syncManager));
        widgets.addAll(GTMultiblockTextUtil.addRecipeFailReasonLines(this, syncManager));
        return widgets;
    }
}
