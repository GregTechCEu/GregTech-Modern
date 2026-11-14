package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.client.model.FaceLayer;
import com.gregtechceu.gtceu.core.util.extensions.BlockElementFaceExt;

import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.util.GsonHelper;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.lang.reflect.Type;

@Mixin(BlockElement.Deserializer.class)
public class BlockElementDeserializerMixin {

    @ModifyReturnValue(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/renderer/block/model/BlockElement;",
                       at = @At("RETURN"))
    private BlockElement gtceu$readElementLayer(BlockElement element, JsonElement json, Type type,
                                                JsonDeserializationContext context) {
        var object = json.getAsJsonObject();
        if (!object.has(FaceLayer.JSON_PROPERTY)) {
            return element;
        }

        String name = GsonHelper.getAsString(object, FaceLayer.JSON_PROPERTY);
        try {
            FaceLayer layer = FaceLayer.fromSerializedName(name);
            element.faces.values().forEach(face -> {
                BlockElementFaceExt extension = (BlockElementFaceExt) (Object) face;
                if (extension.gtceu$getFaceLayer() == FaceLayer.UNCLASSIFIED) {
                    extension.gtceu$setFaceLayer(layer);
                }
            });
            return element;
        } catch (IllegalArgumentException exception) {
            throw new JsonParseException("Unknown GTCEu face layer: " + name, exception);
        }
    }
}
