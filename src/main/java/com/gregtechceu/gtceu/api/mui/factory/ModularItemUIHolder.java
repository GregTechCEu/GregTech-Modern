package com.gregtechceu.gtceu.api.mui.factory;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.module.AppliedItemModule;
import com.gregtechceu.gtceu.api.item.module.IModularItem;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ItemModuleSlot;
import com.gregtechceu.gtceu.api.mui.GTGuiScreen;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.PlayerArmorInvWrapper;

import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.IUIHolder;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.IKey;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.GuiDraw;
import brachy.modularui.factory.GuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.UISettings;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.value.sync.DynamicSyncHandler;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.SyncHandlers;
import brachy.modularui.widgets.*;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.layout.Grid;
import brachy.modularui.widgets.slot.ItemSlot;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Setter(AccessLevel.PRIVATE)
@Getter(AccessLevel.PRIVATE)
public class ModularItemUIHolder implements IUIHolder<GuiData> {

    private final Player player;
    private boolean inventoryUnlocked = false;
    private IntSyncValue selectedSlotSyncValue = null;
    private int selectedSlot = -1;
    private int panelCount = 0;
    private DynamicSyncHandler dynamicSyncHandler = null;

    public ModularItemUIHolder(Player player) {
        this.player = player;
    }

    private void registerSyncValues(PanelSyncManager syncManager) {
        dynamicSyncHandler = new DynamicSyncHandler();
        dynamicSyncHandler.widgetProvider(this::getStackInfoWidget);
        selectedSlotSyncValue = SyncHandlers.intNumber(this::getSelectedSlot, this::setSelectedSlot);
        syncManager.syncValue("selectedSlot", selectedSlotSyncValue);
    }

    @Override
    public ModularPanel<?> buildUI(GuiData data, PanelSyncManager syncManager, UISettings settings) {
        registerSyncValues(syncManager);
        return new ModularPanel<>("modularItem")
                .leftRel(.2f)
                .width(250)
                .child(GTMuiWidgets.createTitleBar(IDrawable.EMPTY.asIcon().size(0), "Modules", 250,
                        GTGuiTextures.BACKGROUND))
                .child(playerInventory())
                .child(new ToggleButton()
                        .syncHandler("inventoryUnlocked")
                        .overlay(true, GTGuiTextures.BUTTON_LOCK)
                        .overlay(false, GTGuiTextures.BUTTON_LOCK)
                        .invertSelected(true)
                        .left(7).bottom(7))
                .child(new DynamicSyncedWidget<>()
                        .syncHandler(dynamicSyncHandler)
                        .initialChild(new TextWidget<>("Select an item")
                                .center())
                        .widthRel(1)
                        .height(85)
                        .top(5));
    }

    private IWidget getStackInfoWidget(PanelSyncManager psm, FriendlyByteBuf buf) {
        ItemStack stack = getSelectedItem();
        IModularItem modularItem = GTCapabilityHelper.getModularItem(stack);
        List<ItemModuleSlot> slots = modularItem == null ? List.of() : modularItem.getSlots();
        return Flow.col()
                .horizontalCenter()
                .widthRel(1)
                .child(Flow.row()
                        .coverChildren()
                        .childPadding(5)
                        .child(new ItemDisplayWidget().item(stack))
                        .child(new TextWidget<>(IKey.dynamic(stack::getHoverName))))
                .childIf(modularItem == null,
                        () -> new TextWidget<>(IKey.str("This item does not accept modules")).center())
                .childIf(modularItem != null, () -> new Grid()
                        .minColWidth(100)
                        .widthRel(1)
                        .minRowHeight(10)
                        .margin(5)
                        .mapTo(2, slots.size(), index -> {
                            assert modularItem != null;
                            AppliedItemModule appliedModule = modularItem.getModuleInSlot(index);
                            if (appliedModule == null) {
                                return new ButtonWidget<>()
                                        .height(10)
                                        .widthRel(0.5f)
                                        .backgroundOverlay(slots.get(index).getSlotTexture())
                                        .overlay(IKey.lang("metaarmor.tooltip.modifier.empty").scale(0.5f));
                            } else {
                                ItemModule module = appliedModule.getModule();
                                IPanelHandler panelHandler = psm.syncedPanel("module" + index, false,
                                        (psm1, handler) -> createPanelForModule(psm1, handler, index));
                                return new ButtonWidget<>()
                                        .onMousePressed((x, y, button) -> {
                                            panelHandler.openPanel();
                                            return false;
                                        })
                                        .height(10)
                                        .widthRel(0.5f)
                                        .overlay(IKey.dynamic(() -> module.getDisplayName(appliedModule)).scale(0.5f))
                                        .backgroundOverlay(slots.get(index).getSlotTexture())
                                        .addTooltipElement(IKey.dynamic(module::getInfo));
                            }
                        }));
    }

    private ModularPanel<?> createPanelForModule(PanelSyncManager psm, IPanelHandler panelHandler, int index) {
        ItemStack stack = getSelectedItem();
        IModularItem modularItem = GTCapabilityHelper.getModularItem(stack);
        assert modularItem != null;
        AppliedItemModule appliedModule = modularItem.getModuleInSlot(index);
        assert appliedModule != null;
        ItemModule module = appliedModule.getModule();
        ItemStack moduleItem = appliedModule.getModuleItem();
        return new ModularPanel<>("module" + index)
                .onCloseAction(() -> panelCount--)
                .leftRelOffset(0.2f, 250 + 2 - 134 * (panelCount / 3))
                .topRelOffset(0.5f, -83 + 80 * (panelCount++ % 3) + 2)
                .width(134)
                .height(80)
                .child(Flow.col()
                        .left(0)
                        .childPadding(3)
                        .childIf(moduleItem == null || moduleItem.isEmpty(),
                                () -> new TextWidget<>(IKey.dynamic(() -> module.getDisplayName(appliedModule)))
                                        .scale(0.75f)
                                        .horizontalCenter())
                        .childIf(moduleItem != null && !moduleItem.isEmpty(), () -> {
                            assert moduleItem != null;
                            return Flow.row()
                                    .coverChildren()
                                    .padding(4)
                                    .childPadding(4)
                                    .child(new ItemDisplayWidget().item(moduleItem))
                                    .child(Flow.col()
                                            .coverChildrenWidth()
                                            .childPadding(2)
                                            .heightRel(1)
                                            .child(new TextWidget<>(
                                                    IKey.dynamic(() -> module.getDisplayName(appliedModule)))
                                                    .scale(0.75f)
                                                    .left(0))
                                            .child(new TextWidget<>(IKey.dynamic(moduleItem::getHoverName))
                                                    .scale(0.6f)
                                                    .left(0)));
                        })
                        .children(module.getSettings(appliedModule, psm, index)));
    }

    private ItemStack getSelectedItem() {
        return player.getInventory().getItem(selectedSlot);
    }

    @Override
    public ModularScreen createScreen(GuiData data, ModularPanel<?> mainPanel) {
        return new GTGuiScreen(mainPanel);
    }

    private class InventorySlot extends ItemSlot {

        private Integer index = null;

        public InventorySlot index(int index) {
            this.index = index;
            return this;
        }

        @Override
        public @NotNull Result onMousePressed(double mouseX, double mouseY, int button) {
            if (inventoryUnlocked)
                return super.onMousePressed(mouseX, mouseY, button);
            selectedSlotSyncValue.setValue(index != null ? index : this.getSlot().getSlotIndex());
            dynamicSyncHandler.notifyUpdate(buf -> {});
            return Result.SUCCESS;
        }

        @Override
        public boolean onMouseReleased(double mouseX, double mouseY, int button) {
            if (inventoryUnlocked)
                return super.onMouseReleased(mouseX, mouseY, button);
            return false;
        }

        @Override
        protected void drawOverlay(ModularGuiContext context) {
            if (inventoryUnlocked)
                super.drawOverlay(context);
            if ((index != null ? index : this.getSlot().getSlotIndex()) == selectedSlot) {
                GuiDraw.drawBorder(context.getGraphics(), 0, 0, 17, 17, 0xFFFFFF00, 1);
            }
        }

        @Override
        public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
            if (!inventoryUnlocked && !isHovering() && this.getSlot().getSlotIndex() != selectedSlot) {
                GuiDraw.drawRect(context.getGraphics(), 1, 1, 15, 15, 0x88444444);
            }
            super.draw(context, widgetTheme);
        }
    }

    private Flow playerInventory() {
        SlotGroupWidget slotGroupWidget = new SlotGroupWidget();
        slotGroupWidget.coverChildren();
        slotGroupWidget.name("player_inventory");
        String key = "player";
        for (int i = 0; i < 9; i++) {
            slotGroupWidget.child(new InventorySlot()
                    .syncHandler(key, i)
                    .pos(i * 18, 3 * 18 + 4)
                    .name("slot_" + i));
        }
        for (int i = 0; i < 27; i++) {
            slotGroupWidget.child(new InventorySlot()
                    .syncHandler(key, i + 9)
                    .pos(i % 9 * 18, i / 9 * 18)
                    .name("slot_" + (i + 9)));
        }
        SlotGroupWidget armorGroup = new SlotGroupWidget();
        armorGroup.coverChildren();
        armorGroup.name("player_armor");
        PlayerArmorInvWrapper inv = new PlayerArmorInvWrapper(player.getInventory());
        for (int i = 0; i < 4; i++) {
            armorGroup.child(new InventorySlot()
                    .index(36 + i)
                    .slot(inv, i)
                    .pos(0, (3 - i) * 18)
                    .name("slot_" + (i + 36)));
        }
        return Flow.row()
                .child(slotGroupWidget.bottom(7).leftRel(.5f))
                .child(armorGroup.bottom(7).leftRelOffset(.5f, 162 / 2 + 20));
    }
}
