package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.IBreathingEntity;
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
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin implements IFireImmuneEntity, IBreathingEntity {

    @Shadow
    public abstract int getMaxAirSupply();

    @Unique
    private boolean gtceu$fireImmune = false;
    @Unique
    private boolean gtceu$isEntityInit = false;

    @Unique
    private int gtceu$modifiedMaxAirSupply = -1;

    @ModifyReturnValue(method = "fireImmune", at = @At("RETURN"))
    private boolean gtceu$changeFireImmune(boolean original) {
        return original || gtceu$fireImmune;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void gtceu$onEntityInit(EntityType<?> entityType, Level level, CallbackInfo ci) {
        gtceu$isEntityInit = true;
    }

    @ModifyReturnValue(method = "getMaxAirSupply", at = @At("RETURN"))
    private int gtceu$limitGetMaxAirSupply(int original) {
        return gtceu$limitAirSupply(original);
    }

    @ModifyVariable(method = "setAirSupply", at = @At("HEAD"), argsOnly = true)
    private int gtceu$limitSetAirSupply(int original) {
        return gtceu$limitAirSupply(original);
    }

    @ModifyReturnValue(method = "getAirSupply", at = @At("RETURN"))
    private int gtceu$limitGetAirSupply(int original) {
        return gtceu$limitAirSupply(original);
    }

    @Unique
    private int gtceu$limitAirSupply(int original) {
        if (!gtceu$isEntityInit) return original;
        if (!ConfigHolder.INSTANCE.gameplay.hazardsEnabled) return original;

        if (gtceu$modifiedMaxAirSupply > 0 && gtceu$modifiedMaxAirSupply < original) {
            return gtceu$modifiedMaxAirSupply;
        }
        return original;
    }

    @Override
    public void gtceu$setFireImmune(boolean isImmune) {
        this.gtceu$fireImmune = isImmune;
    }

    @Override
    public int gtceu$getOriginalMaxAirSupply() {
        if (!this.gtceu$isEntityInit) {
            return getMaxAirSupply();
        }

        try {
            // set the "in init" flag when getting the original value so the mixin to change it doesn't do anything
            this.gtceu$isEntityInit = false;
            return this.getMaxAirSupply();
        } finally {
            // then set it back after
            this.gtceu$isEntityInit = true;
        }
    }

    @Override
    public void gtceu$setMaxAirSupply(int newMaxAirSupply) {
        this.gtceu$modifiedMaxAirSupply = newMaxAirSupply;
    }
}
