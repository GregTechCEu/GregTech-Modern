package com.gregtechceu.gtceu.api.machine.multiblock.part;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.trait.IRecipeHandlerTrait;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    protected final Set<BlockPos> controllerPositions;

    public MultiblockPartMachine(IMachineBlockEntity holder) {
        super(holder);
        this.controllerPositions = new HashSet<>();
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

    @Override
    public List<IMultiController> getControllers() {
        List<IMultiController> result = new ArrayList<>();
        for (var blockPos : controllerPositions) {
            if (MetaMachine.getMachine(getLevel(), blockPos) instanceof IMultiController controller) {
                result.add(controller);
            }
        }
        return result;
    }

    @Override
    public List<IRecipeHandlerTrait> getRecipeHandlers() {
        return traits.stream().filter(IRecipeHandlerTrait.class::isInstance).map(IRecipeHandlerTrait.class::cast)
                .toList();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        var level = getLevel();
        for (BlockPos pos : controllerPositions) {
            if (level instanceof ServerLevel && level.isLoaded(pos) &&
                    MetaMachine.getMachine(level, pos) instanceof IMultiController controller) {
                removedFromController(controller);
                controller.onPartUnload();
            }
        }
        controllerPositions.clear();
    }

    //////////////////////////////////////
    // *** Multiblock LifeCycle ***//
    //////////////////////////////////////

    @Override
    public void removedFromController(IMultiController controller) {
        controllerPositions.remove(controller.self().getPos());
    }

    @Override
    public void addedToController(IMultiController controller) {
        controllerPositions.add(controller.self().getPos());
    }
}
