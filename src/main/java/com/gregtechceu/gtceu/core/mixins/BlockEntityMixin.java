package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.core.LDLibRuntimeHooks;
import com.gregtechceu.gtceu.core.MixinHelpers;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(value = BlockEntity.class, priority = 1500)
public class BlockEntityMixin {

    @Shadow
    @Nullable
    protected Level level;

    @Inject(method = "saveAdditional", at = @At(value = "HEAD"))
    private void gtceu$captureRegistriesSave(ValueOutput output, CallbackInfo ci) {
        if (LDLibRuntimeHooks.isAutoPersistBlockEntity(this)) {
            MixinHelpers.CURRENT_BE_SAVE_LOAD_REGISTRIES
                    .set(level == null ? RegistryAccess.EMPTY : level.registryAccess());
        }
    }

    @Inject(method = "saveAdditional", at = @At(value = "RETURN"))
    private void gtceu$clearRegistriesSave(ValueOutput output, CallbackInfo ci) {
        if (LDLibRuntimeHooks.isAutoPersistBlockEntity(this)) {
            MixinHelpers.CURRENT_BE_SAVE_LOAD_REGISTRIES.remove();
        }
    }

    @Inject(method = "loadAdditional", at = @At(value = "HEAD"))
    private void gtceu$captureRegistriesLoad(ValueInput input, CallbackInfo ci) {
        if (LDLibRuntimeHooks.hasAutoSyncTag(this, input)) {
            MixinHelpers.CURRENT_BE_SAVE_LOAD_REGISTRIES.set(input.lookup());
        } else if (LDLibRuntimeHooks.isAutoPersistBlockEntity(this)) {
            MixinHelpers.CURRENT_BE_SAVE_LOAD_REGISTRIES.set(input.lookup());
        }
    }

    @Inject(method = "loadAdditional", at = @At(value = "RETURN"))
    private void gtceu$clearRegistriesLoad(ValueInput input, CallbackInfo ci) {
        if (LDLibRuntimeHooks.hasAutoSyncTag(this, input)) {
            MixinHelpers.CURRENT_BE_SAVE_LOAD_REGISTRIES.remove();
        } else if (LDLibRuntimeHooks.isAutoPersistBlockEntity(this)) {
            MixinHelpers.CURRENT_BE_SAVE_LOAD_REGISTRIES.remove();
        }
    }
}
