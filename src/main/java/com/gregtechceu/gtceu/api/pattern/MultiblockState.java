package com.gregtechceu.gtceu.api.pattern;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.pattern.error.PatternError;
import com.gregtechceu.gtceu.api.pattern.error.PatternStringError;
import com.gregtechceu.gtceu.api.pattern.predicates.SimplePredicate;
import com.gregtechceu.gtceu.api.pattern.util.PatternMatchContext;

import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class MultiblockState {

    public final static PatternError UNLOAD_ERROR = new PatternStringError("multiblocked.pattern.error.chunk");
    public final static PatternError UNINIT_ERROR = new PatternStringError("multiblocked.pattern.error.init");

    private BlockPos pos;
    private BlockState blockState;
    private BlockEntity tileEntity;
    private boolean tileEntityInitialized;
    @Getter
    private final PatternMatchContext matchContext;
    @Getter
    private Object2IntOpenHashMap<SimplePredicate> globalCount;
    @Getter
    private Object2IntOpenHashMap<SimplePredicate> layerCount;
    public TraceabilityPredicate predicate;
    public IO io;
    public PatternError error;
    @Getter
    @Setter
    private boolean neededFlip = false;
    @Getter
    public final Level world;
    public final BlockPos controllerPos;
    public IMultiController lastController;

    @Getter
    private Long2ObjectOpenHashMap<TraceabilityPredicate> predicateMap;

    public MultiblockState(Level world, BlockPos controllerPos) {
        this.world = world;
        this.controllerPos = controllerPos;
        this.error = UNINIT_ERROR;
        this.matchContext = new PatternMatchContext();
    }

    public void clean() {
        this.matchContext.reset();
        this.globalCount = new Object2IntOpenHashMap<>();
        this.layerCount = new Object2IntOpenHashMap<>();

        predicateMap = new Long2ObjectOpenHashMap<>();
    }

    public Map<BlockPos, TraceabilityPredicate> getPosPredicateMap() {
        return predicateMap.long2ObjectEntrySet()
                .stream()
                .collect(Collectors.toMap(
                        l -> BlockPos.of(l.getLongKey()),
                        Long2ObjectMap.Entry::getValue
                ));
    }

    public boolean update(BlockPos posIn, TraceabilityPredicate predicate) {
        this.pos = posIn;
        this.blockState = null;
        this.tileEntity = null;
        this.tileEntityInitialized = false;
        this.predicate = predicate;
        this.error = null;
        if (!world.isLoaded(posIn)) {
            error = UNLOAD_ERROR;
            return false;
        }
        return true;
    }

    public IMultiController getController() {
        if (world.isLoaded(controllerPos)) {
            if (world.getBlockEntity(controllerPos) instanceof IMachineBlockEntity machineBlockEntity &&
                    machineBlockEntity.getMetaMachine() instanceof IMultiController controller) {
                return lastController = controller;
            }
        } else {
            error = UNLOAD_ERROR;
        }
        return null;
    }

    public boolean hasError() {
        return error != null;
    }

    public void setError(PatternError error) {
        this.error = error;
        if (error != null) {
            error.setWorldState(this);
        }
    }

    public BlockState getBlockState() {
        if (this.blockState == null) {
            this.blockState = this.world.getBlockState(this.pos);
        }
        if (this.blockState == null) {
            GTCEu.LOGGER.error("could not get BlockState at " + this.pos + " in MultiblockState");
        }
        return this.blockState;
    }

    @Nullable
    public BlockEntity getTileEntity() {
        if (!getBlockState().hasBlockEntity()) {
            return null;
        }
        if (this.tileEntity == null && !this.tileEntityInitialized) {
            this.tileEntity = this.world.getBlockEntity(this.pos);
            this.tileEntityInitialized = true;
        }

        return this.tileEntity;
    }

    public BlockPos getPos() {
        return this.pos.immutable();
    }

    public BlockState getOffsetState(Direction face) {
        if (pos instanceof BlockPos.MutableBlockPos) {
            ((BlockPos.MutableBlockPos) pos).move(face);
            BlockState blockState = world.getBlockState(pos);
            ((BlockPos.MutableBlockPos) pos).move(face.getOpposite());
            return blockState;
        }
        return world.getBlockState(this.pos.relative(face));
    }

    public LongSet getCache() {
        return predicateMap.keySet();
    }

    public void addPosCache(BlockPos pos, TraceabilityPredicate predicate) {
        predicateMap.put(pos.asLong(), predicate);
    }

    public boolean isPosInCache(BlockPos pos) {
        return predicateMap.keySet().contains(pos.asLong());
    }

    public void onBlockStateChanged(BlockPos pos, BlockState state) {
        if (world instanceof ServerLevel serverLevel) {
            if (pos.equals(controllerPos)) {
                if (lastController != null) {
                    if (!state.is(lastController.self().getBlockState().getBlock())) {
                        lastController.onStructureInvalid();
                        var mwsd = MultiblockWorldSavedData.getOrCreate(serverLevel);
                        mwsd.removeMapping(this);
                    }
                }
            } else {
                IMultiController controller = getController();
                if (controller == null && error == UNLOAD_ERROR) {
                    if (!serverLevel.isLoaded(controllerPos)) {
                        GTCEu.LOGGER.info("Controller not loaded, pos {}", controllerPos);
                    }
                }
                if (controller != null) {
                    if (controller.shouldIgnoreChange(pos, state)) {
                        return;
                    }
                    if(controller.isFormed()){
                        var predicate = predicateMap.get(pos.asLong());
                        if(predicate != null ) {
                            if (!update(pos, predicate) || !predicate.test(this)) {
                                controller.self().setFlipped(false);
                                controller.onStructureInvalid();
                                var mwsd = MultiblockWorldSavedData.getOrCreate(serverLevel);
                                mwsd.removeMapping(this);
                                mwsd.addAsyncLogic(controller);
                                return;
                            }
                        }
                    }

                    if (controller.checkPatternWithLock()) {
                        // refresh structure
                        controller.self().setFlipped(this.neededFlip);
                        controller.onStructureFormed();
                    } else {
                        // invalid structure
                        controller.self().setFlipped(false);
                        controller.onStructureInvalid();
                        var mwsd = MultiblockWorldSavedData.getOrCreate(serverLevel);
                        mwsd.removeMapping(this);
                        mwsd.addAsyncLogic(controller);
                    }
                }
            }
        }
    }
}
