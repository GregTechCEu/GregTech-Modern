package com.gregtechceu.gtceu.common.cover.ender;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.ColorBlockWidget;
import com.gregtechceu.gtceu.api.gui.widget.ConfirmTextInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.gui.widget.IOSelectorTextures;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.api.misc.virtualregistry.VirtualEnderRegistry;
import com.gregtechceu.gtceu.api.misc.virtualregistry.VirtualEntry;
import com.gregtechceu.gtceu.common.cover.data.CoverModeTextures;
import com.gregtechceu.gtceu.common.cover.data.ManualIOMode;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SelectableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.TextBoxWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

final class AbstractEnderLinkCoverUI {

    private AbstractEnderLinkCoverUI() {}

    static Widget createUIWidget(AbstractEnderLinkCover<?> cover) {
        return new VirtualEntryWidget(cover);
    }

    static void updateVirtualEntryWidget(Widget widget) {
        if (widget instanceof VirtualEntryWidget virtualEntryWidget) {
            virtualEntryWidget.update();
        }
    }

    static Widget createEmptyVirtualEntryWidget(int x, int y, int width, int height) {
        return new WidgetGroup(x, y, width, height);
    }

    private static class VirtualEntryWidget extends WidgetGroup {

        private static final int WIDGET_BOARD = 20;
        private static final int GROUP_WIDTH = 176;
        private static final int TOTAL_WIDTH = 156;
        private static final int BUTTON_SIZE = 16;
        private final AbstractEnderLinkCover<?> cover;
        private final MutableBoolean showChannels;
        private final WidgetGroup mainGroup;
        private final WidgetGroup mainChannelGroup;
        private final DraggableScrollableWidgetGroup channelsGroup;

        VirtualEntryWidget(AbstractEnderLinkCover<?> cover) {
            super(0, 0, GROUP_WIDTH, 137);
            this.cover = cover;
            this.showChannels = new MutableBoolean(false);
            mainGroup = new WidgetGroup(0, 0, GROUP_WIDTH, 137);
            channelsGroup = new DraggableScrollableWidgetGroup(0, 20, 170, 110)
                    .setYScrollBarWidth(2).setYBarStyle(null, ColorPattern.T_WHITE.rectTexture().setRadius(1));
            mainChannelGroup = new WidgetGroup(10, 20, 156, 20);
            initWidgets();
        }

        public void update() {
            if (isRemote()) return;
            widgets.clear();
            mainGroup.widgets.clear();
            channelsGroup.widgets.clear();
            mainChannelGroup.widgets.clear();
            initWidgets();
            this.detectAndSendChanges();
        }

        private void initWidgets() {
            int currentX = 0;
            final var titleGroup = new WidgetGroup(10, 5, GROUP_WIDTH, 20);

            this.addWidget(titleGroup);
            this.addWidget(mainGroup);
            this.addWidget(channelsGroup.setVisible(false));

            titleGroup.addWidget(createToggleButton());
            titleGroup.addWidget(new LabelWidget(15, 3, cover.getUITitle()));

            var toggleButtonWidget = createToggleButtonForPrivacy(currentX);
            mainChannelGroup.addWidget(toggleButtonWidget);
            currentX += WIDGET_BOARD + 2;
            mainChannelGroup.addWidget(createColorBlockWidget(currentX));
            currentX += WIDGET_BOARD + 2;
            mainChannelGroup.addWidget(createConfirmTextInputWidget(currentX));

            mainChannelGroup.addWidget(new ConfirmTextInputWidget(0, WIDGET_BOARD + 2, GROUP_WIDTH - WIDGET_BOARD,
                    WIDGET_BOARD, cover.getEntry().getDescription(), cover.getEntry()::setDescription,
                    t -> t == null ? "" : t, null).setTooltip("cover.ender_fluid_link.tooltip.channel_description"));

            mainGroup.addWidget(mainChannelGroup);
            mainGroup.addWidget(createWorkingEnabledButton());
            addEnumSelectorWidgets();
            mainGroup.addWidget((Widget) cover.addVirtualEntryWidget(cover.getEntry(), 146, WIDGET_BOARD,
                    WIDGET_BOARD, WIDGET_BOARD, true));

            if (cover.getFilterHandler() != null) {
                mainGroup.addWidget((Widget) cover.getFilterHandler().createFilterSlotUI(117, 108));
                mainGroup.addWidget((Widget) cover.getFilterHandler().createFilterConfigUI(10, 72, 156, 60));
            }
        }

        @Contract(" -> new")
        private @NotNull ToggleButtonWidget createToggleButton() {
            return (ToggleButtonWidget) new ToggleButtonWidget(0, 0, 12, 12, showChannels::getValue, cd -> {
                showChannels.setValue(!showChannels.getValue());
                mainGroup.setVisible(showChannels.isFalse());
                channelsGroup.setVisible(showChannels.isTrue());
                requestUpdate();
            }).setTexture(
                    new GuiTextureGroup(GuiTextures.TOGGLE_BUTTON_BACK.getSubTexture(0, 0, 1, 0.5),
                            GuiTextures.BUTTON_LIST),
                    new GuiTextureGroup(GuiTextures.TOGGLE_BUTTON_BACK.getSubTexture(0, 0.5, 1, 0.5),
                            GuiTextures.BUTTON_LIST))
                    .setHoverTooltips("cover.ender_fluid_link.tooltip.list_button");
        }

        @Contract("_ -> new")
        private @NotNull Widget createToggleButtonForPrivacy(int currentX) {
            return new EnumSelectorWidget<>(currentX, 0,
                    WIDGET_BOARD, WIDGET_BOARD, AbstractEnderLinkCover.Permissions.values(), cover.permission,
                    cover::setPermission,
                    AbstractEnderLinkCover.Permissions::getTooltip, VirtualEntryWidget::getPermissionIcon);
        }

        private static IGuiTexture getPermissionIcon(AbstractEnderLinkCover.Permissions permissions) {
            return switch (permissions) {
                case PUBLIC -> GuiTextures.BUTTON_PUBLIC_PRIVATE.getSubTexture(0, 0, 1, 0.5);
                case PRIVATE -> GuiTextures.BUTTON_PUBLIC_PRIVATE.getSubTexture(0, 0.5, 1, 0.5);
            };
        }

        private ColorBlockWidget createColorBlockWidget(int currentX) {
            return new ColorBlockWidget(currentX, 0, WIDGET_BOARD, WIDGET_BOARD).setColorSupplier(cover::getColor);
        }

        private ConfirmTextInputWidget createConfirmTextInputWidget(int currentX) {
            int GROUP_X = 10;
            int textInputWidth = (GROUP_WIDTH - GROUP_X * 2) - currentX - WIDGET_BOARD - 2;
            return new ConfirmTextInputWidget(currentX, 0, textInputWidth, WIDGET_BOARD, cover.colorStr,
                    cover::setChannelName, text -> {
                        if (text == null || !AbstractEnderLinkCover.COLOR_INPUT_PATTERN.matcher(text).matches()) {
                            return VirtualEntry.DEFAULT_COLOR;
                        }
                        return text;
                    }, text -> {
                        if (text.length() < 8) {
                            text += "F".repeat(8 - text.length());
                        }
                        return text;
                    }).setTooltip("cover.ender_fluid_link.tooltip.channel_name");
        }

        @Contract(" -> new")
        private @NotNull ToggleButtonWidget createWorkingEnabledButton() {
            return new ToggleButtonWidget(116, 82, WIDGET_BOARD, WIDGET_BOARD, GuiTextures.BUTTON_POWER,
                    cover::isWorkingEnabled, cover::setWorkingEnabled);
        }

        private void addEnumSelectorWidgets() {
            mainGroup.addWidget(new EnumSelectorWidget<>(146, 82, WIDGET_BOARD, WIDGET_BOARD, List.of(IO.IN, IO.OUT),
                    cover.io, cover::setIo, IO::getTooltip, IOSelectorTextures::getIcon));
            mainGroup.addWidget(new EnumSelectorWidget<>(146, 107, WIDGET_BOARD, WIDGET_BOARD, ManualIOMode.VALUES,
                    cover.manualIOMode, cover::setManualIOMode, ManualIOMode::getTooltip,
                    CoverModeTextures::getManualIOModeIcon)
                    .setHoverTooltips("cover.universal.manual_import_export.mode.description"));
        }

        private void addChannelWidgets(List<? extends VirtualEntry> entries) {
            channelsGroup.clearAllWidgets();
            int y = 1;
            SelectableWidgetGroup selectedWidget = null;
            for (var entry : entries.stream().sorted(Comparator.comparing(VirtualEntry::getColorStr)).toList()) {
                SelectableWidgetGroup channelWidget = createChannelWidget(entry, 10, y);
                if (cover.getChannelName(entry).equals(cover.getChannelName())) {
                    selectedWidget = channelWidget;
                }
                channelsGroup.addWidget(channelWidget);
                y += 22;
            }
            channelsGroup.setSelected(selectedWidget);
            if (selectedWidget != null) selectedWidget.onSelected();
            channelsGroup.setClientSideWidget();
        }

        private @NotNull SelectableWidgetGroup createChannelWidget(@NotNull VirtualEntry entry, int x, int y) {
            int currentX = 0;
            int MARGIN = 2;
            int availableWidth = TOTAL_WIDTH - (BUTTON_SIZE + MARGIN) * 3;

            final MutableBoolean canSelect = new MutableBoolean(false);
            var des = entry.getDescription();
            TextBoxWidget textBoxWidget = new TextBoxWidget(BUTTON_SIZE + MARGIN,
                    !des.isEmpty() ? 0 : 4, availableWidth, List.of(entry.getColorStr())).setCenter(true);
            SelectableWidgetGroup channelGroup = new SelectableWidgetGroup(x, y, TOTAL_WIDTH, BUTTON_SIZE) {

                @Override
                public boolean allowSelected(double mouseX, double mouseY, int button) {
                    return canSelect.getValue() && super.allowSelected(mouseX, mouseY, button);
                }
            };
            channelGroup.setOnSelected(group -> {
                if (cover.getChannelName().equals(cover.getChannelName(entry))) return;
                writeClientAction(0, buffer -> {
                    String newChannelColorStr = entry.getColorStr();
                    buffer.writeUtf(newChannelColorStr);
                });
                playButtonClickSound();
            }).setSelectedTexture(1, -1);

            ColorBlockWidget colorBlockWidget = new ColorBlockWidget(currentX, 0, BUTTON_SIZE, BUTTON_SIZE)
                    .setCurrentColor(VirtualEntry.parseColor(entry.getColorStr()));
            channelGroup.addWidget(colorBlockWidget);
            currentX += BUTTON_SIZE + MARGIN;

            channelGroup.addWidget(textBoxWidget);
            currentX += availableWidth + MARGIN;
            if (!des.isEmpty()) {
                var desText = new TextTexture(ChatFormatting.DARK_GRAY + des).setDropShadow(false);
                desText.setType(TextTexture.TextType.ROLL).setRollSpeed(0.7f);
                channelGroup.addWidget(new ImageWidget(BUTTON_SIZE + MARGIN, 10, availableWidth, 8, desText));
            }

            Widget slotWidget = (Widget) cover.addVirtualEntryWidget(entry, currentX, 0, BUTTON_SIZE, BUTTON_SIZE,
                    false);
            channelGroup.addWidget(slotWidget);
            currentX += BUTTON_SIZE + MARGIN;

            channelGroup.addWidget(
                    new ButtonWidget(currentX, 0, BUTTON_SIZE, BUTTON_SIZE, GuiTextures.BUTTON_CLEAR_GRID, press -> {
                        writeClientAction(200, buffer -> buffer.writeUtf(cover.getChannelName(entry)));
                        requestUpdate();
                    }) {

                        @Override
                        public boolean isMouseOverElement(double mouseX, double mouseY) {
                            var isOver = super.isMouseOverElement(mouseX, mouseY);
                            if (canSelect.getValue() == isOver) canSelect.setValue(!isOver);
                            return isOver;
                        }
                    }.appendHoverTooltips("cover.ender_fluid_link.tooltip.clear_button"));

            return channelGroup;
        }

        private void requestUpdate() {
            writeClientAction(100, buffer -> buffer.writeBoolean(showChannels.isTrue()));
        }

        @Override
        public void handleClientAction(int id, RegistryFriendlyByteBuf buffer) {
            super.handleClientAction(id, buffer);
            if (id == 0) {
                String newChannelColorStr = buffer.readUtf();
                cover.setChannelName(newChannelColorStr);
            } else if (id == 100) {
                if (!buffer.readBoolean()) return;
                var entries = VirtualEnderRegistry.getInstance().getEntryNames(cover.getOwner(), cover.getEntryType())
                        .stream().map(name -> VirtualEnderRegistry.getInstance().getEntry(cover.getOwner(),
                                cover.getEntryType(), name))
                        .sorted(Comparator.comparing(VirtualEntry::getColorStr));
                writeUpdateInfo(101, buf -> {
                    var list = entries.toList();
                    buf.writeVarInt(list.size());
                    for (var entry : list) {
                        buf.writeNbt(entry.serializeNBT(buf.registryAccess()));
                    }
                });
            } else if (id == 200) {
                String channelName = buffer.readUtf();
                VirtualEnderRegistry.getInstance().getEntry(cover.getOwner(), cover.getEntryType(), channelName)
                        .setDescription("");
            }
        }

        @Override
        public void readUpdateInfo(int id, RegistryFriendlyByteBuf buffer) {
            super.readUpdateInfo(id, buffer);
            if (id == 101) {
                int size = buffer.readVarInt();
                List<VirtualEntry> entries = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    VirtualEntry entry = cover.getEntryType().createInstance();
                    entry.deserializeNBT(buffer.registryAccess(), Objects.requireNonNull(buffer.readNbt()));
                    entries.add(entry);
                }
                addChannelWidgets(entries);
            }
        }
    }
}
