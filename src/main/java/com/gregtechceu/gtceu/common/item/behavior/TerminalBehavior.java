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
import com.gregtechceu.gtceu.api.multiblock.util.BlockPatternStructureHelper;
import com.gregtechceu.gtceu.api.multiblock.util.ExpandablePatternStructureHelper;
import com.gregtechceu.gtceu.client.mui.schema.MutableSchema;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.MultiblockPreviewWidget;

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
import net.minecraft.world.level.block.state.BlockState;

import brachy.modularui.factory.ClientGUI;
import brachy.modularui.factory.PlayerInventoryGuiData;
import brachy.modularui.factory.inventory.InventoryTypes;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.*;
import com.google.common.collect.Table;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine.DEFAULT_STRUCTURE;

public class TerminalBehavior implements IInteractionItem, IItemUIHolder {

    // FIXME these are global for all terminal items rn
    private MultiblockMachineDefinition multiblockDefinition = null;
    private MutableSchema mapSchema;
    private BlockPos controllerPos;
    private Direction frontFacing;
    private Direction upFacing;
    private boolean isFlipped = false;

    private Long2ObjectMap<BlockInfo> userGlobalBlockPreferences;
    private @Nullable Table<PatternPredicate, BasePredicate, BlockInfo> userBasePredicateBlockPreferences;
    private @Nullable Table<PatternPredicate, BasePredicate, IntIntPair> userBasePredicateMinMaxPreferences;

    private Int2IntMap userSliceRepeats;

    private IntList userDimensions = IntLists.emptyList();

    private Map<BlockPos, BlockInfo> structureBlocks = null;

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (player == null || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        if (!(MetaMachine.getMachine(level, pos) instanceof MultiblockControllerMachine controller)) {
            return InteractionResult.PASS;
        }
        if (controller.getDefaultPatternState().isFormed()) {
            return InteractionResult.PASS;
        }
        this.refreshSchema();
        if (this.structureBlocks == null || this.structureBlocks.isEmpty()) {
            return InteractionResult.PASS;
        }

        BlockPos controllerOffset = controller.getBlockPos().offset(mapSchema.getControllerPos().multiply(-1));
        for (var entry : structureBlocks.entrySet()) {
            level.setBlockAndUpdate(entry.getKey().offset(controllerOffset), entry.getValue().getBlockState());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos blockPos = context.getClickedPos();

        if (!(MetaMachine.getMachine(level, blockPos) instanceof MultiblockControllerMachine controller)) {
            return InteractionResult.PASS;
        }
        // always load this data (even if shifting); it's required for #useOn to work
        this.multiblockDefinition = controller.getDefinition();
        this.controllerPos = controller.getBlockPos();
        this.frontFacing = controller.getFrontFacing();
        this.upFacing = controller.getUpwardsFacing();
        this.isFlipped = controller.isFlipped();

        if (player == null || player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            player.displayClientMessage(Component.literal("Loaded controller information"), false);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
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
        MultiblockPreviewWidget previewWidget = new MultiblockPreviewWidget(this.multiblockDefinition)
                .setControllerPos(this.controllerPos)
                .setFrontFacing(this.frontFacing).setUpFacing(this.upFacing).setFlipped(this.isFlipped);
        previewWidget.setOnSchemaRefresh(() -> {
            // straight up copy all the info here when UI selections are changed

            this.mapSchema = previewWidget.getMapSchema();
            this.structureBlocks = previewWidget.getStructureBlocks();
            this.userGlobalBlockPreferences = previewWidget.getUserGlobalBlockPreferences();
            this.userBasePredicateBlockPreferences = previewWidget.getUserBasePredicateBlockPreferences();
            this.userBasePredicateMinMaxPreferences = previewWidget.getUserBasePredicateMinMaxPreferences();
            this.userSliceRepeats = previewWidget.getUserSliceRepeats();
            this.userDimensions = previewWidget.getUserDimensions();
        });
        previewWidget.refreshSchema();

        return ModularPanel.defaultPanel("terminal")
                .coverChildren()
                .child(previewWidget);
    }

    @Override
    public ModularPanel<?> buildUI(PlayerInventoryGuiData<?> data, PanelSyncManager syncManager, UISettings settings) {
        return null;
    }

    private void refreshSchema() {
        Map<BlockPos, BlockInfo> resultStructure = new HashMap<>();
        IBlockPattern pattern = multiblockDefinition.getStructurePatterns().get(DEFAULT_STRUCTURE).get();

        if (pattern instanceof BlockPattern blockPattern) {
            if (userSliceRepeats == null) {
                userSliceRepeats = new Int2IntArrayMap();
            }
            if (userSliceRepeats.isEmpty()) {
                for (int i = 0; i < blockPattern.getSlices().length; i++) {
                    userSliceRepeats.put(i, blockPattern.getSlices()[i].getMinRepeats());
                }
            }
            // reinterpret slider values as slice repeats?
            var structureHelper = new BlockPatternStructureHelper(userBasePredicateBlockPreferences,
                    userBasePredicateMinMaxPreferences, userSliceRepeats);
            char[][][] flattenedCharPattern = structureHelper.flattenBlockPattern(blockPattern);
            char[][][] adjustedCharPattern = BlockPatternStructureHelper.rotateAndFlipPattern(flattenedCharPattern,
                    blockPattern.getDirections(),
                    frontFacing, upFacing, isFlipped);

            structureHelper.populateWithUserBlockPreferences(resultStructure, blockPattern, adjustedCharPattern,
                    userGlobalBlockPreferences, frontFacing, upFacing, isFlipped);

            structureHelper.populateFromPattern(resultStructure, blockPattern, adjustedCharPattern,
                    frontFacing, upFacing, isFlipped);

            BlockPatternStructureHelper.fixRotationsAndFacing(resultStructure, frontFacing, upFacing,
                    multiblockDefinition.getBlock());
        } else if (pattern instanceof ExpandablePattern expandablePattern) {
            if (userDimensions == null || userDimensions.isEmpty()) {
                userDimensions = expandablePattern.getBoundsConstraints().apply().stream()
                        .mapToInt(Pair::left)
                        .collect(IntArrayList::new, IntList::add, IntList::addAll);
            }
            // reinterpret slider values as bounds?
            var expandableStructureHelper = new ExpandablePatternStructureHelper(userBasePredicateBlockPreferences,
                    userBasePredicateMinMaxPreferences, userDimensions);

            expandableStructureHelper.populateWithUserBlockPreferences(resultStructure, expandablePattern,
                    userGlobalBlockPreferences, frontFacing, upFacing, isFlipped);

            expandableStructureHelper.populateFromPattern(resultStructure, expandablePattern, frontFacing,
                    upFacing, isFlipped);

            BlockPatternStructureHelper.fixRotationsAndFacing(resultStructure, frontFacing, upFacing,
                    multiblockDefinition.getBlock());
        }

        Long2ReferenceMap<BlockState> schemaMap = new Long2ReferenceOpenHashMap<>();
        for (var entry : resultStructure.entrySet()) {
            BlockState state = entry.getValue().getBlockState();
            schemaMap.put(entry.getKey().asLong(), state);
        }
        if (this.mapSchema == null) {
            this.mapSchema = new MutableSchema(schemaMap);
        } else {
            this.mapSchema.setBlocks(schemaMap);
        }
        structureBlocks.clear();
        structureBlocks.putAll(resultStructure);
    }
}
