package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.IFilterType;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
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
import com.gregtechceu.gtceu.api.multiblock.error.PatternStringError;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandableMultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandablePattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.data.GTBlocks;
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
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
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
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
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

    private List<Integer> bounds = new ArrayList<>(
            List.of(0, MIN_DEPTH, MIN_RADIUS, MIN_RADIUS, MIN_RADIUS, MIN_RADIUS));
    @Nullable
    private CleanroomType cleanroomType = null;
    @SaveField
    private int cleanAmount;
    // runtime
    @Getter
    @Nullable
    private EnergyContainerList inputEnergyContainers;
    @Getter
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
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        var pState = patternStates.get(substructureName);

        bounds = boundsFunction().apply(getLevel(), getBlockPos().mutable(), getFrontFacing(), getUpwardsFacing());
        int d = bounds.get(1);
        int l = bounds.get(2);
        int r = bounds.get(3);
        int f = bounds.get(4);
        int b = bounds.get(5);
        if (d < MIN_DEPTH || l < MIN_RADIUS || r < MIN_RADIUS || b < MIN_RADIUS || f < MIN_RADIUS) {
            pState.setError(
                    new PatternStringError(Component.translatable("gtceu.predicate_error.cleanroom.too_small")));
            invalidateStructure();
            return;
        }

        if (Math.abs(l - r) > 1 || Math.abs(b - f) > 1) {
            pState.setError(
                    new PatternStringError(Component.translatable("gtceu.predicate_error.cleanroom.not_centered")));
            invalidateStructure();
            return;
        }

        initializeAbilities();

        var cache = patternStates.get(substructureName).getCache();
        IFilterType filterType = null;
        for (var entry : cache.long2ObjectEntrySet()) {
            var state = entry.getValue().getBlockState();
            for (var filter : GTCEuAPI.CLEANROOM_FILTERS.entrySet()) {
                if (filter.getValue().get() == state.getBlock()) {
                    if (filterType == null) filterType = filter.getKey();
                    else {
                        if (filterType != filter.getKey()) {
                            pState.setError(new FilterMatchingError(BlockPos.of(entry.getLongKey()),
                                    filterType.getCleanroomType(),
                                    filter.getKey().getCleanroomType()));
                            invalidateStructure(substructureName);
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
        this.cleanroomProviderTrait.setProvidedTypes(Set.of(this.cleanroomType));

        forEachFormed(substructureName, (info, pos) -> {
            BlockEntity be = info.getBlockEntity();
            if (be instanceof MetaMachine machine) {
                if (isMachineBanned(machine)) {
                    return;
                }
                machine.getTraitOptional(CleanroomReceiverTrait.TYPE).ifPresent(this.cleanroomReceivers::add);
            }
        });
        this.cleanroomReceivers.forEach(receiver -> receiver.setCleanroomProvider(this.cleanroomProviderTrait));

        // max progress is based roughly on the dimensions of the structure: ((w * d) ^ .8 * h)
        // taller cleanrooms take longer than wider ones
        // minimum of 100 is a 5x5x5 cleanroom: 125-25=100 ticks
        // max sized CR is around 1142 ticks per progression

        int leftRight = bounds.get(2) + bounds.get(3) + 1;
        int frontBack = bounds.get(4) + bounds.get(5) + 1;
        var area = (leftRight) * (frontBack);
        var duration = Math.pow(area, 0.8) * (bounds.get(1) + 1);
        this.getRecipeLogic().setDuration(Math.max(100, (int) duration));
    }

    @Override
    public void invalidateStructure(String name) {
        super.invalidateStructure(name);
        this.inputEnergyContainers = null;
        this.cleanAmount = MIN_CLEAN_AMOUNT;
        this.cleanroomProviderTrait.setActive(false);
        this.cleanroomReceivers.forEach(CleanroomReceiverTrait::removeCleanroom);
        this.cleanroomReceivers.clear();
    }

    public boolean shouldAddPartToController(IMultiPart part) {
        var posCache = patternStates.get(DEFAULT_STRUCTURE).getCache().keySet();
        for (Direction side : GTUtil.DIRECTIONS) {
            if (!posCache.contains(part.self().getBlockPos().relative(side).asLong())) { // part is on a wall or edge
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
        for (IMultiPart part : getParts()) {
            if (isPartIgnored(part)) continue;
            var handlerLists = part.getRecipeHandlers();
            for (var handlerList : handlerLists) {
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
    public static ExpandablePattern.BoundsProvider boundsFunction() {
        return (level, controllerPos, frontFacing, upFacing) -> {
            Direction front = frontFacing;
            Direction back = frontFacing.getOpposite();
            Direction left = frontFacing.getCounterClockWise();
            Direction right = left.getOpposite();

            int l = findWallPos(level, left, controllerPos.mutable());
            int r = findWallPos(level, right, controllerPos.mutable());
            int b = findWallPos(level, back, controllerPos.mutable());
            int f = findWallPos(level, front, controllerPos.mutable());
            int d = findFloorPos(level, upFacing.getOpposite(), controllerPos.mutable());

            if (d < MIN_DEPTH || l < MIN_RADIUS || r < MIN_RADIUS || b < MIN_RADIUS || f < MIN_RADIUS) {
                return new IntArrayList(new int[] { 0, MIN_DEPTH, MIN_RADIUS, MIN_RADIUS, MIN_RADIUS, MIN_RADIUS });
            }

            return new IntArrayList(new int[] { 0, d, l, r, f, b });
        };
    }

    public static int findWallPos(Level level, Direction dir, BlockPos.MutableBlockPos pos) {
        for (int i = 1; i <= MAX_RADIUS; i++) {
            var state = level.getBlockState(pos.move(dir));
            if (state == getCasingState() || state == getGlassState()) {
                return i;
            }
        }
        return -1;
    }

    public static int findFloorPos(Level level, Direction dir, BlockPos.MutableBlockPos pos) {
        for (int i = 1; i <= MAX_DEPTH; i++) {
            if (isAllFloorBlocks(level, pos.move(dir, 1).mutable())) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isAllFloorBlocks(Level level, BlockPos.MutableBlockPos pos) {
        pos.move(Direction.SOUTH, 1).move(Direction.WEST, 1);
        for (int j = 0; j < 3; j++) {
            for (int k = 0; k < 3; k++) {
                var checkPos = pos.immutable();
                var s1 = level.getBlockState(checkPos);
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

    public static Function<MultiblockMachineDefinition, IBlockPattern> getPattern() {
        return (definition) -> {
            PatternPredicate wallPredicate = getValidFloorBlocks().or(states(getCasingState(), getGlassState()));
            PatternPredicate energyPredicate = autoAbilities(true, false, false).or(abilities(PartAbility.INPUT_ENERGY)
                    .setMinGlobalLimited(1).setMaxGlobalLimited(3));

            PatternPredicate edgePredicate = wallPredicate.or(energyPredicate);
            PatternPredicate facePredicate = wallPredicate.or(energyPredicate)
                    .or(doorPredicate().setMaxGlobalLimited(8))
                    .or(abilities(PartAbility.PASSTHROUGH_HATCH).setMaxGlobalLimited(30));
            PatternPredicate filterPredicate = cleanroomFilters();
            PatternPredicate innerPredicate = innerPredicate();
            PatternPredicate verticalEdgePredicate = edgePredicate.or(blocks(getGlassState().getBlock()));

            return ExpandableMultiblockPatternBuilder
                    .start(RelativeDirection.UP, RelativeDirection.RIGHT, RelativeDirection.FRONT)
                    .boundsProvider(boundsFunction())
                    .constraintProvider(() -> List.of(IntIntPair.of(0, 0), IntIntPair.of(MIN_DEPTH, MAX_DEPTH),
                            IntIntPair.of(MIN_RADIUS, MAX_RADIUS), IntIntPair.of(MIN_RADIUS, MAX_RADIUS),
                            IntIntPair.of(MIN_RADIUS, MAX_RADIUS), IntIntPair.of(MIN_RADIUS, MAX_RADIUS)))
                    .predicateProvider((bp, b) -> {
                        if (bp.equals(BlockPos.ZERO))
                            return Predicates.controller(definition);

                        int intersections = 0;
                        boolean topAisle = bp.getX() == b.get(0);
                        boolean bottomAisle = bp.getX() == -b.get(1);
                        if (topAisle || bottomAisle) intersections++;
                        // negative signs for the LEFT and BACK ordinals
                        // string dir is right, so its bounds[2] and bounds[3]
                        if (bp.getY() == -b.get(2) || bp.getY() == b.get(3)) intersections++;
                        // char dir is front, so its bounds[4] and bounds[5]
                        if (bp.getZ() == b.get(4) || bp.getZ() == -b.get(5)) intersections++;

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
        };
    }

    // return the default structure, even if there is no valid size found
    // this means auto-build will still work, and prevents terminal crashes.
    // if (getLevel() == null)

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

    // protected to allow easy addition of addon "cleanrooms"
    protected static BlockState getCasingState() {
        return GTBlocks.PLASTCRETE.getDefaultState();
    }

    protected static BlockState getGlassState() {
        return GTBlocks.CLEANROOM_GLASS.getDefaultState();
    }

    protected static PatternPredicate doorPredicate() {
        return Predicates.custom(
                blockWorldState -> blockWorldState.retrieveCurrentBlockState().getBlock() instanceof DoorBlock ? null :
                        Predicates.PLACEHOLDER,
                List.of(new BlockInfo(Blocks.IRON_DOOR.defaultBlockState()), new BlockInfo(
                        Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER))));
    }

    private static PatternPredicate getValidFloorBlocks() {
        return Predicates.blockTag(CustomTags.CLEANROOM_FLOORS);
    }

    protected static PatternPredicate innerPredicate() {
        return new PatternPredicate(blockWorldState -> {
            // all non-GTMachines are allowed inside by default
            BlockEntity blockEntity = blockWorldState.getBlockEntity();
            if (blockEntity instanceof MetaMachine machine) {
                if (isMachineBanned(machine)) {
                    return Predicates.PLACEHOLDER;
                }
            }
            return null;
        }, null);
    }

    protected static boolean isMachineBanned(MetaMachine machine) {
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
                .getter(() -> Component.translatable("gtceu.multiblock.dimensions.1", bounds.get(3) + bounds.get(4) + 1,
                        bounds.get(1) + 1,
                        bounds.get(4) + bounds.get(5) + 1))
                .build();
        syncManager.syncValue("distComponent", distComponent);

        widgets.add(GTMultiblockTextUtil.addUnformedWarning(this, syncManager));

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
         * if (state.hasError()) {
         * var comp = state.getError().getErrorInfo();
         * textList.addAll(comp);
         * }
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
