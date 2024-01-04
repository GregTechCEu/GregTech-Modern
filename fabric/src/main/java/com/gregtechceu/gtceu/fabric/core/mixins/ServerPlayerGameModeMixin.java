package com.gregtechceu.gtceu.fabric.core.mixins;

import com.gregtechceu.gtceu.api.item.fabric.IGTFabricItem;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * @author KilaBash
 * @date 2023/2/24
 * @implNote ServerPlayerGameModeMixin
 */
@Mixin({ServerPlayerGameMode.class})
public class ServerPlayerGameModeMixin {
    @WrapOperation(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;blockActionRestricted(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/GameType;)Z"))
    private boolean gtceu$onBlockStartBreak(ServerPlayer instance, Level level, BlockPos pos, GameType gameType, Operation<Boolean> original) {
        boolean result = original.call(instance, level, pos, gameType);
        ItemStack mainHandItem = instance.getMainHandItem();
        if (mainHandItem.getItem() instanceof IGTFabricItem gtItem) {
            return result || gtItem.onBlockStartBreak(mainHandItem, pos, instance);
        }
        return result;
    }

    @WrapOperation(method = "useItemOn", at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 0),
            @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 1)
    })
    private boolean gtceu$doesSneakBypassUse(ItemStack instance, Operation<Boolean> original, ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult hitResult) {
        boolean isEmpty = original.call(instance);
        if (instance.getItem() instanceof IGTFabricItem gtItem) {
            return isEmpty || gtItem.doesSneakBypassUse(stack, level, hitResult.getBlockPos(), player);
        }
        return isEmpty;
    }
}
