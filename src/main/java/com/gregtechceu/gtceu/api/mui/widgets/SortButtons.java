package com.gregtechceu.gtceu.api.mui.widgets;

import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.api.mui.widgets.slot.SlotGroup;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class SortButtons extends Widget<SortButtons> {

    @Getter
    private String slotGroupName;
    @Getter
    private SlotGroup slotGroup;

    private boolean horizontal = true;
    private final ButtonWidget<?> sortButton = new ButtonWidget<>();
    private final ButtonWidget<?> settingsButton = new ButtonWidget<>();
    @Getter
    private final @NotNull List<IWidget> children = Arrays.asList(sortButton, settingsButton);

    @Override
    public void onInit() {
        super.onInit();
        this.slotGroup = getScreen().getContainer().validateSlotGroup(getPanel().getName(), this.slotGroupName,
                this.slotGroup);
        if (!this.slotGroup.isAllowSorting()) {
            throw new IllegalStateException("Slot group can't be sorted!");
        }
        /*
         * TODO bogosort doesn't exist (yet), choose some other sorting mod to add compat for?
         * this.sortButton.size(10).pos(0, 0)
         * .overlay(IKey.str("z"))
         * .onMousePressed(mouseButton -> {
         * IBogoSortAPI.getInstance().sortSlotGroup(this.slotGroup.getSlots().get(0));
         * return true;
         * });
         * this.settingsButton.size(10)
         * .overlay(IKey.str("..."))
         * .onMousePressed(mouseButton -> {
         * IBogoSortAPI.getInstance().openConfigGui();
         * return true;
         * });
         */
        if (this.horizontal) {
            size(20, 10);
            this.settingsButton.pos(10, 0);
        } else {
            size(10, 20);
            this.settingsButton.pos(0, 10);
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
