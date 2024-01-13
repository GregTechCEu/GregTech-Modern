package com.gregtechceu.gtceu.core.mixins.create;

import com.gregtechceu.gtceu.common.blockentity.KineticMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.kinetic.IKineticMachine;
import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author KilaBash
 * @date 2023/4/1
 * @implNote RotationPropagatorMixin
 */
@Mixin(RotationPropagator.class)
public abstract class RotationPropagatorMixin {
    @Inject(method = "getAxisModifier", at = @At(value = "RETURN"), remap = false, cancellable = true)
    private static void injectAxisModifier(KineticBlockEntity te, Direction direction, CallbackInfoReturnable<Float> cir) {
        if ((te.hasSource() || te.isSource()) && te instanceof KineticMachineBlockEntity kineticMachineBlockEntity) {
            if (kineticMachineBlockEntity.getMetaMachine() instanceof IKineticMachine kineticMachine) {
                cir.setReturnValue(kineticMachine.getRotationSpeedModifier(direction));
            }
        }
    }

}
