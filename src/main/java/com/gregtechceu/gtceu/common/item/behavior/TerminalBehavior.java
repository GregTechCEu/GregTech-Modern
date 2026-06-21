package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.mui.IItemUIHolder;
import com.gregtechceu.gtceu.api.mui.MultiblockSchemaInfo;
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

public class TerminalBehavior implements IInteractionItem, IItemUIHolder {

    // FIXME these are global for all terminal items rn
    private MultiblockMachineDefinition multiblockDefinition = null;
    private MultiblockSchemaInfo multiblockSchemaInfo;
    private BlockPos controllerPos;
    private Direction frontFacing;
    private Direction upFacing;
    private boolean isFlipped = false;

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (player == null || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        if (!player.isCreative()) {
            return InteractionResult.PASS;
        }

        if (!(MetaMachine.getMachine(level, pos) instanceof MultiblockControllerMachine controller)) {
            return InteractionResult.PASS;
        }
        if (controller.getDefaultPatternState().isFormed()) {
            return InteractionResult.PASS;
        }
        if (controller.getDefinition() == this.multiblockDefinition && this.multiblockSchemaInfo != null) {
            this.refreshSchema();
        }
        if (this.multiblockSchemaInfo == null) {
            return InteractionResult.PASS;
        }
        if (this.multiblockSchemaInfo.getStructureBlocks() == null ||
                this.multiblockSchemaInfo.getStructureBlocks().isEmpty()) {
            return InteractionResult.PASS;
        }

        BlockPos controllerOffset = controller.getBlockPos()
                .offset(this.multiblockSchemaInfo.getMapSchema().getControllerPos().multiply(-1));
        if (context.getPlayer().isCreative()) {
            for (var entry : this.multiblockSchemaInfo.getStructureBlocks().entrySet()) {
                level.setBlockAndUpdate(entry.getKey().offset(controllerOffset), entry.getValue().getBlockState());
            }

            if (!level.isClientSide()) {
                // needed to force the multiblock to do a clean check, kinda sus
                controller.getDefaultPatternState().getCache().clear();
                controller.checkAndFormStructure();
            }
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
        if (controller.getDefinition() != this.multiblockDefinition && this.multiblockSchemaInfo != null) {
            this.multiblockSchemaInfo = null;
        }
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
        MultiblockPreviewWidget previewWidget = new MultiblockPreviewWidget(this.multiblockDefinition,
                this.multiblockSchemaInfo)
                .setControllerPos(this.controllerPos)
                .setFrontFacing(this.frontFacing).setUpFacing(this.upFacing).setFlipped(this.isFlipped);
        previewWidget.refreshSchema();

        return ModularPanel.defaultPanel("terminal")
                .coverChildren()
                .onCloseAction(() -> {
                    this.multiblockSchemaInfo = previewWidget.getMultiblockSchemaInfo();
                })
                .child(previewWidget);
    }

    @Override
    public ModularPanel<?> buildUI(PlayerInventoryGuiData<?> data, PanelSyncManager syncManager, UISettings settings) {
        return null;
    }

    private void refreshSchema() {
        this.multiblockSchemaInfo.refreshSchema(multiblockDefinition, frontFacing, upFacing, isFlipped, null);
    }
}
