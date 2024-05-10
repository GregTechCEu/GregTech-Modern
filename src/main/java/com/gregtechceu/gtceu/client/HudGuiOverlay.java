package com.gregtechceu.gtceu.client;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.IItemHUDProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class HudGuiOverlay implements IGuiOverlay {
    @Override
    public void render(ForgeGui forgeGui, PoseStack PoseStack, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isWindowActive() && mc.level != null && !mc.options.renderDebug && !mc.options.hideGui) {
            renderHUDMetaArmor(mc.player.getItemBySlot(EquipmentSlot.HEAD), PoseStack);
            renderHUDMetaArmor(mc.player.getItemBySlot(EquipmentSlot.CHEST), PoseStack);
            renderHUDMetaArmor(mc.player.getItemBySlot(EquipmentSlot.LEGS), PoseStack);
            renderHUDMetaArmor(mc.player.getItemBySlot(EquipmentSlot.FEET), PoseStack);
            renderHUDMetaItem(mc.player.getItemInHand(InteractionHand.MAIN_HAND), PoseStack);
            renderHUDMetaItem(mc.player.getItemInHand(InteractionHand.OFF_HAND), PoseStack);
        }
    }

    private static void renderHUDMetaArmor(@NotNull ItemStack stack, PoseStack PoseStack) {
        if (stack.getItem() instanceof ArmorComponentItem valueItem) {
            if (valueItem.getArmorLogic() instanceof IItemHUDProvider provider) {
                IItemHUDProvider.tryDrawHud(provider, stack, PoseStack);
            }
        }
    }

    private static void renderHUDMetaItem(@NotNull ItemStack stack, PoseStack PoseStack) {
        if (stack.getItem() instanceof ComponentItem valueItem) {
            for (IItemComponent behaviour : valueItem.getComponents()) {
                if (behaviour instanceof IItemHUDProvider provider) {
                    IItemHUDProvider.tryDrawHud(provider, stack, PoseStack);
                }
            }
        }
    }
}
