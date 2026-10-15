package com.gregtechceu.gtceu.common.capability;

import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.core.mixins.PlayerAccessor;
import com.gregtechceu.gtceu.utils.JetpackHelper;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerJetpackHandler implements ICapabilityProvider {

    @Getter
    private final Player player;
    @Getter
    @Setter
    private boolean jetpackThrusting;
    @Getter
    @Setter
    private boolean precisionHovering;
    @Getter
    @Setter
    private boolean safetyBraking;
    @Nullable
    private Vec3 lastServerTickPosition;

    private final LazyOptional<PlayerJetpackHandler> holder = LazyOptional.of(() -> this);

    public PlayerJetpackHandler(Player player) {
        this.player = player;
    }

    public void reset() {
        this.jetpackThrusting = false;
        this.precisionHovering = false;
        this.safetyBraking = false;
        this.lastServerTickPosition = null;
    }

    public void serverTick() {
        Vec3 tickMotion = this.lastServerTickPosition == null ? Vec3.ZERO :
                player.position().subtract(this.lastServerTickPosition);
        this.lastServerTickPosition = player.position();

        JetpackHelper jetpackHelper = JetpackHelper.getActiveJetpackInstance(player);
        if (jetpackHelper != null && jetpackHelper.isFlyingWithJetpack()) {
            jetpackHelper.serverConsumeEnergy();
            jetpackHelper.serverSpawnEffects(tickMotion);

            // If we are thrusting with jetpack, reset fall distance immediately and stop fall flying
            if (!jetpackHelper.isHoverModeEnabled()) {
                player.resetFallDistance();
                if (player.isFallFlying()) {
                    player.stopFallFlying();
                }
            }

            // Follow vanilla behavior and remove entities on shoulder when thrusting or hovering
            ((PlayerAccessor) player).gtceu$removeEntitiesOnShoulder();
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return GTCapability.CAPABILITY_PLAYER_JETPACK_HANDLER.orEmpty(cap, this.holder);
    }
}
