package com.gregtechceu.gtceu.client;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.IItemHUDProvider;
import com.gregtechceu.gtceu.api.item.module.IHUDProviderItemModule;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
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

            renderHUDMetaArmor(player.getItemBySlot(EquipmentSlot.HEAD), hudStrings, guiGraphics);
            renderHUDMetaArmor(player.getItemBySlot(EquipmentSlot.CHEST), hudStrings, guiGraphics);
            renderHUDMetaArmor(player.getItemBySlot(EquipmentSlot.LEGS), hudStrings, guiGraphics);
            renderHUDMetaArmor(player.getItemBySlot(EquipmentSlot.FEET), hudStrings, guiGraphics);
            renderHUDMetaItem(player.getItemInHand(InteractionHand.MAIN_HAND), hudStrings, guiGraphics);
            renderHUDMetaItem(player.getItemInHand(InteractionHand.OFF_HAND), hudStrings, guiGraphics);

            hudStrings.draw(guiGraphics);
        }
    }

    private static void renderHUDMetaArmor(@NotNull ItemStack stack, ArmorUtils.ModularHUD hudStrings,
                                           GuiGraphics guiGraphics) {
        if (stack.getItem() instanceof ArmorComponentItem valueItem) {
            if (valueItem.getArmorLogic() instanceof IItemHUDProvider provider) {
                IItemHUDProvider.tryDrawHud(provider, stack, hudStrings, guiGraphics);
            }
        }
        IHUDProviderItemModule.tryDrawHUD(stack, hudStrings, guiGraphics);
    }

    private static void renderHUDMetaItem(@NotNull ItemStack stack, ArmorUtils.ModularHUD hudStrings,
                                          GuiGraphics guiGraphics) {
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
