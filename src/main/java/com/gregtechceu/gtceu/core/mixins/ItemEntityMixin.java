package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.item.component.SpoilUtils;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {

    @Inject(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/nbt/CompoundTag;equals(Ljava/lang/Object;)Z"),
            method = "areMergable")
    private static void gtceu$beforeComparison(ItemStack a, ItemStack b, CallbackInfoReturnable<Boolean> cir) {
        SpoilUtils.mergeSpoilage(a, b);
    }
}
