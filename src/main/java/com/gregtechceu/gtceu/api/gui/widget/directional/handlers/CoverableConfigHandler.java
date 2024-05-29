package com.gregtechceu.gtceu.api.gui.widget.directional.handlers;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.widget.CoverConfigurator;
import com.gregtechceu.gtceu.api.gui.widget.PredicatedButtonWidget;
import com.gregtechceu.gtceu.api.gui.widget.directional.IDirectionalConfigHandler;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.common.item.CoverPlaceBehavior;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.SceneWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CoverableConfigHandler implements IDirectionalConfigHandler {

    private static final IGuiTexture CONFIG_BTN_TEXTURE = new GuiTextureGroup(GuiTextures.IO_CONFIG_COVER_SETTINGS);

    private final ICoverable machine;
    private ItemStackTransfer transfer;
    private Direction side;

    private ConfiguratorPanel panel;
    private ConfiguratorPanel.FloatingTab coverConfigurator;

    private SlotWidget slotWidget;
    private CoverBehavior coverBehavior;

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
            return CoverPlaceBehavior.isCoverBehaviorItem(itemStack, () -> false,
                    coverDef -> ICoverable.canPlaceCover(coverDef, this.machine));
        });

        return transfer;
    }

    @Override
    public Widget getSideSelectorWidget(SceneWidget scene, FancyMachineUIWidget machineUI) {
        WidgetGroup group = new WidgetGroup(0, 0, (18 * 2) + 1, 18);
        this.panel = machineUI.getConfiguratorPanel();

        group.addWidget(slotWidget = new SlotWidget(transfer, 0, 19, 0)
                .setChangeListener(this::coverItemChanged)
                .setBackgroundTexture(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.IO_CONFIG_COVER_SLOT_OVERLAY)));
        group.addWidget(new PredicatedButtonWidget(0, 0, 18, 18, CONFIG_BTN_TEXTURE, this::toggleConfigTab,
                () -> side != null && coverBehavior != null && machine.getCoverAtSide(side) instanceof IUICover));

        checkCoverBehaviour();

        return group;
    }

    private void coverItemChanged() {
        closeConfigTab();

        if (!(panel.getGui().entityPlayer instanceof ServerPlayer serverPlayer) || side == null)
            return;

        var item = transfer.getStackInSlot(0);
        if (machine.getCoverAtSide(side) != null) {
            machine.removeCover(false, side, serverPlayer);
        }

        if (!item.isEmpty() && machine.getCoverAtSide(side) == null) {
            if (item.getItem() instanceof IComponentItem componentItem) {
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
        checkCoverBehaviour();
        closeConfigTab();
    }

    private void updateWidgetVisibility() {
        var sideSelected = this.side != null;
        slotWidget.setVisible(sideSelected);
        slotWidget.setActive(sideSelected);
    }

    public void checkCoverBehaviour() {
        if (side == null)
            return;

        var coverBehaviour = machine.getCoverAtSide(side);
        if (coverBehaviour != this.coverBehavior) {
            this.coverBehavior = coverBehaviour;

            var attachItem = coverBehaviour == null ? ItemStack.EMPTY : coverBehaviour.getAttachItem();
            transfer.setStackInSlot(0, attachItem);
            transfer.onContentsChanged(0);
        }

        updateWidgetVisibility();
    }

    private void toggleConfigTab(ClickData cd) {
        if (this.coverConfigurator == null)
            openConfigTab();
        else
            closeConfigTab();
    }

    private void openConfigTab() {
        CoverConfigurator configurator = new CoverConfigurator(this.machine, this.side, this.coverBehavior) {

            @Override
            public Component getTitle() {
                // Uses the widget's own title
                return Component.empty();
            }

            @Override
            public IGuiTexture getIcon() {
                return GuiTextures.CLOSE_ICON;
            }

            @Override
            public Widget createConfigurator() {
                WidgetGroup group = new WidgetGroup(new Position(0, 0));

                if (side == null || !(coverable.getCoverAtSide(side) instanceof IUICover iuiCover))
                    return group;

                Widget coverConfigurator = iuiCover.createUIWidget();
                coverConfigurator.addSelfPosition(-1, -20);

                group.addWidget(coverConfigurator);
                group.setSize(new Size(
                        Math.max(120, coverConfigurator.getSize().width),
                        Math.max(80, coverConfigurator.getSize().height - 20)));

                return group;
            }
        };

        this.coverConfigurator = this.panel.createFloatingTab(configurator);
        this.coverConfigurator.setGui(this.panel.getGui());
        this.panel.addWidget(this.coverConfigurator);
        this.panel.expandTab(this.coverConfigurator);

        coverConfigurator.onClose(() -> {
            if (coverConfigurator != null) {
                this.panel.removeWidget(this.coverConfigurator);
            }

            this.coverConfigurator = null;
        });
    }

    private void closeConfigTab() {
        if (this.coverConfigurator != null) {
            this.panel.collapseTab();
        }
    }

    @Override
    public ScreenSide getScreenSide() {
        return ScreenSide.RIGHT;
    }
}
