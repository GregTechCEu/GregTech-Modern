package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.module.*;
import com.gregtechceu.gtceu.common.data.GTItemModules;
import com.gregtechceu.gtceu.utils.GTStringUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.value.sync.PanelSyncManager;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BatteryItemModule extends ItemModule implements ICapabilityModule, IHUDProviderItemModule {

    private static final double PERCENTAGE = 80.0d;

    public static final Codec<BatteryItemModule> CODEC = simpleCodec(BatteryItemModule::new);

    public BatteryItemModule(boolean isEnabled, ItemStack moduleItem) {
        super(isEnabled, moduleItem);
    }

    public BatteryItemModule(ItemStack moduleItem) {
        super(moduleItem);
    }

    @Override
    public ItemModuleType<BatteryItemModule> type() {
        return GTItemModules.BATTERY;
    }

    @Override
    public Component getDisplayName() {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(getModuleItem());
        if (electricItem != null)
            return Component.translatable("metaarmor.tooltip.modifier.battery", GTValues.VNF[electricItem.getTier()]);
        else return super.getDisplayName();
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.battery");
    }

    @Override
    public void onInventoryTick(Player player) {
        super.onInventoryTick(player);
        if (getAppliedTo() == null || getModuleItem() == null) return;
        IElectricItem item = GTCapabilityHelper.getElectricItem(getAppliedTo());
        IElectricItem battery = GTCapabilityHelper.getElectricItem(getModuleItem());
        if (item == null || battery == null || item == battery) return;
        if (item.getCharge() > item.getMaxCharge() * PERCENTAGE / 100) {
            long amount = (long) (item.getCharge() - item.getMaxCharge() * PERCENTAGE / 100);
            item.charge(battery.discharge(amount, battery.getTier(), true, false, false), item.getTier(), true, false);
        } else if (item.getCharge() < item.getMaxCharge() * PERCENTAGE / 100) {
            long amount = (long) (item.getMaxCharge() * PERCENTAGE / 100 - item.getCharge());
            battery.charge(item.discharge(amount, item.getTier(), true, false, false), battery.getTier(), true, false);
        }
        // the battery's inventoryTick method does not use inventorySlot or isCurrentItem
        getModuleItem().inventoryTick(player.level(), player, 0, false);
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips) {
        super.appendHoverText(level, isAdvanced, tooltips);
        tooltips.add(
                Component.translatable("metaarmor.tooltip.modifier.battery", getModuleItem().getHoverName()));
        if (getModuleItem() != null) {
            getModuleItem().getItem().appendHoverText(getModuleItem(), level, tooltips, isAdvanced);
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        if (cap == GTCapability.CAPABILITY_ELECTRIC_ITEM && getModuleItem() != null) {
            return getModuleItem().getCapability(cap);
        } else return LazyOptional.empty();
    }

    @Override
    public void drawHUD(GuiGraphics graphics) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(getModuleItem());
        if (electricItem == null) return;
        EquipmentSlot slot = LivingEntity.getEquipmentSlotForItem(getAppliedTo());
        Component displayName = getModuleItem().getHoverName();
        int x = 10, y;
        switch (slot) {
            case HEAD -> {
                y = 20;
                graphics.drawString(
                        Minecraft.getInstance().font,
                        Component.translatable("metaarmor.tooltip.modifier.battery.hud.helmet", displayName),
                        x, y, 0xFFFFFF);
            }
            case CHEST -> {
                y = 40;
                graphics.drawString(
                        Minecraft.getInstance().font,
                        Component.translatable("metaarmor.tooltip.modifier.battery.hud.chestplate", displayName),
                        x, y, 0xFFFFFF);
            }
            case LEGS -> {
                y = 60;
                graphics.drawString(
                        Minecraft.getInstance().font,
                        Component.translatable("metaarmor.tooltip.modifier.battery.hud.leggings", displayName),
                        x, y, 0xFFFFFF);
            }
            case FEET -> {
                y = 80;
                graphics.drawString(
                        Minecraft.getInstance().font,
                        Component.translatable("metaarmor.tooltip.modifier.battery.hud.boots", displayName),
                        x, y, 0xFFFFFF);
            }
            default -> y = 100;
        }
        graphics.drawString(
                Minecraft.getInstance().font,
                Component.translatable("metaarmor.tooltip.modifier.battery.hud.info", electricItem.getCharge(),
                        electricItem.getMaxCharge()),
                x, y + 10, 0xFFFFFF);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player,
                                                  InteractionHand hand) {
        return getModuleItem().use(level, player, hand);
    }

    @Override
    public ItemModuleSettingsBuilder getSettings(PanelSyncManager psm, int id) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(getModuleItem());
        if (electricItem == null) return super.getSettings(psm, id);
        return super.getSettings(psm, id)
                .progress(Text.lang("gtceu.module.gui.charge"),
                        () -> (double) electricItem.getCharge() / electricItem.getMaxCharge(),
                        x -> GTStringUtils.formatInt((long) (x * electricItem.getMaxCharge())) + " EU");
    }

}
