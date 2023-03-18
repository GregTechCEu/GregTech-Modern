package com.lowdragmc.gtceu.core.mixins;

import com.lowdragmc.gtceu.api.item.IItemUseFirst;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author KilaBash
 * @date 2023/2/24
 * @implNote MultiPlayerGameModeMixin
 */
@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
    @Inject(
            method = {"performUseItemOn"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"
            )},
            cancellable = true
    )
    public void port_lib$useItemOn(LocalPlayer clientPlayerEntity, InteractionHand hand, BlockHitResult blockRayTraceResult, CallbackInfoReturnable<InteractionResult> cir) {
        Item held = clientPlayerEntity.getItemInHand(hand).getItem();
        if (held instanceof IItemUseFirst first) {
            UseOnContext ctx = new UseOnContext(clientPlayerEntity, hand, blockRayTraceResult);
            InteractionResult result = first.onItemUseFirst(clientPlayerEntity.getItemInHand(hand), ctx);
            if (result != InteractionResult.PASS) {
                cir.setReturnValue(result);
            }
        }
    }
}
