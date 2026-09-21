package com.gregtechceu.gtceu.api.item.data;

import net.minecraft.nbt.LongTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

/** Storage semantics shared by electric tools, armor and batteries; energy transfer policy stays with IElectricItem. */
public final class ElectricItemData {

    private ElectricItemData() {}

    public static void setCharge(ItemStack stack, long charge) {
        ItemStackData.update(stack, tag -> tag.putLong("Charge", charge));
    }

    public static void setMaxCharge(ItemStack stack, long maxCharge) {
        ItemStackData.update(stack, tag -> tag.putLong("MaxCharge", maxCharge));
    }

    public static long getMaxCharge(ItemStack stack, long defaultMaxCharge) {
        var tag = ItemStackData.read(stack);
        return tag.get("MaxCharge") instanceof LongTag value ? value.longValue() : defaultMaxCharge;
    }

    public static long getCharge(ItemStack stack, long maxCharge) {
        if (!stack.has(DataComponents.CUSTOM_DATA)) return 0;
        var tag = ItemStackData.read(stack);
        return tag.getBooleanOr("Infinite", false) ? maxCharge : Math.min(tag.getLongOr("Charge", 0), maxCharge);
    }

    public static void setInfinite(ItemStack stack, boolean infinite) {
        ItemStackData.update(stack, tag -> tag.putBoolean("Infinite", infinite));
    }

    public static boolean isDischargeMode(ItemStack stack) {
        return ItemStackData.read(stack).getBooleanOr("DischargeMode", false);
    }

    public static void setDischargeMode(ItemStack stack, boolean enabled) {
        ItemStackData.update(stack, tag -> {
            if (enabled) tag.putBoolean("DischargeMode", true);
            else tag.remove("DischargeMode");
        });
    }

    public static boolean isActive(ItemStack stack) {
        return ItemStackData.read(stack).getBooleanOr("Active", false);
    }

    public static void setActive(ItemStack stack, boolean active) {
        ItemStackData.update(stack, tag -> tag.putBoolean("Active", active));
    }
}
