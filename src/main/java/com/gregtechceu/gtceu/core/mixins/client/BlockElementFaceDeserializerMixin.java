package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.client.model.FaceLayer;
import com.gregtechceu.gtceu.core.util.extensions.BlockElementFaceExt;

import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.util.GsonHelper;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.lang.reflect.Type;

@Mixin(BlockElementFace.Deserializer.class)
public class BlockElementFaceDeserializerMixin {

    @ModifyReturnValue(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/renderer/block/model/BlockElementFace;",
                       at = @At("RETURN"))
    private BlockElementFace gtceu$readFaceLayer(BlockElementFace face, JsonElement json, Type type,
                                                 JsonDeserializationContext context) {
        var object = json.getAsJsonObject();
        if (!object.has("gtceu:face_layer")) {
            return face;
        }

        String name = GsonHelper.getAsString(object, "gtceu:face_layer");
        try {
            FaceLayer layer = FaceLayer.fromSerializedName(name);
            return ((BlockElementFaceExt) (Object) face).gtceu$setFaceLayer(layer);
        } catch (IllegalArgumentException exception) {
            throw new JsonParseException("Unknown GTCEu face layer: " + name, exception);
        }
    }
}
