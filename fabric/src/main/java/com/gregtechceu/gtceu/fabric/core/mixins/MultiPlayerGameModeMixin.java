package com.gregtechceu.gtceu.fabric.core.mixins;

import com.gregtechceu.gtceu.api.item.fabric.IGTFabricItem;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void gtceu$onBlockStartBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        ItemStack mainHandItem = this.minecraft.player.getMainHandItem();
        if (mainHandItem.getItem() instanceof IGTFabricItem gtItem && gtItem.onBlockStartBreak(mainHandItem, pos, this.minecraft.player)) {
            cir.setReturnValue(false);
        }
    }

    @WrapOperation(method = "performUseItemOn", at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 0),
            @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 1)
    })
    private boolean gtceu$doesSneakBypassUse(ItemStack instance, Operation<Boolean> original, LocalPlayer player, InteractionHand hand, BlockHitResult hitResult) {
        boolean isEmpty = original.call(instance);
        if (instance.getItem() instanceof IGTFabricItem gtItem) {
            return isEmpty || gtItem.doesSneakBypassUse(instance, player.level(), hitResult.getBlockPos(), player);
        }
        return isEmpty;
    }
}
