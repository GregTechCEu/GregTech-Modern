package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.api.misc.InputHandler;
import com.gregtechceu.gtceu.core.mixins.ServerPlayNetworkHandlerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadLocalRandom;


/**
 * Logic from SimplyJetpacks2: https://github.com/Tomson124/SimplyJetpacks2/blob/1.12/src/main/java/tonius/simplyjetpacks/item/ItemJetpack.java
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

    default SimpleParticleType getParticle() {
        return ParticleTypes.SMOKE;
    }

    default float getFallDamageReduction() {
        return 0.0f;
    }

    int getEnergyPerUse();

    boolean canUseEnergy(ItemStack stack, int amount);

    void drainEnergy(ItemStack stack, int amount);

    boolean hasEnergy(ItemStack stack);

    default void performFlying(@Nonnull Player player, boolean hover, ItemStack stack) {
        Vec3 deltaMovement = player.getDeltaMovement();
        double motionY = deltaMovement.y();
        double currentAccel = getVerticalAcceleration() * (motionY < 0.3D ? 2.5D : 1.0D);
        double currentSpeedVertical = getVerticalSpeed() * (player.isInWater() ? 0.4D : 1.0D);
        boolean flyKeyDown = InputHandler.isHoldingUp(player);
        boolean descendKeyDown = InputHandler.isHoldingDown(player);

        if (!player.isInWater() && !player.isInLava() && canUseEnergy(stack, getEnergyPerUse())) {
            if (flyKeyDown || hover && !player.onGround()) {
                drainEnergy(stack, (int) (player.isSprinting() ? Math.round(getEnergyPerUse() * getSprintEnergyModifier()) : getEnergyPerUse()));

                if (hasEnergy(stack)) {
                    if (flyKeyDown) {
                        if (!hover) {
                            player.setDeltaMovement(deltaMovement.x, Math.min(motionY + currentAccel, currentSpeedVertical),deltaMovement.z);
                        } else {
                            if (descendKeyDown) player.setDeltaMovement(deltaMovement.x, Math.min(motionY + currentAccel, getVerticalHoverSlowSpeed()),deltaMovement.z);
                            else player.setDeltaMovement(deltaMovement.x, Math.min(motionY + currentAccel, getVerticalHoverSpeed()),deltaMovement.z);
                        }
                    } else if (descendKeyDown) {
                        player.setDeltaMovement(deltaMovement.x, Math.min(motionY + currentAccel, -getVerticalHoverSpeed()),deltaMovement.z);
                    } else {
                        player.setDeltaMovement(deltaMovement.x, Math.min(motionY + currentAccel, -getVerticalHoverSlowSpeed()),deltaMovement.z);
                    }
                    float speedSideways = (float) (player.isShiftKeyDown() ? getSidewaysSpeed() * 0.5f : getSidewaysSpeed());
                    float speedForward = (float) (player.isSprinting() ? speedSideways * getSprintSpeedModifier() : speedSideways);

                    if (InputHandler.isHoldingForwards(player))
                        player.moveRelative(1, new Vec3(0, 0, speedForward));
                    if (InputHandler.isHoldingBackwards(player))
                        player.moveRelative(1, new Vec3(0, 0, -speedSideways * 0.8F));
                    if (InputHandler.isHoldingLeft(player))
                        player.moveRelative(1, new Vec3(speedSideways, 0, 0));
                    if (InputHandler.isHoldingRight(player))
                        player.moveRelative(1, new Vec3(-speedSideways, 0, 0));
                    if (!player.level().isClientSide()) {
                        player.fallDistance = 0.0F;
                        if (player instanceof ServerPlayer) {
                            ((ServerPlayNetworkHandlerAccessor) ((ServerPlayer) player).connection).setFloatingTicks(0);
                        }
                    }

                }
                float random = (ThreadLocalRandom.current().nextFloat() - 0.5F) * 0.1F;
                var mc = Minecraft.getInstance();
                var playerPos = mc.player.position().add(0, 1.5, 0);
                double[] sneakBonus = mc.player.isCrouching() ? new double[] { -0.30, -0.10 } : new double[] { 0, 0 };
                Vec3 vLeft = new Vec3(-0.18, -0.90 + sneakBonus[1], -0.30 + sneakBonus[0]).xRot(0).yRot(mc.player.yBodyRot * -0.017453292F);
                Vec3 vRight = new Vec3(0.18, -0.90 + sneakBonus[1], -0.30 + sneakBonus[0]).xRot(0).yRot(mc.player.yBodyRot * -0.017453292F);

                var v = playerPos.add(vLeft).add(mc.player.getDeltaMovement().scale(getVerticalSpeed()));
                mc.particleEngine.createParticle(ParticleTypes.FLAME, v.x, v.y, v.z, random, -0.2D, random);
                mc.particleEngine.createParticle(getParticle(), v.x, v.y, v.z, random, -0.2D, random);

                v = playerPos.add(vRight).add(mc.player.getDeltaMovement().scale(getVerticalSpeed()));
                mc.particleEngine.createParticle(ParticleTypes.FLAME, v.x, v.y, v.z, random, -0.2D, random);
                mc.particleEngine.createParticle(getParticle(), v.x, v.y, v.z, random, -0.2D, random);
            }
        }
    }
}
