package com.gregtechceu.gtceu.api.item.data;

import net.minecraft.world.item.ItemStack;

/** A snapshot of night-vision state; saving merges only these fields into the current item data. */
public record NightVisionItemData(boolean enabled, byte toggleTimer, int effectTimer) {

    public static NightVisionItemData read(ItemStack stack, int defaultDuration) {
        var tag = ItemStackData.read(stack);
        return new NightVisionItemData(tag.getBooleanOr("nightVision", false), tag.getByteOr("toggleTimer", (byte) 0),
                tag.contains("nightVisionTimer") ? tag.getIntOr("nightVisionTimer", 0) : defaultDuration);
    }

    public void save(ItemStack stack) {
        save(stack, true);
    }

    /** Non-helmet consumers historically update the timers without changing the enabled field. */
    public void save(ItemStack stack, boolean saveEnabled) {
        ItemStackData.update(stack, tag -> {
            if (saveEnabled) tag.putBoolean("nightVision", enabled);
            tag.putByte("toggleTimer", toggleTimer);
            tag.putInt("nightVisionTimer", effectTimer);
        });
    }
}
