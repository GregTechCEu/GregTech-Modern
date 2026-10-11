package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.utils.JetpackHelper;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends Player {

    public LocalPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }

    // Do not allow crouching while flying with jetpack (this does not prevent other shift down-related behaviors)
    @WrapOperation(method = "aiStep",
                   at = @At(value = "FIELD",
                            target = "Lnet/minecraft/client/player/LocalPlayer;crouching:Z",
                            opcode = Opcodes.PUTFIELD))
    private void gtceu$shouldAllowCrouching(LocalPlayer instance, boolean value, Operation<Void> original) {
        original.call(instance, value && !JetpackHelper.isFlyingWithJetpack(instance));
    }

    // Prevent starting fall flying (elytra flight) when using jetpack
    @WrapOperation(method = "aiStep",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/world/item/ItemStack;canElytraFly(Lnet/minecraft/world/entity/LivingEntity;)Z",
                            remap = false))
    private boolean gtceu$shouldAllowElytraFlight(ItemStack instance, LivingEntity entity,
                                                  Operation<Boolean> original) {
        return original.call(instance, entity) && !JetpackHelper.isFlyingWithJetpack((Player) entity);
    }

    // Do not slide down ladders when hovering with jetpack
    @WrapOperation(method = "isSuppressingSlidingDownLadder",
                   at = @At(value = "FIELD",
                            target = "Lnet/minecraft/world/entity/player/Abilities;flying:Z",
                            opcode = Opcodes.GETFIELD))
    private boolean gtceu$shouldSuppressSlidingDownLadder(Abilities instance, Operation<Boolean> original) {
        return original.call(instance) || JetpackHelper.isHoveringWithJetpack(this);
    }

    // Do not spawn sprint particles when hovering above the ground with jetpack
    @WrapOperation(method = "canSpawnSprintParticle",
                   at = @At(value = "FIELD",
                            target = "Lnet/minecraft/world/entity/player/Abilities;flying:Z",
                            opcode = Opcodes.GETFIELD))
    private boolean gtceu$shouldPreventSpawningSprintParticle(Abilities instance, Operation<Boolean> original) {
        return original.call(instance) || JetpackHelper.isHoveringWithJetpack(this);
    }
}
