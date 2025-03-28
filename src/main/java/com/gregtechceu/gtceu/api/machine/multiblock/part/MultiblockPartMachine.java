package com.gregtechceu.gtceu.api.machine.multiblock.part;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.trait.IRecipeHandlerTrait;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.annotation.UpdateListener;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/3/4
 * @implNote MultiblockPartMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiblockPartMachine extends MetaMachine implements IMultiPart {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MultiblockPartMachine.class,
            MetaMachine.MANAGED_FIELD_HOLDER);

    @DescSynced
    @RequireRerender
    @UpdateListener(methodName = "onControllersUpdated")
    protected final Set<BlockPos> controllerPositions = new ObjectOpenHashSet<>(8);
    protected final SortedSet<IMultiController> controllers = new ReferenceLinkedOpenHashSet<>(8);

    public MultiblockPartMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

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
    public void onControllersUpdated(Set<BlockPos> newPositions, Set<BlockPos> old) {
        controllers.clear();
        for (BlockPos blockPos : newPositions) {
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
            onControllersUpdated(controllerPositions, Collections.emptySet());
        }
        return Collections.unmodifiableSortedSet(controllers);
    }

    @Override
    public List<IRecipeHandlerTrait> getRecipeHandlers() {
        return traits.stream()
                .filter(IRecipeHandlerTrait.class::isInstance)
                .map(IRecipeHandlerTrait.class::cast)
                .toList();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (getLevel() instanceof ServerLevel serverLevel) {
            // Need to copy if > 1 so that we can call removedFromController safely without CME
            Set<IMultiController> toIter = controllers.size() > 1 ? new ObjectOpenHashSet<>(controllers) : controllers;
            for (IMultiController controller : toIter) {
                if (serverLevel.isLoaded(controller.self().getPos())) {
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

    @Override
    public void removedFromController(IMultiController controller) {
        controllerPositions.remove(controller.self().getPos());
        controllers.remove(controller);
    }

    @Override
    public void addedToController(IMultiController controller) {
        controllerPositions.add(controller.self().getPos());
        controllers.add(controller);
    }
}
