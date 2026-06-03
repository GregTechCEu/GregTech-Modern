package com.gregtechceu.gtceu.common.machine.mui;

import brachy.modularui.utils.fakelevel.MapSchema;
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
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternSlice;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.client.mui.schema.MutableSchema;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

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

import java.util.*;
import java.util.stream.Collectors;

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
    private final Map<Long, BlockInfo> blockPreferences = new Long2ReferenceOpenHashMap<>();
    private final Map<Integer, Integer> sliceRepeats = new Int2IntArrayMap();
    private final Table<PatternPredicate, BasePredicate, BlockInfo> basePredicateBlockPreferences = HashBasedTable.create();
    private final Table<PatternPredicate, BasePredicate, Pair<Integer, Integer>> basePredicateMinMaxPreferences = HashBasedTable.create(); // Min, Max
    private final Map<PatternPredicate, BasePredicate> disabledBasePredicatePreferences = new Object2ObjectArrayMap<>();




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
        multiblockDefinition = (MultiblockMachineDefinition) GTMultiMachines.ASSEMBLY_LINE;
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
                    String patternName = e.getKey();
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
        Map<BlockPos, BlockInfo> resultStructure; // TODO make this lol

        resultStructure = new HashMap<>();


        Map<BlockPos, BlockState> schemaMap = resultStructure.entrySet()
                .stream()
                .map(entry -> Pair.of(entry.getKey(), entry.getValue().getBlockState()))
                .collect(Collectors.toMap(Pair::left, Pair::right));
        mapSchema = new MapSchema(schemaMap);
        mapSchema.setRenderFilter((pos, state) -> pos.getY() < slice);
        refreshViewWidget();
    }





    /// ==== User Preference UI ======
    private void setPredicateDefaultBlock(PatternPredicate predicate, BasePredicate basePredicate, BlockInfo blockInfo) {
        basePredicateBlockPreferences.put(predicate, basePredicate, blockInfo);
        refreshSchema();
    }

    private void createSliceSliders(Flow col, BlockPattern blockPattern) {
        int repeatSliceIndex = 0;
        for (var patternSlice : blockPattern.getSlices()) {
            if (patternSlice.getMinRepeats() != 1 || patternSlice.getMaxRepeats() != 1) {
                if (!sliceRepeats.containsKey(repeatSliceIndex)) {
                    sliceRepeats.put(repeatSliceIndex, patternSlice.getMinRepeats());
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
                                if (!sliceRepeats.containsKey(finalRepeatSliceIndex)) return 0;
                                return sliceRepeats.get(finalRepeatSliceIndex);
                            }, (v) -> {
                                sliceRepeats.put(finalRepeatSliceIndex, (int) v);
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
