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
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class MultiblockControllerMachine extends MetaMachine {

    public static final String DEFAULT_STRUCTURE = "main";

    private @Nullable CurrentBlockInfo controllerBlockInfo = null;
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

    protected final Reference2ObjectMap<String, IBlockPattern> structures = new Reference2ObjectOpenHashMap<>();
    protected final Reference2ObjectMap<String, PatternState> patternStates = new Reference2ObjectOpenHashMap<>();

    public MultiblockControllerMachine(BlockEntityCreationInfo info) {
        super(info);
        createStructurePatterns();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            // run a structure check on the first tick
            ServerLevel level = (ServerLevel)getLevel();
            level.getServer().tell(new TickTask(2, this::checkAndFormStructure));
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (getLevel() instanceof ServerLevel serverLevel) {
            for (var pattern : patternStates.values()) {
                MultiblockWorldSavedData.getOrCreate(serverLevel).removeMapping(pattern);
            }
        }
    }

    @NotNull
    public CurrentBlockInfo getBlockInfo() {
        if (this.controllerBlockInfo == null) {
            this.controllerBlockInfo = new CurrentBlockInfo();
            this.controllerBlockInfo.setLevel(getLevel());
            this.controllerBlockInfo.setCurrentPos(getBlockPos());
        }
        return this.controllerBlockInfo;
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
        if (this.parts.size() != this.partPositions.length) {
            this.parts.clear();
            for (BlockPos pos : this.partPositions) {
                if (getMachine(getLevel(), pos) instanceof IMultiPart part) {
                    this.parts.add(part);
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

    public void checkAndFormStructure() {
        if (!(getLevel() instanceof ServerLevel serverLevel)) return;
        for (var entry : patternStates.entrySet()) {
            String name = entry.getKey();
            PatternState patternState = getPatternState(name);
            boolean formed = name.equals(DEFAULT_STRUCTURE) ? isFormed : patternState.isFormed();
            if (!formed || patternState.hasError() || patternState.getState() == PatternState.CheckState.UNINITIALIZED) {
                if (!patternState.getState().isValid()) {
                    checkStructurePattern(name);
                }
                if (patternState.getState().isValid()) {
                    formStructure(name);
                }
                MultiblockWorldSavedData.getOrCreate(serverLevel).addMapping(patternState);

            }
        }
    }

    /**
     * Whether the specific part should be added to the part list
     */
    public boolean shouldAddPartToController(IMultiPart part) {
        return true;
    }

    /**
     * Returns a list of all substructures this multiblock has.
     * @return set of substructures used by controller
     */
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

    /**
     * Creates the default pattern and pattern state and populates the state maps
     */
    public void createStructurePatterns() {
        var defaultPattern = createStructurePattern();
        var defaultPatternState = new PatternState();
        patternStates.put(DEFAULT_STRUCTURE, defaultPatternState);
        getSyncDataHolder().markClientSyncFieldDirty("patternStates");
        // defaultPattern.setActivePatternState(defaultPatternState);
        structures.put(DEFAULT_STRUCTURE, defaultPattern);
    }

    public void checkAndFormStructurePatterns() {
        for (String name : structures.keySet()) {
            formStructure(name);
        }
    }

    public PatternState getDefaultPatternState() {
        return getPatternState(DEFAULT_STRUCTURE);
    }

    public PatternState getPatternState(String name) {
        return this.patternStates.get(name);
    }

    public PatternState checkDefaultStructurePattern() {
        return checkStructurePattern(DEFAULT_STRUCTURE);
    }

    public PatternState checkStructurePattern(String structureName) {
        IBlockPattern pattern = getSubstructure(structureName);
        PatternState state = getPatternState(structureName);
        if (!state.shouldUpdate() || getLevel() == null) return state;

        long time = System.nanoTime();
        state.setController(this, getBlockPos());
        pattern.checkPatternFastAt(getLevel(), state, getBlockPos(), getFrontFacing(), getUpwardsFacing(),
                allowFlip());
        // patternStates.put(name, pState);
        // pattern.setActivePatternState(pState);
        // GTCEu.LOGGER.info("Structure check for {} took {} ns", self().getDefinition().getName(),
        // (System.nanoTime() - time));
        return state;
    }

    public void formStructure(@NotNull String substructureName) {
        var patternState = getPatternState(substructureName);
        patternState.setFormed(true);
        if (substructureName.equals(DEFAULT_STRUCTURE)) {
            isFormed = true;
            getSyncDataHolder().markClientSyncFieldDirty("isFormed");
        }

        if (!patternState.getState().isValid()) {
            if (patternState.isFormed()) {
                invalidateStructure(substructureName);
            }
        }

        if (patternState.isFormed()) {
            if (patternState.getState() == PatternState.CheckState.VALID_UNCACHED) {
                forEachMultiPart(substructureName, part -> {
                    if (parts.contains(part)) return true;

                    if (part.hasController(getBlockPos()) && !part.canShared(this, substructureName)) {
                        invalidateStructure(substructureName);
                        return false;
                    }

                    if (shouldAddPartToController(part)) {
                        this.parts.add(part);
                    }
                    return true;
                });

                // this.parts.sort(GTMemoizer.memoizeFunctionWeakIdent(getDefinition().getPartSorter()));
                // this.parts.sort(getDefinition().getPartSorter());
                for (var part : parts) {
                    if (part instanceof ParallelHatchPartMachine pHatch) {
                        this.parallelHatch = pHatch;
                    }
                    part.addedToController(this, substructureName);
                }
                updatePartPositions();

                patternState.setFormed(true);
                if (substructureName.equals(DEFAULT_STRUCTURE)) {
                    this.isFormed = true;
                    getSyncDataHolder().markClientSyncFieldDirty("isFormed");
                }
                setFlipped(patternState.isFlipped(), patternState);
            }
            return;
        }

        boolean valid = forEachMultiPart(substructureName, part -> {
            if (part.hasController(getBlockPos()) && !part.canShared(this, substructureName)) {
                return false;
            }
            return true;
        });

        if (!valid) return;

        patternState.setFormed(true);
        if (substructureName.equals(DEFAULT_STRUCTURE)) {
            isFormed = true;
            getSyncDataHolder().markClientSyncFieldDirty("isFormed");
            MachineRenderState renderState = getRenderState();
            if (renderState.hasProperty(GTMachineModelProperties.IS_FORMED)) {
                setRenderState(renderState.setValue(GTMachineModelProperties.IS_FORMED, true));
            }
        }
        setFlipped(patternState.isFlipped(), patternState);
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
        getSyncDataHolder().markClientSyncFieldDirty("isFormed");
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
            getSyncDataHolder().markClientSyncFieldDirty("isFormed");
        }
        updatePartPositions();
    }

    protected void invalidateStructureCaches() {
        for (var pState : patternStates.values()) {
            pState.getPosCache().clear();
        }
    }

    public IBlockPattern getSubstructure(String name) {
        return structures.get(name);
    }

    protected final boolean forEachMultiPart(String name, Predicate<IMultiPart> action) {
        var cache = patternStates.get(name).getCache();
        for (BlockInfo info : cache.values()) {
            if (info.getBlockEntity() instanceof IMultiPart part) {
                if (!action.test(part)) return false;
            }
        }
        return true;
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
            invalidateStructureCaches();
            var mwsd = MultiblockWorldSavedData.getOrCreate(serverLevel);
            for (var patternState : patternStates.values()) {
                mwsd.removeMapping(patternState);
            }
            // TODO structure check
            checkAndFormStructure();
            //mwsd.addAsyncLogic(this);
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

    public Comparator<IMultiPart> getPartSorter() {
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
                invalidateStructureCaches();
                checkAndFormStructurePatterns();
            }
        }
    }

    @Override
    public void setFrontFacing(Direction facing) {
        super.setFrontFacing(facing);

        if (getLevel() != null && !getLevel().isClientSide) {
            invalidateStructureCaches();
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
    // TODO move to recipe logic
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
