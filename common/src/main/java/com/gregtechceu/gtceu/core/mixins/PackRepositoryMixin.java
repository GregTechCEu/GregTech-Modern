package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.core.MixinHelpers;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = PackRepository.class, priority = 900)
public class PackRepositoryMixin {
    @Inject(method = "openAllSelected", cancellable = true, at = @At(value = "RETURN"))
    private void gtceu$loadPacks(CallbackInfoReturnable<List<PackResources>> cir) {
        cir.setReturnValue(MixinHelpers.addDynamicResourcePack(cir.getReturnValue()));
    }
}
