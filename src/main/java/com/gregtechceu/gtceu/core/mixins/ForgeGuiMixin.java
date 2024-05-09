package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ForgeGui.class, remap = false)
public abstract class ForgeGuiMixin {

    @Shadow
    public abstract Minecraft getMinecraft();

    @ModifyExpressionValue(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getArmorValue()I"))
    private int gtceu$modifyArmorAmount(int level) {
        var armorInv = getMinecraft().player.getInventory().armor;
        for (int i = 0; i < armorInv.size(); ++i) {
            ItemStack armor = armorInv.get(i);
            if (armor.getItem() instanceof ArmorComponentItem armorItem) {
                EquipmentSlot slot = EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, i);
                level += armorItem.getArmorDisplay(getMinecraft().player, armor, slot);
            }
        }
        return level;
    }
}
