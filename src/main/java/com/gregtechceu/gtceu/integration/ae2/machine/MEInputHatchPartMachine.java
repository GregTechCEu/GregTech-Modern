package com.gregtechceu.gtceu.integration.ae2.machine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IHasCircuitSlot;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.AEFluidConfigWidget;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidList;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidSlot;
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
import net.minecraftforge.fluids.capability.IFluidHandler;

import appeng.api.config.Actionable;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEInputHatchPartMachine extends MEHatchPartMachine
                                     implements IDataStickInteractable, IMachineLife, IHasCircuitSlot {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MEInputHatchPartMachine.class, MEHatchPartMachine.MANAGED_FIELD_HOLDER);

    protected ExportOnlyAEFluidList aeFluidHandler;

    public MEInputHatchPartMachine(IMachineBlockEntity holder, Object... args) {
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
    protected NotifiableFluidTank createTank(int initialCapacity, int slots, Object... args) {
        this.aeFluidHandler = new ExportOnlyAEFluidList(this, slots);
        return aeFluidHandler;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    /////////////////////////////////
    // ********** Sync ME *********//
    /////////////////////////////////

    @Override
    protected void autoIO() {
        if (!this.isWorkingEnabled()) return;
        if (!this.shouldSyncME()) return;

        if (this.updateMEStatus()) {
            this.syncME();
            this.updateTankSubscription();
        }
    }

    protected void syncME() {
        MEStorage networkInv = this.getMainNode().getGrid().getStorageService().getInventory();
        for (ExportOnlyAEFluidSlot aeTank : this.aeFluidHandler.getInventory()) {
            // Try to clear the wrong fluid
            GenericStack exceedFluid = aeTank.exceedStack();
            if (exceedFluid != null) {
                int total = GTMath.saturatedCast(exceedFluid.amount());
                int inserted = GTMath
                        .saturatedCast(networkInv.insert(exceedFluid.what(), exceedFluid.amount(), Actionable.MODULATE,
                                this.actionSource));
                if (inserted > 0) {
                    aeTank.drain(inserted, IFluidHandler.FluidAction.EXECUTE);
                    continue;
                } else {
                    aeTank.drain(total, IFluidHandler.FluidAction.EXECUTE);
                }
            }
            // Fill it
            GenericStack reqFluid = aeTank.requestStack();
            if (reqFluid != null) {
                long extracted = networkInv.extract(reqFluid.what(), reqFluid.amount(), Actionable.MODULATE,
                        this.actionSource);
                if (extracted > 0) {
                    aeTank.addStack(new GenericStack(reqFluid.what(), extracted));
                }
            }
        }
    }

    protected void flushInventory() {
        var grid = getMainNode().getGrid();
        if (grid != null) {
            for (var aeSlot : aeFluidHandler.getInventory()) {
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
        group.addWidget(new AEFluidConfigWidget(3, 10, this.aeFluidHandler));

        return group;
    }

    ////////////////////////////////
    // ******* Interaction *******//
    ////////////////////////////////

    @Override
    public final InteractionResult onDataStickShiftUse(Player player, ItemStack dataStick) {
        if (!isRemote()) {
            CompoundTag tag = new CompoundTag();
            tag.put("MEInputHatch", writeConfigToTag());
            dataStick.setTag(tag);
            dataStick.setHoverName(Component.translatable("gtceu.machine.me.fluid_import.data_stick.name"));
            player.sendSystemMessage(Component.translatable("gtceu.machine.me.import_copy_settings"));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public final InteractionResult onDataStickUse(Player player, ItemStack dataStick) {
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

    ////////////////////////////////
    // ****** Configuration ******//
    ////////////////////////////////

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
            CompoundTag stackTag = GenericStack.writeTag(config);
            configStacks.put(Integer.toString(i), stackTag);
        }
        tag.putByte("GhostCircuit",
                (byte) IntCircuitBehaviour.getCircuitConfiguration(circuitInventory.getStackInSlot(0)));
        return tag;
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
}
