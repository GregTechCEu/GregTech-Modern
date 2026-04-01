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

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import static net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER;

public class TestItem extends Item implements ICurioItem, IUIHolder<PlayerInventoryGuiData<?>> {

    public TestItem(Properties properties) {
        super(properties);
        CuriosApi.registerCurio(this, this);
    }

    @Override
    public ModularPanel<?> buildUI(PlayerInventoryGuiData<?> data, PanelSyncManager syncManager, UISettings settings) {
        var cap = data.getUsedItemStack().getCapability(ITEM_HANDLER);
        if (!cap.isPresent() || cap.resolve().isEmpty()) return null;
        IItemHandler itemHandler = cap.resolve().get();
        syncManager.registerSlotGroup("mixer_items", 2);
        if (!(itemHandler instanceof IItemHandlerModifiable ihm)) return null;

        // if the player slot is the slot with this item, then disallow any interaction
        // if the item is not in the player inventory (bauble for example), then this items slot is not on the screen,
        // and we don't need to limit accessibility
        if (data.getInventoryType() == InventoryTypes.PLAYER) {
            syncManager.bindPlayerInventory(data.getPlayer(), (inv, index) -> index == data.getSlotIndex() ?
                    new ModularSlot(inv, index).accessibility(false, false) :
                    new ModularSlot(inv, index));
        }
        ModularPanel<?> panel = ModularPanel.defaultPanel("knapping_gui").resizeableOnDrag(true);
        panel.child(new Column().margin(7)
                .child(new ParentWidget<>().widthRel(1f).expanded()
                        .child(SlotGroupWidget.builder()
                                .row("I I")
                                .row("  I")
                                .row("   ")
                                .row(" I ")
                                .key('I', index -> new ItemSlot().slot(SyncHandlers.itemSlot(ihm, index)
                                        .ignoreMaxStackSize(true)
                                        .slotGroup("mixer_items")
                                        // do not allow putting items which can hold other items into the item
                                        // some mods don't do this on their backpacks, so it won't catch those cases
                                        .filter(stack -> !stack.getCapability(ITEM_HANDLER).isPresent())))
                                .build()
                                .align(Alignment.TopLeft)))
                .child(SlotGroupWidget.playerInventory(false)));

        return panel;
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {

            @Override
            public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                if (cap == ITEM_HANDLER) {
                    var handler = new ItemStackHandler(4);
                    return LazyOptional.of(() -> handler).cast();
                }
                return LazyOptional.empty();
            }
        };
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return true;
    }
}
