package com.gregtechceu.gtceu.integration.ae2.machine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IHasCircuitSlot;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.AEItemConfigWidget;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemList;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemSlot;
import com.gregtechceu.gtceu.utils.GTMath;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEInputBusPartMachine extends MEBusPartMachine
                                   implements IDataStickInteractable, IMachineLife, IHasCircuitSlot {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MEInputBusPartMachine.class,
            MEBusPartMachine.MANAGED_FIELD_HOLDER);
    protected final static int CONFIG_SIZE = 16;

    protected ExportOnlyAEItemList aeItemHandler;

    public MEInputBusPartMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, IO.IN, args);
    }

    /////////////////////////////////
    // ***** Machine LifeCycle ****//
    /////////////////////////////////

    @Override
    public void onMachineRemoved() {
        flushInventory();
    }

    @Override
    protected NotifiableItemStackHandler createInventory(Object... args) {
        this.aeItemHandler = new ExportOnlyAEItemList(this, CONFIG_SIZE);
        return this.aeItemHandler;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
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
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(new Position(0, 0));
        // ME Network status
        group.addWidget(new LabelWidget(3, 0, () -> this.isOnline ?
                "gtceu.gui.me_network.online" :
                "gtceu.gui.me_network.offline"));

        // Config slots
        group.addWidget(new AEItemConfigWidget(3, 10, this.aeItemHandler));

        return group;
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
