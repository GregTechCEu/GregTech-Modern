package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.IFilterType;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleGeneratorMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataInfoProvider;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.error.FilterMatchingError;
import com.gregtechceu.gtceu.api.multiblock.error.PatternError;
import com.gregtechceu.gtceu.api.multiblock.error.PatternStringError;
import com.gregtechceu.gtceu.api.multiblock.pattern.FactoryExpandablePattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternState;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.item.behavior.PortableScannerBehavior;
import com.gregtechceu.gtceu.common.machine.electric.HullMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.generator.LargeCombustionEngineMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.generator.LargeTurbineMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.DiodePartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.MufflerPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.CokeOvenMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.PrimitiveBlastFurnaceMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.PrimitivePumpMachine;
import com.gregtechceu.gtceu.common.machine.trait.CleanroomLogic;
import com.gregtechceu.gtceu.common.machine.trait.CleanroomProviderTrait;
import com.gregtechceu.gtceu.common.machine.trait.CleanroomReceiverTrait;
import com.gregtechceu.gtceu.common.mui.GTMultiblockTextUtil;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.GenericSyncValue;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.StringSyncValue;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.common.mui.GTByteBufAdapters.COMPONENT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CleanroomMachine extends WorkableElectricMultiblockMachine
                              implements IDataInfoProvider {

    public static final int CLEAN_AMOUNT_THRESHOLD = 95;
    public static final int MIN_CLEAN_AMOUNT = 0;

    public static final int MIN_RADIUS = 2;
    public static final int MIN_DEPTH = 3;
    public static final int MAX_RADIUS = 7;
    public static final int MAX_DEPTH = 14;

    private final int[] bounds = { 0, 0, MIN_RADIUS, MIN_RADIUS, MIN_RADIUS, MIN_RADIUS };
    @Nullable
    private CleanroomType cleanroomType = null;
    @SaveField
    private int cleanAmount;
    // runtime
    @Getter
    @Nullable
    private EnergyContainerList inputEnergyContainers;
    @Getter
    @Nullable
    private Collection<CleanroomReceiverTrait> cleanroomReceivers = new ArrayList<>();

    private final CleanroomProviderTrait cleanroomProviderTrait;

    public CleanroomMachine(BlockEntityCreationInfo info) {
        super(info, new CleanroomLogic());
        this.cleanroomProviderTrait = attachTrait(new CleanroomProviderTrait());
    }

    @Override
    public CleanroomLogic getRecipeLogic() {
        return (CleanroomLogic) super.getRecipeLogic();
    }

    //////////////////////////////////////
    // *** Multiblock LifeCycle ***//
    //////////////////////////////////////

    @Override
    public void formStructure(String name) {
        super.formStructure(name);
        var pState = patternStates.get(name);
        initializeAbilities();

        // var cache = getSubstructure(name).getCache();
        var cache = patternStates.get(name).getCache();
        IFilterType filterType = null;
        for (var entry : cache.long2ObjectEntrySet()) {
            var state = entry.getValue().getBlockState();
            for (var filter : GTCEuAPI.CLEANROOM_FILTERS.entrySet()) {
                if (filter.getValue().get() == state.getBlock()) {
                    if (filterType == null) filterType = filter.getKey();
                    else {
                        if (filterType != filter.getKey()) {
                            pState.setError(new FilterMatchingError(BlockPos.of(entry.getLongKey()), filterType,
                                    filter.getKey()));
                            invalidateStructure(name);
                            return;
                        }
                    }
                }
            }
        }
        if (filterType != null) {
            this.cleanroomType = filterType.getCleanroomType();
        } else {
            this.cleanroomType = CleanroomType.CLEANROOM;
        }
        cleanroomProviderTrait.setProvidedTypes(Set.of(this.cleanroomType));

        forEachFormed(name, (info, pos) -> {
            BlockEntity be = info.getBlockEntity();
            // todo check if be and if it has the cleanroom trait
            //if (!(be instanceof ICleanroomReceiver receiver)) return;

            /*if (receiver.getCleanroom() != this) {
                receiver.setCleanroomProvider(cleanroomProviderTrait);
                cleanroomReceivers.add(receiver);
            }*/
        });

        // max progress is based roughly on the dimensions of the structure: ((w * d) ^ .8 * h)
        // taller cleanrooms take longer than wider ones
        // minimum of 100 is a 5x5x5 cleanroom: 125-25=100 ticks
        // max sized CR is around 1142 ticks per progression

        int leftRight = bounds[2] + bounds[3] + 1;
        int frontBack = bounds[4] + bounds[5] + 1;
        var area = (leftRight) * (frontBack);
        var duration = Math.pow(area, 0.8) * (bounds[1] + 1);
        this.getRecipeLogic().setDuration(Math.max(100, (int) duration));
    }

    @Override
    public void invalidateStructure(String name) {
        super.invalidateStructure(name);
        this.inputEnergyContainers = null;
        this.cleanAmount = MIN_CLEAN_AMOUNT;
        cleanroomProviderTrait.setActive(false);
        if (cleanroomReceivers != null) {
            this.cleanroomReceivers.forEach(CleanroomReceiverTrait::removeCleanroom);
            this.cleanroomReceivers = null;
        }
    }

    public boolean shouldAddPartToController(IMultiPart part) {
        var posCache = patternStates.get(DEFAULT_STRUCTURE).getPosCache();
        for (Direction side : GTUtil.DIRECTIONS) {
            if (!posCache.contains(part.self().getBlockPos().relative(side))) { // part is on a wall or edge
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldUpdateActiveBlocks() {
        return false;
    }

    protected void initializeAbilities() {
        List<IEnergyContainer> energyContainers = new ArrayList<>();
        // Long2ObjectMap<IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap",
        // Long2ObjectMaps::emptyMap);
        for (IMultiPart part : getParts()) {
            if (isPartIgnored(part)) continue;
            // IO io = ioMap.getOrDefault(part.self().getPos().asLong(), IO.BOTH);
            // if (io == IO.NONE || io == IO.OUT) continue;
            var handlerLists = part.getRecipeHandlers();
            for (var handlerList : handlerLists) {
                // if (!handlerList.isValid(io)) continue;
                handlerList.getCapability(EURecipeCapability.CAP).stream()
                        .filter(IEnergyContainer.class::isInstance)
                        .map(IEnergyContainer.class::cast)
                        .forEach(energyContainers::add);
            }

            if (part instanceof IMaintenanceMachine maintenanceMachine) {
                getRecipeLogic().setMaintenanceMachine(maintenanceMachine);
            }
        }
        this.inputEnergyContainers = new EnergyContainerList(energyContainers);
        getRecipeLogic().setEnergyContainer(this.inputEnergyContainers);
        this.tier = Math.min(GTValues.MAX, GTUtil.getFloorTierByVoltage(getMaxVoltage()));
    }

    @SuppressWarnings("RedundantIfStatement") // `return false` being a separate statement is better for readability
    private static boolean isPartIgnored(IMultiPart part) {
        if (part instanceof DiodePartMachine) return true;
        if (part instanceof HullMachine) return true;

        return false;
    }

    /**
     * Scans for blocks around the controller to update the dimensions
     */
    public void updateStructureDimensions() {
        if (getLevel() == null) return;
        var pState = patternStates.get(DEFAULT_STRUCTURE);
        Direction front = getFrontFacing();
        Direction back = front.getOpposite();
        Direction left = front.getCounterClockWise();
        Direction right = left.getOpposite();

        int l = findWallPos(left, getBlockPos().mutable());
        int r = findWallPos(right, getBlockPos().mutable());
        int b = findWallPos(back, getBlockPos().mutable());
        int f = findWallPos(front, getBlockPos().mutable());
        int d = findFloorPos(Direction.DOWN, getBlockPos().mutable());

        if (d < MIN_DEPTH || l < MIN_RADIUS || r < MIN_RADIUS || b < MIN_RADIUS || f < MIN_RADIUS) {
            pState.setError(new PatternStringError("gtceu.predicate_error.cleanroom.too_small"));
            invalidateStructure();
            return;
        }

        if (Math.abs(l - r) > 1 || Math.abs(b - f) > 1) {
            pState.setError(new PatternStringError("gtceu.predicate_error.cleanroom.not_centered"));
            invalidateStructure();
            return;
        }

        bounds[1] = d;
        bounds[2] = l;
        bounds[3] = r;
        bounds[4] = f;
        bounds[5] = b;

        /*
         * BlockPos.MutableBlockPos lPos = getPos().mutable();
         * BlockPos.MutableBlockPos rPos = getPos().mutable();
         * BlockPos.MutableBlockPos fPos = getPos().mutable();
         * BlockPos.MutableBlockPos bPos = getPos().mutable();
         * BlockPos.MutableBlockPos hPos = getPos().mutable();
         *
         * // find the distances from the controller to the plascrete blocks on one horizontal axis and the Y axis
         * // repeatable aisles take care of the second horizontal axis
         * int lDist = 0;
         * int rDist = 0;
         * int bDist = 0;
         * int fDist = 0;
         * int hDist = 0;
         *
         * // find the left, right, back, and front distances for the structure pattern
         * // maximum size is 15x15x15 including walls, so check 7 block radius around the controller for blocks
         * for (int i = 1; i < 8; i++) {
         * if (lDist == 0 && isBlockEdge(world, lPos, left)) lDist = i;
         * if (rDist == 0 && isBlockEdge(world, rPos, right)) rDist = i;
         * if (bDist == 0 && isBlockEdge(world, bPos, back)) bDist = i;
         * if (fDist == 0 && isBlockEdge(world, fPos, front)) fDist = i;
         * if (lDist != 0 && rDist != 0 && bDist != 0 && fDist != 0) break;
         * }
         *
         * // height is diameter instead of radius, so it needs to be done separately
         * for (int i = 1; i < 15; i++) {
         * if (isBlockFloor(world, hPos, Direction.DOWN)) hDist = i;
         * if (hDist != 0) break;
         * }
         *
         * if (Math.abs(lDist - rDist) > 1 || Math.abs(bDist - fDist) > 1) {
         * this.isFormed = false;
         * return;
         * }
         *
         * if (lDist < MIN_RADIUS || rDist < MIN_RADIUS || bDist < MIN_RADIUS || fDist < MIN_RADIUS || hDist <
         * MIN_DEPTH) {
         * this.isFormed = false;
         * return;
         * }
         *
         * this.lDist = lDist;
         * this.rDist = rDist;
         * this.bDist = bDist;
         * this.fDist = fDist;
         * this.hDist = hDist;
         */
    }

    public int findWallPos(Direction dir, BlockPos.MutableBlockPos pos) {
        for (int i = 1; i <= MAX_RADIUS; i++) {
            var state = getLevel().getBlockState(pos.move(dir));
            if (state == getCasingState() || state == getGlassState()) {
                return i;
            }
        }
        return -1;
    }

    public int findFloorPos(Direction dir, BlockPos.MutableBlockPos pos) {
        for (int i = 1; i <= MAX_DEPTH; i++) {
            if (isAllFloorBlocks(getBlockPos().mutable().move(dir, i))) {
                return i;
            }
        }
        return -1;
    }

    private boolean isAllFloorBlocks(BlockPos.MutableBlockPos pos) {
        pos.move(Direction.SOUTH, 1).move(Direction.WEST, 1);
        for (int j = 0; j < 3; j++) {
            for (int k = 0; k < 3; k++) {
                var checkPos = pos.immutable();
                var s1 = getLevel().getBlockState(checkPos);
                if (s1 != getCasingState() && s1 != getGlassState() && !(s1.is(CustomTags.CLEANROOM_FLOORS))) {
                    return false;
                }
                pos.move(Direction.NORTH);
            }
            pos.move(Direction.SOUTH, 3);
            pos.move(Direction.EAST);
        }
        return true;
    }

    @Override
    public PatternState checkStructurePattern(String name) {
        createStructurePattern();
        return super.checkStructurePattern(name);
    }

    @Override
    public IBlockPattern createStructurePattern() {
        // return the default structure, even if there is no valid size found
        // this means auto-build will still work, and prevents terminal crashes.
        // if (getLevel() == null)

        updateStructureDimensions();

        var wallPredicate = Predicates.blocks(getCasingState().getBlock(), getGlassState().getBlock());
        var energyPredicate = autoAbilities(true, false, false).or(abilities(PartAbility.INPUT_ENERGY)
                .setMinGlobalLimited(1).setMaxGlobalLimited(3));

        var edgePredicate = wallPredicate.or(energyPredicate);
        var facePredicate = wallPredicate.or(energyPredicate)
                .or(doorPredicate().setMaxGlobalLimited(8))
                .or(abilities(PartAbility.PASSTHROUGH_HATCH).setMaxGlobalLimited(30));
        var filterPredicate = cleanroomFilters();
        var innerPredicate = innerPredicate();
        var verticalEdgePredicate = edgePredicate.or(blocks(getGlassState().getBlock()));

        return FactoryExpandablePattern.start(RelativeDirection.UP, RelativeDirection.RIGHT, RelativeDirection.FRONT)
                .boundsFunction((l, bp, f, u) -> bounds)
                .predicateFunction((bp, b) -> {
                    if (bp.equals(BlockPos.ZERO))
                        return Predicates.controller(Predicates.blocks(getDefinition().getBlock()));

                    int intersections = 0;

                    boolean topAisle = bp.getX() == b[0];
                    boolean bottomAisle = bp.getX() == -b[1];

                    if (topAisle || bottomAisle) intersections++;

                    // negative signs for the LEFT and BACK ordinals
                    // string dir is right, so its bounds[2] and bounds[3]
                    if (bp.getY() == -b[2] || bp.getY() == b[3]) intersections++;
                    // char dir is front, so its bounds[4] and bounds[5]
                    if (bp.getZ() == b[4] || bp.getZ() == -b[5]) intersections++;

                    if (intersections >= 2) {
                        if (topAisle || bottomAisle) return edgePredicate;
                        return verticalEdgePredicate;
                    }

                    if (intersections == 1) {
                        if (topAisle) return filterPredicate;
                        return facePredicate;
                    }

                    return innerPredicate;

                })
                .build();

        /*
         * // these can sometimes get set to 0 when loading the game, breaking JEI
         * if (lDist < MIN_RADIUS) lDist = MIN_RADIUS;
         * if (rDist < MIN_RADIUS) rDist = MIN_RADIUS;
         * if (bDist < MIN_RADIUS) bDist = MIN_RADIUS;
         * if (fDist < MIN_RADIUS) fDist = MIN_RADIUS;
         * if (hDist < MIN_DEPTH) hDist = MIN_DEPTH;
         *
         * if (this.getFrontFacing() == Direction.EAST || this.getFrontFacing() == Direction.WEST) {
         * int tmp = lDist;
         * lDist = rDist;
         * rDist = tmp;
         * }
         *
         * StringBuilder[] floorLayer = new StringBuilder[fDist + bDist + 1];
         * List<StringBuilder[]> wallLayers = new ArrayList<>();
         * StringBuilder[] ceilingLayer = new StringBuilder[fDist + bDist + 1];
         *
         * for (int i = 0; i < floorLayer.length; i++) {
         * floorLayer[i] = new StringBuilder(lDist + rDist + 1);
         * ceilingLayer[i] = new StringBuilder(lDist + rDist + 1);
         * }
         *
         * for (int i = 0; i < hDist - 1; i++) {
         * wallLayers.add(new StringBuilder[fDist + bDist + 1]);
         * for (int j = 0; j < fDist + bDist + 1; j++) {
         * var s = new StringBuilder(lDist + rDist + 1);
         * wallLayers.get(i)[j] = s;
         * }
         * }
         *
         * for (int i = 0; i < lDist + rDist + 1; i++) {
         * for (int j = 0; j < fDist + bDist + 1; j++) {
         * if (i == 0 || i == lDist + rDist || j == 0 || j == fDist + bDist) { // all edges
         * floorLayer[j].append('A'); // floor edge
         * for (int k = 0; k < hDist - 1; k++) {
         * wallLayers.get(k)[j].append('W'); // walls
         * }
         * ceilingLayer[j].append('D'); // ceiling edge
         * } else { // not edges
         * if (i == lDist && j == fDist) { // very center
         * floorLayer[j].append('K');
         * } else {
         * floorLayer[j].append('E'); // floor valid blocks
         * }
         * for (int k = 0; k < hDist - 1; k++) {
         * wallLayers.get(k)[j].append(' ');
         * }
         * if (i == lDist && j == fDist) { // very center
         * ceilingLayer[j].append('C'); // controller
         * } else {
         * ceilingLayer[j].append('F'); // filter
         * }
         * }
         * }
         * }
         *
         * String[] f = new String[bDist + fDist + 1];
         * for (int i = 0; i < floorLayer.length; i++) {
         * f[i] = floorLayer[i].toString();
         * }
         * String[] m = new String[bDist + fDist + 1];
         * for (int i = 0; i < wallLayers.get(0).length; i++) {
         * m[i] = wallLayers.get(0)[i].toString();
         * }
         * String[] c = new String[bDist + fDist + 1];
         * for (int i = 0; i < ceilingLayer.length; i++) {
         * c[i] = ceilingLayer[i].toString();
         * }
         *
         * TraceabilityPredicate wallPredicate = states(getCasingState(), getGlassState());
         * TraceabilityPredicate basePredicate = Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1)
         * .setMaxGlobalLimited(2)
         * .or(blocks(GTMachines.MAINTENANCE_HATCH.get(), GTMachines.AUTO_MAINTENANCE_HATCH.get())
         * .setMinGlobalLimited(ConfigHolder.INSTANCE.machines.enableMaintenance ? 1 : 0)
         * .setMaxGlobalLimited(1))
         * .or(abilities(PartAbility.PASSTHROUGH_HATCH).setMaxGlobalLimited(30));
         *
         * return FactoryBlockPattern.start(LEFT, FRONT, UP)
         * .aisle(f)
         * .aisle(m).setRepeatable(wallLayers.size())
         * .aisle(c)
         * .where('C', Predicates.controller(Predicates.blocks(this.getDefinition().get())))
         * .where('F', Predicates.cleanroomFilters())
         * .where('D', states(getCasingState())) // ceiling edges
         * .where(' ', innerPredicate())
         * .where('E', wallPredicate.or(basePredicate) // inner floor
         * .or(getValidFloorBlocks().setMaxGlobalLimited(4)))
         * .where('K', wallPredicate // very center floor, needed for height check
         * .or(getValidFloorBlocks()))
         * .where('W', wallPredicate.or(basePredicate)// walls
         * .or(doorPredicate().setMaxGlobalLimited(8)))
         * .where('A', wallPredicate.or(basePredicate)) // floor edges
         * .build();
         */
    }

    // protected to allow easy addition of addon "cleanrooms"
    protected BlockState getCasingState() {
        return GTBlocks.PLASTCRETE.getDefaultState();
    }

    protected BlockState getGlassState() {
        return GTBlocks.CLEANROOM_GLASS.getDefaultState();
    }

    protected static PatternPredicate doorPredicate() {
        return Predicates.custom(
                blockWorldState -> blockWorldState.getBlockState().getBlock() instanceof DoorBlock ? null :
                        PatternError.PLACEHOLDER,
                (map) -> new BlockInfo[] { new BlockInfo(Blocks.IRON_DOOR.defaultBlockState()), new BlockInfo(
                        Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER)) });
    }

    private PatternPredicate getValidFloorBlocks() {
        return Predicates.blockTag(CustomTags.CLEANROOM_FLOORS);
    }

    protected PatternPredicate innerPredicate() {
        return new PatternPredicate(blockWorldState -> {
            // all non-GTMachines are allowed inside by default
            BlockEntity blockEntity = blockWorldState.getTileEntity();
            if (blockEntity instanceof MetaMachine machine) {
                if (isMachineBanned(machine)) {
                    return PatternError.PLACEHOLDER;
                }
                machine.getTraitOptional(CleanroomReceiverTrait.TYPE).ifPresent(cleanroomReceivers::add);
            }
            return null;
        }, null);
    }

    protected boolean isMachineBanned(MetaMachine machine) {
        // blacklisted machines: mufflers and all generators, miners/drills, primitives
        if (machine.getTrait(CleanroomProviderTrait.TYPE) != null) return true;
        if (machine instanceof MufflerPartMachine) return true;
        if (machine instanceof SimpleGeneratorMachine) return true;
        if (machine instanceof LargeCombustionEngineMachine) return true;
        if (machine instanceof LargeTurbineMachine) return true;

        if (machine instanceof LargeMinerMachine) return true;
        if (machine instanceof FluidDrillMachine) return true;
        if (machine instanceof BedrockOreMinerMachine) return true;

        if (machine instanceof CokeOvenMachine) return true;
        if (machine instanceof PrimitiveBlastFurnaceMachine) return true;
        return machine instanceof PrimitivePumpMachine;
    }

    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        var state = patternStates.get(DEFAULT_STRUCTURE);
        List<IWidget> widgets = new ArrayList<>();

        // Machine generic sync handlers
        BooleanSyncValue isFormed = syncManager.getOrCreateSyncHandler("isFormed", BooleanSyncValue.class,
                () -> new BooleanSyncValue(this::isFormed));
        BooleanSyncValue workingEnabled = syncManager.getOrCreateSyncHandler("workingEnabled", BooleanSyncValue.class,
                () -> new BooleanSyncValue(this.recipeLogic::isWorkingEnabled, this.recipeLogic::setWorkingEnabled));
        BooleanSyncValue active = syncManager.getOrCreateSyncHandler("isActive", BooleanSyncValue.class,
                () -> new BooleanSyncValue(this.recipeLogic::isActive));
        BooleanSyncValue waiting = syncManager.getOrCreateSyncHandler("isWaiting", BooleanSyncValue.class,
                () -> new BooleanSyncValue(this.recipeLogic::isWaiting));

        // Energy bank specific sync handlers
        // These will not be called anywhere else, so we can create them directly instead of using
        // getOrCreateSyncHandler

        LongSyncValue maxVoltage = new LongSyncValue(this::getMaxVoltage);
        syncManager.syncValue("maxVoltage", maxVoltage);

        StringSyncValue cleanroomTranslationKey = new StringSyncValue(() -> {
            if (this.cleanroomType == null) return "";
            return this.cleanroomType.getTranslationKey();
        });
        syncManager.syncValue("cleanroomTranslationKey", cleanroomTranslationKey);

        BooleanSyncValue cleanroomTypeIsNull = new BooleanSyncValue(() -> this.cleanroomType == null);
        syncManager.syncValue("cleanroomTypeIsNull", cleanroomTypeIsNull);

        BooleanSyncValue cleanroomProviderTraitIsActive = new BooleanSyncValue(
                () -> this.cleanroomProviderTrait != null && this.cleanroomProviderTrait.isActive());
        syncManager.syncValue("cleanroomProviderTrait", cleanroomProviderTraitIsActive);

        IntSyncValue cleanAmount = new IntSyncValue(() -> this.cleanAmount);
        syncManager.syncValue("cleanAmount", cleanAmount);

        GenericSyncValue<Component> distComponent = GenericSyncValue.builder(Component.class)
                .adapter(COMPONENT)
                .getter(() -> Component.translatable("gtceu.multiblock.dimensions.1", bounds[3] + bounds[4] + 1, bounds[1] + 1,
                        bounds[4] + bounds[5] + 1))
                .build();
        syncManager.syncValue("distComponent", distComponent);

        widgets.add(Text.dynamic(() -> {
            Component tooltip = Component.translatable("gtceu.multiblock.invalid_structure.tooltip")
                    .withStyle(ChatFormatting.GRAY);
            return Component.translatable("gtceu.multiblock.invalid_structure")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.RED)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip)));
        })
                .asWidget()
                .setEnabledIf((widget) -> !isFormed.getBoolValue()));

        widgets.add(Text.dynamic(() -> {
            String voltageName = GTValues.VNF[GTUtil.getFloorTierByVoltage(maxVoltage.getLongValue())];
            return Component.translatable("gtceu.multiblock.max_energy_per_tick", maxVoltage.getLongValue(),
                    voltageName);
        })
                .asWidget()
                .setEnabledIf((widget) -> isFormed.getBoolValue() && maxVoltage.getLongValue() > 0));

        widgets.add(Text.dynamic(() -> {
            if (cleanroomTypeIsNull.getBoolValue()) {
                return Component.empty();
            } else {
                return Component.translatable(cleanroomTranslationKey.getStringValue());
            }
        })
                .asWidget()
                .setEnabledIf((widget) -> isFormed.getBoolValue() && !cleanroomTypeIsNull.getBoolValue()));

        widgets.add(Text.dynamic(() -> Component.translatable("gtceu.multiblock.work_paused"))
                .asWidget()
                .setEnabledIf((widget) -> isFormed.getBoolValue() && !workingEnabled.getBoolValue()));

        widgets.add(GTMultiblockTextUtil.addProgressLine(this, syncManager));

        widgets.add(Text.lang("gtceu.multiblock.idling")
                .asWidget()
                .setEnabledIf((widget) -> isFormed.getBoolValue() && workingEnabled.getBoolValue() &&
                        !active.getBoolValue()));

        widgets.add(Text
                .of(Component.translatable("gtceu.multiblock.waiting")
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)))
                .asWidget()
                .setEnabledIf((widget) -> isFormed.getBoolValue() && waiting.getBoolValue()));

        widgets.add(Text.of(Component.translatable("gtceu.multiblock.cleanroom.clean_state"))
                .asWidget()
                .setEnabledIf((widget) -> isFormed.getBoolValue() && cleanroomProviderTraitIsActive.getBoolValue()));
        widgets.add(Text.of(Component.translatable("gtceu.multiblock.cleanroom.dirty_state"))
                .asWidget()
                .setEnabledIf((widget) -> isFormed.getBoolValue() && !cleanroomProviderTraitIsActive.getBoolValue()));

        widgets.add(Text.dynamic(
                () -> Component.translatable("gtceu.multiblock.cleanroom.clean_amount", cleanAmount.getIntValue()))
                .asWidget()
                .setEnabledIf((widget) -> isFormed.getBoolValue()));

        widgets.add(Text.of(Component.translatable("gtceu.multiblock.dimensions.0"))
                .asWidget()
                .setEnabledIf((widget) -> isFormed.getBoolValue()));
        widgets.add(Text.dynamic(distComponent::getValue)
                .asWidget()
                .setEnabledIf((widget) -> isFormed.getBoolValue()));

        return widgets;

        /*
        if (state.hasError()) {
            var comp = state.getError().getErrorInfo();
            textList.addAll(comp);
        }
         */
    }

    /**
     * Adjust the cleanroom's clean amount
     *
     * @param amount the amount of cleanliness to increase/decrease by
     */
    public void adjustCleanAmount(int amount) {
        // do not allow negative cleanliness nor cleanliness above 100
        this.cleanAmount = Mth.clamp(this.cleanAmount + amount, 0, 100);
        cleanroomProviderTrait.setActive(this.cleanAmount >= CLEAN_AMOUNT_THRESHOLD);
    }

    @Override
    public List<Component> getDataInfo(PortableScannerBehavior.DisplayMode mode) {
        if (mode == PortableScannerBehavior.DisplayMode.SHOW_ALL ||
                mode == PortableScannerBehavior.DisplayMode.SHOW_MACHINE_INFO) {
            return Collections.singletonList(Component.translatable(
                    cleanroomProviderTrait.isActive() ? "gtceu.multiblock.cleanroom.clean_state" :
                            "gtceu.multiblock.cleanroom.dirty_state"));
        }
        return new ArrayList<>();
    }

    @Override
    public long getMaxVoltage() {
        if (inputEnergyContainers == null) return GTValues.LV;
        return inputEnergyContainers.getInputVoltage();
    }

    // Do not allow cleanroom to be paused due to custom recipe logic
    @Override
    public boolean isWorkingEnabled() {
        return true;
    }

    @Override
    public void setWorkingEnabled(boolean ignored) {}
}
