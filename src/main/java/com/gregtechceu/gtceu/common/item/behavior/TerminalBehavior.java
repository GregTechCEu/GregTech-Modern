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
import net.minecraft.world.level.block.Block;

import brachy.modularui.factory.ClientGUI;
import brachy.modularui.factory.PlayerInventoryGuiData;
import brachy.modularui.factory.inventory.InventoryTypes;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.*;

import java.util.*;

public class TerminalBehavior implements IInteractionItem, IItemUIHolder {

    private MultiblockMachineDefinition multiblockDefinition = null;

    // schema stuff
    private MutableSchema mapSchema;

    private boolean isFlipped = false;
    private Direction frontFacing;
    private Direction upFacing;
    private BlockPos controllerPos;

    private Map<BlockPos, BlockInfo> structureBlocks = new HashMap<>();

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
        if (!level.isClientSide) {
            for (var entry : structureBlocks.entrySet()) {
                BlockPos.MutableBlockPos mPos = entry.getKey().mutable();
                mPos.move(controller.getBlockPos()).move(mapSchema.getControllerPos().multiply(-1));
                level.setBlock(mPos, entry.getValue().getBlockState(), Block.UPDATE_ALL_IMMEDIATE);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        if (context.getPlayer() == null) return InteractionResult.PASS;
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos blockPos = context.getClickedPos();

        if (!(MetaMachine.getMachine(level, blockPos) instanceof MultiblockControllerMachine controller)) {
            return InteractionResult.PASS;
        }
        if (player == null || player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!controller.getDefaultPatternState().isFormed() || true) {
            if (!level.isClientSide) {
                multiblockDefinition = controller.getDefinition();
                controllerPos = controller.getBlockPos();
                frontFacing = controller.getFrontFacing();
                upFacing = controller.getUpwardsFacing();
                isFlipped = controller.isFlipped();

                player.displayClientMessage(Component.literal("Loaded up controller information"), false);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
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
        MultiblockPreviewWidget previewWidget = new MultiblockPreviewWidget(this.multiblockDefinition);
        previewWidget.setControllerPos(this.controllerPos)
                .setFrontFacing(this.frontFacing).setUpFacing(this.upFacing).setFlipped(this.isFlipped);

        return ModularPanel.defaultPanel("multiblock_preview").child(previewWidget);
    }

    @Override
    public ModularPanel<?> buildUI(PlayerInventoryGuiData<?> data, PanelSyncManager syncManager, UISettings settings) {
        return null;
    }
}
