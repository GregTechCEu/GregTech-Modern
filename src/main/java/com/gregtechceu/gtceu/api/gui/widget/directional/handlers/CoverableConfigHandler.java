package com.gregtechceu.gtceu.api.gui.widget.directional.handlers;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.widget.CoverConfigurator;
import com.gregtechceu.gtceu.api.gui.widget.directional.IDirectionalConfigHandler;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.common.item.CoverPlaceBehavior;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CoverableConfigHandler implements IDirectionalConfigHandler {
    private static final int UPDATE_CLIENT_ID = 0x0001_0001;

    private final ICoverable machine;
    private ItemStackTransfer transfer;
    private Direction side;

    private ConfiguratorPanel panel;
    private ConfiguratorPanel.FloatingTab coverConfigurator;

    private SlotWidget slotWidget;
    private ButtonWidget buttonWidget;

    private CoverBehavior coverBehavior;
    private boolean needsUpdate;

    public CoverableConfigHandler(ICoverable machine) {
        this.machine = machine;
        this.transfer = createItemStackTransfer();
    }

    private ItemStackTransfer createItemStackTransfer() {
        var transfer = new ItemStackTransfer(1) {
            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };

        transfer.setFilter(itemStack -> {
            if (itemStack.isEmpty()) return true;
            if (this.side == null) return false;
            return CoverPlaceBehavior.isCoverBehaviorItem(itemStack, () -> false, coverDef -> ICoverable.canPlaceCover(coverDef, this.machine));
        });

        return transfer;
    }

    @Override
    public Widget getSideSelectorWidget(SceneWidget scene, FancyMachineUIWidget machineUI) {
        WidgetGroup group = new CoverConfigWidgetGroup((18 * 2) + 1, 18);

        this.panel = machineUI.getConfiguratorPanel();
        this.slotWidget = new SlotWidget(transfer, 0, 19, 0)
            .setChangeListener(this::coverItemChanged)
            .setBackgroundTexture(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY));
        this.buttonWidget = new ButtonWidget(0, 0, 18, 18, GuiTextures.VANILLA_BUTTON, this::toggleConfigTab);

        updateWidgetVisibility();

        group.addWidget(slotWidget);
        group.addWidget(buttonWidget);

        return group;
    }

    private void coverItemChanged() {
        if (!(panel.getGui().entityPlayer instanceof ServerPlayer serverPlayer) || side == null)
            return;

        var item = transfer.getStackInSlot(0);
        if (machine.getCoverAtSide(side) != null) {
            machine.removeCover(false, side, serverPlayer);
        }

        if (!item.isEmpty() && machine.getCoverAtSide(side) == null) {
            if (item.getItem() instanceof ComponentItem componentItem) {
                for (IItemComponent component : componentItem.getComponents()) {
                    if (component instanceof CoverPlaceBehavior placeBehavior) {
                        machine.placeCoverOnSide(side, item, placeBehavior.coverDefinition(), serverPlayer);
                        break;
                    }
                }
            }
        }

        checkCoverBehaviour();
    }

    @Override
    public void onSideSelected(BlockPos pos, Direction side) {
        this.side = side;
        updateWidgetVisibility();
        checkCoverBehaviour();
    }

    private void updateWidgetVisibility() {
        var sideSelected = this.side != null;
        slotWidget.setVisible(sideSelected);
        slotWidget.setActive(sideSelected);

        var coverPresent = sideSelected && coverBehavior != null && machine.getCoverAtSide(side) instanceof IUICover;
        buttonWidget.setVisible(coverPresent);
        buttonWidget.setActive(coverPresent);
    }

    public boolean checkCoverBehaviour() {
        if (side != null) {
            var coverBehaviour = machine.getCoverAtSide(side);
            if (coverBehaviour != this.coverBehavior) {
                this.coverBehavior = coverBehaviour;
                var attachItem = coverBehaviour == null ? ItemStack.EMPTY : coverBehaviour.getAttachItem();
                transfer.setStackInSlot(0, attachItem);
                transfer.onContentsChanged(0);
                return true;
            }
        }
        return false;
    }

    private void toggleConfigTab(ClickData cd) {
        if (this.coverConfigurator == null)
            openConfigTab();
        else
            closeConfigTab();
    }

    private void openConfigTab() {
        CoverConfigurator configurator = new CoverConfigurator(this.machine, this.side, this.coverBehavior);

        this.coverConfigurator = this.panel.createFloatingTab(configurator);
        this.coverConfigurator.setGui(panel.getGui());
        this.panel.addWidget(this.coverConfigurator);
        this.panel.expandTab(this.coverConfigurator);
    }

    private void closeConfigTab() {
        if (this.coverConfigurator == null)
            return;

        this.panel.collapseTab();
        this.coverConfigurator.collapseTo(18, 30);
        this.panel.removeWidget(this.coverConfigurator);
        this.coverConfigurator = null;
    }

    @Override
    public ScreenSide getScreenSide() {
        return ScreenSide.RIGHT;
    }


    private class CoverConfigWidgetGroup extends WidgetGroup {
        public CoverConfigWidgetGroup(int width, int height) {
            super(0, 0, width, height);
        }

        @Override
        public void updateScreen() {
            super.updateScreen();
            if (side != null && checkCoverBehaviour()) {
                writeClientAction(UPDATE_CLIENT_ID, $ -> {});
            }
        }

        @Override
        public void detectAndSendChanges() {
            super.detectAndSendChanges();
            if (side != null && needsUpdate && checkCoverBehaviour()) {
                needsUpdate = false;
            }
        }

        @Override
        public void handleClientAction(int id, FriendlyByteBuf buffer) {
            switch (id) {
                case UPDATE_CLIENT_ID -> needsUpdate = true;
                default -> super.handleClientAction(id, buffer);
            }
        }
    }
}
