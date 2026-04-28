package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.TieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.AutoOutputTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;

import lombok.Getter;

import java.util.List;

public class BufferMachine extends TieredMachine implements IFancyUIMachine {

    public static final int TANK_SIZE = 64000;

    @SaveField
    @Getter
    protected final NotifiableItemStackHandler inventory;

    @SaveField
    @Getter
    protected final NotifiableFluidTank tank;
    @SaveField
    @SyncToClient

    public final AutoOutputTrait autoOutput;

    public BufferMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier);
        this.inventory = createInventory();
        this.tank = createTank();
        this.autoOutput = new AutoOutputTrait(this, List.of(inventory), List.of(tank));
    }

    ////////////////////////////////
    // ***** Initialization ******//
    ////////////////////////////////

    public static int getInventorySize(int tier) {
        return (int) Math.pow(tier + 2, 2);
    }

    public static int getTankSize(int tier) {
        return tier + 2;
    }

    protected NotifiableItemStackHandler createInventory() {
        return new NotifiableItemStackHandler(this, getInventorySize(tier), IO.BOTH);
    }

    protected NotifiableFluidTank createTank() {
        return new NotifiableFluidTank(this, getTankSize(tier), TANK_SIZE, IO.BOTH);
    }

    ////////////////////////////////
    // ********** GUI *********** //
    ////////////////////////////////

    @Override
    public Object createUIWidget() {
        return BufferMachineUI.createUIWidget(this);
    }

    ////////////////////////////////
    // ********** Misc ***********//
    ////////////////////////////////

    @Override
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        inventory.dropInventoryInWorld();
    }
}
