package com.gregtechceu.gtceu.api.item.jetpack;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public interface IJetpackLogic {

    /** Context for jetpack related functions */
    record JetpackUseContext(Player player, ItemStack stack, EquipmentSlot slot) {}

    /** Defines stats for the jetpack flight behavior */
    record JetpackStats(double horizontalSpeed,
                        double verticalSpeed,
                        double verticalAcceleration,
                        double verticalMomentumFalloff,
                        double altitudeHoldingHorizontalSpeed,
                        double sprintSpeedMultiplier,
                        boolean shouldProduceSoundEmissions,
                        int energyUsagePerTick,
                        int energyUsagePerTickWhenSprinting,
                        @Nullable ParticleOptions flightParticle) {}

    /** @return true if flight is enabled for this jetpack (not if it is possible, just whenever it is enabled) */
    boolean isFlightEnabled(ItemStack stack);

    /** Sets whenever flight is enabled */
    void setFlightEnabled(ItemStack stack, boolean flightEnabled);

    /** @return true whenever hover mode is supported by this jetpack and setHoverModeEnabled is implemented */
    default boolean supportsHoverMode(ItemStack stack) {
        return false;
    }

    /** @return true if hover mode is enabled for this jetpack (not if it is possible, just whenever it is enabled) */
    default boolean isHoverModeEnabled(ItemStack stack) {
        return false;
    }

    /** Sets whenever hover mode is enabled. Should not be called unless supportsHoverMode returns true */
    default void setHoverModeEnabled(ItemStack stack, boolean hoverModeEnabled) {}

    /** @return stats for the jetpack given the context for jetpack use */
    JetpackStats getJetpackStats(JetpackUseContext context);

    /** @return true if the given amount of energy can be used for jetpack */
    boolean canUseEnergy(JetpackUseContext context, int amount);

    /** Drains the given amount of energy from the jetpack */
    void drainEnergy(JetpackUseContext context, int amount);
}
