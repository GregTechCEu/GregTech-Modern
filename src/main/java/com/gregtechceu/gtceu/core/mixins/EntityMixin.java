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

@Mixin(Entity.class)
public abstract class EntityMixin implements IFireImmuneEntity {

    @Shadow
    public abstract EntityType<?> getType();

    @Unique
    private boolean gtceu$fireImmune = false;
    @Unique
    private boolean gtceu$isEntityInit = false;

    @ModifyReturnValue(method = "fireImmune", at = @At("RETURN"))
    private boolean gtceu$changeFireImmune(boolean original) {
        return original || gtceu$fireImmune;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void gtceu$onEntityInit(EntityType<?> entityType, Level level, CallbackInfo ci) {
        gtceu$isEntityInit = true;
    }

    public void gtceu$setFireImmune(boolean isImmune) {
        this.gtceu$fireImmune = isImmune;
    }

    @ModifyReturnValue(method = "getMaxAirSupply", at = @At("RETURN"))
    private int gtceu$hazardModifyMaxAir(int original) {
        if (!gtceu$isEntityInit) return original;
        if (!ConfigHolder.INSTANCE.gameplay.hazardsEnabled) return original;

        IMedicalConditionTracker tracker = GTCapabilityHelper.getMedicalConditionTracker((Entity) (Object) this);
        if (tracker != null && tracker.getMaxAirSupply() != -1) {
            return tracker.getMaxAirSupply();
        }
        return original;
    }
}
