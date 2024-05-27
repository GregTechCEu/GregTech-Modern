package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.utils.input.KeyBind;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
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

    default void performFlying(@NotNull Player player, boolean hover, ItemStack stack) {
        double currentAccel = getVerticalAcceleration() * (player.getDeltaMovement().y < 0.3D ? 2.5D : 1.0D);
        double currentSpeedVertical = getVerticalSpeed() * (player.isInWater() ? 0.4D : 1.0D);
        boolean flyKeyDown = KeyBind.VANILLA_JUMP.isKeyDown(player);
        boolean descendKeyDown = KeyBind.VANILLA_SNEAK.isKeyDown(player);

        if (!player.isInWater() && !player.isInLava() && canUseEnergy(stack, getEnergyPerUse())) {
            if (flyKeyDown || hover && !player.onGround()) {
                drainEnergy(stack, (int) (player.isSprinting() ?
                        Math.round(getEnergyPerUse() * getSprintEnergyModifier()) : getEnergyPerUse()));

                if (hasEnergy(stack)) {
                    Vec3 delta = player.getDeltaMovement();
                    if (flyKeyDown) {
                        if (!hover) {
                            player.setDeltaMovement(delta.x, Math.min(delta.y + currentAccel, currentSpeedVertical),
                                    delta.z);
                        } else {
                            if (descendKeyDown) {
                                player.setDeltaMovement(delta.x,
                                        Math.min(delta.y + currentAccel, getVerticalHoverSlowSpeed()), delta.z);
                            } else {
                                player.setDeltaMovement(delta.x,
                                        Math.min(delta.y + currentAccel, getVerticalHoverSpeed()), delta.z);
                            }
                        }
                    } else if (descendKeyDown) {
                        player.setDeltaMovement(delta.x, Math.min(delta.y + currentAccel, -getVerticalHoverSpeed()),
                                delta.z);
                    } else {
                        player.setDeltaMovement(delta.x, Math.min(delta.y + currentAccel, -getVerticalHoverSlowSpeed()),
                                delta.z);
                    }
                    float speedSideways = (float) (player.isShiftKeyDown() ? getSidewaysSpeed() * 0.5f :
                            getSidewaysSpeed());
                    float speedForward = (float) (player.isSprinting() ? speedSideways * getSprintSpeedModifier() :
                            speedSideways);

                    player.hurtMarked = true;
                    if (KeyBind.VANILLA_FORWARD.isKeyDown(player))
                        player.moveRelative(speedForward, new Vec3(0, 0, speedForward));
                    if (KeyBind.VANILLA_BACKWARD.isKeyDown(player))
                        player.moveRelative(speedSideways * 0.8f, new Vec3(0, 0, -speedForward));
                    if (KeyBind.VANILLA_LEFT.isKeyDown(player))
                        player.moveRelative(speedSideways, new Vec3(speedSideways, 0, 0));
                    if (KeyBind.VANILLA_RIGHT.isKeyDown(player))
                        player.moveRelative(-speedSideways, new Vec3(speedSideways, 0, 0));
                    if (!player.level().isClientSide) {
                        player.fallDistance = 0;
                    }

                }
                ArmorUtils.spawnParticle(player.level(), player, getParticle(), -0.6D);
            }
        }
    }
}
