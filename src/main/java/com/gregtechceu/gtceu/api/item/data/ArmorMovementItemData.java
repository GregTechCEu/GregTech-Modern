package com.gregtechceu.gtceu.api.item.data;

import net.minecraft.world.item.ItemStack;

/** Storage for suit movement flags and timers. Writes merge into current data after any energy use. */
public final class ArmorMovementItemData {

    private ArmorMovementItemData() {}

    public static boolean isStepAssistEnabled(ItemStack stack) {
        return ItemStackData.read(stack).getBooleanOr("stepAssist", false);
    }

    /** The legacy movement handler enables absent settings, unlike the toggle and tooltip readers. */
    public static boolean shouldApplyStepAssist(ItemStack stack) {
        var tag = ItemStackData.read(stack);
        return !tag.contains("stepAssist") || tag.getBooleanOr("stepAssist", false);
    }

    public static void setStepAssist(ItemStack stack, boolean enabled) {
        ItemStackData.update(stack, tag -> tag.putBoolean("stepAssist", enabled));
    }

    public static byte getStepToggleTimer(ItemStack stack) {
        return ItemStackData.read(stack).getByteOr("toggleStepTimer", (byte) 0);
    }

    public static void setStepToggleTimer(ItemStack stack, byte timer) {
        ItemStackData.update(stack, tag -> tag.putInt("toggleStepTimer", timer));
    }

    private static byte readTimer(ItemStack stack, String key, byte absentDefault) {
        var tag = ItemStackData.read(stack);
        return tag.contains(key) ? tag.getByteOr(key, (byte) 0) : absentDefault;
    }

    public static byte getRunningTimer(ItemStack stack, byte absentDefault) {
        return readTimer(stack, "runningTimer", absentDefault);
    }

    public static void setRunningTimer(ItemStack stack, byte timer) {
        ItemStackData.update(stack, tag -> tag.putByte("runningTimer", timer));
    }

    public static byte getBootsToggleTimer(ItemStack stack, byte absentDefault) {
        return readTimer(stack, "toggleBootsTimer", absentDefault);
    }

    public static void setBootsToggleTimer(ItemStack stack, byte timer) {
        ItemStackData.update(stack, tag -> tag.putInt("toggleBootsTimer", timer));
    }

    public static boolean isBoostedJumpEnabled(ItemStack stack) {
        return ItemStackData.read(stack).getBooleanOr("boostedJump", false);
    }

    public static void setBoostedJump(ItemStack stack, boolean enabled) {
        ItemStackData.update(stack, tag -> tag.putBoolean("boostedJump", enabled));
    }

    public static boolean wasOnGround(ItemStack stack) {
        var tag = ItemStackData.read(stack);
        return !tag.contains("onGround") || tag.getBooleanOr("onGround", false);
    }

    public static void setOnGround(ItemStack stack, boolean onGround) {
        ItemStackData.update(stack, tag -> tag.putBoolean("onGround", onGround));
    }
}
