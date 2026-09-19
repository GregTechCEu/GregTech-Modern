package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.module.*;
import com.gregtechceu.gtceu.api.item.module.ui.ItemModuleSettingsBuilder;
import com.gregtechceu.gtceu.utils.GTStringUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.value.sync.PanelSyncManager;

import java.util.List;

public class BatteryItemModule extends ItemModule implements IHUDProviderItemModule {

    private static final double PERCENTAGE = 80.0d;

    public BatteryItemModule(ResourceLocation id) {
        super(id);
    }

    @Override
    public Component getDisplayName(ModuleContext moduleContext) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(moduleContext.getModuleItem());
        if (electricItem != null)
            return Component.translatable("metaarmor.tooltip.modifier.battery", GTValues.VNF[electricItem.getTier()]);
        else return super.getDisplayName(moduleContext);
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.battery");
    }

    @Override
    public void onInventoryTick(ModuleContext moduleContext, Player player) {
        super.onInventoryTick(moduleContext, player);
        IElectricItem item = GTCapabilityHelper.getElectricItem(moduleContext.getAppliedTo());
        IElectricItem battery = GTCapabilityHelper.getElectricItem(moduleContext.getModuleItem());
        if (item == null || battery == null || item == battery) return;
        if (item.getCharge() > item.getMaxCharge() * PERCENTAGE / 100) {
            long amount = (long) (item.getCharge() - item.getMaxCharge() * PERCENTAGE / 100);
            item.charge(battery.discharge(amount, battery.getTier(), true, false, false), item.getTier(), true, false);
        } else if (item.getCharge() < item.getMaxCharge() * PERCENTAGE / 100) {
            long amount = (long) (item.getMaxCharge() * PERCENTAGE / 100 - item.getCharge());
            battery.charge(item.discharge(amount, item.getTier(), true, false, false), battery.getTier(), true, false);
        }
        // the battery's inventoryTick method does not use inventorySlot or isCurrentItem
        moduleContext.getModuleItem().inventoryTick(player.level(), player, 0, false);
    }

    @Override
    public void appendHoverText(ModuleContext moduleContext, Item.TooltipContext context, TooltipFlag isAdvanced,
                                List<Component> tooltips) {
        super.appendHoverText(moduleContext, context, isAdvanced, tooltips);
        tooltips.add(
                Component.translatable("metaarmor.tooltip.modifier.battery",
                        moduleContext.getData().getModuleItem().getHoverName()));
        moduleContext.getModuleItem().getItem().appendHoverText(moduleContext.getModuleItem(), context, tooltips,
                isAdvanced);
    }

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event, Item item) {
        event.registerItem(GTCapability.CAPABILITY_ELECTRIC_ITEM, (s, v) -> {
            var modular = GTCapabilityHelper.getModularItem(s);
            if (modular == null) return null;
            var cap = modular.getAllModuleInstances().stream()
                    .filter(ctx -> ctx.getModule() instanceof BatteryItemModule).findFirst().orElse(null);
            if (cap == null) return null;
            else return cap.getModuleItem().getCapability(GTCapability.CAPABILITY_ELECTRIC_ITEM);
        }, item);
    }

    @Override
    public void drawHUD(ModuleContext moduleContext, GuiGraphics graphics) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(moduleContext.getModuleItem());
        if (electricItem == null) return;
        Equipable equipable = Equipable.get(moduleContext.getAppliedTo());
        Component displayName = moduleContext.getModuleItem().getHoverName();
        int x = 10, y;
        switch (equipable.getEquipmentSlot()) {
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
    public InteractionResultHolder<ItemStack> use(ModuleContext moduleContext, Level level, Player player,
                                                  InteractionHand hand) {
        return moduleContext.getModuleItem().use(level, player, hand);
    }

    @Override
    public ItemModuleSettingsBuilder getSettings(ModuleContext moduleContext, PanelSyncManager psm, int id) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(moduleContext.getModuleItem());
        if (electricItem == null) return super.getSettings(moduleContext, psm, id);
        return super.getSettings(moduleContext, psm, id)
                .progress(Text.lang("gtceu.module.gui.charge"),
                        () -> (double) electricItem.getCharge() / electricItem.getMaxCharge(),
                        x -> GTStringUtils.formatInt((long) (x * electricItem.getMaxCharge())) + " EU");
    }
}
