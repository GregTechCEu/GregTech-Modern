package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.core.IGTSyncedDataPlayer;
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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements IGTSyncedDataPlayer {

    @Unique
    private static final double SAFETY_BRAKING_DECELERATION = 0.10;
    @Unique
    private static final double SAFETY_BRAKING_OUT_OF_WORLD_DECELERATION = 0.40;
    @Unique
    private static final double SAFETY_BRAKING_DESCENT_SPEED = 0.15;
    @Unique
    private static final double SAFETY_BRAKING_BOUNDARY_RETURN_FACTOR = 0.1;
    @Unique
    private static final float JETPACK_FALL_DISTANCE_ABSORPTION_THRESHOLD = 8.0f;

    @Unique
    private boolean gtceu$isJetpackThrusting;
    @Unique
    private boolean gtceu$isPrecisionHovering;
    @Unique
    private Vec3 gtceu$lastServerTickPosition = Vec3.ZERO;
    @Unique
    private boolean gtceu$isSafetyBraking;

    public PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean gtceu$isJetpackThrusting() {
        return this.gtceu$isJetpackThrusting;
    }

    @Override
    public void gtceu$setJetpackThrusting(boolean isJetpackThrusting) {
        this.gtceu$isJetpackThrusting = isJetpackThrusting;
    }

    @Override
    public boolean gtceu$isPrecisionHovering() {
        return this.gtceu$isPrecisionHovering;
    }

    @Override
    public void gtceu$setPrecisionHovering(boolean isPrecisionHovering) {
        this.gtceu$isPrecisionHovering = isPrecisionHovering;
    }

    @Override
    public boolean gtceu$isSafetyBraking() {
        return this.gtceu$isSafetyBraking;
    }

    @Override
    public void gtceu$setSafetyBraking(boolean isSafetyBraking) {
        this.gtceu$isSafetyBraking = isSafetyBraking;
    }

    @Shadow
    protected void removeEntitiesOnShoulder() {
        throw new UnsupportedOperationException();
    }

    @Inject(method = "aiStep", at = @At(value = "TAIL"))
    private void playerTick(CallbackInfo callbackInfo) {
        Player self = (Player) (Object) this;
        if (level().isClientSide()) {
            return;
        }

        JetpackHelper jetpackHelper = JetpackHelper.getActiveJetpackInstance(self);

        Vec3 tickMotion = self.position().subtract(this.gtceu$lastServerTickPosition);
        this.gtceu$lastServerTickPosition = self.position();

        if (jetpackHelper != null && jetpackHelper.isFlyingWithJetpack()) {
            jetpackHelper.serverConsumeEnergy();
            jetpackHelper.serverSpawnEffects(tickMotion);

            // If we are thrusting with jetpack, reset fall distance immediately and stop fall flying
            if (!jetpackHelper.isHoverModeEnabled()) {
                self.resetFallDistance();
                self.setSharedFlag(FLAG_FALL_FLYING, false);
            }

            // Follow vanilla behavior and remove entities on shoulder when thrusting or hovering
            removeEntitiesOnShoulder();
        }
    }

    @Inject(method = "causeFallDamage", cancellable = true, at = @At("HEAD"))
    private void checkPreventFallDamage(float fallDistance, float multiplier, DamageSource source,
                                        CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        Player self = (Player) (Object) this;
        JetpackHelper jetpackHelper = JetpackHelper.getActiveJetpackInstance(self);

        // Hover mode and safety braking completely negate all fall damage
        if (jetpackHelper != null && (jetpackHelper.isHoverModeEnabled() || gtceu$isSafetyBraking())) {
            callbackInfoReturnable.setReturnValue(false);
        } else if (jetpackHelper != null && fallDistance < JETPACK_FALL_DISTANCE_ABSORPTION_THRESHOLD) {
            // Active jetpack will absorb all fall damage below the threshold
            callbackInfoReturnable.setReturnValue(false);
        }
    }

    // This catches fall damage not caught by causeFallDamage mixin (indirect fall damage from ender pearls and wind
    // charges)
    @WrapOperation(method = "hurt",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean modifyPlayerDamage(Player instance, DamageSource source, float amount,
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
    private void modifyTravelWithJetpack(Player instance, Vec3 travelVector, Operation<Void> original) {
        var jetpackHelper = JetpackHelper.getActiveJetpackInstance(instance);

        if (jetpackHelper != null && jetpackHelper.isHovering() && !isPassenger()) {
            double lastMomentumY = getDeltaMovement().y;
            original.call(instance, travelVector);
            Vec3 currentMomentum = getDeltaMovement();
            double momentumScale = Mth.clamp(1.0f - jetpackHelper.getJetpackStats().verticalMomentumFalloff(), 0.0f,
                    1.0f);
            setDeltaMovement(currentMomentum.x, lastMomentumY * momentumScale, currentMomentum.z);

            resetFallDistance();
            setSharedFlag(FLAG_FALL_FLYING, false);
        } else if (jetpackHelper != null && gtceu$isSafetyBraking()) {
            original.call(instance, travelVector);
            Vec3 currentMomentum = getDeltaMovement();
            boolean isOutOfWorld = getY() < level().getMinBuildHeight();
            double newMomentumY = currentMomentum.y();

            double effectiveBrakingDeceleration = isOutOfWorld ? SAFETY_BRAKING_OUT_OF_WORLD_DECELERATION :
                    SAFETY_BRAKING_DECELERATION;
            double targetMomentumY = isOutOfWorld ? Mth.clamp(
                    (level().getMinBuildHeight() - getY()) * SAFETY_BRAKING_BOUNDARY_RETURN_FACTOR, 0.0,
                    SAFETY_BRAKING_DESCENT_SPEED) : -SAFETY_BRAKING_DESCENT_SPEED;

            if (newMomentumY < targetMomentumY) {
                newMomentumY = Math.min(targetMomentumY, newMomentumY + effectiveBrakingDeceleration);
            }
            setDeltaMovement(new Vec3(currentMomentum.x(), newMomentumY, currentMomentum.z()));
            resetFallDistance();
        } else {
            original.call(instance, travelVector);
        }
    }

    // Prevent underwater mining penalty when hovering with jetpack
    @WrapOperation(method = "getDigSpeed",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;hasAquaAffinity(Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private boolean shouldPreventUnderwaterMiningPenalty(LivingEntity entity, Operation<Boolean> original) {
        return original.call(entity) || JetpackHelper.isHoveringWithJetpack((Player) entity);
    }

    // Prevent airborne mining penalty when hovering with jetpack
    @WrapOperation(method = "getDigSpeed",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/world/entity/player/Player;onGround()Z"))
    private boolean shouldPreventAirborneMiningPenalty(Player instance, Operation<Boolean> original) {
        return original.call(instance) || JetpackHelper.isHoveringWithJetpack(instance);
    }

    // Adjust flying speed when flying with jetpack
    @Inject(method = "getFlyingSpeed", cancellable = true, at = @At("HEAD"))
    private void modifyFlyingSpeed(CallbackInfoReturnable<Float> callbackInfoReturnable) {
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
    private boolean shouldPreventCrouchingPose(Abilities instance, Operation<Boolean> original) {
        Player self = (Player) (Object) this;
        return original.call(instance) || JetpackHelper.isHoveringWithJetpack(self);
    }

    // Allow walking off edges when holding shift when hovering with jetpack
    @WrapOperation(method = "maybeBackOffFromEdge",
                   at = @At(value = "FIELD",
                            target = "Lnet/minecraft/world/entity/player/Abilities;flying:Z",
                            opcode = Opcodes.GETFIELD))
    private boolean shouldAllowBackingOffEdge(Abilities instance, Operation<Boolean> original) {
        Player self = (Player) (Object) this;
        return original.call(instance) || JetpackHelper.isHoveringWithJetpack(self);
    }

    // Do not let the block we are hovering above of with jetpack affect the movement speed
    @WrapOperation(method = "getBlockSpeedFactor",
                   at = @At(value = "FIELD",
                            target = "Lnet/minecraft/world/entity/player/Abilities;flying:Z",
                            opcode = Opcodes.GETFIELD))
    private boolean shouldIgnoreBlockSpeedFactor(Abilities instance, Operation<Boolean> original) {
        Player self = (Player) (Object) this;
        return original.call(instance) || JetpackHelper.isHoveringWithJetpack(self);
    }

    @WrapOperation(method = "shouldRemoveSoulSpeed",
                   at = @At(value = "FIELD",
                            target = "Lnet/minecraft/world/entity/player/Abilities;flying:Z",
                            opcode = Opcodes.GETFIELD))
    private boolean shouldRemoveSoulSpeed(Abilities instance, Operation<Boolean> original) {
        Player self = (Player) (Object) this;
        return original.call(instance) || JetpackHelper.isHoveringWithJetpack(self);
    }

    @WrapOperation(method = "onSoulSpeedBlock",
                   at = @At(value = "FIELD",
                            target = "Lnet/minecraft/world/entity/player/Abilities;flying:Z",
                            opcode = Opcodes.GETFIELD))
    private boolean shouldPreventBeingOnSoulSpeedBlock(Abilities instance, Operation<Boolean> original) {
        Player self = (Player) (Object) this;
        return original.call(instance) || JetpackHelper.isHoveringWithJetpack(self);
    }

    // Do not get pushed by fluids when hovering with jetpack
    @WrapOperation(method = "isPushedByFluid",
                   at = @At(value = "FIELD",
                            target = "Lnet/minecraft/world/entity/player/Abilities;flying:Z",
                            opcode = Opcodes.GETFIELD))
    private boolean shouldPreventPushedByFluid(Abilities instance, Operation<Boolean> original) {
        Player self = (Player) (Object) this;
        return original.call(instance) || JetpackHelper.isHoveringWithJetpack(self);
    }

    // Do not sink or move slower in fluids when hovering with jetpack
    @WrapOperation(method = "isAffectedByFluids",
                   at = @At(value = "FIELD",
                            target = "Lnet/minecraft/world/entity/player/Abilities;flying:Z",
                            opcode = Opcodes.GETFIELD))
    private boolean shouldNotBeAffectedByFluids(Abilities instance, Operation<Boolean> original) {
        Player self = (Player) (Object) this;
        return original.call(instance) || JetpackHelper.isHoveringWithJetpack(self);
    }

    // Do not start or stay swimming when hovering with jetpack
    @WrapOperation(method = "isSwimming",
                   at = @At(value = "FIELD",
                            target = "Lnet/minecraft/world/entity/player/Abilities;flying:Z",
                            opcode = Opcodes.GETFIELD))
    private boolean shouldPreventSwimming(Abilities instance, Operation<Boolean> original) {
        Player self = (Player) (Object) this;
        return original.call(instance) || JetpackHelper.isHoveringWithJetpack(self);
    }

    @WrapOperation(method = "updateSwimming",
                   at = @At(value = "FIELD",
                            target = "Lnet/minecraft/world/entity/player/Abilities;flying:Z",
                            opcode = Opcodes.GETFIELD))
    private boolean shouldStopSwimming(Abilities instance, Operation<Boolean> original) {
        Player self = (Player) (Object) this;
        return original.call(instance) || JetpackHelper.isHoveringWithJetpack(self);
    }

    // Do not make movement sounds when hovering with jetpack
    @WrapOperation(method = "getMovementEmission",
                   at = @At(value = "FIELD",
                            target = "Lnet/minecraft/world/entity/player/Abilities;flying:Z",
                            opcode = Opcodes.GETFIELD))
    private boolean shouldHaveNoMovementEmission(Abilities instance, Operation<Boolean> original) {
        Player self = (Player) (Object) this;
        JetpackHelper jetpackHelper = JetpackHelper.getActiveJetpackInstance(self);
        return original.call(instance) || (jetpackHelper != null && jetpackHelper.isHovering() &&
                !jetpackHelper.getJetpackStats().shouldProduceSoundEmissions());
    }
}
