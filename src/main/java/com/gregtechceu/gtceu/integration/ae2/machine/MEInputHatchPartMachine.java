package com.gregtechceu.gtceu.integration.ae2.machine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickIntractable;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.AEFluidConfigWidget;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidList;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidSlot;

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
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEInputHatchPartMachine extends MEHatchPartMachine implements IDataStickIntractable, IMachineLife {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MEInputHatchPartMachine.class, MEHatchPartMachine.MANAGED_FIELD_HOLDER);

    protected ExportOnlyAEFluidList aeFluidHandler;

    public MEInputHatchPartMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, IO.IN, args);
    }

    @Override
    @NotNull
    protected NotifiableFluidTank createTank(long initialCapacity, int slots, Object... args) {
        this.aeFluidHandler = new ExportOnlyAEFluidList(this, slots);
        return aeFluidHandler;
    }

    @Override
    @NotNull
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(new Position(0, 0));
        // ME Network status
        group.addWidget(new LabelWidget(3, 0, () -> this.isOnline ?
                "gtceu.gui.me_network.online" :
                "gtceu.gui.me_network.offline"));

        // Config slots
        group.addWidget(new AEFluidConfigWidget(3, 10, this.aeFluidHandler));

        return group;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        tankSubs = this.aeFluidHandler.addChangedListener(this::updateTankSubscription);
    }

    @Override
    protected void autoIO() {
        if (!this.isWorkingEnabled()) return;
        if (!this.shouldSyncME()) return;

        if (this.updateMEStatus()) {
            MEStorage aeNetwork = this.getMainNode().getGrid().getStorageService().getInventory();
            for (ExportOnlyAEFluidSlot aeTank : this.aeFluidHandler.getInventory()) {
                // Try to clear the wrong fluid
                GenericStack exceedFluid = aeTank.exceedStack();
                if (exceedFluid != null) {
                    long total = exceedFluid.amount();
                    long inserted = aeNetwork.insert(exceedFluid.what(), exceedFluid.amount(), Actionable.MODULATE,
                            this.actionSource);
                    if (inserted > 0) {
                        aeTank.drain(inserted, false);
                        continue;
                    } else {
                        aeTank.drain(total, false);
                    }
                }
                // Fill it
                GenericStack reqFluid = aeTank.requestStack();
                if (reqFluid != null) {
                    long extracted = aeNetwork.extract(reqFluid.what(), reqFluid.amount(), Actionable.MODULATE,
                            this.actionSource);
                    if (extracted > 0) {
                        aeTank.addStack(new GenericStack(reqFluid.what(), extracted));
                    }
                }
            }
            this.updateTankSubscription();
        }
    }

    @Override
    public void onMachineRemoved() {
        flushInventory();
    }

    protected void flushInventory() {
        if (this.updateMEStatus()) {
            MEStorage storage = this.getMainNode().getGrid().getStorageService().getInventory();

            for (var aeSlot : aeFluidHandler.getInventory()) {
                GenericStack stock = aeSlot.getStock();
                if (stock != null) {
                    storage.insert(stock.what(), stock.amount(), Actionable.MODULATE, actionSource);
                }
            }
        }
    }

    @Override
    public final boolean onDataStickLeftClick(Player player, ItemStack dataStick) {
        if (!isRemote()) {
            CompoundTag tag = new CompoundTag();
            tag.put("MEInputHatch", writeConfigToTag());
            dataStick.setTag(tag);
            dataStick.setHoverName(Component.translatable("gtceu.machine.me.fluid_import.data_stick.name"));
            player.sendSystemMessage(Component.translatable("gtceu.machine.me.import_copy_settings"));
        }
        return true;
    }

    protected CompoundTag writeConfigToTag() {
        CompoundTag tag = new CompoundTag();
        CompoundTag configStacks = new CompoundTag();
        tag.put("ConfigStacks", configStacks);
        for (int i = 0; i < CONFIG_SIZE; i++) {
            var slot = this.aeFluidHandler.getInventory()[i];
            GenericStack config = slot.getConfig();
            if (config == null) {
                continue;
            }
            CompoundTag stackNbt = GenericStack.writeTag(config);
            configStacks.put(Integer.toString(i), stackNbt);
        }
        tag.putByte("GhostCircuit",
                (byte) IntCircuitBehaviour.getCircuitConfiguration(circuitInventory.getStackInSlot(0)));
        return tag;
    }

    @Override
    public final InteractionResult onDataStickRightClick(Player player, ItemStack dataStick) {
        CompoundTag tag = dataStick.getTag();
        if (tag == null || !tag.contains("MEInputHatch")) {
            return InteractionResult.PASS;
        }

        if (!isRemote()) {
            readConfigFromTag(tag.getCompound("MEInputHatch"));
            this.updateTankSubscription();
            player.sendSystemMessage(Component.translatable("gtceu.machine.me.import_paste_settings"));
        }
        return InteractionResult.sidedSuccess(isRemote());
    }

    protected void readConfigFromTag(CompoundTag tag) {
        if (tag.contains("ConfigStacks")) {
            CompoundTag configStacks = tag.getCompound("ConfigStacks");
            for (int i = 0; i < CONFIG_SIZE; i++) {
                String key = Integer.toString(i);
                if (configStacks.contains(key)) {
                    CompoundTag configTag = configStacks.getCompound(key);
                    this.aeFluidHandler.getInventory()[i].setConfig(GenericStack.readTag(configTag));
                } else {
                    this.aeFluidHandler.getInventory()[i].setConfig(null);
                }
            }
        }
        if (tag.contains("GhostCircuit")) {
            circuitInventory.setStackInSlot(0, IntCircuitBehaviour.stack(tag.getByte("GhostCircuit")));
        }
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
