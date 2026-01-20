package com.gregtechceu.gtceu.api.mui.widgets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.drawable.UITexture;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.api.mui.widget.WidgetTree;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.SlotGroup;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.world.inventory.Slot;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class SortButtons extends Widget<SortButtons> {

    public static final UITexture HOVER_SORT_OVERLAY;
    public static final UITexture HOVER_SETTINGS_OVERLAY;

    static {
        // if (GTCEu.isClientSide() /*&& ModularUI.Mods.BOGOSORTER.isLoaded()*/) {
        // HOVER_SORT_OVERLAY = ButtonHandler.BUTTON_SORT.withColorOverride(16777120);
        // HOVER_SETTINGS_OVERLAY = ButtonHandler.BUTTON_SETTINGS.withColorOverride(16777120);
        // } else {
        // any non null value
        HOVER_SORT_OVERLAY = GTGuiTextures.BLOCK;
        HOVER_SETTINGS_OVERLAY = GTGuiTextures.BLOCK;
        // }
    }

    @Getter
    private String slotGroupName;
    @Getter
    private SlotGroup slotGroup;

    private boolean horizontal = true;
    private final ButtonWidget<?> sortButton = new ButtonWidget<>();
    private final ButtonWidget<?> settingsButton = new ButtonWidget<>();
    @Getter
    private final @NotNull List<IWidget> children = Arrays.asList(sortButton, settingsButton);

    public SortButtons() {
        if (GTCEu.isClientSide() && false /* && ModularUI.Mods.BOGOSORTER.isLoaded() */) {
            this.sortButton.size(10).pos(0, 0)
                    // .background(ButtonHandler.BUTTON_BACKGROUND)
                    // .overlay(ButtonHandler.BUTTON_SORT)
                    .hoverOverlay(HOVER_SORT_OVERLAY)
                    .disableHoverBackground()
                    .onMousePressed((x, y, button) -> {
                        sort();
                        return true;
                    });
            this.settingsButton.size(10)
                    // .background(ButtonHandler.BUTTON_BACKGROUND)
                    // .overlay(ButtonHandler.BUTTON_SETTINGS)
                    .hoverOverlay(HOVER_SETTINGS_OVERLAY)
                    .disableHoverBackground()
                    .onMousePressed((x, y, button) -> {
                        // IBogoSortAPI.getInstance().openConfigGui();
                        return true;
                    });
        }
    }

    public void sort() {
        SlotGroup slotGroup = findFirstSlotGroup();
        if (slotGroup != null) {
            Slot slot = slotGroup.getFirstSlotForSorting();
            if (slot != null) {
                // IBogoSortAPI.getInstance().sortSlotGroup(slot);
            }
        }
    }

    public SlotGroup findFirstSlotGroup() {
        if (!isValid()) return null;
        if (this.slotGroup != null) return this.slotGroup;
        SlotGroupWidget sgw = findSlotGroupParent();
        for (IWidget child : sgw.getChildren()) {
            if (child instanceof ItemSlot itemSlot) {
                SlotGroup sg = itemSlot.getSlot().getSlotGroup();
                if (sg != null && sg.isAllowSorting()) return sg;
            }
        }
        return null;
    }

    public SlotGroupWidget findSlotGroupParent() {
        SlotGroupWidget sgw = WidgetTree.findParent(this, SlotGroupWidget.class);
        if (sgw == null) {
            throw new IllegalArgumentException(
                    "If the sort buttons don't have a SlotGroupWidget above itself in the widget tree, then it needs a slot group or name specified. Both were not found.");
        }
        return sgw;
    }

    @Override
    public void onInit() {
        super.onInit();
        if (this.slotGroup != null || this.slotGroupName != null) {
            this.slotGroup = getScreen().getContainer().validateSlotGroup(getPanel().getName(), this.slotGroupName,
                    this.slotGroup);
        }
        if (this.horizontal) {
            size(20, 10);
            this.settingsButton.pos(10, 0);
        } else {
            size(10, 20);
            this.settingsButton.pos(0, 10);
        }
    }

    @Override
    public void beforeResize(boolean onOpen) {
        super.beforeResize(onOpen);
        // we need to do this after init to make sure the slots are also initialized
        if (findFirstSlotGroup() == null) {
            // silently hide buttons if no slot group is found
            setEnabled(false);
        }
    }

    @Override
    public boolean isEnabled() {
        // TODO bogosort doesn't exist (yet), pick some other sorting mod to add compat for
        return false; // return super.isEnabled() && false; ModularUI.isSortModLoaded();
    }

    public SortButtons slotGroup(String slotGroupName) {
        this.slotGroupName = slotGroupName;
        return this;
    }

    public SortButtons slotGroup(SlotGroup slotGroup) {
        this.slotGroup = slotGroup;
        return this;
    }

    public SortButtons horizontal() {
        this.horizontal = true;
        return this;
    }

    public SortButtons vertical() {
        this.horizontal = false;
        return this;
    }
}
