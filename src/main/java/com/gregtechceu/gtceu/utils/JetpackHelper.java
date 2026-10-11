package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.item.jetpack.IJetpackLogic;
import com.gregtechceu.gtceu.api.item.jetpack.IJetpackLogic.JetpackUseContext;
import com.gregtechceu.gtceu.core.IGTSyncedDataPlayer;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class JetpackHelper {

    @Getter
    private final JetpackUseContext context;
    @Getter
    private final IJetpackLogic jetpack;
    @Getter
    private final IJetpackLogic.JetpackStats jetpackStats;
    @Getter
    private final boolean hoverModeEnabled;

    private JetpackHelper(JetpackUseContext context, IJetpackLogic jetpack) {
        this.context = context;
        this.jetpack = jetpack;
        this.jetpackStats = jetpack.getJetpackStats(context);
        this.hoverModeEnabled = jetpack.isHoverModeEnabled(context.stack());
    }

    private boolean canFlyWithJetpack(boolean checkForFlightEnabled) {
        if (this.context.player().isDeadOrDying() || this.context.player().getAbilities().mayfly ||
                this.context.player().isPassenger()) {
            return false;
        }
        if (checkForFlightEnabled && !this.jetpack.isFlightEnabled(this.context.stack())) {
            return false;
        }
        int flightEnergyUsage = this.jetpackStats.energyUsagePerTick();
        return this.jetpack.canUseEnergy(this.context, flightEnergyUsage);
    }

    public boolean isFlyingWithJetpack() {
        IGTSyncedDataPlayer syncedDataAccessor = (IGTSyncedDataPlayer) this.context.player();
        if (isHoverModeEnabled()) {
            return !this.context.player().onGround();
        }
        return syncedDataAccessor.gtceu$isJetpackThrusting() || syncedDataAccessor.gtceu$isSafetyBraking();
    }

    public boolean isHoldingAltitude() {
        IGTSyncedDataPlayer syncedDataAccessor = (IGTSyncedDataPlayer) this.context.player();
        return !isHoverModeEnabled() && syncedDataAccessor.gtceu$isJetpackThrusting() &&
                this.context.player().isShiftKeyDown();
    }

    /** @return true if player is hovering with the jetpack, e.g. has hover mode enabled or is holding altitude */
    public boolean isHovering() {
        return isFlyingWithJetpack() && (isHoverModeEnabled() || isHoldingAltitude());
    }

    private int getEffectiveEnergyConsumption() {
        if (context.player().isSprinting()) {
            int sprintEnergyUsage = this.jetpackStats.energyUsagePerTickWhenSprinting();
            if (this.jetpack.canUseEnergy(this.context, sprintEnergyUsage)) {
                return sprintEnergyUsage;
            }
        }
        return this.jetpackStats.energyUsagePerTick();
    }

    private double getEffectiveSpeedMultiplier() {
        if (context.player().isSprinting()) {
            int sprintEnergyUsage = this.jetpackStats.energyUsagePerTickWhenSprinting();
            if (this.jetpack.canUseEnergy(this.context, sprintEnergyUsage)) {
                return this.jetpackStats.sprintSpeedMultiplier();
            }
        }
        return 1.0;
    }

    public Vec3 getVerticalHoverMovementDelta(int movementDirection) {
        double effectiveVerticalSpeed = this.jetpackStats.verticalSpeed() * getEffectiveSpeedMultiplier();
        return new Vec3(0.0, effectiveVerticalSpeed * movementDirection, 0.0);
    }

    public Vec3 applyVerticalThrust(Vec3 movementDelta) {
        double effectiveVerticalSpeed = this.jetpackStats.verticalSpeed() * getEffectiveSpeedMultiplier();
        double acceleration = this.jetpackStats.verticalAcceleration() * (movementDelta.y < 0.3 ? 2.5 : 1.0);
        double newVerticalSpeed = Math.min(movementDelta.y + acceleration, effectiveVerticalSpeed);
        return new Vec3(movementDelta.x, newVerticalSpeed, movementDelta.z);
    }

    public double getEffectiveHorizontalFlySpeed() {
        if (isHoldingAltitude()) {
            return this.jetpackStats.altitudeHoldingHorizontalSpeed();
        }
        double effectiveSpeed = this.jetpackStats.horizontalSpeed() * getEffectiveSpeedMultiplier();

        // Precision hovering allows slower horizontal movement for more precision when hovering
        IGTSyncedDataPlayer syncedDataAccessor = (IGTSyncedDataPlayer) this.context.player();
        if (syncedDataAccessor.gtceu$isPrecisionHovering()) {
            effectiveSpeed *= 0.3f;
        }
        return effectiveSpeed;
    }

    private Vec3 getFlightParticleOrigin() {
        Player player = this.context.player();
        Vec3 bodyForward = Vec3.directionFromRotation(0.0f, player.yBodyRot);
        return player.position().add(bodyForward.scale(-0.4)).add(0.0, 0.5, 0.0);
    }

    private Vec3 getFlightParticleVelocity(Vec3 playerMotion) {
        if (playerMotion.lengthSqr() < Mth.square(0.05)) {
            return new Vec3(0.0, -0.06, 0.0);
        }
        return playerMotion.normalize().scale(-0.1);
    }

    public void spawnLocalEffects(Vec3 playerMotion) {
        ParticleOptions flightParticle = this.jetpackStats.flightParticle();
        if (flightParticle != null) {
            Vec3 origin = getFlightParticleOrigin();
            Vec3 velocity = getFlightParticleVelocity(playerMotion);
            this.context.player().level().addParticle(flightParticle, origin.x, origin.y, origin.z,
                    velocity.x, velocity.y, velocity.z);
        }
    }

    public void serverSpawnEffects(Vec3 playerMotion) {
        ParticleOptions flightParticle = this.jetpackStats.flightParticle();
        Player player = this.context.player();
        if (flightParticle == null || !(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 origin = getFlightParticleOrigin();
        Vec3 velocity = getFlightParticleVelocity(playerMotion);
        for (ServerPlayer receiver : serverLevel.players()) {
            if (receiver != player) {
                // Count of 0 makes the game treat offset parameters as particle velocity
                serverLevel.sendParticles(receiver, flightParticle, false, origin.x, origin.y, origin.z, 0,
                        velocity.x, velocity.y, velocity.z, 1.0);
            }
        }
    }

    public void serverConsumeEnergy() {
        int effectiveEnergyConsumption = getEffectiveEnergyConsumption();
        if (effectiveEnergyConsumption > 0) {
            this.jetpack.drainEnergy(this.context, effectiveEnergyConsumption);
        }
    }

    /** @return true if player is actively flying with jetpack (either hovering or actively thrusting) */
    public static boolean isFlyingWithJetpack(Player player) {
        JetpackHelper jetpackHelper = getActiveJetpackInstance(player);
        return jetpackHelper != null && jetpackHelper.isFlyingWithJetpack();
    }

    /** @return true if player is actively hovering with jetpack (holding altitude or has hover mode enabled) */
    public static boolean isHoveringWithJetpack(Player player) {
        JetpackHelper jetpackHelper = getActiveJetpackInstance(player);
        return jetpackHelper != null && jetpackHelper.isHovering();
    }

    private static @Nullable JetpackHelper getFirstJetpackInstance(Player player, boolean checkForFlightEnabled,
                                                                   boolean checkUsable) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.isArmor()) {
                ItemStack stack = player.getItemBySlot(slot);

                IJetpackLogic jetpack = getJetpackLogic(stack);
                if (jetpack != null) {
                    var useContext = new JetpackUseContext(player, stack, slot);
                    var jetpackHelper = new JetpackHelper(useContext, jetpack);

                    if (checkUsable && !jetpackHelper.canFlyWithJetpack(checkForFlightEnabled)) {
                        continue;
                    }
                    return jetpackHelper;
                }
            }
        }
        return null;
    }

    /** @return the jetpack helper instance for preferred jetpack (active first, then first usable, then any) */
    public static @Nullable JetpackHelper getPreferredJetpackInstance(Player player) {
        JetpackHelper activeJetpack = getActiveJetpackInstance(player);
        if (activeJetpack != null) {
            return activeJetpack;
        }

        JetpackHelper usableJetpack = getFirstJetpackInstance(player, false, true);
        if (usableJetpack != null) {
            return usableJetpack;
        }

        return getFirstJetpackInstance(player, false, false);
    }

    public static @Nullable IJetpackLogic getJetpackLogic(ItemStack stack) {
        if (stack.getItem() instanceof ArmorComponentItem componentItem) {
            return componentItem.getArmorLogic().getJetpackLogic(stack);
        }
        return null;
    }

    /** @return the jetpack helper instance for first jetpack that could be flown with if flight was enabled */
    public static @Nullable JetpackHelper getUsableJetpackInstance(Player player) {
        return getFirstJetpackInstance(player, false, true);
    }

    /** @return the jetpack helper instance for currently active jetpack, or null if there is not one */
    public static @Nullable JetpackHelper getActiveJetpackInstance(Player player) {
        return getFirstJetpackInstance(player, true, true);
    }
}
