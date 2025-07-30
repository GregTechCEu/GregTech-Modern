package com.gregtechceu.gtceu.core.mixins.ae2;

import appeng.api.config.PowerUnits;
import appeng.api.config.Setting;
import com.google.common.collect.ImmutableSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumSet;

// Reasoning: The Setting enum contains all the possible settings to
// loop through (which is why the deprecated RF, which is a valid Power Unit,
// isn't an option when you click the button). We want to add EU to this list.
@Mixin(value = Setting.class, remap = false)
public abstract class SettingMixin<T extends Enum<T>> {

    @Shadow
    @Final
    @Mutable
    private ImmutableSet<T> values;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void gtceu$onInit(String name, Class<T> enumClass, EnumSet<T> originalValues, CallbackInfo ci) {
        if (enumClass.equals(PowerUnits.class)) {
            EnumSet<T> union = EnumSet.copyOf(originalValues);

            T eu = (T) PowerUnits.valueOf("EU");
            union.add(eu);

            this.values = ImmutableSet.copyOf(union);
        }
    }
}
