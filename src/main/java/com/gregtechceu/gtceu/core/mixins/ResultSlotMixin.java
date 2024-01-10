package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.core.ICraftRemainder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResultSlot.class)
public class ResultSlotMixin {

    @Inject(
            method = "onTake",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/crafting/RecipeManager;getRemainingItemsFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Lnet/minecraft/core/NonNullList;"
            )
    )
    private void gtceu$storeCraftingPlayer(Player player, ItemStack stack, CallbackInfo ci) {
        ICraftRemainder.craftingPlayer.set(player);
    }

    @Inject(
            method = "onTake",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/world/item/crafting/RecipeManager;getRemainingItemsFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Lnet/minecraft/core/NonNullList;",
                    shift = At.Shift.AFTER
            )
    )
    private void gtceu$removeCraftingPlayer(Player player, ItemStack stack, CallbackInfo ci) {
        ICraftRemainder.craftingPlayer.remove();
    }
}
