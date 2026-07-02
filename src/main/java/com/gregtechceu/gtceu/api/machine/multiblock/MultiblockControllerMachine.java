package com.gregtechceu.gtceu.api.machine.multiblock;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTraitType;
import com.gregtechceu.gtceu.api.machine.trait.multiblock.MultiblockMachineTrait;
import com.gregtechceu.gtceu.api.multiblock.MultiblockWorldSavedData;
import com.gregtechceu.gtceu.api.multiblock.pattern.*;
import com.gregtechceu.gtceu.api.multiblock.util.AbstractStructureHelper;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.sync_system.annotations.ClientFieldChangeListener;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.client.mui.schema.MutableSchema;
import com.gregtechceu.gtceu.client.renderer.PatternPreviewRenderer;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ParallelHatchPartMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.PanelSyncManager;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * The base class for all multiblock controllers.
 */
@SuppressWarnings({"UnusedReturnValue"})
public class MultiblockControllerMachine extends MetaMachine {

    public static final String DEFAULT_STRUCTURE = "main";

    private final List<MultiblockPartMachine> parts = new ArrayList<>();
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

    protected static final Table<MultiblockMachineDefinition, String, IBlockPattern> structurePatterns = HashBasedTable
            .create();
    protected final Object2ObjectMap<String, PatternState> patternStates = new Object2ObjectOpenHashMap<>();

    public MultiblockControllerMachine(BlockEntityCreationInfo info) {
        super(info);
        createStructurePatterns();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            // run a structure check on the first tick
            ((ServerLevel) getLevel()).getServer().tell(new TickTask(2, this::checkAndFormStructure));
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
        invalidateAllStructures();
    }

    public Map<String, IBlockPattern> getStructurePatterns() {
        return structurePatterns.row(this.getDefinition());
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
            if (getMachine(getLevel(), pos) instanceof MultiblockPartMachine part) {
                parts.add(part);
            }
        }
    }

    /**
     * Gets the first trait (trait with highest priority) of a specified type from this multiblock's parts.
     *
     * @param type The trait type to get.
     * @return The trait, or null if no traits of the given type are present.
     */
    public <T extends MachineTrait> @Nullable T getTraitFromParts(MachineTraitType<T> type) {
        T trait = null;
        for (var part: getParts()) {
            T partTrait = part.getTrait(type);
            if (partTrait != null && (trait == null || partTrait.getTraitPriority() > trait.getTraitPriority())) trait = partTrait;
        }
        return trait;
    }

    /**
     * Get all traits with the specified type.
     *
     * @param type The trait type to get
     * @return An unmodifiable list containing all traits of the specified type.
     */
    public <T extends MachineTrait> List<T> getTraitsFromParts(MachineTraitType<T> type) {
        List<T> traits = new ObjectArrayList<>();
        for (var part: getParts()) {
            traits.addAll(part.getTraits(type));
        }
        return Collections.unmodifiableList(traits);
    }

    protected void updatePartPositions() {
        this.partPositions = this.parts.isEmpty() ? new BlockPos[0] :
                this.parts.stream().map(part -> part.self().getBlockPos()).toArray(BlockPos[]::new);
        syncDataHolder.markClientSyncFieldDirty("partPositions");
    }

    public List<MultiblockPartMachine> getParts() {
        // for the client side, when the chunk unloaded
        if (this.parts.size() != this.partPositions.length) {
            this.parts.clear();
            for (BlockPos pos : this.partPositions) {
                if (getMachine(getLevel(), pos) instanceof MultiblockPartMachine part) {
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
        if (self().isRemoved()) return;
        for (var entry : patternStates.entrySet()) {
            String name = entry.getKey();
            PatternState patternState = getPatternState(name);
            boolean formed = name.equals(DEFAULT_STRUCTURE) ? isFormed : patternState.isFormed();
            if (!formed || patternState.hasErrors() ||
                    patternState.getState() == PatternState.CheckState.UNINITIALIZED) {
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
    public boolean shouldAddPartToController(MultiblockPartMachine part) {
        return true;
    }

    /**
     * Returns a list of all substructures this multiblock has.
     *
     * @return set of substructures used by controller
     */
    public Set<String> getStructureNames() {
        return structurePatterns.row(this.getDefinition()).keySet();
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
    public IBlockPattern getDefaultStructurePattern() {
        return getDefinition().getStructurePatterns().get(DEFAULT_STRUCTURE).get();
    }

    /**
     * Creates the default pattern and pattern state and populates the state maps
     */
    public void createStructurePatterns() {
        getDefinition().getStructurePatterns().forEach((name, pattern) -> {
            patternStates.put(name, new PatternState());
            structurePatterns.put(this.getDefinition(), name, pattern.get());
        });
        getSyncDataHolder().markClientSyncFieldDirty("patternStates");
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
        IBlockPattern pattern = getSubstructurePattern(structureName);
        PatternState state = getPatternState(structureName);
        if (pattern == null || !state.shouldUpdate() || getLevel() == null) return state;

        state.setController(this, getBlockPos());
        pattern.checkPatternFastAt(getLevel(), state, getBlockPos(), getFrontFacing(), getUpwardsFacing(),
                allowFlip());

        return state;
    }

    public void formStructure(String substructureName) {
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

                this.parts.sort(getDefinition().getPartSorter().apply(this));
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

                for (var trait : getAllTraits()) {
                    if (trait instanceof MultiblockMachineTrait multiblockMachineTrait)
                        multiblockMachineTrait.onStructureFormed(substructureName);
                }
            }
            return;
        }

        boolean valid = forEachMultiPart(substructureName, part ->
                !part.hasController(getBlockPos()) || part.canShared(this, substructureName));

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

    public void invalidateAllStructures() {
        for (String name : patternStates.keySet()) {
            parts.removeIf(part -> {
                if (name.equals(part.getSubstructureName())) {
                    part.removedFromController(this);
                    return true;
                }
                return false;
            });
        }
    }

    public void invalidateStructure() {
        invalidateStructure(DEFAULT_STRUCTURE);
        isFormed = false;
        getSyncDataHolder().markClientSyncFieldDirty("isFormed");
    }

    public void invalidateStructure(String name) {
        var pState = patternStates.get(name);
        // if (!pState.isFormed()) return;

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

        for (var trait : getAllTraits()) {
            if (trait instanceof MultiblockMachineTrait multiblockMachineTrait)
                multiblockMachineTrait.onStructureInvalid(name);
        }
    }

    protected void invalidateStructureCaches() {
        for (var pState : patternStates.values()) {
            pState.getCache().clear();
        }
    }

    @Nullable
    public IBlockPattern getSubstructurePattern(String name) {
        return structurePatterns.get(this.getDefinition(), name);
    }

    protected final boolean forEachMultiPart(String name, Predicate<MultiblockPartMachine> action) {
        var cache = patternStates.get(name).getCache();
        for (BlockInfo info : cache.values()) {
            if (info.getBlockEntity() instanceof MultiblockPartMachine part) {
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
     * {@link #invalidateStructure()} will be called from
     * {@link PatternState#onBlockStateChanged(BlockPos, BlockState, BlockState)}
     */
    public void onPartUnload() {
        parts.removeIf(part -> part.self().isRemoved());
        updatePartPositions();
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
            checkAndFormStructure();
        }
    }

    public boolean allowFlip() {
        return getDefinition().isAllowFlip();
    }

    public @Nullable BlockState getPartAppearance(MultiblockPartMachine part, Direction side,
                                                  @Nullable BlockState sourceState,
                                                  @Nullable BlockPos sourcePos) {
        if (isFormed()) {
            return getDefinition().getPartAppearance().apply(this, part, side);
        }
        return null;
    }

    public Comparator<MultiblockPartMachine> getPartSorter() {
        return getDefinition().getPartSorter().apply(this);
    }

    @Override
    public void setUpwardsFacing(Direction upwardsFacing) {
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
                checkAndFormStructure();
            }
        }
    }

    @Override
    public void setFrontFacing(Direction facing) {
        super.setFrontFacing(facing);

        if (getLevel() != null && !getLevel().isClientSide) {
            invalidateStructureCaches();
            checkAndFormStructure();
        }
    }

    @Override
    public InteractionResult onUse(ExtendedUseOnContext context) {
        if (!isFormed() && context.getPlayer().isShiftKeyDown() &&
                context.getPlayer().getItemInHand(context.getHand()).isEmpty()) {
            if (isRemote()) {
                Map<BlockPos, BlockInfo> resultStructure = new HashMap<>();
                AbstractStructureHelper structureHelper = null;
                IBlockPattern pattern = getStructurePatterns().get(DEFAULT_STRUCTURE);
                if (pattern instanceof BlockPattern blockPattern) {
                    Int2IntMap slices = new Int2IntArrayMap();
                    for (int i = 0; i < blockPattern.getSlices().length; i++) {
                        slices.put(i, blockPattern.getSlices()[i].getMinRepeats());
                    }
                    structureHelper = AbstractStructureHelper.blockPattern(slices);
                } else if (pattern instanceof ExpandablePattern expandablePattern) {
                    IntList dims = new IntArrayList();
                    if (expandablePattern.getBoundsConstraints() != null) {
                        expandablePattern.getBoundsConstraints().apply().stream()
                                .mapToInt(Pair::left)
                                .forEach(dims::add);
                    }
                    structureHelper = AbstractStructureHelper.expandable(dims);
                }

                if (structureHelper != null) {
                    structureHelper.populate(resultStructure, pattern, null, getFrontFacing(), getUpwardsFacing(),
                            isFlipped());
                    Long2ReferenceMap<BlockState> blocks = new Long2ReferenceOpenHashMap<>();
                    resultStructure.forEach((pos, state) -> blocks.put(pos.asLong(), state.getBlockState()));
                    MutableSchema schema = new MutableSchema(blocks);
                    BlockPos origin = this.getBlockPos().mutable().move(schema.getControllerPos().multiply(-1));
                    PatternPreviewRenderer.INSTANCE.showPreviewCycleLevel(origin, schema,
                            ConfigHolder.INSTANCE.client.inWorldPreviewDuration * 20);
                }
            }
            return InteractionResult.sidedSuccess(isRemote());
        }
        return super.onUse(context);
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
