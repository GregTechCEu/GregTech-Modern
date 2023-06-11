package com.gregtechceu.gtceu.api.machine.multiblock;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.pattern.MultiblockWorldSavedData;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/3/3
 * @implNote MultiblockControllerMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiblockControllerMachine extends MetaMachine implements IMultiController {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MultiblockControllerMachine.class, MetaMachine.MANAGED_FIELD_HOLDER);
    private MultiblockState multiblockState;
    @Getter
    protected final List<IMultiPart> parts = new ArrayList<>();

    @Getter
    @Persisted
    @DescSynced
    protected boolean isFormed;

    public MultiblockControllerMachine(IMachineBlockEntity holder) {
        super(holder);
        if (isRemote()) {
            addSyncUpdateListener("isFormed", this::scheduleRender);
        }
    }

    //////////////////////////////////////
    //*****     Initialization    ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public MultiblockMachineDefinition getDefinition() {
        return (MultiblockMachineDefinition) super.getDefinition();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            MultiblockWorldSavedData.getOrCreate(serverLevel).addAsyncLogic(this);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (getLevel() instanceof ServerLevel serverLevel) {
            MultiblockWorldSavedData.getOrCreate(serverLevel).removeAsyncLogic(this);
        }
    }

    @Override
    @Nonnull
    public MultiblockState getMultiblockState() {
        if (multiblockState == null) {
            multiblockState = new MultiblockState(getLevel(), getPos());
        }
        return multiblockState;
    }

    //////////////////////////////////////
    //***    Multiblock LifeCycle    ***//
    //////////////////////////////////////
    @Override
    public void asyncCheckPattern(long periodID) {
        if ((getMultiblockState().hasError() || !isFormed) && (getHolder().getOffset() + periodID) % 4 == 0) { // per second
            var pattern = getPattern();
            if (pattern.checkPatternAt(getMultiblockState(), false)) {
                if (getLevel() instanceof ServerLevel serverLevel) {
                    serverLevel.getServer().execute(() -> {
                        if (checkPattern()) { // formed
                            onStructureFormed();
                            var mwsd = MultiblockWorldSavedData.getOrCreate(serverLevel);
                            mwsd.addMapping(getMultiblockState());
                            mwsd.removeAsyncLogic(this);
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onStructureFormed() {
        isFormed = true;
        this.parts.clear();
        this.parts.addAll(getMultiblockState().getMatchContext().getOrCreate("parts", Collections::emptySet));
        this.parts.sort(getDefinition().getPartSorter());
        for (var part : parts) {
            part.addedToController(this);
        }
    }

    @Override
    public void onStructureInvalid() {
        isFormed = false;
        for (IMultiPart part : parts) {
            part.removedFromController(this);
        }
        parts.clear();
    }

    /**
     * mark multiblockState as unload error first.
     * if it's actually cuz by block breaking.
     * {@link #onStructureInvalid()} will be called from {@link MultiblockState#onBlockStateChanged(BlockPos, BlockState)}
     */
    @Override
    public void onPartUnload() {
        parts.removeIf(part -> part.self().isInValid());
        getMultiblockState().setError(MultiblockState.UNLOAD_ERROR);
        if (getLevel() instanceof ServerLevel serverLevel) {
            MultiblockWorldSavedData.getOrCreate(serverLevel).addAsyncLogic(this);
        }
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        if (oldFacing != newFacing && getLevel() instanceof ServerLevel serverLevel) {
            // invalid structure
            this.onStructureInvalid();
            var mwsd = MultiblockWorldSavedData.getOrCreate(serverLevel);
            mwsd.removeMapping(getMultiblockState());
            mwsd.addAsyncLogic(this);
        }
    }
}
