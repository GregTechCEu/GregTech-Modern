package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.machine.TieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.AutoOutputTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.MethodsReturnNonnullByDefault;

import lombok.Getter;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
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
    public Widget createUIWidget() {
        int invTier = getTankSize(tier);
        var group = new WidgetGroup(0, 0, 18 * (invTier + 1) + 16, 18 * invTier + 16);
        var container = new WidgetGroup(4, 4, 18 * (invTier + 1) + 8, 18 * invTier + 8);

        int index = 0;
        for (int y = 0; y < invTier; y++) {
            for (int x = 0; x < invTier; x++) {
                container.addWidget(new SlotWidget(
                        getInventory().storage, index++, 4 + x * 18, 4 + y * 18, true, true)
                        .setBackgroundTexture(GuiTextures.SLOT));
            }
        }

        index = 0;
        for (int y = 0; y < invTier; y++) {
            container.addWidget(new TankWidget(
                    tank.getStorages()[index++], 4 + invTier * 18, 4 + y * 18, true, true)
                    .setBackground(GuiTextures.FLUID_SLOT));
        }

        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(container);
        return group;
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
