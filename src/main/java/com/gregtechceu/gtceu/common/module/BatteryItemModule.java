package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.capability.ElectricItem;
import com.gregtechceu.gtceu.api.item.component.ElectricStats;
import com.gregtechceu.gtceu.api.item.module.*;
import com.gregtechceu.gtceu.api.item.module.ui.ItemModuleSettingsBuilder;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.value.sync.PanelSyncManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BatteryItemModule extends CapabilityProviderItemModule<IElectricItem> implements IHUDProviderItemModule {

    public BatteryItemModule(ResourceLocation id) {
        super(id);
    }

    @Override
    public Capability<IElectricItem> getCapability() {
        return GTCapability.CAPABILITY_ELECTRIC_ITEM;
    }

    @Override
    public @Nullable IElectricItem createCapabilityForStack(ModuleContext context, ItemStack stack) {
        var batteryItem = context.getData().getModuleItem().getItem();
        if (!(batteryItem instanceof IComponentItem componentItem)) return null;
        for (var component : componentItem.getComponents()) {
            if (component instanceof ElectricStats stats) {
                return new ElectricItem(stack, stats.maxCharge, stats.tier, stats.chargeable, stats.dischargeable);
            }
        }
        return null;
    }

    @Override
    public void clearCapabilityFromStack(ModuleContext context, ItemStack stack) {
        var tag = stack.getOrCreateTag();
        tag.remove("Charge");
        tag.remove("MaxCharge");
        tag.remove("Infinite");
    }

    @Override
    public Component getDisplayName(ModuleContext moduleContext) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(moduleContext.getAppliedTo());
        if (electricItem != null)
            return Component.translatable("module.gtceu.battery", GTValues.VNF[electricItem.getTier()]);
        else return super.getDisplayName(moduleContext);
    }

    @Override
    public Component getInfo() {
        return Component.translatable("module.gtceu.battery.description");
    }

    @Override
    public void onInventoryTick(ModuleContext moduleContext, Player player) {
        super.onInventoryTick(moduleContext, player);
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(moduleContext.getAppliedTo());
        if (electricItem != null)
            electricItem.inventoryTick(moduleContext.getAppliedTo(), player.level(), player, 0, false);
    }


    @Override
    public void appendHoverText(ModuleContext moduleContext, Level level, List<Component> tooltips,
                                TooltipFlag isAdvanced) {
        super.appendHoverText(moduleContext, level, tooltips, isAdvanced);
        tooltips.add(
                Component.translatable("module.gtceu.battery",
                        moduleContext.getData().getModuleItem().getHoverName()));
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(moduleContext.getAppliedTo());
        if (electricItem != null)
            electricItem.appendHoverText(moduleContext.getAppliedTo(), level, tooltips, isAdvanced);
    }

    @Override
    public void drawHUD(ModuleContext moduleContext, GuiGraphics graphics) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(moduleContext.getAppliedTo());
        if (electricItem == null) return;
        EquipmentSlot slot = LivingEntity.getEquipmentSlotForItem(moduleContext.getAppliedTo());
        Component displayName = moduleContext.getData().getModuleItem().getHoverName();
        int x = 10, y;
        switch (slot) {
            case HEAD -> {
                y = 20;
                graphics.drawString(
                        Minecraft.getInstance().font,
                        Component.translatable("module.gtceu.battery.hud.helmet", displayName),
                        x, y, 0xFFFFFF);
            }
            case CHEST -> {
                y = 40;
                graphics.drawString(
                        Minecraft.getInstance().font,
                        Component.translatable("module.gtceu.battery.hud.chestplate", displayName),
                        x, y, 0xFFFFFF);
            }
            case LEGS -> {
                y = 60;
                graphics.drawString(
                        Minecraft.getInstance().font,
                        Component.translatable("module.gtceu.battery.hud.leggings", displayName),
                        x, y, 0xFFFFFF);
            }
            case FEET -> {
                y = 80;
                graphics.drawString(
                        Minecraft.getInstance().font,
                        Component.translatable("module.gtceu.battery.hud.boots", displayName),
                        x, y, 0xFFFFFF);
            }
            default -> y = 100;
        }
        graphics.drawString(
                Minecraft.getInstance().font,
                Component.translatable("module.gtceu.battery.hud.info", electricItem.getCharge(),
                        electricItem.getMaxCharge()),
                x, y + 10, 0xFFFFFF);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(ModuleContext moduleContext, Level level, Player player,
                                                  InteractionHand hand) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(moduleContext.getAppliedTo());
        if (electricItem != null) return electricItem.use(moduleContext.getAppliedTo(), level, player, hand);
        return InteractionResultHolder.pass(moduleContext.getAppliedTo());
    }

    @Override
    public ItemModuleSettingsBuilder getSettings(ModuleContext moduleContext, PanelSyncManager psm, int id) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(moduleContext.getAppliedTo());
        if (electricItem == null) return super.getSettings(moduleContext, psm, id);
        return super.getSettings(moduleContext, psm, id)
                .progress(Text.lang("gui.gtceu.item_module.charge"),
                        () -> (double) electricItem.getCharge() / electricItem.getMaxCharge(),
                        x -> GTStringUtils.formatInt((long) (x * electricItem.getMaxCharge())) + " EU");
    }
}
