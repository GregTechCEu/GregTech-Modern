package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.utils.JetpackHelper;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {

    public PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "causeFallDamage", cancellable = true, at = @At("HEAD"))
    private void gtceu$checkPreventFallDamage(float fallDistance, float multiplier, DamageSource source,
                                              CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        Player self = (Player) (Object) this;
        JetpackHelper jetpackHelper = JetpackHelper.getActiveJetpackInstance(self);

        // Hover mode and safety braking completely negate all fall damage
        if (jetpackHelper != null && (jetpackHelper.isHoverModeEnabled() || jetpackHelper.isSafetyBraking())) {
            callbackInfoReturnable.setReturnValue(false);
        } else if (jetpackHelper != null && jetpackHelper.shouldAbsorbFallDamage(fallDistance)) {
            // Active jetpack will absorb all fall damage below the threshold
            callbackInfoReturnable.setReturnValue(false);
        }
    }

    // This catches fall damage not caught by causeFallDamage mixin (indirect fall damage from ender pearls and wind
    // charges)
    @WrapOperation(method = "hurt",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean gtceu$modifyPlayerDamage(Player instance, DamageSource source, float amount,
                                             Operation<Boolean> original) {
        if (!level().isClientSide() && source.is(DamageTypeTags.IS_FALL)) {
            ItemStack boots = getItemBySlot(EquipmentSlot.FEET);
            if (boots.is(CustomTags.STEP_BOOTS) && boots.getItem() instanceof ArmorComponentItem armor) {
                amount -= armor.getArmorLogic().absorbDamage(this, boots, source, Mth.ceil(amount),
                        EquipmentSlot.FEET, false);
                if (amount <= 0.0f) {
                    return false;
                }
            }
        }
        return original.call(instance, source, amount);
    }

    // Modify travel to allow jetpack to affect vertical momentum, similar to how creative mode flight does it
    @WrapOperation(method = "travel",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/world/entity/LivingEntity;travel(Lnet/minecraft/world/phys/Vec3;)V"))
    private void gtceu$modifyTravelWithJetpack(Player instance, Vec3 travelVector, Operation<Void> original) {
        var jetpackHelper = JetpackHelper.getActiveJetpackInstance(instance);

        if (jetpackHelper != null && jetpackHelper.isHovering() && !isPassenger()) {
            double lastMomentumY = getDeltaMovement().y;
            original.call(instance, travelVector);
            Vec3 currentMomentum = getDeltaMovement();
            setDeltaMovement(jetpackHelper.applyHoveringDeceleration(currentMomentum, lastMomentumY));

            resetFallDistance();
            setSharedFlag(FLAG_FALL_FLYING, false);
        } else if (jetpackHelper != null && jetpackHelper.isSafetyBraking()) {
            original.call(instance, travelVector);
            Vec3 currentMomentum = getDeltaMovement();
            setDeltaMovement(jetpackHelper.applySafetyBraking(currentMomentum));

            resetFallDistance();
        } else {
            original.call(instance, travelVector);
        }
    }

    // Prevent underwater mining penalty when hovering with jetpack
    @WrapOperation(method = "getDigSpeed",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;hasAquaAffinity(Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private boolean gtceu$shouldPreventUnderwaterMiningPenalty(LivingEntity entity, Operation<Boolean> original) {
        return original.call(entity) || JetpackHelper.isHoveringWithJetpack((Player) entity);
    }

    // Prevent airborne mining penalty when hovering with jetpack
    @WrapOperation(method = "getDigSpeed",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/world/entity/player/Player;onGround()Z"))
    private boolean gtceu$shouldPreventAirborneMiningPenalty(Player instance, Operation<Boolean> original) {
        return original.call(instance) || JetpackHelper.isHoveringWithJetpack(instance);
    }

    // Adjust flying speed when flying with jetpack
    @Inject(method = "getFlyingSpeed", cancellable = true, at = @At("HEAD"))
    private void gtceu$modifyFlyingSpeed(CallbackInfoReturnable<Float> callbackInfoReturnable) {
        Player self = (Player) (Object) this;
        JetpackHelper jetpackHelper = JetpackHelper.getActiveJetpackInstance(self);

        if (jetpackHelper != null && jetpackHelper.isFlyingWithJetpack() && !jetpackHelper.isHoldingAltitude()) {
            callbackInfoReturnable.setReturnValue((float) jetpackHelper.getEffectiveHorizontalFlySpeed());
        }
    }

    // Do not enter crouching pose when descending while hovering with jetpack
    @WrapOperation(method = "updatePlayerPose",
                   at = @At(value = "FIELD",
                            target = "Lnet/minecraft/world/entity/player/Abilities;flying:Z",
                            opcode = Opcodes.GETFIELD))
    private boolean gtceu$shouldPreventCrouchingPose(Abilities instance, Operation<Boolean> original) {
        Player self = (Player) (Object) this;
        return original.call(instance) || JetpackHelper.isHoveringWithJetpack(self);
    }

    // Allow walking off edges when holding shift when hovering with jetpack
    @WrapOperation(method = "maybeBackOffFromEdge",
                   at = @At(value = "FIELD",
                            target = "Lnet/minecraft/world/entity/player/Abilities;flying:Z",
                            opcode = Opcodes.GETFIELD))
    private boolean gtceu$shouldAllowBackingOffEdge(Abilities instance, Operation<Boolean> original) {
        Player self = (Player) (Object) this;
        return original.call(instance) || JetpackHelper.isHoveringWithJetpack(self);
    }

    // Do not let the block we are hovering above of with jetpack affect the movement speed
    @WrapOperation(method = "getBlockSpeedFactor",
                   at = @At(value = "FIELD",
                            target = "Lnet/minecraft/world/entity/player/Abilities;flying:Z",
                            opcode = Opcodes.GETFIELD))
    private boolean gtceu$shouldIgnoreBlockSpeedFactor(Abilities instance, Operation<Boolean> original) {
        Player self = (Player) (Object) this;
        return original.call(instance) || JetpackHelper.isHoveringWithJetpack(self);
    }

    @WrapOperation(method = "shouldRemoveSoulSpeed",
                   at = @At(value = "FIELD",
                            target = "Lnet/minecraft/world/entity/player/Abilities;flying:Z",
                            opcode = Opcodes.GETFIELD))
    private boolean gtceu$shouldRemoveSoulSpeed(Abilities instance, Operation<Boolean> original) {
        Player self = (Player) (Object) this;
        return original.call(instance) || JetpackHelper.isHoveringWithJetpack(self);
    }

    @WrapOperation(method = "onSoulSpeedBlock",
                   at = @At(value = "FIELD",
                            target = "Lnet/minecraft/world/entity/player/Abilities;flying:Z",
                            opcode = Opcodes.GETFIELD))
    private boolean gtceu$shouldPreventBeingOnSoulSpeedBlock(Abilities instance, Operation<Boolean> original) {
        Player self = (Player) (Object) this;
        return original.call(instance) || JetpackHelper.isHoveringWithJetpack(self);
    }

    // Do not get pushed by fluids when hovering with jetpack
    @WrapOperation(method = "isPushedByFluid",
                   at = @At(value = "FIELD",
                            target = "Lnet/minecraft/world/entity/player/Abilities;flying:Z",
                            opcode = Opcodes.GETFIELD))
    private boolean gtceu$shouldPreventPushedByFluid(Abilities instance, Operation<Boolean> original) {
        Player self = (Player) (Object) this;
        return original.call(instance) || JetpackHelper.isHoveringWithJetpack(self);
    }

    // Do not sink or move slower in fluids when hovering with jetpack
    @WrapOperation(method = "isAffectedByFluids",
                   at = @At(value = "FIELD",
                            target = "Lnet/minecraft/world/entity/player/Abilities;flying:Z",
                            opcode = Opcodes.GETFIELD))
    private boolean gtceu$shouldNotBeAffectedByFluids(Abilities instance, Operation<Boolean> original) {
        Player self = (Player) (Object) this;
        return original.call(instance) || JetpackHelper.isHoveringWithJetpack(self);
    }

    // Do not start or stay swimming when hovering with jetpack
    @WrapOperation(method = "isSwimming",
                   at = @At(value = "FIELD",
                            target = "Lnet/minecraft/world/entity/player/Abilities;flying:Z",
                            opcode = Opcodes.GETFIELD))
    private boolean gtceu$shouldPreventSwimming(Abilities instance, Operation<Boolean> original) {
        Player self = (Player) (Object) this;
        return original.call(instance) || JetpackHelper.isHoveringWithJetpack(self);
    }

    @WrapOperation(method = "updateSwimming",
                   at = @At(value = "FIELD",
                            target = "Lnet/minecraft/world/entity/player/Abilities;flying:Z",
                            opcode = Opcodes.GETFIELD))
    private boolean gtceu$shouldStopSwimming(Abilities instance, Operation<Boolean> original) {
        Player self = (Player) (Object) this;
        return original.call(instance) || JetpackHelper.isHoveringWithJetpack(self);
    }

    // Do not make movement sounds when hovering with jetpack
    @WrapOperation(method = "getMovementEmission",
                   at = @At(value = "FIELD",
                            target = "Lnet/minecraft/world/entity/player/Abilities;flying:Z",
                            opcode = Opcodes.GETFIELD))
    private boolean gtceu$shouldHaveNoMovementEmission(Abilities instance, Operation<Boolean> original) {
        Player self = (Player) (Object) this;
        JetpackHelper jetpackHelper = JetpackHelper.getActiveJetpackInstance(self);
        return original.call(instance) || (jetpackHelper != null && jetpackHelper.shouldSuppressMovementEmission());
    }
}
