package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.TieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.trait.AutoOutputTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandlers;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Column;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.mui.widgets.slot.FluidSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.SlotGroup;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.MethodsReturnNonnullByDefault;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BufferMachine extends TieredMachine implements IMuiMachine {

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

    // TODO MUI: Needs EIO widget
    @Override
    public @NotNull ModularPanel buildUI(@NotNull PosGuiData data, @NotNull PanelSyncManager syncManager,
                                         @NotNull UISettings settings) {
        for (int i = 0; i < tank.getTanks(); i++) {
            syncManager.syncValue("fluids", i, SyncHandlers.fluidSlot(tank.getStorages()[i]));
        }

        SlotGroup slotGroup = new SlotGroup("inventory", inventory.getSlots());

        int size = tank.getTanks();
        String[] matrix = new String[size];
        for (int i = 0; i < size; i++) {
            var row = new StringBuilder(size + 1);
            for (int j = 0; j < size; j++) {
                row.append("I");
            }
            row.append("F");
            matrix[i] = row.toString();
        }

        SlotGroupWidget slotWidget = SlotGroupWidget.builder()
                .matrix(matrix)
                .key('I', i -> new ItemSlot()
                        .slot(new ModularSlot(inventory, i)
                                .slotGroup(slotGroup)))
                .key('F', i -> new FluidSlot()
                        .syncHandler("fluids", i))
                .build();

        return new ModularPanel(this.getDefinition().getName())
                .size(176, 100 + (18 * size))
                .child(GTMuiWidgets.createTitleBar(this.getDefinition(), 176))
                .child(new ParentWidget<>()
                        .widthRel(1)
                        .height(20 + 18 * size)
                        .child(Flow.row()
                                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                                .align(Alignment.CENTER)
                                .coverChildren()
                                .child(slotWidget
                                        .marginLeft(30)
                                        .marginRight(30)
                                        .verticalCenter())))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7))
                .child(new Column()
                        .coverChildren()
                        .leftRel(1.0f)
                        .reverseLayout(true)
                        .bottom(16)
                        .padding(0, 8, 4, 4)
                        .childPadding(2)
                        .background(GTGuiTextures.BACKGROUND.getSubArea(0.25f, 0f, 1.0f, 1.0f))
                        .child(GTMuiWidgets.createAutoOutputItemButton(autoOutput, syncManager))
                        .child(GTMuiWidgets.createAutoOutputFluidButton(autoOutput, syncManager))
                        .child(GTMuiWidgets.createInputFromOutputItem(autoOutput, syncManager))
                        .child(GTMuiWidgets.createInputFromOutputFluid(autoOutput, syncManager))
                        .excludeAreaInXei());
    }
    /*
     * @Override
     * public Widget createUIWidget() {
     * int invTier = getTankSize(tier);
     * var group = new WidgetGroup(0, 0, 18 * (invTier + 1) + 16, 18 * invTier + 16);
     * var container = new WidgetGroup(4, 4, 18 * (invTier + 1) + 8, 18 * invTier + 8);
     *
     * int index = 0;
     * for (int y = 0; y < invTier; y++) {
     * for (int x = 0; x < invTier; x++) {
     * container.addWidget(new SlotWidget(
     * getInventory().storage, index++, 4 + x * 18, 4 + y * 18, true, true)
     * .setBackgroundTexture(GuiTextures.SLOT));
     * }
     * }
     *
     * index = 0;
     * for (int y = 0; y < invTier; y++) {
     * container.addWidget(new TankWidget(
     * tank.getStorages()[index++], 4 + invTier * 18, 4 + y * 18, true, true)
     * .setBackground(GuiTextures.FLUID_SLOT));
     * }
     *
     * container.setBackground(GuiTextures.BACKGROUND_INVERSE);
     * group.addWidget(container);
     * return group;
     * }
     */

    ////////////////////////////////
    // ********** Misc ***********//
    ////////////////////////////////

    @Override
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        inventory.dropInventoryInWorld();
    }
}
