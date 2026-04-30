package com.lowdragmc.lowdraglib.gui.modular;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;

public class ModularUIContainer extends AbstractContainerMenu implements WidgetUIAccess {

    public static final MenuType<ModularUIContainer> MENUTYPE = null;

    private final ModularUI modularUI;

    public ModularUIContainer(ModularUI modularUI, int containerId) {
        super(null, containerId);
        this.modularUI = modularUI;
    }

    @Override
    public Slot addSlot(Slot slot) {
        return super.addSlot(slot);
    }

    public void removeSlot(Slot slot) {
        slots.remove(slot);
    }

    @Override
    public boolean stillValid(Player player) {
        return modularUI.holder == null || modularUI.holder.isStillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    public void clicked(int slotId, int button, ContainerInput clickType, Player player) {
        // Old LDLib exposed the pre-1.21 click signature. NeoForge now routes
        // through ContainerInput, so this shim keeps old callers source-compatible.
    }

    @Override
    public boolean attemptMergeStack(ItemStack stack, boolean fromContainer, boolean simulate) {
        return mergeItemStack(stack, slots, fromContainer);
    }

    public static boolean mergeItemStack(ItemStack stack, List<Slot> slots, boolean reverse) {
        if (stack.isEmpty()) return false;
        int start = reverse ? slots.size() - 1 : 0;
        int end = reverse ? -1 : slots.size();
        int step = reverse ? -1 : 1;
        for (int i = start; i != end && !stack.isEmpty(); i += step) {
            Slot slot = slots.get(i);
            if (!slot.mayPlace(stack)) continue;
            ItemStack inSlot = slot.getItem();
            if (inSlot.isEmpty()) {
                slot.set(stack.copy());
                stack.setCount(0);
                return true;
            }
        }
        return false;
    }

    @Override
    public void writeClientAction(Widget widget, int id, Consumer<RegistryFriendlyByteBuf> packetBuffer) {}

    @Override
    public void writeUpdateInfo(Widget widget, int id, Consumer<RegistryFriendlyByteBuf> packetBuffer) {}

    public ModularUI getModularUI() {
        return modularUI;
    }
}
