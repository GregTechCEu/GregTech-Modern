package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.utils.input.KeyBind;

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

         if(!hover || !flightEnabled) {
             if (player.position().y() < player.level().getMinBuildHeight() - 5) {
                 performEHover(stack, player);
             } else {
                 if (!player.isCreative() && player.fallDistance - 1.2f >= player.getHealth()) {
                     if (!player.onGround() && !player.isSwimming()) {
                         performEHover(stack, player);
                     }
                 }
             }
         }

        if (!flightEnabled) {
            return;
        }

        boolean flyKeyDown = KeyBind.VANILLA_JUMP.isKeyDown(player);
        boolean descendKeyDown = KeyBind.VANILLA_SNEAK.isKeyDown(player);

        double hoverSpeed = descendKeyDown ? getVerticalHoverSpeed() : getVerticalHoverSlowSpeed();
        double currentAccel = getVerticalAcceleration() * (deltaY < 0.3D ? 2.5D : 1.0D);
        double currentSpeedVertical = getVerticalSpeed() * (player.isInWater() ? 0.4D : 1.0D);

        // !player.isInWater() && !player.isInLava()
        if (!player.onGround() && canUseEnergy(stack, getEnergyPerUse())) {
            drainEnergy(stack, (int) (player.isSprinting() ?
                    Math.round(getEnergyPerUse() * getSprintEnergyModifier()) : getEnergyPerUse()));

            double potentialY;
            boolean editMotion = false;
            if (hasEnergy(stack)) {
                if (hover) {
                    if (flyKeyDown && descendKeyDown) {
                        potentialY = getVerticalHoverSlowSpeed();
                    } else if (flyKeyDown) {
                        potentialY = getVerticalHoverSpeed();
                    } else if (descendKeyDown) {
                        potentialY = -getVerticalHoverSpeed();
                    } else {
                        potentialY = 0.0;
                    }

                    if (player.isFallFlying()) { // if the player is hovering negate fall motion
                        player.stopFallFlying();
                    }
                    editMotion = true;
                } else {
                    if (flyKeyDown) {
                        potentialY = currentSpeedVertical;
                        editMotion = true;
                        if (descendKeyDown) {
                            potentialY -= currentSpeedVertical;
                        }
                    } else {
                        potentialY = -hoverSpeed;
                    }
                }
                potentialY = Math.min(deltaY + currentAccel, potentialY);
                if (editMotion)
                    setYMotion(player, potentialY);

                float speedSideways = (float) (player.isShiftKeyDown() ? getSidewaysSpeed() * 0.5f :
                        getSidewaysSpeed());
                float speedForward = (float) (player.isSprinting() ? speedSideways * getSprintSpeedModifier() :
                        speedSideways);

                // player.hurtMarked = true; // why is this necessary?

                // make sure they arent using elytra movement
                if (!player.isFallFlying()) {
                    if (KeyBind.VANILLA_FORWARD.isKeyDown(player)) {
                        player.moveRelative(speedForward, new Vec3(0, 0, speedForward));
                    }
                    if (KeyBind.VANILLA_BACKWARD.isKeyDown(player)) {
                        player.moveRelative(speedSideways * 0.8f, new Vec3(0, 0, -speedForward));
                    }
                    if (KeyBind.VANILLA_LEFT.isKeyDown(player)) {
                        player.moveRelative(speedSideways, new Vec3(speedSideways, 0, 0));
                    }
                    if (KeyBind.VANILLA_RIGHT.isKeyDown(player)) {
                        player.moveRelative(-speedSideways, new Vec3(speedSideways, 0, 0));
                    }
                }

                // ensure that the player is actually using the jetpack to cancel fall damage
                if (!player.level().isClientSide && (hover || flyKeyDown)) {
                    player.fallDistance = 0;
                    if (player instanceof ServerPlayer) {
                        ((ServerPlayer) player).connection.aboveGroundTickCount = 0;
                    }
                }

            }
            ArmorUtils.spawnParticle(player.level(), player, getParticle(), -0.6D);
        }
    }

    private static void setYMotion(Player player, double value) {
        var motion = player.getDeltaMovement();
        player.setDeltaMovement(motion.x(), value, motion.z());
    }

    private static void performEHover(ItemStack stack, Player player) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("enabled"))
            tag.putBoolean("enabled", true);
        if (tag.contains("hover"))
            tag.putBoolean("hover", true);
        player.displayClientMessage(Component.translatable("metaarmor.jetpack.emergency_hover_mode"), true);
        stack.setTag(tag);

        if (!player.level().isClientSide) {
            player.fallDistance = 0;
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
