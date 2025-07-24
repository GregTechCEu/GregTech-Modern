package com.gregtechceu.gtceu.core.mixins.ldlib;

import com.lowdragmc.lowdraglib.syncdata.SyncUtils;

import net.minecraftforge.fluids.FluidStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SyncUtils.class)
public class SyncUtilsMixin {

    @Inject(method = "isChanged", at = @At("HEAD"), cancellable = true, remap = false)
    private static void gtceu$isChanged(Object oldValue, Object newValue, CallbackInfoReturnable<Boolean> cir) {
        if (oldValue instanceof FluidStack fluidStack) {
            if (!(newValue instanceof FluidStack)) {
                cir.setReturnValue(true);
            }
            cir.setReturnValue(!fluidStack.isFluidStackIdentical((FluidStack) newValue));
        }
    }
}
