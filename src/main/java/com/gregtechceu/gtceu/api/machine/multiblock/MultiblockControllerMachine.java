package com.gregtechceu.gtceu.api.machine.multiblock;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.multiblock.MultiblockWorldSavedData;
import com.gregtechceu.gtceu.api.multiblock.pattern.CurrentBlockInfo;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternState;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.sync_system.annotations.ClientFieldChangeListener;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ParallelHatchPartMachine;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class MultiblockControllerMachine extends MetaMachine {

    private CurrentBlockInfo controllerBlockInfo;
    private final List<IMultiPart> parts = new ArrayList<>();
    private @Nullable ParallelHatchPartMachine parallelHatch = null;
    @Getter
    @SyncToClient
    private BlockPos[] partPositions = new BlockPos[0];
    @Getter
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    protected boolean isFormed;
    @Getter
    @SaveField
    @SyncToClient
    protected boolean isFlipped;

    public static final String DEFAULT_STRUCTURE = "main";

    protected final Reference2ObjectMap<String, IBlockPattern> structures = new Reference2ObjectOpenHashMap<>();
    protected Reference2ObjectMap<String, PatternState> patternStates = new Reference2ObjectOpenHashMap<>();

    public MultiblockControllerMachine(BlockEntityCreationInfo info) {
        super(info);
        createStructurePatterns();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            MultiblockWorldSavedData.getOrCreate(serverLevel).addAsyncLogic(this);
            if (isFormed) {
                // run a structure check on the first tick
                asyncCheckPattern(getOffset() % 4);
            }
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (getLevel() instanceof ServerLevel serverLevel) {
            MultiblockWorldSavedData.getOrCreate(serverLevel).removeAsyncLogic(this);
            for (var pattern : patternStates.values()) {
                MultiblockWorldSavedData.getOrCreate(serverLevel).removeMapping(pattern);
            }
        }
    }

    @NotNull
    public CurrentBlockInfo getBlockInfo() {
        if (controllerBlockInfo == null) {
            controllerBlockInfo = new CurrentBlockInfo();
            controllerBlockInfo.setLevel(getLevel());
            controllerBlockInfo.setCurrentPos(getBlockPos());
        }
        return controllerBlockInfo;
    }

    public Reference2ObjectMap<String, IBlockPattern> getStructurePatterns() {
        return structures;
    }

    public void setFlipped(boolean flipped) {
        isFlipped = flipped;
        syncDataHolder.markClientSyncFieldDirty("isFlipped");
    }

    @SuppressWarnings("unused")
    @ClientFieldChangeListener(fieldName = "partPositions")
    protected void onPartsUpdated() {
        parts.clear();
        for (var pos : partPositions) {
            if (getMachine(getLevel(), pos) instanceof IMultiPart part) {
                parts.add(part);
            }
        }
    }

    protected void updatePartPositions() {
        this.partPositions = this.parts.isEmpty() ? new BlockPos[0] :
                this.parts.stream().map(part -> part.self().getBlockPos()).toArray(BlockPos[]::new);
        syncDataHolder.markClientSyncFieldDirty("partPositions");
    }

    public List<IMultiPart> getParts() {
        // for the client side, when the chunk unloaded
        if (parts.size() != this.partPositions.length) {
            parts.clear();
            for (var pos : this.partPositions) {
                if (getMachine(getLevel(), pos) instanceof IMultiPart part) {
                    parts.add(part);
                }
            }
        }
        return this.parts;
    }

    /**
     * The instance of {@link ParallelHatchPartMachine} attached to this controller.
     * <p>
     * Note that this will return a singular instance, and will not account for multiple attached IParallelHatches
     *
     * @return an {@link Optional} of the attached IParallelHatch, empty if one is not attached
     */
    public Optional<ParallelHatchPartMachine> getParallelHatch() {
        return Optional.ofNullable(parallelHatch);
    }

    //////////////////////////////////////
    // *** Multiblock LifeCycle ***//
    //////////////////////////////////////
    @Getter
    private final Lock patternLock = new ReentrantLock();

    public void asyncCheckPattern(long periodID) {
        if (getLevel() instanceof ServerLevel serverLevel) {
            if (getMachine(serverLevel, getBlockPos()) != this) {
                MultiblockWorldSavedData.getOrCreate(serverLevel).removeAsyncLogic(this);
            }
        }
        for (var entry : patternStates.entrySet()) {
            var name = entry.getKey();
            var patternState = entry.getValue();
            boolean formed = name.equals(DEFAULT_STRUCTURE) ? isFormed : patternState.isFormed();
            if ((patternState.hasError() || !formed ||
                    patternState.getState() == PatternState.CheckState.UNINITIALIZED) &&
                    (getOffset() + periodID) % 4 == 0 &&
                    checkPatternWithTryLock(name)) { // per second
                if (getLevel() instanceof ServerLevel serverLevel) {
                    serverLevel.getServer().execute(() -> {
                        patternLock.lock();
                        var mwsd = MultiblockWorldSavedData.getOrCreate(serverLevel);
                        if (checkPatternWithLock(name)) { // formed
                            formStructure(name);
                            mwsd.removeAsyncLogic(this);
                        }

                        mwsd.addMapping(patternState);

                        patternLock.unlock();
                    });
                }
            }
        }
    }

    /**
     * Check pattern with a lock.
     */
    public boolean checkPatternWithLock(String name) {
        var lock = getPatternLock();
        lock.lock();
        try {
            var patternCheckState = getPatternState(name).getState();
            if (patternCheckState == null || !patternCheckState.isValid()) {
                checkStructurePattern(name);
            }
            return getPatternState(name).getState().isValid();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Check pattern with a try lock
     *
     * @return false - checking failed or cant get the lock.
     */
    public boolean checkPatternWithTryLock(String name) {
        var lock = getPatternLock();
        if (lock.tryLock()) {
            try {
                var patternCheckState = getPatternState(name).getState();
                if (patternCheckState == null || !patternCheckState.isValid()) {
                    checkStructurePattern(name);
                }
                return getPatternState(name).getState() != PatternState.CheckState.UNINITIALIZED;
            } finally {
                lock.unlock();
            }
        } else {
            return false;
        }
    }

    /**
     * should add part to the part list.
     */
    boolean shouldAddPartToController(IMultiPart part) {
        return true;
    }

    public Set<String> getStructureNames() {
        return structures.keySet();
    }

    /**
     * The {@link MultiblockMachineDefinition} of this multiblock.
     *
     * @return The {@link MultiblockMachineDefinition}
     */
    @Override
    public MultiblockMachineDefinition getDefinition() {
        return (MultiblockMachineDefinition) super.getDefinition();
    }

    /**
     * Get structure pattern.
     * You can override it to create dynamic patterns.
     */
    public IBlockPattern createStructurePattern() {
        return getDefinition().getPatternFactory().get();
    }

    public void createStructurePatterns() {
        var defaultPattern = createStructurePattern();
        var defaultPatternState = new PatternState();
        patternStates.put(DEFAULT_STRUCTURE, defaultPatternState);
        // defaultPattern.setActivePatternState(defaultPatternState);
        structures.put(DEFAULT_STRUCTURE, defaultPattern);
    }

    public void checkAndFormStructurePatterns() {
        for (String name : structures.keySet()) {
            formStructure(name);
        }
    }

    public PatternState getDefaultPatternState() {
        return patternStates.get(DEFAULT_STRUCTURE);
    }

    public PatternState getPatternState(String name) {
        return patternStates.get(name);
    }

    public PatternState checkStructurePattern() {
        return checkStructurePattern(DEFAULT_STRUCTURE);
    }

    public PatternState checkStructurePattern(String name) {
        IBlockPattern pattern = getSubstructure(name);
        var pState = patternStates.get(name);
        if (!pState.shouldUpdate() || getLevel() == null) return pState;

        long time = System.nanoTime();
        pState.setController(this, getBlockPos());
        pattern.checkPatternFastAt(getLevel(), pState, getBlockPos(), getFrontFacing(), getUpwardsFacing(),
                allowFlip());
        // patternStates.put(name, pState);
        // pattern.setActivePatternState(pState);
        // GTCEu.LOGGER.info("Structure check for {} took {} ns", self().getDefinition().getName(),
        // (System.nanoTime() - time));
        return pState;
    }

    public void formStructure(String name) {
        var patternState = getPatternState(name);
        patternState.setFormed(true);
        if (name.equals(DEFAULT_STRUCTURE)) {
            isFormed = true;
        }

        if (patternState.getState().isValid()) {
            if (patternState.isFormed()) {
                if (patternState.getState() == PatternState.CheckState.VALID_UNCACHED) {
                    forEachMultiPart(name, part -> {
                        if (parts.contains(part)) return true;

                        if (part.hasController(getBlockPos()) && !part.canShared(this, name)) {
                            invalidateStructure(name);
                            return false;
                        }
                        return true;
                    });

                    forEachMultiPart(name, part -> {
                        if (parts.contains(part)) return true;

                        if (shouldAddPartToController(part)) {
                            this.parts.add(part);
                        }
                        return true;
                    });

                    // this.parts.sort(GTMemoizer.memoizeFunctionWeakIdent(getDefinition().getPartSorter()));
                    // this.parts.sort(getDefinition().getPartSorter());
                    for (var part : parts) {
                        if (part instanceof ParallelHatchPartMachine pHatch) {
                            parallelHatch = pHatch;
                        }
                        part.addedToController(this, name);
                    }
                    updatePartPositions();

                    patternState.setFormed(true);
                    if (name.equals(DEFAULT_STRUCTURE)) {
                        isFormed = true;
                    }
                    setFlipped(patternState.isFlipped(), patternState);
                }
                return;
            }

            boolean[] valid = new boolean[1];
            valid[0] = true;

            forEachMultiPart(name, part -> {
                if (part.hasController(getBlockPos()) && !part.canShared(this, name)) {
                    valid[0] = false;
                    return false;
                }
                return true;
            });

            if (!valid[0]) return;

            patternState.setFormed(true);
            if (name.equals(DEFAULT_STRUCTURE)) {
                isFormed = true;
                MachineRenderState renderState = getRenderState();
                if (renderState.hasProperty(GTMachineModelProperties.IS_FORMED)) {
                    setRenderState(renderState.setValue(GTMachineModelProperties.IS_FORMED, true));
                }
            }
            setFlipped(patternState.isFlipped(), patternState);

        } else {
            if (patternState.isFormed()) {
                invalidateStructure(name);
            }
        }
    }

    public void setFlipped(boolean flipped, PatternState state) {
        boolean flip = state.isActualFlipped();
        if (flip != flipped) {
            state.setActualFlipped(flipped);
            this.isFlipped = flipped;
            notifyBlockUpdate();
        }
    }

    public void invalidateStructure() {
        invalidateStructure(DEFAULT_STRUCTURE);
        isFormed = false;
    }

    public void invalidateStructure(String name) {
        var pState = patternStates.get(name);
        if (!pState.isFormed()) return;

        MachineRenderState renderState = getRenderState();
        if (renderState.hasProperty(GTMachineModelProperties.IS_FORMED)) {
            setRenderState(renderState.setValue(GTMachineModelProperties.IS_FORMED, false));
        }
        parts.removeIf(part -> {
            if (name.equals(part.getSubstructureName())) {
                part.removedFromController(this);
                return true;
            }
            return false;
        });
        pState.setFormed(false);
        if (name.equals(DEFAULT_STRUCTURE)) {
            isFormed = false;
            parallelHatch = null;
        }
        updatePartPositions();
    }

    protected void invalidStructureCaches() {
        for (var pState : patternStates.values()) {
            pState.getPosCache().clear();
        }
    }

    public IBlockPattern getSubstructure(String name) {
        return structures.get(name);
    }

    void forEachMultiPart(String name, Predicate<IMultiPart> action) {
        // var cache = getSubstructure(name).getCache();
        var cache = patternStates.get(name).getCache();
        for (BlockInfo info : cache.values()) {
            BlockEntity be = info.getBlockEntity();
            if (be instanceof IMultiPart part) {
                if (!action.test(part)) return;
            }
        }
    }

    protected void forEachFormed(String name, BiConsumer<BlockInfo, BlockPos.MutableBlockPos> action) {
        // var cache = getSubstructure(name).getCache();
        var cache = patternStates.get(name).getCache();
        var pos = new BlockPos.MutableBlockPos();
        for (var entry : cache.long2ObjectEntrySet()) {
            action.accept(entry.getValue(), pos.set(entry.getLongKey()));
        }
    }

    /**
     * mark multiblockState as unload error first.
     * if it's actually cuz by block breaking.
     * {@link #//onStructureInvalid(String)} will be called from
     * {@link #//onBlockStateChanged(BlockPos, BlockState)}
     */
    public void onPartUnload() {
        /*
         * parts.removeIf(part -> part.self().isRemoved());
         * getMultiblockState().setError(MultiblockState.UNLOAD_ERROR);
         * if (getLevel() instanceof ServerLevel serverLevel) {
         * MultiblockWorldSavedData.getOrCreate(serverLevel).addAsyncLogic(this);
         * }
         * updatePartPositions();
         */
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        if (oldFacing != newFacing && getLevel() instanceof ServerLevel serverLevel) {
            // invalid structure
            // this.onStructureInvalid();
            invalidStructureCaches();
            var mwsd = MultiblockWorldSavedData.getOrCreate(serverLevel);
            for (var patternState : patternStates.values()) {
                // var state = structure.getPatternState();
                mwsd.removeMapping(patternState);
            }
            mwsd.addAsyncLogic(this);
        }
    }

    public boolean allowFlip() {
        return getDefinition().isAllowFlip();
    }

    public @Nullable BlockState getPartAppearance(IMultiPart part, Direction side, BlockState sourceState,
                                                  BlockPos sourcePos) {
        if (isFormed()) {
            return getDefinition().getPartAppearance().apply(this, part, side);
        }
        return null;
    }

    Comparator<IMultiPart> getPartSorter() {
        return getDefinition().getPartSorter().apply(this);
    }

    @Override
    public void setUpwardsFacing(@NotNull Direction upwardsFacing) {
        if (getLevel() == null) return;
        if (!getDefinition().isAllowExtendedFacing()) return;
        BlockState blockState = getBlockState();
        if (blockState.getBlock() instanceof MetaMachineBlock &&
                blockState.getValue(GTBlockStateProperties.UPWARDS_FACING) != upwardsFacing) {
            getLevel().setBlockAndUpdate(getBlockPos(),
                    blockState.setValue(GTBlockStateProperties.UPWARDS_FACING, upwardsFacing));
            if (getLevel() != null && !getLevel().isClientSide) {
                notifyBlockUpdate();
                invalidStructureCaches();
                checkAndFormStructurePatterns();
            }
        }
    }

    /*protected InteractionResult onWrenchClick(Player playerIn, InteractionHand hand, Direction gridSide,
                                              BlockHitResult hitResult) {
        if (gridSide == getFrontFacing() && allowExtendedFacing()) {
            var newUp = getUpwardsFacing().getClockWise(getFrontFacing().getAxis());
            if (playerIn.isShiftKeyDown()) newUp = newUp.getOpposite();
            setUpwardsFacing(newUp);
            playerIn.swing(hand);
            return InteractionResult.sidedSuccess(playerIn.level().isClientSide);
        }
        if (playerIn.isShiftKeyDown()) {
            if (gridSide == getFrontFacing() || !isFacingValid(gridSide)) {
                return InteractionResult.FAIL;
            }
            if (!isRemote()) {
                setFrontFacing(gridSide);
            }
            return InteractionResult.sidedSuccess(playerIn.level().isClientSide);
        }
        return super.onWrenchClick(playerIn, hand, gridSide, hitResult);
    }*/

    @Override
    public void setFrontFacing(Direction facing) {
        super.setFrontFacing(facing);

        if (getLevel() != null && !getLevel().isClientSide) {
            invalidStructureCaches();
            checkAndFormStructurePatterns();
        }
    }

    /**
     *
     * @return Whether batching is enabled on this multiblock
     */
    public boolean isBatchEnabled() {
        return false;
    }

    public void setBatchEnabled(boolean batch) {}

    /**
     * Can be overridden to just add widgets to the black box in the middle instead of overriding the whole UI.
     * Don't forget to invoke {@code super.getWidgetsForDisplay} to add the default lines (progress, voltage, etc.).
     *
     * @param syncManager the sync manager
     * @return list of widgets to be displayed inside the black box in the middle of a standard multiblock UI
     */
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        return new ArrayList<>();
    }

    public boolean allowCircuitSlots() {
        return true;
    }
}
