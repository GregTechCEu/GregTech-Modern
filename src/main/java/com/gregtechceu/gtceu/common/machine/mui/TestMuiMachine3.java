package com.gregtechceu.gtceu.common.machine.mui;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandablePattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.multiblock.util.ExpandablePatternStructureHelper;
import com.gregtechceu.gtceu.client.mui.schema.MultiblockSchema;
import com.gregtechceu.gtceu.client.mui.schema.MutableSchema;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
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
import brachy.modularui.value.IntValue;
import brachy.modularui.value.sync.DynamicSyncHandler;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.EmptyWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.SchemaWidget;
import brachy.modularui.widgets.SliderWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.dynamic.DynamicHandler;
import brachy.modularui.widgets.dynamic.DynamicWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.menu.ContextMenuButton;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine.DEFAULT_STRUCTURE;

public class TestMuiMachine3 extends MetaMachine implements IMuiMachine {

    private final MultiblockMachineDefinition multiblockDefinition;

    // schema stuff
    private SchemaWidget multiSchema;
    private MutableSchema mapSchema;
    private DynamicSyncHandler partsViewWidget;
    private final DynamicHandler selectedBlockHandler = new DynamicHandler();
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
    private final Table<PatternPredicate, BasePredicate, BlockInfo> userBasePredicateBlockPreferences = HashBasedTable
            .create();
    private final Table<PatternPredicate, BasePredicate, IntIntPair> userBasePredicateMinMaxPreferences = HashBasedTable
            .create(); // Min, Max.
    private final List<Integer> userDimensions = new ArrayList<>();
    private @Nullable ExpandablePatternStructureHelper structureHelper;

    public TestMuiMachine3(BlockEntityCreationInfo info) {
        super(info);
        multiblockDefinition = (MultiblockMachineDefinition) GTMultiMachines.CLEANROOM;
        // var pattern = ((ExpandablePattern) multiblockDefinition.getStructurePatterns().get("main").get());
        frontFacing = multiblockDefinition.getRotationState().defaultDirection;
        switch (multiblockDefinition.getRotationState()) {
            case NONE -> upFacing = Direction.UP;
            case ALL -> upFacing = frontFacing.getAxis() == Direction.Axis.Y ? Direction.NORTH : Direction.UP;
            case Y_AXIS -> upFacing = Direction.NORTH;
            case NON_Y_AXIS -> upFacing = Direction.UP;
            default -> upFacing = Direction.UP;
        }

        userDimensions.addAll(List.of(0, 4, 2, 2, 2, 2));
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

                    if (pattern instanceof ExpandablePattern expandablePattern) {
                        // TODO:
                        createConstraintSliders(patternColumn, expandablePattern);
                        // createDimensionSliders(patternColumn, expandablePattern);
                    }
                    patternColumn.child(predicatesRow);
                    return patternColumn;
                }));

        Flow schemaCol = Flow.col().coverChildren();
        refreshSchema();

        selectedBlockHandler.widgetProvider(() -> {
            if (lastBlock == null) {
                return new EmptyWidget();
            }
            PatternPredicate predicate = structureHelper.getPredicateFromPos(
                    (ExpandablePattern) multiblockDefinition.getStructurePatterns().get("main").get(),
                    lastBlock.left(), frontFacing, upFacing, isFlipped);

            return createSelectedBlockMenu(predicate, lastBlock);
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
                        selectedBlockHandler.notifyUpdate();
                        return true;
                    }
                    return false;
                };
            };
            multiSchema.getSchemaRenderer().updateRenderFilter((pos, state) -> pos.getY() < slice);
            schemaCol.child(multiSchema.size(200, 200));
            schemaCol.child(new SchemaWidget.LayerButton(multiSchema.getSchemaRenderer(), 0, maxSlices));
        }
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
        panel.child(new DynamicWidget<>().syncHandler(partsViewWidget).rightRel(-1.0f).coverChildren());
        panel.child(
                new DynamicWidget<>().clientOnlyHandler(selectedBlockHandler).setEnabledIf((w) -> lastBlock != null));
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
        ExpandablePattern pattern = (ExpandablePattern) multiblockDefinition.getStructurePatterns()
                .get(DEFAULT_STRUCTURE).get();

        structureHelper = new ExpandablePatternStructureHelper(userBasePredicateBlockPreferences,
                userBasePredicateMinMaxPreferences, userDimensions);
        structureHelper.populateWithUserBlockPreferences(resultStructure, pattern,
                userGlobalBlockPreferences, frontFacing, upFacing, isFlipped);

        structureHelper.populateFromPattern(resultStructure, pattern, frontFacing, upFacing, isFlipped);

        ExpandablePatternStructureHelper.fixRotationsAndFacing(resultStructure, frontFacing, upFacing,
                multiblockDefinition.getBlock());

        Long2ReferenceMap<BlockState> schemaMap = new Long2ReferenceOpenHashMap<>();
        blockCounts.clear();
        for (var entry : resultStructure.entrySet()) {
            var state = entry.getValue().getBlockState();
            schemaMap.put(entry.getKey().asLong(), state);
            blockCounts.merge(state.getBlock(), 1, Integer::sum);
        }
        if (mapSchema == null) {
            mapSchema = new MultiblockSchema(schemaMap);
        } else {
            mapSchema.setBlocks(schemaMap);
        }
    }

    /// ==== User Preference UI ====

    private void setUserDefinedBlockInfo(BlockPos pos, BlockInfo blockInfo) {
        // todo validation testing?
        userGlobalBlockPreferences.put(pos.asLong(), blockInfo);
        refreshSchema();
        refreshViewWidget();
    }

    private void createConstraintSliders(Flow parent, ExpandablePattern pattern) {
        if (pattern.getBoundsConstraints() != null) {
            List<IntIntPair> constraints = pattern.getBoundsConstraints().apply();
            for (int i = 0; i < constraints.size(); i++) {
                IntIntPair value = constraints.get(i);
                if (value.leftInt() != value.rightInt()) {
                    final int index = i;
                    parent.child(new SliderWidget()
                            .background(GTGuiTextures.FLUID_SLOT)
                            .bounds(value.leftInt(), value.rightInt())
                            .height(16)
                            .width(value.rightInt() * 12)
                            .stopper(1.0f)
                            .value(new IntValue.Dynamic(() -> userDimensions.get(index), v -> {
                                userDimensions.set(index, v);
                                refreshSchema();
                                refreshViewWidget();
                            })));
                }
            }
        }
    }

    private ContextMenuButton<?> createSelectedBlockMenu(PatternPredicate predicate,
                                                         Pair<BlockPos, BlockInfo> lastBlock) {
        return new ContextMenuButton<>(lastBlock.left().toString())
                .size(20)
                .overlay(new ItemDrawable(lastBlock.right().getItemStackForm()))
                .requiresClick()
                .menuList(l -> l
                        .maxSize(80)
                        .coverChildrenWidth()
                        .collapseDisabledChildren()
                        .childSeparator(Icon.EMPTY_2PX)
                        .children(predicate.subPredicates, basePredicate -> {
                            List<BlockInfo> candidates = basePredicate.candidates;
                            if (candidates.isEmpty())
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
}
