package com.gregtechceu.gtceu.common.machine.mui;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternAisle;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.Icon;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.fakelevel.MapSchema;
import brachy.modularui.value.BoolValue;
import brachy.modularui.value.DoubleValue;
import brachy.modularui.value.sync.DynamicSyncHandler;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.EmptyWidget;
import brachy.modularui.widgets.DynamicSyncedWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.SchemaWidget;
import brachy.modularui.widgets.SliderWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.menu.ContextMenuButton;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.*;

import java.util.*;

public class TestMuiMachine2 extends MetaMachine implements IMuiMachine {

    private final MultiblockMachineDefinition multiblockDefinition;

    private SchemaWidget multiSchema;
    private Map<PatternPredicate, BlockInfo> predicateSetting = new HashMap<>();
    private int maxLayers = 0;
    private int layer = 0;
    private final Table<String, Integer, Integer> aisleRepeats = HashBasedTable.create();
    private final Set<Long> allPositions = new HashSet<>();
    private final Reference2ObjectMap<PatternPredicate, Set<Long>> predicatePositions = new Reference2ObjectOpenHashMap<>();
    private final Reference2ObjectMap<PatternPredicate, BlockInfo> predicateBlockMapping = new Reference2ObjectOpenHashMap<>();
    private final Long2ReferenceMap<BlockState> userDefinedBlocks = new Long2ReferenceOpenHashMap<>();
    private final Long2ReferenceMap<BlockState> predicateDefaultBlocks = new Long2ReferenceOpenHashMap<>();
    private final Long2ReferenceMap<BlockState> blocks = new Long2ReferenceOpenHashMap<>();
    private final Reference2IntMap<Block> blockCounts = new Reference2IntOpenHashMap<>();
    private final Map<BasePredicate, Integer> placed = new Reference2IntOpenHashMap<>();

    private Long2ObjectMap<BlockInfo> blockInfo = new Long2ObjectOpenHashMap<>();

    public TestMuiMachine2(BlockEntityCreationInfo info) {
        super(info);
        multiblockDefinition = (MultiblockMachineDefinition) GTMultiMachines.ELECTRIC_BLAST_FURNACE;

        for (var pattern : multiblockDefinition.getStructurePatterns().values()) {
            if (pattern instanceof BlockPattern blockPattern) {
                for (var predicate : blockPattern.getPredicates().values()) {
                    if (predicate.equals(PatternPredicate.ANY) || predicate.equals(PatternPredicate.AIR)) {
                        continue;
                    }
                    predicateBlockMapping.put(predicate, predicate.predicateList.get(0).getCandidates().get(0));
                }
            }
        }
    }

    private DynamicSyncHandler schemaViewWidget;
    private DynamicSyncHandler partsViewWidget;

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
                    String patternName = e.getKey();
                    IBlockPattern pattern = e.getValue().get();

                    Flow patternColumn = Flow.col()
                            .coverChildren();
                    Flow predicatesRow = Flow.row()
                            .name("predicates")
                            .height(20)
                            .coverChildrenWidth();

                    if (pattern instanceof BlockPattern blockPattern) {
                        createAisleSliders(patternColumn, blockPattern, patternName);
                        createPredicateMenus(patternName, blockPattern, predicatesRow);
                    }
                    patternColumn.child(predicatesRow);
                    return patternColumn;
                }));

        schemaViewWidget = new DynamicSyncHandler().widgetProvider((slotsSyncManger, buffer) -> {
            Flow innerCol = Flow.col().coverChildren();
            if (predicateDefaultBlocks.isEmpty()) {
                setupBlocks();
            }
            blocks.clear();
            blockCounts.clear();
            blocks.putAll(predicateDefaultBlocks);
            blocks.putAll(userDefinedBlocks);
            blocks.forEach((pos, state) -> {
                blockCounts.merge(state.getBlock(), 1, Integer::sum);
            });

            MapSchema array = new MapSchema(blocks);
            array.setRenderFilter((pos, state) -> pos.getY() < layer);
            if (getLevel().isClientSide()) {
                multiSchema = new SchemaWidget(array);
                innerCol.child(multiSchema.size(200, 200));
            }
            innerCol.child(new SchemaWidget.LayerButton(array, 0, maxLayers)
                    .onMouseReleased((context, button) -> {
                        layer += 1;
                        layer %= maxLayers;
                        this.refreshViewWidget();
                        return true;
                    }));
            return innerCol;
        }).allowC2S();

        partsViewWidget = new DynamicSyncHandler().widgetProvider((sm, buf) -> {
            Flow innerCol = Flow.col().coverChildren();
            innerCol.children(blockCounts.reference2IntEntrySet(), (e) -> {
                Item item = e.getKey().asItem();
                return new ItemDrawable(new ItemStack(item, e.getIntValue()))
                        .asWidget().tooltip(r -> r.addLine(item.getDescription()));
            });
            innerCol.childPadding(2).rightRelOffset(1.0f, -20);
            return innerCol;
        }).allowC2S();
        col.child(new DynamicSyncedWidget<>().syncHandler(schemaViewWidget).coverChildren());
        panel.child(new DynamicSyncedWidget<>().syncHandler(partsViewWidget).coverChildren());
        refreshViewWidget();
        panel.child(col);
        return panel;
    }

    private void refreshViewWidget() {
        schemaViewWidget.notifyUpdate((packet) -> {});
        partsViewWidget.notifyUpdate((packet) -> {});
    }

    private void setPredicateDefaultBlock(PatternPredicate predicate, BlockInfo blockInfo) {
        predicateBlockMapping.put(predicate, blockInfo);
        predicateDefaultBlocks.clear();
        refreshViewWidget();
    }

    private void createAisleSliders(Flow col, BlockPattern blockPattern, String patternName) {
        int repeatAisleIndex = 0;
        for (var patternAisle : blockPattern.getAisles()) {
            if (patternAisle.getMinRepeats() != 1 || patternAisle.getMaxRepeats() != 1) {
                if (!aisleRepeats.row(patternName).containsKey(repeatAisleIndex)) {
                    aisleRepeats.put(patternName, repeatAisleIndex, patternAisle.getMinRepeats());
                }
                if (patternAisle.getMinRepeats() == patternAisle.getMaxRepeats()) {

                } else {
                    int finalRepeatAisleIndex = repeatAisleIndex;
                    col.child(new SliderWidget()
                            .background(GTGuiTextures.FLUID_SLOT)
                            .height(16)
                            .width(patternAisle.getMaxRepeats() * 12)
                            .stopper(1.0f)
                            .bounds(patternAisle.getMinRepeats(), patternAisle.getMaxRepeats())
                            .value(new DoubleValue.Dynamic(() -> {
                                if (!aisleRepeats.row(patternName).containsKey(finalRepeatAisleIndex)) return 0;
                                return aisleRepeats.row(patternName).get(finalRepeatAisleIndex);
                            }, (v) -> {
                                aisleRepeats.put(patternName, finalRepeatAisleIndex, (int) v);
                                predicateDefaultBlocks.clear();
                                predicatePositions.clear();
                                allPositions.clear();
                                refreshViewWidget();
                            })));
                }
            }
            repeatAisleIndex++;
        }
    }

    private void createPredicateMenus(String patternName, BlockPattern blockPattern, Flow predicatesRow) {
        for (var entry : blockPattern.getPredicates().char2ObjectEntrySet()) {
            var predicate = entry.getValue();
            // todo figure out sliders needed for predicate min/max depending on base predicates in the
            // main predicate
            if (predicate.equals(PatternPredicate.ANY) || predicate.equals(PatternPredicate.AIR)) {
                continue;
            }
            predicateSetting.put(predicate, predicate.predicateList.get(0).getCandidates().get(0));
            var menu = new ContextMenuButton<>(patternName + "#" + entry.getCharKey())
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
                                                    (b) -> setPredicateDefaultBlock(predicate,
                                                            candidates.get(0))))
                                            .size(16)
                                            .tooltip(r -> r.add(basePredicate.getPredicateName()))
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
                                            (b) -> setPredicateDefaultBlock(predicate, blockInfo)))
                                    .size(16)
                                    .tooltip(r -> r.add(stackName))
                                    .overlay(new ItemDrawable(
                                            blockInfo.getItemStackForm()));
                        }));
    }

    private void setupBlocks() {
        IBlockPattern mainPattern = multiblockDefinition.getStructurePatterns()
                .get(MultiblockControllerMachine.DEFAULT_STRUCTURE).get();
        if (mainPattern instanceof BlockPattern blockPattern) {
            var dimensions = blockPattern.getDimensions();
            maxLayers = dimensions[0];
            setupPredicatePositions(MultiblockControllerMachine.DEFAULT_STRUCTURE, blockPattern);

            for (var predicate : predicatePositions.keySet()) {
                if (!predicateBlockMapping.containsKey(predicate)) continue;
                BlockState blockState = predicateBlockMapping.get(predicate).getBlockState();
                for (long pos : predicatePositions.get(predicate)) {
                    blockState = setValidState(blockState, pos);
                    predicateDefaultBlocks.put(pos, blockState);
                }
            }

            for (var entry : predicatePositions.entrySet()) {
                PatternPredicate predicate = entry.getKey();
                int posSize = entry.getValue().size();
                Map<Long, BlockState> predicateBlocks = new HashMap<Long, BlockState>(posSize);
                setupPredicateBlocks(predicate, predicatePositions.get(predicate), predicateBlocks);
                predicateDefaultBlocks.putAll(predicateBlocks);
            }
        }
    }

    private void setupPredicateBlocks(PatternPredicate patternPredicate, Set<Long> positions,
                                      Map<Long, BlockState> blocks) {
        boolean placedBlock = false;
        boolean filledMinCounts = false;
        int predicatePointer = 0;
        for (var pos : positions) {
            BasePredicate basePred = patternPredicate.predicateList.get(predicatePointer);
            // attempt to fill blocks with other predicate values before wrapping back to higher priority predicate
            // values
            int specificPlaced = placed.getOrDefault(basePred, 0);
            while (specificPlaced > basePred.minCount && !filledMinCounts) {
                predicatePointer++;
                if (predicatePointer >= patternPredicate.predicateList.size()) {
                    filledMinCounts = true;
                    break;
                }
                basePred = patternPredicate.predicateList.get(predicatePointer);
                // can not place more of this specific base predicate
                if (placed.getOrDefault(basePred, 0) == basePred.maxCount) {
                    predicatePointer++;
                }
            }

            if (filledMinCounts) {
                predicatePointer = 0;
                basePred = patternPredicate.predicateList.get(predicatePointer);
            }
            BlockState state = null;
            if (predicateBlockMapping.containsKey(patternPredicate)) {
                state = predicateBlockMapping.get(patternPredicate).getBlockState();
            } else {
                List<BlockInfo> candidate = basePred.candidates;
                state = candidate.get(0).getBlockState();
            }
            blocks.put(pos, setValidState(state, pos));
            placed.merge(basePred, 1, Integer::sum);
        }
    }

    private void setupPredicatePositions(String patternName, BlockPattern blockPattern) {
        var dimensions = blockPattern.getDimensions();

        var mapping = blockPattern.getPredicates();
        var aisles = blockPattern.getAisles();
        BlockPos controllerPos = BlockPos.ZERO;
        RelativeDirection[] directions = blockPattern.getDirections();

        Direction frontFacing = multiblockDefinition.getRotationState().defaultDirection;
        Direction upFacing;
        switch (multiblockDefinition.getRotationState()) {
            case NONE -> upFacing = Direction.UP;
            case ALL -> upFacing = frontFacing.getAxis() == Direction.Axis.Y ? Direction.NORTH : Direction.UP;
            case Y_AXIS -> upFacing = Direction.NORTH;
            case NON_Y_AXIS -> upFacing = Direction.UP;
            default -> upFacing = Direction.UP;
        }

        Direction absoluteAisle = directions[0].getRelativeFacing(frontFacing, upFacing, false);
        Direction absoluteString = directions[1].getRelativeFacing(frontFacing, upFacing, false);
        Direction absoluteChar = directions[2].getRelativeFacing(frontFacing, upFacing, false);

        BlockPos.MutableBlockPos aisleStart = controllerPos.mutable();
        blockPattern.getOffset().apply(aisleStart, frontFacing, upFacing, false);

        List<Integer> aisleIndices = new ArrayList<>();
        for (int i = 0; i < dimensions[0]; i++) {
            if (aisleRepeats.row(patternName).containsKey(i)) {
                for (int j = 0; j < aisleRepeats.row(patternName).get(i); j++) {
                    aisleIndices.add(i);
                }
            } else {
                aisleIndices.add(i);
            }
        }

        for (Integer aisleIdx : aisleIndices) {
            PatternAisle aisle = aisles[aisleIdx];
            BlockPos.MutableBlockPos stringStart = aisleStart.mutable();
            BlockPos.MutableBlockPos charPos = aisleStart.mutable();
            for (int strIdx = 0; strIdx < dimensions[1]; strIdx++) {
                for (int charIdx = 0; charIdx < dimensions[2]; charIdx++) {
                    PatternPredicate predicate = mapping.get(aisle.charAt(strIdx, charIdx));
                    if (predicate.equals(PatternPredicate.ANY) || predicate.equals(PatternPredicate.AIR)) {
                        predicatePositions.getOrDefault(PatternPredicate.AIR, new HashSet<>()).add(charPos.asLong());
                    } else {
                        if (!predicatePositions.containsKey(predicate)) {
                            predicatePositions.put(predicate, new HashSet<>());
                        }
                        predicatePositions.get(predicate).add(charPos.asLong());
                        allPositions.add(charPos.asLong());
                    }
                    charPos.move(absoluteChar);
                }
                stringStart.move(absoluteString);
                charPos.set(stringStart);
            }
            aisleStart.move(absoluteAisle);
        }
    }

    public BlockState setValidState(BlockState state, long pos) {
        Direction valid = null;
        BlockPos blockPos = BlockPos.of(pos);
        if (state.getBlock() instanceof MetaMachineBlock machineBlock) {
            for (var dir : GTUtil.HORIZONTALS) {
                if (!allPositions.contains(blockPos.relative(dir).asLong())) {
                    valid = dir;
                    break;
                }
            }
            if (state.hasProperty(BlockStateProperties.FACING)) {
                if (valid != null) {
                    state = state.setValue(machineBlock.getRotationState().property, valid);
                } else {
                    if (!allPositions.contains(blockPos.relative(Direction.UP).asLong())) {
                        state = state.setValue(machineBlock.getRotationState().property, Direction.UP);
                    } else if (!allPositions.contains(blockPos.relative(Direction.DOWN).asLong())) {
                        state = state.setValue(machineBlock.getRotationState().property, Direction.DOWN);
                    }
                }
            }
        }
        return state;
    }
}
