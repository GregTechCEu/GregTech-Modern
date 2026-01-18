package com.gregtechceu.gtceu.api.machine.multiblock.part;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.IRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.syncsystem.annotations.ClientFieldChangeListener;
import com.gregtechceu.gtceu.syncsystem.annotations.SyncToClient;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiblockPartMachine extends MetaMachine implements IMultiPart {

    @SyncToClient
    protected final Set<BlockPos> controllerPositions = new ObjectOpenHashSet<>(8);
    protected final SortedSet<IMultiController> controllers = new ReferenceLinkedOpenHashSet<>(8);

    private @Nullable RecipeHandlerList handlerList;

    public MultiblockPartMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    @Override
    public boolean hasController(BlockPos controllerPos) {
        return controllerPositions.contains(controllerPos);
    }

    @Override
    public boolean isFormed() {
        return !controllerPositions.isEmpty();
    }

    // Not sure if necessary, but added to match the Controller class
    @SuppressWarnings("unused")
    @ClientFieldChangeListener(fieldName = "controllerPositions")
    public void onControllersUpdated() {
        controllers.clear();
        for (BlockPos blockPos : controllerPositions) {
            if (MetaMachine.getMachine(getLevel(), blockPos) instanceof IMultiController controller) {
                controllers.add(controller);
            }
        }
    }

    @Override
    @UnmodifiableView
    public SortedSet<IMultiController> getControllers() {
        // Necessary to rebuild the set of controllers on client-side
        if (controllers.size() != controllerPositions.size()) {
            onControllersUpdated();
        }
        return Collections.unmodifiableSortedSet(controllers);
    }

    public List<RecipeHandlerList> getRecipeHandlers() {
        return List.of(getHandlerList());
    }

    protected RecipeHandlerList getHandlerList() {
        if (handlerList == null) {
            List<IRecipeHandler<?>> handlers = new ArrayList<>();
            IO handlerIO = null;
            for (var trait : traitHolder.getAllTraits()) {
                if (trait instanceof IRecipeHandlerTrait<?> rht) {
                    if (handlerIO == null) handlerIO = rht.getHandlerIO();
                    handlers.add(rht);
                }
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
            // Need to copy if > 1 so that we can call removedFromController safely without CME
            Set<IMultiController> toIter = controllers.size() > 1 ? new ObjectOpenHashSet<>(controllers) : controllers;
            for (IMultiController controller : toIter) {
                if (serverLevel.isLoaded(controller.self().getBlockPos())) {
                    removedFromController(controller);
                    controller.onPartUnload();
                }
            }
        }
        controllerPositions.clear();
        controllers.clear();
    }

    //////////////////////////////////////
    // *** Multiblock LifeCycle ***//
    //////////////////////////////////////

    @MustBeInvokedByOverriders
    @Override
    public void removedFromController(IMultiController controller) {
        controllerPositions.remove(controller.self().getBlockPos());
        controllers.remove(controller);

        if (controllers.isEmpty()) {
            MachineRenderState renderState = getRenderState();
            if (renderState.hasProperty(GTMachineModelProperties.IS_FORMED)) {
                setRenderState(renderState.setValue(GTMachineModelProperties.IS_FORMED, false));
            }
        }
        syncDataHolder.markClientSyncFieldDirty("controllerPositions");
    }

    @MustBeInvokedByOverriders
    @Override
    public void addedToController(IMultiController controller) {
        controllerPositions.add(controller.self().getBlockPos());
        controllers.add(controller);

        syncDataHolder.markClientSyncFieldDirty("controllerPositions");
        MachineRenderState renderState = getRenderState();
        if (renderState.hasProperty(GTMachineModelProperties.IS_FORMED)) {
            setRenderState(renderState.setValue(GTMachineModelProperties.IS_FORMED, true));
        }
    }

    @Override
    public boolean replacePartModelWhenFormed() {
        var renderState = getRenderState();
        return renderState.hasProperty(GTMachineModelProperties.IS_FORMED) &&
                renderState.getValue(GTMachineModelProperties.IS_FORMED);
    }

    @Override
    @Nullable
    public BlockState getFormedAppearance(BlockState sourceState, BlockPos sourcePos, Direction side) {
        if (!replacePartModelWhenFormed()) return null;
        return IMultiPart.super.getFormedAppearance(sourceState, sourcePos, side);
    }
}
