package com.gregtechceu.gtceu.client;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.IItemHUDProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class HudGuiOverlay implements IGuiOverlay {

    @Override
    public void render(ForgeGui forgeGui, GuiGraphics guiGraphics, float partialTick, int screenWidth,
                       int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isWindowActive() && mc.level != null && !mc.options.renderDebug && !mc.options.hideGui) {
            renderHUDMetaArmor(mc.player.getItemBySlot(EquipmentSlot.HEAD), guiGraphics);
            renderHUDMetaArmor(mc.player.getItemBySlot(EquipmentSlot.CHEST), guiGraphics);
            renderHUDMetaArmor(mc.player.getItemBySlot(EquipmentSlot.LEGS), guiGraphics);
            renderHUDMetaArmor(mc.player.getItemBySlot(EquipmentSlot.FEET), guiGraphics);
            renderHUDMetaItem(mc.player.getItemInHand(InteractionHand.MAIN_HAND), guiGraphics);
            renderHUDMetaItem(mc.player.getItemInHand(InteractionHand.OFF_HAND), guiGraphics);
        }
    }

    private static void renderHUDMetaArmor(@NotNull ItemStack stack, GuiGraphics guiGraphics) {
        if (stack.getItem() instanceof ArmorComponentItem valueItem) {
            if (valueItem.getArmorLogic() instanceof IItemHUDProvider provider) {
                IItemHUDProvider.tryDrawHud(provider, stack, guiGraphics);
            }
        }
    }

    private static void renderHUDMetaItem(@NotNull ItemStack stack, GuiGraphics guiGraphics) {
        if (stack.getItem() instanceof ComponentItem valueItem) {
            for (IItemComponent behaviour : valueItem.getComponents()) {
                if (behaviour instanceof IItemHUDProvider provider) {
                    IItemHUDProvider.tryDrawHud(provider, stack, guiGraphics);
                }
            }
        }
    }
}
