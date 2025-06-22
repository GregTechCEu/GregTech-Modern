package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.capability.compat.FeCompat;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.item.capability.ElectricItem;
import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import com.gregtechceu.gtceu.client.renderer.item.ToolChargeBarRenderer;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
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
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class ElectricStats implements IInteractionItem, ISubItemHandler, IAddInformation, IItemLifeCycle,
                           IComponentCapability, IItemDecoratorComponent {

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
    public @NotNull <T> LazyOptional<T> getCapability(ItemStack itemStack, @NotNull Capability<T> capability) {
        return GTCapability.CAPABILITY_ELECTRIC_ITEM.orEmpty(capability,
                LazyOptional.of(() -> new ElectricItem(itemStack, maxCharge, tier, chargeable, dischargeable)));
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
            long transferLimit = electricItem.getTransferLimit();

            if (GTCEu.Mods.isCuriosLoaded()) {
                IItemHandler curios = CuriosApi.getCuriosInventory(player)
                        .<IItemHandler>map(ICuriosItemHandler::getEquippedCurios)
                        .orElse(EmptyHandler.INSTANCE);
                for (int i = 0; i < curios.getSlots(); i++) {
                    var itemInSlot = curios.getStackInSlot(i);
                    long chargedAmount = chargeItemStack(transferLimit, electricItem, itemInSlot);
                    if (chargedAmount > 0L) {
                        transferLimit -= chargedAmount;
                    }
                    if (transferLimit == 0L) break;
                }
            }

            var inventoryPlayer = player.getInventory();
            for (int i = 0; i < inventoryPlayer.getContainerSize(); i++) {
                var itemInSlot = inventoryPlayer.getItem(i);
                long chargedAmount = chargeItemStack(transferLimit, electricItem, itemInSlot);
                if (chargedAmount > 0L) {
                    transferLimit -= chargedAmount;
                }
                if (transferLimit == 0L) break;
            }
        }
    }

    private static long chargeItemStack(long maxDischargeAmount, IElectricItem source, ItemStack target) {
        var slotElectricItem = GTCapabilityHelper.getElectricItem(target);
        if (slotElectricItem != null && !slotElectricItem.canProvideChargeExternally()) {
            return chargeElectricItem(maxDischargeAmount, source, slotElectricItem);
        } else if (ConfigHolder.INSTANCE.compat.energy.nativeEUToFE) {
            var feEnergyItem = GTCapabilityHelper.getForgeEnergyItem(target);
            if (feEnergyItem != null && feEnergyItem.canReceive() &&
                    feEnergyItem.getEnergyStored() < feEnergyItem.getMaxEnergyStored()) {
                return chargeForgeEnergyItem(maxDischargeAmount, source, feEnergyItem);
            }
        }
        return 0;
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

    private static long chargeForgeEnergyItem(long maxDischargeAmount, IElectricItem source, IEnergyStorage target) {
        long maxDischarged = source.discharge(maxDischargeAmount, source.getTier(), false, true, true);
        long received = FeCompat.insertEu(target, maxDischarged, false);
        if (received > 0L) {
            source.discharge(received, source.getTier(), false, true, false);
            return received;
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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem == null) return;
        addCurrentChargeTooltip(tooltipComponents, electricItem.getCharge(), electricItem.getMaxCharge(),
                electricItem.getTier(), electricItem.canProvideChargeExternally());
        if (electricItem.canProvideChargeExternally()) {
            tooltipComponents.add(Component.translatable("metaitem.electric.discharge_mode.tooltip"));
        }
    }

    public static void addCurrentChargeTooltip(List<Component> tooltip, long currentCharge, long maxCharge, int tier,
                                               boolean showTimeRemaining) {
        double percentage = (double) currentCharge / (double) maxCharge;

        Instant start = Instant.now();
        Instant current = Instant.now().plusSeconds((long) ((currentCharge * 1.0) / GTValues.V[tier] / 20));
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

    private static boolean isInDischargeMode(ItemStack itemStack) {
        var tagCompound = itemStack.getTag();
        return tagCompound != null && tagCompound.getBoolean("DischargeMode");
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
        return ElectricStats.create(maxCharge, tier, true, false);
    }

    public static ElectricStats createRechargeableBattery(long maxCharge, int tier) {
        return ElectricStats.create(maxCharge, tier, true, true);
    }

    public static ElectricStats createBattery(long maxCharge, int tier, boolean rechargeable) {
        return ElectricStats.create(maxCharge, tier, rechargeable, true);
    }

    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        var electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem != null) {
            return ToolChargeBarRenderer.renderElectricBar(guiGraphics, electricItem.getCharge(),
                    electricItem.getMaxCharge(), xOffset, yOffset, stack.isBarVisible());
        }
        return false;
    }
}
