package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.item.capability.ElectricItem;
import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.minecraft.ChatFormatting;
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
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class ElectricStats implements IInteractionItem, ISubItemHandler, IAddInformation, IItemLifeCycle,
                           IComponentCapability {

    public static final ElectricStats EMPTY = ElectricStats.create(0, 0, false, false);

    public final long maxCharge;
    public final int tier;

    public final boolean chargeable;
    public final boolean dischargeable;

    protected ElectricStats(long maxCharge, int tier, boolean chargeable, boolean dischargeable) {
        this.maxCharge = maxCharge;
        this.tier = tier;
        this.chargeable = chargeable;
        this.dischargeable = dischargeable;
    }

    public static ElectricStats create(long maxCharge, int tier, boolean chargeable, boolean dischargeable) {
        return new ElectricStats(maxCharge, tier, chargeable, dischargeable);
    }

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event, Item item) {
        event.registerItem(GTCapability.CAPABILITY_ELECTRIC_ITEM, (stack, unused) -> createItem(stack), item);
    }

    public IElectricItem createItem(ItemStack stack) {
        return new ElectricItem(stack, maxCharge, tier, chargeable, dischargeable);
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
    public InteractionResultHolder<ItemStack> use(ItemStack item, Level level, Player player,
                                                  InteractionHand usedHand) {
        var itemStack = player.getItemInHand(usedHand);
        var electricItem = GTCapabilityHelper.getElectricItem(itemStack);
        if (electricItem != null && electricItem.canProvideChargeExternally() && player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                boolean isInDischargeMode = isInDischargeMode(itemStack);
                String locale = "metaitem.electric.discharge_mode." + (isInDischargeMode ? "disabled" : "enabled");
                player.displayClientMessage(Component.translatable(locale), true);
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
                    long chargedAmount = chargeElectricItem(itemInSlot, transferLimit, electricItem, slotElectricItem);
                    if (chargedAmount > 0L) {
                        transferLimit -= chargedAmount;
                        if (transferLimit == 0L) break;
                    }
                }
            }
        }
    }

    private static long chargeElectricItem(ItemStack stack, long maxDischargeAmount, IElectricItem source,
                                           IElectricItem target) {
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
        GTCapabilityHelper.getElectricItem(itemStack).setDischargeMode(isDischargeMode);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem != null && electricItem.canProvideChargeExternally()) {
            addCurrentChargeTooltip(tooltipComponents, electricItem.getCharge(), electricItem.getMaxCharge(),
                    electricItem.getTier());
            tooltipComponents.add(Component.translatable("metaitem.electric.discharge_mode.tooltip"));
        }
    }

    private static void addCurrentChargeTooltip(List<Component> tooltip, long currentCharge, long maxCharge, int tier) {
        double percentage = (double) currentCharge / (double) maxCharge;

        Instant start = Instant.now();
        Instant current = Instant.now().plusSeconds(Math.clamp((long) ((currentCharge * 1.0) / GTValues.V[tier] / 20),
                0L, Instant.MAX.getEpochSecond() - start.getEpochSecond()));
        Instant max = Instant.now().plusSeconds((long) ((maxCharge * 1.0) / GTValues.V[tier] / 20));
        Duration durationCurrent = Duration.between(start, current);
        Duration durationMax = Duration.between(start, max);
        long currentChargeTime;
        long maxChargeTime;
        String unit;
        if (durationCurrent.getSeconds() <= 180) {
            currentChargeTime = durationCurrent.getSeconds();
            unit = LocalizationUtils.format("item.gtceu.battery.charge_unit.second");
        } else if (durationCurrent.toMinutes() <= 180) {
            currentChargeTime = durationCurrent.toMinutes();
            unit = LocalizationUtils.format("item.gtceu.battery.charge_unit.minute");
        } else {
            currentChargeTime = durationCurrent.toHours();
            unit = LocalizationUtils.format("item.gtceu.battery.charge_unit.hour");
        }

        if (durationMax.getSeconds() <= 180) {
            maxChargeTime = durationMax.getSeconds();
        } else if (durationMax.toMinutes() <= 180) {
            maxChargeTime = durationMax.toMinutes();
        } else {
            maxChargeTime = durationMax.toHours();
        }

        if (percentage > 0.5) {
            tooltip.add(Component.translatable("item.gtceu.battery.charge_detailed.0",
                    FormattingUtil.formatNumbers(currentCharge), FormattingUtil.formatNumbers(maxCharge),
                    GTValues.VNF[tier],
                    FormattingUtil.formatNumbers(currentChargeTime), FormattingUtil.formatNumbers(maxChargeTime), unit)
                    .withStyle(ChatFormatting.GREEN));
        } else if (percentage > 0.3) {
            tooltip.add(Component.translatable("item.gtceu.battery.charge_detailed.1",
                    FormattingUtil.formatNumbers(currentCharge), FormattingUtil.formatNumbers(maxCharge),
                    GTValues.VNF[tier],
                    FormattingUtil.formatNumbers(currentChargeTime), FormattingUtil.formatNumbers(maxChargeTime), unit)
                    .withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(Component.translatable("item.gtceu.battery.charge_detailed.2",
                    FormattingUtil.formatNumbers(currentCharge), FormattingUtil.formatNumbers(maxCharge),
                    GTValues.VNF[tier],
                    FormattingUtil.formatNumbers(currentChargeTime), FormattingUtil.formatNumbers(maxChargeTime), unit)
                    .withStyle(ChatFormatting.RED));
        }
    }

    private static boolean isInDischargeMode(ItemStack itemStack) {
        var electric = GTCapabilityHelper.getElectricItem(itemStack);
        if (electric == null) {
            return false;
        }
        return electric.isDischargeMode();
    }

    @Override
    public void fillItemCategory(Item item, CreativeModeTab category, NonNullList<ItemStack> items) {
        items.add(new ItemStack(item));
        var stack = new ItemStack(item);
        var electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem != null) {
            electricItem.charge(electricItem.getMaxCharge(), electricItem.getTier(), true, false);
            items.add(stack);
        }
    }

    public static ElectricStats createElectricItem(long maxCharge, int tier) {
        return new ElectricStats(maxCharge, tier, true, false);
    }

    public static ElectricStats createRechargeableBattery(long maxCharge, int tier) {
        return new ElectricStats(maxCharge, tier, true, true);
    }

    public static ElectricStats createBattery(long maxCharge, int tier, boolean rechargeable) {
        return new ElectricStats(maxCharge, tier, rechargeable, true);
    }
}
