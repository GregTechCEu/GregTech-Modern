package brachy.modularui.widgets;

import brachy.modularui.ModularUI;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.UITexture;
import brachy.modularui.widget.Widget;
import brachy.modularui.widget.WidgetTree;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.SlotGroup;

import net.minecraft.world.inventory.Slot;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@Accessors(fluent = true, chain = true)
public class SortButtons extends Widget<SortButtons> {

    public static final UITexture HOVER_SORT_OVERLAY;
    public static final UITexture HOVER_SETTINGS_OVERLAY;

    static {
        // TODO bogosort doesn't exist (yet), pick some other sorting mod to add compat for
        if (ModularUI.isClientSide() && false /* ModularUI.Mods.BOGOSORTER.isLoaded() */) {
            // HOVER_SORT_OVERLAY = ButtonHandler.BUTTON_SORT.withColorOverride(0xFFFFA0);
            // HOVER_SETTINGS_OVERLAY = ButtonHandler.BUTTON_SETTINGS.withColorOverride(0xFFFFA0);
        } else {
            // any non null value
            HOVER_SORT_OVERLAY = GuiTextures.BLOCK;
            HOVER_SETTINGS_OVERLAY = GuiTextures.BLOCK;
        }
    }

    @Getter private String slotGroupName;
    @Getter @Setter private SlotGroup slotGroup;

    private boolean horizontal = true;
    private final ButtonWidget<?> sortButton = new ButtonWidget<>();
    private final ButtonWidget<?> settingsButton = new ButtonWidget<>();
    @Getter
    private final @NotNull List<IWidget> children = Arrays.asList(sortButton, settingsButton);

    public SortButtons() {
        // TODO bogosort doesn't exist (yet), pick some other sorting mod to add compat for
        if (ModularUI.isClientSide() && false /* && ModularUI.Mods.BOGOSORTER.isLoaded() */) {
            this.sortButton.size(10).pos(0, 0)
                    // .background(ButtonHandler.BUTTON_BACKGROUND)
                    // .overlay(ButtonHandler.BUTTON_SORT)
                    .hoverOverlay(HOVER_SORT_OVERLAY)
                    .disableHoverBackground()
                    .onMousePressed((context, button) -> {
                        sort();
                        return true;
                    });
            this.settingsButton.size(10)
                    // .background(ButtonHandler.BUTTON_BACKGROUND)
                    // .overlay(ButtonHandler.BUTTON_SETTINGS)
                    .hoverOverlay(HOVER_SETTINGS_OVERLAY)
                    .disableHoverBackground()
                    .onMousePressed((context, button) -> {
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
                if (sg != null && sg.allowsSorting()) return sg;
            }
        }
        return null;
    }

    public SlotGroupWidget findSlotGroupParent() {
        SlotGroupWidget parent = WidgetTree.findParent(this, SlotGroupWidget.class);
        if (parent == null) {
            throw new IllegalArgumentException(
                    "If the sort buttons don't have a SlotGroupWidget above itself in the widget tree, then it needs a slot group or name specified. Both were not found.");
        }
        return parent;
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

    @Tolerate
    public SortButtons slotGroup(String slotGroupName) {
        this.slotGroupName = slotGroupName;
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
