package com.gregtechceu.gtceu.common.machine.mui;

import brachy.modularui.utils.fakelevel.MapSchema;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternSlice;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.machines.GCYMMachines;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import com.gregtechceu.gtceu.utils.GTUtil;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.Icon;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.BoolValue;
import brachy.modularui.value.DoubleValue;
import brachy.modularui.value.sync.DynamicSyncHandler;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.EmptyWidget;
import brachy.modularui.widgets.*;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.menu.ContextMenuButton;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.stream.Collectors;

import static com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine.DEFAULT_STRUCTURE;

public class TestMuiMachine2 extends MetaMachine implements IMuiMachine {

    private final MultiblockMachineDefinition multiblockDefinition;

    // schema stuff
    private SchemaWidget multiSchema;
    private MapSchema mapSchema;
    private DynamicSyncHandler partsViewWidget;
    private final Reference2IntMap<Block> blockCounts = new Reference2IntOpenHashMap<>();

    // for the slice slider
    private int slice = 0;
    private int maxSlices = 0;

    // user inputs
    private boolean isFlipped = false;
    private Direction frontFacing;
    private Direction upFacing;
    private final Map<Long, BlockInfo> userGlobalBlockPreferences = new Long2ReferenceOpenHashMap<>();
    private final Map<Integer, Integer> userSliceRepeats = new Int2IntArrayMap();
    private final Table<PatternPredicate, BasePredicate, BlockInfo> userBasePredicateBlockPreferences = HashBasedTable.create();
    private final Table<PatternPredicate, BasePredicate, Pair<Integer, Integer>> userBasePredicateMinMaxPreferences = HashBasedTable.create(); // Min, Max.
    // ^ To disable a base predicate, set min to 0




    ///  ALL INFO RELEVANT TO STRUCTURE AUTO BUILDING:
    /// INPUTS:
    /// User supplied, ordered by priority:
    /// Layer sizes (layerRepeats) from user,
    /// Overrides for which candidate to pick per BlockPos
    ///   - e.g. "left of controller should be maintenance hatch"
    ///   - e.g. DT with 10 repeats of the output hatch isle. Default to PatternLayer.minRepeats
    /// Overrides for which candidate to pick per BasePredicate
    ///   - e.g. "all energy hatches should be HV"
    /// Overrides for the min/max for each BasePredicate
    ///   - e.g. "only put 1 energy hatch instead of 2"
    ///   - has to be within the BasePredicate's minCount/maxCount
    /// Disable a BasePredicate in a PatternPredicate
    ///   - has to respect the BasePredicate's minCount
    ///   - e.g. can disable EBF's fluid outputs, but can't disable EBF's casing
    ///
    ///
    /// Machine supplied:
    /// BlockPattern:
    /// - PatternLayer[], each PatternLayer:
    ///     - minRepeats
    ///     - maxRepeats
    ///     - Char[][] pattern, NxM array of char
    /// - Char <-> PatternPredicate, each PatternPredicate:
    ///     - List of BasePredicates, each BasePredicate:
    ///         - candidates, List<BlockInfo> The candidates to place
    ///         - priority, specifically within the PatternPredicate
    ///         - minCount, total minimum across the whole multi
    ///         - maxCount, total maximum across the whole multi
    ///         - minLayerCount, total minimum in one layer (e.g. hatch in DT) TODO: Should this be in BlockPattern instead?
    ///         - maxLayerCount, total maximum in one layer
    ///
    /// OUTPUTS:
    /// Map<BlockPos, BlockInfo> resultStructure
    /// Descriptive error if not possible
    ///
    /// Naive approach:
    /// 1. Flatten PatternIsle[] (which contains Char[][]) into Char[][][] based on the layerRepeats
    /// 2. Create the final Map<BlockPos, BlockInfo>
    /// 3. Fill in the candidate overrides first (e.g. "0,1,0 should be maintenance hatch") if it fits any of the BasePredicates, error otherwise(?)
    /// 4. In any order (probably just naive x,y,z loop), get the char at that position,
    ///  4a. Go through every BasePredicate in order of priority, see if there's a minCount that's not satisfied yet, then try those
    ///  4b. If all basePredicates with a mincount are satisfied, place the first predicate that works
    ///  4c. If the BasePredicate is at its max, remove it from the list to be considered (maybe not needed at first, but optimization)
    ///  4d. error if none are valid candidates(?)
    ///
    /// note- For this, we should have a isValidCandidate(current resultStructure, new BlockPos, new BlockInfo) function
    ///
    public TestMuiMachine2(BlockEntityCreationInfo info) {
        super(info);
        multiblockDefinition = (MultiblockMachineDefinition) GTMultiMachines.ELECTRIC_BLAST_FURNACE;
        var pattern = ((BlockPattern)multiblockDefinition.getStructurePatterns().get("main").get());
        for(int i=0;i<pattern.getSlices().length;i++){
            userSliceRepeats.put(i, pattern.getSlices()[i].getMinRepeats());
        }
        frontFacing = multiblockDefinition.getRotationState().defaultDirection;
        switch (multiblockDefinition.getRotationState()) {
            case NONE -> upFacing = Direction.UP;
            case ALL -> upFacing = frontFacing.getAxis() == Direction.Axis.Y ? Direction.NORTH : Direction.UP;
            case Y_AXIS -> upFacing = Direction.NORTH;
            case NON_Y_AXIS -> upFacing = Direction.UP;
            default -> upFacing = Direction.UP;
        }
        //userGlobalBlockPreferences.put(BlockPos.asLong(0,0,1), BlockInfo.fromBlock(GTMachines.MAINTENANCE_HATCH.getBlock()));
    }

    @Override
    public ModularPanel<?> buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        ModularPanel<?> panel = new ModularPanel<>("test_tile2")
                // .size(200, 200)
                .padding(8, 4)
                .coverChildren()
                .background(GuiTextures.MC_BACKGROUND);
        Flow col = Flow.col().coverChildren();
        col.child(new ListWidget<>()
                .name("structurePatterns")
                .coverChildren()
                .children(multiblockDefinition.getStructurePatterns().entrySet(), (e) -> {
                    IBlockPattern pattern = e.getValue().get();

                    Flow patternColumn = Flow.col()
                            .coverChildren();
                    Flow predicatesRow = Flow.row()
                            .name("predicates")
                            .height(20)
                            .coverChildrenWidth();

                    if (pattern instanceof BlockPattern blockPattern) {
                        createSliceSliders(patternColumn, blockPattern);
                        createPredicateMenus(predicatesRow, blockPattern);
                    }
                    patternColumn.child(predicatesRow);
                    return patternColumn;
                }));

        Flow schemaCol = Flow.col().coverChildren();
        refreshSchema();

        if (getLevel().isClientSide()) {
            multiSchema = new SchemaWidget(mapSchema);
            schemaCol.child(multiSchema.size(200, 200));
        }
        schemaCol.child(new SchemaWidget.LayerButton(mapSchema, 0, maxSlices)
                .onMouseReleased((context, button) -> {
                    slice = ++slice % maxSlices;
                    this.refreshViewWidget(); // this may not be necessary?
                    return true;
                }));
        col.child(schemaCol);

        partsViewWidget = new DynamicSyncHandler().widgetProvider((sm, buf) -> {
            Flow innerCol = Flow.col().coverChildren().childPadding(2).rightRelOffset(1.0f, -20);
            innerCol.children(blockCounts.reference2IntEntrySet(), (e) -> {
                Item item = e.getKey().asItem();
                return new ItemDrawable(new ItemStack(item, e.getIntValue()))
                        .asWidget().tooltip(r -> r.addLine(item.getDescription()));
            });
            innerCol.childPadding(2).left(2);
            return innerCol;
        }).allowC2S();
        refreshViewWidget();
        panel.child(col);
        panel.child(new DynamicSyncedWidget<>().syncHandler(partsViewWidget).coverChildren());
        return panel;
    }

    private void refreshViewWidget() {
        partsViewWidget.notifyUpdate((packet) -> {});
        if (multiSchema != null) {
            multiSchema.getSchemaRenderer().recompile();
        }
    }



    /// ==== Schema setup ====
    private void refreshSchema(){
        Map<BlockPos, BlockInfo> resultStructure;

        resultStructure = new HashMap<>();
        BlockPattern pattern = (BlockPattern) multiblockDefinition.getStructurePatterns().get(DEFAULT_STRUCTURE).get();

        char[][][] flattenedCharPattern = flattenBlockPattern(pattern, userSliceRepeats);
        char[][][] adjustedCharPattern = rotateAndFlipCharPattern(flattenedCharPattern, pattern.getDirections(), frontFacing, upFacing, isFlipped);

        populateWithUserBlockPreferences(resultStructure, pattern, adjustedCharPattern, userGlobalBlockPreferences);

        populateFromPattern(resultStructure, pattern, adjustedCharPattern);

        fixRotationsAndFacing(resultStructure, frontFacing, upFacing, multiblockDefinition.getBlock());

        Map<BlockPos, BlockState> schemaMap = resultStructure.entrySet()
                .stream()
                .map(entry -> Pair.of(entry.getKey(), entry.getValue().getBlockState()))
                .collect(Collectors.toMap(Pair::left, Pair::right));
        mapSchema = new MapSchema(schemaMap);
        mapSchema.setRenderFilter((pos, state) -> pos.getY() < slice);
    }

    private @UnmodifiableView char[][][] flattenBlockPattern(BlockPattern pattern, Map<Integer, Integer> sliceRepeats){
        int totalSlices = sliceRepeats.values().stream().reduce(0, Integer::sum);
        int[] dimensions = pattern.getDimensions();
        char[][][] flattenedPattern = new char[totalSlices][dimensions[1]][dimensions[2]];
        PatternSlice[] slices = pattern.getSlices();
        int totalSlicesIndex = 0;
        for(int sliceIndex=0; sliceIndex<slices.length; sliceIndex++){
            PatternSlice slice = slices[sliceIndex];
            int repeats = sliceRepeats.getOrDefault(sliceIndex, 1);
            for(int i=0;i<repeats;i++){
                flattenedPattern[totalSlicesIndex] = slice.getPattern();
                totalSlicesIndex++;
            }
        }
        assert(totalSlicesIndex == totalSlices);
        return flattenedPattern;
    }

    private int[] getDimensions(char[][][] charPattern){
        int d0 = charPattern.length;
        int d1 = d0 > 0 ? charPattern[0].length : 0;
        int d2 = d1 > 0 ? charPattern[0][0].length : 0;
        return new int[]{d0, d1, d2};
    }

    /// Re-bins the local {@code [sliceIdx][stringIdx][charIdx]} pattern into an absolute,
    /// axis-aligned {@code [x][y][z]} array matching the schema's world frame.
    ///
    /// The local axes map to world {@link Direction}s exactly like {@code checkSlice} does:
    ///   worldOffset = sliceIdx*abs(directions[0]) + stringIdx*abs(directions[1]) + charIdx*abs(directions[2])
    /// The min corner is placed at index (0,0,0),
    /// i.e. {@code result[x][y][z]} is the char physically located at (x,y,z).
    private char[][][] rotateAndFlipCharPattern(char[][][] localFlattenedPattern, RelativeDirection[] patternDirections, Direction frontFacing, Direction upFacing, boolean isFlipped){
        Direction absoluteDir0 = patternDirections[0].getRelativeFacing(frontFacing, upFacing, isFlipped);
        Direction absoluteDir1 = patternDirections[1].getRelativeFacing(frontFacing, upFacing, isFlipped);
        Direction absoluteDir2 = patternDirections[2].getRelativeFacing(frontFacing, upFacing, isFlipped);

        var dimensions = getDimensions(localFlattenedPattern);
        if (dimensions[0] == 0 || dimensions[1] == 0 || dimensions[2] == 0) return new char[0][0][0];

        // Per-axis step vectors of each absolute direction.
        int[][] steps = {
                { absoluteDir0.getStepX(), absoluteDir0.getStepY(), absoluteDir0.getStepZ() },
                { absoluteDir1.getStepX(), absoluteDir1.getStepY(), absoluteDir1.getStepZ() },
                { absoluteDir2.getStepX(), absoluteDir2.getStepY(), absoluteDir2.getStepZ() },
        };
        // Max local index reached along each local axis.
        int[] extents = { dimensions[0] - 1, dimensions[1] - 1, dimensions[2] - 1 };

        // World-space bounding box. Each axis contributes monotonically, so the extremes are
        // reached at index 0 or at extents[axis] depending on the sign of the step.
        int[] min = new int[3];
        int[] max = new int[3];
        for (int axis = 0; axis < 3; axis++) {
            for (int world = 0; world < 3; world++) {
                int contribution = steps[axis][world] * extents[axis];
                min[world] += Math.min(0, contribution);
                max[world] += Math.max(0, contribution);
            }
        }

        int sizeX = max[0] - min[0] + 1;
        int sizeY = max[1] - min[1] + 1;
        int sizeZ = max[2] - min[2] + 1;
        char[][][] result = new char[sizeX][sizeY][sizeZ];

        for (int s = 0; s < dimensions[0]; s++) {
            for (int t = 0; t < dimensions[1]; t++) {
                for (int c = 0; c < dimensions[2]; c++) {
                    int worldX = steps[0][0] * s + steps[1][0] * t + steps[2][0] * c;
                    int worldY = steps[0][1] * s + steps[1][1] * t + steps[2][1] * c;
                    int worldZ = steps[0][2] * s + steps[1][2] * t + steps[2][2] * c;
                    result[worldX - min[0]][worldY - min[1]][worldZ - min[2]] =
                            localFlattenedPattern[s][t][c];
                }
            }
        }

        return result;
    }

    private void populateWithUserBlockPreferences(Map<BlockPos, BlockInfo> resultStructure, BlockPattern pattern, char[][][] flattenedBlockPattern, Map<Long, BlockInfo> userBlockPreferences) {
        var dimensions = getDimensions(flattenedBlockPattern);
        for(Map.Entry<Long, BlockInfo> blockPreference : userBlockPreferences.entrySet()){
            BlockPos pos = BlockPos.of(blockPreference.getKey());
            BlockInfo blockInfo = blockPreference.getValue();
            if( pos.getX() >= dimensions[0] ||
                pos.getY() >= dimensions[1] ||
                pos.getZ() >= dimensions[2]){
                // TODO: Throw or silently fail?
                throw new IllegalStateException("BlockPos preference " + pos.toString() + "is outside of bounds for pattern of size " + dimensions[0]+","+dimensions[1]+","+dimensions[2]);
            }
            if(!isValidCandidate(resultStructure, pattern, flattenedBlockPattern, pos, blockInfo)){
                throw new IllegalStateException("Invalid preference " + blockInfo.getBlockState().getBlock().getName() +" for position " + pos);
            }
            resultStructure.put(pos, blockInfo);
        }
    }

    private void populateFromPattern(Map<BlockPos, BlockInfo> resultStructure, BlockPattern pattern, char[][][] flattenedBlockPattern){
        /// 4. In any order (probably just naive x,y,z loop), get the char at that position,
        ///  4a. Go through every BasePredicate in order of priority, see if there's a minCount that's not satisfied yet, then try those
        ///  4b. If all basePredicates with a mincount are satisfied, place the first predicate that works
        ///  4c. If the BasePredicate is at its max, remove it from the list to be considered (maybe not needed at first, but optimization)
        ///  4d. error if none are valid candidates(?)
        ///

        // TODO: Change this to loop through the slices to handle min/maxPerSlice
        var dimensions = getDimensions(flattenedBlockPattern);
        for(int x=0;x<dimensions[0];x++) {
            for (int y = 0; y < dimensions[1]; y++) {
                for (int z = 0; z < dimensions[2]; z++) {
                    var pos = new BlockPos(x,y,z);
                    if(resultStructure.containsKey(pos)) continue;
                    char c = flattenedBlockPattern[x][y][z];
                    PatternPredicate predicate = pattern.getPredicates().get(c);

                    if(predicate == PatternPredicate.AIR || predicate == PatternPredicate.ANY){
                        continue;
                    }

                    // Try to find a basePredicate that doesn't have its minCount satisfied, and if so, place that
                    boolean inserted = false;
                    for(BasePredicate basePredicate : predicate.predicateList){
                        int minCount = userBasePredicateMinMaxPreferences.contains(predicate, basePredicate) ?
                                userBasePredicateMinMaxPreferences.get(predicate, basePredicate).left() :
                                basePredicate.minCount;
                        if(minCount == 0) continue;
                        int totalAlreadyPopulated = (int) resultStructure.values()
                                .stream()
                                .filter(blockInfo -> basePredicate.getCandidates().contains(blockInfo))
                                .count();
                        if(totalAlreadyPopulated >= minCount) continue;
                        var toInsert = userBasePredicateBlockPreferences.contains(predicate, basePredicate) ?
                                userBasePredicateBlockPreferences.get(predicate, basePredicate) :
                                basePredicate.getCandidates().get(0);
                        // TODO: is this needed? doesn't this just do what we're already doing?
                        if(!isValidCandidate(resultStructure, pattern, flattenedBlockPattern, pos, toInsert)) continue;
                        resultStructure.put(pos, toInsert);
                        inserted = true;
                        break;
                    }
                    if(inserted) continue;

                    // Try to find a basePredicate that doesn't have its maxCount filled yet, and if so, place that
                    inserted = false;
                    for(BasePredicate basePredicate : predicate.predicateList){
                        int maxCount = userBasePredicateMinMaxPreferences.contains(predicate, basePredicate) ?
                                userBasePredicateMinMaxPreferences.get(predicate, basePredicate).right() :
                                basePredicate.maxCount;
                        if(maxCount == 0) continue;
                        int totalAlreadyPopulated = (int) resultStructure.values()
                                .stream()
                                .filter(blockInfo -> basePredicate.getCandidates().contains(blockInfo))
                                .count();
                        if(totalAlreadyPopulated >= maxCount && maxCount != -1) continue;
                        var toInsert = userBasePredicateBlockPreferences.contains(predicate, basePredicate) ?
                                userBasePredicateBlockPreferences.get(predicate, basePredicate) :
                                basePredicate.getCandidates().get(0);
                        // TODO: is this needed? doesn't this just do what we're already doing?
                        if(!isValidCandidate(resultStructure, pattern, flattenedBlockPattern, pos, toInsert)) continue;
                        resultStructure.put(pos, toInsert);
                        inserted = true;
                        break;
                    }
                    if(inserted) continue;
                    // If we arrive here, there's nothing we can place that doesn't overflow a maxcount!
                    throw new IllegalStateException("Could not place a block without breaking maxCount requirments for character " + c);
                }
            }
        }
    }

    private boolean isValidCandidate(Map<BlockPos, BlockInfo> resultStructure, BlockPattern pattern, char[][][] flattenedBlockPattern, BlockPos pos, BlockInfo newInfo){
        char c = flattenedBlockPattern[pos.getX()][pos.getY()][pos.getZ()];
        PatternPredicate predicate = pattern.getPredicates().get(c);
        // TODO: More intricate checking of e.g. minlimit using resultStructure
        return predicate.predicateList.stream().anyMatch(basePredicate -> basePredicate.candidates.contains(newInfo));
    }
    public static final Direction[] DIRECTIONS_IN_ORDER = { Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP, Direction.DOWN };

    private void fixRotationsAndFacing(Map<BlockPos, BlockInfo> resultStructure, Direction frontFacing, Direction upFacing, Block controllerBlock){
        Map<BlockPos, BlockState> toUpdate = new Object2ObjectOpenHashMap<>();
        for(var entry : resultStructure.entrySet()){
            BlockPos pos = entry.getKey();
            BlockState currentState = entry.getValue().getBlockState();
            Direction valid = null;
            if (currentState.getBlock() instanceof MetaMachineBlock machineBlock) {
                if (!currentState.hasProperty(machineBlock.getRotationState().property)) continue;
                if (machineBlock.equals(controllerBlock)){
                    toUpdate.put(pos, currentState.setValue(machineBlock.getRotationState().property, frontFacing)
                            .setValue(GTBlockStateProperties.UPWARDS_FACING, upFacing));
                    continue;
                }
                for (var dir : DIRECTIONS_IN_ORDER) {
                    if(!machineBlock.getRotationState().test(valid)) continue;
                    if (!resultStructure.containsKey(pos.relative(dir))) {
                        valid = dir;
                        break;
                    }
                }
                if (valid != null) {
                    toUpdate.put(pos, currentState.setValue(machineBlock.getRotationState().property, valid));
                }
            }
        }
        for(var entry : toUpdate.entrySet()){
            resultStructure.put(entry.getKey(), BlockInfo.fromBlockState(entry.getValue()));
        }
    };

    /// ==== User Preference UI ======
    private void setPredicateDefaultBlock(PatternPredicate predicate, BasePredicate basePredicate, BlockInfo blockInfo) {
        userBasePredicateBlockPreferences.put(predicate, basePredicate, blockInfo);
        refreshSchema();
    }

    private void createSliceSliders(Flow col, BlockPattern blockPattern) {
        int repeatSliceIndex = 0;
        for (var patternSlice : blockPattern.getSlices()) {
            if (patternSlice.getMinRepeats() != 1 || patternSlice.getMaxRepeats() != 1) {
                if (!userSliceRepeats.containsKey(repeatSliceIndex)) {
                    userSliceRepeats.put(repeatSliceIndex, patternSlice.getMinRepeats());
                }
                if (patternSlice.getMinRepeats() == patternSlice.getMaxRepeats()) {

                } else {
                    int finalRepeatSliceIndex = repeatSliceIndex;
                    col.child(new SliderWidget()
                            .background(GTGuiTextures.FLUID_SLOT)
                            .height(16)
                            .width(patternSlice.getMaxRepeats() * 12)
                            .stopper(1.0f)
                            .bounds(patternSlice.getMinRepeats(), patternSlice.getMaxRepeats())
                            .value(new DoubleValue.Dynamic(() -> {
                                if (!userSliceRepeats.containsKey(finalRepeatSliceIndex)) return 0;
                                return userSliceRepeats.get(finalRepeatSliceIndex);
                            }, (v) -> {
                                userSliceRepeats.put(finalRepeatSliceIndex, (int) v);
                                refreshSchema();
                            })));
                }
            }
            repeatSliceIndex++;
        }
    }

    private void createPredicateMenus(Flow predicatesRow, BlockPattern blockPattern) {
        for (var entry : blockPattern.getPredicates().char2ObjectEntrySet()) {
            var predicate = entry.getValue();
            // todo figure out sliders needed for predicate min/max depending on base predicates in the
            // main predicate
            if (predicate.equals(PatternPredicate.ANY) || predicate.equals(PatternPredicate.AIR)) {
                continue;
            }
            var menu = new ContextMenuButton<>(String.valueOf(entry.getCharKey()))
                    .size(20)
                    .requiresClick()
                    .menuList(l -> l
                            .maxSize(80)
                            .coverChildrenWidth()
                            .collapseDisabledChildren()
                            .childSeparator(Icon.EMPTY_2PX)
                            .children(predicate.predicateList, basePredicate -> {
                                List<BlockInfo> candidates = basePredicate.candidates;
                                if (candidates == null || candidates.isEmpty())
                                    return new EmptyWidget();
                                if (candidates.size() > 1) {
                                    return createInnerPredicateMenu(predicate, basePredicate, candidates);
                                } else {
                                    return new ToggleButton()
                                            .value(new BoolValue.Dynamic(() -> false,
                                                    (b) -> setPredicateDefaultBlock(predicate, basePredicate, candidates.get(0))))
                                            .size(16)
                                            .tooltip(r -> r.add(
                                                    basePredicate.candidates.get(0).getItemStackForm().getHoverName()))
                                            .overlay(new ItemDrawable(
                                                    candidates.get(0).getItemStackForm()));
                                }
                            }));
            predicatesRow.child(menu);
        }
    }

    private ContextMenuButton<?> createInnerPredicateMenu(PatternPredicate predicate, BasePredicate basePredicate,
                                                          List<BlockInfo> candidates) {
        return new ContextMenuButton<>(basePredicate.getPredicateName())
                .size(16)
                .tooltip(r -> r.add(basePredicate.getPredicateName()))
                .overlay(new ItemDrawable(
                        candidates.get(0).getItemStackForm()))
                .requiresClick()
                .openRightDown()
                .menuList(l1 -> l1
                        .maxSize(80)
                        .coverChildrenWidth()
                        .childSeparator(Icon.EMPTY_2PX)
                        .children(candidates, blockInfo -> {
                            Component stackName = blockInfo
                                    .getItemStackForm().getHoverName();
                            return new ToggleButton()
                                    .value(new BoolValue.Dynamic(
                                            () -> false,
                                            (b) -> setPredicateDefaultBlock(predicate, basePredicate, blockInfo)))
                                    .size(16)
                                    .tooltip(r -> r.add(stackName))
                                    .overlay(new ItemDrawable(
                                            blockInfo.getItemStackForm()));
                        }));
    }
}
