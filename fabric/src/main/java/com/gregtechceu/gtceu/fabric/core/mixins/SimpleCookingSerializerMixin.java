package com.gregtechceu.gtceu.fabric.core.mixins;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * @author KilaBash
 * @date 2023/3/27
 * @implNote SimpleCookingSerializer
 */
@Mixin(SimpleCookingSerializer.class)
public abstract class SimpleCookingSerializerMixin<T extends AbstractCookingRecipe> {

    @Shadow @Final private SimpleCookingSerializer.CookieBaker<T> factory;

    @Shadow @Final private int defaultCookingTime;

    @Inject(method = "fromJson(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonObject;)Lnet/minecraft/world/item/crafting/AbstractCookingRecipe;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/util/GsonHelper;getAsString(Lcom/google/gson/JsonObject;Ljava/lang/String;)Ljava/lang/String;",
                    shift = At.Shift.BEFORE), cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD)
    public void gtceu$modifyItem(ResourceLocation recipeId, JsonObject json, CallbackInfoReturnable<T> cir,
                                                                   String group, CookingBookCategory cookingBookCategory, JsonElement jsonElement, Ingredient ingredient) {
        if (json.has("result") && json.get("result").isJsonObject()) {
            ItemStack itemStack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            float exp = GsonHelper.getAsFloat(json, "experience", 0.0F);
            int time = GsonHelper.getAsInt(json, "cookingtime", this.defaultCookingTime);
            cir.setReturnValue(this.factory.create(recipeId, group, cookingBookCategory, ingredient, itemStack, exp, time));
            cir.cancel();
        }
    }

}
