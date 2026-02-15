package com.gregtechceu.gtceu.common.machine.multiblock.fission;

import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class FuelRodItem extends Item {

    public FuelRodItem(Properties properties) {
        super(properties.stacksTo(64));
    }

    public static void setLifetimeData(ItemStack stack, int ticksAlive, int maxLifetimeTicks) {
        stack.getOrCreateTag().putInt("fuelTicks", ticksAlive);
        stack.getOrCreateTag().putInt("fuelMaxTicks", maxLifetimeTicks);
    }

    public static int getTicksAlive(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getInt("fuelTicks") : 0;
    }

    public static int getMaxLifetimeTicks(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getInt("fuelMaxTicks") : 0;
    }

    public static boolean hasLifetimeData(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains("fuelMaxTicks") &&
                stack.getTag().getInt("fuelMaxTicks") > 0;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return hasLifetimeData(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int max = getMaxLifetimeTicks(stack);
        if (max <= 0) return 13;
        float remaining = 1.0f - (float) getTicksAlive(stack) / max;
        return Math.round(remaining * 13.0f);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        int max = getMaxLifetimeTicks(stack);
        if (max <= 0) return 0xFF00FF00;
        float remaining = 1.0f - (float) getTicksAlive(stack) / max;
        return Mth.hsvToRgb(remaining / 3.0f, 1.0f, 1.0f);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return hasLifetimeData(stack) ? 1 : 64;
    }
}
