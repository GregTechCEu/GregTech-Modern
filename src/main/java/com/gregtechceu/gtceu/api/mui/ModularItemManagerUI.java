package com.gregtechceu.gtceu.api.mui;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.module.AppliedItemModule;
import com.gregtechceu.gtceu.api.item.module.IModularItem;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ItemModuleSlot;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.PlayerArmorInvWrapper;

import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.IUIHolder;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.factory.GuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.BoolValue;
import brachy.modularui.value.sync.*;
import brachy.modularui.widgets.*;
import brachy.modularui.widgets.dynamic.DynamicWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.layout.Grid;
import brachy.modularui.widgets.slot.ModularSlot;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@Setter(AccessLevel.PRIVATE)
@Getter(AccessLevel.PRIVATE)
public class ModularItemManagerUI implements IUIHolder<GuiData> {

    private final Player player;
    private boolean inventoryLocked = true;
    private int selectedSlot = -1;
    private int panelCount = 0;

    public ModularItemManagerUI(Player player) {
        this.player = player;
    }

    @Override
    public ModularPanel<?> buildUI(GuiData data, PanelSyncManager syncManager, UISettings settings) {
        IntSyncValue selectedSlotSyncValue = SyncHandlers.intNumber(this::getSelectedSlot, this::setSelectedSlot)
                .allowC2S();
        syncManager.syncValue("selectedSlot", selectedSlotSyncValue);

        DynamicLinkedSyncHandler<IntSyncValue> dynamicSyncHandler = new DynamicLinkedSyncHandler<>(
                selectedSlotSyncValue)
                .allowC2S()
                .widgetProvider(this::getStackInfoWidget);

        return new ModularPanel<>("modularItem")
                .horizontalCenter()
                .width(250)
                .child(GTMuiWidgets.createTitleBar(null, "Modules", 250,
                        GTGuiTextures.BACKGROUND))
                .child(playerInventory(selectedSlotSyncValue))
                .child(new ToggleButton()
                        .value(new BooleanSyncValue(this::isInventoryLocked, this::setInventoryLocked))
                        .overlay(true, GTGuiTextures.BUTTON_LOCK)
                        .overlay(false, GTGuiTextures.BUTTON_LOCK)
                        .left(7).bottom(7))
                .child(new DynamicWidget<>()
                        .syncHandler(dynamicSyncHandler)
                        .widthRel(1)
                        .height(85)
                        .top(5));
    }

    private IWidget getStackInfoWidget(PanelSyncManager psm, IntSyncValue slotSync) {
        ItemStack stack = getSelectedItem();
        IModularItem modularItem = stack == null ? null : GTCapabilityHelper.getModularItem(stack);
        List<ItemModuleSlot> slots = modularItem == null ? List.of() : modularItem.getSlots();
        return Flow.col()
                .horizontalCenter()
                .widthRel(1)
                .childIf(stack == null, () -> new TextWidget<>(Text.lang("gtceu.module.gui.select_an_item"))
                        .center())
                .childIf(stack != null, () -> {
                    assert stack != null;
                    return Flow.row()
                            .coverChildren()
                            .childPadding(5)
                            .child(new ItemDisplayWidget().item(stack))
                            .child(new TextWidget<>(Text.dynamic(stack::getHoverName)));
                })
                .childIf(modularItem == null && stack != null,
                        () -> new TextWidget<>(Text.lang("gtceu.module.gui.invalid_item")).center())
                .childIf(modularItem != null, () -> new Grid()
                        .minColWidth(100)
                        .widthRel(1)
                        .minRowHeight(10)
                        .margin(5)
                        .gridOfSizeWidth(slots.size(), 2, (x, y, index) -> {
                            assert modularItem != null;
                            AppliedItemModule appliedModule = modularItem.getModuleInSlot(index);
                            if (appliedModule == null) {
                                ButtonWidget<?> button = new ButtonWidget<>()
                                        .height(10)
                                        .widthRel(0.5f)
                                        .overlay(Text.lang("metaarmor.tooltip.modifier.empty").scale(0.5f));
                                if (index < slots.size()) {
                                    button.backgroundOverlay(slots.get(index).getSlotTexture());
                                }
                                return button;
                            } else {
                                ItemModule module = appliedModule.getModule();
                                IPanelHandler panelHandler = psm.syncedPanel("module" + index, false,
                                        (psm1, handler) -> createPanelForModule(psm1, index));
                                return new ButtonWidget<>()
                                        .onMousePressed((ctx, button) -> {
                                            panelHandler.openPanel();
                                            return false;
                                        })
                                        .height(10)
                                        .widthRel(0.5f)
                                        .overlay(Text.dynamic(() -> module.getDisplayName(appliedModule)).scale(0.5f))
                                        .backgroundOverlay(slots.get(index).getSlotTexture())
                                        .tooltipDynamic(tooltip -> tooltip
                                                .add(module.getInfo()));
                            }
                        }));
    }

    private ModularPanel<?> createPanelForModule(PanelSyncManager psm, int index) {
        ItemStack stack = Objects.requireNonNull(getSelectedItem());
        IModularItem modularItem = Objects.requireNonNull(GTCapabilityHelper.getModularItem(stack));
        AppliedItemModule appliedModule = Objects.requireNonNull(modularItem.getModuleInSlot(index));
        ItemModule module = appliedModule.getModule();
        ItemStack moduleItem = appliedModule.getModuleItem();

        return new ModularPanel<>("module" + index)
                .leftRelOffset(0.2f, 250 + 2 - 154 * (panelCount / 3))
                .topRelOffset(0.5f, -83 + 80 * (panelCount++ % 3) + 2)
                .coverChildren()
                .child(Flow.col()
                        .margin(4)
                        .coverChildren()
                        .crossAxisAlignment(Alignment.CrossAxis.START)
                        .childIf(moduleItem == null || moduleItem.isEmpty(),
                                () -> new TextWidget<>(Text.dynamic(() -> module.getDisplayName(appliedModule)))
                                        .scale(0.75f)
                                        .horizontalCenter()
                                        .paddingBottom(2))
                        .childIf(moduleItem != null && !moduleItem.isEmpty(), () -> {
                            assert moduleItem != null;
                            return Flow.row()
                                    .coverChildren()
                                    .padding(4)
                                    .paddingLeft(0)
                                    .childPadding(4)
                                    .child(new ItemDisplayWidget().item(moduleItem))
                                    .child(Flow.col()
                                            .coverChildrenWidth()
                                            .childPadding(2)
                                            .heightRel(1)
                                            .child(new TextWidget<>(
                                                    Text.dynamic(() -> module.getDisplayName(appliedModule)))
                                                    .scale(0.75f)
                                                    .left(0))
                                            .child(new TextWidget<>(Text.dynamic(moduleItem::getHoverName))
                                                    .scale(0.6f)
                                                    .left(0)));
                        })
                        .children(module.getSettings(appliedModule, psm, index)));
    }

    private @Nullable ItemStack getSelectedItem() {
        if (selectedSlot == -1)
            return null;
        return player.getInventory().getItem(selectedSlot);
    }

    @Override
    public ModularScreen createScreen(GuiData data, ModularPanel<?> mainPanel) {
        return new GTGuiScreen(mainPanel);
    }

    private Flow playerInventory(IntSyncValue selectedSlotSyncValue) {
        SlotGroupWidget slotGroupWidget = new SlotGroupWidget();
        slotGroupWidget.coverChildren();
        slotGroupWidget.name("player_inventory");
        String key = "player";
        for (int i = 0; i < 9; i++) {
            slotGroupWidget.child(new SelectableSlot()
                    .selectable(new BoolValue.Dynamic(this::isInventoryLocked, null))
                    .selectedIndex(selectedSlotSyncValue)
                    .syncHandler(key, i)
                    .pos(i * 18, 3 * 18 + 4)
                    .name("slot_" + i));
        }
        for (int i = 0; i < 27; i++) {
            slotGroupWidget.child(new SelectableSlot()
                    .selectable(new BoolValue.Dynamic(this::isInventoryLocked, null))
                    .selectedIndex(selectedSlotSyncValue)
                    .syncHandler(key, i + 9)
                    .pos(i % 9 * 18, i / 9 * 18)
                    .name("slot_" + (i + 9)));
        }
        SlotGroupWidget armorGroup = new SlotGroupWidget();
        armorGroup.coverChildren();
        armorGroup.name("player_armor");
        PlayerArmorInvWrapper inv = new PlayerArmorInvWrapper(player.getInventory());
        for (int i = 0; i < 4; i++) {
            armorGroup.child(new SelectableSlot()
                    .selectable(new BoolValue.Dynamic(this::isInventoryLocked, null))
                    .selectedIndex(selectedSlotSyncValue)
                    .index(36 + i)
                    .slot(ModularSlot.playerSlot(inv, i, player))
                    .pos(0, (3 - i) * 18)
                    .name("slot_" + (i + 36)));
        }
        return Flow.row()
                .child(slotGroupWidget.bottom(7).leftRel(.5f))
                .child(armorGroup.bottom(7).leftRelOffset(.5f, 162 / 2 + 20));
    }
}
