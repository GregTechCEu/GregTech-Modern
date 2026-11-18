package com.gregtechceu.gtceu.core.mixins.spoilage;

import com.gregtechceu.gtceu.api.item.spoilage.SpoilUtils;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {

    @Inject(method = "areMergable",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/nbt/CompoundTag;equals(Ljava/lang/Object;)Z"))
    private static void gtceu$mergeSpoilageBeforeEntityMerge(ItemStack a, ItemStack b,
                                                             CallbackInfoReturnable<Boolean> cir) {
        SpoilUtils.mergeSpoilage(a, b);
    }
}
