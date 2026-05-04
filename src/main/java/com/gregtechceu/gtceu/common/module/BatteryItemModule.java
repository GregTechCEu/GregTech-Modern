package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.module.AppliedItemModule;
import com.gregtechceu.gtceu.api.item.module.ICapabilityModule;
import com.gregtechceu.gtceu.api.item.module.IHUDProviderItemModule;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.utils.GTStringUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

import brachy.modularui.api.drawable.IKey;
import brachy.modularui.value.sync.PanelSyncManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BatteryItemModule extends ItemModule implements ICapabilityModule, IHUDProviderItemModule {

    private static final double PERCENTAGE = 80.0d;

    public BatteryItemModule(ResourceLocation id) {
        super(id);
    }

    @Override
    public Component getDisplayName(AppliedItemModule module) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(module.getModuleItem());
        if (electricItem != null)
            return Component.translatable("metaarmor.tooltip.modifier.battery", GTValues.VNF[electricItem.getTier()]);
        else return super.getDisplayName(module);
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.battery");
    }

    @Override
    public void onInventoryTick(Player player, AppliedItemModule module) {
        super.onInventoryTick(player, module);
        if (module.getAppliedTo() == null || module.getModuleItem() == null) return;
        IElectricItem item = GTCapabilityHelper.getElectricItem(module.getAppliedTo());
        IElectricItem battery = GTCapabilityHelper.getElectricItem(module.getModuleItem());
        if (item == null || battery == null || item == battery) return;
        if (item.getCharge() > item.getMaxCharge() * PERCENTAGE / 100) {
            long amount = (long) (item.getCharge() - item.getMaxCharge() * PERCENTAGE / 100);
            item.charge(battery.discharge(amount, battery.getTier(), true, false, false), item.getTier(), true, false);
        } else if (item.getCharge() < item.getMaxCharge() * PERCENTAGE / 100) {
            long amount = (long) (item.getMaxCharge() * PERCENTAGE / 100 - item.getCharge());
            battery.charge(item.discharge(amount, item.getTier(), true, false, false), battery.getTier(), true, false);
        }
        // the battery's inventoryTick method does not use inventorySlot or isCurrentItem
        module.getModuleItem().inventoryTick(player.level(), player, 0, false);
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips,
                                AppliedItemModule module) {
        super.appendHoverText(level, isAdvanced, tooltips, module);
        tooltips.add(
                Component.translatable("metaarmor.tooltip.modifier.battery", module.getModuleItem().getHoverName()));
        if (module.getModuleItem() != null) {
            module.getModuleItem().getItem().appendHoverText(module.getModuleItem(), level, tooltips, isAdvanced);
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(AppliedItemModule module, @NotNull Capability<T> cap) {
        if (cap == GTCapability.CAPABILITY_ELECTRIC_ITEM && module.getModuleItem() != null) {
            return module.getModuleItem().getCapability(cap);
        } else return LazyOptional.empty();
    }

    @Override
    public void drawHUD(AppliedItemModule module, GuiGraphics graphics) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(module.getModuleItem());
        if (electricItem == null) return;
        EquipmentSlot slot = LivingEntity.getEquipmentSlotForItem(module.getAppliedTo());
        Component displayName = module.getModuleItem().getHoverName();
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
    public InteractionResultHolder<ItemStack> use(AppliedItemModule module, Level level, Player player,
                                                  InteractionHand hand) {
        return module.getModuleItem().use(level, player, hand);
    }

    @Override
    public Settings getSettings(AppliedItemModule module, PanelSyncManager psm, int id) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(module.getModuleItem());
        if (electricItem == null) return super.getSettings(module, psm, id);
        return super.getSettings(module, psm, id)
                .progress(IKey.lang("gtceu.module.gui.charge"),
                        () -> (double) electricItem.getCharge() / electricItem.getMaxCharge(),
                        x -> GTStringUtils.formatInt((long) (x * electricItem.getMaxCharge())) + " EU");
    }
}
