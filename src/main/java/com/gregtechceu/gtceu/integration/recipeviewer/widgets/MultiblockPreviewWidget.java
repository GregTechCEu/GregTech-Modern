package com.gregtechceu.gtceu.integration.recipeviewer.widgets;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandablePattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.predicates.*;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.multiblock.util.BlockPatternStructureHelper;
import com.gregtechceu.gtceu.api.multiblock.util.ExpandablePatternStructureHelper;
import com.gregtechceu.gtceu.client.mui.schema.MutableSchema;
import com.gregtechceu.gtceu.client.renderer.PatternPreviewRenderer;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.IIcon;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IGuiAction;
import brachy.modularui.drawable.Icon;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.drawable.SchemaRenderer;
import brachy.modularui.drawable.schema.BlockHighlight;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.Color;
import brachy.modularui.value.BoolValue;
import brachy.modularui.value.IntValue;
import brachy.modularui.value.ObjectValue;
import brachy.modularui.value.sync.DynamicSyncHandler;
import brachy.modularui.widget.EmptyWidget;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.*;
import brachy.modularui.widgets.dynamic.DynamicHandler;
import brachy.modularui.widgets.dynamic.DynamicWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.menu.ContextMenuButton;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine.DEFAULT_STRUCTURE;

@Accessors(chain = true)
public class MultiblockPreviewWidget extends ParentWidget<MultiblockPreviewWidget> {

    private final MultiblockMachineDefinition multiblockDefinition;

    // schema stuff
    private final SchemaWidget multiSchema;
    @Getter
    @ApiStatus.Internal
    private MutableSchema mapSchema;
    private DynamicSyncHandler partsViewWidget;
    private final SchemaRenderer renderer;
    private final DynamicHandler partsHandler = new DynamicHandler();
    private final DynamicHandler selectedBlockHandler = new DynamicHandler();
    private final Reference2IntMap<Block> blockCounts = new Reference2IntOpenHashMap<>();

    @Setter
    private boolean isFlipped = false;
    @Setter
    private Direction frontFacing;
    @Setter
    private Direction upFacing;
    @Setter
    private @Nullable BlockPos controllerPos;
    private Pair<BlockPos, BlockInfo> lastBlock = null;
    @Getter
    private final Long2ObjectMap<BlockInfo> userGlobalBlockPreferences = new Long2ObjectOpenHashMap<>();
    @Getter
    private final Table<PatternPredicate, BasePredicate, BlockInfo> userBasePredicateBlockPreferences = HashBasedTable
            .create();
    @Getter
    private final Table<PatternPredicate, BasePredicate, IntIntPair> userBasePredicateMinMaxPreferences = HashBasedTable
            .create();

    private int yLevel = -1;
    private int maxHeight = 0;

    private @Nullable BlockPatternStructureHelper structureHelper;
    @Getter
    private final Int2IntMap userSliceRepeats = new Int2IntArrayMap();

    private @Nullable ExpandablePatternStructureHelper expandableStructureHelper;
    @Getter
    private IntList userDimensions = IntLists.emptyList();

    @Getter
    private final Map<BlockPos, BlockInfo> structureBlocks = new HashMap<>();

    @Setter
    private @Nullable Runnable onSchemaRefresh;

    public MultiblockPreviewWidget(MultiblockMachineDefinition definition) {
        this.multiblockDefinition = definition;
        this.frontFacing = definition.getRotationState().defaultDirection;
        this.upFacing = switch (definition.getRotationState()) {
            case Y_AXIS -> Direction.NORTH;
            case ALL, NON_Y_AXIS, NONE -> Direction.UP;
        };

        refreshSchema();

        this.renderer = new SchemaRenderer(this.mapSchema)
                .highlightRenderer(new BlockHighlight(Color.withAlpha(Color.GREEN.brighter(1), 0.9f), 1 / 32f));

        ItemDrawable selectedBlockDrawable = new ItemDrawable();
        ObjectValue<ItemStack> selectedBlock = new ObjectValue<>(ItemStack.class, ItemStack.EMPTY);
        IGuiAction.MouseReleased setBlockOnClick = (ctx, m) -> {
            if (m == InputConstants.MOUSE_BUTTON_LEFT) {
                BlockHitResult rayTrace = this.renderer.lastRayTrace();
                if (rayTrace != null && rayTrace.getType() == HitResult.Type.BLOCK) {
                    BlockState state = this.mapSchema.getLevel().getBlockState(rayTrace.getBlockPos());
                    this.lastBlock = Pair.of(rayTrace.getBlockPos(), BlockInfo.fromBlockState(state));
                    selectedBlock.setValue(new ItemStack(state.getBlock()));
                    selectedBlockDrawable.item(selectedBlock.getValue());
                    this.selectedBlockHandler.notifyUpdate();
                    return true;
                }
            }
            return false;
        };

        this.partsHandler.widgetProvider(() -> Flow.col()
                .name("wrapping_parts_col")
                // NOTE wrapped flows require a fixed size in their axis, relative/coverChildren does not work
                .wrap()
                .coverChildrenWidth(20)
                .height(200)
                .children(blockCounts.reference2IntEntrySet(), e -> {
                    ItemStack stack = new ItemStack(e.getKey(), e.getIntValue());
                    return new ItemDrawable(stack)
                            .asWidget().size(18).margin(1)
                            .tooltip(r -> r.addFromItem(stack));
                }));

        this.selectedBlockHandler.widgetProvider(() -> {
            ItemStack selected = selectedBlock.getValue();
            if (selected.isEmpty()) return null;

            IBlockPattern pattern = multiblockDefinition.getStructurePatterns().get("main").get();
            if (pattern instanceof BlockPattern blockPattern) {
                @SuppressWarnings("DataFlowIssue") // realistically it can't be null here
                PatternPredicate predicate = structureHelper.getPredicateFromPos(
                        blockPattern, lastBlock.left(), frontFacing, upFacing, isFlipped);

                return createSelectedBlockMenu(predicate, lastBlock);
            } else if (pattern instanceof ExpandablePattern expandablePattern) {
                @SuppressWarnings("DataFlowIssue") // realistically it can't be null here
                PatternPredicate predicate = expandableStructureHelper.getPredicateFromPos(
                        expandablePattern, lastBlock.left(), frontFacing, upFacing, isFlipped);

                return createSelectedBlockMenu(predicate, lastBlock);
            }
            return null;
        });

        List<Map.Entry<String, IBlockPattern>> patterns = multiblockDefinition.getStructurePatterns()
                .entrySet().stream().map(e -> Map.entry(e.getKey(), e.getValue().get())).toList();

        this.multiSchema = this.renderer.asWidget()
                .listenGuiAction(setBlockOnClick)
                .tooltipDynamic(text -> {
                    BlockHitResult hit = this.renderer.lastRayTrace();
                    if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                        BlockState state = mapSchema.getLevel().getBlockState(hit.getBlockPos());
                        ItemStack pickedItem = state.getCloneItemStack(hit, mapSchema.getLevel(), hit.getBlockPos(),
                                this.getContext().getMC().player);
                        text.addFromItem(pickedItem);
                    }
                }).tooltipAutoUpdate(true)
                .size(200);
        this.multiSchema.getSchemaRenderer().updateRenderFilter((pos, state) -> {
            if (yLevel == -1) {
                return true;
            }
            return pos.getY() >= yLevel;
        });

        this.coverChildren()
                .padding(7)
                .child(new ButtonWidget<>()
                        .tooltip(r -> r.addLine(Component.literal("Press to display preview in world!")))
                        .rightRel(1.0f)
                        .onMousePressed((c, b) -> {
                            if (controllerPos != null && !structureBlocks.isEmpty()) {
                                BlockPos origin = controllerPos.offset(mapSchema.getControllerPos().multiply(-1));
                                PatternPreviewRenderer.INSTANCE.showPreview(origin,
                                        this.mapSchema, this.multiSchema.getSchemaRenderer().renderFilter(),
                                        ConfigHolder.INSTANCE.client.inWorldPreviewDuration * 20);
                            }
                            return true;
                        }))
                .child(Flow.col()
                        .name("main")
                        .coverChildren()
                        .child(new ListWidget<>()
                                .name("structure_patterns")
                                .widthRel(1f)
                                .coverChildrenHeight()
                                .children(patterns, e -> {
                                    Flow patternColumn = Flow.col()
                                            .coverChildren();
                                    Flow predicatesRow = Flow.row()
                                            .name("predicates")
                                            .height(20)
                                            .coverChildrenWidth();

                                    if (e.getValue() instanceof BlockPattern blockPattern) {
                                        createSliceSliders(patternColumn, blockPattern);
                                        createPredicateMenus(predicatesRow, blockPattern);
                                    } else if (e.getValue() instanceof ExpandablePattern expandablePattern) {
                                        createConstraintSliders(patternColumn, expandablePattern);
                                    }

                                    patternColumn.child(predicatesRow);
                                    return patternColumn;
                                }))
                        .child(Flow.row()
                                .name("schema_widgets")
                                .crossAxisAlignment(Alignment.CrossAxis.START)
                                .coverChildren()
                                .child(new DynamicWidget<>()
                                        .name("selected_block")
                                        .coverChildren(20)
                                        .clientOnlyHandler(this.selectedBlockHandler))
                                .child(this.multiSchema)
                                .child(new DynamicWidget<>()
                                        .coverChildrenWidth()
                                        .heightRel(1f)
                                        .name("parts_view")
                                        .clientOnlyHandler(partsHandler))));
    }

    private ContextMenuButton<?> createSelectedBlockMenu(PatternPredicate predicate,
                                                         Pair<BlockPos, BlockInfo> lastBlock) {
        // TODO this can throw invalid state exception when
        // opening after clicking on a block twice
        return new ContextMenuButton<>(lastBlock.left().toString())
                .size(20)
                .overlay(new ItemDrawable(lastBlock.right().getItemStackForm()).asIcon().center())
                .tooltip(text -> text.addFromItem(lastBlock.right().getItemStackForm()))
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

    private void createPredicateMenus(Flow predicatesRow, BlockPattern blockPattern) {
        for (var entry : blockPattern.getPredicates().char2ObjectEntrySet()) {
            PatternPredicate predicate = entry.getValue();
            // todo figure out sliders needed for predicate min/max depending on base predicates in the
            // main predicate
            if (predicate.equals(PatternPredicate.ANY) || predicate.equals(PatternPredicate.AIR)) {
                continue;
            }
            IDrawable overlay;
            if (predicate.subPredicates.size() == 1 && predicate.subPredicates.get(0).candidates.size() == 1) {
                continue;
            } else {
                overlay = Text.str(String.valueOf(entry.getCharKey())).asIcon().size(8).center();
            }

            var menu = new ContextMenuButton<>(String.valueOf(entry.getCharKey()))
                    .overlay(overlay)
                    .tooltip(text -> {
                        if (overlay instanceof IIcon icon && icon.getWrappedDrawable() instanceof ItemDrawable item) {
                            ItemStack stack = item.getItemList().get(0);
                            text.addFromItem(stack);
                        }
                        text.addLine(Text.str("Multiblock Key: %s", String.valueOf(entry.getCharKey())));
                    })
                    .size(20)
                    .requiresClick()
                    .menuList(l -> l
                            .maxSize(80)
                            .coverChildrenWidth()
                            .collapseDisabledChildren()
                            .childSeparator(Icon.EMPTY_2PX)
                            .children(predicate.subPredicates, basePredicate -> {
                                List<BlockInfo> candidates = basePredicate.candidates;
                                if (candidates.isEmpty()) {
                                    return new EmptyWidget();
                                } else if (candidates.size() > 1) {
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

    /// ==== Schema setup ====
    private void refreshSchema() {
        Map<BlockPos, BlockInfo> resultStructure = new HashMap<>();
        IBlockPattern pattern = multiblockDefinition.getStructurePatterns().get(DEFAULT_STRUCTURE).get();

        if (pattern instanceof BlockPattern blockPattern) {
            if (userSliceRepeats.isEmpty()) {
                for (int i = 0; i < blockPattern.getSlices().length; i++) {
                    userSliceRepeats.put(i, blockPattern.getSlices()[i].getMinRepeats());
                }
            }
            // reinterpret slider values as slice repeats?
            structureHelper = new BlockPatternStructureHelper(userBasePredicateBlockPreferences,
                    userBasePredicateMinMaxPreferences, userSliceRepeats);
            char[][][] flattenedCharPattern = structureHelper.flattenBlockPattern(blockPattern);
            char[][][] adjustedCharPattern = BlockPatternStructureHelper.rotateAndFlipPattern(flattenedCharPattern,
                    blockPattern.getDirections(),
                    frontFacing, upFacing, isFlipped);

            structureHelper.populateWithUserBlockPreferences(resultStructure, blockPattern,
                    adjustedCharPattern,
                    userGlobalBlockPreferences, frontFacing, upFacing, isFlipped);

            structureHelper.populateFromPattern(resultStructure, blockPattern, adjustedCharPattern,
                    frontFacing, upFacing, isFlipped);

            BlockPatternStructureHelper.fixRotationsAndFacing(resultStructure, frontFacing, upFacing,
                    multiblockDefinition.getBlock());
        } else if (pattern instanceof ExpandablePattern expandablePattern) {
            if (userDimensions.isEmpty()) {
                userDimensions = expandablePattern.getBoundsConstraints().apply().stream()
                        .mapToInt(Pair::left)
                        .collect(IntArrayList::new, IntList::add, IntList::addAll);
            }
            // reinterpret slider values as bounds?
            expandableStructureHelper = new ExpandablePatternStructureHelper(userBasePredicateBlockPreferences,
                    userBasePredicateMinMaxPreferences, userDimensions);

            expandableStructureHelper.populateWithUserBlockPreferences(resultStructure, expandablePattern,
                    userGlobalBlockPreferences, frontFacing, upFacing, isFlipped);

            expandableStructureHelper.populateFromPattern(resultStructure, expandablePattern, frontFacing,
                    upFacing, isFlipped);

            BlockPatternStructureHelper.fixRotationsAndFacing(resultStructure, frontFacing, upFacing,
                    multiblockDefinition.getBlock());
        }

        Long2ReferenceMap<BlockState> schemaMap = new Long2ReferenceOpenHashMap<>();
        blockCounts.clear();
        for (var entry : resultStructure.entrySet()) {
            BlockState state = entry.getValue().getBlockState();
            schemaMap.put(entry.getKey().asLong(), state);
            blockCounts.merge(state.getBlock(), 1, Integer::sum);
        }
        if (this.mapSchema == null) {
            this.mapSchema = new MutableSchema(schemaMap);
        } else {
            this.mapSchema.setBlocks(schemaMap);
        }
        structureBlocks.clear();
        structureBlocks.putAll(resultStructure);

        if (onSchemaRefresh != null) {
            onSchemaRefresh.run();
        }
    }

    private void refreshViewWidget() {
        if (partsViewWidget != null) {
            partsViewWidget.notifyUpdate((packet) -> {});
        }
        if (partsHandler != null) {
            partsHandler.notifyUpdate();
        }
        if (multiSchema != null) {
            multiSchema.getSchemaRenderer().notifyRecompile();
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

    private void createConstraintSliders(Flow parent, ExpandablePattern pattern) {
        if (pattern.getBoundsConstraints() == null) {
            return;
        }
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
                        .value(new IntValue.Dynamic(() -> userDimensions.getInt(index), v -> {
                            int oldValue = userDimensions.getInt(index);
                            if (oldValue == v) return;
                            userDimensions.set(index, v);
                            refreshSchema();
                            refreshViewWidget();
                        })));
            }
        }
    }

    private void createSliceSliders(Flow col, BlockPattern blockPattern) {
        int repeatSliceIndex = 0;
        for (var patternSlice : blockPattern.getSlices()) {
            if (patternSlice.getMinRepeats() == 1 && patternSlice.getMaxRepeats() == 1) {
                continue;
            }
            if (!userSliceRepeats.containsKey(repeatSliceIndex)) {
                userSliceRepeats.put(repeatSliceIndex, patternSlice.getMinRepeats());
            }
            if (patternSlice.getMinRepeats() != patternSlice.getMaxRepeats()) {
                final int index = repeatSliceIndex;
                col.child(new SliderWidget()
                        .background(GTGuiTextures.FLUID_SLOT)
                        .height(16)
                        .width(patternSlice.getMaxRepeats() * 12)
                        .stopper(1.0f)
                        .bounds(patternSlice.getMinRepeats(), patternSlice.getMaxRepeats())
                        .value(new IntValue.Dynamic(() -> {
                            if (!userSliceRepeats.containsKey(index)) return 0;
                            return userSliceRepeats.get(index);
                        }, v -> {
                            int oldValue = userSliceRepeats.getOrDefault(index, 0);
                            if (oldValue == v) return;
                            userSliceRepeats.put(index, v);
                            refreshSchema();
                            refreshViewWidget();
                        })));
            }
            repeatSliceIndex++;
        }
    }
}
