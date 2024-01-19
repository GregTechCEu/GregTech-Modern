package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.capability.ElectricItem;
import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class ElectricStats implements IInteractionItem, ISubItemHandler, IAddInformation, IItemLifeCycle, IDurabilityBar, IComponentCapability {

    public static final ElectricStats EMPTY = ElectricStats.create(0, 0, false, false);

    public final long maxCharge;
    public final int tier;

    public final boolean chargeable;
    public final boolean dischargeable;

    protected ElectricStats(long maxCharge, long tier, boolean chargeable, boolean dischargeable) {
        this.maxCharge = maxCharge;
        this.tier = (int) tier;
        this.chargeable = chargeable;
        this.dischargeable = dischargeable;
    }

    public static ElectricStats create(long maxCharge, long tier, boolean chargeable, boolean dischargeable) {
        return new ElectricStats(maxCharge, tier, chargeable, dischargeable);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(ItemStack itemStack, @NotNull Capability<T> capability) {
        if (capability == GTCapability.CAPABILITY_ELECTRIC_ITEM) {
            return GTCapability.CAPABILITY_ELECTRIC_ITEM.orEmpty(capability, LazyOptional.of(() -> new ElectricItem(itemStack, maxCharge, tier, chargeable, dischargeable)));
        }
        return LazyOptional.empty();
    }

    public static float getStoredPredicate(ItemStack itemStack) {
        var electricItem = GTCapabilityHelper.getElectricItem(itemStack);
        if (electricItem != null) {
            var per = (electricItem.getCharge() * 7 / electricItem.getMaxCharge());
            return per / 100f;
        }
        return 0;
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        var itemStack = player.getItemInHand(usedHand);
        var electricItem = GTCapabilityHelper.getElectricItem(itemStack);
        if (electricItem != null && electricItem.canProvideChargeExternally() && player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                boolean isInDischargeMode = isInDischargeMode(itemStack);
                String locale = "metaitem.electric.discharge_mode." + (isInDischargeMode ? "disabled" : "enabled");
                player.sendSystemMessage(Component.translatable(locale));
                setInDischargeMode(itemStack, !isInDischargeMode);
            }
            return InteractionResultHolder.success(itemStack);
        }
        return IInteractionItem.super.use(item, level, player, usedHand);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        var electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (!level.isClientSide && entity instanceof Player player && electricItem != null &&
                electricItem.canProvideChargeExternally() &&
                isInDischargeMode(stack) && electricItem.getCharge() > 0L) {
            var inventoryPlayer = player.getInventory();
            long transferLimit = electricItem.getTransferLimit();

            for (int i = 0; i < inventoryPlayer.getContainerSize(); i++) {
                var itemInSlot = inventoryPlayer.getItem(i);
                var slotElectricItem = GTCapabilityHelper.getElectricItem(itemInSlot);
                if (slotElectricItem != null && !slotElectricItem.canProvideChargeExternally()) {
                    long chargedAmount = chargeElectricItem(transferLimit, electricItem, slotElectricItem);
                    if (chargedAmount > 0L) {
                        transferLimit -= chargedAmount;
                        if (transferLimit == 0L) break;
                    }
                }
            }
        }
    }

    private static long chargeElectricItem(long maxDischargeAmount, IElectricItem source, IElectricItem target) {
        long maxDischarged = source.discharge(maxDischargeAmount, source.getTier(), false, false, true);
        long maxReceived = target.charge(maxDischarged, source.getTier(), false, true);
        if (maxReceived > 0L) {
            long resultDischarged = source.discharge(maxReceived, source.getTier(), false, true, false);
            target.charge(resultDischarged, source.getTier(), false, false);
            return resultDischarged;
        }
        return 0L;
    }

    private static void setInDischargeMode(ItemStack itemStack, boolean isDischargeMode) {
        var tagCompound = itemStack.getOrCreateTag();
        if (isDischargeMode) {
            tagCompound.putBoolean("DischargeMode", true);
        } else {
            tagCompound.remove("DischargeMode");
            if (tagCompound.isEmpty()) {
                itemStack.setTag(null);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem != null && electricItem.canProvideChargeExternally()) {
            addTotalChargeTooltip(tooltipComponents, electricItem.getMaxCharge(), electricItem.getTier());
            tooltipComponents.add(Component.translatable("metaitem.electric.discharge_mode.tooltip"));
        }
    }

    private static void addTotalChargeTooltip(List<Component> tooltip, long maxCharge, int tier) {
        Instant start = Instant.now();
        Instant end = Instant.now().plusSeconds((long) ((maxCharge * 1.0) / GTValues.V[tier] / 20));
        Duration duration = Duration.between(start, end);

        long chargeTime;
        String unit;
        if (duration.getSeconds() <= 180) {
            chargeTime = duration.getSeconds();
            unit = LocalizationUtils.format("item.gtceu.battery.charge_unit.second");
        } else if (duration.toMinutes() <= 180) {
            chargeTime = duration.toMinutes();
            unit = LocalizationUtils.format("item.gtceu.battery.charge_unit.minute");
        } else {
            chargeTime = duration.toHours();
            unit = LocalizationUtils.format("item.gtceu.battery.charge_unit.hour");
        }
        tooltip.add(Component.translatable("item.gtceu.battery.charge_time", chargeTime, unit, GTValues.VNF[tier]));
    }

    private static boolean isInDischargeMode(ItemStack itemStack) {
        var tagCompound = itemStack.getTag();
        return tagCompound != null && tagCompound.getBoolean("DischargeMode");
    }

    @Override
    public void fillItemCategory(ComponentItem item, CreativeModeTab category, NonNullList<ItemStack> items) {
        items.add(new ItemStack(item));
        var stack = new ItemStack(item);
        var electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem != null) {
            electricItem.charge(electricItem.getMaxCharge(), electricItem.getTier(), true, false);
            items.add(stack);
        }
    }

    public static ElectricStats createElectricItem(long maxCharge, long tier) {
        return ElectricStats.create(maxCharge, tier, true, false);
    }

    public static ElectricStats createRechargeableBattery(long maxCharge, int tier) {
        return ElectricStats.create(maxCharge, tier, true, true);
    }

    public static ElectricStats createBattery(long maxCharge, int tier, boolean rechargeable) {
        return ElectricStats.create(maxCharge, tier, rechargeable, true);
    }

    @Override
    public float getDurabilityForDisplay(ItemStack stack) {
        var electricItem = GTCapabilityHelper.getElectricItem(stack);
        return electricItem == null ? 1f : electricItem.getCharge() * 1f / electricItem.getMaxCharge();
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        var electricItem = GTCapabilityHelper.getElectricItem(stack);
        return electricItem != null && electricItem.getCharge() < electricItem.getMaxCharge();
    }
}
