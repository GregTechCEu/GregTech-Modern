package com.gregtechceu.gtceu.client;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.common.capability.PlayerJetpackHandler;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.packets.CPacketJetpackConfiguration;
import com.gregtechceu.gtceu.common.network.packets.CPacketPlayerCommand;
import com.gregtechceu.gtceu.utils.JetpackHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class ClientJetpackHandler {

    private static final float SAFETY_BRAKING_ACTIVATION_FALL_DISTANCE = 5.0f;

    public static void tick(LocalPlayer player) {
        boolean isControlledCamera = Minecraft.getInstance().getCameraEntity() == player;

        // Consume keybindings
        handleKeybindings(player, isControlledCamera);

        // Turn on a disabled jetpack when falling out of world
        handleEmergencyFlight(player);

        PlayerJetpackHandler jetpackHandler = GTCapabilityHelper.getPlayerJetpackHandler(player);
        if (jetpackHandler == null) {
            return;
        }
        var jetpackHelper = JetpackHelper.getActiveJetpackInstance(player);
        // Sync inputs for the active jetpack
        syncJetpackInputs(player, jetpackHandler, jetpackHelper, isControlledCamera);

        // Handle jetpack movement control
        if (jetpackHelper != null && isControlledCamera) {
            handleJetpackControls(player, jetpackHandler, jetpackHelper);
        }

        // Spawn visual effects
        if (jetpackHelper != null && jetpackHelper.isFlyingWithJetpack()) {
            jetpackHelper.spawnLocalEffects(player.getDeltaMovement());
        }
    }

    private static void handleKeybindings(LocalPlayer player, boolean isControlledCamera) {
        // Have to consume even with no active jetpack, otherwise key presses get accumulated
        boolean toggleFlight = false;
        while (GTKeyMappings.JETPACK_ENABLE.consumeClick()) {
            toggleFlight = !toggleFlight;
        }
        boolean toggleHoverMode = false;
        while (GTKeyMappings.TOGGLE_HOVER_MODE.consumeClick()) {
            toggleHoverMode = !toggleHoverMode;
        }

        var jetpackHelper = JetpackHelper.getPreferredJetpackInstance(player);
        if (jetpackHelper != null && isControlledCamera) {
            boolean supportsHoverMode = jetpackHelper.getJetpack()
                    .supportsHoverMode(jetpackHelper.getContext().stack());

            if (toggleFlight) {
                boolean flightEnabled = !jetpackHelper.getJetpack().isFlightEnabled(jetpackHelper.getContext().stack());
                jetpackHelper.getJetpack().setFlightEnabled(jetpackHelper.getContext().stack(), flightEnabled);
                GTNetwork.sendToServer(new CPacketJetpackConfiguration(jetpackHelper.getContext().slot(),
                        CPacketJetpackConfiguration.Setting.FLIGHT_ENABLED, flightEnabled));
                player.displayClientMessage(Component.translatable(
                        "metaarmor.jetpack.flight." + (flightEnabled ? "enable" : "disable")), true);
            }

            if (supportsHoverMode && toggleHoverMode) {
                boolean hoverModeEnabled = !jetpackHelper.getJetpack()
                        .isHoverModeEnabled(jetpackHelper.getContext().stack());
                jetpackHelper.getJetpack().setHoverModeEnabled(jetpackHelper.getContext().stack(), hoverModeEnabled);
                GTNetwork.sendToServer(new CPacketJetpackConfiguration(jetpackHelper.getContext().slot(),
                        CPacketJetpackConfiguration.Setting.HOVER_MODE_ENABLED, hoverModeEnabled));
                player.displayClientMessage(Component.translatable(
                        "metaarmor.jetpack.hover." + (hoverModeEnabled ? "enable" : "disable")), true);
            }
        }
    }

    private static void handleEmergencyFlight(LocalPlayer player) {
        if (player.getY() < player.level().getMinBuildHeight() &&
                JetpackHelper.getActiveJetpackInstance(player) == null) {
            var usableJetpack = JetpackHelper.getUsableJetpackInstance(player);
            if (usableJetpack != null) {
                usableJetpack.getJetpack().setFlightEnabled(usableJetpack.getContext().stack(), true);
                GTNetwork.sendToServer(new CPacketJetpackConfiguration(usableJetpack.getContext().slot(),
                        CPacketJetpackConfiguration.Setting.FLIGHT_ENABLED, true));
                player.displayClientMessage(Component.translatable("metaarmor.jetpack.emergency_hover_mode"), true);
            }
        }
    }

    private static void syncJetpackInputs(LocalPlayer player, PlayerJetpackHandler jetpackHandler,
                                          @Nullable JetpackHelper jetpackHelper, boolean isControlledCamera) {
        // Update thrusting state for jetpack without hover-mode enabled
        boolean newIsJetpackThrusting = false;
        if (jetpackHelper != null && !jetpackHelper.isHoverModeEnabled() && isControlledCamera) {
            newIsJetpackThrusting = player.input.jumping;
        }
        if (jetpackHandler.isJetpackThrusting() != newIsJetpackThrusting) {
            jetpackHandler.setJetpackThrusting(newIsJetpackThrusting);
            var action = newIsJetpackThrusting ? CPacketPlayerCommand.Action.START_JETPACK_THRUSTING :
                    CPacketPlayerCommand.Action.STOP_JETPACK_THRUSTING;
            GTNetwork.sendToServer(new CPacketPlayerCommand(action));
        }

        boolean newIsSafetyBraking = jetpackHandler.isSafetyBraking();
        if (jetpackHelper == null || jetpackHelper.isHoverModeEnabled() || !isControlledCamera ||
                player.onGround() || newIsJetpackThrusting) {
            newIsSafetyBraking = false;
        } else if (player.fallDistance > SAFETY_BRAKING_ACTIVATION_FALL_DISTANCE ||
                player.getY() < player.level().getMinBuildHeight()) {
                    newIsSafetyBraking = true;
                }
        if (jetpackHandler.isSafetyBraking() != newIsSafetyBraking) {
            jetpackHandler.setSafetyBraking(newIsSafetyBraking);
            var action = newIsSafetyBraking ? CPacketPlayerCommand.Action.START_SAFETY_BRAKING :
                    CPacketPlayerCommand.Action.STOP_SAFETY_BRAKING;
            GTNetwork.sendToServer(new CPacketPlayerCommand(action));
        }

        // Update precision hovering state - slow horizontal movement while hovering
        boolean newIsPrecisionHovering = false;
        if (jetpackHelper != null && jetpackHelper.isHoverModeEnabled() && isControlledCamera) {
            newIsPrecisionHovering = player.input.shiftKeyDown && player.input.jumping;
        }
        if (jetpackHandler.isPrecisionHovering() != newIsPrecisionHovering) {
            jetpackHandler.setPrecisionHovering(newIsPrecisionHovering);
            var action = newIsPrecisionHovering ? CPacketPlayerCommand.Action.START_PRECISION_HOVERING :
                    CPacketPlayerCommand.Action.STOP_PRECISION_HOVERING;
            GTNetwork.sendToServer(new CPacketPlayerCommand(action));
        }
    }

    private static void handleJetpackControls(LocalPlayer player, PlayerJetpackHandler jetpackHandler,
                                              JetpackHelper jetpackHelper) {
        // Hover mode vertical controls
        if (jetpackHelper.isHoverModeEnabled()) {
            int verticalMovementDirection = 0;

            if (!jetpackHandler.isPrecisionHovering()) {
                if (player.input.shiftKeyDown) {
                    --verticalMovementDirection;
                }
                if (player.input.jumping) {
                    ++verticalMovementDirection;
                }
            }

            if (verticalMovementDirection != 0) {
                Vec3 verticalMovementDelta = jetpackHelper.getVerticalHoverMovementDelta(verticalMovementDirection);
                player.addDeltaMovement(verticalMovementDelta);
            }
        } else {
            // Jetpack thrusting input
            if (jetpackHandler.isJetpackThrusting() && !jetpackHelper.isHoldingAltitude()) {
                player.setDeltaMovement(jetpackHelper.applyVerticalThrust(player.getDeltaMovement()));
            }
        }
    }
}
