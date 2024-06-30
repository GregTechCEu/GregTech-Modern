package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IMedicalConditionTracker;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.IFireImmuneEntity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements IFireImmuneEntity {

    @Shadow
    public abstract EntityType<?> getType();

    @Unique
    private boolean gtceu$fireImmune = false;
    @Unique
    private boolean gtceu$isEntityInit = false;

    @Inject(method = "fireImmune", at = @At("RETURN"), cancellable = true)
    private void gtceu$changeFireImmune(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(gtceu$fireImmune || cir.getReturnValueZ());
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void gtceu$onEntityInit(EntityType<?> entityType, Level level, CallbackInfo ci) {
        gtceu$isEntityInit = true;
    }

    public void gtceu$setFireImmune(boolean isImmune) {
        this.gtceu$fireImmune = isImmune;
    }

    @SuppressWarnings("UnreachableCode") // it doesn't like the cast because mixin.
    @ModifyReturnValue(method = "getMaxAirSupply", at = @At("RETURN"))
    private int gtceu$hazardModifyMaxAir(int original) {
        if (!gtceu$isEntityInit) {
            return original;
        }

        if (!ConfigHolder.INSTANCE.gameplay.hazardsEnabled)
            return original;

        IMedicalConditionTracker tracker = GTCapabilityHelper.getMedicalConditionTracker((Entity) (Object) this);
        if (tracker != null && tracker.getMaxAirSupply() != -1) {
            return tracker.getMaxAirSupply();
        }
        return original;
    }
}
