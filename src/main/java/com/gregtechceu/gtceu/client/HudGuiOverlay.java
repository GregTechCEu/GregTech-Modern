package com.gregtechceu.gtceu.client;

import com.gregtechceu.gtceu.api.item.IComponentItem;
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
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class HudGuiOverlay implements IGuiOverlay {

    @Override
    public void render(ForgeGui forgeGui, GuiGraphics guiGraphics, float partialTick, int screenWidth,
                       int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isWindowActive() && mc.level != null && !mc.options.renderDebug && !mc.options.hideGui) {
            MutableInt drawOffsetY = new MutableInt();
            renderHUDMetaArmor(mc.player.getItemBySlot(EquipmentSlot.HEAD), guiGraphics, drawOffsetY);
            renderHUDMetaArmor(mc.player.getItemBySlot(EquipmentSlot.CHEST), guiGraphics, drawOffsetY);
            renderHUDMetaArmor(mc.player.getItemBySlot(EquipmentSlot.LEGS), guiGraphics, drawOffsetY);
            renderHUDMetaArmor(mc.player.getItemBySlot(EquipmentSlot.FEET), guiGraphics, drawOffsetY);
            renderHUDMetaItem(mc.player.getItemInHand(InteractionHand.MAIN_HAND), guiGraphics, drawOffsetY);
            renderHUDMetaItem(mc.player.getItemInHand(InteractionHand.OFF_HAND), guiGraphics, drawOffsetY);
        }
    }

    private static void renderHUDMetaArmor(@NotNull ItemStack stack, GuiGraphics guiGraphics, MutableInt drawOffsetY) {
        renderHUDMetaItem(stack, guiGraphics, drawOffsetY);
        if (stack.getItem() instanceof ArmorComponentItem valueItem) {
            if (valueItem.getArmorLogic() instanceof IItemHUDProvider provider) {
                IItemHUDProvider.tryDrawHud(provider, stack, guiGraphics, drawOffsetY);
            }
        }
    }

    private static void renderHUDMetaItem(@NotNull ItemStack stack, GuiGraphics guiGraphics, MutableInt drawOffsetY) {
        if (stack.getItem() instanceof IComponentItem valueItem) {
            for (IItemComponent behaviour : valueItem.getComponents()) {
                if (behaviour instanceof IItemHUDProvider provider) {
                    IItemHUDProvider.tryDrawHud(provider, stack, guiGraphics, drawOffsetY);
                }
            }
        }
    }
}
