package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.mui.IItemUIHolder;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
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

import brachy.modularui.factory.ClientGUI;
import brachy.modularui.factory.PlayerInventoryGuiData;
import brachy.modularui.factory.inventory.InventoryTypes;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.*;

import java.util.*;

public class TerminalBehavior implements IInteractionItem, IItemUIHolder {

    // FIXME these are global for all terminal items rn
    private MultiblockMachineDefinition multiblockDefinition = null;
    private MutableSchema mapSchema;
    private BlockPos controllerPos;
    private Direction frontFacing;
    private Direction upFacing;
    private boolean isFlipped = false;

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

        if (player == null || player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!(MetaMachine.getMachine(level, blockPos) instanceof MultiblockControllerMachine controller)) {
            return InteractionResult.PASS;
        }
        this.multiblockDefinition = controller.getDefinition();
        this.controllerPos = controller.getBlockPos();
        this.frontFacing = controller.getFrontFacing();
        this.upFacing = controller.getUpwardsFacing();
        this.isFlipped = controller.isFlipped();

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
        MultiblockPreviewWidget previewWidget = new MultiblockPreviewWidget(this.multiblockDefinition);
        previewWidget.setControllerPos(this.controllerPos)
                .setFrontFacing(this.frontFacing).setUpFacing(this.upFacing).setFlipped(this.isFlipped);
        previewWidget.setOnSchemaRefresh(() -> {
            this.mapSchema = previewWidget.getMapSchema();
            this.structureBlocks = previewWidget.getStructureBlocks();
        });

        return ModularPanel.defaultPanel("multiblock_preview").child(previewWidget);
    }

    @Override
    public ModularPanel<?> buildUI(PlayerInventoryGuiData<?> data, PanelSyncManager syncManager, UISettings settings) {
        return null;
    }
}
