package com.gregtechceu.gtceu.api.mui.test;

import com.gregtechceu.gtceu.api.mui.base.IUIHolder;
import com.gregtechceu.gtceu.api.mui.factory.PlayerInventoryGuiData;
import com.gregtechceu.gtceu.api.mui.factory.inventory.InventoryTypes;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandlers;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Column;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandlerModifiable;

import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class TestItem extends Item implements ICurioItem, IUIHolder<PlayerInventoryGuiData<?>> {

    public TestItem(Properties properties) {
        super(properties);
        CuriosApi.registerCurio(this, this);
    }

    @Override
    public ModularPanel buildUI(PlayerInventoryGuiData<?> data, PanelSyncManager syncManager, UISettings settings) {
        IItemHandlerModifiable itemHandler = (IItemHandlerModifiable) data.getUsedItemStack()
                .getCapability(ForgeCapabilities.ITEM_HANDLER, null);
        syncManager.registerSlotGroup("mixer_items", 2);

        // if the player slot is the slot with this item, then disallow any interaction
        // if the item is not in the player inventory (bauble for example), then this items slot is not on the screen,
        // and we don't need to limit accessibility
        if (data.getInventoryType() == InventoryTypes.PLAYER) {
            syncManager.bindPlayerInventory(data.getPlayer(), (inv, index) -> index == data.getSlotIndex() ?
                    new ModularSlot(inv, index).accessibility(false, false) :
                    new ModularSlot(inv, index));
        }
        ModularPanel panel = ModularPanel.defaultPanel("knapping_gui").resizeableOnDrag(true);
        panel.child(new Column().margin(7)
                .child(new ParentWidget<>().widthRel(1f).expanded()
                        .child(SlotGroupWidget.builder()
                                .row("II")
                                .row("II")
                                .key('I', index -> new ItemSlot().slot(SyncHandlers.itemSlot(itemHandler, index)
                                        .ignoreMaxStackSize(true)
                                        .slotGroup("mixer_items")
                                        // do not allow putting items which can hold other items into the item
                                        // some mods don't do this on their backpacks, so it won't catch those cases
                                        .filter(stack -> !stack.getCapability(ForgeCapabilities.ITEM_HANDLER)
                                                .isPresent())))
                                .build()
                                .align(Alignment.Center)))
                .child(SlotGroupWidget.playerInventory(false)));

        return panel;
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return true;
    }
}
