package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.core.IGTBakedQuad;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidRotation;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = FaceBakery.class, priority = 1500)
public class FaceBakeryMixin {

    @ModifyReturnValue(method = "bakeQuad(Lnet/minecraft/client/resources/model/ModelBaker;Lorg/joml/Vector3fc;Lorg/joml/Vector3fc;Lnet/minecraft/client/resources/model/cuboid/CuboidFace;Lnet/minecraft/client/resources/model/sprite/Material$Baked;Lnet/minecraft/core/Direction;Lnet/minecraft/client/renderer/block/dispatch/ModelState;Lnet/minecraft/client/resources/model/cuboid/CuboidRotation;ZI)Lnet/minecraft/client/resources/model/geometry/BakedQuad;",
                       at = @At(value = "RETURN"))
    private static BakedQuad gtceu$addQuadTextureKey(BakedQuad quad, ModelBaker baker, Vector3fc posFrom,
                                                     Vector3fc posTo, CuboidFace face, Material.Baked material,
                                                     Direction direction, ModelState modelState,
                                                     CuboidRotation rotation, boolean shade, int lightEmission) {
        return ((IGTBakedQuad) (Object) quad).gtceu$setTextureKey(face.texture());
    }
}
