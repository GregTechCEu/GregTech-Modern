package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.utils.input.SyncedKeyMappings;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Logic from
 * <a href=
 * "https://github.com/Tomson124/SimplyJetpacks2/blob/1.12/src/main/java/tonius/simplyjetpacks/item/ItemJetpack.java">SimplyJetpacks2</a>
 */
public interface IJetpack {

    default double getSprintEnergyModifier() {
        return 1.0D;
    }

    default double getSprintSpeedModifier() {
        return 1.0D;
    }

    default double getVerticalHoverSpeed() {
        return 0.18D;
    }

    default double getVerticalHoverSlowSpeed() {
        return 0.14D;
    }

    default double getVerticalAcceleration() {
        return 0.1D;
    }

    default double getVerticalSpeed() {
        return 0.22D;
    }

    default double getSidewaysSpeed() {
        return 0.0D;
    }

    default boolean removeMiningSpeedPenalty() {
        return true;
    }

    @Nullable
    default ParticleOptions getParticle() {
        return ParticleTypes.LARGE_SMOKE;
    }

    default float getFallDamageReduction() {
        return 0.0f;
    }

    int getEnergyPerUse();

    boolean canUseEnergy(ItemStack stack, int amount);

    void drainEnergy(ItemStack stack, int amount);

    boolean hasEnergy(ItemStack stack);

    default void performFlying(@NotNull Player player, boolean flightEnabled, boolean hover, ItemStack stack) {
        double deltaY = player.getDeltaMovement().y();

        if ((!flightEnabled || !hover) && player.getY() < player.level().getMinBuildHeight() - 5) {
            performEHover(stack, player);
        } else if (!flightEnabled) {
            return;
        }

        boolean flyKeyDown = SyncedKeyMappings.VANILLA_JUMP.isKeyDown(player);
        boolean descendKeyDown = SyncedKeyMappings.VANILLA_SNEAK.isKeyDown(player);
        double currentAccel = getVerticalAcceleration() * (deltaY < 0.3D ? 2.5D : 1.0D);

        if (!player.onGround() && player.getSleepingPos().isEmpty() && canUseEnergy(stack, getEnergyPerUse())) {
            double potentialY = 0;
            boolean editMotion = true;

            if (hover) {
                if (flyKeyDown && descendKeyDown) {
                    potentialY = getVerticalHoverSlowSpeed();
                } else if (flyKeyDown) {
                    potentialY = getVerticalHoverSpeed();
                } else if (descendKeyDown) {
                    potentialY = -getVerticalHoverSpeed();
                } else {
                    potentialY = -getVerticalHoverSlowSpeed();
                }

                if (player.isFallFlying()) { // if the player is hovering negate fall motion
                    player.stopFallFlying();
                }
            } else {
                if (flyKeyDown && descendKeyDown) {
                    potentialY = 0;
                } else if (flyKeyDown) {
                    potentialY = getVerticalSpeed() * (player.isInWater() ? 0.4D : 1.0D);
                } else { // Free fall, don't need to edit motion
                    editMotion = false;
                }
            }

            if (editMotion) {
                potentialY = Math.min(deltaY + currentAccel, potentialY);
                setYMotion(player, potentialY);
            }

            float speedSideways = (float) (player.isShiftKeyDown() ? getSidewaysSpeed() * 0.5f :
                    getSidewaysSpeed());
            float speedForward = (float) (player.isSprinting() ? speedSideways * getSprintSpeedModifier() :
                    speedSideways);

            // Make sure they aren't using elytra movement
            if (!player.isFallFlying()) {
                Vec3 movement = new Vec3(0, 0, 0);
                if (SyncedKeyMappings.VANILLA_FORWARD.isKeyDown(player)) {
                    movement = movement.add(0, 0, speedForward);
                }
                if (SyncedKeyMappings.VANILLA_BACKWARD.isKeyDown(player)) {
                    movement = movement.add(0, 0, -speedSideways * 0.8f);
                }
                if (SyncedKeyMappings.VANILLA_LEFT.isKeyDown(player)) {
                    movement = movement.add(speedSideways, 0, 0);
                }
                if (SyncedKeyMappings.VANILLA_RIGHT.isKeyDown(player)) {
                    movement = movement.add(-speedSideways, 0, 0);
                }

                var dist = movement.length();
                if (dist >= 1.0E-7) {
                    player.moveRelative((float) dist, movement);
                    if (!editMotion) editMotion = true;
                }
            }

            if (editMotion) {
                int energyUsed = (int) Math
                        .round(getEnergyPerUse() * (player.isSprinting() ? getSprintEnergyModifier() : 1));
                drainEnergy(stack, energyUsed);
                ArmorUtils.spawnParticle(player.level(), player, getParticle(), -0.6D);
            }

            // ensure that the player is actually using the jetpack to cancel fall damage
            if (!player.level().isClientSide && (hover || flyKeyDown)) {
                player.fallDistance = 0;
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.aboveGroundTickCount = 0;
                }
            }

        }
    }

    private static void setYMotion(Player player, double value) {
        var motion = player.getDeltaMovement();
        player.setDeltaMovement(motion.x(), value, motion.z());
    }

    static void performEHover(ItemStack stack, Player player) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean("enabled", true);
        tag.putBoolean("hover", true);
        player.displayClientMessage(Component.translatable("metaarmor.jetpack.emergency_hover_mode"), true);
        player.fallDistance = 0;

        if (!player.level().isClientSide) {
            if (player instanceof ServerPlayer) {
                ((ServerPlayer) player).connection.aboveGroundTickCount = 0;
            }
        }

        player.inventoryMenu.sendAllDataToRemote();
    }

    private static void addYMotion(Player player, double value) {
        var motion = player.getDeltaMovement();
        player.addDeltaMovement(new Vec3(motion.x(), value, motion.z()));
    }
}
