package com.gregtechceu.gtceu.fabric.core.mixins;

import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.core.fabric.RecipeHelpers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShapedRecipe.class)
public class ShapedRecipeMixin {
    @Inject(method = "itemStackFromJson", at = @At("HEAD"), cancellable = true)
    private static void gtceu$customNbtItemStack(JsonObject json, CallbackInfoReturnable<ItemStack> cir) {
        if (json.has("nbt"))
            cir.setReturnValue(RecipeHelpers.getItemStack(json, true, true));
    }
}
