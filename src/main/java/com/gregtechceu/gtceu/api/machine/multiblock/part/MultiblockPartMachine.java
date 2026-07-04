package com.gregtechceu.gtceu.api.machine.multiblock.part;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.IRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerList;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.sync_system.annotations.ClientFieldChangeListener;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

/**
 * The base class for all multiblock parts
 */
public class MultiblockPartMachine extends MetaMachine {

    @SyncToClient
    protected final Set<BlockPos> controllerPositions = new ObjectOpenHashSet<>(8);
    protected final SortedSet<MultiblockControllerMachine> controllers = new ReferenceLinkedOpenHashSet<>(8);
    protected @Nullable String substructureName;

    private @Nullable RecipeHandlerList handlerList;

    public MultiblockPartMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    /**
     * Gets all controllers this multiblock part belongs to.
     *
     * @return An unmodifiable set containing the controllers.
     */
    @UnmodifiableView
    public SortedSet<MultiblockControllerMachine> getControllers() {
        synchronized (controllers) {
            // Necessary to rebuild the set of controllers on client-side
            if (controllers.size() != controllerPositions.size()) {
                onControllersUpdated();
            }
            return Collections.unmodifiableSortedSet(new ReferenceLinkedOpenHashSet<>(controllers));
        }
    }

    /**
     * If this multiblock part belongs to a controller at the given position
     *
     * @param controllerPos Controller position
     * @return If this multiblock part belongs to a controller at the given position
     */
    public boolean hasController(BlockPos controllerPos) {
        return controllerPositions.contains(controllerPos);
    }

    /**
     * @return If this multiblock part belongs to a formed multiblock.
     */
    public boolean isFormed() {
        return !controllerPositions.isEmpty();
    }

    /**
     * Gets the name of the main substructure this multiblock part is attached to.
     * 
     * @return The substructure name, or null.
     */
    public @Nullable String getSubstructureName() {
        return substructureName;
    }

    // Not sure if necessary, but added to match the Controller class
    @ClientFieldChangeListener(fieldName = "controllerPositions")
    public void onControllersUpdated() {
        synchronized (controllers) {
            controllers.clear();
            for (BlockPos blockPos : controllerPositions) {
                if (MetaMachine.getMachine(getLevel(), blockPos) instanceof MultiblockControllerMachine controller) {
                    controllers.add(controller);
                }
            }
        }
    }

    /**
     * Get all available recipe handlers for recipe logic.
     */
    public List<RecipeHandlerList> getRecipeHandlers() {
        return List.of(getHandlerList());
    }

    protected RecipeHandlerList getHandlerList() {
        if (handlerList == null) {
            List<IRecipeHandler<?>> handlers = new ArrayList<>();
            IO handlerIO = null;
            for (var rht : getTraitHolder().getTraitsByInterface(IRecipeHandlerTrait.class)) {
                if (handlerIO == null) handlerIO = rht.getHandlerIO();
                handlers.add(rht);
            }

            if (handlers.isEmpty()) {
                handlerList = RecipeHandlerList.NO_DATA;
            } else {
                handlerList = RecipeHandlerList.of(handlerIO, getPaintingColor(), handlers);
            }
        }
        return handlerList;
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (getLevel() instanceof ServerLevel serverLevel) {
            // Copy so we can call removedFromController (which mutates controllers) safely without CME
            Set<MultiblockControllerMachine> toIter;
            synchronized (controllers) {
                toIter = new ObjectOpenHashSet<>(controllers);
            }
            for (MultiblockControllerMachine controller : toIter) {
                if (serverLevel.isLoaded(controller.self().getBlockPos())) {
                    removedFromController(controller);
                    controller.onPartUnload();
                }
            }
        }
        controllerPositions.clear();
        synchronized (controllers) {
            controllers.clear();
        }
        substructureName = null;
    }

    /**
     * @return If this multiblock part can be shared between multiple multiblocks.
     */
    public boolean canShared(MultiblockControllerMachine controller, String substructureName) {
        return true;
    }

    /**
     * Called when the controller's recipe logic status changes
     * 
     * @param oldStatus Old recipe logic status
     * @param newStatus New recipe logic status
     */
    public void recipeLogicStatusChanged(RecipeLogic.Status oldStatus, RecipeLogic.Status newStatus) {}

    /**
     * Called when a recipe is about to be run by the controller, just before inputs are consumed.
     *
     * @return true to cancel the recipe, false to continue
     *
     * @see RecipeLogic#setupRecipe(GTRecipe)
     */
    public boolean beforeWorking(WorkableMultiblockMachine controller) {
        return true;
    }

    /**
     * Called every tick while the controller's recipe logic is working.
     *
     * @return true to interrupt and suspend the recipe, false to continue working
     *
     * @see RecipeLogic#handleRecipeWorking()
     */
    public boolean onWorking(WorkableMultiblockMachine controller) {
        return true;
    }

    /**
     * Called when the controller finishes a recipe, before outputs are produced.
     *
     * @see RecipeLogic#onRecipeFinish()
     */
    public void afterWorking(WorkableMultiblockMachine controller) {}

    /**
     * Override it to modify recipe on the fly e.g. applying overclock, change chance, etc
     *
     * @param recipe recipe from detected from GTRecipeType
     * @return modified recipe.
     *         null -- this recipe is unavailable
     */
    public @Nullable GTRecipe modifyRecipe(GTRecipe recipe) {
        return recipe;
    }

    //////////////////////////////////////
    // *** Multiblock LifeCycle ***//
    //////////////////////////////////////

    /**
     * Called when this part is removed from a multiblock.
     *
     * @param controller The controller which this part has been removed from.
     */
    @MustBeInvokedByOverriders
    public void removedFromController(MultiblockControllerMachine controller) {
        controllerPositions.remove(controller.getBlockPos());
        boolean empty;
        synchronized (controllers) {
            controllers.remove(controller);
            empty = controllers.isEmpty();
        }

        if (empty) {
            this.substructureName = null;
            MachineRenderState renderState = getRenderState();
            if (renderState.hasProperty(GTMachineModelProperties.IS_FORMED)) {
                setRenderState(renderState.setValue(GTMachineModelProperties.IS_FORMED, false));
            }
        }

        getAllTraits().forEach(t -> t.removedFromController(controller));

        syncDataHolder.markClientSyncFieldDirty("controllerPositions");
    }

    /**
     * Called when this part is added to a multiblock.
     *
     * @param controller The controller which this part has been added to
     */
    @MustBeInvokedByOverriders
    public void addedToController(MultiblockControllerMachine controller, String substructureName) {
        controllerPositions.add(controller.getBlockPos());
        synchronized (controllers) {
            controllers.add(controller);
        }
        this.substructureName = substructureName;

        syncDataHolder.markClientSyncFieldDirty("controllerPositions");
        MachineRenderState renderState = getRenderState();
        if (renderState.hasProperty(GTMachineModelProperties.IS_FORMED)) {
            setRenderState(renderState.setValue(GTMachineModelProperties.IS_FORMED, true));
        }

        getAllTraits().forEach(t -> t.addedToController(controller));
    }

    /**
     * If this part's base model can be replaced by controller when it is formed.
     */
    public boolean replacePartModelWhenFormed() {
        var renderState = getRenderState();
        return renderState.hasProperty(GTMachineModelProperties.IS_FORMED) &&
                renderState.getValue(GTMachineModelProperties.IS_FORMED);
    }

    /**
     * Called to get block appearance when this multi part is in a formed multiblock.
     *
     * @see MetaMachine#getBlockAppearance(BlockState, BlockAndTintGetter, BlockPos, Direction, BlockState, BlockPos)
     */
    @Nullable
    public BlockState getFormedAppearance(@Nullable BlockState sourceState, @Nullable BlockPos sourcePos,
                                          Direction side) {
        if (!replacePartModelWhenFormed()) return null;
        for (MultiblockControllerMachine controller : getControllers()) {
            var appearance = controller.getPartAppearance(this, side, sourceState, sourcePos);
            if (appearance != null) return appearance;
        }
        return null;
    }

    @Override
    public BlockState getBlockAppearance(BlockState state, BlockAndTintGetter level, BlockPos pos, Direction side,
                                         @Nullable BlockState sourceState, @Nullable BlockPos sourcePos) {
        if (isFormed()) {
            var appearance = getFormedAppearance(sourceState, sourcePos, side);
            if (appearance != null) return appearance;
        }
        return super.getBlockAppearance(state, level, pos, side, sourceState, sourcePos);
    }
}
