package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.item.IGTTool;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(value = RepairItemRecipe.class)
public abstract class RepairItemRecipeMixin {

    @Inject(
        method = "matches(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/level/Level;)Z",
        at = @At(value = "INVOKE_ASSIGN", target = "Ljava/util/List;get(I)Ljava/lang/Object;"),
        cancellable = true
    )
    public void gtceu$matches(CraftingContainer inv, Level worldIn, CallbackInfoReturnable<Boolean> cir, @Local ItemStack itemstack, @Local ItemStack itemstack1) {
        if (itemstack.getItem() instanceof IGTTool first && itemstack1.getItem() instanceof IGTTool second) {
            if (first.isElectric() || second.isElectric()) {
                cir.setReturnValue(false);
            // do not allow repairing tools if both are full durability
            } else if (!first.definition$isDamaged(itemstack) && !second.definition$isDamaged(itemstack1)) {
                cir.setReturnValue(false);
            }
        }
    }

}
