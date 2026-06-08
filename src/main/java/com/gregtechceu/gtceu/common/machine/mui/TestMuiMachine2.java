package com.gregtechceu.gtceu.common.machine.mui;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.multiblock.util.BlockPatternStructureUtil;
import com.gregtechceu.gtceu.client.mui.schema.MutableSchema;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.Icon;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.drawable.SchemaRenderer;
import brachy.modularui.drawable.schema.BlockHighlight;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Color;
import brachy.modularui.value.BoolValue;
import brachy.modularui.value.DoubleValue;
import brachy.modularui.value.sync.DynamicSyncHandler;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.EmptyWidget;
import brachy.modularui.widgets.*;
import brachy.modularui.widgets.dynamic.DynamicWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.menu.ContextMenuButton;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.*;

import java.util.*;

import static com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine.DEFAULT_STRUCTURE;

public class TestMuiMachine2 extends MetaMachine implements IMuiMachine {

    private final MultiblockMachineDefinition multiblockDefinition;

    // schema stuff
    private SchemaWidget multiSchema;
    private MutableSchema mapSchema;
    private DynamicSyncHandler partsViewWidget;
    private DynamicSyncHandler selectedBlockWidget;
    private final Reference2IntMap<Block> blockCounts = new Reference2IntOpenHashMap<>();

    // for the slice slider
    private int slice = 0;
    private int maxSlices = 0;

    // user inputs
    private boolean isFlipped = false;
    private Direction frontFacing;
    private Direction upFacing;
    private Pair<BlockPos, BlockInfo> lastBlock = null;
    private final Map<Long, BlockInfo> userGlobalBlockPreferences = new Long2ReferenceOpenHashMap<>();
    private final Map<Integer, Integer> userSliceRepeats = new Int2IntArrayMap();
    private final Table<PatternPredicate, BasePredicate, BlockInfo> userBasePredicateBlockPreferences = HashBasedTable
            .create();
    private final Table<PatternPredicate, BasePredicate, Pair<Integer, Integer>> userBasePredicateMinMaxPreferences = HashBasedTable
            .create(); // Min, Max.
    private final BlockPatternStructureUtil structureUtil = new BlockPatternStructureUtil();
    // ^ To disable a base predicate, set min to 0

    /// ALL INFO RELEVANT TO STRUCTURE AUTO BUILDING:
    /// INPUTS:
    /// User supplied, ordered by priority:
    /// Layer sizes (layerRepeats) from user,
    /// Overrides for which candidate to pick per BlockPos
    /// - e.g. "left of controller should be maintenance hatch"
    /// - e.g. DT with 10 repeats of the output hatch isle. Default to PatternLayer.minRepeats
    /// Overrides for which candidate to pick per BasePredicate
    /// - e.g. "all energy hatches should be HV"
    /// Overrides for the min/max for each BasePredicate
    /// - e.g. "only put 1 energy hatch instead of 2"
    /// - has to be within the BasePredicate's minCount/maxCount
    /// Disable a BasePredicate in a PatternPredicate
    /// - has to respect the BasePredicate's minCount
    /// - e.g. can disable EBF's fluid outputs, but can't disable EBF's casing
    ///
    ///
    /// Machine supplied:
    /// BlockPattern:
    /// - PatternLayer[], each PatternLayer:
    /// - minRepeats
    /// - maxRepeats
    /// - Char[][] pattern, NxM array of char
    /// - Char <-> PatternPredicate, each PatternPredicate:
    /// - List of BasePredicates, each BasePredicate:
    /// - candidates, List<BlockInfo> The candidates to place
    /// - priority, specifically within the PatternPredicate
    /// - minCount, total minimum across the whole multi
    /// - maxCount, total maximum across the whole multi
    /// - minLayerCount, total minimum in one layer (e.g. hatch in DT) TODO: Should this be in BlockPattern instead?
    /// - maxLayerCount, total maximum in one layer
    ///
    /// OUTPUTS:
    /// Map<BlockPos, BlockInfo> resultStructure
    /// Descriptive error if not possible
    ///
    /// Naive approach:
    /// 1. Flatten PatternIsle[] (which contains Char[][]) into Char[][][] based on the layerRepeats
    /// 2. Create the final Map<BlockPos, BlockInfo>
    /// 3. Fill in the candidate overrides first (e.g. "0,1,0 should be maintenance hatch") if it fits any of the
    /// BasePredicates, error otherwise(?)
    /// 4. In any order (probably just naive x,y,z loop), get the char at that position,
    /// 4a. Go through every BasePredicate in order of priority, see if there's a minCount that's not satisfied yet,
    /// then try those
    /// 4b. If all basePredicates with a mincount are satisfied, place the first predicate that works
    /// 4c. If the BasePredicate is at its max, remove it from the list to be considered (maybe not needed at first, but
    /// optimization)
    /// 4d. error if none are valid candidates(?)
    ///
    /// note- For this, we should have a isValidCandidate(current resultStructure, new BlockPos, new BlockInfo) function
    ///
    public TestMuiMachine2(BlockEntityCreationInfo info) {
        super(info);
        multiblockDefinition = (MultiblockMachineDefinition) GTMultiMachines.ASSEMBLY_LINE;
        var pattern = ((BlockPattern) multiblockDefinition.getStructurePatterns().get("main").get());
        for (int i = 0; i < pattern.getSlices().length; i++) {
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
        // frontFacing = Direction.UP;
        // upFacing = Direction.WEST;
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

        Flow schemaCol = Flow.col().coverChildren()
                .name("schema_col");
        refreshSchema();

        selectedBlockWidget = new DynamicSyncHandler().widgetProvider((sm, buf) -> {
            if (lastBlock == null) {
                return new EmptyWidget();
            }
            PatternPredicate predicate = structureUtil.getPredicateFromPos(
                    (BlockPattern) multiblockDefinition.getStructurePatterns().get("main").get(),
                    frontFacing, upFacing, isFlipped, lastBlock.left());

            return createSelectedBlockMenu(predicate, lastBlock);
            // return new ItemDrawable(lastBlock.right().getItemStackForm()).asWidget();
        });

        if (getLevel().isClientSide()) {
            SchemaRenderer schemaRenderer = new SchemaRenderer(mapSchema);
            schemaRenderer
                    .highlightRenderer(new BlockHighlight(Color.withAlpha(Color.GREEN.brighter(1), 0.9f), 1 / 32f));

            multiSchema = new SchemaWidget(schemaRenderer) {

                @Override
                public boolean onMouseReleased(int button) {
                    BlockHitResult rayTraceResult = this.getSchemaRenderer().lastRayTrace();
                    if (rayTraceResult != null && rayTraceResult.getType() == HitResult.Type.BLOCK) {
                        BlockState state = this.getSchemaRenderer().schema().getLevel()
                                .getBlockState(rayTraceResult.getBlockPos());
                        lastBlock = Pair.of(rayTraceResult.getBlockPos(), BlockInfo.fromBlockState(state));
                        selectedBlockWidget.notifyUpdate((packet) -> {});
                        return true;
                    }
                    return false;
                }
            };
            multiSchema.tooltipDynamic(text -> {
                BlockHitResult rayTrace = schemaRenderer.lastRayTrace();
                if (rayTrace == null || rayTrace.getType() != HitResult.Type.BLOCK)
                    return;

                BlockState state = mapSchema.getLevel().getBlockState(rayTrace.getBlockPos());
                text.addFromItem(new ItemStack(state.getBlock()));
            }).tooltipAutoUpdate(true);
            multiSchema.getSchemaRenderer().updateRenderFilter((pos, state) -> pos.getY() < slice);
            schemaCol.child(multiSchema.size(200, 200));
            schemaCol.child(new SchemaWidget.LayerButton(multiSchema.getSchemaRenderer(), 0, maxSlices));
        }
        col.child(schemaCol);

        partsViewWidget = new DynamicSyncHandler().widgetProvider((sm, buf) -> {
            Flow innerCol = Flow.col().coverChildren().childPadding(2).rightRelOffset(1.0f, -20);
            innerCol.children(blockCounts.reference2IntEntrySet(), (e) -> {
                ItemStack stack = new ItemStack(e.getKey(), e.getIntValue());
                return new ItemDrawable(stack)
                        .asWidget()
                        .tooltip(r -> r.addFromItem(stack));
            });
            innerCol.childPadding(2).left(2);
            return innerCol;
        }).allowC2S();

        refreshViewWidget();
        panel.child(col);
        panel.child(new DynamicWidget<>().syncHandler(partsViewWidget).rightRel(-1.0f).coverChildren());
        panel.child(new DynamicWidget<>().syncHandler(selectedBlockWidget).setEnabledIf((w) -> lastBlock != null));
        return panel;
    }

    private void refreshViewWidget() {
        partsViewWidget.notifyUpdate((packet) -> {});
        if (multiSchema != null) {
            multiSchema.getSchemaRenderer().notifyRecompile();
        }
    }

    /// ==== Schema setup ====
    private void refreshSchema() {
        Map<BlockPos, BlockInfo> resultStructure;

        resultStructure = new HashMap<>();
        BlockPattern pattern = (BlockPattern) multiblockDefinition.getStructurePatterns().get(DEFAULT_STRUCTURE).get();
        maxSlices = pattern.getDimensions()[1];

        structureUtil.populatePreferenceTables(userBasePredicateBlockPreferences,
                userBasePredicateMinMaxPreferences, userSliceRepeats);
        char[][][] flattenedCharPattern = structureUtil.flattenBlockPattern(pattern);
        char[][][] adjustedCharPattern = structureUtil.rotateAndFlipCharPattern(flattenedCharPattern,
                pattern.getDirections(),
                frontFacing, upFacing, isFlipped);

        structureUtil.populateWithUserBlockPreferences(resultStructure, pattern, adjustedCharPattern,
                userGlobalBlockPreferences, frontFacing, upFacing, isFlipped);

        structureUtil.populateFromPattern(resultStructure, pattern, adjustedCharPattern,
                frontFacing, upFacing, isFlipped);

        structureUtil.fixRotationsAndFacing(resultStructure, frontFacing, upFacing,
                multiblockDefinition.getBlock());

        Long2ReferenceMap<BlockState> schemaMap = new Long2ReferenceOpenHashMap<>();
        blockCounts.clear();
        for (var entry : resultStructure.entrySet()) {
            var state = entry.getValue().getBlockState();
            schemaMap.put(entry.getKey().asLong(), state);
            blockCounts.merge(state.getBlock(), 1, Integer::sum);
        }
        if (mapSchema == null) {
            mapSchema = new MutableSchema(schemaMap);
        } else {
            mapSchema.setBlocks(schemaMap);
        }
    }

    /// ==== User Preference UI ======
    private void setPredicateDefaultBlock(PatternPredicate predicate, BasePredicate basePredicate,
                                          BlockInfo blockInfo) {
        userBasePredicateBlockPreferences.put(predicate, basePredicate, blockInfo);
        refreshSchema();
        refreshViewWidget();
    }

    private void setUserDefinedBlockInfo(BlockPos pos, BlockInfo blockInfo) {
        // todo validation testing?
        userGlobalBlockPreferences.put(pos.asLong(), blockInfo);
        refreshSchema();
        refreshViewWidget();
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
                                int oldVal = userSliceRepeats.getOrDefault(finalRepeatSliceIndex, 0);
                                int newVal = (int) v;
                                if (oldVal == newVal) return;
                                userSliceRepeats.put(finalRepeatSliceIndex, newVal);
                                refreshSchema();
                                refreshViewWidget();
                            })));
                }
            }
            repeatSliceIndex++;
        }
    }

    private ContextMenuButton<?> createSelectedBlockMenu(PatternPredicate predicate,
                                                         Pair<BlockPos, BlockInfo> lastBlock) {
        // TODO this can throw invalid state exception when
        // opening after clicking on a block twice
        return new ContextMenuButton<>(lastBlock.left().toString())
                .size(20)
                .overlay(new ItemDrawable(lastBlock.right().getItemStackForm()))
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
                                                                    (b) -> setUserDefinedBlockInfo(lastBlock.left(),
                                                                            blockInfo)))
                                                            .size(16)
                                                            .tooltip(r -> r.add(stackName))
                                                            .overlay(new ItemDrawable(
                                                                    blockInfo.getItemStackForm()));
                                                }));
                            } else {
                                return new ToggleButton()
                                        .value(new BoolValue.Dynamic(() -> false,
                                                (b) -> setUserDefinedBlockInfo(lastBlock.left(), candidates.get(0))))
                                        .size(16)
                                        .tooltip(r -> r.add(
                                                basePredicate.candidates.get(0).getItemStackForm().getHoverName()))
                                        .overlay(new ItemDrawable(
                                                candidates.get(0).getItemStackForm()));
                            }
                        }));
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
                                                    (b) -> setPredicateDefaultBlock(predicate, basePredicate,
                                                            candidates.get(0))))
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
