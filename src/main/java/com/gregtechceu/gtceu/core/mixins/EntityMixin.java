package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.core.IFireImmuneEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements IFireImmuneEntity {

    @Shadow
    public abstract EntityType<?> getType();

    @Unique
    private boolean gtceu$fireImmune = false;

    @Inject(method = "fireImmune", at = @At("RETURN"), cancellable = true)
    private void gtceu$changeFireImmune(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(gtceu$fireImmune || cir.getReturnValueZ());
    }

    public void gtceu$setFireImmune(boolean isImmune) {
        this.gtceu$fireImmune = isImmune;
    }
}
