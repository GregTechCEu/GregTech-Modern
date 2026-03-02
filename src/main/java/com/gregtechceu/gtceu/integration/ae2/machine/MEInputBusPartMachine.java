package com.gregtechceu.gtceu.integration.ae2.machine;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IHasCircuitSlot;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.drawable.UITexture;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.theme.ThemeAPI;
import com.gregtechceu.gtceu.api.mui.value.BoolValue;
import com.gregtechceu.gtceu.api.mui.value.sync.BooleanSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandlers;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ToggleButton;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Column;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.item.behavior.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTGuis;
import com.gregtechceu.gtceu.integration.ae2.mui.AEConfigWidget;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemList;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemSlot;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAESlot;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEInputBusPartMachine extends MEBusPartMachine
                                   implements IDataStickInteractable, IHasCircuitSlot {

    protected final static int CONFIG_SIZE = 16;

    protected ExportOnlyAEItemList aeItemHandler;

    public MEInputBusPartMachine(BlockEntityCreationInfo info) {
        super(info, IO.IN);
    }

    /////////////////////////////////
    // ***** Machine LifeCycle ****//
    /////////////////////////////////

    @Override
    public void onMachineDestroyed() {
        flushInventory();
    }

    @Override
    protected NotifiableItemStackHandler createInventory() {
        this.aeItemHandler = new ExportOnlyAEItemList(this, CONFIG_SIZE);
        return this.aeItemHandler;
    }

    /////////////////////////////////
    // ********** Sync ME *********//
    /////////////////////////////////

    @Override
    public void autoIO() {
        if (!this.isWorkingEnabled()) return;
        if (!this.shouldSyncME()) return;

        if (this.updateMEStatus()) {
            this.syncME();
            this.updateInventorySubscription();
        }
    }

    protected void syncME() {
        MEStorage networkInv = this.getMainNode().getGrid().getStorageService().getInventory();
        for (ExportOnlyAEItemSlot aeSlot : this.aeItemHandler.getInventory()) {
            // Try to clear the wrong item
            GenericStack exceedItem = aeSlot.exceedStack();
            if (exceedItem != null) {
                long total = exceedItem.amount();
                long inserted = networkInv.insert(exceedItem.what(), exceedItem.amount(), Actionable.MODULATE,
                        this.actionSource);
                if (inserted > 0) {
                    aeSlot.extractItem(0, GTMath.saturatedCast(inserted), false);
                    continue;
                } else {
                    aeSlot.extractItem(0, GTMath.saturatedCast(total), false);
                }
            }
            // Fill it
            GenericStack reqItem = aeSlot.requestStack();
            if (reqItem != null) {
                long extracted = networkInv.extract(reqItem.what(), reqItem.amount(), Actionable.MODULATE,
                        this.actionSource);
                if (extracted != 0) {
                    aeSlot.addStack(new GenericStack(reqItem.what(), extracted));
                }
            }
        }
    }

    protected void flushInventory() {
        var grid = getMainNode().getGrid();
        if (grid != null) {
            for (var aeSlot : aeItemHandler.getInventory()) {
                GenericStack stock = aeSlot.getStock();
                if (stock != null) {
                    grid.getStorageService().getInventory().insert(stock.what(), stock.amount(), Actionable.MODULATE,
                            actionSource);
                }
            }
        }
    }

    ///////////////////////////////
    // ********** GUI ***********//
    ///////////////////////////////

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        var panel = GTGuis.createPanel(this, 176, 192);
        panel.child(GTMuiWidgets.createTitleBar(getDefinition(), 176));

        BooleanSyncValue isOnlineValue = SyncHandlers.bool(this::isOnline, this::setOnline);
        syncManager.syncValue("is_online", isOnlineValue);

        panel.child(IKey.dynamic(() -> isOnlineValue.getBoolValue() ?
                Component.translatable("gtceu.gui.me_network.online") :
                Component.translatable("gtceu.gui.me_network.offline"))
                .asWidget()
                .top(14)
                .left(7));

        registerConfigActions(syncManager);

        panel.child(new AEConfigWidget(aeItemHandler, CONFIG_SIZE, false)
                .syncManager(syncManager)
                .size(8 * 18, 2 * (18 * 2 + 2))
                .top(26)
                .alignX(0.5f));

        var theme = this.getDefinition().getThemeId();
        var backgroundTexture = (UITexture) ThemeAPI.INSTANCE.getTheme(theme).getPanelTheme().getTheme()
                .getBackground();
        if (backgroundTexture == null) {
            backgroundTexture = GTGuiTextures.BACKGROUND;
        }

        panel.child(createButtonColumn(panel, syncManager, backgroundTexture));

        panel.child(SlotGroupWidget.playerInventory(true).bottom(7));

        return panel;
    }

    protected Flow createButtonColumn(ModularPanel panel, PanelSyncManager syncManager,
                                      UITexture backgroundTexture) {
        return new Column()
                .coverChildren()
                .rightRel(1.0f)
                .reverseLayout(true)
                .bottom(16)
                .padding(5, 2, 4, 4)
                .childPadding(2)
                .background(backgroundTexture.getSubArea(0.0f, 0f, 0.75f, 1.0f))
                .child(GTMuiWidgets.createCircuitSlotPanel(this, panel, syncManager))
                .child(new ToggleButton()
                        .value(new BoolValue.Dynamic(this::isDistinct, this::setDistinct))
                        .stateOverlay(GTGuiTextures.BUTTON_DISTINCT)
                        .tooltipAutoUpdate(true)
                        .tooltipBuilder(r -> r
                                .add(Component.translatable("gtceu.multiblock.universal.distinct")
                                        .setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW))
                                        .append(Component.translatable(
                                                "gtceu.multiblock.universal.distinct" +
                                                        (isDistinct() ? ".yes" : ".no"))))));
    }

    protected void registerConfigActions(PanelSyncManager syncManager) {
        syncManager.registerServerSyncedAction("ae_config_set", packet -> {
            int index = packet.readVarInt();
            if (index < 0 || index >= CONFIG_SIZE) return;
            var slot = aeItemHandler.getInventory()[index];
            // Use the carried item from the player opening the UI
            var player = syncManager.getPlayer();
            ItemStack held = player.containerMenu.getCarried();
            if (!held.isEmpty()) {
                slot.setConfig(GenericStack.fromItemStack(held));
            }
        });

        syncManager.registerServerSyncedAction("ae_config_clear", packet -> {
            int index = packet.readVarInt();
            if (index < 0 || index >= CONFIG_SIZE) return;
            aeItemHandler.getInventory()[index].setConfig(null);
        });

        syncManager.registerServerSyncedAction("ae_config_amount", packet -> {
            int index = packet.readVarInt();
            long amount = packet.readVarLong();
            if (index < 0 || index >= CONFIG_SIZE) return;
            var slot = aeItemHandler.getInventory()[index];
            if (slot.getConfig() != null && amount > 0) {
                slot.setConfig(new GenericStack(slot.getConfig().what(), amount));
            }
        });

        syncManager.registerServerSyncedAction("ae_stock_pickup", packet -> {
            int index = packet.readVarInt();
            if (index < 0 || index >= CONFIG_SIZE) return;
            var slot = aeItemHandler.getInventory()[index];
            if (slot.getStock() != null && slot.getStock().what() instanceof AEItemKey key) {
                var player = syncManager.getPlayer();
                if (!player.containerMenu.getCarried().isEmpty()) return;
                ItemStack stack = new ItemStack(key.getItem());
                stack.setCount(Math.min((int) slot.getStock().amount(), stack.getMaxStackSize()));
                if (key.hasTag()) stack.setTag(key.getTag().copy());
                player.containerMenu.setCarried(stack);
                GenericStack remaining = ExportOnlyAESlot.copy(slot.getStock(),
                        Math.max(0, slot.getStock().amount() - stack.getCount()));
                slot.setStock(remaining.amount() == 0 ? null : remaining);
            }
        });

        syncManager.registerServerSyncedAction("ae_config_set_ghost", packet -> {
            int index = packet.readVarInt();
            if (index < 0 || index >= CONFIG_SIZE) return;
            boolean isFluid = packet.readBoolean();
            if (!isFluid) {
                ItemStack item = packet.readItem();
                if (!item.isEmpty()) {
                    aeItemHandler.getInventory()[index].setConfig(GenericStack.fromItemStack(item));
                }
            }
        });
    }

    ////////////////////////////////
    // ******* Interaction *******//
    ////////////////////////////////

    @Override
    public final InteractionResult onDataStickShiftUse(Player player, ItemStack dataStick) {
        if (!isRemote()) {
            CompoundTag tag = new CompoundTag();
            tag.put("MEInputBus", writeConfigToTag());
            dataStick.setTag(tag);
            dataStick.setHoverName(Component.translatable("gtceu.machine.me.item_import.data_stick.name"));
            player.sendSystemMessage(Component.translatable("gtceu.machine.me.import_copy_settings"));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public final InteractionResult onDataStickUse(Player player, ItemStack dataStick) {
        CompoundTag tag = dataStick.getTag();
        if (tag == null || !tag.contains("MEInputBus")) {
            return InteractionResult.PASS;
        }

        if (!isRemote()) {
            readConfigFromTag(tag.getCompound("MEInputBus"));
            this.updateInventorySubscription();
            player.sendSystemMessage(Component.translatable("gtceu.machine.me.import_paste_settings"));
        }
        return InteractionResult.sidedSuccess(isRemote());
    }

    ////////////////////////////////
    // ****** Configuration ******//
    ////////////////////////////////

    protected CompoundTag writeConfigToTag() {
        CompoundTag tag = new CompoundTag();
        CompoundTag configStacks = new CompoundTag();
        tag.put("ConfigStacks", configStacks);
        for (int i = 0; i < CONFIG_SIZE; i++) {
            var slot = this.aeItemHandler.getInventory()[i];
            GenericStack config = slot.getConfig();
            if (config == null) {
                continue;
            }
            CompoundTag stackTag = GenericStack.writeTag(config);
            configStacks.put(Integer.toString(i), stackTag);
        }
        tag.putByte("GhostCircuit",
                (byte) IntCircuitBehaviour.getCircuitConfiguration(circuitInventory.getStackInSlot(0)));
        tag.putBoolean("DistinctBuses", isDistinct());
        return tag;
    }

    protected void readConfigFromTag(CompoundTag tag) {
        if (tag.contains("ConfigStacks")) {
            CompoundTag configStacks = tag.getCompound("ConfigStacks");
            for (int i = 0; i < CONFIG_SIZE; i++) {
                String key = Integer.toString(i);
                if (configStacks.contains(key)) {
                    CompoundTag configTag = configStacks.getCompound(key);
                    this.aeItemHandler.getInventory()[i].setConfig(GenericStack.readTag(configTag));
                } else {
                    this.aeItemHandler.getInventory()[i].setConfig(null);
                }
            }
        }
        if (tag.contains("GhostCircuit")) {
            circuitInventory.setStackInSlot(0, IntCircuitBehaviour.stack(tag.getByte("GhostCircuit")));
        }
        if (tag.contains("DistinctBuses")) {
            setDistinct(tag.getBoolean("DistinctBuses"));
        }
    }
}
