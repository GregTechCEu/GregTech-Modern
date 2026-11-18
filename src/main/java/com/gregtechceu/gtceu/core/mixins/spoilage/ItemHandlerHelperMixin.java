package com.gregtechceu.gtceu.core.mixins.spoilage;

import com.gregtechceu.gtceu.api.item.spoilage.SpoilUtils;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemHandlerHelper.class)
public class ItemHandlerHelperMixin {

    @Inject(at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/nbt/CompoundTag;equals(Ljava/lang/Object;)Z"),
            method = { "canItemStacksStack", "canItemStacksStackRelaxed" })
    private static void gtceu$mergeSpoilageBeforeStackTest(ItemStack a, ItemStack b,
                                                           CallbackInfoReturnable<Boolean> cir) {
        SpoilUtils.mergeSpoilage(a, b);
    }
}
