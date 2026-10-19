package com.gregtechceu.gtceu.utils.lang;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public final class GTCommonComponents {

    private GTCommonComponents() {}

    public static final Component MULTI_SHARING_ENABLED = Component.translatable("multiblock.gtceu.part_sharing.enabled");
    public static final Component MULTI_SHARING_DISABLED = Component.translatable("multiblock.gtceu.part_sharing.disabled");

    // Energy

    public static MutableComponent voltage(IO io, int tier) {
        var key = "common.gtceu.tooltip.voltage_" + io.localeName;

        return Component.translatable(key).withStyle(ChatFormatting.GREEN)
                .append(FormattingUtil.prepend("common.gtceu.eu_per_tick",
                        FormattingUtil.formatNumbers(GTValues.VEX[tier])))
                .append(FormattingUtil.wrap(GTValues.VNC[tier], " (", ")"));
    }

    public static MutableComponent maxVoltage(IO io, int tier) {
        var key = "common.gtceu.tooltip.max_voltage_" + io.localeName;

        return Component.translatable(key).withStyle(ChatFormatting.GREEN)
                .append(FormattingUtil.prepend("common.gtceu.eu_per_tick",
                        FormattingUtil.formatNumbers(GTValues.VEX[tier])))
                .append(FormattingUtil.wrap(GTValues.VNC[tier], " (", ")"));
    }

    public static MutableComponent energyCapacity(long eu) {
        return Component.translatable("common.gtceu.energy_capacity")
                .withStyle(ChatFormatting.RED)
                .append(FormattingUtil.prepend("common.gtceu.eu", FormattingUtil.formatNumbers(eu)));
    }

    /// Item Storage / Capacity

    public static MutableComponent itemSlots(int slots) {
        return Component.translatable("common.gtceu.item_slots")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.translatable(String.valueOf(slots)));
    }

    public static MutableComponent itemCapacity(long capacity) {
        return Component.translatable("common.gtceu.item_capacity")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.translatable(String.valueOf(capacity)));
    }

    public static MutableComponent itemStored(ItemStack stack) {
        return Component.translatable("common.gtceu.item_stored")
                .withStyle(ChatFormatting.LIGHT_PURPLE)
                .append(stack.getDisplayName())
                .append(", ")
                .append(Component.translatable("common.gtceu.tooltip.item_stored",
                        FormattingUtil.formatNumbers(stack.getCount())));
    }

    public static MutableComponent itemStored(Item item, long amount) {
        return Component.translatable("common.gtceu.item_stored")
                .withStyle(ChatFormatting.LIGHT_PURPLE)
                .append(item.getDefaultInstance().getDisplayName())
                .append(", ")
                .append(Component.translatable("common.gtceu.tooltip.item_stored",
                        FormattingUtil.formatNumbers(amount)));
    }

    /// Fluid Storage / Capacity

    public static MutableComponent fluidStorageCapacity(long capacityMillibuckets) {
        return fluidStorageCapacity(capacityMillibuckets, 1);
    }

    public static MutableComponent fluidStorageCapacity(long capacityMillibuckets, int tanks) {

        MutableComponent comp = Component.translatable("common.gtceu.fluid_amount").withStyle(ChatFormatting.BLUE);

        if (tanks == 1) {
            return comp.append(FormattingUtil.prepend("common.gtceu.millibucket",
                    FormattingUtil.formatNumbers(capacityMillibuckets)));
        } else {
            return comp.append(Component.translatable("common.gtceu.tooltip.fluid_storage_capacity_mult",
                    tanks, FormattingUtil.formatNumbers(capacityMillibuckets)));
        }
    }

    public static MutableComponent fluidStored(FluidStack stack) {
        return Component.translatable("common.gtceu.fluid_stored")
                .withStyle(ChatFormatting.DARK_GREEN)
                .append(stack.getDisplayName())
                .append(", ")
                .append(FormattingUtil.prepend("common.gtceu.millibucket",
                        FormattingUtil.formatNumbers(stack.getAmount())));
    }

    public static MutableComponent fluidStored(Fluid fluid, long amount) {
        return Component.translatable("common.gtceu.fluid_stored")
                .withStyle(ChatFormatting.DARK_GREEN)
                .append(new FluidStack(fluid, 1).getDisplayName())
                .append(", ")
                .append(FormattingUtil.prepend("common.gtceu.millibucket",
                        FormattingUtil.formatNumbers(amount)));
    }
}
