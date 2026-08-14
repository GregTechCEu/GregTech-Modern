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
import net.minecraft.nbt.CompoundTag;
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
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TerminalBehavior implements IInteractionItem, IItemUIHolder {

    // Strip these fields and only store the needed info as nbt on the item for now
    private MultiblockMachineDefinition multiblockDefinition = null;
    private BlockPos controllerPos;
    private Direction frontFacing;
    private Direction upFacing;
    private boolean isFlipped = false;
    // this one may be fine to keep? as the info gets rebuild based on nbt....
    private MultiblockSchemaInfo multiblockSchemaInfo;

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
        //if (controller.getDefinition() != this.multiblockDefinition && this.multiblockSchemaInfo != null) {
            //this.multiblockSchemaInfo = null;
        //}
        //this.multiblockDefinition = controller.getDefinition();
        //this.controllerPos = controller.getBlockPos();
        //this.frontFacing = controller.getFrontFacing();
        //this.upFacing = controller.getUpwardsFacing();
        //this.isFlipped = controller.isFlipped();

        if (player == null || player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            writeMultiblockInfo(itemStack, controller, null);
            player.displayClientMessage(Component.literal("Loaded controller information"), false);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public boolean shouldOpenUI() {
        // return
        return this.multiblockDefinition != null;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        if (!shouldOpenUI()) return IItemUIHolder.super.use(item, level, player, usedHand);

        if (level.isClientSide) {
            PlayerInventoryGuiData<?> guiData = PlayerInventoryGuiData.of(player, InventoryTypes.PLAYER, null,
                    usedHand == InteractionHand.OFF_HAND ? Inventory.SLOT_OFFHAND : player.getInventory().selected);
            ModularPanel<?> clientPanel = clientPanel(player.getItemInHand(usedHand));
            ClientGUI.open(createScreen(guiData, clientPanel));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(usedHand), level.isClientSide);
    }

    private ModularPanel<?> clientPanel(ItemStack item) {
        // item.getTag().controller().definition()
        MultiblockPreviewWidget previewWidget = new MultiblockPreviewWidget(this.multiblockDefinition,
                this.multiblockSchemaInfo, 200, 200)
                // item.getTag().controllerPos
                .setControllerPos(this.controllerPos)
                // item.getTag().frontFacing...
                .setFrontFacing(this.frontFacing).setUpFacing(this.upFacing).setFlipped(this.isFlipped);
        previewWidget.refreshSchema();

        return ModularPanel.defaultPanel("terminal")
                .coverChildren()
                /*.onCloseAction(() -> {
                    this.multiblockSchemaInfo = previewWidget.getMultiblockSchemaInfo();
                })*/
                .child(previewWidget)
                .onCloseAction(() -> writeMultiblockInfo(item, null, previewWidget));
    }

    private void writeMultiblockInfo(ItemStack item, @Nullable MultiblockControllerMachine controller, @Nullable MultiblockPreviewWidget previewWidget) {
        // TODO uuid gathering

        CompoundTag tag = item.getOrCreateTag();
        if (controller != null) {
            tag.putString("controller", controller.getDefinition().getName());
            tag.putLong("pos", controller.getBlockPos().asLong());
            tag.putByte("facing", (byte)controller.getFrontFacing().ordinal());
            tag.putByte("upFacing", (byte)controller.getUpwardsFacing().ordinal());
            tag.putBoolean("flipped", controller.isFlipped());
        }

        if (previewWidget != null) {
            var sliceRepeats = previewWidget.getMultiblockSchemaInfo().getUserSliceRepeats();
            if (!sliceRepeats.isEmpty()) {
                tag.putInt("sliceRepeatSize", sliceRepeats.size());
                int[] repeatIndices = new int[sliceRepeats.size()];
                for (int i = 0; i < sliceRepeats.size(); i++) {
                    repeatIndices[i] = sliceRepeats.get(i);
                }
                tag.putIntArray("sliceRepeatIndices", repeatIndices);
            }
            var blockPreferences = previewWidget.getMultiblockSchemaInfo().getUserGlobalBlockPreferences();
            if (!blockPreferences.isEmpty()) {
                tag.putInt("preferenceSize", blockPreferences.size());
                long[] poses = new long[blockPreferences.size()];
                int[] prefs = new int[blockPreferences.size()];
                int i = 0;
                for (var entry : blockPreferences.long2ObjectEntrySet()) {
                    poses[i] = entry.getLongKey();
                    // todo convert from block info index to ordinal
                    prefs[i] = 7;
                    i++;
                }
                tag.putLongArray("preferencePoses", poses);
                tag.putIntArray("preferenceIndices", prefs);
            }
        }
    }

    private CompoundTag getMultiblockInfo(ItemStack item) {
        return item.getOrCreateTag();
    }

    @Override
    public ModularPanel<?> buildUI(PlayerInventoryGuiData<?> data, PanelSyncManager syncManager, UISettings settings) {
        return null;
    }

    private void refreshSchema() {
        this.multiblockSchemaInfo.refreshSchema(multiblockDefinition, frontFacing, upFacing, isFlipped, null);
    }
}
