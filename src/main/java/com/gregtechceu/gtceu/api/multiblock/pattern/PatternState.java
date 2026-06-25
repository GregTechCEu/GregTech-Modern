package com.gregtechceu.gtceu.api.multiblock.pattern;

import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.multiblock.MultiblockWorldSavedData;
import com.gregtechceu.gtceu.api.multiblock.error.PatternError;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/*
 * Contains vital information to an instanced version of a structure pattern.
 */
public class PatternState {

    @Getter
    protected @Nullable BlockPos controllerPos;
    @Getter
    protected @Nullable MultiblockControllerMachine controller;
    @Getter
    @Setter
    protected boolean isFormed = false;
    @Getter
    protected volatile boolean isFlipped = false;
    @Setter
    @Getter
    protected boolean actualFlipped = false;
    @Setter
    protected boolean shouldUpdate = true;
    @Getter
    protected @Nullable List<PatternError> errors;
    @Setter
    @Getter
    protected CheckState state = CheckState.UNINITIALIZED;
    @Getter
    protected CurrentBlockInfo currentBlockInfo = new CurrentBlockInfo();
    protected final Object2IntMap<BasePredicate> globalCount = new Object2IntOpenHashMap<>();
    protected final Object2IntMap<BasePredicate> layerCount = new Object2IntOpenHashMap<>();
    @Getter
    protected final Long2ObjectMap<BlockInfo> cache = new Long2ObjectOpenHashMap<>();

    public void setController(MultiblockControllerMachine controller, BlockPos controllerPos) {
        this.controller = controller;
        this.controllerPos = controllerPos;
    }

    @ApiStatus.Internal
    public void setFlipped(boolean flipped) {
        isFlipped = flipped;
    }

    public boolean shouldUpdate() {
        return shouldUpdate;
    }

    public boolean hasErrors() {
        return errors != null;
    }

    public void setError(@Nullable PatternError error) {
        this.errors = error != null ? List.of(error) : null;
    }

    public void setErrors(@Nullable List<PatternError> error) {
        this.errors = error;
    }

    public void onBlockStateChanged(BlockPos pos, BlockState oldState, BlockState newState) {
        if (!(currentBlockInfo.getLevel() instanceof ServerLevel serverLevel)) return;
        if (pos.equals(controllerPos)) {
            if (controller != null && !newState.is(controller.self().getBlockState().getBlock())) {
                controller.invalidateStructure(MultiblockControllerMachine.DEFAULT_STRUCTURE);
                MultiblockWorldSavedData.getOrCreate(serverLevel).removeMapping(this);
            }
            // block other than controller changed
        } else if (controller != null) {
            // if the blocks that changed where active blocks changing state, don't bother rechecking
            if ((oldState.getBlock() == newState.getBlock()) &&
                    newState.getBlock() instanceof IStructureChangeIgnored &&
                    oldState.getBlock() instanceof IStructureChangeIgnored) {
                return;
            }

            for (var name : controller.getStructureNames()) {
                PatternState patternState = controller.checkStructurePattern(name);
                if (!patternState.hasErrors()) {
                    controller.formStructure(name);
                } else {
                    controller.invalidateStructure(name);
                    if (name.equals(MultiblockControllerMachine.DEFAULT_STRUCTURE)) {
                        MultiblockWorldSavedData.getOrCreate(serverLevel).removeMapping(this);
                        controller.checkAndFormStructure();
                    }
                }
            }
        }
    }

    @Getter
    public enum CheckState {

        /**
         * The cache doesn't match with the structure's data. The structure has been rechecked from scratch, is valid,
         * and the cache is now populated.
         */
        VALID_UNCACHED(true, false),

        /**
         * The cache matches the structure's data.
         */
        VALID_CACHED(true, true),

        /**
         * The cache doesn't match with the structure's data. The structure has been rechecked from scratch, is invalid,
         * and the cache is now empty.
         */
        INVALID_CACHED(false, true),

        /**
         * The cache is empty. The structure has been rechecked from scratch and is invalid, the cache remains empty.
         */
        INVALID_UNCACHED(false, false),

        /**
         * The Check State is not initialized, structure checking failed
         */
        UNINITIALIZED(false, false);

        private final boolean valid;
        private final boolean cached;

        CheckState(boolean valid, boolean cached) {
            this.valid = valid;
            this.cached = cached;
        }
    }
}
