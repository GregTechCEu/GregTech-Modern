package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.common.module.CreativeFlightModule;
import com.gregtechceu.gtceu.common.module.JetpackModule;
import com.gregtechceu.gtceu.common.module.LiquidFuelJetpackModule;
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

    static boolean isEngineEnabled(ItemStack stack) {
        var modular = GTCapabilityHelper.getModularItem(stack);
        if (modular != null) {
            for (ModuleContext context : modular.getAllModuleInstances()) {
                if (context.getModule() instanceof JetpackModule ||
                        context.getModule() instanceof LiquidFuelJetpackModule) {
                    return context.getModule().isEnabled(context);
                }
            }
        }
        return stack.getTag() != null && stack.getTag().getBoolean("enabled");
    }

    static void setEngineEnabled(ItemStack stack, boolean enabled) {
        var modular = GTCapabilityHelper.getModularItem(stack);
        if (modular != null) {
            for (ModuleContext context : modular.getAllModuleInstances()) {
                if (context.getModule() instanceof JetpackModule ||
                        context.getModule() instanceof LiquidFuelJetpackModule) {
                    context.getModule().setEnabled(context, enabled);
                    return;
                }
            }
        }
        stack.getOrCreateTag().putBoolean("enabled", enabled);
    }

    default boolean updateHoverMode(Player player, ItemStack stack, boolean hover, boolean toggle) {
        var modular = GTCapabilityHelper.getModularItem(stack);
        boolean creativeFlight = modular != null && modular.getAllModuleInstances().stream()
                .anyMatch(context -> context.getModule() instanceof CreativeFlightModule &&
                        context.getModule().isEnabled(context));
        if (!creativeFlight) return toggle != hover;

        // The server owns flight changes; vanilla's ability packet also covers double-tap jump.
        if (toggle && !player.level().isClientSide && !player.isSpectator()) {
            var abilities = player.getAbilities();
            var electric = GTCapabilityHelper.getElectricItem(stack);
            if (abilities.flying || (abilities.mayfly && electric != null &&
                    electric.canUse(CreativeFlightModule.ENERGY_PER_TICK))) {
                abilities.flying = !abilities.flying;
                player.onUpdateAbilities();
            }
        }
        return player.getAbilities().flying;
    }

    default void performFlying(@NotNull Player player, boolean flightEnabled, boolean hover, ItemStack stack) {
        if (player.getAbilities().flying) return;

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
            int sprintCost = (int) Math.round(getEnergyPerUse() * getSprintEnergyModifier());
            boolean sprinting = player.isSprinting() && canUseEnergy(stack, sprintCost);
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

            float horizontalSpeed = (float) (player.isShiftKeyDown() ? getSidewaysSpeed() * 0.5f :
                    getSidewaysSpeed());
            if (sprinting) horizontalSpeed *= (float) getSprintSpeedModifier();

            // Make sure they aren't using elytra movement
            if (!player.isFallFlying() && horizontalSpeed > 0) {
                int forward = (SyncedKeyMappings.VANILLA_FORWARD.isKeyDown(player) ? 1 : 0) -
                        (SyncedKeyMappings.VANILLA_BACKWARD.isKeyDown(player) ? 1 : 0);
                int sideways = (SyncedKeyMappings.VANILLA_LEFT.isKeyDown(player) ? 1 : 0) -
                        (SyncedKeyMappings.VANILLA_RIGHT.isKeyDown(player) ? 1 : 0);
                Vec3 movement = new Vec3(sideways, 0, forward);
                if (movement.lengthSqr() > 0) {
                    movement = movement.normalize().scale(horizontalSpeed);
                    player.moveRelative(horizontalSpeed, movement);
                    if (!editMotion) editMotion = true;
                }
            }

            if (editMotion) {
                int energyUsed = sprinting ? sprintCost : getEnergyPerUse();
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
        player.displayClientMessage(Component.translatable("armor.gtceu.jetpack.emergency_hover"), true);
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
