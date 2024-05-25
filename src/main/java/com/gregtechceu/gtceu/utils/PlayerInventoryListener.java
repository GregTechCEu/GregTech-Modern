package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IHazardEffectTracker;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.item.TagPrefixItem;
import com.gregtechceu.gtceu.config.ConfigHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PlayerInventoryListener implements ContainerListener {
    public final Player player;

    public PlayerInventoryListener(Player p) {
        player = p;
    }

    @Override
    public void slotChanged(AbstractContainerMenu containerToSend, int dataSlotIndex, ItemStack stack) {
        /*
        Slot slot = containerToSend.getSlot(dataSlotIndex);
        UnificationEntry entry = null;
        if (ConfigHolder.INSTANCE.gameplay.universalHazards) {
            entry = ChemicalHelper.getUnificationEntry(stack.getItem());
        } else if (stack.getItem() instanceof TagPrefixItem prefixItem) {
            entry = new UnificationEntry(prefixItem.tagPrefix, prefixItem.material);
        }
        if (slot.container != player.getInventory() || entry == null || entry.material == null) {
            return;
        }
        if (!entry.material.hasProperty(PropertyKey.HAZARD)) {
            return;
        }
        IHazardEffectTracker tracker = GTCapabilityHelper.getHazardEffectTracker(player);
        if (tracker == null) {
            return;
        }
        if (stack.isEmpty()) {
            tracker.dropHazardItem(slot.getItem(), entry.material.getProperty(PropertyKey.HAZARD));
        } else {
            tracker.pickupHazardItem(player, stack, entry.material.getProperty(PropertyKey.HAZARD));
        }
        */
    }

    @Override
    public void dataChanged(AbstractContainerMenu containerMenu, int dataSlotIndex, int value) {

    }
}
