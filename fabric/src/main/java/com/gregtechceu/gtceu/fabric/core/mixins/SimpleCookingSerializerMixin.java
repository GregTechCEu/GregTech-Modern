package com.gregtechceu.gtceu.fabric.core.mixins;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author KilaBash
 * @date 2023/3/27
 * @implNote SimpleCookingSerializer
 */
@Mixin(SimpleCookingSerializer.class)
public abstract class SimpleCookingSerializerMixin {

    @Shadow @Final private SimpleCookingSerializer.CookieBaker factory;

    @Shadow @Final private int defaultCookingTime;

    @Inject(method = "fromJson(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonObject;)Lnet/minecraft/world/item/crafting/AbstractCookingRecipe;",
            at =@At(value = "HEAD"), cancellable = true)
    public void getAppearance(ResourceLocation recipeId, JsonObject json, CallbackInfoReturnable<Recipe> cir) {
        if (json.has("result") && json.get("result").isJsonObject()) {
            String string = GsonHelper.getAsString(json, "group", "");
            JsonElement jsonElement = GsonHelper.isArrayNode(json, "ingredient") ? GsonHelper.getAsJsonArray(json, "ingredient") : GsonHelper.getAsJsonObject(json, "ingredient");
            Ingredient ingredient = Ingredient.fromJson(jsonElement);
            var itemStack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            float f = GsonHelper.getAsFloat(json, "experience", 0.0f);
            int i = GsonHelper.getAsInt(json, "cookingtime", this.defaultCookingTime);
            cir.setReturnValue(this.factory.create(recipeId, string, ingredient, itemStack, f, i));
        }
    }

}
