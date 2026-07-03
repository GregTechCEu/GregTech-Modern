package com.gregtechceu.gtceu.integration.recipeviewer.widgets;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.mui.MultiblockSchemaInfo;
import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandablePattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.predicates.*;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
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
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.Color;
import brachy.modularui.value.BoolValue;
import brachy.modularui.value.IntValue;
import brachy.modularui.value.sync.DynamicSyncHandler;
import brachy.modularui.widget.EmptyWidget;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.*;
import brachy.modularui.widgets.dynamic.DynamicHandler;
import brachy.modularui.widgets.dynamic.DynamicWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.menu.ContextMenuButton;
import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Accessors(chain = true)
public class MultiblockPreviewWidget extends ParentWidget<MultiblockPreviewWidget> {

    private final MultiblockMachineDefinition multiblockDefinition;

    // schema stuff
    private DynamicSyncHandler partsViewWidget;
    // private final SchemaRenderer renderer;
    private final DynamicHandler partsHandler = new DynamicHandler();
    private final DynamicHandler selectedBlockHandler = new DynamicHandler();
    private final Reference2IntMap<Block> blockCounts = new Reference2IntOpenHashMap<>();

    @Getter
    @Setter
    private MultiblockSchemaInfo multiblockSchemaInfo;

    @Setter
    private boolean isFlipped = false;
    @Setter
    private Direction frontFacing;
    @Setter
    private Direction upFacing;
    @Setter
    private @Nullable BlockPos controllerPos;
    private SelectionInfo selectionInfo = SelectionInfo.empty();

    private int yLevel = -1;
    private int maxHeight = 0;

    @Setter
    private @Nullable Runnable onSchemaRefresh;

    public MultiblockPreviewWidget(MultiblockMachineDefinition definition, MultiblockSchemaInfo schemaInfo) {
        this.multiblockDefinition = definition;
        this.frontFacing = definition.getRotationState().defaultDirection;
        this.upFacing = switch (definition.getRotationState()) {
            case Y_AXIS -> Direction.NORTH;
            case ALL, NON_Y_AXIS, NONE -> Direction.UP;
        };
        this.multiblockSchemaInfo = schemaInfo == null ? new MultiblockSchemaInfo() : schemaInfo;
        refreshSchema();
        this.multiblockSchemaInfo.setRenderer(new SchemaRenderer(this.multiblockSchemaInfo.getMapSchema())
                .highlightRenderer(new BlockHighlight(Color.withAlpha(Color.GREEN.brighter(1), 0.9f), 1 / 32f)));

        IGuiAction.MouseReleased setBlockOnClick = (ctx, m) -> {
            if (m == InputConstants.MOUSE_BUTTON_LEFT) {
                BlockHitResult rayTrace = this.multiblockSchemaInfo.getRenderer().lastRayTrace();
                if (rayTrace != null && rayTrace.getType() == HitResult.Type.BLOCK) {
                    BlockState state = this.multiblockSchemaInfo.getMapSchema().getLevel()
                            .getBlockState(rayTrace.getBlockPos());
                    this.selectionInfo = SelectionInfo.of(rayTrace, state);
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
                .children(this.multiblockSchemaInfo.getBlockCounts().reference2IntEntrySet(), e -> {
                    ItemStack stack = new ItemStack(e.getKey(), e.getIntValue());
                    return new ItemDrawable(stack)
                            .asWidget().size(18).margin(1)
                            .tooltip(r -> r.addFromItem(stack));
                }));

        this.selectedBlockHandler.widgetProvider(() -> {
            ItemStack selected = this.selectionInfo.stack();
            if (selected.isEmpty()) return null;

            IBlockPattern pattern = multiblockDefinition.getStructurePatterns().get("main").get();
            if (pattern instanceof BlockPattern blockPattern) {
                @SuppressWarnings("DataFlowIssue") // realistically it can't be null here
                PatternPredicate predicate = this.multiblockSchemaInfo.getStructureHelper().getPredicateFromPos(
                        blockPattern, this.selectionInfo.pos(), frontFacing, upFacing, isFlipped);

                return createSelectedBlockMenu(predicate);
            } else if (pattern instanceof ExpandablePattern expandablePattern) {
                @SuppressWarnings("DataFlowIssue") // realistically it can't be null here
                PatternPredicate predicate = this.multiblockSchemaInfo.getStructureHelper().getPredicateFromPos(
                        expandablePattern, this.selectionInfo.pos(), frontFacing, upFacing, isFlipped);

                return createSelectedBlockMenu(predicate);
            }
            return null;
        });

        List<Map.Entry<String, IBlockPattern>> patterns = multiblockDefinition.getStructurePatterns()
                .entrySet().stream().map(e -> Map.entry(e.getKey(), e.getValue().get())).toList();

        this.multiblockSchemaInfo.setMultiSchema(this.multiblockSchemaInfo.getRenderer().asWidget()
                .listenGuiAction(setBlockOnClick)
                .tooltipDynamic(text -> {
                    BlockHitResult hit = this.multiblockSchemaInfo.getRenderer().lastRayTrace();
                    if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                        BlockState state = this.getMultiblockSchemaInfo().getMapSchema().getLevel()
                                .getBlockState(hit.getBlockPos());
                        ItemStack pickedItem = state.getCloneItemStack(hit,
                                this.getMultiblockSchemaInfo().getMapSchema().getLevel(), hit.getBlockPos(),
                                this.getContext().getMC().player);
                        text.addFromItem(pickedItem);
                    }
                }).tooltipAutoUpdate(true)
                .size(200));
        this.multiblockSchemaInfo.getMultiSchema().getSchemaRenderer().updateRenderFilter((pos, state) -> {
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
                            if (controllerPos != null &&
                                    !this.getMultiblockSchemaInfo().getStructureBlocks().isEmpty()) {
                                BlockPos origin = controllerPos.offset(
                                        this.getMultiblockSchemaInfo().getMapSchema().getControllerPos().multiply(-1));
                                PatternPreviewRenderer.INSTANCE.showPreview(origin,
                                        this.getMultiblockSchemaInfo().getMapSchema(),
                                        this.multiblockSchemaInfo.getMultiSchema().getSchemaRenderer().renderFilter(),
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
                                .child(this.multiblockSchemaInfo.getMultiSchema())
                                .child(new DynamicWidget<>()
                                        .coverChildrenWidth()
                                        .heightRel(1f)
                                        .name("parts_view")
                                        .clientOnlyHandler(partsHandler))));
    }

    private ContextMenuButton<?> createSelectedBlockMenu(PatternPredicate predicate) {
        // TODO this can throw invalid state exception when
        // opening after clicking on a block twice
        return new ContextMenuButton<>(this.selectionInfo.pos().toString())
                .size(20)
                .overlay(new ItemDrawable(this.selectionInfo.stack()).asIcon().center())
                .tooltip(text -> text.addFromItem(this.selectionInfo.stack()))
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
                                                                    (b) -> setUserDefinedBlockInfo(
                                                                            this.selectionInfo.pos(),
                                                                            blockInfo)))
                                                            .size(16)
                                                            .tooltip(r -> r.add(stackName))
                                                            .overlay(new ItemDrawable(
                                                                    blockInfo.getItemStackForm()));
                                                }));
                            } else {
                                return new ToggleButton()
                                        .value(new BoolValue.Dynamic(() -> false,
                                                (b) -> setUserDefinedBlockInfo(this.selectionInfo.pos(),
                                                        candidates.get(0))))
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
    @ApiStatus.Internal
    public void refreshSchema() {
        this.multiblockSchemaInfo.refreshSchema(multiblockDefinition, frontFacing, upFacing, isFlipped,
                onSchemaRefresh);
    }

    private void refreshViewWidget() {
        if (partsViewWidget != null) {
            partsViewWidget.notifyUpdate((packet) -> {});
        }
        if (partsHandler != null) {
            partsHandler.notifyUpdate();
        }
        if (this.multiblockSchemaInfo.getRenderer() != null) {
            this.multiblockSchemaInfo.getRenderer().notifyRecompile();
        }
    }

    /// ==== User Preference UI ======
    private void setPredicateDefaultBlock(PatternPredicate predicate, BasePredicate basePredicate,
                                          BlockInfo blockInfo) {
        this.multiblockSchemaInfo.putPredicatePreference(predicate, basePredicate, blockInfo);
        refreshSchema();
        refreshViewWidget();
    }

    private void setUserDefinedBlockInfo(BlockPos pos, BlockInfo blockInfo) {
        // todo validation testing?
        this.multiblockSchemaInfo.getUserGlobalBlockPreferences().put(pos.asLong(), blockInfo);
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
                        .value(new IntValue.Dynamic(
                                () -> this.getMultiblockSchemaInfo().getUserDimensions().getInt(index), v -> {
                                    int oldValue = this.getMultiblockSchemaInfo().getUserDimensions().getInt(index);
                                    if (oldValue == v) return;
                                    this.getMultiblockSchemaInfo().getUserDimensions().set(index, v);
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
                repeatSliceIndex++;
                continue;
            }
            if (!this.multiblockSchemaInfo.getUserSliceRepeats().containsKey(repeatSliceIndex)) {
                this.multiblockSchemaInfo.getUserSliceRepeats().put(repeatSliceIndex, patternSlice.getMinRepeats());
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
                            if (!this.multiblockSchemaInfo.getUserSliceRepeats().containsKey(index)) return 0;
                            return this.multiblockSchemaInfo.getUserSliceRepeats().get(index);
                        }, v -> {
                            int oldValue = this.multiblockSchemaInfo.getUserSliceRepeats().getOrDefault(index, 0);
                            if (oldValue == v) return;
                            this.multiblockSchemaInfo.getUserSliceRepeats().put(index, v);
                            refreshSchema();
                            refreshViewWidget();
                        })));
            }
            repeatSliceIndex++;
        }
    }

    protected record SelectionInfo(BlockPos pos, BlockInfo info) {

        protected static SelectionInfo empty() {
            return new SelectionInfo(BlockPos.ZERO, BlockInfo.EMPTY);
        }

        protected static SelectionInfo of(BlockHitResult result, BlockState state) {
            return new SelectionInfo(result.getBlockPos(), BlockInfo.fromBlockState(state));
        }

        public Block block() {
            return state().getBlock();
        }

        public BlockState state() {
            return info().getBlockState();
        }

        public ItemStack stack() {
            return info().getItemStackForm();
        }
    }
}
