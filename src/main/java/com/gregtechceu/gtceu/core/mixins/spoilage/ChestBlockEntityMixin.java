package com.gregtechceu.gtceu.core.mixins.spoilage;

import com.gregtechceu.gtceu.api.item.component.SpoilUtils;

import net.minecraft.world.level.block.entity.*;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = { ChestBlockEntity.class, BarrelBlockEntity.class, EnderChestBlockEntity.class })
public class ChestBlockEntityMixin {

    @Inject(at = @At(value = "HEAD"), method = "recheckOpen")
    private void gtceu$serverTick(CallbackInfo ci) {
        SpoilUtils.updateBlock((BlockEntity) (Object) this);
    }
}
