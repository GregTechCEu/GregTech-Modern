package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.item.tool.GTToolItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {"net.minecraft.world.item.enchantment.EnchantmentCategory$6",
        "net.minecraft.world.item.enchantment.EnchantmentCategory$7",
        "net.minecraft.world.item.enchantment.EnchantmentCategory$9",
        "net.minecraft.world.item.enchantment.EnchantmentCategory$11",
        "net.minecraft.world.item.enchantment.EnchantmentCategory$13"})
public class MixinEnchantmentCategory {

    @Inject(method = "canEnchant(Lnet/minecraft/world/item/Item;)Z", at = @At("RETURN"), cancellable = true)
    private void gtceu$canEnchantTool(Item item, CallbackInfoReturnable<Boolean> cir) {
        if (item instanceof GTToolItem gtItem) {
            cir.setReturnValue(switch ((EnchantmentCategory)(Object)this) {
                case WEAPON -> gtItem.getToolType() == GTToolType.SWORD ||
                        gtItem.getToolType() == GTToolType.BUTCHERY_KNIFE ||
                        gtItem.getToolType() == GTToolType.KNIFE;
                case DIGGER -> gtItem.getToolType() == GTToolType.AXE ||
                        gtItem.getToolType() == GTToolType.PICKAXE ||
                        gtItem.getToolType() == GTToolType.SHOVEL ||
                        gtItem.getToolType() == GTToolType.HOE ||
                        gtItem.getToolType() == GTToolType.MINING_HAMMER ||
                        gtItem.getToolType() == GTToolType.SAW ||
                        gtItem.getToolType() == GTToolType.HARD_HAMMER ||
                        gtItem.getToolType() == GTToolType.SOFT_MALLET ||
                        gtItem.getToolType() == GTToolType.WRENCH ||
                        gtItem.getToolType() == GTToolType.FILE ||
                        gtItem.getToolType() == GTToolType.CROWBAR ||
                        gtItem.getToolType() == GTToolType.SCREWDRIVER ||
                        gtItem.getToolType() == GTToolType.SCYTHE ||
                        gtItem.getToolType() == GTToolType.PLUNGER;
                default -> cir.getReturnValueZ();
            });
        }
    }
}
