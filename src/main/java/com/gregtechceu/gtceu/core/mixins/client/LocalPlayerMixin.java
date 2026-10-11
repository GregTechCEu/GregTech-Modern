package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.client.GTKeyMappings;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.packets.CPacketJetpackConfiguration;
import com.gregtechceu.gtceu.common.network.packets.CPacketPlayerCommand;
import com.gregtechceu.gtceu.core.IGTSyncedDataPlayer;
import com.gregtechceu.gtceu.utils.JetpackHelper;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends Player {

    @Unique
    private static final float SAFETY_BRAKING_ACTIVATION_FALL_DISTANCE = 5.0f;

    public LocalPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }

    @Shadow
    protected boolean isControlledCamera() {
        throw new UnsupportedOperationException();
    }

    @Unique
    private void gtceu$handleKeybindings() {
        LocalPlayer self = (LocalPlayer) (Object) this;

        // Have to consume even with no active jetpack, otherwise key presses get accumulated
        boolean toggleFlight = false;
        while (GTKeyMappings.JETPACK_ENABLE.consumeClick()) {
            toggleFlight = !toggleFlight;
        }
        boolean toggleHoverMode = false;
        while (GTKeyMappings.TOGGLE_HOVER_MODE.consumeClick()) {
            toggleHoverMode = !toggleHoverMode;
        }

        var jetpackHelper = JetpackHelper.getPreferredJetpackInstance(self);
        if (jetpackHelper != null && isControlledCamera()) {
            boolean supportsHoverMode = jetpackHelper.getJetpack()
                    .supportsHoverMode(jetpackHelper.getContext().stack());

            if (toggleFlight) {
                boolean flightEnabled = !jetpackHelper.getJetpack().isFlightEnabled(jetpackHelper.getContext().stack());
                jetpackHelper.getJetpack().setFlightEnabled(jetpackHelper.getContext().stack(), flightEnabled);
                GTNetwork.sendToServer(new CPacketJetpackConfiguration(jetpackHelper.getContext().slot(),
                        CPacketJetpackConfiguration.Setting.FLIGHT_ENABLED, flightEnabled));
                self.displayClientMessage(Component.translatable(
                        "metaarmor.jetpack.flight." + (flightEnabled ? "enable" : "disable")), true);
            }

            if (supportsHoverMode && toggleHoverMode) {
                boolean hoverModeEnabled = !jetpackHelper.getJetpack()
                        .isHoverModeEnabled(jetpackHelper.getContext().stack());
                jetpackHelper.getJetpack().setHoverModeEnabled(jetpackHelper.getContext().stack(), hoverModeEnabled);
                GTNetwork.sendToServer(new CPacketJetpackConfiguration(jetpackHelper.getContext().slot(),
                        CPacketJetpackConfiguration.Setting.HOVER_MODE_ENABLED, hoverModeEnabled));
                self.displayClientMessage(Component.translatable(
                        "metaarmor.jetpack.hover." + (hoverModeEnabled ? "enable" : "disable")), true);
            }
        }
    }

    @Unique
    private void gtceu$handleEmergencyFlight() {
        LocalPlayer self = (LocalPlayer) (Object) this;

        if (self.getY() < self.level().getMinBuildHeight() && JetpackHelper.getActiveJetpackInstance(self) == null) {
            var usableJetpack = JetpackHelper.getUsableJetpackInstance(self);
            if (usableJetpack != null) {
                usableJetpack.getJetpack().setFlightEnabled(usableJetpack.getContext().stack(), true);
                GTNetwork.sendToServer(new CPacketJetpackConfiguration(usableJetpack.getContext().slot(),
                        CPacketJetpackConfiguration.Setting.FLIGHT_ENABLED, true));
                self.displayClientMessage(Component.translatable("metaarmor.jetpack.emergency_hover_mode"), true);
            }
        }
    }

    @Unique
    private void gtceu$syncJetpackInputs(@Nullable JetpackHelper jetpackHelper) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        IGTSyncedDataPlayer syncedDataAccessor = (IGTSyncedDataPlayer) this;

        // Update thrusting state for jetpack without hover-mode enabled
        boolean newIsJetpackThrusting = false;
        if (jetpackHelper != null && !jetpackHelper.isHoverModeEnabled() && isControlledCamera()) {
            newIsJetpackThrusting = self.input.jumping;
        }
        if (syncedDataAccessor.gtceu$isJetpackThrusting() != newIsJetpackThrusting) {
            syncedDataAccessor.gtceu$setJetpackThrusting(newIsJetpackThrusting);
            var action = newIsJetpackThrusting ? CPacketPlayerCommand.Action.START_JETPACK_THRUSTING :
                    CPacketPlayerCommand.Action.STOP_JETPACK_THRUSTING;
            GTNetwork.sendToServer(new CPacketPlayerCommand(action));
        }

        boolean newIsSafetyBraking = syncedDataAccessor.gtceu$isSafetyBraking();
        if (jetpackHelper == null || jetpackHelper.isHoverModeEnabled() || !isControlledCamera() ||
                self.onGround() || newIsJetpackThrusting) {
            newIsSafetyBraking = false;
        } else if (self.fallDistance > SAFETY_BRAKING_ACTIVATION_FALL_DISTANCE ||
                self.getY() < self.level().getMinBuildHeight()) {
                    newIsSafetyBraking = true;
                }
        if (syncedDataAccessor.gtceu$isSafetyBraking() != newIsSafetyBraking) {
            syncedDataAccessor.gtceu$setSafetyBraking(newIsSafetyBraking);
            var action = newIsSafetyBraking ? CPacketPlayerCommand.Action.START_SAFETY_BRAKING :
                    CPacketPlayerCommand.Action.STOP_SAFETY_BRAKING;
            GTNetwork.sendToServer(new CPacketPlayerCommand(action));
        }

        // Update precision hovering state - slow horizontal movement while hovering
        boolean newIsPrecisionHovering = false;
        if (jetpackHelper != null && jetpackHelper.isHoverModeEnabled() && isControlledCamera()) {
            newIsPrecisionHovering = self.input.shiftKeyDown && self.input.jumping;
        }
        if (syncedDataAccessor.gtceu$isPrecisionHovering() != newIsPrecisionHovering) {
            syncedDataAccessor.gtceu$setPrecisionHovering(newIsPrecisionHovering);
            var action = newIsPrecisionHovering ? CPacketPlayerCommand.Action.START_PRECISION_HOVERING :
                    CPacketPlayerCommand.Action.STOP_PRECISION_HOVERING;
            GTNetwork.sendToServer(new CPacketPlayerCommand(action));
        }
    }

    @Unique
    private void gtceu$handleJetpackControls(JetpackHelper jetpackHelper) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        IGTSyncedDataPlayer syncedDataAccessor = (IGTSyncedDataPlayer) this;

        // Hover mode vertical controls
        if (jetpackHelper.isHoverModeEnabled()) {
            int verticalMovementDirection = 0;

            if (!syncedDataAccessor.gtceu$isPrecisionHovering()) {
                if (self.input.shiftKeyDown) {
                    --verticalMovementDirection;
                }
                if (self.input.jumping) {
                    ++verticalMovementDirection;
                }
            }

            if (verticalMovementDirection != 0) {
                Vec3 verticalMovementDelta = jetpackHelper.getVerticalHoverMovementDelta(verticalMovementDirection);
                self.addDeltaMovement(verticalMovementDelta);
            }
        } else {
            // Jetpack thrusting input
            if (syncedDataAccessor.gtceu$isJetpackThrusting() && !jetpackHelper.isHoldingAltitude()) {
                self.setDeltaMovement(jetpackHelper.applyVerticalThrust(self.getDeltaMovement()));
            }
        }
    }

    @Inject(method = "aiStep", at = @At(value = "TAIL"))
    private void tick(CallbackInfo callbackInfo) {
        LocalPlayer self = (LocalPlayer) (Object) this;

        // Consume keybindings
        gtceu$handleKeybindings();

        // Turn on a disabled jetpack when falling out of world
        gtceu$handleEmergencyFlight();

        var jetpackHelper = JetpackHelper.getActiveJetpackInstance(self);
        // Sync inputs for the active jetpack
        gtceu$syncJetpackInputs(jetpackHelper);

        // Handle jetpack movement control
        if (jetpackHelper != null && isControlledCamera()) {
            gtceu$handleJetpackControls(jetpackHelper);
        }

        // Spawn visual effects
        if (jetpackHelper != null && jetpackHelper.isFlyingWithJetpack()) {
            jetpackHelper.spawnLocalEffects(self.getDeltaMovement());
        }
    }

    // Do not allow crouching while flying with jetpack (this does not prevent other shift down-related behaviors)
    @WrapOperation(method = "aiStep",
                   at = @At(value = "FIELD",
                            target = "Lnet/minecraft/client/player/LocalPlayer;crouching:Z",
                            opcode = Opcodes.PUTFIELD))
    private void shouldAllowCrouching(LocalPlayer instance, boolean value, Operation<Void> original) {
        original.call(instance, value && !JetpackHelper.isFlyingWithJetpack(instance));
    }

    // Prevent starting fall flying (elytra flight) when using jetpack
    @WrapOperation(method = "aiStep",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/world/item/ItemStack;canElytraFly(Lnet/minecraft/world/entity/LivingEntity;)Z",
                            remap = false))
    private boolean shouldAllowElytraFlight(ItemStack instance, LivingEntity entity, Operation<Boolean> original) {
        return original.call(instance, entity) && !JetpackHelper.isFlyingWithJetpack((Player) entity);
    }

    // Do not slide down ladders when hovering with jetpack
    @WrapOperation(method = "isSuppressingSlidingDownLadder",
                   at = @At(value = "FIELD",
                            target = "Lnet/minecraft/world/entity/player/Abilities;flying:Z",
                            opcode = Opcodes.GETFIELD))
    private boolean shouldSuppressSlidingDownLadder(Abilities instance, Operation<Boolean> original) {
        Player self = (Player) (Object) this;
        return original.call(instance) || JetpackHelper.isHoveringWithJetpack(self);
    }

    // Do not spawn sprint particles when hovering above the ground with jetpack
    @WrapOperation(method = "canSpawnSprintParticle",
                   at = @At(value = "FIELD",
                            target = "Lnet/minecraft/world/entity/player/Abilities;flying:Z",
                            opcode = Opcodes.GETFIELD))
    private boolean shouldPreventSpawningSprintParticle(Abilities instance, Operation<Boolean> original) {
        Player self = (Player) (Object) this;
        return original.call(instance) || JetpackHelper.isHoveringWithJetpack(self);
    }
}
