package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.mui.IItemUIHolder;
import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandablePattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.multiblock.util.BlockPatternStructureUtil;
import com.gregtechceu.gtceu.api.multiblock.util.ExpandablePatternStructureUtil;
import com.gregtechceu.gtceu.client.mui.schema.MutableSchema;
import com.gregtechceu.gtceu.client.renderer.PatternPreviewRenderer;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
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
import brachy.modularui.factory.ClientGUI;
import brachy.modularui.factory.PlayerInventoryGuiData;
import brachy.modularui.factory.inventory.InventoryTypes;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.Color;
import brachy.modularui.value.BoolValue;
import brachy.modularui.value.DoubleValue;
import brachy.modularui.value.ObjectValue;
import brachy.modularui.value.sync.DynamicSyncHandler;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.EmptyWidget;
import brachy.modularui.widgets.*;
import brachy.modularui.widgets.dynamic.DynamicHandler;
import brachy.modularui.widgets.dynamic.DynamicWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.menu.ContextMenuButton;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;

import java.util.*;

import static com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine.DEFAULT_STRUCTURE;

public class TerminalBehavior implements IInteractionItem, IItemUIHolder {

    private MultiblockMachineDefinition multiblockDefinition = null;

    // schema stuff
    private SchemaWidget multiSchema;
    private MutableSchema mapSchema;
    private DynamicSyncHandler partsViewWidget;
    private SchemaRenderer renderer;
    private final DynamicHandler partsHandler = new DynamicHandler();
    private final DynamicHandler selectedBlockHandler = new DynamicHandler();
    private final Reference2IntMap<Block> blockCounts = new Reference2IntOpenHashMap<>();

    private boolean isFlipped = false;
    private Direction frontFacing;
    private Direction upFacing;
    private BlockPos controllerPos;
    private Pair<BlockPos, BlockInfo> lastBlock = null;
    private final Map<Long, BlockInfo> userGlobalBlockPreferences = new Long2ReferenceOpenHashMap<>();
    private final Table<PatternPredicate, BasePredicate, BlockInfo> userBasePredicateBlockPreferences = HashBasedTable
            .create();
    private final Table<PatternPredicate, BasePredicate, Pair<Integer, Integer>> userBasePredicateMinMaxPreferences = HashBasedTable
            .create();

    private int yLevel = -1;
    private int maxHeight = 0;

    private final BlockPatternStructureUtil blockPatternStructureUtil = new BlockPatternStructureUtil();
    private final Map<Integer, Integer> userSliceRepeats = new Int2IntArrayMap();
    private final ExpandablePatternStructureUtil expandablePatternStructureUtil = new ExpandablePatternStructureUtil();
    private final List<Integer> userDimensions = new ArrayList<>();

    private Map<BlockPos, BlockInfo> structureBlocks = new HashMap<>();

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack terminal = context.getItemInHand();
        var tag = terminal.getOrCreateTag();

        if (context.getPlayer() == null) return InteractionResult.PASS;
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos blockPos = context.getClickedPos();

        if (MetaMachine.getMachine(level, blockPos) instanceof MultiblockControllerMachine controller) {
            if (player.isShiftKeyDown()) {
                if (!controller.getDefaultPatternState().isFormed()) {
                    if (!level.isClientSide) {
                        for (var entry : structureBlocks.entrySet()) {
                            var mPos = entry.getKey().mutable();
                            mPos.move(controller.getBlockPos()).move(mapSchema.getControllerPos().multiply(-1));
                            level.setBlock(mPos, entry.getValue().getBlockState(), Block.UPDATE_ALL_IMMEDIATE);
                        }
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        if (context.getPlayer() == null) return InteractionResult.PASS;
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos blockPos = context.getClickedPos();

        if (MetaMachine.getMachine(level, blockPos) instanceof MultiblockControllerMachine controller) {
            if (!player.isShiftKeyDown()) {
                if (!controller.getDefaultPatternState().isFormed() || true) {
                    if (!level.isClientSide) {
                        var patterns = controller.getStructurePatterns();

                        multiblockDefinition = controller.getDefinition();
                        controllerPos = controller.getBlockPos();
                        frontFacing = controller.getFrontFacing();
                        upFacing = controller.getUpwardsFacing();
                        isFlipped = controller.isFlipped();
                        userDimensions.clear();
                        userSliceRepeats.clear();
                        userGlobalBlockPreferences.clear();
                        userBasePredicateBlockPreferences.clear();
                        userBasePredicateMinMaxPreferences.clear();

                        player.displayClientMessage(Component.literal("Loaded up controller information"), false);
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean shouldOpenUI() {
        return this.multiblockDefinition != null;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        if (!shouldOpenUI()) return IItemUIHolder.super.use(item, level, player, usedHand);

        if (level.isClientSide) {
            PlayerInventoryGuiData<?> guiData = PlayerInventoryGuiData.of(player, InventoryTypes.PLAYER, null,
                    usedHand == InteractionHand.OFF_HAND ? Inventory.SLOT_OFFHAND : player.getInventory().selected);
            ModularPanel<?> clientPanel = clientPanel();
            ClientGUI.open(createScreen(guiData, clientPanel));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(usedHand), level.isClientSide);
    }

    private ModularPanel<?> clientPanel() {
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
            if (selected.isEmpty() || multiblockDefinition == null) return null;
            IBlockPattern pattern = multiblockDefinition.getStructurePatterns().get("main").get();
            if (pattern instanceof BlockPattern blockPattern) {
                PatternPredicate predicate = blockPatternStructureUtil.getPredicateFromPos(
                        blockPattern, lastBlock.left(), frontFacing, upFacing, isFlipped);

                return createSelectedBlockMenu(predicate, lastBlock);
            } else if (pattern instanceof ExpandablePattern expandablePattern) {
                PatternPredicate predicate = expandablePatternStructureUtil.getPredicateFromPos(
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
                    BlockHitResult rayTrace = this.renderer.lastRayTrace();
                    if (rayTrace != null && rayTrace.getType() == HitResult.Type.BLOCK) {
                        BlockState state = mapSchema.getLevel()
                                .getBlockState(rayTrace.getBlockPos());
                        text.addFromItem(new ItemStack(state.getBlock()));
                    }
                }).tooltipAutoUpdate(true)
                .size(200);
        this.multiSchema.getSchemaRenderer().updateRenderFilter((pos, state) -> {
            if (yLevel == -1) {
                return true;
            }
            return pos.getY() >= yLevel;
        });

        return ModularPanel.defaultPanel("client_test")
                .coverChildren()
                .padding(7)
                .child(new ButtonWidget<>()
                        .tooltip(r -> r.addLine(Component.literal("Press to display preview in world!")))
                        .rightRel(1.0f)
                        .onMousePressed((c, b) -> {
                            if (!structureBlocks.isEmpty()) {
                                BlockPos origin = controllerPos.mutable()
                                        .move(mapSchema.getControllerPos().multiply(-1));
                                PatternPreviewRenderer.INSTANCE.setPreview(origin, structureBlocks, 20000);
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

    @Override
    public ModularPanel<?> buildUI(PlayerInventoryGuiData<?> data, PanelSyncManager syncManager, UISettings settings) {
        return null;
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
                        .children(predicate.predicateList, basePredicate -> {
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
            if (predicate.predicateList.size() == 1 && predicate.predicateList.get(0).candidates.size() == 1) {
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

    /// ==== Schema setup ====
    private void refreshSchema() {
        Map<BlockPos, BlockInfo> resultStructure;

        resultStructure = new HashMap<>();
        IBlockPattern pattern = multiblockDefinition.getStructurePatterns().get(DEFAULT_STRUCTURE).get();
        // maxSlices = pattern.getDimensions()[1];
        if (pattern instanceof BlockPattern blockPattern) {
            if (userSliceRepeats.isEmpty()) {
                for (int i = 0; i < blockPattern.getSlices().length; i++) {
                    userSliceRepeats.put(i, blockPattern.getSlices()[i].getMinRepeats());
                }
            }
            // reinterpret slider values as slice repeats?
            blockPatternStructureUtil.populatePreferenceTables(userBasePredicateBlockPreferences,
                    userBasePredicateMinMaxPreferences, userSliceRepeats);
            char[][][] flattenedCharPattern = blockPatternStructureUtil.flattenBlockPattern(blockPattern);
            char[][][] adjustedCharPattern = blockPatternStructureUtil.rotateAndFlipCharPattern(flattenedCharPattern,
                    blockPattern.getDirections(),
                    frontFacing, upFacing, isFlipped);

            blockPatternStructureUtil.populateWithUserBlockPreferences(resultStructure, blockPattern,
                    adjustedCharPattern,
                    userGlobalBlockPreferences, frontFacing, upFacing, isFlipped);

            blockPatternStructureUtil.populateFromPattern(resultStructure, blockPattern, adjustedCharPattern,
                    frontFacing, upFacing, isFlipped);

            BlockPatternStructureUtil.fixRotationsAndFacing(resultStructure, frontFacing, upFacing,
                    multiblockDefinition.getBlock());
        } else if (pattern instanceof ExpandablePattern expandablePattern) {
            if (userDimensions.isEmpty()) {
                userDimensions
                        .addAll(expandablePattern.getBoundsConstraints().apply().stream().map(Pair::left).toList());
            }
            // reinterpret slider values as bounds?
            expandablePatternStructureUtil.populatePreferenceTables(userBasePredicateBlockPreferences,
                    userBasePredicateMinMaxPreferences, userDimensions);

            expandablePatternStructureUtil.populateWithUserBlockPreferences(resultStructure, expandablePattern,
                    userGlobalBlockPreferences, frontFacing, upFacing, isFlipped);

            expandablePatternStructureUtil.populateFromPattern(resultStructure, expandablePattern, frontFacing,
                    upFacing, isFlipped);

            ExpandablePatternStructureUtil.fixRotationsAndFacing(resultStructure, frontFacing, upFacing,
                    multiblockDefinition.getBlock());
        }

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
        structureBlocks.clear();
        structureBlocks.putAll(resultStructure);
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
        if (pattern.getBoundsConstraints() != null) {
            List<Pair<Integer, Integer>> constraints = pattern.getBoundsConstraints().apply();
            for (int i = 0; i < constraints.size(); i++) {
                Pair<Integer, Integer> value = constraints.get(i);
                if (!Objects.equals(value.left(), value.right())) {
                    int finalI = i;
                    parent.child(new SliderWidget()
                            .background(GTGuiTextures.FLUID_SLOT)
                            .bounds(value.left(), value.right())
                            .height(16)
                            .width(value.right() * 12)
                            .stopper(1.0f)
                            .value(new DoubleValue.Dynamic(() -> {
                                return userDimensions.get(finalI);
                            }, (v) -> {
                                int oldVal = userDimensions.get(finalI);
                                int newVal = (int) v;
                                if (oldVal == newVal) return;
                                userDimensions.set(finalI, (int) v);
                                refreshSchema();
                                refreshViewWidget();
                            })));
                }
            }
        }
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
}
