package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.compat.FeCompat;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.GTUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public interface IElectricItem {

    /**
     * Determines if item can provide external discharging capability "in general"
     * it ensures it can be inserted into battery discharger slots & so
     *
     * @return true if item can be discharged externally
     */
    boolean canProvideChargeExternally();

    boolean chargeable();

    /**
     * Charge an item with a specified amount of energy.
     *
     * @param amount              max amount of energy to charge in EU
     * @param chargerTier         tier of the charging device, has to be at least as high as the item to charge
     * @param ignoreTransferLimit ignore any transfer limits, infinite charge rate
     * @param simulate            don't actually change the item, just determine the return value
     * @return Energy transferred into the electric item
     */
    long charge(long amount, int chargerTier, boolean ignoreTransferLimit, boolean simulate);

    /**
     * Discharge an item by a specified amount of energy
     * <p>
     * The externally parameter is used to prevent non-battery-like items from providing power. For
     * example discharge slots set externally to true, but items using energy for themselves don't.
     * Special cases like the nano saber hitting armor will discharge with externally = false.
     *
     * @param amount              max amount of energy to discharge in EU
     * @param dischargerTier      tier of the discharging device, has to be at least as high as the item to discharge
     * @param ignoreTransferLimit ignore any transfer limits, infinite discharge rate
     * @param externally          use the supplied item externally, i.e. to power something else as if it was a battery
     * @param simulate            don't actually discharge the item, just determine the return value
     * @return Energy retrieved from the electric item
     */
    long discharge(long amount, int dischargerTier, boolean ignoreTransferLimit, boolean externally, boolean simulate);

    /**
     * Determine the transfer limit for the specified item
     *
     * @return maximum transfer rate item can handle in EU/t
     */
    long getTransferLimit();

    /**
     * Determine the charge level for the specified item.
     * <p>
     * The item may not actually be chargeable to the returned level, e.g. if it is a
     * non-rechargeable single use battery.
     *
     * @return maximum charge level in EU
     */
    long getMaxCharge();

    /**
     * Determine the current charge for the specified item
     *
     * @return current charge level in EU
     */
    long getCharge();

    /**
     * Determine if the specified electric item has at least a specific amount of EU.
     *
     * @param amount minimum amount of energy required
     * @return true if there's enough energy
     */
    default boolean canUse(long amount) {
        return discharge(amount, Integer.MAX_VALUE, true, false, true) == amount;
    }

    /**
     * Get the tier of the specified item.
     *
     * @return The tier of the item.
     */
    int getTier();

    // Helper methods for interacting with electric items.

    default InteractionResultHolder<ItemStack> use(ItemStack itemStack, Level level, Player player,
                                                   InteractionHand usedHand) {
        if (canProvideChargeExternally() && player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                boolean isInDischargeMode = isInDischargeMode(itemStack);
                String locale = "metaitem.electric.discharge_mode." + (isInDischargeMode ? "disabled" : "enabled");
                player.displayClientMessage(Component.translatable(locale), true);
                setInDischargeMode(itemStack, !isInDischargeMode);
            }
            return InteractionResultHolder.success(itemStack);
        }
        return InteractionResultHolder.pass(itemStack);
    }

    private static boolean isInDischargeMode(ItemStack itemStack) {
        var tagCompound = itemStack.getTag();
        return tagCompound != null && tagCompound.getBoolean("DischargeMode");
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


    default void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && entity instanceof Player player &&
                canProvideChargeExternally() &&
                isInDischargeMode(stack) && getCharge() > 0L) {
            long transferLimit = getTransferLimit();

            if (GTCEu.Mods.isCuriosLoaded()) {
                IItemHandler curios = CuriosApi.getCuriosInventory(player)
                        .<IItemHandler>map(ICuriosItemHandler::getEquippedCurios)
                        .orElse(EmptyHandler.INSTANCE);
                for (int i = 0; i < curios.getSlots(); i++) {
                    var itemInSlot = curios.getStackInSlot(i);
                    long chargedAmount = chargeItemStack(transferLimit, this, itemInSlot);
                    if (chargedAmount > 0L) {
                        transferLimit -= chargedAmount;
                    }
                    if (transferLimit == 0L) break;
                }
            }

            var inventoryPlayer = player.getInventory();
            for (int i = 0; i < inventoryPlayer.getContainerSize(); i++) {
                var itemInSlot = inventoryPlayer.getItem(i);
                long chargedAmount = chargeItemStack(transferLimit, this, itemInSlot);
                if (chargedAmount > 0L) {
                    transferLimit -= chargedAmount;
                }
                if (transferLimit == 0L) break;
            }
        }
    }

    default void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                 TooltipFlag isAdvanced) {
        IElectricItem.addCurrentChargeTooltip(tooltipComponents, getCharge(), getMaxCharge(),
                getTier(), canProvideChargeExternally());
        if (canProvideChargeExternally()) {
            tooltipComponents.add(Component.translatable("metaitem.electric.discharge_mode.tooltip"));
        }
    }

    static long chargeElectricItem(ItemStack stack, long maxDischargeAmount, IElectricItem source,
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

    static long chargeItemStack(long maxDischargeAmount, IElectricItem source, ItemStack target) {
        var slotElectricItem = GTCapabilityHelper.getElectricItem(target);
        if (slotElectricItem != null && !slotElectricItem.canProvideChargeExternally()) {
            return chargeElectricItem(target, maxDischargeAmount, source, slotElectricItem);
        } else if (ConfigHolder.INSTANCE.compat.energy.nativeEUToFE) {
            var feEnergyItem = GTCapabilityHelper.getForgeEnergyItem(target);
            if (feEnergyItem != null && feEnergyItem.canReceive() &&
                    feEnergyItem.getEnergyStored() < feEnergyItem.getMaxEnergyStored()) {
                return chargeForgeEnergyItem(maxDischargeAmount, source, feEnergyItem);
            }
        }
        return 0;
    }

    static long chargeForgeEnergyItem(long maxDischargeAmount, IElectricItem source, IEnergyStorage target) {
        long maxDischarged = source.discharge(maxDischargeAmount, source.getTier(), false, true, true);
        long received = FeCompat.insertEu(target, maxDischarged, false);
        if (received > 0L) {
            source.discharge(received, source.getTier(), false, true, false);
            return received;
        }
        return 0L;
    }

    static void addCurrentChargeTooltip(List<Component> tooltip, long currentCharge, long maxCharge, int tier,
                                        boolean showTimeRemaining) {
        double percentage = (double) currentCharge / (double) maxCharge;

        Instant start = Instant.now();
        Instant current = Instant.now().plusSeconds(GTMath.clamp((long) ((currentCharge * 1.0) / GTValues.V[tier] / 20),
                0L, Instant.MAX.getEpochSecond() - start.getEpochSecond()));
        Instant max = Instant.now().plusSeconds((long) ((maxCharge * 1.0) / GTValues.V[tier] / 20));
        Duration durationCurrent = Duration.between(start, current);
        Duration durationMax = Duration.between(start, max);
        long currentChargeTime;
        long maxChargeTime;
        Component unit;

        ChatFormatting color = ChatFormatting.RED;
        if (percentage > 0.5) {
            color = ChatFormatting.GREEN;
        } else if (percentage > 0.3) {
            color = ChatFormatting.YELLOW;
        }

        if (showTimeRemaining) {
            if (durationCurrent.getSeconds() <= 60) {
                maxChargeTime = durationMax.getSeconds();
                currentChargeTime = durationCurrent.toSeconds();
                unit = Component.translatable("item.gtceu.battery.charge_unit.second");
            } else if (durationCurrent.toMinutes() <= 60) {
                maxChargeTime = durationMax.toMinutes();
                currentChargeTime = durationCurrent.toMinutes();
                unit = Component.translatable("item.gtceu.battery.charge_unit.minute");
            } else {
                maxChargeTime = durationMax.toHours();
                currentChargeTime = durationCurrent.toHours();
                unit = Component.translatable("item.gtceu.battery.charge_unit.hour");
            }
            tooltip.add(Component.translatable("item.gtceu.battery.charge_detailed",
                    FormattingUtil.formatNumbers(currentCharge), FormattingUtil.formatNumbers(maxCharge),
                    GTValues.VNF[tier],
                    FormattingUtil.formatNumbers(currentChargeTime), FormattingUtil.formatNumbers(maxChargeTime),
                    unit)
                    .withStyle(color));
        } else {
            tooltip.add(Component.translatable("metaitem.generic.electric_item.tooltip",
                    FormattingUtil.formatNumbers(currentCharge), FormattingUtil.formatNumbers(maxCharge),
                    GTValues.VNF[tier]).withStyle(color));
        }
    }
}
