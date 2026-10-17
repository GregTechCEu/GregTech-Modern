package com.gregtechceu.gtceu.client;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.IItemHUDProvider;
import com.gregtechceu.gtceu.api.item.module.IHUDProviderItemModule;
import com.gregtechceu.gtceu.common.module.BatteryItemModule;

import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class HudGuiOverlay implements LayeredDraw.Layer {

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker tracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isWindowActive() && mc.level != null &&
                !mc.gui.getDebugOverlay().showDebugScreen() &&
                !mc.options.hideGui) {

            ArmorUtils.ModularHUD hudStrings = new ArmorUtils.ModularHUD();

            var player = mc.player;
            if (player == null) return;

            renderSuitBatteries(player, hudStrings);

            renderHUDMetaArmor(player.getItemBySlot(EquipmentSlot.HEAD), hudStrings, guiGraphics);
            renderHUDMetaArmor(player.getItemBySlot(EquipmentSlot.CHEST), hudStrings, guiGraphics);
            renderHUDMetaArmor(player.getItemBySlot(EquipmentSlot.LEGS), hudStrings, guiGraphics);
            renderHUDMetaArmor(player.getItemBySlot(EquipmentSlot.FEET), hudStrings, guiGraphics);
            renderHUDMetaItem(player.getItemInHand(InteractionHand.MAIN_HAND), hudStrings, guiGraphics);
            renderHUDMetaItem(player.getItemInHand(InteractionHand.OFF_HAND), hudStrings, guiGraphics);

            hudStrings.draw(guiGraphics);
        }
    }

    private static void renderSuitBatteries(Player player, ArmorUtils.ModularHUD hud) {
        var percentages = Component.empty();
        boolean hasBattery = false;
        for (var slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
            if (slot != EquipmentSlot.HEAD)
                percentages.append(Component.literal(" | ").withStyle(ChatFormatting.WHITE));
            var stack = player.getItemBySlot(slot);
            var modular = GTCapabilityHelper.getModularItem(stack);
            var electric = GTCapabilityHelper.getElectricItem(stack);
            boolean hasModule = modular != null && modular.getAllModuleInstances().stream()
                    .anyMatch(context -> context.getModule() instanceof BatteryItemModule);
            if (!hasModule || electric == null || electric.getMaxCharge() <= 0) {
                percentages.append(Component.literal("--%").withStyle(ChatFormatting.WHITE));
                continue;
            }
            hasBattery = true;
            int percent = (int) Math.clamp(100.0 * electric.getCharge() / electric.getMaxCharge(), 0, 100);
            var color = percent >= 75 ? ChatFormatting.GREEN :
                    percent >= 50 ? ChatFormatting.YELLOW :
                            percent >= 25 ? ChatFormatting.GOLD :
                                    percent > 0 ? ChatFormatting.RED : ChatFormatting.DARK_RED;
            percentages.append(Component.literal(percent + "%").withStyle(color));
        }
        if (hasBattery) {
            hud.newLine(Component.translatable("module.gtceu.battery.hud.suit"));
            hud.newLine(percentages);
        }
    }

    private static void renderHUDMetaArmor(@NotNull ItemStack stack, ArmorUtils.ModularHUD hudStrings,
                                           GuiGraphics guiGraphics) {
        if (stack.getItem() instanceof ArmorComponentItem valueItem) {
            if (valueItem.getArmorLogic() instanceof IItemHUDProvider provider) {
                IItemHUDProvider.tryDrawHud(provider, stack, hudStrings, guiGraphics);
            }
        }
        var modular = GTCapabilityHelper.getModularItem(stack);
        if (modular == null) return;
        for (var context : modular.getAllModuleInstances()) {
            if (context.getModule() instanceof BatteryItemModule) continue;
            if (context.getModule() instanceof IHUDProviderItemModule provider && provider.shouldDrawHUD(context)) {
                provider.drawHUD(context, hudStrings, guiGraphics);
                hudStrings.newLine(CommonComponents.EMPTY);
            }
        }
    }

    private static void renderHUDMetaItem(@NotNull ItemStack stack, ArmorUtils.ModularHUD hudStrings,
                                          GuiGraphics guiGraphics) {
        if (stack.getItem() instanceof ArmorItem) return;
        if (stack.getItem() instanceof ComponentItem valueItem) {
            for (IItemComponent behaviour : valueItem.getComponents()) {
                if (behaviour instanceof IItemHUDProvider provider) {
                    IItemHUDProvider.tryDrawHud(provider, stack, hudStrings, guiGraphics);
                }
            }
        }
        IHUDProviderItemModule.tryDrawHUD(stack, hudStrings, guiGraphics);
    }
}
