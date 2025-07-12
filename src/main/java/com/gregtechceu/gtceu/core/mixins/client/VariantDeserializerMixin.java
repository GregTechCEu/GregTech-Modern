package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.client.util.VariantRotationHelpers;

import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.util.GsonHelper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Variant.Deserializer.class)
public class VariantDeserializerMixin {

    @ModifyReturnValue(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/renderer/block/model/Variant;",
                       at = @At("RETURN"))
    public Variant gtceu$addZRotation(Variant variant, JsonElement json) {
        JsonObject jsonObject = json.getAsJsonObject();
        if (jsonObject.has(GTBlockstateProvider.Z_ROT_PROPERTY_NAME)) {
            var xRot = GsonHelper.getAsInt(jsonObject, "x", 0);
            var yRot = GsonHelper.getAsInt(jsonObject, "y", 0);
            var zRot = GsonHelper.getAsInt(jsonObject, GTBlockstateProvider.Z_ROT_PROPERTY_NAME, 0);
            return VariantRotationHelpers.rotateVariant(variant, xRot, yRot, zRot);
        }
        return variant;
    }
}
